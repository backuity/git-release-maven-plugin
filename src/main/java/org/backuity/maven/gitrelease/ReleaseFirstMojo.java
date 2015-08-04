package org.backuity.maven.gitrelease;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

@Mojo(name = "first")
public class ReleaseFirstMojo extends AbstractMojo {

	/** @parameter */
	private File installWithDependencies;
	
	/** @parameter expression="${project}" */
	private MavenProject project;

	/**
	 * Shall we not push ?
	 * @parameter expression="false"
	 */
	private boolean skipPush;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {			
			new ReleaseProcessor( getLog() ).releaseFirst( project, installWithDependencies, skipPush );
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}