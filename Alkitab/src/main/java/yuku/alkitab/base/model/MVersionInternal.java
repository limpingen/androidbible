package yuku.alkitab.base.model;

import yuku.alkitab.model.Version;

/**
 * Internal version, only one
 */
public class MVersionInternal extends MVersion {
	public static final int DEFAULT_ORDERING = 1;

	public static String getVersionInternalId() {
		return "internal";
	}
	public static String getVersionInternalId2() {
		return "internal2";
	}

	@Override public String getVersionId() {
		return getVersionInternalId();
	}

	@Override public String getVersionId2() {
		return getVersionInternalId2();
	}



	@Override
	public Version getVersion() {
		return VersionImpl.getInternalVersion();
	}
	@Override
	public Version getVersion2() {
		return VersionImpl.getInternalVersion2();
	}
	@Override
	public boolean getActive() {
		return true; // always active
	}

	@Override public boolean hasDataFile() {
		return true; // always has
	}
}
