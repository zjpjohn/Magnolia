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


package info.magnolia.cms.taglibs;


import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.util.Resource;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * User: marcelsalathe
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author marcel Salathe
 * @author Sameer Charles
 * @version 1.1
 */


public class Include extends BodyTagSupport {


    private static Logger log = Logger.getLogger(Include.class);
    private ContentNode contentNode;
    private String path;

    private ArrayList attributes;


    /**
     * <p>after the body, this method sets attributes</p>
     *
     * @return int
     */
    public int doAfterBody() {
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        if ((attributes != null) && (attributes.size()>0)) {
            Iterator i = attributes.iterator();
            while (i.hasNext()) {
                String[] s = (String[])i.next();
                req.setAttribute(s[0],s[1]);
            }
        }

        return SKIP_BODY;
    }


        /**
     * <p>end of includeTemplate tag, includes template and removes attributes</p>
     *
     * @return int
     */
    public int doEndTag() {
        this.include();
        this.removeAttributes();
        return EVAL_PAGE;
    }


    /**
     *
     */
    private void removeAttributes() {
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        if ((attributes != null) && (attributes.size()>0)) {
            Iterator i = attributes.iterator();
            while (i.hasNext()) {
                String[] s = (String[])i.next();
                req.removeAttribute(s[0]);
            }
        }
        attributes = null;
    }



    /**
     *
     */
    private void include() {
        if (this.contentNode == null)
            this.includeFromResource();
        else
            this.includeFromParam();
    }



    /**
     *
     */
    private void includeFromResource() {
        try {
            HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
            if (this.path == null) {
                pageContext.include(Resource.getLocalContentNode(req).getTemplate());
			}
			else
                pageContext.include(this.path);
        } catch (ServletException se) {
            log.error(se.getMessage());
        } catch (Exception e) {log.error(e.getMessage());}
    }



    /**
     *
     */
    private void includeFromParam() {
        Resource.setLocalContentNode((HttpServletRequest)pageContext.getRequest(),this.contentNode);
        try {
            pageContext.include(this.path);
        } catch (ServletException se) {
            log.error(se.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            Resource.removeLocalContentNode((HttpServletRequest)pageContext.getRequest());
        }

    }


	/**
	 * @deprecated
	 */
	public void setContainer(ContentNode contentNode) {
		this.setContentNode(contentNode);
	}



    /**
     * <p>set the content object (type ContentNode)</p>
     *
     * @param contentNode
     */
    public void setContentNode(ContentNode contentNode) {
        this.contentNode = contentNode;
    }



    /**
     * <p>set the jsp path</p>
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }


    /**
     * @param name , name of attribute to pass with the include
     * @param value , value of attribute to pass with the include
     */
    public void setAttribute(String name, String value) {
        if (attributes == null) attributes = new ArrayList();
        String [] attributesArray = new String[] {name,value};
        attributes.add(attributesArray);

    }


}
