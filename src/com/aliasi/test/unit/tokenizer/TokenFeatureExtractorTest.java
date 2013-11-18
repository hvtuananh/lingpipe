package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.io.IOException;

import java.util.Map;

public class TokenFeatureExtractorTest  {

    @Test
    public void testOne() throws IOException, ClassNotFoundException {
        TokenFeatureExtractor extractor1
            = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.INSTANCE);

        TokenFeatureExtractor extractor2
            = (TokenFeatureExtractor)
            AbstractExternalizable.serializeDeserialize(extractor1);

        String in0 = "";
        ObjectToCounterMap<String> expectedMap0
            = new ObjectToCounterMap<String>();

        assertEx(extractor1,in0,expectedMap0);
        assertEx(extractor2,in0,expectedMap0);

        String in1 = "a a b";
        ObjectToCounterMap<String> expectedMap1
            = new ObjectToCounterMap<String>();
        expectedMap1.set("a",2);
        expectedMap1.set("b",1);

        assertEx(extractor1,in1, expectedMap1);
        assertEx(extractor2,in1, expectedMap1);

    }

    void assertEx(FeatureExtractor<CharSequence> extractor,
                  String input,
                  Map<String,? extends Number> expectedMap) {

        Map<String, ? extends Number> map = extractor.features(input);
        assertEquals(expectedMap.size(),map.size());
        for (String key : expectedMap.keySet()) {
            assertEquals(expectedMap.get(key).doubleValue(),
                         map.get(key).doubleValue(),
                         0.0001);
        }
    }


}
