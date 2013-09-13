package moxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 */
public class Responder implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Responder.class);

    private Socket conn;
    private ProxySettings settings;

    public Responder(ProxySettings pc, Socket conn) {
        this.conn = conn;
        this.settings = pc;
    }

    @Override
    public void run() {
        try {
            _run();
        } catch (Exception e) {
            LOG.error("Thread closed by exception: ", e);
        }
    }


    public void _run() throws Exception {
        InputStream is = conn.getInputStream();


        String line = HttpHelper.readLine(is);
        if (line == null || line.length() == 0) {
            return;
        }
        String url = HttpHelper.getUrl(line);

        LOG.error("URL from [{}] -> [{}]", line, url);

        LOG.debug("Going to match [{}]", url);
        ProxyMapping.Destination destination = settings.getMapping().map(url);
        if (destination != null) {
            LOG.debug("Matched {} -> {}:{}", new Object[]{url, destination.host,
                    destination.port});
            forward(destination.host, destination.port, line, url, is, conn);
        } else {
            LOG.error("No match for {}", url);
        }


    }

    private void forward(String host, int port, String line, String url, InputStream is,
                         Socket firstConn) throws IOException {
        LOG.debug("Start processing  {}  ", url);
        Socket secondClient = null;
        // open  connection to the needed host
        try {
            secondClient = new Socket(host, port);
            // secondClient.setSoTimeout(3000);
        } catch (IOException e) {
            // can't connect, close existing connection
            LOG.debug(" can't connect to {}:{}, close existing connection.", host, port);
            firstConn.close();
            throw e;
        }

        try {
            // request to host
            sendRequest(host, line, url, is, secondClient);
            settings.getExecutor().execute(new Responder(settings, conn));
            forwardResponse(secondClient.getInputStream(), firstConn.getOutputStream());
            LOG.debug("Finished {}", url);
        } finally {
            secondClient.close();
        }
    }

    private void sendRequest(String host, String line, String url, InputStream is,
                             Socket secondClient) throws IOException {
        OutputStream out = secondClient.getOutputStream();
        out.write(line.getBytes(), 0, line.length());
        byte[] buf = new byte[2048];
        int n;
        int requestLen = 0;
        while (is.available() > 0 && (n = readBuffer(buf, is)) > 0) {
            out.write(buf, 0, n);
            requestLen += n;
        }
        LOG.debug("Sent " + requestLen + " bytes for " + url + "-> " + host);
        out.flush();
    }

    private void forwardResponse(InputStream hostClientIn, OutputStream proxyClientOut) throws IOException {
        byte[] buf = new byte[2048];
        for (; ; ) {
            int read = readBuffer(buf, hostClientIn);
            if (read < 1) {
                break;
            }
            proxyClientOut.write(buf, 0, read);
        }
        proxyClientOut.flush();
    }

    private int readBuffer(byte[] buf, InputStream clientInput) throws IOException {
        try {
            return clientInput.read(buf, 0, buf.length);
        } catch (java.net.SocketTimeoutException e) {
            // on timeout, if setSoTimeout was set
            return 0;
        }
    }


}

