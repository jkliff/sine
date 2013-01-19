package de.h7r.sine.model;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.h7r.sine.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: john
 * Date: 1/19/13
 * Time: 1:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class SINENode {

    private static final Logger LOG = LoggerFactory.getLogger (SINENode.class);

    private Set<SINENode> children = Sets.newHashSet ();
    private String prefix;
    private String localName;
    private String content;
    private String repr;
    private boolean closed = false;

    public SINENode (String prefix2) {

        this.prefix = prefix2;
        //NodeRegistry.register (this);
    }

    public void close () {

        Gson gson = new GsonBuilder ().create ();
        closed = true;

        LOG.info ("finished " + prefix);
        if (children.isEmpty ()) {
            repr = gson.toJson (content);
        } else {
            StringBuilder sb = new StringBuilder ();
            sb.append ("{");
            boolean more = false;
            for (SINENode n : children) {
                LOG.info ("local name on building: " + n.getLocalName ());

                if (more) {
                    sb.append (",");
                } else {
                    more = true;
                }

                sb.append (gson.toJson (n.getLocalName ()));
                sb.append (":");
                sb.append (n.getRepr ());

            }
            sb.append ("}");

            repr = sb.toString ();
        }
    }

    public String getRepr () {
        // LOG.info ("repr " + localName);
        Preconditions.checkState (closed, "node not closed yet");

        return repr;
    }

    public void setContent (List<String> readLines) {

        this.content = Joiner.on ("\n").join (readLines);

    }

    public SINENode (String prefix2,
                     String localName,
                     List<String> readLines) {

        this (prefix2);
        this.localName = localName;
        setContent (readLines);

        LOG.debug ("created node \t" + prefix2 + ", " + localName);
    }

    public void addChild (SINENode v) {

        children.add (v);

        LOG.trace (String.format ("adding child to %s: %s", this.localName, v));
    }

    public String getPrefix () {

        return prefix;
    }

    @Override
    public String toString () {

        Map<String, String> r = Maps.newHashMap ();
        r.put (localName, content);
        if (!children.isEmpty ()) {
            for (SINENode c : children) {
                r.put (c.getLocalName (), c.toString ());
            }
        }
        return r.toString ();
    }

    public String getLocalName () {

        return localName;
    }

    public void setLocalName (String localName) {

        this.localName = localName;
    }

}
