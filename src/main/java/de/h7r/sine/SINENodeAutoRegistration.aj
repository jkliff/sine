package de.h7r.sine.aj;

import de.h7r.sine.NodeRegistry;
import de.h7r.sine.model.SINENode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: john
 * Date: 1/19/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public aspect SINENodeAutoRegistration {

    private static final Logger LOG = LoggerFactory.getLogger (SINENodeAutoRegistration.class);
    
    after(SINENode f) returning: this(f) && execution(SINENode.new(..)) {
        LOG.info ("registering sine node");
        NodeRegistry.register (f);
    }
    //
}
