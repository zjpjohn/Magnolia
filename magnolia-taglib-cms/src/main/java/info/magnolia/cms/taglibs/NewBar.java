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

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.inline.BarNew;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.ArrayList;
import java.util.List;


/**
 * Displays a newBar that allows you to create new paragraphs.
 * @jsp.tag name="newBar" body-content="JSP"
 * @jsp.tag-example
 *     <cms:newBar contentNodeCollectionName="mainColumnParagraphs"
 *                 paragraph="samplesTextImage,samplesDownload,samplesLink"/>
 *
 *
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class NewBar extends TagSupport implements BarTag {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String DEFAULT_NEW_LABEL = "buttons.new"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(NewBar.class);

    private String contentNodeCollectionName;

    private String paragraph;

    private String newLabel;

    /**
     * Show only in admin instance.
     */
    private boolean adminOnly = true;

    /**
     * Addition buttons (left).
     */
    private List buttonLeft;

    /**
     * Addition buttons (right).
     */
    private List buttonRight;

    private String contentNodeName = "mgnlNew";

    /**
     * Text of the button, defaults to "New".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setNewLabel(String label) {
        this.newLabel = label;
    }

    /**
     * Show only in admin instance, default to true.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    /**
     * A single new paragraph will be stored as a node with this name.
     * @param contentNodeName name of the node
     * @jsp.attribute required="false" rtexprvalue="true"
     * TODO : the doc used to say that this attribute was required, although the TLD declared the opposite - quid ?
     */
    public void setContentNodeName(String contentNodeName) {
        this.contentNodeName = contentNodeName;
    }

    /**
     * New paragraphs will be stored under this node's name. You will need the name for later retrieval of contents.
     * @param name container list name
     * @jsp.attribute required="false" rtexprvalue="true"
     * TODO : the doc used to say that this attribute was required, although the TLD declared the opposite - quid ?
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * Comma separated list of allowed paragraph types.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setParagraph(String list) {
        this.paragraph = list;
    }

    /**
     * @see info.magnolia.cms.taglibs.BarTag#addButtonLeft(info.magnolia.cms.gui.control.Button)
     */
    public void addButtonLeft(Button button) {
        if (buttonLeft == null) {
            buttonLeft = new ArrayList();
        }
        buttonLeft.add(button);
    }

    /**
     * @see info.magnolia.cms.taglibs.BarTag#addButtonRight(info.magnolia.cms.gui.control.Button)
     */
    public void addButtonRight(Button button) {
        if (buttonRight == null) {
            buttonRight = new ArrayList();
        }
        buttonRight.add(button);
    }

    /**
     * @return String , label for the new bar
     */
    private String getNewLabel() {
        String defStr = MessagesManager.getMessages().get(DEFAULT_NEW_LABEL);
        return StringUtils.defaultString(this.newLabel, defStr);
    }

    /**
     * Get the content path (Page or Node).
     * @return String path
     */
    private String getPath() {
        try {
            // Detect if the new bar is being displayed from inside a paragraph and use that instead.
            if (Resource.getLocalContentNode() != null) {
                return Resource.getLocalContentNode().getHandle();
            }
            else {
                return Resource.getCurrentActivePage().getHandle();
            }
        }
        catch (Exception re) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {

        if ((!adminOnly || ServerConfiguration.getInstance().isAdmin()) && MgnlContext.getAggregationState().getMainContent().isGranted(Permission.SET)) {
            HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
            try {
                BarNew bar = new BarNew(request);
                bar.setPath(this.getPath());
                bar.setParagraph(StringUtils.deleteWhitespace(this.paragraph));
                bar.setNodeCollectionName(this.contentNodeCollectionName);
                bar.setNodeName(contentNodeName); //$NON-NLS-1$
                bar.setDefaultButtons();
                if (this.getNewLabel() != null) {
                    if (StringUtils.isEmpty(this.getNewLabel())) {
                        bar.setButtonNew(null);
                    }
                    else {
                        bar.getButtonNew().setLabel(this.getNewLabel());
                    }
                }

                if (buttonRight != null) {
                    bar.getButtonsRight().addAll(buttonRight);
                }
                if (buttonLeft != null) {
                    bar.getButtonsLeft().addAll(buttonLeft);
                }

                bar.placeDefaultButtons();
                bar.drawHtml(pageContext.getOut());
            }
            catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
                }
            }
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        this.contentNodeCollectionName = null;
        this.contentNodeName = null;
        this.paragraph = null;
        this.newLabel = null;
        this.buttonLeft = null;
        this.buttonRight = null;
        this.adminOnly = true;
    }
}
