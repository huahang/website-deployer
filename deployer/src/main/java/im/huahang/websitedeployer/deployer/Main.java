package im.huahang.websitedeployer.deployer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import im.huahang.websitedeployer.deployer.config.GlobalConfig;
import im.huahang.websitedeployer.deployer.config.RootConfig;
import im.huahang.websitedeployer.deployer.upload.AWSUploader;
import im.huahang.websitedeployer.deployer.upload.AliyunUploader;
import im.huahang.websitedeployer.deployer.upload.Uploader;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Main {
    public static void main(final String[] args) throws IOException {
        /* parse input */
        if (ArrayUtils.isEmpty(args)) {
            System.err.println("s3_website_deployer [directory to upload]");
            System.exit(1);
            return;
        }
        File file = new File(args[0]).getAbsoluteFile();
        if (file.getAbsolutePath().endsWith("/.")) {
            file = file.getParentFile();
        }
        if (file == null) {
            System.err.println("Invalid upload path: " + args[0]);
            System.exit(1);
            return;
        }
        /* search for s3 root */
        RootConfig rootConfig = findS3Root();
        if (null == rootConfig) {
            System.err.println("s3_root.json not found!");
            return;
        }
        /* parse config */
        GlobalConfig globalConfig = GlobalConfig.load();
        if (globalConfig == null) {
            globalConfig = new GlobalConfig();
            globalConfig.save();
        }
        if (!globalConfig.configItems.containsKey(rootConfig.bucket)) {
            System.err.println("bucket not found!");
            return;
        }
        final GlobalConfig.ConfigItem[] configItems = globalConfig.configItems.get(rootConfig.bucket);
        LinkedList<Uploader> uploaders = new LinkedList<>();
        for (GlobalConfig.ConfigItem item : configItems) {
            if (GlobalConfig.Vendor.AWS.getValue() == item.vendor.getValue()) {
                uploaders.add(
                    new AWSUploader(
                        item.region,
                        item.appKey,
                        item.appSecret,
                        item.bucketName
                    )
                );
            } else if (GlobalConfig.Vendor.ALIYUN.getValue() == item.vendor.getValue()) {
                uploaders.add(
                    new AliyunUploader(
                        item.region,
                        item.appKey,
                        item.appSecret,
                        item.bucketName
                    )
                );
            } else {
                System.err.println("Vendor not supported: " + item.vendor);
                System.exit(1);
                return;
            }
        }
        /* upload files */
        try {
            Deployer deployer = new Deployer(uploaders, rootConfig.blacklist);
            deployer.upload(new File(rootConfig.rootPath), file);
        } catch (final AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (final AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with S3, "
                + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    private static RootConfig findS3Root() {
        File cursor = Paths.get("").toAbsolutePath().toFile();
        while (true) {
            if (cursor == null || !cursor.exists()) {
                return null;
            }
            if (!cursor.isDirectory()) {
                cursor = cursor.getParentFile();
                continue;
            }
            String absolutePath = cursor.getAbsolutePath();
            RootConfig rootConfig = RootConfig.load(absolutePath + "/s3_root.json");
            if (rootConfig == null) {
                cursor = cursor.getParentFile();
                continue;
            }
            rootConfig.rootPath = absolutePath;
            return rootConfig;
        }
    }
}
