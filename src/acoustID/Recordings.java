package acoustID;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

public class Recordings {
	
	private LinkedList<Recording> recordings;
	
	public Recordings(URL url) throws IOException, JsonException, JsonParsingException, NoSuchElementException {
		recordings = new LinkedList<Recording>();
		
	    InputStreamReader in = new InputStreamReader(url.openStream());
	    JsonParser parser = Json.createParser(in);
	    
	    parser.next();
	    parser.next();
	    if (parser.getString().equals("status")) {
	    	parser.next();
	    	if (parser.getString().equals("ok")) {
	    		parser.next();
	    		parser.next();
	    		
	    		if (parser.next() != Event.START_OBJECT)
	    			throw new JsonException("No results");
	    		
	    		boolean done = false;
	    		while (!done) {
	    			switch (parser.next()) {
	    				case KEY_NAME:
	    					if (parser.getString().equals("recordings")) {
	    						if (parser.next() == Event.START_ARRAY) {
	    							while (parser.next() != Event.END_ARRAY) {
	    								recordings.add(new Recording(parser));
	    							}
	    						} else {
	    							throw new JsonException("Invalid JSON");
	    						}
	    					}
	    					break;
	    					
	    				case END_OBJECT:
	    					done = true;
	    					break;
	    					
	    				default:
	    					break;
	    			}
	    		}
	    	} else {
	    		throw new JsonException("Status not ok");
	    	}
	    } else {
	    	throw new JsonException("Invalid JSON");
	    }
	    
		parser.close();
	}
	
	public Recording getFirstRecording() {
		Iterator<Recording> it = recordings.iterator();
		Recording firstRecording;
		if (it.hasNext())
			firstRecording = it.next();
		else
			return null;
		
		while (it.hasNext()) {
			Recording recording = it.next();
			if (recording.getFirstYear() < firstRecording.getFirstYear())
				firstRecording = recording;
		}
		
		return firstRecording;
	}
}
