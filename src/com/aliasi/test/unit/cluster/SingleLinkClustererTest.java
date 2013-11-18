package com.aliasi.test.unit.cluster;

import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.LeafDendrogram;
import com.aliasi.cluster.SingleLinkClusterer;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import com.aliasi.util.Distance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SingleLinkClustererTest  {

    static class FixedDistance<E> implements Distance<E> {
        Map<E,Map<E,Double>> mVals = new HashMap<E,Map<E,Double>>();
        public double distance(E e1, E e2) {
            if (e1.equals(e2)) return 0.0;
            Map<E,Double> m1 = mVals.get(e1);
            if (m1 == null) return Double.POSITIVE_INFINITY;
            Double v = m1.get(e2);
            if (v == null) return Double.POSITIVE_INFINITY;
            return v.doubleValue();
        }
        public void setVal(E e1, E e2, double val) {
            set(e1,e2,val);
            set(e2,e1,val);
        }
        void set(E e1, E e2, double val) {
            Map<E,Double> m1 = mVals.get(e1);
            if (m1 == null) {
                m1 = new HashMap<E,Double>();
                mVals.put(e1,m1);
            }
            m1.put(e2,Double.valueOf(val));
        }
    }


    static class TestDistance extends FixedDistance<String> {
        TestDistance() {
            setVal("A","B",1);
            setVal("A","C",2);
            setVal("A","D",7);
            setVal("A","E",5);
            setVal("B","C",3);
            setVal("B","D",8);
            setVal("B","E",6);
            setVal("C","D",5);
            setVal("C","E",9);
            setVal("D","E",4);
        }
    }

    static final Distance<String> TEST_DISTANCE 
        = new TestDistance();
    
    @Test
    public void testBoundaries() {
        // cut and paste from complete link
        SingleLinkClusterer<String> clusterer 
            = new SingleLinkClusterer<String>(TEST_DISTANCE);

        Set<String> elts0 = new HashSet<String>();
        Set<Set<String>> clusters = clusterer.cluster(elts0);
        assertEquals(0,clusters.size());

        Set<String> elts1 = new HashSet<String>();
        elts1.add("A");
        Set<Set<String>> clustering = new HashSet<Set<String>>();
        clustering.add(elts1);
        assertEquals(clustering,clusterer.cluster(elts1));
        Dendrogram<String> dendro1 = clusterer.hierarchicalCluster(elts1);
        assertTrue(dendro1 instanceof LeafDendrogram);
        assertEquals(elts1,dendro1.memberSet());
        assertEquals(0.0,dendro1.score(),0.001);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBoundariesExc() {
        SingleLinkClusterer<String> clusterer 
            = new SingleLinkClusterer<String>(TEST_DISTANCE);
        Set<String> elts0 = new HashSet<String>();
        clusterer.hierarchicalCluster(elts0);
    }


    @Test
    public void testOne() {

        SingleLinkClusterer<String> clusterer 
            = new SingleLinkClusterer<String>(TEST_DISTANCE);

        Set<String> elts = new HashSet<String>();
        elts.add("A");
        elts.add("B");
        elts.add("C");
        elts.add("D");
        elts.add("E");
        Dendrogram<String> dendro = clusterer.hierarchicalCluster(elts);
        
        Set<String> a = new HashSet<String>();
        a.add("A");
        Set<String> b = new HashSet<String>();
        b.add("B");
        Set<String> c = new HashSet<String>();
        c.add("C");
        Set<String> d = new HashSet<String>();
        d.add("D");
        Set<String> e = new HashSet<String>();
        e.add("E");
        
        Set<String> ab = new HashSet<String>();
        ab.addAll(a);
        ab.addAll(b);
        
        Set<String> abc = new HashSet<String>();
        abc.addAll(ab);
        abc.addAll(c);
        
        Set<String> de = new HashSet<String>();
        de.addAll(d);
        de.addAll(e);

        Set<String> abcde = new HashSet<String>();
        abcde.addAll(abc);
        abcde.addAll(de);

        assertEquals(abcde,dendro.memberSet());
        
        Set<Set<String>> p1 = new HashSet<Set<String>>();
        p1.add(abcde);
        assertEquals(p1,dendro.partitionK(1));

        Set<Set<String>> p2 = new HashSet<Set<String>>();
        p2.add(abc);
        p2.add(de);
        assertEquals(p2,dendro.partitionK(2));

        Set<Set<String>> p3 = new HashSet<Set<String>>();
        p3.add(abc);
        p3.add(d);
        p3.add(e);
        assertEquals(p3,dendro.partitionK(3));
        
        Set<Set<String>> p4 = new HashSet<Set<String>>();
        p4.add(ab);
        p4.add(c);
        p4.add(d);
        p4.add(e);
        assertEquals(p4,dendro.partitionK(4));

        Set<Set<String>> p5 = new HashSet<Set<String>>();
        p5.add(a);
        p5.add(b);
        p5.add(c);
        p5.add(d);
        p5.add(e);
        assertEquals(p5,dendro.partitionK(5));

        assertEquals(5.0,dendro.score(),0.001);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSlExc1() {
        SingleLinkClusterer<String> clusterer 
            = new SingleLinkClusterer<String>(TEST_DISTANCE);

        Set<String> elts = new HashSet<String>();
        elts.add("A");
        elts.add("B");
        elts.add("C");
        elts.add("D");
        elts.add("E");
        Dendrogram<String> dendro = clusterer.hierarchicalCluster(elts);
        dendro.partitionK(0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testSlExc2() {
        SingleLinkClusterer<String> clusterer 
            = new SingleLinkClusterer<String>(TEST_DISTANCE);

        Set<String> elts = new HashSet<String>();
        elts.add("A");
        elts.add("B");
        elts.add("C");
        elts.add("D");
        elts.add("E");
        Dendrogram<String> dendro = clusterer.hierarchicalCluster(elts);
        dendro.partitionK(6);
    }


}
