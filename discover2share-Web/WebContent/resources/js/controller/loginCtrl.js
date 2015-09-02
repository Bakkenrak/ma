(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * The controller providing functionality for the login view.
	 */
	d2sApp.controller('loginCtrl', function ($scope, $location, authFactory, toaster) {
		$scope.loggedOut = false;
		$scope.loginFailed = false;

		if ($location.path().substr(1) === "logout") { // if logout route was called
			authFactory.logout(); // logout the user by deleting his auth data
			$scope.loggedOut = true; // set status in scope
			toaster.pop('info', 'Logged out!', 'You were logged out successfully.');
		}

		$scope.user = { // initialize the user object that is bound to the form elements
			username : "",
			password : ""
		};

		/**
		 * Calls the auth factory method to login the user. Passes the information from the login form.
		 */
		$scope.login = function () {
			authFactory.login($scope.user).success(function (data, status) {
					if (status !== 200) { // login error
						$scope.loginFailed = true; // set status in scope to display message in view
					} else { // success
						authFactory.setAuthData(data); // save auth data in cookie
						$scope.loginFailed = false; // set login status in scope
						$location.path("/"); // redirect to home page
						toaster.pop('success', 'Logged in!', 'Welcome back, ' + $scope.user.username + '.');
					}
				}).error(function () {
					$scope.loginFailed = true;
					$scope.loggedOut = false;
				});
		};
	});
	
})();