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

package com.aliasi.test.unit.symbol;

import com.aliasi.symbol.SymbolTableCompiler;
import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import java.io.*;

public class SymbolTableCompilerTest extends AbstractSymbolTable {

    @Test
    public void testSerializable() throws IOException {
        String[] symbols = new String[] { "b", "a", "c", "x", "abc", "ab", "z" };
        SymbolTable table = SymbolTableCompiler.asSymbolTable(symbols);
        @SuppressWarnings("unchecked")
        SymbolTable table2
            = (SymbolTable)
            AbstractExternalizable
            .serializeDeserialize((Serializable) table);

        assertEquals(table.numSymbols(), table2.numSymbols());
        for (int i = 0; i < table.numSymbols(); ++i) {
            assertEquals(table.idToSymbol(i), table2.idToSymbol(i));
            assertEquals(table.symbolToID(table.idToSymbol(i)),
                         table2.symbolToID(table2.idToSymbol(i)));
        }


    }

    @Test
    public void testStaticFactoryOrder() {
        String[] symbols = new String[] { "b", "a" };
        SymbolTable table = SymbolTableCompiler.asSymbolTable(symbols);
        assertEquals(0,table.symbolToID("b"));
        assertEquals(1,table.symbolToID("a"));
    }

    @Test
    public void testStaticConstruction() {
        String[] symbols = new String[] { "a", "b" };
        SymbolTable table = SymbolTableCompiler.asSymbolTable(symbols);
        assertEquals(2,table.numSymbols());
        assertEquals("a",table.idToSymbol(0));
        assertEquals("b",table.idToSymbol(1));
        assertEquals(0,table.symbolToID("a"));
        assertEquals(1,table.symbolToID("b"));
        assertEquals(-1,table.symbolToID("c"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testStaticDuplicateException() {
        SymbolTableCompiler.asSymbolTable(new String[] { "a", "b", "a" });
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testStaticImmutableException1() {
        SymbolTableCompiler.asSymbolTable(new String[] { "a", "b" }).clear();
    }


    @Test(expected=UnsupportedOperationException.class)
    public void testStaticImmutableException2() {
        SymbolTableCompiler.asSymbolTable(new String[] { "foo" }).removeSymbol("foo");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testStaticImmutableException3() {
        SymbolTableCompiler.asSymbolTable(new String[] { "foo" }).getOrAddSymbol("foo");
    }


    @Test
    public void testCompilation()
        throws ClassNotFoundException, IOException {

        SymbolTableCompiler compiler = new SymbolTableCompiler();
        compiler.addSymbol("a");
        compiler.addSymbol("bb");

        SymbolTable compiledTable
            = (SymbolTable) AbstractExternalizable.compile(compiler);

        assertTwoElementTable(compiledTable);
        assertTwoElementTable(compiler);
    }


}
