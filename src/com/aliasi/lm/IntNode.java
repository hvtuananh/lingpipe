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


import com.aliasi.symbol.SymbolTable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author  Bob Carpenter
 * @version 3.8
 */
class IntNode {
    int mCount;
    long mExtCount;
    DtrMap mDtrs;
    IntNode() {
        mCount = 0;
        mExtCount = 0L;
        mDtrs = DtrMap0.EMPTY_DTR_MAP;
    }
    IntNode(int[] toks, int start, int end) {
        mCount = 1;
        if (start == end) {
            mDtrs = DtrMap0.EMPTY_DTR_MAP;
            mExtCount = 0L;
            return;
        }
        mExtCount = 1L;
        int tok = toks[start];
        IntNode dtr = new IntNode(toks,start+1,end);
        mDtrs = new DtrMap1(tok,dtr);
    }
    IntNode(int[] toks, int start, int end, int count) {
        mCount = count;
        if (start == end) {
            mDtrs = DtrMap0.EMPTY_DTR_MAP;
            mExtCount = 0L;
            return;
        }
        mExtCount = count;
        int tok = toks[start];
        IntNode dtr = new IntNode(toks,start+1,end,count);
        mDtrs = new DtrMap1(tok,dtr);
    }
    IntNode(int[] toks, int start, int end, int count,
            boolean incrementPath) {
        if (incrementPath)
            throw new IllegalArgumentException("require true");
        if (start == end) {
            mCount = count;
            mDtrs = DtrMap0.EMPTY_DTR_MAP;
            mExtCount = 0L;
            return;
        }
        mCount = 0;
        mExtCount = (start + 1 == end) ? count : 0L;
        int tok = toks[start];
        IntNode dtr = new IntNode(toks,start+1,end,count,incrementPath);
        mDtrs = new DtrMap1(tok,dtr);
    }
    public void prune(int minCount) {
        mDtrs = mDtrs.prune(minCount);
        mExtCount = mDtrs.extensionCount();
    }
    public void rescale(double countMultiplier) {
        mCount = (int)(countMultiplier * mCount);
        mDtrs = mDtrs.rescale(countMultiplier);
        mExtCount = mDtrs.extensionCount();
    }
    public static String idToSymbol(int id, SymbolTable st) {
        if (id == -2) return "EOS";
        if (id == -1) return "UNK";
        return st.idToSymbol(id);
    }
    int trieSize() {
        return 1 + mDtrs.dtrsTrieSize();
    }
    void decrement(int symbol) {
        IntNode dtr = mDtrs.getDtr(symbol);
        if (dtr == null) {
            String msg = "symbol doesn't exist=" + symbol;
            throw new IllegalArgumentException(msg);
        }
        if (mCount <= 0) {
            String msg = "Cannot decrement below zero.";
            throw new IllegalArgumentException(msg);
        }
        if (mExtCount < 1) {
            String msg = "Cannot decrement extensions below zero.";
            throw new IllegalArgumentException(msg);
        }
        --mCount;
        --mExtCount;
        dtr.decrement();
    }
    private void decrement() {
        if (mCount == 0) {
            String msg = "Cannot decrement below 0.";
            throw new IllegalArgumentException(msg);
        }
        --mCount;
    }
    void decrement(int symbol, int count) {
        IntNode dtr = mDtrs.getDtr(symbol);
        if (dtr == null) {
            String msg = "symbol doesn't exist=" + symbol;
            throw new IllegalArgumentException(msg);
        }
        if (mCount - count < 0) {
            String msg = "Cannot decrement below zero."
                + " Count=" + mCount + " decrement=" + count;
            throw new IllegalArgumentException(msg);
        }
        if (mExtCount - count < 0) {
            String msg = "Cannot decrement extension count below zero."
                + " Ext count=" + mExtCount + " decrement=" + count;
            throw new IllegalArgumentException(msg);
        }
        mCount -= count;
        mExtCount -= count;
        dtr.decrementCount(count);
    }
    private void decrementCount(int count) {
        if (mCount - count < 0) {
            String msg = "Cannot decrement below 0."
                + " Count=" + mCount + " decrement=" + count;
            throw new IllegalArgumentException(msg);
        }
        mCount -= count;
    }
    int count() {
        return mCount;
    }
    void addDaughters(List<IntNode> queue) {
        mDtrs.addDtrs(queue);
    }
    long extensionCount() {
        return mExtCount;
    }
    int numExtensions() {
        return mDtrs.numExtensions();
    }
    int[] integersFollowing() {
        return mDtrs.integersFollowing();
    }
    int[] integersFollowing(int[] is, int start, int end) {
        IntNode dtr = getDtr(is,start,end);
        if (dtr == null) return EMPTY_INT_ARRAY;
        return dtr.integersFollowing();
    }
    int[] observedIntegers() {
        return integersFollowing(EMPTY_INT_ARRAY,0,0);
    }
    void incrementSequence(int[] tokIndices, int start, int end, int count) {
        if (start == end) {
            mCount += count;
            return;
        }
        if (start + 1 == end)
            mExtCount += count;
        DtrMap newDtrs = mDtrs.incrementSequence(tokIndices,start,end,count);
        if (!newDtrs.equals(mDtrs)) mDtrs = newDtrs;
    }

    void increment(int[] tokIndices, int start, int end) {
        ++mCount;
        if (start == end) return;
        ++mExtCount;
        DtrMap newDtrs = mDtrs.incrementDtrs(tokIndices,start,end);
        if (!newDtrs.equals(mDtrs)) mDtrs = newDtrs;
    }
    void increment(int[] tokIndices, int start, int end, int count) {
        mCount += count;
        if (start == end) return;
        mExtCount += count;
        DtrMap newDtrs = mDtrs.incrementDtrs(tokIndices,start,end,count);
        if (!newDtrs.equals(mDtrs)) mDtrs = newDtrs;
    }
    IntNode getDtr(int[] toks, int start, int end) {
        if (start == end) return this;
        IntNode dtr = mDtrs.getDtr(toks[start]);
        if (dtr == null) return null;
        return dtr.getDtr(toks,start+1,end);
    }
    public String toString(SymbolTable st) {
        StringBuilder sb = new StringBuilder();
        toString(sb,0,st);
        return sb.toString();
    }
    public void toString(StringBuilder sb, int depth, SymbolTable st) {
        sb.append(count());
        AbstractNode.indent(sb,depth);
        mDtrs.toString(sb,depth,st);
    }
    static final int[] EMPTY_INT_ARRAY = new int[0];
}


interface DtrMap {
    // just passed along
    public int numExtensions();
    public long extensionCount();
    public void addDtrs(List<IntNode> queue);
    public int[] integersFollowing();

    // distinct
    public IntNode getDtr(int tok);
    public int dtrsTrieSize();
    public void toString(StringBuilder sb, int depth, SymbolTable st);

    // these are the killers for unfolding
    public DtrMap rescale(double countMultiplier);
    public DtrMap prune(int minCount);
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end);
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end,
                                int count);
    public DtrMap incrementSequence(int[] tokIndices, int start, int end,
                                    int count);
}


class DtrMap0 implements DtrMap {
    public IntNode getDtr(int tok) {
        return null;
    }
    public int numExtensions() {
        return 0;
    }
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end) {
        if (start == end) return this;
        IntNode dtr = new IntNode(tokIndices,start+1,end);
        return new DtrMap1(tokIndices[start],dtr);
    }
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end,
                                int count) {
        if (start == end) return this;
        IntNode dtr = new IntNode(tokIndices,start+1,end,count);
        return new DtrMap1(tokIndices[start],dtr);
    }
    public DtrMap incrementSequence(int[] tokIndices, int start, int end,
                                    int count) {
        if (start == end) return this;
        IntNode dtr = new IntNode(tokIndices,start+1,end,count,false);
        return new DtrMap1(tokIndices[start],dtr);
    }
    public void toString(StringBuilder sb, int depth, SymbolTable st) {
        /* nothing to add */
    }
    public long extensionCount() {
        return 0l;
    }
    public int[] integersFollowing() {
        return IntNode.EMPTY_INT_ARRAY;
    }
    public DtrMap prune(int minCount) {
        return this; // nothing to prune
    }
    public DtrMap rescale(double countMultiplier) {
        return this; // nought to scale
    }
    public void addDtrs(List<IntNode> queue) {
        /* nothing to add */
    }
    public int dtrsTrieSize() { return 0; }
    static final DtrMap EMPTY_DTR_MAP = new DtrMap0();
}


class DtrMap1 implements DtrMap {
    final int mTok;
    IntNode mDtr = new IntNode();
    public DtrMap1(int tok, IntNode dtr) {
        mTok = tok;
        mDtr = dtr;
    }
    public DtrMap prune(int minCount) {
        if (mDtr.count() < minCount)
            return DtrMap0.EMPTY_DTR_MAP;
        mDtr.prune(minCount);
        return this;
    }
    public DtrMap rescale(double countMultiplier) {
        mDtr.rescale(countMultiplier);
        if (mDtr.count() == 0)
            return DtrMap0.EMPTY_DTR_MAP;
        return this;
    }
    public int numExtensions() {
        return 1;
    }
    public void toString(StringBuilder sb, int depth, SymbolTable st) {
        if (st != null)
            sb.append(IntNode.idToSymbol(mTok,st));
        else
            sb.append(mTok);
        sb.append(": ");
        mDtr.toString(sb,depth+1,st);
    }
    public void addDtrs(List<IntNode> queue) {
        queue.add(mDtr);
    }
    public int dtrsTrieSize() { return mDtr.trieSize(); }
    public IntNode getDtr(int tok) {
        return tok == mTok ? mDtr : null;
    }
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end) {
        if (start == end) return this;
        if (tokIndices[start] == mTok) {
            mDtr.increment(tokIndices,start+1,end);
            return this;
        }
        IntNode dtr = new IntNode(tokIndices,start+1,end);
        if (tokIndices[start] < mTok)
            return new DtrMap2(tokIndices[start],mTok,
                               dtr,mDtr);
        return new DtrMap2(mTok,tokIndices[start],
                           mDtr,dtr);
    }
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end,
                                int count) {
        if (start == end) return this;
        if (tokIndices[start] == mTok) {
            mDtr.increment(tokIndices,start+1,end,count);
            return this;
        }
        IntNode dtr = new IntNode(tokIndices,start+1,end,count);
        if (tokIndices[start] < mTok)
            return new DtrMap2(tokIndices[start],mTok,
                               dtr,mDtr);
        return new DtrMap2(mTok,tokIndices[start],
                           mDtr,dtr);
    }
    public DtrMap incrementSequence(int[] tokIndices, int start, int end,
                                    int count) {
        if (start == end) return this;
        if (tokIndices[start] == mTok) {
            mDtr.incrementSequence(tokIndices,start+1,end,count);
            return this;
        }
        IntNode dtr = new IntNode(tokIndices,start+1,end,count,false);
        if (tokIndices[start] < mTok)
            return new DtrMap2(tokIndices[start],mTok,
                               dtr,mDtr);
        return new DtrMap2(mTok,tokIndices[start],
                           mDtr,dtr);
    }
    public long extensionCount() {
        return mDtr.count();
    }
    public int[] integersFollowing() {
        return new int[] { mTok };
    }
}

class DtrMap2 implements DtrMap {
    final int mTok1;
    final int mTok2;
    IntNode mDtr1;
    IntNode mDtr2;
    public DtrMap2(int tok1,
                   int tok2,
                   IntNode dtr1,
                   IntNode dtr2) {
        mTok1 = tok1;
        mDtr1 = dtr1;
        mTok2 = tok2;
        mDtr2 = dtr2;
    }
    public DtrMap prune(int minCount) {
        if (mDtr1.count() < minCount) {
            if (mDtr2.count() < minCount)
                return DtrMap0.EMPTY_DTR_MAP;
            mDtr2.prune(minCount);
            return new DtrMap1(mTok2,mDtr2);
        }
        mDtr1.prune(minCount);
        if (mDtr2.count() < minCount)
            return new DtrMap1(mTok1,mDtr1);
        mDtr2.prune(minCount);
        return this;
    }
    public DtrMap rescale(double countMultiplier) {
        mDtr1.rescale(countMultiplier);
        mDtr2.rescale(countMultiplier);
        if (mDtr1.count() == 0) {
            if (mDtr2.count() == 0)
                return DtrMap0.EMPTY_DTR_MAP;
            return new DtrMap1(mTok2,mDtr2);
        }
        if (mDtr2.count() == 0)
            return new DtrMap1(mTok1,mDtr1);
        return this;
    }
    public int numExtensions() {
        return 2;
    }
    public void toString(StringBuilder sb, int depth, SymbolTable st) {
        if (st != null)
            sb.append(IntNode.idToSymbol(mTok1,st));
        else
            sb.append(mTok1);
        sb.append(": ");
        mDtr1.toString(sb,depth+1,st);
        AbstractNode.indent(sb,depth);
        if (st != null)
            sb.append(IntNode.idToSymbol(mTok2,st));
        else
            sb.append(mTok2);
        sb.append(": ");
        mDtr2.toString(sb,depth+1,st);
    }
    public void addDtrs(List<IntNode> queue) {
        queue.add(mDtr1);
        queue.add(mDtr2);
    }
    public int dtrsTrieSize() {
        return mDtr1.trieSize() + mDtr2.trieSize();
    }
    public IntNode getDtr(int tok) {
        if (tok == mTok1) return mDtr1;
        if (tok == mTok2) return mDtr2;
        return null;
    }
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end) {
        if (start == end) return this;
        int tok = tokIndices[start];
        if (tok == mTok1) {
            mDtr1.increment(tokIndices,start+1,end);
            return this;
        }
        if (tok == mTok2) {
            mDtr2.increment(tokIndices,start+1,end);
            return this;
        }
        IntNode dtr = new IntNode(tokIndices,start+1,end);
        return new DtrMapMap(tok, mTok1, mTok2,
                             dtr, mDtr1, mDtr2); // ordered by map
    }
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end, int count) {
        if (start == end) return this;
        int tok = tokIndices[start];
        if (tok == mTok1) {
            mDtr1.increment(tokIndices,start+1,end,count);
            return this;
        }
        if (tok == mTok2) {
            mDtr2.increment(tokIndices,start+1,end,count);
            return this;
        }
        IntNode dtr = new IntNode(tokIndices,start+1,end,count);
        return new DtrMapMap(tok, mTok1, mTok2,
                             dtr, mDtr1, mDtr2); // ordered by map
    }
    public DtrMap incrementSequence(int[] tokIndices, int start, int end,
                                    int count) {
        if (start == end) return this;
        int tok = tokIndices[start];
        if (tok == mTok1) {
            mDtr1.incrementSequence(tokIndices,start+1,end,count);
            return this;
        }
        if (tok == mTok2) {
            mDtr2.incrementSequence(tokIndices,start+1,end,count);
        }
        IntNode dtr = new IntNode(tokIndices,start+1,end,count,false);
        return new DtrMapMap(tok, mTok1, mTok2,
                             dtr, mDtr1, mDtr2);
    }
    public int[] integersFollowing() {
        return new int[] { mTok1, mTok2 };
    }
    public long extensionCount() {
        return ((long) mDtr1.count())
            + (long) mDtr2.count();
    }
}

class DtrMapMap
    extends TreeMap<Integer,IntNode>
    implements DtrMap {

    static final long serialVersionUID = -840053413688713070L;

    DtrMapMap(int out1, int out2, int out3,
              IntNode dtr1, IntNode dtr2, IntNode dtr3) {
        add(out1,dtr1);
        add(out2,dtr2);
        add(out3,dtr3);
    }
    public DtrMap prune(int minCount) {
        Iterator<Map.Entry<Integer,IntNode>> entryIt = entrySet().iterator();
        while (entryIt.hasNext()) {
            Map.Entry<Integer,IntNode> entry = entryIt.next();
            IntNode node = entry.getValue();
            if (node.count() < minCount)
                entryIt.remove();
            node.prune(minCount);
        }
        return reduce();
    }
    public DtrMap rescale(double countMultiplier) {
        Iterator<Map.Entry<Integer,IntNode>> entryIt = entrySet().iterator();
        while (entryIt.hasNext()) {
            Map.Entry<Integer,IntNode> entry = entryIt.next();
            IntNode node = entry.getValue();
            node.rescale(countMultiplier);
            if (node.count() == 0)
                entryIt.remove();
        }
        return reduce();
    }

    public DtrMap reduce() {
        if (size() == 0)
            return DtrMap0.EMPTY_DTR_MAP;
        if (size() == 1) {
            Iterator<Map.Entry<Integer,IntNode>> entryIt = entrySet().iterator();
            Map.Entry<Integer,IntNode> entry = entryIt.next();
            int token = entry.getKey().intValue();
            IntNode node = entry.getValue();
            return new DtrMap1(token,node);
        }
        if (size() == 2) {
            Iterator<Map.Entry<Integer,IntNode>> entryIt = entrySet().iterator();
            Map.Entry<Integer,IntNode> entry1 = entryIt.next();
            int token1 = entry1.getKey().intValue();
            IntNode node1 = entry1.getValue();
            Map.Entry<Integer,IntNode> entry2 = entryIt.next();
            int token2 = entry2.getKey().intValue();
            IntNode node2 = entry2.getValue();
            return new DtrMap2(token1,token2,node1,node2);
        }
        return this;
    }

    public int numExtensions() {
        return size();
    }
    private void add(int out, IntNode dtr) {
        put(Integer.valueOf(out),dtr);
    }
    public void toString(StringBuilder sb, int depth, SymbolTable st) {
        Iterator<Map.Entry<Integer,IntNode>> it = entrySet().iterator();
        for (int i = 0; it.hasNext(); ++i) {
            if (i > 0)
                AbstractNode.indent(sb,depth);
            Map.Entry<Integer,IntNode> entry = it.next();
            Integer key = entry.getKey();
            int tok = key.intValue();
            IntNode node = entry.getValue();
            if (st != null)
                sb.append(IntNode.idToSymbol(tok,st));
            else
                sb.append(tok);
            sb.append(": ");
            node.toString(sb,depth+1,st);
        }
    }
    public void addDtrs(List<IntNode> queue) {
        queue.addAll(values());
    }
    public int dtrsTrieSize() {
        int size = 0;
        for (IntNode node : values())
            size += node.trieSize();
        return size;
    }
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end) {
        if (start == end) return this;
        Integer tok = Integer.valueOf(tokIndices[start]);
        IntNode dtr = getNode(tok);
        if (dtr == null)
            put(tok,new IntNode(tokIndices,start+1,end));
        else
            dtr.increment(tokIndices,start+1,end);
        return this;
    }
    public DtrMap incrementDtrs(int[] tokIndices, int start, int end,
                                int count) {
        if (start == end) return this;
        Integer tok = Integer.valueOf(tokIndices[start]);
        IntNode dtr = getNode(tok);
        if (dtr == null)
            put(tok,new IntNode(tokIndices,start+1,end,count));
        else
            dtr.increment(tokIndices,start+1,end,count);
        return this;
    }
    public DtrMap incrementSequence(int[] tokIndices, int start, int end,
                                    int count) {
        if (start == end) return this;
        Integer tok = Integer.valueOf(tokIndices[start]);
        IntNode dtr = getNode(tok);
        if (dtr == null)
            put(tok,new IntNode(tokIndices,start+1,end,count,false));
        else
            dtr.incrementSequence(tokIndices,start+1,end,count);
        return this;
    }
    public IntNode getDtr(int tok) {
        return getNode(Integer.valueOf(tok));
    }
    IntNode getNode(Integer tok) {
        return get(tok);
    }
    public int[] integersFollowing() {
        int[] result = new int[keySet().size()];
        Iterator<Integer> it = keySet().iterator();
        for (int i = 0; it.hasNext(); ++i)
            result[i] = it.next().intValue();
        return result;
    }
    public long extensionCount() {
        long extensionCount = 0l;
        for (IntNode node : values())
            extensionCount += node.count();
        return extensionCount;
    }
}

