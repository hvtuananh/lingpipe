package com.aliasi.lm;

import java.io.IOException;

abstract class DeletingTrieReader implements TrieReader {

    private final TrieReader mReader;
    long mNextCount;

    DeletingTrieReader(TrieReader reader) throws IOException {
	mReader = reader;
	bufferCount();
    }

    public long readCount() {
	return mNextCount;
    }

    public long readSymbol() throws IOException {
	long sym;
	while ((sym = mReader.readSymbol()) != -1L) {
	    if (bufferCount())
		return sym;
	    else
		flushDaughters();
	}
	return -1L;
    }

    long nextCount() throws IOException {
	return mReader.readCount();
    }

    // buffer next count and return true if usable
    abstract boolean bufferCount() throws IOException;

    void flushDaughters() throws IOException {
	while (mReader.readSymbol() != -1L) {
	    mReader.readCount();
	    flushDaughters();
	}
    }

}