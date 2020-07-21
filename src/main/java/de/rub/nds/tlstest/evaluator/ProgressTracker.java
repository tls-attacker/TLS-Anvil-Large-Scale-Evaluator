package de.rub.nds.tlstest.evaluator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rub.nds.tlstest.evaluator.evaluationtasks.EvaluationTask;
import de.rub.nds.tlstest.evaluator.reporting.EvaluationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProgressTracker {
    private static final Logger LOGGER = LogManager.getLogger();

    private int totalTasks = 0;
    private int finishedTasks = 0;

    private static ProgressTracker instance = null;

    private final List<EvaluationResult> evaluationResultList = new ArrayList<>();

    public static ProgressTracker getInstance() {
        if (instance == null) {
            instance = new ProgressTracker();
        }
        return instance;
    }

    ProgressTracker() {

    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getFinishedTasks() {
        return finishedTasks;
    }

    public List<EvaluationResult> getEvaluationResultList() {
        return evaluationResultList;
    }

    public void taskFinished(EvaluationTask task, int exitcode) {
        finishedTasks += 1;
        LOGGER.info(String.format("Finished %d/%d tasks (%s, %d)", finishedTasks, totalTasks, task.getUnRandomizedImageName(), exitcode));
        evaluationResultList.add(new EvaluationResult(task.getUnRandomizedImageName(), exitcode));
        this.createReport();
    }

    public void createReport() {
        ObjectMapper mapper = new ObjectMapper();
        File f = new File(Config.getInstance().getOutputFolder() + "/evaluationResult.json");
        f.getParentFile().mkdirs();

        try {
            f.createNewFile();
            mapper.writeValue(f, evaluationResultList);
        } catch (Exception e) {
            LOGGER.error("Serialization of evaluation results failed", e);
        }
    }
}
