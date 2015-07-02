package de.wwu.d2s.web.providers;

import java.io.IOException;
import java.lang.annotation.Annotation;
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
	 
    // 401 - Access denied
    private static final Response ACCESS_UNAUTHORIZED = Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized.").build();
 
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

            if (!authService.isAuthorized(authId, authToken, rolesAllowed)) {
                requestContext.abortWith(ACCESS_UNAUTHORIZED);
            }
        }
    }
}