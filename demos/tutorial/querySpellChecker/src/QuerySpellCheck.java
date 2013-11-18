import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.SpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Query;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.store.SimpleFSDirectory;

import org.apache.lucene.util.Version;

import com.aliasi.util.Files;
import com.aliasi.util.Streams;

import java.io.*;

public class QuerySpellCheck {

    private static final File MODEL_FILE = new File("SpellCheck.model");
    private static final int NGRAM_LENGTH = 5;
    static final File DATA = new File("../../data/rec.sport.hockey/train");
    static final File LUCENE_INDEX_DIR = new File("lucene");
    static final StandardAnalyzer ANALYZER = new StandardAnalyzer(Version.LUCENE_30);
    static final double MATCH_WEIGHT = -0.0;
    static final double DELETE_WEIGHT = -4.0;
    static final double INSERT_WEIGHT = -1.0;
    static final double SUBSTITUTE_WEIGHT = -2.0;
    static final double TRANSPOSE_WEIGHT = -2.0;
    static final int MAX_HITS = 100;

    static final String TEXT_FIELD = "text";

    public static void main(String[] args)
        throws IOException, ClassNotFoundException, ParseException {

        System.out.print(INTRO);
        // collect argument objects
        System.out.println("     CONFIGURATION:");
        System.out.println("     Model File: " + MODEL_FILE);
        System.out.println("     N-gram Length: " + NGRAM_LENGTH);

        FixedWeightEditDistance fixedEdit =
            new FixedWeightEditDistance(MATCH_WEIGHT,
                                        DELETE_WEIGHT,
                                        INSERT_WEIGHT,
                                        SUBSTITUTE_WEIGHT,
                                        TRANSPOSE_WEIGHT);

        NGramProcessLM lm = new NGramProcessLM(NGRAM_LENGTH);
        TokenizerFactory tokenizerFactory
            = new com.aliasi.tokenizer.LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE);
        TrainSpellChecker sc
            = new TrainSpellChecker(lm,fixedEdit,tokenizerFactory);

        FSDirectory fsDir 
            = new SimpleFSDirectory(LUCENE_INDEX_DIR,
                                    new NativeFSLockFactory());
        IndexWriter luceneIndexWriter 
            = new IndexWriter(fsDir,
                              ANALYZER,
                              IndexWriter.MaxFieldLength.LIMITED);

        if (!DATA.isDirectory()) {
            System.out.println("Could not find training directory=" + DATA);
            System.out.println("Have you unpacked the data?");
        }
        String[] filesToIndex = DATA.list();
        for (int i = 0; i < filesToIndex.length; ++i) {
            System.out.println("     File=" + DATA+"/"+filesToIndex[i]);
            String charSequence
                = Files.readFromFile(new File(DATA,filesToIndex[i]),"ISO-8859-1");
            sc.handle(charSequence);
            Document luceneDoc = new Document();
            Field textField
                = new Field(TEXT_FIELD,
                            charSequence,
                            Field.Store.YES,
                            Field.Index.ANALYZED);
            luceneDoc.add(textField);
            luceneIndexWriter.addDocument(luceneDoc);
        }

        System.out.println("Writing model to file=" + MODEL_FILE);
        writeModel(sc,MODEL_FILE);

        System.out.println("Writing lucene index to =" + LUCENE_INDEX_DIR);
        luceneIndexWriter.close();

        // read compiled model from model file
        System.out.println("Reading model from file=" + MODEL_FILE);
        CompiledSpellChecker compiledSC = readModel(MODEL_FILE);

        compiledSC.setTokenizerFactory(tokenizerFactory);
        IndexSearcher luceneSearcher = new IndexSearcher(fsDir);

        System.out.print(TEST_INTRO);
        testModelLoop(compiledSC,luceneSearcher);
        System.out.print(GOODBYE);
    }

    private static void writeModel(TrainSpellChecker sc, File MODEL_FILE)
        throws IOException {

        // create object output stream from file
        FileOutputStream fileOut = new FileOutputStream(MODEL_FILE);
        BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
        ObjectOutputStream objOut = new ObjectOutputStream(bufOut);

        // write the spell checker to the file
        sc.compileTo(objOut);

        // close the resources
        Streams.closeQuietly(objOut);
        Streams.closeQuietly(bufOut);
        Streams.closeQuietly(fileOut);
    }

    private static CompiledSpellChecker readModel(File file)
        throws ClassNotFoundException, IOException {

        // create object input stream from file
        FileInputStream fileIn = new FileInputStream(file);
        BufferedInputStream bufIn = new BufferedInputStream(fileIn);
        ObjectInputStream objIn = new ObjectInputStream(bufIn);

        // read the spell checker
        CompiledSpellChecker sc = (CompiledSpellChecker) objIn.readObject();

        // close the resources and return result
        Streams.closeQuietly(objIn);
        Streams.closeQuietly(bufIn);
        Streams.closeQuietly(fileIn);
        return sc;
    }

    private static void testModelLoop(SpellChecker sc,
                                      IndexSearcher searcher)
        throws IOException,ParseException {

        // create buffered character reader from system in
        InputStreamReader isReader = new InputStreamReader(System.in);
        BufferedReader bufReader = new BufferedReader(isReader);


        QueryParser queryParser 
            = new QueryParser(Version.LUCENE_30,
                              TEXT_FIELD,
                              ANALYZER);
        while (true) {

            // collect query or end if null
            System.out.print("\n>"); System.out.flush();
            String queryString = bufReader.readLine();
            if (queryString == null || queryString.length() == 0)
                break;

            Query query = queryParser.parse(queryString);
            TopDocs results = searcher.search(query,MAX_HITS);

            System.out.println("Found " + results.totalHits
                               + " document(s) that matched query '"
                               + queryString + "':");

            // compute alternative spelling
            String bestAlternative = sc.didYouMean(queryString);

            if (bestAlternative.equals(queryString)) {
                System.out.println(" No spelling correction found.");
            } else {
                try {
                    Query alternativeQuery
                        = queryParser.parse(bestAlternative);
                    TopDocs results2 = searcher.search(alternativeQuery,MAX_HITS);
                    System.out.println("Found " + results2.totalHits
                                       + " document(s) matching best alt='"
                                       + bestAlternative + "':");
                } catch (ParseException e) {
                    System.out.println("Best alternative not valid query.");
                    System.out.println("Alternative=" + bestAlternative);
                }
            }
        }
    }

    private static final String INTRO
        = '\n'
        + "Alias-i Query Spell Checker Demo"
        + '\n'
        + "PHASE I: TRAINING"
        + '\n';

    private static final String TEST_INTRO
        = '\n'
        + "PHASE II: CORRECTION"
        + '\n'
        + "     Constructing Spell Checker from File"
        + '\n'
        + '\n'
        + "Enter query <RETURN>."
        + '\n'
        + "     Use just <RETURN> to exit."
        + '\n';
    private static final String GOODBYE
        = '\n'
        + "     Detected empty line."
        + '\n'
        + "     Ending test."
        + '\n';


}
