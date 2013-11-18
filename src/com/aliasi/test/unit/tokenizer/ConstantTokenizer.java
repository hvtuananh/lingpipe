package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.Tokenizer;

public class ConstantTokenizer extends Tokenizer {
    private final String[] mTokens;
    private final String[] mWhitespaces;
    private int mNextToken = 0;
    public ConstantTokenizer(String[] tokens,
                             String[] whitespaces) {
        if (whitespaces.length != tokens.length + 1)
            throw new IllegalArgumentException("lengths bad");
        mTokens = tokens;
        mWhitespaces = whitespaces;
    }
    @Override
    public String nextToken() {
        if (mNextToken >= mTokens.length) {
            return null;
        }
        return mTokens[mNextToken++];
    }
    @Override
    public String nextWhitespace() {
        return mWhitespaces[mNextToken];
    }
}
