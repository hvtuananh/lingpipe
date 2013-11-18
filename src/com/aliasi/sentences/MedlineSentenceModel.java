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

package com.aliasi.sentences;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;

/**
 * A <code>MedlineSentenceModel</code> is a heuristic sentence model
 * designed for operating over biomedical research abstracts as found
 * in MEDLINE.  
 * 
 * <P>The MEDLINE model assumes that parentheses are balanced as
 * defined in the class documentation for {@link
 * HeuristicSentenceModel}.  It also assumes the final token is a
 * sentence boundary, overriding any other possible checks.  This is
 * set because there are many truncated MEDLINE abstracts, and this
 * ensures that every token falls within a sentence in the result.
 *
 * <P>The sets required by the superclass constructor {@link
 * HeuristicSentenceModel#HeuristicSentenceModel(Set,Set,Set,boolean,boolean)}
 * determine which tokens are possible sentence stops, which are
 * disallowed before stops, and which are disallowed as starts.  These
 * three sets are:
 * 
 * <blockquote>
 * <table border='0' cellpadding='10'>
 * <tr>

 *   <td width='33%' valign='top'>
 *   <table border="1" cellpadding="3" width='100%'>
 *     <tr><td><b>Possible Stops</b></td></tr>
 *     <tr><td><code><b>.</b></code></td></tr>
 *     <tr><td><code><b>..</b></code></td></tr>
 *     <tr><td><code><b>!</b></code></td></tr>
 *     <tr><td><code><b>?</b></code></td></tr>
 *   </table>
 *   </td>
 *
 *   <td width='33%' valign='top'>
 *   <table border="1" cellpadding="3"  width='100%'>
 *     <tr><td><b>Impossible Penultimates</b></td></tr>
 *     <tr><td><i>some scientific and publishing terms</i></td></tr>
 *     <tr><td><i>personal/professional titles/suffixes</i></td></tr>
 *     <tr><td><i>months, times</i></td></tr>
 *     <tr><td><i>corporate designators</i></td></tr>
 *     <tr><td><i>common abbreviations</i></td></tr>
 *     <tr><td><i>back quotes, commas</i></td></tr>
 *   </table>
 *   </td>
 * 
 *   <td width='33%' valign='top'>
 *   <table border="1" cellpadding="3"  width='100%'>
 *     <tr><td><b>Impossible Sentence Starts</b></td></tr>
 *     <tr><td><i>possible stops (see above)</i></td></tr>
 *     <tr><td><i>close parens, brackets, braces</i></td></tr>
 *     <tr><td><code><b>;</b></code></td></tr>
 *     <tr><td><code><b>:</b></code></td></tr>
 *     <tr><td><code><b>-</b></code></td></tr>
 *     <tr><td><code><b>--</b></code></td></tr>
 *     <tr><td><code><b>---</b></code></td></tr>
 *     <tr><td><code><b>%</b></code></td></tr>
 *   </table>
 *   </td>
 * </table>
 * </blockquote>
 *
 * <P>This class overrides the default implementation of the possible
 * start token method to allow a sentence start to be any sequence of
 * tokens uninterrupted by spaces that contains a non-lowercase letter
 * character.  This behavior is described with examples in its
 * implementing method's documentation: {@link
 * #possibleStart(String[],String[],int,int)}.
 *
 * <h3>Singleton Instance</h3>
 *
 * The instance accessible through the static constant {@link #INSTANCE}
 * may be used anywhere a MEDLINE sentence model is needed.
 *
 * <h3>Thread Safety</h3>
 *
 * A MEDLINE sentence model is thread safe after safely published.
 *
 * <h3>Serialization</h3>
 *
 * A MEDLINE sentence model may be serialized.  The deserialized
 * object will be the singleton instance.
 *
 * @author  Mitzi Morris
 * @author Bob Carpenter
 * @version 3.9
 * @since   LingPipe2.1
 */
public class MedlineSentenceModel 
    extends HeuristicSentenceModel 
    implements Serializable {

    static final long serialVersionUID = -8958290440993791272L;

    /**
     * Construct a MEDLINE sentence model.
     */
    public MedlineSentenceModel() {
        super(POSSIBLE_STOPS,
              IMPOSSIBLE_PENULTIMATES,
              IMPOSSIBLE_SENTENCE_STARTS,
              true,true); // +force final stop, +balance parens
    }

    /**
     * Return <code>true</code> if the specified start index can
     * be a sentence start in the specified array of tokens and
     * whitespaces running up to the end token.  
     * 
     * <P>For MEDLINE, this implementation returns <code>true</code>
     * if the sequence of contiguous tokens starting with the
     * specified token contains an uppercase or digit character.  Each
     * token is considered, beginning with the specified start token
     * and continuing through all tokens that are not separated by
     * non-empty whitespace, up to the token with the end index minus
     * one.  If any of the tokens contains an uppercase or digit
     * character, then the result is <code>true</code>.  Otherwise,
     * the result is <code>false</code>.
     *
     * <P>For example, if the first token is &quot;Therefore&quot;, then
     * it can be a sentence start because it contains the non-lowercase
     * letter &quot;T&quot;.  Similarly, the token &quot;pH&quot; can be a sentence start,
     * as can &quot;p53&quot;, because they have non-lower-case characters &quot;H&quot;
     * and &quot;5&quot; respectively.  If the underlying sequence is 
     * &quot; correlation.  p-53 was...&quot;, then the array of tokens
     * and whitespaces is:
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><i>Index</i></td>
     *     <td><i>Whitespace</i></td>
     *     <td><i>Token</i></td></tr>
     * <tr><td>0</td>
     *     <td><code>&quot; &quot;</code></td>
     *     <td><code>correlation</code></td></tr>
     * <tr><td>1</td>
     *     <td><code>&quot;&quot;</code></td>
     *     <td><code>.</code></td></tr>
     * <tr><td>2</td>
     *     <td><code>&quot;  &quot;</code></td>
     *     <td bgcolor='#CCCCFF'><code>p</code></td></tr>
     * <tr><td>3</td>
     *     <td bgcolor='yellow'><code>&quot;&quot;</code></td>
     *     <td bgcolor='#CCCCFF'><code>-</code></td></tr>
     * <tr><td>4</td>
     *     <td bgcolor='yellow'><code>&quot;&quot;</code></td>
     *     <td bgcolor='#CCCCFF'><code>53</code></td></tr>
     * <tr><td>5</td>
     *     <td><code>&quot; &quot;</code></td>
     *     <td><code>was</code></td></tr>
     * <tr><td>6</td>
     *     <td>...</td>
     *     <td><code>&quot; &quot;</code></td></tr>
     * <tr><td colspan='3'>Tokenization of: <code>&quot; correlation.  p-53 was ...&quot;</code></td></tr>
     * </table>
     * </blockquote>
     *
     * Here, &quot;p&quot; is a valid sentence start token even though
     * it is only a single lowercase character, because it is followed
     * by a hyphen (<code>-</code>) with no intervening whitespace.
     * By way of contrast, the first token
     * <code>&quot;and&quot;</code> in the sequence <code>&quot;and
     * Foo&quot;</code>, can't start a sentence because it is separated
     * from the following token by a non-empty whitespace.
     * 
     * Recall that the whitespace with
     * the same index as a token precedes the token.
     *
     * @param tokens Array of tokens to check.
     * @param whitespaces Array of whitespaces to check.
     * @param start Index of first token to check.
     * @param end Index of last token to check.
     */
    @Override
        protected boolean possibleStart(String[] tokens,
                                        String[] whitespaces,
                                        int start, int end) {
        for (int i = start; i < end; i++) {
            if (LOWERCASE_STARTS.contains(tokens[i])) return true;
            if (containsDigitOrUpper(tokens[i])) return true;
            if (whitespaces[i+1].length() > 0) return false;
        }
        return false;
    }

    private boolean containsDigitOrUpper(CharSequence token) {
        int len = token.length();
        for (int i=0; i < len; i++) {
            if (Character.isUpperCase(token.charAt(i))) return true;
            if (Character.isDigit(token.charAt(i))) return true;
        }
        return false;
    }

    Object writeReplace() {
        return new Serializer();
    }


    private static final Set<String> POSSIBLE_STOPS = new HashSet<String>();
    static {
        POSSIBLE_STOPS.add(".");
        POSSIBLE_STOPS.add("..");  // abbrev + stop occurs
        POSSIBLE_STOPS.add("!");
        POSSIBLE_STOPS.add("?");
    }

    private static final Set<String> IMPOSSIBLE_PENULTIMATES
        = new HashSet<String>();
    static {
        // Common Abbrevs
        IMPOSSIBLE_PENULTIMATES.add("Bros");
        IMPOSSIBLE_PENULTIMATES.add("No");  // too common ??
        IMPOSSIBLE_PENULTIMATES.add("al");
        IMPOSSIBLE_PENULTIMATES.add("vs");
        IMPOSSIBLE_PENULTIMATES.add("etc");
        IMPOSSIBLE_PENULTIMATES.add("Fig"); // thanks to MM

        // Professional Honorifics
        IMPOSSIBLE_PENULTIMATES.add("Dr");
        IMPOSSIBLE_PENULTIMATES.add("Prof");
        IMPOSSIBLE_PENULTIMATES.add("PhD");
        IMPOSSIBLE_PENULTIMATES.add("MD");

        // Corporate Designators
        IMPOSSIBLE_PENULTIMATES.add("Co");
        IMPOSSIBLE_PENULTIMATES.add("Corp");
        IMPOSSIBLE_PENULTIMATES.add("Inc");

        // Month Abbrevs
        IMPOSSIBLE_PENULTIMATES.add("Jan");
        IMPOSSIBLE_PENULTIMATES.add("Feb");
        IMPOSSIBLE_PENULTIMATES.add("Mar");
        IMPOSSIBLE_PENULTIMATES.add("Apr");
        //        IMPOSSIBLE_PENULTIMATES.add("Jun");  common term:  c-jun.
        IMPOSSIBLE_PENULTIMATES.add("Jul");
        IMPOSSIBLE_PENULTIMATES.add("Aug");
        IMPOSSIBLE_PENULTIMATES.add("Sep");
        IMPOSSIBLE_PENULTIMATES.add("Sept");
        IMPOSSIBLE_PENULTIMATES.add("Oct");
        IMPOSSIBLE_PENULTIMATES.add("Nov");
        IMPOSSIBLE_PENULTIMATES.add("Dec");

        // Location Suffixes
        IMPOSSIBLE_PENULTIMATES.add("St");

        // times
        IMPOSSIBLE_PENULTIMATES.add("AM");
        IMPOSSIBLE_PENULTIMATES.add("PM");

    }

    private static final Set<String> IMPOSSIBLE_SENTENCE_STARTS
        = new HashSet<String>();
    static {
        IMPOSSIBLE_SENTENCE_STARTS.add(")");
        IMPOSSIBLE_SENTENCE_STARTS.add("]");
        IMPOSSIBLE_SENTENCE_STARTS.add("}");
        IMPOSSIBLE_SENTENCE_STARTS.add(">");
        IMPOSSIBLE_SENTENCE_STARTS.add("<");
        IMPOSSIBLE_SENTENCE_STARTS.add(".");
        IMPOSSIBLE_SENTENCE_STARTS.add("!");
        IMPOSSIBLE_SENTENCE_STARTS.add("?");
        IMPOSSIBLE_SENTENCE_STARTS.add(":");
        IMPOSSIBLE_SENTENCE_STARTS.add(";");
        IMPOSSIBLE_SENTENCE_STARTS.add("-");
        IMPOSSIBLE_SENTENCE_STARTS.add("--");
        IMPOSSIBLE_SENTENCE_STARTS.add("---");
        IMPOSSIBLE_SENTENCE_STARTS.add("%");
    }

    private static final Set<String> LOWERCASE_STARTS
        = new HashSet<String>();
    static {
        LOWERCASE_STARTS.add("alpha");
        LOWERCASE_STARTS.add("beta");
        LOWERCASE_STARTS.add("gamma");
        LOWERCASE_STARTS.add("delta");
        LOWERCASE_STARTS.add("c");  // c-jun, c-myc etc.
        LOWERCASE_STARTS.add("i");
        LOWERCASE_STARTS.add("ii");
        LOWERCASE_STARTS.add("iii");
        LOWERCASE_STARTS.add("iv");
        LOWERCASE_STARTS.add("v");
        LOWERCASE_STARTS.add("vi");
        LOWERCASE_STARTS.add("vii");
        LOWERCASE_STARTS.add("viii");
        LOWERCASE_STARTS.add("ix");
        LOWERCASE_STARTS.add("x");
    }

    /**
     * A single instance which may be used anywhere a MEDLINE
     * sentence model is needed.
     */
    public static final MedlineSentenceModel INSTANCE
        = new MedlineSentenceModel();


    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 8384392069391677984L;
        public Serializer() {
        }
        public void writeExternal(ObjectOutput out) {
        }
        public Object read(ObjectInput in) {
            return MedlineSentenceModel.INSTANCE;
        }
    }
}
