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

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LMClassifier;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.demo.framework.AbstractTextDemo;

import com.aliasi.xml.SAXWriter;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

import java.util.Properties;

import org.xml.sax.SAXException;

/**
 * The <code>EchoDemo</code> is a text demo that simply pipes
 * its input to its output.  Because the parent class is
 * able to handle arbitrary character sets on input and output,
 * this demo may be used for converting one character set to
 * another.  Because the parent class normalizes HTML to XHTML,
 * this demo may be used for HTML normalization.
 * 
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe2.3
 */
public class PolarityDemo extends AbstractTextDemo {

    final LMClassifier<?,?> mSubjectivityClassifier;
    final LMClassifier<?,?> mPolarityClassifier;
    final SentenceChunker mSentenceChunker;

    public PolarityDemo(String subjectivityModelResource,
                        String polarityModelResource) {
        super(TITLE,DESCRIPTION);

        // marshal models
        mSubjectivityClassifier = (LMClassifier<?,?>) readResource(subjectivityModelResource);
        mPolarityClassifier = (LMClassifier<?,?>) readResource(polarityModelResource);
        mSentenceChunker 
            = new SentenceChunker(IndoEuropeanTokenizerFactory.INSTANCE,
                                  new IndoEuropeanSentenceModel(true,false));
        // add links
        addModel("Subjectivity Model",subjectivityModelResource);
        addModel("Polarity Model",polarityModelResource);
        addTutorial("Sentiment Tutorial",
                    "hptt://alias-i.com/lingpipe/lingpipe/demos/tutorial/sentiment/read-me.html");
    }


    public void process(char[] cs, int start, int end,
			SAXWriter writer,
			Properties properties) 
	throws SAXException {


        String review = new String(cs,start,end-start);
        writer.characters("\n  ");
        writer.startSimpleElement("subjectivity");
        CharSequence subjectiveSentences = subjectiveSentencesReport(review,writer);
        writer.characters("\n  ");
        writer.endSimpleElement("subjectivity");

        ConditionalClassification classification = mPolarityClassifier.classify(subjectiveSentences);
        double pos = Double.NaN;
        if (classification.category(0).equals("pos")) {
            pos = classification.conditionalProbability(0);
        } else {
            pos = classification.conditionalProbability(1);
        }
        writer.characters("\n ");
        writer.startSimpleElement("polarity","probPositive",Double.toString(pos));
        writer.characters("\n");
    }


    CharSequence subjectiveSentencesReport(String review, SAXWriter writer) 
        throws SAXException {

        BoundedPriorityQueue<ScoredObject<CharSequence>> pQueue 
            = new BoundedPriorityQueue<ScoredObject<CharSequence>>(ScoredObject.comparator(),
                                                                   MAX_SENTS);
        Chunking sentenceChunking = mSentenceChunker.chunk(review);
        for (Chunk chunk : sentenceChunking.chunkSet()) {
            CharSequence sentence = review.substring(chunk.start(),chunk.end());
            ConditionalClassification subjClassification
                = (ConditionalClassification) 
                mSubjectivityClassifier.classify(sentence);
            double subjProb = Double.NaN;
            if (subjClassification.category(0).equals("quote"))
                subjProb = subjClassification.conditionalProbability(0);
            else
                subjProb = subjClassification.conditionalProbability(1);
            pQueue.offer(new ScoredObject<CharSequence>(sentence,subjProb));
            writer.characters("\n        ");
            writer.startSimpleElement("s","probSubjective",Double.toString(subjProb));
            writer.characters(sentence.toString());
            writer.endSimpleElement("s");
        }
        StringBuilder reviewBuf = new StringBuilder();
        int numSentences = 0;
        for (ScoredObject<CharSequence> so : pQueue) {
            if (so.score() < .5 && numSentences >= MIN_SENTS) break;
            if (numSentences > 0) reviewBuf.append('\n');
            reviewBuf.append(so.getObject());
            ++numSentences;
        }
        return reviewBuf;
    }

    static int MIN_SENTS = 5;
    static int MAX_SENTS = 25;
    static final String TITLE = "Movie Sentiment Demo";
    static final String DESCRIPTION =  "This is the movie sentiment demo."
        + " It may be used for determining objective/subjective and positive/negative sentences in movie reviews.";



}
