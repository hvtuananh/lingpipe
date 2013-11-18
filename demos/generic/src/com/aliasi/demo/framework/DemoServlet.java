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

import com.aliasi.xml.SAXWriter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * The <code>DemoServlet</code> class provides a servlet interface
 * to streaming demos.  The demo servlet presents demos in three
 * ways: web form, file upload and web service, each with its own
 * method of transmitting property information.
 *
 * <p>See the Web demo instructions for more information on using
 * this class.</p>
 *
 * @author  Bob Carpenter
 * @version 2.3.0
 * @since   LingPipe2.3
 */
public class DemoServlet extends HttpServlet {

    static final long serialVersionUID = 6980095913648079890L;

    private StreamDemo mDemo;

    /**
     * Construct a demo servlet.
     */
    public DemoServlet() { }

    /**
     * Initialize the demo servlet.  This is the initialization
     * called by the servlet container.  It constructs the
     * demo using reflection given the parameters.
     */
    public void init() throws ServletException {
        String demoClass = getInitParameter("demoConstructor");
        String demoConstructorArgs
            = getInitParameter("demoConstructorArgs");

        try {
            mDemo
                = Constants
                .constructDemo(demoClass,
                               demoConstructorArgs);
        } catch (IllegalArgumentException e) {
            String msg = "Error constructing demo="
                + e;
            throw new ServletException(e);
        } catch (ClassCastException e) {
            String msg = "Could not convert class=" + demoClass
                + " to instance of StreamDemo.";
            throw new ServletException(e);
        }
    }

    /**
     * Destroys this demo by null-ing out the resources used.
     */
    public void destroy() {
        mDemo = null;
    }

    /**
     * Handles <code>GET</code> requests by deferring them
     * to <code>POST</code> requests.
     *
     * @param request Servlet equest.
     * @param response Servlet response.
     * @throws IOException If there is an underlying I/O error.
     * @throws ServletException If there is an underlying servlet
     * error.
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {

        doPost(request,response);
    }

    /**
     * Handles <code>POST</code> requests.  First it determines what
     * type of demo is involved by looking at the path information.
     * If the extension is <code>/textInput.html</code>, it generates
     * the input form for the text input demo.  If the extension is
     * <code>/fileInput.html</code>, it generates the input form for
     * file upload.  If neither extension is present, it generates
     * the output for the demo, based on the properties determined
     * from the input.
     *
     * @param request Servlet equest.
     * @param response Servlet response.
     * @throws IOException If there is an underlying I/O error.
     * @throws ServletException If there is an underlying servlet
     * error.
     */
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
        throws IOException, ServletException {

        String pathInfo = request.getPathInfo();
        if (pathInfo.equals("/response.xml")) {
            generateOutput(request,response);
        } else if (pathInfo.equals("/textInput.html")) {
            generateInputForm(request,response,true);
        } else if (pathInfo.equals("/fileInput.html")) {
            generateInputForm(request,response,false);
        }
    }

    void generateOutput(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException {

        InputStream in = null;
        OutputStream out = null;

        try {
            response.setContentType(mDemo.responseType());
            out = response.getOutputStream();


            @SuppressWarnings("unchecked") // bad inherited API from commons
            Properties properties
                = mapToProperties((Map<String,String[]>)request.getParameterMap());

            String reqContentType = request.getContentType();

            if (reqContentType == null
                || reqContentType.startsWith("text/plain")) {

                properties.setProperty("inputType","text/plain");
                String reqCharset = request.getCharacterEncoding();
                if (reqCharset != null)
                    properties.setProperty("inputCharset",reqCharset);
                in = request.getInputStream();

            } else if (reqContentType
                       .startsWith("application/x-www-form-urlencoded")) {

                String codedText = request.getParameter("inputText");
                byte[] bytes = codedText.getBytes("ISO-8859-1");
                in = new ByteArrayInputStream(bytes);

            } else if (ServletFileUpload
                       .isMultipartContent
                       (new ServletRequestContext(request))) {

                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload uploader = new ServletFileUpload(factory);
                @SuppressWarnings("unchecked") // bad commons API
                List<FileItem> items = (List<FileItem>) uploader.parseRequest(request);
                Iterator<FileItem> it = items.iterator();
                while (it.hasNext()) {
                    log("found item");
                    FileItem item = it.next();
                    if (item.isFormField()) {
                        String key = item.getFieldName();
                        String val = item.getString();
                        properties.setProperty(key,val);
                    } else {
                        byte[] bytes = item.get();
                        in = new ByteArrayInputStream(bytes);
                    }
                }

            } else {
                System.out.println("unexpected content type");
                String msg = "Unexpected request content"
                    + reqContentType;
                throw new ServletException(msg);
            }
            mDemo.process(in,out,properties);
        } catch (FileUploadException e) {
            throw new ServletException(e);
        } finally {
            Streams.closeQuietly(in);
            Streams.closeQuietly(out);
        }
    }

    private void generateInputForm(HttpServletRequest request,
                                   HttpServletResponse response,
                                   boolean isTextField)
        throws IOException, ServletException {

        OutputStream out = null;
        try {
            response.setContentType("text/html");
            out = response.getOutputStream();
            String charset
                = mDemo.defaultProperties()
                .getProperty("Input Character Encoding");
            if (charset == null) {
                charset = "UTF-8";
            }
            SAXWriter saxWriter = new SAXWriter(out,charset,XHTML_MODE);
            saxWriter.setDTDString(XHTML_DTD_STRING);
            saxWriter.startDocument();
            Attributes htmlAtts
                = SAXWriter
                .createAttributes("xmlns","http://www.w3.org/1999/xhtml",
                                  "xml:lang","en");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("html",htmlAtts);

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("head");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("title");
            saxWriter.characters(mDemo.title());
            saxWriter.endSimpleElement("title");

            saxWriter.characters("\n");
            Attributes httpAtts
                = SAXWriter
                .createAttributes("http-equiv",
                                  "Content-type",
                                  "content",
                                  "application/xhtml+xml; charset=" + charset);
            saxWriter.startEndSimpleElement("meta",httpAtts);


            saxWriter.characters("\n");
            saxWriter.startSimpleElement("style","type","text/css");
            saxWriter.characters("\n");
            saxWriter.characters(CSS_STYLE);
            saxWriter.characters("\n");
            saxWriter.endSimpleElement("style");
            saxWriter.characters("\n");
            saxWriter.endSimpleElement("head");

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("body");

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("div","id","header");

            Attributes logoAtts
                = SAXWriter
                .createAttributes("href","http://alias-i.com",
                                  "id","logo");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("a",logoAtts);
            Attributes imgAtts
                = SAXWriter
                .createAttributes("src","http://alias-i.com/lingpipe/web/img/logo-small.gif",
                                  "alt","Alias-i Logo");
            saxWriter.startSimpleElement("img",imgAtts);
            saxWriter.endSimpleElement("img");
            saxWriter.endSimpleElement("a");

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("h1","id","pageTitle");
            saxWriter.characters(mDemo.title());
            saxWriter.endSimpleElement("h1");

            saxWriter.characters("\n");
            saxWriter.endSimpleElement("div"); // id=header

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("h2","id","poweredBy");
            saxWriter.characters("Powered by ");
            saxWriter.startSimpleElement("a","href","http://alias-i.com/lingpipe");
            saxWriter.characters("LingPipe");
            saxWriter.endSimpleElement("a");
            saxWriter.endSimpleElement("h2");


            String[] lines = mDemo.description().split("\n");
            if (lines.length > 0) {
                saxWriter.characters("\n");
                saxWriter.startSimpleElement("div","id","description");
                for (int i = 0; i < lines.length; ++i) {
                    saxWriter.characters("\n");
                    saxWriter.startSimpleElement("p");
                    if (i == 0) {
                        saxWriter.startSimpleElement("b");
                        saxWriter.characters("This Demo:");
                        saxWriter.endSimpleElement("b");
                    }
                    saxWriter.characters(" ");
                    saxWriter.characters(lines[i]);
                    saxWriter.endSimpleElement("p");
                }
                saxWriter.characters("\n");
                saxWriter.endSimpleElement("div");
            }

            String actionPath =
                request.getContextPath()
                + request.getServletPath()
                + "/response.xml";

            Attributes formAtts
                = isTextField
                ? SAXWriter.createAttributes("method","post",
                                             "action",actionPath)
                : SAXWriter.createAttributes("method","post",
                                             "action",actionPath,
                                             "enctype","multipart/form-data");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("form",formAtts);

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("div","id","license");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("p");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("b");
            saxWriter.characters("Terms of Use:");
            saxWriter.endSimpleElement("b");
            saxWriter.characters(" This demo is for evaluation purposes only.");
            saxWriter.characters(" Please do not overload our demo server. ");
            saxWriter.startSimpleElement("a","href",
                                         "http://alias-i.com/lingpipe/web/contact.html");
            saxWriter.characters("Contact Alias-i");
            saxWriter.endSimpleElement("a");
            saxWriter.characters(" about other uses.");
            saxWriter.endSimpleElement("p");
            saxWriter.endSimpleElement("div"); // license

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("div","id","input");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("h4");
            saxWriter.characters((isTextField ? "Text" : "File") + " Input");
            saxWriter.endSimpleElement("h4");

            if (isTextField) {
                Attributes textAreaAtts
                    = SAXWriter
                    .createAttributes("name","inputText",
                                      "rows","12",
                                      "cols","60");
                saxWriter.characters("\n");
                saxWriter.startEndSimpleElement("textarea",textAreaAtts);

            } else {
                Attributes fileUploadAtts
                    = SAXWriter
                    .createAttributes("type","file",
                                      "name","inputFile",
                                      "size","50%",
                                      "id","fileUpload");
                saxWriter.characters("\n");
                saxWriter.startEndSimpleElement("input",fileUploadAtts);
            }


            saxWriter.characters("\n");
            saxWriter.startEndSimpleElement("br");

            Attributes inputAtts
                = isTextField
                ? SAXWriter
                  .createAttributes("type","submit",
                                    "value","Submit Text",
                                    "id","submitButton",
                                    "title","Click to analyze input text.")
                : SAXWriter
                .createAttributes("type","submit",
                                  "value","Submit File",
                                  "id","submitButton",
                                  "title",
                                  "Click to upload and analyze selected file.");
            saxWriter.characters("\n");
            saxWriter.startEndSimpleElement("input",inputAtts);

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("span","id","switch");
            saxWriter.characters("(or switch to:");
            if (isTextField) {
                saxWriter.startSimpleElement("a","href","fileInput.html");
                saxWriter.characters("File Input Form");
            } else {
                saxWriter.startSimpleElement("a","href","textInput.html");
                saxWriter.characters("Text Input Form");
            }
            saxWriter.endSimpleElement("a");
            saxWriter.characters(")");
            saxWriter.endSimpleElement("span");

            if (!isTextField) {
                saxWriter.startEndSimpleElement("br");
                saxWriter.startEndSimpleElement("br");
                saxWriter.startEndSimpleElement("br");
                saxWriter.startEndSimpleElement("br");
                saxWriter.startEndSimpleElement("br");
                saxWriter.startEndSimpleElement("br");
                saxWriter.startEndSimpleElement("br");
                saxWriter.startEndSimpleElement("br");
                saxWriter.startEndSimpleElement("br");
            }


            saxWriter.characters("\n");
            saxWriter.endSimpleElement("div");

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("div","id","params");

            Map<String,String[]> propertyDeclarations = mDemo.propertyDeclarations();
            Iterator<Map.Entry<String,String[]>> entryIt 
                = propertyDeclarations.entrySet().iterator();
            while (entryIt.hasNext()) {
                Map.Entry<String,String[]> entry = entryIt.next();
                String key = entry.getKey();
                String[] vals = entry.getValue();
                String toolTip = mDemo.propertyToolTips().get(key);
                saxWriter.characters("\n");
                saxWriter.startSimpleElement("h4");
                saxWriter.characters(key);
                saxWriter.endSimpleElement("h4");
                if (vals.length > 1) {
                    Attributes selectAtts
                        = (toolTip != null)
                        ? SAXWriter.createAttributes("name",key,
                                                     "title",toolTip)
                        : SAXWriter.createAttributes("name",key);
                    saxWriter.characters("\n");
                    saxWriter.startSimpleElement("select",selectAtts);
                    for (int i = 0; i < vals.length; ++i) {
                        saxWriter.characters("\n");
                        saxWriter.startSimpleElement("option","value",vals[i]);
                        saxWriter.characters(vals[i]);
                        saxWriter.endSimpleElement("option");
                    }
                    saxWriter.characters("\n");
                    saxWriter.endSimpleElement("select");
                } else {
                    Attributes paramInputAtts
                        = (vals.length == 1)
                        ? SAXWriter.createAttributes("type","text",
                                                     "name",key,
                                                     "value",vals[0])
                        : SAXWriter.createAttributes("type","text",
                                                     "name",key);
                    saxWriter.characters("\n");
                    saxWriter.startEndSimpleElement("input",paramInputAtts);
                }
            }

            saxWriter.characters("\n");
            saxWriter.endSimpleElement("div");

            saxWriter.characters("\n");
            saxWriter.endSimpleElement("form");


            saxWriter.characters("\n");
            saxWriter.startSimpleElement("div","id","footInstructions");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("div","id","instructions");
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("h3");
            saxWriter.characters("General Web Demo Instructions");
            saxWriter.endSimpleElement("h3");

            saxWriter.characters("\n");
            saxWriter.startSimpleElement("ul");
            writeItems(GENERAL_WEB_INSTRUCTIONS,saxWriter);
            if (isTextField)
                writeItems(TEXT_WEB_INSTRUCTIONS,saxWriter);
            else
                writeItems(FILE_WEB_INSTRUCTIONS,saxWriter);

            saxWriter.characters("\n");
            saxWriter.endSimpleElement("ul");

            saxWriter.startSimpleElement("h3");
            saxWriter.characters("For more info: ");
            saxWriter.startSimpleElement("a","href","http://alias-i.com/web/lingpipe/demos.html");
            saxWriter.characters("Complete LingPipe Demo Instructions");
            saxWriter.endSimpleElement("a");
            saxWriter.endSimpleElement("h3");

            saxWriter.characters("\n");
            saxWriter.endSimpleElement("div"); // instructions

            Map<String,String> modelToResource = mDemo.modelToResource();
            Map<String,String> tutorialToUrl = mDemo.tutorialToUrl();

            if ((modelToResource.size() > 0)
                || (tutorialToUrl.size() > 0)) {

                saxWriter.characters("\n");
                saxWriter.startSimpleElement("div","id","resources");

                saxWriter.characters("\n");
                saxWriter.startSimpleElement("h3");
                saxWriter.characters("Related LingPipe Resources");
                saxWriter.endSimpleElement("h3");

                if (tutorialToUrl.size() > 0) {
                    saxWriter.characters("\n");
                    saxWriter.startSimpleElement("h4");
                    saxWriter.characters("Tutorials");
                    saxWriter.endSimpleElement("h4");

                    saxWriter.characters("\n");
                    saxWriter.startSimpleElement("ul");
                    Iterator<Map.Entry<String,String>> it = tutorialToUrl.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String,String> entry = it.next();
                        String tutorialName = entry.getKey();
                        String tutorialUrl = entry.getValue();
                        saxWriter.characters("\n");
                        saxWriter.startSimpleElement("li");
                        saxWriter.startSimpleElement("a","href",tutorialUrl);
                        saxWriter.characters(tutorialName);
                        saxWriter.endSimpleElement("a");
                        saxWriter.endSimpleElement("li");
                    }
                    saxWriter.characters("\n");
                    saxWriter.endSimpleElement("ul");
                }

                if (modelToResource.size() > 0) {
                    saxWriter.characters("\n");
                    saxWriter.startSimpleElement("h4");
                    saxWriter.characters("Models");
                    saxWriter.endSimpleElement("h4");

                    saxWriter.characters("\n");
                    saxWriter.startSimpleElement("ul");
                    Iterator<Map.Entry<String,String>> it = modelToResource.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String,String> entry = it.next();
                        String modelName = entry.getKey();
                        String modelResourcePath = entry.getValue();
                        saxWriter.characters("\n");
                        saxWriter.startSimpleElement("li");
                        saxWriter.startSimpleElement("a","href","../resource" + modelResourcePath);
                        saxWriter.characters(modelName);
                        saxWriter.endSimpleElement("a");
                        saxWriter.endSimpleElement("li");
                    }
                    saxWriter.characters("\n");
                    saxWriter.endSimpleElement("ul");
                }

                saxWriter.characters("\n");
                saxWriter.endSimpleElement("div"); // resources
            }

            saxWriter.characters("\n");
            saxWriter.endSimpleElement("div"); // footInstructions


	    //Allows Google Analytics to track site usage
	    saxWriter.characters("\n");
	    saxWriter.startSimpleElement("script","type","text/javascript");
	    saxWriter.characters("\n");
	    saxWriter.characters(
				 "var gaJsHost = ((\'https:\' == document.location.protocol) ? \'https://ssl.\' : \'http://www.\');\n" 
				+ "document.write(unescape(\'%3Cscript src=\'\' + gaJsHost + \'google-analytics.com/ga.js\' type=\'text/javascript\'%3E%3C/script%3E\'));\n");
	    saxWriter.endSimpleElement("script");
	    saxWriter.characters("\n");
	    saxWriter.startSimpleElement("script","type","text/javascript");
	    saxWriter.characters("\n");
	    saxWriter.characters("try var pageTracker = _gat._getTracker(\'UA-15123726-1\'); pageTracker._trackPageview(); } catch(err) {}\n");

	    saxWriter.endSimpleElement("script");
	    saxWriter.characters("\n");			

            saxWriter.characters("\n");
            saxWriter.endSimpleElement("body");

            saxWriter.characters("\n");
            saxWriter.endSimpleElement("html");
            saxWriter.endDocument();
        } catch (SAXException e) {
            throw new ServletException(e);
        } finally {
            Streams.closeQuietly(out);
        }
    }

    static void writeItems(String text, SAXWriter saxWriter)
        throws SAXException {

        String[] paragraphs = text.split("\n");
        for (int i = 0; i < paragraphs.length; ++i) {
            saxWriter.characters("\n");
            saxWriter.startSimpleElement("li");
            saxWriter.characters("\n");
            saxWriter.characters(paragraphs[i]);
            saxWriter.characters("\n");
            saxWriter.endSimpleElement("li");
        }
    }

    private Properties mapToProperties(Map<String,String[]> map) {
        Properties reqProperties
            = new Properties(mDemo.defaultProperties());
        Iterator<Map.Entry<String,String[]>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,String[]> entry = it.next();
            String key = entry.getKey();
            String[] vals = entry.getValue();
            if (vals == null || vals.length == 0) continue;
            reqProperties.setProperty(key,vals[0]);
        }
        return reqProperties;
    }

    static final boolean XHTML_MODE = true;

    private static final String XHTML_DTD_STRING
        = "<!DOCTYPE html"
        + " PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
        + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";


    private static final String GENERAL_WEB_INSTRUCTIONS
        =  "Set the web browser's character encoding based on the encoding of text to be submitted (Use browser menu=View, submenu=Encoding)."
        + "\n"
        + "Set the input character encoding to match the actual encoding of the input bytes."
        + "\n"
        + "Set the input type selection to match the type of the input, either plain text, HTML or XML."
        + "\n"
        + "Set the output character encoding to any value; it need not match the input character set or browser.";

    private static final String TEXT_WEB_INSTRUCTIONS
        = "Cut-and-paste or enter text in the specified character encoding."
        + "\n"
        + "To analyze a file, first switch to the file input form, the link for which is next to the submit button.";

    private static final String FILE_WEB_INSTRUCTIONS
        = "Files may be analyzed by entering an absolute path to them in the file input field."
        + " They may also be selected by clicking on the browse button and opening the desired file."
        + " This will write its absolute path into the input field."
        + " Use the submit button to upload the file for analysis."
        + "\n"
        + "To analyze direct text input, first switch to the text input form, the link for which is next to the submit button.";


    private static final String CSS_STYLE
        = "* { margin: 0; padding: 0; font-family: verdana, arial, sans-serif; background-color: #DDD; border: none; font-size: 1em; }"
        + "\n"
        + "#header {background-color: #272651; padding: 0; color: #272651; margin: 0 0 1px 0; border: 15px solid #DDD; border-top: 1em solid #DDD; border-left: 1.15em solid #DDD; border-bottom: 1px solid #DDD; }"
        + "\n"
        + "h1#pageTitle { font-size: 1.5em; color: #EEE; background-color: #272651; padding: 0.75em 0 .75em .333em; }"
        + "\n"
        + "a#logo { float: right; display: inline; padding: 1em 1em 0 0; background-color: #272651; }"
        + "\n"
        + "h2 { font-size: 1em; }"
        + "\n"
        + "h2#poweredBy { float: right; font-size: 0.8em; font-weight: normal; background-color: #272651; color: #EEE; margin: 0; padding: 1em 1.5em; border: 15px solid #DDD; border-top: none }"
        + "\n"
        + "h2#poweredBy a { font-size: 1.5em; font-weight: normal; background-color: #272651; color: #F7EB00; }"
        + "\n"
        + "h3 { font-size: 1em; margin: 1.0em; }"
        + "\n"
        + "h4 { font-size: 0.8em; margin: .25em 0; }"
        + "\n"
        + "#description p { width: 100%; margin-bottom: 0.25em; padding: 0.5em; background-color: #EEE; border: 1px solid #888; font-size: 0.75em;  overflow:hidden }"
        + "\n"
        + "#description p b { background-color: #EEE; }"
        + "\n"
        + "#description { width: 30em; padding: 1.15em 0.75em 1.15em 1.15em; line-height: 140%; }"
        + "\n"
        + "#license { clear: both; width: 30em; padding: 0em 0.75em 1.15em 1.15em; line-height: 140%; }"
        + "\n"
        + "#license p { width: 100%; margin-bottom: 0.25em; padding: 0.75em 0.5em; background-color: #EEE; border: 1px solid #888; font-size: 0.75em;  overflow:hidden }"
        + "\n"
        + "#license p b { background-color: #EEE; }"
        + "\n"
        + "#license p a { background-color: #EEE; }"
        + "\n"
        + "#input { float: left; width: 30em; padding: 1.15em .75em 1.15em 1.15em; }"
        + "\n"
        + "#input textarea { width: 100%; margin-bottom: 0.25em; padding: 0.5em; background-color: #FFF; border: 1px solid #888; font-size: 0.8em;  overflow:hidden }"
        + "\n"
        + "#input #fileUpload { margin-bottom: 0.25em; padding: 0.5em; background-color:#FFF; border: 1px solid black; font-size: 0.8em; }"
        + "\n"
        + "#input #submitButton { float: left; font-size: 0.8em; font-weight: bold; padding: 0.5em; margin-bottom: 1.5em; background-color: #FFF; border: 1px solid #888; }"
        + "\n"
        + "#switch { font-weight: normal; font-size: .8em; float: right; text-align: right; }"
        + "\n"
        + "#params { padding-top: 1.75em; padding-left: 3em; margin-left: 30em; }"
        + "\n"
        + "#params h4 { margin: 1em 0 .1em 0; padding: 0; }"
        + "\n"
        + "#params select { font-size: 0.9em;  background-color: #EEE; border: 1px solid #333; padding: 0;  }"
        + "\n"
        + "#params input { font-size: 0.9em;  background-color: #EEE; border: 1px solid #888; }"
        + "\n"
        + "#footInstructions { clear: both; float: left; width: 30em; padding: 1.15em 0.75em 1.15em 1.15em; }"
        + "\n"
        + "#instructions { width: 100%; margin-bottom: 0.25em; padding: 0.5em; background-color: #EEE; border: 1px solid #888; font-size: 0.8em;  overflow:hidden }"
        + "\n"
        + "#instructions h3 { font-weight: bold; margin: 0em 1em 0 0.5em; background: #EEE; padding-top: 0.75em; }"
        + "\n"
        + "#instructions ul { margin: 0 1.5em 0.5em 2.0em; background: #EEE; }"
        + "\n"
        + "#instructions ul li { margin: 0.75em 0 0 0; background: #EEE; }"
        + "\n"
        + "#resources { width: 100%; margin-bottom: 0.25em; margin-top: 1em; padding: 0.5em; background-color: #EEE; border: 1px solid #888; font-size: 0.8em;  overflow:hidden }"
        + "\n"
        + "#resources h3 { font-size: 1em; background: #EEE; margin: 0; padding: 0.5em 0.5em; }"
        + "\n"
        + "#instructions a { background: #EEE; }"
        + "\n"
        + "#resources h4 { font-size: 0.9em;  margin: 0; padding: 0.5em 0.5em 0 0.5em; background-color: #EEE; }"
        + "\n"
        + "#resources ul { margin: 0 1.5em 0.5em 2.0em; background: #EEE; }"
        + "\n"
        + "#resources ul li { margin: 0.5em 0 0 0; background: #EEE; }"
        + "\n"
        + "#resources ul li a { background: #EEE; font-size: 0.9em; }"
        + "\n"
        ;

}


