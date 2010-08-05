/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.rest.json;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.rest.dialog.Dialog;
import info.magnolia.module.rest.dialog.DialogRegistry;
import info.magnolia.module.rest.dialog.ValidationResult;
import info.magnolia.module.rest.tree.WebsitePage;
import info.magnolia.module.rest.tree.WebsitePageList;
import info.magnolia.module.rest.tree.commands.CreateWebsiteNodeCommand;
import info.magnolia.module.rest.tree.commands.DeleteNodeCommand;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service providing data in form suitable for tree browsing (i.e. incl. children and with the requested property values listed for each child).
 * @author tobias
 *
 * @deprecated since this service is specific to browsing the trees, which is admin central functionality it should be probably provided by the admin central or wcm module.
 */
@Path("/website")
public class WebsiteJsonEndpoint {

    // We need to decide on suitable verbs for activate, deactive, move, copy and so on..

    @GET
    public WebsitePageList getRootNode() throws RepositoryException {
        return getNode("");
    }

    @GET
    @Path("{path:(.)*}")
    public WebsitePageList getNode(@PathParam("path") String path) throws RepositoryException {
        return marshallChildren(path);
    }

    // Since JAX-RS wont match an empty path param (hopefully im doing something wrong...)
    @POST
    @Path("/create")
    public WebsitePageList create() throws RepositoryException {
        return create("");
    }

    // This should be PUT... but we use POST for now since IPSecurity blocks PUT
    @POST
    @Path("{path:(.)*}/create")
    public WebsitePageList create(@PathParam("path") String path) throws RepositoryException {

        CreateWebsiteNodeCommand command = new CreateWebsiteNodeCommand();
        command.setRepository(ContentRepository.WEBSITE);
        command.setPath(StructuredPath.valueOf(path));
        command.setItemType("mgnl:content");

        command.execute();

        WebsitePageList list = new WebsitePageList();
        list.add(marshallNode(getContent(command.getUniquePath())));
        return list;
    }

    @POST
    @Path("{path:(.)*}/update")
    public WebsitePage update(@PathParam("path") String path, WebsitePage page) {

        // read

        // set name, title and template

        // save

        // return

        return new WebsitePage();
    }

    @POST
    @Path("{path:(.)*}/delete")
    public WebsitePage delete(@PathParam("path") String path) throws Exception {

        StructuredPath p = StructuredPath.valueOf(path);

        if (p.isRoot()) {
            // cant delete the root node
        }

        DeleteNodeCommand command = new DeleteNodeCommand();
        command.setRepository(ContentRepository.WEBSITE);
        command.setPath(p);

        command.execute();

        return new WebsitePage();
    }

    @POST
    @Path("{path:(.)*}/edit")
    public Dialog edit(@PathParam("path") String path, @QueryParam("dialogName") String dialogName) throws RepositoryException {

        Content storageNode = getContent(path);

        Dialog dialog = DialogRegistry.getInstance().getDialogProvider(dialogName).create();

        dialog.bind(storageNode);

        return dialog;
    }

    @POST
    @Path("{path:(.)*}/save")
    public ValidationResult save(@PathParam("path") String path, @QueryParam("dialogName") String dialogName, @Context UriInfo uriInfo) throws Exception {

        Content storageNode = getContent(path);

        Dialog dialog = DialogRegistry.getInstance().getDialogProvider(dialogName).create();

        dialog.bind(storageNode);
        dialog.bind(uriInfo.getQueryParameters());

        ValidationResult validationResult = new ValidationResult();

        dialog.validate(validationResult);

        if (validationResult.isSuccess()) {
            dialog.save(storageNode);
            synchronized (ExclusiveWrite.getInstance()) {
                storageNode.save();
            }
        }

        // was it successful ?

        return validationResult;
    }

    private WebsitePageList marshallChildren(String parentPath) throws RepositoryException {

        Content parentNode = getContent(parentPath);

        if (parentNode == null)
            return null;

        return marshallChildren(parentNode);
    }

    private WebsitePageList marshallChildren(Content parentNode) {

        Iterator<Content> contentIterator = parentNode.getChildren(ItemType.CONTENT).iterator();

        WebsitePageList pages = new WebsitePageList();
        while (contentIterator.hasNext()) {
            Content content = contentIterator.next();
            pages.add(marshallNode(content));
        }
        return pages;
    }

    private WebsitePage marshallNode(String path) throws RepositoryException {

        Content storageNode = getContent(path);

        if (storageNode == null)
            return null;

        return marshallNode(storageNode);
    }

    private WebsitePage marshallNode(Content content) {

        boolean permissionWrite = content.isGranted(info.magnolia.cms.security.Permission.WRITE);
        boolean isActivated = content.getMetaData().getIsActivated();
        boolean hasChildren = !content.getChildren(ItemType.CONTENT).isEmpty();

        String title = content.getNodeData("title").getString();

        WebsitePage page = new WebsitePage();
        page.setName(content.getName());
        page.setPath(content.getHandle());
        page.setHasChildren(hasChildren);
        page.setStatus(isActivated ? "activated" : "modified");
        page.setTemplate(getTemplateName(content));
        page.setTitle(title);
        page.setUuid(content.getUUID());
        page.setAvailableTemplates(getAvailableTemplates(content));

        return page;
    }

    private Content getContent(String path) throws RepositoryException {
        return getContent(StructuredPath.valueOf(path));
    }

    private Content getContent(StructuredPath path) throws RepositoryException {

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        if (!hierarchyManager.isExist(path.toString()))
            return null;

        return hierarchyManager.getContent(path.toString());
    }

    private List<String> getAvailableTemplates(Content content) {
        TemplateManager templateManager = TemplateManager.getInstance();
        Iterator<Template> templates = templateManager.getAvailableTemplates(content);
        ArrayList<String> list = new ArrayList<String>();
        while (templates.hasNext()) {
            Template template = templates.next();
            list.add(template.getI18NTitle());
        }
        return list;
    }

    public String getTemplateName(Content content) {
        TemplateManager templateManager = TemplateManager.getInstance();
        String templateName = content.getMetaData().getTemplate();
        Template template = templateManager.getTemplateDefinition(templateName);
        return template != null ? template.getI18NTitle() : StringUtils.defaultString(templateName);
    }
}