import java.util.*;
import java.io.*;

public class PageRank {

  /**
   * Maximal number of documents. We're assuming here that we don't have more docs than we can keep
   * in main memory.
   */
  static final int MAX_NUMBER_OF_DOCS = 2000000;

  /** Number of PageRanked documents to print */
  static final int TOP_N_PAGE_RANK_DOCS = 30;

  /** DavisWiki Titles */
  static final String DAVIS_TITLES = "./davisTitles.txt";

  /** DavisWiki document links */
  static final String LINKS_DAVIS = "./linksDavis.txt";

  /** SVWiki document links */
  static final String LINKS_SV_WIKI = "./linksSvwiki.txt";

  static final String SV_WIKI_TITLES = "./svwikiTitles.txt";

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

  HashMap<String, Double> mcEndPointRandomStartPageRank = new HashMap<>();
  HashMap<String, Double> mcEndPointCyclicStartPageRank = new HashMap<>();
  HashMap<String, Double> mcCompletePathStopDangleNodePageRank = new HashMap<>();
  HashMap<String, Double> mcCompletePathStopRandomStartPageRank = new HashMap<>();

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

  public PageRank(String filename) {
    System.out.println("Davis wiki!!!!");
    int noOfDocs = readDocs(filename);
    readTitles(DAVIS_TITLES);

    System.out.println("PageRank Power Iteration");
    double startTime = System.currentTimeMillis();
    iterate(noOfDocs, 1000);
    double endTime = System.currentTimeMillis();
    System.out.println("Time: " + (endTime - startTime) / 1000.0 + "s");

    System.out.println("PageRank MCEndPointRandomStart");
    double startTime1 = System.currentTimeMillis();
    mcEndPointRandomStart(noOfDocs, 1000 * noOfDocs);
    double endTime1 = System.currentTimeMillis();
    System.out.println("Time: " + (endTime1 - startTime1) / 1000.0 + "s");

    System.out.println("PageRank MCEndPointCyclicStart");
    double startTime2 = System.currentTimeMillis();
    mcEndPointCyclicStart(noOfDocs, 1000 * noOfDocs);
    double endTime2 = System.currentTimeMillis();
    System.out.println("Time: " + (endTime2 - startTime2) / 1000.0 + "s");

    System.out.println("PageRank MCCompletePathStopDangleNode");
    double startTime4 = System.currentTimeMillis();
    mcCompletePathStopDangleNode(noOfDocs, 1000 * noOfDocs);
    double endTime4 = System.currentTimeMillis();
    System.out.println("Time: " + (endTime4 - startTime4) / 1000.0 + "s");

    System.out.println("PageRank MCCompletePathStopRandomStart");
    double startTime5 = System.currentTimeMillis();
    mcCompletePathStopRandomStart(noOfDocs, 1000 * noOfDocs);
    double endTime5 = System.currentTimeMillis();
    System.out.println("Time: " + (endTime5 - startTime5) / 1000.0 + "s");
    experiments(noOfDocs);

    printTopDocs(TOP_N_PAGE_RANK_DOCS);
    writePageRankToFile();
    writeMCPageRankToFile();
  }

  public PageRank(String Filename, int flag) {
    System.out.println("Swedish wiki!!!");
    int noOfDocs = readDocs(Filename);
    readTitles(SV_WIKI_TITLES);

    System.out.println("PageRank MCCompletePathStopRandomStart");
    double startTime5 = System.currentTimeMillis();
    mcCompletePathStopRandomStart(noOfDocs, 20 * noOfDocs);
    double endTime5 = System.currentTimeMillis();
    System.out.println("Time: " + (endTime5 - startTime5) / 1000.0 + "s");
    printTopDocsSVWiki(30);
  }

  /**
   * Compute mc page rank for different methods
   *
   * @param noOfDocs - the number of documents in the corpus
   */
  void experiments(int noOfDocs) {
    System.out.println("Monte-Carlo Page Rank Experiments.......");
    int[] N = new int[] {5, 10, 20, 50, 100};
    for (int i = 0; i < N.length; i++) {
      System.out.println("Number of walks N: " + N[i]);
      int numSteps = noOfDocs * N[i];
      mcEndPointRandomStart(noOfDocs, numSteps);
      mcEndPointCyclicStart(noOfDocs, numSteps);
      mcCompletePathStopDangleNode(noOfDocs, numSteps);
      mcCompletePathStopRandomStart(noOfDocs, numSteps);
      System.out.println(
          "Top 30 results - mcEndPointRandomStart page rank difference: "
              + computeTop30Difference(mcEndPointRandomStartPageRank));
      System.out.println(
          "Top 30 results - mcEndPointCyclicStart page rank difference: "
              + computeTop30Difference(mcEndPointCyclicStartPageRank));
      System.out.println(
          "Top 30 results - mcCompletePathStopDangleNode page rank difference: "
              + computeTop30Difference(mcCompletePathStopDangleNodePageRank));
      System.out.println(
          "Top 30 results - mcCompletePathStopRandomStart page rank difference: "
              + computeTop30Difference(mcCompletePathStopRandomStartPageRank));
    }
  }

  /**
   * Compute the squared difference error for the top 30 documents in the pagerank results
   *
   * @param mcPageRankScores the results from a mc pagerank simulation run
   * @return square difference between pagerank top 30 results mc pagerank top 30 results
   */
  public double computeTop30Difference(HashMap<String, Double> mcPageRankScores) {
    double difference = 0.0;
    for (int i = 0; i < 30; i++) {
      String docNameID = sortedDocNames.get(i);
      HashMap<String, Double> docPageRank = docIDTitlePageRank.get(docNameID);
      String docTitle = docIDTitle.get(docNameID);
      Double docPageRankScore = docPageRank.get(docTitle);
      if (mcPageRankScores.containsKey(docTitle)) {
        difference += Math.pow(docPageRankScore - mcPageRankScores.get(docTitle), 2);
      }
    }
    return difference;
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

      // convergence check
      diff = 0.0;
      for (int w = 0; w < numberOfDocs; w++) {
        diff += Math.abs(updatePageRankScores[w] - pageRankScores[w]);
        pageRankScores[w] = updatePageRankScores[w];
      }

      // normalization
      double pageRankTotalSum = 0.0;
      for (int i = 0; i < numberOfDocs; i++) {
        pageRankTotalSum += pageRankScores[i];
      }
      for (int i = 0; i < numberOfDocs; i++) {
        pageRankScores[i] /= pageRankTotalSum;
      }
      iter++;
      System.out.println("Iteration: " + iter + " - " + "Diff: " + diff);
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
   * Monte-Carlo Endpoint Random Start
   *
   * @param numberOfDocs the number of documents in the network
   * @param maxSteps the number of iterations
   * @return approximated page rank scores
   */
  public void mcEndPointRandomStart(int numberOfDocs, int maxSteps) {
    double[] mcPageRankScores = new double[numberOfDocs];
    int randDocID;
    Set<Integer> outlinks;
    Random randomNumber = new Random();

    for (int i = 0; i < maxSteps; i++) {
      randDocID = randomNumber.nextInt(numberOfDocs);
      while (Math.random() > BORED) {
        try {
          outlinks = link.get(randDocID).keySet();
        } catch (NullPointerException e) {
          randDocID = randomNumber.nextInt(numberOfDocs);
          continue;
        }
        randDocID = (int) outlinks.toArray()[(int) (Math.random() * outlinks.size())];
      }
      mcPageRankScores[randDocID]++;
    }

    for (int i = 0; i < numberOfDocs; i++) {
      mcPageRankScores[i] /= maxSteps;
    }

    ArrayList<Integer> documentIDs = new ArrayList<>();
    ArrayList<String> mcsortedDocNames = new ArrayList<>();
    ArrayList<String> mcsortedDocTitles = new ArrayList<>();
    ArrayList<Double> mcsortedPageRankScores = new ArrayList<>();

    for (int i = 0; i < numberOfDocs; i++) {
      documentIDs.add(i);
    }
    documentIDs.sort((a, b) -> Double.compare(mcPageRankScores[b], mcPageRankScores[a]));
    for (int docID : documentIDs) {
      String docNameID = docName[docID];
      String docTitle = docIDTitle.get(docNameID);
      Double docPageRankScore = mcPageRankScores[docID];

      mcsortedDocNames.add(docNameID);
      mcsortedDocTitles.add(docTitle);
      mcsortedPageRankScores.add(docPageRankScore);

      HashMap<String, Double> docPageRank = new HashMap<>();
      docPageRank.put(docTitle, docPageRankScore);
      mcEndPointRandomStartPageRank.put(docTitle, docPageRankScore);
    }
  }

  /**
   * Monte-Carlo End Point Cyclic Start PageRank
   *
   * @param numberOfDocs number of documents in the network
   * @param maxSteps the number of iterations for monet-carlo simulation
   * @return approximated page rank scores
   */
  public void mcEndPointCyclicStart(int numberOfDocs, int maxSteps) {
    double[] mcPageRankScores = new double[numberOfDocs];
    Set<Integer> outlinks;
    int m = 1;

    for (int i = 0; i < numberOfDocs; i++) {
      for (int j = 0; j < m; j++) {
        int randDocID = i;
        while (Math.random() > BORED) {
          try {
            outlinks = link.get(randDocID).keySet();
          } catch (NullPointerException e) {
            randDocID = (int) (Math.random() * numberOfDocs);
            continue;
          }
          randDocID = (int) outlinks.toArray()[(int) (Math.random() * outlinks.size())];
        }
        mcPageRankScores[randDocID]++;
      }
    }

    for (int i = 0; i < numberOfDocs; i++) {
      mcPageRankScores[i] /= maxSteps;
    }

    ArrayList<Integer> documentIDs = new ArrayList<>();
    ArrayList<String> mcsortedDocNames = new ArrayList<>();
    ArrayList<String> mcsortedDocTitles = new ArrayList<>();
    ArrayList<Double> mcsortedPageRankScores = new ArrayList<>();

    for (int i = 0; i < numberOfDocs; i++) {
      documentIDs.add(i);
    }
    documentIDs.sort((a, b) -> Double.compare(mcPageRankScores[b], mcPageRankScores[a]));
    for (int docID : documentIDs) {
      String docNameID = docName[docID];
      String docTitle = docIDTitle.get(docNameID);
      Double docPageRankScore = mcPageRankScores[docID];

      mcsortedDocNames.add(docNameID);
      mcsortedDocTitles.add(docTitle);
      mcsortedPageRankScores.add(docPageRankScore);

      HashMap<String, Double> docPageRank = new HashMap<>();
      docPageRank.put(docTitle, docPageRankScore);
      mcEndPointCyclicStartPageRank.put(docTitle, docPageRankScore);
    }
  }

  /**
   * Monte-Carlo Complete Path Stopping at Dangling Nodes
   *
   * @param numberOfDocs the number of documents in the network
   * @param maxSteps the number of iterations for monte-carlo simulation
   * @return approximated page rank scores
   */
  public void mcCompletePathStopDangleNode(int numberOfDocs, int maxSteps) {
    double[] mcPageRankScores = new double[numberOfDocs];
    int m = 1;
    Set<Integer> outlinks;
    int visit = 0;

    for (int i = 0; i < numberOfDocs; i++) {
      for (int j = 0; j < m; j++) {
        int randDocID = i;
        while (Math.random() > BORED) {
          mcPageRankScores[randDocID]++;
          visit++;
          try {
            outlinks = link.get(randDocID).keySet();
          } catch (NullPointerException e) {
            break;
          }
          randDocID = (int) outlinks.toArray()[(int) (Math.random() * outlinks.size())];
        }
      }
    }

    for (int i = 0; i < numberOfDocs; i++) {
      mcPageRankScores[i] /= visit;
    }

    ArrayList<Integer> documentIDs = new ArrayList<>();
    ArrayList<String> mcsortedDocNames = new ArrayList<>();
    ArrayList<String> mcsortedDocTitles = new ArrayList<>();
    ArrayList<Double> mcsortedPageRankScores = new ArrayList<>();

    for (int i = 0; i < numberOfDocs; i++) {
      documentIDs.add(i);
    }
    documentIDs.sort((a, b) -> Double.compare(mcPageRankScores[b], mcPageRankScores[a]));
    for (int docID : documentIDs) {
      String docNameID = docName[docID];
      String docTitle = docIDTitle.get(docNameID);
      Double docPageRankScore = mcPageRankScores[docID];

      mcsortedDocNames.add(docNameID);
      mcsortedDocTitles.add(docTitle);
      mcsortedPageRankScores.add(docPageRankScore);

      HashMap<String, Double> docPageRank = new HashMap<>();
      docPageRank.put(docTitle, docPageRankScore);
      mcCompletePathStopDangleNodePageRank.put(docTitle, docPageRankScore);
    }
  }

  /**
   * @param numberOfDocs the number of documents in the network
   * @param maxSteps the number of iterations for monte-carlo simulation
   * @return approximated page rank score
   */
  public void mcCompletePathStopRandomStart(int numberOfDocs, int maxSteps) {
    double[] mcPageRankScores = new double[numberOfDocs];
    int randDocID;
    Set<Integer> outlinks;
    Random randomNumber = new Random();
    int visit = 0;
    for (int i = 0; i < maxSteps; i++) {
      randDocID = randomNumber.nextInt(numberOfDocs);
      while (Math.random() > BORED) {
        mcPageRankScores[randDocID]++;
        visit++;
        try {
          outlinks = link.get(randDocID).keySet();
        } catch (NullPointerException e) {
          break;
        }
        randDocID = (int) outlinks.toArray()[(int) (Math.random() * outlinks.size())];
      }
    }

    for (int i = 0; i < numberOfDocs; i++) {
      mcPageRankScores[i] /= visit;
    }

    ArrayList<Integer> documentIDs = new ArrayList<>();
    ArrayList<String> mcsortedDocNames = new ArrayList<>();
    ArrayList<String> mcsortedDocTitles = new ArrayList<>();
    ArrayList<Double> mcsortedPageRankScores = new ArrayList<>();

    for (int i = 0; i < numberOfDocs; i++) {
      documentIDs.add(i);
    }
    documentIDs.sort((a, b) -> Double.compare(mcPageRankScores[b], mcPageRankScores[a]));
    for (int docID : documentIDs) {
      String docNameID = docName[docID];
      String docTitle = docIDTitle.get(docNameID);
      Double docPageRankScore = mcPageRankScores[docID];

      mcsortedDocNames.add(docNameID);
      mcsortedDocTitles.add(docTitle);
      mcsortedPageRankScores.add(docPageRankScore);

      HashMap<String, Double> docPageRank = new HashMap<>();
      docPageRank.put(docTitle, docPageRankScore);
      mcCompletePathStopRandomStartPageRank.put(docTitle, docPageRankScore);
    }
  }

  /**
   * Get the PageRank score for a document in the daviswiki dataset
   *
   * @param docName document name
   * @return PageRank score
   */
  public double getPageRankScore(String docName) {
    System.out.println("ENTER GET PAGE RANK SCORE FUNCTION");
    String fileName =
        docName.lastIndexOf("/") > 0 ? docName.substring(docName.lastIndexOf("/") + 1) : docName;
    //    System.out.println("FILE NAME: " + fileName);
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

    System.out.println(
        "\nTop " + numberOfDocs + " documents by PageRank mcEndPointRandomStart score:");
    List<Map.Entry<String, Double>> algorithm1 =
        new ArrayList<>(mcEndPointRandomStartPageRank.entrySet());
    algorithm1.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
    for (int i = 0; i < numberOfDocs; i++) {
      Map.Entry<String, Double> entry = algorithm1.get(i);
      String docTitle = entry.getKey();
      double mcPageRankScore = entry.getValue();
      System.out.printf("%s %.5f\n", docTitle, mcPageRankScore);
    }
    System.out.println(
        "\nTop " + numberOfDocs + " documents by PageRank mcEndPointCyclicStart score:");
    List<Map.Entry<String, Double>> algorithm2 =
        new ArrayList<>(mcEndPointCyclicStartPageRank.entrySet());
    algorithm2.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
    for (int i = 0; i < numberOfDocs; i++) {
      Map.Entry<String, Double> entry = algorithm2.get(i);
      String docTitle = entry.getKey();
      double mcPageRankScore = entry.getValue();
      System.out.printf("%s %.5f\n", docTitle, mcPageRankScore);
    }
    System.out.println(
        "\nTop " + numberOfDocs + " documents by PageRank mcCompletePathStopDangleNode score:");
    List<Map.Entry<String, Double>> algorithm4 =
        new ArrayList<>(mcCompletePathStopDangleNodePageRank.entrySet());
    algorithm4.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
    for (int i = 0; i < numberOfDocs; i++) {
      Map.Entry<String, Double> entry = algorithm4.get(i);
      String docTitle = entry.getKey();
      double mcPageRankScore = entry.getValue();
      System.out.printf("%s %.5f\n", docTitle, mcPageRankScore);
    }
    System.out.println(
        "\nTop " + numberOfDocs + " documents by PageRank mcCompletePathStopRandomStart score:");
    List<Map.Entry<String, Double>> algorithm5 =
        new ArrayList<>(mcCompletePathStopRandomStartPageRank.entrySet());
    algorithm5.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
    for (int i = 0; i < numberOfDocs; i++) {
      Map.Entry<String, Double> entry = algorithm5.get(i);
      String docTitle = entry.getKey();
      double mcPageRankScore = entry.getValue();
      System.out.printf("%s %.5f\n", docTitle, mcPageRankScore);
    }
  }

  /**
   * print the top 30 results for the sv wiki
   *
   * @param numberOfDocs number of top ranking documents to print
   */
  public void printTopDocsSVWiki(int numberOfDocs) {
    System.out.println(
        "\nTop "
            + numberOfDocs
            + " documents by PageRank mcCompletePathStopRandomStart score SVWiki:");
    List<Map.Entry<String, Double>> algorithm5 =
        new ArrayList<>(mcCompletePathStopRandomStartPageRank.entrySet());
    algorithm5.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
    for (int i = 0; i < numberOfDocs; i++) {
      Map.Entry<String, Double> entry = algorithm5.get(i);
      String docTitle = entry.getKey();
      double mcPageRankScore = entry.getValue();
      System.out.printf("%s %.5f\n", docTitle, mcPageRankScore);
    }
  }

  /** Write PageRank scores to disk */
  void writePageRankToFile() {
    try {
      System.err.println("Writing PageRank results to disk...");
      BufferedWriter writer = new BufferedWriter(new FileWriter("rank_file.txt"));
      for (String docNameID : sortedDocNames) {
        HashMap<String, Double> docPageRank = docIDTitlePageRank.get(docNameID);
        String docTitle = docIDTitle.get(docNameID);
        Double docPageRankScore = docPageRank.get(docTitle);
        writer.write(String.format("%s %s %.5f\n", docNameID, docTitle, docPageRankScore));
      }
      writer.close();
      System.err.println("done!");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** write mcPageRank results to disk */
  void writeMCPageRankToFile() {
    try {
      System.err.println("Writing MCPageRank results to disk...");
      BufferedWriter writer1 = new BufferedWriter(new FileWriter("mcEndPointRandomStart.txt"));
      BufferedWriter writer2 = new BufferedWriter(new FileWriter("mcEndPointCyclicStart.txt"));
      BufferedWriter writer4 =
          new BufferedWriter(new FileWriter("mcCompletePathStopDangleNode.txt"));
      BufferedWriter writer5 =
          new BufferedWriter(new FileWriter("mcCompletePathStopRandomStart.txt"));

      for (String docNameID : sortedDocNames) {
        String docTitle = docIDTitle.get(docNameID);
        Double docPageRankScore = mcEndPointRandomStartPageRank.get(docTitle);
        writer1.write(String.format("%s %s %.5f\n", docNameID, docTitle, docPageRankScore));
      }
      writer1.close();

      for (String docNameID : sortedDocNames) {
        String docTitle = docIDTitle.get(docNameID);
        Double docPageRankScore = mcEndPointCyclicStartPageRank.get(docTitle);
        writer2.write(String.format("%s %s %.5f\n", docNameID, docTitle, docPageRankScore));
      }
      writer2.close();

      for (String docNameID : sortedDocNames) {
        String docTitle = docIDTitle.get(docNameID);
        Double docPageRankScore = mcCompletePathStopDangleNodePageRank.get(docTitle);
        writer4.write(String.format("%s %s %.5f\n", docNameID, docTitle, docPageRankScore));
      }
      writer4.close();

      for (String docNameID : sortedDocNames) {
        String docTitle = docIDTitle.get(docNameID);
        Double docPageRankScore = mcCompletePathStopRandomStartPageRank.get(docTitle);
        writer5.write(String.format("%s %s %.5f\n", docNameID, docTitle, docPageRankScore));
      }
      writer5.close();

      System.err.println("done!");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /* --------------------------------------------- */

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Please give the name of the link file");
    } else if (args[0].equals(LINKS_SV_WIKI)) {
      new PageRank(args[0], 1);
    } else {
      new PageRank(args[0]);
    }
  }
}
