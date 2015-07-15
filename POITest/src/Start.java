import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Start {

	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));       

        File xlsx;
        do{
        	System.out.println("Correct path and filename of .xlsx source file required:");
            String xlsxPath = br.readLine();
        	xlsx = new File(xlsxPath);
        }while(!xlsx.exists());
        
        System.out.println("Path and filename to save output file under:");
        String owl = br.readLine();
		
		List<ExcelPlatform> platforms = ExcelParser
				.parsePlatforms(xlsx);

		OntologyWriter ontWriter = new OntologyWriter();
		ontWriter.writeAll(platforms, owl);
	}
}
