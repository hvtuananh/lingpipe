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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Arrays;

/**
 * A symbol table that is initialized with an array of symbols, either
 * directory, or through a data input stream.
 * Methods provided to write a table to a data output stream and
 * to reada  table from a data input stream.

 * <p>The binary in-file format of a symbol table is as follows.
 * <br/><br/>
 * <table cellpadding="5" border="1">
 * <tr><td width="20%"><b>Number</b></td>
 *     <td width="20%"><b>Variable</b></td>
 *     <td width="20%"><b>Type</b></td>
 *     <td width="60%"><b>Description</b></td></tr>
 * <tr><td><code>1</code></td>
 *     <td><code>numSymbols</code></td>
 *     <td><code>int</code></td>
 *     <td>Number of symbols</td></tr>
 * <tr><td rowspan=2><code>numSymbols</code></td>
 *     <td><code>numChars</code></td>
 *     <td><code>short</code></td>
 *     <td>Number of characters in next symbol</td></tr>
 * <tr><td><code>numChars * short</code></td>
 *     <td><code>char</code></td>
 *     <td>Characters for next symbol, encoded separately.</td></tr>
 * </table>
 * <br/>
 * As dictated by the use of <code>short</code> values to
 * encode lengths, the longest symbol allowable in a binary
 * symbol table will be {@link java.lang.Short#MAX_VALUE}.
 * </p>
 *
 * @author  Bob Carpenter
 * @version 3.8.1
 * @since   LingPipe1.0
 */
class CompiledSymbolTable implements SymbolTable, Serializable {

    static final long serialVersionUID = -8025428413920807070L;

    /**
     * Sorted array of symbols in the symbol table.
     */
    private final String[] mSymbols;

    /**
     * Construct a compiled symbol table from the specified symbols.
     * Makes a local copy of the symbol array for safety.  The symbols
     * are sorted in order to produce identifiers.
     *
     * @param symbols Array of symbols.
     */
    public CompiledSymbolTable(String[] symbols) {
        mSymbols = new String[symbols.length];
        System.arraycopy(symbols,0,mSymbols,0,symbols.length);
        Arrays.sort(mSymbols);
    }

    private CompiledSymbolTable(String[] symbols, boolean ignore) {
        mSymbols = symbols;
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    /**
     * Returns a string representation of the symbols in
     * order, beginning with symbol 0.
     *
     * @return String representation of this symbol table.
     */
    @Override
    public String toString() {
        return java.util.Arrays.asList(mSymbols).toString();
    }

    /**
     * Write the symbol table to a data output stream.  This method is
     * designed to write a symbol table as part of an output stream,
     * so the stream is not closed after the symbol table is written.
     * The bytes may be read in through a data input stream using the
     * static {@link #read(DataInputStream)}.  The format is described
     * in {@link #read(DataInputStream)}.
     *
     * @param out Data output stream to which the symbol table is written.
     * @throws IOException If there is an exception writing to the
     * underlying stream.
     */
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(numSymbols());
        for (int i = 0; i < numSymbols(); ++i) {
            String symbol = idToSymbol(i);
            out.writeShort(symbol.length());
            out.writeChars(symbol);
        }
    }

    void writeObj(ObjectOutput out) throws IOException {
        out.writeInt(numSymbols());
        for (int i = 0; i < numSymbols(); ++i) {
            String symbol = idToSymbol(i);
            out.writeShort(symbol.length());
            out.writeChars(symbol);
        }
    }

    /**
     * Return the identifier corresponding to the specified symbol,
     * or <code>-1</code> if the symbol does not exist.
     *
     * @param symbol Symbol whose identifier is returned.
     * @return Identifier corresponding to specified symbol or
     * <code>-1</code> if the symbol does not exist.
     */
    public int symbolToID(String symbol) {
        int result = Arrays.binarySearch(mSymbols,symbol);
        return result < 0 ? -1 : result;
    }

    /**
     * Return the symbol corresponding to the specified identifier.
     * Symbols exist for identifiers between <code>0</code> and the
     * number of symbols in the table minus one, inclusive.  Raises an
     * index out of bounds exception for identifiers out of range.
     *
     * @param id Identifier whose symbol is returned.
     * @return Symbol corresponding to the specified identifier.
     * @throws IndexOutOfBoundsException If there is no symbol for the
     * specified identifier.
     */
    public String idToSymbol(int id) {
        return mSymbols[id];
    }

    /**
     * Returns the number of symbols in this symbol table.
     *
     * @return Number of symbols in this table.
     */
    public int numSymbols() {
        return mSymbols.length;
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
     * Throws an unsupported operation exception.
     *
     * @param symbol Symbol is ignored.
     * @return Always throws an exception before returning a value.
     * @throws UnsupportedOperationException Always.
     */
    public int removeSymbol(String symbol) {
        throw new UnsupportedOperationException();
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 2115083345444042460L;
        private final CompiledSymbolTable mSymbolTable;
        public Serializer(CompiledSymbolTable symbolTable) {
            mSymbolTable = symbolTable;
        }
        public Serializer() {
            this(null);
        }
        public void writeExternal(ObjectOutput out)
            throws IOException {
            out.writeInt(mSymbolTable.mSymbols.length);
            for (String symbol : mSymbolTable.mSymbols)
                out.writeUTF(symbol);
        }
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {
            int numSymbols = in.readInt();
            String[] symbols = new String[numSymbols];
            for (int i = 0; i < numSymbols; ++i)
                symbols[i] = in.readUTF();
            return new CompiledSymbolTable(symbols,IGNORE);
        }
        static final boolean IGNORE = true;
    }



}

