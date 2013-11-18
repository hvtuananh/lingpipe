import com.aliasi.corpus.Parser;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.tag.Tagging;


import com.aliasi.util.Files;
import com.aliasi.util.Iterators;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.util.Iterator;

import org.xml.sax.InputSource;

public class MedPostPosCorpus implements PosCorpus {

    private final File mMedPostDir;

    public MedPostPosCorpus(File medPostDir) {
	mMedPostDir = medPostDir;
    }

    public Iterator<InputSource> sourceIterator() {
	return new MedPostSourceIterator(mMedPostDir);
    }

    public Parser<ObjectHandler<Tagging<String>>> parser() {
	return new MedPostPosParser();
    }
			  
    public static class MedPostSourceIterator extends Iterators.Buffered<InputSource> {
	private final File[] mFiles;
	private int mNextFileIndex = 0;
	public MedPostSourceIterator(File medPostDir) {
	    mFiles = medPostDir.listFiles(new FileExtensionFilter("ioc"));
	}
	public InputSource bufferNext() {
	    if (mNextFileIndex >= mFiles.length) return null;
	    try { 
		String url = mFiles[mNextFileIndex++].toURI().toURL().toString();
		return new InputSource(url);
	    } catch (IOException e) {
		return null;
	    }
	}
    }
}

