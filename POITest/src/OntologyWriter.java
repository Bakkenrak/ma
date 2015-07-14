import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class OntologyWriter {

	private final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	private final String D2S = "http://www.discover2share.org/d2s-ont/";
	private final String DBPP = "http://dbpedia.org/property/";
	private final String DBPR = "http://dbpedia.org/resource/";
	private final String DBPO = "http://dbpedia.org/ontology/";
	private final String DCT = "http://purl.org/dc/terms/";
	private final String LGD = "http://linkedgeodata.org/ontology/";
	private final String SKOS = "http://www.w3.org/2004/02/skos/core#";
	private final String TIME = "http://www.w3.org/2006/time#";
	private final String WORDNET = "http://wordnet-rdf.princeton.edu/wn31/";

	private OntModel ontologyModel;
	
	private Resource currentResource;
	private ExcelPlatform currentPlatform;

	public OntologyWriter() {
		ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ontologyModel.setNsPrefix("dbpp", DBPP);
		ontologyModel.setNsPrefix("dbpr", DBPR);
		ontologyModel.setNsPrefix("dbpo", DBPO);
		ontologyModel.setNsPrefix("dct", DCT);
		ontologyModel.setNsPrefix("lgd", LGD);
		ontologyModel.setNsPrefix("skos", SKOS);
		ontologyModel.setNsPrefix("time", TIME);
		ontologyModel.setNsPrefix("wordnet", WORDNET);

		Map<String, String> a = ontologyModel.getNsPrefixMap();
	}

	public void writeAll(List<ExcelPlatform> platforms, String outputFile) {
		for (ExcelPlatform platform : platforms) {
			currentPlatform = platform;
			constructPlatform();
		}

		try (OutputStream out = new FileOutputStream(outputFile)) {
			ontologyModel.write(out, "Turtle");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void constructPlatform() {
		initializePlatform();

		resourceTypeDimension();
	}
	
	public void initializePlatform(){
		// Create new platform resource
		currentResource = ontologyModel.createResource(D2S + currentPlatform.getIdNew());
		// Create and add rdfs:label property
		Property label = ontologyModel.createProperty(RDFS + "label");
		currentResource.addProperty(label, currentPlatform.getName(), XSDDatatype.XSDstring);
		// Create and add dbpp:url property
		Property url = ontologyModel.createProperty(DBPP + "url");
		currentResource.addProperty(url, currentPlatform.getStrippedUrl(), XSDDatatype.XSDanyURI);
	}
	
	public void resourceTypeDimension(){
		// Create new resource type resource
		Resource resourceType = ontologyModel.createResource(D2S
				+ currentPlatform.getIdNew() + "_resource_type");
		// Create and add rdfs:label property
		Property rTLabel = ontologyModel.createProperty(RDFS + "label");
		resourceType.addProperty(rTLabel, currentPlatform.getResourceType(),
				XSDDatatype.XSDstring);
		// Create and add d2s:has_resource_type
		Property hasResourceType = ontologyModel.createProperty(D2S
				+ "has_resource_type");
		currentResource.addProperty(hasResourceType, resourceType);
	}
}
