package de.wwu.d2s.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import de.wwu.d2s.jpa.Platform;

@Remote
public interface OntologyService {
	
	public List<Map<String, String>> getAllPlatforms();

	public Platform getPlatform(String url);
	
	public Map<String, Map<String, String>> getDescriptions();
	
	public void createPlatform(Platform platform);
	
	public List<Platform> getAllSuggestions();
	
}
