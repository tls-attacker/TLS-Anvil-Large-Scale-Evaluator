package de.rub.nds.tlstest.evaluator;

import de.rub.nds.tlstest.evaluator.evaluationtasks.TestsuiteServerEvaluationTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressTrackerTest {
    private ProgressTracker tracker;

    @BeforeEach
    public void setup() {
        tracker = new ProgressTracker();
    }

    @Test
    public void finishTask() {
        assertEquals(0, tracker.getFinishedTasks());
        tracker.taskFinished(new TestsuiteServerEvaluationTask(), 10);

        assertEquals(1, tracker.getFinishedTasks());
        assertEquals(1, tracker.getEvaluationResultList().size());
    }

}