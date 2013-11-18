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

package com.aliasi.test.unit.io;

import com.aliasi.io.FileExtensionFilter;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;


import com.aliasi.util.Files;

import java.io.File;
import java.io.FileFilter;

public class FileExtensionFilterTest  {

    static final String TEMP_DIRECTORY_SYS_PROPERTY = "java.io.tmpdir";

    static final File TEMP_DIRECTORY
        = new File(System.getProperty(TEMP_DIRECTORY_SYS_PROPERTY));

    static File makeCleanDir(File file) {
        if (file.exists() & !file.isDirectory()) file.delete();
        if (!file.isDirectory()) file.mkdir();
        removeDescendants(file);
        return file;
    }

    static File makeCleanDir(File directory, String name) {
        return makeCleanDir(new File(directory,name));
    }


    static int removeDescendants(File file) {
        if (!file.isDirectory()) return 0;
        int count = 0;
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; ++i)
            count += removeRecursive(files[i]);
        return count;
    }

    static int removeRecursive(File file) {
        if (file == null) return 0; // nothing to remove
        int descCount = removeDescendants(file);
        file.delete();
        return descCount + 1;
    }

    @Test
    public void testAll() {
        File fDir = makeCleanDir(TEMP_DIRECTORY,"fnamefilter");
        File fYes1 = new File(fDir,"foo.txt");
        File fYes2 = new File(fDir,"bar.txt");
        File fMaybe = new File(fDir,"foo.tx");
        File fNo = new File(fDir,"foo");
        File fNo2 = new File(fDir,"foo.foo");
        File fNo3 = new File(fDir,"foo.");

        FileFilter filter1 = new FileExtensionFilter("txt");
        assertTrue(filter1.accept(fDir));
        assertTrue(filter1.accept(fYes1));
        assertTrue(filter1.accept(fYes2));
        assertFalse(filter1.accept(fMaybe));
        assertFalse(filter1.accept(fNo));
        assertFalse(filter1.accept(fNo2));
        assertFalse(filter1.accept(fNo3));


        FileFilter filter1true
            = new FileExtensionFilter("txt",true);
        assertTrue(filter1true.accept(fDir));
        assertTrue(filter1true.accept(fYes1));
        assertTrue(filter1true.accept(fYes2));
        assertFalse(filter1true.accept(fMaybe));
        assertFalse(filter1true.accept(fNo));
        assertFalse(filter1true.accept(fNo2));
        assertFalse(filter1true.accept(fNo3));


        FileFilter filter1false =
            new FileExtensionFilter("txt",false);
        assertFalse(filter1false.accept(fDir));
        assertTrue(filter1false.accept(fYes1));
        assertTrue(filter1false.accept(fYes2));
        assertFalse(filter1false.accept(fMaybe));
        assertFalse(filter1false.accept(fNo));
        assertFalse(filter1false.accept(fNo2));
        assertFalse(filter1false.accept(fNo3));

        FileFilter filter2
            = new FileExtensionFilter(new String[] { "txt", "tx" });
        assertTrue(filter2.accept(fDir));
        assertTrue(filter2.accept(fYes1));
        assertTrue(filter2.accept(fYes2));
        assertTrue(filter2.accept(fMaybe));
        assertFalse(filter2.accept(fNo));
        assertFalse(filter2.accept(fNo2));
        assertFalse(filter2.accept(fNo3));

        FileFilter filter2true
            = new FileExtensionFilter(new String[] { "txt", "tx" },
                                      true);
        assertTrue(filter2true.accept(fDir));
        assertTrue(filter2true.accept(fYes1));
        assertTrue(filter2true.accept(fYes2));
        assertTrue(filter2true.accept(fMaybe));
        assertFalse(filter2true.accept(fNo));
        assertFalse(filter2true.accept(fNo2));
        assertFalse(filter2true.accept(fNo3));

        FileFilter filter2false
            = new FileExtensionFilter(new String[] { "txt", "tx" },
                                      false);
        assertFalse(filter2false.accept(fDir));
        assertTrue(filter2false.accept(fYes1));
        assertTrue(filter2false.accept(fYes2));
        assertTrue(filter2false.accept(fMaybe));
        assertFalse(filter2false.accept(fNo));
        assertFalse(filter2false.accept(fNo2));
        assertFalse(filter2false.accept(fNo3));
    }



}
