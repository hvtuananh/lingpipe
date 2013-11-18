package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.SoundexTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.IOException;
import java.io.NotSerializableException;

import org.junit.Test;

import com.aliasi.util.AbstractExternalizable;

import static com.aliasi.test.unit.Asserts.assertNotSerializable;

import static com.aliasi.test.unit.tokenizer.TokenizerTest.assertFactory;

import static junit.framework.Assert.assertEquals;

import static org.junit.Assert.assertArrayEquals;

public class SoundexTokenizerFactoryTest {

    @Test
    public void testFactory() {
        TokenizerFactory ieFactory  
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory soundexFactory 
            = new SoundexTokenizerFactory(ieFactory);
        assertFactory(soundexFactory,
                      "");
        assertFactory(soundexFactory,
                      "1",
                      "0000");
        assertFactory(soundexFactory,
                      "1 1",
                      "0000", "0000");
        assertFactory(soundexFactory,
                      "Robert 1 Guttierez",
                      "R163", "0000", "G362");
    }

    @Test
    public void testNotSerializable() {
        SoundexTokenizerFactory unserializable
            = new SoundexTokenizerFactory(TokenizerTest
                                          .UNSERIALIZABLE_FACTORY);
        assertNotSerializable(unserializable);
    }

    @Test
    public void testSoundex() {
        assertSoundex("","0000");
        assertSoundex("1","0000");
        assertSoundex("34","0000");
        assertSoundex("#$%^&*(","0000");

        // from census
        assertSoundex("Gutierrez","G362");
        assertSoundex("Pfister","P236");
        assertSoundex("Jackson","J250");
        assertSoundex("Tymczak","T522");
        assertSoundex("VanDeusen","V532");
        assertSoundex("Ashcraft","A261");

        // from Wikipedia
        assertSoundex("Robert","R163");
        assertSoundex("Rupert","R163");
        assertSoundex("Rubin","R150");

        // from Knuth
        assertSoundex("Euler","E460");
        assertSoundex("Gauss","G200");
        assertSoundex("Hilbert","H416");
        assertSoundex("Knuth","K530");
        assertSoundex("Lloyd","L300");
        // Should work with '\u0141' Dark L from Latin1
        assertSoundex("Lukasiewicz","L222");  
        assertSoundex("Wachs","W200");

        assertSoundex("Ellery","E460");
        assertSoundex("Ghosh","G200");
        assertSoundex("Heilbronn","H416");
        assertSoundex("Kant","K530");
        assertSoundex("Liddy","L300");
        assertSoundex("Lissajous","L222");
        assertSoundex("Waugh","W200");
    }

    void assertSoundex(String in, String soundexToken) {
        String encodedToken = SoundexTokenizerFactory.soundexEncoding(in);
        assertEquals("in=" + in
                     + " soundex=" + soundexToken
                     + " found=" + encodedToken,
                     soundexToken, encodedToken);
    }

}
