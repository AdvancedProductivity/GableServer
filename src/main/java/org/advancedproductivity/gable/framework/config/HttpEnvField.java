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
public interface HttpEnvField {
    String HOST_REPLACE = "host_replace";
    String PROTOCOL_REPLACE = "protocol_replace";
    String PORT_REPLACE = "port_replace";
    String PATH_PRE_APPEND = "path_pre_append";
    String HEADER_REPLACE_OR_ADD = "header_replace_or_add";
    String AUTH_PARAM_REPLACE = "auth_param_replace";
}
