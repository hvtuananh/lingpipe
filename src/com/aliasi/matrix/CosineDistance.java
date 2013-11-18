/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.matrix;

import com.aliasi.util.Proximity;

import java.io.Serializable;

/**
 * The <code>CosineDistance</code> class implements proximity as
 * vector cosine.  Distance is defined as one minus the proximity.
 *
 * <p>The vector cosine operation is defined in the vector interface,
 * {@link Vector#cosine(Vector)}
 *
 * @author  Bob Carpenter
 * @version 3.1.3
 * @since   LingPipe3.1
 */
public class CosineDistance
    implements Proximity<Vector>,
               Serializable {

    static final long serialVersionUID = -8456511197031445244L;

    /**
     * A constant for the cosine distance.  Because the distance
     * function is thread safe, this single instance may be
     * used wherever the cosine distance is needed.
     */
    public static final CosineDistance DISTANCE
        = new CosineDistance();

    /**
     * Construct a cosine proximity.
     */
    public CosineDistance() {
        /* empty constructor */
    }

    /**
     * Returns one minus the proximity of the vectors.
     *
     * <pre>
     *    distance(v1,v2) = 1.0 - proximity(v1,v2)</pre>
     *
     * With this definition, distances run between 0 and 2,
     * with identical vectors being at distance 0, orthogonal
     * vectors at distance 1 and oppositive vectors at distance 2.
     *
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return The negative cosine between the first and second vector.
     */
    public double distance(Vector v1, Vector v2) {
        return 1.0 / (1.0 + proximity(v1,v2));
    }

    /**
     * Returns the cosine between the specified vectors.  The
     * proximity will be 1 if the vectors are identical in
     * direction, 0 if they are orthogonal, and -1 if they
     * are in opposite directions.
     *
     * @param v1 First vector.
     * @param v2 Second vector.
     * @return The cosine between the first and second vector.
     */
    public double proximity(Vector v1, Vector v2) {
        return v1.cosine(v2);
    }

}