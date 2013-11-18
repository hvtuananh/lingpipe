package com.aliasi.test.unit.features;

import com.aliasi.util.FeatureExtractor;

import java.io.Serializable;

import java.util.Map;
import java.util.HashMap;

public class SerializableMockFeatureExtractor
    implements FeatureExtractor<Integer>, Serializable {

    final Map<Integer,Map<String,? extends Number>> mMap = new HashMap<Integer,Map<String,? extends Number>>();

    public Map<String,? extends Number> features(Integer in) {
        Map<String,? extends Number> feats = mMap.get(in);
        return feats == null
            ? new HashMap<String,Double>()
            : feats;
    }

    public void put(int e, Map<String,? extends Number> features) {
        mMap.put(e,features);
    }

    public void put(int e, String[] feats, double[] vals) {
        if (feats.length != vals.length) 
            throw new IllegalArgumentException();
        put(e,toMap(feats,vals));
    }

    static Map<String,? extends Number> toMap(String[] feats, double[] vals) {
        Map<String,Number> result = new HashMap<String,Number>();
        for (int i = 0; i < feats.length; ++i)
            result.put(feats[i],vals[i]);
        return result;
    }

}
    

