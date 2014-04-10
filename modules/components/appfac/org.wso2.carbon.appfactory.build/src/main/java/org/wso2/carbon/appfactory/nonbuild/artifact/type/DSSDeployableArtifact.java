package org.wso2.carbon.appfactory.nonbuild.artifact.type;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.FileUtils;
import org.hsqldb.lib.FileUtil;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.nonbuild.artifact.ArtifactGeneratorFactory;
import org.wso2.carbon.appfactory.nonbuild.artifact.DeployableArtifact;

public class DSSDeployableArtifact extends DeployableArtifact {
	
	private static Log log = LogFactory.getLog(DSSDeployableArtifact.class);

	public DSSDeployableArtifact(String rootPath, String applicationId, String version, String stage) {
	    super(rootPath, applicationId, version, stage);
    }

	@Override
	public void generateDeployableFile() throws AppFactoryException {
		String dbsfilepath =
		                     getRootPath() + File.separator +
		                             ArtifactGeneratorFactory.appfactoryGitTmpFolder +
		                             File.separator + "src" + File.separator + "main" +
		                             File.separator + "dataservice" + File.separator;
		String[] fileNames = FileUtils.getFilesFromExtension(dbsfilepath, new String[] { "dbs" });

		String artifactSrcFile = fileNames[0];
		String targetFile =
		                    artifactSrcFile.substring(artifactSrcFile.lastIndexOf(File.separator) + 1,
		                                              artifactSrcFile.length() - 4);

		if (getVersion().equals("trunk")) {
			targetFile = targetFile + "-default-SNAPSHOT.dbs";
		} else {
			targetFile = targetFile + "-" + getVersion() + ".dbs";
		}

		String artifactTmpFolder = getRootPath() + File.separator +
	                                   		ArtifactGeneratorFactory.deployableAtrifactFolder +
	                                   		File.separator + targetFile;

		try {
			FileUtils.copyFile(new File(artifactSrcFile), new File(artifactTmpFolder));
		} catch (IOException e) {
			String errMsg = "Error when copying folder from src to artifact tmp : " +
			                        e.getMessage();
			log.error(errMsg, e);
			throw new AppFactoryException(errMsg, e);
		}
	}

}
