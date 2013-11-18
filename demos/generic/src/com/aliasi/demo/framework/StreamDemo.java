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

package com.aliasi.demo.framework;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.Properties;

/**
 * The <code>StreamDemo</code> interface provides the necessary
 * method declarations to support streaming demos that act on
 * input/output streams given properties.
 *
 * @author  Bob Carpenter
 * @version 2.3.0
 * @since   LingPipe2.3
 */
public interface StreamDemo {

    /**
     * Initialize the demo.  This will only be called once, some time
     * before the first call to {@link
     * #process(InputStream,OutputStream,Properties)}.
     */
    public void init();

    /**
     * Returns the title of the demo. 
     *
     * @return The title of the demo.
     */
    public String title();

    /**
     * Returns a textual description of the demo.  This should
     * not contain any markup.
     *
     * @return The description of the demo.
     */
    public String description();

    /**
     * Returns a map from property names to arrays of strings of possible
     * values.  If there value is null, then any input is allowed.
     *
     * @return Map from property names to arrays of possible values.
     */
    public Map<String,String[]> propertyDeclarations();

    /**
     * Returns a map from property names to simple descriptions of
     * what they are used for.  These may be used for tool tips in
     * graphical presentations.
     *
     * @return The tool tips for this demo.
     */
    public Map<String,String> propertyToolTips();

    /**
     * Returns a map from model names to resource names.  The resources
     * should be available on the classpath if they are to be served.
     * Typically, this is used to provide models through the web demos.
     *
     * @return A mapping from model names to resource names.
     */
    public Map<String,String> modelToResource();

    /**
     * Returns a map from tutorial names to the URLs of the page
     * containing the tutorial.
     *
     * @return A mapping from tutorial names to URLs.
     */
    public Map<String,String> tutorialToUrl();
    
    /**
     * Returns a properties object containing the default settings
     * of the properties.  This will be used as the basis of a new
     * properties object that may be changed by the demo.
     *
     * @return The default property settings for this demo.
     */
    public Properties defaultProperties();

    /**
     * Returns the response type for this demo.  All of our
     * demos are set up to return <code>&quot;text/xml&quot;</code>.
     *
     * @return The response type for this demo.
     */
    public String responseType();

    /**
     * This method does the actual work of the demo, reading from
     * the input stream and writing to the output stream according
     * to the specified properties.  This method should <b>not</b> close
     * the streams when it is done with them.
     *
     * @param in Stream from which to read.
     * @param out Stream to which to write.
     * @param properties Properties for this demo call.
     * @throws IOException If there is an underlying I/O error in the
     * streams.
    */
    public void process(InputStream in, OutputStream out, 
			Properties properties) 
	throws IOException;
}
