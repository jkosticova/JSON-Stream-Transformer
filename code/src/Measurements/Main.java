package Measurements;

import Prototype.Mapper.Mapper;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.IdentityTransducer;
import Prototype.StateArchitecture.Transducer.StackTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
//import jdk.jfr.*;
//import jdk.jfr.consumer.RecordedEvent;
//import jdk.jfr.consumer.RecordingFile;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Scanner;

import java.nio.file.Path;

import java.lang.management.ManagementFactory;


public class Main {
    private static final com.sun.management.ThreadMXBean bean =
        (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();    

    public static void main(String[] args) throws Exception {
        int runRounds = 1;
        int setupRounds = 1000;
        int evaluationRounds = 1000;
        long baselineBytes = 0;
        String transfType;
        String input;
        String output;
        Path result;
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
            specificationName = args[0].substring(args[0].lastIndexOf("\\") + 1, args[0].lastIndexOf("."));
            inputName = input.substring(input.lastIndexOf("\\") + 1, input.lastIndexOf("."));
            output = "JsonExamples\\Evaluation\\" + transfType + "\\output" + specificationName + "_" + inputName
                    + ".json";
            result = Path.of("JsonExamples\\Evaluation\\" + transfType + "\\results\\" + specificationName + "_"
                    + inputName + ".txt");
        } else {// {if (args.length == 1) {
            transfType = "baseline";
            input = args[0];
            inputName = input.substring(input.lastIndexOf("\\") + 1, input.lastIndexOf("."));
            output = "JsonExamples\\Evaluation\\" + transfType + "\\output\\baseline_" + inputName + ".json";
            result = Path.of("JsonExamples\\Evaluation\\" + transfType + "\\results\\baseline_" + inputName + ".txt");
        }
        Files.writeString(result, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        for (int rr = 0; rr < runRounds; rr++) {
            Files.writeString(result, "Run " + (rr + 1) + " of " + runRounds + "\n", StandardOpenOption.APPEND);

            if (transfType.equals("copy") || transfType.equals("move")) {
                // runBufferTransducerWithOutput(input, mapper, output);

                long bytes = evaluateBufferTransducerRuns(setupRounds, evaluationRounds, mapper, input);

                appendCsv(csv,
                        rr,
                        specificationName,
                        transfType,
                        input,
                        setupRounds,
                        evaluationRounds,
                        bytes, baselineBytes);
                continue;

                // outputTotalBytes(transfType, evaluationRounds, result);
            }
            if (transfType.equals("baseline")) {
                long bytes = evaluateJacksonBaselineRuns(setupRounds, evaluationRounds, input);
                baselineBytes = bytes / evaluationRounds;
                appendCsv(csv,
                        rr,
                        specificationName,
                        transfType,
                        input,
                        setupRounds,
                        evaluationRounds,
                        bytes, baselineBytes);
                continue;
            }
            // runTransducerWithOutput(input, mapper, output);

            long bytes = evaluateTransducerRuns(setupRounds, evaluationRounds, mapper, input);

            appendCsv(csv,
                    rr,
                    specificationName,
                    transfType,
                    input,
                    setupRounds,
                    evaluationRounds,
                    bytes, baselineBytes);            
        }
    }

    private static void runBufferTransducerWithOutput(String input, Mapper mapper, String output) throws IOException {
        InputStream inputStream = new FileInputStream(input);
        OutputStream outputStream = new FileOutputStream(output);

        BufferTransducer bufferTransducer = new BufferTransducer(mapper, inputStream, outputStream);

        bufferTransducer.process();

        inputStream.close();
        outputStream.close();
    }

    private static void runTransducerWithOutput(String input, Mapper mapper, String output) throws IOException {
        InputStream inputStream = new FileInputStream(input);
        OutputStream outputStream = new FileOutputStream(output);

        Transducer transducer = getTransducerFromType(mapper, inputStream, outputStream);

        transducer.process();

        inputStream.close();
        outputStream.close();
    }

    private static long evaluateTransducerRuns(
            int setupRounds,
            int evaluationRuns,
            Mapper mapper,
            String input) throws IOException {

        // -------------------------
        // WARMUP (no measurement)
        // -------------------------
        for (int i = 0; i < setupRounds; i++) {
            try (InputStream inputStream = new FileInputStream(input);
                    OutputStream outputStream = OutputStream.nullOutputStream()) {

                Transducer transducer = getTransducerFromType(mapper, inputStream, outputStream);
                transducer.process();
            }
        }

        long totalBytes = 0;

        // -------------------------
        // MEASUREMENT PHASE
        // -------------------------
        for (int i = 0; i < evaluationRuns; i++) {

            long bytes = measureAllocatedBytes(() -> {
                try (InputStream inputStream = new FileInputStream(input);
                        OutputStream outputStream = OutputStream.nullOutputStream()) {

                    Transducer transducer = getTransducerFromType(mapper, inputStream, outputStream);
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

                BufferTransducer t = new BufferTransducer(mapper, inputStream, outputStream);
                t.process();
            }
        }

        long totalBytes = 0;

        for (int i = 0; i < evaluationRuns; i++) {

            long bytes = measureAllocatedBytes(() -> {
                try (InputStream inputStream = new FileInputStream(input);
                        OutputStream outputStream = OutputStream.nullOutputStream()) {

                    BufferTransducer t = new BufferTransducer(mapper, inputStream, outputStream);
                    t.process();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            totalBytes += bytes;
        }

        return totalBytes;
    }

    

    private static Transducer getTransducerFromType(Mapper mapper, InputStream inputStream, OutputStream outputStream) {
        Transducer transducer;
        if (mapper.getTransformationFormat().getType().equals("identity")) {
            transducer = new IdentityTransducer(mapper, inputStream, outputStream);
        } else {
            transducer = new StackTransducer(mapper, inputStream, outputStream);
        }

        return transducer;
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
                    JsonGenerator generator = factory.createGenerator(OutputStream.nullOutputStream())) {

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
                        JsonGenerator generator = factory.createGenerator(OutputStream.nullOutputStream())) {

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
            Files.writeString(csv,
                    "",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(csv,
                    "run,algorithm,specification,input,setupRounds,evaluationRounds,allocationBytes,bytesPerRun\n",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        }
    }

    private static void appendCsv(Path csv,
            int run,
            String spec,
            String algorithm,
            String input,
            int setupRounds,
            int evaluationRounds,
            long bytes, long baselineBytes) throws IOException {

        long perRun = (long) bytes / evaluationRounds;
        long delta = perRun - baselineBytes;
                String line = run + "," +
                algorithm + "," +
                spec + "," +
                input + "," +
                setupRounds + "," +
                evaluationRounds + "," +
                bytes + "," +
                perRun + "\n";
                

        Files.writeString(csv, line,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static long measureAllocatedBytes(Runnable task) {
        long tid = Thread.currentThread().threadId();

        long before = bean.getThreadAllocatedBytes(tid);

        task.run();

        long after = bean.getThreadAllocatedBytes(tid);

        return after - before;
    }

}
