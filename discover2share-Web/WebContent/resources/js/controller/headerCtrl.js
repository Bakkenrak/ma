(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Header controller that provides functionality for the navigation bar on the top of the page.
	 */
	d2sApp.controller('headerCtrl', function ($scope, $rootScope,	$location, $cookieStore, authFactory, toaster) {

		/**
		 * Watches for route changes. Sets the currentTab variable in the scope and checks whether the user currently has auth data.
		 */
		$rootScope.$on('$locationChangeSuccess', function (event) {
			$scope.currentTab = $location.path().substr(1); // set scope variable to current route
			
			$scope.loggedIn = authFactory.isAuthenticated(); // determine if logged in from cookie
			if ($scope.loggedIn) { // if logged in
				$scope.authRole = authFactory.getAuthData().authRole; // set auth data to scope
				$scope.username = authFactory.getAuthData().username;
			} else {
				$scope.authRole = undefined; // remove auth data from scope
				$scope.username = undefined;
			}
		});

		/**
		 * Calls the auth factory method to remove the auth data cookie.
		 */
		$scope.logout = function () {
			authFactory.logout();
			toaster.pop('success', 'Logged out!', 'You were logged out successfully.');
		};
	});
	
})();