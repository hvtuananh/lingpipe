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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A <code>MapSymbolTable</code > is a dynamic symbol table based on a
 * pair of underlying maps.  After creating a map symbol table, new
 * symbols may be added using {@link #getOrAddSymbol(String)}.
 *
 * <p>Map symbol tables are serializable.  The result of writing
 * to an object output stream and reading back in through an
 * object input stream produces an instance of this same class,
 * <code>MapSymbolTable</code>, with the same behavior as the
 * instance serialized.
 *
 * <P><i>Implementation Note:</i> This table uses a pair of
 * maps, one in each direction between symbols represented
 * by instances of <code>String</code> and identifiers
 * represented by instance of <code>Integer</code>.
 *
 * @author  Bob Carpenter
 * @author  Mike Ross
 * @version 4.0.0
 * @since   LingPipe2.0
 */
public class MapSymbolTable 
    implements Serializable, SymbolTable {

    static final long serialVersionUID = 3515814090489781415L;

    final HashMap<String,Integer> mSymbolToId = new HashMap<String,Integer>();
    final HashMap<Integer,String> mIdToSymbol = new HashMap<Integer,String>();
    private int mNextSymbol;

    /**
     * Construct an empty map symbol table.  The default first
     * symbol identifier is zero (<code>0</code>) and subsequent
     * symbols are assigned successive integer identifiers.
     */
    public MapSymbolTable() {
        this(0);
    }

    /**
     * Construct an empty map symbol table that begins allocating
     * identifiers at the specified value.  Subsequent symbols
     * will be assigned identifiers to successive identifiers.
     *
     * @param firstId Identifier of first symbol.
     */
    public MapSymbolTable(int firstId) {
        mNextSymbol = firstId;
    }
    
    /**
     * Construct a map symbol which associates symbols to identifiers
     * based on the specified map.
     *
     * @param symbolToIdMap A map from symbols to identifiers.
     * @throws IllegalArgumentException If two distinct symbols map to
     * the same identifier, or if the unknown symbol identifier, {@link
     * #UNKNOWN_SYMBOL_ID}, is used as an identifier.
     */
    public MapSymbolTable(Map<String,Integer> symbolToIdMap) {
        int maxSymbol = -1;
        for(Map.Entry<String, Integer> entry : symbolToIdMap.entrySet()) {
            String symbol = entry.getKey();
            Integer id = entry.getValue();
            if (id.intValue() == UNKNOWN_SYMBOL_ID) {
                String msg = "Symbols cannot be equal to the unknown symbol ID."
                    + " MapSymbolTable.UNKNOWN_SYMBOL_ID=" + UNKNOWN_SYMBOL_ID
                    + " found id=" + id;
                throw new IllegalArgumentException(msg);
            }
            if (mIdToSymbol.put(id,symbol) != null) {
                String msg = "Identifiers must be unique."
                    + " Found duplicate identifiers."
                    + " Identifier=" + id;
                throw new IllegalArgumentException(msg);
            }
            maxSymbol = Math.max(maxSymbol, entry.getValue());
        }
        mNextSymbol = maxSymbol+1;
        mSymbolToId.putAll(symbolToIdMap);
    }
    
    /**
     * Returns the complete set of ids in this symbol table.
     *
     * @return The set of ids for this symbol table.
     */
    public Set<Integer> idSet() {
        return Collections.<Integer>unmodifiableSet(mIdToSymbol.keySet());
    }

    /**
     * Returns the complete set of symbols in this symbol table. The
     * result is an unmodifiable view of the symbols underlying this
     * table and will change as the symbols in this table change.
     *
     * @return The set of symbols for this symbol table.
     */
    public Set<String> symbolSet() {
        return Collections.<String>unmodifiableSet(mSymbolToId.keySet());
    }

    private MapSymbolTable(ObjectInput objIn) throws IOException {
        int numEntries = objIn.readInt();
        int max = 0;
        for (int i = 0; i < numEntries; ++i) {
            String symbol = objIn.readUTF();
            Integer id = Integer.valueOf(objIn.readInt());
            max = Math.max(max,id.intValue());
            mSymbolToId.put(symbol,id);
            mIdToSymbol.put(id,symbol);
        }
        mNextSymbol = max+1;
    }


    // for serialization support
    Object writeReplace() {
        return new Externalizer(this);
    }

    public int numSymbols() {
        return mSymbolToId.size();
    }

    public int symbolToID(String symbol) {
        Integer result = symbolToIDInteger(symbol);
        return result == null ? -1 : result.intValue();
    }

    /**
     * Returns an Integer representation of the symbol if
     * it exists in the table or null if it does not.
     *
     * @param symbol Symbol whose identifer is returned.
     * @return Integer identifier for symbol, or null if it
     * does not exist.
     */
    public Integer symbolToIDInteger(String symbol) {
        return mSymbolToId.get(symbol);
    }

    /**
     * Returns the symbol for the specified identifier.
     * If the identifier has no defined symbol, an exception
     * is thrown.
     *
     * @param id Integer identifier.
     * @return The symbol for the identifier.
     * @throws IndexOutOfBoundsException If the symbol could
     * not be found in the symbol table.
     */
    public String idToSymbol(Integer id) {
        String symbol = mIdToSymbol.get(id);
        if (symbol == null) {
            String msg="Could not find id=" + id;
            throw new IndexOutOfBoundsException(msg);
        }
        return symbol;
    }

    public String idToSymbol(int id) {
        return idToSymbol(Integer.valueOf(id));
    }

    /**
     * Removes the specified symbol from the symbol table.  After the
     * symbol is removed, its identifier will not be assigned to
     * another symbol.
     *
     * @param symbol Symbol to remove.
     * @return The previous id of the symbol if it was in the table,
     * or -1 if it was not.
     */
    public int removeSymbol(String symbol) {
        int id = symbolToID(symbol);
        if (id >= 0) {
            mSymbolToId.remove(symbol);
            mIdToSymbol.remove(Integer.valueOf(id));
        }
        return id;
    }

    /**
     * Clears all of the symbols from the symbol table.  It does
     * not reset the symbol counter, so the removed identifiers
     * will not be reused.
     */
    public void clear() {
        mSymbolToId.clear();
        mIdToSymbol.clear();
    }

    /**
     * Returns the identifier for the specified symbol, adding
     * it to the symbol table if necessary.
     *
     * @param symbol Symbol to get or add to the table.
     * @return Identifier for specified symbol.
     */
    public int getOrAddSymbol(String symbol) {
        return getOrAddSymbolInteger(symbol).intValue();
    }

    /**
     * Returns the integer identifier for the specified symbol,
     * adding it to the symbol table if necessary.
     *
     * @param symbol Symbol to get or add to the table.
     * @return Identifier for specified symbol.
     */
    public Integer getOrAddSymbolInteger(String symbol) {
        Integer id = mSymbolToId.get(symbol);
        if (id != null) return id;
        Integer freshId = Integer.valueOf(mNextSymbol++);
        mSymbolToId.put(symbol,freshId);
        mIdToSymbol.put(freshId,symbol);
        return freshId;
    }

    /**
     * Returns a string-based representation of this symbol table
     * by printing the underlying identifier to symbol mapping.
     *
     * @return A string-based representation of this symbol table.
     */
    @Override
    public String toString() {
        return mIdToSymbol.toString();
    }

    /**
     * Returns a view of the specified symbol table that cannot be
     * modified.  The methods {@link #clear()}, {@link #getOrAddSymbol(String)},
     * and {@link #removeSymbol(String)} will throw unsupported operation
     * exceptions when called.  The remaining methods will delegate to
     * the specified table, to which the returned view holds a reference.
     *
     * <p>The unmodifiable view is serializable if the underlying
     * symbol table is serializable.  The symbol table read back in
     * will also be unmodifiable.
     *
     * @param table Table a view of which is returned.
     * @return An unmodifiable view of the specified table.
     */
    public static SymbolTable unmodifiableView(SymbolTable table) {
        return new UnmodifiableViewTable(table);
    }

    private static class UnmodifiableViewTable 
        implements SymbolTable, 
                   Serializable {
        static final long serialVersionUID = 3326236896411055713L;
        private final SymbolTable mSymbolTable;
        UnmodifiableViewTable(SymbolTable symbolTable) {
            mSymbolTable = symbolTable;
        }
        public void clear() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        public int getOrAddSymbol(String symbol) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        public int removeSymbol(String symbol) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        public String idToSymbol(int id) {
            return mSymbolTable.idToSymbol(id);
        }
        public int numSymbols() {
            return mSymbolTable.numSymbols();
        }
        public int symbolToID(String symbol) {
            return mSymbolTable.symbolToID(symbol);
        }
        @Override
        public String toString() {
            return mSymbolTable.toString();
        }
        Object writeReplace() {
            return new Serializer(this);
        }
        static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = -5293452773208612837L;
            final UnmodifiableViewTable mSymbolTable;
            public Serializer() {
                this(null);
            }
            public Serializer(UnmodifiableViewTable symbolTable) {
                mSymbolTable = symbolTable;
            }
            @Override
            public void writeExternal(ObjectOutput objOut) throws IOException {
                objOut.writeObject(mSymbolTable.mSymbolTable); // write embedded table
            }
            @Override
            public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
                SymbolTable symbolTable = (SymbolTable) in.readObject();
                return new UnmodifiableViewTable(symbolTable);
            }
        }
        static final String UNSUPPORTED_MSG
            = "Cannot modify the underlying symbol table from this view.";
    }

    private static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = -6040616216389802649L;
        final MapSymbolTable mSymbolTable;
        public Externalizer() { mSymbolTable = null; }
        public Externalizer(MapSymbolTable symbolTable) {
            mSymbolTable = symbolTable;
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            return new MapSymbolTable(in);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mSymbolTable.mSymbolToId.size());
            for (Map.Entry<String,Integer> entry : mSymbolTable.mSymbolToId.entrySet()) {
                objOut.writeUTF(entry.getKey());
                objOut.writeInt(entry.getValue().intValue());
            }
        }
    }






}
