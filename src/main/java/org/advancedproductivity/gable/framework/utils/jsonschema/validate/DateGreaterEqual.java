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

package org.advancedproductivity.gable.framework.utils.jsonschema.validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import org.advancedproductivity.gable.framework.utils.jsonschema.JsonSchemaErrorCode;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;

/**
 * @author zzq
 */
public class DateGreaterEqual  extends AbstractKeyword  {

    public DateGreaterEqual() {
        super("dateGE");
    }

    private static final class Validator extends DateValidator {

        public Validator(String keyword,String schema) {
            super(keyword, schema);
        }

        @Override
        protected Set<ValidationMessage> validateTwoDate(Date defineDate, Date jsonDate, String at,
                                                         String defineTimeStr, String jsonTimeStr) {
            long jsonTime = jsonDate.getTime();
            long defineTime = defineDate.getTime();
            if (jsonTime >= defineTime) {
                return pass();
            }
            return fail(CustomErrorMessageType.of(JsonSchemaErrorCode.DATE_LESS_THAN_ERROR,
                    new MessageFormat("{0} must be later than or equal to {1}, but actual are {2}")), at, defineTimeStr, jsonTimeStr);
        }
    }

    @Override
    public JsonValidator newValidator(String schemaPath,
                                      JsonNode schemaNode,
                                      JsonSchema parentSchema,
                                      ValidationContext validationContext) throws JsonSchemaException, Exception {
        return new Validator(getValue(), schemaNode.asText());
    }
}
