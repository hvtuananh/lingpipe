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
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.xml.DelegatingHandler;

import java.util.ArrayList;
import java.util.List;


import org.xml.sax.Attributes;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>Muc6ChunkParser</code> parses MUC6-formatted named-entity
 * corpora in XML.
 *
 * <h3>SGML to XML Munging</h3>
 *
 * <p>Because the MUC corpora are formatted using SGML, we employed a
 * program to munge the actual data by replacing unknown entity
 * references with simple equivalents, as follows:
 *
 * <ul>
 * <li> <code>&amp;MD;</code> is replaced with a pair of dashes
 * (<code>--</code>)</li>
 * <li> <code>&amp;LR;</code>, <code>&amp;UR;</code>, <code>&amp;QR;</code>
 * and <code>&amp;QC;</code> are removed
 * <li><code>&amp;AMP;</code> is replaced with <code>&amp;amp;</code>.
 * </ul>
 *
 * We also added a DTD declaration with the UTF-8 character format
 * (the original data is all in the ASCII range, 0-127).  Finally,
 * we removed <code>STORYID</code> and <code>SLUG</code> elements
 * and all of their content.
 *
 * <h3>Corpus Format Requirements</h3>
 *
 * <p>The data files must be well-formed XML, as an XML parser is used
 * to parse them.  Training is restricted to the sentence
 * (<code>s</code>) elements, the entities in which are wrapped in
 * an <code>ENAMEX</code> element.  An example is:

 only requirements for this format is that it is organized by
 * sentence with named-entities marked with the <code>ENAMEX</code>
 * element, as in:
 *
 * <blockquote><pre>...
 * &lt;s&gt; After 20 years of pushing labor proposals to
 * overhaul the nation's health-care system, &lt;ENAMEX
 * TYPE="PERSON"&gt;Bert Seidman&lt;/ENAMEX&gt; of &lt;ENAMEX
 * TYPE="ORGANIZATION"&gt;the AFL-CIO&lt;/ENAMEX&gt; is finding interest from
 * an unlikely quarter: big business.  &lt;/s&gt;
 * ...</pre></blockquote>
 *
 * <p>Any other containing elements, such as the paragraph
 * (<code>p</code>) elements in the MUC6 data, will be ignored.  There
 * should be no additional element markup within the <code>s</code>
 * elements other than the <code>ENAMEX</code> elements.  These
 * <code>ENAMEX</code> elements must have an attribute
 * <code>TYPE</code> whose value is the entity type of the element.
 * For most of the chunkers, extra whitespace does not matter; the
 * extra whitespace above is courtesy of the original corpus.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe2.2
 * @deprecated This class will move to the demos in 4.0.
 */
@Deprecated
public class Muc6ChunkParser 
    extends XMLParser<ObjectHandler<Chunking>> {

    String mSentenceTag = "s";  // default for MUC6

    /**
     * Construct a MUC6 chunk parser with no handler specified.
     */
    public Muc6ChunkParser() {
        super();
    }

    /**
     * Construct a MUC6 chunk parser with the specified chunk handler.
     *
     * @param handler Chunk handler for the parser.
     */
    public Muc6ChunkParser(ObjectHandler<Chunking> handler) {
        super(handler);
    }

    @Override
    protected DefaultHandler getXMLHandler() {
        return new MucHandler(getHandler());
    }

    /**
     * Sets the value of the sentence tag to be the specified value.
     * Only elements within sentences will be picked up by the parser.
     *
     * @param tag Tag marking sentence elements.
     */
    public void setSentenceTag(String tag) {
        mSentenceTag = tag;
    }

    class MucHandler extends DelegatingHandler {
        ObjectHandler<Chunking> mChunkHandler;
        SentenceHandler mSentHandler;
        MucHandler(ObjectHandler<Chunking> chunkHandler) {
            mChunkHandler = chunkHandler;
            mSentHandler = new SentenceHandler();
            setDelegate(mSentenceTag,mSentHandler);
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler handler) {
            Chunking chunking = mSentHandler.getChunking();
            mChunkHandler.handle(chunking);
        }
    }

    static class SentenceHandler extends DefaultHandler {
        StringBuilder mBuf;
        String mType;
        int mStart;
        int mEnd;
        final List<Chunk> mChunkList = new ArrayList<Chunk>();
        SentenceHandler() {
            /* do nothing */
        }
        @Override
        public void startDocument() {
            mBuf = new StringBuilder();
            mChunkList.clear();
        }
        @Override
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes) {
            if (!"ENAMEX".equals(qName)) return;
            mType = attributes.getValue("TYPE");
            mStart = mBuf.length();
        }
        @Override
        public void endElement(String uri, String localName, String qName) {
            if (!"ENAMEX".equals(qName)) return;
            mEnd = mBuf.length();
            Chunk chunk = ChunkFactory.createChunk(mStart,mEnd,mType,0);
            mChunkList.add(chunk);
        }
        @Override
        public void characters(char[] cs, int start, int length) {
            mBuf.append(cs,start,length);
        }
        public Chunking getChunking() {
            ChunkingImpl chunking = new ChunkingImpl(mBuf);
            for (Chunk chunk : mChunkList)
                chunking.add(chunk);
            return chunking;
        }
    }

}
