import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

import com.aliasi.crf.ChainCrf;
import com.aliasi.crf.ChainCrfChunker;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SimpleEntityChunk {

    public static void main(String[] args) 
        throws ClassNotFoundException, IOException {

        File modelFile = new File(args[0]);
        @SuppressWarnings("unchecked")
        ChainCrfChunker crfChunker
            = (ChainCrfChunker) 
            AbstractExternalizable.readObject(modelFile);

        for (int i = 1; i < args.length; ++i) {
            String arg = args[i];
            char[] cs = arg.toCharArray();

            System.out.println("\nFIRST BEST");
            Chunking chunking = crfChunker.chunk(arg);
            System.out.println(chunking);

            int maxNBest = 10;
            System.out.println("\n" + maxNBest + " BEST CONDITIONAL");
            System.out.println("Rank log p(tags|tokens)  Tagging");
            Iterator<ScoredObject<Chunking>> it
                = crfChunker.nBestConditional(cs,0,cs.length,maxNBest);
            for (int rank = 0; rank < maxNBest && it.hasNext(); ++rank) {
                ScoredObject<Chunking> scoredChunking = it.next();
                System.out.println(rank + "    " + scoredChunking.score() + " " + scoredChunking.getObject());
            }

            System.out.println("\nMARGINAL CHUNK PROBABILITIES");
            System.out.println("Rank Chunk Phrase");
            int maxNBestChunks = 10;
            Iterator<Chunk> nBestChunkIt = crfChunker.nBestChunks(cs,0,cs.length,maxNBestChunks);
            for (int n = 0; n < maxNBestChunks && nBestChunkIt.hasNext(); ++n) {
                Chunk chunk = nBestChunkIt.next();
                System.out.println(n + " " + chunk + " " + arg.substring(chunk.start(),chunk.end()));
            }
        }
    }

}