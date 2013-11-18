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

package com.aliasi.test.unit;

import com.aliasi.util.Tuple;

import java.util.ArrayList;

public class MockObjectHelper {

    private final ArrayList mCalls = new ArrayList();

    public MockObjectHelper() { 
        /* do nothing */
    }

    public void add(String method) {
        mCalls.add(Tuple.create(new Object[] { method }));
    }

    public void add(String method, Object arg1) {
        mCalls.add(Tuple.create(new Object[] { method, arg1 }));
    }

    public void add(String method, Object arg1, Object arg2) {
        mCalls.add(Tuple.create(new Object[] { method, arg1, arg2 }));
    }

    public void add(String method, Object arg1, Object arg2, Object arg3) {
        mCalls.add(Tuple.create(new Object[] { method, arg1, arg2, arg3 }));
    }

    public void add(String method, Object arg1, Object arg2, Object arg3,
                    Object arg4) {
        mCalls.add(Tuple.create(new Object[] { method, arg1, arg2, arg3,
                                               arg4}));
    }

    public void add(String method, Object arg1, Object arg2, Object arg3,
                    Object arg4, Object arg5) {
        mCalls.add(Tuple.create(new Object[] { method, arg1, arg2, arg3,
                                               arg4, arg5}));
    }

    public void add(String method, Object arg1, Object arg2, Object arg3,
                    Object arg4, Object arg5, Object arg6) {
        mCalls.add(Tuple.create(new Object[] { method, arg1, arg2, arg3,
                                  arg4, arg5, arg6}));
    }

    public ArrayList getCalls() {
        return mCalls;
    }

}
