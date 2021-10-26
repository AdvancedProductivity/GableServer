package org.advancedproductivity.gable.framework.config;

/**
 * @author zzq
 */
public enum IntegrateStepStatus {
    NOT_RUN(0),
    RUNNING(1),
    SUCCESS(2),
    FAILED(3);

    private int value;

    IntegrateStepStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
