package com.aliasi.demo.demos;

import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.NBestChunker;
import com.aliasi.chunk.ConfidenceChunker;

import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.SimpleElementHandler;

import com.aliasi.util.FastCache;
import com.aliasi.util.Streams;
import com.aliasi.util.ScoredObject;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class NamedEntityDemo extends AbstractSentenceDemo {

    private Chunker mEntityChunker;

    private final int MAX_N_BEST = 16;
    private final int MAX_CONF = 16;
    private final double MIN_CONF_LOG2_P = -10;

    public NamedEntityDemo(String tokenizerFactoryClassName,
                           String sentenceModelClassName,
                           String chunkerResourceName,
                           String genre) {
        super(tokenizerFactoryClassName,sentenceModelClassName,
              "Named Entity Demo",
              "Named Entity Demo for " + genre);

        mEntityChunker 
            = (Chunker) readResource(chunkerResourceName);

        declareProperty(PosDemo.RESULT_TYPE_PARAM,
                        PosDemo.RESULT_TYPE_VALS,
                        PosDemo.RESULT_TYPE_TOOL_TIP);

    }

    public void processSentence(String sentenceText, SAXWriter writer,
                                Properties properties,
                                int sentId) throws SAXException {
        String resultType = properties.getProperty("resultType");
        int pos = 0;
        if (resultType.equals(PosDemo.FIRST_BEST_RESULT_TYPE)) {
            Chunking mentionChunking = mEntityChunker.chunk(sentenceText);
            writeMentionChunking(writer,mentionChunking);
        } else if (resultType.equals(PosDemo.N_BEST_RESULT_TYPE)) {
            if (!(mEntityChunker instanceof NBestChunker)) {
                writer.characters("THIS NE MODEL DOES NOT SUPPORT N-BEST");
                return;
            }
            NBestChunker nBestChunker = (NBestChunker) mEntityChunker;
            char[] cs = sentenceText.toCharArray();
            Iterator<ScoredObject<Chunking>> chunkingIt = nBestChunker.nBest(cs,0,cs.length,
                                                                             MAX_N_BEST);
            for (int i = 0; i < MAX_N_BEST && chunkingIt.hasNext(); ++i) {
                ScoredObject<Chunking> so = chunkingIt.next();
                double log2P = so.score();
                Chunking chunking = so.getObject();
                writer.startSimpleElement("analysis",
                                          "rank",Integer.toString(i),
                                          "jointLog2P",Double.toString(log2P));
                writeMentionChunking(writer,chunking);
                writer.endSimpleElement("analysis");
            }
        } else if (resultType.equals(PosDemo.CONF_RESULT_TYPE)) {
            if (!(mEntityChunker instanceof ConfidenceChunker)) {
                writer.characters("THIS NE MODEL DOES NOT SUPPORT CONFIDENCE CHUNKING");
                return;
            }
            writer.startSimpleElement("nBestEntities");
            writer.startSimpleElement("s");
            writer.characters(sentenceText);
            writer.endSimpleElement("s");
            writer.startSimpleElement("confidence");
            ConfidenceChunker confChunker = (ConfidenceChunker) mEntityChunker;
            char[] cs = sentenceText.toCharArray();
            Iterator<Chunk> it = confChunker.nBestChunks(cs,0,cs.length,MAX_CONF);
            for (int i = 0; i < MAX_CONF && it.hasNext(); ++i) {
                Chunk chunk = it.next();
                int start = chunk.start();
                int end = chunk.end();
                String type = chunk.type();
                String mentionText = sentenceText.substring(start,end);
                double score = chunk.score();
                double condProb = java.lang.Math.pow(2.0,score);
                Attributes atts 
                    = SimpleElementHandler
                    .createAttributes("TYPE",type,
                                      "START",Integer.toString(start),
                                      "END",Integer.toString(end),
                                      "condProb",
                                      Double.toString(condProb),
                                      "TEXT",mentionText,
                                      "RANK",Integer.toString(i));
                writer.startSimpleElement("ENAMEX",atts);
                writer.characters(mentionText);
                writer.endSimpleElement("ENAMEX");
            }
            writer.endSimpleElement("confidence");
        }
    }

    void writeMentionChunking(SAXWriter writer, Chunking mentionChunking) 
        throws SAXException {

        Set<Chunk> chunkSet = new TreeSet<Chunk>(Chunk.TEXT_ORDER_COMPARATOR);
        chunkSet.addAll(mentionChunking.chunkSet());
        Iterator<Chunk> it = chunkSet.iterator();
        String text = mentionChunking.charSequence().toString();
        int pos = 0;
        while (it.hasNext()) {
            Chunk neChunk = it.next();
            int start = neChunk.start();
            int end = neChunk.end();
            String type = neChunk.type();
            String chunkText = text.substring(start,end);
            String whitespace = text.substring(pos,start);
            writer.characters(whitespace);
            writer.startSimpleElement("ENAMEX","TYPE",type);
            writer.characters(chunkText);
            writer.endSimpleElement("ENAMEX");
            pos = end;
        }
        String whitespace = text.substring(pos);
        writer.characters(whitespace);
    }
            
}
