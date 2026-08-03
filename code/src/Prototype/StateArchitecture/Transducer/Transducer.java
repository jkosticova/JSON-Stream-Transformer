package Prototype.StateArchitecture.Transducer;

import Prototype.StateArchitecture.JsonPushdownAutomaton.JsonPushdownAutomaton;
import Prototype.StateArchitecture.State.State;
import Prototype.Writer.JsonWriter;
import Prototype.Writer.RawUtf8Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Transducer {
    
    protected JsonParser jParser;
    protected JsonWriter jWriter;

    private boolean paused = false;

    protected JsonPushdownAutomaton jsonPda;
    // zovseobecnit na synchronizacne n-tice stavov
    protected ArrayList<State> syncStates;

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    public Transducer(InputStream inputStream, OutputStream outputStream) {                
        try {
            this.jParser = JSON_FACTORY.createParser(inputStream);            
        }
        catch (Exception e) {
            e.printStackTrace();
        } 
                
        RawUtf8Writer writer = new RawUtf8Writer(outputStream);
        this.jWriter = new JsonWriter(writer);    
    }
        
    public boolean process() {
        try {
            JsonToken token = null;            
            // kym sa cita nieco zo vstupu
            while (!jParser.isClosed()) {                
                // paused musia respektovat vsetky automaty
                if (!this.paused) {
                    token = jParser.nextToken();
                }                
                // EOF && prazdny stack
                if (token == null || jsonPda.stackIsEmpty()) break;                
                                                
                
                // logika generovania
                if (jsonPda.isGenerating()) {
                    jWriter.writeCurrentEvent(jParser);
                    jWriter.flush();
                }   
                
                if (true) {
                    // write current event to buffer
                }

                if (true) {
                    token = jParser.nextToken();
                }

                // zavolat process na vsetkych automatoch
                jsonPda.process(token, jParser.getText());                
                if (this.syncStates.contains(jsonPda.getState())) {
                    this.synchronize();
                }
                
            }
            jParser.close();
            jWriter.flush();
            //writer.close();
        } catch (Exception e) {
            System.out.println("Issue while processing StackTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }

    void synchronize() {
        return;
    }    
}
