import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;


public class FileIndexing {
    
    private String testFilesPath = "./testFiles";
    private DocumentBuilder builders[] = {DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder(),
            DocumentBuilderFactory.getColorStructureDescriptorDocumentBuilder(),
            DocumentBuilderFactory.getScalableColorBuilder(),
            DocumentBuilderFactory.getCEDDDocumentBuilder(),
            DocumentBuilderFactory.getColorHistogramDocumentBuilder(),
            DocumentBuilderFactory.getColorLayoutBuilder(),
            DocumentBuilderFactory.getTamuraDocumentBuilder(),
            DocumentBuilderFactory.getEdgeHistogramBuilder(),
            DocumentBuilderFactory.getFCTHDocumentBuilder(),
            DocumentBuilderFactory.getGaborDocumentBuilder(),
            DocumentBuilderFactory.getJCDDocumentBuilder(),
            DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder()};
    private static ImageSearcher searchers[] = {
    		ImageSearcherFactory.createAutoColorCorrelogramImageSearcher(10),
    		ImageSearcherFactory.createColorStructureDescriptorImageSearcher(10),
    		ImageSearcherFactory.createScalableColorImageSearcher(10),
    		ImageSearcherFactory.createCEDDImageSearcher(10),
    		ImageSearcherFactory.createColorHistogramImageSearcher(10),
    		ImageSearcherFactory.createColorLayoutImageSearcher(10),
    		ImageSearcherFactory.createTamuraImageSearcher(10),
    		ImageSearcherFactory.createEdgeHistogramImageSearcher(10),
    		ImageSearcherFactory.createFCTHImageSearcher(10),
    		ImageSearcherFactory.createGaborImageSearcher(10),
    		ImageSearcherFactory.createJCDImageSearcher(10),
    		ImageSearcherFactory.createJpegCoefficientHistogramImageSearcher(10)
    };
    private static String indexPaths[] = {"AutoColorCorrelagram",
            "ColorStructureDescriptor",
            "ScalableColor",
            "CEDD",
            "ColorHistrogram",
            "ColorLayout",
            "Tamura",
            "EdgeHistogram",
            "FCTH",
            "Gabor",
            "JCD",
            "JpegCoefficientHistogram"};
    private ArrayList<String> images;
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        FileIndexing fi = new FileIndexing();
        try {
            fi.createIndex();
            System.out.println("Indexing finished.");
            
            for (int i = 0; i < searchers.length; i++) {
                fi.searchFiles(searchers[i], indexPaths[i]);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Creates an index with an extensive list of global features.
     *
     * @throws IOException
     */
    private void createIndex() throws IOException {
        System.out.println("-< Getting files to index >--------------");
        images = FileUtils.getAllImages(new File(testFilesPath), true);
        for (int i = 0; i < builders.length; i++) {
            indexFiles(images, builders[i], indexPaths[i]);
        }
    }
    
    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println("-< Indexing " + images.size() + " files with " + indexPath + " >--------------");
        IndexWriter iw = LuceneUtils.createIndexWriter("index-" + indexPath, true);
        
        int count = 0;
        long time = System.currentTimeMillis();
        
        for (String identifier : images) {
            Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count % 100 == 0) System.out.println(count + " files indexed.");
        }
        
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;
        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        
        iw.optimize();
        iw.close();
    }
    
    private void searchFiles(ImageSearcher searcher, String prefix) throws IOException {
    	// copy index to ram to be much faster ...
        IndexReader reader = IndexReader.open(new RAMDirectory(FSDirectory.open(new File("index-" + prefix))), true);
        Pattern p = Pattern.compile("([0-9]+).jpg");
        double map = 0;
        double errorRate = 0d;
        double precision10 = 0d;
        double[] pr10cat = new double[10];
        double[] pr10cnt = new double[10];
        for (int i = 0; i < pr10cat.length; i++) {
            pr10cat[i] = 0d;
            pr10cnt[i] = 0d;
        }
        System.out.println("name\tmap\tp@10\terror rate");
        for (int i = 0; i < images.size(); i++) {
//            String id = images.get(i);
//            String file = testFilesPath + "/" + id;
        	FileInputStream imageStream = new FileInputStream(testFilesPath + "/" + i + ".jpg");
            BufferedImage image = ImageIO.read(imageStream);
            System.out.println("Hšhe: " + image.getHeight() + " Breite: " + image.getWidth());
            ImageSearchHits hits = searcher.search(image, reader);
            System.out.println("Treffer Anzahl: " + hits.length());
            int goodOnes = 0;
            double avgPrecision = 0d;
            double precision10temp = 0d;
            int countResults = 0;
            for (int j = 0; j < hits.length(); j++) {
                Document d = hits.doc(j);
                String hitsId = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
//                System.out.println("Treffer ID: " + hitsId);
                Matcher matcher = p.matcher(hitsId);
                if (matcher.find())
                    hitsId = matcher.group(1);
                int testID = Integer.parseInt(hitsId);
                if (testID != i) countResults++;
                if ((testID != i) && ((int) Math.floor(i / 100) == (int) Math.floor(testID / 100))) {
                    goodOnes++;
                    // Only if there is a change in recall
                    avgPrecision += (double) goodOnes / (double) countResults;
//                    System.out.print("x");
                    if (j <= 10) {
                        precision10temp += 1d;
                    }
                } else {
                    if (j == 1) { // error rate
                        errorRate++;
                    }
                }
            }  // end for loop iterating results.
//            if (avgPrecision<=0) {
//                System.out.println("avgPrecision = " + avgPrecision);
//                System.out.println("goodOnes = " + goodOnes);
//            }
            avgPrecision = avgPrecision / goodOnes;
            precision10 += precision10temp / 10d;
            // precision @ 10 for each category ...
            pr10cat[(int) Math.floor(i / 100)] += precision10temp / 10d;
            pr10cnt[(int) Math.floor(i / 100)] += 1d;
            map += avgPrecision;
        }
        map = map / images.size();
        errorRate = errorRate / images.size();
        precision10 = precision10 / images.size();
        System.out.print(prefix + "\t");
        System.out.print(String.format("%.5f", map) + '\t');
        System.out.print(String.format("%.5f", precision10) + '\t');
        System.out.print(String.format("%.5f", errorRate) + '\t');
        // precision@10 per category
        for (int i = 0; i < pr10cat.length; i++) {
            double v = 0;
            if (pr10cnt[i] > 0)
                v = pr10cat[i] / pr10cnt[i];
//            System.out.print(i + ": ");
            System.out.printf("%.5f\t", v);

        }
        System.out.println();
    }
    
}