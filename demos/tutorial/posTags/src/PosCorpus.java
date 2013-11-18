import com.aliasi.corpus.Parser;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.tag.Tagging;

import java.io.IOException;

import java.util.Iterator;

import org.xml.sax.InputSource;


public interface PosCorpus {

    public Parser<ObjectHandler<Tagging<String>>> parser();

    public Iterator<InputSource> sourceIterator() throws IOException;

}
