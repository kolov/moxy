package moxy;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test
 */

public class HttpHelperTest {



    @Test
    public void testMatch() {
        Assert.assertEquals("x", HttpHelper.getUrl("GET x HTTP/1"));
        Assert.assertEquals("/", HttpHelper.getUrl("GET / HTTP"));
        Assert.assertEquals("/a", HttpHelper.getUrl("GET /a HTTP1.1"));
        Assert.assertEquals("/b", HttpHelper.getUrl("GET /b HTTP1.1\r"));
        Assert.assertEquals("/sad?a=4&b=5+%23\\sad&12=-^rd",
                HttpHelper.getUrl("GET /sad?a=4&b=5+%23\\sad&12=-^rd HTTP"));

    }
}
