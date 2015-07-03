(function(){
	'use strict';
	
	var d2sApp = angular.module('d2sApp');
	
	d2sApp.factory('authFactory', function ($rootScope, $http, $cookieStore) {
		
	        var authFactory = {};
	 
	        authFactory.login = function (user) {
	            return $http.post('api/auth/login', user);
	        };
	        
	        authFactory.register = function (user) {
	            return $http.post('api/auth/register', user);
	        };
	        
	        authFactory.setAuthData = function (authData) {
	            $cookieStore.put('authData', {
	                username: authData.username,
	                authToken: authData.authToken,
	                authRole: authData.authRole
	            });
	            $rootScope.$broadcast('authChanged');
	        };
	 
	        authFactory.getAuthData = function () {
	            return $cookieStore.get("authData");
	        };
	 
	        authFactory.isAuthenticated = function () {
	            return (!angular.isUndefined(this.getAuthData()) && !angular.isUndefined(this.getAuthData().username));
	        };
	        
	        authFactory.logout = function(){
	        	$cookieStore.remove("authData");
	        };
	 
	    return authFactory;
	});
	
	d2sApp.factory('authHttpInterceptor', function ($rootScope, $injector, $cookieStore) {
	    var authHttpInterceptor = {
	        request: function ($request) {
	            var authFactory = $injector.get('authFactory');
	            if (authFactory.isAuthenticated()) {
	                $request.headers['auth-id'] = authFactory.getAuthData().username;
	                $request.headers['auth-token'] = authFactory.getAuthData().authToken;
	            }
	            return $request;
	        },
		    responseError: function (res) {
	            if(res.status === 401 && !angular.isUndefined(res.data.authCode)){
	            	var authCode = res.data.authCode;
	            	if(authCode !== "-1")
	            		$cookieStore.remove("authData");
	            }
	            return res;
	        }
	    };
	 
	    return authHttpInterceptor;
	});
	//Add interceptor to chain:
	d2sApp.config(function ($httpProvider) {
		$httpProvider.interceptors.push('authHttpInterceptor');
	});
	
})();