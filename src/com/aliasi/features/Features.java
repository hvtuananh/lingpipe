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

package com.aliasi.features;

import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;

import com.aliasi.symbol.SymbolTable;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code Features} class contains static utility classes for
 * manipulating features.
 *
 * @author  Bob Carpenter
 * @version 3.8.1
 * @since   LingPipe3.9
 */
public class Features {

    private Features() {
        /* no construction */
    }


    /**
     * Convert the specified feature vector into a sparse float vector
     * using the specified symbol table to encode features as
     * integers, adding features to the symbol table if necessary.
     * Features that do not exist as symbols in the symbol table will
     * be added to the symbol table.  If the add intercept flag is set
     * to {@code true}, an intercept value of 1.0 will be added as the
     * value of dimension 0.
     *
     * @param table Symbol table for encoding features as integers.
     * @param featureVector Feature vector to convert to sparse float vector.
     * @param numDimensions Number of dimensions for the vector.
     * @param addIntercept Flag indicating whether or not to add an intercept
     * value of 1.0 at position 0.
     * @return Sparse float vector encoding the feature vector with
     * the symbol table.
     */
    public static Vector
        toVectorAddSymbols(Map<String,? extends Number> featureVector,
                           SymbolTable table,
                           int numDimensions,
                           boolean addIntercept) {
        int size = (featureVector.size() * 3) / 2;
        Map<Integer, Number> vectorMap
            = new HashMap<Integer, Number>(size);
        for (Map.Entry<String, ? extends Number> entry
                 : featureVector.entrySet()) {
            String feature = entry.getKey();
            Number val = entry.getValue();
            int id = table.getOrAddSymbol(feature);
            vectorMap.put(Integer.valueOf(id), val);
        }
        if (addIntercept)
            vectorMap.put(Integer.valueOf(0), 1.0);
        return new SparseFloatVector(vectorMap, numDimensions);
    }


    /**
     * Convert the specified feature vector into a sparse float vector using
     * the specified symbol table to encode features as integers.  Features
     * that do not exist as symbols in the symbol table will be ignored.  If
     * the add intercept flag is set to {@code true}, an intercept value of
     * 1.0 will be added as the value of dimension 0.
     *

     *
     * @param table Symbol table for encoding features as integers.
     * @param featureVector Feature vector to convert to sparse float vector.
     * @param numDimensions Number of dimensions for the vector.
     * @param addIntercept Flag indicating whether or not to add an intercept
     * value of 1.0 at position 0.
     * @return Sparse float vector encoding the feature vector with
     * the symbol table.
     */
    public static Vector
        toVector(Map<String,? extends Number> featureVector,
                 SymbolTable table,
                 int numDimensions,
                 boolean addIntercept) {
        int size = (featureVector.size() * 3) / 2;
        Map<Integer, Number> vectorMap
            = new HashMap<Integer, Number>(size);
        for (Map.Entry<String, ? extends Number> entry
                 : featureVector.entrySet()) {
            String feature = entry.getKey();
            int id = table.symbolToID(feature);
            if (id < 0)
                continue; // ignore unknown
            Number val = entry.getValue();
            vectorMap.put(Integer.valueOf(id), val);
        }
        if (addIntercept)
            vectorMap.put(Integer.valueOf(0), 1.0);
        return new SparseFloatVector(vectorMap, numDimensions);
    }

}