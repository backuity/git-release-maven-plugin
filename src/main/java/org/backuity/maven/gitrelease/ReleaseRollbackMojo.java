package org.backuity.maven.gitrelease;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Rollbak a release (that could have failed in the middle of the process)
 * @goal rollback
 */
public class ReleaseRollbackMojo extends AbstractMojo {
	
	/** @parameter expression="${project}" */
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {			
			new ReleaseProcessor( getLog() ).rollback( project );
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
