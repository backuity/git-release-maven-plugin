package org.backuity.maven.gitrelease;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Rollback a release (that could have failed in the middle of the process)
 */
@Mojo(name="rollback")
public class ReleaseRollbackMojo extends AbstractMojo {
	
	@Parameter(defaultValue="${project}")
	private MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {			
			new ReleaseProcessor( getLog() ).rollback( project );
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
