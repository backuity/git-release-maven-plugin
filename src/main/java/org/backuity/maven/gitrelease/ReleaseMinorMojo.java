package org.backuity.maven.gitrelease;


import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "minor",
	  aggregator = true)
public class ReleaseMinorMojo extends ReleaseMojo {
	public ReleaseMinorMojo() {
		super( ReleaseMode.MINOR);
	}
}
