package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.TagChunkCodec;

import com.aliasi.crf.ForwardBackwardTagLattice;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tag.StringTagging;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Scored;

import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import static com.aliasi.test.unit.Asserts.succeed;

public class BioTagChunkCodecTest {

    @Test
    public void testNBestChunks() {
        TagChunkCodec codec
            = new BioTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   false);
        TagLattice<String> lattice
            = new ForwardBackwardTagLattice<String>(Collections.<String>emptyList(),
                                                    Collections.<String>emptyList(),
                                                    new double[0][0],
                                                    new double[0][0],
                                                    new double[0][0][0],
                                                    0.0);
        Iterator<Chunk> it = codec.nBestChunks(lattice,new int[0],new int[0],100);
        assertFalse(it.hasNext());

        lattice
            = new ForwardBackwardTagLattice<String>(Arrays.asList("John"),
                                                    Arrays.asList("O","B_PER","I_PER"),
                                                    new double[][] { { -1, -3, -100} },
                                                    new double[][] { { 0, 0, 0 } },
                                                    new double[0][0][0],
                                                    -1.5);
        it = codec.nBestChunks(lattice,new int[] { 0 }, new int[] { 4 },100);
        assertIterator(it,
                       ChunkFactory.createChunk(0,4,"PER",-1.5));


        lattice
            = new ForwardBackwardTagLattice<String>(Arrays.asList("John"),
                                                    Arrays.asList("O","B_PER","I_PER","B_LOC","I_LOC"),
                                                    new double[][] { { -1, -3, -100, -5, -200} },
                                                    new double[][] { { 0, 0, 0, 0, 0 } },
                                                    new double[0][0][0],
                                                    -2.5);
        it = codec.nBestChunks(lattice,new int[] { 0 }, new int[] { 4 },100);
        assertIterator(it,
                       ChunkFactory.createChunk(0,4,"PER",-0.5),
                       ChunkFactory.createChunk(0,4,"LOC",-2.5));

        it = codec.nBestChunks(lattice,new int[] { 0 }, new int[] { 4 },1);
        assertIterator(it,
                       ChunkFactory.createChunk(0,4,"PER",-0.5));

        Random random = new Random(42);
        assertRandomLattice(random,"John",new HashSet<String>(Arrays.asList("PER")),100,codec);
        assertRandomLattice(random,"John",new HashSet<String>(Arrays.asList("PER","LOC")),100,codec);
        assertRandomLattice(random,"John ran",new HashSet<String>(Arrays.asList("PER","LOC")),100,codec);
        assertRandomLattice(random,"Mary jumped",new HashSet<String>(Arrays.asList("PER","LOC")),100,codec);
        assertRandomLattice(random,"Mary likes John",new HashSet<String>(Arrays.asList("PER","LOC")),100,codec);

    }

    void assertRandomLattice(Random random, String cs, Set<String> chunkTypes, int max, TagChunkCodec codec) {
        List<Integer> tokenStartList = new ArrayList<Integer>();
        List<Integer> tokenEndList = new ArrayList<Integer>();
        List<String> tokenList = new ArrayList<String>();
        Tokenizer tokenizer = IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(cs.toCharArray(),0,cs.length());
        for (String token : tokenizer) {
            tokenList.add(token);
            tokenStartList.add(tokenizer.lastTokenStartPosition());
            tokenEndList.add(tokenizer.lastTokenEndPosition());
        }
        // System.out.println("tokens=" + tokenList + " starts=" + tokenStartList + " ends=" + tokenEndList);
        int[] starts = toInts(tokenStartList);
        int[] ends = toInts(tokenEndList);

        TagLattice lattice = randomLattice(chunkTypes,tokenList,random);
        // System.out.println("\nLATTICE\n" + lattice + "\n");
        assertNBestChunks(lattice,starts,ends,max,cs,codec);
    }

    static int[] toInts(List<Integer> xs) {
        int[] ys = new int[xs.size()];
        for (int i = 0; i < ys.length; ++i)
            ys[i] = xs.get(i);
        return ys;
    }

    TagLattice randomLattice(Set<String> chunkTypes,
                             List<String> tokens,
                             Random random) {
        List<String> tags = new ArrayList<String>(1 + 2 * chunkTypes.size());
        tags.add(BioTagChunkCodec.OUT_TAG);
        for (String type : chunkTypes) {
            tags.add(BioTagChunkCodec.BEGIN_TAG_PREFIX + type);
            tags.add(BioTagChunkCodec.IN_TAG_PREFIX + type);
        }
        double[][] logForwards = new double[tokens.size()][tags.size()];
        logForwards[0] = randomArray(random,tags.size());

        // remove illegal starts
        for (int i = 2; i < tags.size(); i += 2) { // in tags
            logForwards[0][i] = Double.NEGATIVE_INFINITY;
        }

        double[][][] logTransitions = new double[tokens.size()-1][tags.size()][];
        for (int i = 0; i < logTransitions.length; ++i)
            for (int j = 0; j < logTransitions[i].length; ++j)
                logTransitions[i][j] = randomArray(random,tags.size());

        // remove illegal transitions
        for (int i = 0; i < logTransitions.length; ++i) {
            for (int kTo = 2; kTo < logTransitions[i].length; kTo += 2) { // in tags
                for (int kFrom = 0; kFrom < logTransitions[i].length; ++kFrom) {
                    if ((kFrom != (kTo - 1)) && (kFrom != kTo)) {
                        logTransitions[i][kFrom][kTo] = Double.NEGATIVE_INFINITY;
                    }
                }
            }
        }


        double[] basis = new double[tags.size()];
        for (int i = 1; i < tokens.size(); ++i) {
            for (int j = 0; j < tags.size(); ++j) {
                for (int k = 0; k < tags.size(); ++k) {
                    basis[k] = logForwards[i-1][k] + logTransitions[i-1][k][j];
                }
                logForwards[i][j] = com.aliasi.util.Math.logSumOfExponentials(basis);
            }
        }
        double logZ = com.aliasi.util.Math.logSumOfExponentials(logForwards[logForwards.length-1]);
        double[][] logBackwards = new double[tokens.size()][tags.size()];
        for (int i = tokens.size()-1; --i >= 0; ) {
            for (int j = 0; j < tags.size(); ++j) {
                for (int k = 0; k < tags.size(); ++k) {
                    basis[k] = logBackwards[i+1][k] + logTransitions[i][j][k];
                }
                logBackwards[i][j] = com.aliasi.util.Math.logSumOfExponentials(basis);
            }
        }
        return new ForwardBackwardTagLattice(tokens,tags,logForwards,logBackwards,logTransitions,logZ);
    }

    static double[] randomArray(Random random, int length) {
        double[] xs = new double[length];
        for (int n = 0; n < xs.length; ++n)
            xs[n] = -10.0 * random.nextDouble();
        return xs;
    }






    void assertNBestChunks(TagLattice lattice, int[] starts, int[] ends, int max, String cs, TagChunkCodec codec) {
        List<Chunk> chunks = bruteForce(lattice, starts,ends,cs,codec);
        Iterator<Chunk> it = codec.nBestChunks(lattice,starts,ends,max);
        for (int i = 0; i < max && it.hasNext(); ++i) {
            Chunk c1 = chunks.get(i);
            Chunk c2 = it.next();
            // System.out.println(c1 + " =?= " + c2);
            assertEqualScoredChunks(c1,c2);
        }
        assertFalse(it.hasNext());
    }

    void assertEqualScoredChunks(Chunk c1, Chunk c2) {
        assertEquals(c1.start(),c2.start());
        assertEquals(c1.end(),c2.end());
        assertEquals(c1.type(),c2.type());
        assertEquals(c1.score(),c2.score(),0.1);
    }

    List<Chunk> bruteForce(TagLattice<String> lattice,
                           int[] tokenStarts,
                           int[] tokenEnds,
                           String charSequence,
                           TagChunkCodec codec) {
        Map<Chunk,List<Double>> chunkToScores = new HashMap<Chunk,List<Double>>();
        if (lattice.numTokens() == 0) return new ArrayList<Chunk>(0);
        SymbolTable tagSymbolTable = lattice.tagSymbolTable();
        List<String[]> tagSeqs = new ArrayList<String[]>();
        add(tagSeqs,0,lattice,new String[lattice.numTokens()]);
        List<Double> logProbs = new ArrayList<Double>();
        for (String[] tags : tagSeqs) {
            StringTagging tagging = new StringTagging(lattice.tokenList(),
                                                      Arrays.asList(tags),
                                                      charSequence,
                                                      tokenStarts,tokenEnds);
            if (codec.legalTags(tags)) {
                Chunking chunking = codec.toChunking(tagging);
                double logScore = lattice.logForward(0,tagSymbolTable.symbolToID(tags[0]))
                    - lattice.logZ();
                for (int n = 1; n < tags.length; ++n)
                    logScore += lattice.logTransition(n-1,
                                                      tagSymbolTable.symbolToID(tags[n-1]),
                                                      tagSymbolTable.symbolToID(tags[n]));
                logScore += lattice.logBackward(lattice.numTokens()-1,
                                                tagSymbolTable.symbolToID(tags[lattice.numTokens()-1]));
                // System.out.println(logScore + " " + Arrays.asList(tags));
                logProbs.add(logScore);
                for (Chunk chunk : chunking.chunkSet()) {
                    List<Double> scores = chunkToScores.get(chunk);
                    if (scores == null) {
                        scores = new ArrayList<Double>();
                        chunkToScores.put(chunk,scores);
                    }
                    scores.add(logScore);
                }
            }
        }
        double[] logScoreArray = new double[logProbs.size()];
        for (int i = 0; i < logScoreArray.length; ++i)
            logScoreArray[i] = logProbs.get(i);
        double totalProb = com.aliasi.util.Math.logSumOfExponentials(logScoreArray);
        // System.out.println("\nTOTAL=" + totalProb);

        // System.out.println("\nCHUNKS");
        List<Chunk> chunks = new ArrayList<Chunk>();
        for (Map.Entry<Chunk,List<Double>> entry : chunkToScores.entrySet()) {
            Chunk c = entry.getKey();
            List<Double> vals = entry.getValue();
            double[] xs = new double[vals.size()];
            for (int i = 0; i < xs.length; ++i)
                xs[i] = vals.get(i);
            double logSumExp = com.aliasi.util.Math.logSumOfExponentials(xs);
            chunks.add(ChunkFactory.createChunk(c.start(),
                                                c.end(),
                                                c.type(),
                                                logSumExp));
        }
        Collections.sort(chunks,ScoredObject.reverseComparator());
        return chunks;
    }

    void add(List<String[]> tagSeqs,
             int pos,
             TagLattice<String> lattice,
             String[] tags) {
        if (pos == lattice.numTokens()) {
            tagSeqs.add(tags.clone());
            return;
        }
        for (int k = 0; k < lattice.numTags(); ++k) {
            tags[pos] = lattice.tag(k);
            add(tagSeqs,pos+1,lattice,tags);
        }
    }


    void assertIterator(Iterator<Chunk> it, Chunk... chunks) {
        for (Chunk chunk : chunks) {
            assertTrue(it.hasNext());
            Chunk next = it.next();
            assertEquals(chunk.start(),next.start());
            assertEquals(chunk.end(),next.end());
            assertEquals(chunk.type(),next.type());
            assertEquals(chunk.score(),next.score(),0.1);
        }
        assertFalse(it.hasNext());
    }


    @Test
    public void testLegalTagSubSequence() {
        TagChunkCodec codec
            = new BioTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   false);

        assertTrue(codec.legalTagSubSequence("O"));
        assertTrue(codec.legalTagSubSequence("B_PER"));
        assertTrue(codec.legalTagSubSequence("I_PER"));
        assertFalse(codec.legalTagSubSequence("F"));
        assertFalse(codec.legalTagSubSequence("M_PER"));

        assertTrue(codec.legalTagSubSequence("O","B_PER"));
        assertTrue(codec.legalTagSubSequence("I_PER","O"));
        assertTrue(codec.legalTagSubSequence("B_PER","B_PER"));
        assertTrue(codec.legalTagSubSequence("B_PER","I_PER"));
        assertTrue(codec.legalTagSubSequence("B_PER","I_PER", "I_PER"));
        assertTrue(codec.legalTagSubSequence("B_PER","I_PER", "I_PER", "O"));
        assertTrue(codec.legalTagSubSequence("O","B_PER","I_PER", "I_PER", "O"));

        assertFalse(codec.legalTagSubSequence("O","I_PER"));
        assertFalse(codec.legalTagSubSequence("B_LOC","I_PER"));
    }

    @Test
    public void testLegalTags() {
        TagChunkCodec codec
            = new BioTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   false);

        assertTrue(codec.legalTags("O"));
        assertTrue(codec.legalTags("B_PER"));
        assertFalse(codec.legalTags("F"));
        assertFalse(codec.legalTags("M_PER"));

        assertTrue(codec.legalTags("O","B_PER"));
        assertTrue(codec.legalTags("B_PER","B_PER"));
        assertTrue(codec.legalTags("B_PER","I_PER"));
        assertTrue(codec.legalTags("B_PER","I_PER", "I_PER"));
        assertTrue(codec.legalTags("B_PER","I_PER", "I_PER", "O"));
        assertTrue(codec.legalTags("O","B_PER","I_PER", "I_PER", "O"));

        assertFalse(codec.legalTags("I_PER","O"));
        assertFalse(codec.legalTags("I_PER"));
        assertFalse(codec.legalTags("O","I_PER"));
        assertFalse(codec.legalTags("B_LOC","I_PER"));
    }



    @Test
    public void testBioCodecTagSet() {
        TagChunkCodec codec
            = new BioTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   false);
        Set<String> chunkTypes
            = new HashSet<String>(Arrays.asList("PER","LOC"));
        Set<String> expectedTags =
            new HashSet<String>(Arrays.asList("O","B_PER","I_PER","B_LOC","I_LOC"));
        assertEquals(expectedTags,codec.tagSet(chunkTypes));
    }


    @Test
    public void testEncodable()
        throws IOException, ClassNotFoundException {

        TagChunkCodec codec
            = new BioTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   true); // don't need to check errors to test

        ChunkingImpl chunkingOk
            = new ChunkingImpl("John Jones Mary and Mr. J. J. Jones ran to Washington.");
        Chunk chunk1 = ChunkFactory.createChunk(0,10,"PER");
        Chunk chunk2 = ChunkFactory.createChunk(11,15,"PER");
        Chunk chunk3 = ChunkFactory.createChunk(24,35,"PER");
        Chunk chunk4 = ChunkFactory.createChunk(43,53,"LOC");
        chunkingOk.add(chunk2);
        chunkingOk.add(chunk4);
        chunkingOk.add(chunk3);
        chunkingOk.add(chunk1);
        assertEncodable(codec,chunkingOk);

        ChunkingImpl chunkingBad
            = new ChunkingImpl("John ran");
        //                      012345678
        Chunk chunk2_1 = ChunkFactory.createChunk(0,4,"PER");
        Chunk chunk2_2 = ChunkFactory.createChunk(0,8,"LOC");
        chunkingBad.add(chunk2_1);
        chunkingBad.add(chunk2_2);
        assertNotEncodable(codec,chunkingBad);

        ChunkingImpl chunkingBad3
            = new ChunkingImpl("John ran");
        Chunk chunk3_1 = ChunkFactory.createChunk(0,5,"PER");
        chunkingBad3.add(chunk3_1);
        assertNotEncodable(codec,chunkingBad3);

        ChunkingImpl chunkingBad4
            = new ChunkingImpl("John ran");
        Chunk chunk4_1 = ChunkFactory.createChunk(1,4,"PER");
        chunkingBad4.add(chunk4_1);
        assertNotEncodable(codec,chunkingBad4);
        assertNotEncodable(codec,chunkingBad4);

        ChunkingImpl chunkingBad5
            = new ChunkingImpl("John ran");
        Chunk chunk5_1 = ChunkFactory.createChunk(5,5,"LOC");
        chunkingBad5.add(chunk5_1);
        assertNotEncodable(codec,chunkingBad5);

        ChunkingImpl chunkingOk2
            = new ChunkingImpl("John ran");
        assertTrue(codec.isEncodable(chunkingOk2));
        Chunk chunk6_1 = ChunkFactory.createChunk(0,8,"LOC");
        chunkingOk2.add(chunk6_1);
        assertEncodable(codec,chunkingOk2);

        ChunkingImpl chunkingOk3
            = new ChunkingImpl("Mr. John Jones ran to Washington.");
        //                      0123456789012345678901234567890123456789
        //                      0         1         2         3
        Chunk jj = ChunkFactory.createChunk(4,14,"PER");
        Chunk w = ChunkFactory.createChunk(22,32,"LOC");
        chunkingOk3.add(jj);
        chunkingOk3.add(w);
        assertEncodable(codec,chunkingOk3);
    }

    @Test
    public void testDecodable()
        throws IOException, ClassNotFoundException {

        TagChunkCodec codec
            = new BioTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   true); // don't need to check errors to test

        StringTagging taggingOk
            = new StringTagging(Arrays.asList("John","ran","to","Washington","DC"),
                                Arrays.asList("B_PER","O","O","B_LOC","I_LOC"),
                                "John ran to Washington DC",
                                //1234567890123456789012345
                                new int[] { 0, 5, 9, 12, 23 },
                                new int[] { 4, 8, 11, 22, 25 });
        assertDecodable(codec,taggingOk);

        StringTagging taggingBad
            = new StringTagging(Arrays.asList("John","ny","ran","."),
                                Arrays.asList("B_PER","I_PER","O","O"),
                                "Johnny ran.",
                               //01234567890
                                new int[] { 0, 4, 7, 10 },
                                new int[] { 4, 6, 10, 11 });
        assertNotDecodable(codec,taggingBad);


    }


    void assertEncodable(TagChunkCodec codec,
                         Chunking chunking)
        throws IOException, ClassNotFoundException {

        assertEncodable2(codec,chunking);

        @SuppressWarnings("unchecked")
        TagChunkCodec codec2
            = (TagChunkCodec)
            AbstractExternalizable
            .serializeDeserialize((Serializable) codec);
        assertEncodable2(codec2,chunking);
    }

    void assertEncodable2(TagChunkCodec codec,
                          Chunking chunking) {
        assertTrue(codec.isEncodable(chunking));

        StringTagging tagging = codec.toStringTagging(chunking);
        assertTrue(codec.isDecodable(tagging));

        Chunking chunking2 = codec.toChunking(tagging);
        assertEquals(chunking,chunking2);

        Tagging tagging2 = codec.toStringTagging(chunking2);
        assertEquals(tagging,tagging2);
    }

    void assertNotEncodable(TagChunkCodec codec,
                            Chunking chunking) {
        assertFalse(codec.isEncodable(chunking));

        try {
            codec.toTagging(chunking);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    void assertDecodable(TagChunkCodec codec,
                         StringTagging tagging)
        throws IOException, ClassNotFoundException {

        assertDecodable2(codec,tagging);

        @SuppressWarnings("unchecked")
        TagChunkCodec codec2
            = (TagChunkCodec)
            AbstractExternalizable
            .serializeDeserialize((Serializable) codec);
        assertDecodable2(codec2,tagging);
    }

    void assertDecodable2(TagChunkCodec codec,
                          StringTagging tagging) {
        assertTrue(codec.isDecodable(tagging));

        Chunking chunking = codec.toChunking(tagging);
        assertTrue(codec.isEncodable(chunking));

        StringTagging tagging2 = codec.toStringTagging(chunking);
        assertEquals(tagging,tagging2);

        Chunking chunking2 = codec.toChunking(tagging2);
        assertEquals(chunking,chunking2);
    }

    void assertNotDecodable(TagChunkCodec codec,
                            StringTagging tagging) {
        assertFalse(codec.isDecodable(tagging));
        try {
            codec.toChunking(tagging);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }


}

