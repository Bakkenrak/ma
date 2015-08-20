package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

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
	public String doQuery(String query) {
		return ontologyService.doQuery(query);
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

}
