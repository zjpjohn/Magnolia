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
package info.magnolia.test.hamcrest;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import javax.jcr.RepositoryException;

/**
 * A minimal set of useful Matchers to test Content.
 * TODO: use hamcrest-generator to generate this class out of non-inner Matcher classes ?
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ContentMatchers {

    // TODO if we name this method hasProperty, we conflict with org.hamcrest.Matchers#hasProperty
    public static Matcher<Content> hasNodeData(final String propertyName) {
        return new TypeSafeMatcher<Content>() {
            @Override
            protected boolean matchesSafely(Content item) {
                try {
                    return item.hasNodeData(propertyName);
                } catch (RepositoryException e) {
                    throw new RuntimeException(e); // TODO
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a property named '").appendText(propertyName).appendText("'");
            }

            @Override
            protected void describeMismatchSafely(Content item, Description mismatchDescription) {
                mismatchDescription.appendText("there is no '").appendText(propertyName).appendText("'");
            }
        };
    }

    // TODO validate this works with properties other than String
    // TODO combine with hasProperty(String)
    public static Matcher<Content> hasNodeData(final String propertyName, final Object expectedValue) {
        return new TypeSafeMatcher<Content>() {
            @Override
            protected boolean matchesSafely(Content item) {
                try {
                    if (!item.hasNodeData(propertyName)) {
                        return false;
                    } else {
                        final NodeData nodeData = item.getNodeData(propertyName);
                        final Object value = NodeDataUtil.getValueObject(nodeData);
                        return expectedValue == null ? value == null : expectedValue.equals(value);
                    }
                } catch (RepositoryException e) {
                    throw new RuntimeException(e); // TODO
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a property named '").appendText(propertyName).appendText("' with a value of '").appendText(String.valueOf(expectedValue)).appendText("'");
            }

            @Override
            protected void describeMismatchSafely(Content item, Description mismatchDescription) {
                try {
                    if (!item.hasNodeData(propertyName)) {
                        mismatchDescription.appendText("there is no '").appendText(propertyName).appendText("'");
                    } else {
                        final String currentValue = item.getNodeData(propertyName).getString();
                        mismatchDescription.appendText("a property named '").appendText(propertyName).appendText("' with a value of '").appendText(currentValue).appendText("'");
                    }
                } catch (RepositoryException e) {
                    throw new RuntimeException(e); // TODO
                }
            }
        };
    }

}