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

2. Inside the `run_search_engine.sh` file change `/info/DD2476/ir22/lab/davisWiki` to local path of where the DavisWiki is located  

3. Bash Aliases 
```bash 
alias compile-all="sh ./compile_all.sh"
alias run-persistent="sh ./run_persistent.sh"
alias run-persistent-guardian="sh ./run_persistent_guardian.sh"
alias run-search-engine="sh ./run_search_engine.sh"
alias run-search-engine-guardian="sh ./run_search_engine_guardian.sh"
alias run-tokenizer="sh ./run_tokenizer.sh"
alias compile-run-search="compile-all; run-search-engine"
```

## 1 - Boolean Retrieval 
### 1.1 Tokenization 
- [Regex101](https://regex101.com/) - for testing and developing regular expressions for the tokenizer 
### 1.2 Basic Inverted Index 
### 1.3 Multiword Queries 
### 1.4 Phrase Queries 
### 1.5 What is a good search result 
### 1.6 What is a good query 
### 1.7 Inverted index as a hash table on disk 


