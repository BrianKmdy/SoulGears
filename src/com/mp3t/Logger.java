package com.mp3t;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class Logger {

	private HashMap<String, FileWriter> logFiles;
	private String defaultLog;
	
	public Logger() {
		logFiles = new HashMap<String, FileWriter>();
	}
	
	public Logger(String defaultLog) {
		this();
		this.defaultLog = defaultLog;
	}
	
	public void add(String name, String path) throws IOException {
		logFiles.put(name, new FileWriter(path, true));
	}
	
	public void write(String name, String text) {
		try {
			logFiles.get(name).write(text);
			logFiles.get(name).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(String text) {
		if (defaultLog != null)
			write(defaultLog, text);
	}
	
	public void writeLine(String name, String text) {
		write(name, text + '\n');
	}
	
	public void writeLine(String text) {
		if (defaultLog != null)
			writeLine(defaultLog, text);
	}
	
	public void writeStamp(String name, String text) {
		String stamp = new SimpleDateFormat("hh:mm:ss").format(new Date());
		write(name, "[" + stamp + "] " + text);
	}
	
	public void writeStamp(String text) {
		if (defaultLog != null)
			writeStamp(defaultLog, text);
	}

	public void writeLineStamp(String name, String text) {
		writeStamp(name, text + '\n');
	}
	

	public void writeLineStamp(String text) {
		if (defaultLog != null)
			writeLineStamp(defaultLog, text);
	}
	
	public void setDefaultLog(String defaultLog) {
		this.defaultLog = defaultLog;
	}
	
	public void close(String name) {
		try {
			logFiles.get(name).close();
		} catch (IOException e) {
			// Do nothing
		}
	}
	
	public void closeAll() {
		Iterator<FileWriter> it = logFiles.values().iterator();
		while (it.hasNext()) {
			try {
				it.next().close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}
}
