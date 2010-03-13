/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.workflow.inbox;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.freemarker.FreemarkerUtil;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public class SubPagesControl extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SubPagesControl.class);

    public void drawHtml(Writer out) throws IOException {
        try {
            if (NodeDataUtil.getBoolean(this.getWebsiteNode(), "recursive", false)) {
                this.drawHtmlPre(out);
                out.write(FreemarkerUtil.process(this));
                this.drawHtmlPost(out);
            }
        }
        catch (Exception e) {
            log.error("can't show subpages", e);
            out.write(e.toString());
        }
    }

    public String getWorkItemId(){
        Content itemNode;
        try {
            itemNode = getWebsiteNode().getParent().getParent();
            return itemNode.getNodeData("ID").getString();
        }
        catch (Exception e) {
            log.error("can't evaluate the workitems id", e);
        }
        return "";
    }

}
