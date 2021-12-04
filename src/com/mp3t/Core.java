/**
 * Core functionality of the mp3 tagger program. Responsible for loading, managing, and updating all the songs.
 * 
 * @author Brian Moody
 * @date 8/22/2012
 * @version 0.1
 * 
 */

/* TO-DO:
 *			-(Linux) Text wrap doesn't work

 *			-(Mac) Buttons too short for icons
 *			-(Mac) Can't scroll song table while text boxes are selected
 *			-(Mac) No version URL link
 *
 * * 			-Clearing tags doesn't work for entagged
 *  * 			-Maybe implement echoprint
 */

package com.mp3t;

import java.io.File;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Display;

public class Core implements Runnable {
	
	public static final boolean DEBUG = false;
	
	public static final String PROGRAM_NAME = "SoulGears";
	public static final String PROGRAM_VERSION = "1.0";
	public static final String AUTHOR_NAME = "Brian Moody";
	public static final String PUBLICATION_DATE = "4/16/2016";
	
	public static final String PROGRAM_NAME_HEADER = "[" + PROGRAM_NAME + "] ";
	
	public static final String DEBUG_HEADER = "[Debug] ";
	public static final String ERROR_HEADER = "[Error] ";
	
	public static final String VERSION_URL = "https://sourceforge.net/p/soulgears/code/ci/master/tree/version.txt?format=raw";
	public static final String DOWNLOAD_URL = "https://sourceforge.net/projects/soulgears/files/?source=navbar";
	public static final String USERGUIDE_URL = "https://sourceforge.net/p/soulgears/wiki/Home/";
	
	public static final String HOME_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + '.' + PROGRAM_NAME.toLowerCase() + System.getProperty("file.separator");
	
	public static final boolean LOGGING = false;
	public static final String LOG_FILE = "log.txt";
	public static Logger log = new Logger();
	
	/* Maximum number of songs to add to the song list every tick */
	public static final int MAX_NEW_SONGS_PER_TICK = 1;
	
	private Display display;
	
	/* The complete list of loaded songs */
	private Songs songs;
	
	/* The list of new songs to hand over to whatever object wants them */
	private LinkedList<Object[]> newSongs;
	
	private SongLoader songLoader;
	
	private int thumbnailWidth;
	
	/* The search filter text the user's specified */
	private String searchText = null;
	
	/* If refreshing then re-add the list of valid songs from the songs list to newSongs **/
	private boolean refresh = false;
	private boolean refreshing = false;
	
	private boolean reset = false;
	
	/* Keep looping so long as this is true, it gets set to false upon program termination */
	private boolean stillAlive = true;
	
	private boolean canScan = false;
	
	private int maxNumSongs;
	
	private int numSongsToDeduct = 0;
	
	public Core(SongLoader songLoader, int maxNumSongs, int thumbnailWidth) {
		songs = new Songs();
		newSongs = new LinkedList<Object[]>();
		this.songLoader = songLoader;
		this.maxNumSongs = maxNumSongs;
		this.thumbnailWidth = thumbnailWidth;
	}
	
	public void setDisplay(Display display) {
		this.display = display;
	}

	/* Returns a reference to the list that holds songs to be added to the main table */
	public LinkedList<Object[]> getNewSongs() {
		return newSongs;
	}
	
	/* Returns the number of songs in the song list */
	public int getNumSongs() {
		return songs.size();
	}
	
	/* Returns the number of new songs in the newSongs list */
	public int getNumNewSongs() {
		return newSongs.size();
	}
	
	public int getTotalNumSongs() {
		return songLoader.getNumSongs() - numSongsToDeduct;
	}
	
	public int getMaxNumSongs() {
		return maxNumSongs;
	}
	
	public int getNumPendingChanges() {
		int numPendingChanges = 0;
		
		synchronized (songs) {
			for (int i = 0; i < songs.size(); i++) {
				if (songs.get(i).hasPendingChanges())
					numPendingChanges++;
			}
		}
		
		return numPendingChanges;
	}
	
	/* Activates refresh mode which sorts the song list, clears the newSongs list, and copies new songs over to be added to the table */
	public void refresh() {
		synchronized (songLoader) {
			this.refresh = true;
			songLoader.notifyAll();
		}
	}
	
	/* Set whether the sort should be ascending or descending */
	public void setSortDirection(byte sortDirection) {
		synchronized (songs) {
			songs.setSortDirection(sortDirection);
		}
	}
	
	/* Set the order the columns should be sorted by */
	public void setSortOrder(byte[] sortOrder) {
		synchronized (songs) {
			songs.setSortOrder(sortOrder);
		}
	}
	
	/* Set the filter text for searching */
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
	
	public LinkedList<Song> getPendingSongs() {
		LinkedList<Song> pendingSongs = new LinkedList<Song>();
		
		synchronized (songs) {
			for (int i = 0; i < songs.size(); i++) {
				if (songs.get(i).hasPendingChanges()) 
					pendingSongs.add(songs.get(i));
			}
		}
		
		return pendingSongs;
	}
	
	public void removeSong(Song song) {
		synchronized (songs) {
			song.dispose();
			songs.remove(song);
		}
		
		synchronized (songLoader) {
			songLoader.notifyAll();
		}
		
		numSongsToDeduct++;
	}
	
	public void removeSongs(Song[] songs) {
		synchronized (this.songs) {
			for (int i = 0; i < songs.length; i++) {
				songs[i].dispose();
				this.songs.remove(songs[i]);
			}
		}
		
		synchronized (songLoader) {
			songLoader.notifyAll();
		}
		
		numSongsToDeduct += songs.length;
	}
	
	public void removeAll() {
		songLoader.clear();
		reset = true;
		
		synchronized (songs) {
			songs.clear();
		}
		
		synchronized (newSongs) {
			newSongs.clear();
		}
		
		synchronized (songLoader) {
			songLoader.notifyAll();
		}
		
		numSongsToDeduct = 0;
	}
	
	public boolean refreshing() {
		return refreshing;
	}
	
	/* Terminate the core's main sequence */
	// Doesn't work at the moment, not sure why
	public void kill() {
		this.stillAlive = false;
	}

	@Override
	public void run() {
		/* Initialize buffers and indexes */
		int songIndex = 0;
		int addedSongIndex = 0;
		
		File[] songFilesBuffer = null;
	    int bufferIndex = 0;
	    
		while (stillAlive) {
			/* If given the refresh command activate the refreshing process, reset the indices, and set refresh to false */
			
			if (refresh) {	
				refresh = false;
				refreshing = true;
				
				songIndex = 0;
				addedSongIndex = 0;
				
				synchronized (newSongs) {
					newSongs.clear();
				}
				
				synchronized (songs) {
					songs.sort();
				}
			}
			
			/* Refreshing sequence, add MAX_NEW_SONGS_PER_TICK songs into newSongs to be added to the table */
			if (refreshing) {
				synchronized (songs) {
					/* Set the number of loops equal to either songIndex + MAX_NEW_SONGS_PER_TICK or the size of the song list, whichever is less */
					int endNum = ((songIndex + MAX_NEW_SONGS_PER_TICK) < songs.size()) ? (songIndex + MAX_NEW_SONGS_PER_TICK) : songs.size();
					
					for ( ; songIndex < endNum; songIndex++) {
						
						Object[] newSong = new Object[2];
						
						newSong[0] = songs.get(songIndex);
						newSong[1] = new Integer(addedSongIndex);
						
						if (searchText == null || ((Song) newSong[0]).containsText(searchText)) {
							synchronized (newSongs) {
								if (reset)
									break;
								
								newSongs.add(newSong);
							}
							display.wake();
							
							addedSongIndex++;
						}
					}
					
					/* Reset the indexes and disable the refreshing process once we've reached the end */
					if (endNum == songs.size()) {
						songIndex = 0;
						addedSongIndex = 0;
						refreshing = false;
					}
				}
			/* If we're not refreshing see if we can load any more files into the song list */
			} else {
				if (songFilesBuffer == null) {
					int numSongs = getNumSongs();
					if (numSongs < maxNumSongs) {
						LinkedList<File> queuedSongFiles = songLoader.getList();
						synchronized(queuedSongFiles) {
							/* If there's queued up song files ready to be added */
							if (queuedSongFiles.size() > 0) {
								int numSongsToFetch = ((maxNumSongs - numSongs) < queuedSongFiles.size()) ? (maxNumSongs - numSongs) : queuedSongFiles.size();
								
								songFilesBuffer = new File[numSongsToFetch];
								
								/* Copy all the queued up songs files into songFilesBuffer and remove them from the queuedSongFiles list */
								for (int i = 0; i < numSongsToFetch; i++) {
									songFilesBuffer[i] = queuedSongFiles.get(0);
									queuedSongFiles.remove(0);
								}
							}
						}
					}
				/* If there's song files in the buffer waiting to be added to the main song list */
				} else {
					/* endNum is either equal to bufferIndex + MAX_NEW_SONGS_PER_TICK or the length of songFilesBuffer, whichever's less */
					int endNum = ((bufferIndex + MAX_NEW_SONGS_PER_TICK) < songFilesBuffer.length) ? (bufferIndex + MAX_NEW_SONGS_PER_TICK) : songFilesBuffer.length;
					
					for ( ; bufferIndex < endNum; bufferIndex++) {
						if (songFilesBuffer[bufferIndex] != null) {
							try {
								Object[] newSong = new Object[2];
								
								/* Create a new song object from a file in the buffer and add it to the songs list, then prepare an object array to pass the song object and its index to newSongs */
								synchronized (songs) {
									if (songs.contains(songFilesBuffer[bufferIndex]))
										throw new Exception("Duplicate song");
									
									Song song = new Song(songFilesBuffer[bufferIndex], thumbnailWidth, display);
									
									if (reset)
										break;
									
									songs.add(song);
	
									newSong[0] = song;
									newSong[1] = new Integer(songs.indexOf(song));
								}
								
								/* If the new song object contains the filter text then add it to the newSongs list */
								if (searchText == null || ((Song) newSong[0]).containsText(searchText)) {
									synchronized (newSongs) {
										if (reset)
											break;
										
										newSongs.add(newSong);
									}
									display.wake();
								}
							/* If something goes wrong creating the song object then do nothing (Print a warning in debug mode) */
							} catch (Exception e) {
								numSongsToDeduct++;
								if (DEBUG)
									System.out.println(DEBUG_HEADER + e.toString() + " for song file: " + songFilesBuffer[bufferIndex].getName());
							}
						}
					}
					
					/* If we reach the end of the song file buffer then reset the buffer and its index */
					if (bufferIndex == songFilesBuffer.length) {
						songFilesBuffer = null;
						bufferIndex = 0;
					}
				}
				
				if (reset) {
					songIndex = 0;
					addedSongIndex = 0;
					songFilesBuffer = null;
				    bufferIndex = 0;
					refreshing = false;
					reset = false;
				}
				
				/* If all current tasks are completed then wait */
				if (!refreshing) {
					if (songFilesBuffer == null) {
						if (!songLoader.hasFiles() || getNumSongs() == maxNumSongs) {
							synchronized (songLoader) {
								try {
									songLoader.wait();
								} catch (InterruptedException e) {
									System.err.println("Core interrupted: " + e.toString());
								}
							}
						}
					}
				}
			}
			
			if (reset) {
				songIndex = 0;
				addedSongIndex = 0;
				songFilesBuffer = null;
			    bufferIndex = 0;
				refreshing = false;
				reset = false;
			}
		}
	}
}
