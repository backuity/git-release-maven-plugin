package org.backuity.maven.gitrelease;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class ReleaseMojo extends AbstractMojo {
	private ReleaseMode mode;

	@Parameter
	private File installWithDependencies;
	
	@Parameter(defaultValue = "${project}")
	private MavenProject project;
	
	/**
	 * Shall we not push ?
	 */
	@Parameter(defaultValue = "false")
	private boolean skipPush;

	/** Shall we run the tests prior to deploy */
	@Parameter(defaultValue = "false")
	private boolean runTests;

	public ReleaseMojo( ReleaseMode mode ) {
		this.mode = mode;
	}
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {			
			new ReleaseProcessor( getLog() ).release( mode, project, installWithDependencies, runTests, skipPush );
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}
