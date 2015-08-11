package com.mp3t;

public class Settings {
	
	public static final int WINDOWS = 0;
	public static final int LINUX   = 1;
	public static final int MAC     = 2;
	
	public static final int ROW_HEIGHT = 32;
	
	public static final int LIB_ENTAGGED = 0;
	public static final int LIB_MP3MAGIC = 1;
	public static final int LIB_COMBINED = 2;

	public static int OS;
	public static boolean imagesEnabled;
	public static boolean canScan;
	public static int library;
	
	public static boolean imagesEnabled() {
		return imagesEnabled;
	}
	
	public static boolean canScan() {
		return canScan;
	}
	
	public static int getLibrary() {
		return library;
	}
}
