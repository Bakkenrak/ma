package de.wwu.d2s.web.providers;

import java.io.IOException;
import java.io.Serializable;
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
            RolesAllowed rolesAllowedAnnotation = methodInvoked.getAnnotation(RolesAllowed.class);
            Set<String> rolesAllowed = new HashSet<>(Arrays.asList(rolesAllowedAnnotation.value()));

            int authResult = authService.isAuthorized(authId, authToken, rolesAllowed);
            if (authResult<1) {
                requestContext.abortWith(buildResponse(authResult));
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