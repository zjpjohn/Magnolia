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
package info.magnolia.logging;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.auth.login.FormLogin;
import info.magnolia.cms.security.auth.login.LoginResult;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.UserContext;

import javax.jcr.nodetype.NodeType;
import javax.servlet.http.HttpServletRequest;

/**
 * Utilities class used to log 'auditory actions'.
 *
 * @author tmiyar
 */
public class AuditLoggingUtil {

    public static final String ACTION_CREATE = "create";
    public static final String ACTION_MODIFY = "modify";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_COPY = "copy";
    public static final String ACTION_MOVE = "move";
    public static final String ACTION_ACTIVATE = "activate";
    public static final String ACTION_DEACTIVATE = "deactivate";
    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGOUT = "logout";

    /**
     * log create, modify, activate, deactivate, delete incl. node type.
     * 
     * @deprecated since 4.5, use {@link #log(String, String, NodeType, String)} instead.
     */
    @Deprecated
    public static void log(String action, String workspaceName, ItemType nodeType, String nodePath) {
        AuditLoggingUtil.log(action, new String[]{AuditLoggingUtil.getUser(), workspaceName, nodeType == null ? "" : nodeType.getSystemName(), nodePath});
    }

    /**
     * log create, modify, activate, deactivate, delete incl. node type.
     */
    public static void log(String action, String workspaceName, NodeType nodeType, String nodePath) {
        AuditLoggingUtil.log(action, new String[] { AuditLoggingUtil.getUser(), workspaceName, nodeType == null ? "" : nodeType.getName(), nodePath });
    }

    /**
     * Log copy, move.
     */
    public static void log(String action, String workspaceName, String nodePathFrom, String nodePathTo ) {
        AuditLoggingUtil.log(action, new String[]{AuditLoggingUtil.getUser(), workspaceName, nodePathFrom, nodePathTo});
    }

    /**
     * Log user logout.
     */
    public static void log(final UserContext userContext ) {
        AuditLoggingUtil.log(AuditLoggingUtil.ACTION_LOGOUT, null, (String) null, null);
    }

    /**
     * Log user login.
     */
    public static void log(final LoginResult loginResult, final HttpServletRequest request ) {
        if(loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED || loginResult.getStatus() == LoginResult.STATUS_FAILED) {
            // need request as if the user is not logged yet, the id is not in the context
            String userid = request.getParameter(FormLogin.PARAMETER_USER_ID);
            if (UserManager.ANONYMOUS_USER.equals(userid)) {
                // do not log for anonymous
                return;
            }

            String result;
            if(loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED) {
                result = "Success";
            } else {
                result = "Failure " + loginResult.getLoginException().getLocalizedMessage();
            }
            AuditLoggingUtil.log(AuditLoggingUtil.ACTION_LOGIN, new String[]{userid, request.getRemoteAddr(), result});
        }

    }

    private static void log(String action, String[] data) {
        AuditLoggingManager manager = AuditLoggingManager.getInstance();
        if(manager != null) {
            manager.log(action, data);
        }
    }

    private static String getUser() {
        if (MgnlContext.isSystemInstance()) {
            return "SystemUser";
        }
        try {
            if(MgnlContext.hasInstance() && MgnlContext.getUser() != null) {
                return MgnlContext.getUser().getName();
            }
        }catch (Exception e) {
            return "system user";
        }
        return "user not set";
    }

}
