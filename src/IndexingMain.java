import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;

import net.semanticmetadata.lire.*;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.DocumentFactory;


public class IndexingMain {

	private static String[] testFilesCategoryOne = new String[100];
	private static String[] testFilesCategoryTwo = new String[100];
	private static String testFilesPath = "./bin/resources/images/";
	Directory index = new RAMDirectory();
	
	public static void main(String[] args) throws IOException, ParseException {
		
		// Testdata category 1: 0.jpg - 99.jpg
		// Testdata category 2: 100.jpg - 199.jpg
		for(int i = 0; i < 100; i++) {
			testFilesCategoryOne[i] = i + ".jpg";
			testFilesCategoryTwo[i] = 100 + i + ".jpg";
		}
		
		TestWang test = new TestWang();
		try {
//			test.setUp();
//			test.testIndexWang();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		IndexingMain indexing = new IndexingMain();
//		DocumentBuilder builder = DocumentBuilderFactory.getColorStructureDescriptorDocumentBuilder();
//		DocumentBuilder builder = DocumentBuilderFactory.getCEDDDocumentBuilder();
//		indexing.createIndex(builder);
//		indexing.colorSearcher();
//		indexing.CSDSearcher();
//		indexing.CEDDSearcher();
		
		// Aufgabe 2a
		indexing.createIndex();
		
		// Aufgabe 2b
		// Beispielbild
		FileInputStream imageStream = new FileInputStream(testFilesPath + "0.jpg");
        BufferedImage image = ImageIO.read(imageStream);
        // Anzahl der Ergebnisse
		int countResults = 5;
		indexing.queryByExample(image, countResults);
	}
	
	// Query-by-Example: Es muss eine Bilddatei angegeben werden und die Anzahl der Ergebnisse.
	public void queryByExample(BufferedImage image, int countResults) throws CorruptIndexException, IOException {
  		createIndex();
		searchIndex(image, countResults);
	}

	public void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		// Create an appropriate DocumentBuilder
		ChainedDocumentBuilder builder = new ChainedDocumentBuilder();
		builder.addBuilder(DocumentBuilderFactory.getColorStructureDescriptorDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
		builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
		builder.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
		builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
		builder.addBuilder(DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder());
		
		StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_33);
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_33, standardAnalyzer);
		
		IndexWriter iw = new IndexWriter(index, conf);
			    
		int count = 0;
        long time = System.currentTimeMillis();
		
        System.out.println(">> Indexing " + testFilesCategoryOne.length + " files.");
        
		for (String identifier : testFilesCategoryOne) {
		    // Build the Lucene Documents
			Document doc = builder.createDocument(new FileInputStream(testFilesPath + identifier), identifier);
			// Add the Documents to the index
			iw.addDocument(doc);
			// Counter, shows elapsed time and indexed files.
			count++;
            if (count % 10 == 0) {
                System.out.print(count + " files indexed. ");
                float pct = (float) count / (float) testFilesCategoryOne.length;
                float tmp = (float) (System.currentTimeMillis() - time) / 1000;
                float remain = (tmp / pct) * (1f - pct);
                System.out.println("Remaining: <" + ((int) (remain) + 1) + " seconds of <" + ((int) ((tmp / pct)) + 1) + " seconds");
            }
		}
		iw.optimize();
	    iw.close();
	}
	
	public void searchIndex(BufferedImage image, int countResults) throws CorruptIndexException, IOException {
		// Opening an IndexReader (Lucene v3.0+)
        IndexReader reader = IndexReader.open(index);
        // Creating an ImageSearcher
        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(countResults);
        // Search for similar images
        ImageSearchHits hits = null;
        hits = searcher.search(image, reader);
		// print out results
        for (int i = 0; i < 4; i++) {
            System.out.println(hits.score(i) + ": " + 
				hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
	}

	public void colorSearcher() throws CorruptIndexException, IOException {
		// Opening an IndexReader (Lucene v3.0+)
        IndexReader reader = IndexReader.open(index);
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        // Creating an ImageSearcher
        ImageSearcher searcher = ImageSearcherFactory.createColorHistogramImageSearcher(5);
     // Reading the color, which is our "query"
        Document document = DocumentFactory.createColorOnlyDocument(Color.green);
     // Search for similar images
        ImageSearchHits hits = searcher.search(document, reader);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + 
				hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
	}
	
	public void CEDDSearcher() throws CorruptIndexException, IOException {
		// Opening an IndexReader (Lucene v3.0+)
        IndexReader reader = IndexReader.open(index);
        // Creating an ImageSearcher
        ImageSearcher searcher = ImageSearcherFactory.createDefaultSearcher();
        // Reading the sample image, which is our "query"
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFilesCategoryOne[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        // Search for similar images
        ImageSearchHits hits = null;
        hits = searcher.search(bimg, reader);
		// print out results
        for (int i = 0; i < 4; i++) {
            System.out.println(hits.score(i) + ": " + 
				hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
 
        // Get a document from the results
        Document document = hits.doc(3);
        // Search for similar Documents based on the image  features
        hits = searcher.search(document, reader);
        for (int i = 0; i < 4; i++) {
            System.out.println(hits.score(i) + ": " + 
				hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
	}
	
	public void CSDSearcher() throws CorruptIndexException, IOException {
		// Opening an IndexReader (Lucene v3.0+)
        IndexReader reader = IndexReader.open(index);
        // Creating an ImageSearcher
        ImageSearcher searcher = ImageSearcherFactory.createColorStructureDescriptorImageSearcher(5);
        // Reading the sample image, which is our "query"
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFilesCategoryOne[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        // Search for similar images
        ImageSearchHits hits = null;
        hits = searcher.search(bimg, reader);
		// print out results
        for (int i = 0; i < 4; i++) {
            System.out.println(hits.score(i) + ": " + 
				hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
 
        // Get a document from the results
        Document document = hits.doc(3);
        // Search for similar Documents based on the image  features
        hits = searcher.search(document, reader);
        for (int i = 0; i < 4; i++) {
            System.out.println(hits.score(i) + ": " + 
				hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
	}
	
}
