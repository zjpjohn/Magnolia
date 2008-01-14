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
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle intercepted administrative requests.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class InterceptFilter extends AbstractMgnlFilter {

    /**
     * Request parameter: the INTERCEPT holds the name of an administrative action to perform.
     */
    public static final String INTERCEPT = "mgnlIntercept"; //$NON-NLS-1$

    /**
     * Action: sort a paragraph.
     */
    private static final String ACTION_NODE_SORT = "NODE_SORT"; //$NON-NLS-1$

    /**
     * Action: delete a paragraph.
     */
    private static final String ACTION_NODE_DELETE = "NODE_DELETE"; //$NON-NLS-1$

    /**
     * Action: preview a page.
     */
    private static final String ACTION_PREVIEW = "PREVIEW"; //$NON-NLS-1$

    /**
     * request parameter: repository name.
     */
    private static final String PARAM_REPOSITORY = "mgnlRepository"; //$NON-NLS-1$

    /**
     * request parameter: node path, used for paragraph deletion.
     */
    private static final String PARAM_PATH = "mgnlPath"; //$NON-NLS-1$

    /**
     * request parameter: sort-above paragraph.
     */
    private static final String PARAM_PATH_SORT_ABOVE = "mgnlPathSortAbove"; //$NON-NLS-1$

    /**
     * request parameter: selected paragraph.
     */
    private static final String PARAM_PATH_SELECTED = "mgnlPathSelected"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(InterceptFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{

        if (request.getParameter(INTERCEPT) != null) {
            this.intercept(request, response);
        }

        chain.doFilter(request, response);
    }


    /**
     * Request and Response here is same as receivced by the original page so it includes all post/get data. Sub action
     * could be called from here once this action finishes, it will continue loading the requested page.
     */
    public void intercept(HttpServletRequest request, HttpServletResponse response) {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        String action = request.getParameter(INTERCEPT);
        String repository = request.getParameter(PARAM_REPOSITORY);
        String nodePath = request.getParameter(PARAM_PATH);
        String handle = aggregationState.getHandle();

        if (repository == null) {
            repository = aggregationState.getRepository();
        }

        if (repository == null) {
            repository = ContentRepository.WEBSITE;
        }

        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
        synchronized (ExclusiveWrite.getInstance()) {
            if (action.equals(ACTION_PREVIEW)) {
                // preview mode (button in main bar)
                String preview = request.getParameter(Resource.MGNL_PREVIEW_ATTRIBUTE);
                if (preview != null) {

                    // @todo IMPORTANT remove use of http session
                    HttpSession httpsession = request.getSession(true);
                    if (BooleanUtils.toBoolean(preview)) {
                        httpsession.setAttribute(Resource.MGNL_PREVIEW_ATTRIBUTE, Boolean.TRUE);
                    }
                    else {
                        httpsession.removeAttribute(Resource.MGNL_PREVIEW_ATTRIBUTE);
                    }
                }
            }
            else if (action.equals(ACTION_NODE_DELETE)) {
                // delete paragraph
                try {
                    Content page = hm.getContent(handle);
                    page.updateMetaData();
                    hm.delete(nodePath);
                    hm.save();
                }
                catch (RepositoryException e) {
                    log.error("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
                }
            }
            else if (action.equals(ACTION_NODE_SORT)) {
                // sort paragrpahs
                try {
                    String pathSelected = request.getParameter(PARAM_PATH_SELECTED);
                    String pathSortAbove = request.getParameter(PARAM_PATH_SORT_ABOVE);
                    String pathParent = StringUtils.substringBeforeLast(pathSelected, "/"); //$NON-NLS-1$
                    String srcName = StringUtils.substringAfterLast(pathSelected, "/");
                    String destName = StringUtils.substringAfterLast(pathSortAbove, "/");
                    if (StringUtils.equalsIgnoreCase(destName, "mgnlNew")) {
                        destName = null;
                    }
                    hm.getContent(pathParent).orderBefore(srcName, destName);
                    hm.save();
                }
                catch (RepositoryException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
                    }
                }
            }
        }
    }

}
