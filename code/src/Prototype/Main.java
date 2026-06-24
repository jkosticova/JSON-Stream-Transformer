package Prototype;

import Prototype.Mapper.Mapper;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.StackTransducer;
import Prototype.StateArchitecture.Transducer.IdentityTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;

import java.io.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class Main {

    public static void main(String[] args) throws IOException {

        Mapper mapper;

        if (args.length < 3) {
            throw new RuntimeException("Missing specification file or input or output file");
        }

        Path specificationPath = Paths.get(args[0]).toAbsolutePath().normalize();

        mapper = initializeMapper(specificationPath);

        Path inputPath = Paths.get(args[1]).toAbsolutePath().normalize();
        Path outputPath = Paths.get(args[2]).toAbsolutePath().normalize();

        //System.out.println("Processing...");
        InputStream inputStream = Files.newInputStream(inputPath);
        OutputStream outputStream = Files.newOutputStream(outputPath);

        if (mapper.getTransformationFormat().getType().equals("copy")
                || mapper.getTransformationFormat().getType().equals("move")) {
            BufferTransducer bufferTransducer = new BufferTransducer(mapper, inputStream, outputStream);
            if (bufferTransducer.process()) {
                //System.out.println("SUCCESS");
                // Clean up the file paths to extract clean names (e.g., "specIdentity" and "evaluationInputBig")
                String specName = specificationPath.getFileName().toString();
                String inputName = inputPath.getFileName().toString();

                // Print a line starting with a specific prefix so PowerShell can grab it
                // Format: EXPORT,Specification,InputFile,PeakBytes
                System.out.println("EXPORT," + specName + "," + inputName + "," + bufferTransducer.getPeakBufferBytes());
            } else {
                //System.out.println("FAILURE");
            }
            //System.out.println("Done processing BUFFER Transformation.");
        }
        else {
            Transducer transducer;                    
            if (mapper.getTransformationFormat().getType().equals("identity")) {
                transducer = new IdentityTransducer(mapper, inputStream, outputStream);
            } else {
                transducer = new StackTransducer(mapper, inputStream, outputStream);
            }

            if (transducer.process()) {
              //  System.out.println("SUCCESS");
            } else {
              //  System.out.println("FAILURE");
            }
            //System.out.println("Done processing NON-BUFFER transformation.");
        }

    }

    public static Mapper initializeMapper(Path specificationPath) {
        File specificationJsonFile = specificationPath.toFile();
        return new Mapper(specificationJsonFile);
    }
}
