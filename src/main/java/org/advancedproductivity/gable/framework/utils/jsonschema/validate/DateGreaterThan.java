package org.advancedproductivity.gable.framework.utils.jsonschema.validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import org.advancedproductivity.gable.framework.utils.jsonschema.JsonSchemaErrorCode;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;

public class DateGreaterThan  extends AbstractKeyword {

    public DateGreaterThan() {
        super("dateGT");
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
            if (jsonTime > defineTime) {
                return pass();
            }
            return fail(CustomErrorMessageType.of(JsonSchemaErrorCode.DATE_LESS_THAN_ERROR,
                    new MessageFormat("{0} must be later than {1}, but actual are {2}")), at, defineTimeStr, jsonTimeStr);
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
