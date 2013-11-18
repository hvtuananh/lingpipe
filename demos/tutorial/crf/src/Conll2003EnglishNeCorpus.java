import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.IoTagChunkCodec;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.chunk.TagChunkCodecAdapters;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.tag.LineTaggingParser;
import com.aliasi.tag.Tagging;

import java.io.File;
import java.io.IOException;

public class Conll2003EnglishNeCorpus
    extends Corpus<ObjectHandler<Chunking>> {

    private final File mConllDataDir;

    public Conll2003EnglishNeCorpus(File conllMungedDataDir)
        throws IOException {

        mConllDataDir = conllMungedDataDir;

    }

    public void visitTrain(ObjectHandler<Chunking> handler)
        throws IOException {

        visit(TRAIN_FILE_NAME,handler);
        visit(DEV_FILE_NAME,handler);
    }


    public void visitTest(ObjectHandler<Chunking> handler) 
        throws IOException {

        visit(TEST_FILE_NAME,handler);
    }

    private void visit(String fileName, 
                       final ObjectHandler<Chunking> handler)
        throws IOException {

        TagChunkCodec codec
            = new IoTagChunkCodec(); 

        ObjectHandler<Tagging<String>> tagHandler
            = TagChunkCodecAdapters
            .chunkingToTagging(codec,handler);

        Parser<ObjectHandler<Tagging<String>>> parser
            = new LineTaggingParser(TOKEN_TAG_LINE_REGEX,
                                    TOKEN_GROUP, TAG_GROUP,
                                    IGNORE_LINE_REGEX,
                                    EOS_REGEX);
        parser.setHandler(tagHandler);
        File file = new File(mConllDataDir,fileName);
        parser.parse(file);
    }

    static final String TOKEN_TAG_LINE_REGEX
        = "(\\S+)\\s\\S+\\s\\S+\\s(O|[B|I]-\\S+)"; // token posTag chunkTag entityTag

    static final int TOKEN_GROUP = 1; // token
    static final int TAG_GROUP = 2;   // entityTag

    static final String IGNORE_LINE_REGEX
        = "-DOCSTART(.*)";  // lines that start with "-DOCSTART"

    static final String EOS_REGEX
        = "\\A\\Z"; 

    static final String TRAIN_FILE_NAME = "eng.train";
    static final String DEV_FILE_NAME = "eng.testa";
    static final String TEST_FILE_NAME = "eng.testb";
    
    public static void main(String[] args) throws IOException {
        File dataDir = new File(args[0]);
        Conll2003EnglishNeCorpus corpus
            = new Conll2003EnglishNeCorpus(dataDir);
        ObjectHandler<Chunking> printer
            = new ObjectHandler<Chunking>() {
                    public void handle(Chunking chunking) {
                        System.out.println(chunking);
                    }
                };
        System.out.println("\nTRAIN");
        corpus.visitTrain(printer);
        
        System.out.println("\nTEST");
        corpus.visitTest(printer);
    }



}