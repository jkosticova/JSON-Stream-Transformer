package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.Utils.Helper;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.State.Sync;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openjdk.jol.info.GraphLayout;

public class BufferTransducer {
    private final State currentState;
    private final SourceTransducer sourceTransducer;
    private final DestinationTransducer destinationTransducer;

    private boolean paused;
    JsonGenerator generator;
    JsonParser parser;
    TokenBuffer buffer;
    TransformationFormat specification;
    // Track the highest memory footprint the buffer reaches
    private long peakBufferBytes = 0;

    public BufferTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {        
            specification = mapper.getTransformationFormat();
            paused = false;

            JsonFactory factory = new JsonFactory();
            try {
                parser = factory.createParser(inputStream);
                generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //ObjectMapper objectMapper = new ObjectMapper();
            buffer = new TokenBuffer((ObjectCodec) null, false);
            sourceTransducer = new SourceTransducer(mapper, this);
            destinationTransducer = new DestinationTransducer(mapper, this);
            currentState = new Sync(this);        
    }

    public void getFromMemory() {
        try {
            buffer.serialize(generator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToMemory() {
        try {
            buffer.copyCurrentEvent(parser);
            //recordBufferMemory();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public JsonGenerator getGenerator() {
        return this.generator;
    }

    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public SourceTransducer getSourceTransducer() {
        return this.sourceTransducer;
    }

    public DestinationTransducer getDestinationTransducer() {
        return this.destinationTransducer;
    }

    public boolean process() {
        try {
            JsonToken event = null;

            while (!parser.isClosed()) {
                if (!paused) {
                    event = parser.nextToken();
                }
                if (event == null) break;
                currentState.process(event, parser);
            }

            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing BufferTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void recordBufferMemory() {
        if (this.buffer != null) {
            try {
                // JOL walks the object graph safely on modern Java versions
                long currentBufferBytes = GraphLayout.parseInstance(this.buffer).totalSize();

                if (currentBufferBytes > this.peakBufferBytes) {
                    this.peakBufferBytes = currentBufferBytes;
                }
            } catch (Exception e) {
                System.err.println("Skipping memory sample: " + e.getMessage());
            }
        }
    }

    public long getPeakBufferBytes() {
        return this.peakBufferBytes;
    }

    public double getPeakBufferMegabytes() {
        return this.peakBufferBytes / (1024.0 * 1024.0);
    }
}
