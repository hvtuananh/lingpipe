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

import java.io.ObjectInput;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;

/**
 * An {@code EnglishStopTokenizerFactory} applies an English stop
 * list to a contained base tokenizer factory.
 *
 * <p>The built-in stoplist consists of the following words:
 *
 * <blockquote>
 *   a, be, had, it, only, she, was, about, because, has,
 *   its, of, some, we, after, been, have, last, on, such, were, all,
 *   but, he, more, one, than, when, also, by, her, most, or, that,
 *   which, an, can, his, mr, other, the, who, any, co, if, mrs, out,
 *   their, will, and, corp, in, ms, over, there, with, are, could, inc,
 *   mz, s, they, would, as, for, into, no, so, this, up, at, from, is,
 *   not, says, to
 * </blockquote>
 *
 * Note that the stoplist entries are all lowercase.  Thus the input
 * should probably first be filtered by a {@link
 * LowerCaseTokenizerFactory}.
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
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
public class EnglishStopTokenizerFactory
    extends StopTokenizerFactory
    implements Serializable {

    static final long serialVersionUID = 4616272325206021322L;

    /**
     * Construct an English stop tokenizer factory with the
     * specified base factory.
     *
     * @param factory Base tokenizer factory.
     */
    public EnglishStopTokenizerFactory(TokenizerFactory factory) {
        super(factory,STOP_SET);
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer
        extends AbstractSerializer<EnglishStopTokenizerFactory> {
        static final long serialVersionUID = 3382872690562205086L;
        public Serializer(EnglishStopTokenizerFactory factory) {
            super(factory);
        }
        public Serializer() {
            this(null);
        }
        public Object read(ObjectInput in,
                           TokenizerFactory baseFactory) {
            return new EnglishStopTokenizerFactory(baseFactory);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName()
            + "\n  stop set=" + stopSet()
            + "\n  base factory=\n    "  
            + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    /**
     * The set of stop words, all lowercased.
     */
    static final Set<String> STOP_SET = new HashSet<String>();
    static {
        STOP_SET.add("a");
        STOP_SET.add("be");
        STOP_SET.add("had");
        STOP_SET.add("it");
        STOP_SET.add("only");
        STOP_SET.add("she");
        STOP_SET.add("was");
        STOP_SET.add("about");
        STOP_SET.add("because");
        STOP_SET.add("has");
        STOP_SET.add("its");
        STOP_SET.add("of");
        STOP_SET.add("some");
        STOP_SET.add("we");
        STOP_SET.add("after");
        STOP_SET.add("been");
        STOP_SET.add("have");
        STOP_SET.add("last");
        STOP_SET.add("on");
        STOP_SET.add("such");
        STOP_SET.add("were");
        STOP_SET.add("all");
        STOP_SET.add("but");
        STOP_SET.add("he");
        STOP_SET.add("more");
        STOP_SET.add("one");
        STOP_SET.add("than");
        STOP_SET.add("when");
        STOP_SET.add("also");
        STOP_SET.add("by");
        STOP_SET.add("her");
        STOP_SET.add("most");
        STOP_SET.add("or");
        STOP_SET.add("that");
        STOP_SET.add("which");
        STOP_SET.add("an");
        STOP_SET.add("can");
        STOP_SET.add("his");
        STOP_SET.add("mr");
        STOP_SET.add("other");
        STOP_SET.add("the");
        STOP_SET.add("who");
        STOP_SET.add("any");
        STOP_SET.add("co");
        STOP_SET.add("if");
        STOP_SET.add("mrs");
        STOP_SET.add("out");
        STOP_SET.add("their");
        STOP_SET.add("will");
        STOP_SET.add("and");
        STOP_SET.add("corp");
        STOP_SET.add("in");
        STOP_SET.add("ms");
        STOP_SET.add("over");
        STOP_SET.add("there");
        STOP_SET.add("with");
        STOP_SET.add("are");
        STOP_SET.add("could");
        STOP_SET.add("inc");
        STOP_SET.add("mz");
        STOP_SET.add("s");
        STOP_SET.add("they");
        STOP_SET.add("would");
        STOP_SET.add("as");
        STOP_SET.add("for");
        STOP_SET.add("into");
        STOP_SET.add("no");
        STOP_SET.add("so");
        STOP_SET.add("this");
        STOP_SET.add("up");
        STOP_SET.add("at");
        STOP_SET.add("from");
        STOP_SET.add("is");
        STOP_SET.add("not");
        STOP_SET.add("says");
        STOP_SET.add("to");
    }

}


