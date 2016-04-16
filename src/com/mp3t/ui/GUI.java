/**
 * ...
 * 
 * @author Brian Moody
 * @date 8/22/2012
 * @version 0.1
 */

/* TO-DO:
 * -Possibly remove this class, is it even useful or is it just a middle man for the shells?
 */


package com.mp3t.ui;

import org.eclipse.swt.widgets.*;

import com.mp3t.Core;
import com.mp3t.SongLoader;
import com.mp3t.ui.gui.MainWindow;


public class GUI implements UserInterface {

	private Display display;
	
	public GUI(Core core, SongLoader songLoader, int thumbnailWidth) {
		
		Display.setAppName(Core.PROGRAM_NAME);
		display = new Display();
		
		new MainWindow(display, core, songLoader, thumbnailWidth);
		
		display.dispose();

	}
}