package com.aliasi.demo.demos;

import com.aliasi.demo.framework.AbstractTextDemo;

import com.aliasi.spell.CompiledSpellChecker;

import com.aliasi.xml.SAXWriter;

import com.aliasi.util.Streams;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ChineseWordsDemo extends AbstractTextDemo {

    private String mCorpusName;
    private CompiledSpellChecker mTokenizer;

    public ChineseWordsDemo(String corpusName, String modelResource) {
	mCorpusName = corpusName;
	addModel("Chinese Word Segmentation Model: " + corpusName,
		 modelResource);
	InputStream in = null;
	BufferedInputStream bufIn = null;
	ObjectInputStream objIn = null;
	try {
	    in = ChineseWordsDemo.class.getResourceAsStream(modelResource);
	    bufIn = new BufferedInputStream(in);
	    objIn = new ObjectInputStream(bufIn);
	    Object tokenizer = objIn.readObject();
	    mTokenizer = (CompiledSpellChecker) tokenizer;
	    mTokenizer.setAllowInsert(true);
	    mTokenizer.setAllowMatch(true);
	    mTokenizer.setAllowDelete(false);
	    mTokenizer.setAllowSubstitute(false);
	    mTokenizer.setAllowTranspose(false);
	    mTokenizer.setNumConsecutiveInsertionsAllowed(1);
	    mTokenizer.setNBest(128);
	} catch (IOException e) {
	    String msg = "Corpus Name=" + corpusName
		+ " Model resource=" + modelResource
		+ " IOException=" + e;
	    throw new IllegalArgumentException(msg);
	} catch (ClassNotFoundException e) {
	    String msg = "Corpus Name=" + corpusName
		+ " Model resource=" + modelResource
		+ " IOException=" + e;
	    throw new IllegalArgumentException(msg);
	} finally {
	    Streams.closeQuietly(objIn);
	    Streams.closeQuietly(bufIn);
	    Streams.closeQuietly(in);
	}
    }

    public String title() {
	return "Chinese Word Segmentation: " + mCorpusName;
    }
    
    public String description() {
	return "This is the LingPipe demo for Chinese word segmentation,"
	    + " also known as tokenization."
	    + " It wraps Chinese words in XML elements."
	    + " The notion of word is derived from the corpus prepared by " 
	    + mCorpusName + ".";
    }

    public void process(char[] cs, int start, int end,
			SAXWriter writer,
			Properties properties) throws SAXException {
	String input = new String(cs,start,end-start);
	String response = mTokenizer.didYouMean(input);

	/* more efficient -- no extra double allocation (strings + char array)
	for (int next = 0, last = 0; 
	     (next = response.indexOf(" ",last)) != 0;
	     last = next + 1) {
	    
	    writer.startSimpleElement("tok");
	    writer.characters(cs,start+last,start+next);
	    writer.endSimpleElement("tok");
	}
	*/

	String[] tokens = response.split(" ");
	for (int i = 0; i < tokens.length; ++i) {
	    writer.startSimpleElement("tok");
	    writer.characters(tokens[i]);
	    writer.endSimpleElement("tok");
	}

    }

}
