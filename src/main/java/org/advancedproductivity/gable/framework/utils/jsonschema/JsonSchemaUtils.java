package org.advancedproductivity.gable.framework.utils.jsonschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasquatch.jsonschemainferrer.*;
import org.advancedproductivity.gable.web.entity.Result;

public class JsonSchemaUtils {
    private static final JsonSchemaInferrer INFERRER = JsonSchemaInferrer.newBuilder()
            .setSpecVersion(SpecVersion.DRAFT_07)
            .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.allowed())
            .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
            .build();

    public static void main(String[] args) {
        Result result = Result.success();
        result.put("aStr", "ok ko");
        result.put("1+2", 3);
        System.out.printf(INFERRER.inferForSample(result).toPrettyString());
    }

}
