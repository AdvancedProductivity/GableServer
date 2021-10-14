package org.advancedproductivity.gable.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.*;
import org.advancedproductivity.gable.framework.config.HttpResponseField;
import org.advancedproductivity.gable.framework.core.TestType;
import org.advancedproductivity.gable.web.entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;

/**
 * @author zzq
 */
@RestController
@RequestMapping("/api/jsonSchema")
public class JsonSchemaController {
    private static final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
            .setSpecVersion(SpecVersion.DRAFT_07)
            // Requires commons-validator
            .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed())
            .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
            .setArrayLengthFeatures(EnumSet.allOf(ArrayLengthFeature.class))
            .addEnumExtractors(EnumExtractors.validEnum(java.time.Month.class),EnumExtractors.validEnum(java.time.DayOfWeek.class))
            .build();

    @PostMapping
    public Result generate(@RequestBody JsonNode in, @RequestParam String type) {
        if (StringUtils.equals(TestType.HTTP.name(), type)) {
            JsonNode body = in.path(HttpResponseField.CONTENT);
            if (body.isMissingNode() || body.isNull()) {
                return Result.success(inferrer.inferForSample(body));
            }
            return Result.success(inferrer.inferForSample(body));
        }
        return Result.success(inferrer.inferForSample(in));
    }
}
