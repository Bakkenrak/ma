package de.wwu.d2s.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Structure used to transfer triple information for a given property and resource.
 * Contains all values that this property takes on for the resource.
 */
public class ResourceDetails implements Serializable {
	private static final long serialVersionUID = -5999577844440036250L;

	private boolean isProperty; // is property of another resource, i.e. the current resource is the object in that triple
	
	private String name; // name of the property
	
	private Map<String, Map<String, String>> values = new HashMap<String, Map<String, String>>(); // resources on the other side of the triples with this property
	
	public ResourceDetails(boolean isProperty, String name){
		this.isProperty = isProperty;
		this.name = name;
	}

	public boolean isProperty() {
		return isProperty;
	}

	public void setProperty(boolean isProperty) {
		this.isProperty = isProperty;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Map<String, String>> getValues() {
		return values;
	}

	public void setValues(Map<String, Map<String, String>> values) {
		this.values = values;
	}
	
	/**
	 * Check if the given resource or literal is already contained in the values Map.
	 * @param node
	 * 			Literal or resource to find in the values Map
	 * @return Boolean, whether the literal/reource already exists in the Map.
	 */
	public boolean containsValue(RDFNode node) {
		if (node.isLiteral()) { // if node is literal
			String literal = node.asLiteral().getString(); // extract string representation
			return (values.containsKey(literal) && values.get(literal).get("type").equals("literal")); // check if literal with this name is already present
		} else if (node.isResource()) { // if node is resource
			String uri = node.asResource().getURI(); // get its URI
			return uri != null && values.containsKey(uri) && values.get(uri).get("type").equals("uri"); // check if uri with this name is already present
		}
		return false;
	}
	
	/**
	 * Adds the given literal or resource to the values Map if it is not present there yet.
	 * 
	 * @param node
	 * 			The literal/resource to add.
	 */
	public void addValue(RDFNode node) {
		if(node == null || containsValue(node)) return;
		
		if (node.isLiteral()) { // if node is literal
			String literal = node.asLiteral().getString(); // extract string representation
			
			Map<String, String> value = new HashMap<String, String>(); // new map
			value.put("name", literal);
			value.put("type", "literal");
			values.put(literal, value); // add to values map with the literal as key
		} else if (node.isResource()) { // if node is resource
			String uri = node.asResource().getURI(); // get its URI
			if (uri != null) { // check if uri is not null to avoid errors with anonymous nodes
				Map<String, String> value = new HashMap<String, String>(); // new map
				value.put("name", uri);
				value.put("type", "uri");
				values.put(uri, value); // add to values map with the uri as key
			}
		}
	}
}
