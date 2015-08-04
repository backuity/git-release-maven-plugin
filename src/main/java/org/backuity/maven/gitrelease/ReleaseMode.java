package org.backuity.maven.gitrelease;

public enum ReleaseMode {
	MAJOR {
		@Override
		public Version releaseVersion(Version version) {
			return version.increaseMajor().resetMinor().resetBugFix();
		}
	}, 
	
	MINOR {
		@Override
		public Version releaseVersion(Version version) {
			return version.increaseMinor().resetBugFix();
		}
	}, 
	
	BUGFIX {
		@Override
		public Version releaseVersion(Version version) {
			return version;
		}
	};

	public abstract Version releaseVersion(Version version);
}
