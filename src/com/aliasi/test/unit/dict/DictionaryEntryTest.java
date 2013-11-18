package com.aliasi.test.unit.dict;

import com.aliasi.dict.DictionaryEntry;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.NotSerializableException;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;

public class DictionaryEntryTest  {

    @Test
    public void testOne() throws Exception {
    // use serializable
    DictionaryEntry entry = new DictionaryEntry("foo",Integer.valueOf(1),4,2.75);
    DictionaryEntry compiledEntry
        = (DictionaryEntry) AbstractExternalizable.compile(entry);
    assertEquals(entry,compiledEntry);

    // use compilable
    DictionaryEntry entry2 = new DictionaryEntry("bar",entry,5,14.4);
    DictionaryEntry compiledEntry2
        = (DictionaryEntry) AbstractExternalizable.compile(entry2);
    assertEquals(entry2,compiledEntry2);
    }

    @Test(expected=NotSerializableException.class)
    public void testExc() throws ClassNotFoundException, IOException {
        DictionaryEntry entry3 = new DictionaryEntry("baz",new Object(), 4, 17.9);
        AbstractExternalizable.compile(entry3);
    }

}


