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
 * The <code>HiddenMarkovModel</code> interface provides a means
 * for definining the probability estimates and symbol table
 * underlying a hidden Markov model (HMM).
 * Probabilities may be retrieved by symbol identifier or by string,
 * and in either straight or log<sub><sub>2</sub></sub> form.  Hence
 * the class looks a little more complicated than it is, because there
 * are four variants of each method:
 *
 * <blockquote>
 * <table cellpadding='5' border='1'>

 * <tr><td>&nbsp;</td>
 *     <td><i>by Tag Name</i></td>
 *      <td><i>by Tag Identifier</i></td></tr>
 *
 * <tr><td rowspan='4'><i>Linear</i></td>
 *     <td>{@link #startProb(String)}</td>
 *     <td>{@link #startProb(int)}</td></tr>
 *
 * <tr><td>{@link #endProb(String)}</td>
 *     <td>{@link #endProb(int)}</td></tr>
 *
 * <tr><td>{@link #transitProb(String,String)}</td>
 *     <td>{@link #transitProb(int,int)}</td></tr>
 *
 * <tr><td>{@link #emitProb(String,CharSequence)}</td>
 *     <td>{@link #emitProb(int,CharSequence)}</td></tr>
 * <tr><td rowspan='4'><i>Log</i></td>
 *     <td>{@link #startLog2Prob(String)}</td>
 *     <td>{@link #startLog2Prob(int)}</td></tr>
 *
 * <tr><td>{@link #endLog2Prob(String)}</td>
 *     <td>{@link #endLog2Prob(int)}</td></tr>
 *
 * <tr><td>{@link #transitLog2Prob(String,String)}</td>
 *     <td>{@link #transitLog2Prob(int,int)}</td></tr>
 *
 * <tr><td>{@link #emitLog2Prob(String,CharSequence)}</td>
 *     <td>{@link #emitLog2Prob(int,CharSequence)}</td></tr>
 * </table>
 * </blockquote>
 *
 * In addition, the method {@link #stateSymbolTable()} returns
 * the symbol table for states.
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public interface HiddenMarkovModel {

    /**
     * Return the symbol table for the states for this HMM.
     *
     * @return The symbol table for the states for this HMM.
     */
    public SymbolTable stateSymbolTable();

    /**
     * Returns the start probability for the state with the specified
     * identifier.
     *
     * @param stateId Identifier of state.
     * @return Start probability of state with specified identifier.
     */
    public double startProb(int stateId);

    /**
     * Returns the start probability for the specified state.
     *
     * @param state HMM state.
     * @return Start probability of specified state.
     */
    public double startProb(String state);


    /**
     * Returns the log (base 2) of the start probability for the
     * state with the specified identifier.
     *
     * @param stateId Identifier of state.
     * @return Start probability of state with specified identifier.
     */
    public double startLog2Prob(int stateId);

    /**
     * Returns the log (base 2) of the start probability for the
     * specified state.
     *
     * @param state HMM State.
     * @return Log start probability of state.
     */
    public double startLog2Prob(String state);


    /**
     * Returns the end probability for the state with the specified
     * identifier.
     *
     * @param stateId Identifier of state.
     * @return End probability of state with specified identifier.
     */
    public double endProb(int stateId);

    /**
     * Returns the end probability for the specified state.
     *
     * @param state HMM state.
     * @return End probability of specified state.
     */
    public double endProb(String state);


    /**
     * Returns the log (base 2) of the end probability for the
     * state with the specified identifier.
     *
     * @param stateId Identifier of state.
     * @return End probability of state with specified identifier.
     */
    public double endLog2Prob(int stateId);


    /**
     * Returns the log (base 2) of the end probability for the
     * specified state.
     *
     * @param state HMM State.
     * @return Log end probability of state.
     */
    public double endLog2Prob(String state);

    /**
     * Return an estimate of the transition likelihood from the source
     * state with the specified identifier to the target state with the
     * specified identifier.
     *
     * @param sourceId Source state identifier.
     * @param targetId Target state identifier.
     * @return Estimate of the transition probability.
     */
    public double transitProb(int sourceId, int targetId);

    /**
     * Returns the log (base 2) of the transition estimate.  See
     * {@link #transitProb(int,int)} for more information.
     *
     * @param sourceId Source state identifier.
     * @param targetId Target state identifier.
     * @return Estimate of the transition probability.
     */
    public double transitLog2Prob(int sourceId, int targetId);
    
    /**
     * Return an estimate of the likelihood of the state with the
     * specified identifier to emit the specified string.
     * 
     * @param stateId State identifier.
     * @param emission Character sequence emitted.
     * @return Estimate of probability of the specified state emitting the
     * specified string.
     */
    public double emitProb(int stateId, CharSequence emission);

    /**
     * Return the log (base 2) of the emission estimate.  See
     * {@link #emitProb(int,CharSequence)} for more details.
     *
     * @param stateId State identifier.
     * @param emission Character sequence emitted.
     * @return Log (base 2) estimate of probability of the specified
     * state emitting the specified string.
     */
    public double emitLog2Prob(int stateId, CharSequence emission);



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
    public double transitProb(String source, String target);

    /**
     * Returns the log (base 2) estimate of a transition probability.
     * See {@link #transitProb(String,String)} for more information.
     *
     * @param source Label of source state.
     * @param target Label of target state.
     * @return Log (base 2) Estimate of likelihood of transition from the source
     * to the target state.
     */
    public double transitLog2Prob(String source, String target);

    /**
     * Returns an estimate the emission of the specified string
     * by the state with the specified label.  
     *
     * @param state Label of state.
     * @param emission Character sequence emitted.
     * @return Estimate of likelihood of the state emitting the
     * string.
     */
    public double emitProb(String state, CharSequence emission);

    /**
     * Returns the log (base 2) of the emission estimate.  See
     * {@link #emitProb(String,CharSequence)} for more information.
     *
     * @param state Label of state.
     * @param emission Character sequence emitted.
     * @return Log (base 2) estimate of likelihood of the state
     * emitting the string.
     */
    public double emitLog2Prob(String state, CharSequence emission);
    
}
