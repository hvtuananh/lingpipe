package com.aliasi.test.unit.io;

import com.aliasi.io.BitInput;
import com.aliasi.io.BitOutput;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;


import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.Random;

public class BitInputTest  {

    @Test
    public void testUnaryBig() throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        BitOutput bitOutput = new BitOutput(bytesOut);
        for (int i = 1; i < 150; ++i)
            bitOutput.writeUnary(i);
        bitOutput.writeUnary(1500);
        bitOutput.flush();

        byte[] bytes = bytesOut.toByteArray();
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        BitInput bitInput = new BitInput(bytesIn);
        for (int i = 1; i < 150; ++i)
            assertEquals(i,bitInput.readUnary());
        assertEquals(1500,bitInput.readUnary());
    }


    @Test
    public void testUnary() throws IOException {
        String bits = "1" + "01" + "00001";
        BitInput bitsIn = bitInput(bits);
        assertEquals(1,bitsIn.readUnary());
        assertEquals(2,bitsIn.readUnary());
        assertFalse(bitsIn.endOfStream());
        assertEquals(5,bitsIn.readUnary());
        assertTrue(bitsIn.endOfStream());
    }

    @Test
    public void testUnary2() throws IOException {
        String bits = "00000000" + "00000010";
        BitInput bitsIn = bitInput(bits);
        assertEquals(15,bitsIn.readUnary());
        assertFalse(bitsIn.endOfStream());
    }

    @Test(expected=IOException.class)
    public void testUnaryExceptions() throws IOException {
        String bits = "00000000";
        BitInput bitsIn = bitInput(bits);
        bitsIn.readUnary();
    }

    @Test
    public void testBitInputStream() {
        BitInputStream bitIn = new BitInputStream("00100100");
        assertEquals(1,bitIn.available());
        assertEquals(36,bitIn.read());
        assertEquals(-1,bitIn.read());

        bitIn = new BitInputStream("11111111" + "00000000" + "00000001");
        assertEquals(3,bitIn.available());
        assertEquals(255,bitIn.read());
        assertEquals(0,bitIn.read());
        assertEquals(1,bitIn.read());
        assertEquals(-1,bitIn.read());



    }

    @Test
    public void testBitInput() throws IOException {
        BitInputStream in
            = new BitInputStream("10101010" + "01101101");
        BitInput bitsIn = new BitInput(in);
        assertEquals(16l,bitsIn.available());
        assertEquals(true,bitsIn.readBit());
        assertEquals(false,bitsIn.readBit());
        assertEquals(true,bitsIn.readBit());
        assertEquals(false,bitsIn.readBit());
        assertEquals(true,bitsIn.readBit());
        assertEquals(false,bitsIn.readBit());
        assertEquals(true,bitsIn.readBit());
        assertEquals(false,bitsIn.readBit());

        assertFalse(bitsIn.endOfStream());

        assertEquals(false,bitsIn.readBit());
        assertEquals(true,bitsIn.readBit());
        assertEquals(true,bitsIn.readBit());
        assertEquals(false,bitsIn.readBit());
        assertEquals(true,bitsIn.readBit());
        assertEquals(true,bitsIn.readBit());
        assertEquals(false,bitsIn.readBit());
        assertEquals(true,bitsIn.readBit());

        assertTrue(bitsIn.endOfStream());
        assertFalse(in.mClosed);
        bitsIn.close();
        assertTrue(in.mClosed);
    }

    @Test(expected=IOException.class)
    public void testBinaryExceptions() throws IOException {
        BitInput bitsIn = bitInput("01010101");
        bitsIn.readBinary(9);
    }

    @Test
    public void testBinary() throws IOException {
        String bits = "0" + "1"
            + "00" + "01" + "10" + "11"
            + "000" + "001" + "111"
            + "0000000000000001"
            + "00000";

        BitInput bitsIn = bitInput(bits);
        assertEquals(0,bitsIn.readBinary(1));
        assertEquals(1,bitsIn.readBinary(1));

        assertEquals(0,bitsIn.readBinary(2));
        assertEquals(1,bitsIn.readBinary(2));
        assertEquals(2,bitsIn.readBinary(2));
        assertEquals(3,bitsIn.readBinary(2));

        assertEquals(0,bitsIn.readBinary(3));
        assertEquals(1,bitsIn.readBinary(3));
        assertEquals(7,bitsIn.readBinary(3));

        assertEquals(1,bitsIn.readBinary(16));

        assertFalse(bitsIn.endOfStream());

        assertEquals(0,bitsIn.readBinary(5));
        assertTrue(bitsIn.endOfStream());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRiceExceptions() throws IOException {
        BitInput bitInput = bitInput("00000001");
        bitInput.readRice(0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testRiceExceptions2() throws IOException {
        BitInput bitInput = bitInput("00000001");
        bitInput.readRice(65);
    }


    @Test(expected=IOException.class)
    public void testRiceExceptions3() throws IOException {
        BitInput bitInput = bitInput("00000001");
        bitInput.readRice(15);
    }

    @Test
    public void testRice() throws IOException {
        String bits
            = "10"
            + "11"
            + "010"
            + "011"
            + "0010"
            + "0011"
            + "0000000010"
            + "0000";
        BitInput bitsIn = bitInput(bits);
        assertEquals(1,bitsIn.readRice(1));
        assertEquals(2,bitsIn.readRice(1));
        assertEquals(3,bitsIn.readRice(1));
        assertEquals(4,bitsIn.readRice(1));
        assertEquals(5,bitsIn.readRice(1));
        assertEquals(6,bitsIn.readRice(1));
        assertEquals(17,bitsIn.readRice(1));
        assertFalse(bitsIn.endOfStream());
    }

    @Test(expected=IOException.class)
    public void testRiceExc() throws IOException {
        String bits
            = "10"
            + "11"
            + "010"
            + "011"
            + "0010"
            + "0011"
            + "0000000010"
            + "0000";
        BitInput bitsIn = bitInput(bits);
        bitsIn.readRice(1);
        bitsIn.readRice(1);
        bitsIn.readRice(1);
        bitsIn.readRice(1);
        bitsIn.readRice(1);
        bitsIn.readRice(1);
        bitsIn.readRice(1);

        bitsIn.readRice(1);
    }

    @Test
    public void testRice2() throws IOException {
        String bits
            = "100"
            + "101"
            + "110"
            + "111"
            + "0100"
            + "0101"
            + "0000100"
            + "00000";
        BitInput bitsIn = bitInput(bits);
        assertEquals(1,bitsIn.readRice(2));
        assertEquals(2,bitsIn.readRice(2));
        assertEquals(3,bitsIn.readRice(2));
        assertEquals(4,bitsIn.readRice(2));
        assertEquals(5,bitsIn.readRice(2));
        assertEquals(6,bitsIn.readRice(2));
        assertEquals(17,bitsIn.readRice(2));
        assertFalse(bitsIn.endOfStream());
    }

    @Test(expected=IOException.class)
    public void testRice2Exc() throws IOException {
        String bits
            = "100"
            + "101"
            + "110"
            + "111"
            + "0100"
            + "0101"
            + "0000100"
            + "00000";
        BitInput bitsIn = bitInput(bits);
        bitsIn.readRice(2);
        bitsIn.readRice(2);
        bitsIn.readRice(2);
        bitsIn.readRice(2);
        bitsIn.readRice(2);
        bitsIn.readRice(2);
        bitsIn.readRice(2);

        bitsIn.readRice(2); // fails
    }


    @Test
    public void testRice3() throws IOException {
        String bits
            = "1000"
            + "1001"
            + "1010"
            + "1011"
            + "1100"
            + "1101"
            + "001000"
            + "00";
        BitInput bitsIn = bitInput(bits);
        assertEquals(1,bitsIn.readRice(3));
        assertEquals(2,bitsIn.readRice(3));
        assertEquals(3,bitsIn.readRice(3));
        assertEquals(4,bitsIn.readRice(3));
        assertEquals(5,bitsIn.readRice(3));
        assertEquals(6,bitsIn.readRice(3));
        assertEquals(17,bitsIn.readRice(3));
        assertFalse(bitsIn.endOfStream());
    }

    @Test(expected=IOException.class)
    public void testRice3Exc() throws IOException {
        String bits
            = "1000"
            + "1001"
            + "1010"
            + "1011"
            + "1100"
            + "1101"
            + "001000"
            + "00";
        BitInput bitsIn = bitInput(bits);
        bitsIn.readRice(3);
        bitsIn.readRice(3);
        bitsIn.readRice(3);
        bitsIn.readRice(3);
        bitsIn.readRice(3);
        bitsIn.readRice(3);
        bitsIn.readRice(3);

        bitsIn.readRice(3);
    }

    @Test(expected=IOException.class)
    public void testFibonacciExceptions() throws IOException {
        String bits = "00000000";
        BitInput bitsIn = bitInput(bits);
        bitsIn.readFibonacci();
    }

    @Test
    public void testFibonacci() throws IOException {
        String bits
            = "11"
            + "011"
            + "0011"
            + "1011"
            + "00011"
            + "10011"
            + "01011"
            + "1010011"
            + "00000";
        BitInput bitsIn = bitInput(bits);
        assertEquals(1,bitsIn.readFibonacci());
        assertEquals(2,bitsIn.readFibonacci());
        assertEquals(3,bitsIn.readFibonacci());
        assertEquals(4,bitsIn.readFibonacci());
        assertEquals(5,bitsIn.readFibonacci());
        assertEquals(6,bitsIn.readFibonacci());
        assertEquals(7,bitsIn.readFibonacci());
        assertEquals(17,bitsIn.readFibonacci());
    }

    @Test(expected=IOException.class)
    public void testFibonacciExcs() throws IOException {
        String bits
            = "11"
            + "011"
            + "0011"
            + "1011"
            + "00011"
            + "10011"
            + "01011"
            + "1010011"
            + "00000";
        BitInput bitsIn = bitInput(bits);
        bitsIn.readFibonacci();
        bitsIn.readFibonacci();
        bitsIn.readFibonacci();
        bitsIn.readFibonacci();
        bitsIn.readFibonacci();
        bitsIn.readFibonacci();
        bitsIn.readFibonacci();
        bitsIn.readFibonacci();

        bitsIn.readFibonacci();
    }

    @Test
    public void testN2Minus1RoundTrips() throws IOException {
        long[] testVals = new long[] { 2, 3, 4, 5, 6, 7, 8,
                                       9, 10, 11, 12, 13, 14, 15 };
        testRoundTrip(testVals);
    }

    @Test
    public void testLowRoundTrips() throws IOException {
        long[] testVals = new long[1024];
        for (int i = 0; i < testVals.length; ++i)
            testVals[i] = i+1;
        testRoundTrip(testVals);
    }

    @Test
    public void testRandomRoundTrips() throws IOException {
        long[] testVals = randomVals(100,0,62);
        testRoundTrip(testVals);
    }

    void testRoundTrip(long[] testVals) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        BitOutput bitOutput = new BitOutput(bytesOut);
        for (int i = 0; i < testVals.length; ++i) {
            bitOutput.writeBinary(testVals[i],63);
            bitOutput.writeGamma(testVals[i]);
            bitOutput.writeDelta(testVals[i]);
            bitOutput.writeFibonacci(testVals[i]);
            bitOutput.writeRice(testVals[i],59);
            bitOutput.writeRice(testVals[i],61);
            bitOutput.writeRice(testVals[i],63);
        }
        bitOutput.flush();

        Random random = new Random();

        byte[] bytes = bytesOut.toByteArray();
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        BitInput bitInput = new BitInput(bytesIn);
        for (int i = 0; i < testVals.length; ++i) {
            // System.out.println("test " + i + "=" + testVals[i]);
            if (random.nextBoolean()) bitInput.skip(63);
            else assertEquals("binary",testVals[i],bitInput.readBinary(63));

            if (random.nextBoolean()) bitInput.skipGamma();
            else assertEquals("gamma",testVals[i],bitInput.readGamma());

            if (random.nextBoolean()) bitInput.skipDelta();
            else assertEquals("delta",testVals[i],bitInput.readDelta());

            if (random.nextBoolean()) bitInput.skipFibonacci();
            else assertEquals("fib",testVals[i],bitInput.readFibonacci());

            if (random.nextBoolean()) bitInput.skipRice(59);
            else assertEquals("rice59",testVals[i],bitInput.readRice(59));

            if (random.nextBoolean()) bitInput.skipRice(61);
            else assertEquals("rice61",testVals[i],bitInput.readRice(61));

            if (random.nextBoolean()) bitInput.skipRice(63);
            else assertEquals("rice63",testVals[i],bitInput.readRice(63));
        }
    }

    public static long[] randomVals(int sizePer, int minShift, int maxShift) {
        Random random = new Random();
        long[] vals = new long[sizePer * (maxShift-minShift+1)];
        int index = 0;
        for (int shift = minShift; shift <= maxShift; ++shift) {
            for (int i = 0; i < sizePer; ++i) {
                long n = random.nextLong();
                if (n < 0) n = -(n+1);
                n >>>= shift;
                if (n == 0) n = n+1;
                vals[index++] = n;
            }
        }
        return vals;
    }

    @Test(expected=IOException.class)
    public void testGammaException() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 75; ++i)
            sb.append("0");
        sb.append("1");
        sb.append("00000001010101010000");
        BitInput bitsIn = bitInput(sb.toString());
        bitsIn.readGamma();
    }


    @Test(expected=IOException.class)
    public void testGammaException2() throws IOException {
        BitInput bitsIn = bitInput("00001000");
        bitsIn.readGamma();
    }

    @Test
    public void testGamma() throws IOException {
        String bits = "1" + "010" + "011"
            + "00100" + "00101"
            + "000010001"
            + "000000"; // padding
        BitInput bitsIn = bitInput(bits);
        assertEquals(1,bitsIn.readGamma());
        assertEquals(2,bitsIn.readGamma());
        assertEquals(3,bitsIn.readGamma());
        assertEquals(4,bitsIn.readGamma());
        assertEquals(5,bitsIn.readGamma());
        assertEquals(17,bitsIn.readGamma());
        assertFalse(bitsIn.endOfStream());
        try { bitsIn.readGamma(); } catch (IOException e) { /* swallow */ }
        assertTrue(bitsIn.endOfStream());
    }


    @Test(expected=IOException.class)
    public void testGammaExc() throws IOException {
        String bits = "1" + "010" + "011"
            + "00100" + "00101"
            + "000010001"
            + "000000"; // padding
        BitInput bitsIn = bitInput(bits);
        bitsIn.readGamma();
        bitsIn.readGamma();
        bitsIn.readGamma();
        bitsIn.readGamma();
        bitsIn.readGamma();
        bitsIn.readGamma();

        bitsIn.readGamma();
    }

    @Test(expected=IOException.class)
    public void testDeltaExceptions() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("00000001" + "1111111");
        for (int i = 0; i < 305; ++i) sb.append("0");
        BitInput bitsIn = bitInput(sb.toString());

        bitsIn.readDelta();
    }


    @Test(expected=IOException.class)
    public void testDeltaExceptions2() throws IOException {
        BitInput bitsIn = bitInput("00111000");
        bitsIn.readDelta();
    }

    @Test
    public void testDelta() throws IOException {
        String bits = "1"
            + "0100"
            + "0101"
            + "01100"
            + "01101"
            + "001010001"
            + "0000";
        BitInput bitsIn = bitInput(bits);
        assertEquals(1,bitsIn.readDelta());
        assertEquals(2,bitsIn.readDelta());
        assertEquals(3,bitsIn.readDelta());
        assertEquals(4,bitsIn.readDelta());
        assertEquals(5,bitsIn.readDelta());
        assertEquals(17,bitsIn.readDelta());
        assertFalse(bitsIn.endOfStream());
    }

    @Test(expected=IOException.class)
    public void testDeltaExc() throws IOException {
        String bits = "1"
            + "0100"
            + "0101"
            + "01100"
            + "01101"
            + "001010001"
            + "0000";
        BitInput bitsIn = bitInput(bits);
        bitsIn.readDelta();
        bitsIn.readDelta();
        bitsIn.readDelta();
        bitsIn.readDelta();
        bitsIn.readDelta();
        bitsIn.readDelta();

        bitsIn.readDelta();
    }

    @Test
    public void testSkip() throws IOException {

        BitInputStream in
            = new BitInputStream("10101010" + "01101101");
        BitInput bitsIn = new BitInput(in);

        assertEquals(7,bitsIn.skip(7));
        assertFalse(bitsIn.readBit());
        assertEquals(8,bitsIn.skip(9));
        assertTrue(bitsIn.endOfStream());


        in = new BitInputStream("11111111" + "00000000" + "00000001");
        bitsIn = new BitInput(in);
        bitsIn.readBit();
        bitsIn.readBit();
        assertEquals(9,bitsIn.skip(9));
        assertEquals(13,bitsIn.available());

        in = new BitInputStream("11111111" + "00000000" + "00000001");
        bitsIn = new BitInput(in);
        bitsIn.readBit();
        assertEquals(23,bitsIn.skip(23));
        assertTrue(bitsIn.endOfStream());

    }

    @Test(expected=IllegalArgumentException.class)
    public void testSkipExc() throws IOException {
        BitInputStream in
            = new BitInputStream("10101010" + "01101101");
        BitInput bitsIn = new BitInput(in);
        bitsIn.skip(-12);
    }



    static BitInput bitInput(String inputBits) throws IOException {
        return new BitInput(new BitInputStream(inputBits));
    }

    static class BitInputStream extends ByteArrayInputStream {
        boolean mClosed = false;
        public BitInputStream(String inputBits) {
            super(bitsToBytes(inputBits));
        }
        static byte[] bitsToBytes(String bits) {
            if (bits.length() % 8 != 0) {
                String msg = "bits.length()=" + bits.length();
                throw new IllegalArgumentException(msg);
            }
            byte[] bytes = new byte[bits.length() / 8];
            for (int i = 0, n=0; i < bits.length(); i += 8, ++n)
                bytes[n] = (byte) Integer.parseInt(bits.substring(i,i+8),2);
            return bytes;
        }
        @Override
        public void close() {
            mClosed = true;
        }
    }

}
