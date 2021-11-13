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

package org.advancedproductivity.gable.framework.config;

/**
 * @author zzq
 */
public interface CaseField {
    String DIFF = "diff";
    String JSON_SCHEMA = "jsonSchema";
    String HEADERS = "headers";
    String RECORD = "record";
    String VERSION = "version";
    String ID = "id";
    String TITLE = "title";
    String CHINESE_TITLE = "用例标题";
    String ALL_FIELD = "allField";
    String IN = "in";
    String DIFF_REPLACE = "replace";
    String DIFF_ADD = "add";
    String DIFF_REMOVE = "remove";
    String DIFF_REMOVE_BY_INDEX = "removeByIndex";
    String ARRAY_ADD_FIRST = "addFirst";
    String ARRAY_ADD_LAST = "addLast";
    String CASE_TITLE = "gable_title";
}
