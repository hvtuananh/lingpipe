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

package com.aliasi.lm;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.ObjectToCounterMap;

import com.aliasi.util.Strings;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Bob Carpenter
 * @version 3.8
 */
interface Node {
    public long count(char[] cs, int start, int end);
    public long count();
    public long contextCount(char[] cs, int start, int end);
    public int numOutcomes(char[] cs, int start, int end);
    public char[] outcomes(char[] cs, int start, int end);
    public long size();
    public Node increment(char[] cs, int start, int end);
    public Node increment(char[] cs, int start, int end, int incr);
    public Node decrement();
    public Node decrement(int count);
    // just decrements final char in path
    public Node decrement(char[] cs, int start, int end);
    public Node decrement(char[] cs, int start, int end, int count);
    public Node prune(long minCount);
    // below here is just for reporting!
    public void toString(StringBuilder sb, int depth);
    public void addCounts(List<Long> counts, int dtrLevel);
    public void topNGrams(NBestCounter counter,
                          char[] csAccum, int level, int dtrLevel);
    public void addNGramCounts(long[][] uniqueTotalCounts, int depth);
    public long uniqueNGramCount(int dtrLevel);
    public long totalNGramCount(int dtrLevel);
    public void countNodeTypes(ObjectToCounterMap<String> counter);
    public void addDaughters(LinkedList<Node> queue);
}

abstract class AbstractNode implements Node {
    public abstract void topNGramsDtrs(NBestCounter counter,
                                       char[] csAccum,
                                       int level, int dtrLevel);
    public abstract void countNodeTypes(ObjectToCounterMap<String> counter);
    public abstract long dtrUniqueNGramCount(int dtrLevel);
    public abstract long dtrTotalNGramCount(int dtrLevel);
    public abstract void addDtrCounts(List<Long> counts, int dtrLevel);
    public abstract void addDtrNGramCounts(long[][] uniqueNGramCount,
                                           int depth);
    public void addNGramCounts(long[][] uniqueTotalCounts, int depth) {
        uniqueTotalCounts[depth][0] += 1;
        uniqueTotalCounts[depth][1] += count();
        addDtrNGramCounts(uniqueTotalCounts,depth+1);
    }
    public void topNGrams(NBestCounter counter, char[] csAccum,
                          int level, int dtrLevel) {
        if (dtrLevel == 0)
            counter.put(csAccum,level,count());
        else
            topNGramsDtrs(counter,csAccum,level,dtrLevel);
    }
    public void addCounts(List<Long> counts, int dtrLevel) {
        if (dtrLevel == 0) {
            counts.add(Long.valueOf(count()));
            return;
        }
        addDtrCounts(counts,dtrLevel-1);
    }
    public long uniqueNGramCount(int dtrLevel) {
        if (dtrLevel == 0) return 1;
        return dtrUniqueNGramCount(dtrLevel-1);
    }
    public long totalNGramCount(int dtrLevel) {
        if (dtrLevel == 0) return count();
        return dtrTotalNGramCount(dtrLevel-1);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb,0);
        return sb.toString();
    }
    static void indent(StringBuilder sb, int depth) {
        sb.append('\n');
        for (int i = 0; i < depth; ++i)
            sb.append("  ");
    }
    protected static void toString(StringBuilder sb,
                                   char c, Node daughter, int depth) {
        indent(sb,depth);
        sb.append(c);
        daughter.toString(sb,depth+1);
    }
}


abstract class AbstractDtrNode extends AbstractNode {
    abstract char[] chars();
    abstract Node[] dtrs();
    abstract int numDtrs();
    Node getDtr(char c) {
        char[] cs = chars();
        int i = Arrays.binarySearch(cs,c);
        if (i < 0) return null;
        return dtrs()[i];
    }

    public int numOutcomes(char[] cs, int start, int end) {
        if (start == end) return numDtrs();
        Node dtr = getDtr(cs[start]);
        if (dtr == null) return 0;
        return dtr.numOutcomes(cs,start+1,end);
    }
    public long count(char[] cs, int start, int end) {
        if (start == end) {
            return count();
        }
        Node dtr = getDtr(cs[start]);
        if (dtr == null) return 0;
        return dtr.count(cs,start+1,end);
    }
    public long contextCount() {
        Node[] dtrs = dtrs();
        long dtrCount = 0;
        for (int i = 0; i < dtrs.length; ++i)
            dtrCount += dtrs[i].count();
        return dtrCount;
    }
    public long contextCount(char[] cs, int start, int end) {
        if (start == end) {
            return contextCount();
        }
        Node dtr = getDtr(cs[start]);
        if (dtr == null) return 0;
        return dtr.contextCount(cs,start+1,end);
    }
    public Node decrement() {
        return decrement(1);
    }
    public Node decrement(int decr) {
        return NodeFactory.createNode(chars(),dtrs(),count()-decr);
    }
    public Node decrement(char[] cs, int start, int end) {
        if (start == end)
            return decrement();
        char[] dtrCs = chars();
        int k = Arrays.binarySearch(dtrCs,cs[start]);
        if (k >= 0) {
            Node[] dtrs = dtrs();
            dtrs[k] = dtrs[k].decrement(cs,start+1,end);
            return NodeFactory.createNodePrune(dtrCs,dtrs,count());
        }
        String msg = "Could not find string to decrement="
            + new String(cs,start,end-start);
        throw new IllegalArgumentException(msg);
    }
    public Node decrement(char[] cs, int start, int end, int decr) {
        if (start == end)
            return decrement(decr);
        char[] dtrCs = chars();
        int k = Arrays.binarySearch(dtrCs,cs[start]);
        if (k >= 0) {
            Node[] dtrs = dtrs();
            dtrs[k] = dtrs[k].decrement(cs,start+1,end,decr);
            return NodeFactory.createNodePrune(dtrCs,dtrs,count());
        }
        String msg = "Could not find string to decrement="
            + new String(cs,start,end-start);
        throw new IllegalArgumentException(msg);
    }
    public Node increment(char[] cs, int start, int end) {
        return increment(cs,start,end,1);
    }
    public Node increment(char[] cs, int start, int end, int incr) {
        if (start == end)
            return NodeFactory.createNode(chars(),dtrs(),count() + incr);
        char[] dtrCs = chars();
        int k = Arrays.binarySearch(dtrCs,cs[start]);
        Node[] dtrs = dtrs();
        if (k >= 0) {
            dtrs[k] = dtrs[k].increment(cs,start+1,end,incr);
            return NodeFactory.createNode(dtrCs,dtrs,count() + incr);
        }
        char[] newCs = new char[dtrCs.length+1];
        Node[] newDtrs = new Node[dtrs.length+1];
        int i = 0;
        for (; i < dtrCs.length && dtrCs[i] < cs[start]; ++i) {
            newCs[i] = dtrCs[i];
            newDtrs[i] = dtrs[i];
        }
        newCs[i] = cs[start];
        newDtrs[i] = NodeFactory.createNode(cs,start+1,end,incr);
        for (; i < dtrCs.length; ++i) {
            newCs[i+1] = dtrCs[i];
            newDtrs[i+1] = dtrs[i];
        }
        return NodeFactory.createNode(newCs,newDtrs,count()+incr);
    }
    // below here for reporting -- don't over-optimize
    public long size() {
        Node[] dtrs = dtrs();
        long size = 1;
        for (int i = 0; i < dtrs.length; ++i)
            size += dtrs[i].size();
        return size;
    }
    @Override
    public void topNGramsDtrs(NBestCounter counter, char[] csAccum,
                              int level, int dtrLevel) {
        Node[] dtrs = dtrs();
        char[] cs = chars();
        for (int i = 0; i < dtrs.length; ++i) {
            csAccum[level] = cs[i];
            dtrs[i].topNGrams(counter,csAccum,level+1,dtrLevel-1);
        }
    }
    @Override
    public void addDtrNGramCounts(long[][] uniqueTotalCounts, int depth) {
        Node[] dtrs = dtrs();
        for (int i = 0; i < dtrs.length; ++i)
            dtrs[i].addNGramCounts(uniqueTotalCounts,depth);
    }
    @Override
    public long dtrUniqueNGramCount(int dtrLevel) {
        Node[] dtrs = dtrs();
        long sum = 0;
        for (int i = 0; i < dtrs.length; ++i)
            sum += dtrs[i].uniqueNGramCount(dtrLevel);
        return sum;
    }
    @Override
    public long dtrTotalNGramCount(int dtrLevel) {
        Node[] dtrs = dtrs();
        long sum = 0;
        for (int i = 0; i < dtrs.length; ++i)
            sum += dtrs[i].totalNGramCount(dtrLevel);
        return sum;
    }
    @Override
    public void addDtrCounts(List<Long> accum, int nGramOrder) {
        Node[] dtrs = dtrs();
        for (int i = 0; i < dtrs.length; ++i)
            dtrs[i].addCounts(accum,nGramOrder);
    }
    public void addDaughters(LinkedList<Node> queue) {
        Node[] dtrs = dtrs();
        for (int i = 0; i < dtrs.length; ++i)
            queue.addLast(dtrs[i]);
    }
    public char[] outcomes(char[] cs, int start, int end) {
        if (start == end)
            return chars();
        Node dtr = getDtr(cs[start]);
        if (dtr == null)
            return Strings.EMPTY_CHAR_ARRAY;
        return dtr.outcomes(cs,start+1,end);
    }
    @Override
    public void countNodeTypes(ObjectToCounterMap<String> counter) {
        counter.increment(this.getClass().toString());
        Node[] dtrs = dtrs();
        for (int i = 0; i < dtrs.length; ++i)
            dtrs[i].countNodeTypes(counter);
    }
    public void toString(StringBuilder sb, int depth) {
        char[] cs = chars();
        Node[] dtrs = dtrs();
        sb.append(' ');
        sb.append(count());
        for (int i = 0; i < dtrs.length; ++i)
            toString(sb,cs[i],dtrs[i],depth);
    }
    public Node prune(long minCount) {
        long count = count();
        if (count < minCount) return null;
        Node[] dtrs = dtrs();
        for (int i = 0; i < dtrs.length; ++i)
            dtrs[i] = dtrs[i].prune(minCount);
        return NodeFactory.createNodePrune(chars(),dtrs,count);
    }
}

abstract class TerminalNode extends AbstractDtrNode {
    @Override
    char[] chars() {
        return Strings.EMPTY_CHAR_ARRAY;
    }
    @Override
    Node[] dtrs() {
        return NodeFactory.EMPTY_NODES;
    }
    @Override
    public long contextCount(char[] cs, int start, int end) {
        return 0;
    }
    @Override
    public Node getDtr(char c) { return null; }
    @Override
    public int numDtrs() { return 0; }
}

abstract class OneDtrNode extends AbstractDtrNode {
    char mC;
    Node mDaughter;
    public OneDtrNode(char c, Node daughter) {
        mC = c;
        mDaughter = daughter;
    }
    @Override
    public long contextCount() {
        return mDaughter.count();
    }
    @Override
    public Node getDtr(char c) {
        return c == mC ? mDaughter : null;
    }
    @Override
    char[] chars() {
        return new char[] { mC };
    }
    @Override
    Node[] dtrs() {
        return new Node[] { mDaughter };
    }
    @Override
    public int numDtrs() { return 1; }
}

abstract class TwoDtrNode extends AbstractDtrNode {
    char mC1;
    Node mDaughter1;
    char mC2;
    Node mDaughter2;
    public TwoDtrNode(char c1, Node daughter1,
                      char c2, Node daughter2) {
        mC1 = c1;
        mDaughter1 = daughter1;
        mC2 = c2;
        mDaughter2 = daughter2;
    }
    @Override
    public long contextCount() {
        return mDaughter1.count()
            + mDaughter2.count();
    }
    @Override
    public Node getDtr(char c) {
        return c == mC1
            ? mDaughter1
            : ( c == mC2
                ? mDaughter2
                : null );
    }
    @Override
    char[] chars() {
        return new char[] { mC1, mC2 };
    }
    @Override
    Node[] dtrs() {
        return new Node[] { mDaughter1, mDaughter2 };
    }
    @Override
    public int numDtrs() { return 2; }
}

abstract class ThreeDtrNode extends AbstractDtrNode {
    char mC1;
    Node mDaughter1;
    char mC2;
    Node mDaughter2;
    char mC3;
    Node mDaughter3;
    public ThreeDtrNode(char c1, Node daughter1,
                        char c2, Node daughter2,
                        char c3, Node daughter3) {
        mC1 = c1;
        mDaughter1 = daughter1;
        mC2 = c2;
        mDaughter2 = daughter2;
        mC3 = c3;
        mDaughter3 = daughter3;
    }
    @Override
    public long contextCount() {
        return mDaughter1.count()
            + mDaughter2.count()
            + mDaughter3.count();
    }
    @Override
    public Node getDtr(char c) {
        return c == mC1
            ? mDaughter1
            : ( c == mC2
                ? mDaughter2
                : ( c == mC3
                    ? mDaughter3
                    : null ) );
    }
    @Override
    char[] chars() {
        return new char[] { mC1, mC2, mC3 };
    }
    @Override
    Node[] dtrs() {
        return new Node[] { mDaughter1, mDaughter2, mDaughter3 };
    }
    @Override
    public int numDtrs() { return 3; }
}

abstract class ArrayDtrNode extends AbstractDtrNode {
    char[] mCs;
    Node[] mDtrs;
    public ArrayDtrNode(char[] cs, Node[] daughters) {
        mCs = cs;
        mDtrs = daughters;
    }
    @Override
    char[] chars() {
        return mCs;
    }
    @Override
    Node[] dtrs() {
        return mDtrs;
    }
    @Override
    public int numDtrs() { return mDtrs.length; }
}

abstract class AbstractPATNode extends AbstractNode {
    abstract char[] chars();
    abstract int length();
    public Node prune(long minCount) {
        return count() < minCount ? null : this;
    }
    public long count(char[] cs, int start, int end) {
        return match(cs,start,end)
            ? count()
            : 0;
    }
    public long contextCount(char[] cs, int start, int end) {
        return properSubMatch(cs,start,end) ? count() : 0;
    }
    boolean match(char[] cs, int start, int end) {
        if ((end-start) > length()) return false;
        return stringMatch(cs,start,end);
    }
    boolean properSubMatch(char[] cs, int start, int end) {
        if ((end-start) >= length()) return false;
        return stringMatch(cs,start,end);
    }
    abstract boolean stringMatch(char[] cs, int start, int end);
    @Override
    public void addDtrNGramCounts(long[][] uniqueTotalCounts, int depth) {
        int patDepth = chars().length;
        long count = count();
        for (int i = 0; i < patDepth; ++i) {
            uniqueTotalCounts[depth+i][0] += 1;
            uniqueTotalCounts[depth+i][1] += count;
        }
    }
    @Override
    public void topNGramsDtrs(NBestCounter counter, char[] csAccum,
                              int level, int dtrLevel) {
        char[] patCs = chars();
        if (dtrLevel > patCs.length) return;
        for (int i = 0; i < dtrLevel; ++i)
            csAccum[level+i] = patCs[i];
        counter.put(csAccum,level+dtrLevel,count());
    }
    @Override
    public void addDtrCounts(List<Long> accum, int nGramOrder) {
        char[] patCs = chars();
        if (nGramOrder < patCs.length)
            accum.add(Long.valueOf(count()));
    }
    public int numOutcomes(char[] cs, int start, int end) {
        return properSubMatch(cs,start,end) ? 1 : 0;
    }
    public Node increment(char[] cs, int start, int end) {
        return increment(cs,start,end,1);
    }
    public Node increment(char[] cs, int start, int end, int incr) {
        char[] patCs = chars();
        long count = count();
        if ((patCs.length == (end-start)) && match(cs,start,end)) {
            return NodeFactory.createNode(patCs,0,patCs.length,count+incr);
        }
        Node tailNode = NodeFactory.createNode(patCs,1,patCs.length,count);
        // can unfold OneDtrNode's increment into here;
        // eventually becomes loop of matching w. one-dtr nodes
        // until a split and a two-dtr node is created
        Node newNode = NodeFactory.createNode(patCs[0],tailNode,count);
        return newNode.increment(cs,start,end,incr);
    }
    public Node decrement(char[] cs, int start, int end) {
        if (end == start) return decrement();
        char[] patCs = chars();
        long count = count();
        Node tailNode = NodeFactory.createNode(patCs,1,patCs.length,count);
        // can unfold OneDtrNode's increment into here;
        // eventually becomes loop of matching w. one-dtr nodes
        // until a split and a two-dtr node is created
        Node newNode = NodeFactory.createNode(patCs[0],tailNode,count);
        return newNode.decrement(cs,start,end);
    }
    public Node decrement(char[] cs, int start, int end, int decr) {
        if (end == start) return decrement(decr);
        char[] patCs = chars();
        long count = count();
        Node tailNode = NodeFactory.createNode(patCs,1,patCs.length,count);
        // can unfold OneDtrNode's increment into here;
        // eventually becomes loop of matching w. one-dtr nodes
        // until a split and a two-dtr node is created
        Node newNode = NodeFactory.createNode(patCs[0],tailNode,count);
        return newNode.decrement(cs,start,end,decr);
    }
    public Node decrement() {
        long count = count();
        if (count == 0L) return this;
        char[] patCs = chars();
        Node tailNode
            = NodeFactory.createNode(patCs,1,patCs.length,count);
        return NodeFactory.createNode(patCs[0],tailNode,count-1);
    }
    public Node decrement(int decr) {
        long count = count();
        long decrL = Math.min(count,decr); // don't go below 0
        char[] patCs = chars();
        Node tailNode
            = NodeFactory.createNode(patCs,1,patCs.length,count-decrL);
        return NodeFactory.createNode(patCs[0],tailNode,count-decrL);
    }


    public long size() {
        return chars().length + 1;
    }
    public char[] outcomes(char[] cs, int start, int end) {
        char[] patCs = chars();
        for (int i = 0; i < patCs.length; ++i) {
            if (start+i == end)
                return new char[] { patCs[i] };
            if (patCs[i] != cs[start+i])
                return Strings.EMPTY_CHAR_ARRAY;
        }
        return Strings.EMPTY_CHAR_ARRAY; // ran off end of PAT
    }
    @Override
    public long dtrUniqueNGramCount(int dtrLevel) {
        return dtrLevel < chars().length ? 1 : 0;
    }
    @Override
    public long dtrTotalNGramCount(int dtrLevel) {
        return dtrLevel < chars().length ? count() : 0;
    }
    public void addDaughters(LinkedList<Node> queue) {
        char[] patCs = chars();
        Node tailNode = NodeFactory.createNode(patCs,1,patCs.length,count());
        queue.add(tailNode);
    }
    public void toString(StringBuilder sb, int depth) {
        sb.append(new String(chars()));
        sb.append(' ');
        sb.append(count());
    }
    @Override
    public void countNodeTypes(ObjectToCounterMap<String> counter) {
        counter.increment(this.getClass().toString());
    }
}

abstract class PAT1Node extends AbstractPATNode {
    char mC;
    PAT1Node(char c) {
        mC = c;
    }
    @Override
    char[] chars() {
        return new char[] { mC };
    }
    @Override
    int length() { return 1; }
    // cascade without break is intentional; checks all way down
    @Override
    @SuppressWarnings("fallthrough")
    boolean stringMatch(char[] cs, int start, int end) {
        switch (end-start) {
        case 1: if (cs[start] != mC) return false;
        default: return true;
        }
    }
}

abstract class PAT2Node extends AbstractPATNode {
    char mC1;
    char mC2;
    PAT2Node(char c1, char c2) {
        mC1 = c1;
        mC2 = c2;
    }
    @Override
    char[] chars() {
        return new char[] { mC1, mC2 };
    }
    @Override
    int length() { return 2; }
    // cascade without break is intentional; checks all way down
    @Override
    @SuppressWarnings("fallthrough")
    boolean stringMatch(char[] cs, int start, int end) {
        switch (end-start) {
        case 2: if (cs[start+1] != mC2) return false;
        case 1: if (cs[start] != mC1) return false;
        default: return true;
        }
    }
}

abstract class PAT3Node extends AbstractPATNode {
    char mC1;
    char mC2;
    char mC3;
    PAT3Node(char c1, char c2, char c3) {
        mC1 = c1;
        mC2 = c2;
        mC3 = c3;
    }
    @Override
    char[] chars() {
        return new char[] { mC1, mC2, mC3 };
    }
    @Override
    int length() { return 3; }
    // cascade without break is intentional; checks all way down
    @Override
    @SuppressWarnings("fallthrough")
    boolean stringMatch(char[] cs, int start, int end) {
        switch (end-start) {
        case 3: if (cs[start+2] != mC3) return false;
        case 2: if (cs[start+1] != mC2) return false;
        case 1: if (cs[start] != mC1) return false;
        default: return true;
        }
    }
}

abstract class PAT4Node extends AbstractPATNode {
    char mC1;
    char mC2;
    char mC3;
    char mC4;
    PAT4Node(char c1, char c2, char c3, char c4) {
        mC1 = c1;
        mC2 = c2;
        mC3 = c3;
        mC4 = c4;
    }
    @Override
    char[] chars() {
        return new char[] { mC1, mC2, mC3, mC4 };
    }
    @Override
    int length() { return 4; }
    // cascade without break is intentional; checks all way down
    @Override
    @SuppressWarnings("fallthrough")
    boolean stringMatch(char[] cs, int start, int end) {
        switch (end-start) {
        case 4: if (cs[start+3] != mC4) return false;
        case 3: if (cs[start+2] != mC3) return false;
        case 2: if (cs[start+1] != mC2) return false;
        case 1: if (cs[start] != mC1) return false;
        default: return true;
        }
    }
}

abstract class PATArrayNode extends AbstractPATNode {
    char[] mCs;
    PATArrayNode(char[] cs) {
        mCs = cs;
    }
    @Override
    char[] chars() {
        return mCs;
    }
    @Override
    int length() { return mCs.length; }
    @Override
    boolean stringMatch(char[] cs, int start, int end) {
        for (int i = 0; i < (end-start); ++i)
            if (mCs[i] != cs[start+i]) return false;
        return true;
    }
}



final class PAT1NodeOne extends PAT1Node {
    public PAT1NodeOne(char c) {
        super(c);
    }
    public long count() {
        return 1l;
    }
}
final class PAT2NodeOne extends PAT2Node {
    public PAT2NodeOne(char c1, char c2) {
        super(c1,c2);
    }
    public long count() {
        return 1l;
    }
}
final class PAT3NodeOne extends PAT3Node {
    public PAT3NodeOne(char c1, char c2, char c3) {
        super(c1,c2,c3);
    }
    public long count() {
        return 1l;
    }
}
final class PAT4NodeOne extends PAT4Node {
    public PAT4NodeOne(char c1, char c2, char c3, char c4) {
        super(c1,c2,c3,c4);
    }
    public long count() {
        return 1l;
    }
}
final class PATArrayNodeOne extends PATArrayNode {
    int mCount;
    public PATArrayNodeOne(char[] cs) {
        super(cs);
    }
    public long count() {
        return 1l;
    }
}

final class PAT1NodeTwo extends PAT1Node {
    public PAT1NodeTwo(char c) {
        super(c);
    }
    public long count() {
        return 2l;
    }
}
final class PAT2NodeTwo extends PAT2Node {
    public PAT2NodeTwo(char c1, char c2) {
        super(c1,c2);
    }
    public long count() {
        return 2l;
    }
}
final class PAT3NodeTwo extends PAT3Node {
    public PAT3NodeTwo(char c1, char c2, char c3) {
        super(c1,c2,c3);
    }
    public long count() {
        return 2l;
    }
}
final class PAT4NodeTwo extends PAT4Node {
    public PAT4NodeTwo(char c1, char c2, char c3, char c4) {
        super(c1,c2,c3,c4);
    }
    public long count() {
        return 2l;
    }
}
final class PATArrayNodeTwo extends PATArrayNode {
    int mCount;
    public PATArrayNodeTwo(char[] cs) {
        super(cs);
    }
    public long count() {
        return 2l;
    }
}

final class PAT1NodeThree extends PAT1Node {
    public PAT1NodeThree(char c) {
        super(c);
    }
    public long count() {
        return 3l;
    }
}
final class PAT2NodeThree extends PAT2Node {
    public PAT2NodeThree(char c1, char c2) {
        super(c1,c2);
    }
    public long count() {
        return 3l;
    }
}
final class PAT3NodeThree extends PAT3Node {
    public PAT3NodeThree(char c1, char c2, char c3) {
        super(c1,c2,c3);
    }
    public long count() {
        return 3l;
    }
}
final class PAT4NodeThree extends PAT4Node {
    public PAT4NodeThree(char c1, char c2, char c3, char c4) {
        super(c1,c2,c3,c4);
    }
    public long count() {
        return 3l;
    }
}
final class PATArrayNodeThree extends PATArrayNode {
    int mCount;
    public PATArrayNodeThree(char[] cs) {
        super(cs);
    }
    public long count() {
        return 3l;
    }
}

final class PAT1NodeByte extends PAT1Node {
    final byte mCount;
    public PAT1NodeByte(char c, long count) {
        super(c);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT2NodeByte extends PAT2Node {
    final byte mCount;
    public PAT2NodeByte(char c1, char c2, long count) {
        super(c1,c2);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT3NodeByte extends PAT3Node {
    final byte mCount;
    public PAT3NodeByte(char c1, char c2, char c3, long count) {
        super(c1,c2,c3);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT4NodeByte extends PAT4Node {
    final byte mCount;
    public PAT4NodeByte(char c1, char c2, char c3, char c4,
                        long count) {
        super(c1,c2,c3,c4);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class PATArrayNodeByte extends PATArrayNode {
    final byte mCount;
    public PATArrayNodeByte(char[] cs, long count) {
        super(cs);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class TerminalNodeByte extends TerminalNode {
    final byte mCount;
    public TerminalNodeByte(long count) {
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class OneDtrNodeByte extends OneDtrNode {
    final byte mCount;
    public OneDtrNodeByte(char c, Node dtr, long count) {
        super(c,dtr);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class TwoDtrNodeByte extends TwoDtrNode {
    final byte mCount;
    public TwoDtrNodeByte(char c1, Node dtr1,
                          char c2, Node dtr2,
                          long count) {
        super(c1,dtr1,c2,dtr2);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class ThreeDtrNodeByte extends ThreeDtrNode {
    final byte mCount;
    public ThreeDtrNodeByte(char c1, Node dtr1,
                            char c2, Node dtr2,
                            char c3, Node dtr3,
                            long count) {
        super(c1,dtr1,c2,dtr2,c3,dtr3);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}
final class ArrayDtrNodeByte extends ArrayDtrNode {
    final byte mCount;
    public ArrayDtrNodeByte(char[] cs, Node[] dtrs, long count) {
        super(cs,dtrs);
        mCount = (byte) count;
    }
    public long count() {
        return mCount;
    }
}

final class PAT1NodeShort extends PAT1Node {
    final short mCount;
    public PAT1NodeShort(char c, long count) {
        super(c);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT2NodeShort extends PAT2Node {
    final short mCount;
    public PAT2NodeShort(char c1, char c2, long count) {
        super(c1,c2);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT3NodeShort extends PAT3Node {
    final short mCount;
    public PAT3NodeShort(char c1, char c2, char c3, long count) {
        super(c1,c2,c3);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT4NodeShort extends PAT4Node {
    final short mCount;
    public PAT4NodeShort(char c1, char c2, char c3, char c4,
                         long count) {
        super(c1,c2,c3,c4);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}
final class PATArrayNodeShort extends PATArrayNode {
    final short mCount;
    public PATArrayNodeShort(char[] cs, long count) {
        super(cs);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}
final class TerminalNodeShort extends TerminalNode {
    final short mCount;
    public TerminalNodeShort(long count) {
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}
final class OneDtrNodeShort extends OneDtrNode {
    final short mCount;
    public OneDtrNodeShort(char c, Node dtr, long count) {
        super(c,dtr);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}
final class TwoDtrNodeShort extends TwoDtrNode {
    final short mCount;
    public TwoDtrNodeShort(char c1, Node dtr1,
                           char c2, Node dtr2,
                           long count) {
        super(c1,dtr1,c2,dtr2);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}
final class ThreeDtrNodeShort extends ThreeDtrNode {
    final short mCount;
    public ThreeDtrNodeShort(char c1, Node dtr1,
                             char c2, Node dtr2,
                             char c3, Node dtr3,
                             long count) {
        super(c1,dtr1,c2,dtr2,c3,dtr3);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}

abstract class ArrayDtrNodeCacheExtCount extends ArrayDtrNode {
    long mExtCount = -1;
    public ArrayDtrNodeCacheExtCount(char[] cs, Node[] dtrs) {
        super(cs,dtrs);
    }
    @Override
    public long contextCount() {
        // must synch outside because long's not atomic
        synchronized (this) {
            if (mExtCount == -1)
                mExtCount = super.contextCount();
            return mExtCount;
        }
    }
}

final class ArrayDtrNodeShort extends ArrayDtrNodeCacheExtCount {
    final short mCount;
    public ArrayDtrNodeShort(char[] cs, Node[] dtrs, long count) {
        super(cs,dtrs);
        mCount = (short) count;
    }
    public long count() {
        return mCount;
    }
}


final class PAT1NodeInt extends PAT1Node {
    final int mCount;
    public PAT1NodeInt(char c, long count) {
        super(c);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT2NodeInt extends PAT2Node {
    final int mCount;
    public PAT2NodeInt(char c1, char c2, long count) {
        super(c1,c2);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT3NodeInt extends PAT3Node {
    final int mCount;
    public PAT3NodeInt(char c1, char c2, char c3, long count) {
        super(c1,c2,c3);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT4NodeInt extends PAT4Node {
    final int mCount;
    public PAT4NodeInt(char c1, char c2, char c3, char c4,
                       long count) {
        super(c1,c2,c3,c4);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class PATArrayNodeInt extends PATArrayNode {
    final int mCount;
    public PATArrayNodeInt(char[] cs, long count) {
        super(cs);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class TerminalNodeInt extends TerminalNode {
    final int mCount;
    public TerminalNodeInt(long count) {
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class OneDtrNodeInt extends OneDtrNode {
    final int mCount;
    public OneDtrNodeInt(char c, Node dtr, long count) {
        super(c,dtr);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class TwoDtrNodeInt extends TwoDtrNode {
    final int mCount;
    public TwoDtrNodeInt(char c1, Node dtr1,
                         char c2, Node dtr2,
                         long count) {
        super(c1,dtr1,c2,dtr2);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class ThreeDtrNodeInt extends ThreeDtrNode {
    final int mCount;
    public ThreeDtrNodeInt(char c1, Node dtr1,
                           char c2, Node dtr2,
                           char c3, Node dtr3,
                           long count) {
        super(c1,dtr1,c2,dtr2,c3,dtr3);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}
final class ArrayDtrNodeInt extends ArrayDtrNodeCacheExtCount {
    final int mCount;
    public ArrayDtrNodeInt(char[] cs, Node[] dtrs, long count) {
        super(cs,dtrs);
        mCount = (int) count;
    }
    public long count() {
        return mCount;
    }
}

final class PAT1NodeLong extends PAT1Node {
    final long mCount;
    public PAT1NodeLong(char c, long count) {
        super(c);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT2NodeLong extends PAT2Node {
    final long mCount;
    public PAT2NodeLong(char c1, char c2, long count) {
        super(c1,c2);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT3NodeLong extends PAT3Node {
    final long mCount;
    public PAT3NodeLong(char c1, char c2, char c3, long count) {
        super(c1,c2,c3);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class PAT4NodeLong extends PAT4Node {
    final long mCount;
    public PAT4NodeLong(char c1, char c2, char c3, char c4,
                        long count) {
        super(c1,c2,c3,c4);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class PATArrayNodeLong extends PATArrayNode {
    final long mCount;
    public PATArrayNodeLong(char[] cs, long count) {
        super(cs);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class TerminalNodeLong extends TerminalNode {
    final long mCount;
    public TerminalNodeLong(long count) {
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class OneDtrNodeLong extends OneDtrNode {
    final long mCount;
    public OneDtrNodeLong(char c, Node dtr, long count) {
        super(c,dtr);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class TwoDtrNodeLong extends TwoDtrNode {
    final long mCount;
    public TwoDtrNodeLong(char c1, Node dtr1,
                          char c2, Node dtr2,
                          long count) {
        super(c1,dtr1,c2,dtr2);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class ThreeDtrNodeLong extends ThreeDtrNode {
    final long mCount;
    public ThreeDtrNodeLong(char c1, Node dtr1,
                            char c2, Node dtr2,
                            char c3, Node dtr3,
                            long count) {
        super(c1,dtr1,c2,dtr2,c3,dtr3);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}
final class ArrayDtrNodeLong extends ArrayDtrNodeCacheExtCount {
    final long mCount;
    public ArrayDtrNodeLong(char[] cs, Node[] dtrs, long count) {
        super(cs,dtrs);
        mCount = count;
    }
    public long count() {
        return mCount;
    }
}

class NodeFactory {
    static char[] sliceToArray(char[] cs, int start, int end) {
        if (start == 0 && end==cs.length) return cs;
        char[] result = new char[end-start];
        for (int i = 0; i < result.length; ++i)
            result[i] = cs[start+i];
        return result;
    }
    static Node[] TERMINAL_NODES
        = new Node[1024];
    static {
        for (int i = 0; i < TERMINAL_NODES.length; ++i)
            TERMINAL_NODES[i] = createTerminalNode(i);
    }
    static Node createNode(long count) {
        if (count < TERMINAL_NODES.length)
            return TERMINAL_NODES[(int)count];
        return createTerminalNode(count);
    }
    static Node createNode(char[] cs, int start, int end, long count) {
        switch (end-start) {
        case 0: return createNode(count);
        case 1: return createNode(cs[start],count);
        case 2: return createNode(cs[start],cs[start+1],count);
        case 3: return createNode(cs[start],cs[start+1],cs[start+2],count);
        case 4: return createNode(cs[start],cs[start+1],cs[start+2],
                                  cs[start+3],count);
        default: return createPATArrayNode(sliceToArray(cs,start,end),count);
        }
    }
    static Node createNode(char[] cs, Node[] dtrs, long count) {
        switch (dtrs.length) {
        case 0: return createNode(count);
        case 1: return createNode(cs[0],dtrs[0],count);
        case 2: return createNode(cs[0],dtrs[0],cs[1],dtrs[1],count);
        case 3: return createNode(cs[0],dtrs[0],cs[1],dtrs[1],
                                  cs[2],dtrs[2],count);
        default: return createArrayDtrNode(cs,dtrs,count);
        }
    }
    static Node createNodePrune(char[] cs, Node[] dtrs, long count) {
        int numOutcomes = 0;
        for (int i = 0; i < dtrs.length; ++i)
            if (dtrs[i] != null) ++numOutcomes;
        if (numOutcomes == dtrs.length) return createNode(cs,dtrs,count);
        char[] csOut = new char[numOutcomes];
        Node[] dtrsOut = new Node[numOutcomes];
        int indexOut = 0;
        for (int i = 0; i < dtrs.length; ++i) {
            if (dtrs[i] != null) {
                csOut[indexOut] = cs[i];
                dtrsOut[indexOut] = dtrs[i];
                ++indexOut;
            }
        }
        return createNode(csOut,dtrsOut,count);
    }
    static Node createNode(char[] cs, int start, int end,
                           long headCount, long tailCount) {
        if (end == start)
            return createNode(headCount);
        if (headCount == tailCount)
            return createNode(cs,start,end,headCount);
        return createNode(cs[start],
                          createNode(cs,start+1,end,tailCount),
                          headCount);
    }
    static Node createTerminalNode(long count) {
        if (count <= Byte.MAX_VALUE)
            return new TerminalNodeByte(count);
        else if (count <= Short.MAX_VALUE)
            return new TerminalNodeShort(count);
        else if (count <= Integer.MAX_VALUE)
            return new TerminalNodeInt(count);
        else
            return new TerminalNodeLong(count);
    }
    static Node createNode(char c, long count) {
        if (count == 1)
            return new PAT1NodeOne(c);
        else if (count == 2)
            return new PAT1NodeTwo(c);
        else if (count == 3)
            return new PAT1NodeThree(c);
        else if (count <= Byte.MAX_VALUE)
            return new PAT1NodeByte(c,count);
        else if (count <= Short.MAX_VALUE)
            return new PAT1NodeShort(c,count);
        else if (count <= Integer.MAX_VALUE)
            return new PAT1NodeInt(c,count);
        else
            return new PAT1NodeLong(c,count);
    }
    static Node createNode(char c1, char c2, long count) {
        if (count == 1)
            return new PAT2NodeOne(c1,c2);
        else if (count == 2)
            return new PAT2NodeTwo(c1,c2);
        else if (count == 3)
            return new PAT2NodeThree(c1,c2);
        else if (count <= Byte.MAX_VALUE)
            return new PAT2NodeByte(c1,c2,count);
        else if (count <= Short.MAX_VALUE)
            return new PAT2NodeShort(c1,c2,count);
        else if (count <= Integer.MAX_VALUE)
            return new PAT2NodeInt(c1,c2,count);
        else
            return new PAT2NodeLong(c1,c2,count);
    }
    static Node createNode(char c1, char c2, char c3, long count) {
        if (count == 1)
            return new PAT3NodeOne(c1,c2,c3);
        else if (count == 2)
            return new PAT3NodeTwo(c1,c2,c3);
        else if (count == 3)
            return new PAT3NodeThree(c1,c2,c3);
        else if (count <= Byte.MAX_VALUE)
            return new PAT3NodeByte(c1,c2,c3,count);
        else if (count <= Short.MAX_VALUE)
            return new PAT3NodeShort(c1,c2,c3,count);
        else if (count <= Integer.MAX_VALUE)
            return new PAT3NodeInt(c1,c2,c3,count);
        else
            return new PAT3NodeLong(c1,c2,c3,count);
    }
    static Node createNode(char c1, char c2, char c3, char c4, long count) {
        if (count == 1)
            return new PAT4NodeOne(c1,c2,c3,c4);
        else if (count == 2)
            return new PAT4NodeTwo(c1,c2,c3,c4);
        else if (count == 3)
            return new PAT4NodeThree(c1,c2,c3,c4);
        else if (count <= Byte.MAX_VALUE)
            return new PAT4NodeByte(c1,c2,c3,c4,count);
        else if (count <= Short.MAX_VALUE)
            return new PAT4NodeShort(c1,c2,c3,c4,count);
        else if (count <= Integer.MAX_VALUE)
            return new PAT4NodeInt(c1,c2,c3,c4,count);
        else
            return new PAT4NodeLong(c1,c2,c3,c4,count);
    }
    static Node createPATArrayNode(char[] cs, long count) {
        if (count == 1)
            return new PATArrayNodeOne(cs);
        else if (count == 2)
            return new PATArrayNodeTwo(cs);
        else if (count == 3)
            return new PATArrayNodeThree(cs);
        else if (count <= Byte.MAX_VALUE)
            return new PATArrayNodeByte(cs,count);
        else if (count <= Short.MAX_VALUE)
            return new PATArrayNodeShort(cs,count);
        else if (count <= Integer.MAX_VALUE)
            return new PATArrayNodeInt(cs,count);
        else
            return new PATArrayNodeLong(cs,count);
    }
    static Node createPATNode(char firstC, char[] restCs, long count) {
        switch (restCs.length) {
        case 0:
            return createNode(firstC,count);
        case 1:
            return createNode(firstC,restCs[0],count);
        case 2:
            return createNode(firstC,restCs[0],restCs[1],count);
        case 3:
            return createNode(firstC,restCs[0],restCs[1],restCs[2],count);
        default:
            char[] cs = new char[restCs.length+1];
            cs[0] = firstC;
            System.arraycopy(restCs,0,cs,1,restCs.length);
            return createPATArrayNode(cs,count);
        }
    }
    static Node createNodeFold(char c, Node dtr, long count) {
        if (dtr.count() == count) {
            if (dtr instanceof AbstractPATNode) {
                AbstractPATNode patDtr = (AbstractPATNode) dtr;
                return createPATNode(c,patDtr.chars(),count);
            }
            if (dtr instanceof TerminalNode) {
                return createNode(c,count);
            }
        }
        return createNode(c,dtr,count);
    }
    static Node createNode(char c, Node dtr, long count) {
        if (count <= Byte.MAX_VALUE)
            return new OneDtrNodeByte(c,dtr,count);
        else if (count <= Short.MAX_VALUE)
            return new OneDtrNodeShort(c,dtr,count);
        else if (count <= Integer.MAX_VALUE)
            return new OneDtrNodeInt(c,dtr,count);
        else
            return new OneDtrNodeLong(c,dtr,count);
    }
    static Node createNode(char c1, Node dtr1,
                           char c2, Node dtr2,
                           long count) {
        if (count <= Byte.MAX_VALUE)
            return new TwoDtrNodeByte(c1,dtr1,c2,dtr2,count);
        else if (count <= Short.MAX_VALUE)
            return new TwoDtrNodeShort(c1,dtr1,c2,dtr2,count);
        else if (count <= Integer.MAX_VALUE)
            return new TwoDtrNodeInt(c1,dtr1,c2,dtr2,count);
        else
            return new TwoDtrNodeLong(c1,dtr1,c2,dtr2,count);
    }
    static Node createNode(char c1, Node dtr1,
                           char c2, Node dtr2,
                           char c3, Node dtr3,
                           long count) {
        if (count <= Byte.MAX_VALUE)
            return new ThreeDtrNodeByte(c1,dtr1,c2,dtr2,c3,dtr3,count);
        else if (count <= Short.MAX_VALUE)
            return new ThreeDtrNodeShort(c1,dtr1,c2,dtr2,c3,dtr3,count);
        else if (count <= Integer.MAX_VALUE)
            return new ThreeDtrNodeInt(c1,dtr1,c2,dtr2,c3,dtr3,count);
        else
            return new ThreeDtrNodeLong(c1,dtr1,c2,dtr2,c3,dtr3,count);
    }
    static Node createArrayDtrNode(char[] cs, Node[] dtrs, long count) {
        if (count <= Byte.MAX_VALUE)
            return new ArrayDtrNodeByte(cs,dtrs,count);
        else if (count <= Short.MAX_VALUE)
            return new ArrayDtrNodeShort(cs,dtrs,count);
        else if (count <= Integer.MAX_VALUE)
            return new ArrayDtrNodeInt(cs,dtrs,count);
        else
            return new ArrayDtrNodeLong(cs,dtrs,count);
    }

    static Node[] EMPTY_NODES = new Node[0];
}


class NBestCounter extends BoundedPriorityQueue<NBestCounter.NBEntry> {
    static final long serialVersionUID = -1604467508550079460L;
    private final boolean mReversed;  // [carp: my hack]
    public NBestCounter(int maxEntries) {
        this(maxEntries,false);
    }
    public NBestCounter(int maxEntries, boolean reversed) {
        super(COMPARATOR,maxEntries);
        mReversed = reversed;
    }
    public ObjectToCounterMap<String> toObjectToCounter() {
        ObjectToCounterMap<String> otc = new ObjectToCounterMap<String>();
        for (NBEntry entry : this) {
            if (entry.mCount > Integer.MAX_VALUE) {
                String msg = "Entry too large.";
                throw new IllegalArgumentException(msg);
            }
            otc.set(entry.mString,(int)entry.mCount);
        }
        return otc;
    }
    public void put(char[] cs, int length, long count) {
        offer(new NBEntry(cs,length,count));
    }
    class NBEntry implements Comparable<NBEntry> {
        // static w/o hack
        final String mString;
        final long mCount;
        public NBEntry(char[] cs, int length, long count) {
            mString = new String(cs,0,length);
            mCount = count;
        }
        public int compareTo(NBEntry thatEntry) {
            if (thatEntry.mCount == mCount)
                return thatEntry.mString.compareTo(mString);
            long diff = thatEntry.mCount - mCount;
            int comp
                = (diff < 0)
                ? -1
                : ( (diff > 0)
                    ? 1
                    : 0 );
            return mReversed ? -comp : comp;
        }
    }
    static Comparator<NBestCounter.NBEntry> COMPARATOR
        = new Comparator<NBEntry>() {
        public int compare(NBestCounter.NBEntry entry1,
                           NBestCounter.NBEntry entry2) {
            return entry1.compareTo(entry2);
        }
    };
}
