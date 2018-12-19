/*
 Name: Chase Bakkeby
 ID: 604831840
 Course: PIC 20A
 Prof: Ernest Ryu
 TA: Andrew Krieger
 Collaborators: Andrew Krieger
 Date: 12/05/18
 Assignment: Yelp Data Analysis
*/


package hw6;


import java.io.*;
import java.util.*;
import hw6.Business;


public class YelpAnalysis {

	
	// Stores objects of all the businesses, sorted by custom ReviewCharCountComparator
	private PriorityQueue<Business> businessPriorityQueue = new PriorityQueue<Business>(10, new ReviewCharCountComparator());
	// Stores all distinct words contained in all reviews of all businesses, and count of their occurrences
	private Map<String,Integer> masterWordMap = new HashMap<>();
	// Builds a very long String using .append() method and then prints all at once
	// More efficient that using many print statements and String concatenation
	private java.lang.StringBuilder sb = new java.lang.StringBuilder();
	
	
	// With no variables to instantiate, this Constructor simply creates an instance of the class to call runYelpAnalysis()
	public YelpAnalysis() {}
	
	
	// This is the main body of the code, called by main() function
	public void runYelpAnalysis() {
		InputStream is = null;
		Reader r = null;
		BufferedReader br = null;
		
		try {
			long startTime = System.currentTimeMillis();
			
			is = new FileInputStream("hw6/yelpDatasetParsed_full.txt"); 
			r = new InputStreamReader(is);
			br = new BufferedReader(r);

			String line = br.readLine();
			while (line != null) {
				Business biz = new Business(line);
				businessPriorityQueue.add(biz);
				bizCounter(biz);
				line = br.readLine();
			}
			
			// For the top 10 businesses with most review characters
			for (int i=0; i<10; i++) {
				// Removes in order of businesses with highest character counts
				Business currentBiz = businessPriorityQueue.poll();
				sb.append(currentBiz.toString());
				printer();
				tfidfCalculator(currentBiz);
			}
			
			long endTime = System.currentTimeMillis();
			sb.append("\nExecution Time: ").append((endTime - startTime)/1000.0).append("s");
			printer();
			
		} 
		
		catch (IOException e) { 
			sb.append("\nIOException: ").append(e.getMessage());
			printer();
		} 
		
		finally {
			if (br!=null) {
				System.out.println();
				try { 
					br.close();
				} catch (IOException e) { 
					sb.append("\nIOException: ").append(e.getMessage());
					printer();
				} 
			}
		}
	}
	
	
	// Private inner class
	// Implements PriorityQueue's Comparator interface to compare elements in PriorityQueue
	// Modified code from "https://www.geeksforgeeks.org/implement-priorityqueue-comparator-java/"
	private static class ReviewCharCountComparator implements Comparator<Business> { 
        // Overriding compare() method of Comparator for descending order of Business reviewCharCount
		@Override
        public int compare(Business b1, Business b2) { 
            if (b1.getReviewCharCount() < b2.getReviewCharCount()) 
                return 1; 
            else if (b1.getReviewCharCount() > b2.getReviewCharCount()) 
                return -1;
            else
                return 0; 
        } 
    }
	
	
	// Private inner class
	// Implements List's Comparator interface to compare elements in ArrayList
	// Modified code from "https://www.geeksforgeeks.org/implement-priorityqueue-comparator-java/"
	private static class TfidfMapEntryValueComparator implements Comparator<Map.Entry<String, Double>> {
		// Overriding compare() method of Comparator for descending order of tfidf values in tfidfList
		@Override
		public int compare(Map.Entry<String, Double> m1, Map.Entry<String, Double> m2) {
			if (m1.getValue() < m2.getValue())
				return 1;
			else if (m1.getValue() > m2.getValue())
				return -1;
			else
				return 0;
		}
	}


	// Populates masterWordMap with the total amount of businesses that each distinct word occurs in
	// Used for calculating -idf portion of tf-idf score
	private void bizCounter(Business currentBiz) {
		String currentBizReviews = currentBiz.getReviews();
		// The following creates a HashSet of all distinct words found in this specific business' String reviews
		Set<String> distinctWordsInCurrentBizReviews = new HashSet<>();
		for (String word : currentBizReviews.split(" ", 0))
			distinctWordsInCurrentBizReviews.add(word);
			
		for (String word : distinctWordsInCurrentBizReviews) {
			// First makes sure that word is stored in the master HashMap, otherwise adds it
			masterWordMap.putIfAbsent(word, 0);
			// Then increases the word's value by 1 in the master HashMap
			masterWordMap.computeIfPresent(word, (k,v) -> v+1);
		}
	}
	
	
	// Goes through each distinct word in the reviews of a business, and calculates its tf-idf score
	private void tfidfCalculator(Business currentBiz) {
		String currentBizReviews = currentBiz.getReviews();
		// Creates a HashMap of all distinct words and number of their occurrences in this specific business' reviews
		// Simultaneously builds up set and count
		Map<String,Integer> currentBizWordMap = new HashMap<>();
		for (String word : currentBizReviews.split(" ", 0)) {
			// First makes sure that word is stored in the HashMap, otherwise adds it
			currentBizWordMap.putIfAbsent(word, 0);
			// Then increases the word's value by 1 in the HashMap
			currentBizWordMap.computeIfPresent(word, (k,v) -> v+1);
		}	
		
		Map<String, Double> tfidfScoreMap = new HashMap<>();
		// Uses keySet() method to obtain set of all the keys in the HashMap
		for (String word : currentBizWordMap.keySet()) {
			int bizCount = masterWordMap.get(word);
			int wordCount = currentBizWordMap.get(word);
			double tfidf = 0;
			if (bizCount >= 5)
				tfidf = (1.0*wordCount) / bizCount;
			tfidfScoreMap.put( word, tfidf );
		}
		
		// Returns an ArrayList of type Map.Entry objects (which contain both key and value for each)
		// Necessary to sort and preserve ordering of objects
		List<Map.Entry<String, Double>> tfidfList = new ArrayList<>(tfidfScoreMap.entrySet());
		// Sorts by the custom TfidfMapEntryValueComparator
		Collections.sort(tfidfList, new TfidfMapEntryValueComparator());
		for (int i = 0; i < 30; i++) {
			Map.Entry<String, Double> currentElement = tfidfList.get(i);
			String currentKey = currentElement.getKey();
			// Formats each tfidf score to display 2 decimal places, while adding it to the StringBuilder
			String currentTfidf = String.format("%.2f", currentElement.getValue());
			sb.append("(").append(currentKey).append(", ").append(currentTfidf).append(") ");
		}	 

		printer();
	}
	
	
	// Keeps margins 79 characters long or less
	// Modified code from "https://stackoverflow.com/questions/4212675/wrap-the-string-after-a-number-of-characters-word-wise-in-java"
	private void printer() {
		int firstNewlineChar = 0;
		int secondNewlineChar = 0;
		int newlineIndex = 0;
		int spaceIndex = 0;
		
		while ((secondNewlineChar = sb.indexOf("\n", firstNewlineChar+1)) != -1) {
			if (secondNewlineChar - firstNewlineChar > 80) {
				newlineIndex = sb.lastIndexOf(" ", secondNewlineChar);
				while (newlineIndex - firstNewlineChar > 80)
					newlineIndex = sb.lastIndexOf(" ", newlineIndex-1);
				sb.replace(newlineIndex, newlineIndex + 1, "\n");
			}
			firstNewlineChar = secondNewlineChar;
		}
		
		while (spaceIndex + 79 < sb.length() && (spaceIndex = sb.lastIndexOf(" ", spaceIndex + 79)) != -1) {
		    sb.replace(spaceIndex, spaceIndex + 1, "\n");
		}
		
		System.out.println(sb.toString());
		sb.delete(0, sb.length());
	}


	public static void main(String[] args) {
		YelpAnalysis ya = new YelpAnalysis();
		ya.runYelpAnalysis();
	}
	
}