package Prototype.Writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

/*
Generated custom Raw Writer (by Claude Sonnet 5) to avoid allocation overhead 
that depends on the output size introduced by standard JDK writers
*/
public final class RawUtf8Writer {

    private final OutputStream out;
    private final CharsetEncoder encoder;

    private final char[] charArray;
    private final CharBuffer charBuf; // view over charArray, position/limit managed manually
    private final ByteBuffer byteBuf; // reused output buffer

    public RawUtf8Writer(OutputStream out) {
        this(out, 512, 1024);
    }

    public RawUtf8Writer(OutputStream out, int charBufSize, int byteBufSize) {
        this.out = out;
        this.encoder = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        this.charArray = new char[charBufSize];
        this.charBuf = CharBuffer.wrap(charArray);
        this.byteBuf = ByteBuffer.allocate(byteBufSize);
    }

    private int pending = 0; // number of chars currently buffered in charArray

    public void write(int c) throws IOException {
        if (pending == charArray.length) {
            encodeAndReset(false);
        }
        charArray[pending++] = (char) c;
    }

    public void write(char[] src, int off, int len) throws IOException {
        int written = 0;
        while (written < len) {
            if (pending == charArray.length) {
                encodeAndReset(false);
            }
            int n = Math.min(charArray.length - pending, len - written);
            System.arraycopy(src, off + written, charArray, pending, n);
            pending += n;
            written += n;
        }
    }

    /** Encode everything currently buffered, then reset pending to 0. */
    private void encodeAndReset(boolean endOfInput) throws IOException {
        charBuf.clear();
        charBuf.limit(pending);
        CoderResult result;
        do {
            result = encoder.encode(charBuf, byteBuf, endOfInput);
            if (result.isOverflow()) {
                flushBytes();
            }
        } while (result.isOverflow());
        // result.isUnderflow() means charBuf fully consumed (REPLACE actions avoid errors)
        pending = 0;
    }

    private void flushBytes() throws IOException {
        byteBuf.flip();
        out.write(byteBuf.array(), byteBuf.arrayOffset() + byteBuf.position(), byteBuf.remaining());
        byteBuf.clear();
    }

    public void flush() throws IOException {
        encodeAndReset(false);
        flushBytes();
        out.flush();
    }

    public void close() throws IOException {
        encodeAndReset(true);
        CoderResult r;
        do {
            r = encoder.flush(byteBuf);
            if (r.isOverflow()) flushBytes();
        } while (r.isOverflow());
        flushBytes();
        out.close();
    }
}