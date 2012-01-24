/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.jsp.cms;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.templating.jsp.AbstractTagTestCase;

import org.junit.Before;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @version $Id$
 *
 */
public class EditBarTagTest extends AbstractTagTestCase {

    private String jspPath;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        jspPath = getClass().getName().replace('.', '/') + ".jsp";

        ConfiguredTemplateDefinition renderableDefinition = new ConfiguredTemplateDefinition();
        renderableDefinition.setId("testParagraph1");
        renderableDefinition.setTitle("Test Paragraph 1");
        renderableDefinition.setDialog("testDialog");
        renderableDefinition.setTemplateScript(jspPath);

        setRendableDefinition(renderableDefinition);
    }


    @Test
    public void testOutput() throws Exception {
        // GIVEN
        final String jspUrl = "http://localhost" + CONTEXT + "/" + jspPath;

        final WebRequest request = new GetMethodWebRequest(jspUrl);
        final WebResponse response = runner.getResponse(request);

        // WHEN
        final String responseStr = response.getText();

        // THEN
        //Check first div (default dialog)
        assertThat(responseStr, containsString("<!-- cms:edit content=\"website:/foo/bar/paragraphs/1\" label=\"Test Paragraph 1\" dialog=\"testDialog\" template=\"testParagraph1\" -->"));
        //Check second div (nyCustomDialog) defining a dialog
        assertThat(responseStr, containsString("<!-- cms:edit content=\"website:/foo/bar/paragraphs/1\" label=\"Test Paragraph 1\" dialog=\"myCustomDialog\" template=\"testParagraph1\" -->\n<!-- /cms:edit -->\n"));
       //Check second div (customDialogFormat) defining a dialog and a format
        assertThat(responseStr, containsString("<!-- cms:edit content=\"website:/foo/bar/paragraphs/1\" label=\"Test Paragraph 1\" format=\"format\" dialog=\"customDialogFormat\" template=\"testParagraph1\" -->\n<!-- /cms:edit -->\n"));

    }


}
