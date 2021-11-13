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
public interface ConfigField {
    String CONFIG_DEFINE_FILE_NAME = "define.json";
    String UUID = "uuid";
    String TEST_TYPE = "type";
    String DETAIL = "config";
    String VERSION = "version";
    String HTTP_METHOD = "method";
    String HTTP_PROTOCOL = "protocol";
    String HTTP_HOST = "host";
    String HTTP_PORT = "port";
    String HTTP_PATH = "path";
    String HTTP_QUERY = "query";
    String HTTP_BODY = "body";
    String HTTP_BODY_TYPE = "type";
    String HTTP_BODY_CONTENT = "content";
    String HTTP_BODY_FORM_DATA = "form_data";
    String URLENCIDED = "urlencoded";
    String HTTP_HEADER = "header";
    String HTTP_AUTH = "auth";
    String HTTP_AUTH_TYPE = "type";
    String HTTP_AUTH_PARAM = "param";
    String HTTP_FORM_FIELD = "file";
    String HTTP_KEY_VALUE_TYPE = "type";

    String ENV_TYPE = "typeName";
    String ENV_NAME = "name";

    String IS_UNMODIFY = "isUnmodify";
    String FILE_CENTER = "file_center";

    String STATUS = "status";
    String START_TIME = "startTime";
    String END_TIME = "endTime";
    String HISTORY_ID = "historyId";
    String PRE_SCRIPT = "preScript";
    String POST_SCRIPT = "postScript";
}
