package de.wwu.d2s.web.providers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.ejb.AuthService;

/**
 * Interceptor that is called on every incoming request.
 * Runs an authorization check for the auth info included in the request.
 * In case the request is not authorized to run the requested method, a 401 
 * HTTP error is issued with additional information in the body.
 * 
 * Derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
 */
@Provider
public class AuthSecurityInterceptor implements ContainerRequestFilter {
	  
    @EJB
    AuthService authService;
 
    @Context
    private HttpServletRequest request;
 
    @Context
    private ResourceInfo resourceInfo;
 
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Get AuthId and AuthToken from HTTP-Header.
        String authId = requestContext.getHeaderString(AuthAccessElement.PARAM_AUTH_ID);
        String authToken = requestContext.getHeaderString(AuthAccessElement.PARAM_AUTH_TOKEN);
        
        // Get method invoked.
        Method methodInvoked = resourceInfo.getResourceMethod();
        
        if (methodInvoked.isAnnotationPresent(RolesAllowed.class)) {
        	// Gather allowed roles for the requested method to list
            RolesAllowed rolesAllowedAnnotation = methodInvoked.getAnnotation(RolesAllowed.class);
            Set<String> rolesAllowed = new HashSet<>(Arrays.asList(rolesAllowedAnnotation.value()));

            int authResult = authService.isAuthorized(authId, authToken, rolesAllowed);
            if (authResult<1) { //if caller is not authorized for the method
                requestContext.abortWith(buildResponse(authResult)); // abort request
            }
        }
    }
    
    private Response buildResponse(int authCode){
    	String message = "Not authorized.";
    	
		if(authCode == authService.NO_SESSION){
			message = message + " No session found.";
		}else if(authCode == authService.EXPIRED){
			message = message + " The session has expired.";
		}else if(authCode == authService.WRONG_ROLE){
			message = message + " Improper user role to access this endpoint.";
		}
    	
		String response = "{ \"authCode\" : \"" + authCode + "\", \"message\": \""
							+ message + "\"}";
		
    	return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
    }
}