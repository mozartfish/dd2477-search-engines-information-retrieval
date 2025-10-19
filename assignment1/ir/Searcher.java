/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

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
      }
    }
    return null;
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
