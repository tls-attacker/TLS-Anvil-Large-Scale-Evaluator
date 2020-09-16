/**
 * TLS-Testsuite-Large-Scale-Evaluator - A tool for executing the TLS-Testsuite against multiple targets running in Docker containers in parallel
 *
 * Copyright 2020 Ruhr University Bochum and
 * TÜV Informationstechnik GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.evaluator.constants;

public enum EvaluationTaskType {
    TESTSUITE;

    public static EvaluationTaskType fromString(String input) {
        for (EvaluationTaskType output : EvaluationTaskType.values()) {
            if (output.toString().toLowerCase().equals(input.toLowerCase())) {
                return output;
            }
        }
        return null;
    }
}
