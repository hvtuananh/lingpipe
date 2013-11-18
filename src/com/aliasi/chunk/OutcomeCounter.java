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

package com.aliasi.chunk;

import com.aliasi.symbol.SymbolTableCompiler;

// only used by TrainableEstimator, in turn used by TrainTokenShapeChunker
class OutcomeCounter {
    
    /**
     * Symbol representing the outcome of this counter.
     */
    private final String mSymbol;

    /**
     * Symbol table compiler from which the symbol is drawn.
     */
    private final SymbolTableCompiler mSymbolTable;

    /** 
     * Store for the integer count.
     */
    private int mCount;

    /**
     * Estimate of the likelihood of this outcome.
     */
    private float mEstimate;


    /** 
     * Create a counter with specified initial symbol, symbol table compiler,
     * and count.
     *
     * @param symbol Outcome symbol for this counter.
     * @param symbolTable Symbol table compiler for this counter.
     * @param count Initial count for this counter.
     */
    public OutcomeCounter(String symbol, SymbolTableCompiler symbolTable, int count) {
        mSymbol = symbol;
        mSymbolTable = symbolTable;
        mCount = count;
    }


    /** 
     * Increment the count by one.
     */
    public void increment() { 
    ++mCount; 
    }

    /** 
     * Return the current count.
     *
     * @return Current count.
     */
    public int count() { 
    return mCount; 
    }

    /**
     * Adds the symbol for this counter to the symbol table
     * compiler for this counter.
     */
    public void addSymbolToTable() {
        if (mSymbol != null) 
        mSymbolTable.addSymbol(mSymbol);
    }

    /**
     * Returns the ID for the symbol of this counter.  Must
     * be called after all symbols have been added to the symbol
     * table and the symbol table has been compiled.
     *
     * @return ID for the symbol of this counter.
     */
    public int getSymbolID() {
        return mSymbolTable.symbolToID(mSymbol);
    }

    /**
     * Returns the estimate for this counter as an outcome.
     *
     * @return Estimate for this counter's outcome.
     */
    public float estimate() { 
    return mEstimate; 
    }

    /**
     * Sets the estimate for this counter as an outcome to the
     * specified floating-point value.
     *
     * @param estimate Estimate for this counter.
     */
    public void setEstimate(float estimate) { 
    mEstimate = estimate; 
    }

}
