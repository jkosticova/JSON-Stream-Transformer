import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import prototype.mapper.Mapper;
import prototype.stateArchitecture.transducer.BufferSyncTransducer;
import prototype.stateArchitecture.transducer.IoHandler;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static prototype.Main.initializeMapper;


class TransformationTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static Stream<Arguments> scenarios() throws Exception {
        Path root = Paths.get(
                Objects.requireNonNull(
                        TransformationTest.class
                                .getClassLoader()
                                .getResource("")
                ).toURI());

        return Files.walk(root)
                .filter(p ->
                        Files.exists(p.resolve("input.json")) &&
                                Files.exists(p.resolve("specification.json")) &&
                                Files.exists(p.resolve("expected.json"))
                )
                .map(Arguments::of);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void transformationMatchesExpected(Path scenarioDir) throws Exception {
        String inputFileName = String.valueOf(scenarioDir.resolve("input.json"));
        String outputFileName = scenarioDir + "\\output.json";

        Path specificationPath = scenarioDir.resolve("specification.json").toAbsolutePath().normalize();
        File expectedFile = scenarioDir.resolve("expected.json").toFile();

        Mapper mapper = initializeMapper(specificationPath);

        InputStream inputStream = new FileInputStream(inputFileName);
        OutputStream outputStream = new FileOutputStream(outputFileName);

        if (mapper.getTransformationFormat().getType().equals("move")) {
            BufferSyncTransducer bufferTransducer = new BufferSyncTransducer(mapper, inputStream, outputStream);
            bufferTransducer.process();
        }
        else if (mapper.getTransformationFormat().getType().equals("copy") 
    ) {
            BufferSyncTransducer bufferTransducer = new BufferSyncTransducer(mapper, inputStream, outputStream);
            bufferTransducer.process();
        } else {
            Transducer transducer = null;
            JsonFactory factory = new JsonFactory();
            JsonParser parser = IoHandler.createParser(factory, inputStream);
        
            // TODO - close parse if generator creation fails
            JsonGenerator generator = IoHandler.createGenerator(factory, outputStream);
            if (mapper.getTransformationFormat().getType().equals("identity")) {
                transducer = new Transducer(mapper, parser, generator);
            } else {
                transducer = new StackTransducer(mapper, parser, generator);
            }
            transducer.process();
        }

        File outputFile = new File(outputFileName);
        JsonNode expected = objectMapper.readTree(expectedFile);
        JsonNode actual = objectMapper.readTree(outputFile);

        assertEquals(expected, actual);
    }
}