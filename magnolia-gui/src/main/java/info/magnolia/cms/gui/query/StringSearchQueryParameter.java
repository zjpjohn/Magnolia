/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.query;

/**
 * @author Sameer Charles
 * $Id$
 */
public class StringSearchQueryParameter extends SearchQueryParameter {

    /**
     * sql constraint "CONTAINS"
     * */
    public static final String CONTAINS = "contains";

    /**
     * sql constraint "LIKE"
     * */
    public static final String LIKE = "like";

    /**
     * sql constraint "STARTS"
     * */
    public static final String STARTS = "starts";

    /**
     * sql constraint "ENDS"
     * */
    public static final String ENDS = "ends";

    /**
     * @param name of this parameter
     * @param value
     * @param constraint check SearchQueryParameter constants
     * */
    public StringSearchQueryParameter(String name, String value, String constraint) {
        super(name, value, constraint);
    }

    /**
     * get value
     * @return String value
     * */
    public String getValue() {
        return (String) this.value;
    }

    /**
     * set value
     * @param value
     * */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * get string representation
     * @return string representation if this expression
     * */
    public String toString() {
        if (this.getValue() == null)
            return "";
        StringBuffer buffer = new StringBuffer();
        if (this.getConstraint().equalsIgnoreCase(LIKE)) {
            buffer.append(this.getName());
            buffer.append(" like '%");
            buffer.append(this.getValue());
            buffer.append("%'");
        } else if (this.getConstraint().equalsIgnoreCase(CONTAINS)) {
            buffer.append("contains(");
            buffer.append(this.getName());
            buffer.append(", '");
            buffer.append(this.getValue());
            buffer.append("')");
        }
        return buffer.toString();
    }


}
