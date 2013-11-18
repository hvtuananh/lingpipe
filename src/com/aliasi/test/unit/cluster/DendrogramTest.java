package com.aliasi.test.unit.cluster;

import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.LeafDendrogram;
import com.aliasi.cluster.LinkDendrogram;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;

import static com.aliasi.test.unit.Asserts.assertNotEquals;

import static com.aliasi.util.CollectionUtils.asSet;
import com.aliasi.util.SmallSet;

import java.util.HashSet;
import java.util.Set;

public class DendrogramTest  {

    @Test
    public void testEquivalence() {
        Dendrogram leaf1 = new LeafDendrogram("1");
        Dendrogram leaf2 = new LeafDendrogram("2");
        Dendrogram link1_2 = new LinkDendrogram(leaf1,leaf2,1.0);

        Dendrogram leaf3 = new LeafDendrogram("3");
        Dendrogram link12_3 = new LinkDendrogram(leaf3,link1_2,2.0);

        Dendrogram leaf4 = new LeafDendrogram("4");
        Dendrogram leaf5 = new LeafDendrogram("5");
        Dendrogram link4_5 = new LinkDendrogram(leaf4,leaf5,1.5);

        Dendrogram leaf6 = new LeafDendrogram("6");
        Dendrogram link45_6 = new LinkDendrogram(leaf6,link4_5,3.0);

        Dendrogram link123_456 = new LinkDendrogram(link12_3,link45_6,4.0);


        Dendrogram leaf1_B = new LeafDendrogram("1");
        Dendrogram leaf2_B = new LeafDendrogram("2");
        Dendrogram link1_2_B = new LinkDendrogram(leaf2_B,leaf1_B,1.0);

        Dendrogram leaf3_B = new LeafDendrogram("3");
        Dendrogram link12_3_B = new LinkDendrogram(leaf3_B,link1_2_B,2.0);

        Dendrogram leaf4_B = new LeafDendrogram("4");
        Dendrogram leaf5_B = new LeafDendrogram("5");
        Dendrogram link4_5_B = new LinkDendrogram(leaf5_B,leaf4_B,1.5);

        Dendrogram leaf6_B = new LeafDendrogram("6");
        Dendrogram link45_6_B = new LinkDendrogram(leaf6_B,link4_5_B,3.0);

        Dendrogram link123_456_B = new LinkDendrogram(link45_6_B,link12_3_B, 4.0);

        assertTrue(Dendrogram.structurallyEquivalent(leaf1,leaf1));
        assertTrue(Dendrogram.structurallyEquivalent(leaf1,leaf1_B));
        assertFalse(Dendrogram.structurallyEquivalent(leaf1,leaf2_B));
        assertFalse(Dendrogram.structurallyEquivalent(leaf1,link1_2_B));

        assertTrue(Dendrogram.structurallyEquivalent(link1_2,link1_2));
        assertTrue(Dendrogram.structurallyEquivalent(link1_2,link1_2_B));
        assertFalse(Dendrogram.structurallyEquivalent(link1_2,leaf1_B));
        assertFalse(Dendrogram.structurallyEquivalent(link1_2,link12_3));
        assertFalse(Dendrogram.structurallyEquivalent(link1_2,link12_3_B));
        
        assertTrue(Dendrogram.structurallyEquivalent(link123_456,link123_456));
        assertTrue(Dendrogram.structurallyEquivalent(link123_456,link123_456_B));
        assertFalse(Dendrogram.structurallyEquivalent(link123_456,leaf1_B));
        assertFalse(Dendrogram.structurallyEquivalent(link123_456,link1_2));
        
    }

    @Test
    public void testPartition() {
        Dendrogram leaf1 = new LeafDendrogram("1");
        Dendrogram leaf2 = new LeafDendrogram("2");
        Dendrogram link1_2 = new LinkDendrogram(leaf1,leaf2,1.0);

        Dendrogram leaf3 = new LeafDendrogram("3");
        Dendrogram link12_3 = new LinkDendrogram(leaf3,link1_2,2.0);

        Dendrogram leaf4 = new LeafDendrogram("4");
        Dendrogram leaf5 = new LeafDendrogram("5");
        Dendrogram link4_5 = new LinkDendrogram(leaf4,leaf5,1.5);

        Dendrogram leaf6 = new LeafDendrogram("6");
        Dendrogram link45_6 = new LinkDendrogram(leaf6,link4_5,3.0);

        Dendrogram link123_456 = new LinkDendrogram(link12_3,link45_6,4.0);

        assertEquals(6,link123_456.size());

        assertEqualPartition(new Dendrogram[] { link12_3, link45_6},
                             link123_456.partitionK(2));

        assertEqualPartition(new Dendrogram[] { link12_3, link4_5, leaf6},
                             link123_456.partitionK(3));

        assertEqualPartition(new Dendrogram[] { link1_2, leaf3, link4_5, leaf6},
                             link123_456.partitionK(4));

        assertEqualPartition(new Dendrogram[] { link1_2, leaf3, leaf4, leaf5, leaf6},
                             link123_456.partitionK(5));

        assertEqualPartition(new Dendrogram[] { leaf1, leaf2, leaf3, leaf4, leaf5, leaf6},
                             link123_456.partitionK(6));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testPartitionExc1() {
        Dendrogram leaf1 = new LeafDendrogram("1");
        Dendrogram leaf2 = new LeafDendrogram("2");
        Dendrogram link1_2 = new LinkDendrogram(leaf1,leaf2,1.0);

        Dendrogram leaf3 = new LeafDendrogram("3");
        Dendrogram link12_3 = new LinkDendrogram(leaf3,link1_2,2.0);

        Dendrogram leaf4 = new LeafDendrogram("4");
        Dendrogram leaf5 = new LeafDendrogram("5");
        Dendrogram link4_5 = new LinkDendrogram(leaf4,leaf5,1.5);

        Dendrogram leaf6 = new LeafDendrogram("6");
        Dendrogram link45_6 = new LinkDendrogram(leaf6,link4_5,3.0);

        Dendrogram link123_456 = new LinkDendrogram(link12_3,link45_6,4.0);

        link123_456.partitionK(7);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testPartitionExc2() {
        Dendrogram leaf1 = new LeafDendrogram("1");
        Dendrogram leaf2 = new LeafDendrogram("2");
        Dendrogram link1_2 = new LinkDendrogram(leaf1,leaf2,1.0);

        Dendrogram leaf3 = new LeafDendrogram("3");
        Dendrogram link12_3 = new LinkDendrogram(leaf3,link1_2,2.0);

        Dendrogram leaf4 = new LeafDendrogram("4");
        Dendrogram leaf5 = new LeafDendrogram("5");
        Dendrogram link4_5 = new LinkDendrogram(leaf4,leaf5,1.5);

        Dendrogram leaf6 = new LeafDendrogram("6");
        Dendrogram link45_6 = new LinkDendrogram(leaf6,link4_5,3.0);

        Dendrogram link123_456 = new LinkDendrogram(link12_3,link45_6,4.0);

        link123_456.partitionK(0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testPartitionExc3() {
        Dendrogram leaf1 = new LeafDendrogram("1");
        Dendrogram leaf2 = new LeafDendrogram("2");
        Dendrogram link1_2 = new LinkDendrogram(leaf1,leaf2,1.0);

        Dendrogram leaf3 = new LeafDendrogram("3");
        Dendrogram link12_3 = new LinkDendrogram(leaf3,link1_2,2.0);

        Dendrogram leaf4 = new LeafDendrogram("4");
        Dendrogram leaf5 = new LeafDendrogram("5");
        Dendrogram link4_5 = new LinkDendrogram(leaf4,leaf5,1.5);

        Dendrogram leaf6 = new LeafDendrogram("6");
        Dendrogram link45_6 = new LinkDendrogram(leaf6,link4_5,3.0);

        Dendrogram link123_456 = new LinkDendrogram(link12_3,link45_6,4.0);

        link123_456.partitionK(-10);
    }

    private void assertEqualPartition(Dendrogram[] ds1, Set ds2) {
        HashSet expected = new HashSet();
        for (int i = 0; i < ds1.length; ++i)
            expected.add(ds1[i].memberSet());
        assertEquals(expected,ds2);
    }

    @Test
    public void testPartitionByMax() {
        Dendrogram<String> leafaa = new LeafDendrogram<String>("aa");
        Dendrogram<String> leafaaa = new LeafDendrogram<String>("aaa");
        Dendrogram<String> leafaaaaa  = new LeafDendrogram<String>("aaaaa");
        Dendrogram<String> leafbbb = new LeafDendrogram<String>("bbb");
        Dendrogram<String> leafbbbb = new LeafDendrogram<String>("bbbb");

        Dendrogram<String> link_bs = new LinkDendrogram<String>(leafbbb,leafbbbb,
                                                                    1.0);
        Dendrogram<String> link_aa_aaa = new LinkDendrogram<String>(leafaa,leafaaa,
                                                                    1.0);
        Dendrogram<String> link_as = new LinkDendrogram<String>(leafaaaaa,link_aa_aaa,
                                                                2.0);
        Dendrogram<String> dendro = new LinkDendrogram<String>(link_as,link_bs,
                                                             3.0);

        Set<Set<String>> part0 = dendro.partitionDistance(0.0);
        Set<Set<String>> part1 = dendro.partitionDistance(1.0);
        Set<Set<String>> part2 = dendro.partitionDistance(2.0);
        Set<Set<String>> part3 = dendro.partitionDistance(3.0);
        Set<Set<String>> part4 = dendro.partitionDistance(4.0);

        Set<Set<String>> eqClasses0 = new HashSet<Set<String>>();
        eqClasses0.add(leafaa.memberSet());
        eqClasses0.add(leafaaa.memberSet());
        eqClasses0.add(leafaaaaa.memberSet());
        eqClasses0.add(leafbbb.memberSet());
        eqClasses0.add(leafbbbb.memberSet());
        assertEquals(eqClasses0,part0);

        Set<Set<String>> eqClasses1 = new HashSet<Set<String>>();
        eqClasses1.add(link_aa_aaa.memberSet());
        eqClasses1.add(link_bs.memberSet());
        eqClasses1.add(leafaaaaa.memberSet());
        assertEquals(eqClasses1,part1);

        Set<Set<String>> eqClasses2 = new HashSet<Set<String>>();
        eqClasses2.add(link_bs.memberSet());
        eqClasses2.add(link_as.memberSet());
        assertEquals(eqClasses2,part2);

        Set<Set<String>> eqClasses3 = new HashSet<Set<String>>();
        eqClasses3.add(dendro.memberSet());
        assertEquals(eqClasses3,part3);
        assertEquals(eqClasses3,part4);


    }

    @Test
    public void testOne() {
        Dendrogram leaf1 = new LeafDendrogram("foo");
        assertEquals(0.0,leaf1.score(),0.0001);
        assertEquals(leaf1,leaf1.dereference());
        assertNull(leaf1.parent());

        // set behavior
        assertEquals(1,leaf1.size());
        assertTrue(leaf1.contains("foo"));
        assertEquals(leaf1.memberSet(),SmallSet.create("foo"));
    }

    @Test
    public void testTwo() {
        Dendrogram leaf1 = new LeafDendrogram("foo");
        Dendrogram leaf2 = new LeafDendrogram("bar");
        Dendrogram link1 = new LinkDendrogram(leaf1,leaf2,1.0);
        assertEquals(1.0,link1.score(),0.0001);
        assertEquals(link1,link1.dereference());
        assertEquals(link1,leaf1.dereference());
        assertEquals(link1,leaf2.dereference());
        assertEquals(link1,leaf1.parent());
        assertEquals(link1,leaf2.parent());

        // set behavior
        assertEquals(2,link1.size());
        assertTrue(link1.memberSet().contains("foo"));
        assertTrue(link1.memberSet().contains("bar"));
        assertEquals(SmallSet.create("foo","bar"),link1.memberSet());

        Dendrogram leaf3 = new LeafDendrogram("baz");
        Dendrogram link2 = new LinkDendrogram(leaf3,link1,2.0);
        assertEquals(link2,link1.parent());

        assertEquals(link2,link1.dereference());
        assertEquals(link2,link1.dereference());

        assertEquals(link2,link2.dereference());
        assertEquals(link2,link2.dereference());


        assertEquals(link2,leaf1.dereference());
        assertEquals(link2,leaf1.dereference());

        assertEquals(link2,leaf2.dereference());
        assertEquals(link2,leaf2.dereference());

        assertEquals(link2,leaf3.dereference());
        assertEquals(link2,leaf3.dereference());

        HashSet set = new HashSet();
        set.add("foo");
        set.add("bar");
        set.add("baz");
        assertEquals(set,link2.memberSet());
        
        assertNotNull(new LinkDendrogram(leaf1,leaf2,Double.POSITIVE_INFINITY));
    }


    @Test
    public void testEquals() {
        assertNotEquals(new LeafDendrogram("foo"),
                        new LeafDendrogram("foo"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstruxEx1() {
        LeafDendrogram leaf1 = new LeafDendrogram("foo");
        LeafDendrogram leaf2 = new LeafDendrogram("bar");
        new LinkDendrogram(leaf1,leaf2,-3.0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testConstruxEx2() {
        LeafDendrogram leaf1 = new LeafDendrogram("foo");
        LeafDendrogram leaf2 = new LeafDendrogram("bar");
        new LinkDendrogram(leaf1,leaf2,Double.NaN);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstruxEx3() {
        LeafDendrogram leaf1 = new LeafDendrogram("foo");
        LeafDendrogram leaf2 = new LeafDendrogram("bar");
        new LinkDendrogram(leaf1,leaf2,Double.NEGATIVE_INFINITY );

    }

    @Test
    public void testPartitionDistanceLeaf() {
        Dendrogram<String> leaf1 = new LeafDendrogram("foo");
        assertEquals(asSet(asSet("foo")),
                     leaf1.partitionDistance(1.0));
    }

    @Test
    public void testPartitionDistanceLink() {
        Dendrogram<String> leaf1 = new LeafDendrogram("foo");
        Dendrogram<String> leaf2 = new LeafDendrogram("bar");
        Dendrogram<String> link12 = new LinkDendrogram(leaf1,leaf2,2.0);
        assertEquals(asSet(asSet("foo"),asSet("bar")),
                     link12.partitionDistance(1.0));
        assertEquals(asSet(asSet("foo","bar")),
                     link12.partitionDistance(20.0));
    }

    /*
    @Test(expected=IllegalArgumentException.class)
    public void testScatterExc1() {
        Object[] labels = new Object[] { "a", "b", "c" };
        ProximityMatrix matrix = new ProximityMatrix(3);
        matrix.setValue(0,1,5.0);
        matrix.setValue(0,2,3.0);
        matrix.setValue(1,2,2.0);

        Dendrogram leafA = new LeafDendrogram("a");
        Dendrogram leafB = new LeafDendrogram("b");
        Dendrogram leafC = new LeafDendrogram("c");

        Dendrogram linkB_C = new LinkDendrogram(leafB,leafC,2.0);
        Dendrogram linkA__B_C = new LinkDendrogram(linkB_C,leafA,5.0);
        linkA__B_C.withinClusterScatter(0,matrix);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testScatterExc2() {
        Object[] labels = new Object[] { "a", "b", "c" };
        ProximityMatrix matrix = new ProximityMatrix(3);
        matrix.setValue(0,1,5.0);
        matrix.setValue(0,2,3.0);
        matrix.setValue(1,2,2.0);

        Dendrogram leafA = new LeafDendrogram("a");
        Dendrogram leafB = new LeafDendrogram("b");
        Dendrogram leafC = new LeafDendrogram("c");

        Dendrogram linkB_C = new LinkDendrogram(leafB,leafC,2.0);
        Dendrogram linkA__B_C = new LinkDendrogram(linkB_C,leafA,5.0);
        linkA__B_C.withinClusterScatter(4,matrix);
    }


    @Test
    public void testScatter() {
        Object[] labels = new Object[] { "a", "b", "c" };
        ProximityMatrix matrix = new ProximityMatrix(labels);
        matrix.setValue(0,1,5.0);
        matrix.setValue(0,2,3.0);
        matrix.setValue(1,2,2.0);

        Dendrogram leafA = new LeafDendrogram("a",1);
        Dendrogram leafB = new LeafDendrogram("b",2);
        Dendrogram leafC = new LeafDendrogram("c",3);

        Dendrogram linkB_C = new LinkDendrogram(leafB,leafC,2.0);
        Dendrogram linkA__B_C = new LinkDendrogram(linkB_C,leafA,5.0);

        assertEquals(0.0,leafA.totalScatter(matrix),0.0001);
        assertEquals(0.0,leafB.totalScatter(matrix),0.0001);
        assertEquals(0.0,leafC.totalScatter(matrix),0.0001);

        assertEquals(2.0,linkB_C.totalScatter(matrix),0.0001);
        assertEquals(10.0,linkA__B_C.totalScatter(matrix),0.0001);


        assertEquals(10.0,linkA__B_C.withinClusterScatter(1,matrix),0.0001);
        assertEquals(2.0,linkA__B_C.withinClusterScatter(2,matrix),0.0001);
        assertEquals(0.0,linkA__B_C.withinClusterScatter(3,matrix),0.0001);

        Dendrogram linkA_B = new LinkDendrogram(leafA,leafB,5.0);
        assertEquals(5.0,linkA_B.totalScatter(matrix),0.0001);

    }


    @Test
    public void testCophenetic() {
        Dendrogram leafA = new LeafDendrogram("a",1);
        Matrix cmA = leafA.copheneticMatrix(new Object[] { "a" });
        assertEquals(1,cmA.numRows());
        assertEquals(1,cmA.numColumns());
        assertEquals(0.0,cmA.value(0,0),0.0001);

        Dendrogram leafB = new LeafDendrogram("b",2);
        Dendrogram linkA_B = new LinkDendrogram(leafA,leafB,1.0);

        Dendrogram leafC = new LeafDendrogram("c",3);
        Dendrogram leafD = new LeafDendrogram("d",4);
        Dendrogram linkC_D = new LinkDendrogram(leafC,leafD,2.0);
        Dendrogram linkC_D__A_B = new LinkDendrogram(linkA_B,linkC_D,5.0);

        Dendrogram leafE = new LeafDendrogram("e",5);
        Dendrogram top = new LinkDendrogram(linkC_D__A_B,leafE,7.0);
        assertEquals(5,top.size());

        Object[] labels = new Object[] { "a", "b", "c", "d", "e" };
        Matrix cm = top.copheneticMatrix(labels);

        assertEquals(0.0,cm.value(0,0),0.0001);
        assertEquals(1.0,cm.value(0,1),0.0001);
        assertEquals(5.0,cm.value(0,2),0.0001);
        assertEquals(5.0,cm.value(0,3),0.0001);
        assertEquals(7.0,cm.value(0,4),0.0001);
        assertEquals(0.0,cm.value(1,1),0.0001);
        assertEquals(5.0,cm.value(1,2),0.0001);
        assertEquals(5.0,cm.value(1,3),0.0001);
        assertEquals(7.0,cm.value(1,4),0.0001);
        assertEquals(0.0,cm.value(2,2),0.0001);
        assertEquals(2.0,cm.value(2,3),0.0001);
        assertEquals(7.0,cm.value(2,4),0.0001);
        assertEquals(0.0,cm.value(3,3),0.0001);
        assertEquals(7.0,cm.value(3,4),0.0001);
        assertEquals(0.0,cm.value(4,4),0.0001);

        ProximityMatrix pm = new ProximityMatrix(labels);
        pm.setValue(0,1,1);
        pm.setValue(0,2,5);
        pm.setValue(0,3,7);
        pm.setValue(0,4,9);
        pm.setValue(1,2,7);
        pm.setValue(1,3,5);
        pm.setValue(1,4,9);
        pm.setValue(2,3,2);
        pm.setValue(2,4,7);
        pm.setValue(3,4,11);

        double[] xs = new double[] { cm.value(0,1),
                                     cm.value(0,2),
                                     cm.value(0,3),
                                     cm.value(0,4),
                                     cm.value(1,2),
                                     cm.value(1,3),
                                     cm.value(1,4),
                                     cm.value(2,3),
                                     cm.value(2,4),
                                     cm.value(3,4) };
        double[] ys = new double[] { pm.value(0,1),
                                     pm.value(0,2),
                                     pm.value(0,3),
                                     pm.value(0,4),
                                     pm.value(1,2),
                                     pm.value(1,3),
                                     pm.value(1,4),
                                     pm.value(2,3),
                                     pm.value(2,4),
                                     pm.value(3,4) };

        assertEquals(Statistics.correlation(xs,ys),
                     top.copheneticCorrelation(pm),
                     0.0001);
    }
    */

    

}
