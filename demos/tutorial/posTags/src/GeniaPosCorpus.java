import com.aliasi.corpus.Parser;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.tag.Tagging;

import com.aliasi.util.Iterators;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Iterator;

import org.xml.sax.InputSource;

public class GeniaPosCorpus implements PosCorpus {

    private final File mGeniaGZipFile;

    public GeniaPosCorpus(File geniaZipFile) {
        mGeniaGZipFile = geniaZipFile;
    }

    public Iterator<InputSource> sourceIterator() throws IOException {
        FileInputStream fileIn = new FileInputStream(mGeniaGZipFile);
        InputSource in = new InputSource(fileIn);
        return Iterators.singleton(in);
    }

    public Parser<ObjectHandler<Tagging<String>>> parser() {
        return new GeniaPosParser();
    }

}

