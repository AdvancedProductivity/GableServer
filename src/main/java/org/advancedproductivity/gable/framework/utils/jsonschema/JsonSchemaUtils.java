package org.advancedproductivity.gable.framework.utils.jsonschema;

import com.networknt.schema.*;
import com.saasquatch.jsonschemainferrer.*;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import org.advancedproductivity.gable.framework.utils.jsonschema.validate.DateGreaterEqual;
import org.advancedproductivity.gable.framework.utils.jsonschema.validate.DateGreaterThan;
import org.advancedproductivity.gable.framework.utils.jsonschema.validate.DateLessEqual;
import org.advancedproductivity.gable.framework.utils.jsonschema.validate.DateLessThan;
import org.advancedproductivity.gable.web.entity.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zzq
 */
public class JsonSchemaUtils {
    private static final String ID = "$id";
    private static final JsonSchemaInferrer INFERRER = JsonSchemaInferrer.newBuilder()
            .setSpecVersion(SpecVersion.DRAFT_07)
            .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.allowed())
            .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
            .build();
    private static final List<Format> BUILTIN_FORMATS = new ArrayList<Format>(JsonMetaSchema.COMMON_BUILTIN_FORMATS);

    public static void main(String[] args) {
        Result result = Result.success();
        result.put("aStr", "ok ko");
        result.put("1+2", 3);
        System.out.printf(INFERRER.inferForSample(result).toPrettyString());
    }

    public static JsonSchemaFactory getInstance(com.networknt.schema.SpecVersion.VersionFlag versionFlag) {
        JsonMetaSchema metaSchema = null;
        switch (versionFlag) {
            case V201909:
                metaSchema = getV201909();
                break;
            case V7:
                metaSchema = getV7();
                break;
            case V6:
                metaSchema = getV6();
                break;
            case V4:
                metaSchema = getV4();
                break;
            default:
                break;
        }
        if (metaSchema == null) {
            metaSchema = getV7();
        }
        return new JsonSchemaFactory.Builder()
                .defaultMetaSchemaURI(metaSchema.getUri())
                .addMetaSchema(metaSchema)
                .build();
    }

    private static JsonMetaSchema getV4() {
        return new JsonMetaSchema.Builder("https://json-schema.org/draft-04/schema")
                .idKeyword(ID)
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(com.networknt.schema.SpecVersion.VersionFlag.V4))
                .addKeyword(new DateLessThan())
                .addKeyword(new DateLessEqual())
                .addKeyword(new DateGreaterEqual())
                .addKeyword(new DateGreaterThan())
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("id"),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions")
                ))
                .build();
    }

    private static JsonMetaSchema getV201909() {
        return new JsonMetaSchema.Builder("https://json-schema.org/draft/2019-09/schema")
                .idKeyword("$id")
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(com.networknt.schema.SpecVersion.VersionFlag.V201909))
                .addKeyword(new DateLessThan())
                .addKeyword(new DateLessEqual())
                .addKeyword(new DateGreaterEqual())
                .addKeyword(new DateGreaterThan())
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$id"),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions"),
                        new NonValidationKeyword("$comment"),
                        new NonValidationKeyword("$defs"),
                        new NonValidationKeyword("$anchor"),
                        new NonValidationKeyword("additionalItems"),
                        new NonValidationKeyword("deprecated"),
                        new NonValidationKeyword("contentMediaType"),
                        new NonValidationKeyword("contentEncoding"),
                        new NonValidationKeyword("examples"),
                        new NonValidationKeyword("then")
                ))
                .build();
    }

    private static JsonMetaSchema getV7() {
        return new JsonMetaSchema.Builder("https://json-schema.org/draft-07/schema")
                .idKeyword(ID)
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(com.networknt.schema.SpecVersion.VersionFlag.V7))
                .addKeyword(new DateLessThan())
                .addKeyword(new DateLessEqual())
                .addKeyword(new DateGreaterEqual())
                .addKeyword(new DateGreaterThan())
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$id"),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions"),
                        new NonValidationKeyword("$comment"),
                        new NonValidationKeyword("contentMediaType"),
                        new NonValidationKeyword("contentEncoding"),
                        new NonValidationKeyword("examples")
                ))
                .build();
    }

    private static JsonMetaSchema getV6() {
        return new JsonMetaSchema.Builder("https://json-schema.org/draft-06/schema")
                .idKeyword(ID)
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(com.networknt.schema.SpecVersion.VersionFlag.V6))
                .addKeyword(new DateLessThan())
                .addKeyword(new DateLessEqual())
                .addKeyword(new DateGreaterEqual())
                .addKeyword(new DateGreaterThan())
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$id"),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions")
                ))
                .build();
    }
}
