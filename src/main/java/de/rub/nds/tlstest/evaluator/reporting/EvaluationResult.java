/**
 * TLS-Anvil-Large-Scale-Evaluator - A tool for executing TLS-Anvil against multiple targets running in Docker containers in parallel
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.evaluator.reporting;

public class EvaluationResult {
    private String imageName;
    private int exitCode;

    private EvaluationResult() {

    }

    public EvaluationResult(String imageName, int exitCode) {
        this.imageName = imageName;
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getImageName() {
        return imageName;
    }
}
