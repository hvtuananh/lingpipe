package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.HyperbolicTangentKernel;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.io.IOException;

public class HyperbolicTangentKernelTest  {

    @Test
    public void testOne() throws ClassNotFoundException, IOException {
        Vector v1 = new DenseVector(new double[] { -1, 2, 3 });
        Vector v2 = new DenseVector(new double[] { 5, -7, 9 });

        HyperbolicTangentKernel kernel1
            = new HyperbolicTangentKernel(2.0,3.0);

        HyperbolicTangentKernel kernel2
            = (HyperbolicTangentKernel)
            AbstractExternalizable
            .serializeDeserialize(kernel1);

        double dotProduct = 27 - 14 - 5;
        double basis = 2.0 + 3.0 * dotProduct;
        double expectedv1v2 = Math.tanh(basis);
        assertEquals(expectedv1v2,
                     kernel1.proximity(v1,v2),
                     0.0001);
        assertEquals(expectedv1v2,
                     kernel2.proximity(v2,v1),
                     0.0001);


    }

}
