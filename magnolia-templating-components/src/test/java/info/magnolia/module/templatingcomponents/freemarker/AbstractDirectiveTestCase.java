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
package info.magnolia.module.templatingcomponents.freemarker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import freemarker.cache.StringTemplateLoader;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.freemarker.FreemarkerConfig;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.pico.ModuleAdapterFactory;
import info.magnolia.objectfactory.pico.PicoComponentProvider;
import info.magnolia.templating.rendering.RenderException;
import info.magnolia.templating.rendering.RenderingContext;
import info.magnolia.templating.rendering.RenderingEngine;
import info.magnolia.templating.template.RenderableDefinition;
import info.magnolia.templating.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.templating.template.registry.TemplateDefinitionProvider;
import info.magnolia.templating.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

/**
 * In contrast to related tests, this one can't use ComponentsTestUtils & MockComponentProvider (doesn't support determining appropriate constructor to be called) but
 * a proper Pico environment.
 *
 * @version $Id$
 */
public abstract class AbstractDirectiveTestCase {

    private WebContext ctx;
    protected MockHierarchyManager hm;
    private HttpServletRequest req;
    private HttpServletResponse res;
    protected StringTemplateLoader tplLoader;
    protected FreemarkerHelper fmHelper;

    @Before
    public void setUp() throws Exception {
        // shunt loggers
        freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        tplLoader = new StringTemplateLoader();
        FreemarkerConfig fmConfig = new FreemarkerConfig();
        fmConfig.getTemplateLoaders().clear();
        fmConfig.addTemplateLoader(tplLoader);

        fmHelper = new FreemarkerHelper(fmConfig);
        // fmHelper.getConfiguration().setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        hm =
                MockUtil.createHierarchyManager(StringUtils.join(Arrays.asList(
                        "/foo/bar.@type=mgnl:content",
                        "/foo/bar/MetaData.@type=mgnl:metadata",
                        "/foo/bar/MetaData.mgnl\\:template=testPageTemplate",
                        "/foo/bar/paragraphs.@type=mgnl:contentNode",
                        "/foo/bar/paragraphs/0.@type=mgnl:contentNode",
                        "/foo/bar/paragraphs/0.@uuid=100",
                        "/foo/bar/paragraphs/0.text=hello 0",
                        "/foo/bar/paragraphs/0/MetaData.@type=mgnl:metadata",
                        "/foo/bar/paragraphs/0/MetaData.mgnl\\:template=testParagraph0",
                        "/foo/bar/paragraphs/1.@type=mgnl:contentNode",
                        "/foo/bar/paragraphs/1.@uuid=101",
                        "/foo/bar/paragraphs/1.text=hello 1",
                        "/foo/bar/paragraphs/1/MetaData.@type=mgnl:metadata",
                        "/foo/bar/paragraphs/1/MetaData.mgnl\\:template=testParagraph1",
                        "/foo/bar/paragraphs/2.@type=mgnl:contentNode",
                        "/foo/bar/paragraphs/2.@uuid=102",
                        "/foo/bar/paragraphs/2.text=hello 2",
                        "/foo/bar/paragraphs/2/MetaData.@type=mgnl:metadata",
                        "/foo/bar/paragraphs/2/MetaData.mgnl\\:template=testParagraph2",
                        ""), "\n"));

        final AggregationState aggState = new AggregationState();
        // let's make sure we render stuff on an author instance
        aggState.setPreviewMode(false);
        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);

        // setUp Pico
        final PicoBuilder picoBuilder =
                new PicoBuilder().withConstructorInjection().withAnnotatedFieldInjection().withCaching()
                        .withBehaviors(new ModuleAdapterFactory());
        final MutablePicoContainer container = picoBuilder.build();

        PicoComponentProvider provider = new PicoComponentProvider(container);
        Components.setProvider(provider);

        provider.registerInstance(ServerConfiguration.class, serverCfg);
        // register some default components used internally
        provider.registerInstance(MessagesManager.class, new DefaultMessagesManager());
        provider.registerInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        provider.registerInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());

        ConfiguredTemplateDefinition testParagraph0 = new ConfiguredTemplateDefinition();
        testParagraph0.setName("testParagraph0");
        testParagraph0.setTitle("Test Paragraph 0");
        ConfiguredTemplateDefinition testParagraph1 = new ConfiguredTemplateDefinition();
        testParagraph1.setName("testParagraph1");
        testParagraph1.setTitle("Test Paragraph 1");
        testParagraph1.setDialog("testDialog");
        ConfiguredTemplateDefinition testParagraph2 = new ConfiguredTemplateDefinition();
        testParagraph2.setName("testParagraph2");
        testParagraph2.setTitle("Test Paragraph 2");

        final TemplateDefinitionProvider p0provider = mock(TemplateDefinitionProvider.class);
        final TemplateDefinitionProvider p1provider = mock(TemplateDefinitionProvider.class);
        final TemplateDefinitionProvider p2provider = mock(TemplateDefinitionProvider.class);

        when(p0provider.getTemplateDefinition()).thenReturn(testParagraph0);
        when(p1provider.getTemplateDefinition()).thenReturn(testParagraph1);
        when(p2provider.getTemplateDefinition()).thenReturn(testParagraph2);

        final TemplateDefinitionRegistry tdr = new TemplateDefinitionRegistry();
        tdr.registerTemplateDefinition(testParagraph0.getName(), p0provider);
        tdr.registerTemplateDefinition(testParagraph1.getName(), p1provider);
        tdr.registerTemplateDefinition(testParagraph2.getName(), p2provider);

        provider.registerInstance(TemplateDefinitionRegistry.class, tdr);

        req = mock(HttpServletRequest.class);
        req.setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);

        res = mock(HttpServletResponse.class);
        when(res.getWriter()).thenReturn(null);

        ctx = mock(WebContext.class);
        when(ctx.getAggregationState()).thenReturn(aggState);
        when(ctx.getLocale()).thenReturn(Locale.US);
        when(ctx.getHierarchyManager(hm.getName())).thenReturn(hm);
        when(ctx.getResponse()).thenReturn(res);
        when(ctx.getRequest()).thenReturn(req);

        setupExpectations(ctx, req);

        MgnlContext.setInstance(ctx);

        final RenderingContext renderingContext = mock(RenderingContext.class);
        when(renderingContext.getCurrentContent()).thenReturn(hm.getContent("/foo/bar/paragraphs/1").getJCRNode());
        when(renderingContext.getRenderableDefinition()).thenReturn(new ConfiguredTemplateDefinition());

        provider.registerInstance(RenderingEngine.class, new RenderingEngine() {
            @Override
            public void render(Node content, Appendable out) throws RenderException {
                // no impl required
            }

            @Override
            public void render(Node content, Map<String, Object> contextObjects, Appendable out) throws RenderException {
                // no impl required
            }

            @Override
            public void render(Node content, RenderableDefinition definition, Map<String, Object> contextObjects, Appendable out) throws RenderException {
                // no impl required
            }

            @Override
            public RenderingContext getRenderingContext() {
                return renderingContext;
            }
        });
    }

    /**
     * Hook method - overwrite if you want to set up special expectations.
     */
    protected void setupExpectations(WebContext ctx, HttpServletRequest req) {
        // no specific expectations here
    }

    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }

    public String renderForTest(final String templateSource) throws Exception {
        tplLoader.putTemplate("test.ftl", templateSource);

        final Map<String, Object> map = contextWithDirectives();
        map.put("content", hm.getContent("/foo/bar/"));

        final StringWriter out = new StringWriter();
        fmHelper.render("test.ftl", map, out);

        return out.toString();
    }

    protected Map<String, Object> contextWithDirectives() {
        // this is the only thing we expect rendering engines to do: added the directives to the rendering context so
        // they can be refered to via "@cms"
        return createSingleValueMap("cms", new Directives());
    }

    protected Map<String, Object> createSingleValueMap(String key, Object value) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        return map;
    }
}