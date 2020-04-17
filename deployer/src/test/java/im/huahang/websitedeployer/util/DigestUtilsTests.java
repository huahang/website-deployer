package im.huahang.websitedeployer.util;

import im.huahang.websitedeployer.deployer.util.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

@RunWith(JUnit4.class)
public class DigestUtilsTests {
    @Test
    public void testMD5() throws IOException, NoSuchAlgorithmException {
        Assert.assertEquals(
            "8c4063a28a414176dc91859d0842d7d4",
            md5("root_config.json")
        );
    }

    @Test
    public void testMD5_31M() throws IOException, NoSuchAlgorithmException {
        Assert.assertEquals(
            "1aeccb12f5492286f2b24732048cc2c8",
            md5("31M")
        );
    }

    @Test
    public void testMD5_32M() throws IOException, NoSuchAlgorithmException {
        Assert.assertEquals(
            "12780970a6c904f03bcb5ccc9f28bfc9",
            md5("32M")
        );
    }

    @Test
    public void testMD5_33M() throws IOException, NoSuchAlgorithmException {
        Assert.assertEquals(
            "f6a7ca3e094c4eeaec6c77518f6a5fdc",
            md5("33M")
        );
    }

    private static String md5(String resource) throws IOException, NoSuchAlgorithmException {
        InputStream is = DigestUtilsTests.class
            .getClassLoader()
            .getResourceAsStream(resource);
        return DigestUtils.md5String(is);
    }
}
