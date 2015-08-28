package de.wwu.d2s.web.api;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.jpa.User;

@Path("/auth")
@Produces("application/json")
@Consumes("application/json")
public interface AuthApi {
 
    @POST
    @Path("login")
    @PermitAll
    public AuthAccessElement login(AuthLoginElement loginElement);
    
    @POST
    @Path("register")
    @RolesAllowed(value={"admin"})
    public Response registerUser(@Context HttpServletRequest request, User user);
}