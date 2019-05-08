package im.huahang.s3websitedeployer.deployer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(final String[] args) throws IOException {
        /* parse input */
        if (ArrayUtils.isEmpty(args)) {
            System.err.println("s3_website_deployer [directory to upload]");
            System.exit(1);
            return;
        }
        File directory = new File(args[0]).getAbsoluteFile();
        if (directory.getAbsolutePath().endsWith("/.")) {
            directory = directory.getParentFile();
        }
        if (directory == null || !directory.isDirectory()) {
            System.err.println("Invalid upload path: " + args[0]);
            System.exit(1);
            return;
        }
        /* search for s3 root */
        S3RootConfig rootConfig = findS3Root();
        if (null == rootConfig) {
            System.err.println("s3_root.json not found!");
            return;
        }
        /* parse config */
        S3Config s3Config = S3Config.load();
        if (s3Config == null) {
            s3Config = new S3Config();
        }
        if (!s3Config.configItems.containsKey(rootConfig.bucket)) {
            S3Config.ConfigItem configItem = new S3Config.ConfigItem();
            configItem.bucketName = rootConfig.bucket;
            System.console().writer().println(
                "No S3 config found for bucket: " + rootConfig.bucket + ", will create one."
            );
            System.console().writer().print("Please enter an AWS region: ");
            System.console().writer().flush();
            configItem.region = System.console().readLine();
            System.console().writer().print("Please enter an CloudFront distribution ID: ");
            System.console().writer().flush();
            configItem.distributionID = System.console().readLine();
            System.console().writer().print("Please enter an app key: ");
            System.console().writer().flush();
            configItem.appKey = new String(System.console().readPassword());
            System.console().writer().print("Please enter the app secret: ");
            System.console().writer().flush();
            configItem.appSecret = new String(System.console().readPassword());
            s3Config.configItems.put(rootConfig.bucket, configItem);
            s3Config.save();
        }
        final S3Config.ConfigItem configItem = s3Config.configItems.get(rootConfig.bucket);
        /* prepare client */
        AmazonS3ClientBuilder s3ClientBuilder = AmazonS3ClientBuilder.standard();
        s3ClientBuilder.setRegion(configItem.region);
        s3ClientBuilder.setCredentials(configItem.getCredentialsProvider());
        AmazonS3 s3 = s3ClientBuilder.build();
        AmazonCloudFrontClientBuilder cloudFrontClientBuilder = AmazonCloudFrontClientBuilder.standard();
        cloudFrontClientBuilder.setRegion(configItem.region);
        cloudFrontClientBuilder.setCredentials(configItem.getCredentialsProvider());
        AmazonCloudFront cloudFront = cloudFrontClientBuilder.build();
        /* upload files */
        try {
            AWSDeployer awsDeployer = new AWSDeployer(
                s3,
                configItem.bucketName,
                cloudFront,
                configItem.distributionID,
                rootConfig.blacklist
            );
            awsDeployer.uploadDirectory(new File(rootConfig.rootPath), directory);
            awsDeployer.flushCDN();
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

    private static S3RootConfig findS3Root() {
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
            S3RootConfig rootConfig = S3RootConfig.load(absolutePath + "/s3_root.json");
            if (rootConfig == null) {
                cursor = cursor.getParentFile();
                continue;
            }
            rootConfig.rootPath = absolutePath;
            return rootConfig;
        }
    }
}
