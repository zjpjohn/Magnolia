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



package info.magnolia.cms.gui.dialog;

import javax.servlet.jsp.JspWriter;

/**
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: May 13, 2004
 * Time: 3:32:50 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DialogInterface {

	/**
	 * 
	 *
	 * */

	public void drawHtml(JspWriter out);

	public void drawSubs(JspWriter out);

	public void drawHtmlPreSubs(JspWriter out);

	public void drawHtmlPostSubs(JspWriter out);

}
