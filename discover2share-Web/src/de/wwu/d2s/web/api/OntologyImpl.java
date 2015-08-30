package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

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
	public void addSuggestion(Platform platform) {
		ontologyService.addSuggestion(platform);
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
		Map<String, String> result = ontologyService.doQuery(query);
		if (result.containsKey("success"))
			return Response.status(Response.Status.OK).entity(result.get("success")).build();
		else if (result.containsKey("error"))
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		else
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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

}
