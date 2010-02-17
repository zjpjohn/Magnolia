/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.templatinguicomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.gui.i18n.DefautlI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

import java.io.StringWriter;
import java.util.Locale;

import static org.easymock.EasyMock.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EditParagraphBarTest extends TestCase {
    public void testPathNodeCollectionNameEtc() throws Exception {
        final MockHierarchyManager hm = MockUtil.createHierarchyManager("/foo/bar/baz/paragraphs/01.text=dummy");
        AccessManager accessManager = createMock(AccessManager.class);
        // for finer-but-not-too-verbose checks, use the contains() constraint
        expect(accessManager.isGranted(isA(String.class), anyLong())).andReturn(true).anyTimes();
        hm.setAccessManager(accessManager);

        final AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(hm.getContent("/foo/bar/baz"));
        aggregationState.setCurrentContent(hm.getContent("/foo/bar/baz/paragraphs/01"));
        final WebContext ctx = createMock(WebContext.class);
        expect(ctx.getAggregationState()).andReturn(aggregationState).anyTimes();
        expect(ctx.getLocale()).andReturn(Locale.US).anyTimes();
        MgnlContext.setInstance(ctx);
        replay(accessManager, ctx);

        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
        // register some default components used internally
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefautlI18nAuthoringSupport());

        final EditParagraphBar bar = new EditParagraphBar(serverCfg, aggregationState);
        final StringWriter out = new StringWriter();
        bar.doRender(out);

        // TODO assertTrue(out.contains(....))
    }

    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.getProperties().clear();
        super.tearDown();
    }
}