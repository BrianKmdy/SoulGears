package com.mp3t.ui.gui;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import com.mp3t.SaveThread;
import com.mp3t.Song;

public class SaveProgressWindow {

	private Shell shell;
	
	private ProgressBar progress;
	
	private StyledText logField;
	
	private Button okCancelButton;
	
	private Button showErrorsButton;
	
	private Display display;
	
	private LinkedList<Song> songs;
	
	private LinkedList<String> fileLocations;
	
	private boolean replaceOriginal;
	
	private String outputFormat;
	private String outputLocation;
	
	private boolean deleteOriginal;
	private boolean removeFrames;
	private boolean overwrite;
	
	private int counter;
	
	private Color black;
	private Color red;
	private Color green;
	private Color header;
	private Color data;
	
	public SaveProgressWindow(Shell mainWindow, Display display, LinkedList<Song> songs, boolean replaceOriginal, String outputFormat, String outputLocation, boolean deleteOriginal, boolean removeFrames, boolean overwrite, boolean onlyErrors) {
		
		this.display = display;
		this.songs = songs;
		this.replaceOriginal = replaceOriginal;
		this.outputFormat = outputFormat;
		this.outputLocation = outputLocation;
		this.deleteOriginal = deleteOriginal;
		this.removeFrames = removeFrames;
		this.overwrite = overwrite;
		
		this.counter = 1;
		
		this.fileLocations = new LinkedList<String>();
		
		shell = new Shell(mainWindow, SWT.TITLE | SWT.SYSTEM_MODAL | SWT.CLOSE);
		shell.setText("Save Progress");
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 8;
		gridLayout.marginHeight = 8;
		gridLayout.horizontalSpacing = 4;
		gridLayout.verticalSpacing = 4;
		shell.setLayout(gridLayout);
		
		scaleAndCenter();
		
		Label label = new Label(shell, SWT.NONE);
		
		label = new Label(shell, SWT.NONE);
		label.setText("Progress:");
		GridData gridData = new GridData(SWT.CENTER, SWT.END, false, false);
		label.setLayoutData(gridData);
		
		progress = new ProgressBar(shell, SWT.NONE);
		progress.setMaximum(songs.size());
		progress.setMinimum(0);
		
		gridData = new GridData(SWT.CENTER, SWT.FILL, false, false);
		gridData.heightHint = 26;
		gridData.widthHint = Math.round(((float) (shell.getSize().x)) * 0.88f); 
		progress.setLayoutData(gridData);
		
		//progress.setText("0 / " + songs.size() + " Songs saved");
		
		label = new Label(shell, SWT.NONE);
		
		label = new Label(shell, SWT.NONE);
		label.setText("Log:");
		gridData = new GridData(SWT.BEGINNING, SWT.END, false, false);
		label.setLayoutData(gridData);
		
		logField = new StyledText(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		logField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		logField.setEditable(false);
		
		showErrorsButton = new Button(shell, SWT.CHECK);
		showErrorsButton.setSelection(onlyErrors);
		showErrorsButton.setText("Only show errors");
		gridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		showErrorsButton.setLayoutData(gridData);
		
		label = new Label(shell, SWT.NONE);
		
		okCancelButton = new Button(shell, SWT.NONE);
		okCancelButton.setText("Cancel");
		gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gridData.heightHint = 32;
		gridData.widthHint = 64;
		okCancelButton.setLayoutData(gridData);
		
		label = new Label(shell, SWT.NONE);
		
		shell.layout();
		
		black = new Color(display, 0, 0, 0);
		red = new Color(display, 255, 55, 55);
		green = new Color(display, 23, 153, 27);
		header = new Color(display, 85, 85, 85);
		data = new Color(display, 100, 100, 100);
		
		progress.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				String string = progress.getSelection() + "/" + progress.getMaximum();
				Point point = progress.getSize();
				  
				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * string.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground(black);
				e.gc.drawString(string, (point.x-width)/2 , (point.y-height)/2, true);
			}
		});
		
		okCancelButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {	
				shell.dispose();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	private void scaleAndCenter() {
		final double SHELL_RATIO = 0.66;
		
		Rectangle bounds = shell.getMonitor().getBounds();
		Rectangle shellBounds = shell.getBounds();
		
		int width = (int) Math.round((double) shellBounds.width * SHELL_RATIO);
		int height = (int) Math.round((double) shellBounds.height * SHELL_RATIO);
		Rectangle saveProgressWindowBounds = new Rectangle(bounds.x + (bounds.width - width) / 2, bounds.y + (bounds.height - height) / 2, width, height);
		shell.setBounds(saveProgressWindowBounds);
	}
	
	public LinkedList<Song> open() {
		shell.open();
		
		LinkedList<Song> failedToSave = new LinkedList<Song>();
		
		String oldFileName;
		String oldTitle;
		String oldArtist;
		String oldAlbum;
		String oldGenre;
		String oldYear;
		String oldComment;
		String oldTrackNumber;
		
		for (int i = 0; i < songs.size(); i++) {
			
			Song song = songs.get(i);
			
			oldFileName    = song.getFileName();
			oldTitle       = song.getTitle();
			oldArtist      = song.getArtist();
			oldAlbum       = song.getAlbum();
			oldGenre       = song.getGenre();
			oldYear        = song.getYear();
			oldComment     = song.getComment();
			oldTrackNumber = song.getTrackNumber();

			SaveThread save = new SaveThread(song, outputFormat, outputLocation, replaceOriginal, deleteOriginal, removeFrames, overwrite, fileLocations, counter++);
			(new Thread(save)).start();

			while (!save.isDone()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			
			if (shell.isDisposed()) {
				if (!save.hasError())
					i++;
				
				for (; i < songs.size(); i++)
					failedToSave.add(songs.get(i));
				
				for (int o = 0; o < failedToSave.size(); o++)
					songs.remove(failedToSave.get(o));
			
				return songs;
			} else {
				progress.setSelection(i + 1);
				
				if (save.hasError()) {
					printError(save.getError());
					failedToSave.add(song);
				} else {
					if (!showErrorsButton.getSelection())
						printSuccess(song, oldFileName, oldTitle, oldArtist, oldAlbum, oldGenre, oldYear, oldComment, oldTrackNumber);

					fileLocations.add(save.getSaveLocation());
				}
			}
		}
		
		for (int i = 0; i < failedToSave.size(); i++)
			songs.remove(failedToSave.get(i));
		
		okCancelButton.setText("Close");
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return songs;
	}
	
	private void printError(String lineBody) {
		if (lineBody == null)
			lineBody = new String();
		
		StyleRange style;
		
		int index = logField.getCharCount();
		
		if (index > 0) {
			logField.append("\n");
			index++;
		}
		
		String lineHeader = "[Error] ";
		logField.append(lineHeader);
		
		style = new StyleRange();
		style.start = index;
		style.length = lineHeader.length();
		style.foreground = red;
		logField.setStyleRange(style);
		index += lineHeader.length();
		
		logField.append(lineBody);
		
		style = new StyleRange();
		style.start = index;
		style.length = lineBody.length();
		style.foreground = black;
		logField.setStyleRange(style);
		
		logField.setTopIndex(logField.getLineCount() - 1);
	}
	
	private void printSuccess(Song song, String oldFileName, String oldTitle, String oldArtist, String oldAlbum, String oldGenre, String oldYear, String oldComment, String oldTrackNumber) {
		StyleRange style;
		
		int index = logField.getCharCount();
		
		if (index > 0) {
			logField.append("\n");
			index++;
		}
		
		String lineHeader = "[Success] ";
		logField.append(lineHeader);
		
		style = new StyleRange();
		style.start = index;
		style.length = lineHeader.length();
		style.foreground = green;
		logField.setStyleRange(style);
		index += lineHeader.length();
		
		logField.append(oldFileName);
		
		style = new StyleRange();
		style.start = index;
		style.length = oldFileName.length();
		style.foreground = header;
		logField.setStyleRange(style);
		index += oldFileName.length();
		
		logField.append("   --->   ");
		index += 10;
		
		logField.append(song.getFileName());
		
		style = new StyleRange();
		style.start = index;
		style.length = song.getFileName().length();
		style.foreground = header;
		logField.setStyleRange(style);
		index += song.getFileName().length();
		
		// Code to print additional info on the next line. Looks tacky.
		/*
		logField.append("\n");
		index++;
		
		logField.append("     ");
		index += 5;
		
		index = printField("Title", oldTitle, index, data);
		index = printField("Artist", oldArtist, index, data);
		index = printField("Album", oldAlbum, index, data);
		index = printField("Genre", oldGenre, index, data);
		index = printField("Year", oldYear, index, data);
		index = printField("Comment", oldComment, index, data);
		index = printField("Track No", oldTrackNumber, index, data);	
		
		logField.append(" -> ");
		index += 4;
		
		index = printField("Title", song.getTitle(), index, data);
		index = printField("Artist", song.getArtist(), index, data);
		index = printField("Album", song.getAlbum(), index, data);
		index = printField("Genre", song.getGenre(), index, data);
		index = printField("Year", song.getYear(), index, data);
		index = printField("Comment", song.getComment(), index, data);
		index = printField("Track No", song.getTrackNumber(), index, data);	
		*/
		
		logField.setTopIndex(logField.getLineCount() - 1);
	}
	
	private int printField(String header, String field, int index, Color color) {
		if (!field.isEmpty()) {
			logField.append("[");
			index++;
			logField.append(header);
			index += header.length();
			logField.append(": ");
			index += 2;
			
			logField.append(field);
			
			StyleRange style = new StyleRange();
			style.start = index;
			style.length = field.length();
			style.foreground = color;
			logField.setStyleRange(style);
			index += field.length();
			
			logField.append("]");
			index++;
		}
		
		return index;
	}
	
	public boolean getShowErrors() {
		return showErrorsButton.getSelection();
	}
	
	public Shell getShell() {
		return shell;
	}
}
