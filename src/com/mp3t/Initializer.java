package com.mp3t;

import java.io.File;
import java.io.IOException;

import com.mp3t.ui.GUI;

/**
 * Class to filter through arguments and initialize program depending on the options the user selects.
 * 
 * @author Brian Moody
 * @date 8/22/2012
 * @version 0.1
 */

/* TO-DO:
 * -
 */

public class Initializer {
	
	public static final int MAX_SONGS_LOADED = 1000;
	
	private static final int THUMBNAIL_WIDTH = 48;
	
	/** Start of the program */
	public static void main(String[] args) {
		if (args.length < 2) {
			// Correct number of arguments - start the core

			/*
			if (Core.LOGGING) {
				try {
					File homeFolder = new File(Core.HOME_PATH.substring(0, Core.HOME_PATH.length() - 1));
					homeFolder.mkdir();
					
					Core.log.add(Core.LOG_FILE, Core.HOME_PATH + Core.LOG_FILE);
					Core.log.setDefaultLog(Core.LOG_FILE);
				} catch (IOException e) {
					System.err.println("Unable to open log file");
				}
			}
			*/
		
			Settings.OS = Settings.WINDOWS;
			
			File fpcalc = null;
			if (Settings.OS == Settings.WINDOWS)
				fpcalc = new File(System.getProperty("user.dir") + File.separator + "fpcalc.exe");
			else if ((Settings.OS == Settings.LINUX) || (Settings.OS == Settings.MAC))
				fpcalc = new File(System.getProperty("user.dir") + File.separator + "fpcalc");
				
			boolean fpcalcExecutable = false;
			if (fpcalc.exists() && fpcalc.canExecute())
				fpcalcExecutable = true;
			
			Settings.imagesEnabled = false;
			Settings.canScan = fpcalcExecutable;
			Settings.library = Settings.LIB_COMBINED;
			
			SongLoader songLoader = new SongLoader();
			(new Thread(songLoader)).start();
			Core core = new Core(songLoader, MAX_SONGS_LOADED, THUMBNAIL_WIDTH);
			(new Thread(core)).start();
			
			if (args.length == 1) {
				String arg = args[0].toLowerCase();
				
				if (arg.equals("-h") || arg.equals("--help")) {
					System.out.println("\n\t\t\t\t\t\t" + Core.PROGRAM_NAME + " version " + Core.PROGRAM_VERSION + " released on " + Core.PUBLICATION_DATE + "\n" +
									   "\t\t\t\t\t\t\tDeveloped by " + Core.AUTHOR_NAME + "\n" + 
							           "\tExecute program with --gui or without arguments to launch the GUI. Execute with --cli to launch the command line interface.");
				} else if (arg.equals("-g") || arg.equals("--gui"))
					new GUI(core, songLoader, THUMBNAIL_WIDTH);
				else if (arg.equals("-c") || arg.equals("--cli")) {
					System.err.println(Core.ERROR_HEADER + "Command line interface still under construction");
					System.exit(1);
				} else {
					System.err.println(Core.ERROR_HEADER + "Unknown argument");
					System.exit(1);
				}
			// If the program's started without arguments then launch the GUI
			} else {
				new GUI(core, songLoader, THUMBNAIL_WIDTH);
			}
		
		} else {
			System.err.println(Core.ERROR_HEADER + "Invalid number of arguments. Execute with --help for program instructions.");
			System.exit(1);
		}
		
		// Program executed successfully
		System.exit(0);
	}
}
