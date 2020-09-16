/**
 * TLS-Testsuite-Large-Scale-Evaluator - A tool for executing the TLS-Testsuite against multiple targets running in Docker containers in parallel
 *
 * Copyright 2020 Ruhr University Bochum and
 * TÜV Informationstechnik GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
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