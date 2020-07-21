package de.rub.nds.tlstest.evaluator.evaluationtasks;

import de.rub.nds.tlstest.evaluator.Config;
import de.rub.nds.tlstest.evaluator.constants.ImplementationModeType;

public class EvaluationTaskFactory {

    public static EvaluationTask forMode(ImplementationModeType role) {
        switch (Config.getInstance().getEvaluator()) {
            case TESTSUITE:
                switch (role) {
                    case SERVER:
                        return new TestsuiteServerEvaluationTask();
                    case CLIENT:
                        return new TestsuiteClientEvaluationTask();
                }
        }

        throw new UnsupportedOperationException("EvaluationTaskType or ConnectionRole not known");
    }

}
