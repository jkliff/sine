package de.h7r.sine.aj;

import de.h7r.sine.NodeRegistry;
import de.h7r.sine.model.SINENode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOT USED until there is support from aspectj-maven-plugin for java 7.
 */
public aspect SINENodeAutoRegistration {

    private static final Logger LOG = LoggerFactory.getLogger (SINENodeAutoRegistration.class);
    
    after(SINENode f) returning: this(f) && execution(SINENode.new(..)) {
        LOG.info ("registering sine node");
        NodeRegistry.register (f);
    }
    //
}
