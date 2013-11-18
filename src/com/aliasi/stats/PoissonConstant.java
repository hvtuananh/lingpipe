package com.aliasi.stats;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A <code>PoissonConstant</code> implements a Poisson
 * distribution with a fixed mean. Constant distributions may be
 * compiled to an object output; the distribution read back in will
 * also be a constant Poisson distribution.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class PoissonConstant extends PoissonDistribution {

    private final double mMean;

    /**
     * Construct a constant Poisson distribution with the specified
     * mean.
     *
     * @param mean Mean of distribution.
     * @throws IllegalArgumentException If mean is not a finite
     * positive number.
     */
    public PoissonConstant(double mean) {
    if (mean <= 0.0) {
        String msg = "Mean must be finite and strictly > 0."
        + " Found mean=" + mean;
        throw new IllegalArgumentException(msg);
    }
    mMean = mean;
    }

    /**
     * Compiles an instance of this constant Poisson distribution to
     * the specified object output.  The corresponding object read in
     * will be an instance of this class,
     * <code>PoissonConstant</code>, with the same mean
     * as this distribution.
     *
     * @param objOut Object output to which this distribution is
     * compiled.
     * @throws IOException If there is an I/O exception writing.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
    objOut.writeObject(new Externalizer(this));
    }

    /**
     * Returns the mean of this distribution, which is fixed at
     * construction time.  
     *
     * @return Mean of this Poisson distribution.
     */
    @Override
    public double mean() {
    return mMean;
    }

    private static class Externalizer extends AbstractExternalizable {
    private static final long serialVersionUID = -2824074866517957016L;
    final PoissonConstant mDistro;
    public Externalizer() { mDistro = null; }
    Externalizer(PoissonConstant distro) {
        mDistro = distro;
    }
    @Override
    public void writeExternal(ObjectOutput objOut) throws IOException {
        objOut.writeDouble(mDistro.mean());
    }
    @Override
    protected Object read(ObjectInput objIn) throws IOException {
        return new PoissonConstant(objIn.readDouble());
    }
    }

}


