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



package info.magnolia.cms.exchange.ice;

import info.magnolia.exchange.PacketHeader;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Enumeration;



/**
 * Date: May 4, 2004
 * Time: 5:10:56 PM
 *
 * @author Sameer Charles
 */



public class PacketHeaderImpl implements PacketHeader {



    private Hashtable header;




    public PacketHeaderImpl() {
        this.header = new Hashtable();
    }



    public void addHeader(String name, String value)
            throws IllegalArgumentException {
        if (value == null)
            throw (new IllegalArgumentException("Null value not allowed"));

        this.header.put(name, value);
    }



    public String getValueByName(String name) {
        return (String) this.header.get(name);
    }



    public Enumeration getKeys() {
        return this.header.keys();
    }



}
