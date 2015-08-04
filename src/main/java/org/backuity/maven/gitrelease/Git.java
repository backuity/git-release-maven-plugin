package org.backuity.maven.gitrelease;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

public class Git {

    private static final String NOTHING_TO_COMMIT = "nothing to commit (working directory clean)";

    /**
     * git commit -a -m commitMessage
     */
    public static void stageAndCommit( String commitMessage ) throws GitException {
        execute( "commit", "-a", "-m", commitMessage );
    }

    /**
     * @return true if there's nothing to commit
     */
    public static boolean isNothingToCommit() throws GitException {
        return executeToString( "status" ).contains( NOTHING_TO_COMMIT );
    }

    /**
     * @return true if the working tree is dirty i.e if there are untracked files
     * or changes or stuff in the index.
     */
    public static boolean isWorkingTreeDirty() throws GitException {
        return ! isNothingToCommit();
    }

    /**
     * Same as {@link #isNothingToCommit()}
     */
    public static boolean isWorkingTreeClean() throws GitException {
        return isNothingToCommit();
    }

    public static String getLastCommit() throws GitException {
        return executeToString( "log", "-1", "--pretty=format:%H" );
    }

    /**
     * @return true if the current directory is a git repository
     */
    public static boolean isRepository() throws GitException {
        return ! executeToString( "log", "-1" ).contains( "Not a git repository" );
    }

    public static void push() throws GitException {
        try {
            execute( "push" );
        } catch( GitException e ) {
            // yeah it sucks, git push outputs *everything* to stderr
            // so we have to check if it's really an error
            if( e.getMessage().contains( "error: failed to push" ) ||
                    e.getMessage().contains( "fatal: No destination configured to push to." )) {
                throw e;
            }
        }
    }

    // Reset
    // ------------------------------------------------------------------

    public static void resetHardToPrevious() throws GitException {
        execute( "reset", "--hard", "HEAD^");
    }

    /**
     * @param nbCommits must be > 0
     * @throws IllegalArgumentException if nbCommits <= 0
     */
    public static void resetHardTo( int nbCommits ) throws GitException {
        if( nbCommits <= 0 ) {
            throw new IllegalArgumentException( "nbCommits must be < 0" );
        }
        execute( "reset", "--hard", "HEAD~" + nbCommits );
    }

    // Tags
    // ------------------------------------------------------------------

    private static final String TAG_REF = "refs/tags/";

    public static void tag( String tag ) throws GitException {
        execute( "tag", tag );
    }

    public static void forceTag( String tag ) throws GitException {
        execute( "tag", "-f", tag );
    }

    public static void deleteTag( String tag ) throws GitException {
        execute( "tag", "-d", tag );
    }

    /**
     * @return null if no tag can be found
     */
    public static String getLastTag() throws GitException {
        String tag = executeToString( "git", "log", "--tags", "--simplify-by-decoration",
                "--pretty=\"format:%d\"", "-1" );
        tag = tag.trim();
        // should return something like
        // (refs/tags/1.1.2)
        String prefix = "(" + TAG_REF;

        if( tag.startsWith( prefix ) ) {
            return StringUtils.removeEnd( StringUtils.removeStart(tag, prefix ), ")" );
        }

        return null;
    }

    /**
     * Execute a Git command and return its output stream if everything went fine.
     * @return the Git command output stream (see {@link Process#getInputStream()}
     * @throws GitException if the command fails or prints things on the error output.
     */
    private static InputStream execute( String ... args ) throws GitException {
        String[] command = new String[args.length + 1];
        command[ 0 ] = "git";
        // append the git parameters
        System.arraycopy(args, 0, command, 1, args.length);

        try {
            Process child = Runtime.getRuntime().exec( command );

            String errors = IOUtils.toString(child.getErrorStream());
            if( errors.length() > 0 ) {
                throw new GitException( errors );
            }
            return child.getInputStream();
        } catch( IOException e ) {
            throw new GitException( "Can't execute git command " +
                    StringUtils.join(command, " ") +
                    ": " + e.getMessage(), e );
        }
    }

    /**
     * Execute a Git command and parses the output as a String.
     * @return the Git command output
     * @throws GitException if the command fails or prints things on the error output.
     */
    private static String executeToString(String ... args) throws GitException {
        try {
            return IOUtils.toString( execute( args ) );
        } catch (IOException e) {
            throw new GitException( "Can't execute git command " +
                    StringUtils.join( args, " " ) + ": " + e.getMessage(), e );
        }
    }
}