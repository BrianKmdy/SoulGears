package acoustID;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.json.JsonException;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

public class Recording {
	
	private LinkedList<Release> releases;
	
	private String artist;
	private String title;
	
	private int firstYear;
	
	public Recording(JsonParser parser) throws JsonException, JsonParsingException, NoSuchElementException {
		releases = new LinkedList<Release>();
		
		boolean done = false;
		while (!done) {
			switch (parser.next()) {
				case KEY_NAME:
					switch (parser.getString()) {
						case "releases":
    						if (parser.next() == Event.START_ARRAY) {
    							while (parser.next() != Event.END_ARRAY) {
    								releases.add(new Release(parser));
    							}
    						} else {
    							throw new JsonException("Invalid JSON");
    						}
							break;
							
						case "artists":
							if (parser.next() != Event.START_ARRAY)
								throw new JsonException("Invalid JSON");
							
							Event e;
							while ((e = parser.next()) != Event.END_ARRAY) {
								if (e == Event.KEY_NAME) {
									if (parser.getString().equals("name")) {
										parser.next();
										if (this.artist == null)
											this.artist = parser.getString();
									}
								}
							}
							break;
							
						case "title":
							parser.next();
							this.title = parser.getString();
							break;
							
						default:
							break;
								
					}
					break;
					
				case START_ARRAY:
					exitArray(parser);
					break;
					
				case START_OBJECT:
					exitObject(parser);
					break;
					
				case END_OBJECT:
					done = true;
					break;
					
				default:
					break;
			}
		}
		
		Iterator<Release> it = releases.iterator();
		if (it.hasNext())
			firstYear = it.next().getYearInt();
		
		while (it.hasNext()) {
			int year = it.next().getYearInt();
			if (year < firstYear)
				firstYear = year;
		}
	}
	
	public static void exitArray(JsonParser parser) {
		Event e;
		while ((e = parser.next()) != Event.END_ARRAY) {
			if (e == Event.START_ARRAY)
				exitArray(parser);
			else if (e == Event.START_OBJECT)
				exitObject(parser);
		}
	}
	
	public static void exitObject(JsonParser parser) {
		Event e;
		while ((e = parser.next()) != Event.END_OBJECT) {
			if (e == Event.START_ARRAY)
				exitArray(parser);
			else if (e == Event.START_OBJECT)
				exitObject(parser);
		}
	}
	
	public String getArtist() {
		return artist;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getFirstYear() {
		return firstYear;
	}
	
	public Release getFirstRelease() {
		Iterator<Release> it = releases.iterator();
		Release firstRelease;
		if (it.hasNext())
			firstRelease = it.next();
		else
			return null;
		
		while (it.hasNext()) {
			Release release = it.next();
			if (release.getYearInt() < firstRelease.getYearInt())
				firstRelease = release;
		}
		
		return firstRelease;
	}
}
