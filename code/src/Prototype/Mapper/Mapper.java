package Prototype.Mapper;

import Prototype.SpecificationParser.TransformationFormat;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class Mapper implements SpecificationMapper {
    private final File specification;
    private final InputStream schema;
    private TransformationFormat transformationFormat;

    public Mapper(File specificationFile) {
        specification = specificationFile;
        schema = Mapper.class.getClassLoader().getResourceAsStream("schema.json");
        init();
    }

    @Override
    public TransformationFormat getTransformationFormat() {
        return transformationFormat;
    }

    private void init() {
        try {
            // ObjectMapper je trieda v Jacksone
            ObjectMapper mapper = new ObjectMapper();

            // objekt JSONSchemaValidator
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            // objekt JSONSchemaValidator
            JsonSchema schema = factory.getSchema(this.schema);
            // objekt Jackson
            JsonNode jsonNode = mapper.readTree(this.specification);
            // metoda JSONSchemaValidator
            Set<ValidationMessage> validationMessages = schema.validate(jsonNode);

            if (!validationMessages.isEmpty()) {
                validationMessages.forEach(vm -> System.out.println("Validation error: " + vm.getMessage()));
                throw new RuntimeException("JSON failed schema validation.");
            }

            // objekt Jacksonu
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, TransformationFormat.class);
            List<TransformationFormat> listObj = mapper.treeToValue(jsonNode, type);

            // iba posledna dvojica key/value je ulozena, ale mal by to byt druh operacie
            for (TransformationFormat transformation : listObj) {
                this.transformationFormat = transformation;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Problem with JDTMapper");
        }
    }
}
