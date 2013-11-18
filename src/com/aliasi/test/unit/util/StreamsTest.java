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

package com.aliasi.test.unit.util;

import com.aliasi.util.Files;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.xml.sax.InputSource;

public class StreamsTest  {

    static String FILE_URL_PREFIX = "file:///";

    static String fileToURLName(File file) throws IOException {
        return FILE_URL_PREFIX + file.getCanonicalPath();
    }


    static final String TEMP_DIRECTORY_SYS_PROPERTY = "java.io.tmpdir";

    static final File TEMP_DIRECTORY
        = new File(System.getProperty(TEMP_DIRECTORY_SYS_PROPERTY));

    static File createTempFile(String fileName) {
        File tempFile = new File(TEMP_DIRECTORY,fileName);
        tempFile.deleteOnExit();
        return tempFile;
    }

    static void writeStringToFile(String s, File file)
        throws IOException {

        writeCharsToFile(s.toCharArray(),file);
    }

    static void writeStringToFile(String s, File file, String encoding)
        throws IOException {

        writeCharsToFile(s.toCharArray(),file,encoding);
    }

    static void writeCharsToFile(char[] chars, File file)
        throws IOException {

        writeCharsToFile(chars,file,Strings.UTF8);
    }

    static void writeCharsToFile(char[] chars,
                                        File file, String encoding)
        throws IOException {

        FileOutputStream out = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(out,encoding);
        writer.write(chars);
        Streams.closeQuietly(writer);
    }


    @Test
    public void testGetDefaultJavaCharset() {
        assertNotNull(Streams.getDefaultJavaCharset());
    }

    @Test
    public void testCopyReader() throws IOException {
        assertCopyReader(new char[] { });
        assertCopyReader(new char[] { 'a', 'b', 'c' });
        char[] chars = new char[100000];
        for (int i = 0; i < chars.length; ++i)
            chars[i] = 'a';
        assertCopyReader(chars);
    }

    @Test
    public void testCopyStream() throws IOException {
        assertCopyStream(new byte[] { });
        assertCopyStream(new byte[] { (byte)17, (byte)12, (byte)13 });
        byte[] bytes = new byte[100000];
        for (int i = 0; i < bytes.length; ++i)
            bytes[i] = (byte) 12;
        assertCopyStream(bytes);
    }

    @Test
    public void testCloseInputStream() {
        Streams.closeQuietly(null);
        DummyInputStream in = new DummyInputStream();
        Streams.closeQuietly(in);
        assertTrue(in.isClosed());
    }

    @Test
    public void testCloseOutputStream() {
        Streams.closeQuietly(null);
        DummyOutputStream out = new DummyOutputStream();
        assertFalse(out.isClosed());
        Streams.closeQuietly(out);
        assertTrue(out.isClosed());
    }

    @Test
    public void testCloseReader() {
        Streams.closeQuietly(null);
        DummyReader reader = new DummyReader();
        assertFalse(reader.isClosed());
        Streams.closeQuietly(reader);
        assertTrue(reader.isClosed());
    }

    @Test
    public void testCloseWriter() {
        Streams.closeQuietly(null);
        DummyWriter writer = new DummyWriter();
        assertFalse(writer.isClosed());
        Streams.closeQuietly(writer);
        assertTrue(writer.isClosed());
    }

    @Test
    public void testToCharArray() throws IOException {
        String t1 = "SIL-1";
        StringReader reader = new StringReader(t1);
        InputSource in = new InputSource(reader);
        assertEquals(t1,new String(Streams.toCharArray(in)));

        byte[] bytes = t1.getBytes();
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        in = new InputSource(bytesIn);
        assertEquals(t1,new String(Streams.toCharArray(in)));

        bytes = t1.getBytes("UTF-16");
        bytesIn = new ByteArrayInputStream(bytes);
        in = new InputSource(bytesIn);
        in.setEncoding("UTF-16");
        assertEquals(t1,new String(Streams.toCharArray(in)));

        File tempFile = createTempFile("LingPipe_StreamsTest");
        writeStringToFile(t1,tempFile);
        String urlName = fileToURLName(tempFile);
        in = new InputSource(urlName);
        assertEquals(t1,new String(Streams.toCharArray(in)));

        writeStringToFile(t1,tempFile,"UTF-16");
        in = new InputSource(urlName);
        in.setEncoding("UTF-16");
        assertEquals(t1,new String(Streams.toCharArray(in)));
    }

    private void assertCopyReader(char[] chars) throws IOException {
        CharArrayReader reader = new CharArrayReader(chars);
        CharArrayWriter writer = new CharArrayWriter();
        Streams.copy(reader,writer);
        assertArrayEquals(chars,writer.toCharArray());
    }

    private void assertCopyStream(byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Streams.copy(in,out);
        assertArrayEquals(bytes,out.toByteArray());
    }


    private static class DummyOutputStream extends OutputStream {
        private boolean mIsClosed = false;
        @Override
        public void write(int c) throws IOException {
            /* do nothing */
        }
        @Override
        public void close() {
            mIsClosed=true;
        }
        public boolean isClosed() {
            return mIsClosed;
        }
    }

    private static class DummyInputStream extends InputStream {
        private boolean mIsClosed = false;
        @Override
        public int read() throws IOException {
            return -1;
        }
        @Override
        public void close() {
            mIsClosed=true;
        }
        public boolean isClosed() {
            return mIsClosed;
        }
    }

    private static class DummyReader extends Reader {
        private boolean mIsClosed = false;
        @Override
        public int read(char[] chars, int offset, int length) {
            return -1;
        }
        @Override
        public void close() {
            mIsClosed=true;
        }
        public boolean isClosed() {
            return mIsClosed;
        }
    }

    private static class DummyWriter extends Writer {
        private boolean mIsClosed = false;
        @Override
        public void write(char[] chars, int offset, int length) {
            /* do nothing */
        }
        @Override
        public void close() {
            mIsClosed=true;
        }
        @Override
        public void flush() {
            /* do nothing */
        }
        public boolean isClosed() {
            return mIsClosed;
        }
    }

}
