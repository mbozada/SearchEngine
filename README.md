# SearchEngine

## Group Members
Michael Bozada  
bozada.2@wright.edu  

Quinn Hirt  
hirt.14@wright.edu  


## Directories and Files
### corpus/  
Cranfield dataset files downloadable from Prasad's [website](https://web1.cs.wright.edu/~tkprasad/courses/cs7800/cs7800.html).  
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
Output of running the 20 selected test queries with diffierent Search Engine configurations.
- __1_standard_resultx.txt__: StandardAnalyzer, Single Field Query Parser
- __2_multiIndex_resultx.txt__: StandardAnalyzer, Boosted MultiField Query Parser
- __3_stopWords_resultx.txt__: StopAnalyzer, Single Field Query Parser
- __4_multiIndex_stopWords_resultx.txt__: StopAnalyzer, Boosted MultiField Query Parser

### scripts/
Python Scripts used to prepare the corpus for Lucene.
- __format_relevance.py__: Creates __input/myQueryRels.txt__ using __corpus/qrels.text__
- __split_corpus.py__: Creates __input/corpus_data.txt__ using __corpus/cran.all__

### src/
__Main.java__ is the singular source file. Run to use the Search Engine.

## Report
### Indexer
__createIndex()__ on line 129  
Pretty straightforward. Can be run with the StopAnalyzer if needed.  

### Index Searcher
__searchIndex()__ on line 164  
Uses either StandardAnalyzer or StopAnalyzer depending on the set flags.  
If multiIndex is set to true, it will use the MultiIndexQueryParser with boosted Fields.  

### Lucene Experience
We used Total Hits, Recall, Precision, and MAP to evaluate different configurations.  

Due to inexperience, it's difficult to say whether the different configurations "enhanced" search results.  

Surprisingly, the standard configuration had the highest MAP at 0.011253, but that's obviously a pretty low number.  

With just the multiIndexer enabled, hits blew up across the board. That let Recall = 1.000 for almost all queries, but Recall isn't a good metric when you're retrieiving every document...  

Enabling only stop words resulted in the lowest MAP, but had mixed performance on individual queries. It brought hits to their lowest numbers, but Recall and Precision were frequently 0. Maybe a different stop words list would improve results.  

Finally, using both the boosted multiIndex and stopWords analyzer gave somewhat confusing results. I don't know if it's the best or worst of both worlds, but performance seems averaged between the extremes of just MultiIndex or stopWords.  

In conclusion, working with Lucene was interesting but frustrating. It's difficult to assess performance and even more difficult to concretely improve search results.  