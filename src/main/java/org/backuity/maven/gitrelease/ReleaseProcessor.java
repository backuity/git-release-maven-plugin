package org.backuity.maven.gitrelease;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.cli.CommandLineException;


public class ReleaseProcessor {
	private Log log;
	
	private static final Version FIRST_VERSION = new Version( 1, 0, 0 );
	
	public ReleaseProcessor( Log log ) {
		this.log = log;
	}
	
	/**
	 * @param installWithDependencies if not null assemble with dependencies and copy to installWithDependencies
	 */
	public void release( ReleaseMode mode,
						 MavenProject project,
						 File installWithDependencies,
						 boolean runTests,
						 boolean skipPush ) throws Exception {
		checkNoSnapshotDependency(project);		
		Version currentVersion = new Version( project.getVersion() );		
		
		Version releaseVersion;
		if( currentVersion.isSnapshot() ) {
			releaseVersion = mode.releaseVersion( currentVersion ).noType();	
		} else {			
			releaseVersion = currentVersion;
		}
				
		Version postReleaseVersion = releaseVersion.increaseBugFix().snapshot();
		
		release( project, currentVersion, releaseVersion, postReleaseVersion, installWithDependencies, runTests, skipPush );
	}


	/**
	 * Release the first version of the software. Set release version to 1.0.0 and 
	 * post-release version to 1.0.1-SNAPSHOT
	 * @param installWithDependencies if not null assemble with dependencies and copy to installWithDependencies
	 */
	public void releaseFirst(MavenProject project, File installWithDependencies, boolean runTests, boolean skipPush ) throws Exception {
		checkNoSnapshotDependency(project);
		
		Version currentVersion = null;
		try {
			currentVersion = new Version( project.getVersion() );
		} catch( Exception e ) {
			// fine - it can be a different format, say 1.0-SNAPSHOT, or 1.0 ...)
		}
			
		if( currentVersion != null && currentVersion.isGreater( FIRST_VERSION ) ) {
			throw new Exception( "It seems that the project has already " +
					"gone through a first release. You can't release 'first' more than once." );
		}
		
		release( project, currentVersion,
				FIRST_VERSION, 
				new Version( "1.0.1-SNAPSHOT" ),
				installWithDependencies,
				runTests,
				skipPush );
	}
	
	/**
	 * @param currentVersion can be null
	 */
	private void release( MavenProject project,
						  Version currentVersion,
						  Version releaseVersion,
						  Version postReleaseVersion,
						  File installWithDependencies,
						  boolean runTests,
						  boolean skipPush ) throws Exception {

		checkRepository();
		checkWorkingTree();
		
		// we push twice as we want to catch push errors early
		// (the second push wont be really costly anyway)
		push( skipPush );
		
		if( ! releaseVersion.equals( currentVersion ) ) {
			updateVersion( releaseVersion, project.getFile() );
			
			try {
				Git.tag( releaseVersion.toString() );
			} catch( Exception e ) {
				log.info( "Undo version update" );
				resetToPrevious();
				throw new Exception( "Release failed, can't tag git : " + 
						e.getMessage(), e );
			}
			
			try {
				deploy( project.getFile(), runTests );
			} catch( Exception e ) {
				rollbackRelease(releaseVersion);
				throw new Exception( "Release failed, can't deploy : " + 
						e.getMessage(), e );
			}
		} else {
			log.info( "Resuming release " + currentVersion );
		}
				
		try {
			installWithDependencies( project, releaseVersion, installWithDependencies);
			
			updateVersion( postReleaseVersion, project.getFile() );
			install( project.getFile(), false ); // don't run the tests as we haven't changed anything in the code
			                                     // and we've already run the tests above
			push( skipPush );
		} catch( Exception e ) {
			// At that point we've deploy the app so we don't really want to rollback
			// (as far as I know maven doesn't have an undeploy plugin).
			// 
			// The problem might be fixed without changing the project. If the problem needs
			// changing the project, then the user will have to rollback and manually remove
			// the deployed artifacts.
			throw new Exception( e.getMessage() + 
					" Fix the problem and re-run git-release. " +
					"If you need to change the project then consider using rollback " +
					"(and manually remove the deployed artifacts)."
					, e );
		}
	}


	/**
	 * @throws Exception if git push fails
	 */
	private void push( boolean skipPush ) throws Exception {
		if( ! skipPush ) {
			try {
				Git.push();
			} catch( GitException e ) {
				throw new Exception( "Can't push : " + e.getMessage() + ".", e );
			}
		}
	}

	/**
	 * This action shouldn't be needed as the release process takes care of rolling back whenever possible.
	 * In some cases though, the automatic rollback won't work (e.g when the artifact has already been deployed).
	 */
	public void rollback(MavenProject project) throws Exception {
		checkRepository();
		checkWorkingTree();
		
		Version version = new Version( project.getVersion() );
		if( version.isSnapshot() ) {
			rollbackSnapshot( version, project.getFile() );
		} else {
			rollbackRelease( version );
		}		
	}
	
	/**
	 * The release process broke in the middle (say during deploy), we want to remove 
	 * the tag and reset git to a previous state.
	 */
	private void rollbackRelease(Version version) throws Exception {
		try {
			log.info("Rolling back " + version + " - if the artifact has already been deployed you'll " +
					"need to manually remove it from the remote repository.");
			try {
				Git.deleteTag(version.toString());
				log.info( "Deleted tag " + version );
			} catch( GitException e ) {
				log.error( "Couldn't delete tag " + version, e );
			}
			resetToPrevious();
		} catch( Exception e ) {
			throw new Exception( "Rolling back " + version + 
					" failed : " + e.getMessage(), e );
		}
	}

	private void resetToPrevious() throws Exception {
		try {
			Git.resetHardToPrevious();
			// TODO
			//log.info( "Reset to commit " + gitCommit.getCurrent() );
		} catch( GitException e ) {
			throw new Exception( "Couldn't reset to previous, you'll have " +
					"to do it manually : " + e.getMessage(), e );			
		}
	}

	/**
	 * Undo 2 actions (2 commits) : releasing the version and moving to snapshot, and remove the eventual tag.</br>
	 * Ex: version is 1.2.1-SNAPSHOT -> release major -> 1.3.0 (git tag + commit) -> 1.3.1-SNAPSHOT (git commit)
	 * -> rollback (2 commits + 1 tag) -> 1.2.1-SNAPSHOT.
	 */
	private void rollbackSnapshot(Version version, File pomFile ) throws Exception {
		try {
			Git.resetHardTo(2);
		} catch( GitException e ) {
			throw new Exception( "Couldn't rollback snapshot " +
					version + " : " + e.getMessage(), e );			
		} finally {
			// try to remove the last tag before failing
			// note: that method might throw an exception, which might hide the eventual 
			// rollback error but that's fine
			//
			// Retrieve the current version (in case the git reset was successful, the version
			// has now changed).
			Version currentVersion = new PomEditor(pomFile).getVersion();
			
			removeLastTagIfGreaterThan( currentVersion );	
		}		
	}

	private void removeLastTagIfGreaterThan(Version version) throws GitException {
		String lastTag = Git.getLastTag();
		if( lastTag != null ) {
			Version versionTag = new Version( lastTag );
			if( versionTag.isGreater( version ) ) {
				log.info( "Removing tag " + lastTag );
				Git.deleteTag(lastTag);
			}
		}
	}

	private void installWithDependencies(MavenProject project, Version version,
										 File installWithDependencies) throws Exception {
		if( installWithDependencies != null ) {
			try {
				assembleWithDependencies( project.getFile() );
				String jarFileName = project.getArtifactId() + "-" + version + "-jar-with-dependencies.jar";
				File outputDir = new File( project.getBuild().getDirectory() );
				File jarFile = new File( outputDir, jarFileName );
				FileUtils.copyFile(jarFile, installWithDependencies);
			} catch( Exception e ) {
				throw new Exception( "Installing the application to " + installWithDependencies + 
				" failed (" + e.getMessage() + ").", e );
			}
		}
	}

	private void checkNoSnapshotDependency( MavenProject project ) {
		for( Object obj : project.getDependencies() ) {
			Dependency dep = (Dependency)obj;
			if( dep.getVersion().endsWith( Version.SNAPSHOT ) ) {
				throw new IllegalStateException( "Dependency " + 
						dep.getGroupId() + ":" + dep.getArtifactId() +
						" is a SNAPSHOT. You can't release an artifact " +
						"depending on snapshots." );
			}
		}
	}
	
	private void checkSnapshot(Version version) {
		if( ! version.isSnapshot() ) {
			throw new IllegalStateException( "The current version isn't a snapshot !" );
		}
	}

	/**
	 * Make sure we're in a git repository
	 */
	private void checkRepository() throws GitException {
		if( ! Git.isRepository() ) {
			throw new IllegalStateException( "The current directory is not a git repository !" );
		}
	}

	private void checkWorkingTree() throws GitException {
		if( Git.isWorkingTreeDirty() ) {
			throw new IllegalStateException( "Working tree dirty." );
		}
	}

	private void updateVersion(Version version, File pomFile) throws IOException, CommandLineException, MavenInvocationException {
		executeGoals(pomFile, "versions:set -DnewVersion=" + version + " -DgenerateBackupPoms=false");
		Git.stageAndCommit("updated version to " + version);
	}
	
	private void assembleWithDependencies(File pomFile) throws CommandLineException, MavenInvocationException {
		executeGoals(pomFile, "assembly:assembly -DdescriptorId=jar-with-dependencies");
	}

	private void install(File pomFile, boolean runTests) throws MavenInvocationException, CommandLineException {
		executeGoals( pomFile, skipTestsIfNeeded("install", runTests) );
	}

	private void deploy(File pomFile, boolean runTests) throws MavenInvocationException, CommandLineException {
		executeGoals( pomFile, "clean", skipTestsIfNeeded("deploy", runTests) );
	}

	private String skipTestsIfNeeded(String goal, boolean runTests) {
		if( !runTests ) {
			goal += " -Dmaven.test.skip=true";
		}
		return goal;
	}
	
	private void executeGoals( File pomFile, String ... goals ) throws CommandLineException, MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile( pomFile );
		request.setGoals( Arrays.asList( goals ) );

		Invoker invoker = new DefaultInvoker();
		InvocationResult result = invoker.execute( request );
		if( result.getExecutionException() != null ) {
			throw result.getExecutionException();
		}
		if( result.getExitCode() != 0 ) {
			throw new CommandLineException( "mvn " + StringUtils.join( goals, " " ) + " failed." );
		}
	}
}
