package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.NotSerializableException;

import org.junit.Test;
import static com.aliasi.test.unit.Asserts.assertNotSerializable;
import static com.aliasi.test.unit.tokenizer.TokenizerTest.assertFactory;

public class LowerCaseTokenizerFactoryTest {

    @Test
    public void testFactory() {
        TokenizerFactory ieFactory  
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory factory
            = new LowerCaseTokenizerFactory(ieFactory);
        assertFactory(factory,
                      "");
        assertFactory(factory,
                      "a",
                      "a");
        assertFactory(factory,
                      "A",
                      "a");
        assertFactory(factory,
                      "Mr. John Smith is 47 today.",
                      "mr",".","john","smith","is","47","today",".");
    }

    @Test
    public void testNotSerializable() {
        LowerCaseTokenizerFactory unserializable
            = new LowerCaseTokenizerFactory(TokenizerTest
                                            .UNSERIALIZABLE_FACTORY);
        assertNotSerializable(unserializable);
    }


}
