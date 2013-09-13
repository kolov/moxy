package moxy;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**

 */
public class ProxyConfigurationTest {


    @Test
    public void testRead() throws Exception {
        Assert.assertEquals(1,  getEntries("a b 3").size());
        Assert.assertEquals("b",  getEntries("a b 3").get(0).destination.host);
        Assert.assertEquals(3,  getEntries("a b 3").get(0).destination.port);
        Assert.assertEquals("a",  getEntries("a b 3").get(0).regex);

        Assert.assertEquals(2,  getEntries("a b 3\nc d 4").size());
        Assert.assertEquals(2,  getEntries("a b 3\nc d 4").size());
        Assert.assertEquals(2,  getEntries("a b 3\n  \nc d 4").size());
        Assert.assertEquals(2,  getEntries("a b 3\n\nc d 4").size());
        Assert.assertEquals(2,  getEntries("a b 3\n#  \nc d 4  ").size());
        Assert.assertEquals(2,  getEntries("a b 3\n  \nc d 4\n     \n#\n").size());




    }

    private List<ProxyConfiguration.Entry> getEntries(String content) throws IOException {
        ProxyConfiguration pc = new ProxyConfiguration();
        pc.read(new ByteArrayInputStream(content.getBytes()));
        return pc.getEntries();
    }
}
