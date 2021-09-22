package org.advancedproductivity.gable.framework.runner;

import org.advancedproductivity.gable.framework.core.TestType;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzq
 */
public class RunnerHolder {

    public static final ConcurrentHashMap<String, TestAction> HOLDER = new ConcurrentHashMap<>();

    static {
        HttpRunner httpRunner = new HttpRunner();
        HOLDER.put(httpRunner.getTestType().name(), httpRunner);
    }
}
