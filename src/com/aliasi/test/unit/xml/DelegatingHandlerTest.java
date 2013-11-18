package com.aliasi.test.unit.xml;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;


import java.io.CharArrayReader;
import java.io.IOException;

import java.util.ArrayList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

public class DelegatingHandlerTest  {

    @Test
    public void testOne() throws IOException, SAXException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        TopHandler testHandler = new TopHandler();
        xmlReader.setContentHandler(testHandler);
        String chars
            = "<DOC>"
            + "<A><LIST><ELT>a</ELT><ELT>b</ELT><ELT>c</ELT></LIST></A>"
            + "<B><LIST><ELT>1</ELT><ELT>2</ELT></LIST></B>"
            + "</DOC>";
        CharArrayReader charReader = new CharArrayReader(chars.toCharArray());
        InputSource inSource = new InputSource(charReader);
        xmlReader.parse(inSource);
        String[] expectedAValue = new String[] { "a", "b", "c" };
        String[] expectedBValue = new String[] { "1", "2" };
        assertArrayEquals(expectedAValue,testHandler.aValue());
        assertArrayEquals(expectedBValue,testHandler.bValue());
    }

    @Test
    public void testMulti() throws IOException, SAXException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        MultiHandler handler = new MultiHandler();
        xmlReader.setContentHandler(handler);

        String chars
            = "<A><B>foo</B><C><B>bar</B></C></A>";
        CharArrayReader reader
            = new CharArrayReader(chars.toCharArray());
        InputSource in = new InputSource(reader);
        xmlReader.parse(in);
        assertEquals("foo",handler.b());
        assertEquals("bar",handler.c());
    }


}


class MultiHandler extends DelegatingHandler {
    TextAccumulatorHandler mBHandler = new TextAccumulatorHandler();
    TextAccumulatorHandler mCHandler = new TextAccumulatorHandler();
    public MultiHandler() {
        setDelegate("B",mBHandler);
        setDelegate("C",mCHandler);
    }
    @Override
    public void startDocument() throws SAXException {
        mBHandler.reset();
        mCHandler.reset();
        super.startDocument();
    }
    public String b() {
        return mBHandler.getText();
    }
    public String c() {
        return mCHandler.getText();
    }
}

class ListHandler2 extends DelegateHandler {
    private final DefaultHandler mElementHandler;
    private final ArrayList mElements = new ArrayList();
    public ListHandler2(DelegatingHandler delegatingHandler) {
        super(delegatingHandler);
        mElementHandler = new TextAccumulatorHandler();
        setDelegate("ELT",mElementHandler);
    }
    @Override
    public void startDocument() throws SAXException {
        reset();
        super.startDocument();
    }
    @Override
    public void finishDelegate(String qName, DefaultHandler handler) {
        if ("ELT".equals(qName)) {
            String eltValue = ((TextAccumulatorHandler) handler).getText();
            mElements.add(eltValue);
        }
    }
    public void reset() {
        mElements.clear();
    }
    public String[] value() {
        String[] result = new String[mElements.size()];
        mElements.toArray(result);
        return result;
    }
}


class AHandler extends DelegateHandler {
    ListHandler2 mListHandler;
    public AHandler(DelegatingHandler delegatingHandler) {
        super(delegatingHandler);
        mListHandler = new ListHandler2(delegatingHandler);
        setDelegate("LIST",mListHandler);
    }
    public String[] value() {
        return mListHandler.value();
    }
    @Override
    public void finishDelegate(String name, DefaultHandler handler) {
        /* do nothing */
    }
    public void reset() {
        mListHandler.reset();
    }
}

class BHandler extends DelegateHandler {
    ListHandler2 mListHandler;
    public BHandler(DelegatingHandler delegatingHandler) {
        super(delegatingHandler);
        mListHandler = new ListHandler2(delegatingHandler);
        setDelegate("LIST",mListHandler);
    }
    public String[] value() {
        return mListHandler.value();
    }
    @Override
    public void finishDelegate(String name, DefaultHandler handler) {
        /* do nothing */
    }
    public void reset() {
        mListHandler.reset();
    }
}


class TopHandler extends DelegatingHandler {
    AHandler mAHandler;
    BHandler mBHandler;
    public TopHandler() {
        mAHandler = new AHandler(this);
        setDelegate("A",mAHandler);
        mBHandler = new BHandler(this);
        setDelegate("B",mBHandler);
    }
    @Override
    public void startDocument() throws SAXException {
        mAHandler.reset();
        super.startDocument();
    }
    @Override
    public void finishDelegate(String name, DefaultHandler handler) {
        /* do nothing */
    }
    public String[] aValue() {
        return mAHandler.value();
    }
    public String[] bValue() {
        return mBHandler.value();
    }
}


