package com.aliasi.test.unit.features;

import com.aliasi.features.StringLengthFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.io.NotSerializableException;

import java.util.Map;

public class StringLengthFeatureExtractorTest {

    
    @Test(expected = IllegalArgumentException.class)
    public void testEx1() {
        new StringLengthFeatureExtractor();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEx2() {
        new StringLengthFeatureExtractor(2,-1);
    }

    @Test
    public void testAll() throws IOException {
        StringLengthFeatureExtractor fe
            = new StringLengthFeatureExtractor(5, 1, 2);
        
        Map<String,? extends Number> features
            = fe.features("");
        assertTrue(features.isEmpty());

        features = fe.features("a");
        assertEquals(1,features.size());
        assertEquals(1.0,features.get("LEN>=1").doubleValue(),0.0001);


        features = fe.features("abc");
        assertEquals(2,features.size());
        assertEquals(1.0,features.get("LEN>=1").doubleValue(),0.0001);
        assertEquals(1.0,features.get("LEN>=2").doubleValue(),0.0001);

        features = fe.features("abcdef");
        assertEquals(3,features.size());
        assertEquals(1.0,features.get("LEN>=1").doubleValue(),0.0001);
        assertEquals(1.0,features.get("LEN>=2").doubleValue(),0.0001);
        assertEquals(1.0,features.get("LEN>=5").doubleValue(),0.0001);


        @SuppressWarnings("unchecked")
        FeatureExtractor<CharSequence> fe2
            = (FeatureExtractor<CharSequence>)
            AbstractExternalizable.serializeDeserialize(fe);

        features = fe2.features("a");
        assertEquals(1,features.size());
        assertEquals(1.0,features.get("LEN>=1").doubleValue(),0.0001);


        features = fe2.features("abc");
        assertEquals(2,features.size());
        assertEquals(1.0,features.get("LEN>=1").doubleValue(),0.0001);
        assertEquals(1.0,features.get("LEN>=2").doubleValue(),0.0001);

        features = fe2.features("abcdef");
        assertEquals(3,features.size());
        assertEquals(1.0,features.get("LEN>=1").doubleValue(),0.0001);
        assertEquals(1.0,features.get("LEN>=2").doubleValue(),0.0001);
        assertEquals(1.0,features.get("LEN>=5").doubleValue(),0.0001);
    }

}