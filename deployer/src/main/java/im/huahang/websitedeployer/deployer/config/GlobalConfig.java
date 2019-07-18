package im.huahang.websitedeployer.deployer.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

public class GlobalConfig {
    public enum Vendor {
        @SerializedName("AWS")
        AWS(1),

        @SerializedName("ALIYUN")
        ALIYUN(2);

        Vendor(int i) {
            this.value = i;
        }

        public int getValue() {
            return value;
        }

        private int value;
    }

    public static String CONFIG_FILE_NAME = ".website_deployer_global_config.json";

    public static class ConfigItem {
        @SerializedName("vendor")
        public Vendor vendor = Vendor.AWS;

        @SerializedName("region")
        public String region = "";

        @SerializedName("bucket")
        public String bucketName = "";

        @SerializedName("app_key")
        public String appKey = "";

        @SerializedName("app_secret")
        public String appSecret = "";

        public CredentialsProvider getCredentialsProvider() {
            return new CredentialsProvider();
        }

        public class CredentialsProvider implements AWSCredentialsProvider {
            @Override
            public AWSCredentials getCredentials() {
                AWSCredentials awsCredentials = new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return ConfigItem.this.appKey;
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return ConfigItem.this.appSecret;
                    }
                };
                return awsCredentials;
            }

            @Override
            public void refresh() {

            }
        }
    }

    @SerializedName("config_items")
    public Map<String, ConfigItem[]> configItems = Collections.emptyMap();

    public static GlobalConfig load(final InputStream inputStream) {
        Gson gson = new Gson();
        try {
            GlobalConfig s3Config = gson.fromJson(
                new BufferedReader(new InputStreamReader(inputStream)),
                GlobalConfig.class
            );
            return s3Config;
        } catch (Exception exception) {
            System.err.println("Failed to parse global config: " + exception.getMessage());
        }
        return null;
    }

    public static GlobalConfig load() {
        final File configFile = new File(getConfigFileName());
        if (!(configFile.exists() && configFile.isFile())) {
            return null;
        }
        try {
            return load(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(this);
        return jsonString;
    }

    public void save() throws IOException {
        final File configFile = new File(getConfigFileName());
        configFile.delete();
        Writer writer = null;
        try {
            configFile.createNewFile();
            writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(configFile)
                )
            );
            writer.write(toString());
            writer.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static String getConfigFileName() {
        final String userHome = System.getProperty("user.home");
        return "" + userHome + File.separator + CONFIG_FILE_NAME;
    }
}
