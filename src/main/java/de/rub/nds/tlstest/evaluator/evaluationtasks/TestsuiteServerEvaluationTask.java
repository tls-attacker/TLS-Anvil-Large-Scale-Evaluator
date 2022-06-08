/**
 * TLS-Anvil-Large-Scale-Evaluator - A tool for executing TLS-Anvil against multiple targets running in Docker containers in parallel
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.evaluator.evaluationtasks;

import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import de.rub.nds.tls.subject.ConnectionRole;
import de.rub.nds.tls.subject.TlsImplementationType;
import de.rub.nds.tls.subject.docker.DockerTlsInstance;
import de.rub.nds.tls.subject.docker.DockerTlsManagerFactory;
import de.rub.nds.tls.subject.docker.DockerTlsManagerFactory.TlsServerInstanceBuilder;
import de.rub.nds.tls.subject.docker.DockerTlsServerInstance;
import de.rub.nds.tlstest.evaluator.Config;
import de.rub.nds.tlstest.evaluator.constants.DockerEntity;
import static de.rub.nds.tlstest.evaluator.evaluationtasks.EvaluationTask.DOCKER;
import static de.rub.nds.tlstest.evaluator.evaluationtasks.EvaluationTask.LOGGER;

import java.nio.file.FileSystems;

import java.util.LinkedList;


public class TestsuiteServerEvaluationTask extends EvaluationTask {
    private String networkName;
    private String networkId;
    private String targetHostname;
    private String targetPort;

    private String createNetwork() throws Exception {
        return DOCKER.createNetworkCmd()
                .withAttachable(true)
                .withName(networkName)
                .exec().getId();
    }

    private String createTestsuiteContainer() throws Exception {
        String mountPath = FileSystems.getDefault().getPath(Config.getInstance().getOutputFolder() + "/" + imageName).toString();
        Volume volume = new Volume("/output");
        String image = Config.getInstance().getTestsuiteImage();
        return DOCKER.createContainerCmd(image)
                .withName("Testsuite-" + hostName)
                .withEnv("LogFilename=" + imageName)
                .withCmd("-outputFolder", "./",
                        "-parallelHandshakes", "1",
                        "-parallelTests", "3",
                        "-strength", Integer.toString(Config.getInstance().getStrength()),
                        "-timeoutActionScript", "curl", "--connect-timeout", "2", targetHostname + ":8090/shutdown",
                        "-restartServerAfter", Integer.toString(Config.getInstance().getRestartServerAfter()),
                        "server",
                        "-connect", targetHostname + ":" + targetPort,
                        "-doNotSendSNIExtension")
                .withHostConfig(HostConfig.newHostConfig()
                        .withNetworkMode(networkId)
                        .withBinds(new Bind(mountPath, volume))
                        .withMemory(Config.getInstance().getContainerRAM() * 1000 * 1000 * 1000L)
                        //.withNanoCPUs(1000000000L * 12)
                )
                .exec().getId();
    }

    private DockerTlsInstance createTargetContainer() {
        
        try {
            TlsServerInstanceBuilder targetInstanceBuilder = DockerTlsManagerFactory.getTlsServerBuilder(imageImplementation, imageVersion);
            targetInstanceBuilder = addTargetSpecificFlags(targetInstanceBuilder, imageImplementation);
            DockerTlsServerInstance targetInstance = targetInstanceBuilder.containerName(targetHostname).port(4433).hostname("0.0.0.0").ip("0.0.0.0").parallelize(true)
                    .hostConfigHook(hostConfig -> {
                        hostConfig.withPortBindings(new LinkedList<>()).withNetworkMode(networkId);
                        return hostConfig;
                    }).build();
            targetPort = DockerTlsManagerFactory.retrieveImageProperties(ConnectionRole.SERVER, imageImplementation).getInternalPort().toString();
            targetInstance.ensureContainerExists();
            return targetInstance;
        } catch (DockerException | InterruptedException ex) {
            throw new RuntimeException("Failed to create target instance");
        }
    }
    
    private TlsServerInstanceBuilder addTargetSpecificFlags(TlsServerInstanceBuilder instanceBuilder, TlsImplementationType imageImplementation) {
        if(imageImplementation == TlsImplementationType.WOLFSSL) {
            instanceBuilder = instanceBuilder.additionalParameters("-v d");
        } else if(imageImplementation == TlsImplementationType.MATRIXSSL) {
            instanceBuilder = instanceBuilder.additionalParameters("-W 3,4");
        }
        return instanceBuilder;
    }

    @Override
    public int execute() throws Exception {
        targetHostname = "target-" + hostName;
        networkName = imageName + "_network";

        networkId = createNetwork();
        cleanupService.addEntityToCleanUp(DockerEntity.NETWORK, networkId);

        DockerTlsInstance targetInstance = createTargetContainer();
        cleanupService.addEntityToCleanUp(DockerEntity.CONTAINER, targetInstance.getId());
        targetInstance.start();
        LOGGER.debug(targetInstance.getId() + " container started!");

        try {
            Thread.sleep(10000);
        } catch (Exception ignored) {}

        int failedCount = 0;
        while (!DOCKER.inspectContainerCmd(targetInstance.getId()).exec().getState().getRunning()) {
            failedCount += 1;
            if (failedCount > 10) {
                throw new RuntimeException(targetInstance.getId()+ " could not start successfully!");
            }
            try {
                Thread.sleep(1000);
                targetInstance.start();
            } catch (Exception ignored) {
            }
        }

        String testsuiteContainerId = createTestsuiteContainer();
        cleanupService.addEntityToCleanUp(DockerEntity.CONTAINER, testsuiteContainerId);
        DOCKER.startContainerCmd(testsuiteContainerId).exec();

        LOGGER.info("Waiting for testsuite " + imageName + " to finish");

        new Thread(() -> {
            while (!finished) {
                try {
                    if (!DOCKER.inspectContainerCmd(targetInstance.getId()).exec().getState().getRunning()) {
                        DOCKER.startContainerCmd(targetInstance.getId()).exec();
                    }
                } catch (Exception e) {
                    LOGGER.warn("DOCKER error...", e);
                }

                try {
                    Thread.sleep(500);
                } catch (Exception ignored) { }
            }
        }).start();

        new Thread(() -> {
            while (!finished) {
                LOGGER.debug("Still waiting for container " + getUnRandomizedImageName() + "(" + testsuiteContainerId + ")");
                try {
                    Thread.sleep(30000);
                } catch (Exception e) {}
            }
        }).start();

        //waitForContainerToFinish(testsuiteContainerId);
        int exitCode = DOCKER.waitContainerCmd(testsuiteContainerId).exec(new WaitContainerResultCallback()).awaitStatusCode();
        LOGGER.info("Testsuite for " + imageName + " finished and exited with status code " + exitCode);
        return exitCode;
    }
}
