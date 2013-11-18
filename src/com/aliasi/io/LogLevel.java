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


import java.util.Comparator;

/**
 * A {@code LogLevel} is used to indicate a severity level for selecting
 * which logging messages to report.   
 *
 * <p>The static singleton comparator {@link #COMPARATOR} may be used
 * to compare severity levels.
 *
 * <p>Unlike other commonly used logging packages (e.g. Java Logging, 
 * Log4J, Jakarta Commons Logging), this logging package is completely
 * configuration free in the sense that it is configured entirely
 * through the API.  
 *
 * <p>Each logging level has an integer severity level.  A {@link
 * Reporter} will report messages that are at or above its specified
 * level.
 * 
 * <p>The logging levels in increasing order of severity are:
 *
 * <ul>
 * <li> {@link LogLevel#TRACE}: Verbose debugging.
 * <li> {@link LogLevel#DEBUG}: Fine-grained debugging.
 * <li> {@link LogLevel#INFO}:  Coarse-grained application flow.
 * <li> {@link LogLevel#WARN}:  Potentially harmful situation.
 * <li> {@link LogLevel#ERROR}: Potentially recoverable error.
 * <li> {@link LogLevel#FATAL}: Unrecoverable error.
 * </ul>
 *
 * <p>There are also two special levels which bound the regular severity
 * levels.  
 *
 * <ul>
 * <li> {@link LogLevel#ALL} : Report all log messages.  Highest possible severity.
 * <li> {@link LogLevel#NONE}: Report no log messages.  Lowest possible severity.
 * </ul>
 *
 * <p>The levels were chosen to follow the most popular loggers
 * for Java:
 * 
 * <ul>
 * <li> Java Logging: <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/logging/Level.html"><code>java.util.logging.Level</code></a></li>
 * <li> Apaceh Log4J: <a href="http://logging.apache.org/log4j/1.2/apidocs/index.html?org/apache/log4j/Level.html"><code>org.apache.log4j.Level</code></a></li>
 * <li>Jakarta Commons Logging: <a href="http://commons.apache.org/logging/apidocs/org/apache/commons/logging/Log.html"><code>org.apache.commons.logging.Log</code></a></li>
 * <li>Apache Avalong LogKit: <a href="http://excalibur.apache.org/apidocs/org/apache/avalon/framework/logger/LogKitLogger.html"><code>org.apache.avalon.framework.logger.LogKitLogger</code></a>
 * </ul>
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since Lingpipe3.8
 */
public enum LogLevel {

    /**
     * Special level with lowest possible severity.  
     */
    ALL(Integer.MIN_VALUE),

        /** 
         * Very detailed debugging information. 
         */
        TRACE(0),
            
        /**
         * Fine-grained debugging.
         */
        DEBUG(1),

        /**
         * Coarse-grained application flow information. 
         */
        INFO(2),

        /**
         * Possibly harmful situation .  
         */
        WARN(3),

        /**
         * Error that may be recoverable.
         */
        ERROR(4),

        /**
         * Unrecoverable error. 
         */
        FATAL(5),

        /**
         * Special severity with level higher than all other levels.  
         */
        NONE(Integer.MAX_VALUE);

    int mSeverity;
    LogLevel(int severity) {
        mSeverity = severity;
    }

    /**
     * Returns a comparator that compares levels by severity.  If
     * {@code level1} is more severe than {@code level2}, {@code
     * LogLevel.COMPARATOR.compare(level1,level2)} returns {@code 1},
     * if {@code level1} is less severe than {@code level2}, it
     * returns {@code -1}, and if they are equally severe, it returns
     * {@code 0}.
     */
    public static final Comparator<LogLevel> COMPARATOR 
        = new Comparator<LogLevel>() {
        public int compare(LogLevel level1, LogLevel level2) {
            return 
            level1.mSeverity > level2.mSeverity
            ? 1
            : ( level1.mSeverity < level2.mSeverity
                ? -1
                : 0 );
            
        }
    };

}


