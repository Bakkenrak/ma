(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('indexCtrl', function ($scope, authFactory) {
		$scope.loggedIn = function () {
			if (authFactory.isAuthenticated()) {
				return authFactory.getAuthData().username;
			} else {
				return false;
			}
		};
	});
	
})();