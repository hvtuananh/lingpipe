package com.aliasi.test.unit.symbol;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.util.AbstractExternalizable;

public class MapSymbolTableTest  {

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorDupDetection() {
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("foo",1);
        map.put("bar",2);
        map.put("baz",3);
        map.put("bing",2);
        new MapSymbolTable(map);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorNegDetection() {
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("foo",1);
        map.put("bar",-1);
        map.put("baz",3);
        new MapSymbolTable(map);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testExc1() {
        MapSymbolTable table = new MapSymbolTable();
        table.idToSymbol(1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testExc2() {
        MapSymbolTable table = new MapSymbolTable();
        table.idToSymbol(-1);
    }

    @Test
    public void testOne() {
        MapSymbolTable table = new MapSymbolTable();
        assertEquals(0,table.numSymbols());

        assertEquals(-1,table.symbolToID("abc"));

        assertEquals(0,table.getOrAddSymbol("abc"));
        assertEquals(0,table.getOrAddSymbol("abc"));
        assertEquals(0,table.symbolToID("abc"));
        assertEquals("abc",table.idToSymbol(0));

        assertEquals(1,table.getOrAddSymbol("xyz"));
        assertEquals(1,table.getOrAddSymbol("xyz"));
        assertEquals(1,table.symbolToID("xyz"));
        assertEquals("xyz",table.idToSymbol(1));

        assertEquals(2,table.getOrAddSymbol("mno"));
        assertEquals(2,table.getOrAddSymbol("mno"));
        assertEquals(2,table.symbolToID("mno"));
        assertEquals("mno",table.idToSymbol(2));

        assertEquals(-1,table.symbolToID("jk"));
        assertEquals(-1,table.symbolToID("abcd"));
        
        
        Set<Integer> ids = table.idSet();
        assertEquals(3,table.numSymbols());
        assertEquals(ids.size(),table.numSymbols());
        assertEquals(3,ids.size());
        assertTrue(ids.contains(0));
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(2));
        
    }

    @Test
    public void testTwo() {
        MapSymbolTable table = new MapSymbolTable();
        assertEquals(0,table.numSymbols());
        table.getOrAddSymbol("a");
        assertEquals(1,table.numSymbols());
        assertEquals("a",table.idToSymbol(table.symbolToID("a")));

        assertEquals(table.symbolToID("a"),table.getOrAddSymbol("a"));
        assertEquals(1,table.numSymbols());

        table.getOrAddSymbol("b");
        assertEquals(2,table.numSymbols());

        int bId = table.symbolToID("b");
        assertEquals(bId,table.removeSymbol("b"));
        assertEquals(1,table.numSymbols());

        table.getOrAddSymbol("c");
        table.clear();
        assertEquals(0,table.numSymbols());
        assertEquals(-1,table.symbolToID("a"));
        assertEquals(-1,table.symbolToID("b"));
        assertEquals(-1,table.symbolToID("c"));
    }


    @Test
    public void testThree() throws ClassNotFoundException, IOException {
        MapSymbolTable table = new MapSymbolTable();

        int aID = table.getOrAddSymbol("a");
        assertEquals(aID,table.getOrAddSymbol("a"));
        assertEquals(aID,table.symbolToID("a"));
        assertEquals("a",table.idToSymbol(table.symbolToID("a")));

        int bID = table.getOrAddSymbol("b");

        int cdID = table.getOrAddSymbol("cd");
    
        MapSymbolTable table2
            = (MapSymbolTable)
            AbstractExternalizable.serializeDeserialize(table);

        assertEquals(3,table2.numSymbols());
        assertEquals(bID,table2.symbolToID("b"));
        assertEquals(cdID,table2.symbolToID("cd"));
    }
    
    @Test
    public void testFour() {
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("a",45);
        map.put("b",0);
        map.put("c",1);
        MapSymbolTable table = new MapSymbolTable(map);
        assertEquals(table.numSymbols(),3);
        assertEquals(45,table.symbolToID("a"));
        assertEquals(0,table.symbolToID("b"));
        assertEquals(1,table.symbolToID("c"));
        assertEquals(-1,table.symbolToID("d"));
        assertEquals(46,table.getOrAddSymbol("d"));
        
    }

}
