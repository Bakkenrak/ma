package de.wwu.d2s.web.api;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.wwu.d2s.jpa.Person;

@Path("ind/")
public interface IndexApi {
	@GET
	@Produces("application/json")
	public List<Person> getTests();
	
	@Path("vip")
	@GET
	@Produces("appication/json")
	@RolesAllowed(value = {"admin"})
	public String vip();
}
