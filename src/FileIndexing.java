import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;


public class FileIndexing {
    
    private String indexPath = "test-index";
    private String testFilesPath = "./testFiles";

    // Lire Feature auswählen
    private DocumentBuilder getDocumentBuilder() {
        ChainedDocumentBuilder builder = new ChainedDocumentBuilder();
//        builder.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getColorStructureDescriptorDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder());
        return builder;
    }

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
        System.out.println("-< Indexing " + images.size() + " files >--------------");
        
        
        ChainedDocumentBuilder builder = (ChainedDocumentBuilder) getDocumentBuilder();
        indexFiles(images, builder, indexPath);
        
    }
    
    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);
        
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
