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
package info.magnolia.ui.admincentral.column;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

import info.magnolia.jcr.util.JCRMetadataUtil;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;

import java.io.Serializable;
import java.util.Calendar;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * Column that displays a property for a nodes MetaData. Used to display the modification date of
 * content nodes.
 */
public class MetaDataColumn extends AbstractColumn<MetaDataColumnDefinition> implements Serializable {

    private String datePattern;

    protected static final String DEFAULT_DATE_PATTERN = "yy-MM-dd, HH:mm";

    public MetaDataColumn(MetaDataColumnDefinition def) {
        super(def);
    }

    @Override
    public Component getComponent(Item item) throws RepositoryException {
        if (item instanceof Node) {
            Node node = (Node) item;
            Calendar date = JCRMetadataUtil.getMetaData(node).getCreationDate();
            final String pattern = StringUtils.isNotBlank(datePattern) ? datePattern : DEFAULT_DATE_PATTERN;
            final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance(pattern);
            return date != null ? new Label(DATE_FORMAT.format(date.getTime())) : new Label("");
        }
        return new Label();
    }

    /**
     * @param datePattern a {@link SimpleDateFormat} compatible pattern
     */
    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }
    /**
     * @return {@link SimpleDateFormat} compatible pattern
     */
    public String getDatePattern() {
        return datePattern;
    }
}
