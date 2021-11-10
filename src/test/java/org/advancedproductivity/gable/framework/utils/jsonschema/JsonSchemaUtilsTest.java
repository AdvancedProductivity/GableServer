package org.advancedproductivity.gable.framework.utils.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaUtilsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonSchema(){
        System.out.println("Hello World");
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/json/DateJsonSchema.json");
        try {
            JsonNode jsonNode = mapper.readTree(resourceAsStream);
            JsonSchemaFactory factory = JsonSchemaUtils.getInstance(SpecVersionDetector.detect(jsonNode));
            JsonSchema schema = factory.getSchema(jsonNode);
            Set<ValidationMessage> validate = schema.validate(getTestJson());
            for (ValidationMessage validationMessage : validate) {
                System.out.println(validationMessage.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ObjectNode getTestJson(){
        ObjectNode mapperObjectNode = mapper.createObjectNode();
        mapperObjectNode.put("dateLtSuccessDemo", "2021-10-04 10:22:36");
        mapperObjectNode.put("dateLtErrorDemo", "2021-10-04 10:22:36");
        return mapperObjectNode;
    }
}