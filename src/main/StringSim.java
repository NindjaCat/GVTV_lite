package main;
import java.util.HashMap;
import java.util.Map;
import channels.ChannelItem;

public class StringSim {
	public static int similarity(final String s1, final HashMap<Integer,ChannelItem> channels) {
		double prevString=-100;
		int prevChannel=1;
		for(Map.Entry<Integer, ChannelItem> c:channels.entrySet())
		{
		  double thisString = 0;
		  String longer = c.getValue().getName(), shorter = s1;
		  int longerLength = longer.length();
		  int shorterLength = shorter.length();
		  if(longerLength<shorterLength) 
		  {
			  
			  int length=longerLength;
			  longerLength=shorterLength;
			  shorterLength=length;
			  String string=longer;
			  longer=shorter;
			  shorter=string;
		  }
		  thisString=((longerLength - editDistance(longer, shorter)) / (double) longerLength);
		  if(thisString>prevString)
		  {
			  System.err.println(thisString+" "+c.getValue().getName());
			  prevString=thisString;
			  prevChannel=c.getKey();
		  }
		}
		return prevChannel;
	}	

	// Example implementation of the Levenshtein Edit Distance
	  // See http://rosettacode.org/wiki/Levenshtein_distance#Java
	  public static int editDistance(String s1, String s2) {
	    s1 = s1.toLowerCase();
	    s2 = s2.toLowerCase();

	    int[] costs = new int[s2.length() + 1];
	    for (int i = 0; i <= s1.length(); i++) {
	      int lastValue = i;
	      for (int j = 0; j <= s2.length(); j++) {
	        if (i == 0)
	          costs[j] = j;
	        else {
	          if (j > 0) {
	            int newValue = costs[j - 1];
	            if (s1.charAt(i - 1) != s2.charAt(j - 1))
	              newValue = Math.min(Math.min(newValue, lastValue),
	                  costs[j]) + 1;
	            costs[j - 1] = lastValue;
	            lastValue = newValue;
	          }
	        }
	      }
	      if (i > 0)
	        costs[s2.length()] = lastValue;
	    }
	    return costs[s2.length()];
	  }
}
