package Prototype.Utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Helper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(); // shared, thread-safe for reads
    
    public static void writeJsonValue(JsonGenerator gen, String jsonOrValue) throws IOException {
        gen.setCodec(OBJECT_MAPPER);
        try {
            JsonNode node = OBJECT_MAPPER.readTree(jsonOrValue);
            gen.writeTree(node);
        } catch (JsonProcessingException e) {
            gen.writeString(jsonOrValue);
        }
    }
          
}
