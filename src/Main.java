import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
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
	public static ArrayList<Integer> ourSearch(String our_query, int counter) throws IOException, ParseException {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer();
		File file = new File("corpus_data.txt");
		Scanner fin = new Scanner(file);

		// 1. create the index
		Directory index = FSDirectory.open((new File("./index/").toPath()));

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		boolean preserveIndex = false;
		if(counter < 1) {
			int count = 0;
			// TODO: add our docs instead
			IndexWriter w = new IndexWriter(index, config);
			try {
				while(fin.hasNextLine()) {
					// System.out.println("hello");
					Document doc = new Document();
					String title = fin.nextLine();
					String contents = fin.nextLine();
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
		}
		fin.close();
		

		// 2. query
		while(true) {
			
				// the "title" arg specifies the default field to use
			// when no field is explicitly specified in the query.
			QueryParser parser = new QueryParser("title", analyzer);
			org.apache.lucene.search.Query q;
			try {
				q = parser.parse(our_query);
			}
			// I AM SO SORRY
			catch (Exception e) {
				System.out.println(e);
				q = null;
			}

			// 3. search
			int hitsPerPage = 1401;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(q, hitsPerPage);
			ScoreDoc[] hits = docs.scoreDocs;
			ArrayList<Integer> hits_arr = new ArrayList<Integer>();

			// 4. display results
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				hits_arr.add(docId);
				org.apache.lucene.document.Document d = searcher.doc(docId);
				// System.out.println((i + 1) + ". \t" + d.get("doc_id") + ".\t" + d.get("title") + "\t\t\t" + d.get("contents"));
				// System.out.println(d.get("doc_id"));
			}

			// reader can only be closed when there
			// is no need to access the documents any more.
			reader.close();
			return hits_arr;
			}
		
	}

	public static void main(String[] arggggggggggggggggggggggg) throws IOException, ParseException {
		ArrayList<Integer> query_ids = new ArrayList<Integer>();
		ArrayList<String> queries = new ArrayList<String>();
		File f = new File("myQuery.txt");
		Scanner fin = new Scanner(f);
		while(fin.hasNextLine()) {
			query_ids.add(Integer.parseInt(fin.nextLine()));
			queries.add(fin.nextLine());
		}
		// System.out.println(truth_ids);
		// System.out.println(turth_queries);
		ArrayList<ArrayList<Integer>> guess_ids = new ArrayList<ArrayList<Integer>>();
		int count = 0;
		for(String query: queries) {
			guess_ids.add(ourSearch(query, count));
			count++;
		}

		f = new File("myQueryRels.txt");
		fin = new Scanner(f);
		ArrayList<ArrayList<Integer>> rel_arr = new ArrayList<ArrayList<Integer>>();
		while(fin.hasNextLine()) {
			String nums = fin.nextLine();
			String[] nums_to_str_arr = nums.split(" ");
			ArrayList<Integer> rels = new ArrayList<Integer>();
			for(int i = 0; i < nums_to_str_arr.length; i++) {
				if(i != 0) {
					rels.add(Integer.parseInt(nums_to_str_arr[i]));
				}
				else {
					// empty else
				}
			}
			rel_arr.add(rels);
			// System.out.println(rel_arr);
		}
		System.out.println(rel_arr);
		ArrayList<Integer> tp_arr = new ArrayList<Integer>();
		ArrayList<Double> ap_arr = new ArrayList<Double>();
		for(int i = 0; i < rel_arr.size(); i++) {
			// tp_arr.add(findTruePositives(guess_ids.get(i), rel_arr.get(i)));
			ap_arr.add(findAP(guess_ids.get(i), rel_arr.get(i)));
		}
		System.out.println(tp_arr);
		System.out.println(ap_arr);
		// MAP IT UP

	}

	public static int findTruePositives(ArrayList<Integer> guess, ArrayList<Integer> truth) {
		// System.out.println(guess.size());
		int tp_sum = 0;
		for(Integer i: truth) {
			if (guess.contains(i)) {
				tp_sum += 1;
			}
		}
		return tp_sum;
	}

	public static ArrayList<Integer> indiciesOfRelevant(ArrayList<Integer> guess,  ArrayList<Integer> truth) {
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for(Integer i: truth) {
			if (guess.contains(i)) {
				indicies.add(1);
			}
			else {
				indicies.add(0);
			}
		}
		return indicies;
	}

	// TODO: Need to figure this out, well not Quinn technically
	public static double findAP(ArrayList<Integer> guess, ArrayList<Integer> truth) {
		double sum_precisions = 0.0;
		ArrayList<Double> temp = new ArrayList<Double>();
		for (int i = 0; i < guess.size(); i++) {
			double tp = findTruePositives(new ArrayList<Integer>(guess.subList(0, i)), truth);
			System.out.println(tp);
			double precision = tp / (i + 1.0);
			System.out.println(precision);
			temp.add(precision);
		}
		ArrayList<Integer> indicies = indiciesOfRelevant(guess,  truth);
		for(int i = 0; i < indicies.size(); i++) {
			sum_precisions += indicies.get(i) * temp.get(i);
		}

			double tp = findTruePositives(guess, truth);
			return sum_precisions / tp;
		}
}