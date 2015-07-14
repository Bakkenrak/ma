import java.util.List;

public class Start {

	public static void main(String[] args) {
		List<ExcelPlatform> platforms = ExcelParser
				.parsePlatforms("D:\\MA\\Tabelle.xlsx");

		OntologyWriter ontWriter = new OntologyWriter();
		ontWriter.writeAll(platforms, "D:\\a.rdf");
	}
}
