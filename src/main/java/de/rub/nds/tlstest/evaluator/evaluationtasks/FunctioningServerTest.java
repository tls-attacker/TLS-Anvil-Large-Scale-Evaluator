package de.rub.nds.tlstest.evaluator.evaluationtasks;

import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import de.rub.nds.tls.subject.ConnectionRole;
import de.rub.nds.tls.subject.docker.DockerTlsInstance;
import de.rub.nds.tls.subject.docker.DockerTlsManagerFactory;
import de.rub.nds.tls.subject.docker.DockerTlsServerInstance;
import de.rub.nds.tlstest.evaluator.Config;
import de.rub.nds.tlstest.evaluator.constants.DockerEntity;
import static de.rub.nds.tlstest.evaluator.evaluationtasks.EvaluationTask.DOCKER;
import static de.rub.nds.tlstest.evaluator.evaluationtasks.EvaluationTask.LOGGER;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FunctioningServerTest extends EvaluationTask {
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

    private String createTestClientContainer() throws Exception {
        String mountPath = FileSystems.getDefault().getPath(Config.getInstance().getOutputFolder() + "/" + imageName).toString();
        Volume volume = new Volume("/output");
        return DOCKER.createContainerCmd("basic-client:latest")
                .withEnv("TARGET=" + targetHostname)
                .withCmd("-keylogfile", "./keyfile.log",
                        "-connect", targetHostname + ":" + targetPort)
                .withHostConfig(HostConfig.newHostConfig()
                        .withNetworkMode(networkId)
                        .withBinds(new Bind(mountPath, volume)))
                .withName("BasicClient-" + hostName).exec().getId();
    }

    private DockerTlsInstance createTargetContainer() {
        try {
            DockerTlsManagerFactory.TlsServerInstanceBuilder targetInstanceBuilder = DockerTlsManagerFactory.getTlsServerBuilder(imageImplementation, imageVersion);
            DockerTlsServerInstance targetInstance = targetInstanceBuilder.containerName(targetHostname).port(4433).hostname("0.0.0.0").ip("0.0.0.0").parallelize(true)
                    .hostConfigHook(hostConfig -> {
                        hostConfig.withPortBindings(new LinkedList<>()).withNetworkMode(networkId);
                        return hostConfig;
                    }).build();
            targetPort = DockerTlsManagerFactory.retrieveImageProperties(ConnectionRole.SERVER, imageImplementation).getInternalPort().toString();
            return targetInstance;
        } catch (DockerException | InterruptedException ex) {
            throw new RuntimeException("Failed to create test target container");
        }
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
        LOGGER.debug(targetInstance.getContainerName() + " container started!");

        try {
            Thread.sleep(10000);
        } catch (Exception ignored) {}


        String testsuiteContainerId = createTestClientContainer();
        cleanupService.addEntityToCleanUp(DockerEntity.CONTAINER, testsuiteContainerId);
        DOCKER.startContainerCmd(testsuiteContainerId).exec();

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
        if (exitCode != 0) {
            LOGGER.error("FunctionTest for " + imageName + " finished and exited with status code " + exitCode);
        }

        return exitCode;
    }
}
