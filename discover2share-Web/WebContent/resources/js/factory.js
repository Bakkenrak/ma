(function(){
	'use strict';
	
	var d2sApp = angular.module('d2sApp');
	
	d2sApp.factory('authFactory', function ($rootScope, $http, $cookieStore) {
		
	        var authFactory = {
	            authData: $cookieStore.get('authData')
	        };
	 
	        authFactory.login = function (user) {
	            return $http.post('api/auth/login', user);
	        };
	        
	        authFactory.setAuthData = function (authData) {
	            this.authData = {
	                authId: authData.authId,
	                authToken: authData.authToken,
	                authPermission: authData.authPermission
	            };
	            $cookieStore.put('authData', this.authData);
	            $rootScope.$broadcast('authChanged');
	        };
	 
	        authFactory.getAuthData = function () {
	            return this.authData;
	        };
	 
	        authFactory.isAuthenticated = function () {
	            return !angular.isUndefined(this.getAuthData());
	        };
	 
	    return authFactory;
	});
	
	d2sApp.factory('authHttpRequestInterceptor', ['$rootScope', '$injector', function ($rootScope, $injector) {
	    var authHttpRequestInterceptor = {
	        request: function ($request) {
	            var authFactory = $injector.get('authFactory');
	            if (authFactory.isAuthenticated()) {
	                $request.headers['auth-id'] = authFactory.getAuthData().authId;
	                $request.headers['auth-token'] = authFactory.getAuthData().authToken;
	            }
	            return $request;
	        }
	    };
	 
	    return authHttpRequestInterceptor;
	}]);
	//Add interceptor to chain:
	d2sApp.config(function ($httpProvider) {
		$httpProvider.interceptors.push('authHttpRequestInterceptor');
	});
	
})();