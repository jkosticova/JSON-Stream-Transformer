package measurements;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import prototype.mapper.Mapper;
import prototype.stateArchitecture.transducer.BufferSyncTransducer;
import prototype.stateArchitecture.transducer.IoHandler;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Main {

    private static final com.sun.management.ThreadMXBean bean =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    public static void main(String[] args) throws Exception {
        int runRounds = 1;
        int setupRounds = 500;
        int evaluationRounds = 500;

        String transfType;
        String input;
        String specificationName = null;
        String inputName;
        Mapper mapper = null;

        Path csv = Path.of("JsonExamples/Evaluation/results.csv");

        initCsv(csv);

        System.gc();
        Thread.sleep(100);

        if (args.length == 2) {
            mapper = initializeMapper(args[0]);
            input = args[1];

            transfType = mapper.getTransformationFormat().getType();

            specificationName = args[0].substring(
                    args[0].lastIndexOf("\\") + 1,
                    args[0].lastIndexOf(".")
            );

            inputName = input.substring(
                    input.lastIndexOf("\\") + 1,
                    input.lastIndexOf(".")
            );
        } else {
            transfType = "baseline";
            input = args[0];

            inputName = input.substring(
                    input.lastIndexOf("\\") + 1,
                    input.lastIndexOf(".")
            );
        }

        for (int rr = 0; rr < runRounds; rr++) {

            long bytes;

            if (transfType.equals("copy") || transfType.equals("move")) {
                bytes = evaluateBufferTransducerRuns(
                        setupRounds,
                        evaluationRounds,
                        mapper,
                        input
                );
            } else if (transfType.equals("baseline")) {
                bytes = evaluateJacksonBaselineRuns(
                        setupRounds,
                        evaluationRounds,
                        input
                );
            } else {
                bytes = evaluateTransducerRuns(
                        setupRounds,
                        evaluationRounds,
                        mapper,
                        input
                );
            }

            appendCsv(
                    csv,
                    rr,
                    specificationName,
                    transfType,
                    input,
                    setupRounds,
                    evaluationRounds,
                    bytes
            );
        }
    }

    private static long evaluateTransducerRuns(
            int setupRounds,
            int evaluationRuns,
            Mapper mapper,
            String input) throws IOException {

        for (int i = 0; i < setupRounds; i++) {
            try (InputStream inputStream = new FileInputStream(input);
                    OutputStream outputStream = OutputStream.nullOutputStream()) {

                Transducer transducer =
                        getTransducerFromType(mapper, inputStream, outputStream);

                transducer.process();
            }
        }

        long totalBytes = 0;

        for (int i = 0; i < evaluationRuns; i++) {
            long bytes = measureAllocatedBytes(() -> {
                try (InputStream inputStream = new FileInputStream(input);
                        OutputStream outputStream = OutputStream.nullOutputStream()) {

                    Transducer transducer =
                            getTransducerFromType(mapper, inputStream, outputStream);

                    transducer.process();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            totalBytes += bytes;
        }

        return totalBytes;
    }

    private static long evaluateBufferTransducerRuns(
            int setupRounds,
            int evaluationRuns,
            Mapper mapper,
            String input) throws IOException {

        for (int i = 0; i < setupRounds; i++) {
            try (InputStream inputStream = new FileInputStream(input);
                    OutputStream outputStream = OutputStream.nullOutputStream()) {

                BufferSyncTransducer transducer =
                        new BufferSyncTransducer(mapper, inputStream, outputStream);

                transducer.process();
            }
        }

        long totalBytes = 0;

        for (int i = 0; i < evaluationRuns; i++) {
            long bytes = measureAllocatedBytes(() -> {
                try (InputStream inputStream = new FileInputStream(input);
                        OutputStream outputStream = OutputStream.nullOutputStream()) {

                    BufferSyncTransducer transducer =
                            new BufferSyncTransducer(mapper, inputStream, outputStream);

                    transducer.process();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            totalBytes += bytes;
        }

        return totalBytes;
    }

    private static Transducer getTransducerFromType(
            Mapper mapper,
            InputStream inputStream,
            OutputStream outputStream) {

        JsonFactory factory = new JsonFactory();        
        if (mapper.getTransformationFormat().getType().equals("identity")) {
            return new Transducer(mapper, 
                                 IoHandler.createParser(factory, inputStream),
                                 IoHandler.createGenerator(factory, outputStream));
        }

        return new StackTransducer(mapper, 
                                 IoHandler.createParser(factory, inputStream),
                                 IoHandler.createGenerator(factory, outputStream));
    }

    public static Mapper initializeMapper(String specificationFileName) {
        File specificationJsonFile = new File(specificationFileName);
        return new Mapper(specificationJsonFile);
    }

    private static long evaluateJacksonBaselineRuns(
            int setupRounds,
            int evaluationRuns,
            String input) throws IOException {

        JsonFactory factory = new JsonFactory();

        for (int i = 0; i < setupRounds; i++) {
            try (InputStream inputStream = new FileInputStream(input);
                    JsonParser parser = factory.createParser(inputStream);
                    JsonGenerator generator =
                            factory.createGenerator(OutputStream.nullOutputStream())) {

                while (parser.nextToken() != null) {
                    generator.copyCurrentEvent(parser);
                }
            }
        }

        long totalBytes = 0;

        for (int i = 0; i < evaluationRuns; i++) {
            long bytes = measureAllocatedBytes(() -> {
                try (InputStream inputStream = new FileInputStream(input);
                        JsonParser parser = factory.createParser(inputStream);
                        JsonGenerator generator =
                                factory.createGenerator(OutputStream.nullOutputStream())) {

                    while (parser.nextToken() != null) {
                        generator.copyCurrentEvent(parser);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            totalBytes += bytes;
        }

        return totalBytes;
    }

    private static void initCsv(Path csv) throws IOException {
        if (!Files.exists(csv)) {
            Files.writeString(
                    csv,
                    "run,algorithm,specification,input,setupRounds,"
                            + "evaluationRounds,allocationBytes,bytesPerRun\n",
                    StandardOpenOption.CREATE
            );
        }
    }

    private static void appendCsv(
            Path csv,
            int run,
            String spec,
            String algorithm,
            String input,
            int setupRounds,
            int evaluationRounds,
            long bytes) throws IOException {

        long perRun = bytes / evaluationRounds;

        String line = run + ","
                + algorithm + ","
                + spec + ","
                + input + ","
                + setupRounds + ","
                + evaluationRounds + ","
                + bytes + ","
                + perRun + "\n";

        Files.writeString(
                csv,
                line,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    private static long measureAllocatedBytes(Runnable task) {
        long threadId = Thread.currentThread().threadId();

        long before = bean.getThreadAllocatedBytes(threadId);

        task.run();

        long after = bean.getThreadAllocatedBytes(threadId);

        return after - before;
    }
}