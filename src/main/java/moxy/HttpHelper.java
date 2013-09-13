package moxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**

 */
public class HttpHelper {

    public static String getUrl(String s) {
        Pattern p = Pattern.compile("\\w+\\s+([^\\s]+).+", Pattern.DOTALL);
        //  get a matcher object
        Matcher m = p.matcher(s);
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public static String readLine(InputStream is) throws IOException {
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
