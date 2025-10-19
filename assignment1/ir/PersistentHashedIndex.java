/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, KTH, 2018
 */

package ir;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.nio.charset.*;

/*
 *   Implements an inverted index as a hashtable on disk.
 *
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks.
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

  /** The directory where the persistent index files are stored. */
  public static final String INDEXDIR = "../local-disk";

  /** The dictionary file name */
  public static final String DICTIONARY_FNAME = "dictionary";

  /** The data file name */
  public static final String DATA_FNAME = "data";

  /** The terms file name */
  public static final String TERMS_FNAME = "terms";

  /** The doc info file name */
  public static final String DOCINFO_FNAME = "docInfo";

  /** The dictionary hash table on disk can fit this many entries. */
//  public static final long TABLESIZE = 15 * 611953L;
  public static final long TABLESIZE = 6 * 611953L;

  /** The dictionary hash table is stored in this file. */
  RandomAccessFile dictionaryFile;

  /** The data (the PostingsLists) are stored in this file. */
  RandomAccessFile dataFile;

  /** Pointer to the first free memory cell in the data file. */
  long free = 0L;

  /** The cache as a main-memory hash map. */
  HashMap<String, PostingsList> index = new HashMap<String, PostingsList>();

  // ===================================================================

  /** A helper class representing one entry in the dictionary hashtable. */
  public class Entry {
    /** hash value for a term */
    private long hashValue;

    /** ptr in the dictionary file that points to the postings list data in the data file */
    private long ptr;

    /** the size of the postings list - for knowing how many bytes are in the file */
    private int postingsListLength;

    /** the size of an entry (fixed size) - hashvalue + ptr + postings list length */
    private static final int ENTRY_WIDTH = Long.BYTES + Long.BYTES + Integer.BYTES;

    /**
     * Constructs a new entry object. An entry object is stored in the dictionary file and contains
     * a ptr to a postings lists in the data file.
     *
     * @param hashValue generated from hashing a token with the hash function
     * @param ptr ptr in the dictionary file that points to the postings list in the data file
     * @param postingsListLength the size of the postings list - for knowing how many bytes the
     *     postings list takes up in memory
     */
    public Entry(long hashValue, long ptr, int postingsListLength) {
      this.hashValue = hashValue;
      this.ptr = ptr;
      this.postingsListLength = postingsListLength;
    }

    /**
     * Get the hash value for the term(s) stored in the entry object
     *
     * @return hash value
     */
    public long getHashValue() {
      return this.hashValue;
    }

    /**
     * Get the ptr to the data stored in the entry object
     *
     * @return ptr to the data stored in the entry object
     */
    public long getPtr() {
      return this.ptr;
    }

    /**
     * Get the size of the postings list stored in the entry object
     *
     * @return size of the postings list stored in the entry object
     */
    public int getPostingsListLength() {
      return this.postingsListLength;
    }
  }

  // ==================================================================

  /**
   * Constructor. Opens the dictionary file and the data file. If these files don't exist, they will
   * be created.
   */
  public PersistentHashedIndex() {
    try {
      dictionaryFile = new RandomAccessFile(INDEXDIR + "/" + DICTIONARY_FNAME, "rw");
      dataFile = new RandomAccessFile(INDEXDIR + "/" + DATA_FNAME, "rw");
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      readDocInfo();
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Writes data to the data file at a specified place.
   *
   * @return The number of bytes written.
   */
  int writeData(String dataString, long ptr) {
    try {
      dataFile.seek(ptr);
      byte[] data = dataString.getBytes();
      dataFile.write(data);
      return data.length;
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
  }

  /** Reads data from the data file */
  String readData(long ptr, int size) {
    try {
      dataFile.seek(ptr);
      byte[] data = new byte[size];
      dataFile.readFully(data);
      return new String(data);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  // ==================================================================
  //
  //  Reading and writing to the dictionary file.

  /*
   *  Writes an entry to the dictionary hash table file.
   *
   *  @param entry The key of this entry is assumed to have a fixed length
   *  @param ptr   The place in the dictionary file to store the entry
   */
  void writeEntry(Entry entry, long ptr) {
    try {
      ByteBuffer buffer = ByteBuffer.allocate(Entry.ENTRY_WIDTH);
      buffer.putLong(entry.getHashValue());
      buffer.putLong(entry.getPtr());
      buffer.putInt(entry.getPostingsListLength());
      dictionaryFile.seek(ptr);
      dictionaryFile.write(buffer.array());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Reads an entry from the dictionary file.
   *
   * @param ptr The place in the dictionary file where to start reading.
   */
  Entry readEntry(long ptr) {
    try {
      ByteBuffer buffer = ByteBuffer.allocate(Entry.ENTRY_WIDTH);
      dictionaryFile.seek(ptr);
      dictionaryFile.readFully(buffer.array());

      long hashValue = buffer.getLong();
      long ptrValue = buffer.getLong();
      int size = buffer.getInt();

      // check if empty/invalid entry
      if (hashValue == 0 && ptrValue == 0 && size == 0) {
        return null;
      }

      return new Entry(hashValue, ptrValue, size);

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  // ==================================================================

  /**
   * Writes the document names and document lengths to file.
   *
   * @throws IOException { exception_description }
   */
  private void writeDocInfo() throws IOException {
    FileOutputStream fout = new FileOutputStream(INDEXDIR + "/docInfo");
    for (Map.Entry<Integer, String> entry : docNames.entrySet()) {
      Integer key = entry.getKey();
      String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
      fout.write(docInfoEntry.getBytes());
    }
    fout.close();
  }

  /**
   * Reads the document names and document lengths from file, and put them in the appropriate data
   * structures.
   *
   * @throws IOException { exception_description }
   */
  private void readDocInfo() throws IOException {
    File file = new File(INDEXDIR + "/docInfo");
    FileReader freader = new FileReader(file);
    try (BufferedReader br = new BufferedReader(freader)) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] data = line.split(";");
        docNames.put(new Integer(data[0]), data[1]);
        docLengths.put(new Integer(data[0]), new Integer(data[2]));
      }
    }
    freader.close();
  }

  /** Write the index to files. */
  public void writeIndex() {
    int collisions = 0;
    try {
      // Write the 'docNames' and 'docLengths' hash maps to a file
      writeDocInfo();

      // clear the previous contents of the dictionary file
      dictionaryFile.setLength(0);
      // set size of the dictionary file
      dictionaryFile.setLength(TABLESIZE * Entry.ENTRY_WIDTH);

      // Write the dictionary and the postings list
      for (Map.Entry<String, PostingsList> entry : index.entrySet()) {
        // write the word to the dictionary file
        String key = entry.getKey();
        PostingsList postingsList = entry.getValue();
        long hashValue = hashFuncOne(key);
        long ptrDataValue = free;
        long ptrDictValue = hashValue * Entry.ENTRY_WIDTH;
        Entry newEntry = readEntry(ptrDictValue);

        // Double Hashing strategy
        long secondHashValue = hashFuncTwo(key);
        while (newEntry != null) {
          collisions++;
          hashValue = (hashValue + secondHashValue) % TABLESIZE;
          ptrDictValue = hashValue * Entry.ENTRY_WIDTH;
          newEntry = readEntry(ptrDictValue);
        }

        // write postings list to data file
        String postingsListData = key + "|" + postingsList;
        int postingListSize = postingsListData.length();
        Entry dictEntry = new Entry(hashValue, ptrDataValue, postingListSize);

        // write to the dict file
        writeEntry(dictEntry, ptrDictValue);
        // write to the data file
        free += writeData(postingsListData, ptrDataValue);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    System.err.println(collisions + " collisions.");
  }

  /**
   * Compute a unique hash value to hash a term for the dictionary file
   *
   * @param term word that is hashed for indexing into the data file
   * @return hash value
   */
  private long hashFuncTwo(String term) {
    return 1 + Math.abs(term.hashCode()) % (TABLESIZE - 1);
  }

  /**
   * Compute a unique hash value to hash a term for the dictionary file
   *
   * @param term word that is hashed for indexing into the data file
   * @return hash value
   */
  private long hashFuncOne(String term) {
    return Math.abs(term.hashCode()) % TABLESIZE;
  }

  // ==================================================================

  /** Returns the postings for a specific term, or null if the term is not in the index. */
  public PostingsList getPostings(String token) {
    long hashValue = hashFuncOne(token);
    long ptrDictValue = hashValue * Entry.ENTRY_WIDTH;
    Entry currentEntry = readEntry(ptrDictValue);

    long secondHashValue = hashFuncTwo(token);
    while (currentEntry != null) {
      if (currentEntry.getHashValue() == hashValue) {
        String postingsListsData = readData(currentEntry.ptr, currentEntry.getPostingsListLength());
        if (postingsListsData.startsWith(token)) {
          String postingsListStart = postingsListsData.substring(token.length() + 1);
          return PostingsList.stringToPostingsList(postingsListStart);
        }
      }
      hashValue = (hashValue + secondHashValue) % TABLESIZE;
      ptrDictValue = hashValue * Entry.ENTRY_WIDTH;
      currentEntry = readEntry(ptrDictValue);
    }
    return null;
  }

  /** Inserts this token in the main-memory hashtable. */
  public void insert(String token, int docID, int position) {
    // if term in index - update postings list associated with term
    // else add term to index and create a new postings list
    if (index.containsKey(token)) {
      PostingsList postingsList = index.get(token);
      // term in same document
      if (postingsList.get(postingsList.size() - 1).docID == docID) {
        // score keeps track of the term frequency (tf)
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

  /** Write index to file after indexing is done. */
  public void cleanup() {
      System.err.println(index.size() + " unique words");
      System.err.print("Writing index to disk...");
      writeIndex();
      System.err.println("done!");
  }
}
