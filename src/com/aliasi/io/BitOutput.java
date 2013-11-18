package com.aliasi.io;

import com.aliasi.util.Math;

import java.io.OutputStream;
import java.io.IOException;

/** 
 * A <code>BitOutput</code> wraps an underlying output stream to
 * provide bit-level output.  Output is written through the method
 * {@link #writeBit(boolean)}, with <code>true</code> used for the bit
 * <code>1</code> and <code>false</code> for the bit <code>0</code>.
 * The methods {@link #writeTrue()} and {@link #writeFalse()} are
 * shorthand for <code>writeBit(true)</code> and
 * <code>writeBit(false)</code> respectively.
 *
 * <P>If the number of bits written before closing the output does not
 * land on a byte boundary, the remaining fractional byte is filled
 * with <code>0</code> bits.
 *
 * <P>None of the methods in this class are safe for concurrent access
 * by multiple threads.
 *
 * @author Bob Carpenter
 * @version 2.1.1
 * @since LingPipe2.1.1
 */
public class BitOutput {

    private int mNextByte;
    private int mNextBitIndex;
    private final OutputStream mOut;

    /** 
     * Construct a bit output wrapping the specified output stream.
     *
     * @param out Underlying output stream.
     */
    public BitOutput(OutputStream out) {
	mOut = out;
	reset();
    }

    /**
     * Writes the bits for a unary code for the specified positive
     * number.  The unary code for the number <code>n</code> is
     * defined by:
     *
     * <blockquote><code>
     * unaryCode(n) = 0<sup><sup>n-1</sup></sup> 1
     * </code></blockquote>
     *
     * In words, the number <code>n</code> is coded as
     * <code>n-1</code> zeros followed by a one.  The following
     * table illustrates the first few unary codes:
     *
     * <blockquote><table border='1' cellpadding='5'>
     * <tr><td><i>Number</i></td><td><i>Code</i></td></tr>
     * <tr><td>1</td><td><code>1</code></td></tr>
     * <tr><td>2</td><td><code>01</code></td></tr>
     * <tr><td>3</td><td><code>001</code></td></tr>
     * <tr><td>4</td><td><code>0001</code></td></tr>
     * <tr><td>5</td><td><code>00001</code></td></tr>
     * </table></blockquote>
     * 
     * @param n Number to code.
     * @throws IOException If there is an I/O error writing
     * to the underlying output stream.
     * @throws IllegalArgumentException If the number to be encoded is
     * zero or negative.
     */
    public void writeUnary(int n) throws IOException {
	validatePositive(n);

	// fit in buffer
	int numZeros = n - 1;
	if (numZeros <= mNextBitIndex) {
	    mNextByte = mNextByte << numZeros;
	    mNextBitIndex -= numZeros;
	    writeTrue();
	    return;
	} 

	// fill buffer, write and flush
	// numZeros > mNextBitIndex
	mOut.write(mNextByte << mNextBitIndex);
	numZeros -= (mNextBitIndex+1);
	reset();

	// fill in even multiples of eight
	for (; numZeros >= 8; numZeros -= 8)
	    mOut.write(ZERO_BYTE);


	// fill in last zeros
	mNextBitIndex -= numZeros;
	writeTrue();
    }

    /**
     * Writes the bits of a binary representation of the specified
     * non-negative number in the specified number of bits.  if the
     * number will not fit in the number of bits specified, an
     * exception is raised.
     *
     * <P>For instance, the following illustrates one, two and
     * three-bit codings.
     * 
     * <blockquote><table border='1' cellpadding='5'>
     * <tr><td rowspan='2'><i>Number</i></td>
     *     <td rowspan='2'><i>Binary</i></td>
     *     <td colspan='3'><i>Code for Num Bits</i></td></tr>
     * <tr><td><i>1</i></td> <td><i>2</i></td> <td><i>3</i></td></tr>
     * <tr><td>0</td><td>1</td>  
     *     <td>0</td> 
     *     <td>00</td> 
     *     <td>000</td></tr>
     * <tr><td>1</td><td>1</td>  
     *     <td>1</td> 
     *     <td>01</td> 
     *     <td>001</td></tr>
     * <tr><td>2</td><td>10</td> 
     *     <td><code>Exception</code></td>
     *     <td>10</td>
     *     <td>010</td></tr>
     * <tr><td>3</td><td>10</td> 
     *     <td><code>Exception</code></td>
     *     <td>11</td>
     *     <td>011</td></tr>
     * <tr><td>4</td><td>100</td> 
     *     <td><code>Exception</code></td>
     *     <td><code>Exception</code></td>
     *     <td>100</td></tr>
     * <tr><td>5</td><td>101</td> 
     *     <td><code>Exception</code></td>
     *     <td><code>Exception</code></td>
     *     <td>101</td></tr>
     * <tr><td>6</td><td>110</td> 
     *     <td><code>Exception</code></td>
     *     <td><code>Exception</code></td>
     *     <td>110</td></tr>
     * <tr><td>7</td><td>111</td> 
     *     <td><code>Exception</code></td>
     *     <td><code>Exception</code></td>
     *     <td>111</td></tr>
     * <tr><td>8</td><td>1000</td> 
     *     <td><code>Exception</code></td>
     *     <td><code>Exception</code></td>
     *     <td><code>Exception</code></td></tr>
     * </table></blockquote>
     *
     * @param n Number to code.
     * @param numBits Number of bits to use for coding.
     * @throws IllegalArgumentException If the number to code is
     * negative, the number of bits is greater than 63, or the number
     * will not fit into the specified number of bits.
     * @throws IOException If there is an error writing to the
     * underlying output stream.
     */
    public void writeBinary(long n, int numBits) throws IOException {
	validateNonNegative(n);
	validateNumBits(numBits);
	int k = mostSignificantPowerOfTwo(n);
	if (k >= numBits) {
	    String msg = "Number will not fit into number of bits."
		+ " n=" + n
		+ " numBits=" + numBits;
	    throw new IllegalArgumentException(msg);
	}
	writeLowOrderBits(numBits,n);
    }

    /**
     * Writes the bits for Rice code for the specified non-negative
     * number with the specified number of bits fixed for the binary
     * remainder.  Rice coding is a form of Golomb coding where the
     * Golomb paramemter is a power of two (2 to the number of bits in
     * the remainder).  The Rice code is defined by unary coding a
     * magnitude and then binary coding the remainder.  It can be
     * defined by taking a quotient and remainder:
     *
     * <blockquote>
     * <table border='0' cellpadding='5'>
     * <tr><td align='right'>m</td> <td>=</td> <td>2<sup><sup>b</sup></sup></td> 
     *                  <td>=</td> <td>(1&lt;&lt;b)</td></tr>
     * <tr><td align='right'>q</td> <td>=</td> <td>(n - 1) / m</td>
     *                <td>=</td> <td>(n - 1) &gt;&gt;&gt; b</td></tr>
     * <tr><td align='right'>r</td> <td>=</td> <td>n - q*m - 1</td> 
     *                <td>=</td> <td>n - (q &lt;&lt; b) - 1</td></tr>
     * </table>
     * </code></blockquote>
     *
     * both of which are defined by shifting, and then coding each
     * in turn using a unary code for the quotient and binary code for
     * the remainder:
     * 
     * <blockquote><code>
     * riceCode(n,b) = unaryCode(q) binaryCode(r)
     * </code></blockquote>
     *
     * For example, we get the following codes with the number of
     * fixed remainder bits set to 1, 2 and 3, with the unary coded
     * quotient separated from the binary coded remainder by a space:
     *
     * <blockquote><table border='1' cellpadding='5'>
     * <tr><td rowspan='2'>Number<br><code>n</code></td>
     *     <td rowspan='2'>Binary</td>
     *     <td colspan='3'>Code for Number of Remainder Bits</td></tr>
     * <tr><td>b=<i>1</i></td> <td>b=<i>2</i></td> <td>b=<i>3</i></td></tr>
     * <tr><td>1</td> <td>1</td> 
     *     <td>1 0</td> <td>1 00</td> <td>1 000</td></tr>
     * <tr><td>2</td> <td>10</td> 
     *     <td>1 1</td> <td>1 01</td> <td>1 001</td></tr>
     * <tr><td>3</td> <td>11</td> 
     *     <td>01 0</td> <td>1 10</td> <td>1 010</td></tr>
     * <tr><td>4</td> <td>100</td> 
     *     <td>01 1</td> <td>1 11</td> <td>1 011</td></tr>
     * <tr><td>5</td> <td>101</td> 
     *     <td>001 0</td> <td>01 00</td> <td>1 100</td></tr>
     * <tr><td>6</td> <td>110</td> 
     *     <td>001 1</td> <td>01 01</td> <td>1 101</td></tr>
     * <tr><td>7</td> <td>111</td> 
     *     <td>0001 0</td> <td>01 10</td> <td>1 110</td></tr>
     * <tr><td>8</td> <td>1000</td> 
     *     <td>0001 1</td> <td>01 11</td> <td>1 111</td></tr>
     * <tr><td>9</td> <td>1001</td> 
     *     <td>00001 0</td> <td>001 00</td> <td>01 000</td></tr>
     * <tr><td>10</td> <td>1010</td> 
     *     <td>00001 1</td> <td>001 01</td> <td>01 001</td></tr>
     * <tr><td>11</td> <td>1011</td> 
     *     <td>000001 0</td> <td>001 10</td> <td>01 010</td></tr>
     * <tr><td>12</td> <td>1100</td> 
     *     <td>000001 1</td> <td>001 11</td> <td>01 011</td></tr>
     * <tr><td>13</td> <td>1101</td> 
     *     <td>0000001 0</td> <td>0001 00</td> <td>01 100</td></tr>
     * <tr><td>14</td> <td>1110</td> 
     *     <td>0000001 1</td> <td>0001 01</td> <td>01 101</td></tr>
     * <tr><td>15</td> <td>1111</td> 
     *     <td>00000001 0</td> <td>0001 10</td> <td>01 110</td></tr>
     * <tr><td>16</td> <td>10000</td> 
     *     <td>00000001 1</td> <td>0001 11</td> <td>01 111</td></tr>
     * <tr><td>17</td> <td>10001</td> 
     *     <td>000000001 0</td> <td>00001 00</td> <td>001 000</td></tr>
     * </table></blockquote>
     * 
     * In the limit, if the number of remaining bits to code is set to
     * zero, the Rice code would reduce to a unary code:
     *
     * <blockquote><code>
     * riceCode(n,0) = unaryCode(n)
     * </code></blockquote>
     *
     * but this method will throw an exception with a remainder size
     * of zero.
     *
     * <P>In the limit the other way, if the number of remaining bits
     * is set to the width of the maximum value, the Rice code is just
     * the unary coding of 1, which is the single binary digit 1,
     * followed by the binary code itself:
     *
     * <blockquote><code>
     * riceCode(n,64) = unaryCode(1) binaryCode(n,64) = 1 binaryCode(n,64)
     * </code></blockquote>
     * 
     * <P>The method will throw an exception if the encoding
     * produces a unary code that would output more bits
     * than would fit in a positive integer (that is, more
     * than (2<sup><sup>32</sup></sup>-1) bits.
     * 
     * For more information, see:
     * 
     * <UL>
     *
     * <LI> Golomb, S. 1966. Run-length encodings. <i>IEEE
     * Trans. Inform. Theory</i>.  <bf>12</bf>(3):399-401.
     *
     * <LI> Rice, R. F. 1979. Some practical universal noiseless
     * coding techniques. <i>JPL Publication 79-22</i>. March 1979.
     *
     * <LI> Witten, Ian H., Alistair Moffat, and Timothy C. Bell.
     * 1999. <i>Managing Gigabytes</i>. Academic Press.
     *
     * <LI> <a href="http://en.wikipedia.org/wiki/Golomb_coding"
     * >Wikipedia: Golomb coding</a>
     *
     * </UL>
     *
     * @param n Number to code.
     * @param numFixedBits Number of bits to use for the fixed
     * remainder encoding.  
     * @throws IOException If there is an error writing to the
     * underlying output stream.
     * @throws IllegalArgumentException If the number to be encoded is
     * not positive, if the number of fixed bits is not positive, or if
     * the unary prefix code overflows.
     */
    public void writeRice(long n, int numFixedBits) throws IOException {
	validatePositive(n);
	validateNumBits(numFixedBits);
	long q = (n - 1l) >> numFixedBits;
	long prefixBits = q + 1l;
	if (prefixBits >= Integer.MAX_VALUE) {
	    String msg = "Prefix too long to code."
		+ " n=" + n
		+ " numFixedBits=" + numFixedBits
		+ " number of prefix bits=(n>>numFixBits)=" + prefixBits;
	    throw new IllegalArgumentException(msg);
	}
	writeUnary((int) prefixBits);
	long remainder = n - (q << numFixedBits) - 1;
	writeLowOrderBits(numFixedBits,remainder);
    }

    /**
     * Writes the Fibonacci code for the specified positive number.
     * Roughly speaking, the Fibonacci code specifies a number
     * as a sum of non-consecutive Fibonacci numbers, terminating
     * a representation with two consecutive 1 bits.  
     *
     * <P>Fibonacci
     * numbers are defined by setting 
     *
     * <blockquote><pre>
     * Fib(0) = 0
     * Fib(1) = 1
     * Fib(n+2) = Fib(n+1) + Fib(n)
     * </pre></blockquote>
     *
     * The first few Fibonacci numbers are:
     *
     * <blockquote><code>
     * 0, 1, 1, 2, 3, 5, 8, 13, 21, ...
     * </code></blockquote>
     *
     * This method starts with the second <code>1</code> value,
     * namely <code>Fib(2)</code>, making the sequence a sequence
     * of unique numbers starting with <code>1, 2, 3, 5,...</code>.
     *
     * <P>The Fibonacci representation of a number is a bit vector
     * indicating the Fibonacci numbers used in the sum.  The
     * Fibonacci code reverses the Fibonacci representation and
     * appends a 1 bit.  Here are examples for the first 17 numbers:
     *
     * <blockquote><table border='1' cellpadding='5'>
     * <tr><td><i>Number</i></td> <td><i>Fibonacci Representation</i></td>
     *     <td><i>Fibonacci Code</i></td></tr>
     * <tr><td>1</td> <td>1</td> <td>11</td></tr>
     * <tr><td>2</td> <td>10</td> <td>01 1</td></tr>
     * <tr><td>3</td> <td>100</td> <td>001 1</td></tr>
     * <tr><td>4</td> <td>101</td> <td>101 1</td></tr>
     * <tr><td>5</td> <td>1000</td> <td>0001 1</td></tr>
     * <tr><td>6</td> <td>1001</td> <td>1001 1</td></tr>
     * <tr><td>7</td> <td>1010</td> <td>0101 1</td></tr>
     * <tr><td>8</td> <td>10000</td> <td>00001 1</td></tr>
     * <tr><td>9</td> <td>10001</td> <td>10001 1</td></tr>
     * <tr><td>10</td> <td>10010</td> <td>01001 1</td></tr>
     * <tr><td>11</td> <td>10100</td> <td>00101 1</td></tr>
     * <tr><td>12</td> <td>10101</td> <td>10101 1</td></tr>
     * <tr><td>13</td> <td>100000</td> <td>000001 1</td></tr>
     * <tr><td>14</td> <td>100001</td> <td>100001 1</td></tr>
     * <tr><td>15</td> <td>100010</td> <td>010001 1</td></tr>
     * <tr><td>16</td> <td>100100</td> <td>001001 1</td></tr>
     * <tr><td>17</td> <td>100101</td> <td>101001 1</td></tr>
     * </table></blockquote>
     *
     * For example, the number 11 is coded as the sum of the
     * non-consecutive Fibonacci numbers 8 + 3, so the Fibonacci
     * representation is <code>10100</code> (8 is the fifth number in
     * the series above, 3 is the third).  Its Fibonacci code reverses
     * the number to <code>00101</code> and appends a <code>1</code>
     * to yield <code>001011</code>.
     *
     * <P>Fibonacci codes can represent arbitrary positive numbers up
     * to <code>Long.MAX_VALUE</code>.  
     *
     * <P>See {@link Math#FIBONACCI_SEQUENCE} for a definition of
     * the Fibonacci sequence as an array of longs.
     *
     * <P>In the limit (for larger numbers), the number of bits
     * used by a Fibonacci coding is roughly 60 percent higher
     * than the number of bits used for a binary code.  The benefit
     * is that Fibonacci codes are prefix codes, whereas binary codes
     * are not.
     *
     * @param n Number to encode.
     * @throws IllegalArgumentException If the number is not positive.
     * @throws IOException If there is an I/O exception writing to the
     * underlying stream.
     */
    public void writeFibonacci(long n) throws IOException {
	validatePositive(n);
	long[] fibs = Math.FIBONACCI_SEQUENCE;
	boolean[] buf = FIB_BUF;
	int mostSigPlace = mostSigFibonacci(fibs,n);
	for (int place = mostSigPlace; place >= 0; --place) {
	    if (n >= fibs[place]) {
		n -= fibs[place];
		buf[place] = true;
	    } else {
		buf[place] = false;
	    }
	}
	for (int i = 0; i <= mostSigPlace; ++i)
	    writeBit(buf[i]);
	writeTrue();
    }


    /**
     * Writes the bits for the Elias gamma code for the specified
     * positive number.  The gamma code of the number <code>n</code>
     * is based on its binary representation <code>b[k-1],...,b[0]</code>:
     *
     * <blockquote><code>
     * gammaCode(b[k-1],...,b[0]) = unaryCode(k),b[k-1],...,b[0]
     * </code></blockquote>
     *
     * In words, the position of the most significant binary digit is
     * coded using a unary code, with the remaining digits making up
     * the rest of the gamma code.
     *
     * <P>The Following table provides an illustration of the gamma
     * coding of the first 17 positive integers.  Each row displays
     * the number being coded, its binary representation, and its
     * gamma code.  The gamma code is displayed as its unary coding of
     * the number of digits in the binary representation followed by a
     * space and then by the digits of the binary representation after
     * the first one.
     *
     * <blockquote><table border='1' cellpadding='5'>
     * <tr><td><i>Number</i></td>
     *     <td><i>Binary</i></td>
     *     <td><i>Gamma code</i></td></tr>
     * <tr><td>1</td><td>1</td><td>1</td></tr>
     * <tr><td>2</td><td>10</td><td>01 0</td></tr>
     * <tr><td>3</td><td>11</td><td>01 1</td></tr>
     * <tr><td>4</td><td>100</td><td>001 00</td></tr>
     * <tr><td>5</td><td>101</td><td>001 01</td></tr>
     * <tr><td>6</td><td>110</td><td>001 10</td></tr>
     * <tr><td>7</td><td>111</td><td>001 11</td></tr>
     * <tr><td>8</td><td>1000</td><td>0001 000</td></tr>
     * <tr><td>9</td><td>1001</td><td>0001 001</td></tr>
     * <tr><td>10</td><td>1010</td><td>0001 010</td></tr>
     * <tr><td>11</td><td>1011</td><td>0001 011</td></tr>
     * <tr><td>12</td><td>1100</td><td>0001 100</td></tr>
     * <tr><td>13</td><td>1101</td><td>0001 101</td></tr>
     * <tr><td>14</td><td>1110</td><td>0001 110</td></tr>
     * <tr><td>15</td><td>1111</td><td>0001 111</td></tr>
     * <tr><td>16</td><td>10000</td><td>00001 0000</td></tr>
     * <tr><td>17</td><td>10001</td><td>00001 0001</td></tr>
     * </table></blockquote>
     *
     * For more information on gamma coding, see:
     * 
     * <UL>
     * <LI> Witten, Ian H., Alistair Moffat, and Timothy C. Bell.
     * 1999. <i>Managing Gigabytes</i>. Academic Press.
     * <LI> <a href="http://en.wikipedia.org/wiki/Elias_gamma_coding"
     *       >Wikipedia: Elias gamma coding</a>
     * </UL>
     *
     * @param n Number to code.
     * @throws IOException If there is an I/O error writing to the
     * underlying stream.
     * @throws IllegalArgumentException If the number to be encoded is
     * zero or negative.
     */
    public void writeGamma(long n) throws IOException {
	validatePositive(n);
	if (n == 1l) {
	    writeTrue();
	    return;
	}
	int k = mostSignificantPowerOfTwo(n);
	writeUnary(k+1);
	writeLowOrderBits(k,n);
    }


    /**
     * Writes the bits for the Elias delta code for the specified
     * positive number.  The delta code of the number <code>n</code>
     * is based on its binary representation
     * <code>b[k-1],...,b[0]</code>:
     *
     * <blockquote><code>
     * deltaCode(b[k-1],...,b[0]) = gammaCode(k),b[k-1],...,b[0]
     * </code></blockquote>
     *
     * In words, the position of the most significant binary digit is
     * coded using a gamma code, with the remaining digits making up
     * the rest of the gamma code.  
     *
     * <P>The following table illustrates the delta codes for some
     * small numbers.  Each row lists the number, its binary
     * representation, and its delta code.  The delta code is
     * written as the initial gamma code of its most significant digit's
     * position and the remaining bits in the binary representation.
     * Note that the delta codes are longer for small numbers,
     * but shorter for large numbers.  
     *
     * <blockquote><table border='1' cellpadding='5'>
     * <tr><td><i>Number</i></td>
     *     <td><i>Binary</i></td>
     *     <td><i>Delta code</i></td></tr>
     * <tr><td>1</td><td>1</td><td>1</td></tr>
     * <tr><td>2</td><td>10</td><td>010 0</td></tr>
     * <tr><td>3</td><td>11</td><td>010 1</td></tr>
     * <tr><td>4</td><td>100</td><td>011 00</td></tr>
     * <tr><td>5</td><td>101</td><td>011 01</td></tr>
     * <tr><td>6</td><td>110</td><td>011 10</td></tr>
     * <tr><td>7</td><td>111</td><td>011 11</td></tr>
     * <tr><td>8</td><td>1000</td><td>00100 000</td></tr>
     * <tr><td>9</td><td>1001</td><td>00100 001</td></tr>
     * <tr><td>10</td><td>1010</td><td>00100 010</td></tr>
     * <tr><td>11</td><td>1011</td><td>00100 011</td></tr>
     * <tr><td>12</td><td>1100</td><td>00100 100</td></tr>
     * <tr><td>13</td><td>1101</td><td>00100 101</td></tr>
     * <tr><td>14</td><td>1110</td><td>00100 110</td></tr>
     * <tr><td>15</td><td>1111</td><td>00100 111</td></tr>
     * <tr><td>16</td><td>10000</td><td>00101 0000</td></tr>
     * <tr><td>17</td><td>10001</td><td>00101 0001</td></tr>
     * </table></blockquote>
     *
     * For more information on delta coding, see:
     * 
     * <UL>
     * <LI> Witten, Ian H., Alistair Moffat, and Timothy C. Bell.
     * 1999. <i>Managing Gigabytes</i>. Academic Press.
     * <LI> <a href="http://en.wikipedia.org/wiki/Elias_delta_coding"
     *       >Wikipedia: Elias delta coding</a>
     * </UL>
     *
     * @param n Number to code.
     * @throws IOException If there is an I/O error writing to the
     * underlying stream.
     * @throws IllegalArgumentException If the number to be encoded is
     * zero or negative.
     */
    public void writeDelta(long n) throws IOException {
	validatePositive(n);
	int numBits = mostSignificantPowerOfTwo(n); // 1 to 63
	if (numBits > 63) {
	    throw new IOException("numBits too large=" + numBits);
	}
	writeGamma(numBits+1);
	if (numBits > 0) 
	    writeLowOrderBits(numBits,n);
    }

    /**
     * Closes underlying output stream and releases any resources
     * associated with the stream.  This method first flushes the
     * output stream, which sets any remaining bits in the byte
     * currently being written to <code>0</code>.
     * 
     * <P>The close method calls the {@link OutputStream#close()}
     * method on the contained output stream.
     *
     * @throws IOException If there is an I/O exception writing the
     * next byte or closing the underlying output stream.
     */
    public void close() throws IOException {
	flush();
	mOut.close();
    }

    /** 
     * Flushes writes to the underlying output stream.  First, this
     * method sets any bits remaining in the current byte to
     * <code>0</code>.  It then calls {@link OutputStream#flush()} on
     * the underlying output stream.
     
     * @throws IOException If there is an exception writing to or
     * flushing the underlying output stream.
     */
    public void flush() throws IOException {
	if (mNextBitIndex < 7) {
	    mOut.write(mNextByte << mNextBitIndex); // shift to fill
	    reset();
	}
	mOut.flush();
    }
    
    /** 
     * Writes the specified bit.  The boolean <code>true</code> is
     * used for the bit <code>1</code> and <code>false</code> for
     * <code>0</code>.
     * 
     * @param bit Value to write.
     * @throws IOException If there is an exception writing to the
     * underlying output stream.
     */
    public void writeBit(boolean bit) throws IOException {
	if (bit) writeTrue();
	else writeFalse();
    }

    /** 
     * Writes a single <code>true</code> (<code>1</code>) bit.
     * 
     * @throws IOException If there is an exception writing to the
     * underlying output stream.
     */
    public void writeTrue() throws IOException {
	if (mNextBitIndex == 0) {
	    mOut.write(mNextByte | 1);
	    reset();
	} else {
	    mNextByte = (mNextByte | 1) << 1;
	    --mNextBitIndex;
	}
    }

    /** 
     * Writes a single <code>false</code> (<code>0</code>) bit.
     * 
     * @throws IOException If there is an exception writing to the
     * underlying output stream.
     */
    public void writeFalse() throws IOException {
	if (mNextBitIndex == 0) {
	    mOut.write(mNextByte);
	    reset();
	} else {
	    mNextByte <<= 1;
	    --mNextBitIndex;
	}
    }

    // writes out k lowest bits
    private void writeLowOrderBits(int numBits, long n) throws IOException {
	/* simple version that works:
	   while (--numBits >= 0)  
	   writeBit(((ONE << numBits) & n) != 0);
	*/

	// if fits without output, pack and return
	if (mNextBitIndex >= numBits) {
	    mNextByte 
		= ( (mNextByte << (numBits-1))
		    | (int) leastSignificantBits2(n,numBits))
		<< 1;
	    mNextBitIndex -= numBits;
	    return;
	}

	// pack rest of bit buffer and output
	numBits -= (mNextBitIndex + 1);
	mOut.write((mNextByte << mNextBitIndex) 
		   | (int) sliceBits2(n,numBits,mNextBitIndex+1));
           
	// write even numbers of bytes where available
	while (numBits >= 8) {
	    numBits -= 8;
	    mOut.write((int) sliceBits2(n,numBits,8));
	}

	// write remainder
	if (numBits == 0) {
	    reset();
	    return;
	}
	mNextByte = ((int) leastSignificantBits2(n,numBits)) << 1;
	mNextBitIndex = 7 - numBits;
    }

    private void reset() {
	mNextByte = 0;
	mNextBitIndex = 7;
    }

    private static final long ALL_ONES_LONG = ~0l;

    // not thread safe anyway, so might as well spend 800 bytes for class
    private static final boolean[] FIB_BUF 
	= new boolean[Math.FIBONACCI_SEQUENCE.length+1];

    private static final byte ZERO_BYTE = (byte) 0;
    

    /**
     * Returns the specified number of the least significant bits of
     * the specified long value as a long.  For example,
     * <code>leastSignificantBits(13,2) = 3</code>, because 13 is
     * <code>1011</code> in binary and the two least significant
     * digits are <code>11</code>. 
     * 
     * @param n Value whose least significant bits are returned.
     * @param numBits The number of bits to return.
     * @return The least significant number of bits.
     * @throws IllegalArgumentException If the number of bits is less than
     * 1 or greater than 64.
     */
    public static long leastSignificantBits(long n, int numBits) {
	if (numBits < 1 || numBits > 64) {
	    String msg = "Number of bits must be between 1 and 64 inclusive."
		+ " Found numBits=" + numBits;
	    throw new IllegalArgumentException(msg);
	}
	return leastSignificantBits2(n,numBits);
    }

    /**
     * Returns a slice of bits in the specified long value running
     * from the specified least significant bit for the specified
     * number of bits.  The bits are indexed in increasing order of
     * significance from 0 to 63.  So for the binary <code>110</code>,
     * the bit indexed 0 is 0, the bit indexed 1 is 1 and the bit
     * indexed 2 is 1.  For example, <code>sliceBits(57,2,3) =
     * 6</code>, because 57 is <code>111001</code> in binary and the
     * three bits extending to the left from position 2 are
     * <code>110</code>, which is 2.
     *
     * @param n Value to be sliced.
     * @param leastSignificantBit Index of least significant bit in
     * the result.
     * @param numBits Number of bits including least significant bit
     * to return.
     * @throws IllegalArgumentException If the number of bits is less
     * than zero or greater than 64, or if the least significant bit
     * index is less than 0 or greater than 63.
     */
    public static long sliceBits(long n, int leastSignificantBit, 
				 int numBits) {
	if (leastSignificantBit < 0 || leastSignificantBit > 63) {
	    String msg = "Least significant bit must be between 0 and 63."
		+ " Found leastSignificantBit=" + leastSignificantBit;
	    throw new IllegalArgumentException(msg);
	}
	if (numBits < 1 || numBits > 64) {
	    String msg = "Number of bits must be between 1 and 64 inclusive."
		+ " Found numBits=" + numBits;
	    throw new IllegalArgumentException(msg);
	}
	return sliceBits2(n,leastSignificantBit,numBits);
    }

    static long leastSignificantBits2(long n, int numBits) {
	return (ALL_ONES_LONG >>> (64-numBits)) & n;
    }

    static long sliceBits2(long n, int leastSignificantBit, int numBits) {
	return leastSignificantBits2(n >>> leastSignificantBit,
				     numBits);
    }

    /**
     * Returns the index of the most significant bit filled for the
     * specified long value.  For example, 
     *
     * <blockquote><pre>
     * mostSignificantPowerOfTwo(1) = 0
     * mostSignificantPowerOfTwo(2) = 1
     * mostSignificantPowerOfTwo(4) = 2
     * mostSignificantPowerOfTwo(8) = 3
     * </pre></blockquote>
     *
     * <p>This result of this method may be defined in terms of
     * the built-in method {@link Long#numberOfLeadingZeros(long)}, added
     * in Java 1.5, by:
     *
     * <blockquote><pre>
     * mostSignificantPowerOfTwo(n) = Math.max(0,63-Long.numberOfLeadingZeros(n))
     * </pre></blockquote>
     * 
     * @param n The specified value.
     * @return The most significant power of 2 of the specified value.
     */
    public static int mostSignificantPowerOfTwo(long n) {
	int sum = (n >> 32 != 0) ? 32 : 0;
	if (n >> (sum | 16) != 0) sum = (sum | 16);
	if (n >> (sum | 8) != 0) sum = (sum | 8);
	if (n >> (sum | 4) != 0) sum = (sum | 4);
	if (n >> (sum | 2) != 0) sum = (sum | 2);
	return (n >> (sum | 1) != 0) ? (sum | 1) : sum;
    }

    static int mostSigFibonacci(long[] fibs, long n) {
	int low = 0;
	int high = fibs.length-1;
	while (low <= high) {
	    int mid = (low + high) / 2;
	    if (fibs[mid] < n)
		low = (low == mid) ? mid+1 : mid;
	    else if (fibs[mid] > n)
		high = (high == mid) ? mid-1 : mid;
	    else return mid;
	}
	return low-1;
    }

    static void validateNumBits(int numBits) {
	if (numBits > 0) return;
	String msg = "Number of bits must be positive."
	    + " Found numBits=" + numBits;
	throw new IllegalArgumentException(msg);
    }

    static void validatePositive(long n) {
	if (n > 0) return;
	String msg = "Require number greater than zero."
	    + " Found n=" + n;
	throw new IllegalArgumentException(msg);
    }

    static void validateNonNegative(long n) {
	if (n >= 0) return;
	String msg = "Require non-negative number."
	    + " Found n=" + n;
	throw new IllegalArgumentException(msg);
    }


}
