import com.aliasi.io.FileLineReader;

import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.TrieIntSeqCounter;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Pair;
import com.aliasi.util.Streams;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.zip.GZIPOutputStream;

// always UTF-8?
public class TokenNGramFiles {


    public static void writeNGrams(TokenizedLM lm,
                                   File file,
                                   int minNGramOrder,
                                   int maxNGramOrder,
                                   int minCount,
                                   String encoding)
        throws IOException {

        maxNGramOrder = Math.max(maxNGramOrder,lm.nGramOrder());
        SymbolTable symbolTable = lm.symbolTable();
        TrieIntSeqCounter seqCounter = lm.sequenceCounter();

        OutputStream fileOut = null;
        OutputStream gzipOut = null;
        Writer writer = null;
        Writer bufWriter = null;
        try {
            fileOut = new FileOutputStream(file);
            gzipOut = new GZIPOutputStream(fileOut);
            writer = new OutputStreamWriter(gzipOut,encoding);
            bufWriter = new BufferedWriter(writer);
            LinkedList<SearchNode> queue = new LinkedList<SearchNode>();
            SearchNode root = new SearchNode(); // root
            queue.offer(root);
            while (!queue.isEmpty()) {              // depth first for alpha order
                SearchNode node = queue.removeFirst();
                node.write(bufWriter,seqCounter);
                int[] nextNodes = node.nextNodes(seqCounter);
                String[] nextStrings = new String[nextNodes.length];
                for (int i = 0; i < nextStrings.length; ++i)
                    nextStrings[i] = (nextNodes[i] == -2)
                        ? "<S>"
                        : symbolTable.idToSymbol(nextNodes[i]);
                Arrays.sort(nextStrings);
                for (int i = nextNodes.length; --i >= 0; )
                    queue.addFirst(new SearchNode(node,
                                                  "<S>".equals(nextStrings[i])
                                                  ? -2
                                                  : symbolTable.symbolToID(nextStrings[i]),
                                                  nextStrings[i]));
            }
        } finally {
            Streams.closeQuietly(bufWriter);
            Streams.closeQuietly(writer);
            Streams.closeQuietly(gzipOut);
            Streams.closeQuietly(fileOut);
        }
    }


    static class SearchNode {
        final String mTokens;
        final int[] mIds;
        SearchNode() {
            mIds = new int[0];
            mTokens = "";
        }
        SearchNode(SearchNode node, int nextId, String nextSymbol) {
            int[] nodeIds = node.mIds;
            int[] ids = new int[node.mIds.length+1];
            for (int i = 0; i < nodeIds.length; ++i)
                ids[i] = nodeIds[i];
            ids[ids.length-1] = nextId;
            mIds = ids;
            mTokens = node.mTokens
                + ( node.mTokens.length() > 0
                    ? (" " + nextSymbol)
                    : nextSymbol );
        }
        int[] nextNodes(TrieIntSeqCounter seqCounter) {
            return seqCounter.integersFollowing(mIds,0,mIds.length);
        }
        int count(TrieIntSeqCounter seqCounter) {
            return seqCounter.count(mIds,0,mIds.length);
        }
        void write(Writer writer, TrieIntSeqCounter counter)
            throws IOException {

            writer.write(mTokens);
            writer.write(' ');
            writer.write(Integer.toString(count(counter)));
            writer.write('\n');
        }
    }


    public static void addNGrams(File file,
                                 String encoding,
                                 TokenizedLM lm,
                                 int minCount)

        throws IOException {

        int nGramOrder = lm.nGramOrder();
        SymbolTable symbolTable = lm.symbolTable();
        TrieIntSeqCounter counter = lm.sequenceCounter();
        int[] ids = new int[nGramOrder];
        String[] tokens = new String[nGramOrder];
        boolean gzipped = true;
        FileLineReader lines = new FileLineReader(file,encoding,gzipped);
        for (String line : lines) {
            if (line.length() == 0) continue;
            int start;
            int pos = 0;
            if (line.charAt(0) == ' ') {
                start = 1;
                pos = 0;
            } else {
                start = 0;
                int next = -1;
                for (pos = 0; (next = line.indexOf(' ',start)) != -1; ++pos) {
                    tokens[pos] = line.substring(start,next);
                    ids[pos] = tokens[pos].equals("<S>")
                        ? -2
                        : symbolTable.getOrAddSymbol(tokens[pos]);
                    start = next+1;
                }
            }
            int count = Integer.parseInt(line.substring(start));
            if (count < minCount) continue;
            int nGramSize = pos;
            if (nGramSize > nGramOrder) continue;
            // System.out.println(count + " |" + line.substring(0,start-1) + "|");
            counter.incrementSequence(ids,0,nGramSize,count);
        }
        Streams.closeQuietly(lines);
    }

    public static void merge(List<File> filesIn,
                             File fileOut,
                             String encoding,
                             int minCount,
                             boolean deleteInputFiles)
        throws IOException {

        OutputStream out = null;
        OutputStream gzipOut = null;
        OutputStreamWriter writer = null;
        BufferedWriter bufWriter = null;

        BoundedPriorityQueue<NGramStream> queue
            = new BoundedPriorityQueue<NGramStream>(NGRAM_STREAM_COMPARATOR,
                                                    filesIn.size());

        List<NGramStream> streams = new ArrayList<NGramStream>();
        try {
            out = new FileOutputStream(fileOut);
            gzipOut = new GZIPOutputStream(out);
            writer = new OutputStreamWriter(gzipOut,encoding);
            bufWriter = new BufferedWriter(writer);
            for (File file : filesIn) {
                NGramStream stream = new NGramStream(file,encoding);
                streams.add(stream);
                queue.offer(stream);
            }
            String tokens = null;
            long count = 0;
            while (!queue.isEmpty()) {
                NGramStream stream = queue.poll();
                String nextTokens = stream.nextTokens();
                if (nextTokens == null) continue;
                long nextCount = stream.nextCount();
                stream.pop();
                queue.offer(stream);
                if (tokens == null) { // only first loop
                    tokens = nextTokens;
                    count = nextCount;
                } else if (tokens.equals(nextTokens)) {
                    count += nextCount;
                } else {
                    if (count >= minCount) {
                        bufWriter.write(tokens);
                        bufWriter.write(' ');
                        bufWriter.write(Long.toString(count));
                        bufWriter.write('\n');
                    }
                    tokens = nextTokens;
                    count = nextCount;
                }
            }
            if (tokens != null && count >= minCount) {
                // cut and paste from above
                bufWriter.write(tokens);
                bufWriter.write(' ');
                bufWriter.write(Long.toString(count));
                bufWriter.write('\n');
            }
        } finally {
            for (NGramStream stream : streams)
                stream.close();
            Streams.closeQuietly(bufWriter);
            Streams.closeQuietly(writer);
            Streams.closeQuietly(gzipOut);
            Streams.closeQuietly(out);
            if (deleteInputFiles) {
                for (File file : filesIn) {
                    if (!file.delete()) {
                        System.out.println("could not delete file=" + file);
                    }
                }
            }
        }
    }




    static class NGramStream implements Comparable<NGramStream> {
        private String mNextTokens;
        private long mNextCount;

        private final FileLineReader mReader;
        private final Iterator<String> mIterator;
        private final String mFileName;
        public NGramStream(File file, String encoding) throws IOException {
            mFileName = file.toString();
            boolean gzipped = true;
            mReader = new FileLineReader(file,encoding,gzipped);
            mIterator = mReader.iterator();
            buffer();
        }
        public int compareTo(NGramStream that) {
            return NGRAM_STREAM_COMPARATOR.compare(this,that);
        }
        String nextTokens() throws IOException {
            return mNextTokens;
        }
        long nextCount() throws IOException {
            return mNextCount;
        }
        void pop() throws IOException {
            mNextTokens = null;
            buffer();
        }
        void buffer() throws IOException {
            while (mNextTokens == null && mIterator.hasNext()) {
                String line = mIterator.next();
                int pos = line.lastIndexOf(' ');
                if (pos < 0) continue;
                mNextTokens = line.substring(0,pos);
                mNextCount = Long.parseLong(line.substring(pos+1));
            }
        }
        void close()  {
            Streams.closeQuietly(mReader);
        }
        public String toString() {
            return mNextTokens + "[" + mNextCount + "]";
        }
    }

    static Comparator<NGramStream> NGRAM_STREAM_COMPARATOR
        = new Comparator<NGramStream>() {
        public int compare(NGramStream s1, NGramStream s2) {
            String ts1 = s1.mNextTokens;
            String ts2 = s2.mNextTokens;
            if (ts1 == null)
                return (ts2 != null) ? -1 : 0;
            if (ts2 == null)
                return 1;
            int c = ts1.compareTo(ts2);
            if (c != 0)
                return -c;
            return -s1.mFileName.compareTo(s2.mFileName);
        }
    };

}