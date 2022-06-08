/**
 * TLS-Anvil-Large-Scale-Evaluator - A tool for executing TLS-Anvil against multiple targets running in Docker containers in parallel
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
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
                break;
            case FUNCTIONINGTEST:
                switch (role) {
                    case SERVER:
                        return new FunctioningServerTest();
                    case CLIENT:
                        throw new UnsupportedOperationException("Not available yet");
                }
                break;
        }

        throw new UnsupportedOperationException("EvaluationTaskType or ConnectionRole not known");
    }

}
