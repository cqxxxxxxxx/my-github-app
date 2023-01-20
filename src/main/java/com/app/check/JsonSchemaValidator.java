package com.app.check;

import com.app.Constant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class JsonSchemaValidator {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String PRODUCT_JSON_SCHEMA = "/product_json_schema.json";

    public static void main(String[] args) throws IOException {
        String s = validateProductJson(JsonSchemaValidator.class.getResourceAsStream("/product_demo.json"));
        System.out.println(s);
    }

    public static String validateProductJson(InputStream jsonInputStream) throws IOException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        InputStream schemaInputStream = JsonSchemaValidator.class.getResourceAsStream(PRODUCT_JSON_SCHEMA);
        JsonSchema jsonSchema = factory.getSchema(schemaInputStream);
        JsonNode jsonNode = mapper.readTree(jsonInputStream);
        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        if (errors == null || errors.isEmpty()) {
            return Constant.SUCCESS;
        }
        StringBuilder sb = new StringBuilder();
        errors.stream().forEach(x -> sb.append(x).append("\n"));
        return sb.toString();
    }
}
