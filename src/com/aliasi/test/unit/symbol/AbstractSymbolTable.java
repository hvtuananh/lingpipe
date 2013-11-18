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

import com.aliasi.symbol.SymbolTable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;


public abstract class AbstractSymbolTable  {

    protected void assertTwoElementTable(SymbolTable table) {
        assertNotNull(table);
        assertEquals(2,table.numSymbols());
        assertEquals("a",table.idToSymbol(0));
        assertEquals("bb",table.idToSymbol(1));
        assertEquals(0,table.symbolToID("a"));
        assertEquals(1,table.symbolToID("bb"));
        boolean threw = false;
        try {
        table.idToSymbol(2);
        } catch (IndexOutOfBoundsException e) {
        threw = true;
        }
        assertTrue(threw);
    }
    
}
