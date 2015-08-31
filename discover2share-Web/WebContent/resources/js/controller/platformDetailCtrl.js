(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('platformDetailCtrl', function ($scope, $route, platformFactory, authFactory, $rootScope, $location, toaster, platform) {
		$scope.isSuggestion = $route.current.$$route.isSuggestion;
				
		if (platform.status === 204) {
			if ($scope.isSuggestion) {
				toaster.pop("error", "Error!", "No suggestion found with ID " + $route.current.params.id + ".");
				$location.path("suggestions/");
			} else {
				toaster.pop("error", "Error!", "No platform found with URI " + $route.current.params.platform + ".");
				$location.path("platforms/");
			}
		} else if (platform.status === 401) {
			toaster.pop("error", "Unauthorized!", "You need to be logged in to access the information on that page.");
			$location.path("login/");
		}
		
		$scope.platform = platform.data;

		// retrieve launchYear label
		if (!angular.isUndefined($scope.platform.yearLaunch)) {
			$scope.platform.yearLaunchName = $scope.platform.yearLaunch.replace("http://www.discover2share.net/d2s-ont/", "");
		}

		$scope.timeConverter = function (owlTime) {
			switch (owlTime) {
			case "http://www.w3.org/2006/time#unitMinute":
				return "Minutes";
			case "http://www.w3.org/2006/time#unitHour":
				return "Hours";
			case "http://www.w3.org/2006/time#unitDay":
				return "Days";
			case "http://www.w3.org/2006/time#unitWeek":
				return "Weeks";
			case "http://www.w3.org/2006/time#unitMonth":
				return "Months";
			}
			return owlTime;
		};

		// retrieve dimension comments and labels (only once)
		if (angular.isUndefined($rootScope.descriptions)) {
			platformFactory.getDescriptions().success(function (data, status) {
				if (status >= 400) {
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving descriptions from the ontology.');
				}
				$rootScope.descriptions = data;
			});
		}
		
		$scope.loggedIn = function () {
			return authFactory.isAuthenticated();
		};
		
		$scope.removeSuggestion = function () {
			platformFactory.removeSuggestion($scope.platform.id).success(function (data, status) {
				if (status === 200 || status === 204) {
					$location.path("suggestions/");
					toaster.pop('success', 'Suggestion removed!', 'The suggestion for platform ' + $scope.platform.label + ' was successfully removed.');
				} else {
					toaster.pop('error', 'Code ' + status, 'Sorry, there was an error removing the suggestion for platform ' + $scope.platform.label + '.');
				}
			});
		};
		
		$scope.saveSuggestion = function () {
			platformFactory.savePlatformSuggestion($scope.platform.id).success(function (data, status) {
				if (status === 200 || status === 204) {
					$location.path("suggestions/");
					toaster.pop('success', 'Suggestion added!', 'The suggestion for platform ' + $scope.platform.label + ' was successfully added to the ontology.');
				} else {
					toaster.pop('error', 'Code ' + status, 'Sorry, there was an error adding the suggestion for platform ' + $scope.platform.label + ' to the ontology.');
				}
			});
		};
		
		$scope.editSuggestion = function () {
			$location.path("suggestions/edit/" + $route.current.params.id);
		};
		
		$scope.editPlatform = function () {
			$location.path("platforms/edit/" + $route.current.params.platform);
		};
		
		$scope.removePlatform = function () {
			platformFactory.removePlatform($route.current.params.platform).success(function (data, status) {
				if (status === 200 || status === 204) {
					$location.path("platforms/");
					toaster.pop('success', 'Platform removed!', 'The platform ' + $scope.platform.label + ' was successfully removed from the ontology.');
				} else {
					toaster.pop('error', 'Code ' + status, 'Sorry, there was an error removing the platform ' + $scope.platform.label + ' from the ontology.');
				}
			});
		};
	});
	
})();