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

package com.aliasi.corpus;

import com.aliasi.util.Files;
import com.aliasi.util.Streams;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xml.sax.InputSource;


/**
 * A <code>DiskCorpus</code> reads data from a specified training and
 * test directory using a specified parser.
 *
 * <p>The disk corpus parses the data on-the-fly from disk rather
 * than reading it into memory.  
 *
 * <p>The directories holding training and test data are visited
 * recursively.  An GZIP files will be uncompressed and any
 * Zip archives visited recursively.
 * 
 * @author Bob Carpenter
 * @author Mike Ross
 * @version 3.8.1
 * @since LingPipe2.3
 * @param <H> the type of handler to which this corpus sends events
 */
public class DiskCorpus<H extends Handler> extends Corpus<H> {
    
    private final Parser<H> mParser;
    private final File mTrainDir;
    private final File mTestDir;

    private String mCharEncoding = null;
    private String mSystemId = null;

    /**
     * Construct a corpus from the specified parser and data
     * directory.  The training data will be read from the
     * subdirectory of the specified directory named
     * <code>&quot;train&quot;</code> (see {@link
     * #DEFAULT_TRAIN_DIR_NAME}).  The testing data is read from
     * <code>&quot;test&quot;</code> (see {@link
     * #DEFAULT_TEST_DIR_NAME}).  See {@link
     * #DiskCorpus(Parser,File,File)} for more information.
     *
     * @param parser Parser for the data.
     * @param dir Directory in which to find the data.
     */
    public DiskCorpus(Parser<H> parser, 
                      File dir) {
        this(parser,
             new File(dir,DEFAULT_TRAIN_DIR_NAME),
             new File(dir,DEFAULT_TEST_DIR_NAME));
    }

    /**
     * Construct a corpus from the specified parser and training and
     * test directories.  If either directory is
     * <code>null</code>, the corresponding <code>visit</code> method
     * will not produce any events.  
     *
     * @param parser Parser for the data.
     * @param trainDir Directory of training data.
     * @param testDir Directory of testing data.
     */
    public DiskCorpus(Parser<H> parser,
                      File trainDir,
                      File testDir) {
        mParser = parser;
        mTrainDir = trainDir;
        mTestDir = testDir;
    }


    /**
     * Sets the character encoding for this corpus.  If there
     * is no character encoding set, the parser will determine
     * the default character encoding.
     *
     * @param encoding Character encoding.
     */
    public void setCharEncoding(String encoding) {
        mCharEncoding = encoding;
    }

    /**
     * Returns the current character encoding, or <code>null</code>
     * if none has been specified.
     * 
     * @return The current character encoding.
     */
    public String getCharEncoding() {
        return mCharEncoding;
    }

    /**
     * Sets the system identifier for the corpus.  This will be
     * provided to the input sources used for parsing, which use the
     * system identifier to resolve relative URLs (e.g., for resolving
     * DTD references in XML documents).  The system identifier will
     * default to the name of the file being processed or the
     * containing zip/gzip file.
     *
     * @param systemId System identifier.
     */
    public void setSystemId(String systemId) {
        mSystemId = systemId;
    }

    /**
     * Return the system identifier for this corpus or <code>null</code>
     * if none has been specified.  See {@link #setSystemId(String)} for
     * more information.
     *
     * @return The specified system identifier.
     */
    public String getSystemId() {
        return mSystemId;
    }

    /**
     * Returns the data parser for this corpus.
     *
     * @return The data parser for this corpus.
     */
    public Parser<H> parser() {
        return mParser;
    }


    /**
     * Visit the training data, sending extracted events to
     * the specified handler.  This method walks over the entire training
     * directory and the files within any GZip or Zip compressed
     * files.
     *
     * @param handler Handler to receive training events.
     * @throws IOException If there is an underlying I/O error.
     */
    @Override
        public void visitTrain(H handler) throws IOException {
        visit(handler,mTrainDir);
    }
    
    /**
     * Visit the testing data, sending extracted events to the
     * specified handler.  This method walks over the entire test
     * directory and the files within any GZip or Zip compressed
     * files.
     *
     * @param handler Handler to receive testing events.
     * @throws IOException If there is an underlying I/O error.
     */
    @Override
        public void visitTest(H handler) throws IOException {
        visit(handler,mTestDir);
    }


    private void visit(H handler, File file) throws IOException {
        Parser<H> parser = parser();
        parser.setHandler(handler);
        visit(parser,file);
    }

    /**
     * The name of the default training directory,
     * <code>&quot;train&quot;</code>.
     */
    public static final String DEFAULT_TRAIN_DIR_NAME = "train";

    /**
     * The name of the default testing directory,
     * <code>&quot;test&quot;</code>.
     */
    public static final String DEFAULT_TEST_DIR_NAME = "test";


    
    private void visit(Parser<H> parser, File file) 
        throws IOException {

        if (file.isDirectory())
            visitDir(parser,file);
        else if (file.getName().endsWith(".gz"))
            visitGzip(parser,file);
        else if (file.getName().endsWith(".zip"))
            visitZip(parser,file);
        else
            visitOrdinaryFile(parser,file);
    }
    
    private void visitDir(Parser<H> parser, File dir) 
        throws IOException {

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; ++i)
            visit(parser,files[i]);
    }

    private void visitGzip(Parser<H> parser, File gzipFile) 
        throws IOException {

        FileInputStream fileIn = null;
        BufferedInputStream bufIn = null;
        GZIPInputStream gzipIn = null;
        try {
            fileIn = new FileInputStream(gzipFile);
            bufIn = new BufferedInputStream(bufIn);
            gzipIn = new GZIPInputStream(bufIn);
            InputSource inSource = new InputSource(gzipIn);
            configure(inSource,gzipFile);
            parser.parse(inSource);
        } finally {
            Streams.closeQuietly(gzipIn);
            Streams.closeQuietly(bufIn);
            Streams.closeQuietly(fileIn);
        }
    }

    private void visitZip(Parser<H> parser, File zipFile) 
        throws IOException {

        FileInputStream fileIn = null;
        BufferedInputStream bufIn = null;
        ZipInputStream zipIn = null;
        try {
            fileIn = new FileInputStream(zipFile);
            bufIn = new BufferedInputStream(bufIn);
            zipIn = new ZipInputStream(bufIn);
            ZipEntry entry = null;
            while ((entry = zipIn.getNextEntry()) != null) { 
                if (entry.isDirectory()) continue;
                InputSource inSource = new InputSource(zipIn);
                configure(inSource,zipFile);
                parser.parse(inSource);
            }

        } finally {
            Streams.closeQuietly(zipIn);
            Streams.closeQuietly(bufIn);
            Streams.closeQuietly(fileIn);
        }
    }

    private void visitOrdinaryFile(Parser<H> parser, File file) 
        throws IOException {

        InputSource in = new InputSource(file.toURI().toURL().toString());
        configure(in,file);
        parser.parse(in);
    }


    private void configure(InputSource inSource, File file)
        throws IOException {

        inSource.setSystemId(mSystemId == null
                             ? file.toURI().toURL().toString()
                             : mSystemId);
        if (mCharEncoding != null)
            inSource.setEncoding(mCharEncoding);
    }

}

