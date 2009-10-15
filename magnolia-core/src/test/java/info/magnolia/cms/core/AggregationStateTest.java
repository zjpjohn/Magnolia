/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.core;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AggregationStateTest extends TestCase {
    private WebContext webCtx;
    private AggregationState aggState;

    protected void setUp() throws Exception {
        super.setUp();
        aggState = new AggregationState();
        aggState.setCharacterEncoding("UTF-8");

        webCtx = createMock(WebContext.class);
        expect(webCtx.getContextPath()).andReturn("/foo");
        MgnlContext.setInstance(webCtx);
        replay(webCtx);
    }

    protected void tearDown() throws Exception {
        verify(webCtx);

        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testUriDecodingShouldStripCtxPath() {
        assertEquals("/pouet", aggState.stripContextPathIfExists("/foo/pouet"));
    }

    public void testUriDecodingShouldReturnPassedURIDoesntContainCtxPath() {
        assertEquals("/pouet", aggState.stripContextPathIfExists("/pouet"));
    }

}
