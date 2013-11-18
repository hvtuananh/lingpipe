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

package com.aliasi.test.unit;

import com.aliasi.util.AbstractExternalizable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Comparator;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author Bob Carpenter
 * @version 3.8
 * @since Lingpipe3.8
 */
public class Asserts {
    
    private static String EMPTY_MSG = "";

    // don't construct instances -- all functionality is static
    private Asserts() { }

    public static void assertNotSerializable(Object x) {
        if (!(x instanceof Serializable)) return;
        try {
            AbstractExternalizable.serializeDeserialize((Serializable)x);
            fail("Should have serialized");
        } catch (NotSerializableException e) {
            succeed();
        } catch (IOException e) {
            fail("Found IO exception instead of NotSerializableException");
        }
    }
    
    /**
     * Asserts that the two specified objects are not
     * both <code>null</code> and not both non-<code>null</code>
     * and equal.
     *
     * @param x First object to test.
     * @param y Second object to test.
     */
    public static void assertNotEquals(Object x, Object y) {
        assertNotEquals("",x,y);
    }

    /**
     * Asserts that the two specified objects are not
     * both <code>null</code> and not both non-<code>null</code>
     * and equal.
     *
     * @param msg Message to return for failure of assertion.
     * @param x First object to test.
     * @param y Second object to test.
     */
    public static void assertNotEquals(String msg, Object x, Object y) {
        if (x == null) {
            assertNotNull(msg,y); // if x is null, y must not be
            return;
        }
        if (y == null) return; // if x is not null, y can be
        assertFalse(msg,x.equals(y));
    }



    /**
     * Tests that the two specified arrays are not null, the same
     * length, and the element at each index is equal.
     *
     * @param xs First array to test.
     * @param ys Second array to test.
     * @param tolerance Tolerance of double equality test.
     */
    public static void assertEqualsArray(double[] xs, double[] ys,
                                         double tolerance) {
        assertEqualsArray("",xs,ys,tolerance);
    }

    public static void assertEqualsArray2D(double[][] xss, double[][] yss,
                                          double tolerance) {
        assertEquals(xss.length, yss.length);
        for (int i = 0; i < xss.length; ++i)
            assertEqualsArray(xss[i],yss[i],tolerance);
    }

    /**
     * Tests that the two specified arrays are not null, the same
     * length, and the element at each index is equal.
     *
     * @param msg Message to return if tests fial.
     * @param xs First array to test.
     * @param ys Second array to test.
     * @param tolerance Tolerance of double equality test.
     */
    public static void assertEqualsArray(String msg, double[] xs, double[] ys,
                                     double tolerance) {
        assertNotNull("Null first array. " + msg, xs);
        assertNotNull("Null second array. " + msg, ys);
        assertEquals("Arrays have different lengths. " + msg,
                     xs.length,ys.length);
        for (int i = 0; i < xs.length; ++i)
            assertEquals("row + " + i + ": " + msg,
                         xs[i],ys[i],tolerance);
    }

    /**
     * Asserts that two objects are equal and they obey the subsequent
     * requirements of the equality contract.  First, the objects must
     * be symmetrically equal, so that both <code>x.equals(y)</code>
     * and <code>y.equals(x)</code>.  In addition, their hash codes
     * must be equivalent to pass this test.
     *
     * @param x First object to test for equality.
     * @param y Second object to test for equality.
     */
    public static void assertFullEquals(Object x, Object y) {
        assertFullEquals(EMPTY_MSG,x,y);
    }

    /**
     * Asserts that two objects are not equal and they obey the
     * subsequent reqirements of the equality contract.  The objects
     * must be symmetrically not equal, so that
     * <code>!x.equals(y)</code> and <code>!y.equals(x)</code>.  Note
     * that their hash codes may be equivalent.
     *
     * @param x First object to test for inequality.
     * @param y Second object to test for inequality.
     */
    public static void assertFullNotEquals(Object x, Object y) {
        assertFullNotEquals(EMPTY_MSG,x,y);
    }


    /**
     * Asserts that the object may be serialized and deserialized
     * and the result will be fully equal to this object in the
     * sense defined by {@link #assertFullEquals}.
     *
     * @param x Object to test for serialization.
     */
    public static void assertFullSerialization(Object x) {
        assertFullSerialization(EMPTY_MSG,x);
    }


    /**
     * Tests whether the first argument is less than the second argument according to
     * its <code>Comparable.compareTo</code> method, and vice-versa.  Also tests that the first
     * and second arguments are self-equal using the same method.
     *
     * @param x Object that should be smaller.
     * @param y Object that should be bigger.
     */
    public static void assertFullLessThan(Comparable x, Comparable y) {
        assertFullLessThan(EMPTY_MSG,x,y);
    }

    /**
     * Tests whether the first argument is equal to itself according
     * to <code>java.lang.Comparable.compareTo</code>.  This also checks
     * that the comparable is not equal to a fresh object according to
     * this method, although a <code>java.lang.ClassCastException</code> is
     * allowed to be thrown.
     *
     * @param xc Comparable to test for self equality and inequality
     * with fresh object.
     */
    public static void assertComparableSelf(Comparable xc) {
        assertComparableSelf("",xc);
    }


    /**
     * Tests that the two iterators return the same elements as
     * compared by {@link #assertEquals(Object,Object)}.
     *
     * @param it1 First iterator.
     * @param it2 Second iterator.
     */
    public static void assertEqualsIterations(Iterator it1, Iterator it2) {
        assertEqualsIterations("",it1,it2);
    }

    /**
     * Tests that the two iterators return the same elements as
     * compared by {@link #assertEquals(Object,Object)}, with the
     * specified error message.
     *
     * @param msg Message to report along with the location of the
     * difference.
     * @param it1 First iterator.
     * @param it2 Second iterator.
     */
    public static void assertEqualsIterations(String msg, Iterator it1, Iterator it2) {
        for (int i = 0; it1.hasNext(); ++i) {
            assertTrue(msg + "First iterator longer",it2.hasNext());
            assertEquals(msg + "Differ on element=" + i, it1.next(),it2.next());
        }
        assertFalse(msg + "Second iterator longer", it2.hasNext());
    }

    /**
     * Asserts within a specified test case that two objects are equal
     * and they obey the subsequent requirements of the equality
     * contract.  First, the objects must be symmetrically equal, so
     * that both <code>x.equals(y)</code> and
     * <code>y.equals(x)</code>.  In addition, their hash codes must
     * be equivalent to pass this test.  Although recommend rather
     * than required by <code>java.util.Comparable</code>, if either object
     * is comparable, then its natural ordering is tested for
     * consistency with equals.
     *
     * @param msg Message prefix to print upon failure.
     * @param x First object to test for equality.
     * @param y Second object to test for equality.
     */
    public static void assertFullEquals(String msg, Object x, Object y) { 
        assertNotNull("First object null.",x);
        assertNotNull("Second object null.",y);
        assertEquals(msg+"Forward Equality Failure as Object.",x,y);
        assertEquals(msg+"Backward Equality Failure as Object.",y,x);
        assertEquals(msg+"Hash Code Equality Failure.",x.hashCode(),y.hashCode());
        assertFalse(msg+"First object equals null failure.",x.equals(null));
        assertFalse(msg+"Second object equals null failure.",y.equals(null));
        if (x instanceof Comparable) {
            Comparable xc = (Comparable)x;
            assertEquals(msg + "First object's atural ordering incompatible with equals.",
                         0,xc.compareTo(y));
        }
        if (y instanceof Comparable) {
            Comparable yc = (Comparable)y;
            assertEquals(msg + "Second object's natural ordering incompatible with equals",
                         0,yc.compareTo(x));
        }
    }

    /**
     * Asserts within a specified test case that two objects are not
     * equal and they obey the subsequent reqirements of the equality
     * contract.  The objects must be symmetrically not equal, so that
     * <code>!x.equals(y)</code> and <code>!y.equals(x)</code>.  Note
     * that their hash codes may be equivalent.  If either argument object
     * is an instance of {@link java.lang.Comparable} then its comparison result
     * must also be non-zero against the other object.
     *
     * @param msg Message prefix to print upon failure.
     * @param x First object to test for inequality.
     * @param y Second object to test for inequality.
     */
    public static void assertFullNotEquals(String msg, Object x, Object y) {
        assertNotNull("First object null.",x);
        assertNotNull("Second object null.",y);
        assertFalse(msg + "Forward Inequality Failure as Object.",x.equals(y));
        assertFalse(msg + "Backward Inequality Failure as Object.",y.equals(x));
        if (x instanceof Comparable) {
            assertComparableNotEquals(msg + "Forwards. ",(Comparable)x,y);
        }
        if (y instanceof Comparable) {
            assertComparableNotEquals(msg + "Backwards. ",(Comparable)y,x);
        }
    }

    /**
     * Asserts within a specified test case that a comparable and a second object are
     * not equal according to the comparables {@link java.lang.Comparable#compareTo} method.
     * Provides the specified message as a prefix in the case of failure.
     *
     * @param msg Message prefix to print upon failure.
     * @param xc Comparable object to test for inequality.
     * @param y Second object to test for inequality.
     */
    private static void assertComparableNotEquals(String msg, Comparable xc, 
                                                  Object y) {
        try {
            int comp = xc.compareTo(y);
            assertFalse(msg + "Compare to Equals.",comp==0);
        } catch (ClassCastException e) {
            succeed(msg);
        }
    }

    /**
     * Asserts within a specified test case that the object may be
     * serialized and deserialized and the result will be fully equal
     * to this object in the sense defined by {@link
     * #assertFullEquals}.
     *
     * @param msg Message prefix to print upon failure.
     * @param x Object to test for serialization.
     */
    public static void assertFullSerialization(String msg, Object x) {
        assertNotNull("Object null.",x);
        try {
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outBytes);
            out.writeObject(x);
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(outBytes.toByteArray()));
            Object y = in.readObject();
            assertFullEquals(msg+"After Serialization: ",x,y);
        } catch (IOException e) {
            fail("IOException during serialization: " + e);
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException during serialization: " + e);
        }
    }

    /**
     * Tests within a specified test case whether the first argument
     * is less than the second argument according to its
     * <code>Comparable.compareTo</code> method, and vice-versa.  Also
     * tests that the first and second arguments are self-equal using
     * the same method.
     *
     * @param msg Message prefix to print upon failure.
     * @param x Object that should be smaller.
     * @param y Object that should be bigger.
     */
    public static void assertFullLessThan(String msg, Comparable x, Comparable y) {
        assertNotNull(msg+"First argument null.",x);
        assertNotNull(msg+"Second argument null.",y);
        assertTrue(msg+"First argument not less than second argument via compareTo.",
                            x.compareTo(y) < 0);
        assertTrue(msg+"Second argument not greater than first argument via compareTo.",
                            y.compareTo(x) > 0);
    }

    /**
     * Tests within a specified test case whether the first argument
     * is equal to itself according to <code>java.lang.Comparable.compareTo</code>.  This also checks that the
     * comparable is not equal to a fresh object according to this
     * method, although a <code>java.lang.ClassCastException</code> is allowed to be thrown.
     *
     * @param msg Message prefix to print upon failure.
     * @param xc Comparable to test for self equality and inequality with fresh object.
     */
    public static void assertComparableSelf(String msg, Comparable xc) {
        assertNotNull(msg+"Object null.",xc);
        assertEquals(msg+"Object not equal to self via compareTo.",0,xc.compareTo(xc));
        try {
            int result = xc.compareTo(new Object());
            assertFalse(msg+"Object compares to fresh object with equality.",0==result);
        } catch (ClassCastException e) {
            succeed(msg);
        }
    }



    /**
     * Provides a test success with the default message.
     */
    public static void succeed() {
        succeed("");
    }

    /**
     * Provides a test success with the specified message for
     * this test case.
     *
     * @param msg Message prefix to print on failure.
     */
    public static void succeed(String msg) {
        assertTrue(msg,true);
    }


    /**
     * 
     * @param <E> type of objects being compared
     */
    public static <E> void assertLess(Comparator<? super E> comp,
                                      E o1, E o2) {
        assertTrue(comp.compare(o1,o2) < 0);
        assertTrue(comp.compare(o2,o1) > 0);
    }

    /**
     * 
     * @param <E> type of objects being compared
     */
    public static <E> void assertSame(Comparator<? super E> comp,
                                      E o1, E o2) {
        assertEquals(0,comp.compare(o1,o2));
    }

}

