import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;


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
    public static void main() {
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // 1. create the index
        Directory index = new MMapDirectory(new File("~/").toPath());

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);
        addDoc(w, "Lucene in Action", "193398817");
        addDoc(w, "Lucene for Dummies", "55320055Z");
        addDoc(w, "Managing Gigabytes", "55063554A");
        addDoc(w, "The Art of Computer Science", "9900333X");
        w.close();
    }

    // title: .T, abst: .W
    private static void addDoc(IndexWriter w, String title, String abst) throws IOException {
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        doc.add(new TextField("title", title, org.apache.lucene.document.Field.Store.YES));

        doc.add(new TextField("abstract", abst, org.apache.lucene.document.Field.Store.YES));

        w.addDocument(doc);
    }
    
}