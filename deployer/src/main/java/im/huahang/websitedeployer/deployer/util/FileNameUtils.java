package im.huahang.websitedeployer.deployer.util;

public class FileNameUtils {
    static public String getExtensionFromFilename(final String filename) {
        String[] splits = filename.split("\\.");
        if (splits.length < 2) {
            return "";
        }
        return splits[splits.length - 1];
    }
}
