package im.huahang.websitedeployer.deployer.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {
    public static byte[] md5(InputStream is) throws NoSuchAlgorithmException, IOException {
        byte[] buffer = new byte[16 * 1024 * 1024];
        MessageDigest md = MessageDigest.getInstance("MD5");
        while (true) {
            int bytes = is.read(buffer);
            if (bytes < 0) {
                break;
            }
            md.update(buffer, 0, bytes);
        }
        byte[] digest = md.digest();
        return digest;
    }

    public static String md5String(InputStream is) throws IOException, NoSuchAlgorithmException {
        byte[] digest = md5(is);
        if (ArrayUtils.isEmpty(digest)) {
            return "";
        }
        return Hex.encodeHexString(digest);
    }
}
