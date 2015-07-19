package de.wwu.d2s.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface OntologyService {
	
	public List<Map<String, String>> getAllPlatforms();

	public Map<String, String> getPlatform(String url);
	
}
