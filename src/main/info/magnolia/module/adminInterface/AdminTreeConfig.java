package info.magnolia.module.adminInterface;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.control.TreeMenuItem;
import info.magnolia.cms.gui.misc.Icon;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;


/**
 * Handles the tree rendering for the "config" repository.
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class AdminTreeConfig implements AdminTree {

    /**
     * @see AdminTree#configureTree(Tree, HttpServletRequest, String, String, String, boolean, String)
     */
    public void configureTree(Tree tree, HttpServletRequest request, String path, String pathOpen, String pathSelected,
        boolean create, String createItemType) {

        tree.setIconPage(Tree.ICONDOCROOT + "folder_cubes.gif");
        tree.setPath(path);
        if (create) {
            tree.createNode(createItemType);
        }
        else {
            tree.setPathOpen(pathOpen);
            tree.setPathSelected(pathSelected);
        }
        tree.addItemType(ItemType.NT_CONTENT);
        tree.addItemType(ItemType.NT_CONTENTNODE);
        tree.addItemType(ItemType.NT_NODEDATA);
        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setWidth(1);
        column0.setHtmlEdit();
        column0.setIsLabel(true);
        column0.setWidth(3);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("");
        column1.setTitle("Value");
        column1.setIsNodeDataValue(true);
        column1.setWidth(2);
        column1.setHtmlEdit();
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName("");
        column2.setTitle("Type");
        column2.setIsNodeDataType(true);
        column2.setWidth(2);
        Select typeSelect = new Select();
        typeSelect.setName(tree.getJavascriptTree() + TreeColumn.EDIT_NAMEADDITION);
        typeSelect.setSaveInfo(false);
        typeSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);
        typeSelect.setEvent("onblur", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE);
        typeSelect.setOptions(PropertyType.TYPENAME_STRING, Integer.toString(PropertyType.STRING));
        typeSelect.setOptions(PropertyType.TYPENAME_BOOLEAN, Integer.toString(PropertyType.BOOLEAN));
        typeSelect.setOptions(PropertyType.TYPENAME_LONG, Integer.toString(PropertyType.LONG));
        typeSelect.setOptions(PropertyType.TYPENAME_DOUBLE, Integer.toString(PropertyType.DOUBLE));
        // todo: typeSelect.setOptions(PropertyType.TYPENAME_DATE,Integer.toString(PropertyType.DATE));
        column2.setHtmlEdit(typeSelect.getHtml());
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass("");
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        columnIcons.setIconsPermission(true);
        TreeColumn column4 = new TreeColumn(tree.getJavascriptTree(), request);
        column4.setName(MetaData.LAST_MODIFIED);
        column4.setIsMeta(true);
        column4.setDateFormat("yy-MM-dd, HH:mm");
        column4.setWidth(2);
        column4.setTitle("Mod. date");
        tree.addColumn(column0);
        tree.addColumn(column1);
        tree.addColumn(column2);
        if (Server.isAdmin()) {
            tree.addColumn(columnIcons);
        }
        tree.addColumn(column4);
        TreeMenuItem menuNewPage = new TreeMenuItem();
        menuNewPage.setLabel("<img src=\""
            + request.getContextPath()
            + new Icon().getSrc(Icon.PAGE, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px;\">New folder</span>");
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.NT_CONTENT + "');");
        menuNewPage.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");
        menuNewPage.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotContentNode");
        TreeMenuItem menuNewContentNode = new TreeMenuItem();
        menuNewContentNode.setLabel("<img src=\""
            + request.getContextPath()
            + new Icon().getSrc(Icon.CONTENTNODE, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px\">New content node</span>");
        menuNewContentNode.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.NT_CONTENTNODE + "');");
        menuNewContentNode.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");
        TreeMenuItem menuNewNodeData = new TreeMenuItem();
        menuNewNodeData.setLabel("<img src=\""
            + request.getContextPath()
            + new Icon().getSrc(Icon.NODEDATA, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px;\">New node data</span>");
        menuNewNodeData.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.NT_NODEDATA + "');");
        menuNewNodeData.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");
        TreeMenuItem menuDelete = new TreeMenuItem();
        menuDelete.setLabel("Delete");
        // menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();");
        menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuCopy = new TreeMenuItem();
        menuCopy.setLabel("Copy");
        menuCopy.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();");
        TreeMenuItem menuCut = new TreeMenuItem();
        menuCut.setLabel("Move");
        menuCut.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuCut.setOnclick(tree.getJavascriptTree() + ".cutNode();");
        TreeMenuItem menuActivateExcl = new TreeMenuItem();
        menuActivateExcl.setLabel("Activate this node");
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);");
        menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");
        TreeMenuItem menuActivateIncl = new TreeMenuItem();
        menuActivateIncl.setLabel("Activate incl. sub nodes");
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);");
        menuActivateIncl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuActivateIncl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");
        TreeMenuItem menuDeActivate = new TreeMenuItem();
        menuDeActivate.setLabel("De-activate");
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");");
        menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");
        TreeMenuItem menuRefresh = new TreeMenuItem();
        menuRefresh.setLabel("Refresh");
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();");
        tree.addMenuItem(menuNewPage);
        tree.addMenuItem(menuNewContentNode);
        tree.addMenuItem(menuNewNodeData);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuDelete);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuCopy);
        tree.addMenuItem(menuCut);
        if (Server.isAdmin()) {
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuActivateIncl);
            tree.addMenuItem(menuDeActivate);
        }
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuRefresh);

    }

}
