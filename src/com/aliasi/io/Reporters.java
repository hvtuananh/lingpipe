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

import com.aliasi.util.Strings;

import java.nio.charset.UnsupportedCharsetException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;

/**
 * The {@code Reporters} utility class provides static factory methods
 * for the creation of various instances of {@link Reporter}.  The
 * reporters created wrap various output methods from writers to
 * streams to files to standard output to nothing.
 *
 * <h4>Thread Safety</h4>
 *
 * <p>Each of the primitive implementations is itself thread safe
 * because calls to report are synchronized.
 *
 * <p>The compound reporter produced by {@link #tee(Reporter[])} will
 * be thread safe if each of its contained reporters is thread safe.
 *
 * <h4>Serializability</h4>
 *
 * <p>None of the reporters are serializable.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since   Lingpipe3.8
 */
public class Reporters {

    // don't allow instances
    private Reporters() { /* intentionally blank */ }

    /**
     *  Return a reporter that writes to the specified print
     *  writer.
     *
     * <p>The initial log level is {@link LogLevel#NONE}.  This
     * may be reset using the method {@link Reporter#setLevel(LogLevel)}.
     *
     * @param writer Writer to use for reporting.
     * @return The writer-based reporter.
     */
    public static Reporter writer(Writer writer) {
        return new PrintWriterReporter(new PrintWriter(writer));
    }

    /**
     * Return a reporter that writes to the specified output
     * stream using the specified encoding.
     *
     * @param out Output stream to which to write.
     * @param encoding Encoding to use for writing.
     * @return The stream-based reporter.
     * @throws UnsupportedCharsetException If the encoding is not
     * supported.
     */
    public static Reporter stream(OutputStream out, String encoding)
        throws UnsupportedCharsetException {
        try {
            return writer(new OutputStreamWriter(out,encoding));
        } catch (UnsupportedEncodingException e) {
            String msg = "Encoding=" + encoding + " Base exception=" + e;
            throw new UnsupportedCharsetException(msg);
        }
    }

    /**
     * Return a reporter that writes to the specified file
     * using the specified encoding.
     *
     * @param f File to which to write.
     * @param encoding Character encoding to use for writing.
     * @return The file-based reporter.
     * @throws UnsupportedCharsetException If the encoding is not supported.
     * @throws IOException If the file is not found.
     */
    public static Reporter file(File f, String encoding) throws IOException {
        return stream(new FileOutputStream(f),encoding);
    }

    /**
     * Return a reporter that writes to standard output using
     * the UTF-8 character encoding.  The standard output stream
     * is retrieved through {@link System#out}.
     *
     * <p>To write to standard output using a different character set,
     * use the factory method {@link #stream(OutputStream,String)}
     * with the stream set to {@code System.out}.
     *
     * @return The standard-out-based reporter.
     * @throws UnsupportedCharsetException If this platform does not
     * support UTF-8; the Java specification requires UTF-8 support,
     * so this should not occur on standard platforms.
     */
    public static Reporter stdOut() {
        return stream(System.out,Strings.UTF8);
    }

    /**
     * Returns a compound reporter that sends reports to all of the
     * specified reporters.
     *
     * <p>The compound reporter may have its log level set like any
     * reporter.  Only reports at a log level whose severity is
     * enabled are reported to the contained reporters through their
     * report methods.
     *
     * <p>To allow the compound reporter to control logging levels for
     * the contained reporters, set the contained reporters log levels
     * to {@link LogLevel#ALL} so that they report all messages sent
     * to them by the compound reporter.  For instance:
     *
     * <blockquote><pre>
     * for (Reporter reporter : reporters)
     *     reporter.setLevel(LogLevel.ALL);
     * Reporter rep = Reporters.tee(reporters);
     * rep.setLevel(LogLevel.DEBUG);</pre></blockquote>
     *
     * creates a compound logger printing messages to all contained
     * reporters at or above the debug level of severity.
     *
     * @param reporters A variable-length list of reporters.
     */
    public static Reporter tee(Reporter... reporters) {
        return new TeeReporter(reporters);
    }

    static final class TeeReporter extends Reporter {
        final Reporter[] mReporters;
        public TeeReporter(Reporter[] reporters) {
            mReporters = reporters;
        }
        public void report(LogLevel level, String msg) {
            if (!isEnabled(level)) return;
            for (Reporter reporter : mReporters)
                reporter.report(level,msg);
        }
        public void close() {
            for (Reporter rep : mReporters)
                rep.close();
        }
    }

    /**
     * Returns a silent reporter that writes its output
     * nowhere. This is like piping a file to {@code dev/null}
     * on a Unix machine.
     *
     * <p>The log levle on this reporter is fixed at {@link
     * LogLevel#NONE}.  Although levels may be set on silent
     * reporters, no matter what level is set, nothing is output.
     *
     * <p>There is only a single silent reporter that is
     * returned for all calls to this method.
     *
     * @return The silent reporter.
     */
    public static Reporter silent() {
        return SILENT_REPORTER;
    }

    static final Reporter SILENT_REPORTER
        = new Reporter(LogLevel.NONE) {
                public void report(LogLevel level, String msg) {
                    /* intentionally blank */
                }
                public void close() {
                    /* intentionally blank */
                }
            };

}
