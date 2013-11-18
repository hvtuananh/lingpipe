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

package com.aliasi.cluster;

import com.aliasi.util.Distance;
import com.aliasi.util.BoundedPriorityQueue;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A <code>LinkDendrogram</code> consists of a pair of sub-dendrograms
 * which are joined at a specified cost.  Although typically used in
 * the case where the sub-dendrograms have lower costs than their
 * parent dendrograms, this condition is not enforced by this
 * implementation.
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.0
 * @param <E> the type of objects being clustered
 */
public class LinkDendrogram<E> extends Dendrogram<E> {

    private final double mCost;
    private final Dendrogram<E> mDendrogram1;
    private final Dendrogram<E> mDendrogram2;

    /**
     * Construct a link dendrogram containing the specified object.
     *
     * @param dendrogram1 First dendrogram in cluster.
     * @param dendrogram2 Second dendrogram in cluster.
     * @param cost Cost of creating this dendrogram from the specified
     * dendrograms.
     * @throws IllegalArgumentException If the cost is less than
     * <code>0.0</code>.
     */
    public LinkDendrogram(Dendrogram<E> dendrogram1,
                          Dendrogram <E> dendrogram2,
                          double cost) {
        if (cost < 0.0 || Double.isNaN(cost)) {
            String msg = "Cost must be >= 0.0"
                + " Found cost=" + cost;
            throw new IllegalArgumentException(msg);
        }
        dendrogram1.setParent(this);
        dendrogram2.setParent(this);
        mDendrogram1 = dendrogram1;
        mDendrogram2 = dendrogram2;
        mCost = cost;
    }

    /**
     * Returns the cost of this dendogram.  The cost is specified at
     * construction time and is meant to indicate the proximity
     * between the elements.
     *
     * @return The proximity between the pair of component
     * dendrograms making up this dendrogram.
     */
    @Override
    public double score() {
        return mCost;
    }

    @Override
    public Set<E> memberSet() {
        HashSet<E> members = new HashSet<E>();
        addMembers(members);
        return members;
    }

    @Override
    void addMembers(Set<E> set) {
        mDendrogram1.addMembers(set);
        mDendrogram2.addMembers(set);
    }

    @Override
    void split(Collection<Set<E>> resultSet,
               BoundedPriorityQueue<Dendrogram<E>> queue) {
        queue.offer(mDendrogram1);
        queue.offer(mDendrogram2);
    }

    /**
     * Returns the first dendrogram in the linked dendrogram.  This is
     * the first dendrogram in constructor argument order, but the
     * order is irrelevant in the semantics of dendrograms as they
     * represent unordered trees.
     *
     * @return The first dendrogram linked.
     */
    public Dendrogram<E> dendrogram1() {
        return mDendrogram1;
    }

    /**
     * Returns the second dendrogram in the linked dendrogram.  This
     * is the second dendrogram in constructor argument order, but the
     * order is irrelevant in the semantics of dendrograms as they
     * represent unordered trees.
     *
     * @return The second dendrogram linked.
     */
    public Dendrogram<E> dendrogram2() {
        return mDendrogram2;
    }

    @Override
    void subpartitionDistance(LinkedList<Dendrogram<E>> stack) {
        stack.addFirst(dendrogram1());
        stack.addFirst(dendrogram2());
    }

    @Override
    int copheneticCorrelation(int i, double[] xs, double[] ys,
                              Distance<? super E> distance) {
        for (E e1 : mDendrogram1.memberSet()) {
            for (E e2 : mDendrogram2.memberSet()) {
                xs[i] = score();
                ys[i] = distance.distance(e1,e2);
                ++i;
            }
        }
        return i;
    }

    @Override
    void toString(StringBuilder sb, int depth) {
        sb.append('{');
        mDendrogram1.toString(sb,depth+1);
        sb.append('+');
        mDendrogram2.toString(sb,depth+1);
        sb.append("}:");
        sb.append(mCost);
    }
    
    @Override
    void prettyPrint(StringBuilder sb, int depth) {
        indent(sb,depth);
        sb.append(score());
        mDendrogram1.prettyPrint(sb,depth+1);
        mDendrogram2.prettyPrint(sb,depth+1);
    }

}
