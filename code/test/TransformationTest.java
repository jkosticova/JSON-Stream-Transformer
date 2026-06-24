import Prototype.Mapper.Mapper;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.IdentityTransducer;
import Prototype.StateArchitecture.Transducer.StackTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;


import static Prototype.Main.initializeMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;


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

        if (mapper.getTransformationFormat().getType().equals("copy") || mapper.getTransformationFormat().getType().equals("move")) {
            BufferTransducer bufferTransducer = new BufferTransducer(mapper, inputStream, outputStream);
            bufferTransducer.process();
        } else {
            Transducer transducer = null;
            if (mapper.getTransformationFormat().getType().equals("identity")) {
                transducer = new IdentityTransducer(mapper, inputStream, outputStream);
            } else {
                transducer = new StackTransducer(mapper, inputStream, outputStream);
            }
            transducer.process();
        }

        File outputFile = new File(outputFileName);
        JsonNode expected = objectMapper.readTree(expectedFile);
        JsonNode actual = objectMapper.readTree(outputFile);

        assertEquals(expected, actual);
    }
}