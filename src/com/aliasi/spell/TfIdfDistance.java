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

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Counter;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * The <code>TfIdfDistance</code> class provides a string distance
 * based on term frequency (TF) and inverse document frequency (IDF).
 * The method {@link #distance(CharSequence,CharSequence)} will return
 * results in the range between <code>0</code> (perfect match) and
 * <code>1</code> (no match) inclusive; the method {@link
 * #proximity(CharSequence,CharSequence)} runs in the opposite
 * direction, returning <code>0</code> for no match and <code>1</code>
 * for a perfect match.  Full details are provided below.
 *
 * <p>Terms are produced from the character sequences being compared
 * by a tokenizer factory fixed at construction time.  These terms
 * form the dimensions of vectors whose values are the counts for the
 * terms in the strings being compared.
 *
 * <p>The raw term frequencies are adjusted in scale and by inverse
 * document frequency.  The resulting term vectors are then compared
 * by one minus their cosine.  Because the term vectors contain only
 * positive values, the result is a distance between zero
 * (<code>0</code>), for completely dissimilar strings, to one
 * (<code>1</code>), for character-by-character identical strings.
 *
 * <p>The inverse document frequencies are defined over a collection
 * of documents.  The collection of documents must be provided to this
 * class one at a time through the {@link #handle(CharSequence)} method.
 *
 * <h3>Formal Definition of TF/IDF Distance </h3>
 *
 * <p>Note that there are a range of different distances called
 * &quot;TF/IDF&quot; distance.  The one in this class is defined to
 * be symmetric, unlike typical TF/IDF distances defined for
 * information retrieval.  It scales inverse-document frequencies by
 * logs, and both inverse-document frequencies and term frequencies by
 * square roots.  This causes the influence of IDF to grow
 * logarithmically, and term frequency comparison to grow linearly.
 *
 * <p>Suppose we have a collection <code>docs</code> of <code>n</code>
 * strings, which we will call documents in keeping with tradition.
 * Further let <code>df(t,docs)</code> be the document frequency of
 * token <code>t</code>, that is, the number of documents in which the
 * token <code>t</code> appears.  Then the inverse document frequency
 * (IDF) of <code>t</code> is defined by:
 *
 * <blockquote><code>
 * idf(t,docs) = sqrt(log(n/df(t,docs)))
 * </code></blockquote>
 *
 * <p>If the document frequency <code>df(t,docs)</code> of a term is
 * zero, then <code>idf(t,docs)</code> is set to zero.  As a result,
 * only terms that appeared in at least one training document are
 * used during comparison.
 *
 * <p>The term vector for a string is then defined by its term
 * frequencies.  If <code>count(t,cs)</code> is the count of term
 * <code>t</code> in character sequence <code>cs</code>, then
 * the term frequency (TF) is defined by:
 *
 * <blockquote><code>
 * tf(t,cs) = sqrt(count(t,cs))
 * </code></blockquote>
 *
 * <p>The term-frequency/inverse-document frequency (TF/IDF) vector
 * <code>tfIdf(cs,docs)</code> for a character sequence <code>cs</code>
 * over a collection of documents <code>ds</code> has a value
 * <code>tfIdf(cs,docs)(t)</code> for term <code>t</code> defined by:
 *
 * <blockquote><code>
 * tfIdf(cs,docs)(t) = tf(t,cs) * idf(t,docs)
 * </code></blockquote>
 *
 * <p>The proximity between character sequences <code>cs1</code> and
 * <code>cs2</code> is defined as the cosine of their TF/IDF
 * vectors:
 *
 * <blockquote><code>
 * dist(cs1,cs2) = 1 - cosine(tfIdf(cs1,docs),tfIdf(cs2,docs))
 * </code></blockquote>
 *
 * <p>Recall that the cosine of two vectors is the dot product of the
 * vectors divided by their lengths:
 *
 * <blockquote><code>
 * cos(x,y) = x <sup>.</sup> y / ( |x| * |y| )
 * </code></blockquote>
 *
 * where dot products are defined by:
 *
 * <blockquote><code>
 * x <sup>.</sup> y = <big>&Sigma;</big><sub>i</sub> x[i] * y[i]
 * </code></blockquote>
 *
 * and length is defined by:
 *
 * <blockquote><code>
 * |x| = sqrt(x <sup>.</sup> x)
 * </code></blockquote>
 *
 * <p>Distance is then just 1 minus the proximity value.
 *
 * <blockquote><pre>
 * distance(cs1,cs2) = 1 - proximity(cs1,cs2)
 * </pre></blockquote>
 * <h3>References</h3>
 *
 * <ul>
 *
 * <li>Wikipedia.
 * <a href="http://en.wikipedia.org/wiki/Tf-idf">Tf-idf</a>.</li>
 *
 *
 * <li>Wikipedia.  <a
 * href="http://en.wikipedia.org/wiki/Vector_space_model">Vector space
 * model</a>.</li>
 *
 * <li>Witten, Moffat and Bell.  1999.  <a
 * href="http://www.cs.mu.oz.au/mg/">Managing Gigabytes: Compressing
 * and Indexing Documents and Images</a>.  Second Edition.  Morgan
 * Kaufmann.</li>
 *
 * <li>Apache Lucene Project.  <a href="http://lucene.apache.org/java/docs/api/org/apache/lucene/search/Similarity.html"><code>org.apache.lucene.search.Similarity</code> Class Documentation</a>.
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.4
 */
public class TfIdfDistance 
    extends TokenizedDistance 
    implements ObjectHandler<CharSequence> {

    private int mDocCount = 0;
    private final ObjectToCounterMap<String> mDocFrequency
        = new ObjectToCounterMap<String>();

    /**
     * Construct an instance of TF/IDF string distance based on the
     * specified tokenizer factory.
     *
     * @param tokenizerFactory Tokenizer factory for this distance.
     */
    public TfIdfDistance(TokenizerFactory tokenizerFactory) {
        super(tokenizerFactory);
    }

    /**
     * Add the specified character sequence as a document for training.
     *
     * @param cSeq Characters to trai.
     */
    public void handle(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        for (String token : tokenSet(cs,0,cs.length))
            mDocFrequency.increment(token);
        ++mDocCount;
        
    }

    /**
     * Return the TF/IDF distance between the specified character
     * sequences.  This distance depends on the training instances
     * supplied.  See the class documentation above for more
     * information.
     *
     * @param cSeq1 First character sequence.
     * @param cSeq2 Second character sequence.
     * @return The TF/IDF distance between the two sequences.
     */
    public double distance(CharSequence cSeq1, CharSequence cSeq2) {
        return 1.0 - proximity(cSeq1,cSeq2);
    }

    /**
     * Returns the TF/IDF proximity between the specified character
     * sequences.  The proximity depends on training instances
     * and the tokenizer factory; see the class documentation above
     * for details.
     *
     * @param cSeq1 First character sequence.
     * @param cSeq2 Second character sequence.
     * @return The TF/IDF proximity between the two sequences.
     */
    public double proximity(CharSequence cSeq1, CharSequence cSeq2) {
        // really only need to create one of these; other can just it and add
        ObjectToCounterMap<String> tf1 = termFrequencyVector(cSeq1);
        ObjectToCounterMap<String> tf2 = termFrequencyVector(cSeq2);
        double len1 = 0.0;
        double len2 = 0.0;
        double prod = 0.0;
        for (Map.Entry<String,Counter> entry : tf1.entrySet()) {
            String term = entry.getKey();
            Counter count1 = entry.getValue();
            double tfIdf1 = tfIdf(term,count1);
            len1 += tfIdf1 * tfIdf1;
            Counter count2 = tf2.remove(term);
            if (count2 == null) continue;
            double tfIdf2 = tfIdf(term,count2);
            len2 += tfIdf2 * tfIdf2;
            prod += tfIdf1 * tfIdf2;
        }
        // increment length for terms in cSeq2 but not in cSeq1
        for (Map.Entry<String,Counter> entry : tf2.entrySet()) {
            String term = entry.getKey();
            Counter count2 = entry.getValue();
            double tfIdf2 = tfIdf(term,count2);
            len2 += tfIdf2 * tfIdf2;
        }
        if (len1 == 0)
            return len2 == 0.0 ? 1.0 : 0.0;
        if (len2 == 0) return 0.0;
        double prox = prod / Math.sqrt(len1 * len2);
        return prox < 0.0 
            ? 0.0
            : (prox > 1.0
               ? 1.0
               : prox);
    }


    /**
     * Returns the number of training documents that contained
     * the specified term.
     *
     * @param term Term to test.
     * @return The number of training documents that contained the
     * specified term.
     */
    public int docFrequency(String term) {
        return mDocFrequency.getCount(term);
    }

    /**
     * Return the inverse-document frequency for the specified
     * term.  See the class doducmentation above for a formal
     * definition.
     *
     * @param term The term whose IDF is returned.
     * @return The IDF of the specified term.
     */
    public double idf(String term) {
        int df = mDocFrequency.getCount(term);
        if (df == 0) return 0.0;
        return java.lang.Math.log(((double) mDocCount)/((double) df));
    }

    /**
     * Returns the total number of training documents.
     *
     * @return The total number of training documents.
     */
    public int numDocuments() {
        return mDocCount;
    }

    /**
     * Returns the number of terms that have been seen
     * during training.
     *
     * @return The number of terms for this distance.
     */
    public int numTerms() {
        return mDocFrequency.size();
    }

    /**
     * Returns the set of known terms for this distance.  The set will
     * contain every token that was derived from a training instance.
     * Only terms in the returned term set will contribute to
     * similarity.  All other terms have an inverse-document frequency
     * of zero, so will not be matched, though they will contribute
     * to length during the cosine calculation.
     *
     * @return The set of terms for this distance measure.
     */
    public Set<String> termSet() {
        return Collections.<String>unmodifiableSet(mDocFrequency.keySet());
    }

    double tfIdf(String term, Counter count) {
        double idf = idf(term);
        double tf = count.doubleValue();
        return Math.sqrt(tf * idf);
    }

}
