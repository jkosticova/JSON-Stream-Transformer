package Measurements;

import Prototype.Mapper.Mapper;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.IdentityTransducer;
import Prototype.StateArchitecture.Transducer.StackTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import jdk.jfr.*;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

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
        String transfType;
        String input;
        String output;
        Path result;
        String specificationName = null;
        String inputName;
        Mapper mapper = null;
        Path csv = Path.of("JsonExamples/Evaluation/results.csv");
        initCsv(csv);

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
                        bytes);
                continue;

                //outputTotalBytes(transfType, evaluationRounds, result);               
            }
            if (transfType.equals("baseline")) {
                long bytes = evaluateJacksonBaselineRuns(setupRounds, evaluationRounds, input);                

                appendCsv(csv,
                        rr,
                        specificationName,                        
                        transfType,
                        input,
                        setupRounds,
                        evaluationRounds,
                        bytes);
                outputTotalBytes("baseline", evaluationRounds, result);                                    
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
                    bytes);
            outputTotalBytes(transfType, evaluationRounds, result);
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

    private static long evaluateTransducerRuns(int setupRounds, int evaluationRuns, Mapper mapper, String input)
            throws IOException {
        for (int i = 0; i < setupRounds; i++) {            
            InputStream inputStream = new FileInputStream(input);
            OutputStream outputStream = OutputStream.nullOutputStream();

            Transducer transducer = getTransducerFromType(mapper, inputStream, outputStream);

            transducer.process();

            inputStream.close();
            outputStream.close();
        }
        long totalBytes = 0;

        for (int i = 0; i < evaluationRuns; i++) {

            Recording r = new Recording();
            r.enable("jdk.ObjectAllocationInNewTLAB");
            r.enable("jdk.ObjectAllocationOutsideTLAB");
            r.enable("jdk.GCHeapSummary");
            r.enable("jdk.GarbageCollection");

            r.start();

            InputStream inputStream = new FileInputStream(input);
            OutputStream outputStream = OutputStream.nullOutputStream();

            Transducer transducer = getTransducerFromType(mapper, inputStream, outputStream);

            transducer.process();

            inputStream.close();
            outputStream.close();

            r.stop();
            r.dump(Path.of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\run_" + i + ".jfr"));
            long bytes = readAllocation(Path.of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\run_" + i + ".jfr"));
            totalBytes += bytes;
            r.close();
        }
        return totalBytes;
        /*
         * r.stop();
         * r.dump(Path.
         * of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "
         * \\run.jfr"));
         * r.close();
         */
    }

    private static long evaluateBufferTransducerRuns(int setupRounds, int evaluationRuns, Mapper mapper, String input)
            throws IOException {
         long totalBytes = 0;
        for (int i = 0; i < setupRounds; i++) {
            InputStream inputStream = new FileInputStream(input);
            OutputStream outputStream = OutputStream.nullOutputStream();

            BufferTransducer transducer = new BufferTransducer(mapper, inputStream, outputStream);

            transducer.process();

            inputStream.close();
            outputStream.close();
        }

        for (int i = 0; i < evaluationRuns; i++) {
            Recording r = new Recording();
            r.enable("jdk.ObjectAllocationInNewTLAB")
                    .withStackTrace()
                    .withThreshold(Duration.ZERO);

            r.enable("jdk.ObjectAllocationOutsideTLAB")
                    .withStackTrace()
                    .withThreshold(Duration.ZERO);
            /*
             * r.enable("jdk.ObjectAllocationSample")
             * .withStackTrace()
             * .withPeriod(Duration.ofMillis(1));
             */

            r.enable("jdk.GCHeapSummary");
            r.enable("jdk.GarbageCollection");

            r.start();

            InputStream inputStream = new FileInputStream(input);
            OutputStream outputStream = OutputStream.nullOutputStream();

            BufferTransducer transducer = new BufferTransducer(mapper, inputStream, outputStream);

            transducer.process();

            inputStream.close();
            outputStream.close();

            r.stop();
            r.dump(Path.of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\run_" + i + ".jfr"));
            long bytes = readAllocation(Path.of("JsonExamples\\Evaluation\\" + mapper.getTransformationFormat().getType() + "\\run_" + i + ".jfr"));
            totalBytes += bytes;
            r.close();

        }
        return totalBytes;

    }

     private static void outputTotalBytes(String type, int evaluationRounds, Path result) throws IOException {
        long totalBytes = 0;

        for (int i = 0; i < evaluationRounds; i++) {
            Path jfrPath = Path.of("JsonExamples\\Evaluation\\" + type + "\\run_" + i + ".jfr");
            jfrPath.toFile().length();

            try (RecordingFile rf = new RecordingFile(jfrPath)) {
                while (rf.hasMoreEvents()) {
                    RecordedEvent e = rf.readEvent();
                    String name = e.getEventType().getName();

                    if (name.equals("jdk.ObjectAllocationInNewTLAB")
                            || name.equals("jdk.ObjectAllocationOutsideTLAB")) {
                        totalBytes += e.getLong("allocationSize");
                    }
                }
            }
        }

        System.out.println("Total bytes counted: " + totalBytes);

        Files.writeString(result,
                "Total allocated: " + totalBytes + "\n",
                StandardOpenOption.APPEND);

        Files.writeString(result,
                "Allocated per run(" + evaluationRounds + "): " + totalBytes / evaluationRounds + "\n",
                StandardOpenOption.APPEND);
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

    private static long evaluateJacksonBaselineRuns(int setupRounds, int evaluationRuns, String input)
            throws IOException {
        JsonFactory factory = new JsonFactory();
        long totalBytes = 0;        
        for (int i = 0; i < setupRounds; i++) {
            try (InputStream inputStream = new FileInputStream(input);
                    JsonParser parser = factory.createParser(inputStream);
                    JsonGenerator generator = factory.createGenerator(OutputStream.nullOutputStream())) {
                while (parser.nextToken() != null) {
                    generator.copyCurrentEvent(parser);
                }
            }
        }

        for (int i = 0; i < evaluationRuns; i++) {
            Recording r = new Recording();
            r.enable("jdk.ObjectAllocationInNewTLAB").withStackTrace().withThreshold(Duration.ZERO);
            r.enable("jdk.ObjectAllocationOutsideTLAB").withStackTrace().withThreshold(Duration.ZERO);
            r.enable("jdk.GCHeapSummary");
            r.enable("jdk.GarbageCollection");
            r.start();
            try (InputStream inputStream = new FileInputStream(input);
                    JsonParser parser = factory.createParser(inputStream);
                    JsonGenerator generator = factory.createGenerator(OutputStream.nullOutputStream())) {
                while (parser.nextToken() != null) {
                    generator.copyCurrentEvent(parser);
                }
            }
            r.stop();
            r.dump(Path.of("JsonExamples\\Evaluation\\baseline\\run_" + i + ".jfr"));
            long bytes = readAllocation(Path.of("JsonExamples\\Evaluation\\baseline\\run_" + i + ".jfr"));
            totalBytes += bytes;
            r.close();
        }
        return totalBytes;

    }

    private static void initCsv(Path csv) throws IOException {
        if (!Files.exists(csv)) {
            Files.writeString(csv,
                    "run,algorithm,specification,input,setupRounds,evaluationRounds,allocationBytes,bytesPerRun\n",
                    StandardOpenOption.CREATE);
        }
    }

    private static void appendCsv(Path csv,
            int run,
            String spec,
            String algorithm,
            String input,
            int setupRounds,
            int evaluationRounds,
            long bytes) throws IOException {

        String line = run + "," +
                algorithm + "," +
                spec + "," +
                input + "," +
                setupRounds + "," +
                evaluationRounds + "," +
                bytes + "," +
                (bytes / evaluationRounds) + "\n";

        Files.writeString(csv, line,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static long readAllocation(Path jfrPath) throws IOException {
        long total = 0;

        try (RecordingFile rf = new RecordingFile(jfrPath)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();
                String name = e.getEventType().getName();

                if (name.equals("jdk.ObjectAllocationInNewTLAB")
                        || name.equals("jdk.ObjectAllocationOutsideTLAB")) {
                    total += e.getLong("allocationSize");
                }
            }
        }

        return total;
    }
    
}
