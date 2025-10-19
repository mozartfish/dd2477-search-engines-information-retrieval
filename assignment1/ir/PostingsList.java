/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.ArrayList;

public class PostingsList {

  /** The postings list */
  private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();

  /**
   * Add a PostingsEntry to the PostingsList
   *
   * @param docID document the term appears in
   * @param score score associated with this particular document and term
   * @param position position where the term appears
   */
  public void add(int docID, double score, int position) {
    PostingsEntry newEntry = new PostingsEntry(docID, score, position);
    list.add(newEntry);
  }

  /**
   * Add a PostingsEntry to the PostingsList
   *
   * @param postingsEntry PostingEntry object
   */
  public void add(PostingsEntry postingsEntry) {
    list.add(postingsEntry);
  }

  /**
   * Number of postings in this list.
   *
   * @return length of PostingsList
   */
  public int size() {
    return list.size();
  }

  /**
   * Returns the ith posting.
   *
   * @param i index in PostingsList
   * @return PostingsEntry located at index i
   */
  public PostingsEntry get(int i) {
    return list.get(i);
  }

  /**
   * A string representation of a PostingsList. Used for reconstructing postings lists when
   * retrieving them from disk.
   *
   * @return String representation of a PostingsList object
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (PostingsEntry postingEntry : list) {
      sb.append(postingEntry).append(">");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  /**
   * Construct a PostingsList object from a string
   *
   * @param s String representation of a postings list
   * @return PostingsList object
   */
  public static PostingsList stringToPostingsList(String s) {
    PostingsList postingsList = new PostingsList();

    // process posting entries
    String[] postingEntries = s.split(">");
    for (String postingEntry : postingEntries) {
      try {
        // split string into docID, score and positional indices
        String[] postingsData = postingEntry.split(":");

        // get docID and score
        String[] docIDScore = postingsData[0].split("\\^");
        int docID = Integer.parseInt(docIDScore[0]);
        double score = Double.parseDouble(docIDScore[1]);

        // get positional indices
        ArrayList<Integer> positionList = new ArrayList<>();
        String[] positions = postingsData[1].split(",");
        for (String position : positions) {
          positionList.add(Integer.parseInt(position.trim()));
        }

        // create a new postings entry and add to postings list
        PostingsEntry newEntry = new PostingsEntry(docID, score, positionList);

        // add to the postings list
        postingsList.add(newEntry);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return postingsList;
  }

  /**
   * Sort PostingsList by score
   *
   * @return a sorted PostingsList sorted in descending order
   */
  public PostingsList sort() {
    list.sort(PostingsEntry::compareTo);
    return this;
  }
}
