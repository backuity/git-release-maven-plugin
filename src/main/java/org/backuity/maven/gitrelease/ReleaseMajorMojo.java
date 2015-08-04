package org.backuity.maven.gitrelease;


import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "major")
public class ReleaseMajorMojo extends ReleaseMojo {
	public ReleaseMajorMojo() {
		super( ReleaseMode.MAJOR);
	}
}
