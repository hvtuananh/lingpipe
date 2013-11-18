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

import java.io.IOException;
import java.io.PrintWriter;


import java.util.IllegalFormatException;


class PrintWriterReporter extends Reporter {

    private long mStartTime;
    private final PrintWriter mWriter;
    private LogLevel mLevel;

    public PrintWriterReporter(PrintWriter writer) {
        mWriter = writer;
        mStartTime = System.nanoTime();
    }

    public synchronized void report(LogLevel level, String msg) {
        if (!isEnabled(level)) return;
        if (mWriter == null) return;
        printTimeStamp();
        mWriter.println(msg);
        mWriter.flush();
    }

    public synchronized void reportf(LogLevel level, String format, Object... args) {
        if (!isEnabled(level)) return;
        if (mWriter == null) return;
        printTimeStamp();
        try {
            mWriter.printf(format,args);
        } catch (IllegalFormatException e) {
            report(LogLevel.WARN,"Illegal format in printf");
            if (format != null)
                mWriter.print("format=" + format);
            for (int i = 0; i < args.length; ++i)
                mWriter.print("; arg[" + i + "]=" + args[i]);
            mWriter.println();
        }
        mWriter.flush();
    }


    public synchronized void close() {
        if (mWriter != null)
            mWriter.close();
    }

    void printTimeStamp() {
        mWriter.printf("%9s ", elapsedTime());
    }

    long elapsedTimeNano() {
        return System.nanoTime() - mStartTime;
    }

    String elapsedTime() {
        return Strings.nsToString(elapsedTimeNano());
    }


}