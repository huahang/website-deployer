package im.huahang.websitedeployer.config;

import im.huahang.websitedeployer.deployer.config.GlobalConfig;
import im.huahang.websitedeployer.deployer.config.RootConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;

@RunWith(JUnit4.class)
public class ConfigTests {
    @Test
    public void testRootConfig() throws IOException {
        try(final InputStream is = getClass().getClassLoader().getResourceAsStream("root_config.json")) {
            RootConfig rootConfig = RootConfig.load(is);
            Assert.assertEquals("test", rootConfig.bucket);
            Assert.assertEquals(2, rootConfig.blacklist.size());
            Assert.assertTrue(rootConfig.blacklist.contains("a"));
            Assert.assertTrue(rootConfig.blacklist.contains("bcd"));
        }
    }

    @Test
    public void testGlobalConfig() throws IOException {
        try(final InputStream is = getClass().getClassLoader().getResourceAsStream("global_config.json")) {
            GlobalConfig globalConfig = GlobalConfig.load(is);
            Assert.assertNotNull(globalConfig);
        }
    }
}
