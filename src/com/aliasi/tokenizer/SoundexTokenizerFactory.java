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

package com.aliasi.tokenizer;

import com.aliasi.util.Strings;

import java.io.ObjectInput;
import java.io.Serializable;

/**
 * A {@code SoundexTokenizerFactory} modifies the output of a base
 * tokenizer factory to produce tokens in soundex representation.
 * Soundex replaces sequences of characters with a crude
 * four-character approximation of their pronunciation plus initial
 * letter.
 *
 * <h3>Soundex Representations</h3>
 *
 * <p>The process for converting an input to its Soundex
 * representation is fairly straighforward for inputs that are all
 * ASCII letters.  Soundex is case insensitive, but is only defined
 * for strings of ASCII letters.  Thus to begin, all characters
 * that are not Latin1 letters are removed, and all Latin1 characters
 * are stripped of their diacritics.  The algorithm then proceeds
 * according to its standard definition:
 *
 * <ol>
 * <li>Normalize input by removing all characters that are not
 * Latin1 letters, and converting all other characters to uppercase
 * ASCII after first removing any diacritics.
 * <li>If the input is empty, return "0000"
 * </li>
 * <li>Set the first letter of the output to the first letter of the input.
 * </li>
 * <li>While there are less than four letters of output do:
 *   <ol>
 *   <li>If the next letter is a vowel, unset the last letter's code.
 *   <li>If the next letter is <code>A</code>, <code>E</code>, <code>I</code>, <code>O</code>, <code>U</code>, <code>H</code>, <code>W</code>, <code>Y</code>, continue.
 *   <li>If the next letter's code is equal to the previous letter's
 *       code, continue.
 *   <li>Set the next letter of output to the current letter's code.
 *   </ol>
 * </li>
 * <li>If there are fewer than four characters of output, pad
 * the output with zeros (<code>0</code>)
 * <li>Return the output string.
 * </ol>
 *
 * <p>The table of individual character encodings is as follows:
 *
 * <blockquote><table border='1' cellpadding='5'>
 * <tr><th>Characters</th><th>Code</th></tr>
 * <tr><td>B, F, P, V</td><td>1</td></tr>
 * <tr><td>C, G, J, K, Q, S, X, Z</td><td>2</td></tr>
 * <tr><td>D, T</td><td>3</td></tr>
 * <tr><td>L</td><td>4</td></tr>
 * <tr><td>M, N</td><td>5</td></tr>
 * <tr><td>R</td><td>6</td></tr>
 * <table></blockquote>
 *
 * <p>Here are some examples of translations from the unit tests,
 * drawn from the sources cited below.
 *
 * <blockquote><table border='1' cellpadding='5'>
 * <tr><th>Tokens</th><th>Soundex Encoding</th><th>Notes</th></tr>
 * <tr><td>Gutierrez</td><td>G362</td><td> </td></tr>
 * <tr><td>Pfister</td><td>P236</td><td> </td></tr>
 * <tr><td>Jackson</td><td>J250</td><td> </td></tr>
 * <tr><td>Tymczak</td><td>T522</td><td> </td></tr>
 * <tr><td>Ashcraft</td><td>A261</td><td> </td></tr>
 * <tr><td>Robert, Rupert</td><td>R163</td><td> </td></tr>
 * <tr><td>Euler, Ellery</td><td>E460</td><td> </td></tr>
 * <tr><td>Gauss, Ghosh</td><td>G200</td><td> </td></tr>
 * <tr><td>Hilbert, Heilbronn</td><td>H416</td><td> </td></tr>
 * <tr><td>Knuth, Kant</td><td>K530</td><td> </td></tr>
 * <tr><td>Lloyd, Liddy</td><td>L300</td><td> </td></tr>
 * <tr><td>Lukasiewicz, Lissajous</td><td>L222</td><td> </td></tr>
 * <tr><td>Wachs, Waugh</td><td>W200</td><td></td></tr>
 * </table></blockquote>
 *
 * <p>As a tokenizer filter, the <code>SoundexFilterTokenizer</code>
 * simply replaces each token with its Soundex equivalent.  Note that
 * this may produce very many <code>0000</code> outputs if it is fed
 * standard text with punctuation, numbers, etc.
 *
 * <p>Note: In order to produce a deterministic tokenizer filter,
 * names with prefixes are coded with the prefix.  Recall that
 * Soundex considers the following set of words prefixes, and suggests
 * providing both the Soundex computed with the prefix and the
 * Soundex encoding computed without the prefix:
 *
 * <blockquote><pre>
 * Van, Con, De, Di, La, Le</pre></blockquote>
 *
 * <p>These are not accorded any special treatment by this
 * implementation.
 *
 *
 * <h4>Thread Safety</h4>
 *
 * An English stop-listed tokenizer factory is thread safe if its
 * base tokenizer factory is thread safe.
 *
 * <h4>Serialization</h4>
 *
 * <p>An {@code EnglishStopTokenizerFactory} is serializable if its
 * base tokenizer factory is serializable.
 *
 * <h3>References and Historical Notes</h3>
 *
 * Soundex was invented and patented by Robert C. Russell in 1918.
 * The original version involved eight categories, including one for
 * vowels, without the initial character being treated specially as to
 * coding.  The first vowel was retained in the original Soundex.
 * Furthermore, some positional information was added, such as the
 * deletion of final <code>s</code> and <code>z</code>.
 *
 * <p> The version in this class is the one described by Donald Knuth
 * in <i>The Art of Computer Programming</i> and the one described by
 * the United States National Archives and Records Administration
 * version, which has been used for the United States Census.
 *
 * <ul>
 *
 * <li>Knuth, D.  1973.  <i>The Art of Computer Programming Volum 3: Sorting and Searching</i>. Addison-Wesley.  2nd Edition Pages 394-395.</li>
 *
 * <li>Wikipedia. <a href="http://en.wikipedia.org/wiki/Soundex">Soundex</a>.
 *
 * <li>United States National Archives and Records Administration.
 * <a href="http://www.archives.gov/publications/general-info-leaflets/55.html">Using the Census Soundex.</a>
 * General Information Leaflet 55.
 * </li>
 *
 * <li>Robert C. Russell.  1918. <a
 * href="http://www.pat2pdf.org/pat2pdf/foo.pl?number=1261167">United States Patent
 * 1,261,167</a>.  </li>

 * <li>Robert C. Russell.  1922.
 * <a href="http://www.pat2pdf.org/pat2pdf/foo.pl?number=1435663">United States Patent 1,435,663</a>.
 * </li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
public class SoundexTokenizerFactory
    extends ModifyTokenTokenizerFactory
    implements Serializable {

    static final long serialVersionUID = -7062805184862100578L;

    /**
     * Construct a Soundex-based tokenizer factory that converts
     * tokens produced by the specified base factory into their
     * soundex representations.
     *
     * @param factory Base tokenizer factory.
     */
    public SoundexTokenizerFactory(TokenizerFactory factory) {
        super(factory);
    }

    /**
     * Returns the Soundex encoding of the specified token.
     *
     * <p>See the class documentation above for more
     * information on the encoding.
     *
     * @param token Input token.
     * @return The soundex encoding of the input token.
     */
    public String modifyToken(String token) {
        return soundexEncoding(token);
    }

    @Override
    public String toString() {
        return getClass().toString()
            + "\n  base factory=" 
            + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    /**
     * Returns the Soundex encoding of the specified token.
     *
     * @param token Token to be encoded.
     * @return The Soundex encoding of the specified token.
     */
    public static String soundexEncoding(String token) {
        int pos = 0;
        while (pos < token.length()) {
            char c = token.charAt(pos);
            if (c < 256 && INITIAL_CODES[c] != NON_CHAR_CODE)
                break;
            ++pos;
        }
        if (pos == token.length())
            return "0000"; // nothing

        int csPos = 1;
        char[] cs = new char[4];
        cs[0] = INITIAL_CODES[token.charAt(pos)];
        char lastCode = CODES[token.charAt(pos)];
        ++pos;

        while (csPos < 4 && pos < token.length()) {
            char c = token.charAt(pos);
            ++pos;
            if (c > 255) continue;
            char code = CODES[c];
            if (code == NON_CHAR_CODE) {
                if (VOWELS[c])
                    lastCode = '7'; // never matches, forces next char to code
                continue;
            }
            if (code == lastCode) continue;
            cs[csPos] = code;
            lastCode = code;
            ++csPos;
        }

        while (csPos < 4) {
            cs[csPos] = '0';
            ++csPos;
        }
        return new String(cs);
    }

    static char soundexCode(char upperCaseLetter) {
        switch (upperCaseLetter) {
        case 'B' : return '1';
        case 'F' : return '1';
        case 'P' : return '1';
        case 'V' : return '1';

        case 'C' : return '2';
        case 'G' : return '2';
        case 'J' : return '2';
        case 'K' : return '2';
        case 'Q' : return '2';
        case 'S' : return '2';
        case 'X' : return '2';
        case 'Z' : return '2';

        case 'D' : return '3';
        case 'T' : return '3';

        case 'L' : return '4';

        case 'M' : return '5';
        case 'N' : return '5';

        case 'R' : return '6';

        // ignore A, E, I, O, U, H, W, Y  & all else
        default: return NON_CHAR_CODE;
        }
    }

    static char NON_CHAR_CODE = (char) 0xFF;
    static final char[] INITIAL_CODES = new char[256];
    static final char[] CODES = new char[256];
    static {
        for (int i = 0; i < 256; ++i) {
            char c = (char) i;
            if (!Character.isLetter(c)) {
                INITIAL_CODES[i] = NON_CHAR_CODE;
                CODES[i] = NON_CHAR_CODE;
            } else {
                INITIAL_CODES[i]
                    = Character
                    .toUpperCase(Strings.deAccentLatin1(c));
                CODES[i] = soundexCode(INITIAL_CODES[i]);
            }
        }
    }

    static final boolean[] VOWELS = new boolean[256];
    static {
        for (int i = 0; i < 256; ++i) {
            char initCode = INITIAL_CODES[i];
            VOWELS[i] = initCode == 'A'
                || initCode == 'E'
                || initCode == 'I'
                || initCode == 'O'
                || initCode == 'U';
        }
    }

    static class Serializer
        extends AbstractSerializer<SoundexTokenizerFactory> {
        static final long serialVersionUID = 2496844521092643488L;
        public Serializer(SoundexTokenizerFactory factory) {
            super(factory);
        }
        public Serializer() {
            this(null);
        }
        public Object read(ObjectInput in,
                           TokenizerFactory baseFactory) {
            return new SoundexTokenizerFactory(baseFactory);
        }
    }



}