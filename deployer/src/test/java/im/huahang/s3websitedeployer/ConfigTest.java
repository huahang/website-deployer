package im.huahang.s3websitedeployer;

import im.huahang.s3websitedeployer.deployer.S3RootConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;

@RunWith(JUnit4.class)
public class ConfigTest {
    @Test
    public void testRootConfig() throws IOException {
        try(final InputStream is = getClass().getClassLoader().getResourceAsStream("s3_root.json")) {
            S3RootConfig rootConfig = S3RootConfig.load(is);
            Assert.assertEquals("test", rootConfig.bucket);
            Assert.assertEquals(2, rootConfig.blacklist.size());
            Assert.assertTrue(rootConfig.blacklist.contains("a"));
            Assert.assertTrue(rootConfig.blacklist.contains("bcd"));
        }
    }
}
