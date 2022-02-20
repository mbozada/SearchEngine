import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class SearchEngine {
    // Milestones
    // Prepare the corpus according to the input requirements of the Lucene API.
    // Read from File, Split on '.I'
    // Pass String to Indexer??

    // Implement Indexer to index all the documents in the corpus and store the index on disk (of the local machine).
    // Create fields for Title, Author, Publishing, Content
        // Split on '.T', '.A', '.B', and '.W' respectively
    // Analyze the contents of each(?), build index

    // Implement Query Parser to process the incoming queries.
    // Use the same analyzer to analyze queries?

    // Implement Index Searcher to satisfy the user query and retrieve the relevant documents.

    // Enhance the search results and the search engine performance by modifying Lucene API parameters
    
}