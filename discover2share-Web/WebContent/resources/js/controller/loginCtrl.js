(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('loginCtrl', function ($scope, $location, authFactory, toaster) {
		$scope.loggedOut = false;

		if ($location.path().substr(1) === "logout") {
			authFactory.logout();
			$scope.loggedOut = true;
			toaster.pop('info', 'Logged out!', 'You were logged out successfully.');
		}

		$scope.user = {
			username : "",
			password : ""
		};

		$scope.loginFailed = false;

		$scope.login = function () {
			authFactory.login($scope.user)
				.success(function (data, status) {
					if (status !== 200) {
						$scope.loginFailed = true;
					} else {
						authFactory.setAuthData(data);
						$scope.loginFailed = false;
						$location.path("/");
						toaster.pop('success', 'Logged in!', 'Welcome back, ' + $scope.user.username + '.');
					}
				}).error(function () {
					$scope.loginFailed = true;
					$scope.loggedOut = false;
				});
		};
	});
	
})();