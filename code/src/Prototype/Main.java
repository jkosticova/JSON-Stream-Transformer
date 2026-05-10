package Prototype;

import Prototype.Mapper.Mapper;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.StackTransducer;
import Prototype.StateArchitecture.Transducer.IdentityTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        Mapper mapper;
        String specificationPath = "JsonExamples\\SpecificationFiles\\specification.json";

        Scanner scanner = new Scanner(System.in);

        if (args.length == 1) {
            specificationPath = args[0];
        } else {
            System.out.println("Input specification file.");
            String specFileFromInput = scanner.nextLine();

            if (!specFileFromInput.isEmpty()) {
                specificationPath = specFileFromInput;
            }
        }

        mapper = initializeMapper(specificationPath);
        
        if (mapper == null) {
            throw new RuntimeException("Null Mapper");
        }

        System.out.println("Input file to be processed.");
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            } else {
                System.out.println("Processing...");
                InputStream inputStream = new FileInputStream(input);
                String inputName = input.substring(input.lastIndexOf("\\") + 1, input.lastIndexOf("."));
                OutputStream outputStream = new FileOutputStream("JsonExamples\\output" + inputName + ".json");

                if (mapper.getTransformationFormat().getType().equals("copy") || mapper.getTransformationFormat().getType().equals("move")) {
                    BufferTransducer bufferTransducer = new BufferTransducer(mapper, inputStream, outputStream);
                    if (bufferTransducer.process()) {
                        System.out.println("SUCCESS");
                    } else {
                        System.out.println("FAILURE");
                    }
                    System.out.println("Done processing BUFFER Transformation. Input next file to be processed or type \"exit\" to end the program.");
                    continue;
                }

                Transducer transducer;
                if (mapper.getTransformationFormat().getType().equals("identity")) {
                    transducer = new IdentityTransducer(mapper, inputStream, outputStream);
                } else {
                    transducer = new StackTransducer(mapper, inputStream, outputStream);
                }

                if (transducer.process()) {
                    System.out.println("SUCCESS");
                } else {
                    System.out.println("FAILURE");
                }

                System.out.println("Done processing. Input next file to be processed or type \"exit\" to end the program.");
            }
        }

    }

    public static Mapper initializeMapper(String specificationFileName) {
        File specificationJsonFile = new File(specificationFileName);
        return new Mapper(specificationJsonFile);
    }
}
