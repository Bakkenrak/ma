(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('accountCtrl', function ($scope, $location, toaster, authFactory) {
		$scope.user = authFactory.getAuthData();
		$scope.deleteUser = {
				username: $scope.user.username
			};
		$scope.getUsers = function () {
			if ($scope.user.authRole === 'admin') {
				authFactory.getUsers().success(function (data, status) {
					if (status === 200) {
						$scope.users = data.filter(function (user) {
							return (user.username !== $scope.user.username);
						});
					}
				});
			}
		};
		$scope.getUsers();
		
		$scope.loggedIn = function () {
			if (authFactory.isAuthenticated()) {
				return authFactory.getAuthData().username;
			} else {
				return false;
			}
		};
		
		$scope.changePassword = function () {
			authFactory.changePassword($scope.user).success(function (data, status) {
				if (status === 200 || status === 204) {
					toaster.pop("success", "Password changed!", "Your password was changed successfully.");
					$scope.user = authFactory.getAuthData(); // reset form
				} else {
					toaster.pop("error", "Code " + status, "Password couldn't be changed. Be sure to provide the correct current password.");
				}
			});
		};
		
		$scope.deleteOwnAccount = function () {
			authFactory.deleteOwnAccount($scope.deleteUser).success(function (data, status) {
				if (status === 200 || status === 204) {
					toaster.pop("success", "Account deleted!", "Your account was successfully deleted.");
					authFactory.logout();
					$location.path("/");
				} else {
					toaster.pop("error", "Code " + status, "The account could not be deleted. Be sure to provide the correct current password.");
				}
			});
		};
		
		$scope.deleteAccount = function (user) {
			authFactory.deleteAccount(user).success(function (data, status) {
				if (status === 200 || status === 204) {
					toaster.pop("success", "Account deleted!", "Successfully deleted account '" + user.username + "'.");
					$scope.getUsers();
				} else {
					toaster.pop("error", "Code " + status, "Couldn't delete account '" + user.username + "'.");
				}
			});
		};
		
		if (!$scope.loggedIn()) {
			toaster.pop("error", "Unauthorized!", "You need to be logged in to view the contents of that page.");
			$location.path("/");
		}
		
	});
	
})();