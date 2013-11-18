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

package com.aliasi.lm;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.Strings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.io.ObjectInput;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A <code>CompiledTokenizedLM</code> implements a tokenized bounded
 * sequence language model.  Instances are read from streams of bytes
 * created by compiling a {@link TokenizedLM}; see that class for
 * more information.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.0
 */
public  class CompiledTokenizedLM
    implements LanguageModel.Sequence,
               LanguageModel.Tokenized {

    private final TokenizerFactory mTokenizerFactory;
    private final SymbolTable mSymbolTable;
    private final LanguageModel.Sequence mUnknownTokenModel;
    private final LanguageModel.Sequence mWhitespaceModel;

    private final int mMaxNGram;
    private final int[] mTokens;
    private final float[] mLogProbs;
    private final float[] mLogLambdas;
    private final int[] mFirstChild;

    CompiledTokenizedLM(ObjectInput in)
        throws IOException, ClassNotFoundException {

        String tokenizerClassName = in.readUTF();
        if (tokenizerClassName.equals("")) {
            mTokenizerFactory
                = (TokenizerFactory) in.readObject();
        }  else {
            try {
                Class<?> tokenizerClass = Class.forName(tokenizerClassName);
                @SuppressWarnings({"unchecked","rawtypes"}) // required for generic arrays
                Constructor<? extends TokenizerFactory> tokCons
                    = (Constructor<? extends TokenizerFactory>) tokenizerClass.getConstructor(new Class[0]);
                mTokenizerFactory
                    = (TokenizerFactory) tokCons.newInstance(new Object[0]);
            } catch (NoSuchMethodException e) {
                throw new ClassNotFoundException("Constructing " + tokenizerClassName,
                                                 e);
            } catch (InstantiationException e) {
                throw new ClassNotFoundException("Constructing " + tokenizerClassName,
                                                 e);
            } catch (IllegalAccessException e) {
                throw new ClassNotFoundException("Constructing " + tokenizerClassName,
                                                 e);
            } catch (InvocationTargetException e) {
                throw new ClassNotFoundException("Constructing " + tokenizerClassName,
                                                 e);
            }
        }

        mSymbolTable = (SymbolTable) in.readObject();

        mUnknownTokenModel
            = (LanguageModel.Sequence) in.readObject();

        mWhitespaceModel
            = (LanguageModel.Sequence) in.readObject();

        mMaxNGram = in.readInt();

        int numNodes = in.readInt();
        int lastInternalNodeIndex = in.readInt();
        mTokens = new int[numNodes];
        mLogProbs = new float[numNodes];
        mLogLambdas = new float[lastInternalNodeIndex+1];
        mFirstChild = new int[lastInternalNodeIndex+2];
        mFirstChild[mFirstChild.length-1] = numNodes;  // one past last dtr
        for (int i = 0; i < numNodes; ++i) {
            mTokens[i] = in.readInt();
            mLogProbs[i] = in.readFloat();
            if (i <= lastInternalNodeIndex) {
                mLogLambdas[i] = in.readFloat();
                mFirstChild[i] = in.readInt();
            }
        }
    }

    /**
     * Returns a string-based representation of this compiled language
     * model.
     *
     * <P><i>Warning:</i> The output may be very long for a large model
     * and may blow out memory attempting to pile it into a string
     * buffer.
     *
     * @return A string-based representation of this language model.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tokenizer Class Name=" + mTokenizerFactory);
        sb.append('\n');
        sb.append("Symbol Table=" + mSymbolTable);
        sb.append('\n');
        sb.append("Unknown Token Model=" + mUnknownTokenModel);
        sb.append('\n');
        sb.append("Whitespace Model=" + mWhitespaceModel);
        sb.append('\n');
        sb.append("Token Trie");
        sb.append('\n');
        sb.append("Nodes=" + mTokens.length + " Internal=" + mLogLambdas.length);
        sb.append('\n');
        sb.append("Index Tok logP firstDtr log(1-L)");
        sb.append('\n');
        for (int i = 0; i < mTokens.length; ++i) {
            sb.append(i);

            sb.append('\t');
            sb.append(mTokens[i]);
            sb.append('\t');
            sb.append(mLogProbs[i]);
            if (i < mFirstChild.length) {
                sb.append('\t');
                sb.append(mFirstChild[i]);
                if (i < mLogLambdas.length) {
                    sb.append('\t');
                    sb.append(mLogLambdas[i]);
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // next two method cut-and-pasted from TokenizedLM
    public double log2Estimate(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return log2Estimate(cs,0,cs.length);
    }

    public double log2Estimate(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        double logEstimate = 0.0;

        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end-start);
        List<String> tokenList = new ArrayList<String>();
        while (true) {
            String whitespace = tokenizer.nextWhitespace();
            logEstimate += mWhitespaceModel.log2Estimate(whitespace);
            String token = tokenizer.nextToken();
            if (token == null) break;
            tokenList.add(token);
        }

        // collect token ids, estimate unknown tokens
        int[] tokIds = new int[tokenList.size()+2];
        tokIds[0] = TokenizedLM.BOUNDARY_TOKEN;
        tokIds[tokIds.length-1] = TokenizedLM.BOUNDARY_TOKEN;
        Iterator<String> it = tokenList.iterator();
        for (int i = 1; it.hasNext(); ++i) {
            String token = it.next();
            tokIds[i] = mSymbolTable.symbolToID(token);
            if (tokIds[i] < 0) {
                logEstimate += mUnknownTokenModel.log2Estimate(token);
            }
        }

        // estimate token ids
        for (int i = 2; i <= tokIds.length; ++i) {
            logEstimate += conditionalTokenEstimate(tokIds,0,i);
        }
        return logEstimate;
    }

    private double conditionalTokenEstimate(int[] tokIds, int start, int end) {
        double estimate = 0.0;
        int contextEnd = end-1;
        int tokId = tokIds[contextEnd]; // tok past ctxt is estimated
        int maxContextLength = Math.min(contextEnd-start,mMaxNGram-1);
        for (int contextLength = maxContextLength;
             contextLength >= 0;
             --contextLength) {

            int contextStart = contextEnd - contextLength;
            int contextIndex = getIndex(tokIds,contextStart,contextEnd);
            if (contextIndex == -1) continue;
            if (tokId == TokenizedLM.UNKNOWN_TOKEN) {
                if (hasDtrs(contextIndex))
                    estimate += mLogLambdas[contextIndex];
                continue;
            }
            int outcomeIndex = getIndex(contextIndex,tokId);
            if (outcomeIndex != -1)
                return estimate + mLogProbs[outcomeIndex];
            if (hasDtrs(contextIndex))
                estimate += mLogLambdas[contextIndex];
        }
        // fall through to here for unknowns
        return estimate;
    }

    public double tokenLog2Probability(String[] tokens, int start, int end) {
        int[] tokIds = new int[tokens.length];
        for (int i = 0; i < tokens.length; ++i)
            tokIds[i] = mSymbolTable.symbolToID(tokens[i]);
        double sum = 0.0;
        for (int i = start+1; i <= end; ++i)
            sum += conditionalTokenEstimate(tokIds,start,i);
        return sum;
    }

    public double tokenProbability(String[] tokens, int start, int end) {
        return java.lang.Math.pow(2.0,tokenLog2Probability(tokens,start,end));
    }

    /**
     * Returns <code>true</code> if the context with the specified
     * index has at least one daughter node.
     *
     * @return <code>true</code> if the specified context node has a
     * daughter.
     */
    boolean hasDtrs(int contextIndex) {
        return contextIndex < mLogLambdas.length
            && !Double.isNaN(mLogLambdas[contextIndex]);
    }

    // following cut-and-paste fr. CompiledNGramProcessLM w. type substs
    private int getIndex(int fromIndex, int tokId) {
        if (fromIndex+1 >= mFirstChild.length) return -1;
        int low = mFirstChild[fromIndex];
        int high = mFirstChild[fromIndex+1]-1;
        while (low <= high) {
            int mid = (high + low)/2;
            if (mTokens[mid] == tokId) {
                return mid;
            }
            else if (mTokens[mid] < tokId)
                low = (low == mid) ? mid+1 : mid;
            else
                high = (high == mid) ? mid-1 : mid;
        }
        return -1;
    }

    private int getIndex(int[] tokIds, int start, int end) {
        int index = 0;
        for (int currentStart = start;
             currentStart < end;
             ++currentStart) {
            index = getIndex(index,tokIds[currentStart]);
            if (index == -1) return -1;
        }
        return index;
    }

}
