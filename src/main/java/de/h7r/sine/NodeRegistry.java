package de.h7r.sine;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import de.h7r.sine.model.SINENode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NodeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger (NodeRegistry.class);

    private static Map<String, SINENode> nodes = Maps.newHashMap ();

    public static void register (SINENode sineNode) {

        nodes.put (sineNode.getPrefix (), sineNode);
        LOG.info ("Registering creation of node " + sineNode.getPrefix ());

    }

    // FIXME: threadsafety
    public static void truncate () {
        LOG.info ("TRUNCATING NODES!");
        nodes = Maps.newHashMap ();
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
