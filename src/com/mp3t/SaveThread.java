package com.mp3t;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import com.mp3t.ui.gui.MainWindow;

public class SaveThread implements Runnable {
	
	private Song song;
	
	private String formatString;
	private String locationString;
	
	private String fileLocation;
	
	private LinkedList<String> fileLocations;
	
	private boolean replaceOriginal;
	private boolean deleteOriginal;
	private boolean removeFrames;
	private boolean overwrite;
	
	private boolean done;
	private boolean hasError;
	
	private int num;
	
	String error;
	
	public SaveThread(Song song, String formatString, String locationString, boolean replaceOriginal, boolean deleteOriginal, boolean removeFrames, boolean overwrite, LinkedList<String> fileLocations, int num) {
		this.song = song;
		this.formatString = formatString;
		this.locationString = locationString;
		
		this.fileLocations = fileLocations;
		
		this.replaceOriginal = replaceOriginal;
		this.deleteOriginal = deleteOriginal;
		this.removeFrames = removeFrames;
		this.overwrite = overwrite;
		
		this.num = num;
		
		this.done = false;
		this.hasError = false;
	}

	@Override
	public void run() {
		try {
			if (!replaceOriginal) {
				song.save(removeFrames);
			} else {
				fileLocation = buildFileLocation();
				if (!locationAlreadyUsed())
					song.save(fileLocation, !deleteOriginal, removeFrames, overwrite);
				else
					throw new Exception("A song was already saved to " + fileLocation);
			}
		} catch (Exception e) {
			hasError = true;
			error = e.getMessage();
			e.printStackTrace();
		}
		
		done = true;
	}
	
	private boolean locationAlreadyUsed() {
		Iterator<String> it = fileLocations.iterator();
		
		while (it.hasNext()) {
			if (fileLocation.equals(it.next()))
				return true;
		}
		
		return false;
	}
	
	private String buildFileLocation() throws Exception {
		
		StringBuffer fileLocationBuffer = new StringBuffer();
		fileLocationBuffer.append(locationString + System.getProperty("file.separator"));
		
		for (int i = 0; i < formatString.length(); i++) {
			if (formatString.charAt(i) == MainWindow.FORMAT_CHAR) {
				i++;
				if (i < formatString.length()) {
					switch (formatString.charAt(i)) {
						case MainWindow.TITLE_CHAR:
							if (song.hasPendingTitle())
								fileLocationBuffer.append(song.getPendingTitle());
							else if (song.hasTitle())
								fileLocationBuffer.append(song.getTitle());
							else
								throw new Exception("No title field in song " + song.getFileName());
							break;
						
						case MainWindow.ARTIST_CHAR:
							if (song.hasPendingArtist())
								fileLocationBuffer.append(song.getPendingArtist());
							else if (song.hasArtist())
								fileLocationBuffer.append(song.getArtist());
							else
								throw new Exception("No artist field in song " + song.getFileName());
							break;
						
						case MainWindow.ALBUM_CHAR:
							if (song.hasPendingAlbum())
								fileLocationBuffer.append(song.getPendingAlbum());
							else if (song.hasAlbum())
								fileLocationBuffer.append(song.getAlbum());
							else
								throw new Exception("No album field in song " + song.getFileName());
							break;
						
						case MainWindow.GENRE_CHAR:
							if (song.hasPendingGenre())
								fileLocationBuffer.append(song.getPendingGenre());
							else if (song.hasGenre())
								fileLocationBuffer.append(song.getGenre());
							else
								throw new Exception("No genre field in song " + song.getFileName());
							break;
						
						case MainWindow.YEAR_CHAR:
							if (song.hasPendingYear())
								fileLocationBuffer.append(song.getPendingYear());
							else if (song.hasYear())
								fileLocationBuffer.append(song.getYear());
							else
								throw new Exception("No year field in song " + song.getFileName());
							break;
						
						case MainWindow.COMMENT_CHAR:
							if (song.hasPendingComment())
								fileLocationBuffer.append(song.getPendingComment());
							else if (song.hasComment())
								fileLocationBuffer.append(song.getComment());
							else
								throw new Exception("No comment field in song " + song.getFileName());
							break;
							
						case MainWindow.TRACK_CHAR:
							if (song.hasPendingTrackNumber())
								fileLocationBuffer.append(song.getPendingTrackNumber());
							else if (song.hasTrackNumber())
								fileLocationBuffer.append(song.getTrackNumber());
							else
								throw new Exception("No track number field in song " + song.getFileName());
							break;
						
						default:
							throw new Exception("Unknown field type specified by " + MainWindow.FORMAT_CHAR + formatString.charAt(i));
					}
				} else {
					throw new Exception("The format string can't end with " + MainWindow.FORMAT_CHAR);
				}
			} else if (formatString.charAt(i) == MainWindow.NUM_CHAR) {
				fileLocationBuffer.append(num);
			} else if (MainWindow.isSeperator(formatString.charAt(i))) {
				File folder = new File(fileLocationBuffer.toString());
				if (!folder.exists())
					folder.mkdir();
				fileLocationBuffer.append(formatString.charAt(i));
			} else {
				fileLocationBuffer.append(formatString.charAt(i));
			}
		}
		
		return fileLocationBuffer.toString();
	}
	
	public boolean isDone() {
		return done;
	}
	
	public boolean hasError() {
		return hasError;
	}
	
	public String getError() {
		return error;
	}
	
	public String getSaveLocation() {
		return fileLocation;
	}
}
