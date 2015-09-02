(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Index controller that provides functionality for the home page.
	 */
	d2sApp.controller('indexCtrl', function ($scope, authFactory) {
		
		/**
		 * @return The username if the user is currently logged in, false otherwise
		 */
		$scope.loggedIn = function () {
			if (authFactory.isAuthenticated()) {
				return authFactory.getAuthData().username;
			} else {
				return false;
			}
		};
	});
	
})();