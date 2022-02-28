import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Main {
	public static void main(String[] args) throws IOException, ParseException {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer();
		File file = new File("corpus_data.txt");
		Scanner sin = new Scanner(file);

		// 1. create the index
		Directory index = FSDirectory.open((new File("./index/").toPath()));

		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		int count = 0;
		// TODO: add our docs instead
		IndexWriter w = new IndexWriter(index, config);
		try {
			while(sin.hasNextLine()) {
				// System.out.println("hello");
				Document doc = new Document();
				String title = sin.nextLine();
				String contents = sin.nextLine();
				// System.out.println(title);
				// System.out.println(contents);
				count++;
				String countString = Integer.toString(count);
				doc.add(new Field("doc_id", countString, TextField.TYPE_STORED));
				doc.add(new Field("title", title, TextField.TYPE_STORED));
				doc.add(new Field("contents", contents, TextField.TYPE_STORED));
				w.addDocument(doc);
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		w.close();

		// 2. query
		String querystr = args.length > 0 ? args[0] : "shear";
		System.out.println(querystr);

		// the "title" arg specifies the default field to use
		// when no field is explicitly specified in the query.
		QueryParser parser = new QueryParser("title", analyzer);
		org.apache.lucene.search.Query q;
		try {
			q = parser.parse(querystr);
		}
		// I AM SO SORRY
		catch (Exception e) {
			System.out.println(e);
			q = null;
		}

		// 3. search
		int hitsPerPage = 10;
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs docs = searcher.search(q, hitsPerPage);
		ScoreDoc[] hits = docs.scoreDocs;

		// 4. display results
		System.out.println("Found " + hits.length + " hits.");
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			org.apache.lucene.document.Document d = searcher.doc(docId);
			System.out.println((i + 1) + ". \t" + d.get("doc_id") + ".\t" + d.get("title") + "\t\t\t" + d.get("contents"));
			System.out.println(d);
		}

		// reader can only be closed when there
		// is no need to access the documents any more.
		reader.close();
	}
}