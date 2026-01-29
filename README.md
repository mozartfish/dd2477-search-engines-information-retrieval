# dd2477-search-engines-information-retrieval

## Setup and Installation

**Linux and MacOS**

1. Inside the `assignment1 directory`

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

2. Inside the `run_search_engine.sh` file change `/info/DD2476/ir22/lab/davisWiki` to the releative path `../DavisWiki` of where the DavisWiki is located

## 1 - Boolean Retrieval

### Bash Aliases

```bash
alias compile-all="sh ./compile_all.sh"
alias run-persistent="sh ./run_persistent.sh"
alias run-persistent-guardian="sh ./run_persistent_guardian.sh"
alias run-search-engine="sh ./run_search_engine.sh"
alias run-search-engine-guardian="sh ./run_search_engine_guardian.sh"
alias run-tokenizer="sh ./run_tokenizer.sh"
alias compile-run-search="compile-all; run-search-engine"
```

### 1.1 Tokenization

- [Regex101](https://regex101.com/) - for testing and developing regular expressions for the tokenizer
- `patterns.txt`- **Last Modified** - `10/18/2025`

### 1.2 Basic Inverted Index
- `PostingsEntry.java`- **Last Modified** - `10/19/2025`
- `PostingsList.java` - **Last Modified** - `10/19/2025`
- `HashedIndex.java` - **Last Modified** - `10/19/2025`
- `Query.java` - **Last Modified** - `10/19/2025`
- `Searcher.java` - **Last Modified** - `10/19/2025`

### 1.3 Multiword Queries

- `Searcher.java` - **Last Modified** - `10/19/2025`

### 1.4 Phrase Queries

- `Searcher.java` - **Last Modified** - `10/19/2025`

### 1.5 What is a good search result

### 1.6 What is a good query

### 1.7 Inverted Index as a hash table on Disk
- To run this feature create a new local directory called `local-disk` which represents the disk storage. 
- To run persistent mode do the following: 
1. Change the index in `Engine.java` to **PersistentHashedIndex**
2. Run `compile-run-search`
3. Once all the files have been indexed, close the search engine
4. Run `run-persistent`
- `PostingsEntry.java`- **Last Modified** - `10/19/2025`
- `PostingsList.java` - **Last Modified** - `10/19/2025`
- `PersistentHashedIndex.java` - **Last Modified** - `10/19/2025`
- `Engine.java` - **Last Modified** - `10/19/2025` 

## 2 - Ranked Retrieval

### Bash Aliases
```bash
alias compile-run-pagerank="javac PageRank.java; java -Xmx1g PageRank ./linksDavis.txt"
alias compile-run-mc-pagerank="javac PageRank.java; java -Xmx1g PageRank ./linksSvwiki.txt"
alias compile-run-hits="javac -cp . -d classes ir/HITSRanker.java; java -cp classes ir.HITSRanker ../rank-disk/linksDavis.txt ../rank-disk/davisTitles.txt"
```

### 2.1 Ranked Retrieval
- `Searcher.java` - **Last Modified** - `10/25/2025`

### 2.2 Ranked Multiword Retrieval
- `Searcher.java` - **Last Modified** - `10/25/2025`

### 2.4 What is a good search result

### 2.5 Computing PageRank with Power Iteration + combining PageRank with TF-IDF
- To run this feature create a new local directory called `rank-disk` which represents the storage for all things ranking. 
1. Run `compile-run-pagerank`
2. Run `compile-all`
3. Run `run-search-engine`
- `PageRank.java` - **Last Modified** - `01/29/2026`
- `Engine.java` - **Last Modified** - `01/29/2026`
- `Searcher.java` - **Last Modified** - `01/29/2026`

### 2.6 Cosine Similarity with Euclidean Length
- To run this feature create a new local directory called `local-disk` which represents the disk storage. 
- To run persistent mode do the following: 
1. Change the index in `Engine.java` to **PersistentHashedIndex**
2. Run `compile-run-search`
3. Once all the files have been indexed, close the search engine
4. Run `run-persistent`
- `Index.java` - **Last Modified** - `10/25/2025` 
- `PersistentHashedIndex.java` - **Last Modified** - `10/25/2025`
- `Engine.java` - **Last Modified** - `10/25/2025`

### 2.7 - Monte-Carlo PageRank Approximation
1. Run `compile-run-mc-pagerank`
- `PageRank.java` - **Last Modified** - `01/29/2026`

### 2.8 - Hubs and Authorities
1. Run `compile-run-hits`
2. Run `compile-all`
3. Run `run-search-engine`
- `HITSRanker.java` - **Last Modified** - `01/29/2026`
- `Engine.java` - **Last Modified** - `01/29/2026`
- `Searcher.java` - **Last Modified** - `01/29/2025`
- `RankingType.java` - **Last Modified** - `01/29/2025`
- `SearchGUI.java` - **Last Modified** - `01/29/2025`
- `run_persistent.sh` - **Last Modified** - `01/29/2025`
- `run_search_engine.sh` - **Last Modified** - `01/29/2025`

**At this point everything should work for PersistentHashedIndex**
1. Change the index in `Engine.java` to **PersistentHashedIndex**
2. Run `compile-run-search`
3. Once all the files have been indexed, close the search engine
4. Run `run-persistent`

## 3 - Relevance Feedback and Tolerant Retrieval

### Bash Aliases

### 3.1 Relevance Feedback

### 3.2 Evaluation using non-binary judgements

### 3.3 K-gram Index

### 3.4 - Wildcard Queries

### 3.5 - Isolated spelling correction of one-word queries

### 3.6 - Isolated spelling correction of multiword queries
