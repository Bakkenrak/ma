(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Controller providing functionality for the user registration form
	 */
	d2sApp.controller('registrationCtrl', function ($scope, $location, authFactory) {
		// save in scope whether the current user is an admin
		$scope.isAdmin = (authFactory.isAuthenticated() && authFactory.getAuthData().authRole === "admin");
		$scope.user = {};
		$scope.registerFailed = false;
		$scope.registerSuccess = false;

		/**
		 * Calls the auth factory method to create a new user account.
		 */
		$scope.register = function () {
			if ($scope.user.password === $scope.user.passwordConfirm) { // check if the password was confirmed correctly
				authFactory.register($scope.user).success(function (data, status) {
						$scope.registerFailed = (status !== 200 && status !== 204);
						$scope.registerSuccess = (status === 200 || status === 204);
						$scope.user = {};
						if ($scope.registerFailed) { // error
							$scope.errorMessage = data.error; // display message
						}
					}).error(function () {
						$scope.registerFailed = true;
						$scope.registerSuccess = false;
					});
			}
		};
	});
	
})();