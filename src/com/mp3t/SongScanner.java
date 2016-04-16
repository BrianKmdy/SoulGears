package com.mp3t;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.eclipse.swt.widgets.Display;

import acoustID.Recording;
import acoustID.Recordings;
import acoustID.Release;

public class SongScanner implements Runnable {

	private Song[] songs;
	private Display display;
	
	private LinkedList<Song> changedSongs;
	private LinkedList<Song> scannedSongs;
	
	private int numSongsRemaining;
	private int numChanges;
	
	public SongScanner(Song[] songs, Display display) {
		this.songs = songs;
		this.display = display;
		
		for (int i = 0; i < songs.length; i++)
			songs[i].setQueuedToScan(true);
		
		changedSongs = new LinkedList<Song>();
		scannedSongs = new LinkedList<Song>();
		numSongsRemaining = songs.length;
		numChanges = 0;
	}

	public void run() {
		for (int i = 0; i < songs.length; i++) {
			if (lookupTag(songs[i])) {
				synchronized (changedSongs) {
					changedSongs.add(songs[i]);
				}
			} else {
				synchronized (scannedSongs) {
					scannedSongs.add(songs[i]);
				}
			}
			songs[i].setQueuedToScan(false);
			songs[i].setScanned(true);
			songs[i].setError(false);
			display.wake();
		}
	}
	
	public Song[] getChangedSongs() {
		Song[] songList;
		
		synchronized(changedSongs) {
			songList = changedSongs.toArray(new Song[changedSongs.size()]);
			numSongsRemaining -= changedSongs.size();
			changedSongs.clear();
		}
		
		
		return songList;
	}
	
	public Song[] getScannedSongs() {
		Song[] songList;
		
		synchronized(scannedSongs) {
			songList = scannedSongs.toArray(new Song[scannedSongs.size()]);
			numSongsRemaining -= scannedSongs.size();
			scannedSongs.clear();
		}
		
		return songList;
	}
	
	public int getNumSongsRemaining() {
		return numSongsRemaining;
	}
	
	public int getNumChanges() {
		return numChanges;
	}
	
	public void resetNumChanges() {
		numChanges = 0;
	}

	private boolean lookupTag(Song song) {
		String fpcalc = null;
		
		if ((Settings.OS == Settings.LINUX) || (Settings.OS == Settings.MAC))
			fpcalc = "./fpcalc";
		else if (Settings.OS == Settings.WINDOWS)
			fpcalc = "fpcalc.exe";
		
	    boolean madeChanges = false;
	    boolean hadChanges = song.hasPendingChanges();
		
		try {
		    ProcessBuilder pb = new ProcessBuilder(fpcalc, song.getFile().getAbsolutePath());
		    BufferedReader br = new BufferedReader(new InputStreamReader(pb.start().getInputStream())); 
		    
		    int duration = 0;
		    String fingerprint = null;
		    
		    String line = null;
		    while((line = br.readLine()) != null) {
		    	if (line.length() > 8) {
		    		if (line.substring(0, 8).equals("DURATION"))
	    				duration = Integer.parseInt(line.substring(9));
		    		
		    		if (line.length() > 11) {
		    			if (line.substring(0, 11).equals("FINGERPRINT"))
		    				fingerprint = line.substring(12);
		    		}
		    	}
		    }
		    
		    Recordings recordings = new Recordings(new URL("http://api.acoustid.org/v2/lookup?client=PUAVU3LKzJ&meta=recordings+tracks+releases+compress&format=json" + "&duration=" + duration + "&fingerprint=" + fingerprint));
		    Recording firstRecording = recordings.getFirstRecording();
		    Release firstRelease = firstRecording.getFirstRelease();
		    
		    if (firstRecording != null) {
			    if (firstRecording.getTitle() != null) {
			    	song.setPendingTitle(firstRecording.getTitle());
			    	madeChanges = true;
			    }
			    
			    if (firstRecording.getArtist() != null) {
			    	song.setPendingArtist(firstRecording.getArtist());
			    	madeChanges = true;
			    }
			    
			    if (firstRelease != null) {
				    if (firstRelease.getAlbum() != null) {
				    	song.setPendingAlbum(firstRelease.getAlbum());
				    	madeChanges = true;
				    }
				    
				    if (firstRelease.getYear() != null) {
				    	song.setPendingYear(firstRelease.getYear());
				    	madeChanges = true;
				    }
				    
				    if (firstRelease.getTrackNum() != null) {
				    	song.setPendingTrackNumber(firstRelease.getTrackNum());
				    	madeChanges = true;
				    }
			    }
		    }
		    
		    // Old way of doing things. Doesn't ensure all tags are from the same release of a track.
		    /*
			URL acoustid = new URL("http://api.acoustid.org/v2/lookup?client=8XaBELgH&meta=recordings+releasegroups+tracks+releases+compress&format=json" + "&duration=" + duration + "&fingerprint=" + fingerprint);
		    InputStreamReader in = new InputStreamReader(acoustid.openStream());
		    
		    JsonParser parser = Json.createParser(in);
		    
		    Event e;
		    while (parser.hasNext()) {
		    	e = parser.next();
		    	if ((e == Event.KEY_NAME) && (parser.getString().equals("recordings"))){
					parser.next();
					while (parser.hasNext()) {
						e = parser.next();
						if (e == Event.KEY_NAME){ 
							switch (parser.getString()) {
								case "title":
									parser.next();
									song.setPendingTitle(parser.getString());
									madeChanges = true;
									break;
								case "artists":
									while (parser.hasNext()) {
										e = parser.next();
										if ((e == Event.KEY_NAME) && (parser.getString().equals("name"))) {
											parser.next();
											song.setPendingArtist(parser.getString());
											madeChanges = true;
											break;
										}
									}
									while (parser.hasNext()) {
										e = parser.next();
										if (e == Event.END_ARRAY)
											break;
									}
									break;
								case "releasegroups":
									int numArrays1 = 0;
									int numObjects1 = 0;
									
									while (parser.hasNext()) {
										e = parser.next();
										if (e == Event.KEY_NAME) {
											switch (parser.getString()) {
												case "title":
													e = parser.next();
													song.setPendingAlbum(parser.getString());
													madeChanges = true;
													break;
												case "secondarytypes":
													while (parser.hasNext()) {
														e = parser.next();
														if (e == Event.VALUE_STRING) {
															song.setPendingGenre(parser.getString());
															madeChanges = true;
															break;
														}
													}
													while (parser.hasNext()) {
														e = parser.next();
														if (e == Event.END_ARRAY)
															break;
													}
													break;
												case "releases":
													int numArrays2 = 0;
													int numObjects2 = 0;
													
													while (parser.hasNext()) {
														e = parser.next();
														if (e == Event.KEY_NAME) {
															switch (parser.getString()) {
																case "date":
																	while (parser.hasNext()) {
																		e = parser.next();
																		if ((e == Event.KEY_NAME) && (parser.getString().equals("year"))) {
																			e = parser.next();
																			song.setPendingYear(parser.getString());
																			madeChanges = true;
																			break;
																		}
																	}
																	while (parser.hasNext()) {
					    												e = parser.next();
					    												if (e == Event.END_OBJECT)
					    													break;
					    											}
																	break;
																case "tracks":
																	while (parser.hasNext()) {
																		e = parser.next();
																		if (e == Event.KEY_NAME) {
																			switch (parser.getString()) {
																				case "position":
																					e = parser.next();
																					song.setPendingTrackNumber(parser.getString());
																					madeChanges = true;
																					break;
																				case "artists":
																					while (parser.hasNext()) {
																						e = parser.next();
																						if (e == Event.END_ARRAY)
																							break;
																					}
																					break;
																			}
																		} else if (e == Event.END_ARRAY) {
																			break;
																		}
																	}
																	break;
															}
														} else {
															if (e == Event.START_ARRAY)
																numArrays2++;
															else if (e == Event.START_OBJECT)
																numObjects2++;
															else if (e == Event.END_ARRAY)
																numArrays2--;
															else if (e == Event.END_OBJECT)
																numObjects2--;
															
															if ((numArrays2 == 0) && (numObjects2 == 0))
																break;
														}
													}
													break;
											}
										} else {
											if (e == Event.START_ARRAY)
												numArrays1++;
											else if (e == Event.START_OBJECT)
												numObjects1++;
											else if (e == Event.END_ARRAY)
												numArrays1--;
											else if (e == Event.END_OBJECT)
												numObjects1--;
											
											if ((numArrays1 == 0) && (numObjects1 == 0))
												break;
										}
									}
									break;
							}
						}
					}
		    	}
		    }
		    */
		} catch (Exception e) {
			if (Core.DEBUG)
				System.out.println("Error reading file \"" + (song.getFileName()) + "\": " + e.getMessage());
		}
		
		if (madeChanges && !hadChanges)
			numChanges++;
		
		return madeChanges;
	}
}
