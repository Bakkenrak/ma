(function () {
	'use strict';
	
	var d2sApp = angular.module('d2sApp');

	
	/**
	 *	Provides methods for user authentication. This encompasses API communication
	 *	and cookie management.
	 *
	 *	derived partly from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
	 */
	d2sApp.factory('authFactory', function ($rootScope, $http, $cookieStore) {
		var authFactory = {};

		authFactory.login = function (user) {
			return $http.post('api/auth/login', user); // POST call to API
		};

		authFactory.register = function (user) {
			return $http.post('api/auth/register', user);
		};
		
		authFactory.getUsers = function () {
			return $http.get('api/auth/users');
		};
		
		authFactory.changePassword = function (user) {
			return $http.post('api/auth/changePassword', user);
		};
		
		authFactory.deleteOwnAccount = function (user) {
			return $http.post('api/auth/deleteOwnAccount', user);
		};
		
		authFactory.deleteAccount = function (user) {
			return $http.post('api/auth/deleteAccount', user);
		};

		authFactory.setAuthData = function (authData) {
			$cookieStore.put('authData', { // set new cookie
				username: authData.username,
				authToken: authData.authToken,
				authRole: authData.authRole
			});
			$rootScope.$broadcast('authChanged');
		};

		authFactory.getAuthData = function () {
			return $cookieStore.get("authData"); // get cookie
		};

		authFactory.isAuthenticated = function () { //is authenticated when auth data containing a username is present
			return (!angular.isUndefined(this.getAuthData()) && !angular.isUndefined(this.getAuthData().username));
		};

		authFactory.logout = function () {
			$cookieStore.remove("authData"); // remove cookie
		};

		return authFactory;
	});
	
})();