package com.aliasi.test.unit.features;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.aliasi.features.BoundedFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import java.io.IOException;
import java.io.NotSerializableException;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class BoundedFeatureExtractorTest {

    @Test
    public void testBounds() {
        ObjectToDoubleMap<String> feats1 = new ObjectToDoubleMap<String>();
        feats1.put("A",10.0);
        feats1.put("B",2.0);
        feats1.put("C",-50.0);

        MockFeatureExtractor mfe = new MockFeatureExtractor();
        mfe.put(1,feats1);

        FeatureExtractor<Integer> boundFe = new BoundedFeatureExtractor(mfe,1.0,3.0);
        Map<String,? extends Number> feats = boundFe.features(1);
        assertEquals(3.0,feats.get("A"));
        assertEquals(2.0,feats.get("B"));
        assertEquals(1.0,feats.get("C"));
    }

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        ObjectToDoubleMap<String> feats1 = new ObjectToDoubleMap<String>();
        feats1.put("A",10.0);
        feats1.put("B",2.0);
        feats1.put("C",-50.0);

        SerializableMockFeatureExtractor mfe = new SerializableMockFeatureExtractor();
        mfe.put(1,feats1);

        BoundedFeatureExtractor<Integer> boundFe 
            = new BoundedFeatureExtractor<Integer>(mfe,1.0,3.0);

        FeatureExtractor<Integer> boundFe2
            = (FeatureExtractor<Integer>) AbstractExternalizable.serializeDeserialize(boundFe);

        Map<String,? extends Number> feats = boundFe2.features(1);

        assertEquals(3.0,feats.get("A"));
        assertEquals(2.0,feats.get("B"));
        assertEquals(1.0,feats.get("C"));
    }

    @Test(expected = NotSerializableException.class)
    public void testUnSerialize() throws IOException, ClassNotFoundException {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        BoundedFeatureExtractor<Integer> boundFe = new BoundedFeatureExtractor<Integer>(mfe,1.0,3.0);
        AbstractExternalizable.serializeDeserialize(boundFe);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testE1() {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        new BoundedFeatureExtractor<Integer>(mfe,1.0,0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testE2() {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        new BoundedFeatureExtractor<Integer>(mfe,Double.POSITIVE_INFINITY,2.0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testE3() {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        new BoundedFeatureExtractor<Integer>(mfe,15,Double.NEGATIVE_INFINITY);
    }

}
