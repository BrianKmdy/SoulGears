package acoustID;

import java.util.NoSuchElementException;

import javax.json.JsonException;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

public class Release {
	
	String album;
	String year;
	String trackNum;
	
	int iYear;
	
	public Release(JsonParser parser) throws JsonException, JsonParsingException, NoSuchElementException {
		iYear = 1000000;
		
		boolean done = false;
		while (!done) {
			switch (parser.next()) {
				case  KEY_NAME:
					switch (parser.getString()) {
						case "title":
							parser.next();
							this.album = parser.getString();
							break;
							
						case "date":
							if (parser.next() != Event.START_OBJECT)
								throw new JsonException("Invalid JSON");
							
							Event eYear;
							while ((eYear = parser.next()) != Event.END_OBJECT) {
								if (eYear == Event.KEY_NAME) {
									if (parser.getString().equals("year")) {
										parser.next();
										this.year = parser.getString();
										this.iYear = parser.getInt();
									}
								}
							}
							break;
							
						case "mediums":
							if (parser.next() != Event.START_ARRAY)
								throw new JsonException("Invalid JSON");
							if (parser.next() != Event.START_OBJECT)
								throw new JsonException("Invalid JSON");
							
							Event eMedium;
							while ((eMedium = parser.next()) != Event.END_ARRAY) {
								if (eMedium == Event.KEY_NAME) {
									if (parser.getString().equals("tracks")) {
										if (parser.next() != Event.START_ARRAY)
											throw new JsonException("Invalid JSON");
										if (parser.next() != Event.START_OBJECT)
											throw new JsonException("Invalid JSON");
										
										Event eTrack;
										while ((eTrack = parser.next()) != Event.END_ARRAY) {
											if (eTrack == Event.KEY_NAME) {
												if (parser.getString().equals("position")) {
													parser.next();
													if (this.trackNum == null)
														this.trackNum = parser.getString();
												}
											} else {
												if (eTrack == Event.START_ARRAY) 
													Recording.exitArray(parser);
												else if (eTrack == Event.START_OBJECT)
													Recording.exitObject(parser);
											}
										}
									}
								} else {
									if (eMedium == Event.START_ARRAY) 
										Recording.exitArray(parser);
									else if (eMedium == Event.START_OBJECT)
										Recording.exitObject(parser);
								}
							}
							break;
						
						default:
							break;
					}
					break;
					
				case START_ARRAY:
					Recording.exitArray(parser);
					break;
					
				case START_OBJECT:
					Recording.exitObject(parser);
					break;
					
				case END_OBJECT:
					done = true;
					break;
					
				default:
					break;
			}
		}
	}
	
	public String getAlbum() {
		return album;
	}
	
	public String getYear() {
		return year;
	}
	
	public int getYearInt() {
		return iYear;
	}
	
	public String getTrackNum() {
		return trackNum;
	}
}
