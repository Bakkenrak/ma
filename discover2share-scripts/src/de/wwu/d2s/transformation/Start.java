package de.wwu.d2s.transformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Run this class to transform an excel file containing P2P SCC platforms into instances of the D2S ontology.
 */
public class Start {

	public static void main(String[] args) throws IOException {
		// Have user provide the path to the excel file
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		File xlsx;
		do {
			System.out.println("Correct path and filename of .xlsx source file required:");
			String xlsxPath = br.readLine();
			xlsx = new File(xlsxPath);
		} while (!xlsx.exists()); // repeat while no valid file was provided

		System.out.println("Path and filename to save output file under:");
		String owl = br.readLine();

		List<ExcelPlatform> platforms = ExcelParser.parsePlatforms(xlsx); // parse platform objects from excel file.

		OntologyWriter ontWriter = new OntologyWriter();
		ontWriter.writeAll(platforms, owl); // transform platform objects into RDF
	}
}
