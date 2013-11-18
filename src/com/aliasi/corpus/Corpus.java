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

package com.aliasi.corpus;

import java.io.IOException;

/**
 * The <code>Corpus</code> abstract class provides a basis for passing
 * training and testing data to data handlers.  The methods walk
 * handlers over the training and/or test data, depending on which of
 * the methods is called.
 *
 * @author Bob Carpenter
 * @version 3.0
 * @since LingPipe2.3
 * @param <H> the type of handler to which this corpus sends events
 */
public class Corpus<H extends Handler> {

    /**
     * Construct a corpus.
     */
    protected Corpus() { 
        /* only for protection */
    }

    /**
     * Visit the entire corpus, sending all extracted events to the
     * specified handler.
     *
     * <p>This is just a convenience method that is defined by:
     * <blockquote><pre>
     * visitCorpus(handler,handler);
     * </pre></blockquote>
     *
     * @param handler Handler for events extracted from the corpus.
     * @throws IOException If there is an underlying I/O error.
     */
    public void visitCorpus(H handler) 
        throws IOException {

        visitCorpus(handler,handler);
    }

    /**
     * Visit the entire corpus, first sending training events to the
     * specified training handler and then sending testing events to
     * the test handler.
     *
     * <p>This is just a convenience method that is defined by:
     * <blockquote><pre>
     * visitTrain(trainHandler);
     * visitTest(testHandler);
     * </pre></blockquote>
     *
     * @param trainHandler Handler for training events from the corpus.
     * @param testHandler Handler for testing events from the corpus.
     * @throws IOException If there is an underlying I/O error.
     */
    public void visitCorpus(H trainHandler, H testHandler) 
        throws IOException {

        visitTrain(trainHandler);
        visitTest(testHandler);
    }

    /**
     * Visit the training section of the corpus, sending events to the
     * specified handler.
     *
     * <p>The implementation does nothing.  This method should be
     * overridden by subclasses that contain training data.
     *
     * @param handler Handler for training events.
     * @throws IOException If there is an underlying I/O error.
     */
    public void visitTrain(H handler) 
        throws IOException {
        
        /* override with subclass to do something */
    }

    /**
     * Visit the testing section of the corpus, sending events to the
     * specified handler.
     *
     * <p>The implementation does nothing.  This method should be
     * overridden by subclasses that contain test data.
     *
     * @param handler Handler for training events.
     * @throws IOException If there is an underlying I/O error.
     */
    public void visitTest(H handler) 
        throws IOException {

        /* override by subclass to do something */
    }

}
