package com.mp3t.utils;

public class CapitalizeString {
	
	public final static int CAPITALIZE_COMPLETE      = 1;
	public final static int CAPITALIZE_PROPER        = 1 << 1;
	public final static int CAPITALIZE_FIX_ALL_UPPER = 1 << 2;
	public final static int CAPITALIZE_LOWER_FIRST   = 1 << 3;

	public final static String[] WORDS_NOT_CAPPED = 
	{   "a",
	    "aboard",
	    "about",
	    "above",
	    "absent",
	    "across",
	    "after",
	    "against",
	    "ago",
	    "along",
	    "alongside",
	    "although",
	    "amid",
	    "amidst",
	    "among",
	    "amongst",
	    "an",
	    "and",
	    "anti",
	    "apart",
	    "around",
	    "as",
	    "aside",
	    "aslant",
	    "astride",
	    "at",
	    "atop",
	    "away",
	    "barring",
	    "because",
	    "before",
	    "behind",
	    "below",
	    "beneath",
	    "beside",
	    "besides",
	    "between",
	    "betwixt",
	    "beyond",
	    "both",
	    "but",
	    "by",
	    "despite",
	    "down",
	    "during",
	    "either",
	    "except",
	    "failing",
	    "following",
	    "for",
	    "from",
	    "hence",
	    "in",
	    "inside",
	    "into",
	    "is",
	    "like",
	    "merry",
	    "mid",
	    "minus",
	    "near",
	    "neither",
	    "next",
	    "nor",
	    "notwithstanding",
	    "of",
	    "off",
	    "on",
	    "onto",
	    "opposite",
	    "or",
	    "outside",
	    "over",
	    "past",
	    "per",
	    "plus",
	    "regarding",
	    "round",
	    "sans",
	    "save",
	    "since",
	    "so",
	    "than",
	    "the",
	    "through",
	    "throughout",
	    "till",
	    "times",
	    "to",
	    "toward",
	    "towards",
	    "under",
	    "underneath",
	    "unlike",
	    "until",
	    "unto",
	    "up",
	    "upon",
	    "via",
	    "versus",
	    "vis-a-vis",
	    "vs",
	    "when",
	    "while",
	    "with",
	    "withal",
	    "within",
	    "without"	};
	
	private String string;
	
	public CapitalizeString(String string) {
		this.string = string;
	}

	public String capitalize(int flags) {
	    if ((flags & CAPITALIZE_LOWER_FIRST) != 0) {
	    	string = string.toLowerCase();
	    } else if ((flags & CAPITALIZE_FIX_ALL_UPPER) != 0) {
	        boolean allUpper = true;

	        for (int i = 0; i < string.length(); i++) {
	            if (Character.isLowerCase(string.charAt(i))) {
	                allUpper = false;
	                break;
	            }
	        }

	        if (allUpper)
	            string = string.toLowerCase();
	    }

	    if ((flags & CAPITALIZE_PROPER) != 0)
	        capitalizeProper();
	    else
	        capitalizeComplete();

	    return string;
	}

	private void capitalizeProper() {
		
	    boolean firstWord = true; // First word always gets capitalized
	    boolean lastWord = false; // Last word always gets capitalized
	    
	    char[] str = string.toCharArray();

	    for (int i = 0; i < str.length; i++) {
	        if (Character.isLetterOrDigit(str[i])) {
	        	
	            int len = 0;
	            int indexOfNextWord = 0;

	            boolean endOfWord = false;
	            boolean endOfString = true;

	            for (int o = i; o < str.length; o++) {
	                if (Character.isLetterOrDigit(str[o])) {
	                    if (endOfWord) {
	                        endOfString = false;
	                        indexOfNextWord = o;
	                        break;
	                    }
	                } else {
	                    if (!endOfWord) {
	                        len = o - i;
	                        endOfWord = true;
	                    }

	                    if (!Character.isWhitespace(str[o]))
	                        lastWord = true;
	                }
	            }

	            if (endOfString) {
	                len = str.length - i;
	                lastWord = true;
	            } else {
	            	if (i > 0) {
		                if (str[i - 1] == '\'') {
		                    str[i] = Character.toLowerCase(str[i]);
		                    i = indexOfNextWord - 1;
		                    continue;
		                }
	            	}
	            }

	            String word = string.substring(i, i + len);

	            word = word.toLowerCase();

	            boolean capWord = true;

	            for (int o = 0; o < WORDS_NOT_CAPPED.length; o++) {
	                if (word.equals(WORDS_NOT_CAPPED[o])) {
	                    capWord = false;
	                    break;
	                }
	            }

	            if (capWord) {
	                str[i] = Character.toUpperCase(str[i]);
	            } else {
	                for (int o = 0; o < word.length(); o++)
	                    str[i + o] = word.charAt(o);
	            }

	            if (lastWord) {
	                str[i] = Character.toUpperCase(str[i]);
	                lastWord = false;
	                firstWord = true;
	            } else if (firstWord) {
	                str[i] = Character.toUpperCase(str[i]);
	                firstWord = false;
	            }

	            if (endOfString)
	                break;

	            i = indexOfNextWord - 1;
	        }
	    }
	    
	    string = new String(str);
	}

	private void capitalizeComplete()
	{
	    boolean inWord = false;
	    
	    char[] str = string.toCharArray();

	    for (int i = 0; i < str.length; i++)
	    {
	        if (Character.isLetter(str[i]))
	        {
	            if (inWord == false)
	            {
	                str[i] = Character.toUpperCase(str[i]);
	                inWord = true;
	            }
	        }
	        else
	        {
	            if (inWord == true)
	                inWord = false;
	        }
	    }
	    
	    string = new String(str);
	}
}
