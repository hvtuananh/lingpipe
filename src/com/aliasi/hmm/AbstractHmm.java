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

package com.aliasi.hmm;

import com.aliasi.symbol.SymbolTable;

/**
 * An <code>AbstractHmm</code> is an abstract implementation of a
 * hidden Markov model which manages a symbol table and defines the
 * basic methods in terms of the symbolic linear probability methods.
 * Specifically, the following four methods must be implemented by
 * subclasses:
 *
 * <blockquote><table border='1' cellpadding='5'>
 * <tr><td colspan='2'><b>Abstract Methods to Implement</b></td></tr>
 * <tr><td>{@link #startProb(String)}</td>
 *     <td>{@link #endProb(String)}</td></tr>
 * <tr><td>{@link #transitProb(String,String)}</td>
 *     <td>{@link #emitProb(String,CharSequence)}</td></tr>
 * </table></blockquote>
 *
 * For efficiency, the log- and identifier-based versions of these
 * methods may also be overridden.  The symbol table is provided to
 * the constructor and managed by this class.
 * 
 * @author  Bob Carpenter
 * @version 2.2
 * @since   LingPipe2.1
 */
public abstract class AbstractHmm implements HiddenMarkovModel {

    private final SymbolTable mStateSymbolTable;

    /**
     * Construct an HMM with the specified symbol table.
     *
     * @param stateSymbolTable Symbol table for the HMM.
     */
    public AbstractHmm(SymbolTable stateSymbolTable) {
	mStateSymbolTable = stateSymbolTable;
    }

    /**
     * Adds the state with the specified name to this hidden Markov
     * model.  
     *
     * @param state Name of state to add.
     * @return <code>true</code> if the state was not already a state
     * in this HMM.
     */
    public boolean addState(String state) {
	if (mStateSymbolTable.symbolToID(state) >= 0) return false;
	mStateSymbolTable.getOrAddSymbol(state);
	return true;
    }
                    

    /**
     * Return the symbol table for the states for this HMM.  This is
     * the actual symbol table used by this HMM and changes to it
     * affect this HMM.
     *
     * @return The symbol table for the states for this HMM.
     */
    public SymbolTable stateSymbolTable() {
	return mStateSymbolTable;
    }

    /**
     * Returns the start probability for the specified state.
     *
     * @param state HMM state.
     * @return Start probability of specified state.
     */
    public abstract double startProb(String state);

    /**
     * Returns the start probability for the state with the specified
     * identifier.
     *
     * <P>This method is implemented in terms of {@link #stateSymbolTable()}
     * and {@link #startProb(String)}.  
     *
     * @param stateId Identifier of state.
     * @return Start probability of state with specified identifier.
     */
    public double startProb(int stateId) {
	String state = stateSymbolTable().idToSymbol(stateId);
	return startProb(state);
    }

    /**
     * Returns the log (base 2) of the start probability for the
     * specified state.
     *
     * <P>This method is implemented in terms of {@link
     * com.aliasi.util.Math#log2(double)} and {@link
     * #startProb(String)}.
     *
     * @param state HMM State.
     * @return Log start probability of state.
     */
    public double startLog2Prob(String state) {
	return com.aliasi.util.Math.log2(startProb(state));
    }

    /**
     * Returns the log (base 2) of the start probability for the
     * state with the specified identifier.
     *
     * <P>This method is implemented in terms of {@link
     * com.aliasi.util.Math#log2(double)} and {@link #startProb(int)}.
     *
     * @param stateId Identifier of state.
     * @return Start probability of state with specified identifier.
     */
    public double startLog2Prob(int stateId) {
	return com.aliasi.util.Math.log2(startProb(stateId));
    }

    /**
     * Returns the end probability for the specified state.
     *
     * @param state HMM state.
     * @return End probability of specified state.
     */
    public abstract double endProb(String state);

    /**
     * Returns the end probability for the state with the specified
     * identifier.
     *
     * <P>This method is implemented with {@link #stateSymbolTable()} and
     * and {@link #endProb(String)}.  
     *
     * @param stateId Identifier of state.
     * @return End probability of state with specified identifier.
     */
    public double endProb(int stateId) {
	String state = stateSymbolTable().idToSymbol(stateId);
	if (stateId < 0) return 0.0;
	return endProb(state);
    }

    /**
     * Returns the log (base 2) of the end probability for the
     * specified state.
     *
     * <P>This method is implemented in terms of {@link
     * com.aliasi.util.Math#log2(double)} and {@link #endProb(String)}
     *
     * @param state HMM State.
     * @return Log end probability of state.
     */
    public double endLog2Prob(String state) {
	return com.aliasi.util.Math.log2(endProb(state));
    }

    /**
     * Returns the log (base 2) of the end probability for the
     * state with the specified identifier.
     *
     * <P>This method is implemented in terms of {@link
     * com.aliasi.util.Math#log2(double)} and {@link #endProb(int)}
     *
     * @param stateId Identifier of state.
     * @return End probability of state with specified identifier.
     */
    public double endLog2Prob(int stateId) {
	return com.aliasi.util.Math.log2(endProb(stateId));
    }


    /**
     * Returns an estimate of a transition from the source state with
     * the specified name to the target state with the specified
     * label.
     *
     * @param source Label of source state.
     * @param target Label of target state.
     * @return Estimate of likelihood of transition from the source
     * to the target state.
     */
    public abstract double transitProb(String source, String target);

    /**
     * Return an estimate of the transition likelihood from the source
     * state with the specified identifier to the target state with the
     * specified identifier.
     *
     * <P>This method is implemented in terms of {@link #stateSymbolTable()}
     * and {@link #transitProb(String,String)}.
     *
     * @param sourceId Source state identifier.
     * @param targetId Target state identifier.
     * @return Estimate of the transition probability.
     */
    public double transitProb(int sourceId, int targetId) {
	return transitProb(mStateSymbolTable.idToSymbol(sourceId),
			   mStateSymbolTable.idToSymbol(targetId));
    }

    /**
     * Returns the log (base 2) estimate of a transition probability.
     * See {@link #transitProb(String,String)} for more information.
     *
     * <P>This method is implemented in terms of {@link
     * com.aliasi.util.Math#log2(double)} and {@link
     * #transitProb(String,String)}.
     * 
     * @param source Label of source state.
     * @param target Label of target state.
     * @return Log (base 2) Estimate of likelihood of transition from
     * the source to the target state.
     */
    public double transitLog2Prob(String source, String target) {
	return com.aliasi.util.Math.log2(transitProb(source,target));
    }

    /**
     * Returns the log (base 2) of the transition estimate.  See
     * {@link #transitProb(int,int)} for more information.
     *
     * <P>This method is implemented in terms of {@link
     * com.aliasi.util.Math#log2(double)} and {@link
     * #transitProb(int,int)}
     * 
     * @param sourceId Source state identifier.
     * @param targetId Target state identifier.
     * @return Estimate of the transition probability.
     */
    public double transitLog2Prob(int sourceId, int targetId) {
	return com.aliasi.util.Math.log2(transitProb(sourceId,targetId));
    }
    


    /**
     * Returns an estimate the emission of the specified string
     * by the state with the specified label.  
     *
     * @param state Label of state.
     * @param emission Character sequence emitted.
     * @return Estimate of likelihood of the state emitting the
     * string.
     */
    public abstract double emitProb(String state, CharSequence emission);

    /**
     * Return an estimate of the likelihood of the state with the
     * specified identifier to emit the specified string.
     *
     * <P>This method is implemented in terms of {@link
     * #emitProb(String,CharSequence)}.
     * 
     * @param stateId State identifier.
     * @param emission Character sequence emitted.
     * @return Estimate of probability of the specified state emitting the
     * specified string.
     */
    public double emitProb(int stateId, CharSequence emission) {
	return emitProb(mStateSymbolTable.idToSymbol(stateId),emission);
    }

    /**
     * Return the log (base 2) of the emission estimate.  See
     * {@link #emitProb(int,CharSequence)} for more details.
     *
     * <P>This method is implemented in terms of {@link
     * com.aliasi.util.Math#log2(double)} and {@link
     * #emitProb(int,CharSequence)}.
     * 
     * @param stateId State identifier.
     * @param emission Character sequence emitted.
     * @return Log (base 2) estimate of probability of the specified
     * state emitting the specified string.
     */
    public double emitLog2Prob(int stateId, CharSequence emission) {
	return com.aliasi.util.Math.log2(emitProb(stateId,emission));
    }

    /**
     * Returns the log (base 2) of the emission estimate.  See
     * {@link #emitProb(String,CharSequence)} for more information.
     *
     * <P>This method is implemented in terms of {@link
     * com.aliasi.util.Math#log2(double)} and {@link
     * #emitProb(String,CharSequence)}.
     * 
     * @param state Label of state.
     * @param emission Character sequence emitted.
     * @return Log (base 2) estimate of likelihood of the state
     * emitting the string.
     */
    public double emitLog2Prob(String state, CharSequence emission) {
	return com.aliasi.util.Math.log2(emitProb(state,emission));
    }
    
}
