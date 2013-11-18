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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.nio.charset.Charset;

import java.util.Map;
import java.util.Set;

/**
 * The <code>Constants</code> class simply provides constants
 * used in other classes.
 *
 * @author  Bob Carpenter
 * @version 2.3.0
 * @since   LingPipe2.3
 */
public class Constants {

    public final static String LOGO_PATH
	= "/com/aliasi/demo/framework/logo-small.gif";

    public final static String INPUT_FILE_PARAM
	= "inFile";

    public final static String OUTPUT_FILE_PARAM
	= "outFile";

    public final static String INPUT_DIR_PARAM
	= "inDir";

    public final static String OUTPUT_DIR_PARAM
	= "outDir";

    public final static String DEMO_CONSTRUCTOR_PARAM
	= "demoConstructor";

    public final static String DEMO_CONSTRUCTOR_ARGS_PARAM
	= "demoConstructorArgs";

    public final static String OUTPUT_CHAR_ENCODING_PARAM
	= "outCharset";

    public final static String INPUT_CHAR_ENCODING_PARAM
	= "inCharset";

    public final static String CONTENT_TYPE_PARAM 
	= "contentType";

    public final static String REMOVE_ELTS_PARAM
	= "removeElts";

    public final static String INCLUDE_ELTS_PARAM
	= "includeElts";

    public final static String EXCLUDE_ELTS_PARAM
	= "excludeElts";

    public final static String TEXT_PLAIN
	= "text/plain";

    public final static String TEXT_XML
	= "text/xml";

    public final static String TEXT_HTML
	= "text/html";



    public final static String[] AVAILABLE_CONTENT_TYPES
	= new String[] { TEXT_PLAIN,
			 TEXT_XML,
			 TEXT_HTML };

    public static String[] AVAILABLE_CHARSETS;
    static {
	Map<String,Charset> availableCharsetMap = Charset.availableCharsets();
	Set<String> availableCharsetSet = availableCharsetMap.keySet();
	
	String[] allCharsets
	    = availableCharsetSet.toArray(new String[0]);
	
	String defaultCharset = getDefaultCharset();

	AVAILABLE_CHARSETS = new String[allCharsets.length+1];
	System.arraycopy(allCharsets,0,AVAILABLE_CHARSETS,1,
			 allCharsets.length);
	AVAILABLE_CHARSETS[0] = defaultCharset;
    }

    public static String getDefaultCharset() {
	// in 1.5: return Charset.defaultCharset().name();
	ByteArrayInputStream bytesIn = new ByteArrayInputStream(new byte[0]);
	InputStreamReader reader = new InputStreamReader(bytesIn);
	String charsetName = reader.getEncoding();
	Charset charset = Charset.forName(charsetName);
	return charset.name();
    }


    public static StreamDemo constructDemo(String demoClassName,
					   String demoConstructorArgsParam) 
	throws ClassCastException {

	if (demoClassName == null) {
	    String msg = "Require init parameter=demoConstructor"
		+ " with value set to name of instance of StreamDemo"
		+ " with implementation on the classpath.";
	    throw new IllegalArgumentException(msg);
	}

	Object[] demoConstructorArgs 
	    = (demoConstructorArgsParam == null)
	    ? new String[0]
	    : demoConstructorArgsParam.split(",");
        
        @SuppressWarnings({"unchecked","rawtypes"})
        Class<?>[] argClasses = (Class<?>[]) new Class[demoConstructorArgs.length];
	java.util.Arrays.fill(argClasses,String.class);

        try {
            Class<?> consClass = Class.forName(demoClassName);
            Constructor<?> cons = consClass.getConstructor(argClasses);
	    StreamDemo demo
		= (StreamDemo) cons.newInstance(demoConstructorArgs);
	    demo.init();
	    return demo;
        } catch (IllegalAccessException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (InstantiationException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (InvocationTargetException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (ExceptionInInitializerError e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (ClassNotFoundException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (NoSuchMethodException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        }
	return null;
    }

    static void printDebug(Throwable e,
			   String demoClassName,
			   Object[] demoConstructorArgs) {
	System.out.println("Exception in constructor=" + e);
	System.out.println("  demoClassName=|" + demoClassName + "|");
	System.out.println("  demoConstructorArgs=");
	for (int i = 0; i < demoConstructorArgs.length; ++i)
	    System.out.println("    " + i + "=|" + demoConstructorArgs[i] + "|");
	System.out.println("  stack trace=");
	e.printStackTrace(System.out);
	
	throw new IllegalArgumentException("Exception=" + e);
    }

}