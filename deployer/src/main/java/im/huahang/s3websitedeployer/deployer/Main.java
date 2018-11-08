package im.huahang.s3websitedeployer.deployer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

public class Main {
    public static void main(final String[] args) throws IOException {
        /* parse input */
        if (ArrayUtils.isEmpty(args)) {
            System.err.println("s3_website_deployer [directory to upload]");
            System.exit(1);
            return;
        }
        File directory = new File(args[0]);
        if (!directory.isDirectory()) {
            System.err.println("" + args[0] + " is not a directory");
            System.exit(1);
            return;
        }
        /* parse config */
        S3Config s3Config = S3Config.load();
        if (s3Config == null) {
            S3Config.ConfigItem configItem = new S3Config.ConfigItem();
            System.console().writer().println("No active S3 config found. Will create one.");
            System.console().writer().print("Please enter an AWS region: ");
            System.console().writer().flush();
            configItem.region = System.console().readLine();
            System.console().writer().print("Please enter an S3 bucket name: ");
            System.console().writer().flush();
            configItem.bucketName = System.console().readLine();
            System.console().writer().print("Please enter an app key: ");
            System.console().writer().flush();
            configItem.appKey = new String(System.console().readPassword());
            System.console().writer().print("Please enter the app secret: ");
            System.console().writer().flush();
            configItem.appSecret = new String(System.console().readPassword());
            s3Config = new S3Config();
            s3Config.activeConfig = "default";
            s3Config.configItems = new TreeMap<String, S3Config.ConfigItem>();
            s3Config.configItems.put(s3Config.activeConfig, configItem);
            s3Config.save();
        }
        if (!s3Config.configItems.containsKey(s3Config.activeConfig)) {
            System.err.println("Could not find an active config. Will exit.");
            System.exit(1);
        }
        final S3Config.ConfigItem configItem = s3Config.configItems.get(s3Config.activeConfig);
        /* prepare client */
        AmazonS3ClientBuilder s3ClientBuilder = AmazonS3ClientBuilder.standard();
        s3ClientBuilder.setRegion(configItem.region);
        s3ClientBuilder.setCredentials(configItem.getCredentialsProvider());
        AmazonS3 s3 = s3ClientBuilder.build();
        /* upload files */
        try {
            S3Utils.uploadDirectory(s3, configItem.bucketName, directory);
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
}
