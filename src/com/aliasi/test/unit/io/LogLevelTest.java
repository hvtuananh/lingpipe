package com.aliasi.test.unit.io;

import com.aliasi.io.LogLevel;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;


public class LogLevelTest {

    @Test
    public void testToString() {
        assertEquals("WARN",LogLevel.WARN.toString());
    }

    @Test
    public void testComparator() {
        LogLevel[] levels
            = new LogLevel[] { LogLevel.ALL, 
                               LogLevel.DEBUG,
                               LogLevel.INFO,
                               LogLevel.WARN,
                               LogLevel.ERROR,
                               LogLevel.FATAL,
                               LogLevel.NONE };
        for (int i = 0; i < levels.length; ++i) {
            assertEquals(0,LogLevel.COMPARATOR.compare(levels[i],levels[i]));
            for (int j = i+1; j < levels.length; ++j) {
                assertEquals(-1,LogLevel.COMPARATOR.compare(levels[i],levels[j]));
                assertEquals(1,LogLevel.COMPARATOR.compare(levels[j],levels[i]));
            }
        }
    }
        
}
