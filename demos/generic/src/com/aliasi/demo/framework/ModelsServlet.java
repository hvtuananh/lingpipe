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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>ModelsServlet</code> provides a means of serving
 * resources as octet streams.  The content type of the
 * return is <code>application/octet-stream</code>.  The path
 * for the resource is derived from the request using
 * <code>getPathInfo()</code>.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.3
 */
public class ModelsServlet extends HttpServlet {

    static final long serialVersionUID = 5172692720244475582L;

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
     * Handles <code>POST</code> requests.  This simply involves
     * getting the resource as a stream from the resource path
     * provided by the request and copying it to the output.
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
	
	String resourcePath = request.getPathInfo();
	response.setContentType("application/octet-stream");
	OutputStream out = null;
	InputStream in = null;
	try {
	    in = DemoServlet.class.getResourceAsStream(resourcePath);
	    out = response.getOutputStream();
	    Streams.copy(in,out);
	} finally {
	    Streams.closeQuietly(in);
	    Streams.closeQuietly(out);
	}
    }

}
