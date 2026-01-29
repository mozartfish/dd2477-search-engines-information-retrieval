package ir;

import java.util.*;
import java.io.*;

public class PageRank {

  /**
   * Maximal number of documents. We're assuming here that we don't have more docs than we can keep
   * in main memory.
   */
  static final int MAX_NUMBER_OF_DOCS = 2000000;

  /** disk for writing and reading page rank scores from */
  public static final String PAGERANK_INDEX_DIR = "../rank-disk";

  /** Number of PageRanked documents to print */
  static final int TOP_N_PAGE_RANK_DOCS = 30;

  /** DavisWiki Titles */
  static final String DAVIS_TITLES =
      "../dd2477-search-engines-information-retrieval/rank-disk/davisTitles.txt";

  /** DavisWiki document links */
  static final String LINKS_DAVIS =
      "../dd2477-search-engines-information-retrieval/rank-disk/linksDavis.txt";


  /** Mapping from document names to document numbers. */
  HashMap<String, Integer> docNumber = new HashMap<String, Integer>();

  /** Mapping from documentID to document title */
  HashMap<String, String> docIDTitle = new HashMap<>();

  /** Mapping from document numbers to document names */
  String[] docName = new String[MAX_NUMBER_OF_DOCS];

  /** Document numbers list */
  ArrayList<Integer> documentIDs = new ArrayList<>();

  /** Sorted document names - sorted by PageRank score */
  ArrayList<String> sortedDocNames = new ArrayList<>();

  /** Sorted document titles - sorted by PageRank score */
  ArrayList<String> sortedDocTitles = new ArrayList<>();

  /** The PageRank scores for all the documents. */
  double[] pageRankScores = new double[MAX_NUMBER_OF_DOCS];

  /** Sorted PageRank scores - mapped to the ordering of sortedDocNames */
  ArrayList<Double> sortedPageRankScores = new ArrayList<>();

  /** Map doc name to doc title and page rank score */
  HashMap<String, HashMap<String, Double>> docIDTitlePageRank = new HashMap<>();

  /** Map doc title to page rank score */
  HashMap<String, Double> docTitlePageRank = new HashMap<>();

  /**
   * A memory-efficient representation of the transition matrix. The outlinks are represented as a
   * HashMap, whose keys are the numbers of the documents linked from.
   *
   * <p>The value corresponding to key i is a HashMap whose keys are all the numbers of documents j
   * that i links to.
   *
   * <p>If there are no outlinks from i, then the value corresponding key i is null.
   */
  HashMap<Integer, HashMap<Integer, Boolean>> link =
      new HashMap<Integer, HashMap<Integer, Boolean>>();

  /** The number of outlinks from each node. */
  int[] out = new int[MAX_NUMBER_OF_DOCS];

  /**
   * The probability that the surfer will be bored, stop following links, and take a random jump
   * somewhere.
   */
  static final double BORED = 0.15;

  /**
   * Convergence criterion: Transition probabilities do not change more that EPSILON from one
   * iteration to another.
   */
  static final double EPSILON = 0.0001;

  /* --------------------------------------------- */

  public PageRank(String linksFileName, String titlesFileName) {
    int noOfDocs = readDocs(linksFileName);
    readTitles(titlesFileName);
    iterate(noOfDocs, 1000);
    writePageRankToFile();
  }

  /* --------------------------------------------- */

  /**
   * Reads the documents and fills the data structures.
   *
   * @return the number of documents read.
   */
  int readDocs(String filename) {
    int fileIndex = 0;
    try {
      System.err.print("Reading file... ");
      BufferedReader in = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = in.readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS) {
        int index = line.indexOf(";");
        String title = line.substring(0, index);
        Integer fromdoc = docNumber.get(title);
        //  Have we seen this document before?
        if (fromdoc == null) {
          // This is a previously unseen doc, so add it to the table.
          fromdoc = fileIndex++;
          docNumber.put(title, fromdoc);
          docName[fromdoc] = title;
        }
        // Check all outlinks.
        StringTokenizer tok = new StringTokenizer(line.substring(index + 1), ",");
        while (tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS) {
          String otherTitle = tok.nextToken();
          Integer otherDoc = docNumber.get(otherTitle);
          if (otherDoc == null) {
            // This is a previousy unseen doc, so add it to the table.
            otherDoc = fileIndex++;
            docNumber.put(otherTitle, otherDoc);
            docName[otherDoc] = otherTitle;
          }
          // Set the probability to 0 for now, to indicate that there is
          // a link from fromdoc to otherDoc.
          if (link.get(fromdoc) == null) {
            link.put(fromdoc, new HashMap<Integer, Boolean>());
          }
          if (link.get(fromdoc).get(otherDoc) == null) {
            link.get(fromdoc).put(otherDoc, true);
            out[fromdoc]++;
          }
        }
      }
      if (fileIndex >= MAX_NUMBER_OF_DOCS) {
        System.err.print("stopped reading since documents table is full. ");
      } else {
        System.err.print("done. ");
      }
    } catch (FileNotFoundException e) {
      System.err.println("File " + filename + " not found!");
    } catch (IOException e) {
      System.err.println("Error reading file " + filename);
    }
    System.err.println("Read " + fileIndex + " number of documents");
    return fileIndex;
  }

  /**
   * Read and process the titles in DavisWiki
   *
   * @param filename name of file containing titles
   */
  void readTitles(String filename) {
    try {
      File file = new File(filename);
      FileReader freader = new FileReader(file);
      BufferedReader br = new BufferedReader(freader);
      String line;
      while ((line = br.readLine()) != null) {
        String[] data = line.split(";");
        String docNumber = data[0];
        String docName = data[1];
        docIDTitle.put(docNumber, docName);
      }
      freader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /* --------------------------------------------- */

  /*
   *   Chooses a probability vector a, and repeatedly computes
   *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
   */
  void iterate(int numberOfDocs, int maxIterations) {
    // initialize PageRank scores with uniform probability
    double initProbability = 1.0 / numberOfDocs;
    for (int i = 0; i < numberOfDocs; i++) {
      pageRankScores[i] = initProbability;
    }
    double[] updatePageRankScores = new double[numberOfDocs];
    int iter = 0;
    double diff = Double.MAX_VALUE;

    // PageRank + power iteration algorithm
    while (iter < maxIterations && diff > EPSILON) {
      // reset new scores for current iteration
      Arrays.fill(updatePageRankScores, 0);
      double baseScore = BORED / numberOfDocs;
      for (int i = 0; i < numberOfDocs; i++) {
        updatePageRankScores[i] = baseScore;
      }

      // nodes with outlinks
      for (int j = 0; j < numberOfDocs; j++) {
        if (out[j] == 0) {
          continue;
        }
        double linkWeight = ((1.0 - BORED) * pageRankScores[j]) / out[j];
        HashMap<Integer, Boolean> outlinks = link.get(j);
        if (outlinks != null) {
          for (Integer target : outlinks.keySet()) {
            updatePageRankScores[target] += linkWeight;
          }
        }
      }
      // nodes without outlinks
      double totalDanglingWeight = 0.0;
      for (int k = 0; k < numberOfDocs; k++) {
        if (out[k] == 0) {
          totalDanglingWeight += pageRankScores[k];
        }
      }
      double danglingWeight = ((1.0 - BORED) * totalDanglingWeight) / numberOfDocs;
      for (int h = 0; h < numberOfDocs; h++) {
        updatePageRankScores[h] += danglingWeight;
      }

      // normalization
      double pageRankTotalSum = 0.0;
      for (int i = 0; i < numberOfDocs; i++) {
        pageRankTotalSum += pageRankScores[i];
      }
      for (int i = 0; i < numberOfDocs; i++) {
        pageRankScores[i] /= pageRankTotalSum;
      }

      // convergence check
      diff = 0.0;
      for (int w = 0; w < numberOfDocs; w++) {
        diff += Math.abs(updatePageRankScores[w] - pageRankScores[w]);
        pageRankScores[w] = updatePageRankScores[w];
      }

      iter++;
      System.out.println("PageRank Iteration: " + iter + ", Diff: " + diff);
    }

    // Sort documents by PageRank score
    for (int i = 0; i < numberOfDocs; i++) {
      documentIDs.add(i);
    }
    documentIDs.sort((a, b) -> Double.compare(pageRankScores[b], pageRankScores[a]));
    // map sorted page rank documents to appropriate data structures
    for (int docID : documentIDs) {
      String docNameID = docName[docID];
      String docTitle = docIDTitle.get(docNameID);
      Double docPageRankScore = pageRankScores[docID];

      sortedDocNames.add(docNameID);
      sortedDocTitles.add(docTitle);
      sortedPageRankScores.add(docPageRankScore);

      HashMap<String, Double> docPageRank = new HashMap<>();
      docPageRank.put(docTitle, docPageRankScore);
      docTitlePageRank.put(docTitle, docPageRankScore);
      docIDTitlePageRank.put(docNameID, docPageRank);
    }
  }

  /**
   * Get the PageRank score for a document in the daviswiki dataset
   *
   * @param docName document name
   * @return PageRank score
   */
  public double getPageRankScore(String docName) {
    String fileName =
        docName.lastIndexOf("/") > 0 ? docName.substring(docName.lastIndexOf("/") + 1) : docName;
    return docTitlePageRank.get(fileName);
  }

  /**
   * Print the top number of PageRanked Documents
   *
   * @param numberOfDocs - the number of documents
   */
  void printTopDocs(int numberOfDocs) {
    System.out.println("\nTop " + numberOfDocs + " documents by PageRank score:");
    for (int i = 0; i < numberOfDocs; i++) {
      String docNameID = sortedDocNames.get(i);
      HashMap<String, Double> docPageRank = docIDTitlePageRank.get(docNameID);
      String docTitle = docIDTitle.get(docNameID);
      Double docPageRankScore = docPageRank.get(docTitle);
      System.out.printf("%s %s %.5f\n", docNameID, docTitle, docPageRankScore);
    }
  }

  /** Write PageRank scores to disk */
  void writePageRankToFile() {
    try {
      System.err.println("Writing PageRank results to disk...");
      BufferedWriter writer =
          new BufferedWriter(new FileWriter(PAGERANK_INDEX_DIR + "/" + "rank_file.txt"));
      for (String docNameID : sortedDocNames) {
        HashMap<String, Double> docPageRank = docIDTitlePageRank.get(docNameID);
        String docTitle = docIDTitle.get(docNameID);
        Double docPageRankScore = docPageRank.get(docTitle);
        writer.write(String.format("%s %.5f\n", docNameID, docPageRankScore));
      }
      writer.close();
      System.err.println("done!");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
