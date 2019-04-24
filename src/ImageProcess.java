import com.baidu.aip.ocr.AipOcr;
import com.giaybac.traprange.PDFTableExtractor;
import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableCell;
import com.giaybac.traprange.entity.TableRow;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ImageProcess {

    File file;
    AipOcr client = null;

    // invoice
    String APP_ID = "15634368";
    String API_KEY = "vIRm95VbtvBU7azKy6Vbtran";
    String SECRET_KEY = "o5ws1H8f8vgn5MecdWnnyvhWzV6xMN6c";



    ImageProcess (File file){
        this.file = file;
        client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
    }

    public BufferedImage convertFromPdfToJpg (File file){

        try {
            PDDocument document = PDDocument.load(file);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++ page){
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                return bim;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getImageRecognition(BufferedImage img, String purpose){
        JSONObject result = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytearray = bos.toByteArray();

        if (purpose.equals("invoice")) {
            HashMap<String, String> options = new HashMap<>();
            options.put("accuracy", "high");
            result = client.vatInvoice(bytearray, options);
        }return result;
    }

    public JSONObject getImageRecognition (File file, String purpose){
        JSONObject result = null;
        BufferedImage img = null;
        if (file.getAbsolutePath().endsWith(".pdf")){
            img = convertFromPdfToJpg(file);
        }else {
            try {
                img = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytearray = bos.toByteArray();

        if (purpose.equals("invoice")) {
            HashMap<String, String> options = new HashMap<>();
            options.put("accuracy", "high");
            result = client.vatInvoice(bytearray, options);
        }
//        }else {
//            result = client.tableRecognitionAsync(bytearray, new HashMap<String, String>());
//            System.out.println(result);
//            JSONArray array = result.getJSONArray("result");
//            result = array.getJSONObject(0);
//            String request_id = result.getString("request_id");
//            result = client.getTableRecognitionJsonResult(request_id);
//            while ((result.getJSONObject("result").getInt("percent")) != 100){
//                try {
//                    TimeUnit.SECONDS.sleep(3);
//                    result = client.getTableRecognitionJsonResult(request_id);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
        return result;
    }

    List<Product> productList = new ArrayList<Product>();

    public List<Product> extractFromTable(File file){
        PDFTableExtractor extractor = new PDFTableExtractor();
        List<Table> tables = extractor.setSource(file.getAbsolutePath()).exceptLine(new int[] {0,1,2,3,4,5,6,7, 8, 9, -1 , -2, -3, -4, -5, -6, -7, -8, 10, 26,31 }).extract();
        Table table = tables.get(0);
        String csv = table.toString();
        System.out.print(csv);
        String[] lines = csv.split("\\n");
        for (String line : lines) {
            String[] items = line.split(";");
            String type = items[0];
            String index = items[1];
            int retail_num = checkWhereRetailEnds(items);
            String retail = concatenation(Arrays.copyOfRange(items, 2, retail_num+1));
            String discount = items[retail_num+1] + items[retail_num+2];
            int wholesale_num = checkWhereWholeSaleEnds(retail_num + 3, items);
            String wholesale = concatenation(Arrays.copyOfRange(items, retail_num+3, wholesale_num+1));
            String quantity  = items[wholesale_num+1] + items[wholesale_num+2];
            String total = concatenation(Arrays.copyOfRange(items, wholesale_num+3, items.length));
            productList.add(new Product(type, index, retail, discount, wholesale, quantity, total));
        }

        return productList;
    }

    public String concatenation (String[] array){
        String string = "";
        for (String s : array){
            string += s;
        }return string;
    }

    public int checkWhereRetailEnds (String[] lines){

        for (int i = 2; i < lines.length; i++){
            if (lines[i].equals("%")){
                return i - 2;
            }else continue;
        }return 0;
    }

    public int checkWhereWholeSaleEnds (int start, String[] lines){

        for (int i = start+1; i < lines.length; i++){
            if (lines[i].startsWith("¥")){
                return i - 3;
            }
        }return 0;
    }




    public static void main(String[] args){
        File file = new File("src/WS1903ST0040_武汉市凯德明科技有限公司.pdf");
        ImageProcess ip = new ImageProcess(file);
        List<Product> products = ip.extractFromTable(file);
    }

}
