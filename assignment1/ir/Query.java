/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.nio.charset.*;
import java.io.*;

/** A class for representing a query as a list of words, each of which has an associated weight. */
public class Query {

  /** Help class to represent one query term, with its associated weight. */
  class QueryTerm {
    String term;
    double weight;

    QueryTerm(String t, double w) {
      term = t;
      weight = w;
    }
  }

  /**
   * Representation of the query as a list of terms with associated weights. In assignments 1 and 2,
   * the weight of each term will always be 1.
   */
  public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

  /**
   * Relevance feedback constant alpha (= weight of original query terms). Should be between 0 and
   * 1. (only used in assignment 3).
   */
  double alpha = 0.2;

  /**
   * Relevance feedback constant beta (= weight of query terms obtained by feedback from the user).
   * (only used in assignment 3).
   */
  double beta = 1 - alpha;

  /** Creates a new empty Query */
  public Query() {}

  /** Creates a new Query from a string of words */
  public Query(String queryString) {
    StringTokenizer tok = new StringTokenizer(queryString);
    while (tok.hasMoreTokens()) {
      queryterm.add(new QueryTerm(tok.nextToken(), 1.0));
    }
  }

  /** Returns the number of terms */
  public int size() {
    return queryterm.size();
  }

  /** Returns the Manhattan query length */
  public double length() {
    double len = 0;
    for (QueryTerm t : queryterm) {
      len += t.weight;
    }
    return len;
  }

  /** Returns a copy of the Query */
  public Query copy() {
    Query queryCopy = new Query();
    for (QueryTerm t : queryterm) {
      queryCopy.queryterm.add(new QueryTerm(t.term, t.weight));
    }
    return queryCopy;
  }

  /**
   * String representation of a query
   *
   * @return a string representation of a query
   */
  String queryToString() {
    StringBuilder sb = new StringBuilder();
    for (QueryTerm q : queryterm) {
      sb.append(q.term).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("\n");
    return sb.toString();
  }

  /**
   * Expands the Query using Relevance Feedback
   *
   * @param results The results of the previous query.
   * @param docIsRelevant A boolean array representing which query results the user deemed relevant.
   * @param engine The search engine object
   */
  public void relevanceFeedback(PostingsList results, boolean[] docIsRelevant, Engine engine) {
    // Rocchio 1971 SMART Algorithm
    // Calculate the number of relevant documents
    int relevantDocs = 0;
    Query modifiedQuery = new Query();
    for (boolean b : docIsRelevant) {
      if (b) {
        relevantDocs++;
      }
    }
    if (relevantDocs == 0) {
      return;
    }

    for (QueryTerm queryTerm : queryterm) {
      QueryTerm newTerm = new QueryTerm(queryTerm.term, queryTerm.weight * alpha);
      modifiedQuery.queryterm.add(newTerm);
    }

    for (int i = 0; i < docIsRelevant.length; i++) {
      if (docIsRelevant[i]) {
        PostingsEntry entry = results.get(i);
        String docName = engine.index.docNames.get(entry.docID);
        HashMap<String, Double> documentTermFrequency = documentTermFrequency(docName);
        for (String term : documentTermFrequency.keySet()) {
          boolean found = false;
          for (QueryTerm queryTerm : modifiedQuery.queryterm) {
            if (queryTerm.term.equals(term)) {
              queryTerm.weight += (beta * documentTermFrequency.get(term)) / relevantDocs;
              found = true;
              break;
            }
          }
          if (!found) {
            QueryTerm queryTerm =
                new QueryTerm(term, beta * documentTermFrequency.get(term) / relevantDocs);
            modifiedQuery.queryterm.add(queryTerm);
          }
        }
      }
    }

    queryterm = modifiedQuery.queryterm;
  }

  /**
   * This function returns the frequency of terms in a document. The Rocchio SMART 1971 algorithm
   * centroid calculations require documents to be represented using the vector space model. In
   * order for the documents in the results query to be represented in the same vector space as the
   * inverted index vector space, the same tokenizer is used to ensure terms are standardized using
   * the same criteria as the inverted index. I found this article describing a data structure
   * called a Forward Index which does the inverse of the Inverted Index - mapping documents ->
   * words. Resource: https://www.geeksforgeeks.org/difference-inverted-index-forward-index/
   *
   * @param docName name of the document
   * @return hash map: term -> term frequency
   */
  public HashMap<String, Double> documentTermFrequency(String docName) {
    HashMap<String, Double> termFrequency = new HashMap<>();
    try {
      Reader reader = new InputStreamReader(new FileInputStream(docName), StandardCharsets.UTF_8);
      Tokenizer tokenizer = new Tokenizer(reader, true, false, true, "patterns.txt");
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        // record the term frequency
        termFrequency.merge(token, 1.0, Double::sum);
      }
    } catch (IOException e) {
      System.err.println("Warning: IOException during indexing.");
    }
    return termFrequency;
  }
}
