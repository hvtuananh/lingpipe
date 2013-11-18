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

import java.util.Stack;

/**
 * @author  Bob Carpenter
 * @version 3.8
 */
class BitTrie {

    private final Stack<Long> mLastSymbolStack 
        = new Stack<Long>();

    protected BitTrie() { 
        /* no op constructor */
    }

    long popValue() {
        return mLastSymbolStack.pop().longValue();
    }

    long pushValue(long n) {
        mLastSymbolStack.push(Long.valueOf(n));
        return n;
    }

    static void checkCount(long n) {
        if (n > 0) return;
        String msg = "All counts must be positive."
            + " Found count=" + n;
        throw new IllegalArgumentException(msg);
    }
    
}
