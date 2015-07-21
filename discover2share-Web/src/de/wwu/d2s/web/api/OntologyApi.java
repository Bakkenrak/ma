package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.hp.hpl.jena.query.ResultSet;

@Path("platforms/")
public interface OntologyApi {

	@GET
	@Produces("application/json")
	public List<Map<String, String>> getAllPlatforms();
	
	@Path("{platform}")
	@GET
	@Produces("application/json")
	public Map<String, List<String>> getPlatform(@PathParam("platform") String url);
	
	@Path("descriptions/")
	@GET
	@Produces("application/json")
	public Map<String, Map<String, String>> getDescriptions();
}
