/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.util.*;

public class SpellChecker {
  /** The regular inverted index to be used by the spell checker */
  Index index;

  /** K-gram index to be used by the spell checker */
  KGramIndex kgIndex;

  /** The auxiliary class for containing the value of your ranking function for a token */
  class KGramStat implements Comparable {
    double score;
    String token;

    KGramStat(String token, double score) {
      this.token = token;
      this.score = score;
    }

    public String getToken() {
      return token;
    }

    public int compareTo(Object other) {
      if (this.score == ((KGramStat) other).score) return 0;
      return this.score < ((KGramStat) other).score ? -1 : 1;
    }

    public String toString() {
      return token + ";" + score;
    }
  }

  /**
   * The threshold for Jaccard coefficient; a candidate spelling correction should pass the
   * threshold in order to be accepted
   */
  private static final double JACCARD_THRESHOLD = 0.4;

  /** The threshold for edit distance for a candidate spelling correction to be accepted. */
  private static final int MAX_EDIT_DISTANCE = 2;

  public SpellChecker(Index index, KGramIndex kgIndex) {
    this.index = index;
    this.kgIndex = kgIndex;
  }

  /**
   * Computes the Jaccard coefficient for two sets A and B, where the size of set A is <code>szA
   * </code>, the size of set B is <code>szB</code> and the intersection of the two sets contains
   * <code>intersection</code> elements.
   */
  private double jaccard(int szA, int szB, int intersection) {
    return (double) intersection / (szA + szB - intersection);
  }

  /**
   * Computing Levenshtein edit distance using dynamic programming. Allowed operations are: =>
   * insert (cost 1) => delete (cost 1) => substitute (cost 2)
   */
  private int editDistance(String s1, String s2) {
    int[][] cache = new int[s1.length() + 1][s2.length() + 1];
    for (int i = 0; i <= s1.length(); i++) {
      for (int j = 0; j <= s2.length(); j++) {
        if (i == 0) {
          cache[i][j] = j;
        } else if (j == 0) {
          cache[i][j] = i;
        } else {
          cache[i][j] =
              Math.min(
                  cache[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 2),
                  Math.min(cache[i - 1][j] + 1, cache[i][j - 1] + 1));
        }
      }
    }

    return cache[s1.length()][s2.length()];
  }

  /**
   * Checks spelling of all terms in <code>query</code> and returns up to <code>limit</code> ranked
   * suggestions for spelling correction.
   */
  public String[] check(Query query, int limit) {

    int numQueryTerms = query.queryterm.size();
    // size(query terms) == 0
    if (numQueryTerms == 0) {
      return new String[0];
    }

    // one word query
    if (numQueryTerms == 1) {
      String term = query.queryterm.getFirst().term;
      // check if the term is spelled correctly
      // return the term if it exists in the index
      if (index.getPostings(term) != null) {
        return new String[] {term};
      }

      // get all possible corrections for mispelled word
      List<KGramStat> suggestedCorrections = rankedCorrections(term);

      // return up to limit or suggestedcorrection size whichever is smaller
      String[] result = new String[Math.min(limit, suggestedCorrections.size())];
      for (int i = 0; i < result.length; i++) {
        result[i] = suggestedCorrections.get(i).getToken();
      }
      return result;
    }

    // multi-word query
    List<List<KGramStat>> queryTermCorrections = new ArrayList<>();
    for (Query.QueryTerm queryTerm : query.queryterm) {
      String term = queryTerm.term;
      // check if term is spelled correctly
      if (index.getPostings(term) != null) {
        List<KGramStat> correctSpelling = new ArrayList<>();
        correctSpelling.add(new KGramStat(term, 1.0));
        queryTermCorrections.add(correctSpelling);
      } else {
        List<KGramStat> suggestedCorrections = rankedCorrections(term);
        if (suggestedCorrections.isEmpty()) {
          suggestedCorrections = new ArrayList<>();
          suggestedCorrections.add(new KGramStat(term, Double.MAX_VALUE));
        }
        queryTermCorrections.add(suggestedCorrections);
      }
    }

    // merge corrections
    List<KGramStat> mergedPhrases = mergeCorrections(queryTermCorrections, limit);

    // return to limit or mergedPhrases size whichever is smaller
    String[] result = new String[Math.min(limit, mergedPhrases.size())];
    for (int i = 0; i < result.length; i++) {
      result[i] = mergedPhrases.get(i).getToken();
    }

    return result;
  }

  /**
   * ranking single word correction queries
   *
   * @param term word to be corrected
   * @return a list of ranked possible words
   */
  protected List<KGramStat> rankedCorrections(String term) {
    //    1. do union search in the kgram index for term
    //    2. calculate the jacquard coefficient between term and words
    //    3. if jc > threshold for word w, calculate edit distance between w and term
    //    4. if edit distance < other threshhold, w is a possible correction
    //    add w to list of corrections

    // kGrams for mispelled words
    HashSet<String> kGrams = new HashSet<>();
    String kGramToken = "^" + term + "$";
    for (int i = 0; i <= kGramToken.length() - kgIndex.getK(); i++) {
      kGrams.add(kGramToken.substring(i, i + kgIndex.getK()));
    }

    // kgram union search
    HashSet<String> words = new HashSet<>();
    for (String kgram : kGrams) {
      List<KGramPostingsEntry> kgramPostings = kgIndex.getPostings(kgram);
      if (kgramPostings != null) {
        for (KGramPostingsEntry entry : kgramPostings) {
          words.add(kgIndex.id2term.get(entry.tokenID));
        }
      }
    }

    // k-gram overlap - jaccard coefficients
    HashMap<String, Double> jaccardCoeffs = new HashMap<>();

    // construct kgrams for words for jaccard computation
    for (String word : words) {
      HashSet<String> wordKGrams = new HashSet<>();
      String wordkGramToken = "^" + word + "$";
      for (int i = 0; i <= wordkGramToken.length() - kgIndex.getK(); i++) {
        wordKGrams.add(wordkGramToken.substring(i, i + kgIndex.getK()));
      }

      // compute jaccard coefficient
      int szA = kGrams.size();
      int szB = wordKGrams.size();
      int intersection = 0;
      for (String kgram : kGrams) {
        if (wordKGrams.contains(kgram)) {
          intersection++;
        }
      }
      double jaccardCoeff = jaccard(szA, szB, intersection);

      // filter by jaccard threshold
      if (jaccardCoeff >= JACCARD_THRESHOLD) {
        jaccardCoeffs.put(word, jaccardCoeff);
      }
    }

    // calculate edit distance for jaccard filtered words
    ArrayList<KGramStat> result = new ArrayList<>();
    for (String candidateWord : jaccardCoeffs.keySet()) {
      int editDistance = editDistance(term, candidateWord);
      if (editDistance <= MAX_EDIT_DISTANCE) {
        double jaccardCoeff = jaccardCoeffs.get(candidateWord);
        PostingsList postings = index.getPostings(candidateWord);
        // rank the scores
        double kStatScore;

        if (postings != null) {
          kStatScore = (double) editDistance / (postings.size() * jaccardCoeff);
        } else {
          kStatScore = (double) editDistance / jaccardCoeff;
        }
        result.add(new KGramStat(candidateWord, kStatScore));
      }
    }
    Collections.sort(result);

    return result;
  }

  /**
   * Merging ranked candidate spelling corrections for all query terms available in <code>
   * qCorrections</code> into one final merging of query phrases. Returns up to <code>limit</code>
   * corrected phrases.
   */
  private List<KGramStat> mergeCorrections(List<List<KGramStat>> qCorrections, int limit) {
    if (qCorrections.isEmpty()) {
      return new ArrayList<>();
    }
    List<KGramStat> currentTermCorrectionList = qCorrections.getFirst();
    List<KGramStat> queryPhrases = new ArrayList<>(currentTermCorrectionList);
    for (int i = 1; i < qCorrections.size(); i++) {
      currentTermCorrectionList = qCorrections.get(i);
      List<KGramStat> currentPhrases = new ArrayList<>();
      for (KGramStat phrase : queryPhrases) {
        for (KGramStat currentTerm : currentTermCorrectionList) {
          currentPhrases.add(
              new KGramStat(
                  phrase.getToken() + " " + currentTerm.getToken(),
                  phrase.score * currentTerm.score));
        }
      }
      Collections.sort(currentPhrases);
      queryPhrases = currentPhrases.subList(0, Math.min(limit, currentPhrases.size()));
    }
    return queryPhrases;
  }
}
