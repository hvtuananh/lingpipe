package com.aliasi.test.unit.util;

import com.aliasi.util.Iterators;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;

import static com.aliasi.test.unit.Asserts.assertEqualsIterations;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;



public class IteratorsTest  {

    @Test
    public void testOne() {
        List xs = Arrays.asList(new String[0]);
        assertEqualsIterations(xs.iterator(),
                               new TrueIterator(xs.iterator()));
        assertEqualsIterations(xs.iterator(),
                               new FalseIterator(xs.iterator()));
        List ys = Arrays.asList(new String[] { "a", "b", "c" });
        assertEqualsIterations(ys.iterator(),
                               new TrueIterator(ys.iterator()));
        assertEqualsIterations(xs.iterator(),
                               new FalseIterator(ys.iterator()));
        List zs1 = Arrays.asList(new String[] { "a", "b", "c" });
        List zs1a = Arrays.asList(new String[] { "b", "c" });
        assertEqualsIterations(zs1a.iterator(),
                               new RemoveIterator(zs1.iterator(),"a"));
        List zs1b = Arrays.asList(new String[] { "a", "c" });
        assertEqualsIterations(zs1b.iterator(),
                               new RemoveIterator(zs1.iterator(),"b"));
        List zs1c = Arrays.asList(new String[] { "a", "b" });
        assertEqualsIterations(zs1c.iterator(),
                               new RemoveIterator(zs1.iterator(),"c"));


    }

    @Test(expected=UnsupportedOperationException.class)
    public void testTwo() {
        List xs = Arrays.asList(new String[] { "a", "b" });
        TrueIterator it = new TrueIterator(xs.iterator());
        assertNotNull(it.next());
        it.remove();
    }

    @Test(expected=NoSuchElementException.class)
    public void testExcThreeA() {
        List xs = Arrays.asList(new String[] { });
        TrueIterator it = new TrueIterator(xs.iterator());
        assertFalse(it.hasNext());
        it.next();
    }

    @Test(expected=NoSuchElementException.class)
    public void testExcThreeB() {
        List ys = Arrays.asList(new String[] { "a", "b" });
        TrueIterator it2 = new TrueIterator(ys.iterator());
        assertNotNull(it2.next());
        assertNotNull(it2.next());
        assertFalse(it2.hasNext());
        it2.next();
    }

    static class TrueIterator extends Iterators.Filter {
        public TrueIterator(Iterator it) {
            super(it);
        }
        @Override
        public boolean accept(Object x) {
            return true;
        }
    }

    static class FalseIterator extends Iterators.Filter {
        public FalseIterator(Iterator it) {
            super(it);
        }
        @Override
        public boolean accept(Object x) {
            return false;
        }
    }

    static class RemoveIterator extends Iterators.Filter {
        private final String mX;
        public RemoveIterator(Iterator it, String x) {
            super(it);
            mX = x;
        }
        @Override
        public boolean accept(Object x) {
            return !x.equals(mX);
        }
    }

    @Test
    public void testBufferedOne() {
        List xs = Arrays.asList(new String[0]);
        Iterator it = new ListBufferedIterator(xs.iterator());
        assertEqualsIterations(xs.iterator(),it);

        List ys = Arrays.asList(new String[] { "a" });
        Iterator it2 = new ListBufferedIterator(ys.iterator());
        assertEqualsIterations(ys.iterator(),it2);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testBufferedTwo() {
        List xs = Arrays.asList(new String[0]);
        Iterator it = new ListBufferedIterator(xs.iterator());
        it.remove();
    }

    @Test(expected=NoSuchElementException.class)
    public void testBufferedThreeExcA() {
        List xs = Arrays.asList(new String[0]);
        Iterator it = new ListBufferedIterator(xs.iterator());
        it.next();
    }

    @Test(expected=NoSuchElementException.class)
    public void testBufferedThreeExcB() {
        List ys = Arrays.asList(new String[] { "a", "b", "c" });
        Iterator it2 = new ListBufferedIterator(ys.iterator());
        assertNotNull(it2.next());
        assertNotNull(it2.next());
        assertNotNull(it2.next());
        it2.next();
    }

    public static class ListBufferedIterator extends Iterators.Buffered {
        private final Iterator mIterator;
        public ListBufferedIterator(Iterator iterator) {
            mIterator = iterator;
        }
        @Override
        public Object bufferNext() {
            if (!mIterator.hasNext()) return null;
            while (mIterator.hasNext()) {
                Object next = mIterator.next();
                if (next != null) return next;
            }
            return null;
        }
    }


    @Test(expected=IllegalStateException.class)
    public void testSIOne() {
        HashSet<Object> s1 = new HashSet<Object>();
        List<Iterator<Object>> its = Arrays.asList(s1.iterator());
        Iterator<Object> it = Iterators.sequence(its);
        assertIteration(it,new Object[] { });
        it.remove();
    }


    @Test(expected=IllegalStateException.class)
    public void testSIOneB() {
        HashSet<Object> s1 = new HashSet<Object>();
        HashSet<Object> s2 = new HashSet<Object>();
        Iterator<Object> it2 = Iterators.sequence(Arrays.asList(s1.iterator(),
                                                                s2.iterator()));
        assertIteration(it2,new Object[] { });
        it2.remove();
    }

    
    @Test(expected=IllegalStateException.class)
    public void testSIOneC() {
        HashSet<Object> s1 = new HashSet<Object>();
        HashSet<Object> s2 = new HashSet<Object>();
        Iterator<Object> it3 = Iterators.sequence(Arrays.asList(s1.iterator(),
                                                                s2.iterator()));
        assertIteration(it3,new Object[] { });
        it3.remove();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRemove() {
        List<String> xs = Arrays.asList(new String[] { "a", "b" });
        List<String> ixs = Collections.unmodifiableList(xs); 
        Iterator<String> it2 = Iterators.sequence(Arrays.asList(ixs.iterator()));
        it2.next();
        it2.remove();
    }

    @Test(expected=IllegalStateException.class)
    public void testSITwo() {
        HashSet<String> s1 = new HashSet<String>();
        s1.add("a");
        Iterator<String> it = Iterators.sequence(Arrays.asList(s1.iterator()));
        assertIteration(it,new Object[] { "a" });
        it.remove();
        assertEquals(Collections.EMPTY_SET,s1);
        it.remove();
    }

    @Test
    public void testSITwoB() {
        List<String> xs = Arrays.asList(new String[] { "a", "b" });
        List<String> ixs = Collections.unmodifiableList(xs); 
        Iterator<String> it2 = Iterators.sequence(Arrays.asList(ixs.iterator()));
        assertIteration(it2,new Object[] { "a", "b" });

        ArrayList<String> xs2 = new ArrayList<String>();
        xs2.add("a");
        xs2.add("b");
        Iterator<String> it3 = Iterators.sequence(Arrays.asList(xs2.iterator()));
        assertIteration(it3,new Object[] { "a", "b" });
        it3.remove();
        ArrayList<String> xs2expected = new ArrayList<String>();
        xs2expected.add("a");
        assertEquals(xs2expected,xs2);

        ArrayList<String> xs3 = new ArrayList<String>();
        xs3.add("a");
        xs3.add("b");
        Iterator<String> it4 = Iterators.sequence(Arrays.asList(xs3.iterator()));
        it4.next();
        it4.remove();
        ArrayList<String> xs3expected = new ArrayList<String>();
        xs3expected.add("b");
        assertEquals(xs3expected,xs3);
    }

    @Test
    public void testSIThree() {
        ArrayList<String> ab = new ArrayList<String>();
        ab.add("a");
        ab.add("b");
        ArrayList<String> c = new ArrayList<String>();
        c.add("c");
        ArrayList<String> de = new ArrayList<String>();
        de.add("d");
        de.add("e");
        Iterator<String> it = Iterators.sequence(Arrays.asList(ab.iterator(),
                                                               c.iterator(),
                                                               de.iterator()));
        assertIteration(it,new Object[] { "a", "b", "c", "d", "e" });
        
    }

    @Test
    public void testSIFour() {
        ArrayList<String> ab = new ArrayList<String>();
        ab.add("a");
        ab.add("b");
        ArrayList<String> empty = new ArrayList<String>();
        ArrayList<String> cde = new ArrayList<String>();
        cde.add("c");
        cde.add("d");
        cde.add("e");

        assertIteration(Iterators.sequence(Arrays.asList(empty.iterator(),
                                                         ab.iterator(), 
                                                         cde.iterator())),
                        new Object[] { "a", "b", "c", "d", "e" });

        assertIteration(Iterators.sequence(Arrays.asList(
                                                 ab.iterator(), 
                                                 empty.iterator(), 
                                                 cde.iterator())),
                        new Object[] { "a", "b", "c", "d", "e" });

        assertIteration(Iterators.sequence(Arrays.asList(
                                                 ab.iterator(), 
                                                 cde.iterator(),
                                                 empty.iterator())),
                        new Object[] { "a", "b", "c", "d", "e" });


        assertIteration(Iterators.sequence(Arrays.asList(
                                                 empty.iterator(),
                                                 empty.iterator(),
                                                 ab.iterator(), 
                                                 empty.iterator(),
                                                 empty.iterator(),
                                                 cde.iterator(),
                                                 empty.iterator(),
                                                 empty.iterator())),
                        new Object[] { "a", "b", "c", "d", "e" });

        
    }

    @Test
    public void testSIFive() {
        ArrayList<String> ab = new ArrayList<String>();
        ab.add("a");
        ab.add("b");
        ArrayList<String> cd = new ArrayList<String>();
        cd.add("c");
        cd.add("d");
        Iterator<String> it = Iterators.sequence(Arrays.asList(
                                                               ab.iterator(), cd.iterator()));
        it.next();
        it.next();
        it.remove();
        ArrayList<String> a = new ArrayList<String>();
        a.add("a");
        assertEquals(a,ab);
        it.next();
        it.remove();
        ArrayList<String> d = new ArrayList<String>();
        d.add("d");
        assertEquals(d,cd);
    }

    @Test
    public void testSISix() {
        ArrayList<String> empty = new ArrayList<String>();
        ArrayList<String> ab = new ArrayList<String>();
        ab.add("a");
        ab.add("b");
        ArrayList<String> cd = new ArrayList<String>();
        cd.add("c");
        cd.add("d");
        Iterator<String> it = Iterators.sequence(Arrays.asList(
                                                               ab.iterator(), empty.iterator(),
                                                               empty.iterator(), cd.iterator()));
        it.next();
        it.next();
        it.remove();
        ArrayList<String> a = new ArrayList<String>();
        a.add("a");
        assertEquals(a,ab);
        it.next();
        it.remove();
        ArrayList<String> d = new ArrayList<String>();
        d.add("d");
        assertEquals(d,cd);
    }


    public void assertIteration(Iterator<?> it,
                                Object[] values) {
        for (Object val : values) {
            assertTrue(it.hasNext());
            assertEquals(val,it.next());
        }
        assertFalse(it.hasNext());
    }
    


    @Test
    public void testArrayIterators() {
        assertIteration(new Integer[] { });
        assertIteration(new Integer[] { Integer.valueOf(0) });
        assertIteration(new Integer[] { Integer.valueOf(0),
                                        Integer.valueOf(1) });
        assertIteration(new Integer[] { Integer.valueOf(0),
                                        Integer.valueOf(1),
                                        Integer.valueOf(2)  });
        assertIteration(new Integer[] { Integer.valueOf(0),
                                        Integer.valueOf(1),
                                        Integer.valueOf(2),
                                        Integer.valueOf(3) });

        Object[] xs = new Object[] { "a", "b", "c" };
        assertNotNull(xs[1]);
        Iterator it = Iterators.array(xs);
        it.next();
        it.next();
        it.remove();
        assertNull(xs[1]);

        Object[] ys = new Object[] { "a" };
        assertNotNull(ys[0]);
        it = Iterators.array(ys);
        boolean threw = false;
        try {
            it.remove();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        it.next();
        it.remove();
        try {
            it.remove();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    public void assertIteration(Object[] objs) {
        Iterator it = Iterators.array(objs);
        for (int i = 0; i < objs.length; ++i) {
            assertEquals(objs[i],it.next());
        }
        assertFalse(it.hasNext());
        boolean threw = false;
        try {
            it.next();
        } catch (NoSuchElementException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test(expected=NoSuchElementException.class)
    public void testSliceIteratorA() {
        Object[] xs = new Object[] { };
        assertSliceIterator(xs,0,0);
    }

    @Test(expected=NoSuchElementException.class)
    public void testSliceIteratorB() {
        Object[] ys = new Object[] { "A" };
        assertSliceIterator(ys,0,1);
    }

    @Test(expected=NoSuchElementException.class)
    public void testSliceIteratorC() {
        Object[] zs = new Object[] { "A", "B", "C", "D" };
        assertSliceIterator(zs,0,4);
    }

    @Test(expected=NoSuchElementException.class)
    public void testSliceIteratorD() {
        Object[] zs = new Object[] { "A", "B", "C", "D" };
        assertSliceIterator(zs,2,2);
    }

    @Test(expected=NoSuchElementException.class)
    public void testSliceIteratorE() {
        Object[] zs = new Object[] { "A", "B", "C", "D" };
        assertSliceIterator(zs,0,2);
    }

    @Test(expected=NoSuchElementException.class)
    public void testSliceIteratorF() {
        Object[] zs = new Object[] { "A", "B", "C", "D" };
        assertSliceIterator(zs,2,1);
    }

    // expected to throw NoSuchElementException after finishing
    public void assertSliceIterator(Object[] objs, int start, int length) {
        Iterator it = Iterators.arraySlice(objs,start,length);
        for (int i = 0; i < length; ++i) {
            assertEquals(it.next(),objs[start+i]);
        }
        assertFalse(it.hasNext());
        it.next();  // should always throw
    }


}

