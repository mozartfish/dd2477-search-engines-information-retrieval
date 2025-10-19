/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.ArrayList;

/** Searches an index for results of a query. */
public class Searcher {

  /** The index to be searched by this Searcher. */
  Index index;

  /** The k-gram index to be searched by this Searcher */
  KGramIndex kgIndex;

  /** Constructor */
  public Searcher(Index index, KGramIndex kgIndex) {
    this.index = index;
    this.kgIndex = kgIndex;
  }

  /**
   * Searches the index for postings matching the query.
   *
   * @return A postings list representing the result of the query.
   */
  public PostingsList search(
      Query query, QueryType queryType, RankingType rankingType, NormalizationType normType) {
    //
    //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
    //
    System.out.println("Query terms: " + query.queryToString());
    // intersection queries
    if (queryType == QueryType.INTERSECTION_QUERY) {
      System.out.println("INTERSECTION QUERY");
      if (query.size() == 1) {
        System.out.println("ONE-WORD QUERY");
        return oneWordQuery(query);
      } else {
        System.out.println("MULTI-WORD QUERY");
        return multiWordQuery(query);
      }
    }
    // phrase query
    if (queryType == QueryType.PHRASE_QUERY) {
      System.out.println("PHRASE QUERY");
    }
    return null;
  }

  private PostingsList multiWordQuery(Query query) {
    ArrayList<PostingsList> postingsLists = getPostingsList(query);

    // size(postingsLists) == 0
    if (postingsLists.isEmpty()) {
      return new PostingsList();
    }

    // size(postingsLists) == 2
    PostingsList p1 = postingsLists.getFirst();
    PostingsList p2 = postingsLists.get(1);
    PostingsList result = intersect(p1, p2);

    // size(postingsLists) > 2
    for (int i = 2; i < postingsLists.size(); i++) {
      PostingsList postings = postingsLists.get(i);
      if (postings != null) {
        result = intersect(result, postings);
      }
    }
    return result;
  }

  /**
   * Compute the intersection of two postings lists
   *
   * @param p1 first postings list
   * @param p2 second postings list
   * @return postings list that contains the intersection of p1 and p2 postings list
   */
  private PostingsList intersect(PostingsList p1, PostingsList p2) {
    PostingsList result = new PostingsList();
    int i = 0;
    int j = 0;
    while (i < p1.size() && j < p2.size()) {
      if (p1.get(i).docID == p2.get(j).docID) {
        if (result.size() == 0 || result.get(result.size() - 1).docID != p1.get(i).docID) {
          PostingsEntry postingEntry = new PostingsEntry(p1.get(i).docID, 1, 1);
          result.add(postingEntry);
        }
        i++;
        j++;
      } else if (p1.get(i).docID < p2.get(j).docID) {
        i++;
      } else {
        j++;
      }
    }
    return result;
  }

  /**
   * Retrieve the postings lists for each term in the query
   *
   * @param query information requested by the user
   * @return a list of postings associated with each term in the query
   */
  private ArrayList<PostingsList> getPostingsList(Query query) {
    ArrayList<PostingsList> postingsList = new ArrayList<>();
    for (int i = 0; i < query.size(); i++) {
      postingsList.add(index.getPostings(query.queryterm.get(i).term));
    }
    return postingsList;
  }

  /**
   * One-Word Query
   *
   * @param query information requested by user
   * @return PostingsList containing data requested by user
   */
  private PostingsList oneWordQuery(Query query) {
    PostingsList result;
    String term = query.queryterm.getFirst().term;
    result = index.getPostings(term);
    return result;
  }
}
