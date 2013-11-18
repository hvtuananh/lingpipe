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

import com.aliasi.util.Arrays;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;


import com.aliasi.xml.GroupCharactersFilter;
import com.aliasi.xml.RemoveElementsFilter;
import com.aliasi.xml.SAXFilterHandler;
import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.TextContentFilter;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cyberneko.html.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The <code>AbstractTextDemo</code> class provides a basis for stream
 * demos dealing with text.  It sets up default properties for dealing
 * with character sets, input content type, and some XML/HTML-specific
 * input element information.  The abstract text demos operate over
 * plain text, HTML or XML input, and provide XML output.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.3
 */
public abstract class AbstractTextDemo implements StreamDemo {

    private final Map<String,String[]> mPropertyDeclarations = new HashMap<String,String[]>();

    private final Map<String,String> mPropertyToolTips = new HashMap<String,String>();

    private final Map<String,String> mModelToResource = new HashMap<String,String>();
    private final Map<String,String> mTutorialToUrl = new HashMap<String,String>();

    private final Properties mDefaultProperties
        = new Properties();

    private final String mTitle;
    private final String mDescription;

    /**
     * Construct an abstract text demo with default title and description.
     */
    public AbstractTextDemo() {
        this("Demo","This is a demo.");
    }

    /**
     * Construct an abstract text demow ith the specified title and description.
     * The title is returned by {@link #title()} and the description
     * by {@link #description()}.

     * @param title Title of the demo.
     * @param description Text description of the demo.
     */
    public AbstractTextDemo(String title,
                            String description) {
        mTitle = title;
        mDescription = description;
        declareProperty(Constants.INPUT_CHAR_ENCODING_PARAM,
                        Constants.AVAILABLE_CHARSETS,
                        IN_CHARSET_TIP);
        declareProperty(Constants.OUTPUT_CHAR_ENCODING_PARAM,
                        Constants.AVAILABLE_CHARSETS,
                        OUT_CHARSET_TIP);
        declareProperty(Constants.CONTENT_TYPE_PARAM,
                        Constants.AVAILABLE_CONTENT_TYPES,
                        IN_CONTENT_TYPE_TIP);
        declareProperty(Constants.INCLUDE_ELTS_PARAM,
                        new String[0],
                        INCLUDE_ELTS_TIP);
        declareProperty(Constants.REMOVE_ELTS_PARAM,
                        new String[0],
                        REMOVE_ELTS_TIP);
    }

    /**
     * Declares a property for this demo with the specified key, legal
     * values and tool tip.  If the values are given as
     * <code>null</code>, any possible value is allowed.
     *
     * @parma key Name of the property.
     * @param values Array of legal property values.
     * @param tooltip Tool tip for this property.
     */
    public void declareProperty(String key, String[] values, 
                                String tooltip) {
        mPropertyToolTips.put(key,tooltip);
        mPropertyDeclarations.put(key,values);
        if (values != null && values.length > 0) {
            String defaultValue = values[0];
            mDefaultProperties.setProperty(key,defaultValue);
        }
    }

    /**
     * Adds the specified tutorial at the specified URL.
     *
     * @param tutorial Name of tutorial.
     * @param url URL of tutorial.
     */
    public void addTutorial(String tutorial, String url) {
        mTutorialToUrl.put(tutorial,url);
    }

    /**
     * Adds a the specified resource for the specified model name.
     *
     * @param model Model name.
     * @param resourcePath Path to resource from the class.
     */
    public void addModel(String model, String resourcePath) {
        mModelToResource.put(model,resourcePath);
    }

    /**
     * Returns the mapping of model names to resources.  The mapping
     * is constructed from calls to {@link #addModel(String,String)}.
     *
     * @return The mapping from model names to resource paths.
     */
    public Map<String,String> modelToResource() {
        return mModelToResource;
    }

    /**
     * Returns the mapping of tutorial names to URLs.  The mapping
     * is constructed from calls to {@link #addTutorial(String,String)}.
     *
     * @return The mapping from tutorial names to URLs.
     */
    public Map<String,String> tutorialToUrl() {
        return mTutorialToUrl;
    }

    /**
     * Do-nothing implementation.
     */
    public void init() { 
    }

    /**
     * Returns the default properties.  These are established for
     * general text demos, plus whatever was set by {@link
     * #declareProperty(String,String[],String)}.
     *
     * @return The default mapping from properties to values.
     */
    public Properties defaultProperties() {
        return mDefaultProperties;
    }

    /**
     * Returns the mapping from properties to legal values.  The value
     * will be <code>null</code> if any input is allowed.  These
     * values derive from the underlying text demo properties plus
     * whatever was set by {@link
     * #declareProperty(String,String[],String)}.
     *
     * @return The property declarations for this demo.
     */
    public Map<String,String[]> propertyDeclarations() {
        return mPropertyDeclarations;
    }

    /**
     * Returns the mapping from properties to tool tips.  These values
     * derive from the underlying text demo tool tips plus whatever
     * was set by {@link #declareProperty(String,String[],String)}.
     *
     * @return The property declarations for this demo.
     */
    public Map<String,String> propertyToolTips() {
        return mPropertyToolTips;
    }

    /**
     * Returns the title specified in the constructor.
     *
     * @return The title specified in the constructor.
     */
    public String title() {
        return mTitle;
    }

    /**
     * Returns the description specified in the constructor.
     *
     * @return The description specified in the constructor.
     */
    public String description() {
        return mDescription;
    }

    /**
     * Returns the XML content type, <code>text/xml</code>.
     */
    public String responseType() {
        return "text/xml";
    }

    /**
     * This method implements the basic stream demo process method.
     * It handles all input/output character sets and all aspects of
     * the XML/HTML handling.  The actual demo processing is done by
     * calling the abstract method {@link
     * #process(char[],int,int,SAXWriter,Properties)}.
     */
    public void process(InputStream in, OutputStream out, 
                        Properties properties) 
        throws IOException {

        try {
            String outCharset 
                = properties.getProperty(Constants.OUTPUT_CHAR_ENCODING_PARAM);
            SAXWriter saxWriter = new SAXWriter(out,outCharset,!XHTML_MODE); // don't need XML
            
            String inCharset 
                = properties.getProperty(Constants.INPUT_CHAR_ENCODING_PARAM);

            String inType = properties.getProperty(Constants.CONTENT_TYPE_PARAM);
            if (inType.startsWith(Constants.TEXT_PLAIN)) {
                char[] cs = Streams.toCharArray(in,inCharset);
                saxWriter.startDocument();
                saxWriter.startSimpleElement("output");
                process(cs,0,cs.length,saxWriter,properties);
                saxWriter.endSimpleElement("output");
                saxWriter.endDocument();
                return;
            }

            // annotator
            DefaultHandler handler
                = new ProcessHandler(saxWriter,properties);
            InputSource inSource = new InputSource(in);
            inSource.setEncoding(inCharset);
            XMLReader xmlReader = null;
            if (inType.startsWith(Constants.TEXT_XML)) {
                xmlReader = XMLReaderFactory.createXMLReader();
            } else if (inType.startsWith(Constants.TEXT_HTML)) {
                xmlReader = new SAXParser();
            } else {
                String msg = "Unexpected input content type=" + inType;
                throw new SAXException(msg);
            }

            // restrict to included elements
            String eltsToAnnotateCSV 
                = properties.getProperty(Constants.INCLUDE_ELTS_PARAM);
            if (nonEmpty(eltsToAnnotateCSV)) {
                handler = new IncludeElementHandler(eltsToAnnotateCSV,
                                                    saxWriter,
                                                    handler);
            }
            
            handler = new GroupCharactersFilter(handler);
                
            // remove elements
            String eltsToRemoveCSV 
                = properties.getProperty(Constants.REMOVE_ELTS_PARAM);
            if (nonEmpty(eltsToRemoveCSV)) {
                String[] eltsToRemove = eltsToRemoveCSV.split(",");
                RemoveElementsFilter filter 
                    = new RemoveElementsFilter(handler);
                for (int i = 0; i < eltsToRemove.length; ++i)
                    filter.removeElement(eltsToRemove[i]);
                handler = filter;
            }

            // parse and handle
            xmlReader.setContentHandler(handler);
            xmlReader.parse(inSource);
        } catch (SAXException e) {
            throw new IOException("SAXException=" + e);
        }
    }

    /**
     * This is the method called to actually process text provided as
     * a character slice, writing output to the specified SAX writer,
     * given the specified properties.
     *
     * @param cs Underlying characters.
     * @param start Index of the first character of slice.
     * @param end Index of one past the last character of the slice.
     * @param writer SAXWriter to which output is written.
     * @param properties Properties for the processing.
     * @throws SAXException If there is an error during processing.
     */
    public abstract void process(char[] cs, int start, int end,
                                 SAXWriter writer,
                                 Properties properties) 
        throws SAXException;

    private class ProcessHandler extends SAXFilterHandler {
        final Properties mProperties;
        final SAXWriter mWriter;
        ProcessHandler(SAXWriter writer, Properties properties) {
            super(writer);
            mWriter = writer;
            mProperties = properties;
        }
        public void characters(char[] cs, int start, int length) 
            throws SAXException {

            process(cs,start,start+length,mWriter,mProperties);
        }
    }

    /**
     * Reads the resource of the specified name relative
     * to this class and returns it as an object.  The
     * input stream for reading is created using:
     *
     * <pre>in = this.getClass().getResourceAsStream(resourceName);</pre>
     *
     * <p>This method returns <code>null</code> if there is an error
     * reading the resource or if it is not found.
     *
     * @param resourceName Name of resource.
     * @return The value of the resource.
     */
    protected Object readResource(String resourceName) {
        InputStream in = null;
        BufferedInputStream bufIn = null;
        ObjectInputStream objIn = null;
        try {
            in = this.getClass().getResourceAsStream(resourceName);
            if (in == null) {
                String msg = "Could not open stream for resource="
                    + resourceName;
                throw new IOException(msg);
            }
            bufIn = new BufferedInputStream(in);
            objIn = new ObjectInputStream(bufIn);
            return objIn.readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.toString());
        } finally {
            Streams.closeQuietly(objIn);
            Streams.closeQuietly(bufIn);
            Streams.closeQuietly(in);
        }
    }


    final static String INCLUDE_ELTS_TIP
        = "Comma-separated array of elements whose text content will be annotated.";

    final static String REMOVE_ELTS_TIP
        = "Comma-separated array of elements whose tags will be removed from the output.";

    final static String IN_CHARSET_TIP 
        = "Character encoding of input. All encodings shown, default first.";

    final static String OUT_CHARSET_TIP 
        = "Char encoding of output.  All encodings shown, default first.";

    final static String IN_CONTENT_TYPE_TIP 
        = "Set to the content type of the input text.";

    static final boolean XHTML_MODE = true;

    static boolean nonEmpty(String s) {
        return s != null
            && s.length() > 0;
    }

    static class IncludeElementHandler extends TextContentFilter {
        DefaultHandler mIncludedEltHandler;
        IncludeElementHandler(String eltsToAnnotateCSV,
                              SAXWriter writer,
                              DefaultHandler includedEltHandler) {
            super(writer);
            mIncludedEltHandler = includedEltHandler;
            String[] eltsToAnnotate = eltsToAnnotateCSV.split(",");
            for (int i = 0; i < eltsToAnnotate.length; ++i)
                filterElement(eltsToAnnotate[i]);
        }
        public void filteredCharacters(char[] cs, int start, int length) 
            throws SAXException {

            mIncludedEltHandler.characters(cs,start,length);
        }
    }



}
