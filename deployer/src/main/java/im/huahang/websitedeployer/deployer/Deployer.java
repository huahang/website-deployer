package im.huahang.websitedeployer.deployer;

import im.huahang.websitedeployer.deployer.upload.Uploader;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Deployer {
    public Deployer(final List<Uploader> uploaderList, final Set<String> blacklist) {
        this.uploaderList = uploaderList;
        this.blacklist.addAll(blacklist);
    }

    public void uploadDirectory(
        final File root,
        final File directory
    ) {
        String rootPath = root.getAbsolutePath();
        String directoryPath = directory.getAbsolutePath();
        String destination = directoryPath.replaceFirst(rootPath, "");
        if (destination.startsWith("/")) {
            destination = destination.substring(1);
        }
        uploadDirectory(directory, destination);
    }

    public void uploadDirectory(
        final File directory,
        final String destination
    ) {
        String prefix = destination.endsWith("/") ? destination : destination + "/";
        prefix = StringUtils.isBlank(destination) ? "" : prefix;
        File[] files = directory.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                uploadDirectory(f, prefix + f.getName());
                continue;
            }
            if (f.isFile() && !blacklist.contains(f.getName())) {
                uploadFile(f, prefix + f.getName());
            }
        }
    }

    public void uploadFile(
        final File file,
        final String objectName
    ) {
        for (Uploader uploader : uploaderList) {
            uploader.uploadFile(file, objectName);
        }
    }


    List<Uploader> uploaderList;

    private Set<String> blacklist = new TreeSet<>();
}
