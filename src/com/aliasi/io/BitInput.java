package com.aliasi.io;

import com.aliasi.util.Math;

import java.io.InputStream;
import java.io.IOException;

/** 
 * A <code>BitInput</code> wraps an underlying input stream to provide
 * bit-level input.  Input is read through the method {@link
 * #readBit()}, which returns a boolean value.  Bits are coded
 * as booleans, with <code>true=1</code> and <code>false=0</code>.
 *
 * @author Bob Carpenter
 * @version 2.1.1
 * @since 2.1.1
 */
public class BitInput {

    private final InputStream mIn;
    
    private int mNextByte; // implied = 0;
    
    private int mNextBitIndex;

    private boolean mEndOfStream = false;

    /** 
     * Constructs a bit input wrapping the specified input stream.
     * The input stream is accessed by the constructor to fill
     * a read-ahead buffer.  Thus the constructor will throw an
     * exception if there is a read exception

     * @param in Input stream backing this bit input.
     * @throws IOException If there is an exception reading from the
     * specified input stream.
     */
    public BitInput(InputStream in) throws IOException {
        mIn = in;
        readAhead();
    }

    /** 
     * Returns number of bits available for reading without blocking.
     * This is the sum of the number of bits buffered and the result
     * of querying the number of bytes available from the underlying
     * input stream using {@link InputStream#available()}.
     * 
     * @return Number of bits available for reading.
     * @throws IOException If there is an exception while checking
     * availability in the underlying input stream.
     */
    public long available() throws IOException {
        return mEndOfStream
            ? 0l
            : ((mNextBitIndex + 1l) + (8l * (long) mIn.available()));
    }

    /** 
     * Closes this input stream and releases associated resources.
     * This method will close the underlying input stream for this bit
     * input by calling {@link InputStream#close()}.  After calls to
     * this method, {@link #available()} will return zero and {@link
     * #endOfStream()} will return <code>true</code>.
     * 
     * @throws IOException If there is an exception closing the
     * underlying input stream.
     */
    public void close() throws IOException { 
        mEndOfStream = true;
        mIn.close(); 
    }

    /** 
     * Returns <code>true</code> if all of the available bits have
     * been read or if this stream has been closed.
     *
     * @return <code>true</code> if all of the available bits have
     * been read.
     */
    public boolean endOfStream() { 
        return mEndOfStream; 
    }


    /**
     * Skips and discards the specified number of bits in the input.
     * The return value is the number of bits skipped, which may be
     * less than the number requested if the end-of-stream is reached
     * or if the underlying input stream cannot skip the required
     * number of bytes.
     *
     * @param numBits Number of bits to skip.
     * @return Number of bits actually skipped.
     * @throws IOException If there is an I/O error skipping on the
     * underlying input stream.
     * @throws IllegalArgumentException If the number of bits argument
     * is less than zero.
     */
    public long skip(long numBits) throws IOException {
        if (numBits < 0) {
            String msg = "Require positive number of bits to skip."
                + " Found numBits=" + numBits;
            throw new IllegalArgumentException(msg);
        }
        if (mNextBitIndex >= numBits) {  
            mNextBitIndex -= numBits; // decrement within byte buffer
            return numBits;
        }

        long numBitsSkipped = mNextBitIndex + 1;
        long numBitsLeft = numBits - numBitsSkipped;

        long bytesToSkip = numBitsLeft / 8;
        long bytesSkipped = mIn.skip(bytesToSkip);

        numBitsSkipped += 8 * bytesSkipped;
        if (bytesSkipped < bytesToSkip) {
            mEndOfStream = true;  // exhausted input stream
            return numBitsSkipped;
        }

        readAhead();
        if (mEndOfStream) 
            return numBitsSkipped; // nothing left
        mNextBitIndex = 7 - (((int)numBitsLeft) % 8);
        return numBits;
    }

    /** 
     * Reads the next bit from the input stream.  Bits are encoded
     * as binary values, using <code>true</code> for <code>1</code>
     * and <code>false</code> for <code>0</code>.
     *
     * <P>The return value is undefined if the end of stream has been
     * reached, but this method will not throw an exception.
     *
     * @return The boolean value of the next bit.
     * @throws IOException If there is an exception reading from the
     * underlying stream.
     */
    public boolean readBit() throws IOException {
        switch (mNextBitIndex--) {
        case 0:
            boolean result = ((mNextByte & 1) != 0); 
            readAhead();
            return result;
        case 1: 
            return ((mNextByte & 2) != 0);
        case 2: 
            return ((mNextByte & 4) != 0);
        case 3: 
            return ((mNextByte & 8) != 0);
        case 4:
            return ((mNextByte & 16) != 0);
        case 5:
            return ((mNextByte & 32) != 0);
        case 6:
            return ((mNextByte & 64) != 0);
        case 7:
            return ((mNextByte & 128) != 0);
        default: 
            String msg 
                = "Index out of bounds. mNextBitIndex=" + mNextBitIndex;
            throw new IOException(msg);
        }
    }


    /**
     * Reads the next value from the input using a unary code.  If all
     * of the remaining bits are zeros, it throws an I/O exception.
     * If the end of stream has been reached, it returns -1.
     *
     * <P>See {@link BitOutput#writeUnary(int)} for a description of
     * unary coding.
     *
     * @return The next integer read with unary code.
     * @throws IOException If the remaining bits are all zeros.
     */
    public int readUnary() throws IOException {
        int result = 1;

        // look through remaining buffered bits
        for ( ; !endOfStream() && mNextBitIndex != 7; ++result) 
            if (readBit()) 
                return result;

        // jump over whole 0 bytes
        while (!endOfStream() && mNextByte == ZERO_BYTE) {
            result += 8;
            mNextByte = mIn.read();
            if (mNextByte == -1) {
                String msg = "Final sequence of 0 bits with no 1";
                throw new IOException(msg);
            }
        }

        // read to last one bit (could do the search trick here!)
        while (!readBit()) // know we'll find it given last test
            ++result;
        return result;
    }

    /**
     * Skips over and discards the next value from the input using a
     * unary code.
     *
     * @throws IOException If there is a format error in the input stream
     * or an error reading from the underlying stream.
     */
    public void skipUnary() throws IOException {
        // cut and paste from readUnary without result increments
        // look through remaining buffered bits
        while (!endOfStream() && mNextBitIndex != 7)
            if (readBit()) 
                return;

        // jump over whole 0 bytes
        while (!endOfStream() && mNextByte == ZERO_BYTE) {
            mNextByte = mIn.read();
            if (mNextByte == -1) {
                String msg = "Final sequence of 0 bits with no 1";
                throw new IOException(msg);
            }
        }

        // read to last one bit (could do the search trick here!)
        while (!readBit()) ; // know we'll find it given last test
        // 
    }

    /**
     * Reads the next value from the input using an Elias gamma code.
     * If the code is incomplete or not well-formed, an I/O exception
     * is raised.  If the end of stream has been reached, the value
     * returned is -1.
     *
     * <P>See {@link BitOutput#writeGamma(long)} for a description of
     * gamma coding.
     *
     * @return The next integer read using gamma coding.
     * @throws IOException If the prefix of the remaining bits is not
     * a valid gamma code or if there is an error reading from the
     * underlying input stream.
     */
    public long readGamma() throws IOException {
        int numBits = readUnary();
        if (numBits > 63) {
            String msg = "Gamma code binary part must be <= 63 bits."
                + " Found numBits=" + numBits;
            throw new IOException(msg);
        }
        return readRest(numBits-1,1l);
    }

    /**
     * Skips over and discards the next value from the input using an
     * Elias gamma code.
     * 
     * @throws IOException If there is a format error in the input stream
     * or an error reading from the underlying stream.
     */
    public void skipGamma() throws IOException {
        int numBits = readUnary();
        checkGamma(numBits);
        skip(numBits-1);
    }

    /**
     * Reads the next value from the input using an Elias delta code.
     * If the code is incomplete or not well-formed, an I/O exception
     * is raised.  If the end of stream has been reached, the value
     * returned is -1.
     *
     * <P>See {@link BitOutput#writeDelta(long)} for a description of
     * gamma coding.
     *
     * @return The next integer read using delta coding.
     * @throws IOException If the prefix of the remaining bits is not
     * a valid delta code or there is an error reading from the
     * underlying input stream.
     */
    public long readDelta() throws IOException {
        long numBits = readGamma();
        checkDelta(numBits);
        if (numBits > 63l) {
            String msg = "Delta code must use <= 63 bits for fixed portion."
                + " Found number of remaining bits=" + numBits;
            throw new IOException(msg);
        }
        return readRest((int)numBits-1,1l);
    }

    /**
     * Skips over and discards the next value from the input using an
     * Elias delta code.
     * 
     * @throws IOException If there is a format error in the input
     * stream or an error reading from the underlying stream.
     */
    public void skipDelta() throws IOException {
        long numBits = readGamma();
        checkDelta(numBits);
        skip(numBits-1);
    }

    /**
     * Reads the next value from the input using a fixed-length binary
     * code of the specified number of bits.  This method throws an
     * exception if there are not enough bits remaining in the input.
     * The number of bits must be greater than zero and not more than
     * 63, so that the result will fit into a long integer (note that
     * the negative numbers are not used).
     *
     * <P>To skip over a binary encoding of a specified number of
     * bits, just call {@link #skip(long)} on the number of bits.
     *
     * <P>See {@link BitOutput#writeBinary(long,int)} for a definition
     * of binary encoding and examples.
     * 
     * @param numBits Number of bits in encoding.
     * @return The integer read in binary using the specified number
     * of bits.  
     * @throws IOException If there are not enough bits remaining in
     * the input or there is an error reading from the underlying
     * input stream.
     * @throws IllegalArgumentException If the number of bits is less
     * than 1 or greater than 63.
     */
    public long readBinary(int numBits) throws IOException {
        if (numBits > 63) {
            String msg = "Cannot read more than 63 bits into positive long."
                + " Found numBits=" + numBits;
            throw new IllegalArgumentException(msg);
        }
        if (numBits < 1) {
            String msg = "Number of bits to read must be > 0."
                + " Found numBits=" + numBits;
            throw new IllegalArgumentException(msg);
        }
        long result = readBit() ? 1l : 0l;
        return readRest(numBits-1,result);
    }

    /**
     * Reads the next number from the input using a Rice coding with
     * the specified number of fixed bits for the remainder.  This
     * method will throw an exception if the number of fixed bits is
     * greater than 63 or less than 1.
     *
     * @param numFixedBits The number of fixed bits in the Rice code.
     * @return The next number read using a Rice code with the
     * specified number of fixed bits.
     * @throws IOException If the coding is illegal.
     * @throws IllegalArgumentException If the number of fixed bits is
     * less than 1 or greater than 63.
     */
    public long readRice(int numFixedBits) throws IOException {
        if (numFixedBits < 1) {
            String msg = "Rice coding requires a number of fixed bits > 0."
                + " Found numFixedBits=" + numFixedBits;
            throw new IllegalArgumentException(msg);
        }
        if (numFixedBits > 63) {
            String msg = "Rice coding requires a number of fixed bits < 64."
                +  "Found numFixedBits=" + numFixedBits;
            throw new IllegalArgumentException(msg);
        }
        long prefixBits = readUnary();
        long remainder = readBinary(numFixedBits);
        long q = prefixBits - 1l;
        long div = (q << numFixedBits);
        return div + (remainder + 1l);
    }

    /**
     * Skips over and discards the next value from the input using a
     * Rice code with the specified number of fixed bits.
     * 
     * @throws IOException If there is a format error in the input
     * stream or an error reading from the underlying stream.
     */
    public void skipRice(int numFixedBits) throws IOException {
        skipUnary();
        skip(numFixedBits);
    }

    /**
     * Reads the next value from the input using a Fibonacci code.  If
     * the code is not legal, this method throws an I/O exception.
     *
     * <P>See {@link BitOutput#writeFibonacci(long)} for more information
     * on Fibonacci codes.
     *
     * @return The next number read from the input using a Fibonacci
     * code.
     * @throws IOException If the remaining bits do not contain
     * a pair of consecutive 1 bits or there is an exception reading from
     * the underlying input stream.
     */
    public long readFibonacci() throws IOException {
        long[] fibs = Math.FIBONACCI_SEQUENCE;
        long sum = 0l;
        for (int i = 0; i < fibs.length && !endOfStream(); ++i) {
            if (readBit()) {
                sum += fibs[i++];
                if (!endOfStream() && readBit()) return sum;
            }
        }
        String msg = "Ran off end of input or beyond maximum length "
            + " without finding two consecutive 1s";
        throw new IOException(msg);
    }
    
    /**
     * Skips over and discards the next value from the input using a
     * Fibonacci code.
     * 
     * @throws IOException If there is a format error in the input
     * stream or an error reading from the underlying stream.
     */
    public void skipFibonacci() throws IOException {
        while (!endOfStream())
            if (readBit() && !endOfStream() && readBit()) 
                return;
        String msg = "Ran off end of input without finding two consecutive 1s";
        throw new IOException(msg);
    }

    long readRest(int numBits, long result) throws IOException {
        /* simple working version:
           while (--numBits >= 0) {
           notEndOfStream();
           result = result << 1;
           if (readBit()) result |= 1l;
           }
           return result;
        */

        if (numBits == 0) return result;
    
        notEndOfStream();

        // can read from currently buffered byte
        if (mNextBitIndex >= numBits) {
            mNextBitIndex -= numBits;
            return (result << numBits)
                | sliceBits2(mNextByte,mNextBitIndex+1,numBits+1);
        }

        // numBits > mNextBitIndex
        // read rest of current byte
        numBits -= (mNextBitIndex+1);
        result = (result << (mNextBitIndex+1)) 
            | sliceBits2(mNextByte,0,mNextBitIndex+1);

        for ( ; numBits >= 8; numBits -= 8) {
            int nextByte = mIn.read();
            if (nextByte == -1) {
                mEndOfStream = true;
                String msg = "Premature end of stream reading binary - mid.";
                throw new IOException(msg);
            }
            result = (result << 8) | nextByte;
        }
        readAhead();

        if (numBits == 0) return result;
        notEndOfStream();
        mNextBitIndex = 7 - numBits;
        return (result << numBits)
            | sliceBits2(mNextByte,mNextBitIndex+1,numBits);
    }

    private void readAhead() throws IOException {
        if (mEndOfStream) return;
        mNextByte = mIn.read(); 
        if (mNextByte == -1) { 
            mEndOfStream = true; 
            return; 
        }
        mNextBitIndex = 7;
    }


    private void notEndOfStream() throws IOException {
        if (endOfStream()) {
            String msg = "End of stream reached prematurely.";
            throw new IOException(msg);
        }
    }

    static final byte ZERO_BYTE = (byte) 0;

    static int ALL_ONES_INT = ~0;
    
    static long leastSignificantBits2(int n, int numBits) {
        return (ALL_ONES_INT >>> (32-numBits)) & n;
    }

    static long sliceBits2(int n, int leastSignificantBit, int numBits) {
        return leastSignificantBits2(n >>> leastSignificantBit,
                                     numBits);
    }

    static void checkGamma(int numBits) throws IOException {
        if (numBits <= 63) return;
        String msg = "Gamma code binary part must be <= 63 bits."
            + " Found numBits=" + numBits;
        throw new IOException(msg);
    }
    
    static void checkDelta(long numBits) throws IOException {
        if (numBits <= 63l) return;
        String msg = "Delta code binary part must be <= 63 bits."
            + " Number of bits specified=" + numBits;
        throw new IOException(msg);
    }



}
