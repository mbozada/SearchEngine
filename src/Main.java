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
	public static void main(String[] arg) throws IOException, ParseException {
		// Create Analyzer and open Index's Directory
		StandardAnalyzer analyzer = new StandardAnalyzer();
		Directory index = FSDirectory.open((new File("./index/").toPath()));

		// If necessary, create the index using corpus_data.txt
		boolean createIndex = false;
		if(createIndex) {
			createIndex(analyzer, index, "corpus_data.txt");
		}
		
		// Search test queries from myQuery.txt
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
		for(String query: queries) {
			guess_ids.add(searchIndex(analyzer, index, query, "contents"));
		}

		// Process myQueryRels.txt into Array of Arrays of Ints for relevant document matching.
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
		fin.close();
		ArrayList<Double> guess_sizes = new ArrayList<Double>();
		ArrayList<Double> tp_arr = new ArrayList<Double>();
		ArrayList<Double> ap_arr = new ArrayList<Double>();
		
		for(int i = 0; i < rel_arr.size(); i++) {
			guess_sizes.add((guess_ids.get(i).size() + 0.0));
			tp_arr.add(findTruePositives(guess_ids.get(i), rel_arr.get(i)));
			ap_arr.add(findAP(guess_ids.get(i), rel_arr.get(i)));
		}

		// Output Precision and Recall for each query. Sum Precisions.
		double sumAveragePrecisions = 0;
		for(int i = 0; i < rel_arr.size(); i++) {
			System.out.format("Query: %d\n", (i+1));

			Double recall = tp_arr.get(i) / rel_arr.get(i).size();
			System.out.format("Recall: %f\n", recall);

			Double precision = tp_arr.get(i) / guess_sizes.get(i);
			System.out.format("Precision: %f\n", precision);

			sumAveragePrecisions += ap_arr.get(i);
		}

		System.out.format("Mean Average Precision: %f\n", (sumAveragePrecisions/ rel_arr.size()));

		
		System.out.println(tp_arr);
		System.out.println(ap_arr);
		// MAP IT UP
	}

	public static void createIndex(StandardAnalyzer analyzer, Directory index, String corpusPath) throws IOException {

		File file = new File(corpusPath);
		Scanner fin = new Scanner(file);
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter w = new IndexWriter(index, config);
		Integer count = 0;

		try {
			while(fin.hasNextLine()) {
				Document doc = new Document();
				String title = fin.nextLine();
				String contents = fin.nextLine();
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
		fin.close();
	}

	public static ArrayList<Integer> searchIndex(StandardAnalyzer analyzer, Directory index, String query, String fieldToSearch) throws IOException, ParseException {
		// 2. query
		while(true) {
			// the "title" arg specifies the default field to use
			// when no field is explicitly specified in the query.
			QueryParser parser = new QueryParser(fieldToSearch, analyzer);
			org.apache.lucene.search.Query q;
			try {
				q = parser.parse(query);
			}
			// I AM SO SORRY
			catch (Exception e) {
				System.out.println(e);
				q = null;
			}

			// 3. search
			int hitsPerPage = 400;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(q, hitsPerPage);
			ScoreDoc[] hits = docs.scoreDocs;
			ArrayList<Integer> hits_arr = new ArrayList<Integer>();

			// Add hits to array.
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				hits_arr.add(docId);
				org.apache.lucene.document.Document d = searcher.doc(docId);
			}

			// reader can only be closed when there
			// is no need to access the documents any more.
			reader.close();
			return hits_arr;
			}
	}

	public static double findTruePositives(ArrayList<Integer> guess, ArrayList<Integer> truth) {
		// System.out.println(guess.size());
		double tp_sum = 0;
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
		ArrayList<Double> precisionsArr = new ArrayList<Double>();
		for (int i = 0; i < guess.size(); i++) {
			double tp = findTruePositives(new ArrayList<Integer>(guess.subList(0, i)), truth);
			double precision = tp / (i + 1.0);
			precisionsArr.add(precision);
		}
		ArrayList<Integer> indicies = indiciesOfRelevant(guess,  truth);
		for(int i = 0; i < indicies.size(); i++) {
			sum_precisions += indicies.get(i) * precisionsArr.get(i);
		}

		double tp = findTruePositives(guess, truth);
		double averagePrecision =  sum_precisions / tp;
		
		if(Double.isNaN(averagePrecision)) {
			return 0.0;
		} else {
			return averagePrecision;
		}
	}
}