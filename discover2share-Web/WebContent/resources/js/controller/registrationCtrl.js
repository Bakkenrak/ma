(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('registrationCtrl', function ($scope, $location, authFactory) {
		$scope.isAdmin = (authFactory.isAuthenticated() && authFactory.getAuthData().authRole === "admin");
		$scope.user = {};
		$scope.registerFailed = false;
		$scope.registerSuccess = false;

		$scope.register = function () {
			if ($scope.user.password === $scope.user.passwordConfirm) {
				delete $scope.user.passwordConfirm;
				authFactory.register($scope.user)
					.success(function (data, status) {
						$scope.registerFailed = (status !== 200 && status !== 204);
						$scope.registerSuccess = (status === 200 || status === 204);
						$scope.user = {};
						if ($scope.registerFailed) {
							$scope.errorMessage = data.error;
						}
					}).error(function () {
						$scope.registerFailed = true;
						$scope.registerSuccess = false;
					});
			}
		};
	});
	
})();