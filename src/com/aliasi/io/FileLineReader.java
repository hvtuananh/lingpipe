/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.io;

import com.aliasi.util.Iterators;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.LineNumberReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.zip.GZIPInputStream;

/**
 * A {@code FileLineReader} instance represents the lines of a file.  The
 * lines may be streamed through an iterator or eturned all at once.
 * This class encapsulates good practices for resources and adapts
 * file line reading to for-each loops by implementing the {@link
 * Iterable} interface.
 *
 * <p>The definition of a line is defined by the {@link
 * java.io.BufferedReader} class's {@link java.io.BufferedReader#readLine()} method.
 *
 * <p>
 * The two standard usage patterns are streaming and all-at-once
 * reading.  For streaming, this class implements the {@link Iterable}
 * interface, so for-each loops work:
 *
 * <blockquote><pre>{@code
 * File file = ...;
 *FileLineReader lines = new FileLineReader(file,"UTF-8");
 *for (String line : lines) {
 *   processLine(line);
 *}
 *lines.close();}</pre></blockquote>
 *
 * The iterable implementation reads a line at a time using a buffered
 * reader, so is scalable.
 *
 * <p>The {@link #readLines()} method returns the lines all at once
 * as a collection, and automatically closes all resources used:
 *
 * <blockquote><pre>{@code
 * List<String> lines = new FileLines(file,"UTF-8").readLines();}</pre></blockquote>
 *
 * The lines are read into the list, so enough memory should be
 * available to hold the entire file.
 *
 * <p>Instances of this class may be used as ordinary line number
 * readers, too.  The {@link #getLineNumber()} method is particularly
 * useful for error reporting.
 *
 * @author Bob Carpenter
 * @version 3.9
 * @since   Lingpipe3.8
 */
public class FileLineReader extends LineNumberReader
    implements Iterable<String> {


    /**
     * Construct a new file lines iterator from the specified file
     * using the specified character encoding, assuming no
     * compression.
     *
     * <p><b>Warning:</b> The iterator should be closed using the
     * {@link #close()} method to avoid any dangling file references.
     * Closing the JVM also closes, so short programs may avoid
     * closing the streams explicitly.
     *
     * <p>If the file is not found or the encoding is not supported,
     * any file-system resources allocated will be released and an
     * {@code IOException} thrown.
     *
     * @param file File from which to read lines.
     * @param encoding Character encoding.
     * @throws FileNotFoundException If the file is not found.
     * @throws UnsupportedEncodingException If the specified encoding
     * is not supported.
     */
    public FileLineReader(File file, String encoding) throws IOException {
        this(file,encoding,false);
    }

    /**
     * Construct a new file lines iterator from the specified file
     * using the specified character encoding, uncompressing gzipped
     * input if the compresison flag is true.
     *
     * <p><b>Warning:</b> The iterator should be closed using the
     * {@link #close()} method to avoid any dangling file references.
     * Closing the JVM also closes, so short programs may avoid
     * closing the streams explicitly.
     *
     * <p>If the file is not found or the encoding is not supported,
     * any file-system resources allocated will be released and an
     * {@code IOException} thrown.
     *
     * @param file File from which to read lines.
     * @param encoding Character encoding.
     * @param gzipped Set to {@code true} if file is gzipped.
     * @throws FileNotFoundException If the file is not found.
     * @throws UnsupportedEncodingException If the specified encoding
     * is not supported.
     */
    public FileLineReader(File file, String encoding, boolean gzipped)
        throws IOException {
        super(buildReader(file,encoding,gzipped));
    }

    /**
     * Returns an iterator over the remaining lines of the file.
     * Because it buffers one line ahead, any use of {@link
     * #getLineNumber()} from this class will be one greater than it
     * should be.
     *
     * <p>There is no concurrent protection for this method, so it
     * should only be used from a single thread.
     *
     * @return This iterator.
     */
    public Iterator<String> iterator() {
        return new Iterators.Buffered<String>() {
                @Override
                public String bufferNext() {
                    try {
                        return readLine();
                    } catch (IOException e) {
                        throw new IllegalStateException("I/O error reading",e);
                    }
                }
            };
    }

    /**
     * Returns the list of lines remaining to be read from this line
     * iterator and closes all resources.  If this method is called
     * before any calls to {@link #iterator()}, it returns all the
     * lines read from the file.
     *
     * @return The list of lines read from the file.
     * @throws IOException If there is an underlying I/O error
     * reading from the file.
     */
    public List<String> readLines() throws IOException {
        List<String> lineList = new ArrayList<String>();
        try {
            for (String line : this)
                lineList.add(line);
        } finally {
            close();
        }
        return lineList;
    }

    /**
     * Return the list of lines read from the specified file using
     * the specified character encoding.
     *
     * @param in File whose lines are read.
     * @param encoding Character encoding to decode chars in files.
     * @return The list of lines read from the file.
     * @throws UnsupportedEncodingException If the encoding is not
     * supported on the JVM.
     * @throws IOException If there is an underlying I/O error
     * reading from the file.
     */
    public static List<String> readLines(File in, String encoding)
        throws IOException, UnsupportedEncodingException {
        FileLineReader reader = new FileLineReader(in,encoding);
        return reader.readLines();
    }

    /**
     * Return the array of lines read from the specified file using
     * the specified character encoding.
     *
     * @param in File whose lines are read.
     * @param encoding Character encoding to decode chars in files.
     * @return The lines read from the file.
     * @throws UnsupportedEncodingException If the encoding is not
     * supported on the JVM.
     * @throws IOException If there is an underlying I/O error
     * reading from the file.
     */
    public static String[] readLineArray(File in, String encoding)
        throws IOException, UnsupportedEncodingException {

        List<String> lineList = readLines(in,encoding);
        return lineList.toArray(Strings.EMPTY_STRING_ARRAY);
    }


    static Reader buildReader(File file, String encoding, boolean gzipped)
        throws IOException {

        InputStream in = null;
        InputStream zipIn = null;
        InputStreamReader reader = null;
        try {
            in = new FileInputStream(file);
            zipIn = gzipped ? new GZIPInputStream(in) : in;
            reader = new InputStreamReader(zipIn,encoding);
            return reader;
        } catch (IOException e) {
            Streams.closeQuietly(reader);
            Streams.closeQuietly(zipIn);
            Streams.closeQuietly(in);
            throw e;
        }
    }



}
