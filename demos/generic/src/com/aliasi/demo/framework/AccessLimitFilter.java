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


import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This filter limits access by IP address.  It takes two
 * initialization parameters that control the maximum number of
 * accesses (<code>maxAccesses</code>) and time between resets
 * (<code>resetInterval</code>).  It does not persist between
 * init/destroy lifecycle events.
 * 
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.3
 */
public class AccessLimitFilter implements Filter {

    private ObjectToCounterMap<String> mIPCounter;
    private int mMaxAccesses;
    private long mResetInterval;
    private long mStartTime;

    /**
     * Do nothing.
     */
    public void destroy() { 
    }

    /**
     * Initialize time, counters, and maximum access/reset interval.
     *
     * @param config Configuration information.
     */
    public void init(FilterConfig config) { 
	mStartTime = System.currentTimeMillis();
	mIPCounter = new ObjectToCounterMap<String>();

	String maxAccessString = config.getInitParameter("maxAccesses");
	try {
	    mMaxAccesses = Integer.valueOf(maxAccessString);
	    System.out.println("AccessLimitFilter: Found maxAccesses=" + mMaxAccesses);
	} catch (Exception e) {
	    mMaxAccesses = DEFAULT_MAX_ACCESSES;
	}

	String mInterval = config.getInitParameter("resetInterval");
	try {
	    mResetInterval = Long.valueOf(mInterval);
	    System.out.println("AccessLimitFilter: Found resetInterval=" + mResetInterval);
	} catch (Exception e) {
	    mResetInterval = DEFAULT_RESET_INTERVAL;
	}
    }


    /**
     * Perform the filtering logic, testing if an IP address is over limit.
     * If it's overlimit, a report is generated to that effect.  If it's
     * not overlimit, the request is passed along the filter chain.
     *
     * @param request User request.
     * @param response System response.
     * @param chain Downstream filter chain.
     * @throws IOException If there is an underlying I/O error reading or writing.
     * @throws ServletException If there is an underlying servlet exception.
     */
    public void doFilter(ServletRequest request, ServletResponse response, 
			 FilterChain chain) 
	throws IOException, ServletException {

	// bail altogether if can't find remote address
	String remoteAddr = request.getRemoteAddr();
	if (remoteAddr == null) {
	    overLimitReport(remoteAddr,response,0L);
	    return;
	}

	// check whether or not to reset counter
	long timeBeforeReset = timeBeforeReset();
	if (timeBeforeReset < 0L) {
	    timeBeforeReset = 0L;
	    synchronized (this) {
		mStartTime = System.currentTimeMillis();
		mIPCounter = new ObjectToCounterMap<String>();
	    }
	}

	// check limit & increment
	synchronized (this) {
	    if (mIPCounter.getCount(remoteAddr) >= mMaxAccesses) {
		overLimitReport(remoteAddr,response,timeBeforeReset);
		return;
	    }
	    mIPCounter.increment(remoteAddr);
	}
	chain.doFilter(request,response);
    }

    void overLimitReport(String ipAddress, ServletResponse response, 
			 long timeBeforeReset) 
	throws ServletException, IOException {

	response.setContentType("text/html");
	response.setCharacterEncoding(Strings.UTF8);
	Writer writer = response.getWriter();
	writer.write("<html><head></head><body>");
	writer.write("<h1>Access Limit Exceeded</h1>");
	if (ipAddress == null) {
	    writer.write("<p>Unknown IP Addresses not allowed.</p>");
	} else {
	    writer.write("<p>IP Address=" 
			 + ipAddress);
	    writer.write("<p>Maximum Accesses=" 
			 + mMaxAccesses + "</p>");
	    writer.write("<p>Time between resets=" 
			 + Strings.msToString(mResetInterval));
	    writer.write("<p>Time before next reset (HH:MM:SS)="
			 + Strings.msToString(timeBeforeReset) + "</p>");
	}
	writer.write("</body></html>");
	writer.close();
    }

    long timeBeforeReset() {
	long elapsedTime = System.currentTimeMillis() - mStartTime;
	return mResetInterval - elapsedTime;
    }
					
    private static final int DEFAULT_MAX_ACCESSES = 32;

    
    private static final long DEFAULT_RESET_INTERVAL 
	= 24L // hours/day
	* 60L // minutes/hour
	* 60L // seconds/minute
	* 1000L // milliseconds/second
	;  

}
    
