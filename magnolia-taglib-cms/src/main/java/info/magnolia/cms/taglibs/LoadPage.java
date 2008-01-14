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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Loads another page into actPage. One usage would be within a site-menu structure. loadPage does not nest pages, so
 * the corresponding unloadPage-tag will not revert to the previously loaded page, but restore actPage to the currently
 * displayed page, i.e. the value it held before loadPage was called for the first time.
 * @author Marcel Salathe
 * @version $Revision$ ($Author$)
 */
public class LoadPage extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LoadPage.class);

    /**
     * Tag attribute: path of the page to be loaded.
     */
    private String path;

    /**
     * Tag attribute: template name.
     */
    private String templateName;

    /**
     * Tag attribute: level.
     */
    private int level;

    /**
     * Setter for the "path" tag attribute.
     * @param path path of the page to be loaded
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Setter for the "templateName" tag attribute.
     * @param templateName
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Setter for the "level" tag attribute.
     * @param level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        Content newActpage = Resource.getCurrentActivePage();

        String actPageHandle = newActpage.getHandle();

        if (StringUtils.isNotEmpty(this.templateName)) {
            Content startPage;
            try {
                startPage = Resource.getCurrentActivePage().getAncestor(this.level);
                HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                newActpage = hm.getPage(startPage.getHandle(), this.templateName);
            }
            catch (RepositoryException e) {
                log.error(e.getClass().getName()
                    + " caught while loading page with template "
                    + this.templateName
                    + " (start level="
                    + this.level
                    + ") from "
                    + actPageHandle
                    + ": "
                    + e.getMessage(), e);
                return EVAL_PAGE;
            }
        }
        else if (StringUtils.isNotEmpty(this.path)) {
            try {
                newActpage = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).getContent(this.path);
            }
            catch (RepositoryException e) {
                log.error(e.getClass().getName()
                    + " caught while loading path "
                    + this.path
                    + " from "
                    + actPageHandle
                    + ": "
                    + e.getMessage(), e);
                return EVAL_PAGE;
            }
        }
        else {
            try {
                newActpage = Resource.getCurrentActivePage().getAncestor(this.level);
            }
            catch (RepositoryException e) {
                log.error(e.getClass().getName()
                    + " caught while loading page with level "
                    + this.level
                    + " from "
                    + actPageHandle
                    + ": "
                    + e.getMessage(), e);
                return EVAL_PAGE;
            }
        }
        MgnlContext.getAggregationState().setCurrentContent(newActpage);
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.path = null;
        this.templateName = null;
        this.level = 0;
    }
}
