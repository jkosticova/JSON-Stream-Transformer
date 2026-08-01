package Prototype.Writer;

import java.util.Arrays;
import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/* 
    Custom JSON Writer
    It uses underlying custom raw UTF8 writer
    All write methods must not perform heap allocations per output event,
    otherwise the measurement of overall memory allocations wouldn't work as desired
*/
public class JsonWriter {

    private final RawUtf8Writer out;
    private Context[] ctxStack = new Context[16];
    private int ctxSize = 0;

    private static class Context {
        boolean object;
        boolean first;
    }

    public JsonWriter(RawUtf8Writer out) {
        this.out = out;
    }

    /* 
    */
    public void writeValue(JsonParser jParser) throws IOException {

        JsonToken token = jParser.currentToken();
        if (token == null) {
            return;
        }
        // primitive value
        if (!token.isStructStart()) {
            if (token == JsonToken.VALUE_STRING) {
                writeString(jParser);
            } else {
                writeRaw(jParser);
            }
            // jParser.nextToken();
            return;
        }
        int depth = 0;
        while (true) {
            token = jParser.currentToken();

            switch (token) {
                case START_OBJECT:
                    writeStartObject();
                    depth++;
                    break;
                case END_OBJECT:
                    writeEndObject();
                    depth--;
                    break;
                case START_ARRAY:
                    writeStartArray();
                    depth++;
                    break;
                case END_ARRAY:
                    writeEndArray();
                    depth--;
                    break;
                case FIELD_NAME:
                    writeFieldName(jParser);
                    break;
                case VALUE_STRING:
                    writeString(jParser);
                    break;
                default:
                    writeRaw(jParser);
                    break;
            }
            // end of the generated subtree
            if (depth == 0) {
                break;

            }
            jParser.nextToken();
        }
    }

    public void writeCurrentEvent(JsonParser jParser) throws IOException {
        JsonToken token = jParser.currentToken();

        switch (token) {
            case START_OBJECT:
                writeStartObject();
                break;
            case END_OBJECT:
                writeEndObject();
                break;
            case START_ARRAY:
                writeStartArray();
                break;
            case END_ARRAY:
                writeEndArray();
                break;
            case FIELD_NAME:
                writeFieldName(jParser);
                break;
            case VALUE_STRING:
                writeString(jParser);
                break;
            default:
                writeRaw(jParser);
                break;
        }
    }

    public void writeBuffer(JsonParser jParser) throws IOException {

        while (jParser.nextToken() != null) {
            JsonToken token = jParser.currentToken();

            switch (token) {
                case START_OBJECT:
                    writeStartObject();
                    break;
                case END_OBJECT:
                    writeEndObject();
                    break;
                case START_ARRAY:
                    writeStartArray();
                    break;
                case END_ARRAY:
                    writeEndArray();
                    break;
                case FIELD_NAME:
                    writeFieldName(jParser);
                    break;
                case VALUE_STRING:
                    writeString(jParser);
                    break;
                default:
                    writeRaw(jParser);
                    break;
            }
        }
    }    

    void writeStartObject() throws IOException {
        beforeValue();
        out.write('{');

        pushContext(true);
    }

    void writeEndObject() throws IOException {
        out.write('}');
        popContext();
    }

    void writeStartArray() throws IOException {
        beforeValue();
        out.write('[');

        pushContext(false);
    }

    void writeEndArray() throws IOException {
        out.write(']');
        popContext();
    }

    void writeFieldName(JsonParser jParser) throws IOException {
        Context ctx = ctxStack[ctxSize - 1];

        if (!ctx.first)
            out.write(',');

        ctx.first = false;

        out.write('"');

        char[] buffer = jParser.getTextCharacters();
        out.write(buffer, jParser.getTextOffset(), jParser.getTextLength());

        out.write('"');
        out.write(':');
    }

    // TODO context???
    public void writeFieldName(String fieldName) throws IOException {
        Context ctx = ctxStack[ctxSize - 1];

        if (!ctx.first)
            out.write(',');

        ctx.first = false;

        out.write('"');
        
        char[] chars = fieldName.toCharArray();
        out.write(chars, 0, chars.length);

        out.write('"');
        out.write(':');
    }    

    public void writeString(JsonParser jParser) throws IOException {
        beforeValue();

        char[] buffer = jParser.getTextCharacters();
        int offset = jParser.getTextOffset();
        int length = jParser.getTextLength();

        out.write('"');
        out.write(buffer, offset, length);
        out.write('"');
    }


    public void writeRaw(JsonParser parser) throws IOException {
        beforeValue();

        char[] buffer = parser.getTextCharacters();
        out.write(buffer, parser.getTextOffset(), parser.getTextLength());
    }

    public void writeRaw(String value) throws IOException {
        beforeValue();

        char[] chars = value.toCharArray();
        out.write(chars, 0, chars.length);
    }

    // void writeBoolean(boolean value);

    // void writeNull();

    private void beforeValue() throws IOException {

        if (ctxSize == 0)
            return;
        Context ctx = ctxStack[ctxSize - 1];
        // Only arrays need commas before values.
        if (!ctx.object) {

            if (!ctx.first)
                out.write(',');

            ctx.first = false;
        }
    }

    public void flush() throws IOException {
        out.flush();
    }

    private void pushContext(boolean object) {

        if (ctxSize == ctxStack.length) {
            ctxStack = Arrays.copyOf(ctxStack, ctxStack.length * 2);
        }

        Context ctx = ctxStack[ctxSize];

        if (ctx == null) {
            ctx = new Context();
            ctxStack[ctxSize] = ctx;
        }

        ctx.object = object;
        ctx.first = true;

        ctxSize++;
    }

    private void popContext() {
        ctxSize--;
    }
}