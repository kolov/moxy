package moxy;

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
}
