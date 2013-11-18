package com.aliasi.test.unit.io;

import com.aliasi.io.BitOutput;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import java.util.Random;

public class BitOutputTest  {

    @Test
    public void testPowers() {
        Random random = new Random();

        assertEquals(Math.max(0,63- Long.numberOfLeadingZeros(0L)),
                     BitOutput.mostSignificantPowerOfTwo(0L));
        assertEquals(Math.max(0,63- Long.numberOfLeadingZeros(1L)),
                     BitOutput.mostSignificantPowerOfTwo(1L));
        assertEquals(Math.max(0,63- Long.numberOfLeadingZeros(Long.MAX_VALUE)),
                     BitOutput.mostSignificantPowerOfTwo(Long.MAX_VALUE));

        for (int i = 0; i < 100; ++i) {
            long n = random.nextLong();
            assertEquals(63 - Long.numberOfLeadingZeros(n),
                         BitOutput.mostSignificantPowerOfTwo(n));
        }
    }

    /*
    @Test
    public void testMostSigPowerOf2() {
        assertEquals(0,BitOutput.mostSignificantPowerOfTwo(1));
        assertEquals(1,BitOutput.mostSignificantPowerOfTwo(2));
        assertEquals(1,BitOutput.mostSignificantPowerOfTwo(3));
        assertEquals(2,BitOutput.mostSignificantPowerOfTwo(4));
        assertEquals(2,BitOutput.mostSignificantPowerOfTwo(7));
        assertEquals(3,BitOutput.mostSignificantPowerOfTwo(8));
        long n = 1;
        for (int i = 0; i < 64; ++i) {
            assertEquals(i,BitOutput.mostSignificantPowerOfTwo(n));
            n <<= 1;
        }
    }

    @Test
    public void testSlice() {
        assertSlice(57,2,3,6);
        assertSlice(127,3,2,3);
        assertSlice(128,3,2,0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSliceEx1() {
        BitOutput.sliceBits(512L,-1,14);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSliceEx2() {
        BitOutput.sliceBits(512L,64,14);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSliceEx3() {
        BitOutput.sliceBits(512L,12,-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSliceEx4() {
        BitOutput.sliceBits(512L,12,65);
    }

    @Test
    public void testLeastSigBits() {
        assertLeastSigBits(0,4,0);
        assertLeastSigBits(7,2,3);
        assertLeastSigBits(Long.MAX_VALUE,64,Long.MAX_VALUE);
        assertLeastSigBits(Long.MAX_VALUE,63,Long.MAX_VALUE);
        assertLeastSigBits(Long.MAX_VALUE << 1,64,Long.MAX_VALUE << 1);
        assertLeastSigBits(Long.MAX_VALUE,1,1l);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testLeastSigBitsEx1() {
        BitOutput.leastSignificantBits(57L,-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLeastSigBitsEx2() {
        BitOutput.leastSignificantBits(57L,0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLeastSigBitsEx3() {
        BitOutput.leastSignificantBits(57L,65);
    }


    void assertLeastSigBits(long in, int numBits, long expected) {
        assertEquals(expected,BitOutput.leastSignificantBits(in,numBits));
    }

    @Test
    public void testBitOutputStream() throws IOException {
        BitOutputStream bitOut = new BitOutputStream();
        bitOut.write(1);  // 100
        String one = "00000001";
        assertBits("00000001",bitOut.toString());
        String seventyThree = "01001001";
        bitOut.write(73);
        assertBits(one + seventyThree,bitOut.toString());
    }

    @Test
    public void testFlushClose() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        assertBits("",bits.toString());
        bo.flush();
        assertBits("",bits.toString());
        bo.writeBit(true);
        bo.writeBit(false);
        bo.flush();
        assertBits("10000000",bits.toString());
        bo.flush();
        assertBits("10000000",bits.toString());
        bo.close();
    }

    @Test(expected=IOException.class)
    public void testFlushCloseExc() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.close();
        bo.close(); // second one throws
    }

    @Test
    public void testWriteBit() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeBit(true);
        bo.writeBit(true);
        bo.writeBit(false);
        bo.writeBit(true);
        bo.writeBit(false);
        bo.writeBit(true);
        bo.writeBit(true);
        bo.writeBit(false);
        assertBits("11010110",bits.toString());
        bo.writeBit(false);
        bo.writeBit(true);
        bo.flush();
        assertBits("11010110"+"01000000",bits.toString());

    }

    @Test(expected=IllegalArgumentException.class)
    public void testUnaryExceptions() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeUnary(0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testUnaryExceptions2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeUnary(-17);
    }

    @Test
    public void testUnary() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeUnary(3);
        bo.flush();
        String expected = "00100000";
        assertBits(expected,bits.toString());

        bo.writeUnary(3);
        bo.writeUnary(2);
        bo.writeUnary(1);
        bo.writeUnary(7);
        bo.flush();
        expected += "001" + "01" + "1" + "0000001" + "000";
        assertBits(expected,bits.toString());
    }

    @Test
    public void testUnaryLong() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeUnary(3);
        bo.writeUnary(27);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 3; ++i) sb.append("0");
        sb.append("1");
        for (int i = 1; i < 27; ++i) sb.append("0");
        sb.append("1");
        sb.append("00");
        bo.flush();
        assertBits(sb.toString(),bits.toString());
    }

    @Test
    public void testUnaryVeryLong() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 64; ++i) {
            bo.writeUnary(i);
            for (int j = 1; j < i; ++j)
                sb.append("0");
            sb.append("1");
        }
        bo.flush();
        assertBits(sb.toString(),bits.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBinaryExceptions() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeBinary(16,3);
    }



    @Test(expected=IllegalArgumentException.class)
    public void testBinaryExceptions2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeBinary(7,-3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBinaryExceptions3() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeBinary(-17,21);
    }


    @Test
    public void testBinary() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeBinary(7,3);
        bo.writeBinary(7,4);
        bo.writeBinary(0,1);
        bo.flush();
        assertBits("11101110",bits.toString());
    }

    @Test
    public void testBinary2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeBinary(Long.MAX_VALUE,64); // 2**63 - 1
        bo.flush();
        StringBuilder sb = new StringBuilder();
        sb.append("0");
        for (int i = 0; i < 63; ++i)
            sb.append("1");
        assertBits(sb.toString(),bits.toString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGammaExceptions() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeGamma(0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGammaExceptions2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeGamma(-12);
    }

    @Test
    public void testGamma() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeGamma(1);
        bo.writeGamma(2);
        bo.writeGamma(3);
        bo.flush();
        assertBits("1" + "010" + "011" + "0",bits.toString());
    }

    @Test
    public void testGamma2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeGamma(8);
        bo.flush();
        assertBits("0001" + "000" + "0",bits.toString());
    }

    @Test
    public void testGamma3() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeGamma(15);
        bo.flush();
        assertBits("0001" + "111" + "0",bits.toString());
    }

    @Test
    public void testGamma4() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeGamma(Long.MAX_VALUE); // 2**63-1 (binary 1 63 times)
        bo.flush();
        StringBuilder sb = new StringBuilder();
        // unary prefix: 6
        for (int i = 0; i < 62; ++i)
            sb.append("0"); // 62 "0"s
        sb.append("1"); // 1 "1" = 63 in unary
        // 62 remaining digits
        for (int i = 0; i < 62; ++i)
            sb.append("1");
        // padding
        sb.append("0");
        sb.append("0");
        sb.append("0");
        assertBits(sb.toString(),bits.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDeltaExceptions() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeDelta(0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testDeltaExceptions2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeDelta(-12);
    }

    @Test
    public void testDelta() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeDelta(1);
        bo.writeDelta(2);
        bo.writeDelta(3);
        bo.writeDelta(4);
        bo.flush();
        String expected = "1" + "0100" + "0101" + "01100" + "00";
        assertBits(expected,bits.toString());
    }

    @Test
    public void testDelta2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeDelta(8);
        bo.writeDelta(15);
        bo.flush();
        String expected = "00100000" + "00100111";
        assertBits(expected,bits.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRiceExceptions() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeRice(-1,2);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testRiceExceptions2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeRice(127,0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRiceExceptions3() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeRice(Long.MAX_VALUE,3);
    }

    @Test
    public void testRice() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        StringBuilder sb = new StringBuilder();
        bo.writeRice(1,1);
        sb.append("10");
        bo.writeRice(2,1);
        sb.append("11");
        bo.writeRice(3,1);
        sb.append("010");
        bo.writeRice(4,1);
        sb.append("011");
        bo.writeRice(5,1);
        sb.append("0010");
        bo.writeRice(6,1);
        sb.append("0011");
        bo.writeRice(7,1);
        sb.append("00010");
        bo.writeRice(8,1);
        sb.append("00011");
        bo.writeRice(9,1);
        sb.append("000010");
        sb.append("000000"); // pad
        bo.flush();
        assertBits(sb.toString(),bits.toString());
    }

    @Test
    public void testRice2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        StringBuilder sb = new StringBuilder();
        bo.writeRice(1,2);
        sb.append("100");
        bo.writeRice(2,2);
        sb.append("101");
        bo.writeRice(3,2);
        sb.append("110");
        bo.writeRice(4,2);
        sb.append("111");
        bo.writeRice(5,2);
        sb.append("0100");
        bo.writeRice(6,2);
        sb.append("0101");
        bo.writeRice(7,2);
        sb.append("0110");
        bo.writeRice(8,2);
        sb.append("0111");
        bo.writeRice(9,2);
        sb.append("00100");
        sb.append("0000000"); // pad
        bo.flush();
        assertBits(sb.toString(),bits.toString());
    }

    @Test
    public void testRice3() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        StringBuilder sb = new StringBuilder();
        bo.writeRice(1,3);
        sb.append("1000");
        bo.writeRice(2,3);
        sb.append("1001");
        bo.writeRice(3,3);
        sb.append("1010");
        bo.writeRice(4,3);
        sb.append("1011");
        bo.writeRice(5,3);
        sb.append("1100");
        bo.writeRice(6,3);
        sb.append("1101");
        bo.writeRice(7,3);
        sb.append("1110");
        bo.writeRice(8,3);
        sb.append("1111");
        bo.writeRice(9,3);
        sb.append("01000");
        sb.append("000"); // pad
        bo.flush();
        assertBits(sb.toString(),bits.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFibonacciExceptions() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeFibonacci(0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testFibonacciExceptions2() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        bo.writeFibonacci(-12);
    }

    @Test
    public void testFibonacci() throws IOException {
        BitOutputStream bits = new BitOutputStream();
        BitOutput bo = new BitOutput(bits);
        StringBuilder sb = new StringBuilder();
        bo.writeFibonacci(1);
        sb.append("11");
        bo.writeFibonacci(2);
        sb.append("011");
        bo.writeFibonacci(3);
        sb.append("0011");
        bo.writeFibonacci(4);
        sb.append("1011");
        bo.writeFibonacci(5);
        sb.append("00011");
        bo.writeFibonacci(6);
        sb.append("10011");
        bo.writeFibonacci(7);
        sb.append("01011");
        bo.writeFibonacci(8);
        sb.append("000011");
        bo.writeFibonacci(9);
        sb.append("100011");
        bo.writeFibonacci(10);
        sb.append("010011");
        bo.writeFibonacci(11);
        sb.append("001011");
        bo.writeFibonacci(12);
        sb.append("101011");
        bo.writeFibonacci(13);
        sb.append("0000011");
        bo.writeFibonacci(14);
        sb.append("1000011");
        bo.writeFibonacci(15);
        sb.append("0100011");
        bo.writeFibonacci(16);
        sb.append("0010011");
        bo.writeFibonacci(17);
        sb.append("1010011");
        sb.append("000");
        bo.flush();
        assertBits(sb.toString(),bits.toString());
    }

    void assertBits(String exp, String found) {
        String msg =  "exp=" + exp + " length=" + exp.length() 
            + " found=" + found + " length=" + found.length();
        assertTrue(msg,  exp.equals(found));
    }

    void assertSlice(long n, int leastSignificantBit, int numBits,
                     long expected) {
        assertEquals(expected,
                     BitOutput.sliceBits(n,leastSignificantBit,numBits));
    }


    static class BitOutputStream extends ByteArrayOutputStream {
        boolean mClosed = false;
        public String toString() {
            byte[] bytes = toByteArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; ++i) {
                byte b = bytes[i];
                for (int j = 8; --j >= 0; ) {
                    sb.append(((b & (1 << j)) == 0) ? '0' : '1');
                }
            }
            return sb.toString();          
        }
        public void close() throws IOException {
            if (mClosed) throw new IOException("closed twice.");
            mClosed = true;
        }
    }
    */


}
