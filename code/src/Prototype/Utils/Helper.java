package Prototype.Utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Stack;

public class Helper {
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String GetSimplifiedJSONPathFromStack(Stack<String> stack) {
        StringBuilder sb = new StringBuilder();
        for (String path : stack) {
            if (path.equals("$")) {
                sb.append("$");
            } else if (path.equals("arr")) {
                sb.append("[");
            } else if (path.equals("obj")) {
                sb.append(".");
            } else if (isNumeric(path)) {
                sb.append(path);
                sb.append("]");
            } else {
                sb.append(path);
            }
        }

        return sb.toString();
    }

    public static void writeJsonValue(JsonGenerator gen, String jsonOrValue) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        gen.setCodec(mapper);
        try {
            JsonNode node = mapper.readTree(jsonOrValue);

            gen.setCurrentValue(node);
            gen.writeTree(node);
        } catch (JsonProcessingException e) {
            gen.writeString(jsonOrValue);
        }
    }
}
