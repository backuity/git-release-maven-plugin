package org.backuity.maven.gitrelease;


import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "bugfix",
 	  aggregator = true)
public class ReleaseBugFixMojo extends ReleaseMojo {
	public ReleaseBugFixMojo() {
		super( ReleaseMode.BUGFIX);
	}
}
