import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

public class TinyEntityCorpus extends Corpus<ObjectHandler<Chunking>> {

    public void visitTrain(ObjectHandler<Chunking> handler) {
        for (Chunking chunking : CHUNKINGS)
            handler.handle(chunking);
    }

    public void visitTest(ObjectHandler<Chunking> handler) {
        /* no op */
    }

    static final Chunking[] CHUNKINGS
        = new Chunking[] {
        chunking(""),
        chunking("The"),
        chunking("John ran.",
                 chunk(0,4,"PER")),
        chunking("Mary ran.",
                 chunk(0,4,"PER")),
        chunking("The kid ran."),
        chunking("John likes Mary.",
                 chunk(0,4,"PER"),
                 chunk(11,15,"PER")),
        chunking("Tim lives in Washington",
                 chunk(0,3,"PER"),
                 chunk(13,23,"LOC")),
        chunking("Mary Smith is in New York City",
                 chunk(0,10,"PER"),                     
                 chunk(17,30,"LOC")),
        chunking("New York City is fun",
                 chunk(0,13,"LOC")),
        chunking("Chicago is not like Washington",
                 chunk(0,7,"LOC"),
                 chunk(20,30,"LOC"))
    };

    static Chunking chunking(String s, Chunk... chunks) {
        ChunkingImpl chunking = new ChunkingImpl(s);
        for (Chunk chunk : chunks) 
            chunking.add(chunk);
        return chunking;
    }
    
    static Chunk chunk(int start, int end, String type) {
        return ChunkFactory.createChunk(start,end,type);
    }

}