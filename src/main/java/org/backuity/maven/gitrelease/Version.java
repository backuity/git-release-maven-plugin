package org.backuity.maven.gitrelease;

import org.apache.commons.lang3.StringUtils;

/**
 * A version is a string that follow this pattern : major.minor.bugFix[-type]
 * Immutable. 
 */
public class Version {
	
	public static final String SNAPSHOT = "SNAPSHOT";
	private int major, minor, bugFix;
	private String type;
	
	public Version( String version ) {
		String rest = version;
		try {
			major = Integer.parseInt( StringUtils.substringBefore( rest, "." ) );
			rest = StringUtils.substringAfter( version, "." );
			
			minor = Integer.parseInt( StringUtils.substringBefore( rest, "." ) );
			rest = StringUtils.substringAfter( rest, "." );
			
			if( rest.contains( "-" ) ) {
				bugFix = Integer.parseInt( StringUtils.substringBefore( rest, "-" ));
				type = StringUtils.substringAfter(rest, "-" );				
			} else {
				bugFix = Integer.parseInt( rest );
			}						
		} catch( Throwable t ) {
			throw new IllegalArgumentException( "Version " + version + " has incorrect " +
					"major.minor.bugFix structure.");
		}
	}

    public Version(int major, int minor, int bugFix) {
        this(major, minor, bugFix, null);
    }

	public Version(int major, int minor, int bugFix, String type) {
		this.major = major;
		this.minor = minor;
		this.bugFix = bugFix;
		this.type = type;
	}

	public int getMajor() {
		return major;
	}
	
	public Version increaseMajor() {
		return new Version( major + 1, minor, bugFix, type );
	}

	public Version increaseMinor() {
		return new Version(major, minor + 1, bugFix, type );
	}
	
	public Version increaseBugFix() {
		return new Version(major, minor, bugFix + 1, type );
	}
	
	public Version resetMinor() {
		return new Version(major, 0, bugFix, type );
	}
	
	public Version resetBugFix() {
		return new Version(major, minor, 0, type );
	}

	
	public int getMinor() {
		return minor;
	}
	
	public int getBugFix() {
		return bugFix;
	}
	
	public String getType() {
		return type;
	}
	
	/**
	 * <ul>
	 * <li>1.3.0 is greater than 1.2.1-SNAPSHOT</li>
	 * <li>2.3.0 is greater than 1.8.9</li>
	 * <li>1.0.0 is greater than 1.0.0-SNAPSHOT</li>
	 * <li>1.0.2 is not greater than 1.0.19</li>
	 * <li>1.0.0 is not greater than 1.0.0</li>
	 * </ul> 
	 * @return true if this version is greater than other.
	 */
	public boolean isGreater( Version other ) {		
		if( this.major != other.major) {
			return this.major > other.major;
		}

		if( this.minor != other.minor) {
			return this.minor > other.minor;
		}
		
		if( this.bugFix != other.bugFix) {
			return this.bugFix > other.bugFix;
		}
		
		return ! this.isSnapshot() && other.isSnapshot();
	}
	
	public Version noType() {
		if( type == null ) {
			return this;
		} else {
			return new Version(major, minor, bugFix, null );
		}
	}
	
	public Version snapshot() {
		if( isSnapshot() ) {
			return this;
		} else {
			return new Version(major, minor, bugFix, SNAPSHOT );
		}
	}
	
	public boolean isSnapshot() {
		return SNAPSHOT.equals( type );
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + bugFix;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		if (bugFix != other.bugFix)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	public String toString() {
		String version = "" + major + "." + minor + "." + bugFix;
		if( type != null ) {
			return version + "-" + type;
		} else {
			return version;
		}
	}
}
