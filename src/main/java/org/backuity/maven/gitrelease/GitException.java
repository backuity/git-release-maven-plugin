package org.backuity.maven.gitrelease;

import java.io.IOException;

public class GitException extends IOException {

    public GitException(String message) {
        super(message);
    }

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}
