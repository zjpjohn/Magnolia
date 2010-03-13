/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
package info.magnolia.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.model.Version;

import javax.jcr.RepositoryException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CoreModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {
    
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/core.xml";
    }

    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new CoreModuleVersionHandler() {
            // cheat - one of the conditions needs web.xml. Can't be bothered to fake that here
            protected List<Condition> getInstallConditions() {
                return Collections.emptyList();
            }
        };
    }

    public void testPngSwfMimeTypesOnInstall() throws ModuleManagementException, RepositoryException {
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom35x() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // prior to 3.6.4, the mime types for flv and svg did not exit
        // prior to 3.6.4, the mime types for png and swf were incorrect
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "application/octet-stream");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "application/octet-stream");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.5"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom35xWithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a 3pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // prior to 3.6.4, the mime types for flv and svg did not exit
        // prior to 3.6.4, the mime types for png and swf were incorrect - but values have been customized on this instance
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.5"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom364() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "image/png;");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.6.4"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 3.6.7+, since we have corrected values
     * for the mimetypes; when updating from 3.6.4 in an instance where the user
     * customized the mime types values, or updating from 3.6.7, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    public void testPngSwfMimeTypesOnUpdateFrom364WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.6.4"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom401() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "image/png;");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.1"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 4.0.3+, since we have corrected values
     * for the mimetypes; when updating from 4.0.1 in an instance where the user
     * customized the mime types values, or updating from 4.0.3, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    public void testPngSwfMimeTypesOnUpdateFrom401WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.1"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom410() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "image/png;");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 4.1.1+, since we have corrected values
     * for the mimetypes; when updating from 4.1.0 in an instance where the user
     * customized the mime types values, or updating from 4.1.1, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    public void testPngSwfMimeTypesOnUpdateFrom410WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    private String getMimeTypePropertyValue(String typeName) throws RepositoryException {
        return MgnlContext.getHierarchyManager("config").getContent("/server/MIMEMapping/" + typeName).getNodeData("mime-type").getString();
    }

    /**
     * test unicode normalization filter and the new filter ordering (update version to 4.3).
     */
    public void testUnicodeNormalizerAndFilterOrderOnUpdateFrom410() throws ModuleManagementException, RepositoryException {
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0

        setupConfigProperty("/server/filters/context", "enable", "true");
        setupConfigProperty("/server/filters/contentType", "enable", "true");
        setupConfigProperty("/server/filters/login", "enable", "true");
        setupConfigProperty("/server/filters/logout", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity", "enable", "true");
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/activation", "enable", "true");
        // let's make sure we've set up this test with filters in their pre-4.3 order
        final Iterator<Content> filters = MgnlContext.getHierarchyManager("config").getContent("/server/filters/").getChildren().iterator();
        assertEquals("context", filters.next().getName());
        assertEquals("contentType", filters.next().getName());
        assertEquals("login", filters.next().getName());
        assertEquals("logout", filters.next().getName());
        assertEquals("uriSecurity", filters.next().getName());
        assertEquals("multipartRequest", filters.next().getName());
        assertEquals("activation", filters.next().getName());
        assertFalse(filters.hasNext());


        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));

        final Iterator<Content> updatedFilters = MgnlContext.getHierarchyManager("config").getContent("/server/filters/").getChildren().iterator();
        assertEquals("context", updatedFilters.next().getName());
        assertEquals("contentType", updatedFilters.next().getName());
        assertEquals("multipartRequest", updatedFilters.next().getName());
        assertEquals("unicodeNormalization", updatedFilters.next().getName());
        assertEquals("login", updatedFilters.next().getName());
        assertEquals("logout", updatedFilters.next().getName());
        assertEquals("uriSecurity", updatedFilters.next().getName());
        assertEquals("activation", updatedFilters.next().getName());
        assertFalse(updatedFilters.hasNext());
    }

}
