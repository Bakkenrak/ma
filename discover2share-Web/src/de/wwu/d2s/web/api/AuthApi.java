package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
    public Response registerUser(User user);
    
    @POST
    @Path("changePassword")
    @RolesAllowed(value={"admin", "mod"})
    public Response changePassword(User user);
    
    @POST
    @Path("deleteOwnAccount")
    @RolesAllowed(value={"admin", "mod"})
    public Response deleteOwnAccount(AuthLoginElement user);
    
    @POST
    @Path("deleteAccount")
    @RolesAllowed(value={"admin"})
    public Response deleteAccount(User user);
    
    @GET
    @Path("users")
    @RolesAllowed(value={"admin"})
    public List<AuthAccessElement> getUsers();
}