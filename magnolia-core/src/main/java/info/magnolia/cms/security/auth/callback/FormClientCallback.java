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
package info.magnolia.cms.security.auth.callback;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.security.auth.login.LoginException;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.freemarker.FreemarkerUtil;
import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.auth.login.LoginFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.ClassUtils;

/**
 * @author Sameer Charles
 * $Id$
 */
public class FormClientCallback extends AbstractHttpClientCallback {

    private static final Logger log = LoggerFactory.getLogger(FormClientCallback.class);

    public static final String ERROR_STRING = "errorString";

    private String loginForm;

    public void doCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!response.isCommitted()) {
                response.setContentType("text/html");
                if (null == request.getCharacterEncoding()) {
                    response.setCharacterEncoding(MIMEMapping.getContentEncodingOrDefault("text/html"));
                }
            }
            FreemarkerUtil.process(getLoginForm(), getMessages(request), response.getWriter());
        }
        catch (Throwable t) {
            log.error("exception while writing login template", t);
        }
    }

    public String getLoginForm() {
        return loginForm;
    }

    public void setLoginForm(String loginForm) {
        this.loginForm = loginForm;
    }

    /**
     * simply sets "errorString" in case of login exception.
     * override this to pass more objects to the freemarker template.
     * @return an empty map
     */
    protected Map getMessages(HttpServletRequest request) {
        // FIXE revert that
        LoginException exception = (LoginException) request.getAttribute(LoginFilter.ATTRIBUTE_LOGINERROR);
        Map messages = new HashMap();
        if (null != exception) {
            final String exName = ClassUtils.getShortClassName(exception, null);
            final Messages mm = MessagesManager.getMessages();
            final String defaultMessage = mm.get("login.defaultError");
            messages.put(ERROR_STRING, mm.getWithDefault("login." + exName, defaultMessage));
        }
        return messages;
    }

}
