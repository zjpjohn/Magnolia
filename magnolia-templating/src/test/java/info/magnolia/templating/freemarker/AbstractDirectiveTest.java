/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.templating.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.templating.elements.AbstractContentTemplatingElement;
import info.magnolia.templating.elements.TemplatingElement;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AbstractDirective's utility methods.
 *
 * @version $Id$
 */
public class AbstractDirectiveTest {

    @Test
    public void testBodyCheck() throws TemplateModelException {
        final TemplateDirectiveBody dummyDirBody = new TemplateDirectiveBody() {
            @Override
            public void render(Writer out) throws TemplateException, IOException {
                // no action required
            }
        };

        final AbstractDirective dir = new TestAbstractDirective();
        // no exceptions
        dir.checkBody(null, false);
        dir.checkBody(dummyDirBody, true);

        try {
            dir.checkBody(null, true);
            fail("should have failed");
        } catch (TemplateModelException e) {
            assertEquals("This directive needs a body", e.getMessage());
        }

        try {
            dir.checkBody(dummyDirBody, false);
            fail("should have failed");
        } catch (TemplateModelException e) {
            assertEquals("This directive does not support a body", e.getMessage());
        }
    }

    @Test
    public void testInitContentComponent() throws TemplateModelException {
        final AbstractDirective dir = new TestAbstractDirective();
        AbstractContentTemplatingElement component = mock(AbstractContentTemplatingElement.class);
        Map<String, TemplateModel> params = new LinkedHashMap<String, TemplateModel>();
        dir.initContentElement(params, component);
    }

    private static class TestAbstractDirective extends AbstractDirective {
        @Override
        protected void prepareTemplatingElement(TemplatingElement templatingElement, Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateModelException, IOException {
            // no action required
        }
    }
}
