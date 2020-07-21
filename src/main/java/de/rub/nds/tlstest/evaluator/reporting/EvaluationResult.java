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
