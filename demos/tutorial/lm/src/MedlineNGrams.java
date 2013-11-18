import com.aliasi.lingmed.medline.parser.Abstract;
import com.aliasi.lingmed.medline.parser.Article;
import com.aliasi.lingmed.medline.parser.MedlineCitation;
import com.aliasi.lingmed.medline.parser.MedlineHandler;
import com.aliasi.lingmed.medline.parser.MedlineParser;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.Chunker;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Streams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.Set;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @deprecated Because MEDLINE was deprecated; see lingmed sandbox project.
 */
@SuppressWarnings("deprecation") @Deprecated
public class MedlineNGrams {

    public static void main(String[] args)
        throws IOException,
               SAXException {

        File medlineDir = new File(args[0]);
        System.out.println("MEDLINE Sample Data Directory=" + medlineDir);

        File indexDir = new File(args[1]);
        System.out.println("Index Directory=" + indexDir);


        int maxCharsBuffered = 4 * 1024 * 1024;
        int maxFilesPerLevel = 2;
        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        int nGramOrder = 5;
        String encoding = "UTF-8";
        final TokenNGramIndexer indexer
            = new TokenNGramIndexer(maxCharsBuffered,
                                    maxFilesPerLevel,
                                    tokenizerFactory,
                                    nGramOrder,
                                    indexDir,encoding);
        final Chunker sentenceChunker
            = new SentenceChunker(tokenizerFactory,
                                  new MedlineSentenceModel());

        /**
         * @deprecated Medline handlers deprecated
         */
        @SuppressWarnings("deprecation") @Deprecated
        MedlineHandler medlineHandler = new MedlineHandler() {
                public void handle(MedlineCitation citation) {
                    String pmid = citation.pmid();
                    Article article = citation.article();
                    String title = article.articleTitleText();
                    // System.out.println("T " + pmid + "|" + title);
                    indexer.handle(title);
                    Abstract abstrct = article.abstrct();
                    if (abstrct == null) return;
                    String text = abstrct.textWithoutTruncationMarker();
                    Chunking sentenceChunking
                        = sentenceChunker.chunk(text);
                    Set<Chunk> sentenceChunks
                        = sentenceChunking.chunkSet();
                    int sentCount = 0;
                    for (Chunk chunk : sentenceChunks) {
                        int start = chunk.start();
                        int end = chunk.end();
                        String sentence = text.substring(start,end);
                        ++sentCount;
                        // System.out.println("S" + sentCount
                        // + " " + pmid
                        // + "|" + sentence);
                        indexer.handle(sentence);
                    }
                }
                public void delete(String id) { }
            };

        boolean includeRawXml = false;

        MedlineParser parser = new MedlineParser(includeRawXml);
        parser.setHandler(medlineHandler);

        FileExtensionFilter xmlGzipFilter
            = new FileExtensionFilter(".xml.gz",false); // no dirs
        for (File file : medlineDir.listFiles(xmlGzipFilter)) {
            System.out.println(" Data File=" + file);
            InputStream fileIn = null;
            InputStream zipIn = null;
            try {
                fileIn = new FileInputStream(file);
                zipIn = new GZIPInputStream(fileIn);
                InputSource in = new InputSource(zipIn);
                in.setEncoding("UTF-8");
                parser.parse(in);
            } finally {
                Streams.closeQuietly(zipIn);
                Streams.closeQuietly(fileIn);
            }

        }
        System.out.println("Closing Index");
        indexer.close();
        int minFinalCount = 2;
        System.out.println("Optimizing, minFinalCount=" + minFinalCount);
        indexer.optimize(minFinalCount);
    }

}