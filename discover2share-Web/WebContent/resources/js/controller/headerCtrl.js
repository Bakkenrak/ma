(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('headerCtrl', function ($scope, $rootScope,	$location, $cookieStore, authFactory, toaster) {
		// listener for route change
		$rootScope.$on('$locationChangeSuccess', function (event) {
			$scope.currentTab = $location.path().substr(1); // set scope variable to current route
			$scope.loggedIn = authFactory.isAuthenticated(); // determine if logged in from cookie
			if ($scope.loggedIn) {
				$scope.authRole = authFactory.getAuthData().authRole;
				$scope.username = authFactory.getAuthData().username;
			} else {
				$scope.authRole = undefined;
				$scope.username = undefined;
			}
		});

		$scope.logout = function () {
			authFactory.logout();
			toaster.pop('success', 'Logged out!', 'You were logged out successfully.');
		};
	});
	
})();