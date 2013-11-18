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

import java.io.IOException;

import java.util.Stack;

/**
 * A <code>MultiTrieReader</code> merges two trie readers, providing
 * output that is the result of adding the counts from the two readers.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.3
 */
public class MultiTrieReader implements TrieReader {

    private final Stack<Long> mStack1 = new Stack<Long>();
    private final Stack<Long> mStack2 = new Stack<Long>();

    private final TrieReader mReader1;
    private final TrieReader mReader2;

    private boolean mNotInitialized = true;

    /**
     * Construct a multiple trie reader that returns counts that
     * are the sum of the two readers' counts.
     *
     * @param reader1 First reader.
     * @param reader2 Second reader.
     */
    public MultiTrieReader(TrieReader reader1, TrieReader reader2) {
        mReader1 = reader1;
        mReader2 = reader2;
    }

    public long readSymbol() throws IOException {
        if (mStack1.size() > mStack2.size()) {
            long symbol = mStack1.peek();
            if (symbol == -1L) {
                mStack1.pop();
                replace(mStack1,mReader1);
            } else {
                replace(mStack1,-2L);
            }
            return symbol;
        }
        if (mStack1.size() < mStack2.size()) {
            long symbol = mStack2.peek();
            if (symbol == -1L) {
                mStack2.pop();
                replace(mStack2,mReader2);
            } else {
                replace(mStack2,-2L);
            }
            return symbol;
        }

        // same stack size
        long top1 = mStack1.peek();
        long top2 = mStack2.peek();
        if (top1 == -1 && top2 == -1) {  
            mStack1.pop();
            replace(mStack1,mReader1);
            mStack2.pop();
            replace(mStack2,mReader2);
            return -1L;
        }
        if (top2 == -1L || (top1 != -1L && top1 < top2)) {
            replace(mStack1,-2L);
            return top1;
        } 
        if (top1 == -1L || (top2 != -1L && top2 < top1)) {
            replace(mStack2,-2L);
            return top2;
        }
    
        // top1 == top2, top1 != -1
        replace(mStack1,-2L);
        replace(mStack2,-2L);
        return top1;
    }

    public long readCount() throws IOException {
        if (mNotInitialized) {
            mNotInitialized = false;
            long count = mReader1.readCount() + mReader2.readCount();
            mStack1.push(mReader1.readSymbol());
            mStack2.push(mReader2.readSymbol());
            return count;
        } 
        if (mStack1.size() > mStack2.size()) {
            long count = mReader1.readCount();
            mStack1.push(mReader1.readSymbol());
            return count;
        }
        if (mStack1.size() < mStack2.size()) {
            long count = mReader2.readCount();
            mStack2.push(mReader2.readSymbol());
            return count;
        }
        if (mStack1.peek() == -2 && mStack2.peek() == -2) {
            long count = mReader1.readCount() + mReader2.readCount();
            mStack1.push(mReader1.readSymbol());
            mStack2.push(mReader2.readSymbol());
            return count;
        }
        if (mStack1.peek() == -2) {
            long count = mReader1.readCount();
            mStack1.push(mReader1.readSymbol());
            return count;
        } 
        if (mStack2.peek() == -2) {
            long count = mReader2.readCount();
            mStack2.push(mReader2.readSymbol());
            return count;
        }
        throw new IllegalStateException("readCount(): Stack1=" + mStack1
                                        + " Stack2=" + mStack2);
    }


    static void replace(Stack<Long> stack, TrieReader reader) throws IOException {
        if (stack.size() > 0)
            replace(stack,reader.readSymbol());
    }

    static void replace(Stack<Long> stack, long x) {
        if (stack.size() == 0) return;
        stack.pop();
        stack.push(Long.valueOf(x));
    }

}
