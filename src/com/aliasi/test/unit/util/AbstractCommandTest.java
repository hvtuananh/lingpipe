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

import com.aliasi.util.AbstractCommand;

import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static com.aliasi.test.unit.Asserts.succeed;

public class AbstractCommandTest {

    @Test
    public void test1() {
        assertTrue(true);
    }



    @Test
    public void testOne() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { });
        assertFalse(args.hasFlag("foo"));
        assertNull(args.getBareArgument(1));
        assertNull(args.getArgument("bar"));
    }

    @Test
    public void testFilePrefixes() {
        // test result of submitted bug that file1 failed w/o parent
        new TestCmd(new String[] {"-file1=abc","-file2=build/b","c"}).run();
    }

    @Test
    public void testTwo() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "foo", "bar" });
        assertFalse(args.hasFlag("foo"));
        assertEquals("foo",args.getBareArgument(0));
        assertEquals("bar",args.getBareArgument(1));
        assertNull(args.getArgument("bar"));
    }

    @Test
    public void testThree() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "-foo", "bar" });
        assertTrue(args.hasFlag("foo"));
        assertEquals("bar",args.getBareArgument(0));
        assertNull(args.getArgument("baz"));
    }

    @Test
    public void testFour() {
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "-foo", "bar", "-baz=ping" });
        assertTrue(args.hasFlag("foo"));
        assertEquals("bar",args.getBareArgument(0));
        assertEquals("ping",args.getArgument("baz"));
    }

    @Test
    public void testFive() {
        Properties defaults = new Properties();
        defaults.setProperty("a","b");
        CommandLineArguments args
            = new CommandLineArguments(new String[] { "-foo", "bar", "-baz=ping" },
                                       defaults);
        assertTrue(args.hasFlag("foo"));
        assertEquals("bar",args.getBareArgument(0));
        assertEquals("ping",args.getArgument("baz"));
        assertEquals("b",args.getArgument("a"));
    }

    @Test
    public void testExceptions1() {
        new CommandLineArguments(new String[] { "bar", "-foo=" });
        succeed();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptions2() {
        new CommandLineArguments(new String[] { "-=val", "bar" });
    }

    @Test
    public void testHasArgument() {
        String[] argArray = new String[] { "-foo", "-bar=17", "-baz=17a" };
        CommandLineArguments args = new CommandLineArguments(argArray);
        assertTrue(args.hasArgument("bar"));
        assertFalse(args.hasArgument("abc"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInt() {
        String[] argArray = new String[] { "-foo", "-bar=17", "-baz=17a" };
        CommandLineArguments args = new CommandLineArguments(argArray);
        assertEquals(17,args.getArgumentInt("bar"));
        boolean threw = false;
        args.getArgumentInt("baz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDouble() {
        String[] argArray = new String[] { "-foo", "-bar=17.9", "-baz=17a" };
        CommandLineArguments args = new CommandLineArguments(argArray);
        assertEquals(17.9,args.getArgumentDouble("bar"),0.005);
        boolean threw = false;
        args.getArgumentInt("baz");
    }

    @Test
    public void testHasProperty() {
        String[] argArray = new String[] { "-foo", "-bar=17.9", "-baz=17a" };
        CommandLineArguments args = new CommandLineArguments(argArray);

        assertTrue(args.hasFlag("foo"));
        assertFalse(args.hasFlag("bar"));
        assertFalse(args.hasFlag("boo"));

        assertFalse(args.hasProperty("foo"));
        assertTrue(args.hasProperty("bar"));
        assertTrue(args.hasProperty("baz"));
    }

    private static class CommandLineArguments extends AbstractCommand {

        public CommandLineArguments(String[] args) {
            super(args);
        }

        public CommandLineArguments(String[] args, Properties props) {
            super(args,props);
        }

        @Override
        public void run() { 
            /* do nothing */
        }
    }

    private static class TestCmd extends AbstractCommand {
        public TestCmd(String[] args) { super(args); }
        @Override
        public void run() {
            getArgumentCreatableFile("file1");
            // File f2 = getArgumentCreatableFile("file2");
        }
    }


}
