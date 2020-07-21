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
