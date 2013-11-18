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

import com.aliasi.util.AbstractExternalizable;

import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A <code>LineTokenizerFactory</code> treats each line of an input as
 * a token.  Whitespaces separating lines are simply newlines.  This
 * is useful for decoders that work at the line level.
 *
 * <p>Line terminators are as defined in {@link java.util.regex.Pattern},
 * and include all of the Windows, Unix, and Macintosh standards, as well
 * as some unicode extensions.
 *
 * <p>Whitespaces will be either empty strings or strings representing
 * one or more newlines.
 *
 * <p>Tokens may consist entirely of whitespace characters if
 * whitespace is the only thing on a line.  But tokens will never contain
 * sequences representing newlines.  Tokens will alwyas consist of at
 * least one character.
 *
 * <h3>Examples</h3>
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><th>Input String</th><th>Tokens</th><th>Whitespaces</th></tr>
 * <tr><td><code>&quot;&quot;</code></td><td><code>{}</code></td><td><code>{ &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot;abc&quot;</code></td><td><code>{ &quot;abc&quot; }</code></td><td><code>{ &quot;&quot;, &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot;abc\ndef&quot;</code></td><td><code>{ &quot;abc&quot;, &quot;def&quot; }</code></td><td><code>{ &quot;&quot;, &quot;\n&quot;, &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot;abc\r\ndef&quot;</code></td><td><code>{ &quot;abc&quot;, &quot;def&quot; }</code></td><td><code>{ &quot;&quot;, &quot;\r\n&quot;, &quot;&quot; }</code></td></tr>
 * <tr><td><code>&quot; abc\n def \n&quot;</code></td><td><code>{ &quot; abc&quot;, &quot; def &quot; }</code></td><td><code>{ &quot;&quot;, &quot;\n&quot;, &quot;\n&quot; }</code></td></tr>
 * <tr><td><code>&quot;  \n&quot;</code></td><td><code>{ &quot;  &quot; }</code></td><td><code>{ &quot;&quot;, &quot;\n&quot; }</code></td></tr>
 * </table>
 * </blockquote>
 *
 * <h3>Thread Safety</h3>
 * 
 * Line tokenizer factories are completely thread safe.

 * <h3>Serialization</h3>
 *
 * <p>A line tokenizer factory may be serialized.  Upon
 * deserialization, the resulting class will be the singleton
 * item {@link #INSTANCE}.
 *
 * <h3>Implementation Note</h3>
 *
 * <p>This tokenizer factory is nothing more than a convenience
 * wrapper around a very simple {@link RegExTokenizerFactory}, with
 * the simplest possible regular expression:
 *
 * <pre>
 *      RegExTokenizerFactory(&quot;.+&quot;)</pre>
 *
 * <p>Because the regular expression tokenizer factory takes the
 * default regular expression flags (see {@link java.util.regex.Pattern}),
 * the period (<code>.</code>) matches any character except a newline.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe3.2
 */
public class LineTokenizerFactory extends RegExTokenizerFactory {


    static final long serialVersionUID = -6005548133398620559L;

    LineTokenizerFactory() {
        super(".+");
    }

    /**
     * Returns a string representation of this factory, consisting
     * of its name.
     *
     * @return The name of this class.
     */
    @Override public String toString() {
        return getClass().getName();
    }

    Object writeReplace() {
        return new Externalizer();
    }

    /**
     * A reusable instance of this class.  Because line tokenizer
     * factories are thread safe, this instance may be used
     * everywhere.
     */
    public static final LineTokenizerFactory INSTANCE 
        = new LineTokenizerFactory();

    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = -8526719341046184908L;
        public Externalizer() { }
        @Override
        public Object read(ObjectInput in) {
            return INSTANCE;
        }
        @Override
        public void writeExternal(ObjectOutput out) {
        }
    }

}
