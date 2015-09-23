package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.wwu.d2s.dto.PropertyInfo;
import de.wwu.d2s.jpa.Platform;

/**
 * API section providing all necessary methods to handle P2P SCC platforms and suggestions thereof.
 * This includes adding, editing, removing and querying them.
 */
@Path("ontology/")
public interface OntologyApi {

	/**
	 * @return A list of all platforms in the ontology
	 */
	@Path("platforms/")
	@GET
	@Produces("application/json")
	public List<Platform> getAllPlatforms();
	
	/**
	 * @param uri
	 * 			The URI to find the platform by
	 * @return The platform with the given URI
	 */
	@Path("platforms/{platform}")
	@GET
	@Produces("application/json")
	public Platform getPlatform(@PathParam("platform") String uri);
	
	/**
	 * Removes the platform with the given URI from the ontology.
	 * Only executable by authenticated moderators or administrators.
	 * 
	 * @param uri
	 * 			The URI to find the platform by
	 */
	@Path("platforms/{platform}")
	@DELETE
	@RolesAllowed(value={"admin", "mod"})
	public void removePlatform(@PathParam("platform") String url);
	
	/**
	 * @return A Map structure containing all P2P SCC Dimension (value) classes' descriptions
	 */
	@Path("descriptions/")
	@GET
	@Produces("application/json")
	public Map<String, Map<String, String>> getDescriptions();
	
	/**
	 * @return A list of all cities used in the ontology
	 */
	@Path("cities/")
	@GET
	@Produces("application/json")
	public List<Map<String, String>> getAllCities();
	
	/**
	 * @return A list of all resource types used in the ontology.
	 */
	@Path("resourceTypes/")
	@GET
	@Produces("application/json")
	public List<Map<String, String>> getAllResourceTypes();
	
	/**
	 * Saves the given platform suggestion for review by a moderator.
	 * 
	 * @param platform
	 * 			The suggestion to save
	 * @return HTTP Response with an error/success status and an error/success message
	 */
	@Path("platforms/suggestions/add")
	@POST
	@Consumes("application/json")
	public Response addSuggestion(Platform platform);
	
	/**
	 * Directly saves the given suggestion to the ontology without further review.
	 * Only executable by authenticated moderators or administrators.
	 * 
	 * @param platform
	 * 			The suggestion to add to the ontology
	 */
	@Path("platforms/suggestions/directSave")
	@POST
	@Consumes("application/json")
	@RolesAllowed(value={"admin", "mod"})
	public void directSaveSuggestion(Platform platform);
	
	/**
	 * Saves the changes to the given suggestion in the database.
	 * 
	 * @param platform
	 * 			Suggestion object containing changes and the info to identify the suggestion by in the database
	 */
	@Path("platforms/suggestions/edit")
	@POST
	@Consumes("application/json")
	@RolesAllowed(value={"admin", "mod"})
	public void editSuggestion(Platform platform);
	
	/**
	 * Only executable by authenticated moderators or administrators.
	 * 
	 * @return A list of all suggestions in the system
	 */
	@Path("platforms/suggestions/")
	@GET
	@Produces("application/json")
	@RolesAllowed(value={"admin", "mod"})
	public List<Platform> getAllSuggestions();
	
	/**
	 * Only executable by authenticated moderators or administrators.
	 * 
	 * @param id
	 * 			Suggestion ID to query by.
	 * @return The suggestion with the given ID.
	 */
	@Path("platforms/suggestions/{id}")
	@GET
	@Produces("application/json")
	@RolesAllowed(value={"admin", "mod"})
	public Platform getSuggestion(@PathParam("id") int id);
	
	/**
	 * Deletes the suggestion with the given ID.
	 * Only executable by authenticated moderators or administrators.
	 * 
	 * @param id
	 * 			ID to find the suggestion by
	 */
	@Path("platforms/suggestions/{id}")
	@DELETE
	@RolesAllowed(value={"admin", "mod"})
	public void deleteSuggestion(@PathParam("id") int id);
	
	/**
	 * Persists the suggestion with the given ID in the ontology. Deletes it afterwards.
	 * Only executable by authenticated moderators or administrators.
	 * 
	 * @param id
	 * 			ID find the suggestion by.
	 */
	@Path("platforms/suggestions/save/{id}")
	@GET
	@RolesAllowed(value={"admin", "mod"})
	public void saveSuggestion(@PathParam("id") int id);
	
	/**
	 * @param id
	 * 			External ID to find a suggestion by
	 * @return The suggestion with the given external ID
	 */
	@Path("external/{id}")
	@GET
	@Produces("application/json")
	public Platform getSuggestionExternal(@PathParam("id") String id);
	
	/**
	 * Saves changes to the suggestion with the given external ID.
	 * 
	 * @param id
	 * 			External ID to find the suggestion to overwrite by
	 * @param platform
	 * 			Suggestion object containing the changes to save
	 * @return HTTP response with an error/success status
	 */
	@Path("external/{id}")
	@POST
	@Consumes("application/json")
	public Response editSuggestionExternal(@PathParam("id") String id, Platform platform);
	
	/**
	 * Queries the triplestore's SPARQL endpoint with the given query.
	 * This API method can be used instead of directly querying the Endpoint to allow the proper display of error messages that might
	 * occur on query execution.
	 * 
	 * @param query
	 * 			SPARQL query to execute
	 * @return HTTP response containing the error/success status and query results or error message
	 */
	@Path("query/")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response doQuery(String query);
	
	/**
	 * Finds all triples in which the resource with the given name takes on the role of subject or object.
	 * 
	 * @param name
	 * 			Name of the resource to look for
	 * @return List of all properties and the respective values that the resource is connected to
	 */
	@Path("resource/{name}")
	@GET
	@Produces("application/json")
	public List<PropertyInfo> getResourceDetails(@PathParam("name") String name);
}
