/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.spell;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.Serializable;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/**
 * An {@code AutoCompleter} maintains a dictionary of phrases with
 * counts and provides suggested completions based on prefix matching
 * by weighted edit distance and phrase likelihood.
 *
 * <p>The maximum number of results cannot be changed dynamically,
 * because it is used during construction to set up all of the
 * compiled trie structures used for decoding.  To change the
 * maximum number of results, simply construct a new auto completer
 * using the same phrase counts as edit distance as this completer;
 * these may be retrieved using the methods
 * {@link #phraseCountMap()} and {@link
 * #editDistance()}.
 *
 * <h3>Scoring</h3>
 *
 * <p>For a given input, the {@link #complete(String)} method returns
 * a sorted set of scored objects, containing the most likely phrase
 * completions and their score, up to the maximum number of results
 * specified during construction.
 *
 * <p>Scores for the phrases are defined by the log (base 2) of their
 * probability estimates:
 *
 * <blockquote><pre>
 * score(phrase) = log<sub>2</sub> p'(phrase)</pre></blockquote>
 *
 * where probabilities are estimated using maximum likelihood:
 *
 * <blockquote><pre>
 * p'(phrase) = count(phrase) / <big><big>&Sigma;</big></big><sub><sub>phrase'</sub></sub> count(phrase')</pre></blockquote>
 *
 * <p>Additive smoothing may be easily carried out on the inputs, so it
 * is not carried out by this class.
 *
 * <p>The score for a prefix matching a phrase is given by:
 *
 * <blockquote><pre>
 * score(prefix,phrase)
 *   = MAX<sub>phrase.startsWith(prefix')</sub> editDistance.distance(prefix,prefix') + log<sub><sub>2</sub></sub> p'(phrase)</pre></blockquote>
 *
 * In words, the score for a prefix matching a phrase is the sum of
 * log probability of the phrase plus the edit distance between the
 * prefix and the best matching prefix of the phrase.  The edit
 * distances should thus be scaled as log probabilities in order to
 * combine with the phrase probabilities properly.  See the class
 * documentation for {@link TrainSpellChecker} for general advice on
 * combining the tuning of edit distance with that of phrase
 * probabilities.
 *
 * <h3>Thread Safety</h3>
 *
 * After safe publication, an {@code AutoCompleter} is completely thread
 * safe.  Setting the maximum search queue size is safe because
 * integer writes are atomic, but it is not declared volate, and hence
 * may not be visible to other threads without synchronization.
 *
 * <h3>Serialization</h3>
 *
 * An {@code AutoCompleter} may be serialized if and only if its
 * weighted edit distance is serializable.  If so, the result
 * of serializing and reconstituting an auto-completer will produce
 * an auto-completer with the same behavior as the one serialized,
 * modulo the edit distance serialization, which is under control
 * of the edit distance implementation.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   Lingpipe3.8
 */
public class AutoCompleter implements Serializable {

    static final long serialVersionUID = -6846604550231066369L;

    // use to normalize log2 prob back to count
    final double mTotalCount;

    // Phrase inputs with probs (parallel arrays)
    final String[] mPhrases;
    final float[] mPhraseLog2Probs;

    // Nodes (Parallel arrays)
    final char[] mLabels;
    final int[] mFirstDtr;      // points to self
    final int[] mFirstOutcome;  // points to mOutcomes

    // outcome IDs for nodes
    final int[] mOutcomes;

    int mMaxSearchQueueSize;
    final int mMaxResultsPerPrefix;

    final WeightedEditDistance mEditDistance;

    final double mMinScore;

    /**
     * Construct an automatic completer from the specified phrases,
     * phrase counts, edit distance, and search parameters.
     *
     * @param phraseCounts Map from phrases to counts.
     * @param editDistance Distance used to compare mismatched
     * suggestions.
     * @param maxResultsPerPrefix The maximum number of results that
     * can be returned.
     * @param maxSearchQueueSize The beam size for searching for
     * matches.
     * @param minScore Minimum score for outcome to be retained in results.
     * @throws IllegalArgumentException If any of the counts is not finite or negative,
     * or if the max results or max queue sizes are not positive, or if the minimum
     * score is not finite and negative.
     */
    public AutoCompleter(Map<String,? extends Number> phraseCounts,
                         WeightedEditDistance editDistance,
                         int maxResultsPerPrefix,
                         int maxSearchQueueSize,
                         double minScore) {

        if (Double.isInfinite(minScore)
            || Double.isNaN(minScore)
            || minScore >= 0.0) {
            String msg = "Minimum score must be finite and negative."
                + " Found minScore=" + minScore;
            throw new IllegalArgumentException(msg);
        }
        mMinScore = minScore;
        String[] phrases = new String[phraseCounts.size()];
        float[] counts = new float[phraseCounts.size()];
        mPhrases = phrases;
        int idx = 0;
        for (Map.Entry<String,? extends Number> entry : phraseCounts.entrySet()) {
            mPhrases[idx] = entry.getKey();
            counts[idx] = entry.getValue().floatValue();
            if (Float.isNaN(counts[idx])
                || Float.isInfinite(counts[idx])
                || counts[idx] < 0.0f) {
                String msg = "Counts must be finite and non-negative."
                    + " Found phrase=" + entry.getKey()
                    + " count=" + entry.getValue();
                throw new IllegalArgumentException(msg);
            }
            ++idx;
        }
        setMaxSearchQueueSize(maxSearchQueueSize);
        if (maxResultsPerPrefix <= 0) {
            String msg = "Max results per prefix must be positive."
                + " Found maxResultsPerPrefix=" + maxResultsPerPrefix;
            throw new IllegalArgumentException(msg);
        }

        mMaxResultsPerPrefix = maxResultsPerPrefix;
        mEditDistance = editDistance;

        float[] phraseLog2Probs = new float[counts.length];
        mPhraseLog2Probs = phraseLog2Probs;
        double totalCount = 0.0;
        for (int i = 0; i < counts.length; ++i)
            totalCount += counts[i];
        mTotalCount = totalCount;
        for (int i = 0; i < counts.length; ++i)
            phraseLog2Probs[i]
                = (float) com.aliasi.util.Math.log2(counts[i]/totalCount);


        int maxLength = maxLength(phrases);


        int[] numNodes = new int[maxLength];
        String last = "";
        for (String phrase : phrases) {
            for (int pos = prefixMatchLength(phrase,last);
                 pos < phrase.length(); ++pos)
                ++numNodes[pos];
            last = phrase;
        }

        int totalNumNodes = 1; // start with root
        for (int pos = 0; pos < maxLength; ++pos)
            totalNumNodes += numNodes[pos];


        int[] nextIndex = new int[maxLength];
        nextIndex[0] = 1;
        for (int pos = 1; pos < maxLength; ++pos)
            nextIndex[pos] = nextIndex[pos-1] + numNodes[pos-1];


        mLabels = new char[totalNumNodes];
        mFirstDtr = new int[totalNumNodes+1];
        mFirstOutcome = new int[totalNumNodes+1];  // populated later
        last = "";
        for (String phrase : phrases) {
            for (int pos = prefixMatchLength(phrase,last);
                 pos < phrase.length(); ++pos) {
                mLabels[nextIndex[pos]] = phrase.charAt(pos);
                mFirstDtr[nextIndex[pos]]
                    = (pos+1) < nextIndex.length
                    ? nextIndex[pos+1]
                    : totalNumNodes;
                ++nextIndex[pos];
            }
            last = phrase;
        }


        int outcomesLength = 0;
        int prefixCount = 0;
        for (int length = 0; length <= maxLength; ++length) {
            for (int i = 0; i < phrases.length; ) {
                while (i < phrases.length && phrases[i].length() < length)
                    ++i;
                if (i >= phrases.length) break;
                String currentPrefix = phrases[i].substring(0,length);
                for ( ; i < phrases.length
                          && phrases[i].startsWith(currentPrefix); ++i)
                    ++prefixCount;
                outcomesLength += Math.min(prefixCount,maxResultsPerPrefix);
                prefixCount = 0;
            }
        }

        mOutcomes= new int[outcomesLength];

        BoundedPriorityQueue<ScoredObject<Integer>> queue
            = new BoundedPriorityQueue<ScoredObject<Integer>>(ScoredObject.comparator(),
                                                              maxResultsPerPrefix);
        int prefixIdx = 0;
        int id = 0;
        for (int length = 0; length <= maxLength; ++length) {
            for (int i = 0; i < phrases.length; ) {
                while (i < phrases.length && phrases[i].length() < length)
                    ++i;
                if (i >= phrases.length) break;
                String currentPrefix = phrases[i].substring(0,length);
                for ( ; i < phrases.length
                          && phrases[i].startsWith(currentPrefix); ++i)
                    queue.offer(new ScoredObject<Integer>(i,phraseLog2Probs[i]));
                mFirstOutcome[prefixIdx++] = id;
                for (ScoredObject<Integer> so : queue)
                    mOutcomes[id++] = so.getObject();
                queue.clear();
            }
        }

        mFirstDtr[mFirstDtr.length-1] = mFirstDtr.length-1;
        mFirstOutcome[mFirstOutcome.length-1] = mOutcomes.length;

        /*
        System.out.printf("\n%3s  %6s  %s\n",
                          "i", "log2 P", "score");
        System.out.printf("%3s  %6s  %s\n",
                          "---","------","-----");
        for (int i = 0; i < phrases.length; ++i)
            System.out.printf("%3d  %6.2f  %s\n",i,phraseLog2Probs[i],phrases[i]);

        System.out.printf("\n%7s %5s %7s %7s\n",
                          "i", "label", "1st dtr", "1st out");
        System.out.printf("%7s %5s %7s %7s\n",
                          "-------", "-----", "-------", "-------");
        for (int i = 0; i < totalNumNodes; ++i)
            System.out.printf("%7d %5s %7d %7d\n",
                              i,
                              "" + mLabels[i],
                              mFirstDtr[i],
                              mFirstOutcome[i]);

        System.out.printf("\n%7s %5s %20s %6s\n",
                          "i", "out", "phrase", "log2 P");
        System.out.printf("%7s %5s %20s %6s\n",
                          "-------", "-----", "--------------------", "------");
        for (int i = 0; i < mOutcomes.length; ++i)
            System.out.printf("%7d %5d %20s %6.2f\n",
                              i,
                              mOutcomes[i],
                              mPhrases[mOutcomes[i]],
                              mPhraseLog2Probs[mOutcomes[i]]);
        */
    }

    /**
     * Returns the maximum number of results returned by this
     * auto completer for each input prefix.
     *
     * @return The maximum number of results returned.
     */
    public int maxResultsPerPrefix() {
        return mMaxResultsPerPrefix;
    }

    /**
     * Returns the weighted edit distance for this auto-completer.
     */
    public WeightedEditDistance editDistance() {
        return mEditDistance;
    }

    /**
     * Returns the phrase counter for this auto completer.  The
     * result will not be identical to the phrase counter used to
     * construct this map because this method reconstitutes the map
     * with {@code Float} values calculated from compiled probability
     * estimates.  Changes to the returned count map will not affect
     * this class.
     *
     * @return The phrase counter for this auto completer.
     */
    public Map<String,Float> phraseCountMap() {
        Map<String,Float> counter = new HashMap<String,Float>((mPhrases.length * 3)/2);
        for (int i = 0; i < mPhrases.length; ++i)
            counter.put(mPhrases[i],
                        (float) (mTotalCount * Math.pow(2.0,mPhraseLog2Probs[i])));
        return counter;
    }

    /**
     * Returns the maximum number of elements on the search queue.
     * This number is the size of the beam.  The value may be set
     * using {@link #setMaxSearchQueueSize(int)}.
     *
     * @return The maximum search queue size.
     */
    public int maxSearchQueueSize() {
        return mMaxSearchQueueSize;
    }

    /**
     * Sets the maximum search queue size to the specified value.
     * Larger values may produce more accurate search, but may take
     * longer to perform completions.
     *
     * <p>This operation is thread safe because integer sets are
     * atomic.  But changes may not be visible to other threads if not
     * synchronized.
     *
     * @param size The new search queue size.
     * @throws IllegalArgumentException If the size is zero or negative.
     */
    public void setMaxSearchQueueSize(int size) {
        if (size <= 0) {
            String msg = "Max queue size must be positive."
                + " Found maxSearchQueueSize=" + size;
            throw new IllegalArgumentException(msg);
        }
        mMaxSearchQueueSize = size;
    }


    /**
     * Returns a set of scored phrases sorted into decreasing order of
     * score.  The scores are determined as described in the class documentation
     * above.
     *
     * <p>To print out all the matches in descending order of scores, use:
     *
     * <blockquote><pre>{@code
     *  for (ScoredObject<String> so : complete(String))
     *      println("phrase=" + so.getObject() + " score=" + so.score());
     * }</pre></blockquote>
     *
     * @param in The string to complete.
     * @return The best scoring completions of the string.
     */
    public SortedSet<ScoredObject<String>> complete(String in) {
        Results results = new Results(mMaxResultsPerPrefix);
        BoundedPriorityQueue<SearchState> queue
            = new BoundedPriorityQueue<SearchState>(ScoredObject.comparator(),
                                                    mMaxSearchQueueSize);
        queue.offer(new SearchState(0, 0, 0.0, mPhraseLog2Probs[0]));
        while (!queue.isEmpty()) {
            SearchState state = queue.poll();
            if (results.dominate(state.mEditCost))
                return results;
            if (state.mInputPosition == in.length()) {
                for (int k = mFirstOutcome[state.mTrieNode]; k < mFirstOutcome[state.mTrieNode+1]; ++k) {
                    double score
                        = mPhraseLog2Probs[mOutcomes[k]]
                        + state.mEditCost;
                    if (score < mMinScore) 
                        continue;
                    results.add(mPhrases[mOutcomes[k]],score);
                }
                continue;
            } 
            char c = in.charAt(state.mInputPosition);
            for (int i = mFirstDtr[state.mTrieNode]; i < mFirstDtr[state.mTrieNode+1]; ++i) {
                char d = mLabels[i];
                double bestCompletionCost = mPhraseLog2Probs[mOutcomes[mFirstOutcome[i]]];
                // match or subst
                double editCost
                    = (c == d)
                    ? state.mEditCost
                    : (state.mEditCost + mEditDistance.substituteWeight(c,d));
                double score = editCost + bestCompletionCost;
                if (score >= mMinScore && !results.dominate(score))
                    queue.offer(new SearchState(state.mInputPosition+1,
                                                i,
                                                editCost,
                                                bestCompletionCost));

                // insert
                editCost = state.mEditCost + mEditDistance.insertWeight(d);
                score = editCost + bestCompletionCost;
                if (score >= mMinScore && !results.dominate(score))
                    queue.offer(new SearchState(state.mInputPosition,
                                                i,
                                                editCost,
                                                bestCompletionCost));
                // transpose
                // not implemented yet
            }
            // delete
            double editCost = state.mEditCost + mEditDistance.deleteWeight(c);
            double bestCompletionCost
                = mPhraseLog2Probs[mOutcomes[mFirstOutcome[state.mTrieNode]]];
            double score = editCost + bestCompletionCost;
            if (score >= mMinScore && !results.dominate(score))
                queue.offer(new SearchState(state.mInputPosition+1,
                                            state.mTrieNode,
                                            editCost,
                                            bestCompletionCost));
        }
        return results;
    }

    static class Results
        extends AbstractSet<ScoredObject<String>>
        implements SortedSet<ScoredObject<String>> {

        private final String[] mResults;
        private final double[] mScores;
        private int mSize = 0;
        Results(int maxSize) {
            mResults = new String[maxSize];
            mScores = new double[maxSize];
        }
        public boolean dominate(double score) {
            return full() && mScores[mSize-1] >= score;
        }
        public void add(String s, double score) {
            for (int i = 0; i < mSize; ++i) {
                if (score > mScores[i]) {
                    tamp(i,s);
                    mScores[i] = score;
                    mResults[i] = s;
                    return;
                }
                if (mResults[i].equals(s))
                    return;
            }
            if (mSize < mResults.length) {
                mResults[mSize] = s;
                mScores[mSize] = score;
                ++mSize;
            }
        }
        void tamp(int i, String s) {
            // check for dups and push up
            for (int pos = i; pos < mSize; ++pos) {
                if (mResults[pos].equals(s)) {
                    while (++pos < mSize) {
                        mResults[pos-1] = mResults[pos];
                        mScores[pos-1] = mScores[pos];
                    }
                    return;
                }
            }
            // increment if not max size; else don't increment and don't run over
            int pos = (mSize < mResults.length) ? (mSize++) : (mSize-1);
            while (--pos >= i) {
                    mResults[pos+1] = mResults[pos];
                    mScores[pos+1] = mScores[pos];
            }
        }
        public boolean full() {
            return mSize == mResults.length;
        }
        @Override
        public int size() {
            return mSize;
        }
        public ScoredObject<String> first() {
            if (mSize == 0)
                throw new NoSuchElementException();
            return new ScoredObject<String>(mResults[0],mScores[0]);
        }
        public ScoredObject<String> last() {
            if (mSize == 0)
                throw new NoSuchElementException();
            return new ScoredObject<String>(mResults[mSize-1],mScores[mSize-1]);
        }

        public SortedSet<ScoredObject<String>> headSet(ScoredObject<String> from) {
            return null;
        }
        public SortedSet<ScoredObject<String>> tailSet(ScoredObject<String> from) {
            return null;
        }
        public SortedSet<ScoredObject<String>> subSet(ScoredObject<String> from,
                                                       ScoredObject<String> to) {
            return null;
        }
        public Comparator<ScoredObject<String>> comparator() {
            return ScoredObject.reverseComparator();
        }

        @Override
        public Iterator<ScoredObject<String>> iterator() {
            return new ResultsIterator();
        }

        class ResultsIterator implements Iterator<ScoredObject<String>> {
            int mPosition = 0;
            public boolean hasNext() {
                return mPosition < mSize;
            }
            public ScoredObject<String> next() {
                ++mPosition;
                return new ScoredObject<String>(mResults[mPosition-1],
                                                mScores[mPosition-1]);
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    boolean dominated(double score, SortedSet<ScoredObject<String>> results) {
        return (score < mMinScore)
            || ( (results.size() == mMaxResultsPerPrefix)
                 && (results.last().score() >= score) );
    }

    private Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 2403836255870648306L;
        final AutoCompleter mAutoCompleter;
        public Serializer() {
            this(null);
        }
        public Serializer(AutoCompleter autoCompleter) {
            mAutoCompleter = autoCompleter;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeObject(mAutoCompleter.mEditDistance);
            objOut.writeInt(mAutoCompleter.mMaxResultsPerPrefix);
            objOut.writeInt(mAutoCompleter.mMaxSearchQueueSize);
            objOut.writeInt(mAutoCompleter.mPhrases.length);
            for (int i = 0; i < mAutoCompleter.mPhrases.length; ++i) {
                objOut.writeUTF(mAutoCompleter.mPhrases[i]);
                objOut.writeFloat((float)
                                  (mAutoCompleter.mTotalCount
                                   * Math.pow(2.0,mAutoCompleter.mPhraseLog2Probs[i])));
            }
            objOut.writeDouble(mAutoCompleter.mMinScore);

        }
        @Override
        public Object read(ObjectInput objIn)
            throws ClassNotFoundException, IOException {

            WeightedEditDistance editDistance = (WeightedEditDistance) objIn.readObject();
            int maxResultsPerPrefix = objIn.readInt();
            int maxSearchQueueSize = objIn.readInt();
            int numPhrases = objIn.readInt();
            Map<String,Float> phraseCountMap = new HashMap<String,Float>((numPhrases*3)/2);
            for (int i = 0; i < numPhrases; ++i) {
                String phrase = objIn.readUTF();
                float count = objIn.readFloat();
                phraseCountMap.put(phrase,count);
            }
            double minScore = objIn.readDouble();
            return new AutoCompleter(phraseCountMap,
                                     editDistance,
                                     maxResultsPerPrefix,
                                     maxSearchQueueSize,
                                     minScore);
        }
    }

    static class SearchState implements Scored {
        final int mInputPosition;
        final int mTrieNode;
        final double mEditCost;
        final double mScore;
        SearchState(int inputPosition,
                    int trieNode,
                    double editCost,
                    double bestCompletionCost) {
            mInputPosition = inputPosition;
            mTrieNode = trieNode;
            mEditCost = editCost;
            mScore = editCost + bestCompletionCost;
        }
        public double score() {
            return mScore;
        }
    }

    static int prefixMatchLength(String x, String y) {
        int len = Math.min(x.length(),y.length());
        for (int i = 0; i < len; ++i)
            if (x.charAt(i) != y.charAt(i))
                return i;
        return len;
    }


    static int maxLength(String[] xs) {
        int max = -1;
        for (String x : xs)
            if (x.length() > max)
                max = x.length();
        return max;
    }

}
