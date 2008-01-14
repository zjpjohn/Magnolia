/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import info.magnolia.cms.core.Access;
import info.magnolia.context.MgnlContext;

/**
 * @author Sameer Charles
 * $Id: ContentSecurityFilter.java 9391 2007-05-11 15:48:02Z scharles $
 */
public class ContentSecurityFilter extends BaseSecurityFilter {
    private static final Logger log = LoggerFactory.getLogger(ContentSecurityFilter.class);

    public boolean isAllowed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String repositoryName = MgnlContext.getAggregationState().getRepository();
        AccessManager accessManager = MgnlContext.getAccessManager(repositoryName);
        return isAuthorized(accessManager);
    }

    /**
     * check for read permissions of the aggregated handle
     * */
    protected boolean isAuthorized(AccessManager accessManager) {
        if (null == accessManager) return false;
        try {
            final String handle = MgnlContext.getAggregationState().getHandle();
            Access.isGranted(accessManager, handle, Permission.READ);
            return true;
        } catch (AccessDeniedException ade) {
            log.debug(ade.getMessage(), ade);
        }
        return false;
    }


}
