/*
 *  Copyright (c) 2021 AdvancedProductivity
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.advancedproductivity.gable.framework.runner;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzq
 */
public class RunnerHolder {

    public static final ConcurrentHashMap<String, TestAction> HOLDER = new ConcurrentHashMap<>();

    static {
        HttpRunner httpRunner = new HttpRunner();
        GroovyCodeRunner groovyCodeRunner = new GroovyCodeRunner();
        HOLDER.put(httpRunner.getTestType().name(), httpRunner);
        HOLDER.put(groovyCodeRunner.getTestType().name(), groovyCodeRunner);
    }
}
