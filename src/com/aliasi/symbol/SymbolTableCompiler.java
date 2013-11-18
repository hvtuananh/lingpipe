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

package com.aliasi.symbol;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Strings;

import java.util.Arrays;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A <code>SymbolTableCompiler</code> collects symbols represented as
 * <code>String</code> instances and compiles them into a {@link
 * SymbolTable}.  At any point after adding symbols, the symbol table
 * can be written to a data output stream.  After being written, the
 * symbol table compiler properly implements the {@link SymbolTable}
 * interface through the result of compilation.  A static method,
 * <code>read</code>, is provided to read a symbol table from a data
 * input stream.  It works as a symbol table after a call to {@link
 * #compileTo(ObjectOutput)}.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe1.0
 */
public class SymbolTableCompiler implements Compilable, SymbolTable {

    /**
     * The set of symbols in the symbol table to be compiled.
     */
    private final Set<String> mSymbolSet = new HashSet<String>();

    /**
     * Compiled symbol table for this symbol table.  Exists after
     * compilation.
     */
    private CompiledSymbolTable mSymbolTable;

    /**
     * Construct a fresh symbol table compiler.
     */
    public SymbolTableCompiler() {
        /* do nothing */
    }

    /**
     * Returns a compiled symbol table constructed from the specified
     * list of symbols with symbol identifiers determined by array
     * position.  The array of symbols must not contain duplicate
     * symbols, but may be in any order.
     *
     * <p>The array of symbols is copied so that subsequent changes to
     * the symbol array will not affect the returned symbol table.
     *
     * <p>The returned symbol table may be serialized.
     *
     * @param symbols Array of symbols to back the symbol table.
     * @return Compiled symbol table corresponding to the array of symbols.
     * @throws IllegalArgumentException If there are duplicate symbols in
     * the array.
     */
    public static SymbolTable asSymbolTable(String[] symbols) {
        Set<String> symbolSet = new HashSet<String>();
        for (String symbol : symbols) {
            if (!symbolSet.add(symbol)) {
                String msg = "Duplicate symbol=" + symbol
                    + " Symbols=" + Arrays.asList(symbols);
                throw new IllegalArgumentException(msg);
            }
        }
        MapSymbolTable table = new MapSymbolTable(0);
        for (String symbol : symbols)
            table.getOrAddSymbolInteger(symbol);
        return MapSymbolTable.unmodifiableView(table);
    }


    /**
     * Compiles the symbol table to the specified object output.
     *
     * @param objOut Object output to which symbol table is written.
     * @throws IOException If there is an underlying I/O error.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }


    /**
     * Return the identifier corresponding to the specified symbol, or
     * <code>-1</code> if the symbol does not exist.  This method
     * should only be called after compilation; calling it before will
     * throw an illegal state exception.
     *
     * @param symbol Symbol whose identifier is returned.
     * @return Identifier corresponding to specified symbol or
     * <code>-1</code> if the symbol does not exist.
     * @throws IllegalStateException If this method is called before
     * compilation.
     */
    public int symbolToID(String symbol) {
        if (mSymbolTable == null) return -1;
        return mSymbolTable.symbolToID(symbol);
    }

    /**
     * Returns the symbols in this symbol table.
     *
     * @return The symbols in this symbol table.
     */
    public String[] symbols() {
        return mSymbolSet.<String>toArray(Strings.EMPTY_STRING_ARRAY);
    }

    /**
     * Return the symbol corresponding to the specified identifier.
     * Symbols exist for identifiers between <code>0</code> and the
     * number of symbols in the table minus one, inclusive.  Raises an
     * index out of bounds exception for identifiers out of range.
     * This method should only be called after compilation; calling it
     * before will throw an illegal state exception.
     *
     * @param id Identifier whose symbol is returned.
     * @return Symbol corresponding to the specified identifier.
     * @throws IndexOutOfBoundsException If there is no symbol for the
     * specified identifier.
     * @throws IllegalStateException If this method is called before
     * compilation.
     */
    public String idToSymbol(int id) {
        if (mSymbolTable == null)
            throw new IndexOutOfBoundsException("Symbol table not compiled");
        return mSymbolTable.idToSymbol(id);
    }

    /**
     * Returns the number of symbols in this symbol table.
     *
     * @return Number of symbols in this table.
     * @throws IllegalStateException If this method is called before
     * compilation.
     */
    public int numSymbols() {
        if (mSymbolTable == null) return -1;
        return mSymbolTable.numSymbols();
    }

    /**
     * Throws an unsupported operation exception.
     *
     * @throws UnsupportedOperationException Always.
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an unsupported operation exception.
     *
     * @param symbol Symbol is ignored.
     * @return Always throws an exception before returning a value.
     * @throws UnsupportedOperationException Always.
     */
    public int getOrAddSymbol(String symbol) {
        throw new UnsupportedOperationException();
    }

    /**
     * Add a symbol to the symbol table to be compiled.  Symbols are
     * restricted to a maximum length of {@link Short#MAX_VALUE};
     * attempts to add longer symbols will throw an
     * <code>IllegalArgumentException</code>.
     *
     * @param symbol Symbol to add.
     * @return <code>true</code> if the symbol was not already in the
     * table.
     * @throws IllegalArgumentException If the symbol is longer than
     * {@link Short#MAX_VALUE}.
     */
    public boolean addSymbol(String symbol) {
        if (symbol.length() > Short.MAX_VALUE) {
            String msg = "Symbol=" + symbol +
                " too long; max length="
                + Short.MAX_VALUE;
            throw new IllegalArgumentException(msg);
        }
        return mSymbolSet.add(symbol);
    }

    /**
     * Throws an unsupported operation exception.
     *
     * @param symbol Symbol is ignored.
     * @return Always throws an exception before returning a value.
     * @throws UnsupportedOperationException Always.
     */
    public int removeSymbol(String symbol) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a string-based representation of this
     * symbol table.
     *
     * @return A string-based representation of this
     * symbol table.
     */
    @Override
    public String toString() {
        return compile().toString();
    }

    private  CompiledSymbolTable compile() {
        if (mSymbolTable != null) return mSymbolTable;
        String[] symbols = new String[mSymbolSet.size()];
        Iterator<String> symbolIterator = mSymbolSet.iterator();
        for (int id = 0; symbolIterator.hasNext(); ++id)
            symbols[id] = symbolIterator.next();
        mSymbolTable = new CompiledSymbolTable(symbols);
        return mSymbolTable;
    }

    private static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 1065202374901852230L;
        final SymbolTableCompiler mCompiler;
        public Externalizer() {
            this(null);
        }
        public Externalizer(SymbolTableCompiler compiler) {
            mCompiler = compiler;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            mCompiler.compile().writeObj(objOut);
        }
        @Override
        public Object read(ObjectInput objIn) throws IOException {
            String[] symbols = new String[objIn.readInt()];
            for (int i = 0; i < symbols.length; ++i) {
                StringBuilder sb = new StringBuilder();
                int length = objIn.readShort();
                for (int j = 0; j < length; ++j)
                    sb.append(objIn.readChar());
                symbols[i] = sb.toString();
            }
            return new CompiledSymbolTable(symbols);
        }
    }

}
