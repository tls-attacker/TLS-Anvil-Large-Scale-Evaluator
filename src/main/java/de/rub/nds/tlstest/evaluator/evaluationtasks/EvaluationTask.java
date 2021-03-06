/**
 * TLS-Anvil-Large-Scale-Evaluator - A tool for executing TLS-Anvil against multiple targets running in Docker containers in parallel
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.evaluator .evaluationtasks;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import de.rub.nds.tls.subject.TlsImplementationType;
import de.rub.nds.tls.subject.constants.TlsImageLabels;
import de.rub.nds.tls.subject.docker.DockerClientManager;
import de.rub.nds.tlstest.evaluator.Config;
import de.rub.nds.tlstest.evaluator.DockerCleanupService;
import de.rub.nds.tlstest.evaluator.ProgressTracker;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

abstract public class EvaluationTask implements Runnable {

    protected static final Logger LOGGER = LogManager.getLogger();
    protected static final DockerClient DOCKER = DockerClientManager.getDockerClient();
    private static final int RANDOM_LENGTH = 5;
    public final static String VOLUME_PATH = "/output";

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
        Map<String, String> labels = image.getLabels();
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
    
    protected List<String> getFilteredTestCommands() {
        LinkedList<String> commands = new LinkedList<>();
        if(Config.getInstance().getTestPackage() != null) {
            commands.add("-testPackage");
            commands.add(Config.getInstance().getTestPackage());
        }
        if(Config.getInstance().getTestTags() != null) {
            commands.add("-tags");
            commands.add(Config.getInstance().getTestTags());
        }
        return commands;
    }

    public void waitForContainerToFinish(String id) {
        boolean finished = false;
        int waitCounter = 0;
        while (!finished) {
            try {
                boolean isRunning = DOCKER.inspectContainerCmd(id).exec().getState().getRunning();
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
