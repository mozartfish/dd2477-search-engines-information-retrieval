/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

  /** A unique umber representing a document in the corpus */
  public int docID;

  /**
   * A numerical value for ranking the importance of a particular document when performing ranked
   * retrieval
   */
  public double score = 0;

  /** A list that stores the positions where the term appears */
  public ArrayList<Integer> positionList = new ArrayList<>();

  /**
   * Postings Entry Constructor
   *
   * @param docID document the term appears in
   * @param score score associated with this particular document and term
   * @param position position where the term appears
   */
  public PostingsEntry(int docID, double score, int position) {
    this.docID = docID;
    this.score = score;
    this.positionList.add(position);
  }

  /**
   * Postings Entry Constructor
   *
   * @param docID document the term appears in
   * @param score score associated with this particular document and term
   * @param positionList list containing the positions where the term appears in the document
   */
  public PostingsEntry(int docID, double score, ArrayList<Integer> positionList) {
    this.docID = docID;
    this.score = score;
    this.positionList = positionList;
  }

  /**
   * A string representation of a PostingsEntry. Used for reconstructing posting entries when
   * retrieving them from disk.
   *
   * @return String representation of a postings entry object - documentId, score, and positionList
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(docID).append("^").append(score).append(":");
    for (Integer offset : positionList) {
      sb.append(offset).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  /**
   * PostingsEntries are compared by their score (only relevant in ranked retrieval).
   *
   * <p>The comparison is defined so that entries will be put in descending order.
   */
  public int compareTo(PostingsEntry other) {
    return Double.compare(other.score, score);
  }

  /**
   * Merge the position indexes of two postings lists into a single list in sorted order
   *
   * @param p1 position list of the first entry
   * @param p2 position list of the second entry
   * @return list of merge position indexes from the two entry inputs
   */
  public ArrayList<Integer> mergePositionList(ArrayList<Integer> p1, ArrayList<Integer> p2) {
    ArrayList<Integer> result = new ArrayList<>();
    int i = 0;
    int j = 0;

    while (i < p1.size() && j < p2.size()) {
      int position1 = p1.get(i);
      int position2 = p2.get(j);
      if (position1 < position2) {
        result.add(position1);
        i++;
      } else if (position1 > position2) {
        result.add(position2);
        j++;
      } else {
        result.add(position1);
        i++;
        j++;
      }
    }

    while (i < p1.size()) {
      result.add(p1.get(i));
      i++;
    }

    while (j < p2.size()) {
      result.add(p2.get(j));
      j++;
    }

    return result;
  }
}
