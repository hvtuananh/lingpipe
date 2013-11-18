package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.NotSerializableException;

import java.util.regex.Pattern;

import org.junit.Test;
import static com.aliasi.test.unit.Asserts.assertNotSerializable;
import static com.aliasi.test.unit.tokenizer.TokenizerTest.assertFactory;

public class RegExFilteredTokenizerFactoryTest {

    // two or more capital letters only
    static final Pattern TEST_PATTERN = Pattern.compile("[A-Z][A-Z]+");

    @Test
    public void testFactory() {
        TokenizerFactory ieFactory  
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory factory
            = new RegExFilteredTokenizerFactory(ieFactory,
                                                TEST_PATTERN);
        assertFactory(factory,
                      "");
        assertFactory(factory,
                      "a");
        assertFactory(factory,
                      "A");
        assertFactory(factory,
                      "The starling is flying towards home smiling happily");
        assertFactory(factory,
                      "a AA A BBB c DDD",
                      "AA","BBB","DDD");
    }

    @Test
    public void testNotSerializable() {
        RegExFilteredTokenizerFactory unserializable
            = new RegExFilteredTokenizerFactory(TokenizerTest
                                                .UNSERIALIZABLE_FACTORY,
                                                TEST_PATTERN);
        assertNotSerializable(unserializable);
    }

}
