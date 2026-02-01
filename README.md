# dd2477-search-engines-information-retrieval

This project implements a self-contained search engine from the ground up using minimal skeleton code that enables users to search and explore the [DavisWiki](https://en.wikipedia.org/wiki/DavisWiki) - a wiki based in Davis, California about the people, events, universities, bands, places and other things of the cities. The particular data used is a cleaned version from the 2014 version of the wiki. For use with more recent versions of the data, there will need to be significant changes to the tokenization and preprocessing of the data.

## Setup and Installation

```bash
dos2unix compile_all.sh
dos2unix run_persistent_guardian.sh
dos2unix run_persistent.sh
dos2unix run_search_engine_guardian.sh
dos2unix run_search_engine.sh
dos2unix run_tokenizer.sh
```

```bash
chmod +x compile_all.sh
chmod +x run_persistent_guardian.sh
chmod +x run_persistent.sh
chmod +x  run_search_engine_guardian.sh
chmod +x run_search_engine.sh
chmod +x  run_tokenizer.sh
```

### Bash Aliases

```bash
alias compile-all="sh ./compile_all.sh"
alias run-persistent="sh ./run_persistent.sh"
alias run-persistent-guardian="sh ./run_persistent_guardian.sh"
alias run-search-engine="sh ./run_search_engine.sh"
alias run-search-engine-guardian="sh ./run_search_engine_guardian.sh"
alias run-tokenizer="sh ./run_tokenizer.sh"
alias compile-run-search="compile-all; run-search-engine"
alias compile-run-pagerank="javac PageRank.java; java -Xmx1g PageRank ./linksDavis.txt"
alias compile-run-mc-pagerank="javac PageRank.java; java -Xmx1g PageRank ./linksSvwiki.txt"
alias compile-run-hits="javac -cp . -d classes ir/HITSRanker.java; java -cp classes ir.HITSRanker ../rank-disk/linksDavis.txt ../rank-disk/davisTitles.txt"
alias kgrams-test='java -cp classes ir.KGramIndex -f kgram_test.txt -p patterns.txt -k 3 -kg "ove mea"'
alias kgrams-test-1='java -cp classes ir.KGramIndex -f kgram_test.txt -p patterns.txt -k 2 -kg "ve"'
alias kgrams-test-2='java -cp classes ir.KGramIndex -f kgram_test.txt -p patterns.txt -k 2 -kg "th he"'
alias compile-run-kgrams-test="compile-all; kgrams-test"
alias compile-run-kgrams-test-1="compile-all; kgrams-test-1"
alias compile-run-kgrams-test-2="compile-all; kgrams-test-2"
```

### Document Contents

To see the contents of a returned document click returned document text ie. clicking `JasonRifkind.f` will display the contents of the `JasonRifkind` document

## 1 Boolean Retrieval

### Test Queries

- `zombie`
- `attack`
- `zombie attack`
- `money transfer`
- `a cell phone`
- `what they are selling`
- `graduate program mathematics`

### Tokenization

- [Regex101](https://regex101.com/) - for testing and developing regular expressions for the tokenizer

The `Tokenizer.java` class tokenizes the text but does not handle non-standard tokens. In the `patterns.txt` file, regular expressions were developed using `Regex101` and experimentation to handle non-standard tokens in the data.

### Basic Inverted Index - Multiword Queries

Multi-word queries consist of queries with one or more terms. Using properties of sets a query `zombie` returns the relevant results containing information related to zombie. For a query `zombie attack` we consider the intersection of `zombie` set and `attack` set and return the relevant results that contain this information. For queries with more than 2 words such as `a cell phone`, `what they are selling` we implement a concatenation of the different sets and compute the intersections - `a cell` followed by the intersection of the `a cell` set with the `phone` set.

### Basic Inverted Index - Phrase Queries

For phrase queries we consider `zombie attack` as a single contiguous phrase term. The results returned should contain information about `zombie attack`. This is different from the intersection query `zombie attack` which considers `zombie` and `attack` as unique different terms.

### Inverted Index as a hash table on Disk

This problem considers the systems side of information retrieval - how to scale and not build the index every time we start the search engine. In this project we implement a local disk containing the entire index and associated information about posting entries and postings lists in a hash table. In industrial applications (ie Google) many algorithms and techniques involving distributed systems were developed to manage this problem at scale including the development of `Spanner`, `Big Table`, `MapReduce`. Even though this solution is a local-disk solution, the hash table and data representation had to be engineered such that queries could produce the same results for both the `persistent index` and `non-persistent index`.

- To run this feature create a new local directory called `local-disk` which represents the disk storage.
- To run persistent mode do the following:

1. Change the index in `Engine.java` to **PersistentHashedIndex**
2. Run `compile-run-search`
3. Once all the files have been indexed, close the search engine
4. Run `run-persistent`

## 2 - Ranked Retrieval

### Test Queries

- `zombie`
- `attack`
- `zombie attack`
- `money transfer`
- `a cell phone`
- `what they are selling`
- `graduate program mathematics`

### Ranked Retrieval - TF-IDF, PageRank, HITS(Hubs and Authorities)

This part of the project investigates different ways of ranking information to produce the most relevant results for a user. This is a challenging problem involving HCI, Information Retrieval and Systems - what is a relevant result for a user and how do we serve that information. Methods explored here include TF-IDF, PageRank and HITS methods. HITS was a popular algorithm behind `Yahoo Search` but eventually fell out of favor due to its vulnerability to link manipulation and page importance combined with its ability to scale. Nowadays Google has moved on from `PageRank` with newer efficient secret solutions based upon the ideas in the original paper but this algorithm is still groundbreaking and worth studying to understand how to scale search and information retrieval.

### Computing PageRank with Power Iteration + combining PageRank with TF-IDF

- To run this feature create a new local directory called `rank-disk` which represents the storage for all things ranking.

1. Run `compile-run-pagerank`
2. Run `compile-all`
3. Run `run-search-engine`

### Cosine Similarity with Euclidean Length

- To run this feature create a new local directory called `local-disk` which represents the disk storage.
- To run persistent mode do the following:

1. Change the index in `Engine.java` to **PersistentHashedIndex**
2. Run `compile-run-search`
3. Once all the files have been indexed, close the search engine
4. Run `run-persistent`

### Monte-Carlo PageRank Approximation

This experiment involves considering faster alternatives to the original `PageRank` which uses power iteration. Using probabilitic approaches, PageRank can converge faster to relevant pages by estimating the important pages. This estimation is particularly important when scaling ranking to Google size. The methods investigated in this section use the ideas described in Monte Carlo Methods in PageRank by Avrachenkov et al.

1. Run `compile-run-mc-pagerank`

### HITS(Hubs and Authorities)

1. Run `compile-run-hits`
2. Run `compile-all`
3. Run `run-search-engine`

**At this point everything should work for PersistentHashedIndex**

1. Change the index in `Engine.java` to **PersistentHashedIndex**
2. Run `compile-run-search`
3. Once all the files have been indexed, close the search engine
4. Run `run-persistent`

## 3 - Relevance Feedback and Tolerant Retrieval

At this point everything was built and tested for `HashedIndex`. If everything is implemented properly it should also work for `PersistentHashedIndex`. This part of the project focuses on developing methods for spelling correction, relevance feedback by implementing the [Rocchio Algorithm](https://en.wikipedia.org/wiki/Rocchio_algorithm) introduced in the SMART Information Retreival System and tolerant retrieval methods.

### Test Queries

- `zombie`
- `mo*y transfer`
- `b* colo*r`
- `a* m*ks*e`
- `thn`
- `dcmber`
- `zmbie atck`
- `mny tranfr`
- `a cll phne`
- `wht the ae selig`

### Relevance Feedback

- Once the search engine has started select `Ranked Retrieval` (`TF-IDF` is the default) and type a query ie. `zombie`. Once the relevant documents are returned select the checkbox next to the documents you think are relevant for your search click the text box again and hit `Enter`. A new list of documents relevant to the documents you selected are returned using the `Rocchio Algorithm`.

### Tolerant Retrieval - K-gram Index, Wildcard Queries, Isolated spelling correction of one-word queries, Isolated spelling correction of multiword queries

This part of the project focuses on how to make the search engine robust to spelling errors, alternative spellings. An in-memory `K-Gram Index` was developed to handle wildcard search and spelling corrections for one word and multi-word queries.

- Once the search engine has started try some of the incorrect spelled words as an intersection or ranked retrieval query.
