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

import com.aliasi.xml.SAXWriter;

import java.util.Properties;

import org.xml.sax.SAXException;


/**
 * The <code>SentenceDemo</code> provides sentence detection relative
 * to a sentence model and tokenizer factory.  All of the work is
 * done by the parent class.
 *
 * @author  Bob Carpenter
 * @version 2.3.0
 * @since   LingPipe2.3
 */
public class SentenceDemo extends AbstractSentenceDemo {

    /**
     * Construct a sentence demo from a model of the specified
     * name, a tokenizer factory of the given name.  The genre
     * specificationis merely used in the title and description; it
     * will not affect behavior in any other way.
     *
     * @param tokenizerFactoryClassName Name of tokenizer factory's class.
     * @param sentenceModelName Name of sentence model's class.
     * @param genreTip A description of the genre for title and description.
     */
    public SentenceDemo(String tokenizerFactoryClassName,
			String sentenceModelName,
			String genreTip) {
	
	super(tokenizerFactoryClassName,
	      sentenceModelName,
	      "Sentence Demo: " + genreTip,
	      "This is the sentence demo." 
	      + " It is intended to run over text of genre " + genreTip + ".");
    }

    /** 
     * Simply writes the text of the sentence to the writer.
     * 
     * @param sentenceText Text of sentence to process.
     * @param writer SAX writer to which to write results.
     * @param properities Properties of the request
     */
    public void processSentence(String sentenceText,
				SAXWriter writer,
				Properties properties,
				int sentenceNum)
	 throws SAXException {
	 
	 writer.characters(sentenceText);
     }
    
    

}


