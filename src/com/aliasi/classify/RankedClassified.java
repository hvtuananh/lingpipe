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

package com.aliasi.classify;

/**
 * A {@code RankedClassified} represents an object that has been
 * classified with a ranked classification.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe3.9.1
 * @param <E> the type of object classified
 */
public class RankedClassified<E> 
    extends Classified<E> {

    /**
     * Construct a classified object consisting of the specified
     * object and classification.
     *
     * @param object Object being classified.
     * @param c Classification of object.
     */
    public RankedClassified(E object, RankedClassification c) {
        super(object,c);
    }

    /**
     * Return the classification of the object.
     *
     * @return The classification.
     */
    public RankedClassification getClassification() {
        @SuppressWarnings("unchecked")
        RankedClassification result = (RankedClassification) super.getClassification();
        return result;
    }

}