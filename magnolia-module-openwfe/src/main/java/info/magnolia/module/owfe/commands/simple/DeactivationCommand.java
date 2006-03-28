package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.module.owfe.commands.MgnlCommand;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class DeactivationCommand extends MgnlCommand {

    public boolean exec(HashMap params, Context Ctx) {
        String path;
        path = (String) params.get(P_PATH);
        try {
            doDeactivate(path);
        } catch (Exception e) {
            log.error("cannot do activate", e);
            return false;
        }
        return true;
    }

    private void doDeactivate(String path) throws Exception {
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_FILE);
        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), REPOSITORY, ContentRepository.getDefaultWorkspace(REPOSITORY), rule);
        syndicator.deActivate(path);
    }

}
