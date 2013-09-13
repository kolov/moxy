package moxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 */
public class Proxy {

    public static final String MOXY_PROPERTIES = "/moxy.properties";

    public static void main(String[] args) throws Exception {
        new Proxy().start();
    }

    public void start() throws Exception {

        URL res = getClass().getResource(MOXY_PROPERTIES);
        if( res == null) {
            System.out.print("Could not locate file " + MOXY_PROPERTIES + " from classpath ");
            return;
        }
        ProxyConfiguration pc = new ProxyConfiguration();
        pc.read(res.openStream());

        System.out.print("Starting proxy wit settings:\n");
        pc.dump(System.out);


        ServerSocket server = new ServerSocket(47000);
        while (true) {
            Socket conn = server.accept();
            new Thread(new Responder(pc, conn)).start();
        }
    }

}
