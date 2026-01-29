/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.ArrayList;
import java.util.HashMap;

/** Searches an index for results of a query. */
public class Searcher {

  /** The index to be searched by this Searcher. */
  Index index;

  /** The k-gram index to be searched by this Searcher */
  KGramIndex kgIndex;

  /** PageRank */
  PageRank pageRank;

  /** HITSRank */
  HITSRanker hitsRank;

  /** Ranking Weights */
  Double TF_IDF_WEIGHT = 45.0;

  Double PAGE_RANK_WEIGHT = 32.0;

  /** Constructor */
  public Searcher(Index index, KGramIndex kgIndex, PageRank pageRank, HITSRanker hitsRank) {
    this.index = index;
    this.kgIndex = kgIndex;
    this.pageRank = pageRank;
    this.hitsRank = hitsRank;
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
    System.out.println("QUERY_TERMS -> " + query.queryToString());
    // intersection queries
    if (queryType == QueryType.INTERSECTION_QUERY) {
      if (query.size() == 1) {
        System.out.println(queryType + " -> " + "ONE_WORD_QUERY");
        return oneWordQuery(query);
      } else {
        System.out.println(queryType + " -> " + "MULTI_WORD_QUERY");
        return multiWordQuery(query);
      }
    }
    // phrase query
    if (queryType == QueryType.PHRASE_QUERY) {
      System.out.println(queryType);
      return phraseQuery(query);
    }
    // ranked query
    // tf-idf
    if (queryType == QueryType.RANKED_QUERY && rankingType == RankingType.TF_IDF) {
      System.out.println(queryType + " " + rankingType);
      return tf_idfQuery(query, normType);
    }
    // pagerank
    if (queryType == QueryType.RANKED_QUERY && rankingType == RankingType.PAGERANK) {
      System.out.println(queryType + " " + rankingType);
      return pageRankQuery(query);
    }
    // combination - tf-idf + pagerank
    if (queryType == QueryType.RANKED_QUERY && rankingType == RankingType.COMBINATION) {
      System.out.println(queryType + " " + rankingType);
      return combinationQuery(query, normType);
    }

    // HITS (Hypertext-Induced Topic Selection), Hubs and Authorities
    if (queryType == QueryType.RANKED_QUERY && rankingType == RankingType.HITS) {
      System.out.println(queryType + " " + rankingType);
      return hitsRankQuery(query);
    }
    return null;
  }

  /**
   * HITS (Hypertext-Induced Topic Selection) query
   *
   * @param query information requested by user
   * @return PostingList containing data requested by user
   */
  private PostingsList hitsRankQuery(Query query) {
    HashMap<Integer, Double> hitsScores = new HashMap<>();
    PostingsList result = new PostingsList();

    for (int i = 0; i < query.queryterm.size(); i++) {
      PostingsList postings = index.getPostings(query.queryterm.get(i).term);
      if (postings != null) {
        postings = hitsRank.rank(postings);
        for (int j = 0; j < postings.size(); j++) {
          int docID = postings.get(j).docID;
          double score = postings.get(j).score;
          if (hitsScores.containsKey(docID)) {
            hitsScores.put(docID, hitsScores.get(docID) + score);
          } else {
            hitsScores.put(docID, score);
          }
        }
      }
    }
    for (Integer docID : hitsScores.keySet()) {
      result.add(docID, hitsScores.get(docID), 0);
    }

    // rank results from most relevant to least relevant based on their scores
    result.sort();

    return result;
  }

  /**
   * Combination Query: tf-idf + pagerank
   *
   * @param query information requested by user
   * @param normType normalization measure
   * @return PostingList containing data requested by user
   */
  private PostingsList combinationQuery(Query query, NormalizationType normType) {
    // # documents in corpus - 17,478 documents in  davisWiki Corpus
    int N = index.docLengths.size();
    double[] scores = new double[N];
    PostingsList result = new PostingsList();

    // TF_IDF
    for (int i = 0; i < query.queryterm.size(); i++) {
      PostingsList postings = index.getPostings(query.queryterm.get(i).term);
      if (postings != null) {
        // # documents in corpus that contain term (document frequency)
        int df = postings.size();
        // inverse document-frequency (idf)
        double idf = Math.log((double) N / df);
        for (int j = 0; j < postings.size(); j++) {
          int docID = postings.get(j).docID;
          // # occurrences of token in document (term frequency)
          double tf = postings.get(j).positionList.size();
          // TF_IDF weight
          scores[docID] += tf * idf * query.queryterm.get(i).weight;
        }
      }
    }

    // TF_IDF normalization
    for (int k = 0; k < N; k++) {
      if (scores[k] > 0) {
        if (normType == NormalizationType.NUMBER_OF_WORDS) {
          //          System.out.println("Document Length Distance");
          scores[k] /= index.docLengths.get(k);
        }
        if (normType == NormalizationType.EUCLIDEAN) {
          //          System.out.println("Euclidean Distance");
          scores[k] /= index.docEuclideanDistances.get(k);
        }
      }
    }

    // PageRank
    for (int i = 0; i < query.queryterm.size(); i++) {
      PostingsList postings = index.getPostings(query.queryterm.get(i).term);
      if (postings != null) {
        for (int j = 0; j < postings.size(); j++) {
          int docID = postings.get(j).docID;
          String docName = index.docNames.get(docID);
          scores[docID] =
              (scores[docID] * TF_IDF_WEIGHT)
                  + (PAGE_RANK_WEIGHT * pageRank.getPageRankScore(docName));
        }
      }
    }

    for (int k = 0; k < N; k++) {
      if (scores[k] > 0) {
        result.add(k, scores[k], 0);
      }
    }

    // rank results from most relevant to least relevant based on their scores
    result.sort();

    return result;
  }

  /**
   * PageRank Query
   *
   * @param query information requested by user
   * @return PostingList containing data requested by user
   */
  private PostingsList pageRankQuery(Query query) {
    // # documents in corpus - 17,478 documents in  davisWiki Corpus
    int N = index.docLengths.size();
    double[] scores = new double[N];
    PostingsList result = new PostingsList();

    for (int i = 0; i < query.queryterm.size(); i++) {
      PostingsList postings = index.getPostings(query.queryterm.get(i).term);
      if (postings != null) {
        for (int j = 0; j < postings.size(); j++) {
          int docID = postings.get(j).docID;
          String docName = index.docNames.get(docID);
          scores[docID] += pageRank.getPageRankScore(docName);
        }
      }
    }

    for (int k = 0; k < N; k++) {
      if (scores[k] > 0) {
        result.add(k, scores[k], 0);
      }
    }

    // rank results from most relevant to least relevant based on their scores
    result.sort();

    return result;
  }

  /**
   * TF-IDF Ranking Query
   *
   * @param query information requested by user
   * @param normType normalization measure
   * @return PostingList containing data requested by user
   */
  private PostingsList tf_idfQuery(Query query, NormalizationType normType) {
    // # documents in corpus - 17,478 documents in davisWiki Corpus
    int N = index.docLengths.size();
    double[] scores = new double[N];
    PostingsList result = new PostingsList();

    for (int i = 0; i < query.queryterm.size(); i++) {
      PostingsList postings = index.getPostings(query.queryterm.get(i).term);
      if (postings != null) {
        // # documents in corpus that contain term (document frequency)
        int df = postings.size();
        // inverse document-frequency(idf)
        double idf = Math.log((double) N / df);
        for (int j = 0; j < postings.size(); j++) {
          int docID = postings.get(j).docID;
          // # occurrences of token in document (term frequency)
          double tf = postings.get(j).positionList.size();
          // TF_IDF weight
          scores[docID] += tf * idf * query.queryterm.get(i).weight;
        }
      }
    }

    // normalization
    System.out.println("NORMALIZATION_TYPE -> " + normType.toString());
    for (int k = 0; k < N; k++) {
      if (scores[k] > 0) {
        if (normType == NormalizationType.NUMBER_OF_WORDS) {
          scores[k] /= index.docLengths.get(k);
        }
        if (normType == NormalizationType.EUCLIDEAN) {
          scores[k] /= index.docEuclideanDistances.get(k);
        }
        result.add(k, scores[k], 0);
      }
    }

    // rank results from most relevant to least relevant based on their scores
    result.sort();

    return result;
  }

  /**
   * Phrase Query
   *
   * @param query information requested by user
   * @return PostingsList containing data requested by user
   */
  private PostingsList phraseQuery(Query query) {
    ArrayList<PostingsList> postingsLists = getPostingsList(query);

    // size(postingsLists) == 0
    if (postingsLists.isEmpty()) {
      return new PostingsList();
    }

    PostingsList result = postingsLists.getFirst();
    for (int i = 1; i < postingsLists.size(); i++) {
      result = positionalIntersect(result, postingsLists.get(i));
    }

    return result;
  }

  /**
   * Compute the positional set intersection of two posting lists
   *
   * @param p1 first postings list
   * @param p2 second postings list
   * @return postings list that contains the positional intersection of p1 and p2 postings lists
   */
  private PostingsList positionalIntersect(PostingsList p1, PostingsList p2) {
    PostingsList result = new PostingsList();
    int i = 0;
    int j = 0;

    while (i < p1.size() && j < p2.size()) {
      if (p1.get(i).docID == p2.get(j).docID) {
        ArrayList<Integer> positions = new ArrayList<>();
        for (int pos1 : p1.get(i).positionList) {
          for (int pos2 : p2.get(j).positionList) {
            // check if first term comes after next term
            if (pos2 == pos1 + 1) {
              positions.add(pos2);
            }
          }
        }
        // add the document containing position intersections to result
        if (!positions.isEmpty()) {
          PostingsEntry postingEntry = new PostingsEntry(p2.get(j).docID, 1, positions);
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
   * Multi-Word Query
   *
   * @param query information requested by user
   * @return PostingList containing data requested by user
   */
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
   * @return postings list that contains the intersection of p1 and p2 postings lists
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
