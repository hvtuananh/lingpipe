package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.DotProductKernel;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.io.IOException;

public class DotProductKernelTest  {

    @Test
    public void testOne() throws ClassNotFoundException, IOException {
        Vector v1 = new DenseVector(new double[] { -1, 2, 3 });
        Vector v2 = new DenseVector(new double[] { 5, -7, 9 });

        DotProductKernel kernel1
            = new DotProductKernel();

        DotProductKernel kernel2
            = (DotProductKernel)
            AbstractExternalizable
            .serializeDeserialize(kernel1);

        double expectedv1v2 = 27 - 14 - 5;
        assertEquals(expectedv1v2,
                     kernel1.proximity(v1,v2),
                     0.0001);
        assertEquals(expectedv1v2,
                     kernel2.proximity(v2,v1),
                     0.0001);


    }

}
