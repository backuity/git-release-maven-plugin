package org.backuity.maven.gitrelease;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class VersionTest {
	@Test
	public void versionShouldExtractMajorMinorBugFix() {
		Version version = new Version( "1.2.34" );
		assertThat(version.getMajor()).isEqualTo(1);
		assertThat(version.getMinor()).isEqualTo(2);
		assertThat(version.getBugFix()).isEqualTo(34);
		assertThat(version.getType()).isNull();
	}
	
	@Test
	public void versionShouldExtractMajorMinorBugFixType() {
		Version version = new Version( "1.2.34-SNAPSHOT" );
		assertThat(version.getMajor()).isEqualTo(1);
		assertThat(version.getMinor()).isEqualTo(2);
		assertThat(version.getBugFix()).isEqualTo(34);
		assertThat(version.getType()).isEqualTo("SNAPSHOT");
		assertThat(version.isSnapshot()).isTrue();
	}
	
	@Test
	public void testIsGreater() {
		assertTrue(isGreater("1.3.0", "1.2.1-SNAPSHOT"));
		assertTrue( isGreater( "2.3.0", "1.8.9" ) );
		assertTrue( isGreater( "1.0.0", "1.0.0-SNAPSHOT" ) );
		assertFalse( isGreater( "1.0.2", "1.0.19" ) );
		assertFalse( isGreater( "1.0.0", "1.0.0" ) );
	}

	private boolean isGreater(String v1, String v2) {
		return new Version( v1 ).isGreater( new Version( v2 ));
	}
}
