/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.servlets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of a MVCHandler. Calls the command through reflection.
 * @author Philipp Bracher
 * @version $Id$
 */
public abstract class MVCServletHandlerImpl implements MVCServletHandler {

    protected static final String VIEW_ERROR = "error"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MVCServletHandlerImpl.class);

    protected HttpServletRequest request;

    protected HttpServletResponse response;

    private String name;

    private String command;

    protected MVCServletHandlerImpl(String name, HttpServletRequest request, HttpServletResponse response) {
        this.name = name;
        this.setRequest(request);
        this.setResponse(response);
        init();
    }

    public void init() {
        try {
            BeanUtils.populate(this, getRequest().getParameterMap());
        }
        catch (Exception e) {
            log.error("can't set properties on the handler", e);
        }
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandler#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Call the method through reflection
     * @param command
     * @return the name of the view to show (used in renderHtml)
     */
    public String execute(String command) {
        String view = VIEW_ERROR;
        Method method;

        try {
            method = this.getClass().getMethod(command, new Class[]{});
            // method.setAccessible(true);
            view = (String) method.invoke(this, new Object[]{});
        }
        catch (InvocationTargetException e) {
            log.error("can't call command: " + command, e.getTargetException()); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("can't call command: " + command, e); //$NON-NLS-1$
        }

        return view;
    }

    /**
     * @param request The request to set.
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * @return Returns the request.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * @param response The response to set.
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * @return Returns the response.
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * @return Returns the command.
     */
    public String getCommand() {
        return this.command;
    }

    /**
     * @param command The command to set.
     */
    public void setCommand(String command) {
        this.command = command;
    }
}
