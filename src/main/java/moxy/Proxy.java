package moxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class Proxy {

    public static final String MOXY_PROPERTIES = "/moxy.properties";

    ExecutorService executor = Executors.newFixedThreadPool(100);

    public static void main(String[] args) throws Exception {
        new Proxy().start();
    }

    public void start() throws Exception {

        URL res = getClass().getResource(MOXY_PROPERTIES);
        if( res == null) {
            System.out.print("Could not locate file " + MOXY_PROPERTIES + " from classpath ");
            return;
        }
        ProxyMapping pc = new ProxyMapping();
        pc.read(res.openStream());

        System.out.print("Starting proxy with settings:\n");
        pc.dump(System.out);


        ServerSocket server = new ServerSocket(47000);
        while (true) {
            Socket conn = server.accept();
            executor.execute(new Responder(pc, conn));
        }
    }

}
