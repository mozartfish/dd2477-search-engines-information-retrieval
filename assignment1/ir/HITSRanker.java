/**
 * Computes the Hubs and Authorities for an every document in a query-specific link graph, induced
 * by the base set of pages.
 *
 * @author Dmytro Kalpakchi
 */
package ir;

import java.util.*;
import java.io.*;

public class HITSRanker {
  /** Disk Path */
  public static final String HITSINDEXDIR = "../rank-disk";

  /** Max number of iterations for HITS */
  static final int MAX_NUMBER_OF_STEPS = 1000;

  /**
   * Convergence criterion: hub and authority scores do not change more that EPSILON from one
   * iteration to another.
   */
  static final double EPSILON = 0.001;

  /** Hubs Weight - inspired by the weight for combination query */
  double HUBS_WEIGHT = 1.0;

  /** Authority Weight - inspired by the weight for combination query */
  double AUTHORITY_WEIGHT = 1.0;

  /** The inverted index */
  Index index;

  /** Mapping from the titles to internal document ids used in the links file */
  HashMap<String, Integer> titleToId = new HashMap<String, Integer>();

  /** Outlinks - mapping internal document ids to all outgoing links from a node in the graph */
  HashMap<Integer, ArrayList<Integer>> outLinks = new HashMap<>();

  /** Inlinks - mapping internal document ids to all incoming links to a node in the graph */
  HashMap<Integer, ArrayList<Integer>> inLinks = new HashMap<>();

  /** Sparse vector containing hub scores */
  HashMap<Integer, Double> hubs;

  /** Sparse vector containing authority scores */
  HashMap<Integer, Double> authorities;

  /* --------------------------------------------- */

  /**
   * Constructs the HITSRanker object
   *
   * <p>A set of linked documents can be presented as a graph. Each page is a node in graph with a
   * distinct nodeID associated with it. There is an edge between two nodes if there is a link
   * between two pages.
   *
   * <p>Each line in the links file has the following format:
   * nodeID;outNodeID1,outNodeID2,...,outNodeIDK This means that there are edges between nodeID and
   * outNodeIDi, where i is between 1 and K.
   *
   * <p>Each line in the titles file has the following format: nodeID;pageTitle
   *
   * <p>NOTE: nodeIDs are consistent between these two files, but they are NOT the same as docIDs
   * used by search engine's Indexer
   *
   * @param linksFilename File containing the links of the graph
   * @param titlesFilename File containing the mapping between nodeIDs and pages titles
   * @param index The inverted index
   */
  public HITSRanker(String linksFilename, String titlesFilename, Index index) {
    this.index = index;
    readDocs(linksFilename, titlesFilename);
  }

  /* --------------------------------------------- */

  /**
   * A utility function that gets a file name given its path. For example, given the path
   * "davisWiki/hello.f", the function will return "hello.f".
   *
   * @param path The file path
   * @return The file name.
   */
  private String getFileName(String path) {
    String result = "";
    StringTokenizer tok = new StringTokenizer(path, "\\/");
    while (tok.hasMoreTokens()) {
      result = tok.nextToken();
    }
    return result;
  }

  /**
   * Reads the files describing the graph of the given set of pages.
   *
   * @param linksFilename File containing the links of the graph
   * @param titlesFilename File containing the mapping between nodeIDs and pages titles
   */
  void readDocs(String linksFilename, String titlesFilename) {
    // titles
    System.out.println("Read Titles...");
    try {
      File file = new File(titlesFilename);
      FileReader freader = new FileReader(file);
      BufferedReader br = new BufferedReader(freader);
      String line;
      while ((line = br.readLine()) != null) {
        String[] data = line.split(";");
        Integer docID = Integer.parseInt(data[0]);
        String docTitle = data[1];
        titleToId.put(docTitle, docID);
      }
      freader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // links
    System.out.println("Read links...");
    System.out.println("Process outlinks...");
    try {
      File file = new File(linksFilename);
      FileReader freader = new FileReader(file);
      BufferedReader br = new BufferedReader(freader);
      String line;
      while ((line = br.readLine()) != null) {
        String[] data = line.split(";");
        Integer docID = Integer.parseInt(data[0]);
        if (data.length > 1) {
          String outNodes = data[1];
          ArrayList<Integer> edges = new ArrayList<>();
          for (String node : outNodes.split(",")) {
            int outNode = Integer.parseInt(node);
            edges.add(outNode);
          }
          outLinks.put(docID, edges);
        }
      }
      freader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Process inlinks...");
    for (Map.Entry<Integer, ArrayList<Integer>> entry : outLinks.entrySet()) {
      Integer sourceNode = entry.getKey();
      ArrayList<Integer> edges = entry.getValue();
      for (Integer outNode : edges) {
        if (!inLinks.containsKey(outNode)) {
          inLinks.put(outNode, new ArrayList<>());
        }
        inLinks.get(outNode).add(sourceNode);
      }
    }
  }

  /**
   * Perform HITS iterations until convergence
   *
   * @param titles The titles of the documents in the root set
   */
  private void iterate(String[] titles) {
    // hubs and authority setup
    // root set
    HashSet<Integer> rootSet = new HashSet<>();
    for (String title : titles) {
      Integer docID = titleToId.get(title);
      rootSet.add(docID);
    }
    // base set - linking from
    HashSet<Integer> baseSet = new HashSet<>(rootSet);
    for (Integer docID : rootSet) {
      if (outLinks.containsKey(docID)) {
        ArrayList<Integer> edges = outLinks.get(docID);
        if (edges != null) {
          baseSet.addAll(edges);
        }
      }
    }
    // base set - linking to
    for (Map.Entry<Integer, ArrayList<Integer>> entry : outLinks.entrySet()) {
      Integer sourceNode = entry.getKey();
      ArrayList<Integer> edges = entry.getValue();
      for (Integer outNode : edges) {
        if (rootSet.contains(outNode)) {
          baseSet.add(sourceNode);
        }
      }
    }

    System.out.println("root set size -> " + rootSet.size());
    System.out.println("base set size -> " + baseSet.size());

    // hubs and authority - algorithm
    hubs = new HashMap<>();
    authorities = new HashMap<>();
    for (Integer docId : baseSet) {
      hubs.put(docId, 1.0);
      authorities.put(docId, 1.0);
    }

    int iter = 0;
    double diff = Double.MAX_VALUE;
    while (iter < MAX_NUMBER_OF_STEPS && diff > EPSILON) {
      HashMap<Integer, Double> newHubs = new HashMap<>();
      HashMap<Integer, Double> newAuthorities = new HashMap<>();
      // update authority score
      for (Integer docID : baseSet) {
        double authorityScore = 0.0;
        ArrayList<Integer> docInLinks = inLinks.get(docID);
        if (docInLinks != null) {
          for (Integer node : docInLinks) {
            if (baseSet.contains(node)) {
              authorityScore += hubs.get(node);
            }
          }
        }
        newAuthorities.put(docID, authorityScore);
      }
      // normalize authority score
      double authoritySumSquared = 0.0;
      for (Double authorityScore : newAuthorities.values()) {
        authoritySumSquared += authorityScore * authorityScore;
      }
      for (Integer docID : newAuthorities.keySet()) {
        double normalizedScore = newAuthorities.get(docID) / Math.sqrt(authoritySumSquared);
        newAuthorities.put(docID, normalizedScore);
      }
      // update hubs score
      for (Integer docID : baseSet) {
        double hubScore = 0.0;
        ArrayList<Integer> docOutLinks = outLinks.get(docID);
        if (docOutLinks != null) {
          for (Integer node : docOutLinks) {
            if (baseSet.contains(node)) {
              hubScore += newAuthorities.get(node);
            }
          }
        }
        newHubs.put(docID, hubScore);
      }
      // normalize hub score
      double hubSumSquared = 0.0;
      for (Double hubScore : newHubs.values()) {
        hubSumSquared += hubScore * hubScore;
      }
      for (Integer docID : newHubs.keySet()) {
        double normalizedScore = newHubs.get(docID) / Math.sqrt(hubSumSquared);
        newHubs.put(docID, normalizedScore);
      }

      // convergence check
      double authoritiesDiff = 0.0;
      for (Integer docID : newAuthorities.keySet()) {
        authoritiesDiff += Math.abs(newAuthorities.get(docID) - authorities.get(docID));
      }
      double hubsDiff = 0.0;
      for (Integer docID : newHubs.keySet()) {
        hubsDiff += Math.abs(newHubs.get(docID) - hubs.get(docID));
      }
      diff = 0.0;
      diff += authoritiesDiff + hubsDiff;

      // update scores
      authorities = newAuthorities;
      hubs = newHubs;
      iter++;
      System.out.println("HITS Iteration: " + iter + " | Diff: " + diff);
    }
  }

  /**
   * Rank the documents in the subgraph induced by the documents present in the postings list
   * `post`.
   *
   * @param post The list of postings fulfilling a certain information need
   * @return A list of postings ranked according to the hub and authority scores.
   */
  PostingsList rank(PostingsList post) {
    if (post == null || post.size() == 0) {
      return post;
    }
    PostingsList result = new PostingsList();

    // get data for hits
    String[] titles = new String[post.size()];
    for (int i = 0; i < post.size(); i++) {
      PostingsEntry entry = post.get(i);
      String docPath = index.docNames.get(entry.docID);
      String docTitle = getFileName(docPath);
      titles[i] = docTitle;
    }

    // run hits
    iterate(titles);

    // process scores
    HashMap<Integer, Double> docHitsScores = new HashMap<>();
    for (int i = 0; i < post.size(); i++) {
      PostingsEntry entry = post.get(i);
      String docPath = index.docNames.get(entry.docID);
      String docTitle = getFileName(docPath);
      Integer hitsID = titleToId.get(docTitle);
      double hubsScore = hubs.get(hitsID) * HUBS_WEIGHT;
      double authorityScore = authorities.get(hitsID) * AUTHORITY_WEIGHT;
      double hitsScore = hubsScore + authorityScore;
      docHitsScores.put(entry.docID, hitsScore);
    }

    // sort values
    docHitsScores = sortHashMapByValue(docHitsScores);
    for (Map.Entry<Integer, Double> entry : docHitsScores.entrySet()) {
      result.add(entry.getKey(), entry.getValue(), 0);
    }

    return result;
  }

  /**
   * Sort a hash map by values in the descending order
   *
   * @param map A hash map to sorted
   * @return A hash map sorted by values
   */
  private HashMap<Integer, Double> sortHashMapByValue(HashMap<Integer, Double> map) {
    if (map == null) {
      return null;
    } else {
      List<Map.Entry<Integer, Double>> list =
          new ArrayList<Map.Entry<Integer, Double>>(map.entrySet());

      Collections.sort(
          list,
          new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
              return (o2.getValue()).compareTo(o1.getValue());
            }
          });

      HashMap<Integer, Double> res = new LinkedHashMap<Integer, Double>();
      for (Map.Entry<Integer, Double> el : list) {
        res.put(el.getKey(), el.getValue());
      }
      return res;
    }
  }

  /**
   * Write the first `k` entries of a hash map `map` to the file `fname`.
   *
   * @param map A hash map
   * @param fname The filename
   * @param k A number of entries to write
   */
  void writeToFile(HashMap<Integer, Double> map, String fname, int k) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(fname));

      if (map != null) {
        int i = 0;
        for (Map.Entry<Integer, Double> e : map.entrySet()) {
          i++;
          writer.write(e.getKey() + ": " + String.format("%.5g%n", e.getValue()));
          if (i >= k) break;
        }
      }
      writer.close();
    } catch (IOException e) {
    }
  }

  /**
   * Rank all the documents in the links file. Produces two files: hubs_top_30.txt with documents
   * containing top 30 hub scores authorities_top_30.txt with documents containing top 30 authority
   * scores
   */
  void rank() {
    iterate(titleToId.keySet().toArray(new String[0]));
    HashMap<Integer, Double> sortedHubs = sortHashMapByValue(hubs);
    HashMap<Integer, Double> sortedAuthorities = sortHashMapByValue(authorities);
    writeToFile(sortedHubs, HITSINDEXDIR + "/hubs_top_30.txt", 30);
    writeToFile(sortedAuthorities, HITSINDEXDIR + "/authorities_top_30.txt", 30);
  }

  /* --------------------------------------------- */

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Please give the names of the link and title files");
    } else {
      HITSRanker hr = new HITSRanker(args[0], args[1], null);
      hr.rank();
    }
  }
}
