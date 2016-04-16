/**
 * ...
 * 
 * @author Brian Moody
 * @date 8/22/2012
 * @version 0.1
 */

package com.mp3t.ui.gui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

import com.mp3t.Core;
import com.mp3t.Settings;
import com.mp3t.Song;
import com.mp3t.SongLoader;
import com.mp3t.SongScanner;
import com.mp3t.Songs;
import com.mp3t.utils.CapitalizeString;

public class MainWindow {
	
	private static final int MIN_HEIGHT = 800;
	private static final int MIN_WIDTH = 1200;
	
	/* Maximum number of songs added to the list with every UI refresh */
	private static final int MAX_SONGS_PER_TICK = 3;
	
	private byte[] sortOrder = Song.DEFAULT_SORT_ORDER;
	
	public static final char TITLE_CHAR    = 't';
	public static final char ARTIST_CHAR   = 'a';
	public static final char ALBUM_CHAR    = 'A';
	public static final char GENRE_CHAR    = 'g';
	public static final char YEAR_CHAR     = 'y';
	public static final char COMMENT_CHAR  = 'c';
	public static final char TRACK_CHAR    = '#';
	public static final char NUM_CHAR	   = '*';
	
	public static final char WILDCARD_CHAR = '*';
	public static final char OPENING_CHAR  = '"';
	public static final char CLOSING_CHAR  = '"';
	public static final char FORMAT_CHAR   = '%';
	
	private final int NO_TAG_TYPES  = 0;
	private final int TYPE_TITLE    = 1;
	private final int TYPE_ARTIST   = 1 << 1;
	private final int TYPE_ALBUM    = 1 << 2;
	private final int TYPE_GENRE    = 1 << 3;
	private final int TYPE_YEAR     = 1 << 4;
	private final int TYPE_COMMENT  = 1 << 5;
	private final int TYPE_TRACK    = 1 << 6;
	private final int TYPE_WILDCARD = 1 << 7;
	
	private static final double FILENAME_RATIO 			= 0.1333;
	private static final double DIRECTORY_RATIO 		= 0.08;
	private static final double SUGGESTED_FORMAT_RATIO 	= 0.0733;
	private static final double TITLE_RATIO 			= 0.1333;
	private static final double ARTIST_RATIO 			= 0.1333;
	private static final double ALBUM_RATIO				= 0.1333;
	private static final double GENRE_RATIO				= 0.10;
	private static final double YEAR_RATIO 				= 0.04;
	private static final double COMMENT_RATIO 			= 0.1333;
	
	private static final String SETTINGS_FILE = "settings.dat";
	
	private int imageCellWidth;
	
	private Display display;
	private Core core;
	private SongLoader songLoader;
	
	private SaveProgressWindow saveProgressWindow;
	private boolean onlyShowErrors;
	
	private SongScanner songScanner;
	
	private Shell shell;
	
	private Table songTable;
	private DropTarget songTableDropTarget;
	
	private Menu rightClickMenu;
	private MenuItem removeSongsItem;
	private MenuItem removeAllSongsItem;
	private MenuItem copyFieldItem;
	private MenuItem clearFieldItem;
	private MenuItem clearPendingFieldItem;
	private MenuItem clearExistingItem;
	private MenuItem clearChangesItem;
	private MenuItem clearDataItem;
	private MenuItem playSongItem;
	private MenuItem browseFolderItem;
	private MenuItem deleteSongsItem;
	
	private TableColumn[] columns;
	
	private Button openFolderButton;
	private Button openFoldersButton;
	private Button openFilesButton;
	
	private StyledText searchField;
	
	private Composite statusComposite;
	
	private Button refreshButton;
	
	private Label songsLoadedLabel;
	private Label pendingChangesLabel;
	private Label errorLabel;
	
	private Group manualGroup;
	
	private GhostText titleField;
	private GhostText artistField;
	private GhostText albumField;
	private GhostText commentField;
	private GhostText yearField;
	private GhostText trackNumberField;
	private GhostText genreField;
	
	//private Button genreButton;
	
	private StyledText lastSelected1;
	private StyledText lastSelected2;
	
	private Button manualApplySelectedButton;
	private Button manualApplyAllButton;
	
	private Group automaticGroup;
	
	private Button scanSelectedButton;
	private Button scanAllButton;
	
	private StyledText formatField;
	
	private Button capitalizeButton;
	private Button capitalizeProperButton;
	private Button capitalizeProperButton2;
	private Button capitalizeAllButton;
	
	private Button automaticApplyAllButton;
	private Button automaticApplySelectedButton;
	
	private Group finalizeGroup;
	
	private Button newLocationButton;
	private Button deleteOriginalButton;
	//private Button removeFramesButton;
	private Button overwriteButton;
	
	private Label outputLocationLabel;
	private StyledText outputLocationField;
	
	private Button openOutputFolderButton;
	
	private Label outputFormatLabel;
	private StyledText outputFormatField;
	
	private Button applyChangesButton;
	private Button saveSelectedButton;
	private Button saveAllButton;
	
	private String version;
	private boolean notifyUpdates;
	private boolean hasUpdate;
	private boolean couldScan;
	
	private Rectangle userGuide;
	private Rectangle downloadNewVersion;
	
	private int numPendingChanges = 0;
	
	/* Used for copying songs from the newSong list retrieved from the core, songs are then copied from here and added to the table at a rate of MAX_SONGS_PER_TICK every refresh */
	private Object[][] buffer = null;
	
	/* The current index of the buffer */
    private int bufferIndex = 0;
    
    private int numSongs = 0;
    
    /* When set to true triggers a refresh, this causes items in the table to be overwritten and replaced rather than added to. Usually accompanied
     * by resetting the buffer to null and the bufferIndex to 0. It's also necessary to call refresh on the core to retrieve a new list of songs for
     * the table.
     */
    private boolean refresh = false;
    
    /* Used to keep track of whether the the string in searchField is growing or shrinking which affects how the table should update its contents */
    private int previousSearchFieldLength = 0;
    
    private int columnClicked = -1;
    
    private int selectionStartIndex = -1;
    private int selectionEndIndex = -1;
    
    private boolean songTableShowAlt = false;
    
    private boolean refreshingDisplayed = false;
    
    private static final String TABLEITEM_DATA_KEY = "Song";
    
    private Color cBlack;
    private Color cPendingChanges;
    private Color cError;
    private Color cNotScanned;
    private Color cQueued;
 

	public MainWindow(Display display, Core core, SongLoader songLoader, int imageCellWidth) {
		this.display = display;
		this.core = core;
		this.songLoader = songLoader;
		this.imageCellWidth = imageCellWidth;
		
		core.setDisplay(this.display);
		shell = new Shell(this.display);
		
		shell.setText(Core.PROGRAM_NAME + " - Version " + Core.PROGRAM_VERSION);
		
		try {
			final Image image = new Image(null, getClass().getResourceAsStream("img/icon.png"));
			shell.setImage(image);
		} catch (Exception e) {
			System.out.println(e.toString());
			System.exit(0);
		}
		
		/* Retrieve a list of songs to add to the table, list is frequently checked for new contents */
		LinkedList<Object[]> newSongs = core.getNewSongs();
		
		userGuide = new Rectangle(0, 0, 0, 0);
		downloadNewVersion = new Rectangle(0, 0, 0, 0);
		
		this.createWidgets();
		
		notifyUpdates = true;
		couldScan = true;
		
		loadSettings();
		
		this.addListeners();
		
		applyChangesButton.setEnabled(false);
		saveSelectedButton.setEnabled(false);
		saveAllButton.setEnabled(false);
		
		shell.pack();
		this.scaleAndCenter();
		
		shell.open();
		this.scaleSongTableWidgets();
		
		/* disable temporarily until new url for version
		try {
			fetchVersion();
			if (!version.equals(Core.PROGRAM_VERSION))
				hasUpdate = true;
			
			if (hasUpdate) {
				if (notifyUpdates) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setText(Core.PROGRAM_NAME);
					messageBox.setMessage("A new version is available, would you like to download it?");
					int result = messageBox.open();
					
					if (result == SWT.YES)
						Desktop.getDesktop().browse(new URI(Core.DOWNLOAD_URL));
					else
						notifyUpdates = false;
				}
			}
		} catch (Exception e) {
			if (Core.DEBUG)
				e.printStackTrace();
		}
		*/
		
		if (!Settings.canScan() && couldScan) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText(Core.PROGRAM_NAME);
			messageBox.setMessage("fpcalc is missing or not executable, automatic song lookup will be disabled");
			messageBox.open();
		}
			
		
	    cBlack = new Color(display, 0, 0, 0);
	    cPendingChanges = new Color(display, 255, 111, 41);
	    cError = new Color(display, 255, 55, 55);
	    cNotScanned = new Color(display, 85, 85, 85);
	    cQueued = new Color(display, 21, 86, 180);
		
		if (hasUpdate)
			songTable.redraw();
		
		/* Main program loop - keep going until the window's closed */
		while (!shell.isDisposed()) {
			/* If there's already songs in the buffer then add MAX_SONGS_PER_TICK to the table */
			if (buffer != null) {
				/* endNum is either going to be set to the current index + MAX_SONGS_PER_TICK or to the buffer's total length, whichever is less */
				int endNum = ((bufferIndex + MAX_SONGS_PER_TICK) < buffer.length) ? (bufferIndex + MAX_SONGS_PER_TICK) : buffer.length;
				
				for ( ; bufferIndex < endNum; bufferIndex++)
					addTableEntry(buffer[bufferIndex]);
				
				/* If we've reached the end of the buffer then reset it to null and reset the buffer index to 0 */
				if (bufferIndex == buffer.length) {
					buffer = null;
					bufferIndex = 0;
				}
				
				updateSongsLoadedLabel();
				display.update();
				
			/* Otherwise copy songs from the newSongs list into the buffer if there is any */
			} else {
				synchronized (newSongs) {
					if (newSongs.size() > 0) {
						
						buffer = new Object[newSongs.size()][2];
						
						for (int i = 0; i < newSongs.size(); i++) {
							buffer[i] = newSongs.get(i);
						}
	
						/* Clear the newSongs list after copying the songs out */
						newSongs.clear();
					}
				}
				
				if ((refreshingDisplayed && buffer == null) || (!refreshingDisplayed && buffer != null))
					updateErrorLabel(null, 0);
			}
			
			if (core.getNumSongs() != numSongs) {
				updateSongsLoadedLabel();
				numSongs = core.getNumSongs();
			}
			
			/* If the user added songs while the filter text was set redraw the background of songTable */
			if (core.getNumSongs() > 0) {
				if (searchField.getCharCount() > 0) {
					if (songTable.getItemCount() == 0)
						songTable.redraw();
				}
			}
			
			/* Update songs that are being scanned */
			if (songScanner != null) {
				Song[] changedSongs = songScanner.getChangedSongs();
				Song[] scannedSongs = songScanner.getScannedSongs();
				
				for (int i = 0; i < changedSongs.length; i++) {
					TableItem[] items = songTable.getItems();
					
					for (int o = 0; o < items.length; o++) {
						if ((Song) items[o].getData(TABLEITEM_DATA_KEY) == changedSongs[i]) {
							updateTableItem(items[o]);
							break;
						}
					}
				}
				
				for (int i = 0; i < scannedSongs.length; i++) {
					TableItem[] items = songTable.getItems();
					
					for (int o = 0; o < items.length; o++) {
						if ((Song) items[o].getData(TABLEITEM_DATA_KEY) == scannedSongs[i]) {
							updateTableItem(items[o]);
							break;
						}
					}
				}
				
				if (songScanner.getNumChanges() > 0) {
					numPendingChanges += songScanner.getNumChanges();
					songScanner.resetNumChanges();
					toggleApplyChangesButton();
					updatePendingChangesLabel();
				}
				
				if (songScanner.getNumSongsRemaining() == 0) {
					songScanner = null;
					
					toggleScanSelectedButton();
					toggleScanAllButton();
				}
				
				updateErrorLabel(null, 0);
			}
			
			display.update();
			
			/* If there's no commands for the GUI then sleep */
			if (!display.readAndDispatch())
				display.sleep();
		}	
	}
	
	/* Set the initial window size to SCREEN_RATIO percent of the total screen size and center it */
	private void scaleAndCenter() {
		final double SCREEN_RATIO = 0.80;
		
		Rectangle bounds = shell.getMonitor().getBounds();
		
		/* Set the width and height to half that of the monitor */
		int width = (int) Math.round(bounds.width * SCREEN_RATIO);
		int height = (int) Math.round(bounds.height * SCREEN_RATIO);
		
		width = (width > MIN_WIDTH) ? width : MIN_WIDTH;
		height = (height > MIN_HEIGHT) ? height : MIN_HEIGHT;
		
		/* Center the shell on the screen */
		shell.setBounds((bounds.width - width) / 2, (bounds.height - height) / 2, width, height);
		shell.setMinimumSize(MIN_WIDTH, MIN_HEIGHT);
	}
	
	/* Scale the columns so they take up the table's whole width */
	private void scaleSongTableWidgets() {
		songTable.setRedraw(false);
		
		Rectangle area = songTable.getClientArea();
		
		if (Settings.imagesEnabled())
			area.width -= imageCellWidth;
		
		int columnIndex = (Settings.imagesEnabled()) ? 1 : 0;
		
		columns[columnIndex++].setWidth((int) (area.width * FILENAME_RATIO));
		columns[columnIndex++].setWidth((int) (area.width * DIRECTORY_RATIO));
		columns[columnIndex++].setWidth((int) (area.width * SUGGESTED_FORMAT_RATIO));
		columns[columnIndex++].setWidth((int) (area.width * TITLE_RATIO));
		columns[columnIndex++].setWidth((int) (area.width * ARTIST_RATIO));
		columns[columnIndex++].setWidth((int) (area.width * ALBUM_RATIO));
		columns[columnIndex++].setWidth((int) (area.width * GENRE_RATIO));
		columns[columnIndex++].setWidth((int) (area.width * YEAR_RATIO));
		columns[columnIndex++].setWidth((int) (area.width * COMMENT_RATIO));
		
		columnIndex = (Settings.imagesEnabled()) ? 1 : 0;
		int currentWidth = 0;
		for (; columnIndex < columns.length - 1; columnIndex++)
			currentWidth += columns[columnIndex].getWidth();
		
		if (Settings.OS == Settings.LINUX)
			columns[columnIndex].setWidth(area.width - currentWidth - 25);
		else
			columns[columnIndex].setWidth(area.width - currentWidth);
		
		if (Settings.OS == Settings.LINUX) {
			TableItem items[] = songTable.getItems();
			for (int i = 0 ; i < items.length; i++)
				updateTableItem(items[i]);
		}
		
		songTable.setRedraw(true);
	}
	
	/* Create all of the window's components */
	private void createWidgets() {
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 8;
		gridLayout.marginHeight = 8;
		gridLayout.horizontalSpacing = 8;
		gridLayout.verticalSpacing = 8;
		shell.setLayout(gridLayout);
		
		songTable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL | SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
		songTable.setHeaderVisible(true);
		songTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1));
		
		rightClickMenu = new Menu(songTable);
		songTable.setMenu(rightClickMenu);
		
		removeSongsItem = new MenuItem(rightClickMenu, SWT.NONE);
		removeSongsItem.setText("Remove selected song(s)");
		removeAllSongsItem = new MenuItem(rightClickMenu, SWT.NONE);
		removeAllSongsItem.setText("Remove all songs");
		new MenuItem(rightClickMenu, SWT.SEPARATOR);
		copyFieldItem = new MenuItem(rightClickMenu, SWT.NONE);
		copyFieldItem.setText("Copy \"Title\" field of selected song");
		new MenuItem(rightClickMenu, SWT.SEPARATOR);
		clearFieldItem = new MenuItem(rightClickMenu, SWT.NONE);
		clearFieldItem.setText("Clear \"Title\" field for selected song(s)");
		clearPendingFieldItem = new MenuItem(rightClickMenu, SWT.NONE);
		clearPendingFieldItem.setText("Clear pending changes to \"Title\" field for selected song(s)");
		clearDataItem = new MenuItem(rightClickMenu, SWT.NONE);
		clearDataItem.setText("Clear all tag data for selected song(s)");
		clearExistingItem = new MenuItem(rightClickMenu, SWT.NONE);
		clearExistingItem.setText("Clear all stored tags for selected song(s)");
		clearChangesItem = new MenuItem(rightClickMenu, SWT.NONE);
		clearChangesItem.setText("Clear all pending changes for selected song(s)");
		new MenuItem(rightClickMenu, SWT.SEPARATOR);
		playSongItem = new MenuItem(rightClickMenu, SWT.NONE);
		playSongItem.setText("Play song");
		browseFolderItem = new MenuItem(rightClickMenu, SWT.NONE);
		browseFolderItem.setText("Browse folder");
		deleteSongsItem = new MenuItem(rightClickMenu, SWT.NONE);
		deleteSongsItem.setText("Delete song(s) from disk");
		
		songTableDropTarget = new DropTarget(songTable, DND.DROP_COPY | DND.DROP_MOVE);
		songTableDropTarget.setTransfer(new Transfer[] {FileTransfer.getInstance()});
		
		/* Create the table columns */
		if (Settings.imagesEnabled())
			columns = new TableColumn[11];
		else
			columns = new TableColumn[10];
		
		int columnIndex = 0;
		
		if (Settings.imagesEnabled()) {
			columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
			columns[columnIndex++].setText("Image");
		}
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Filename");
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Directory");
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Format");
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Title");
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Artist");
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Album");
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Genre");
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Year");
	
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Comment");
		
		columns[columnIndex] = new TableColumn(songTable, SWT.CENTER);
		columns[columnIndex++].setText("Track #");
		
		if (Settings.imagesEnabled())
			songTable.setSortColumn(columns[2]);
		else
			songTable.setSortColumn(columns[1]);
		songTable.setSortDirection(SWT.DOWN);
		core.setSortDirection(Songs.SORT_DESCENDING);
		core.setSortOrder(sortOrder);
		
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
			columns[i].setResizable(false);
		}
		
		if (Settings.imagesEnabled())
			columns[0].setWidth(imageCellWidth);
		
		Composite searchComposite = new Composite(shell, SWT.NONE);
		searchComposite.setLayout(new GridLayout(5, false));
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		try {
			final Image image1 = new Image(null, getClass().getResourceAsStream("img/folder_24.png"));
			final Image image2 = new Image(null, getClass().getResourceAsStream("img/folder_r_24.png"));
			final Image image3 = new Image(null, getClass().getResourceAsStream("img/mp3file_24.png"));
			
			openFolderButton = new Button(searchComposite, SWT.PUSH);
			openFolderButton.setImage(image1);
			openFolderButton.setToolTipText("Load a folder");
			
			openFoldersButton = new Button(searchComposite, SWT.PUSH);
			openFoldersButton.setImage(image2);
			openFoldersButton.setToolTipText("Load a folder (including subfolders)");
			
			openFilesButton = new Button(searchComposite, SWT.PUSH);
			openFilesButton.setImage(image3);
			openFilesButton.setToolTipText("Load individual audio file(s)");
		} catch (Exception e) {
			System.out.println(e.toString());
			System.exit(0);
		}
		
		Label searchLabel = new Label(searchComposite, SWT.NONE);
		searchLabel.setText(" Filter: ");
		searchLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		
		searchField = new StyledText(searchComposite, SWT.SINGLE | SWT.BORDER);
		searchField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		
		statusComposite = new Composite(shell, SWT.NONE);
		statusComposite.setLayout(new GridLayout(3, false));
		statusComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		try {
			final Image refresh = new Image(null, getClass().getResourceAsStream("img/refresh.png"));
			
			refreshButton = new Button(statusComposite, SWT.FLAT);
			refreshButton.setImage(refresh);
			refreshButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		songsLoadedLabel = new Label(statusComposite, SWT.NONE);
		songsLoadedLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		updateSongsLoadedLabel();
		
		pendingChangesLabel = new Label(statusComposite, SWT.NONE);
		pendingChangesLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		updatePendingChangesLabel();
		
		errorLabel = new Label(shell, SWT.NONE);
		errorLabel.setText("                                                                                                    ");
		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		errorLabel.setForeground(cError);
		
		statusComposite.pack();
		
		createManualGroup();
		createAutomaticGroup();
		createFinalizeGroup();
		
		lastSelected1 = titleField;
		lastSelected2 = outputFormatField;
		
		toggleApplySelectedButtons();
		toggleApplyAllButtons();
	}
	
	/* Create and layout the manual widget group */
	private void createManualGroup() {
		manualGroup = new Group(shell, SWT.NONE);
		manualGroup.setText("Manual");
		
		manualGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
		
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 8;
		gridLayout.marginHeight = 8;
		gridLayout.horizontalSpacing = 4;
		gridLayout.verticalSpacing = 4;
		manualGroup.setLayout(gridLayout);
		
		Label label;

		label = new Label(manualGroup, SWT.NONE);
		label.setText("Title: ");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		titleField = new GhostText(manualGroup, SWT.SINGLE | SWT.BORDER);
		titleField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

		label = new Label(manualGroup, SWT.NONE);
		label.setText("Artist: ");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		artistField = new GhostText(manualGroup, SWT.SINGLE | SWT.BORDER);
		artistField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		
		label = new Label(manualGroup, SWT.NONE);
		label.setText("Album: ");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		albumField = new GhostText(manualGroup, SWT.SINGLE | SWT.BORDER);
		albumField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

		label = new Label(manualGroup, SWT.NONE);
		label.setText("Comm: ");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		commentField = new GhostText(manualGroup, SWT.SINGLE | SWT.BORDER);
		commentField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		
		label = new Label(manualGroup, SWT.NONE);
		label.setText("Genre: ");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		Composite genreComposite = new Composite(manualGroup, SWT.NONE);
		genreComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		GridLayout genreLayout = new GridLayout(2, false);
		genreLayout.marginWidth = 0;
		genreLayout.marginHeight = 0;
		genreLayout.horizontalSpacing = 0;
		genreLayout.verticalSpacing = 0;
		genreComposite.setLayout(genreLayout);
		
		genreField = new GhostText(genreComposite, SWT.SINGLE | SWT.BORDER);
		genreField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		
		//TODO: Re-add genreButton
		/*
		genreButton = new Button(genreComposite, SWT.ARROW | SWT.LEFT);
		genreButton.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false, 1, 1));
		*/
		
		label = new Label(manualGroup, SWT.NONE);
		label.setText("Year: ");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		yearField = new GhostText(manualGroup, SWT.SINGLE | SWT.BORDER);
		yearField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		
		manualApplySelectedButton = new Button(manualGroup, SWT.PUSH);
		manualApplySelectedButton.setText("Apply to selected");
		manualApplySelectedButton.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1));
		
		label = new Label(manualGroup, SWT.NONE);
		label.setText("Track #: ");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		trackNumberField = new GhostText(manualGroup, SWT.SINGLE | SWT.BORDER);
		trackNumberField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		
		manualApplyAllButton = new Button(manualGroup, SWT.PUSH);
		manualApplyAllButton.setText("   Apply to all  ");
		manualApplyAllButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1));
		
		//TODO: Implement for genre button
		/*
		genreField.add("");
		Vector<String> genreList = new Vector<String>();
		for (int i = 0; i < Constants.genreIdToString.size(); i++)
			genreList.add(Constants.genreIdToString.get(i));
		Collections.sort(genreList);
		for (int i =0; i < genreList.size(); i++)
			genreField.add(genreList.get(i));
		*/
	
		manualGroup.pack();
	}
	
	/* Create and layout the manual automatic group */
	private void createAutomaticGroup() {
		automaticGroup = new Group(shell, SWT.NONE);
		automaticGroup.setText("Automatic");
		
		automaticGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 8;
		gridLayout.marginHeight = 8;
		gridLayout.horizontalSpacing = 4;
		gridLayout.verticalSpacing = 4;
		automaticGroup.setLayout(gridLayout);
		
		Label label;
		
		Composite scanComposite = new Composite(automaticGroup, SWT.NONE);
		scanComposite.setLayout(new GridLayout(7, true));
		scanComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		
		scanSelectedButton = new Button(scanComposite, SWT.PUSH);
		scanSelectedButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		scanSelectedButton.setText("Scan selected");
		
		label = new Label(scanComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		scanAllButton = new Button(scanComposite, SWT.PUSH);
		scanAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		scanAllButton.setText("    Scan all    ");
		
		toggleScanSelectedButton();
		toggleScanAllButton();
		
		Composite formatComposite = new Composite(automaticGroup, SWT.NONE);
		formatComposite.setLayout(new GridLayout(2, false));
		formatComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		label = new Label(formatComposite, SWT.NONE);
		label.setText("Format: ");
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		formatField = new StyledText(formatComposite, SWT.SINGLE | SWT.BORDER);
		formatField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		formatField.setToolTipText("" + FORMAT_CHAR + TITLE_CHAR    + " - Title\n" +
								        FORMAT_CHAR + ARTIST_CHAR   + " - Artist\n" +
								        FORMAT_CHAR + ALBUM_CHAR    + " - Album\n" +
								        FORMAT_CHAR + GENRE_CHAR    + " - Genre\n" +
								        FORMAT_CHAR + YEAR_CHAR     + " - Year\n" +
								        FORMAT_CHAR + COMMENT_CHAR  + " - Comment\n" +
								        FORMAT_CHAR + TRACK_CHAR    + " - Track number\n" +
								                      WILDCARD_CHAR + " - Wildcard");
		
		label = new Label(automaticGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		
		capitalizeButton = new Button(automaticGroup, SWT.CHECK);
		capitalizeButton.setText("Automatic capitalization");
		capitalizeButton.setLayoutData(new GridData(GridData.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		Composite capitalizeComposite = new Composite(automaticGroup, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 8;
		gridLayout.marginHeight = 0;
		capitalizeComposite.setLayout(gridLayout);
		capitalizeComposite.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false));
		
		final String example1 = "\"this is an example sentence\"";
		final String example2 = "\"THIS IS AN EXAMPLE SENTENCE\"";
		final String example3 = "\"NATO was formed on April 4, 1949\"";
		
		capitalizeProperButton = new Button(capitalizeComposite, SWT.RADIO);
		capitalizeProperButton.setText("Proper capitalization");
		capitalizeProperButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		capitalizeProperButton.setToolTipText("Follows proper english grammar conventions, but doesn't fix all caps\n\n" +
											  "Example:\n" +
											  "   " + example1 + " -> " + (new CapitalizeString(example1)).capitalize(CapitalizeString.CAPITALIZE_PROPER) + "\n" + 
											  "   " + example2 + " -> " + (new CapitalizeString(example2)).capitalize(CapitalizeString.CAPITALIZE_PROPER));
		
		capitalizeProperButton2 = new Button(capitalizeComposite, SWT.RADIO);
		capitalizeProperButton2.setText("Proper capitalization (Fix mixed case)");
		capitalizeProperButton2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		capitalizeProperButton2.setToolTipText("Follows proper english grammar conventions and corrects for excessive capitalization, but may impact acronyms\n\n" +
											   "Example:\n" +
				                               "   " + example1 + " -> " + (new CapitalizeString(example1)).capitalize(CapitalizeString.CAPITALIZE_PROPER | CapitalizeString.CAPITALIZE_LOWER_FIRST) + "\n" + 
				                               "   " + example2 + " -> " + (new CapitalizeString(example2)).capitalize(CapitalizeString.CAPITALIZE_PROPER | CapitalizeString.CAPITALIZE_LOWER_FIRST) + "\n" + 
				                               "   " + example3 + " -> " + (new CapitalizeString(example3)).capitalize(CapitalizeString.CAPITALIZE_PROPER | CapitalizeString.CAPITALIZE_LOWER_FIRST));
		
		capitalizeAllButton = new Button(capitalizeComposite, SWT.RADIO);
		capitalizeAllButton.setText("Capitalize every word");
		capitalizeAllButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		capitalizeAllButton.setToolTipText("Capitalize every word, but doesn't fix all caps\n\n" +
				  							  "Example:\n" +
				  							  "   " + example1 + " -> " + (new CapitalizeString(example1)).capitalize(CapitalizeString.CAPITALIZE_COMPLETE) + "\n" + 
				  							  "   " + example2 + " -> " + (new CapitalizeString(example2)).capitalize(CapitalizeString.CAPITALIZE_COMPLETE));
		
		Composite automaticApplyComposite = new Composite(automaticGroup, SWT.NONE);
		automaticApplyComposite.setLayout(new GridLayout(1, true));
		automaticApplyComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		
		automaticApplySelectedButton = new Button(automaticApplyComposite, SWT.PUSH);
		automaticApplySelectedButton.setText("Apply to selected");
		automaticApplySelectedButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		automaticApplyAllButton = new Button(automaticApplyComposite, SWT.PUSH);
		automaticApplyAllButton.setText("Apply to all");
		automaticApplyAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		automaticGroup.pack();
	}
	
	/* Create and layout the manual finalize group */
	private void createFinalizeGroup() {
		finalizeGroup = new Group(shell, SWT.NONE);
		finalizeGroup.setText("Save");
		
		finalizeGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 8;
		gridLayout.marginHeight = 8;
		gridLayout.horizontalSpacing = 4;
		gridLayout.verticalSpacing = 4;
		finalizeGroup.setLayout(gridLayout);
		
		newLocationButton = new Button(finalizeGroup, SWT.CHECK);
		newLocationButton.setText("Save to new location");
		newLocationButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		newLocationButton.setSelection(true);
		
		Label label;
		
		label = new Label(finalizeGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		
		Composite formatComposite = new Composite(finalizeGroup, SWT.NONE);
		gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		formatComposite.setLayout(gridLayout);
		formatComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 2));

		outputFormatLabel = new Label(formatComposite, SWT.NONE);
		outputFormatLabel.setText("Output format: ");
		outputFormatLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		outputFormatField = new StyledText(formatComposite, SWT.SINGLE | SWT.BORDER);
		outputFormatField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		outputFormatField.setToolTipText("" + FORMAT_CHAR + TITLE_CHAR    + " - Title\n" +
			                                  FORMAT_CHAR + ARTIST_CHAR   + " - Artist\n" +
			                                  FORMAT_CHAR + ALBUM_CHAR    + " - Album\n" +
			                                  FORMAT_CHAR + GENRE_CHAR    + " - Genre\n" +
			                                  FORMAT_CHAR + YEAR_CHAR     + " - Year\n" +
			                                  FORMAT_CHAR + COMMENT_CHAR  + " - Comment\n" +
			                                  FORMAT_CHAR + TRACK_CHAR    + " - Track number\n" +
			                                  					NUM_CHAR  + " - Sequential numbering\n" + 	
				                                                     "/ or \\ - Path separator");
		
		outputLocationLabel = new Label(formatComposite, SWT.NONE);
		outputLocationLabel.setText("Output location: ");
		outputLocationLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		outputLocationField = new StyledText(formatComposite, SWT.SINGLE | SWT.BORDER);
		outputLocationField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		
		try {
			final Image image1 = new Image(null, getClass().getResourceAsStream("img/folder_16.png"));
			
			openOutputFolderButton = new Button(formatComposite, SWT.PUSH);
			openOutputFolderButton.setImage(image1);
			openOutputFolderButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

		} catch (Exception e) {
			System.out.println(e.toString());
			System.exit(0);
		}
		
		Composite checkboxComposite = new Composite(finalizeGroup, SWT.NONE);
		gridLayout = new GridLayout(1, true);
		checkboxComposite.setLayout(gridLayout);
		checkboxComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		
		deleteOriginalButton = new Button(checkboxComposite, SWT.CHECK);
		deleteOriginalButton.setText("Delete original file");
		deleteOriginalButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		deleteOriginalButton.setToolTipText("After saving the new file, delete the input file.");
		
		overwriteButton = new Button(checkboxComposite, SWT.CHECK);
		overwriteButton.setText("Overwrite existing files");
		overwriteButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		overwriteButton.setToolTipText("If any files with the output name already exist, delete them before saving");
		
		//removeFramesButton = new Button(checkboxComposite, SWT.CHECK);
		//removeFramesButton.setText("Remove unnecessary tags from file");
		//removeFramesButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		//removeFramesButton.setToolTipText("If the file has any tags that aren't specified within this program they will be removed");
		
		Composite saveComposite = new Composite(finalizeGroup, SWT.NONE);
		gridLayout = new GridLayout(1, true);
		saveComposite.setLayout(gridLayout);
		saveComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		applyChangesButton = new Button(saveComposite, SWT.PUSH);
		applyChangesButton.setText("Save changes");
		applyChangesButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

		saveSelectedButton = new Button(saveComposite, SWT.PUSH);
		saveSelectedButton.setText("Save selected");
		saveSelectedButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		saveAllButton = new Button(saveComposite, SWT.PUSH);
		saveAllButton.setText("     Save all     ");
		saveAllButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

		finalizeGroup.pack();
	}
	
	/* Add listeners to the widgets */
	private void addListeners() {
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				saveSettings();
			}
		});
		
		/* Add functionality to the open folder button */
		openFolderButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {	
				try {
					songLoader.addLocation(new DirectoryDialog(shell).open(), false);
				} catch (Exception ex) {
					System.out.println(ex.toString());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		/* Add functionality to the open folders button */
		openFoldersButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {			
				try {
					songLoader.addLocation(new DirectoryDialog(shell).open(), true);
				} catch (Exception ex) {
					System.out.println(ex.toString());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		/* Add functionality to the open files button */
		openFilesButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {			
				FileDialog openFolderDialog = new FileDialog(shell, SWT.MULTI);
				
				String[] filterExtensions = {"*.mp3;*.ogg;*.flac;*.ape;*.mpc;*.wma"};
				//String[] filterExtensions = {"*.mp3"};
				
				openFolderDialog.setFilterExtensions(filterExtensions);
				String result = openFolderDialog.open();
				
				if (result != null) {
					String[] files = openFolderDialog.getFileNames();
					
					for (int i = 0; i < files.length; i++)
						files[i] = openFolderDialog.getFilterPath() + File.separatorChar + files[i];
					
					try {
						songLoader.addLocations(files, false);
					} catch (Exception ex) {
						if (Core.DEBUG) {
							System.out.println(ex.toString());
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		/* Add a modify listener to searchField, if the string contents change at all then update the song filter */
		searchField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				/* Set the user's view to the top of the table */
				if (songTable.getItemCount() > 0)
					songTable.showItem(songTable.getItem(0));
				
				/* Update the core's search text */
				core.setSearchText(searchField.getText());
				
				/* If we're shortening the field then refresh the song table since it means we're going to be potentially adding songs to it */
				if (searchField.getCharCount() < previousSearchFieldLength) {
					core.refresh();
					refreshSongTable();
					
					clearGhostFields();
				/* Otherwise just remove non-relevant items */
				} else {
					TableItem[] tableItems = songTable.getItems();

					int[] buffer = new int[tableItems.length];
					int bufferIndex = 0;
					
					for (int i = 0; i < tableItems.length; i++) {
						if (!((Song) tableItems[i].getData(TABLEITEM_DATA_KEY)).containsText(searchField.getText()))
						{
							buffer[bufferIndex] = i;
							bufferIndex++;
						}
						
					}
					
					int[] indicesToRemove = new int[bufferIndex];
					System.arraycopy(buffer, 0, indicesToRemove, 0, bufferIndex);
					
					songTable.remove(indicesToRemove);
					
					clearGhostFields();
					setGhostFields(songTable.getSelection());
				}

				previousSearchFieldLength = searchField.getText().length();
			}
		});
		
		songTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TableItem[] items = songTable.getSelection();
				if (items.length == 1) {
					Song song = (Song) items[0].getData(TABLEITEM_DATA_KEY);
					try {
						Desktop.getDesktop().open(song.getFile());
					} catch (IOException ex) {
						if (Core.DEBUG)
							ex.printStackTrace();
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				toggleApplySelectedButtons();
				toggleScanSelectedButton();
				
				TableItem[] items = songTable.getSelection();
				
				if (items.length > 0) {
					setGhostFields(items);
				}
				
				if (e.button == 3) {
					if (items.length > 0) {
						removeSongsItem.setEnabled(true);
						deleteSongsItem.setEnabled(true);
						
						if (items.length == 1) {
							browseFolderItem.setEnabled(true);
							playSongItem.setEnabled(true);
						} else {
							browseFolderItem.setEnabled(false);
							playSongItem.setEnabled(false);
						}

						
						boolean hasChanges = false;
						boolean hasExistingTags = false;
						for (int i = 0; i < items.length; i++) {
							Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
							
							if (song.hasPendingChanges())
								hasChanges = true;
							if (song.hasExistingTags())
								hasExistingTags = true;
							
							if (hasChanges && hasExistingTags)
								break;
						}
						
						if (hasChanges)
							clearChangesItem.setEnabled(true);
						else
							clearChangesItem.setEnabled(false);
						
						if (hasExistingTags)
							clearExistingItem.setEnabled(true);
						else
							clearExistingItem.setEnabled(false);
						
						if (hasChanges || hasExistingTags)
							clearDataItem.setEnabled(true);
						else
							clearDataItem.setEnabled(false);
					} else {
						removeSongsItem.setEnabled(false);
						clearDataItem.setEnabled(false);
						clearChangesItem.setEnabled(false);
						clearExistingItem.setEnabled(false);
						playSongItem.setEnabled(false);
						browseFolderItem.setEnabled(false);
						deleteSongsItem.setEnabled(false);
					}
					
					if (core.getNumSongs() > 0)
						removeAllSongsItem.setEnabled(true);
					else
						removeAllSongsItem.setEnabled(false);
				} else if (e.button == 1) {
					if (core.getNumSongs() == 0) {
						try {
							int x = 0;
							int y = 0;
							
							if (Settings.OS == Settings.WINDOWS) {
								x = e.x;
								y = e.y;
							} else {
								x = e.x;
								y = e.y + 20;
							}
							
							if (userGuide.contains(x, y)) {
								Desktop.getDesktop().browse(new URI(Core.USERGUIDE_URL));
							} else if (downloadNewVersion.contains(x, y)) {
								if (hasUpdate)
									Desktop.getDesktop().browse(new URI(Core.DOWNLOAD_URL));
							}
						} catch (Exception ex) {
							if (Core.DEBUG)
								ex.printStackTrace();
						}
					}
				}
				
				columnClicked = getColumn(e.x);
				toggleClearFieldItems();
				toggleCopyFieldItem();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
		
		songTable.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (core.getNumSongs() == 0) {
					int x = 0;
					int y = 0;
					
					if (Settings.OS == Settings.WINDOWS) {
						x = e.x;
						y = e.y;
					} else {
						x = e.x;
						y = e.y + 20;
					}
					
					if (userGuide.contains(x, y)) {
						shell.setCursor(new Cursor(display, SWT.CURSOR_HAND));
					} else if (downloadNewVersion.contains(x, y)) {
						if (hasUpdate)
							shell.setCursor(new Cursor(display, SWT.CURSOR_HAND));
						else
							shell.setCursor(null);
					} else {
						shell.setCursor(null);
					}
				} else {
					shell.setCursor(null);
				}
			}
		});
		
		/* Keep track of the table being resized so we can scale all the columns along with it */
		songTable.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				scaleSongTableWidgets();
			}

			@Override
			public void controlMoved(ControlEvent e) {
			}
		});
		
		songTable.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (core.getNumSongs() == 0) {
					paintTable(e);
				} else {
					if (songTable.getItemCount() == 0) {
						Rectangle bounds = songTable.getClientArea();
						e.gc.fillRectangle(bounds);
					}
				}
			}
		});
		
		/* Set the height of table rows */
		songTable.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				if (Settings.imagesEnabled())
					// height cannot be per row so simply set
					event.height = imageCellWidth;
				else
					event.height = Settings.ROW_HEIGHT;
			}
		});
	
		/* Select all songs in the list when the users presses ctrl + a */
		songTable.addKeyListener(new KeyAdapter()
		{	
				public void keyPressed(KeyEvent e)
				{
					switch (e.keyCode) {
						case 'a':
							if ((e.stateMask & SWT.CTRL) != 0) {
								songTable.selectAll();
								setGhostFields(songTable.getItems());
							}
							break;
						case SWT.DEL:
							if (songTable.getSelectionCount() > 0)
								removeSongs(songTable.getSelection());
							break;
						case SWT.ALT:
							songTableShowAlt = true;
							TableItem items[] = songTable.getItems();
							for (int i = 0 ; i < items.length; i++)
								updateTableItem(items[i]);
							break;
					}
				}
				
				public void keyReleased(KeyEvent e) {
					switch (e.keyCode){ 
						case SWT.ALT:
							songTableShowAlt = false;
							TableItem items[] = songTable.getItems();
							for (int i = 0 ; i < items.length; i++)
								updateTableItem(items[i]);
							break;
					}
				}
		});
		
		songTableDropTarget.addDropListener(new DropTargetListener() {
			
			public void dragEnter(DropTargetEvent event) {	
			}
			public void dragLeave(DropTargetEvent event) {
			}
			public void dragOperationChanged(DropTargetEvent event) {
			}
			public void dragOver(DropTargetEvent event) {
			}
			public void drop(DropTargetEvent event) {
				if (songTableDropTarget.getTransfer()[0].isSupportedType(event.currentDataType)) {
					String[] files = (String[]) event.data;			
					try {
						songLoader.addLocations(files, true);
					} catch (Exception ex) {
						if (Core.DEBUG)
							System.out.println(ex.toString());
					}
				}
			}
			public void dropAccept(DropTargetEvent event) {
			}
		});
		
		songTable.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {		
				if ((e.stateMask & SWT.SHIFT) != 0) {
					if (selectionStartIndex == -1)
						selectionStartIndex = 0;
					
					selectionEndIndex = songTable.indexOf((TableItem) e.item);
				} else {
					selectionStartIndex = songTable.indexOf((TableItem) e.item);
					selectionEndIndex = selectionStartIndex;
				}
				
				setGhostFields(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		
		/* Add listener to the column headers for sorting */
		Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				
				/* Set the user's view to the top of the table */
				if (songTable.getItemCount() > 0)
					songTable.showItem(songTable.getItem(0));
				
				TableColumn column = (TableColumn) e.widget;
				if (songTable.getSortColumn() == column) {
					if (songTable.getSortDirection() == SWT.UP) {
						songTable.setSortDirection(SWT.DOWN);
						core.setSortDirection(Songs.SORT_DESCENDING);
					} else {
						songTable.setSortDirection(SWT.UP);
						core.setSortDirection(Songs.SORT_ASCENDING);
					}
				} else {
					songTable.setSortColumn(column);
					songTable.setSortDirection(SWT.DOWN);
					core.setSortDirection(Songs.SORT_DESCENDING);
					
					setSortOrder(getColumnNum(column));

					core.setSortOrder(sortOrder);
				}
				
				core.refresh();
				refreshSongTable();
			}
		};
		
		int startIndex = (Settings.imagesEnabled()) ? 1 : 0;
		for (int i = startIndex; i < columns.length; i++) {
			columns[i].addListener(SWT.Selection, sortListener);
		}
		
		searchField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							searchField.selectAll();
					break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.ALT) != 0) {
							titleField.setFocus();
							titleField.setSelection(0, titleField.getCharCount());
							searchField.setSelection(0, 0);
						}
					break;
				}
			}
		});
		
		refreshButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {		
				core.refresh();
				refreshSongTable();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		titleField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							titleField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0)
							applyAllField(songTable.getItems(), TITLE_CHAR);
						else
							manualApplyActivated(songTable.getSelection());
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.ALT) != 0) {
							searchField.setFocus();
							searchField.setSelection(0, searchField.getCharCount());
							titleField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionUp();
							else
								moveSelectionUp();
							titleField.setText("");
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.ALT) != 0) {
							artistField.setFocus();
							artistField.setSelection(0, artistField.getCharCount());
							titleField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionDown();
							else
								moveSelectionDown();
							titleField.setText("");
						}
						break;
					case SWT.ARROW_RIGHT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							lastSelected1 = titleField;
							titleField.setSelection(0, 0);
						}
						break;
				}
			}
		});
		
		titleField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		artistField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							artistField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0)
							applyAllField(songTable.getItems(), ARTIST_CHAR);
						else
							manualApplyActivated(songTable.getSelection());
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.ALT) != 0) {
							titleField.setFocus();
							titleField.setSelection(0, titleField.getCharCount());
							artistField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionUp();
							else
								moveSelectionUp();
							artistField.setText("");
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.ALT) != 0) {
							albumField.setFocus();
							albumField.setSelection(0, albumField.getCharCount());
							artistField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionDown();
							else
								moveSelectionDown();
							artistField.setText("");
						}
						break;
					case SWT.ARROW_RIGHT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							artistField.setSelection(0, 0);
							lastSelected1 = artistField;
						}
						break;
				}
			}
		});
		
		artistField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		albumField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							albumField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0)
							applyAllField(songTable.getItems(), ALBUM_CHAR);
						else
							manualApplyActivated(songTable.getSelection());
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.ALT) != 0) {
							artistField.setFocus();
							artistField.setSelection(0, artistField.getCharCount());
							albumField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionUp();
							else
								moveSelectionUp();
							albumField.setText("");
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.ALT) != 0) {
							commentField.setFocus();
							commentField.setSelection(0, commentField.getCharCount());
							albumField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionDown();
							else
								moveSelectionDown();
							albumField.setText("");
						}
						break;
					case SWT.ARROW_RIGHT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							albumField.setSelection(0, 0);
							lastSelected1 = albumField;
						}
						break;
				}
			}
		});
		
		albumField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		genreField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		genreField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0)
							applyAllField(songTable.getItems(), GENRE_CHAR);
						else
							manualApplyActivated(songTable.getSelection());
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.ALT) != 0) {
							commentField.setFocus();
							commentField.setSelection(0, commentField.getCharCount());
							genreField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionUp();
							else
								moveSelectionUp();
							genreField.setText("");
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.ALT) != 0) {
							yearField.setFocus();
							yearField.setSelection(0, yearField.getCharCount());
							genreField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionDown();
							else
								moveSelectionDown();
							genreField.setText("");
						}
						break;
					case SWT.ARROW_RIGHT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							genreField.setSelection(0, 0);
							lastSelected1 = genreField;
						}
						break;
				}
			}
		});
		
		yearField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							yearField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0)
							applyAllField(songTable.getItems(), YEAR_CHAR);
						else
							manualApplyActivated(songTable.getSelection());
						break;
					case SWT.TAB:
						trackNumberField.setFocus();
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.ALT) != 0) {
							genreField.setFocus();
							genreField.setSelection(0, genreField.getCharCount());
							yearField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionUp();
							else
								moveSelectionUp();
							yearField.setText("");
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.ALT) != 0) {
							trackNumberField.setFocus();
							trackNumberField.setSelection(0, trackNumberField.getCharCount());
							yearField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionDown();
							else
								moveSelectionDown();
							yearField.setText("");
						}
						break;
					case SWT.ARROW_RIGHT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							yearField.setSelection(0, 0);
							lastSelected1 = yearField;
						}
						break;
				}
			}
		});
		
		yearField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		commentField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							commentField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0)
							applyAllField(songTable.getItems(), COMMENT_CHAR);
						else
							manualApplyActivated(songTable.getSelection());
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.ALT) != 0) {
							albumField.setFocus();
							albumField.setSelection(0, albumField.getCharCount());
							commentField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionUp();
							else
								moveSelectionUp();
							commentField.setText("");
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.ALT) != 0) {
							genreField.setFocus();
							genreField.setSelection(0, genreField.getCharCount());
							commentField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionDown();
							else
								moveSelectionDown();
							commentField.setText("");
						}
						break;
					case SWT.ARROW_RIGHT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							commentField.setSelection(0, 0);
							lastSelected1 = commentField;
						}
						break;
				}
			}
		});
		
		commentField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		trackNumberField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							trackNumberField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0)
							applyAllField(songTable.getItems(), TRACK_CHAR);
						else
							manualApplyActivated(songTable.getSelection());
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.ALT) != 0) {
							yearField.setFocus();
							yearField.setSelection(0, yearField.getCharCount());
							trackNumberField.setSelection(0, 0);
						} else if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionUp();
							else
								moveSelectionUp();
							trackNumberField.setText("");
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionDown();
							else
								moveSelectionDown();
							trackNumberField.setText("");
						}
						break;
					case SWT.ARROW_RIGHT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							trackNumberField.setSelection(0, 0);
							lastSelected1 = trackNumberField;
						}
						break;
				}
			}
		});
		
		trackNumberField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		manualApplySelectedButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {		
				manualApplyActivated(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		manualApplyAllButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				manualApplyActivated(songTable.getItems());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		scanSelectedButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				scanSongs(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		scanAllButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				scanSongs(songTable.getItems());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		formatField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							formatField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0)
							automaticApplyActivated(songTable.getItems());
						else
							automaticApplyActivated(songTable.getSelection());
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionUp();
							else
								moveSelectionUp();
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.CTRL) != 0) {
							if ((e.stateMask & SWT.SHIFT) != 0)
								addSelectionDown();
							else
								moveSelectionDown();
						}
						break;
					case SWT.ARROW_LEFT:
						if ((e.stateMask & SWT.ALT) != 0) {
							lastSelected1.setFocus();
							lastSelected1.setSelection(0, lastSelected1.getCharCount());
							formatField.setSelection(0, 0);
						}
						break;
					case SWT.ARROW_RIGHT:
						if ((e.stateMask & SWT.ALT) != 0) {
							lastSelected2.setFocus();
							lastSelected2.setSelection(0, lastSelected2.getCharCount());
							formatField.setSelection(0, 0);
						}
						break;
				}
			}
		});
		
		formatField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		outputFormatField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							outputFormatField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0) {
							if (saveAllButton.getEnabled()) {
								if (validateOutputLocation())
									saveSongs(songTable.getItems());
							}
						} else {
							if (applyChangesButton.getEnabled()) {
								if (validateOutputLocation())
									saveSongs(core.getPendingSongs());
							}
						}
						break;
					case SWT.ARROW_LEFT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							outputFormatField.setSelection(0, 0);
							lastSelected2 = outputFormatField;
						}
						break;
					case SWT.ARROW_DOWN:
						if ((e.stateMask & SWT.ALT) != 0) {
							outputLocationField.setFocus();
							outputLocationField.setSelection(0, outputLocationField.getCharCount());
							outputFormatField.setSelection(0, 0);
						}
						break;
				}
			}
		});
		
		outputFormatField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplyChangesButton();
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		outputLocationField.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					case 'a':
						if ((e.stateMask & SWT.CTRL) != 0)
							outputLocationField.selectAll();
					break;
					case SWT.CR:
						if ((e.stateMask & SWT.ALT) != 0) {
							if (saveAllButton.getEnabled()) {
								if (validateOutputLocation())
									saveSongs(songTable.getItems());
							}
						} else {
							if (applyChangesButton.getEnabled()) {
								if (validateOutputLocation())
									saveSongs(core.getPendingSongs());
							}
						}
						break;
					case SWT.ARROW_LEFT:
						if ((e.stateMask & SWT.ALT) != 0) {
							formatField.setFocus();
							formatField.setSelection(0, formatField.getCharCount());
							outputLocationField.setSelection(0, 0);
							lastSelected2 = outputLocationField;
						}
						break;
					case SWT.ARROW_UP:
						if ((e.stateMask & SWT.ALT) != 0) {
							outputFormatField.setFocus();
							outputFormatField.setSelection(0, outputFormatField.getCharCount());
							outputLocationField.setSelection(0, 0);
						}
						break;
				}
			}
		});
		
		outputLocationField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				toggleApplyChangesButton();
				toggleApplySelectedButtons();
				toggleApplyAllButtons();
			}
		});
		
		
		automaticApplySelectedButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selectedItems = songTable.getSelection();
				
				automaticApplyActivated(selectedItems);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		automaticApplyAllButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selectedItems = songTable.getItems();
				
				automaticApplyActivated(selectedItems);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		applyChangesButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (!newLocationButton.getSelection() || validateOutputLocation())
					saveSongs(core.getPendingSongs());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		saveSelectedButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (!newLocationButton.getSelection() || validateOutputLocation())
					saveSongs(songTable.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		saveAllButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (!newLocationButton.getSelection() || validateOutputLocation())
					saveSongs(songTable.getItems());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		capitalizeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				capitalizeButtonPressed();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		/* Add functionality to the open folder button */
		openOutputFolderButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {	
				try {
					DirectoryDialog dialog = new DirectoryDialog(shell);
					String folder = dialog.open();
					if (folder != null) {
						outputLocationField.setText(folder);
					}
				} catch (Exception ex) {
					System.out.println(ex.toString());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		newLocationButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				newLocationButtonPressed();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		removeSongsItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				removeSongs(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		removeAllSongsItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setText(Core.PROGRAM_NAME);
				messageBox.setMessage("Are you sure you want to remove all the songs?");
				int result = messageBox.open();
				
				if (result == SWT.YES)
					removeAllSongs();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		copyFieldItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				copyField(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		clearPendingFieldItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				clearPendingField(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		clearFieldItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				clearField(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		clearDataItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				clearData(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		clearChangesItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				clearChanges(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		clearExistingItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				clearExisting(songTable.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		playSongItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = songTable.getSelection();
			
				if (items.length == 1) {
					Song song = (Song) items[0].getData(TABLEITEM_DATA_KEY);
					try {
						Desktop.getDesktop().open(song.getFile());
					} catch (IOException ex) {
						if (Core.DEBUG)
							ex.printStackTrace();
					}
				} else {
					MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					dialog.setText(Core.PROGRAM_NAME);
					dialog.setMessage("Multiple songs selected");
					dialog.open(); 
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		browseFolderItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = songTable.getSelection();
			
				if (items.length == 1) {
					Song song = (Song) items[0].getData(TABLEITEM_DATA_KEY);
					try {
						Desktop.getDesktop().open(song.getFile().getParentFile());
					} catch (IOException ex) {
						if (Core.DEBUG)
							ex.printStackTrace();
					}
				} else {
					MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					dialog.setText(Core.PROGRAM_NAME);
					dialog.setMessage("Multiple songs selected");
					dialog.open(); 
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		deleteSongsItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = songTable.getSelection();
				
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setText(Core.PROGRAM_NAME);
				messageBox.setMessage("Are you sure you want to delete " + items.length + " song(s) from disk?");
				int result = messageBox.open();
				
				if (result == SWT.YES) {
					for (int i = 0; i < items.length; i++) {
						Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
						song.getFile().delete();
					}
					
					removeSongs(items);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
	}
	
	private void moveSelectionUp() {
		if (selectionEndIndex > 0) {
			selectionEndIndex--;
			selectionStartIndex = selectionEndIndex;
			
			songTable.setSelection(selectionEndIndex);
			setGhostFields(songTable.getSelection());
		} else {
			songTable.setSelection(0);
		}
		
		setGhostFields(songTable.getSelection());
	}
	
	private void moveSelectionDown() {
		if (selectionEndIndex < songTable.getItemCount() - 1) {
			selectionEndIndex++;
			selectionStartIndex = selectionEndIndex;
			
			songTable.setSelection(selectionEndIndex);
			setGhostFields(songTable.getSelection());
		} else {
			songTable.setSelection(songTable.getItemCount() - 1);
		}
		
		setGhostFields(songTable.getSelection());
	}
	
	private void addSelectionUp() {
		if (selectionEndIndex > 0) {
			selectionEndIndex--;
			
			if (selectionStartIndex < selectionEndIndex)
				songTable.setSelection(selectionStartIndex, selectionEndIndex);
			else if (selectionEndIndex < selectionStartIndex) 
				songTable.setSelection(selectionEndIndex, selectionStartIndex);
			else
				songTable.setSelection(selectionStartIndex);
			
			
			setGhostFields(songTable.getSelection());
			songTable.showItem(songTable.getItem(selectionEndIndex));
			
			setGhostFields(songTable.getSelection());
		}
	}
	
	private void addSelectionDown() {
		if (selectionEndIndex < songTable.getItemCount() - 1) {
			selectionEndIndex++; 
			
			if (selectionStartIndex < 0)
				selectionStartIndex = 0;
			
			if (selectionStartIndex < selectionEndIndex)
				songTable.setSelection(selectionStartIndex, selectionEndIndex);
			else if (selectionEndIndex < selectionStartIndex) 
				songTable.setSelection(selectionEndIndex, selectionStartIndex);
			else
				songTable.setSelection(selectionStartIndex);
			
			setGhostFields(songTable.getSelection());
			songTable.showItem(songTable.getItem(selectionEndIndex));
			
			setGhostFields(songTable.getSelection());
		}
	}
	
	private void paintTable(PaintEvent e) {	
		Rectangle bounds = songTable.getClientArea();
		
		e.gc.setAntialias(SWT.ON);
		e.gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		e.gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		
		Font font;
		if (Settings.OS == Settings.WINDOWS)
			font = new Font(display, "Nyala", 32, SWT.NONE);
		else
			font = new Font(display, "Arial", 32, SWT.NONE);
		
		e.gc.setFont(font);
		FontMetrics fm = e.gc.getFontMetrics();
		
		String caption = "Drag files/folders here to begin";
		int charHeight = fm.getHeight();
		int x = (int) Math.round(((double) (bounds.width - getLineWidth(caption, e.gc))) / 2.0);
		int y = (int) Math.round(((double) (bounds.height - charHeight)) / 2.0);
		e.gc.drawString(caption, x, y, true);
		font.dispose();
		
		final int OFFSET_X = 20;
		final int OFFSET_Y = -5;
		final int OFFSET_WIDTH = 10;
		
		e.gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
		
		font = new Font(display, "Arial", 12, SWT.NONE);
		e.gc.setFont(font);
		fm = e.gc.getFontMetrics();
		
		caption = "User guide";
		x = (int) Math.round(((double) (bounds.width - getLineWidth(caption, e.gc))) / 2.0) + OFFSET_X;
		y = y + charHeight + OFFSET_Y;
		userGuide = new Rectangle(x, y, getLineWidth(caption, e.gc) + OFFSET_WIDTH, fm.getHeight());
		
		TextLayout textLayout = new TextLayout(display);
		textLayout.setText(caption);
		textLayout.setFont(font);
		
		TextStyle textStyle = new TextStyle();
		textStyle.underline = true;
		textLayout.setStyle(textStyle, 0, caption.length());
		
		textLayout.draw(e.gc, x, y);
		
		font.dispose();
		
		if (hasUpdate) {
			final int OFFSET_X2 = 10;
			final int EDGE_OFFSET_Y = -4;
			final int EDGE_OFFSET_X = -16;
		
			font = new Font(display, "Arial", 10, SWT.NONE);
			e.gc.setFont(font);
			fm = e.gc.getFontMetrics();
			
			caption = "Download version " + version;
			x = bounds.width - getLineWidth(caption, e.gc) + EDGE_OFFSET_X;
			y = bounds.height - fm.getHeight() + EDGE_OFFSET_Y;
			downloadNewVersion = new Rectangle(x, y, getLineWidth(caption, e.gc) + OFFSET_X2, fm.getHeight());
			
			textLayout = new TextLayout(display);
			textLayout.setText(caption);
			textLayout.setFont(font);
			
			textStyle = new TextStyle();
			textStyle.underline = true;
			textLayout.setStyle(textStyle, 0, caption.length());
			
			textLayout.draw(e.gc, x, y);
			
			font.dispose();
		}
	}
	
	private int getLineWidth(String string, GC gc) {
		int width = 0;
		
		for (int i = 0; i < string.length(); i++) 
			width += gc.getCharWidth(string.charAt(i));
		
		return width;
	}
	
	/* Reset the buffer and buffer index, trigger refresh so songs currently in the table will be overwritten */
	private void refreshSongTable() {
		buffer = null;
	    bufferIndex = 0;
	    refresh = true;
	    
		selectionStartIndex = -1;
		selectionEndIndex = -1;
	}
	
	/* Object arrays from the buffer are passed here to be added to the table, they consist of two objects: A song object and an index of where it should be placed in the table */
	private void addTableEntry(Object[] newSong) {
		Song song = (Song) newSong[0];
		int index = ((Integer) newSong[1]).intValue();
		
		/* If the song doesn't contain the searchField text then do nothing, it's been filtered out */
		if (!song.containsText(searchField.getText()))
				return;
		
		/* If the index of the song being added is higher than the number of songs in the table set it to the last possible position */
		if (index > songTable.getItemCount())
			index = songTable.getItemCount();
		
		/* If we're refreshing then replace songs currently in the table rather than adding to them */
		if (refresh)
		{
			if (index < songTable.getItemCount())
				songTable.remove(index);
			/* We've replaced every song in the table, turn off refresh */
			else
				refresh = false;
		}
		
		TableItem item = new TableItem(songTable, SWT.NONE, index);
		
		/* Embed a reference to the song object in the table item */
		item.setData(TABLEITEM_DATA_KEY, song);
		
		if (Settings.imagesEnabled()) {
			if (song.hasThumbnail())
				item.setImage(0, song.getThumbnail());
		}
		
		updateTableItem(item);
		
		toggleApplyAllButtons();
		toggleScanAllButton();
	}
	
	private void scanSongs(TableItem[] selectedSongs) {
		if (selectedSongs.length > 0) {		
			Song[] songs = new Song[selectedSongs.length];
			
			for (int i = 0; i < selectedSongs.length; i++) {
				songs[i] = (Song) selectedSongs[i].getData(TABLEITEM_DATA_KEY);
				songs[i].setError(false);
			}
			
			songScanner = new SongScanner(songs, display);
			
			toggleScanSelectedButton();
			toggleScanAllButton();
			
			for (int i = 0; i < selectedSongs.length; i++)
				updateTableItem(selectedSongs[i]);
			
			(new Thread(songScanner)).start();
		}
	}
	
	private int getColumnNum(TableColumn column) {
		int startIndex = (Settings.imagesEnabled()) ? 1 : 0;
		
		for (int i = startIndex; i < columns.length; i++) {
			if (column == columns[i])
				return i;
		}
		
		return-1;
	}
	
	private void setSortOrder(int order) {
		order += (Settings.imagesEnabled()) ? 0 : 1;
		
		switch (order) {
			case 1:
				sortOrder = new byte[] {Song.FILENAME_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.GENRE_SORT_ID, Song.YEAR_SORT_ID, Song.COMMENT_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
			case 2:
				sortOrder = new byte[] {Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.GENRE_SORT_ID, Song.YEAR_SORT_ID, Song.COMMENT_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
			case 3:
				sortOrder = new byte[] {Song.SUGGESTEDFORMAT_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.FILENAME_SORT_ID, Song.GENRE_SORT_ID, Song.COMMENT_SORT_ID, Song.YEAR_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
			case 4:
				sortOrder = new byte[] {Song.TITLE_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.GENRE_SORT_ID, Song.YEAR_SORT_ID, Song.COMMENT_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
			case 5:
				sortOrder = new byte[] {Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.GENRE_SORT_ID, Song.YEAR_SORT_ID, Song.COMMENT_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
			case 6:
				sortOrder = new byte[] {Song.ALBUM_SORT_ID, Song.TRACKNUMBER_SORT_ID, Song.ARTIST_SORT_ID, Song.TITLE_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.GENRE_SORT_ID, Song.YEAR_SORT_ID, Song.COMMENT_SORT_ID};
				break;
			case 7:
				sortOrder = new byte[] {Song.GENRE_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.YEAR_SORT_ID, Song.COMMENT_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
			case 8:
				sortOrder = new byte[] {Song.YEAR_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.GENRE_SORT_ID, Song.COMMENT_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
			case 9:
				sortOrder = new byte[] {Song.COMMENT_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.GENRE_SORT_ID, Song.YEAR_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
			case 10:
				sortOrder = new byte[] {Song.TRACKNUMBER_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.GENRE_SORT_ID, Song.COMMENT_SORT_ID, Song.YEAR_SORT_ID};
				break;
			default:
				sortOrder = new byte[] {Song.DIRECTORYNAME_SORT_ID, Song.SUGGESTEDFORMAT_SORT_ID, Song.FILENAME_SORT_ID, Song.ARTIST_SORT_ID, Song.ALBUM_SORT_ID, Song.TITLE_SORT_ID, Song.GENRE_SORT_ID, Song.YEAR_SORT_ID, Song.COMMENT_SORT_ID, Song.TRACKNUMBER_SORT_ID};
				break;
		}
	}
	
	private void toggleApplyChangesButton() {
		if (newLocationButton.getSelection()) {
			if ((outputFormatField.getCharCount() > 0) && (outputLocationField.getCharCount() > 0)) {
				if (numPendingChanges > 0)
					applyChangesButton.setEnabled(true);
				else
					applyChangesButton.setEnabled(false);
			} else {
				applyChangesButton.setEnabled(false);
			}
		} else {
			if (numPendingChanges > 0)
				applyChangesButton.setEnabled(true);
			else
				applyChangesButton.setEnabled(false);
		}
	}
	
	private void toggleClearFieldItems() {
		int threshold = (Settings.imagesEnabled()) ? 4 : 3;
		
		if (columnClicked >= threshold) {
			String field = columns[columnClicked].getText();
			clearFieldItem.setText("Clear \"" + field + "\" field for selected song(s)");
			clearPendingFieldItem.setText("Clear pending changes to \"" + field +"\" field for selected song(s)");
			
			if (songTable.getSelectionCount() > 0) {
				if (hasPendingField(songTable.getSelection()))
					clearPendingFieldItem.setEnabled(true);
				else
					clearPendingFieldItem.setEnabled(false);
				
				if (hasField(songTable.getSelection()))
					clearFieldItem.setEnabled(true);
				else
					clearFieldItem.setEnabled(false);
			} else {
				clearPendingFieldItem.setEnabled(false);
				clearFieldItem.setEnabled(false);
			}
		} else {
			clearPendingFieldItem.setEnabled(false);
			clearFieldItem.setEnabled(false);
		}
	}
	
	private void toggleCopyFieldItem() {
		int threshold = (Settings.imagesEnabled()) ? 4 : 3;
		
		if (columnClicked != -1) {
			String field = columns[columnClicked].getText();
			copyFieldItem.setText("Copy \"" + field + "\" field of selected song");
			
			if (songTable.getSelectionCount() == 1) {
				if (columnClicked < threshold)
					copyFieldItem.setEnabled(true);
				else {
					if (hasPendingField(songTable.getSelection()) || hasField(songTable.getSelection()))
						copyFieldItem.setEnabled(true);
					else
						copyFieldItem.setEnabled(false);
				}
			} else {
				copyFieldItem.setEnabled(false);
			}
		} else {
			copyFieldItem.setEnabled(false);
		}
	}
	
	private boolean hasPendingField(TableItem[] items) {
		int columnIndex = (Settings.imagesEnabled()) ? columnClicked : columnClicked + 1;
		
		for (int i = 0; i < items.length; i++) {
			Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
			
			switch (columnIndex) {
				case 4:
					if (song.hasPendingTitle())
						return true;
					break;
				case 5:
					if (song.hasPendingArtist())
						return true;
					break;
				case 6:
					if (song.hasPendingAlbum())
						return true;
					break;
				case 7:
					if (song.hasPendingGenre())
						return true;
					break;
				case 8:
					if (song.hasPendingYear())
						return true;
					break;
				case 9:
					if (song.hasPendingComment())
						return true;
					break;
				case 10:
					if (song.hasPendingTrackNumber())
						return true;
					break;
			}
		}
		
		return false;
	}
	
	private boolean hasField(TableItem[] items) {
		int columnIndex = (Settings.imagesEnabled()) ? columnClicked : columnClicked + 1;
		
		for (int i = 0; i < items.length; i++) {
			Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
			
			switch (columnIndex) {
				case 4:
					if (song.hasTitle() || song.hasPendingTitle())
						return true;
					break;
				case 5:
					if (song.hasArtist() || song.hasPendingArtist())
						return true;
					break;
				case 6:
					if (song.hasAlbum() || song.hasPendingAlbum())
						return true;
					break;
				case 7:
					if (song.hasGenre() || song.hasPendingGenre())
						return true;
					break;
				case 8:
					if (song.hasYear() || song.hasPendingYear())
						return true;
					break;
				case 9:
					if (song.hasComment() || song.hasPendingComment())
						return true;
					break;
				case 10:
					if (song.hasTrackNumber() || song.hasPendingTrackNumber())
						return true;
					break;
			}
		}
		
		return false;
	}
	
	private void saveSongs(TableItem[] items) {
		LinkedList<Song> songs = new LinkedList<Song>();
		
		for (int i = 0; i < items.length; i++)
			songs.add((Song) items[i].getData(TABLEITEM_DATA_KEY));
		
		saveSongs(songs);
	}
	
	private void saveSongs(LinkedList<Song> songs) {
		if (newLocationButton.getSelection()) {
			String outputFormat = outputFormatField.getText();
			boolean tagAfterSeperator = false;
			boolean invalidTag = false;
			char invalidTagChar = ' ';
			
			for (int i = 0; i < outputFormat.length(); i++) {
				if (outputFormat.charAt(i) == FORMAT_CHAR) {
					tagAfterSeperator = true;
					if (i < outputFormat.length() - 1) {
						switch (outputFormat.charAt(i + 1)) {
							case TITLE_CHAR:
							case ARTIST_CHAR:
							case ALBUM_CHAR:
							case GENRE_CHAR:
							case YEAR_CHAR:
							case COMMENT_CHAR:
							case TRACK_CHAR:
								break;
							default:
								invalidTag = true;
								invalidTagChar = outputFormat.charAt(i + 1);
								break;
						}
					}
				} else if (outputFormat.charAt(i) == NUM_CHAR) {
					tagAfterSeperator = true;
				} else if (isSeperator(outputFormat.charAt(i))) {
					tagAfterSeperator = false;
				}
			}
			
			if (!tagAfterSeperator || invalidTag || outputFormat.charAt(outputFormat.length() - 1) == FORMAT_CHAR) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				dialog.setText("Unable to apply format");
				
				if (!tagAfterSeperator)
					dialog.setMessage("No tag specifier after the last slash in the output format");
				else if (invalidTag)
					dialog.setMessage("Unknown field type specified by " + MainWindow.FORMAT_CHAR + invalidTagChar);
				else
					dialog.setMessage("Invalid tag specifier");
				
				dialog.open(); 
				
				return;
			}
		}
		
		
		//saveProgressWindow = new SaveProgressWindow(shell, display, songs, newLocationButton.getSelection(), outputFormatField.getText(), outputLocationField.getText(), deleteOriginalButton.getSelection(), removeFramesButton.getSelection(), overwriteButton.getSelection(), onlyShowErrors);
		saveProgressWindow = new SaveProgressWindow(shell, display, songs, newLocationButton.getSelection(), outputFormatField.getText(), outputLocationField.getText(), deleteOriginalButton.getSelection(), false, overwriteButton.getSelection(), onlyShowErrors);

		
		saveProgressWindow.getShell().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				onlyShowErrors = saveProgressWindow.getShowErrors();
			}
		});
		
		LinkedList<Song> savedSongs = saveProgressWindow.open();
		
		if (savedSongs.size() > 0) {
			for (int i = 0; i < savedSongs.size(); i++) {
				
				TableItem[] items = songTable.getItems();
				
				for (int o = 0; o < items.length; o++) {
					if (items[o].getData(TABLEITEM_DATA_KEY) == savedSongs.get(i)) {
						songTable.remove(o);
						break;
					}
				}
				
				core.removeSong(savedSongs.get(i));
			}
			
			numPendingChanges = core.getNumPendingChanges();
			
			toggleApplyChangesButton();
			
			updatePendingChangesLabel();
			updateSongsLoadedLabel();
		}
		
		songTable.deselectAll();
		clearGhostFields();
	}
	
	private void applyAllField(TableItem[] selectedItems, char field) {
		for (int i = 0; i < selectedItems.length; i++) {
			Song currentItem = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
			
			boolean hasPendingChanges = false;
			if (currentItem.hasPendingChanges())
				hasPendingChanges = true;
			
			switch (field) {
				case TITLE_CHAR:
					if (titleField.getText().length() > 0) {
						if (!titleField.getText().equals(currentItem.getTitle()))
							currentItem.setPendingTitle(titleField.getText());
					} else {
						return;
					}
					break;
				
				case ARTIST_CHAR:
					if (artistField.getText().length() > 0) {
						if (!artistField.getText().equals(currentItem.getArtist()))
							currentItem.setPendingArtist(artistField.getText());
					} else {
						return;
					}
					break;
				
				case ALBUM_CHAR:
					if (albumField.getText().length() > 0) {
						if (!albumField.getText().equals(currentItem.getAlbum()))
							currentItem.setPendingAlbum(albumField.getText());
					} else {
						return;
					}
					break;
					
				case GENRE_CHAR:
					if (genreField.getText().length() > 0) {
						if (!genreField.getText().equals(currentItem.getGenre()))
							currentItem.setPendingGenre(genreField.getText());
					} else {
						return;
					}
					break;
				
				case YEAR_CHAR:
					if (yearField.getText().length() > 0) {
						if (!yearField.getText().equals(currentItem.getYear()))
							currentItem.setPendingYear(yearField.getText());
					} else {
						return;
					}
					break;
					
				case COMMENT_CHAR:
					if (commentField.getText().length() > 0) {
						if (!commentField.getText().equals(currentItem.getComment()))
							currentItem.setPendingComment(commentField.getText());
					} else {
						return;
					}
					break;
				
				case TRACK_CHAR:
					if (trackNumberField.getText().length() > 0) {
						if (!trackNumberField.getText().equals(currentItem.getTrackNumber()))
							currentItem.setPendingTrackNumber(trackNumberField.getText());
					} else {
						return;
					}
					break;
			}
			
			currentItem.setError(false);
			
			updateTableItem(selectedItems[i]);
			
			if (!hasPendingChanges) {
				if (currentItem.hasPendingChanges())
					numPendingChanges++;
			}
		}
		
		updatePendingChangesLabel();
		updateErrorLabel(new String(), 0);
		
		toggleApplyChangesButton();
	}
	
	private void manualApplyActivated(TableItem[] selectedItems) {
		for (int i = 0; i < selectedItems.length; i++) {
			Song currentItem = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
			
			boolean hasPendingChanges = false;
			if (currentItem.hasPendingChanges())
				hasPendingChanges = true;
			
			if (titleField.getText().length() > 0) {
				if (!titleField.getText().equals(currentItem.getTitle()))
					currentItem.setPendingTitle(titleField.getText());
			}
			
			if (artistField.getText().length() > 0) {
				if (!artistField.getText().equals(currentItem.getArtist()))
					currentItem.setPendingArtist(artistField.getText());
			}
			
			if (albumField.getText().length() > 0) {
				if (!albumField.getText().equals(currentItem.getAlbum()))
					currentItem.setPendingAlbum(albumField.getText());
			}
			
			if (genreField.getText().length() > 0) {
				if (!genreField.getText().equals(currentItem.getGenre()))
					currentItem.setPendingGenre(genreField.getText());
			}
			
			if (yearField.getText().length() > 0) {
				if (!yearField.getText().equals(currentItem.getYear()))
					currentItem.setPendingYear(yearField.getText());
			}
			
			if (commentField.getText().length() > 0) {
				if (!commentField.getText().equals(currentItem.getComment()))
					currentItem.setPendingComment(commentField.getText());
			}
			
			if (trackNumberField.getText().length() > 0) {
				if (!trackNumberField.getText().equals(currentItem.getTrackNumber()))
					currentItem.setPendingTrackNumber(trackNumberField.getText());
			}
			
			currentItem.setError(false);
			
			updateTableItem(selectedItems[i]);
			
			if (!hasPendingChanges) {
				if (currentItem.hasPendingChanges())
					numPendingChanges++;
			}
		}
		
		updatePendingChangesLabel();
		updateErrorLabel(new String(), 0);
		
		toggleApplyChangesButton();
	}
	
	private void automaticApplyActivated(TableItem[] selectedItems) {
		Song[] songs = new Song[selectedItems.length];
		
		for (int i = 0; i < selectedItems.length; i++) 
			songs[i] = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
		
		try {
			applyFormatToSongs(songs);
		} catch (Exception ex) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("Unable to apply format");
			dialog.setMessage(ex.getMessage());
			dialog.open(); 
		}
		
		for (int i = 0; i < selectedItems.length; i++)
			updateTableItem(selectedItems[i]);
		
		updatePendingChangesLabel();
		toggleApplyChangesButton();
		setGhostFields(songTable.getSelection());
	}
	
	private String getField(Song song, char field) {
		switch (field) {
			case TITLE_CHAR:
				if (song.hasPendingTitle())
					return song.getPendingTitle();
				else
					return song.getTitle();
			case ARTIST_CHAR:
				if (song.hasPendingArtist())
					return song.getPendingArtist();
				else
					return song.getArtist();
			case ALBUM_CHAR:
				if (song.hasPendingAlbum())
					return song.getPendingAlbum();
				else
					return song.getAlbum();
			case YEAR_CHAR:
				if (song.hasPendingYear())
					return song.getPendingYear();
				else
					return song.getYear();
			case TRACK_CHAR:
				if (song.hasPendingTrackNumber())
					return song.getPendingTrackNumber();
				else
					return song.getTrackNumber();
			case COMMENT_CHAR:
				if (song.hasPendingComment())
					return song.getPendingComment();
				else
					return song.getComment();
			case GENRE_CHAR:
				if (song.hasPendingGenre())
					return song.getPendingGenre();
				else
					return song.getGenre();
			default:
				return null;
				
		}
	}
	
	private void setGhostFields(TableItem[] selectedItems) {
		if (selectedItems.length > 0) {
			Song firstSong = (Song) selectedItems[0].getData(TABLEITEM_DATA_KEY);
			
			boolean fieldIsConsistent;
			
			fieldIsConsistent = true;
			for (int i = 1; i < selectedItems.length; i++) {
				Song song = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
				if (!getField(song, TITLE_CHAR).equals(getField(firstSong, TITLE_CHAR))) {
					fieldIsConsistent = false;
					break;
				}
			}
			
			if (!titleField.isFocusControl()) {
				if (fieldIsConsistent)
					titleField.activateGhostText(getField(firstSong, TITLE_CHAR));
				else
					titleField.activateGhostText(GhostText.INCONSISTENT_FIELDS);
			} else {
				if (fieldIsConsistent)
					titleField.setGhostText(getField(firstSong, TITLE_CHAR));
				else
					titleField.setGhostText(GhostText.INCONSISTENT_FIELDS);
			}
			
			fieldIsConsistent = true;
			for (int i = 1; i < selectedItems.length; i++) {
				Song song = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
				if (!getField(song, ARTIST_CHAR).equals(getField(firstSong, ARTIST_CHAR))) {
					fieldIsConsistent = false;
					break;
				}
			}
			
			if (!artistField.isFocusControl()) {
				if (fieldIsConsistent)
					artistField.activateGhostText(getField(firstSong, ARTIST_CHAR));
				else
					artistField.activateGhostText(GhostText.INCONSISTENT_FIELDS);
			} else {
				if (fieldIsConsistent)
					artistField.setGhostText(getField(firstSong, ARTIST_CHAR));
				else
					artistField.setGhostText(GhostText.INCONSISTENT_FIELDS);
			}
			
			fieldIsConsistent = true;
			for (int i = 1; i < selectedItems.length; i++) {
				Song song = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
				if (!getField(song, ALBUM_CHAR).equals(getField(firstSong, ALBUM_CHAR))) {
					fieldIsConsistent = false;
					break;
				}
			}
			
			if (!albumField.isFocusControl()) {
				if (fieldIsConsistent)
					albumField.activateGhostText(getField(firstSong, ALBUM_CHAR));
				else
					albumField.activateGhostText(GhostText.INCONSISTENT_FIELDS);
			} else {
				if (fieldIsConsistent)
					albumField.setGhostText(getField(firstSong, ALBUM_CHAR));
				else
					albumField.setGhostText(GhostText.INCONSISTENT_FIELDS);
			}
			
			fieldIsConsistent = true;
			for (int i = 1; i < selectedItems.length; i++) {
				Song song = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
				if (!getField(song, YEAR_CHAR).equals(getField(firstSong, YEAR_CHAR))) {
					fieldIsConsistent = false;
					break;
				}
			}
			
			if (!yearField.isFocusControl()) {
				if (fieldIsConsistent)
					yearField.activateGhostText(getField(firstSong, YEAR_CHAR));
				else
					yearField.activateGhostText(GhostText.INCONSISTENT_FIELDS);
			} else {
				if (fieldIsConsistent)
					yearField.setGhostText(getField(firstSong, YEAR_CHAR));
				else
					yearField.setGhostText(GhostText.INCONSISTENT_FIELDS);
			}
			
			fieldIsConsistent = true;
			for (int i = 1; i < selectedItems.length; i++) {
				Song song = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
				if (!getField(song, TRACK_CHAR).equals(getField(firstSong, TRACK_CHAR))) {
					fieldIsConsistent = false;
					break;
				}
			}
			
			if (!trackNumberField.isFocusControl()) {
				if (fieldIsConsistent)
					trackNumberField.activateGhostText(getField(firstSong, TRACK_CHAR));
				else
					trackNumberField.activateGhostText(GhostText.INCONSISTENT_FIELDS);
			} else {
				if (fieldIsConsistent)
					trackNumberField.setGhostText(getField(firstSong, TRACK_CHAR));
				else
					trackNumberField.setGhostText(GhostText.INCONSISTENT_FIELDS);
			}
			
			fieldIsConsistent = true;
			for (int i = 1; i < selectedItems.length; i++) {
				Song song = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
				if (!getField(song, COMMENT_CHAR).equals(getField(firstSong, COMMENT_CHAR))) {
					fieldIsConsistent = false;
					break;
				}
			}
			
			if (!commentField.isFocusControl()) {
				if (fieldIsConsistent)
					commentField.activateGhostText(getField(firstSong, COMMENT_CHAR));
				else
					commentField.activateGhostText(GhostText.INCONSISTENT_FIELDS);
			} else {
				if (fieldIsConsistent)
					commentField.setGhostText(getField(firstSong, COMMENT_CHAR));
				else
					commentField.setGhostText(GhostText.INCONSISTENT_FIELDS);
			}
			
			fieldIsConsistent = true;
			for (int i = 1; i < selectedItems.length; i++) {
				Song song = (Song) selectedItems[i].getData(TABLEITEM_DATA_KEY);
				if (!getField(song, GENRE_CHAR).equals(getField(firstSong, GENRE_CHAR))) {
					fieldIsConsistent = false;
					break;
				}
			}
			
			if (!genreField.isFocusControl()) {
				if (fieldIsConsistent)
					genreField.activateGhostText(getField(firstSong, GENRE_CHAR));
				else
					genreField.activateGhostText(GhostText.INCONSISTENT_FIELDS);
			} else {
				if (fieldIsConsistent)
					genreField.setGhostText(getField(firstSong, GENRE_CHAR));
				else
					genreField.setGhostText(GhostText.INCONSISTENT_FIELDS);
			}
		}
	}
	
	private void applyFormatToSongs(Song[] songs) throws Exception {
		int lastIndex = 0;
		int currentIndex = 0;
		boolean startsWithChunk = true;
		boolean endsWithChunk = true;
		LinkedList<Integer> tagTypes = new LinkedList<Integer>();
		LinkedList<String> textChunks = new LinkedList<String>();
		
		char[] format = formatField.getText().toCharArray();
		
		for ( ; currentIndex < format.length; currentIndex++) {
			if (format[currentIndex] == FORMAT_CHAR || format[currentIndex] == OPENING_CHAR || format[currentIndex] == WILDCARD_CHAR) {
				
				if (currentIndex == 0) {
					startsWithChunk = false;
				} else {
					if (lastIndex == currentIndex)
						throw new Exception("No constant group between tag groups");
					
					textChunks.add(String.copyValueOf(format, lastIndex, currentIndex - lastIndex));
				}
				
				int currentTags = NO_TAG_TYPES;
				
				if (format[currentIndex] == FORMAT_CHAR) {
					if (currentIndex + 1 < format.length)
						currentIndex++;
					else
						throw new Exception("String ends before end of tag");
					
					switch (format[currentIndex]) {
						case TITLE_CHAR:
							currentTags = TYPE_TITLE;
							break;
						case ARTIST_CHAR:
							currentTags = TYPE_ARTIST;
							break;
						case ALBUM_CHAR:
							currentTags = TYPE_ALBUM;
							break;
						case GENRE_CHAR:
							currentTags = TYPE_GENRE;
							break;
						case YEAR_CHAR:
							currentTags = TYPE_YEAR;
							break;
						case COMMENT_CHAR:
							currentTags = TYPE_COMMENT;
							break;
						case TRACK_CHAR:
							currentTags = TYPE_TRACK;
							break;
						default:
							throw new Exception("Invalid tag character specified");
					}
					
				} else if (format[currentIndex] == OPENING_CHAR) {
					if (currentIndex + 1 < format.length)
						currentIndex++;
					else
						throw new Exception("String ends before end of tag group");
					
					while (format[currentIndex] != CLOSING_CHAR) {
						switch (format[currentIndex]) {
							case TITLE_CHAR:
								currentTags |= TYPE_TITLE;
								break;
							case ARTIST_CHAR:
								currentTags |= TYPE_ARTIST;
								break;
							case ALBUM_CHAR:
								currentTags |= TYPE_ALBUM;
								break;
							case GENRE_CHAR:
								currentTags |= TYPE_GENRE;
								break;
							case YEAR_CHAR:
								currentTags |= TYPE_YEAR;
								break;
							case COMMENT_CHAR:
								currentTags |= TYPE_COMMENT;
								break;
							case TRACK_CHAR:
								currentTags |= TYPE_TRACK;
								break;
							default:
								throw new Exception("Invalid tag character '" + format[currentIndex] + "' in tag group");
						}
						
						if (currentIndex + 1 < format.length)
							currentIndex++;
						else
							throw new Exception("String ends before end of tag group");
					}
				} else {
					currentTags = TYPE_WILDCARD;
				}
				
				if (currentTags == NO_TAG_TYPES) {
					throw new Exception("No tags in tag group");
				} else {
					tagTypes.add(new Integer(currentTags));
				}
				
				lastIndex = currentIndex + 1;
			}
		}
		
		if (tagTypes.isEmpty())
			throw new Exception("No tags");
		
		if (lastIndex == currentIndex)
			endsWithChunk = false;
		else
			textChunks.add(String.copyValueOf(format, lastIndex, currentIndex - lastIndex));
		
		String fileNameOfFirstFailedSong = new String();
		int numFailures = 0;
		
		for (int i = 0; i < songs.length; i++) {
			try {
				boolean hasChanges = false;
				if (songs[i].hasPendingChanges())
					hasChanges = true;
				applyFormatToSong(songs[i], tagTypes, textChunks, startsWithChunk, endsWithChunk);
				if (!hasChanges)
					numPendingChanges++;
				
				songs[i].setError(false);
			} catch (Exception e) {
				if (numFailures == 0)
					fileNameOfFirstFailedSong = songs[i].getFileName();
				
				songs[i].setError(true);
				
				numFailures++;
				
				if (Core.DEBUG) System.out.println(e.toString() + " for song \"" + songs[i].getFileName() + "\"");
			}
		}	
		
		updateErrorLabel(fileNameOfFirstFailedSong, numFailures);
	}
	
	private void applyFormatToSong(Song song, LinkedList<Integer> tagTypes, LinkedList<String> textChunks, boolean startsWithChunk, boolean endsWithChunk) throws Exception {
		int textChunksIndex = 0;
		int tagTypesIndex = 0;
		int lastIndex = 0;
		int currentIndex = 0;
		
		String fileName = song.getFileName();
		
		if (startsWithChunk) {
			String textChunk = textChunks.get(textChunksIndex);
			
			if (fileName.length() > textChunk.length()) {
				if (textChunk.equals(fileName.substring(0, textChunk.length()))) {
					lastIndex = textChunk.length();
					currentIndex = textChunk.length();
				} else {
					throw new Exception("Invalid format; starting chunk doesn't match");
				}
			} else {
				throw new Exception("Invalid format; first chunk longer than entire filename");
			}
			textChunksIndex++;
		}
		
		String title       = null;
		String artist      = null;
		String album       = null;
		String genre       = null;
		String year        = null;
		String comment     = null;
		String trackNumber = null;
		
		char[] fileNameArray = fileName.toCharArray();
		char[] textChunkArray;
		
		for ( ; textChunksIndex < textChunks.size(); textChunksIndex++) {
			textChunkArray = textChunks.get(textChunksIndex).toCharArray();
			
			boolean textChunkFound = false;
			
			for ( ; currentIndex < fileNameArray.length; currentIndex++) {
				if (fileNameArray[currentIndex] == textChunkArray[0]) {
					boolean matchFound = true;
					
					if (currentIndex + textChunkArray.length <= fileNameArray.length) {
						for (int o = 0; o < textChunkArray.length; o++) {
							if (textChunkArray[o] != fileNameArray[currentIndex + o]) {
								matchFound = false;
								break;
							}
						}
					} else {
						throw new Exception("text chunk not found within filename");
					}
					
					if (matchFound) {
						
						int tags = tagTypes.get(tagTypesIndex).intValue();
						
						if ((tags & TYPE_TITLE) != NO_TAG_TYPES)
							title = fileName.substring(lastIndex, currentIndex);
						if ((tags & TYPE_ARTIST) != NO_TAG_TYPES)
							artist = fileName.substring(lastIndex, currentIndex);
						if ((tags & TYPE_ALBUM) != NO_TAG_TYPES)
							album = fileName.substring(lastIndex, currentIndex);
						if ((tags & TYPE_GENRE) != NO_TAG_TYPES)
							genre = fileName.substring(lastIndex, currentIndex);
						if ((tags & TYPE_YEAR) != NO_TAG_TYPES)
							year = fileName.substring(lastIndex, currentIndex);
						if ((tags & TYPE_COMMENT) != NO_TAG_TYPES)
							comment = fileName.substring(lastIndex, currentIndex);
						if ((tags & TYPE_TRACK) != NO_TAG_TYPES)
							trackNumber = fileName.substring(lastIndex, currentIndex);
						
						textChunkFound = true;
						tagTypesIndex++;
						currentIndex += textChunkArray.length;
						lastIndex = currentIndex;
						break;
					}
				}
			}
			
			if (!textChunkFound) {
				throw new Exception("Unable to find text chunk in filename");
			}
		}
		
		if (!endsWithChunk) {
			if (currentIndex < fileName.length()) {
				
				int tags = tagTypes.get(tagTypesIndex).intValue();
				
				if ((tags & TYPE_TITLE) != NO_TAG_TYPES)
					title = fileName.substring(currentIndex, fileName.length());
				if ((tags & TYPE_ARTIST) != NO_TAG_TYPES)
					artist = fileName.substring(currentIndex, fileName.length());
				if ((tags & TYPE_ALBUM) != NO_TAG_TYPES)
					album = fileName.substring(currentIndex, fileName.length());
				if ((tags & TYPE_GENRE) != NO_TAG_TYPES)
					genre = fileName.substring(currentIndex, fileName.length());
				if ((tags & TYPE_YEAR) != NO_TAG_TYPES)
					year = fileName.substring(currentIndex, fileName.length());
				if ((tags & TYPE_COMMENT) != NO_TAG_TYPES)
					comment = fileName.substring(currentIndex, fileName.length());
				if ((tags & TYPE_TRACK) != NO_TAG_TYPES)
					trackNumber = fileName.substring(currentIndex, fileName.length());
				
			} else {
				throw new Exception("Invalid Format");
			}
		}
		
		if (capitalizeButton.getSelection()) {
			int tags;
			
			if (capitalizeProperButton.getSelection())
				tags = CapitalizeString.CAPITALIZE_PROPER;
			else if (capitalizeProperButton2.getSelection())
				tags = (CapitalizeString.CAPITALIZE_PROPER | CapitalizeString.CAPITALIZE_LOWER_FIRST);
			else
				tags = CapitalizeString.CAPITALIZE_COMPLETE;
			
			if (title != null)
				title = (new CapitalizeString(title)).capitalize(tags);
			if (artist != null)
				artist = (new CapitalizeString(artist)).capitalize(tags);
			if (album != null)
				album = (new CapitalizeString(album)).capitalize(tags);
			if (genre != null)
				genre = (new CapitalizeString(genre)).capitalize(tags);
			if (year != null)
				year = (new CapitalizeString(year)).capitalize(tags);
			if (comment != null)
				comment = (new CapitalizeString(comment)).capitalize(tags);
			if (trackNumber != null)
				trackNumber = removeLeadingZeroes((new CapitalizeString(trackNumber)).capitalize(tags));
		}
		
		if (title != null)
			song.setPendingTitle(title);
		if (artist != null)
			song.setPendingArtist(artist);
		if (album != null)
			song.setPendingAlbum(album);
		if (genre != null)
			song.setPendingGenre(genre);
		if (year != null)
			song.setPendingYear(year);
		if (comment != null)
			song.setPendingComment(comment);
		if (trackNumber != null)
			song.setPendingTrackNumber(trackNumber);
	}
	
	private String removeLeadingZeroes(String num) {
		if (num.length() > 1) {
			for (int i = 0; i < num.length(); i++) {
				if (num.charAt(i) != '0')
					return num.substring(i, num.length());
			}
		}
		
		return num;
	}
	
	private String formatTableEntry(String text, int width) {
		if (Settings.OS == Settings.LINUX) {
			GC gc = new GC(songTable);
			int charWidth = gc.getFontMetrics().getAverageCharWidth() + 1;
			gc.dispose();
				
			if (width > 0) {
				if (charWidth * text.length() > width) {
					StringBuffer strBuff = new StringBuffer();
					
					int whitespaceIndex = -1;
					for (int i = 0; i < (width / charWidth); i++) {
						if (Character.isWhitespace(text.charAt(i)))
							whitespaceIndex = i;
					}
					
					for (int i = 0; i < text.length(); i++) {
						if (i == whitespaceIndex)
							strBuff.append('\n');
						else
							strBuff.append(text.charAt(i));
					}
					return strBuff.toString();
				}
			}
		}
		
		return text;
	}
	
	private void updateTableItem(TableItem item) {
		Song currentItem = (Song) item.getData(TABLEITEM_DATA_KEY);
		
		if (currentItem == null)
			return;
		
		String[] tableEntry = new String[(Settings.imagesEnabled()) ? 11 : 10];
		int columnIndex = (Settings.imagesEnabled()) ? 1 : 0;
		
		tableEntry[columnIndex] = formatTableEntry(currentItem.getFileName(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getDirectoryName(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getSuggestedFormat(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getTitle(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getArtist(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getAlbum(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getGenre(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getYear(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getComment(), columns[columnIndex++].getWidth());
		tableEntry[columnIndex] = formatTableEntry(currentItem.getTrackNumber(), columns[columnIndex++].getWidth());

		columnIndex = (Settings.imagesEnabled()) ? 1 : 0;
		
		for (; columnIndex < columns.length; columnIndex++)
			item.setForeground(columnIndex, cBlack);
		
		columnIndex = (Settings.imagesEnabled()) ? 1 : 0;
		
		if (currentItem.hasError()) 
			item.setForeground(columnIndex, cError);
		else if (currentItem.queuedToScan())
			item.setForeground(columnIndex, cQueued);
		else if (currentItem.hasPendingChanges())
			item.setForeground(columnIndex, cPendingChanges);
		else if (!currentItem.alreadyScanned())
			item.setForeground(columnIndex, cNotScanned);
		
		if (songTableShowAlt) {
			tableEntry[columnIndex + 3] = (currentItem.getTitle());
			item.setForeground(columnIndex + 3, cBlack);
		
			tableEntry[columnIndex + 4] = (currentItem.getArtist());
			item.setForeground(columnIndex + 4, cBlack);
		
			tableEntry[columnIndex + 5] = (currentItem.getAlbum());
			item.setForeground(columnIndex + 5, cBlack);
		
			tableEntry[columnIndex + 6] = (currentItem.getGenre());
			item.setForeground(columnIndex + 6, cBlack);
		
			tableEntry[columnIndex + 7] = (currentItem.getYear());
			item.setForeground(columnIndex + 7, cBlack);
		
			tableEntry[columnIndex + 8] = (currentItem.getComment());
			item.setForeground(columnIndex + 8, cBlack);
		
			tableEntry[columnIndex + 9] = (currentItem.getTrackNumber());
			item.setForeground(columnIndex + 9, cBlack);
				
		} else {
			if (currentItem.hasPendingTitle()) {
				tableEntry[columnIndex + 3] = (currentItem.getPendingTitle());
				item.setForeground(columnIndex + 3, cPendingChanges);
			}
			
			if (currentItem.hasPendingArtist()) {
				tableEntry[columnIndex + 4] = (currentItem.getPendingArtist());
				item.setForeground(columnIndex + 4, cPendingChanges);
			}
			
			if (currentItem.hasPendingAlbum()) {
				tableEntry[columnIndex + 5] = (currentItem.getPendingAlbum());
				item.setForeground(columnIndex + 5, cPendingChanges);
			}
			
			if (currentItem.hasPendingGenre()) {
				tableEntry[columnIndex + 6] = (currentItem.getPendingGenre());
				item.setForeground(columnIndex + 6, cPendingChanges);
			}
			
			if (currentItem.hasPendingYear()) {
				tableEntry[columnIndex + 7] = (currentItem.getPendingYear());
				item.setForeground(columnIndex + 7, cPendingChanges);
			}
			
			if (currentItem.hasPendingComment()) {
				tableEntry[columnIndex + 8] = (currentItem.getPendingComment());
				item.setForeground(columnIndex + 8, cPendingChanges);
			}
			
			if (currentItem.hasPendingTrackNumber()) {
				tableEntry[columnIndex + 9] = (currentItem.getPendingTrackNumber());
				item.setForeground(columnIndex + 9, cPendingChanges);
			}
		}
		
		item.setText(tableEntry);
	}
	
	private void updateSongsLoadedLabel() {
		int numSongsLoaded = core.getNumSongs();
		int totalNumSongs = core.getTotalNumSongs();
		int maxNumSongs = core.getMaxNumSongs();
		if (totalNumSongs <= maxNumSongs)
			songsLoadedLabel.setText("[" + numSongsLoaded + "/" + totalNumSongs + "] Songs loaded");
		else
			songsLoadedLabel.setText("[" + numSongsLoaded + "/" + maxNumSongs + " (" + (totalNumSongs - maxNumSongs) + ")] Songs loaded");
		statusComposite.layout();
		statusComposite.update();
	}
	
	private void updatePendingChangesLabel() {
		pendingChangesLabel.setText("[" + numPendingChanges + "] Songs with pending changes");
		statusComposite.layout();
		statusComposite.update();
	}
	
	private void updateErrorLabel(String fileName, int numFailures) {
		if (buffer != null) {
			errorLabel.setForeground(cBlack);
			
			errorLabel.setText("Refreshing...");
			
			refreshingDisplayed = true;
		} else if (songScanner != null) {
			errorLabel.setForeground(cQueued);
			
			errorLabel.setText("[" + songScanner.getNumSongsRemaining() + "] Songs queued to scan");
		} else if (numFailures > 0) {
			errorLabel.setForeground(cError);
			
			GC gc = new GC(errorLabel);
			int charWidth = gc.getFontMetrics().getAverageCharWidth();
			gc.dispose();
			
			int areaWidth = finalizeGroup.getSize().x;
			
			int numChars = areaWidth / charWidth;
			
			// To account for padding or some shit
			numChars = Math.round(((float) numChars) * 1.0f);
			
			if (numFailures > 1) {
				
				if ((numFailures - 1) >= 10000)
					numChars -= 5;
				else if ((numFailures - 1) >= 1000)
					numChars -= 4;
				else if ((numFailures - 1) >= 100)
					numChars -= 3;
				else if ((numFailures - 1) >= 10)
					numChars -= 2;
				else
					numChars -= 1;
				
				
				if (numFailures > 2)
					numChars -= 35;
				else
					numChars -= 24;
				
			} else if (numFailures == 1) {
				numChars -= 21;
			}
			
			if (fileName.length() > numChars) {
				fileName = fileName.substring(0, numChars - 3);
				fileName += "...";
			}
			
			if (numFailures > 2)
				errorLabel.setText("Invalid format for \"" + fileName + "\" and [" + (numFailures - 1) + "] others");
			else if (numFailures > 1)
				errorLabel.setText("Invalid format for \"" + fileName + "\" and [" + (numFailures - 1) + "] other");
			else if (numFailures == 1)
				errorLabel.setText("Invalid format for \"" + fileName + "\"");
		} else {
			errorLabel.setText("");
			refreshingDisplayed = false;
		}	
	}
	
	private void removeSongs(TableItem[] items) {
		Song[] songs = new Song[items.length];
		
		for (int i = 0; i < items.length; i++) {
			Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
			songs[i] = song;
			
			songTable.remove(songTable.indexOf(items[i]));
			
			if (song.hasPendingChanges())
				numPendingChanges--;
		}
		
		core.removeSongs(songs);
		
		updatePendingChangesLabel();
		updateSongsLoadedLabel();
		updateErrorLabel(new String(), 0);
		
		toggleApplySelectedButtons();
		toggleApplyAllButtons();
		toggleScanSelectedButton();
		toggleScanAllButton();
		toggleApplyChangesButton();
		
		if (songTable.getSelectionCount() == 0)
			clearGhostFields();
	}
	
	private void removeAllSongs() {
		core.removeAll();
		songTable.removeAll();
		
		buffer = null;
	    bufferIndex = 0;
	    numPendingChanges = 0;
		
		updatePendingChangesLabel();
		updateSongsLoadedLabel();
		updateErrorLabel(new String(), 0);
		
		toggleApplySelectedButtons();
		toggleApplyAllButtons();
		toggleScanSelectedButton();
		toggleScanAllButton();
		toggleApplyChangesButton();
		
		clearGhostFields();
	}
	
	private void clearGhostFields() {
		titleField.removeGhostText();
		artistField.removeGhostText();
		albumField.removeGhostText();
		trackNumberField.removeGhostText();
		yearField.removeGhostText();
		commentField.removeGhostText();
		
		selectionStartIndex = -1;
		selectionEndIndex = -1;
	}
	
	private void clearData(TableItem[] items) {
		for (int i = 0; i < items.length; i++) {
			Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
			
			if (song.hasPendingChanges())
				numPendingChanges--;
			
			song.clearExistingTags();
			song.clearPendingChanges();
			song.setError(false);
			updateTableItem(items[i]);
		}
		
		updatePendingChangesLabel();
		updateSongsLoadedLabel();
		updateErrorLabel(new String(), 0);
		
		toggleApplyChangesButton();
		
		setGhostFields(items);
	}
	
	private void clearExisting(TableItem[] items) {
		for (int i = 0; i < items.length; i++) {
			Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
			
			song.clearExistingTags();
			song.setError(false);
			updateTableItem(items[i]);
		}
		
		updateSongsLoadedLabel();
		updateErrorLabel(new String(), 0);
		
		setGhostFields(items);
	}
	
	private void clearChanges(TableItem[] items) {
		for (int i = 0; i < items.length; i++) {
			Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
			
			if (song.hasPendingChanges()) {
				song.clearPendingChanges();
				numPendingChanges--;
			}
			
			song.setError(false);
			updateTableItem(items[i]);
		}
		
		updatePendingChangesLabel();
		updateSongsLoadedLabel();
		updateErrorLabel(new String(), 0);
		
		toggleApplyChangesButton();
		
		setGhostFields(items);
	}
	
	private void clearPendingField(TableItem[] items) {
		int columnIndex = (Settings.imagesEnabled()) ? columnClicked : columnClicked + 1;
		
		for (int i = 0; i < items.length; i++) {
			Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
			
			boolean hasPendingChanges = song.hasPendingChanges();
			
			switch (columnIndex) {
				case 4:
					song.setPendingTitle(new String());
					break;
				case 5:
					song.setPendingArtist(new String());
					break;
				case 6:
					song.setPendingAlbum(new String());
					break;
				case 7:
					song.setPendingGenre(new String());
					break;
				case 8:
					song.setPendingYear(new String());
					break;
				case 9:
					song.setPendingComment(new String());
					break;
				case 10:
					song.setPendingTrackNumber(new String());
					break;
			}
			
			if (hasPendingChanges && !song.hasPendingChanges())
				numPendingChanges--;
			
			song.setError(false);
			updateTableItem(items[i]);
		}
		
		updatePendingChangesLabel();
		updateSongsLoadedLabel();
		updateErrorLabel(new String(), 0);
		
		toggleApplyChangesButton();
		
		setGhostFields(items);
	}
	
	private void copyField(TableItem[] items) {
		int columnIndex = (Settings.imagesEnabled()) ? columnClicked : columnClicked + 1;
		
		if (items.length == 1) {
			Song song = (Song) items[0].getData(TABLEITEM_DATA_KEY);
			
			String textData = "";
			
			if (columnIndex < 4) {
				switch (columnIndex) {
					case 1:
						textData = song.getFileName();
						break;
					case 2:
						textData = song.getDirectoryName();
						break;
					case 3:
						textData = song.getSuggestedFormat();
						break;
				}
			} else {
				switch (columnIndex) {
					case 4:
						if (song.hasPendingTitle())
							textData = song.getPendingTitle();
						else
							textData = song.getTitle();
						break;
					case 5:
						if (song.hasPendingArtist())
							textData = song.getPendingArtist();
						else
							textData = song.getArtist();
						break;
					case 6:
						if (song.hasPendingAlbum())
							textData = song.getPendingAlbum();
						else
							textData = song.getAlbum();
						break;
					case 7:
						if (song.hasPendingGenre())
							textData = song.getPendingGenre();
						else
							textData = song.getGenre();
						break;
					case 8:
						if (song.hasPendingYear())
							textData = song.getPendingYear();
						else
							textData = song.getYear();
						break;
					case 9:
						if (song.hasPendingComment())
							textData = song.getPendingComment();
						else
							textData = song.getComment();
						break;
					case 10:
						if (song.hasPendingTrackNumber())
							textData = song.getPendingTrackNumber();
						else
							textData = song.getTrackNumber();
						break;
				}
			}
			
	        Clipboard clipboard = new Clipboard(display);
	        TextTransfer textTransfer = TextTransfer.getInstance();
	        Transfer[] transfers = new Transfer[]{textTransfer};
	        Object[] data = new Object[]{textData};
	        clipboard.setContents(data, transfers);
	        clipboard.dispose();
		}
	}
	
	private void clearField(TableItem[] items) {
		int columnIndex = (Settings.imagesEnabled()) ? columnClicked : columnClicked + 1;
		
		for (int i = 0; i < items.length; i++) {
			Song song = (Song) items[i].getData(TABLEITEM_DATA_KEY);
			
			boolean hasPendingChanges = song.hasPendingChanges();
			
			switch (columnIndex) {
				case 4:
					song.setTitle(new String());
					song.setPendingTitle(new String());
					break;
				case 5:
					song.setArtist(new String());
					song.setPendingArtist(new String());
					break;
				case 6:
					song.setAlbum(new String());
					song.setPendingAlbum(new String());
					break;
				case 7:
					song.setGenre(new String());
					song.setPendingGenre(new String());
					break;
				case 8:
					song.setYear(new String());
					song.setPendingYear(new String());
					break;
				case 9:
					song.setComment(new String());
					song.setPendingComment(new String());
					break;
				case 10:
					song.setTrackNumber(new String());
					song.setPendingTrackNumber(new String());
					break;
			}
			
			if (hasPendingChanges && !song.hasPendingChanges())
				numPendingChanges--;
			
			song.setError(false);
			updateTableItem(items[i]);
		}
		
		updatePendingChangesLabel();
		updateSongsLoadedLabel();
		updateErrorLabel(new String(), 0);
		
		toggleApplyChangesButton();
		
		setGhostFields(items);
	}
	
	private int getColumn(int x) {
		int left = (Settings.imagesEnabled()) ? columns[0].getWidth() : 0;
		int startIndex = (Settings.imagesEnabled()) ? 1 : 0;
		
		for (int i = startIndex; i < columns.length; i++) {
			int right = left + columns[i].getWidth();
			if (x >= left && x < right)
				return i;
			else
				left = right;
		}
		
		return -1;
	}
	
	private void toggleApplyAllButtons() {
		if (songTable.getItemCount() > 0) {
			if (manualFieldsHaveChars())
				manualApplyAllButton.setEnabled(true);
			else
				manualApplyAllButton.setEnabled(false);
			
			if (formatField.getCharCount() > 0)
				automaticApplyAllButton.setEnabled(true);
			else
				automaticApplyAllButton.setEnabled(false);
			
			if ((outputFormatField.getCharCount() > 0 && outputLocationField.getCharCount() > 0) || !newLocationButton.getSelection())
				saveAllButton.setEnabled(true);
			else
				saveAllButton.setEnabled(false);
		} else {
			manualApplyAllButton.setEnabled(false);
			automaticApplyAllButton.setEnabled(false);
			saveAllButton.setEnabled(false);
		}
	}
	
	private void toggleApplySelectedButtons() {
		if (songTable.getSelectionCount() > 0) {
			if (manualFieldsHaveChars())
				manualApplySelectedButton.setEnabled(true);
			else
				manualApplySelectedButton.setEnabled(false);
			
			if (formatField.getCharCount() > 0)
				automaticApplySelectedButton.setEnabled(true);
			else
				automaticApplySelectedButton.setEnabled(false);
			
			if ((outputFormatField.getCharCount() > 0 && outputLocationField.getCharCount() > 0) || !newLocationButton.getSelection())
				saveSelectedButton.setEnabled(true);
			else
				saveSelectedButton.setEnabled(false);
		} else {
			manualApplySelectedButton.setEnabled(false);
			automaticApplySelectedButton.setEnabled(false);
			saveSelectedButton.setEnabled(false);
		}
	}
	
	private void toggleScanAllButton() {
		if (songTable.getItemCount() > 0) {
			if (Settings.canScan() && songScanner == null) 
				scanAllButton.setEnabled(true);
			else
				scanAllButton.setEnabled(false);
		} else {
			scanAllButton.setEnabled(false);
		}
	}
	
	private void toggleScanSelectedButton() {
		if (songTable.getSelectionCount() > 0) {
			if (Settings.canScan() && songScanner == null) 
				scanSelectedButton.setEnabled(true);
			else
				scanSelectedButton.setEnabled(false);
		} else {
			scanSelectedButton.setEnabled(false);
		}
	}
	
	private boolean manualFieldsHaveChars() {
		if (titleField.getText().length() > 0)
			return true;
		if (artistField.getText().length() > 0)
			return true;
		if (albumField.getText().length() > 0)
			return true;
		if (genreField.getText().length() > 0)
			return true;
		if (yearField.getText().length() > 0)
			return true;
		if (commentField.getText().length() > 0)
			return true;
		if (trackNumberField.getText().length() > 0)
			return true;
		
		return false;
	}

	private boolean validateOutputLocation() {
		if ((new File(outputLocationField.getText())).exists()) {
			return true;
		} else {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("Error");
			dialog.setMessage("Invalid output location");
			dialog.open(); 
			
			return false;
		}
	}
	
	private void newLocationButtonPressed() {
		Color color;
		
		if (newLocationButton.getSelection()) {
			outputFormatField.setEnabled(true);
			outputFormatLabel.setEnabled(true);
			outputLocationField.setEnabled(true);
			outputLocationLabel.setEnabled(true);
			openOutputFolderButton.setEnabled(true);
			deleteOriginalButton.setEnabled(true);
			overwriteButton.setEnabled(true);
			
			color = new Color(display, GhostText.normalForegroundRGB);
			outputFormatField.setForeground(color);
			outputLocationField.setForeground(color);
		} else {
			outputFormatField.setEnabled(false);
			outputFormatLabel.setEnabled(false);
			outputLocationField.setEnabled(false);
			outputLocationLabel.setEnabled(false);
			openOutputFolderButton.setEnabled(false);
			deleteOriginalButton.setEnabled(false);
			overwriteButton.setEnabled(false);
			
			color = new Color(display, GhostText.ghostForegroundRGB);
			outputFormatField.setForeground(color);
			outputLocationField.setForeground(color);
		}
	}
	
	private void capitalizeButtonPressed() {
		if (capitalizeButton.getSelection()) {
			capitalizeProperButton.setEnabled(true);
			capitalizeProperButton2.setEnabled(true);
			capitalizeAllButton.setEnabled(true);
			
			if (!capitalizeProperButton.getSelection() && !capitalizeProperButton2.getSelection() && !capitalizeAllButton.getSelection())
				capitalizeProperButton.setSelection(true);
		} else {
			capitalizeProperButton.setEnabled(false);
			capitalizeProperButton2.setEnabled(false);
			capitalizeAllButton.setEnabled(false);
		}
	}
	
	private void fetchVersion() throws Exception {
		URL url = new URL(Core.VERSION_URL);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        
        version = in.readLine();
        in.close();
	}
	
	/**
	 * 
	 */
	private void loadSettings() {
		//String path = Core.HOME_PATH + SETTINGS_FILE;
		String path = SETTINGS_FILE;
		
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(path));
			
			String version;
			if (dis.readUnsignedByte() == 0xA5) {
				char[] buff = new char[5];
				for (int i = 0; i < 5; i++)
					buff[i] = dis.readChar();
				version = new String(buff);
			} else {
				version = "00.90";
				dis.close();
				dis = new DataInputStream(new FileInputStream(path));
			}
			
			switch (version) {
				case "01.00":
					couldScan = dis.readBoolean();
				case "00.90":
					notifyUpdates = dis.readBoolean();
					newLocationButton.setSelection(dis.readBoolean());
					deleteOriginalButton.setSelection(dis.readBoolean());
					overwriteButton.setSelection(dis.readBoolean());
					//removeFramesButton.setSelection(dis.readBoolean());
					dis.readBoolean();
					capitalizeButton.setSelection(dis.readBoolean());
					
					int capitalizeSelection = dis.readInt();
					switch (capitalizeSelection) {
						case 0:
							capitalizeProperButton.setSelection(true);
							break;
						case 1:
							capitalizeProperButton2.setSelection(true);
							break;
						case 2:
							capitalizeAllButton.setSelection(true);
							break;
					}
					
					onlyShowErrors = dis.readBoolean();
					int columnNum = dis.readInt();
					if (!Settings.imagesEnabled())
						columnNum--;
					
					setSortOrder(columnNum);
					songTable.setSortColumn(columns[columnNum]);
					core.setSortOrder(sortOrder);
					
					if (dis.readBoolean() == true) {
						songTable.setSortDirection(SWT.UP);
						core.setSortDirection(Songs.SORT_ASCENDING);
					} else {
						songTable.setSortDirection(SWT.DOWN);
						core.setSortDirection(Songs.SORT_DESCENDING);
					}
					
					BufferedReader br = new BufferedReader(new InputStreamReader(dis));

					String text = br.readLine();
					if (text != null) {
						outputFormatField.setText(text);
						text = br.readLine();
						if (text != null) {
							outputLocationField.setText(text);
						}
					}
					
					br.close();
					break;
			}
		} catch (IOException e) {
			if (Core.DEBUG)
				System.err.println("Unable to to load settings");
		}
		
		newLocationButtonPressed();
		capitalizeButtonPressed();
	}
	
	private void saveSettings() {
		//String path = Core.HOME_PATH + SETTINGS_FILE;
		String path = SETTINGS_FILE;
		
		try {
			(new File(Core.HOME_PATH)).mkdir();
			
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(path));
			
			dos.writeByte(0xA5);
			dos.writeChars("01.00");
			dos.writeBoolean(Settings.canScan());
			dos.writeBoolean(notifyUpdates);
			dos.writeBoolean(newLocationButton.getSelection());
			dos.writeBoolean(deleteOriginalButton.getSelection());
			dos.writeBoolean(overwriteButton.getSelection());
			//dos.writeBoolean(removeFramesButton.getSelection());
			dos.writeBoolean(false);
			dos.writeBoolean(capitalizeButton.getSelection());
			
			if (capitalizeProperButton.getSelection())
				dos.writeInt(0);
			else if (capitalizeProperButton2.getSelection())
				dos.writeInt(1);
			else if (capitalizeAllButton.getSelection())
				dos.writeInt(2);
			else
				dos.writeInt(-1);
			
			dos.writeBoolean(onlyShowErrors);
			if (Settings.imagesEnabled())
				dos.writeInt(getColumnNum(songTable.getSortColumn()));
			else
				dos.writeInt(getColumnNum(songTable.getSortColumn()) + 1);
			if (songTable.getSortDirection() == SWT.UP)
				dos.writeBoolean(true);
			else
				dos.writeBoolean(false);
			
			PrintWriter pw = new PrintWriter(dos);
			
			pw.println(outputFormatField.getText());
			pw.println(outputLocationField.getText());
			
			pw.close();
		} catch (IOException e) {
			if (Core.DEBUG)
				System.err.println("Unable to save settings to file");
		}
	}
	
	public static boolean isSeperator(char ch) {
		if (ch == '/' || ch == '\\')
			return true;
		else
			return false;
	}
}