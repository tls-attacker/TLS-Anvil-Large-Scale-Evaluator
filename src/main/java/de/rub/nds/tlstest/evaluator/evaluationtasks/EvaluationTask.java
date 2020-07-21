package de.rub.nds.tlstest.evaluator .evaluationtasks;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;
import de.rub.nds.tls.subject.TlsImplementationType;
import de.rub.nds.tls.subject.constants.TlsImageLabels;
import de.rub.nds.tls.subject.docker.DockerTlsManagerFactory;
import de.rub.nds.tlstest.evaluator.DockerCleanupService;
import de.rub.nds.tlstest.evaluator.ProgressTracker;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

abstract public class EvaluationTask implements Runnable {

    protected static final Logger LOGGER = LogManager.getLogger();
    protected final DockerClient DOCKER = new DefaultDockerClient("unix:///var/run/docker.sock");
    protected static final DockerTlsManagerFactory dockermanager = new DockerTlsManagerFactory();
    private static final int RANDOM_LENGTH = 5;

    protected Image image;
    protected String imageName;
    protected String hostName;
    protected String imageVersion;
    protected TlsImplementationType imageImplementation;

    protected DockerCleanupService cleanupService = new DockerCleanupService();
    protected boolean finished = false;

    abstract public int execute() throws Exception;

    public void run() {
        int exitCode = -1;
        try {
            exitCode = this.execute();
        } catch (Exception e) {
            LOGGER.error("Could not execute task:", e);
        }

        finished = true;
        ProgressTracker.getInstance().taskFinished(this, exitCode);
        cleanupService.cleanup();
    }

    public void setImageToEvaluate(Image image) {
        this.image = image;
        Map<String, String> labels = image.labels();
        imageVersion = labels.get(TlsImageLabels.VERSION.getLabelName());
        String implementation = labels.get(TlsImageLabels.IMPLEMENTATION.getLabelName());

        imageName = String.format("%s-%s-%s-%s",
                implementation,
                labels.get(TlsImageLabels.CONNECTION_ROLE.getLabelName()),
                imageVersion,
                RandomStringUtils.randomAlphanumeric(RANDOM_LENGTH)
        );
        hostName = imageName.replace(".", "").replace("_", "-");

        imageImplementation = TlsImplementationType.fromString(implementation);
        if (imageImplementation == null) {
            LOGGER.error("Unknown implementation type!");
            throw new RuntimeException("Unknown implementation type!");
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void waitForContainerToFinish(String id) {
        boolean finished = false;
        int waitCounter = 0;
        while (!finished) {
            try {
                boolean isRunning = DOCKER.inspectContainer(id).state().running();
                finished = !isRunning;
                Thread.sleep(5000);
                waitCounter++;
                if (waitCounter % 6 == 0) {
                    waitCounter = 0;
                    LOGGER.debug("Still waiting for " + getUnRandomizedImageName() + " to finish... (" + id + ")");
                }
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                LOGGER.warn("DOCKER error...", e);
            }
        }

        LOGGER.debug("Waiting finished for " + getUnRandomizedImageName() + " finished (" + id + ")");
    }

    public String getUnRandomizedImageName() {
        return imageName.substring(0, imageName.length() - RANDOM_LENGTH - 1);
    }
}
