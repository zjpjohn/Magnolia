/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.Filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public abstract class BaseFilter implements Filter {

    private FilterConfig filterConfig;

    protected BaseFilter() {
    }

    /**
     *
     */
    public void init(final FilterConfig filterConfig) {
        setFilterConfig(filterConfig);
    }

    /**
     *
     */
    public void destroy() {
        this.filterConfig = null;
    }

    /**
     * All filtering tasks which are common to filters extending BaseFilter.
     * @param req , Servlet request as given by servlet container
     * @param res , Servlet response as given by servlet container
     * @param filterChain , FilterChain object available to the developer
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException,
        javax.servlet.ServletException {
        filterChain.doFilter(req, res);
    }

    /**
     * @param filterConfig , defined in web.xml
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * @return FilterConfig object
     */
    public FilterConfig getFilterConfig() {
        return this.filterConfig;
    }
}
