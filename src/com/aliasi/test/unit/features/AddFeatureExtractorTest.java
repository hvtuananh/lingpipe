package com.aliasi.test.unit.features;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.aliasi.features.AddFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import java.io.IOException;
import java.io.NotSerializableException;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class AddFeatureExtractorTest {

    @Test
    public void testAdd() {
        MockFeatureExtractor me1 = new MockFeatureExtractor();
        me1.put(1,
                new String[] { "a", "b" },
                new double[] { 1.0, 2.0 });
        me1.put(2,
                new String[] { "a", "c" },
                new double[] { 10.0, 30.0 });
        me1.put(3,
                new String[] { "a", "d" },
                new double[] { 100.0, -1.0 });

        MockFeatureExtractor me2 = new MockFeatureExtractor();
        me2.put(1,
                new String[] { "a", "c" },
                new double[] { 10.0, 30.0 });


        MockFeatureExtractor me3 = new MockFeatureExtractor();
        me3.put(1,
                new String[] { "a", "d" },
                new double[] { 100.0, -1.0 });
        me3.put(3,
                new String[] { "a", "d" },
                new double[] { 100.0, -1.0 });
        
        List<MockFeatureExtractor> extractorList
            = new ArrayList<MockFeatureExtractor>();
        extractorList.add(me1);
        extractorList.add(me2);

        AddFeatureExtractor<Integer> fe1 = new AddFeatureExtractor<Integer>(extractorList);
        AddFeatureExtractor<Integer> fe2 = new AddFeatureExtractor<Integer>(me1,me2);
        
        Map<String,? extends Number> feats1X = fe1.features(1);
        assertEquals(11.0,feats1X.get("a").doubleValue(),0.0001);
        assertEquals(2.0,feats1X.get("b").doubleValue(),0.0001);
        assertEquals(30.0,feats1X.get("c").doubleValue(),0.0001);

        Map<String,? extends Number> feats2X = fe2.features(1);
        assertEquals(11.0,feats2X.get("a").doubleValue(),0.0001);
        assertEquals(2.0,feats2X.get("b").doubleValue(),0.0001);
        assertEquals(30.0,feats2X.get("c").doubleValue(),0.0001);
        
        extractorList.add(me3); // no effect
        assertEquals(11.0,feats1X.get("a").doubleValue(),0.0001);
        assertEquals(2.0,feats1X.get("b").doubleValue(),0.0001);
        assertEquals(30.0,feats1X.get("c").doubleValue(),0.0001);

        AddFeatureExtractor fe3 
            = new AddFeatureExtractor(extractorList);
        Map<String,? extends Number> feats3X = fe3.features(1);
        assertEquals(111.0,feats3X.get("a").doubleValue(),0.0001);
        assertEquals(2.0,feats3X.get("b").doubleValue(),0.0001);
        assertEquals(30.0,feats3X.get("c").doubleValue(),0.0001);
        assertEquals(-1.0,feats3X.get("d").doubleValue(),0.0001);
    }

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        ObjectToDoubleMap feats1 = new ObjectToDoubleMap();
        feats1.set("a",1.0);
        feats1.set("b",2.0);
        
        ObjectToDoubleMap feats2 = new ObjectToDoubleMap();
        feats2.set("a",10.0);
        feats2.set("c",30.0);

        ObjectToDoubleMap feats3 = new ObjectToDoubleMap();
        feats3.set("a",100.0);
        feats3.set("d",-1);

        SerializableMockFeatureExtractor me1 = new SerializableMockFeatureExtractor();
        me1.put(1,feats1);
        me1.put(2,feats2);
        me1.put(3,feats3);

        SerializableMockFeatureExtractor me2 = new SerializableMockFeatureExtractor();
        me2.put(1,feats2);

        AddFeatureExtractor fe1 = new AddFeatureExtractor(me1,me2);
        Map<String,? extends Number> feats1X = fe1.features(1);
        assertEquals(11.0,feats1X.get("a").doubleValue(),0.0001);
        assertEquals(2.0,feats1X.get("b").doubleValue(),0.0001);
        assertEquals(30.0,feats1X.get("c").doubleValue(),0.0001);

        @SuppressWarnings("unchecked")
        AddFeatureExtractor<Integer> fe2 = 
            (AddFeatureExtractor<Integer>) AbstractExternalizable.serializeDeserialize(fe1);

        Map<String,? extends Number> feats2X = fe2.features(1);
        assertNotNull(feats2X);
        assertEquals(11.0,feats2X.get("a").doubleValue(),0.0001);
        assertEquals(2.0,feats2X.get("b").doubleValue(),0.0001);
        assertEquals(30.0,feats2X.get("c").doubleValue(),0.0001);
        
    }

    @Test(expected = NotSerializableException.class)
    public void testNotSerialize() throws IOException, ClassNotFoundException {
        MockFeatureExtractor me1 = new MockFeatureExtractor();
        MockFeatureExtractor me2 = new MockFeatureExtractor();
        AddFeatureExtractor afe = new AddFeatureExtractor(me1,me2);
        AbstractExternalizable.serializeDeserialize(afe);
    }



}
