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

import com.aliasi.util.AbstractCommand;


import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * The <code>AbstractDemoCommand</code> class provides a basis for
 * the command-line and GUI demos, both of which are launched from
 * the command line.  Basically, this involves setting up default
 * constants and properties.
 *
 * @author  Bob Carpenter
 * @version 2.3.0
 * @since   LingPipe2.3
 */
public abstract class AbstractDemoCommand extends AbstractCommand {

    /**
     * The underlying demo for this command.
     */
    protected final StreamDemo mDemo;

    /**
     * The default properties for the command. These consist of the
     * default properties specified in the {@link
     * #AbstractCommand(String[],Properites)} constructor, with
     * overriding properties drawn from the command's default
     * properties.
     */
    protected final Properties mCommandProperties;

    /**
     * Construct an abstract demo command from the specified arguments.
     * This percolates up to the superclass with a newly created set
     * of properties instantiated from the command-line arguments.
     *
     * @param args Command-line arguments.
     */
    public AbstractDemoCommand(String[] args) {
	this(args,new Properties());
    }

    /**
     * Construct a demo command from the specified command-line arguments
     * with the specified default properties.  This method basically
     * constructs the demo from its class name and a comma-separated
     * array of arguments.  It uses the demo's default properties to
     * override any specified default properties.  
     *
     * @param args Command-line arguments.
     * @param defaultProperties Default properties for the command.
     */
    public AbstractDemoCommand(String[] args, Properties defaultProperties) {
	super(args,defaultProperties);
	String demoClass
	    = getArgument(Constants.DEMO_CONSTRUCTOR_PARAM);
	String demoConstructorArgs
	    = getArgument(Constants.DEMO_CONSTRUCTOR_ARGS_PARAM);
	mDemo
	    = Constants.constructDemo(demoClass,
				      demoConstructorArgs);

	Properties commandArgs = getArguments();
	Properties demoDefaults = mDemo.defaultProperties();
	mCommandProperties 
	    = new Properties(demoDefaults);
	mCommandProperties.putAll(commandArgs); // commands override defaults
    }

}
