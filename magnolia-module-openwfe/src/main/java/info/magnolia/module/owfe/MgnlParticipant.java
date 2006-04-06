package info.magnolia.module.owfe;

import info.magnolia.cms.beans.commands.CommandsMap;
import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.module.owfe.commands.ParametersSetterHelper;
import info.magnolia.module.owfe.jcr.JCRWorkItemAPI;
import openwfe.org.embed.impl.engine.AbstractEmbeddedParticipant;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.log4j.Logger;

public class MgnlParticipant extends AbstractEmbeddedParticipant {
    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(AbstractEmbeddedParticipant.class);
    JCRWorkItemAPI storage = null;

    public MgnlParticipant() throws Exception {
        super();
        storage = new JCRWorkItemAPI();
        if (log.isDebugEnabled())
            log.debug("storage = " + storage);
    }

    public MgnlParticipant(String arg0) throws Exception {
        super(arg0);
        storage = new JCRWorkItemAPI();
        if (log.isDebugEnabled())
            log.debug("storage = " + storage);
    }

    /* (non-Javadoc)
     * @see openwfe.org.embed.engine.EmbeddedParticipant#consume(openwfe.org.engine.workitem.WorkItem)
     */
    public void consume(WorkItem wi) throws Exception {

        // get participant name
        if (log.isDebugEnabled())
            log.debug("enter consume()..");
        if (wi == null) {
            log.error("work item is null");
            return;
        }
        String parName = ((InFlowWorkItem) (wi)).getParticipantName();
        if (log.isDebugEnabled())
            log.debug("participant name = " + parName);
        if (parName.startsWith(MgnlCommand.PREFIX_COMMAND)) // handle commands
        {
            log.info("consume command " + parName + "...");
            if (log.isDebugEnabled())
                log.debug("command name is " + parName);


            try {
                MgnlCommand c = CommandsMap.getCommandFromFullName(parName);
                if (c != null) {
                   log.info("Command has been found through the magnolia catalog:" + c.getClass().getName());

                    // set parameters in the context
                    Context context = MgnlContext.getInstance();
                    context.put(MgnlConstants.INFLOW_PARAM, wi);

                    // translate parameter
                    new ParametersSetterHelper().translateParam(c, context);

                    // execute
                    c.execute(context);
                    
                    OWFEEngine.getEngine().reply((InFlowWorkItem)wi);

                } else // not found, do in the old ways
                    log.error("No command has been found through the magnolia catalog for name:" + parName);

                log.info("consume command " + parName + " end.");
            }
            catch (Exception e) {
                log.error("consume command failed", e);
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("storage = " + storage);
            storage.storeWorkItem("", (InFlowWorkItem) wi);
        }

        if (log.isDebugEnabled())
            log.debug("leave consume()..");

    }


}
