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
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A <code>FixedWeightEditDistance</code> sets constant weights for
 * the edit operations for weighted edit distance.
 *
 * <P>Subclasses of this class may override any of the weight
 * implementations.
 *
 * <h3>Serialization and Compilation</h3>
 *
 * <p>Serialization and compilation both do the same thing, namely
 * store the fixed weight edit distance for reading in later.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.0
 */
public class FixedWeightEditDistance
    extends WeightedEditDistance
    implements Compilable, Serializable {

    static final long serialVersionUID = 6520949001598595981L;

    private final double mMatchWeight;
    private final double mDeleteWeight;
    private final double mInsertWeight;
    private final double mSubstituteWeight;
    private final double mTransposeWeight;

    /**
     * Construct a weighted edit distance with the specified constant
     * weights for edits.
     *
     * <p>See the {@link WeightedEditDistance} class documewntation
     * for more information on similarity versus dissimialrity
     * measures.
     *
     * @param matchWeight Weight for matching.
     * @param deleteWeight Weight for deleting.
     * @param insertWeight Weight for inserting.
     * @param substituteWeight Weight for substituting.
     * @param transposeWeight Weight for transposing.
     */
    public FixedWeightEditDistance(double matchWeight,
                                   double deleteWeight,
                                   double insertWeight,
                                   double substituteWeight,
                                   double transposeWeight) {
        mMatchWeight = matchWeight;
        mDeleteWeight = deleteWeight;
        mInsertWeight = insertWeight;
        mSubstituteWeight = substituteWeight;
        mTransposeWeight = transposeWeight;
    }

    /**
     * Constructs an edit distance where the match weight is zero and
     * all other weights are positive infinity.  If none of the weight
     * methods are overridden in a sublcass, the result is a string
     * distance that is zero between identical strings and negative
     * infinity otherwise.
     */
    public FixedWeightEditDistance() {
        this(0.0,
             Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
             Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }


    /**
     * Returns a string-based representation of this fixed-weight
     * edit distance's parameters.
     *
     * @return String-based representation of this distance.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Edit Distance Class=" + getClass());
        sb.append("FixedWeightEditDistance costs:");
        sb.append("  match weight=" + mMatchWeight);
        sb.append("  insert weight=" + mInsertWeight);
        sb.append("  delete weight=" + mDeleteWeight);
        sb.append("  substitute weight=" + mSubstituteWeight);
        sb.append("  transpose weight=" + mTransposeWeight);
        return sb.toString();
    }

    private Object writeReplace() {
        return new Externalizer(this);
    }

    private static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 636473803792927790L;
        private final FixedWeightEditDistance mFWED;
        public Externalizer() {
            mFWED = null;
        }
        public Externalizer(FixedWeightEditDistance fwed) {
            mFWED = fwed;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeDouble(mFWED.mMatchWeight);
            objOut.writeDouble(mFWED.mDeleteWeight);
            objOut.writeDouble(mFWED.mInsertWeight);
            objOut.writeDouble(mFWED.mSubstituteWeight);
            objOut.writeDouble(mFWED.mTransposeWeight);
        }
        @Override
        public Object read(ObjectInput objIn) throws IOException {
            return new FixedWeightEditDistance(objIn.readDouble(),
                                               objIn.readDouble(),
                                               objIn.readDouble(),
                                               objIn.readDouble(),
                                               objIn.readDouble());
        }
    }

    /**
     * Writes a compiled version of this edit distance to the
     * specified object output.  If this method is called on an
     * instance of a subclass, only the fixed weight component is
     * compiled.  Thus subclasses should either override this method
     * or throw an {@link UnsupportedOperationException}.
     *
     * @param objOut The object output to which this fixed weight
     * edit distance is written.
     * @throws IOException If there is an I/O error while writing.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    /**
     * Returns the constant weight of matching the specified character.
     *
     * @param cMatched Character matched.
     * @return Weight of matching character.
     */
    @Override
    public double matchWeight(char cMatched) {
        return mMatchWeight;
    }

    /**
     * Returns the constant weight of deleting the specified character.
     *
     * @param cDeleted Character deleted.
     * @return Weight of deleting character.
     */
    @Override
    public double deleteWeight(char cDeleted) {
        return mDeleteWeight;
    }

    /**
     * Returns the constant weight of inserting the specified character.
     *
     * @param cInserted Character inserted.
     * @return Weight of inserting character.
     */
    @Override
    public double insertWeight(char cInserted) {
        return mInsertWeight;
    }

    /**
     * Returns the constant weight of substituting the inserted character for
     * the deleted character.
     *
     * @param cDeleted Deleted character.
     * @param cInserted Inserted character.
     * @return The weight of substituting the inserted character for
     * the deleted character.
     */
    @Override
    public double substituteWeight(char cDeleted, char cInserted) {
        return mSubstituteWeight;
    }

    /**
     * Returns the constant weight of transposing the specified characters.  Note
     * that the order of arguments follows that of the input.
     *
     * @param cFirst First character in input.
     * @param cSecond Second character in input.
     * @return The weight of transposing the specified characters.
     */
    @Override
    public double transposeWeight(char cFirst, char cSecond) {
        return mTransposeWeight;
    }


}
