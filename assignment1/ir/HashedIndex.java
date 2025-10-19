/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.HashMap;
import java.util.Iterator;

/** Implements an inverted index as a Hashtable from words to PostingsLists. */
public class HashedIndex implements Index {

  /** The index as a hashtable. */
  private HashMap<String, PostingsList> index = new HashMap<String, PostingsList>();

  /**
   * Inserts this token in the hashtable.
   *
   * @param token document term
   * @param docID document the term appears in
   * @param position the index where the terms occurs
   */
  public void insert(String token, int docID, int position) {
    // if term in index - update postings list associated with term
    // else add term to index and create a new postings list
    if (index.containsKey(token)) {
      PostingsList postingsList = index.get(token);
      // term in same document
      if (postingsList.get(postingsList.size() - 1).docID == docID) {
        // score keeps track of term frequency
        postingsList.get(postingsList.size() - 1).score++;
        // add term position
        postingsList.get(postingsList.size() - 1).positionList.add(position);
      } else {
        // term in a different document
        postingsList.add(docID, 1, position);
      }
    } else {
      PostingsList postingsList = new PostingsList();
      postingsList.add(docID, 1, position);
      index.put(token, postingsList);
    }
  }

  /**
   * Returns the postings for a specific term, or null if the term is not in the index.
   *
   * @param token term
   * @return PostingsList for the token for a particular token
   */
  public PostingsList getPostings(String token) {
    return index.get(token);
  }

  /** No need for cleanup in a HashedIndex. */
  public void cleanup() {}
}
