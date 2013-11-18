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
package com.aliasi.demo.demos;

import com.aliasi.demo.framework.AbstractTextDemo;

import com.aliasi.xml.SAXWriter;

import java.util.Properties;

import org.xml.sax.SAXException;

/**
 * The <code>EchoDemo</code> is a text demo that simply pipes
 * its input to its output.  Because the parent class is
 * able to handle arbitrary character sets on input and output,
 * this demo may be used for converting one character set to
 * another.  Because the parent class normalizes HTML to XHTML,
 * this demo may be used for HTML normalization.
 * 
 * @author  Bob Carpenter
 * @version 2.3.0
 * @since   LingPipe2.3
 */
public class EchoDemo extends AbstractTextDemo {

    /**
     * A constructor callable by reflection.
     */
    public EchoDemo() { } 

    /** 
     * This constructor is for uniformity in creation by reflection.
     *
     * @param ignore Ignore the input.  
     */
    public EchoDemo(String ignore) {     
    }

    public String title() {
	return "Echo Demo";
    }

    public String description() {
	return "This is the echo demo."
	    + " It may be used for transcoding character sets."
	    + " It may also be used for normalizing HTML to XHTML";
    }

    public void process(char[] cs, int start, int end,
			SAXWriter writer,
			Properties properties) 
	throws SAXException {

	writer.characters(cs,start,end);
    }

}
