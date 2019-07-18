package im.huahang.websitedeployer.deployer.upload;

import java.io.File;

public interface Uploader {
    void uploadFile(final File file, final String objectName);
}
