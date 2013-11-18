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

import java.io.IOException;

/**
 * A {@code Reporter} is the abstract base class on which reporters
 * are defined that provide incremental feedback from long-running
 * tasks at various levels of granularity.  See the utility class
 * {@code Reporters} for factory methods to create reporters with
 * various output sources ranging from files to writers to standard
 * output.
 *
 * <p>A reporter has a defined severity level, which is an instance of
 * {@link LogLevel}.  A reporter will report (by whichever means at
 * its disposal) all reports at or above its specified severity level.
 * Whether a reporter will report a message reported at level may
 * be determined by calling {@link #isEnabled(LogLevel)}, which returns
 * {@code true} if a message at the specified level will be reported.
 *
 * <p>Messages are reported as simple strings at a specified log level
 * using {@link #report(LogLevel,String)}.  It is up to specific
 * implementations to embellish these with time stamps, thread
 * identifiers, etc.
 *
 * <p>The severity level may be inspected using {@link #getLevel()}
 * and changed using {@link #setLevel(LogLevel)}.
 *
 * <p><b>Reporting versus Logging</b>
 *
 * <p>aAlthough this class behaves like other loggers, such as
 * those found in Apache's Log4J or Java's util.logging package,
 * it is not intended to replace them in server-side applications.
 * Unlike these other loggers, reporters as defined in this
 * abstract class are not configurable through reflection -- they
 * are purely controlled through the API.  Their intended use is
 * in command-line programs that call long-running training or
 * run methods.
 *
 * <p><b>Creating Reporters</b>
 *
 * The static utility class {@link Reporters} contains static
 * factory methods for creating a variety of different kinds of
 * reporters.
 *
 * <p><b>Thread Safety</b>
 *
 * <p>The {@code getLevel()} and {@code setLevel(LogLevel)} methods
 * are synchronized with each other, and thus thread safe.  If
 * reporters accept concurrent reports from multiple threads, the
 * report method should be synchronized.  As long as the reporter uses
 * the {@code getLevel()} method to access the current log level, it
 * does not need to be synchronized with the get and set methods.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since   Lingpipe3.8
 */
public abstract class Reporter {

    private LogLevel mLevel;

    /**
     * Construct an instance of a reporter with log level {@code
     * LogLevel.NONE}.
     */
    public Reporter() {
        this(LogLevel.NONE);
    }

    /**
     * Construct an instance of a reporter with the specified log
     * level.
     *
     * @param level Initial log level for this reporter.
     */
    public Reporter(LogLevel level) {
        mLevel = level;
    }

    /**
     * Reports the specified message if the specified level's severity
     * is at or above the level of this reporter.
     *
     * <p>The objects should be converted to strings and concatenated.
     *
     * <p>The default implementation in this class does nothing.
     *
     * <p>If this reporter supports concurrent reports from multiple
     * threads, this method should in some way be synchronized.
     *
     * @param level Log level for this report instance.
     * @param msg Message to report.
     */
    public abstract void report(LogLevel level, String msg);

    /**
     * Utility method for trace-level reports.
     *
     * <p>This is a convenience method calling {@code
     * report(LogLevel.TRACE,msg)}.
     *
     * @param msg Message to report.
     */
    public void trace(String msg) {
        report(LogLevel.TRACE,msg);
    }

    /**
     * Utility method for debug-level reports.
     *
     * <p>This is a convenience method calling {@code
     * report(LogLevel.DEBUG,msg)}.
     *
     * @param msg Message to report.
     */
    public void debug(String msg) {
        report(LogLevel.DEBUG,msg);
    }

    /**
     * Utility method for info-level reports.
     *
     * <p>This is a convenience method calling {@code
     * report(LogLevel.INFO,msg)}.
     *
     * @param msg Message to report.
     */
    public void info(String msg) {
        report(LogLevel.INFO,msg);
    }

    /**
     * Utility method for warn-level reports.
     *
     * <p>This is a convenience method calling {@code
     * report(LogLevel.WARN,msg)}.
     *
     * @param msg Message to report.
     */
    public void warn(String msg) {
        report(LogLevel.WARN,msg);
    }

    /**
     * Utility method for error-level reports.
     *
     * <p>This is a convenience method calling {@code
     * report(LogLevel.ERROR,msg)}.
     *
     * @param msg Message to report.
     */
    public void error(String msg) {
        report(LogLevel.ERROR,msg);
    }

    /**
     * Utility method for fatal-level reports.
     *
     * <p>This is a convenience method calling {@code
     * report(LogLevel.FATAL,msg)}.
     *
     * @param msg Message to report.
     */
    public void fatal(String msg) {
        report(LogLevel.FATAL,msg);
    }

    /**
     * Return the log level for this reporter.
     *
     * <p>The default implementation in this base class returns
     * {@link LogLevel#NONE}.
     *
     * @return The log level for this reporter.
     */
    public final synchronized LogLevel getLevel() {
        return mLevel;
    }


    /**
     * Returns {@code true} if the specified level is at least as
     * severe as the level specified by this reporter.
     *
     * <p>This is a utility method that is equivalent to
     * {@code LogLevel.COMPARATOR.compare(leve,getLevel())}.
     *
     * @param level Level to test.
     * @return {@code true} if the specified level will be reported.
     */
    public boolean isEnabled(LogLevel level) {
        return LogLevel.COMPARATOR.compare(level,getLevel()) >= 0;
    }

    /**
     * Returns {@code true} if this reporter is enabled at
     * the trace level.  
     *
     * <p>This is just a convenience method calling
     * {@code isEnabled(LogLevel.TRACE)}.
     *
     * @return {@code true} if trace reporting is enabled.
     */
    public boolean isTraceEnabled() {
        return isEnabled(LogLevel.TRACE);
    }

    /**
     * Returns {@code true} if this reporter is enabled at
     * the debug level.  
     *
     * <p>This is just a convenience method calling
     * {@code isEnabled(LogLevel.DEBUG)}.
     *
     * @return {@code true} if debug reporting is enabled.
     */
    public boolean isDebugEnabled() {
        return isEnabled(LogLevel.DEBUG);
    }

    /**
     * Returns {@code true} if this reporter is enabled at
     * the info level.  
     *
     * <p>This is just a convenience method calling
     * {@code isEnabled(LogLevel.INFO)}.
     *
     * @return {@code true} if info reporting is enabled.
     */
    public boolean isInfoEnabled() {
        return isEnabled(LogLevel.INFO);
    }

    /**
     * Returns {@code true} if this reporter is enabled at
     * the warn level.  
     *
     * <p>This is just a convenience method calling
     * {@code isEnabled(LogLevel.WARN)}.
     *
     * @return {@code true} if warn reporting is enabled.
     */
    public boolean isWarnEnabled() {
        return isEnabled(LogLevel.WARN);
    }


    /**
     * Returns {@code true} if this reporter is enabled at
     * the error level.  
     *
     * <p>This is just a convenience method calling
     * {@code isEnabled(LogLevel.ERROR)}.
     *
     * @return {@code true} if error reporting is enabled.
     */
    public boolean isErrorEnabled() {
        return isEnabled(LogLevel.ERROR);
    }


    /**
     * Returns {@code true} if this reporter is enabled at
     * the fatal level.  
     *
     * <p>This is just a convenience method calling
     * {@code isEnabled(LogLevel.FATAL)}.
     *
     * @return {@code true} if fatal reporting is enabled.
     */
    public boolean isFatalEnabled() {
        return isEnabled(LogLevel.FATAL);
    }

    /**
     * Sets the log level for this reporter to the specified level
     * and returns the reporter.  The reporter is returned to allow
     * argument chaining, as in:
     *
     * <blockquote><pre>
     * Reporter r = Reporters.stdOut().setLevel(LogLevel.DEBUG);</pre></blockquote>
     *
     * <p>The default implementation in this class sets the logging
     * level to the specified level and returns this reporter.
     *
     * <p>This method must return this reporter in order to
     * be plug-and-play compatible with the built-in reporters.
     *
     * @param level New log level.
     * @return This reporter.
     */
    public final synchronized Reporter setLevel(LogLevel level) {
        mLevel = level;
        return this;
    }

    /**
     * Close this reporter and free all resources associated with
     * it.  This method will likely to cause the reporter to become
     * inoperable after closed.
     *
     * <p>The default implementation in this class does nothing.
     */
    public abstract void close();

}