package com.mp3t;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class SongLoader implements Runnable {

	private LinkedList<File> files;
	private LinkedList<Object[]> locations;
	
	int numSongs;
	
	public SongLoader() {
		files = new LinkedList<File>();
		locations = new LinkedList<Object[]>();
		numSongs = 0;
	}
	
	public void addLocation(String location, boolean recursive) {
		addLocation(new File(location), recursive);
	}
	
	public void addLocation(File location, boolean recursive) {
		Object[] entry = new Object[2];
		
		entry[0] = location;
		entry[1] = recursive;
		
		synchronized (locations) {
			locations.add(entry);
		}
		
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	public void addLocations(File locations[], boolean recursive) {
		for (int i = 0; i < locations.length; i++) 
			addLocation(locations[i], recursive);
	}
	
	public void addLocations(String locations[], boolean recursive) {
		for (int i = 0; i < locations.length; i++) 
			addLocation(new File(locations[i]), recursive);
	}
	
	public int getNumSongs() {
		return numSongs;
	}
	
	public boolean hasFiles() {
		if (files.size() > 0) 
			return true;
		else 
			return false;
	}
	
	public int numFiles() {
		return files.size();
	}
	
	public LinkedList<File> getList() {
		return files;
	}
	
	public void clear() {
		synchronized (locations) {
			locations.clear();
		}
		
		synchronized (files) {
			files.clear();
		}
		
		numSongs = 0;
	}
	
	/* Load the .mp3 files in a folder into the queued song file list */
	public void loadFolder(File folder, boolean recursive) throws IOException {
		
		if (!folder.exists())
			throw new IOException(new String("File " + folder.getAbsolutePath() + " does not exist"));
		
		if (!folder.isDirectory())
			throw new IOException(new String("File " + folder.getAbsolutePath() + " is not a directory"));
		
		File[] files = folder.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				if (recursive)
					this.loadFolder(files[i], true);
			} else {
				loadFile(files[i], false);
			}
		}
	}
	
	public void loadFile(String fileName, boolean recursive) throws IOException {
		loadFile(new File(fileName), recursive);
	}
	
	public void loadFile(File file, boolean recursive) throws IOException {
		
		if (file.isDirectory())
			loadFolder(file, recursive);
		
		if (!file.exists())
			throw new IOException(new String("File " + file.getAbsolutePath() + " does not exist"));
		
		if (Song.isValidExt(file)) {
			synchronized (files) {
				files.add(file);
				numSongs++;
			}
		}
	}

	public void run() {
		while (true) {
			synchronized (locations) {
				if (locations.size() > 0) {
					Iterator<Object[]> it = locations.iterator();
					
					while (it.hasNext()) {
						Object[] location = it.next();
						
						try {
							loadFile((File) location[0], (Boolean) location[1]);
						} catch (IOException e) {
							if (Core.DEBUG) 
								System.out.println(Core.DEBUG_HEADER + "Error loading file " + location[0].toString());
						}
						
						it.remove();
					}
					
					synchronized (this) {
						this.notifyAll();
					}
				}
			}
			
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					//
				}
			}
		}
	}
}
