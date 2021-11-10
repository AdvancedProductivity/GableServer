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