(function () {
	'use strict';
	
	var d2sApp = angular.module('d2sApp');
	
	/**
	 *	Is called on every request or response of an HTTP call. Adds authentication info
	 *	from cookies to requests. Removes old authentication info from cookies on responses
	 *	with error code 401 due to an expired authentication.
	 *
	 *	derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
	 */
	d2sApp.factory('authHttpInterceptor', function ($rootScope, $injector, $cookieStore) {
		var authHttpInterceptor = {
			request: function ($request) { // on requests
				if ($request.url.substr(0, 3) === "api") { // attach auth only to api calls
					var authFactory = $injector.get('authFactory');
					if (authFactory.isAuthenticated()) { // if auth data is present
						$request.headers['auth-id'] = authFactory.getAuthData().username; // add user name
						$request.headers['auth-token'] = authFactory.getAuthData().authToken; // and auth token to headers
					}
				}
				return $request;
			},
			responseError: function (res) { // on responses with an erroneous status
				if (res.status === 401 && !angular.isUndefined(res.data.authCode)) { // unauthorized with auth code
					var authCode = res.data.authCode;
					if (authCode !== "-1") { // auth code -1 meaning no valid auth data
						var authFactory = $injector.get('authFactory');
						authFactory.logout(); // remove auth data cookie
					}
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