package moxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: M01I187
 * Date: 11-9-13
 * Time: 17:56
 * To change this template use File | Settings | File Templates.
 */
public class Responder implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Responder.class);

    private Socket conn;
    private List<ProxyConfiguration.Entry> entries;

    public Responder(ProxyConfiguration pc, Socket conn) {
        this.conn = conn;
        this.entries = pc.getEntries();
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


        for (; ; ) {
            String line = readLine(is);
            if (line == null || line.length() == 0) {
                break;
            }
            String url = HttpHelper.getUrl(line);
            if( url == null) {
                LOG.error("URL null grom[{}]", url);
            }
            boolean matched = false;
            for (ProxyConfiguration.Entry entry : entries) {
                LOG.debug("Going to match {}", url);
                if (entry.regex.matcher(url).matches()) {
                    LOG.debug("Matched {} -> {}:{}", new Object[]{url, entry.destination.host,
                            entry.destination.port});
                    forward(entry.destination.host, entry.destination.port, line, url, is, conn);
                    matched = true;
                    break;
                }
            }
            // no match
            if (!matched) {
                LOG.error("No match for {}", url);
            }

        }
    }

    private void forward(String host, int port, String line, String url, InputStream is,
                         Socket firstConn) throws IOException {
        LOG.debug("Start processing  {}  ", url);
        Socket secondClient = null;
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
            new Thread(this).start();
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
            return 0;
        }
    }

    private String readLine(InputStream is) throws IOException {
        byte[] result = new byte[1024];
        int c;
        int pos = 0;
        for (; ; ) {
            c = is.read();
            if (c == -1)
                break;
            result[pos++] = (byte) c;
            if (c == '\n' || c == '\r') {
                break;
            }
        }
        return new String(result, 0, pos);
    }
}

