/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.util;


import jdsl.core.ref.ComparableComparator;
import info.magnolia.cms.core.ContentNode;


/**
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Sameer Charles
 * @version 1.1
 */



public class StringComparator extends ComparableComparator  {



    private String nodeDataName;


    public StringComparator(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }


    public int compare(Object o, Object o1) throws ClassCastException {
        String URI1 = ((ContentNode)o).getNodeData(this.nodeDataName).getString();
        String URI2 = ((ContentNode)o1).getNodeData(this.nodeDataName).getString();
        return super.compare(URI1, URI2);
    }


    public boolean isComparable(Object o) {
        return true;
    }

}
