package de.wwu.d2s.transformation;

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

/**
 * Provides a class to parse information about user countries from an Alexa.com page.
 */
public class AlexaParser {

	/**
	 * Parses the user country data from the given alexa page if possible
	 * 
	 * @param website
	 *            The website to lookup on alexa
	 * @return Map with country names as keys and percentage of overall users as values
	 */
	public static Map<String, Double> parseAlexa(String website) {
		// turn off output of irrelevant information
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

		WebClient webClient = new WebClient(); // create virtual browser
		webClient.getOptions().setJavaScriptEnabled(false); // faster execution, less errors
		webClient.getOptions().setCssEnabled(false);

		HtmlPage currentPage;
		try {
			currentPage = webClient.getPage("http://www.alexa.com/siteinfo/" + website); // browse to page

			// find the country table
			HtmlTable countryTable = currentPage.getHtmlElementById("demographics_div_country_table");
			String cssClass = countryTable.getAttribute("class"); // read the table's css classes

			// if "data-table-nodata" is not among the table's classes, data is available
			if (!cssClass.contains("data-table-nodata")) {
				DomNodeList<HtmlElement> countryTableRows = countryTable.getHtmlElementsByTagName("tbody").get(0)
						.getElementsByTagName("tr"); // parse table rows

				Map<String, Double> tableData = new HashMap<String, Double>(); // map to output

				for (HtmlElement line : countryTableRows) { // for each row
					DomNodeList<HtmlElement> cells = line.getElementsByTagName("td"); // parse row cells
					// remove the % sign from the second cell's content and convert to double
					Double percentage = Double.parseDouble(cells.get(1).asText().replace("%", "").trim());
					// replace non-breakable whitespace, trim, replace normal whitespaces with %20 to avoid problems
					// when querying with this string at geonames.org
					String name = cells.get(0).asText().replace(String.valueOf((char) 160), " ").trim()
							.replace(" ", "%20");
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
