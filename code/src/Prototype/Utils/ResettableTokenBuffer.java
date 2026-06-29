package Prototype.Utils;

import java.io.IOException;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.util.TokenBuffer;


public class ResettableTokenBuffer extends TokenBuffer {
    private long writes = 0;


    public ResettableTokenBuffer() {
        super((ObjectCodec) null, false);
    }


    @Override
    public void writeString(String value) throws IOException {
        writes++;
        super.writeString(value);
    }



    public void reset() {        
        _first = new Segment();
        _last = _first;
        _appendAt = 0;

        _hasNativeTypeIds = false;
        _hasNativeObjectIds = false;

        _closed = false;
    }

}