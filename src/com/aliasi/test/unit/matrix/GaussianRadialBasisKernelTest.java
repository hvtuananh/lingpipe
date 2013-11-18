package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.GaussianRadialBasisKernel;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.io.IOException;

public class GaussianRadialBasisKernelTest  {

    @Test
    public void testOne() throws ClassNotFoundException, IOException {
        Vector v1 = new DenseVector(new double[] { -1, 2, 3 });
        Vector v2 = new DenseVector(new double[] { 5, -7, 9 });

        GaussianRadialBasisKernel kernel1
            = new GaussianRadialBasisKernel(2.0);

        GaussianRadialBasisKernel kernel2
            = (GaussianRadialBasisKernel)
            AbstractExternalizable
            .serializeDeserialize(kernel1);

        double expectedv1v2 = Math.exp(-2.0 * Math.sqrt(36 +81 +36));
        assertEquals(expectedv1v2,
                     kernel1.proximity(v1,v2),
                     0.0001);
        assertEquals(expectedv1v2,
                     kernel2.proximity(v2,v1),
                     0.0001);


    }

}
