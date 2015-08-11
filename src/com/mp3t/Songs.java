package com.mp3t;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Container class to manage/sort song objects.
 * 
 * @author Brian Moody
 * @date 9/19/2012
 * @version 0.1
 */

/* TO-DO:
 * 
 * 		Current:

 * 
 * 		Distant:

 */

public class Songs extends LinkedList<Song> {

	// Not quite sure what this is for, but eclipse suggested I add it
	private static final long serialVersionUID = 6524054608129281828L;
	
	public static final byte SORT_ASCENDING  = -1;
	public static final byte SORT_DESCENDING = 1;
	
	public static final byte DEFAULT_SORT_DIRECTION = SORT_DESCENDING;
	
	private byte sortDirection;
	
	private byte[] sortOrder;

	public Songs() {
		super();
		sortOrder = (byte[]) Song.DEFAULT_SORT_ORDER.clone();
		sortDirection = DEFAULT_SORT_DIRECTION;
	}
	
	public Songs(Collection<Song> c) {
		super(c);
		sortOrder = (byte[]) Song.DEFAULT_SORT_ORDER.clone();
		sortDirection = DEFAULT_SORT_DIRECTION;
	}
	
	public boolean add(Song song) {
		if (song != null) {
			song.setSortOrder(sortOrder);
			
			if (sortDirection == SORT_ASCENDING) {		
				for (int i = 0; i < size(); i++) {
					
					if (song.compareTo(get(i), sortDirection) >= 0) {
						add(i, song);
						return true;
					}
				}
			} else if (sortDirection == SORT_DESCENDING) {
				for (int i = 0; i < size(); i++) {
					
					if (song.compareTo(get(i), sortDirection) <= 0) {
						add(i, song);
						return true;	
					}
				}
			}
			
			addLast(song);
			return true;
		}
		
		return false;
	}
	
	public boolean contains(Song song) {
		for (int i = 0; i < size(); i++) {
			if (get(i).equals(song))
				return true;
		}
		return false;
	}
	
	public boolean contains(File file) {
		for (int i = 0; i < size(); i++) {
			if (get(i).equals(file))
				return true;
		}
		return false;
	}
	
	public void setSortDirection(byte sortDirection) {
		this.sortDirection = sortDirection;
	}
	
	public void setSortOrder(byte[] sortOrder) {
		this.sortOrder = sortOrder.clone();
	}
	
	public void sort() {
		for (int i = 0; i < size(); i++) {
			get(i).setSortOrder(sortOrder);
		}
		
		Comparator<Object> songComparator;
		
		if (sortDirection == SORT_DESCENDING) {
			songComparator = new Comparator<Object>() {

				public int compare(Object song1, Object song2) {
					if ((song1 instanceof Song) && (song2 instanceof Song)) {
						return ((Song) song1).compareTo((Song) song2, sortDirection);
					} else {
						return 0;
					}
				}
				
			};
		} else {
			songComparator = new Comparator<Object>() {

				public int compare(Object song1, Object song2) {
					if ((song1 instanceof Song) && (song2 instanceof Song)) {
						return ((Song) song2).compareTo((Song) song1, sortDirection);
					} else {
						return 0;
					}
				}
				
			};
		}
		
		Collections.sort(this, songComparator);
	}
	
	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		
		strBuff.append("size = " + size() + '\n');
		
		strBuff.append('\n');
		strBuff.append('[');
		
		for (int i = 0; i < size(); i++) {
			strBuff.append("Song " + i + ":\n" + get(i).toString());
			if (i + 1 < size())
				strBuff.append("\n\n");
		}
		
		strBuff.append(']');
		
		return strBuff.toString();
	}
}
