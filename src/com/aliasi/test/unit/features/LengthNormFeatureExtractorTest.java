package com.aliasi.test.unit.features;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import static com.aliasi.test.unit.features.MockFeatureExtractor.assertFeats;

import com.aliasi.features.LengthNormFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import java.io.IOException;
import java.io.NotSerializableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class LengthNormFeatureExtractorTest {

    @Test
    public void testCache() {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        mfe.put(1, new String[] { "A", "B" }, new double[] { 3.0, 4.0 });
        mfe.put(2, new String[] { "C" }, new double[] { -50.0 });
        mfe.put(3, new String[] { }, new double[] { });
                
        LengthNormFeatureExtractor<Integer> lnfe
            = new LengthNormFeatureExtractor<Integer>(mfe);

        assertFeats(lnfe,1,new String[] { "A", "B" }, new double[] { 3.0/5.0, 4.0/5.0 });
        assertFeats(lnfe,2,new String[] { "C" }, new double[] { -1.0 });
        assertFeats(lnfe,3,new String[] { }, new double[] { });
    }

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        SerializableMockFeatureExtractor mfe = new SerializableMockFeatureExtractor();
        mfe.put(1, new String[] { "A", "B" }, new double[] { 3.0, 4.0 });
        mfe.put(2, new String[] { "C" }, new double[] { -50.0 });
        mfe.put(3, new String[] { }, new double[] { });
                
        LengthNormFeatureExtractor<Integer> lnfe
            = new LengthNormFeatureExtractor<Integer>(mfe);

        FeatureExtractor<Integer> lnfe2
            = (FeatureExtractor<Integer>) AbstractExternalizable.serializeDeserialize(lnfe);
        
        assertFeats(lnfe2,1,new String[] { "A", "B" }, new double[] { 3.0/5.0, 4.0/5.0 });
        assertFeats(lnfe2,2,new String[] { "C" }, new double[] { -1.0 });
        assertFeats(lnfe2,3,new String[] { }, new double[] { });
                
    }

    @Test(expected = NotSerializableException.class)
    public void testUnSerialize() throws IOException, ClassNotFoundException {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        LengthNormFeatureExtractor<Integer> lnfe
            = new LengthNormFeatureExtractor<Integer>(mfe);
        AbstractExternalizable.serializeDeserialize(lnfe);
    }


}
