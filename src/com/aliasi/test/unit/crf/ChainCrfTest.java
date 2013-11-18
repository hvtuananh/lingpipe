package com.aliasi.test.unit.crf;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.crf.ChainCrf;
import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.crf.ChainCrfFeatures;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.symbol.SymbolTable;
import com.aliasi.symbol.SymbolTableCompiler;

import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

import static com.aliasi.test.unit.Asserts.succeed;

public class ChainCrfTest {

    static String CAT1 = "X";
    static String CAT2 = "Y";
    static String CAT3 = "Z";
    static String[] TAGS = new String[] { CAT1, CAT2, CAT3 };

    static String X1 = "a";
    static String X2 = "b";
    static String X3 = "c";
    static String X4 = "d";
    static String[] TOKENS = new String[] { X1, X2, X3, X4 };

    static String[] FEATURES =
        new String[] { CAT1, CAT2, CAT3, X1, X2, X3, X4 };

    static double XX = 1.0;
    static double XY = 1.0;
    static double XZ = 2.0;
    static double YX = 2.0;
    static double YY = -1.0;
    static double YZ = 4.0;
    static double ZX = 3.0;
    static double ZY = 1.0;
    static double ZZ = 6.0;

    static double[][] TRANSITION_WEIGHTS = new double[][] {
        { XX, YX, ZX },
        { XY, YY, ZY },
        { XZ, YZ, ZZ }
    };

    static double Xa = 4.0;
    static double Xb = 5.0;
    static double Xc = 6.0;
    static double Xd = 7.0;

    static double Ya = -1.0;
    static double Yb = 10.0;
    static double Yc = -1.0;
    static double Yd = 1.0;

    static double Za = -2.0;
    static double Zb = -4.0;
    static double Zc = -6.0;
    static double Zd = 15.0;

    static double[][] TOKEN_WEIGHTS = new double[][] {
        { Xa, Xb, Xc, Xd },
        { Ya, Yb, Yc, Yd },
        { Za, Zb, Zc, Zd }
    };



    static int NUM_TAGS = TAGS.length;


    static Vector[] COEFFICIENTS = new DenseVector[] {
        new DenseVector(new double[] { XX, YX, ZX,
                                       Xa, Xb, Xc, Xd }),

        new DenseVector(new double[] { XY, YY, ZY,
                                       Ya, Yb, Yc, Yd }),

        new DenseVector(new double[] { XZ, YZ, ZZ,
                                       Za, Zb, Zc, Zd })
    };



    static final SymbolTable FEATURE_SYMBOL_TABLE
        = SymbolTableCompiler.asSymbolTable(FEATURES);

    static final ChainCrfFeatureExtractor<String> FEATURE_EXTRACTOR
        = new TestFeatureExtractor();

    static class TestFeatureExtractor
        implements ChainCrfFeatureExtractor<String>,
                   Serializable {

        public ChainCrfFeatures<String> extract(List<String> tokens, List<String> tags) {
            return new TestCrfFeatures(tokens,tags);
        }
    }

    static class TestCrfFeatures
        extends ChainCrfFeatures<String> {
        // could cache maps -- only need one per token and per tag
        public TestCrfFeatures(List<String> tokens, List<String> tags) {
            super(tokens,tags);
        }
        public Map<String,Integer> nodeFeatures(int n) {
            return Collections.singletonMap(token(n),
                                            Integer.valueOf(1));
        }
        public Map<String,Integer> edgeFeatures(int n, int prevTagIndex) {
            return Collections.singletonMap(tag(prevTagIndex),
                                            Integer.valueOf(1));
        }
    }

    static boolean ADD_INTERCEPT_FEATURE = false;

    static ChainCrf<String> CRF
        = new ChainCrf<String>(TAGS,
                               COEFFICIENTS,
                               FEATURE_SYMBOL_TABLE,
                               FEATURE_EXTRACTOR,
                               ADD_INTERCEPT_FEATURE);

    @Test
    public void testDecoder() throws IOException {
        @SuppressWarnings("unchecked")
        ChainCrf<String> crf2
            = (ChainCrf<String>) AbstractExternalizable.serializeDeserialize(CRF);

        assertEquals(CRF.addInterceptFeature(), crf2.addInterceptFeature());
        assertEquals(CRF.featureSymbolTable().numSymbols(),
                     crf2.featureSymbolTable().numSymbols());
        for (int i = 0; i < CRF.featureSymbolTable().numSymbols(); ++i)
            assertEquals(CRF.featureSymbolTable().idToSymbol(i),
                         crf2.featureSymbolTable().idToSymbol(i));
        assertEquals(CRF.tags(), crf2.tags());

        Vector[] coeffsCRF = CRF.coefficients();
        Vector[] coeffsCrf2 = crf2.coefficients();
        assertEquals(coeffsCRF.length, coeffsCrf2.length);
        for (int i = 0; i < coeffsCRF.length; ++i) {
            assertEquals(coeffsCRF[i].numDimensions(),
                         coeffsCrf2[i].numDimensions());
            assertArrayEquals(coeffsCRF[i].nonZeroDimensions(),
                              coeffsCrf2[i].nonZeroDimensions());
            for (int d : coeffsCRF[i].nonZeroDimensions())
                assertEquals(coeffsCRF[i].value(d),
                             coeffsCrf2[i].value(d), 0.0001);
        }


        // tests all 4**0, 4**1, 4**2, and 4**3 length inputs
        for (int length = 0; length < 5; ++length) {
            for (int[] tokenIds : allArrays(length,TOKENS.length)) {
                List<String> tokenList = new ArrayList<String>(length);
                for (int i = 0; i < tokenIds.length; ++i)
                    tokenList.add(TOKENS[tokenIds[i]]);

                // brute force
                ObjectToDoubleMap<int[]> otdMap = bruteForce(tokenIds,TAGS.length,
                                                             TRANSITION_WEIGHTS,
                                                             TOKEN_WEIGHTS);

                // first best eval
                assertCorrectAnswer(CRF,tokenList,otdMap,TAGS);
                assertCorrectAnswer(crf2,tokenList,otdMap,TAGS);

                // n-best eval
                Iterator<ScoredTagging<String>> nBest = CRF.tagNBest(tokenList,Integer.MAX_VALUE);
                assertCorrectNBest(otdMap,nBest,TAGS,false);

                Iterator<ScoredTagging<String>> nBestCond
                    = CRF.tagNBestConditional(tokenList,Integer.MAX_VALUE);
                assertCorrectNBest(otdMap,nBestCond,TAGS,true);

                // marginal eval
                TagLattice<String> tagLattice = CRF.tagMarginal(tokenList);
                assertCorrectMarginal(otdMap,tagLattice,TAGS,tokenList);
            }
        }
    }

    void assertCorrectMarginal(ObjectToDoubleMap<int[]> otdMap,
                               TagLattice<String> tagLattice,
                               String[] tags,
                               List<String> tokenList) {
        assertEquals(tokenList,tagLattice.tokenList());

        double logZ = logZ(otdMap);
        assertEquals(logZ,tagLattice.logZ(),0.001);

        List<String> tagList = tagLattice.tagList();
        for (int pos = 0; pos < tokenList.size(); ++pos) {
            double sum = 0.0;
            for (int tagId = 0; tagId < tagList.size(); ++tagId) {
                sum += Math.exp(tagLattice.logProbability(pos,tagId));
                assertEquals(logMarginal(otdMap,pos,tagId,tags.length,logZ),
                             tagLattice.logProbability(pos,tagId),
                             0.0001);
            }
            assertEquals("marginals norm " + pos + " " + tokenList,1.0,sum,0.01);
        }
    }

    static double logMarginal(ObjectToDoubleMap<int[]> otdMap,
                              int pos,
                              int tagId,
                              int numTags,
                              double logZ) {
        int count = 0;
        for (int[] key : otdMap.keySet()) {
            if (key[pos] == tagId)
                ++count;
        }
        double[] xs = new double[count];
        count = 0;
        for (Map.Entry<int[],Double> entry : otdMap.entrySet())
            if (tagId == entry.getKey()[pos])
                xs[count++] = entry.getValue();
        return com.aliasi.util.Math.logSumOfExponentials(xs) - logZ;
    }

    static double logZ(ObjectToDoubleMap<int[]> otdMap) {
        double[] xs = new double[otdMap.size()];
        int idx = 0;
        for (double x : otdMap.values())
            xs[idx++] = x;
        return com.aliasi.util.Math.logSumOfExponentials(xs);
    }


    void assertCorrectNBest(ObjectToDoubleMap<int[]> otdMap,
                            Iterator<ScoredTagging<String>> nBest,
                            String[] tags,
                            boolean conditional) {
        double logZ = conditional ? logZ(otdMap) : 0.0;

        ObjectToDoubleMap<String> otdMap2 = new ObjectToDoubleMap<String>();
        int count = 0;
        Set<String> expectedTaggingSet = new TreeSet<String>();
        for (Map.Entry<int[],Double> entry : otdMap.entrySet()) {
            Double val = entry.getValue();
            int[] tagIds = entry.getKey();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tagIds.length; ++i) {
                sb.append(tags[tagIds[i]]);
            }
            String tagRep = sb.toString();
            otdMap2.put(tagRep,val);
            expectedTaggingSet.add(tagRep);
            ++count;
        }
        Set<String> foundTaggingSet = new TreeSet<String>();
        for (count = 0; nBest.hasNext(); ++count) {
            ScoredTagging<String> scoredTagging = nBest.next();
            double val = scoredTagging.score();
            List<String> tagList = scoredTagging.tags();
            StringBuilder sb = new StringBuilder();
            for (String tag : tagList)
                sb.append(tag);
            String tagRep = sb.toString();
            foundTaggingSet.add(tagRep);
            double expectedVal = otdMap2.get(tagRep) - logZ;
            assertEquals(expectedVal,val,0.0001);
        }
        assertEquals(expectedTaggingSet,foundTaggingSet);
    }

    @Test
    public void testAllOutputsSizes() {
        assertEquals(1,allArrays(0,5).size());
        assertEquals(5,allArrays(1,5).size());
        assertEquals(25,allArrays(2,5).size());
        assertEquals(125,allArrays(3,5).size());
    }

    static void assertCorrectAnswer(ChainCrf<String> crf,
                                    List<String> tokenList,
                                    ObjectToDoubleMap<int[]> otdMap,
                                    String[] tags) {
        // complexity is dealing with n-best
        Tagging<String> tagging = crf.tag(tokenList);
        List<String> foundTags = tagging.tags();
        List<int[]> keysList = otdMap.keysOrderedByValueList();
        double score = otdMap.getValue(keysList.get(0));
        for (int[] keys : keysList) {
            double score2 = otdMap.getValue(keys);
            if (score2 < score) {
                fail();
            }
            if (areEqualTags(foundTags,keys,tags)) {
                succeed();
                return;
            }
        }
    }

    static boolean areEqualTags(List<String> foundTags,
                                int[] expectedTags,
                                String[] tags) {
        for (int i = 0; i < expectedTags.length; ++i)
            if (!foundTags.get(i).equals(tags[expectedTags[i]]))
                return false;
        return true;
    }

    static ObjectToDoubleMap<int[]>  bruteForce(int[] tokens, int numTags,
                                                double[][] transitionWeights, double[][] tokenWeights) {
        ObjectToDoubleMap<int[]> outputMap = new ObjectToDoubleMap<int[]>();
        List<int[]> allArrays = allArrays(tokens.length,numTags);
        for (int[] output : allArrays) {
            double score = score(tokens,output,transitionWeights,tokenWeights);
            outputMap.put(output,score);
        }
        return outputMap;
    }

    static double score(int[] tokens, int[] output, double[][] transitionWeights, double[][] tokenWeights) {
        double score = 0.0;
        for (int i = 0; i < tokens.length; ++i)
            score += tokenWeights[output[i]][tokens[i]];
        for (int i = 1; i < tokens.length; ++i)
            score += transitionWeights[output[i]][output[i-1]];
        return score;
    }

    static List<int[]> allArrays(int size, int maxVal) {
        List<int[]> result = new ArrayList<int[]>();
        allArrays(size,maxVal,new int[size],result);
        return result;
    }

    static void allArrays(int size, int maxVal, int[] buf, List<int[]> result) {
        if (size == 0) {
            result.add(buf.clone());
            return;
        }
        for (int i = 0; i < maxVal; ++i) {
            buf[size-1] = i;
            allArrays(size-1,maxVal,buf,result);
        }
    }

    static class TestCorpus extends Corpus<ObjectHandler<Tagging<String>>> {
        static final String[][][] WORDS_TAGSS = new String[][][] {
            { { }, { } },
            { { "." }, { "EOS" } },
            { { "John", "ran", "." }, { "PN", "IV", "EOS" } },
            { { "Mary", "ran", "." }, { "PN", "IV", "EOS" } },
            { { "John", "jumped", "!" }, { "PN", "IV", "EOS" } },
            { { "The", "dog", "jumped", "!" }, { "DET", "N", "IV", "EOS" } },
            { { "The", "dog", "sat", "." }, { "DET", "N", "IV", "EOS" } },
            { { "Mary", "sat", "!" }, { "PN", "IV", "EOS" } },
            { { "Mary", "likes", "John", "." }, { "PN", "TV", "PN", "EOS" } },
            { { "The", "dog", "likes", "Mary", "." }, { "DET", "N", "TV", "PN", "EOS" } },
            { { "John", "likes", "the", "dog", "." }, { "PN", "TV", "DET", "N", "EOS" } },
            { { "The", "dog", "ran", "." }, { "DET", "N", "IV", "EOS", } },
            { { "The", "dog", "ran", "." }, { "DET", "N", "IV", "EOS", } }
        };
        public void visitTrain(ObjectHandler<Tagging<String>> handler) {
            for (String[][] wordsTags : WORDS_TAGSS) {
                String[] words = wordsTags[0];
                String[] tags = wordsTags[1];
                Tagging<String> tagging
                    = new Tagging<String>(Arrays.asList(words),
                                          Arrays.asList(tags));
                handler.handle(tagging);
            }
        }
        public void visitTest(ObjectHandler<Tagging<String>> handler) {
        }
    }


    @Test
    public void testEstimate() throws Exception {
        Corpus<ObjectHandler<Tagging<String>>> corpus = new TestCorpus();
        int minCount = 1;
        boolean addIntercept = true;
        boolean cacheFeatureVectors = true;
        boolean allowUnseenTransitions = true;
        RegressionPrior prior = RegressionPrior.gaussian(10.0,true);
        int priorBlockSize = 3;
        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.exponential(0.02,0.995);
        double minImprovement = 0.00001;
        int minEpochs = 2;
        int maxEpochs = 2000;
        Reporter reporter = null; // Reporters.stdOut().setLevel(LogLevel.DEBUG);
        ChainCrf<String> crf
            = ChainCrf.estimate(corpus,
                                FEATURE_EXTRACTOR,
                                addIntercept,
                                minCount,
                                cacheFeatureVectors,
                                allowUnseenTransitions,
                                prior,
                                priorBlockSize,
                                annealingSchedule,
                                minImprovement,
                                minEpochs,
                                maxEpochs,
                                reporter);

        assertTagging(Arrays.asList("John","ran","."),
                      Arrays.asList("PN","IV","EOS"),
                      crf);

        assertTagging(Arrays.asList("Mary","ran","."),
                      Arrays.asList("PN","IV","EOS"),
                      crf);
        assertTagging(Arrays.asList("The","dog","ran","."),
                      Arrays.asList("DET","N","IV","EOS"),
                      crf);
        assertTagging(Arrays.asList("The","dog","ran","!"),
                      Arrays.asList("DET","N","IV","EOS"),
                      crf);
        assertTagging(Arrays.asList("The","dog","sat","!"),
                      Arrays.asList("DET","N","IV","EOS"),
                      crf);
        assertTagging(Arrays.asList("The","dog","sat","."),
                      Arrays.asList("DET","N","IV","EOS"),
                      crf);
        assertTagging(Arrays.asList("John","likes","Mary","."),
                      Arrays.asList("PN","TV","PN","EOS"),
                      crf);
        assertTagging(Arrays.asList("Mary","likes","John","."),
                      Arrays.asList("PN","TV","PN","EOS"),
                      crf);

        // don't barf on unknown words
        assertNotNull(crf.tag(Arrays.asList("Fred","likes","John",".")));
        assertNotNull(crf.tag(Arrays.asList(";",".","likes","likes")));
    }

    static <E> void assertTagging(List<E> tokens,
                                  List<String> tagsExpected,
                                  ChainCrf<E> crf) {
        Tagging<E> tagging = crf.tag(tokens);
        // System.out.println(tagging);
        List<String> tagsFound = tagging.tags();
        assertEquals(tagsExpected,tagsFound);
    }


}

