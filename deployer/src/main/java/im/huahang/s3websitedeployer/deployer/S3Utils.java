package im.huahang.s3websitedeployer.deployer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class S3Utils {
    static public void uploadDirectory(
        final AmazonS3 s3,
        final String bucketName,
        final File directory
    ) {
        uploadDirectory(s3, bucketName, directory, "");
    }

    static public void uploadDirectory(
        final AmazonS3 s3,
        final String bucketName,
        final File directory,
        final String destination
    ) {
        String prefix = destination.endsWith("/") ? destination : destination + "/";
        prefix = StringUtils.isBlank(destination) ? "" : prefix;
        File[] files = directory.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                uploadDirectory(s3, bucketName, f, prefix + f.getName());
                continue;
            }
            if (f.isFile()) {
                uploadFile(s3, bucketName, f, prefix + f.getName());
            }
        }
    }

    static public void uploadFile(
        final AmazonS3 s3,
        final String bucketName,
        final File file,
        final String objectName) {
        String extension = getExtensionFromFilename(file.getName());
        String mime = ExtentionMimeTypeMap.getMimeType(extension);
        PutObjectRequest request = new PutObjectRequest(bucketName, objectName, file);
        request.withCannedAcl(CannedAccessControlList.PublicRead);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(mime);
        metadata.setCacheControl("60");
        request.setMetadata(metadata);
        s3.putObject(request);
        System.out.println("Upload file success: " + objectName);
    }

    static private String getExtensionFromFilename(final String filename) {
        String[] splits = filename.split("\\.");
        if (splits.length < 2) {
            return "";
        }
        return splits[splits.length - 1];
    }
}
