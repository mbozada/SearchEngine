# SearchEngine

## Group Members
Michael Bozada  
bozada.2@wright.edu  

Quinn Hirt  
hirt.14@wright.edu  


## Directories and Files
### corpus/  
Cranfield datset files downloadable from Prasad's [website](https://web1.cs.wright.edu/~tkprasad/courses/cs7800/cs7800.html).  
corpus/cran.all has been slightly modified to make processing the dataset for Lucene easier.

### index/
Index created by __src/Main.java__ using Lucene.  
Needs to be deleted anytime createIndex = true.

### input/
Input files required by Main.java.  
- __corpus_data.txt__: __corpus/cran.all__ processed using __scripts/split_corpus.py__
- __myQuery.txt__: Test queries manually extracted from __corpus/query.text__
- __myQueryRels.txt__: __corpus/qrels.text__ processed using __scripts/format_relevance.py__ Follows the format of "query_id relevant_doc_id...".
- __stop_words_english.txt__: Downloaded from [countwordsfree.com](https://countwordsfree.com/stopwords)

### lib/
Lucene 9.0.0 required .jar files.

### output/
Contains the output of running the 20 selected test queries with diffierent Search Engine configurations.
- __1_standard_resultx.txt__: StandardAnalyzer, Single Field Query Parser
- __2_multiIndex_resultx.txt__: StandardAnalyzer, Boosted MultiField Query Parser
- __3_stopWords_resultx.txt__: StopAnalyzer, Single Field Query Parser
- __4_multiIndex_stopWords_resultx.txt__: StopAnalyzer, Boosted MultiField Query Parser