
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.tokenizer.*;
import com.aliasi.symbol.*;
import com.aliasi.util.ObjectToCounterMap;

import java.io.*;
import java.util.*;
import java.util.zip.*;



// ftp://ftp.wormbase.org/pub/wormbase/misc/literature/2007-12-01-wormbase-literature.endnote.gz

public class LdaWormbase {

    public static void main(String[] args) throws Exception {
        File corpusFile = new File(args[0]);
        int minTokenCount = 5;
        short numTopics = 50;
        double topicPrior = 0.1;
        double wordPrior = 0.01;
        int burninEpochs = 0;
        int sampleLag = 1;
        int numSamples = 2000;
        long randomSeed = 6474835;

        System.out.println("Citation file=" + corpusFile);
        System.out.println("Minimum token count=" + minTokenCount);
        System.out.println("Number of topics=" + numTopics);
        System.out.println("Topic prior in docs=" + topicPrior);
        System.out.println("Word prior in topics=" + wordPrior);
        System.out.println("Burnin epochs=" + burninEpochs);
        System.out.println("Sample lag=" + sampleLag);
        System.out.println("Number of samples=" + numSamples);

        CharSequence[] articleTexts = readCorpus(corpusFile);
        
        // reportCorpus(articleTexts);

        SymbolTable symbolTable = new MapSymbolTable();
        int[][] docTokens
            = LatentDirichletAllocation
            .tokenizeDocuments(articleTexts,WORMBASE_TOKENIZER_FACTORY,symbolTable,minTokenCount);
        
        System.out.println("Number of unique words above count threshold=" + symbolTable.numSymbols());

        int numTokens = 0;
        for (int[] tokens : docTokens)
            numTokens += tokens.length;
        System.out.println("Tokenized.  #Tokens After Pruning=" + numTokens);

        LdaReportingHandler handler
            = new LdaReportingHandler(symbolTable);

        LatentDirichletAllocation.GibbsSample sample
            = LatentDirichletAllocation
            .gibbsSampler(docTokens,

                          numTopics,
                          topicPrior,
                          wordPrior,

                          burninEpochs,
                          sampleLag,
                          numSamples,

                          new Random(randomSeed),
                          handler);

        int maxWordsPerTopic = 200;
        int maxTopicsPerDoc = 10;
        boolean reportTokens = true;
        handler.fullReport(sample,maxWordsPerTopic,maxTopicsPerDoc,reportTokens);
    }

    static void reportCorpus(CharSequence[] cSeqs) {
        ObjectToCounterMap<String> tokenCounter = new ObjectToCounterMap<String>();
        for (CharSequence cSeq : cSeqs) {
            char[] cs = cSeq.toString().toCharArray();
            for (String token : WORMBASE_TOKENIZER_FACTORY.tokenizer(cs,0,cs.length))
                tokenCounter.increment(token);
        }
        System.out.println("TOKEN COUNTS");
        for (String token : tokenCounter.keysOrderedByCountList())
            System.out.printf("%9d %s\n",tokenCounter.getCount(token),token);
    }

    static CharSequence[] readCorpus(File file) throws IOException {
        FileInputStream fileIn = new FileInputStream(file);
        GZIPInputStream gzipIn = new GZIPInputStream(fileIn);
        InputStreamReader isReader = new InputStreamReader(gzipIn,"ASCII");
        BufferedReader bufReader = new BufferedReader(isReader);

        List<CharSequence> articleTextList = new ArrayList<CharSequence>(15000);
        StringBuilder docBuf = new StringBuilder();
        String line;
        while ((line = bufReader.readLine()) != null) {
            // docs may only have title
            if (line.startsWith("%T")) {
                docBuf.append(line.substring(3));
            } else if (line.startsWith("%X")) {
                docBuf.append(' ');
                docBuf.append(line.substring(3)); // leave space
            } else if (line.length() == 0 && docBuf.length() > 0) {
                articleTextList.add(docBuf);
                docBuf = new StringBuilder();
            }

        }
        bufReader.close();
        
        int charCount = 0;
        for (CharSequence cs : articleTextList)
            charCount += cs.length();

        System.out.println("#articles=" + articleTextList.size() + " #chars=" + charCount);

        CharSequence[] articleTexts
            = articleTextList
            .<CharSequence>toArray(new CharSequence[articleTextList.size()]);
        return articleTexts;
    }



    /*x LdaWormbase.1 */
    static final TokenizerFactory wormbaseTokenizerFactory() {
        TokenizerFactory factory = BASE_TOKENIZER_FACTORY;
        factory = new NonAlphaStopTokenizerFactory(factory);
        factory = new LowerCaseTokenizerFactory(factory);
        factory = new EnglishStopTokenizerFactory(factory);
        factory = new StopTokenizerFactory(factory,STOPWORD_SET);
        factory = new StemTokenizerFactory(factory);
        return factory;
    }


    static boolean validStem(String stem) {
        if (stem.length() < 2) return false;
        for (int i = 0; i < stem.length(); ++i) {
            char c = stem.charAt(i);
            for (int k = 0; k < VOWELS.length; ++k)
                if (c == VOWELS[k])
                    return true;
        }
        return false;
    }


    static final TokenizerFactory BASE_TOKENIZER_FACTORY
        = new RegExTokenizerFactory("[\\x2Da-zA-Z0-9]+"); // letter or digit or hyphen (\x2D)


    static final char[] VOWELS
        = new char[] { 'a', 'e', 'i', 'o', 'u', 'y' };


    static final String[] STOPWORD_LIST
        = new String[] {

        "these",
        "elegan",
        "caenorhabditi",
        "both",
        "may",
        "between",
        "our",
        "et",
        "al",
        "however",
        "many",

        "thu",
        "thus", // thus

        "how",
        "while",
        "same",
        "here",
        "although",
        "those",
        "might",
        "see",
        "like",
        "likely",
        "where",

        // looked at all 100 count plus

        // "first",
        // "second",
        // "third",
        // "fourth",
        // "fifth",
        // "sixth",
        // "seventh",
        // "eighth",
        // "ninth",

        "i",
        "ii",
        "iii",
        "iv",
        "v",
        "vi",
        "vii",
        "viii",
        "ix",
        "x",
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteen",
        "sixteen",
        "seventeen",
        "eighteen",
        "nineteen",
        "twenty",
        "thirty",
        "forty",
        "fifty",
        "sixty",
        "seventy",
        "eighty",
        "ninety",
        "hundred",
        "thousand",
        "million"
    };

    static final Set<String> STOPWORD_SET
        = new HashSet<String>(Arrays.asList(STOPWORD_LIST));

    static final TokenizerFactory WORMBASE_TOKENIZER_FACTORY
        = wormbaseTokenizerFactory();


    // removes tokens that have no letters
    static class NonAlphaStopTokenizerFactory extends ModifyTokenTokenizerFactory {
        static final long serialVersionUID = -3401639068551227864L;
        public NonAlphaStopTokenizerFactory(TokenizerFactory factory) {
            super(factory);
        }
        public String modifyToken(String token) {
            return stop(token) ? null : token;
        }
        public boolean stop(String token) {
            if (token.length() < 2) return true;
            for (int i = 0; i < token.length(); ++i)
                if (Character.isLetter(token.charAt(i)))
                    return false;
            return true;
        }
    }

    static class StemTokenizerFactory extends ModifyTokenTokenizerFactory {
        static final long serialVersionUID = -6045422132691926248L;
        public StemTokenizerFactory(TokenizerFactory factory) {
            super(factory);
        }
        static final String[] SUFFIXES = new String[] {
            "ss", "ies", "sses", "s" // s must be last as its weaker
        };
        public String modifyToken(String token) {
            for (String suffix : SUFFIXES) {
                if (token.endsWith(suffix)) {
                    String stem = token.substring(0,token.length()-suffix.length());
                    return validStem(stem) ? stem : token;
                }
            }
            return token;
        }
    }



}