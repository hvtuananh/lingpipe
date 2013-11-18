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

/**
 * Interface mapping symbols as strings to integer identifiers and
 * vice-versa.  In addition to the mapping, the symbol table provides
 * interfaces for optional operations of removing a symbol or clearing
 * the entire symbol, as well as adding a symbol to the table.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe1.0
 */
public interface SymbolTable {

    /**
     * Return the identifier corresponding to the specified symbol,
     * <code>-1</code> if the symbol does not exist.  The constant
     * <code>-1</code> is available as the value of {@link
     * #UNKNOWN_SYMBOL_ID}.
     *
     * @param symbol Symbol whose identifier is returned.
     * @return Identifier corresponding to specified symbol or
     * <code>-1</code> if the symbol does not exist.
     */
    public int symbolToID(String symbol);

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
    public String idToSymbol(int id);

    /**
     * Returns the number of symbols in this symbol table.
     *
     * @return Number of symbols in this table.
     */
    public int numSymbols();

    /**
     * Returns the identifier for the specified symbol.  If
     * the symbol is not in the table before the call to this
     * method, it is added and its identifier returned.  Optional
     * operation.
     *
     * @param symbol Symbol whose identifier is returned.
     * @return Integer identifier for specified symbol.
     * @throws UnsupportedOperationException If this operation is not
     * supproted.
     */
    public int getOrAddSymbol(String symbol);

    /**
     * Removes the specified symbol from the symbol table if
     * it was in the table and returns its identifier.  If the
     * symbol was not in the table, <code>-1</code>, or
     * {@link #UNKNOWN_SYMBOL_ID} is returned.  Optional operation.
     *
     * @param symbol Symbol to remove.
     * @return Previous identifier for the symbol, or
     * <code>-1</code> if it didn't exist.
     * @throws UnsupportedOperationException If this operation is
     * not supported by this implementation.
     */
    public int removeSymbol(String symbol);

    /**
     * Removes all the symbols from the symbol table.
     *
     * <P>If an implementing class does not allow removal, it may
     * throw an unsupported operation exception for this method.
     * OPtional operationl.
     *
     * @throws UnsupportedOperationException If this operation is
     * not supported by this implementation.
     */
    public void clear();

    /**
     * The value returned for a symbol that is not in
     * the symbol table, namely <code>-1</code>.
     */
    public static final int UNKNOWN_SYMBOL_ID = -1;




}
