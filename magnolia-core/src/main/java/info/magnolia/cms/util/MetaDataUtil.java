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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class MetaDataUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MetaDataUtil.class);

    /**
     * @deprecated this is a util
     */
    private Content content;

    /**
     * @deprecated this is a util
     */
    public MetaDataUtil(Content c) {
        this.setContent(c);
    }

    /**
     * @deprecated this is a util
     */
    public void setContent(Content c) {
        this.content = c;
    }

    /**
     * @deprecated this is a util
     */
    public Content getContent() {
        return this.content;
    }

    /**
     * @deprecated this is a util
     */
    public String getPropertyValueString(String propertyName) {
        return getPropertyValueString(content, propertyName, null);
    }

    public static String getPropertyValueString(Content content, String propertyName) {
        return getPropertyValueString(content, propertyName, null);
    }

    /**
     * @deprecated this is a util
     */
    public String getPropertyValueString(String propertyName, String dateFormat) {
        return getPropertyValueString(content, propertyName, dateFormat);
    }

    /**
     * <p/> Returns the representation of the value as a String:
     * </p>
     * @return String
     */
    public static String getPropertyValueString(Content content, String propertyName, String dateFormat) {
        try {
            if (propertyName.equals(MetaData.CREATION_DATE) || propertyName.equals(MetaData.LAST_MODIFIED) || propertyName.equals(MetaData.LAST_ACTION)) {
                Date date = content.getMetaData().getDateProperty(propertyName).getTime();

                return DateUtil.format(date, dateFormat);
            }
            else if (propertyName.equals(MetaData.ACTIVATED)) {
                return Boolean.toString(content.getMetaData().getBooleanProperty(propertyName));
            }
            else {
                return content.getMetaData().getStringProperty(propertyName);
            }
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getActivationStatusIcon(Content content) {
        String imgSrc;
        switch (content.getMetaData().getActivationStatus()) {
            case MetaData.ACTIVATION_STATUS_MODIFIED :
                imgSrc = "indicator_yellow.gif";
                break;
            case MetaData.ACTIVATION_STATUS_ACTIVATED :
                imgSrc = "indicator_green.gif";
                break;
            default :
                imgSrc = "indicator_red.gif";
        }
        return imgSrc;
    }

}
