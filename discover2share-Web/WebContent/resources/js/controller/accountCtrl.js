(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Controller for the /account route and view. Provides functionality for changing the user's own password or deleting his account. 
	 * Allows the deletion of outher users' accounts for admins.
	 */
	d2sApp.controller('accountCtrl', function ($scope, $location, toaster, authFactory) {
		$scope.user = authFactory.getAuthData(); // retrieve user information from auth cookie
		
		$scope.deleteUser = { // instantiate DTO to use when deleting the user's own account
				username: $scope.user.username
			};
		
		/**
		 * Retrieves all users from the server (apart from the one currently logged in) using the auth factory. 
		 * Sets the result to $scope.users.
		 * Only executable by admins.
		 */
		$scope.getUsers = function () {
			if ($scope.user.authRole === 'admin') { // if user is admin
				authFactory.getUsers().success(function (data, status) { // call server
					if (status === 200) { // on success
						$scope.users = data.filter(function (user) { // filter array
							return (user.username !== $scope.user.username); // remove the currently logged in user from list
						});
					}
				});
			}
		};
		$scope.getUsers(); // retrieve users right away on startup
		
		/**
		 * @return The username of the logged in user or false if no valid login was found.
		 */
		$scope.loggedIn = function () {
			if (authFactory.isAuthenticated()) {
				return authFactory.getAuthData().username;
			} else {
				return false;
			}
		};
		
		/**
		 * Calls the auth factory method to change the current user's password.
		 */
		$scope.changePassword = function () {
			authFactory.changePassword($scope.user).success(function (data, status) {
				if (status === 200 || status === 204) { // success
					toaster.pop("success", "Password changed!", "Your password was changed successfully.");
					$scope.user = authFactory.getAuthData(); // reset form
				} else { // error
					toaster.pop("error", "Code " + status, "Password couldn't be changed. Be sure to provide the correct current password.");
				}
			});
		};
		
		/**
		 * Calls the auth factory method to delete the current user's own account.
		 */
		$scope.deleteOwnAccount = function () {
			authFactory.deleteOwnAccount($scope.deleteUser).success(function (data, status) {
				if (status === 200 || status === 204) { // success
					toaster.pop("success", "Account deleted!", "Your account was successfully deleted.");
					authFactory.logout(); // delete auth cookie
					$location.path("/"); // redirect to home page
				} else { // error
					toaster.pop("error", "Code " + status, "The account could not be deleted. Be sure to provide the correct current password.");
				}
			});
		};
		
		/**
		 * Calls the auth factory method to delete a user account.
		 * Only executable for admins.
		 */
		$scope.deleteAccount = function (user) {
			authFactory.deleteAccount(user).success(function (data, status) {
				if (status === 200 || status === 204) { // success
					toaster.pop("success", "Account deleted!", "Successfully deleted account '" + user.username + "'.");
					$scope.getUsers(); // update list of all users
				} else { // error
					toaster.pop("error", "Code " + status, "Couldn't delete account '" + user.username + "'.");
				}
			});
		};
		
		// on startup, check if the user is logged in
		if (!$scope.loggedIn()) { // if not
			toaster.pop("error", "Unauthorized!", "You need to be logged in to view the contents of that page.");
			$location.path("/"); // redirect to home page
		}
	});
	
})();