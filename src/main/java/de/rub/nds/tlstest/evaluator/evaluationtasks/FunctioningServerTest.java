package de.rub.nds.tlstest.evaluator.evaluationtasks;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import de.rub.nds.tls.subject.docker.DockerTlsInstance;
import de.rub.nds.tlstest.evaluator.Config;
import de.rub.nds.tlstest.evaluator.constants.DockerEntity;

import java.nio.file.FileSystems;
import java.util.HashMap;

public class FunctioningServerTest extends EvaluationTask {
    private String networkName;
    private String networkId;
    private String targetHostname;
    private String targetPort;

    private String createNetwork() throws Exception {
        return DOCKER.createNetwork(NetworkConfig.builder()
                .attachable(true)
                .name(networkName)
                .build()).id();
    }

    private String createTestClientContainer() throws Exception {
        String mountPath = FileSystems.getDefault().getPath(Config.getInstance().getOutputFolder() + "/" + imageName).toString();

        return DOCKER.createContainer(ContainerConfig.builder()
                .image("basic-client:latest")
                .env("TARGET=" + targetHostname)
                .cmd("-keylogfile", "./keyfile.log",
                        "-connect", targetHostname + ":" + targetPort
                )
                .hostConfig(HostConfig.builder()
                        .networkMode(networkId)
                        .appendBinds(mountPath + ":/output")
                        .build())
                .build(), "BasicClient-" + hostName).id();
    }

    private DockerTlsInstance createTargetContainer() {
        DockerTlsInstance targetInstance = (DockerTlsInstance)dockermanager.getTlsServer(imageImplementation, imageVersion, 4433);
        targetInstance.setName(targetHostname);
        targetInstance.getHostInfo().setHostname("0.0.0.0");
        targetInstance.getHostInfo().setIp("0.0.0.0");
        targetInstance.setParallelize(true);

        targetPort = targetInstance.getImageProperties().getInternalPort().toString();

        ContainerConfig targetConfig = targetInstance.getContainerConfig();
        HostConfig hostConfig = targetConfig.hostConfig();
        if (hostConfig == null)
            hostConfig = HostConfig.builder().build();

        targetInstance.setContainerConfig(targetConfig.toBuilder()
                .hostConfig(hostConfig.toBuilder()
                        .portBindings(new HashMap<>())
                        .networkMode(networkId)
                        .build())
                .exposedPorts()
                .build()
        );

        targetInstance.createContainer();
        return targetInstance;
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
        LOGGER.debug(targetInstance.getName() + " container started!");

        try {
            Thread.sleep(10000);
        } catch (Exception ignored) {}


        String testsuiteContainerId = createTestClientContainer();
        cleanupService.addEntityToCleanUp(DockerEntity.CONTAINER, testsuiteContainerId);
        DOCKER.startContainer(testsuiteContainerId);

        new Thread(() -> {
            while (!finished) {
                try {
                    if (!DOCKER.inspectContainer(targetInstance.getId()).state().running()) {
                        DOCKER.startContainer(targetInstance.getId());
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
        ContainerExit exit = DOCKER.waitContainer(testsuiteContainerId);

        int exitCode = exit.statusCode().intValue();
        if (exitCode != 0) {
            LOGGER.error("FunctionTest for " + imageName + " finishd and exited with status code " + exitCode);
        }

        return exitCode;
    }
}
