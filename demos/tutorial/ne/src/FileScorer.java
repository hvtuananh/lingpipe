import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingEvaluation;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;

public class FileScorer {

    private final Parser<ObjectHandler<Chunking>> mParser;

    private final ChunkingEvaluation mEvaluation
        = new ChunkingEvaluation();

    public FileScorer(Parser<ObjectHandler<Chunking>> parser) {
        mParser = parser;
    }

    public ChunkingEvaluation evaluation() {
        return mEvaluation;
    }

    public void score(File refFile, File responseFile) throws IOException {
        ChunkingCollector refCollector = new ChunkingCollector();
        mParser.setHandler(refCollector);
        mParser.parse(refFile);
        List<Chunking> refChunkings = refCollector.mChunkingList;

        ChunkingCollector responseCollector = new ChunkingCollector();
        mParser.setHandler(responseCollector);
        mParser.parse(responseFile);
        List<Chunking> responseChunkings = responseCollector.mChunkingList;

        if (refChunkings.size() != responseChunkings.size())
            throw new IllegalArgumentException("chunkings not same size");

        for (int i = 0; i < refChunkings.size(); ++i)
            mEvaluation.addCase(refChunkings.get(i),
                                responseChunkings.get(i));
    }


    private static class ChunkingCollector 
        implements ObjectHandler<Chunking> {

        private final List<Chunking> mChunkingList = new ArrayList<Chunking>();
        public void handle(Chunking chunking) {
            mChunkingList.add(chunking);
        }
    }


    public static void main(String[] args) throws IOException {
        File refFile = new File(args[0]);
        File responseFile = new File(args[1]);

        @SuppressWarnings("deprecation")
        Parser<ObjectHandler<Chunking>> parser 
            = new com.aliasi.corpus.parsers.Muc6ChunkParser();
        FileScorer scorer = new FileScorer(parser);
        scorer.score(refFile,responseFile);

        System.out.println(scorer.evaluation().toString());
    }



}