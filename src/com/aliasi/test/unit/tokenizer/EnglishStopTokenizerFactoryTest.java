package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.NotSerializableException;

import org.junit.Test;
import static com.aliasi.test.unit.Asserts.assertNotSerializable;
import static com.aliasi.test.unit.tokenizer.TokenizerTest.assertFactory;

public class EnglishStopTokenizerFactoryTest {

    @Test
    public void testFactory() {
        TokenizerFactory ieFactory  
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory factory
            = new EnglishStopTokenizerFactory(ieFactory);
        assertFactory(factory,
                      "");
        assertFactory(factory,
                      "a");
        assertFactory(factory,
                      "the a");
        assertFactory(factory,
                      "the foo a",
                      "foo");
        assertFactory(factory,
                      "foo bar a the baz",
                      "foo","bar","baz");
    }

    @Test
    public void testNotSerializable() {
        EnglishStopTokenizerFactory unserializable
            = new EnglishStopTokenizerFactory(TokenizerTest
                                              .UNSERIALIZABLE_FACTORY);
        assertNotSerializable(unserializable);
    }


}
