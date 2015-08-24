package org.backuity.maven.gitrelease;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "first",
	  aggregator = true)
public class ReleaseFirstMojo extends AbstractMojo {

	@Parameter
	private File installWithDependencies;
	
	@Parameter(defaultValue="${project}")
	private MavenProject project;

	/**
	 * Shall we not push ?
	 */
	@Parameter(defaultValue = "false")
	private boolean skipPush;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {			
			new ReleaseProcessor( getLog() ).releaseFirst( project, installWithDependencies, skipPush );
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}