package hw6;

public class Business {
  private String businessID;
  private String businessName;
  private String businessAddress;
  private String reviews;
  private int reviewCharCount;

  public Business(String line) {
    int firstComma = line.indexOf(',', 0);
    int secondComma = line.indexOf(',', firstComma+1);
    int thirdComma = line.indexOf(',', secondComma+1);

    // Starting at index 1, and ending at index line.length()-1 removes the brackets that come around each String
    businessID = line.substring(1, firstComma);
    // Adding 2 to the index skips the comma and the space following it
    businessName = line.substring(firstComma+2, secondComma);
    businessAddress = line.substring(secondComma+2, thirdComma);
    reviews = line.substring(thirdComma+2, line.length()-1);
    reviewCharCount = reviews.length();
  }

  public String getReviews() {
    return reviews;
  }
  
  public int getReviewCharCount() {
  	return reviewCharCount;
  }
  
  public String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder();
    return sb.append("-------------------------------------------------------------------------------\nBusiness ID: ")
    	.append(businessID).append("\nBusiness Name: ").append(businessName).append("\nBusiness Address: ")
    	.append(businessAddress).append("\nCharacter Count: ").append(reviewCharCount).toString();
          //+ "Reviews: " + reviews + "\n"
  }

}