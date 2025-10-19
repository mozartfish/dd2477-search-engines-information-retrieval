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

### 2.1 Ranked Retrieval

### 2.2 Ranked Multiword Retrieval

### 2.4 What is a good search result

### 2.5 Computing PageRank with Power Iteration + combining PageRank with TF-IDF

### 2.6 Cosine Similarity with Euclidean Length

### 2.7 - Monte-Carlo PageRank Approximation

### 2.8 - Hubs and Authorities

## 3 - Relevance Feedback and Tolerant Retrieval

### Bash Aliases

### 3.1 Relevance Feedback

### 3.2 Evaluation using non-binary judgements

### 3.3 K-gram Index

### 3.4 - Wildcard Queries

### 3.5 - Isolated spelling correction of one-word queries

### 3.6 - Isolated spelling correction of multiword queries
