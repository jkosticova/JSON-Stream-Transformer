package prototype.mapper;

import prototype.specificationParser.TransformationFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Mapper implements SpecificationMapper {
    private final File specification;
    private final InputStream schema;
    private TransformationFormat transformationFormat;

    public Mapper(File specificationFile) {
        specification = specificationFile;
        schema = Mapper.class.getClassLoader().getResourceAsStream("schema.json");

        if (schema == null) {
            // Programmer/packaging error, not a user error: the schema ships
            // with the application, so its absence means something is
            // wrong with the build/classpath, not with the user's input.
            throw new IllegalStateException(
                    "Could not load schema.json from classpath. Check that it is packaged as a resource.");
        }

        init();
    }

    @Override
    public TransformationFormat getTransformationFormat() {
        return transformationFormat;
    }

    private void init() {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = readSpecification(mapper);
        validateAgainstSchema(mapper, jsonNode);
        this.transformationFormat = parseTransformations(mapper, jsonNode);
    }

    /**
     * Reads the specification file into a JsonNode tree.
     *
     * @throws SpecificationException if the file is missing, unreadable,
     *                                 or not valid JSON.
     */
    private JsonNode readSpecification(ObjectMapper mapper) {
        try {
            return mapper.readTree(this.specification);
        } catch (IOException e) {
            throw new SpecificationException(
                    "Could not read specification file: " + specification.getAbsolutePath(), e);
        }
    }

    /**
     * Validates the parsed specification against the bundled JSON schema.
     *
     * @throws SpecificationException if the specification fails validation.
     */
    private void validateAgainstSchema(ObjectMapper mapper, JsonNode jsonNode) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonSchema schema = factory.getSchema(this.schema);
        Set<ValidationMessage> validationMessages = schema.validate(jsonNode);

        if (!validationMessages.isEmpty()) {
            String details = validationMessages.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining("; "));
            throw new SpecificationException("Specification failed schema validation: " + details);
        }
    }

    /**
     * Deserializes the validated specification into a list of transformations
     * and returns the one to apply.
     *
     * @throws SpecificationException if the specification cannot be mapped
     *                                 to known transformation types, or if it
     *                                 contains no transformations at all.
     */
    private TransformationFormat parseTransformations(ObjectMapper mapper, JsonNode jsonNode) {
        try {
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, TransformationFormat.class);
            List<TransformationFormat> listObj = mapper.treeToValue(jsonNode, type);

            if (listObj == null || listObj.isEmpty()) {
                throw new SpecificationException("Specification contains no transformations.");
            }

            // TODO: only the last transformation in the list is kept, even though
            // the schema/format implies a sequence of operations should all be
            // applied. Preserved here to keep this change focused on exception
            // handling; fixing the semantics is a separate change.
            return listObj.get(listObj.size() - 1);
        } catch (JsonProcessingException e) {
            throw new SpecificationException("Could not parse specification into transformations", e);
        }
    }
}
