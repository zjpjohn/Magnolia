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
package info.magnolia.module.exchangesimple.setup.for3_5;

import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.exchangesimple.DefaultActivationManager;
import info.magnolia.module.exchangesimple.DefaultSubscriber;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;
import org.apache.commons.collections.MapUtils;
import static org.easymock.EasyMock.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class UpdateActivationConfigTaskTest extends TestCase {
    private static final String DEFAULT_EE_30_CFG                         = "" +
            "/server\n" + // this node is supposed to be there prior to UpdateEEActivationTask execution
            "/subscribers/default.URL = someFakeUrl/magnoliaPublic\n" +
            "/subscribers/default.active = true\n" +
            "/subscribers/default/context/website/0001.subscribedURI=/\n" +
            "/subscribers/default/context/users/0001.subscribedURI=/\n" +
            "/subscribers/default/context/userroles/0001.subscribedURI=/\n" +
            "/subscribers/default/context/usergroups/0001.subscribedURI=/\n" +
            "/subscribers/default/context/config/0001.subscribedURI=/\n" +
            "/subscribers/default/context/dms/0001.subscribedURI=/\n";

    private static final String DEFAULT_CE_30_CFG                         = "" +
            "/server\n" + // this node is supposed to be there prior to UpdateEEActivationTask execution
            "/subscriber.URL = someFakeUrl/magnoliaPublic\n" +
            "/subscriber.active = true\n" +
            "/subscriber/context/website/0001.subscribedURI=/\n" +
            "/subscriber/context/users/0001.subscribedURI=/\n" +
            "/subscriber/context/userroles/0001.subscribedURI=/\n" +
            "/subscriber/context/usergroups/0001.subscribedURI=/\n" +
            "/subscriber/context/config/0001.subscribedURI=/\n" +
            "/subscriber/context/dms/0001.subscribedURI=/\n";

    private static final Map DEFAULT_35_CONFIG = new HashMap() {
        {
            put("/server/activation.class", DefaultActivationManager.class.getName());
            put("/server/activation/subscribers/default.URL", "someFakeUrl/magnoliaPublic");
            put("/server/activation/subscribers/default.active", "true");
            put("/server/activation/subscribers/default.class", DefaultSubscriber.class.getName());
            put("/server/activation/subscribers/default/subscriptions/website0001.repository", "website");
            put("/server/activation/subscribers/default/subscriptions/website0001.fromURI", "/");
            put("/server/activation/subscribers/default/subscriptions/website0001.toURI", "/");
            put("/server/activation/subscribers/default/subscriptions/config0001.repository", "config");
            put("/server/activation/subscribers/default/subscriptions/config0001.fromURI", "/");
            put("/server/activation/subscribers/default/subscriptions/config0001.toURI", "/");
            put("/server/activation/subscribers/default/subscriptions/users0001.repository", "users");
            put("/server/activation/subscribers/default/subscriptions/users0001.fromURI", "/");
            put("/server/activation/subscribers/default/subscriptions/users0001.toURI", "/");
            put("/server/activation/subscribers/default/subscriptions/usergroups0001.repository", "usergroups");
            put("/server/activation/subscribers/default/subscriptions/usergroups0001.fromURI", "/");
            put("/server/activation/subscribers/default/subscriptions/usergroups0001.toURI", "/");
            put("/server/activation/subscribers/default/subscriptions/userroles0001.repository", "userroles");
            put("/server/activation/subscribers/default/subscriptions/userroles0001.fromURI", "/");
            put("/server/activation/subscribers/default/subscriptions/userroles0001.toURI", "/");
            put("/server/activation/subscribers/default/subscriptions/dms0001.repository", "dms");
            put("/server/activation/subscribers/default/subscriptions/dms0001.fromURI", "/");
            put("/server/activation/subscribers/default/subscriptions/dms0001.toURI", "/");
        }
    };

    private static final String CUSTOM_30_CFG = "" +
            "/server\n" + // this node is supposed to be there prior to UpdateEEActivationTask execution
            "/subscribers/first.URL = firstUrl\n" +
            "/subscribers/first.active = true\n" +
            "/subscribers/first/context/website/boo.subscribedURI=/boo\n" +
            "/subscribers/first/context/config/test.subscribedURI=/\n" +
            "/subscribers/second.URL = secondUrl\n" +
            "/subscribers/second.active = false\n" +
            "/subscribers/second/context/website/0001.subscribedURI=/foo\n" +
            "/subscribers/second/context/website/0002.subscribedURI=/bar\n" +
            "/subscribers/second/context/dms/0001.subscribedURI=/\n";

    private static final Map CUSTOM_35_CONFIG = new HashMap() {
        {
            put("/server/activation.class", DefaultActivationManager.class.getName());
            put("/server/activation/subscribers/first.URL", "firstUrl");
            put("/server/activation/subscribers/first.active", "true");
            put("/server/activation/subscribers/first.class", DefaultSubscriber.class.getName());
            put("/server/activation/subscribers/first/subscriptions/websiteboo.repository", "website");
            put("/server/activation/subscribers/first/subscriptions/websiteboo.fromURI", "/boo");
            put("/server/activation/subscribers/first/subscriptions/websiteboo.toURI", "/boo");
            put("/server/activation/subscribers/first/subscriptions/configtest.repository", "config");
            put("/server/activation/subscribers/first/subscriptions/configtest.fromURI", "/");
            put("/server/activation/subscribers/first/subscriptions/configtest.toURI", "/");

            put("/server/activation/subscribers/second.URL", "secondUrl");
            put("/server/activation/subscribers/second.active", "false");
            put("/server/activation/subscribers/second.class", DefaultSubscriber.class.getName());
            put("/server/activation/subscribers/second/subscriptions/website0001.repository", "website");
            put("/server/activation/subscribers/second/subscriptions/website0001.fromURI", "/foo");
            put("/server/activation/subscribers/second/subscriptions/website0001.toURI", "/foo");
            put("/server/activation/subscribers/second/subscriptions/website0002.repository", "website");
            put("/server/activation/subscribers/second/subscriptions/website0002.fromURI", "/bar");
            put("/server/activation/subscribers/second/subscriptions/website0002.toURI", "/bar");
            put("/server/activation/subscribers/second/subscriptions/dms0001.repository", "dms");
            put("/server/activation/subscribers/second/subscriptions/dms0001.fromURI", "/");
            put("/server/activation/subscribers/second/subscriptions/dms0001.toURI", "/");
        }
    };

    public void testUpdatesDefaultEEConfigProperly() throws Exception {
        doTestUpdatesConfigProperly(DEFAULT_EE_30_CFG                        , DEFAULT_35_CONFIG);
    }

    public void testUpdatesDefaultCEConfigProperly() throws Exception {
        doTestUpdatesConfigProperly(DEFAULT_CE_30_CFG                        , DEFAULT_35_CONFIG);
    }

    public void testUpdatesCustomEEConfigProperly() throws Exception {
        doTestUpdatesConfigProperly(CUSTOM_30_CFG, CUSTOM_35_CONFIG);
    }

    public void testFailsIf30SubscribersNodeNotFound() throws Exception {
        // let's pretend there's no /subscriber or /subscribers node. Can be the case when install 3.5 without updating from 3.0
        final MockHierarchyManager hm = MockUtil.createHierarchyManager("/server\n");
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        replay(ctx);
        try {
            new UpdateActivationConfigTask().execute(ctx);
            fail("should have failed");
        } catch (TaskExecutionException e) {
            assertEquals("Couldn't find /subscriber nor /subscribers node, can't update activation subscribers configuration.", e.getMessage());
        } finally {
            verify(ctx);
        }
    }

    private void doTestUpdatesConfigProperly(String initialConfig, Map expectedResult) throws Exception {
        final MockHierarchyManager hm = MockUtil.createHierarchyManager(initialConfig);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        replay(ctx);

        new UpdateActivationConfigTask().execute(ctx);

        verify(ctx);

        final String expectedReadable = asComparableString(expectedResult);
        final Properties actual = PropertiesImportExport.toProperties(hm);
        final String actualReadable = asComparableString(actual);
        assertEquals(expectedReadable, actualReadable);
        assertEquals(expectedResult, actual);
    }

    private String asComparableString(Map map) {
        final ByteArrayOutputStream readableOS = new ByteArrayOutputStream();
        MapUtils.verbosePrint(new PrintStream(readableOS), null, new TreeMap(map));
        return readableOS.toString();
    }
}
