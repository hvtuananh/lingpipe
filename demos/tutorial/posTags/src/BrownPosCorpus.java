import com.aliasi.corpus.Parser;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.tag.Tagging;

import com.aliasi.util.Iterators;
import com.aliasi.util.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Iterator;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.xml.sax.InputSource;

public class BrownPosCorpus implements PosCorpus {

    private final File mBrownZipFile;

    public BrownPosCorpus(File brownZipFile) {
	mBrownZipFile = brownZipFile;
    }

    public Parser<ObjectHandler<Tagging<String>>> parser() {
	return new BrownPosParser();
    }

    public Iterator<InputSource> sourceIterator() throws IOException {
	return new BrownSourceIterator(mBrownZipFile);
    }

    static class BrownSourceIterator extends Iterators.Buffered<InputSource> {
	private ZipInputStream mZipIn = null;
	public BrownSourceIterator(File brownZipFile) throws IOException {
	    FileInputStream fileIn = new FileInputStream(brownZipFile);
	    mZipIn = new ZipInputStream(fileIn);
	}
	public InputSource bufferNext() {
	    ZipEntry entry = null;
	    try {
		while ((entry = mZipIn.getNextEntry()) != null) {
		    if (entry.isDirectory()) continue;
		    String name = entry.getName();
		    if (name.equals("brown/CONTENTS") 
			|| name.equals("brown/README")) continue;
		    return new InputSource(mZipIn);
		}
	    } catch (IOException e) {
		// ignore and close and return null
	    }
	    Streams.closeQuietly(mZipIn);
	    return null;	
	}
    }
}
    
