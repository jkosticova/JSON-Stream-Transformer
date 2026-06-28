package Measurements;

import Prototype.Mapper.Mapper;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.IdentityTransducer;
import Prototype.StateArchitecture.Transducer.StackTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import jdk.jfr.*;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Scanner;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {
        int runRounds = 1;
        int setupRounds = 50;
        int evaluationRounds = 200;
        Mapper mapper = initMapper(args);
        String input;
        if (args.length > 1) {
            input = args[1];
        } else {
            input = "JsonExamples\\evaluationInput.json";
        }

        String output;
        Path result;
        if (args.length == 0) {
            output = "JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\output.json";
            result = Path.of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\results.txt");
        } else {
            String specificationName = args[0].substring(args[0].lastIndexOf("\\") + 1, args[0].lastIndexOf("."));
            String inputName = input.substring(input.lastIndexOf("\\") + 1, input.lastIndexOf("."));
            output = "JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\output" + specificationName + "_" + inputName + ".json";
            result = Path.of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\results\\" + specificationName + "_" + inputName + ".txt");
        }
        Files.writeString(result, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        for (int rr = 0; rr < runRounds; rr++) {
            Files.writeString(result, "Run " + (rr + 1) + " of " + runRounds + "\n", StandardOpenOption.APPEND);

            if (mapper.getTransformationFormat().getType().equals("copy") || mapper.getTransformationFormat().getType().equals("move")) {
                //runBufferTransducerWithOutput(input, mapper, output);

                evaluateBufferTransducerRuns(setupRounds, evaluationRounds, mapper, input);

                outputTotalBytes(mapper.getTransformationFormat().getType(), evaluationRounds, result);

                continue;
            }

            //runTransducerWithOutput(input, mapper, output);

            evaluateTransducerRuns(setupRounds, evaluationRounds, mapper, input);

            outputTotalBytes(mapper.getTransformationFormat().getType(), evaluationRounds, result);
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

    private static void evaluateTransducerRuns(int setupRounds, int evaluationRuns, Mapper mapper, String input) throws IOException {
        for (int i = 0; i < setupRounds; i++) {
            InputStream inputStream = new FileInputStream(input);
            OutputStream outputStream = OutputStream.nullOutputStream();

            Transducer transducer = getTransducerFromType(mapper, inputStream, outputStream);

            transducer.process();

            inputStream.close();
            outputStream.close();
        }

        Recording r = new Recording();
        r.enable("jdk.ObjectAllocationInNewTLAB")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        r.enable("jdk.ObjectAllocationOutsideTLAB")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        r.enable("jdk.GCHeapSummary");
        r.enable("jdk.GarbageCollection");

        r.start();

        for (int i = 0; i < evaluationRuns; i++) {
            InputStream inputStream = new FileInputStream(input);
            OutputStream outputStream = OutputStream.nullOutputStream();

            Transducer transducer = getTransducerFromType(mapper, inputStream, outputStream);

            transducer.process();

            inputStream.close();
            outputStream.close();
        }

        r.stop();
        r.dump(Path.of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\run.jfr"));
        r.close();
    }

    private static void evaluateBufferTransducerRuns(int setupRounds, int evaluationRuns, Mapper mapper, String input) throws IOException {
        for (int i = 0; i < setupRounds; i++) {
            InputStream inputStream = new FileInputStream(input);
            OutputStream outputStream = OutputStream.nullOutputStream();

            BufferTransducer transducer = new BufferTransducer(mapper, inputStream, outputStream);

            transducer.process();

            inputStream.close();
            outputStream.close();
        }

        Recording r = new Recording();
        r.enable("jdk.ObjectAllocationInNewTLAB")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        r.enable("jdk.ObjectAllocationOutsideTLAB")
                .withStackTrace()
                .withThreshold(Duration.ZERO);

        r.enable("jdk.GCHeapSummary");
        r.enable("jdk.GarbageCollection");

        r.start();

        for (int i = 0; i < evaluationRuns; i++) {
            InputStream inputStream = new FileInputStream(input);
            OutputStream outputStream = OutputStream.nullOutputStream();

            BufferTransducer transducer = new BufferTransducer(mapper, inputStream, outputStream);

            transducer.process();

            inputStream.close();
            outputStream.close();
        }

        r.stop();
        r.dump(Path.of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\run.jfr"));
        r.close();
    }

    private static void outputTotalBytes(String type, int evaluationRounds, Path result) throws IOException {
        long totalBytes = 0;
        long maxHeapUsed = 0;

        try (RecordingFile rf = new RecordingFile(Path.of("JsonExamples\\Evaluation\\" + type + "\\run.jfr"))) {
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();

                String name = e.getEventType().getName();

                if (name.equals("jdk.ObjectAllocationInNewTLAB") || name.equals("jdk.ObjectAllocationOutsideTLAB")) {
                    totalBytes += e.getLong("allocationSize");
                }
            }
        }
        
        Files.writeString(result, "Total allocated: " + totalBytes + "\n", StandardOpenOption.APPEND);
        Files.writeString(result, "Allocated per run(" + evaluationRounds + "): " + totalBytes / evaluationRounds + "\n", StandardOpenOption.APPEND);
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

    static Mapper initMapper(String[] args) {
        Mapper mapper = null;

        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Input specification file.");

            String specFile = scanner.nextLine();
            if (!specFile.isEmpty()) {
                mapper = initializeMapper(specFile);
            } else {
                mapper = initializeMapper("JsonExamples\\Evaluation\\specification.json");
            }
        } else if (args.length > 0) {
            mapper = initializeMapper(args[0]);
        }

        if (mapper == null) {
            throw new RuntimeException("Null Mapper");
        }

        
        return mapper;
    }
}

