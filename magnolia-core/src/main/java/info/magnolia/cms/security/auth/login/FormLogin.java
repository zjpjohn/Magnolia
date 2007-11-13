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
package info.magnolia.cms.security.auth.login;

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sameer Charles
 * $Id$
 */
public class FormLogin extends 
LoginHandlerBase implements LoginHandler {

    private static final Logger log = LoggerFactory.getLogger(FormLogin.class);

    public static final String PARAMETER_USER_ID = "mgnlUserId";

    public static final String PARAMETER_PSWD = "mgnlUserPSWD";

    public static final String PARAMETER_REALM = "mgnlRealm";

    /**
     * The JAAS chain/module to use.
     */

    private String jaasChain = "magnolia";

    public LoginResult handle(HttpServletRequest request, HttpServletResponse response) {
        String userid = request.getParameter(PARAMETER_USER_ID);
        if (StringUtils.isNotEmpty(userid)) {
            String pswd = StringUtils.defaultString(request.getParameter(PARAMETER_PSWD));
            String realm = StringUtils.defaultString(request.getParameter(PARAMETER_REALM));

            CredentialsCallbackHandler callbackHandler = new PlainTextCallbackHandler(userid, pswd.toCharArray(), realm);
            return authenticate(callbackHandler, getJaasChain());
        }
        return LoginResult.NOT_HANDLED;
    }

    public String getJaasChain() {
        return this.jaasChain;
    }


    public void setJaasChain(String jaasChain) {
        this.jaasChain = jaasChain;
    }

}
