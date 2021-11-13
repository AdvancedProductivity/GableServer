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
import com.networknt.schema.AbstractJsonValidator;
import com.networknt.schema.CustomErrorMessageType;
import com.networknt.schema.ValidationMessage;
import org.advancedproductivity.gable.framework.utils.DateFormatHolder;
import org.advancedproductivity.gable.framework.utils.jsonschema.JsonSchemaErrorCode;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * @author zzq
 */
public abstract class DateValidator extends AbstractJsonValidator {
    private String schemaExpress;

    public DateValidator(String keyword,String schema) {
        super(keyword);
        this.schemaExpress = schema;
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        if (!node.isTextual()) {
            return pass();
        }
        String jsonDateStr = node.asText();
        if (StringUtils.isEmpty(this.schemaExpress)) {
            return fail(CustomErrorMessageType.of(JsonSchemaErrorCode.EXPRESS_EMPTY,
                    new MessageFormat("{0} the format define can not be empty")), at);
        }
        if (StringUtils.isEmpty(jsonDateStr)) {
            return fail(CustomErrorMessageType.of(JsonSchemaErrorCode.JSON_VALUE_EMPTY,
                    new MessageFormat("{0} the json value is empty")), at);
        }
        String[] param = StringUtils.split(this.schemaExpress, "_#_");
        if (param == null || param.length != 3) {
            return fail(CustomErrorMessageType.of(JsonSchemaErrorCode.PARAM_ERROR,
                    new MessageFormat("{0} the format define can not be empty")), at);
        }else {
            for (int i = 0; i < param.length; i++) {
                param[i] = StringUtils.trim(param[i]);
            }
        }
        SimpleDateFormat selfFormat = DateFormatHolder.getInstance(param[1]);
        SimpleDateFormat jsonFormat = DateFormatHolder.getInstance(param[2]);
        try {
            Date defineDate = selfFormat.parse(param[0]);
            Date jsonDate = jsonFormat.parse(jsonDateStr);
            return validateTwoDate(defineDate, jsonDate, at, param[0], jsonDateStr);
        } catch (Exception e) {
            return fail(CustomErrorMessageType.of(JsonSchemaErrorCode.DATE_PARSER_ERROR,
                    new MessageFormat("{0} date parser error: {1}")), at, e.getMessage());
        }
    }

    protected abstract Set<ValidationMessage> validateTwoDate(Date defineDate, Date jsonDate, String at,
                                                              String defineTimeStr, String jsonTimeStr);

    @Override
    public void preloadJsonSchema() {
        // not used and the Validator is not extending from BaseJsonValidator
    }
}