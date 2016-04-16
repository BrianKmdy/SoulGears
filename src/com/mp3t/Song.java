package com.mp3t;

import com.mp3t.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.mpatric.mp3agic.AbstractID3v2Tag;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import entagged.audioformats.AudioFile;
import entagged.audioformats.AudioFileIO;
import entagged.audioformats.Tag;
import entagged.audioformats.exceptions.CannotReadException;
import entagged.audioformats.exceptions.CannotWriteException;

/**
 * This class holds a reference to an mp3 file and contains all the tags that can be customized
 * as well as the methods necessary to do so.
 * 
 * @author Brian Moody
 * @date 9/19/2012
 * @version 0.2
 */

public class Song {
	
	public static final byte FILENAME_SORT_ID        = 0x00;
	public static final byte DIRECTORYNAME_SORT_ID   = 0x01;
	public static final byte TITLE_SORT_ID           = 0x02;
	public static final byte ARTIST_SORT_ID          = 0x03;
	public static final byte ALBUM_SORT_ID           = 0x04;
	public static final byte GENRE_SORT_ID           = 0x05;
	public static final byte YEAR_SORT_ID            = 0x06;
	public static final byte COMMENT_SORT_ID         = 0x07;
	public static final byte TRACKNUMBER_SORT_ID     = 0x08;
	public static final byte SUGGESTEDFORMAT_SORT_ID = 0x09;
	
	/** The order by which to compare the song's data fields when sorting */
	public static final byte[] DEFAULT_SORT_ORDER = {DIRECTORYNAME_SORT_ID, SUGGESTEDFORMAT_SORT_ID, FILENAME_SORT_ID, ARTIST_SORT_ID, ALBUM_SORT_ID, GENRE_SORT_ID, TITLE_SORT_ID, YEAR_SORT_ID, COMMENT_SORT_ID, TRACKNUMBER_SORT_ID};
	
	private static final int CHAR_TYPE_ALPHANUM   = 0;
	private static final int CHAR_TYPE_WHITESPACE = 1;
	private static final int CHAR_TYPE_DIVIDER    = 2;
	
	private static final double DEFAULT_IMAGE_WIDTH  = 24.0;
	
	/** A reference to the actual physical mp3 file. */
	private File file;
	
	/** Name of the file (not including the extension). */
	private String fileName;
	
	/** Name of the file's parent directory. */
	private String directoryName;
	
	/** The suggested data format of the song's filename */
	private String suggestedFormat;
	
	/** Title of the song. */
	private String title;
	
	/** Artist of the song. */
	private String artist;
	
	/** The song's album. */
	private String album;
	
	/** The song's genre. */
	private String genre;
	
	/** The year the song was released. */
	private String year;
	
	/** A comment that gets embedded in the song's tags. */
	private String comment;
	
	/** The song's track number */
	private String trackNumber;
	private String pendingTitle;
	private String pendingArtist;
	private String pendingAlbum;
	private String pendingGenre;
	private String pendingYear;
	private String pendingComment;
	private String pendingTrackNumber;
	
	private boolean hasError;
	private boolean queuedToScan;
	private boolean alreadyScanned;
	
	private Image thumbnail;
	
	private boolean isMp3;
	
	/** The order to compare data fields */
	private byte[] sortOrder;
	
	
	/** Creates a new Song object using a String for the location of an mp3 file and the default sort order 
	 * @throws InvalidDataException 
	 * @throws UnsupportedTagException 
	 * @throws CannotReadException */
	public Song(String fileLocation, Display display) throws IOException, UnsupportedTagException, InvalidDataException, CannotReadException {
		this(new File(fileLocation), DEFAULT_SORT_ORDER, DEFAULT_IMAGE_WIDTH, display);
	}
	
	/** Creates a new Song object using a File object that refers to an mp3 file and the default sort order 
	 * @throws InvalidDataException 
	 * @throws UnsupportedTagException 
	 * @throws CannotReadException */
	public Song(File file, Display display) throws IOException, UnsupportedTagException, InvalidDataException, CannotReadException {
		this(file, DEFAULT_SORT_ORDER, DEFAULT_IMAGE_WIDTH, display);
	}
	
	public Song(String fileLocation, double imageWidth, Display display) throws IOException, UnsupportedTagException, InvalidDataException, CannotReadException {
		this(new File(fileLocation), DEFAULT_SORT_ORDER, imageWidth, display);
	}
	
	public Song(File file, double imageWidth, Display display) throws IOException, UnsupportedTagException, InvalidDataException, CannotReadException {
		this(file, DEFAULT_SORT_ORDER, imageWidth, display);
	}
	
	/** Creates a new Song object using a String for the location of an mp3 file. 
	 * @throws InvalidDataException 
	 * @throws UnsupportedTagException 
	 * @throws CannotReadException */
	public Song(String fileLocation, byte[] sortOrder, double imageWidth, Display display) throws IOException, UnsupportedTagException, InvalidDataException, CannotReadException {
		this(new File(fileLocation), sortOrder, imageWidth, display);
	}
	
	/** Creates a new Song object using a File object that refers to an mp3 file. 
	 * @throws InvalidDataException 
	 * @throws UnsupportedTagException 
	 * @throws CannotReadException */
	public Song(File file, byte[] sortOrder, double imageWidth, Display display) throws IOException, UnsupportedTagException, InvalidDataException, CannotReadException {
		this.file = file;
		
		if (!this.file.exists()) 
			throw new IOException(new String("File " + this.file.getAbsolutePath() + " does not exist"));
		if (!this.file.canRead())
			throw new IOException(new String("Unable to read file " + this.file.getAbsolutePath()));

		this.fileName = this.file.getName();
		
		if (getExt(fileName).equals("mp3"))
			isMp3 = true;
		else
			isMp3 = false;

		this.fileName = this.fileName.substring(0, this.fileName.lastIndexOf("."));
		this.directoryName = this.file.getParentFile().getName();

		this.suggestedFormat = generateSuggestedFormat(this.fileName);

		byte[] image = null;
		
		if (isMp3 && ((Settings.imagesEnabled()) || (Settings.getLibrary() == Settings.LIB_MP3MAGIC))) {
			Mp3File mp3File = new Mp3File(this.file.getAbsolutePath());
			
			if (mp3File.hasId3v2Tag()) {
				ID3v2 id3v2 = mp3File.getId3v2Tag();
				
				this.title       = id3v2.getTitle().trim();
				this.artist      = id3v2.getArtist().trim();
				this.album       = id3v2.getAlbum().trim();
				this.genre       = id3v2.getGenreStr().trim();
				this.year        = id3v2.getYear().trim();
				this.comment     = id3v2.getComment().trim();
				this.trackNumber = id3v2.getTrack().trim();
				
				if (Settings.imagesEnabled()) {
					byte[] buff = id3v2.getAlbumImage();
					if (buff != null) {
						String mimeType = id3v2.getAlbumImageMimeType();
						mimeType = mimeType.substring(mimeType.lastIndexOf("/") + 1, mimeType.length());
						
						// This block removes problem causing header information from certain jpegs
						if (mimeType.equals("jpg") || mimeType.equals("jpeg")) {
							final byte[] jpegStartSequence1 = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
							final byte[] jpegStartSequence2 = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xFE};
							
							for (int i = 0; i < buff.length; i++) {
								if (buff[i] == jpegStartSequence1[0]) {
									boolean matchFound = true;
									
									for (int o = 0; o < jpegStartSequence1.length; o++) {
										if (buff[i + o] != jpegStartSequence1[o] && buff[i + o] != jpegStartSequence2[o]) {
											matchFound = false;
											break;
										}
									}
									
									if (matchFound) {
										image = new byte[buff.length - i];
										System.arraycopy(buff, i, image, 0, buff.length - i);
										break;
									}
								}
							}
							
						} else {
							image = buff;
						}
					}
				}
				
			// If an mp3 file doesn't have an id3v2 tag but does have an id3v1 tag then use it, otherwise just stick to the id3v2
			} else if (mp3File.hasId3v1Tag()) {
				ID3v1 id3v1 = mp3File.getId3v1Tag();
				
				this.title       = id3v1.getTitle().trim();
				this.artist      = id3v1.getArtist().trim();
				this.album       = id3v1.getAlbum().trim();
				this.genre       = Constants.genreIdToString.get(id3v1.getGenre());
				this.year        = id3v1.getYear().trim();
				this.comment     = id3v1.getComment().trim();
				this.trackNumber = id3v1.getTrack().trim();
				
			// If the mp3 file doesn't have any tags then just initialize the fields as empty strings to avoid null pointers
			} 
		} else {
			AudioFile audioFile = AudioFileIO.read(file);
			
			Tag tag = audioFile.getTag();
			
			this.title = tag.getFirstTitle().trim();
			this.artist = tag.getFirstArtist().trim();
			this.album = tag.getFirstAlbum().trim();
			this.genre = tag.getFirstGenre().trim();
			this.year = tag.getFirstYear().trim();
			this.comment = tag.getFirstComment().trim();
			this.trackNumber = tag.getFirstTrack().trim();
		}
		
		if (this.title == null)
			this.title = new String();
		if (this.artist == null)
			this.artist = new String();
		if (this.album == null)
			this.album = new String();
		if (this.genre == null)
			this.genre = new String();
		if (this.year == null)
			this.year = new String();
		if (this.comment == null)
			this.comment = new String();
		if (this.trackNumber == null)
			this.trackNumber = new String();
		
		this.pendingTitle       = new String();
		this.pendingArtist      = new String();
		this.pendingAlbum       = new String();
		this.pendingGenre       = new String();
		this.pendingYear        = new String();
		this.pendingComment     = new String();
		this.pendingTrackNumber = new String();	
		
		this.hasError       = false;
		this.queuedToScan   = false;
		this.alreadyScanned = false;
		
		this.sortOrder = sortOrder.clone();
		
		this.thumbnail = null;
		if (image != null) {
			/* Convert the byte array image in the song object to an swt Image */
			ByteArrayInputStream bais = new ByteArrayInputStream(image);
			ImageData imageData = null;
			try {
				
				imageData = new ImageData(bais);	
				
				/* Scale the image to IMAGE_HEIGHT and IMAGE_WIDTH */
				double ratio = 0.0d;
				if (imageData.height > imageData.width)
					ratio = imageWidth / (double) imageData.height;
				else
					ratio = imageWidth / (double) imageData.width;
				
				imageData = imageData.scaledTo((int) Math.round((double) imageData.width * ratio), (int) Math.round((double) imageData.height * ratio));
				thumbnail = new Image(display, imageData);
				
			} catch (Exception e) {
				if (Core.DEBUG)
					System.out.println(e.toString());
			}
		}
	}
	
	public void dispose() {
		if (thumbnail != null)
			thumbnail.dispose();
	}
	
	/** Sets the sort order for this song object */
	public void setSortOrder(byte[] sortOrder) {
		this.sortOrder = sortOrder.clone();
	}
	
	/** Sets the title of the song. */
	public void setTitle(String title) {
		this.title = new String(title);
	}

	/** Sets the artist of the song. */
	public void setArtist(String artist) {
		this.artist = new String(artist);
	}
	
	/** Sets the song's album. */
	public void setAlbum(String album) {
		this.album = new String(album);
	}
	
	/** Sets the song's genre. */
	public void setGenre(String genre) {
		this.genre = new String(genre);
	}
	
	/** Sets the year for the song. */
	public void setYear(String year) {
		this.year = new String(year);
	}
	
	/** Sets the comment for the song. */
	public void setComment(String comment) {
		this.comment = new String(comment);
	}
	
	/** Sets the track number for the song using a string. */
	public void setTrackNumber(String trackNumber) {
		this.trackNumber = new String(trackNumber);
	}
	
	/** Sets the track number for the song using an int. */
	public void setTrackNumber(int trackNumber) {
		this.trackNumber = new String(Integer.toString(trackNumber));
	}
	
	public void setPendingTitle(String title) {
		this.pendingTitle = new String(title);
	}

	public void setPendingArtist(String artist) {
		this.pendingArtist = new String(artist);
	}
	
	public void setPendingAlbum(String album) {
		this.pendingAlbum = new String(album);
	}
	
	public void setPendingGenre(String genre) {
		this.pendingGenre = new String(genre);
	}
	
	public void setPendingYear(String year) {
		this.pendingYear = new String(year);
	}
	
	public void setPendingComment(String comment) {
		this.pendingComment = new String(comment);
	}
	
	public void setPendingTrackNumber(String trackNumber) {
		this.pendingTrackNumber = new String(trackNumber);
	}
	
	public void setPendingTrackNumber(int trackNumber) {
		this.pendingTrackNumber = new String(Integer.toString(trackNumber));
	}
	
	public void setError(boolean hasError) {
		this.hasError = hasError;
	}
	
	public void setQueuedToScan(boolean queuedToScan) {
		this.queuedToScan = queuedToScan;
	}
	
	public void setScanned(boolean alreadyScanned) {
		this.alreadyScanned = alreadyScanned;
	}
	
	/** Returns true if the title has been set or false otherwise */
	public boolean hasTitle() {
		if (this.title.isEmpty())
			return false;
		else
			return true;
	}
	
	/** Returns true if the artist has been set or false otherwise */
	public boolean hasArtist() {
		if (this.artist.isEmpty())
			return false;
		else
			return true;
	}
	
	/** Returns true if the album has been set or false otherwise */
	public boolean hasAlbum() {
		if (this.album.isEmpty())
			return false;
		else
			return true;
	}
	
	/** Returns true if the genre has been set or false otherwise */
	public boolean hasGenre() {
		if (this.genre.isEmpty())
			return false;
		else
			return true;
	}
	
	/** Returns true if the year has been set or false otherwise */
	public boolean hasYear() {
		if (this.year.isEmpty())
			return false;
		else
			return true;
	}
	
	/** Returns true if the comment has been set or false otherwise */
	public boolean hasComment() {
		if (this.comment.isEmpty())
			return false;
		else
			return true;
	}
	
	/** Returns true if the trackNumber has been set or false otherwise */
	public boolean hasTrackNumber() {
		if (this.trackNumber.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean hasPendingChanges() {
		if (!this.pendingTitle.isEmpty())
			return true;
		if (!this.pendingArtist.isEmpty())
			return true;
		if (!this.pendingAlbum.isEmpty())
			return true;
		if (!this.pendingGenre.isEmpty())
			return true;
		if (!this.pendingYear.isEmpty())
			return true;
		if (!this.pendingComment.isEmpty())
			return true;
		if (!this.pendingTrackNumber.isEmpty())
			return true;
		
		return false;
	}
	
	public boolean hasExistingTags() {
		if (!this.title.isEmpty())
			return true;
		if (!this.artist.isEmpty())
			return true;
		if (!this.album.isEmpty())
			return true;
		if (!this.genre.isEmpty())
			return true;
		if (!this.year.isEmpty())
			return true;
		if (!this.comment.isEmpty())
			return true;
		if (!this.trackNumber.isEmpty())
			return true;
		
		return false;
	}
	
	public void clearPendingChanges() {
		this.pendingTitle       = new String();
		this.pendingArtist      = new String();
		this.pendingAlbum       = new String();
		this.pendingGenre       = new String();
		this.pendingYear        = new String();
		this.pendingComment     = new String();
		this.pendingTrackNumber = new String();
	}
	
	public void clearExistingTags() {
		this.title       = new String();
		this.artist      = new String();
		this.album       = new String();
		this.genre       = new String();
		this.year        = new String();
		this.comment     = new String();
		this.trackNumber = new String();
	}
	
	public boolean hasPendingTitle() {
		if (this.pendingTitle.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean hasPendingArtist() {
		if (this.pendingArtist.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean hasPendingAlbum() {
		if (this.pendingAlbum.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean hasPendingGenre() {
		if (this.pendingGenre.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean hasPendingYear() {
		if (this.pendingYear.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean hasPendingComment() {
		if (this.pendingComment.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean hasPendingTrackNumber() {
		if (this.pendingTrackNumber.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean hasError() {
		return this.hasError;
	}
	
	public boolean queuedToScan() {
		return this.queuedToScan;
	}
	
	public boolean alreadyScanned() {
		return this.alreadyScanned;
	}
	
	public boolean hasThumbnail() {
		if (thumbnail != null)
			return true;
		else
			return false;
	}
	
	public File getFile() {
		return this.file;
	}
	
	/** Get the file name of the mp3 file. */
	public String getFileName() {
		return this.fileName;
	}
	
	/** Get the directory name of the mp3 file. */
	public String getDirectoryName() {
		return this.directoryName;
	}
	
	/** Get the directory name of the mp3 file. */
	public String getSuggestedFormat() {
		return this.suggestedFormat;
	}
	
	/** Get the title of the song. */
	public String getTitle() {
		return this.title;
	}
	
	/** Get the artist of the song. */
	public String getArtist() {
		return this.artist;
	}
	
	/** Get the album of the song. */
	public String getAlbum() {
		return this.album;
	}
	
	/** Get the genre of the song. */
	public String getGenre() {
		return this.genre;
	}
	
	/** Get the year released from the song. */
	public String getYear() {
		return this.year;
	}
	
	/** Get the comment from the song. */
	public String getComment() {
		return this.comment;
	}
	
	/** Get the comment from the song. */
	public String getTrackNumber() {
		return this.trackNumber;
	}
	
	public String getPendingTitle() {
		return this.pendingTitle;
	}
	
	public String getPendingArtist() {
		return this.pendingArtist;
	}
	
	public String getPendingAlbum() {
		return this.pendingAlbum;
	}
	
	public String getPendingGenre() {
		return this.pendingGenre;
	}
	
	public String getPendingYear() {
		return this.pendingYear;
	}
	
	public String getPendingComment() {
		return this.pendingComment;
	}
	
	public String getPendingTrackNumber() {
		return this.pendingTrackNumber;
	}
	
	public String getDisplayTitle() {
		if (this.hasPendingTitle())
			return this.pendingTitle;
		else
			return this.title;
	}
	
	public String getDisplayArtist() {
		if (this.hasPendingArtist())
			return this.pendingArtist;
		else
			return this.artist;
	}
	
	public String getDisplayAlbum() {
		if (this.hasPendingAlbum())
			return this.pendingAlbum;
		else
			return this.album;
	}
	
	public String getDisplayGenre() {
		if (this.hasPendingGenre())
			return this.pendingGenre;
		else
			return this.genre;
	}
	
	public String getDisplayYear() {
		if (this.hasPendingYear())
			return this.pendingYear;
		else
			return this.year;
	}
	
	public String getDisplayComment() {
		if (this.hasPendingComment())
			return this.pendingComment;
		else
			return this.comment;
	}
	
	public String getDisplayTrackNumber() {
		if (this.hasPendingTrackNumber())
			return this.pendingTrackNumber;
		else
			return this.trackNumber;
	}
	
	
	public Image getThumbnail() {
		return thumbnail;
	}

	/** Updates mp3 file with new tag information. 
	 * @throws InvalidDataException 
	 * @throws UnsupportedTagException 
	 * @throws NotSupportedException 
	 * @throws CannotReadException 
	 * @throws CannotWriteException */
	public void save(boolean removeUnspecifiedFrames) throws IOException, NotSupportedException, UnsupportedTagException, InvalidDataException, CannotReadException, CannotWriteException {
		this.save (this.file, false, removeUnspecifiedFrames, false);
	}
	
	/**
	 * Outputs mp3 with new tag information to outputLocation (if outputLocation refers to current location it will just update).
	 * This song object will refer to the new mp3 file after this method is called.
	 * 
	 * @param outputLocation The location to output the new file.
	 * @param keepOriginal Whether or not the original file should be deleted after the new file is written (only relevant if outputLocation differs from current file location).
	 * @throws InvalidDataException 
	 * @throws UnsupportedTagException 
	 * @throws NotSupportedException 
	 * @throws CannotReadException 
	 * @throws CannotWriteException 
	 */
	public void save(String outputLocation, boolean keepOriginal, boolean removeUnspecifiedFrames, boolean overwriteExisting) throws IOException, NotSupportedException, UnsupportedTagException, InvalidDataException, CannotReadException, CannotWriteException {
		this.save (new File(outputLocation), keepOriginal, removeUnspecifiedFrames, overwriteExisting);
	}
	
	/**
	 * Outputs mp3 with new tag information to outputLocation (if outputLocation refers to current location it will just update).
	 * This song object will refer to the new mp3 file after this method is called.
	 * 
	 * @param outputLocation The location to output the new file.
	 * @param keepOriginal Whether or not the original file should be deleted after the new file is written (only relevant if outputLocation differs from current file location).
	 * @throws NotSupportedException 
	 * @throws InvalidDataException 
	 * @throws UnsupportedTagException 
	 * @throws CannotReadException 
	 * @throws CannotWriteException 
	 */
	public void save(File outputFile, boolean keepOriginal, boolean removeUnspecifiedFrames, boolean overwriteExisting) throws IOException, NotSupportedException, UnsupportedTagException, InvalidDataException, CannotReadException, CannotWriteException {
		// Check to make sure the file ends in .mp3
		if (!getExt(outputFile).equals(getExt(this.file)))
			outputFile = new File(outputFile.getAbsolutePath() + '.' + getExt(this.file));
		
		boolean replaceOriginal = false;
		if (!outputFile.equals(this.file)) {
			if (outputFile.exists()) {
				if (overwriteExisting) {
					outputFile.delete();
				} else {
					throw new IOException("File " + outputFile.getAbsolutePath() + " already exists");
				}
			}
		} else {
			replaceOriginal = true;
			if (isMp3)
				outputFile = File.createTempFile(Core.PROGRAM_NAME + "_", "", outputFile.getParentFile());
		}
		
		if (isMp3 && ((Settings.imagesEnabled()) || (Settings.getLibrary() == Settings.LIB_MP3MAGIC) || (Settings.getLibrary() == Settings.LIB_COMBINED))) {
			Mp3File mp3File = new Mp3File(this.file.getAbsolutePath());
			ID3v2 id3v2;
			
			if (mp3File.hasId3v2Tag()) {
				byte[] image;
				String mimeType;
				
				id3v2 = mp3File.getId3v2Tag();
				
				if (removeUnspecifiedFrames) {
					image = id3v2.getAlbumImage();
					mimeType = id3v2.getAlbumImageMimeType();
					
					id3v2 = new ID3v23Tag();
					mp3File.setId3v2Tag(id3v2);
					
					if (image != null)
						id3v2.setAlbumImage(image, mimeType);
				}
			} else {
				id3v2 = new ID3v23Tag();
				mp3File.setId3v2Tag(id3v2);
			}
			
			if (this.hasPendingTitle())
				id3v2.setTitle(pendingTitle);
			else if (this.hasTitle())
				id3v2.setTitle(title);
			else 
				id3v2.clearFrameSet(AbstractID3v2Tag.ID_TITLE);
			
			if (this.hasPendingArtist())
				id3v2.setArtist(pendingArtist);
			else if (this.hasArtist())
				id3v2.setArtist(artist);
			else 
				id3v2.clearFrameSet(AbstractID3v2Tag.ID_ARTIST);
			
			if (this.hasPendingAlbum())
				id3v2.setAlbum(pendingAlbum);
			else if (this.hasAlbum())
				id3v2.setAlbum(album);
			else 
				id3v2.clearFrameSet(AbstractID3v2Tag.ID_ALBUM);
			
			if (this.hasPendingGenre())
				id3v2.setGenre(pendingGenre);
			else if (this.hasGenre())
				id3v2.setGenre(genre);
			else 
				id3v2.clearFrameSet(AbstractID3v2Tag.ID_GENRE);
			
			if (this.hasPendingYear())
				id3v2.setYear(pendingYear);
			else if (this.hasYear())
				id3v2.setYear(year);
			else 
				id3v2.clearFrameSet(AbstractID3v2Tag.ID_YEAR);
			
			if (this.hasPendingComment())
				id3v2.setComment(pendingComment);
			else if (this.hasComment())
				id3v2.setComment(comment);
			else 
				id3v2.clearFrameSet(AbstractID3v2Tag.ID_COMMENT);
			
			if (this.hasPendingTrackNumber())
				id3v2.setTrack(pendingTrackNumber);
			else if (this.hasTrackNumber())
				id3v2.setTrack(trackNumber);
			else 
				id3v2.clearFrameSet(AbstractID3v2Tag.ID_TRACK);
		
			if (mp3File.hasId3v1Tag()) 
				mp3File.removeId3v1Tag();
			
			mp3File.save(outputFile.getAbsolutePath());
			
			if (replaceOriginal || !keepOriginal)
				this.file.delete();
			
			if (replaceOriginal)
				outputFile.renameTo(this.file);
			else
				this.file = outputFile;
			
			mp3File = new Mp3File(this.file.getAbsolutePath());
		} else {
			if (!replaceOriginal && keepOriginal)
				Files.copy(this.file.toPath(), outputFile.toPath());
			
			AudioFile audioFile;
			
			if (keepOriginal)
				audioFile = AudioFileIO.read(outputFile);
			else
				audioFile = AudioFileIO.read(this.file);
			
			if (removeUnspecifiedFrames)
				AudioFileIO.delete(audioFile);
			
			Tag tag = audioFile.getTag();
			
			if (this.hasPendingTitle())
				tag.setTitle(pendingTitle);
			else  if (this.hasTitle())
				tag.setTitle(title);

			if (this.hasPendingArtist())
				tag.setArtist(pendingArtist);
			else if (this.hasArtist())
				tag.setArtist(artist);
			
			if (this.hasPendingAlbum())
				tag.setAlbum(pendingAlbum);
			else  if (this.hasAlbum())
				tag.setAlbum(album);
			
			if (this.hasPendingGenre()) 
				tag.setGenre(pendingGenre);
			else  if (this.hasGenre())
				tag.setGenre(genre);
			
			if (this.hasPendingYear())
				tag.setYear(pendingYear);
			else  if (this.hasYear())
				tag.setYear(year);
			
			if (this.hasPendingComment())
				tag.setComment(pendingComment);
			else  if (this.hasComment())
				tag.setComment(comment);
			
			if (this.hasPendingTrackNumber())
				tag.setTrack(pendingTrackNumber);
			else  if (this.hasTrackNumber())
				tag.setTrack(trackNumber);


			AudioFileIO.write(audioFile);
			
			if (!keepOriginal)
				this.file.renameTo(outputFile);
			
			if (!replaceOriginal)
				this.file = outputFile;
		}
		
		this.fileName = this.file.getName();
		this.fileName = this.fileName.substring(0, this.fileName.lastIndexOf("."));
		
		if (this.hasPendingTitle()) {
			this.title = this.pendingTitle;
			this.pendingTitle = new String();
		}
		if (this.hasPendingArtist()) {
			this.artist = this.pendingArtist;
			this.pendingArtist = new String();
		}
		if (this.hasPendingAlbum()) {
			this.album = this.pendingAlbum;
			this.pendingAlbum = new String();
		}
		if (this.hasPendingGenre()) {
			this.genre = this.pendingGenre;
			this.pendingGenre = new String();
		}
		if (this.hasPendingYear()) {
			this.year = this.pendingYear;
			this.pendingYear = new String();
		}
		if (this.hasPendingComment()) {
			this.comment = this.pendingComment;
			this.pendingComment = new String();
		}
		if (this.hasPendingTrackNumber()) {
			this.trackNumber = this.pendingTrackNumber;
			this.pendingTrackNumber = new String();
		}	
	}
	
	public static boolean isValidExt(File file) {
		return isValidExt(file.getName());
	}
	
	public static boolean isValidExt(String filename) {
		switch(filename.substring(filename.lastIndexOf('.') + 1)) {
			case "mp3":
			case "ogg":
			//case "wav":
			case "wma":
			case "flac":
			case "ape":
			case "mpc":
				return true;
			default:
				return false;
		}
	}
	
	/** 
	 * Check to see if any of this song object's fields contain the text
	 * 
	 * @param text to search for
	 * @return true if found false if not
	 */
	public boolean containsText(String text) {
		text = text.toLowerCase();
		
		if (fileName.toLowerCase().contains(text)) return true;
		if (directoryName.toLowerCase().contains(text)) return true;
		if (title.toLowerCase().contains(text)) return true;
		if (artist.toLowerCase().contains(text)) return true;
		if (album.toLowerCase().contains(text)) return true;
		if (genre.toLowerCase().contains(text)) return true;
		if (year.toLowerCase().contains(text)) return true;
		if (comment.toLowerCase().contains(text)) return true;
		if (trackNumber.toLowerCase().contains(text)) return true;
		if (suggestedFormat.contains(text)) return true;
		if (pendingTitle.toLowerCase().contains(text)) return true;
		if (pendingArtist.toLowerCase().contains(text)) return true;
		if (pendingAlbum.toLowerCase().contains(text)) return true;
		if (pendingGenre.toLowerCase().contains(text)) return true;
		if (pendingYear.toLowerCase().contains(text)) return true;
		if (pendingComment.toLowerCase().contains(text)) return true;
		if (pendingTrackNumber.toLowerCase().contains(text)) return true;
		
		return false;
	}
	
	public boolean equals(Song song) {
		if (this.file.equals(song.file))
			return true;
		else
			return false;
	}
	
	public boolean equals(File file) {
		if (this.file.equals(file))
			return true;
		else
			return false;
	}
	
	private int compareString(String string1, String string2, int sortDirection) {
		if (string1.length() > 0 || string2.length() > 0) {
			if (string1.length() > 0 && string2.length() > 0) {
				return string1.compareToIgnoreCase(string2);
			} else if (string1.length() > 0) {
				return -1 * sortDirection;
			} else {
				return 1 * sortDirection;
			}
		} else {
			return 0;
		}
	}
	
	private int compareInt(String string1, String string2, int sortDirection) {
		if (string1.length() > 0 || string2.length() > 0) {
			if (string1.length() > 0 && string2.length() > 0) {
				int str1;
				int str2;
				
				try {
					str1 = new Integer(string1);
				} catch (Exception e) {
					str1 = 1000000;
				}
				
				try {
					str2 = new Integer(string2);
				} catch (Exception e) {
					str2 = 1000000;
				}
				
				if (str1 < str2)
					return -1;
				else if (str2 < str1)
					return 1;
				else
					return 0;
			} else if (string1.length() > 0) {
				return -1 * sortDirection;
			} else {
				return 1 * sortDirection;
			}
		} else {
			return 0;
		}
	}
	
	/**
	 * The character sequence represented by the fields in this song object is compared lexicographically to the character sequence represented by the argument song object. 
	 * The result is a negative integer if this song object lexicographically precedes the argument song. The result is a positive integer if this song object lexicographically 
	 * follows the argument song. The result is zero if the strings are equal; compareTo returns 0 exactly when the equals(Object) method would return true. The order the fields
	 * are compared in is specified by sortOrder.
	 * 
	 * @param The song object to compare to
	 * @return The value 0 if the argument song is equal to this song; a value less than 0 if this song is lexicographically less than the song argument; and a value greater than 0 if this song is lexicographically greater than the song argument.
	 */
	public int compareTo(Song song, int sortDirection) {
		if (!this.hasPendingChanges() && song.hasPendingChanges())
			return -1 * sortDirection;
		else if (this.hasPendingChanges() && !song.hasPendingChanges())
			return 1 * sortDirection;
		
		int result;
		for (int i = 0; i < this.sortOrder.length; i++) {
			switch (this.sortOrder[i]) {
				case FILENAME_SORT_ID:
					if ((result = compareString(this.fileName, song.getFileName(), sortDirection)) != 0)
						return result;
					break;
					
				case DIRECTORYNAME_SORT_ID:
					if ((result = compareString(this.directoryName, song.getDirectoryName(), sortDirection)) != 0)
						return result;
					break;
					
				case SUGGESTEDFORMAT_SORT_ID:
					if ((result = compareString(this.suggestedFormat, song.getSuggestedFormat(), sortDirection)) != 0)
						return result;
					break;
					
				case TITLE_SORT_ID:					
					if ((result = compareString(this.getDisplayTitle(), song.getDisplayTitle(), sortDirection)) != 0)
						return result;
					break;
					
				case ARTIST_SORT_ID:
					if ((result = compareString(this.getDisplayArtist(), song.getDisplayArtist(), sortDirection)) != 0)
						return result;
					break;
					
				case ALBUM_SORT_ID:
					if ((result = compareString(this.getDisplayAlbum(), song.getDisplayAlbum(), sortDirection)) != 0)
						return result;
					break;
					
				case GENRE_SORT_ID:
					if ((result = compareString(this.getDisplayGenre(), song.getDisplayGenre(), sortDirection)) != 0)
						return result;
					break;
					
				case YEAR_SORT_ID:
					if ((result = compareInt(this.getDisplayYear(), song.getDisplayYear(), sortDirection)) != 0)
						return result;
					break;
					
				case COMMENT_SORT_ID:
					if ((result = compareString(this.getDisplayComment(), song.getDisplayComment(), sortDirection)) != 0)
						return result;
					break;
					
				case TRACKNUMBER_SORT_ID:
					if ((result = compareInt(reduceTrackNum(this.getDisplayTrackNumber()), reduceTrackNum(song.getDisplayTrackNumber()), sortDirection)) != 0)
						return result;
					break;
			}
		}
		
		return 0;
	}
	
	private String reduceTrackNum(String trackNum) {
		if (trackNum.contains("/"))
			return trackNum.substring(0, trackNum.indexOf("/"));
		else if (trackNum.contains("\\"))
			return trackNum.substring(0, trackNum.indexOf("\\"));
		else
			return trackNum;
	}
	
	/** Generate the suggested name format for this song based off of the given string */
	private String generateSuggestedFormat(String string) {
		StringBuilder format = new StringBuilder();
		
		char[] buffer = string.toCharArray();
		
		boolean textSequence = false;
		for (int i = 0; i < buffer.length; i++) {
			int charType = getCharType(buffer[i]);
			
			if (charType == CHAR_TYPE_ALPHANUM) {
				if (!textSequence) {
					format.append('x');
					textSequence = true;
				}
			} else if (charType == CHAR_TYPE_WHITESPACE) {
				if (i + 1 < buffer.length) {
					if (getCharType(buffer[i + 1]) == CHAR_TYPE_DIVIDER)
						textSequence = false;
				}
				
				if (!textSequence)
					format.append(buffer[i]);
			} else if (charType == CHAR_TYPE_DIVIDER) {
				format.append(buffer[i]);
				textSequence = false;
			}
		}
		
		return format.toString();
	}
	
	/* Return an integer value corresponding to whether the input character is alphanum (in this case letter, digit, or a few other acceptable symbols), a whitespace, or a divider (any other character) */
	private int getCharType(char c) {
		
		if (Character.isLetterOrDigit(c))
			return CHAR_TYPE_ALPHANUM;
		
		switch (c) {
			case '!':
			case '@':
			case '#':
			case '$':
			case '%':
			case '^':
			case '&':
			case '\'':
			case '.':
			case ',':
			case '`':
				return CHAR_TYPE_ALPHANUM;
			default:
				break;
		}
		
		if (Character.isWhitespace(c))
			return CHAR_TYPE_WHITESPACE;
		
		return CHAR_TYPE_DIVIDER;
	}
	
	/** Return the string representation of this song object */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("file             = " + this.file.toString() + "\n");
		sb.append("fileName         = " + this.fileName + "\n");
		sb.append("directoryName    = " + this.directoryName + "\n");
		sb.append("title            = " + this.title + "\n");
		sb.append("artist           = " + this.artist + "\n");
		sb.append("album            = " + this.album + "\n");
		sb.append("genre            = " + this.genre + "\n");
		sb.append("year             = " + this.year + "\n");
		sb.append("comment          = " + this.comment + "\n");
		sb.append("image (length)   = ");
		
		return sb.toString();
	}
	
	private String getExt(File file) {
		return getExt(file.getName());
	}
	
	private String getExt(String name) {
		return name.substring(name.lastIndexOf(".") + 1, name.length());
	}
}