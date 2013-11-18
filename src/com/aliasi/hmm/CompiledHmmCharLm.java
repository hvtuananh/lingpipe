package com.aliasi.hmm;

import com.aliasi.lm.LanguageModel;

import com.aliasi.symbol.SymbolTable;

import java.io.IOException;
import java.io.ObjectInput;

class CompiledHmmCharLm extends AbstractHmm {

    private final double[][] mTransitionProbs;
    private final double[][] mTransitionLog2Probs;
    private final double[] mStartProbs;
    private final double[] mStartLog2Probs;
    private final double[] mEndProbs;
    private final double[] mEndLog2Probs;

    private final LanguageModel[] mEmissionLms;

    public CompiledHmmCharLm(ObjectInput in) 
        throws ClassNotFoundException, IOException {

        super((SymbolTable) in.readObject());
        int numStates = stateSymbolTable().numSymbols();
        mTransitionProbs = new double[numStates][numStates];
        mTransitionLog2Probs = new double[numStates][numStates];
        for (int i = 0; i < numStates; ++i)
            for (int j = 0; j < numStates; ++j)
                mTransitionLog2Probs[i][j]
                    = com.aliasi.util.Math.log2(mTransitionProbs[i][j] 
                                                = in.readDouble());
        mEmissionLms = new LanguageModel[numStates];
        for (int i = 0; i < numStates; ++i)
            mEmissionLms[i] = (LanguageModel) in.readObject();

        mStartProbs = new double[numStates];
        mStartLog2Probs = new double[numStates];
        mEndProbs = new double[numStates];
        mEndLog2Probs = new double[numStates];
        for (int i = 0; i < numStates; ++i)
            mStartLog2Probs[i] 
                = com.aliasi.util.Math.log2(mStartProbs[i] = in.readDouble());
        for (int i = 0; i < numStates; ++i)
            mEndLog2Probs[i] 
                = com.aliasi.util.Math.log2(mEndProbs[i] = in.readDouble());
        
    }


    @Override
    public double startProb(String state) {
        int id = stateSymbolTable().symbolToID(state);
        return id < 0 ? 0.0 : startProb(id);
    }

    @Override
    public double startProb(int stateId) {
        return mStartProbs[stateId];
    }

    @Override
    public double startLog2Prob(int stateId) {
        return mStartLog2Probs[stateId];
    }



    @Override
    public double endProb(String state) {
        int id = stateSymbolTable().symbolToID(state);
        return id < 0 ? 0.0 : endProb(id);
    }

    @Override
    public double endProb(int stateId) {
        return mEndProbs[stateId];
    }

    @Override
    public double endLog2Prob(int stateId) {
        return mEndLog2Probs[stateId];
    }



    @Override
    public double transitProb(String source, String target) {
        int idSrc = stateSymbolTable().symbolToID(source);
        if (idSrc < 0) return 0.0;
        int idTarget = stateSymbolTable().symbolToID(target);
        return idTarget < 0 ? 0.0 : transitProb(idSrc,idTarget);
    }

    @Override
    public double transitProb(int sourceId, int targetId) {
        return mTransitionProbs[sourceId][targetId];
    }

    @Override
    public double transitLog2Prob(int sourceId, int targetId) {
        return mTransitionLog2Probs[sourceId][targetId];
    }

    @Override
    public double emitProb(String state, CharSequence emission) {
        int id = stateSymbolTable().symbolToID(state);
        return id < 0 ? 0.0 : emitProb(id,emission);
    }

    @Override
    public double emitProb(int stateId, CharSequence emission) {
        return java.lang.Math.pow(2.0,emitLog2Prob(stateId,emission));
    }

    @Override
    public double emitLog2Prob(int stateId, CharSequence emission) {
        return mEmissionLms[stateId].log2Estimate(emission);
    }


}
