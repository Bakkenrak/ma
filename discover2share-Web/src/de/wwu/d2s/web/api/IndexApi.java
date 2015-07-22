package de.wwu.d2s.web.api;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("ind/")
public interface IndexApi {
	
	@Path("vip")
	@GET
	@Produces("appication/json")
	@RolesAllowed(value = {"admin", "mod"})
	public String vip();
}
