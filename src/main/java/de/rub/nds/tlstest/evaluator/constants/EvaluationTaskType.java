/**
 * TLS-Anvil-Large-Scale-Evaluator - A tool for executing TLS-Anvil against multiple targets running in Docker containers in parallel
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.evaluator.constants;

public enum EvaluationTaskType {
    TESTSUITE,
    FUNCTIONINGTEST;

    public static EvaluationTaskType fromString(String input) {
        for (EvaluationTaskType output : EvaluationTaskType.values()) {
            if (output.toString().toLowerCase().equals(input.toLowerCase())) {
                return output;
            }
        }
        return null;
    }
}
