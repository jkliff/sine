package de.h7r.sine;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: john
 * Date: 1/19/13
 * Time: 1:45 PM
 * To change this template use File | Settings | File Templates.
 */
class NodeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger (NodeRegistry.class);

    private static Map<String, SINENode> nodes = Maps.newHashMap ();

    public static void register (SINENode sineNode) {

        nodes.put (sineNode.getPrefix (), sineNode);
        LOG.info ("Registering creation of node " + sineNode.getPrefix ());

    }

    public static String get (String k) {

        if (!nodes.containsKey (k)) {
            return null;
        }
        return nodes.get (k).getRepr ();
    }

    public static ImmutableSet<String> allKeys () {

        return ImmutableSet.copyOf (nodes.keySet ());

    }

}
