/**
 * This file Copyright (c) 2003-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.context;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.objectfactory.Components;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


/**
 * Context implementation keeping track of the current request, response, servletContext and pageContext.
 * @author Sameer Charles
 * @version $Id$
 */
public abstract class WebContextImpl extends UserContextImpl implements WebContext {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebContextImpl.class);

    private static final long serialVersionUID = 222L;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private ServletContext servletContext;

    /**
     * the jsp page context.
     */
    private PageContext pageContext;

    protected AggregationState aggregationState;

    private Stack<HttpServletResponse> responseStack = new Stack<HttpServletResponse>();

    private Stack<HttpServletRequest> requestStack = new Stack<HttpServletRequest>();

    /**
     * Use init to initialize the object.
     */
    public WebContextImpl() {
        log.debug("new WebContextImpl() {}", this);
    }

    @Override
    public void init(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
        //reset();
        //setUser(getAnonymousUser());
        setAttributeStrategy(new RequestAttributeStrategy(this));
        setRepositoryStrategy(createRepositoryStrategy());
    }

    private DefaultRepositoryStrategy createRepositoryStrategy() {
        return Components.newInstance(DefaultRepositoryStrategy.class, this);
    }

    @Override
    public AggregationState getAggregationState() {
        if (aggregationState == null) {
            aggregationState = newAggregationState();
        }
        return aggregationState;
    }

    /**
     * @see info.magnolia.context.WebContextFactoryImpl#newAggregationState()
     */
    protected abstract AggregationState newAggregationState();

    /**
     * This will only reset the original URI/URL by calling {@link AggregationState#resetURIs()}.
     */
    @Override
    public void resetAggregationState() {
        getAggregationState().resetURIs();
    }

    /**
     * Get form object assembled by <code>MultipartRequestFilter</code>.
     * @return multipart form object
     */
    @Override
    public MultipartForm getPostedForm() {
        return (MultipartForm) getAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME, LOCAL_SCOPE);
    }

    /**
     * Get parameter value as string.
     * @return parameter value
     */
    @Override
    public String getParameter(String name) {
        return this.request.getParameter(name);
    }

    /**
     * Get parameter values as string[].
     * @return parameter values
     */
    @Override
    public String[] getParameterValues(String name) {
        return this.request.getParameterValues(name);
    }

    /**
     * Get parameter values as a Map<String, String> (unlike HttpServletRequest.getParameterMap() which returns a Map<String,
     * String[]>, so don't expect to retrieve multiple-valued form parameters here).
     * @return parameter values
     */
    @Override
    public Map<String, String> getParameters() {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration<String> paramEnum = this.request.getParameterNames();
        while (paramEnum.hasMoreElements()) {
            final String name = paramEnum.nextElement();
            map.put(name, this.request.getParameter(name));
        }
        return map;
    }

    /**
     * Avoid the call to this method where ever possible.
     * @return Returns the request.
     */
    @Override
    public HttpServletRequest getRequest() {
        return this.request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    @Override
    public String getContextPath() {
        return this.request.getContextPath();
    }

    /**
     * Does an include using the request that was set when setting up this context, or using the
     * request wrapped by the pageContext if existing.
     */
    @Override
    public void include(final String path, final Writer out) throws ServletException, IOException {
        try {
            final ServletRequest requestToUse = /*pageContext != null ? pageContext.getRequest() :*/ this.getRequest();
            final HttpServletResponse responseToUse = /*(pageContext != null && pageContext.getResponse() instanceof HttpServletResponse) ? (HttpServletResponse) pageContext.getResponse() :*/ response;
            final WriterResponseWrapper wrappedResponse = new WriterResponseWrapper(responseToUse, out);

            requestToUse.getRequestDispatcher(path).include(requestToUse, wrappedResponse);
        }
        catch (ServletException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageContext getPageContext() {
        return pageContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void login() {
        setRepositoryStrategy(createRepositoryStrategy());
    }

    /**
     * Closes opened JCR sessions and invalidates the current HttpSession.
     * @see #release()
     */
    @Override
    public void logout() {
        releaseJCRSessions();
        super.logout();

        HttpSession session = this.request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Closes opened JCR sessions.
     */
    @Override
    public void release() {
        releaseJCRSessions();
        this.request = null;
        this.response = null;
    }

    protected void releaseJCRSessions() {
        getRepositoryStrategy().release();
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#pop()
     */
    @Override
    public void pop() {
        request = requestStack.pop();
        response = responseStack.pop();
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#push(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void push(HttpServletRequest request, HttpServletResponse response) {
        requestStack.push(this.request);
        this.request = request;
        responseStack.push(this.response);
        this.response = response;
    }
}
