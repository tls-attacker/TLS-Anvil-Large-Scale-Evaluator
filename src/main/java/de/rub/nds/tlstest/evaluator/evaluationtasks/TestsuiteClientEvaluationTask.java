package de.rub.nds.tlstest.evaluator.evaluationtasks;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import de.rub.nds.tls.subject.docker.DockerTlsInstance;
import de.rub.nds.tlstest.evaluator.Config;
import de.rub.nds.tlstest.evaluator.constants.DockerEntity;

import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestsuiteClientEvaluationTask extends EvaluationTask {
    private String networkId;
    private String targetHostname;

    private String createNetwork(String networkName) throws Exception {
        return DOCKER.createNetwork(NetworkConfig.builder()
                .attachable(true)
                .name(networkName)
                .build()).id();
    }

    private String createTestsuiteContainer() throws Exception {
        String mountPath = FileSystems.getDefault().getPath(Config.getInstance().getOutputFolder() + "/" + imageName).toString();

        return DOCKER.createContainer(ContainerConfig.builder()
                .image("testsuite:latest")
                .env("LogFilename=" + imageName)
                .cmd("-outputFile", "./",
                        "-keylogfile", "./keyfile.log",
                        "client",
                        "-port", "443",
                        "-wakeupScript", "curl", "--connect-timeout", "2", targetHostname + ":8090/trigger")
                .hostConfig(HostConfig.builder()
                        .networkMode(networkId)
                        .appendBinds(mountPath + ":/output")
                        .memory(4 * 1000 * 1000 * 1000L)
                        .build()
                )
                .build(), "Testsuite-" + hostName).id();
    }

    private DockerTlsInstance createTargetContainer(String ipAddress) {
        DockerTlsInstance targetInstance = (DockerTlsInstance)dockermanager.getTlsClient(imageImplementation, imageVersion, ipAddress, 443);
        targetInstance.setInsecureConnection(true);
        targetInstance.setName(targetHostname);

        ContainerConfig targetConfig = targetInstance.getContainerConfig();
        if (targetInstance.getImage().labels().get("tls_implementation").equals("rustls")) {
            targetInstance.getHostInfo().setHostname("Testsuite-" + hostName);
        }

        HostConfig hostConfig = targetConfig.hostConfig();
        if (hostConfig == null)
            hostConfig = HostConfig.builder().build();

        targetInstance.setContainerConfig(targetConfig.toBuilder()
                .hostConfig(hostConfig.toBuilder()
                        .extraHosts()
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
        String networkName = imageName + "_network";

        networkId = createNetwork(networkName);
        cleanupService.addEntityToCleanUp(DockerEntity.NETWORK, networkId);

        String testsuiteContainerId = createTestsuiteContainer();
        cleanupService.addEntityToCleanUp(DockerEntity.CONTAINER, testsuiteContainerId);
        DOCKER.startContainer(testsuiteContainerId);
        LOGGER.debug("Testsuite_" + imageName + " container started!");

        String testsuiteIp = DOCKER.inspectContainer(testsuiteContainerId).networkSettings().networks().get(networkName).ipAddress();
        DockerTlsInstance targetInstance = createTargetContainer(testsuiteIp);
        cleanupService.addEntityToCleanUp(DockerEntity.CONTAINER, targetInstance.getId());
        targetInstance.start();

        LOGGER.info("Waiting for testsuite " + imageName + " to finish");
        new Thread(() -> {
            while (!finished) {
                LOGGER.debug("Still waiting for container " + getUnRandomizedImageName() + "(" + testsuiteContainerId + ")");
                try {
                    Thread.sleep(30000);
                } catch (Exception e) {}
            }
        }).start();

        ContainerExit exit = DOCKER.waitContainer(testsuiteContainerId);

        int exitCode = exit.statusCode().intValue();
        LOGGER.info("Testsuite for " + imageName + " finishd and exited with status code " + exitCode);
        return exitCode;
    }
}
