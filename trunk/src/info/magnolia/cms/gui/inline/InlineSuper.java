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



package info.magnolia.cms.gui.inline;

/**
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: Jul 20, 2004
 * Time: 8:29:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class InlineSuper {
	private String path="";
	private String paragraph="";
	private String contentNode;


	public void setPath(String s) {this.path = s;}
	public String getPath() {return this.path;}

	public void setParagraph(String s) {this.paragraph=s;}
	public String getParagraph() {return this.paragraph;}

}
