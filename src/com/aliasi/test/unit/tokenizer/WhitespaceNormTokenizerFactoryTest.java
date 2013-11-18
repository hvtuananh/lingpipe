package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.WhitespaceNormTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.NotSerializableException;

import org.junit.Test;
import static com.aliasi.test.unit.Asserts.assertNotSerializable;
import static com.aliasi.test.unit.tokenizer.TokenizerTest.assertFactory;

public class WhitespaceNormTokenizerFactoryTest {

    @Test
    public void testFactory() {
        TokenizerFactory ieFactory  
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory factory
            = new WhitespaceNormTokenizerFactory(ieFactory);
        assertFactory(factory,
                      "",
                      new String[] { },
                      new String[] { "" });
        assertFactory(factory,
                      "a",
                      new String[] { "a" },
                      new String[] { "", "" });
        assertFactory(factory,
                      "\n\na   \n",
                      new String[] { "a" },
                      new String[] { " ", " " });
        assertFactory(factory,
                      " a bb\nc\n",
                      new String[] { "a", "bb", "c" },
                      new String[] { " ", " ", " ", " " });
    }

    @Test
    public void testNotSerializable() {
        WhitespaceNormTokenizerFactory unserializable
            = new WhitespaceNormTokenizerFactory(TokenizerTest
                                                .UNSERIALIZABLE_FACTORY);
        assertNotSerializable(unserializable);
    }

}
