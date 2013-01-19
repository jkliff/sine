package de.h7r.sine;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.gson.Gson;
import de.h7r.sine.model.SINENode;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Main {

    static {
        // if not given in the command line, set log file name to the default value.
        if (System.getProperty ("sine_log_file") == null) {
            System.setProperty ("sine_log_file", SINEConstants.DEFAULT_OUTPUT_LOG_FILE);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger (Main.class);

    public static void main (String[] args)
            throws Exception {

        // read configuration
        LOG.warn ("/****************************************************************");
        LOG.warn ("SINE configuration server starting ...");

        SINEConfiguration conf = SINEConfiguration.fromSystemProperties ();

        buildData (conf.getConfigPath ());

        // startup jetty with rest interface
        startServerAndListen (conf);
    }

    private static SINENode buildData (File configPath)
            throws IOException {

        LOG.info (String.format ("Reading properties data from store %s", configPath));

        File env = new File (configPath, SINEConstants.ENVS);

        return walk ("", SINEConstants.ENVS, env.listFiles ());

    }

    private static SINENode walk (String prefix,
                                  String currentName,
                                  File[] listFiles)
            throws IOException {

        Gson gson = new Gson ();

        SINENode n = new SINENode (prefix);
        n.setLocalName (currentName);

        for (int i = 0; i < listFiles.length; i++) {
            String prefix2 = prefix + "/" + listFiles[i].getName ();

            LOG.debug ("walking {} > {}", new Object[] {prefix, prefix2});

            if (listFiles[i].isDirectory ()) {
                SINENode n1 = walk (prefix2, listFiles[i].getName (), listFiles[i].listFiles ());
                n.addChild (n1);
                LOG.trace ("Is now parent of (pushed node on) " + prefix2 + ", as " + n1);

            } else {
                SINENode c = new SINENode (prefix2, listFiles[i].getName (), Files.readLines (listFiles[i], Charset.defaultCharset ()));
                n.addChild (c);
                c.close ();
            }
        }

        LOG.trace ("Finished walking {}: {}", new Object[] {prefix, n});
        n.close ();
        return n;
    }

    private static void startServerAndListen (SINEConfiguration conf)
            throws Exception {

        LOG.info (String.format ("Starting embedded web server with configuration: %s", conf));

        Server server = new Server ();
        Connector connector = new SelectChannelConnector ();
        connector.setHost (conf.getBindAddress ());
        connector.setPort (conf.getPort ());
        server.addConnector (connector);

        server.setHandler (new AbstractHandler () {

            @Override
            public void handle (String target,
                                Request baseRequest,
                                HttpServletRequest req,
                                HttpServletResponse resp)
                    throws IOException, ServletException {

                resp.setContentType ("application/json");

                try {
                    String p = req.getRequestURI ().substring (1);

                    LOG.trace (String.format ("Handling request for %s", p));

                    if (p.startsWith (SINEConstants.META)) {

                        // + 1 to remove the leading '/'
                        String cmd = p.trim ().substring (SINEConstants.META.length () + 1);

                        LOG.debug ("Received command {} {}", new Object[] {p, cmd});
                        /*switch (cmd) {
                            case "update":
                                LOG.warn ("STARTING BASE UPDATE should lock, yadda yadda...");
                                
                                break;
                            default:
                                LOG.error ("Received bad management request {}", new Object[] {p});
                                resp.setStatus (HttpServletResponse.SC_BAD_REQUEST);
                        } */
                        
                    } else if (p.startsWith (SINEConstants.ENVS)) {

                        String q = p.trim ().substring (SINEConstants.ENVS.length ());

                        if (q.endsWith ("/")) {
                            q = q.substring (0, Math.max (q.length () - 2, 0));
                        }

                        LOG.debug ("query for " + q);

                        String s = coalesce (NodeRegistry.get (q), "null");

                        LOG.debug ("processed: {} -> {}", new Object[] {p, s});

                        resp.setStatus (HttpServletResponse.SC_OK);
                        resp.getWriter ().write (s);

                    } else {
                        LOG.debug ("Don't know how to handle request to [{}]", p);
                        resp.setStatus (HttpServletResponse.SC_BAD_REQUEST);
                    }

                } catch (Exception e) {
                    LOG.error ("Unhandled error serving request.", e);
                    resp.setStatus (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } finally {
                    baseRequest.setHandled (true);
                }

            }

            private String coalesce (String v,
                                     String def) {

                return v == null ? def : v;
            }

        });

        // ready to rumble
        server.start ();
        server.join ();
    }
}

class SINEConfiguration {

    private String bindAddress;
    private int port;
    private File configPath;

    public String getBindAddress () {

        return bindAddress;
    }

    public static SINEConfiguration fromSystemProperties ()
            throws IOException {

        File storePath = new File (System.getProperty ("store", ".")).getCanonicalFile ();

        File env = new File (storePath, SINEConstants.ENVS);
        Preconditions.checkArgument (env.exists (), "Check your configuration/startup parameters: envs directory does not exist.");

        SINEConfiguration sc = new SINEConfiguration ();

        sc.setBindAddress (System.getProperty ("bindAddress", SINEConstants.DEFAULT_BIND_ADDRESS));
        sc.setPort (Integer.parseInt (System.getProperty ("port", SINEConstants.DEFAULT_PORT)));

        Preconditions.checkArgument (storePath.exists (), String.format ("storePath [%s] does not exist", storePath));
        Preconditions.checkArgument (storePath.isDirectory (), String.format ("storePath [%s] does not point to a directory", storePath));

        sc.setConfigPath (storePath);

        return sc;
    }

    public void setBindAddress (String bindAddress) {

        this.bindAddress = bindAddress;
    }

    public int getPort () {

        return port;
    }

    public void setPort (int port) {

        this.port = port;
    }

    public File getConfigPath () {

        return configPath;
    }

    public void setConfigPath (File configPath) {

        this.configPath = configPath;
    }

    @Override
    public String toString () {

        return "SINEConfiguration [bindAddress=" + bindAddress + ", port=" + port + ", configPath=" + configPath + "]";
    }
}

