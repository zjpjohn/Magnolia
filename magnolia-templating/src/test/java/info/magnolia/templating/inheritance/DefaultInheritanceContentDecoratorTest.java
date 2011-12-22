/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.templating.inheritance;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.rendering.template.InheritanceConfiguration;
import info.magnolia.rendering.template.configured.ConfiguredInheritance;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Test;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test case for {@link DefaultInheritanceContentDecorator}.
 * 
 * @version $Id$
 */
public class DefaultInheritanceContentDecoratorTest {

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testDoesNothingOnTopLevelPage() throws RepositoryException, IOException {

        Map<String, String> sections = loadSessionConfigs("testDoesNothingOnTopLevelPage");
        Session session = wrapSessionForInheritance(sections, "/page1/main");
        assertEquals(sections.get("Expected"), sessionToString(session));
    }

    @Test
    public void testPropertyInheritance() throws RepositoryException, IOException {
        Map<String, String> sections = loadSessionConfigs("testPropertyInheritance");
        Session session = wrapSessionForInheritance(sections, "/page1/page2/main");
        assertEquals(sections.get("Expected"), sessionToString(session));
    }

    @Test
    public void testMultiLevelPropertyInheritance() throws Exception {

        Map<String, String> sections = loadSessionConfigs("testMultiLevelPropertyInheritance");
        Session session = wrapSessionForInheritance(sections, "/page1/page2/page3/main");
        assertEquals(sections.get("Expected"), sessionToString(session));
    }

    @Test
    public void testMultiLevelPropertyInheritanceCollision() throws Exception {
        Map<String, String> sections = loadSessionConfigs("testMultiLevelPropertyInheritanceCollision");
        Session session = wrapSessionForInheritance(sections, "/page1/page2/page3/main");
        assertEquals(sections.get("Expected"), sessionToString(session));
    }

    @Test
    public void testComponentInheritance() throws Exception {
        Map<String, String> sections = loadSessionConfigs("testComponentInheritance");
        Session session = wrapSessionForInheritance(sections, "/page1/page2/main");
        assertEquals(sections.get("Expected"), sessionToString(session));
    }

    @Test
    public void testDisabledPropertyInheritance() throws Exception {
        Map<String, String> sections = loadSessionConfigs("testDisabledPropertyInheritance");
        ConfiguredInheritance inheritanceConfiguration = new ConfiguredInheritance();
        inheritanceConfiguration.setProperties(ConfiguredInheritance.PROPERTIES_NONE);
        Session session = wrapSessionForInheritance(sections, "/page1/page2/page3/main", inheritanceConfiguration);
        assertEquals(sections.get("Expected"), sessionToString(session));
    }

    @Test
    public void testPropertyInheritanceWithInheritanceDisabled() throws Exception {
        Map<String, String> sections = loadSessionConfigs("testPropertyInheritanceWithInheritanceDisabled");
        ConfiguredInheritance inheritanceConfiguration = new ConfiguredInheritance();
        inheritanceConfiguration.setEnabled(false);
        Session session = wrapSessionForInheritance(sections, "/page1/page2/page3/main", inheritanceConfiguration);
        assertEquals(sections.get("Expected"), sessionToString(session));
    }

    @Test
    public void testDestinationIsAlsoAnchorInheritance() throws Exception {
        Map<String, String> sections = loadSessionConfigs("testDestinationIsAlsoAnchorInheritance");
        Session session = wrapSessionForInheritance(sections, "/page1/page2");

        Node page = session.getNode("/page1/page2");

        assertNotNull(page.getProperty("width"));
        assertNotNull(page.getNode("links"));
        assertNotNull(page.getProperty("links/border"));
    }

    private Session wrapSessionForInheritance(Map<String, String> sections, String inheritanceNode) throws RepositoryException, IOException {
        InheritanceConfiguration inheritanceConfiguration = new ConfiguredInheritance();
        return wrapSessionForInheritance(sections, inheritanceNode, inheritanceConfiguration);
    }

    private Session wrapSessionForInheritance(Map<String, String> sections, String inheritanceNode, InheritanceConfiguration inheritanceConfiguration) throws IOException, RepositoryException {
        Session session = createInputSession(sections);
        Node destination = session.getNode(inheritanceNode);
        return new DefaultInheritanceContentDecorator(destination, inheritanceConfiguration).wrapSession(session);
    }

    private Map<String, String> loadSessionConfigs(String resourceSuffix) throws IOException {

        InputStream stream = getClass().getResourceAsStream(getClass().getSimpleName() + "_" + resourceSuffix + ".txt");
        List<String> lines = IOUtils.readLines(stream);

        HashMap<String, String> sections = new HashMap<String, String>();
        String sectionName = null;
        List<String> sectionLines = new ArrayList<String>();

        for (String line : lines) {
            if (line.equals("")) {
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("[")) {
                if (sectionName != null) {
                    sections.put(sectionName, StringUtils.join(sectionLines, "\n"));
                }
                sectionName = StringUtils.substringBetween(line, "[", "]");
                sectionLines.clear();
                continue;
            }
            sectionLines.add(line);
        }
        if (sectionName != null) {
            sections.put(sectionName, StringUtils.join(sectionLines, "\n"));
        }
        return sections;
    }

    private MockSession createInputSession(Map<String, String> sections) throws IOException, RepositoryException {
        return SessionTestUtil.createSession("website", sections.get("Input"));
    }

    private String sessionToString(Session session) throws RepositoryException {
        ArrayList<String> result = new ArrayList<String>();
        NodeIterator nodes = session.getRootNode().getNodes();
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            nodeToString(result, "", node);
        }
        return StringUtils.join(result, "\n");
    }

    private void nodeToString(List<String> result, String parentPath, Node node) throws RepositoryException {
        String path = parentPath + "/" + node.getName();
        result.add(path + ".@type = " + node.getPrimaryNodeType().getName());
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = (Property) propertyIterator.next();
            result.add(path + "." + property.getName() + " = " + property.getString());
        }
        NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            nodeToString(result, path, nodes.nextNode());
        }
    }
}
