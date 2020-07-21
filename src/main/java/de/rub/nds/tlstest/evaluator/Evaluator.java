package de.rub.nds.tlstest.evaluator;

import com.spotify.docker.client.messages.Image;
import de.rub.nds.tlstest.evaluator.constants.ImplementationModeType;
import de.rub.nds.tlstest.evaluator.evaluationtasks.EvaluationTask;
import de.rub.nds.tlstest.evaluator.evaluationtasks.EvaluationTaskFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Evaluator {
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<Image> clientImages;
    private final List<Image> serverImages;
    private final ThreadPoolExecutor executor;

    public Evaluator(List<Image> clientImages, List<Image> serverImages) {
        this.clientImages = clientImages;
        this.serverImages = serverImages;

        int size = Config.getInstance().getParallel();
        this.executor = new ThreadPoolExecutor(size, size, 10, TimeUnit.DAYS, new LinkedBlockingDeque<Runnable>());
    }


    public void start() {
        List<Future<?>> futures = new ArrayList<>();

        for (Image i : clientImages) {
            EvaluationTask task = EvaluationTaskFactory.forMode(ImplementationModeType.CLIENT);
            task.setImageToEvaluate(i);
            LOGGER.debug("Schedule test for image " + i.repoTags().get(0));
            futures.add(executor.submit(task));
        }

        for (Image i : serverImages) {
            EvaluationTask task = EvaluationTaskFactory.forMode(ImplementationModeType.SERVER);
            task.setImageToEvaluate(i);
            LOGGER.debug("Schedule test for image " + i.repoTags().get(0));
            futures.add(executor.submit(task));
        }

        ProgressTracker.getInstance().setTotalTasks(futures.size());
        LOGGER.info(String.format("Starting %d tasks", futures.size()));

        for (Future<?> i : futures) {
            try {
                i.get();
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }

        LOGGER.info("Evaluator finished");
        executor.shutdownNow();
        ProgressTracker.getInstance().createReport();
    }
}
