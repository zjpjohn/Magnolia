
[#-- TODO cringele:does not work anymore, cause cmsfn.page(node) return the page now itself if a page-node was passed.
cmsfn.parent(content, "mgnl:content") will always return a parent page node.
Like this the script can only work either if included from page's script, but not as a Area's script. Or otherwise around.
Much less flexible. Needing cmsfn.page back again? --]
[#macro renderNavigation pageNode maxDepth depth=0 ]

    [#-- Is top page of the site structure -> rendering the top page on same navigation level as its sub-pages--]
    [#assign isRootPage = (pageNode.@path==cmsfn.root(content, "mgnl:content").@path)!false]
    [#if isRootPage && depth == 0]
        [#if pageNode.@path != content.@path]
            <li>
                <a href="${cmsfn.link(pageNode)}.html">${pageNode.title!pageNode.@name}</a>
            </li>
        [#else]
            <li class="selected">
                <span>${pageNode.title!pageNode.@name}</span>
            </li>
        [/#if]
    [/#if]


    [#assign childPages = cmsfn.children(pageNode, "mgnl:content")!]
    [#-- Has child pages AND is not deeper as defined in max allowed depth. --]
    [#if childPages?size!=0 && depth < maxDepth]
        [#list childPages as childPage]

            [#-- will need something again as cmsfn.page(content) [#assign isSelected = (childPage.@path == cmsfn.parent(content, "mgnl:content").@path)!false] --]
            [#assign isSelected = (childPage.@path == content.@path)!false]
            [#assign isSelectedParent = (childPage.@path == cmsfn.parent(content).@path)!false]

            [#if isSelected || isSelectedParent]
                <li class="selected">
                    [#if isSelected]
                        <span>${childPage.title!childPage.@name}</span>
                    [#else]
                        <a href="${cmsfn.link(childPage)}.html"><span>${childPage.title!childPage.@name}</span></a>
                    [/#if]
                </li>
                <ul class="second">
                    [@renderNavigation childPage maxDepth depth+1 /]
                </ul>
            [#else]

                <li>
                    <a href="${cmsfn.link(childPage)}.html">${childPage.title!childPage.@name}</a>
                </li>
            [/#if]

        [/#list]
    [/#if]

[/#macro]