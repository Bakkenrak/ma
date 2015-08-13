package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.wwu.d2s.jpa.Platform;

@Path("ontology/")
public interface OntologyApi {

	@Path("platforms/")
	@GET
	@Produces("application/json")
	public List<Platform> getAllPlatforms();
	
	@Path("platforms/{platform}")
	@GET
	@Produces("application/json")
	public Platform getPlatform(@PathParam("platform") String url);
	
	@Path("descriptions/")
	@GET
	@Produces("application/json")
	public Map<String, Map<String, String>> getDescriptions();
	
	@Path("platforms/suggestions/new")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public void createPlatform(Platform platform);
	
	@Path("platforms/suggestions/")
	@GET
	@Produces("application/json")
	@RolesAllowed(value={"admin", "mod"})
	public List<Platform> getAllSuggestions();
	
	@Path("platforms/suggestions/{id}")
	@GET
	@Produces("application/json")
	@RolesAllowed(value={"admin", "mod"})
	public Platform getSuggestion(@PathParam("id") int id);
	
	@Path("query/")
	@POST
	@Produces("application/json")
	@Consumes("application/x-www-form-urlencoded")
	public String doQuery(String query);
}
