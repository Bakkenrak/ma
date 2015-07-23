import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

public class AlexaParser {

	public static Map<String, Double> parseAlexa(String website) {
		// turn off output of irrelevant information
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

		WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false); // faster execution, less errors
		webClient.getOptions().setCssEnabled(false);

		HtmlPage currentPage;
		try {
			currentPage = webClient.getPage("http://www.alexa.com/siteinfo/" + website);

			HtmlTable countryTable = currentPage.getHtmlElementById("demographics_div_country_table"); // parse table
			String cssClass = countryTable.getAttribute("class");

			if (!cssClass.contains("data-table-nodata")) { // check if country data is available
				DomNodeList<HtmlElement> countryTableRows = countryTable.getHtmlElementsByTagName("tbody").get(0)
						.getElementsByTagName("tr"); // parse table rows

				Map<String, Double> tableData = new HashMap<String, Double>();

				for (HtmlElement line : countryTableRows) {
					DomNodeList<HtmlElement> cells = line.getElementsByTagName("td"); // parse row cells
					// remove the % sign from the second cell's content and convert to double
					Double percentage = Double.parseDouble(cells.get(1).asText().replace("%", "").trim());
					String name = cells.get(0).asText().replace(String.valueOf((char) 160), " ").trim().replace(" ", "%20"); //replace non-breakable whitespace and trim
					tableData.put(name, percentage); // add to output map
				}
				return tableData;
			}
		} catch (FailingHttpStatusCodeException | IOException e) {
			System.out.println("Error parsing Alexa page for website " + website);
		}
		return null;
	}

}
