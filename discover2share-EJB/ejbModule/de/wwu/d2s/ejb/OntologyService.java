package de.wwu.d2s.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import de.wwu.d2s.dto.PropertyInfo;
import de.wwu.d2s.jpa.Platform;

/**
 * Interface offering all operations related to the ontology and its platforms
 */
@Remote
public interface OntologyService {
	/**
	 * @return All platforms from the ontology
	 */
	public List<Platform> getAllPlatforms();

	/**
	 * @param uri
	 * @return The platform that matches the given URI
	 */
	public Platform getPlatform(String uri);

	/**
	 * @return The description texts of all dimension (sub-)classes in the ontology
	 */
	public Map<String, Map<String, String>> getDescriptions();

	/**
	 * Persists a platform suggestion in the relational database.
	 * 
	 * @param platform
	 *            Suggestion
	 * @return The external ID of the new suggestion in case of success, otherwise the error message
	 */
	public Map<String, String> addSuggestion(Platform platform);

	/**
	 * Skips the persisting of the given suggestion for review and adds it to the ontology right away
	 * 
	 * @param suggestion
	 */
	public void directSaveSuggestion(Platform suggestion);

	/**
	 * @return All platform suggestions in the relational database
	 */
	public List<Platform> getAllSuggestions();

	/**
	 * @param id
	 * @return The suggestion with the given ID
	 */
	public Platform getSuggestion(int id);

	/**
	 * Runs the given query against the SPARQL endpoint.
	 * 
	 * @param query
	 *            SPARQL query
	 * @param inference 
	 * 			  Denotes whether to activate inferencing before querying
	 * @return Query Results in case of success, otherwise error message
	 */
	public Map<String, String> doQuery(String query, String inference);

	/**
	 * @return All cities found in the ontology
	 */
	public List<Map<String, String>> getAllCities();

	/**
	 * @return All resource types found in the ontology
	 */
	public List<Map<String, String>> getAllResourceTypes();

	/**
	 * Deletes the suggestion with the given ID.
	 * 
	 * @param id
	 */
	public void deleteSuggestion(int id);

	/**
	 * Adds the suggestion with the given ID to the ontology. Deletes it from the relational database.
	 * 
	 * @param id
	 */
	public void saveSuggestion(int id);

	/**
	 * Updates the given platform suggestion in the database
	 * 
	 * @param suggestion
	 */
	public void editSuggestion(Platform suggestion);

	/**
	 * Removes the platform with the given URI from the ontology as well as all triples referencing it.
	 * 
	 * @param uri
	 */
	public void removePlatform(String uri);

	/**
	 * Retrieves a suggestion by a call using the external ID.
	 * 
	 * @param id Suggestion's external ID
	 * @return The suggestion with the given external ID
	 */
	public Platform getSuggestionExternal(String id);

	/**
	 * Updates the given platform from a call using the suggestion's external ID.
	 * 
	 * @param id Suggestion's external ID
	 * @param platform The edited suggestion
	 * @return true, if update successful, otherwise false
	 */
	public boolean editSuggestionExternal(String id, Platform platform);

	/**
	 * Finds all triples in which the resource with the given name takes on the role of subject or object.
	 * 
	 * @param name
	 * 			Name of the resource to look for
	 * @return List of all properties and the respective values that the resource is connected to
	 */
	public List<PropertyInfo> getResourceDetails(String name);

}
