package im.huahang.s3websitedeployer;

import com.google.common.net.PercentEscaper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StringEscapeTest {
    @Test
    public void test() {
        PercentEscaper escaper = new PercentEscaper("/.-", true);
        String result = escaper.escape(
            "/files/2015-01-30-inner-mongolia-trip/内蒙行10.jpg"
        );
        Assert.assertEquals(
            "/files/2015-01-30-inner-mongolia-trip/%E5%86%85%E8%92%99%E8%A1%8C10.jpg",
            result
        );
        return;
    }
}
