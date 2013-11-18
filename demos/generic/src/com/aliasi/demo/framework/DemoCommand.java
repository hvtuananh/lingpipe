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

import com.aliasi.util.Streams;

import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The <code>DemoCommand</code> class provides a command-line interface
 * to stream demos.  Properties are specified with command-line
 * arguments, and text is provided by file, by directory, or through
 * standard input/output.
 *
 * <p>See the superclass documentation and the command-line demo
 * instructions for more information on using this class.
 *
 * @author  Bob Carpenter
 * @version 2.3.0
 * @since   LingPipe2.3
 */
public class DemoCommand extends AbstractDemoCommand {

    /**
     * Construct a demo command with the specified command-line arguments
     * providing property values.
     *
     * @param args Command-line arguments.
     */
    public DemoCommand(String[] args) {
	super(args);
    }

    /**
     * Runs the command-line demo.  This method does all the I/O, as
     * well as calling the demo's process method.
     */
    public void run() {
	String inDirName = getArgument(Constants.INPUT_DIR_PARAM);
	if (inDirName != null) {
	    String outDirName = getArgument(Constants.OUTPUT_DIR_PARAM);
	    if (outDirName == null) {
		String msg = "If input directory is specified, output directory must be specified.";
		throw new IllegalArgumentException(msg);
	    }
	    File inDir = new File(inDirName);
	    File outDir = new File(outDirName);
	    processDirectory(mDemo,inDir,outDir,
			     mCommandProperties);
	} else {
	    String inFileName = getArgument(Constants.INPUT_FILE_PARAM);
	    File inFile = (inFileName != null)
		? new File(inFileName)
		: null;
	    String outFileName = getArgument(Constants.OUTPUT_FILE_PARAM);
	    File outFile = (outFileName != null)
		? new File(outFileName)
		: null;
	    processFile(mDemo,inFile,outFile,
			mCommandProperties);
	}
    }

    void processDirectory(StreamDemo demo, File inDir, File outDir,
			  Properties properties) {
	if (!outDir.isDirectory()) outDir.mkdirs();
	System.out.println("Input directory=" + inDir
			   + " Ouptut directory=" + outDir);
	File[] files = inDir.listFiles();
	for (int i = 0; i < files.length; ++i) {
	    File outFile = new File(outDir,files[i].getName());
	    if (files[i].isDirectory())
		processDirectory(demo,files[i],outFile,properties);
	    else
		processFile(demo,files[i],outFile,properties);
	}
    }

    void processFile(StreamDemo demo, 
		     File inFile, File outFile,
		     Properties properties) {
	InputStream in = null;
	OutputStream out = null;
	try {
	    if (inFile != null) {
		in = new FileInputStream(inFile);
		System.out.print("Input file=" + inFile);
	    } else {
		in = System.in;
	    }

	    if (outFile != null) {
		out = new FileOutputStream(outFile);
		System.out.println(" Output file=" + outFile);
	    } else {
		out = System.out;
	    }
	    demo.process(in,out,properties);
	} catch (IOException e) {
	    System.out.println("IOException processing " 
			       + " inFile=" + inFile
			       + " outFile=" + outFile);
	    System.out.println("Exception msg=" + e);
	    e.printStackTrace(System.out);
	} finally {
	    Streams.closeQuietly(in);
	    Streams.closeQuietly(out);
	}
    }

    /**
     * This method allows the demo command to be used from the
     * command line.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
	new DemoCommand(args).run();
    }

}
