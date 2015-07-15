import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelParser {
	public static List<ExcelPlatform> parsePlatforms(String filePath) {
		//Config
		Boolean hasHeader = true;
		String[] requiredCols = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
				"J", "K", "L", "M", "N", "O", "P", "Q", "AB", "AC", "AD", "AE",
				"AF", "AG", "AH", "AI", "AJ", "AK", "AU", "AV", "AW", "AX",
				"AY", "AZ", "BA", "BB", "BC", "BD" };
		String idCol = "B";
		
		

		List<ExcelPlatform> platforms = new ArrayList<ExcelPlatform>();
		try {
			FileInputStream file = new FileInputStream(new File(
					filePath));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			
			//Skip first row if table has a header
			if (hasHeader && rowIterator.hasNext())
				rowIterator.next();
			
			//iterate through all remaining rows
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				//if ID column is empty -> no valid entry -> skip row
				CellReference idr = new CellReference(idCol);
				if (getCellValue(row.getCell(idr.getCol())).isEmpty())
					continue;

				//collect row's values in an array
				String[] colValues = new String[requiredCols.length];
				int i = 0;
				for (String c : requiredCols) { //for every specified required column
					CellReference cr = new CellReference(c);
					Cell cell = row.getCell(cr.getCol()); //retrieve the cell from current row

					colValues[i++] = getCellValue(cell).trim(); //save value as string in array
				}
				
				//create platform object from array
				platforms.add(new ExcelPlatform(colValues));
			}
			workbook.close();
			file.close();
			return platforms;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getCellValue(Cell cell) {
		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			return ((int) cell.getNumericCellValue()) + "";
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		default:
			return "";
		}
	}
}