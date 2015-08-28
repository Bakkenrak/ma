package de.wwu.d2s.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import de.wwu.d2s.jpa.Platform;

@Remote
public interface OntologyService {
	
	public List<Platform> getAllPlatforms();

	public Platform getPlatform(String url);
	
	public Map<String, Map<String, String>> getDescriptions();
	
	public void addSuggestion(Platform platform);
	
	public void directSaveSuggestion(Platform platform);
	
	public List<Platform> getAllSuggestions();

	public Platform getSuggestion(int id);

	public Map<String, String> doQuery(String query);

	public List<Map<String, String>> getAllCities();

	public List<Map<String, String>> getAllResourceTypes();

	public void deleteSuggestion(int id);

	public void saveSuggestion(int id);
	
}
