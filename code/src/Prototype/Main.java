package prototype;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import prototype.mapper.Mapper;
import prototype.stateArchitecture.transducer.BufferSyncTransducer;
import prototype.stateArchitecture.transducer.IoHandler;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

public class Main {

    public static void main(String[] args) throws IOException {

        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "Missing specification file, input file, or output file");
        }

        Path specificationPath =
                Paths.get(args[0]).toAbsolutePath().normalize();
        Path inputPath =
                Paths.get(args[1]).toAbsolutePath().normalize();
        Path outputPath =
                Paths.get(args[2]).toAbsolutePath().normalize();

        Mapper mapper = initializeMapper(specificationPath);
        JsonFactory factory = new JsonFactory();

        try (InputStream inputStream = Files.newInputStream(inputPath);
             OutputStream outputStream = Files.newOutputStream(outputPath);
             
             JsonParser parser = IoHandler.createParser(factory, inputStream);
             JsonGenerator generator = IoHandler.createGenerator(factory, outputStream)) {        
            
    
            String type = mapper.getTransformationFormat().getType();
            

            if (type.equals("copy") || type.equals("move")) {
                BufferSyncTransducer transducer =  new BufferSyncTransducer(
                        mapper,
                        inputStream,
                        outputStream
                );
                transducer.process();
            }

            else if (type.equals("identity")) {
                Transducer transducer = new Transducer(
                        mapper,
                        parser,
                        generator
                );
                transducer.process();
            }

            else {
                Transducer transducer = new StackTransducer(
                    mapper,
                    parser,
                    generator
                );
                transducer.process();
            }
        }
        catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
        
    }

    public static Mapper initializeMapper(Path specificationPath) {
        return new Mapper(specificationPath.toFile());
    }

    
}
