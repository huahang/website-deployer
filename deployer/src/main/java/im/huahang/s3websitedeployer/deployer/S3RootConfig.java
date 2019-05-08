package im.huahang.s3websitedeployer.deployer;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

public class S3RootConfig {
    @SerializedName("bucket")
    public String bucket = "";

    @SerializedName("blacklist")
    public Set<String> blacklist = Collections.emptySet();

    @SerializedName("root_path")
    public String rootPath = "";

    public static S3RootConfig load(final InputStream is) {
        if (null == is) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(
            new BufferedReader(new InputStreamReader(is)),
            S3RootConfig.class
        );
    }

    public static S3RootConfig load(final File file) {
        if (null == file) {
            return null;
        }
        try(final InputStream inputStream = new FileInputStream(file)) {
            return load(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    public static S3RootConfig load(final String filename) {
        final File configFile = new File(filename);
        if (!(configFile.exists() && configFile.isFile())) {
            return null;
        }
        return load(configFile);
    }
}
