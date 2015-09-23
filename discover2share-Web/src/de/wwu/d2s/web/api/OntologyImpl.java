package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.Response;

import de.wwu.d2s.dto.PropertyInfo;
import de.wwu.d2s.ejb.OntologyService;
import de.wwu.d2s.jpa.Platform;

@Stateless
public class OntologyImpl implements OntologyApi {

	@EJB
	private OntologyService ontologyService;
	
	@Override
	public List<Platform> getAllPlatforms() {
		return ontologyService.getAllPlatforms();
	}

	@Override
	public Platform getPlatform(String url) {
		return ontologyService.getPlatform(url);
	}

	@Override
	public Map<String, Map<String, String>> getDescriptions() {
		return ontologyService.getDescriptions();
	}

	@Override
	public Response addSuggestion(Platform platform) {
		Map<String, String> result = ontologyService.addSuggestion(platform); // try adding the suggestion and retrieve the status information
		if (result.containsKey("success")) // adding succeeded
			return Response.status(Response.Status.OK).entity(result).build(); // status 200 response containing the success message
		else if (result.containsKey("error")) // error adding the suggestion
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build(); // status 500 response containing the error message
		else // no status message provided
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // status 500 response
	}
	
	@Override
	public void directSaveSuggestion(Platform platform) {
		ontologyService.directSaveSuggestion(platform);
	}

	@Override
	public List<Platform> getAllSuggestions() {
		return ontologyService.getAllSuggestions();
	}

	@Override
	public Platform getSuggestion(int id) {
		return ontologyService.getSuggestion(id);
	}

	@Override
	public Response doQuery(String query) {
		Map<String, String> result = ontologyService.doQuery(query); // try doing the query and retrieve the status information
		if (result.containsKey("success")) // querying succeeded
			return Response.status(Response.Status.OK).entity(result.get("success")).build(); // status 200 response containing the query results
		else if (result.containsKey("error")) // error while querying
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build(); // status 500 response containing the error message
		else // no status message provided
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // status 500 response
	}

	@Override
	public List<Map<String, String>> getAllCities() {
		return ontologyService.getAllCities();
	}

	@Override
	public List<Map<String, String>> getAllResourceTypes() {
		return ontologyService.getAllResourceTypes();
	}

	@Override
	public void deleteSuggestion(int id) {
		ontologyService.deleteSuggestion(id);
	}

	@Override
	public void saveSuggestion(int id) {
		ontologyService.saveSuggestion(id);
	}

	@Override
	public void editSuggestion(Platform platform) {
		ontologyService.editSuggestion(platform);
	}

	@Override
	public void removePlatform(String id) {
		ontologyService.removePlatform(id);
	}

	@Override
	public Platform getSuggestionExternal(String id) {
		return ontologyService.getSuggestionExternal(id);
	}

	@Override
	public Response editSuggestionExternal(String id, Platform platform) {
		if (ontologyService.editSuggestionExternal(id, platform)) // if the external edit succeeded
			return Response.status(Response.Status.NO_CONTENT).build(); // status 204 success response
		else // error editing
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(); // status 500 error response
	}

	@Override
	public List<PropertyInfo> getResourceDetails(String name) {
		return ontologyService.getResourceDetails(name);
	}

}
