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
package info.magnolia.module.templating.paragraphs;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentWrapper;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.mock.MockContent;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspParagraphRendererTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();

        MgnlContext.setInstance(null);

        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testExposesNodesAsMaps() {
        final WebContext magnoliaCtx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(magnoliaCtx);

        final Content page = createStrictMock(Content.class);
        // we have 2 different nodes when rendering a paragraph, but getHandle() is only called on the page node, when using NodeMapWrapper
        expect(page.getHandle()).andReturn("/myPage").times(2);
        final Content paragraph = createStrictMock(Content.class);

        final AggregationState aggState = new AggregationState();
        aggState.setMainContent(page);
        expect(magnoliaCtx.getAggregationState()).andReturn(aggState);

        replay(magnoliaCtx, page, paragraph);
        final Map templateCtx = new HashMap();
        new JspParagraphRenderer().setupContext(templateCtx, paragraph, null, null, null);

        // other tests should verify the other objects !
        assertEquals("Unexpected amount of objects in context", 7, templateCtx.size());
        assertTrue(templateCtx.get("actpage") instanceof Map);
        assertEquals(page, unwrap((Content) templateCtx.get("actpage")));
        assertTrue(templateCtx.get("content") instanceof Map);
        assertEquals(paragraph, unwrap((Content) templateCtx.get("content")));

        verify(magnoliaCtx, page, paragraph);
    }

    /*
    public void testIncludesPathWhenProvided() throws IOException, ServletException {
        final Paragraph paragraph = new Paragraph();
        paragraph.setName("plop");
        paragraph.setTemplatePath("/foo/bar.jsp");
        final WebContext ctx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(ctx);

        final StringWriter w = new StringWriter();
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        MockAggregationState mas = new MockAggregationState();
        mas.setMainContent(new MockContent("bla"));
        expect(ctx.get("content")).andReturn(null);
        expect(ctx.get("result")).andReturn(null);
        expect(ctx.get("action")).andReturn(null);
        expect(ctx.get("paragraphDef")).andReturn(null);

        expect(ctx.put(eq("content"), isA(NodeMapWrapper.class))).andReturn(null);
        expect(ctx.getAggregationState()).andReturn(mas);
        ctx.setAttribute(eq("content"), isA(NodeMapWrapper.class), eq(1));
        ctx.setAttribute(eq("paragraphDef"), isA(Paragraph.class), eq(1));
        ctx.include("/foo/bar.jsp", w);
        expect(ctx.put("content", null)).andReturn(null);
        expect(ctx.put("paragraphDef", null)).andReturn(null);
        replay(ctx);

        renderer.render(null, paragraph, w);

        verify(ctx);
    }
    */

    public void testCantRenderWithoutParagraphPathCorrectlySet() throws IOException {
        final WebContext webContext = createNiceMock(WebContext.class);
        MgnlContext.setInstance(webContext);
        replay(webContext);
        final Content c = new MockContent("pouet");
        final Paragraph paragraph = new Paragraph();
        paragraph.setName("plop");
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            renderer.render(c, paragraph, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Unable to render info.magnolia.cms.beans.config.Paragraph plop in page /pouet: templatePath not set.", e.getMessage());
        }
        verify(webContext);
    }


    public void testShouldFailIfNoContextIsSet() throws IOException {
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            final Paragraph p = new Paragraph();
            p.setName("plop");
            p.setTemplatePath("/foo/bar.jsp");
            renderer.render(null, p, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("MgnlContext is not set for this thread", e.getMessage());
        }
    }

    public void testShouldFailIfContextIsNotWebContext() throws IOException {
        MgnlContext.setInstance(createStrictMock(Context.class));
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            final Paragraph p = new Paragraph();
            p.setName("plop");
            p.setTemplatePath("/foo/bar.jsp");
            renderer.render(null, p, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("This paragraph renderer can only be used with a WebContext", e.getMessage());
        }
    }

    private Content unwrap(Content c) {
        if (c instanceof ContentWrapper) {
            return unwrap(((ContentWrapper) c).getWrappedContent());
        }
        return c;
    }
}
