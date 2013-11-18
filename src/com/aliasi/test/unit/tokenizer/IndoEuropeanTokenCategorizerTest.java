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

package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.TokenCategorizer;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class IndoEuropeanTokenCategorizerTest  {

    @Test
    public void testCategorize() throws Exception {
        assertTokenCategory("","NULL-TOK");
        assertTokenCategory("1","1-DIG");
        assertTokenCategory("22","2-DIG");
        assertTokenCategory("333","3-DIG");
        assertTokenCategory("4444","4-DIG");
        assertTokenCategory("666666","5+-DIG");
        assertTokenCategory("a55","DIG-LET");
        assertTokenCategory("5a","DIG-LET");
        assertTokenCategory("5a66-bb","DIG-LET");
        assertTokenCategory("$%^&*()hdjh76jdj","DIG-LET");
        assertTokenCategory("555-1212","DIG--");
        assertTokenCategory("5/12/12","DIG-/");
        assertTokenCategory("5/12/12.","DIG-/");
        assertTokenCategory("500,000","DIG-,");
        assertTokenCategory("500,000.97","DIG-,");
        assertTokenCategory("500.000","DIG-.");
        assertTokenCategory("A","1-LET-UP");
        assertTokenCategory("B","1-LET-UP");
        assertTokenCategory("ABC","LET-UP");
        assertTokenCategory("Abc","LET-CAP");
        assertTokenCategory("fooBar","LET-MIX");
        assertTokenCategory("FooBar","LET-MIX");
        assertTokenCategory(";:..","PUNC-");
        assertTokenCategory(";:..","PUNC-");
        assertTokenCategory(").","OTHER");
        assertTokenCategory("^&*(a","OTHER");
    }


    private void assertTokenCategory(String token, String category) 
    throws Exception {

        IndoEuropeanTokenCategorizer categorizer = IndoEuropeanTokenCategorizer.CATEGORIZER;
        assertEquals("Expected category(" + token + ")="
                     + category
                     + "; but was category="
                     + categorizer.categorize(token),
                     category,categorizer.categorize(token));

    TokenCategorizer c_categorizer = (TokenCategorizer)
        AbstractExternalizable.compile(categorizer);
        assertEquals("Expected category(" + token + ")="
                     + category
                     + "; but was category="
                     + c_categorizer.categorize(token),
                     category,c_categorizer.categorize(token));
    }

}
