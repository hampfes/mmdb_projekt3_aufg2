import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
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
    private String indexPaths[] = {"AutoColorCorrelagram",
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
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        FileIndexing fi = new FileIndexing();
        try {
            fi.createIndex();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Indexing finished.");
    }
    
    /**
     * Creates an index with an extensive list of global features.
     *
     * @throws IOException
     */
    private void createIndex() throws IOException {
        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(testFilesPath), true);
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
    
}