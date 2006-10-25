/**
 * 
 */
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;


/**
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public abstract class BaseActivationCommand extends RuleBasedCommand {

    /**
     * You can pass a syndicator to the command (optional)
     */
    public static final String ATTRIBUTE_SYNDICATOR = "syndicator";

    private Syndicator syndicator;

    public Syndicator getSyndicator() {
        // lazy bound, but only if this is a clone
        if (syndicator == null && isClone()) {
            syndicator = (Syndicator) FactoryUtil.newInstance(Syndicator.class);
            syndicator.init(
                MgnlContext.getUser(),
                this.getRepository(),
                ContentRepository.getDefaultWorkspace(this.getRepository()),
                getRule());
        }
        return syndicator;
    }

    /**
     * @param syndicator the syndicator to set
     */
    public void setSyndicator(Syndicator syndicator) {
        this.syndicator = syndicator;
    }

}
