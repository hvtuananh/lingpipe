package com.aliasi.test.unit.dict;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.TrieDictionary;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.io.IOException;

public class TrieDictionaryTest  {

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {

        DictionaryEntry<String> entry1 = new DictionaryEntry<String>("this","DET",5,15.0);
        DictionaryEntry<String> entry2 = new DictionaryEntry<String>("the","DET",5.0);
        DictionaryEntry<String> entry3 = new DictionaryEntry<String>("that","DET",5);
        DictionaryEntry<String> entry4 = new DictionaryEntry<String>("that","NP");
        DictionaryEntry<String> entry5 = new DictionaryEntry<String>("a","DET");
        DictionaryEntry<String> entry6 = new DictionaryEntry<String>("member","N");

        TrieDictionary<String> dict = new TrieDictionary<String>();

        dict.addEntry(entry1);
        dict.addEntry(entry2);
        dict.addEntry(entry3);
        dict.addEntry(entry4);
        dict.addEntry(entry5);
        dict.addEntry(entry6);

        TrieDictionary<String> dict2
            = (TrieDictionary<String>) AbstractExternalizable.compile(dict);
        TrieDictionary<String> dict3
            = (TrieDictionary<String>) AbstractExternalizable.serializeDeserialize(dict);
        assertEqualElts(dict,dict2);
        assertEqualElts(dict,dict3);

    }

    void assertEqualElts(TrieDictionary<String> dict, TrieDictionary<String> dict2) {
        assertEquals(getElements(dict),getElements(dict2));
    }

    Set<DictionaryEntry<String>> getElements(TrieDictionary<String> dict) {
        Set<DictionaryEntry<String>> elts = new HashSet<DictionaryEntry<String>>();
        for (DictionaryEntry<String> entry : dict)
            elts.add(entry);
        return elts;
    }


    @Test
    public void testOne() {
        TrieDictionary dict = new TrieDictionary();
        assertFalse(dict.iterator().hasNext());
        assertEquals(0,dict.size());

        DictionaryEntry entryThis = new DictionaryEntry("this","DET");
        DictionaryEntry entryThe = new DictionaryEntry("the","DET");
        DictionaryEntry entryThat = new DictionaryEntry("that","DET");
        DictionaryEntry entryThat2 = new DictionaryEntry("that","NP");
        DictionaryEntry entryA = new DictionaryEntry("a","DET");
        DictionaryEntry entryMember = new DictionaryEntry("member","N");

        dict.addEntry(entryThis);
        assertDict(new DictionaryEntry[] { entryThis },
                   dict);

        dict.addEntry(entryThe);
        assertDict(new DictionaryEntry[] { entryThe, entryThis },
                   dict);

        dict.addEntry(entryA);
        dict.addEntry(entryA);
        assertDict(new DictionaryEntry[] { entryA, entryThe, entryThis },
                   dict);

        dict.addEntry(entryMember);
        assertDict(new DictionaryEntry[] { entryA, entryMember,
                                           entryThe, entryThis },
                   dict);

        dict.addEntry(entryThat);
        dict.addEntry(entryThat2);
        DictionaryEntry[] entries = new DictionaryEntry[] {
            entryA, entryMember, entryThe, entryThis, entryThat, entryThat2
        };
        HashSet expectedEntrySet = new HashSet(Arrays.asList(entries));
        assertEquals(entries.length,expectedEntrySet.size());
        assertEquals(expectedEntrySet,
                     new HashSet(dict.entryList()));

        assertPhraseEntries(dict,"that",
                            new Object[] { entryThat, entryThat2 });
        assertPhraseEntries(dict,"the",
                            new Object[] { entryThe });
        assertPhraseEntries(dict,"member",
                            new Object[] { entryMember });
        assertPhraseEntries(dict,"foo",
                            new Object[] { });

        assertCatEntries(dict,"DET",
                         new Object[] { entryA, entryThe,
                                        entryThis, entryThat });

        assertCatEntries(dict,"NP",
                         new Object[] { entryThat2 });

        assertCatEntries(dict,"V",
                         new Object[] { } );

    }

    void assertCatEntries(TrieDictionary dict, Object cat,
                          Object[] entries) {
        HashSet expectedEntrySet
            = new HashSet(Arrays.asList(entries));
        HashSet foundSet
            = new HashSet(dict.categoryEntryList(cat));
        assertEquals(expectedEntrySet,foundSet);
    }


    void assertPhraseEntries(TrieDictionary dict, String phrase,
                             Object[] entries) {
        HashSet expectedEntrySet
            = new HashSet(Arrays.asList(entries));
        HashSet foundSet
            = new HashSet(dict.phraseEntryList(phrase));
        assertEquals(expectedEntrySet,foundSet);
    }

    void assertDict(DictionaryEntry[] entries, TrieDictionary dict) {
        assertEquals(entries.length,dict.size());
        Iterator it = dict.iterator();
        for (int i = 0; i < entries.length; ++i)
            assertEquals(entries[i],it.next());
    }

}
