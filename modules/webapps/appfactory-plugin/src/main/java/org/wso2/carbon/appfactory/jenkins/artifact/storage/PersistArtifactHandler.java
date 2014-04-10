/*
 *
 */
package org.wso2.carbon.appfactory.jenkins.artifact.storage;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of FileCallable to copy the artifact from
 * workspace to persistent storage at jenkins side
 */
public class PersistArtifactHandler implements FileCallable<Boolean> {

    private PersistArtifactHandlerConfig artifactHandlerConfig;

    public PersistArtifactHandler(PersistArtifactHandlerConfig artifactHandlerConfig) {
        this.artifactHandlerConfig = artifactHandlerConfig;
    }

    /**
     * This method will be invoked when we pass an object of this class to build.getWorkspace().act
     * method. Here the build is an AbstractBuild
     * @param file the workspace directory of the built job
     * @param channel
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Boolean invoke(File file, VirtualChannel channel)
            throws IOException, InterruptedException {
        String[] ext = {this.artifactHandlerConfig.getApplicationArtifactExtension()};
        if (file != null) {
            File target = (File) FileUtils.listFiles(file, ext, true).iterator().next();
            //there should be only one target
            //taking the first one,ignoring rest
            if (target != null) {
                if (new File(artifactHandlerConfig.getPersistentStoragePath()).exists()) {
                    //artifact will be stored at <storage-path>/<job-name>/<tag-Name>/artifact
                    File persistArtifact = new File(artifactHandlerConfig.
                                                    getPersistentStoragePath() + File.separator +
                                                    artifactHandlerConfig.getJobName() +
                                                    File.separator +
                                                    artifactHandlerConfig.getTagName());
                    //noinspection ResultOfMethodCallIgnored
                    persistArtifact.mkdirs();
                    copyArtifact(target.getAbsolutePath(), persistArtifact.getAbsolutePath() +
                                                           File.separator + target.getName());
                }
            }
        }
        return true;
    }

    private void copyArtifact(String source, String destination) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        byte[] buffer = new byte[1024];

        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.close();
    }
}
    
