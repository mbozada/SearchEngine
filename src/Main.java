import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Main {
	public static void main(String[] arg) throws IOException, ParseException {
		// When any of these flags are changed, it's best to recreate the index.
		boolean createIndex = false; // Flag to create index
		boolean multiIndex = false; // Flag to use MultiIndexer
		boolean stopWords = false; // Flag to use a StopWord Analyzer
		
		// Create Analyzer and open Index's Directory
		// Use a StopAnalyzer or StandardAnalyzer
		Analyzer analyzer = new StandardAnalyzer();
		if(stopWords) {
			Path path = FileSystems.getDefault().getPath("./input/", "stop_words_english.txt");
			analyzer = new StopAnalyzer(path);
		}

		
		
		Directory index = FSDirectory.open((new File("./index/").toPath()));
		
		// If necessary, create the index using corpus_data.txt
		if(createIndex) {
			createIndex(analyzer, index, "./input/corpus_data.txt");
		}
		
		// Load in test queries from myQuery.txt
		ArrayList<Integer> query_ids = new ArrayList<Integer>();
		ArrayList<String> queries = new ArrayList<String>();
		File f = new File("./input/myQuery.txt");
		Scanner fin = new Scanner(f);
		while(fin.hasNextLine()) {
			query_ids.add(Integer.parseInt(fin.nextLine()));
			queries.add(fin.nextLine());
		}

		// Search index using test queries.
		ArrayList<ArrayList<Integer>> guess_ids = new ArrayList<ArrayList<Integer>>();
		for(String query: queries) {
			try {
				guess_ids.add(searchIndex(analyzer, index, query, multiIndex));
			} catch (org.apache.lucene.queryparser.classic.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Process myQueryRels.txt for future Recall and Precision Calculations
		f = new File("./input/myQueryRels.txt");
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
			}
			rel_arr.add(rels);
		}
		fin.close();

		// Calculate Recall, Precision, and MAP
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

			System.out.format("Hits %f\n", guess_sizes.get(i));

			Double recall = tp_arr.get(i) / rel_arr.get(i).size();
			System.out.format("Recall: %f\n", recall);

			Double precision = tp_arr.get(i) / guess_sizes.get(i);
			System.out.format("Precision: %f\n\n", precision);

			sumAveragePrecisions += ap_arr.get(i);
		}

		System.out.format("Mean Average Precision: %f\n", (sumAveragePrecisions/ rel_arr.size()));
	}

	/**
	 * Creates the searchable index.
	 * @param analyzer The analyzer used by indexer and the indexSearcher.
	 * @param index The directory containing the index.
	 * @param corpusPath The pathname to the processed corpus data.
	*/
	public static void createIndex(Analyzer analyzer, Directory index, String corpusPath) throws IOException {
		File file = new File(corpusPath);
		Scanner fin = new Scanner(file);
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter w = new IndexWriter(index, config);
		Integer count = 0;

		// Uses lines from corpus_data.txt to create documents in the index.
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

	/**
	 * Parses the query and searches the index.
	 * @param analyzer The analyzer used by indexer and the indexSearcher.
	 * @param index The directory containing the index.
	 * @param query The query string.
	 * @return An ArrayList of doc_ids of the found Documents.
	*/
	public static ArrayList<Integer> searchIndex(Analyzer analyzer, Directory index, String query, Boolean multi) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		// Boosts the title field over contents for increased MAP

		Map<String, Float> boost = new HashMap<String, Float>();
		boost.put("title", (float) .75);
		boost.put("contents", (float) .25);
		MultiFieldQueryParser multiParser = new MultiFieldQueryParser(new String[] {"title", "contents"}, analyzer, boost);
		QueryParser parser = new QueryParser("title", analyzer);

		org.apache.lucene.search.Query q;
		if(multi) {
			q = multiParser.parse(query);
		}
		else {
			q = parser.parse(query);
		}

		// Search the index.
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);

		// Count the hits
		TotalHitCountCollector collector = new TotalHitCountCollector();
		searcher.search(q, collector);

		// Grab all hits
		TopDocs docs = searcher.search(q, collector.getTotalHits());
		ScoreDoc[] hits = docs.scoreDocs;
		ArrayList<Integer> hits_arr = new ArrayList<Integer>();

		// Add hits to array.
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			hits_arr.add(docId);
			// Could be used to print out document fields
			// org.apache.lucene.document.Document d = searcher.doc(docId);
		}

		reader.close();
		return hits_arr;
	}

	/**
	 * Finds relevant documents in returned search.
	 * @param searchResults The results of a search.
	 * @param relevantDocs The DocIds of relevant documents.
	 * @return The number of relevant documents within a search's results. Otherwise known as "True Positives".
	*/
	public static double findTruePositives(ArrayList<Integer> searchResults, ArrayList<Integer> relevantDocs) {
		double tp_sum = 0;
		for(Integer i: relevantDocs) {
			if (searchResults.contains(i)) {
				tp_sum += 1;
			}
		}
		return tp_sum;
	}

	/**
	 * Finds the indicies of relevant documents within a search's results.
	 * @param searchResults The results of a search.
	 * @param relevantDocs The DocIds of relevant documents.
	 * @return An array where a 1 represents a found relevant document and a 0 represents that a document was not relevant.
	*/
	public static ArrayList<Integer> indiciesOfRelevant(ArrayList<Integer> searchResults,  ArrayList<Integer> relevantDocs) {
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for(Integer i: relevantDocs) {
			if (searchResults.contains(i)) {
				indicies.add(1);
			}
			else {
				indicies.add(0);
			}
		}
		return indicies;
	}

	/**
	 * Calculates the average precision of a search.
	 * @param searchResults The results of a search.
	 * @param relevantDocs The DocIds of relevant documents.
	 * @return The average precision of a search.
	*/
	public static double findAP(ArrayList<Integer> searchResults, ArrayList<Integer> relevantDocs) {
		double sum_precisions = 0.0;
		ArrayList<Double> precisionsArr = new ArrayList<Double>();
		// Finds the precision of each index by calculating precision for subsets of searchResults.
		for (int i = 0; i < searchResults.size(); i++) {
			double tp = findTruePositives(new ArrayList<Integer>(searchResults.subList(0, i)), relevantDocs);
			double precision = tp / (i + 1.0);
			precisionsArr.add(precision);
		}

		// Sums the precisions at the index of each relevant document.
		ArrayList<Integer> indicies = indiciesOfRelevant(searchResults,  relevantDocs);
		for(int i = 0; i < indicies.size(); i++) {
			sum_precisions += indicies.get(i) * precisionsArr.get(i);
		}

		double tp = findTruePositives(searchResults, relevantDocs);
		double averagePrecision =  sum_precisions / tp;
		
		// Guards against NaN values entering the array.
		if(Double.isNaN(averagePrecision)) {
			return 0.0;
		} else {
			return averagePrecision;
		}
	}
}