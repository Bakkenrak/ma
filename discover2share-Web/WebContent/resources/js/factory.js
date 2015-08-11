(function(){
	'use strict';
	
	var d2sApp = angular.module('d2sApp');

	
	/**
	 *	Provides methods for user authentication. This encompasses API communication
	 *	and cookie management.
	 *
	 *	derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
	 */
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
	
	/**
	 *	Is called on every request or response of an HTTP call. Adds authentication info
	 *	from cookies to requests. Removes old authentication info from cookies on responses
	 *	with error code 401 due to an expired authentication.
	 *
	 *	derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
	 */
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
	
	
	d2sApp.factory('platformFactory', function ($http){
		var platformFactory = {};

		platformFactory.getCountries = function() {
			return $http.get('resources/js/countries.json');
		};
		
		platformFactory.getAllPlatforms = function() {
			return $http.get('api/platforms');
		};
		
		platformFactory.getPlatform = function(platform) {
			return $http.get('api/platforms/' + platform);
		};
		
		platformFactory.getDescriptions = function(){
			return $http.get('api/platforms/descriptions');
		};
		
		platformFactory.getAllSuggestions = function(){
			return $http.get('api/platforms/suggestions/');
		};
		
		platformFactory.addPlatformSuggestion = function(platform) {
			return $http.post('api/platforms/suggestions/new', platform);
		};
		
		
		
		platformFactory.getGeoData = function(geoUrl){
			var geoId = geoUrl.replace("http://www.geonames.org/", "");
			return $http.jsonp('http://api.geonames.org/getJSON?username=demo&geonameId=' + geoId +"&callback=JSON_CALLBACK");
		};
		
		platformFactory.findCity = function(cityName, country){
			var query = "http://api.geonames.org/searchJSON?username=discover2share&maxRows=10&featureClass=P&q=" + cityName;
			if(!angular.isUndefined(country) && country !== ""){
				query = query + "&country=" + country;
			}
			return $http.jsonp(query+"&callback=JSON_CALLBACK");
		}
		
		platformFactory.findCountry = function(countryName){
			var query = "http://api.geonames.org/searchJSON?username=discover2share&maxRows=10&featureClass=A&isNameRequired&q=" + countryName;
			return $http.get(query);
		}		
		return platformFactory;
	});
})();