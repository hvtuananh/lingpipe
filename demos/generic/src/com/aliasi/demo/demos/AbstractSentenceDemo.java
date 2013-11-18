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

package com.aliasi.demo.demos;

import org.xml.sax.SAXException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

import com.aliasi.demo.framework.AbstractTextDemo;

import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.xml.SAXWriter;

import java.lang.reflect.InvocationTargetException;

import java.util.Iterator;
import java.util.Properties;

/**
 * The <code>AbstractSentenceDemo</code> class provides sentence
 * detection, deferring the text within sentences to further
 * processing.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe2.3
 */
public abstract class AbstractSentenceDemo extends AbstractTextDemo {

    protected final TokenizerFactory mTokenizerFactory;
    protected final SentenceModel mSentenceModel;
    protected final SentenceChunker mSentenceChunker;

    /**
     * Construct a sentence demo using the specified tokenizer
     * factory and model.  The factory and model are reconstituted
     * using reflection using zero-argument constructors.
     *
     * @param tokenizerFactoryClassName Name of tokenizer factory class.
     * @param sentenceModelClassName Name of sentence model class.
     * @param demoName Name of the demo.
     * @param demoDescription Plain text description of the demo.
     */
    public AbstractSentenceDemo(String tokenizerFactoryClassName,
				String sentenceModelClassName,
				String demoName,
				String demoDescription) {
	super(demoName,demoDescription);

        try {
            mTokenizerFactory 
                = (TokenizerFactory)
                Class.forName(tokenizerFactoryClassName).getConstructor(new Class<?>[0]).newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
	try { 
            mSentenceModel
                = (SentenceModel)
                Class.forName(sentenceModelClassName).getConstructor(new Class<?>[0]).newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
	mSentenceChunker
	    = new SentenceChunker(mTokenizerFactory,mSentenceModel);

    }

    /**
     * Extract sentences from the specified character slice,
     * wrapping them in XML sentence elements and deferring
     * their text to <code>processSentence</code> for further
     * processing.
     *
     * @param cs Underlying characters.
     * @param start Index of the first character of slice.
     * @param end Index of one past the last character of the slice.
     * @param writer SAXWriter to which output is written.
     * @param properties Properties for the processing.
     * @throws SAXException If there is an error during processing.
     */
    public void process(char[] cs, int start, int end,
			SAXWriter writer,
			Properties properties) 
	throws SAXException {


	String text = new String(cs,start,end-start);

	Chunking sentenceChunking
	    = mSentenceChunker.chunk(cs,start,end);
	Iterator<Chunk> sentenceIt
	    = sentenceChunking.chunkSet().iterator();
	int pos = 0;
	for (int i = 0; sentenceIt.hasNext(); ++i) {
	    Chunk sentenceChunk = sentenceIt.next();
	    int sentStart = sentenceChunk.start();
	    int sentEnd = sentenceChunk.end();
	    String sentenceText = text.substring(sentStart,sentEnd);

	    writer.characters(text.substring(pos,sentStart));
	    writer.startSimpleElement("s","i",Integer.toString(i));
	    processSentence(sentenceText,writer,properties,i);
	    writer.endSimpleElement("s");
	    pos = sentEnd;
	}
	writer.characters(text.substring(pos));
    }	    

    /**
     * This method will be called with the text of each sentence.
     * The purpose is to analyze the text further, given the specified
     * properites, writing the results to the specified SAX writer.
     * The caller will wrap the output of this method in sentence
     * elements, but does not echo back the sentence text itself -- that
     * must be done in this method.
     *
     * @param sentenceText Text of sentence to process.
     * @param writer SAX writer to which to write results.
     * @param properities Properties of the request
     * @param sentenceNum Number of sentence in order in text.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public abstract void processSentence(String sentenceText,
					 SAXWriter writer,
					 Properties properties,
					 int sentenceNum)
	throws SAXException;

}
