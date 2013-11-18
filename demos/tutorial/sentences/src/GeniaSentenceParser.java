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

package com.aliasi.corpus.parsers;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.sentences.SentenceChunker;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>GeniaSentenceParser</code> provides a chunk parser for the
 * XML version of the GENIA corpus.  The type assigned to sentence
 * chunks is the constant {@link SentenceChunker#SENTENCE_CHUNK_TYPE}.
 * It only returns the sentences from citation abstracts, not
 * sentences in citation titles.
 *
 * <P>The following example is drawn from the initial part of the merged
 * 3.02 version of the GENIA corpus (with some content ellided and replaced
 * by ellipses (<code>...</code>, but all spaces/linebreaks left as is):
 *
 * <blockquote><table border='1' cellpadding='5'><tr><td><pre>
 &lt;set&gt;
 &lt;article&gt;
 &lt;articleinfo&gt;
 &lt;bibliomisc&gt;MEDLINE:95369245&lt;/bibliomisc&gt;
 &lt;/articleinfo&gt;
 &lt;title&gt;
 &lt;sentence&gt;...&lt;/sentence&gt;
 &lt;/title&gt;
 &lt;abstract&gt;
 &lt;sentence&gt;&lt;w c=&quot;NN&quot;&gt;Activation&lt;/w&gt; &lt;w c=&quot;IN&quot;&gt;of&lt;/w&gt; &lt;w c=&quot;DT&quot;&gt;the&lt;/w&gt; &lt;cons lex=&quot;CD28_surface_receptor&quot; sem=&quot;G#protein_family_or_group&quot;&gt;&lt;cons lex=&quot;CD28&quot; sem=&quot;G#protein_molecule&quot;&gt;&lt;w c=&quot;NN&quot;&gt;CD28&lt;/w&gt;&lt;/cons&gt; &lt;w c=&quot;NN&quot;&gt;surface&lt;/w&gt; &lt;w c=&quot;NN&quot;&gt;receptor&lt;/w&gt;&lt;/cons&gt; &lt;w c=&quot;VBZ&quot;&gt;provides&lt;/w&gt; &lt;w c=&quot;DT&quot;&gt;a&lt;/w&gt; &lt;w c=&quot;JJ&quot;&gt;major&lt;/w&gt; &lt;w c=&quot;JJ&quot;&gt;costimulatory&lt;/w&gt; &lt;w c=&quot;NN&quot;&gt;signal&lt;/w&gt; &lt;w c=&quot;IN&quot;&gt;for&lt;/w&gt; &lt;cons lex=&quot;T_cell_activation&quot; sem=&quot;G#other_name&quot;&gt;&lt;w c=&quot;NN&quot;&gt;T&lt;/w&gt; &lt;w c=&quot;NN&quot;&gt;cell&lt;/w&gt; &lt;w c=&quot;NN&quot;&gt;activation&lt;/w&gt;&lt;/cons&gt; &lt;w c=&quot;VBG&quot;&gt;resulting&lt;/w&gt; &lt;w c=&quot;IN&quot;&gt;in&lt;/w&gt; &lt;w c=&quot;VBN&quot;&gt;enhanced&lt;/w&gt; &lt;w c=&quot;NN&quot;&gt;production&lt;/w&gt; &lt;w c=&quot;IN&quot;&gt;of&lt;/w&gt; &lt;cons lex=&quot;interleukin-2&quot; sem=&quot;G#protein_molecule&quot;&gt;&lt;w c=&quot;NN&quot;&gt;interleukin-2&lt;/w&gt;&lt;/cons&gt; &lt;w c=&quot;(&quot;&gt;(&lt;/w&gt;&lt;cons lex=&quot;IL-2&quot; sem=&quot;G#protein_molecule&quot;&gt;&lt;w c=&quot;NN&quot;&gt;IL-2&lt;/w&gt;&lt;/cons&gt;&lt;w c=&quot;)&quot;&gt;)&lt;/w&gt; &lt;w c=&quot;CC&quot;&gt;and&lt;/w&gt; &lt;cons lex=&quot;cell_proliferation&quot; sem=&quot;G#other_name&quot;&gt;&lt;w c=&quot;NN&quot;&gt;cell&lt;/w&gt; &lt;w c=&quot;NN&quot;&gt;proliferation&lt;/w&gt;&lt;/cons&gt;&lt;w c=&quot;.&quot;&gt;.&lt;/w&gt;&lt;/sentence&gt;
 &lt;sentence&gt;...&lt;/sentence&gt;
 ...
 * </pre></td></tr></table></blockquote>
 *
 * All that is required is to pull all of the text content (including
 * informative spaces) from the sentence elements.
 *
 * <P>The GENIA corpus is available free of charge from:
 *
 * <UL>
 *
 * <LI><a href="http://www-tsujii.is.s.u-tokyo.ac.jp/GENIA/"
 *       >GENIA Project Home Page</a>
 * </UL>
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe2.1.1
 * @deprecated This class will move to the demos in 4.0.
 */
@Deprecated
public class GeniaSentenceParser 
    extends XMLParser<ObjectHandler<Chunking>> {


    /**
     * Construct a GENIA sentence chunk parser with no designated chunk
     * handler.  Chunk handlers may be later set using the method
     * {@link #setHandler(Handler)}.
     *
     * @throws SAXException If there is an error configuring the
     * SAX XML reader required for parsing.
     */
    public GeniaSentenceParser() throws SAXException {
        super();
    }

    /**
     * Construct a GENIA sentence chunk parser with the specified
     * chunk handler.
     *
     * @param handler The chunk handler used to process sentences
     * found by this parser.
     * @throws SAXException If there is an error configuring the
     * SAX XML reader required for parsing.
     */
    public GeniaSentenceParser(ObjectHandler<Chunking> handler) 
        throws SAXException {

        super(handler);
    }


    /**
     * Returns the embedded XML handler.  This method implements
     * the required method for the abstract superclass {@link XMLParser}.
     *
     * @return The XML handler for this class.
     */
    @Override
    protected DefaultHandler getXMLHandler() {
        return new SetHandler(getChunkHandler());
    }

    /**
     * Returns the chunk handler for this sentence parser.  The result
     * will be the same as calling the superclass method {@link
     * #getHandler()}, but the result in this case is cast to type
     * <code>ChunkHandler</code>.
     *
     * @return The chunk handler for this sentence parser.
     * @deprecated Use generic {@link #getHandler()} instead.
     */
    @Deprecated
    public ObjectHandler<Chunking> getChunkHandler() {
        return getHandler();
    }

    /**
     * The tag used for sentence elements in GENIA, namely
     * <code>sentence</code>.
     */
    public static final String GENIA_SENTENCE_ELT = "sentence";

    /**
     * The tag used for abstract elements in GENIA, namely
     * <code>abstract</code>.
     */
    public static final String GENIA_ABSTRACT_ELT = "abstract";

    private static class SetHandler extends DelegatingHandler {
        final ObjectHandler<Chunking> mChunkHandler;
        final AbstractHandler mAbstractHandler;

        SetHandler(ObjectHandler<Chunking> chunkHandler) {
            mChunkHandler = chunkHandler;
            mAbstractHandler = new AbstractHandler(this);
            setDelegate(GENIA_ABSTRACT_ELT,mAbstractHandler);
        }

        @Override
        public void finishDelegate(String qName, DefaultHandler delegate) {
            if (qName.equals(GENIA_ABSTRACT_ELT)) {
                handleSentenceTexts(mAbstractHandler.getSentenceTexts());
            }
        }

        void handleSentenceTexts(List<String> texts) {
            StringBuilder sb = new StringBuilder();
            int numChunks = texts.size();
            int[] lengths = new int[numChunks];
            for (int i = 0; i< numChunks; i++) {
                if (i > 0) sb.append(" ");
                String text = texts.get(i);
                sb.append(text);
                lengths[i] = text.length();
            }
            char[] cs = sb.toString().toCharArray();
            int offset = 0;
            ChunkingImpl chunking = new ChunkingImpl(cs,0,cs.length);
            for (int i = 0; i< numChunks; i++) {
                Chunk chunk
                    = ChunkFactory
                    .createChunk(offset,offset+lengths[i],
                                 SentenceChunker.SENTENCE_CHUNK_TYPE);
                chunking.add(chunk);
                offset += lengths[i]+1;
            }
            mChunkHandler.handle(chunking);
        }
    }


    private static class AbstractHandler extends DelegateHandler {
        final List<String> mSentTexts  = new ArrayList<String>();
        final TextAccumulatorHandler mSentenceHandler
            = new TextAccumulatorHandler();
        public AbstractHandler(DelegatingHandler parent) {
            super(parent);
            setDelegate(GENIA_SENTENCE_ELT, mSentenceHandler);
        }
        @Override
        public void startDocument() {
            mSentTexts.clear();
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler delegate) {
            if (qName.equals(GENIA_SENTENCE_ELT)) {
                String text = mSentenceHandler.getText().trim();
                if (text.length() > 0) mSentTexts.add(text);
            }
        }
        List<String> getSentenceTexts() {
            return mSentTexts;
        }
    }



}
