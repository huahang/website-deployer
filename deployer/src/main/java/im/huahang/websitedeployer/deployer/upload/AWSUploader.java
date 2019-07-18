package im.huahang.websitedeployer.deployer.upload;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import im.huahang.websitedeployer.deployer.util.ExtentionMimeTypeMap;
import im.huahang.websitedeployer.deployer.util.FileNameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class AWSUploader implements Uploader {
    public AWSUploader(
        final String endpoint,
        final String key,
        final String secret,
        final String bucketName
    ) {
        AmazonS3ClientBuilder s3ClientBuilder = AmazonS3ClientBuilder.standard();
        s3ClientBuilder.setRegion(endpoint);
        s3ClientBuilder.setCredentials(
            new AWSCredentialsProvider() {
                @Override
                public AWSCredentials getCredentials() {
                    AWSCredentials credentials = new AWSCredentials() {
                        @Override
                        public String getAWSAccessKeyId() {
                            return key;
                        }
                        @Override
                        public String getAWSSecretKey() {
                            return secret;
                        }
                    };
                    return credentials;
                }
                @Override
                public void refresh() {}
            }
        );
        s3 = s3ClientBuilder.build();
        this.bucketName = bucketName;
    }

    @Override
    public void uploadFile(File file, String objectName) {
        final String extension = StringUtils.trimToEmpty(FileNameUtils.getExtensionFromFilename(file.getName()));
        final String mime = ExtentionMimeTypeMap.getMimeType(extension.toLowerCase());
        PutObjectRequest request = new PutObjectRequest(bucketName, objectName, file);
        request.setCannedAcl(CannedAccessControlList.PublicRead);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(mime);
        metadata.setCacheControl("60");
        request.setMetadata(metadata);
        s3.putObject(request);
        System.out.println("AWS upload success: /" + objectName);
    }

    private AmazonS3 s3;

    private String bucketName;
}
