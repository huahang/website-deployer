package im.huahang.websitedeployer.deployer.upload;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.HeadObjectRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import im.huahang.websitedeployer.deployer.util.DigestUtils;
import im.huahang.websitedeployer.deployer.util.ExtentionMimeTypeMap;
import im.huahang.websitedeployer.deployer.util.FileNameUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AliyunUploader implements Uploader {
    public AliyunUploader(
        final String endpoint,
        final String key,
        final String secret,
        final String bucketName
    ) {
        this.oss = new OSSClientBuilder().build(
            endpoint, key, secret
        );
        this.bucketName = bucketName;
    }

    @Override
    public void uploadFile(File file, String objectName) {
        try {
            if (!needUpload(file, objectName)) {
                System.out.println("No need to upload: /" + objectName);
                return;
            }
            final String extension = StringUtils.trimToEmpty(FileNameUtils.getExtensionFromFilename(file.getName()));
            final String mime = ExtentionMimeTypeMap.getMimeType(extension.toLowerCase());
            PutObjectRequest request = new PutObjectRequest(bucketName, objectName, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
            metadata.setContentType(mime);
            metadata.setCacheControl("60");
            request.setMetadata(metadata);
            PutObjectResult result = oss.putObject(request);
            if (result == null) {
                System.err.println("Null result: /" + objectName);
                return;
            }
            Long clientCRC = result.getClientCRC();
            Long serverCRC = result.getServerCRC();
            if (clientCRC == null || !clientCRC.equals(serverCRC)) {
                System.err.println("Aliyun upload failed: /" + objectName);
                return;
            }
            System.out.println("Aliyun upload success: /" + objectName);
        } catch (final OSSException e) {
            System.err.println("Aliyun upload failed: /" + objectName);
            System.err.println("Error message: " + e.getMessage());
        }
    }

    public boolean needUpload(File file, String objectName) {
        try {
            ObjectMetadata metadata = oss.getObjectMetadata(bucketName, objectName);
            String b64MD5 = metadata.getContentMD5();
            if (StringUtils.isBlank(b64MD5)) {
                return true;
            }
            byte[] md5 = Base64.decodeBase64(b64MD5);
            boolean match = Arrays.equals(md5, DigestUtils.md5(new FileInputStream(file)));
            return !match;
        } catch (NoSuchAlgorithmException | IOException | OSSException ignored) {
        }
        return true;
    }

    private OSS oss;
    private String bucketName;
}
