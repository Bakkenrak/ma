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
		if ($scope.platform.yearLaunch) {
			$scope.platform.yearLaunchName = $scope.platform.yearLaunch.replace("http://www.discover2share.net/d2s-ont/", "");
		}
		
		if ($scope.isSuggestion && $scope.platform.editFor) {
			platformFactory.getPlatform($scope.platform.editFor).success(function (data, status) {
				if (status === 200) {
					$scope.originalPlatform = data;
					// retrieve launchYear label
					if ($scope.originalPlatform.yearLaunch) {
						$scope.originalPlatform.yearLaunchName = $scope.originalPlatform.yearLaunch.replace("http://www.discover2share.net/d2s-ont/", "");
					}
				} else {
					toaster.pop("error", "Code " + status, "There was an error retrieving the original platform data from the ontology. The actual changes of this suggestion can thus not be highlighted.");
				}
			});
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
		
		$scope.checkChange = function (suggestion, original, attribute) {
			if (!$scope.isSuggestion || !$scope.platform.editFor) {
				return false;
			}
			if (angular.equals(suggestion, original)) { // completely equal
				return false; // no change
			} else if (angular.isArray(suggestion) && angular.isArray(original)) { // both are arrays
				if (suggestion.length === original.length) { // of the same length
					if (suggestion.length > 0) { // array holds more than one item
						if (attribute && angular.isObject(suggestion[0])) { // an attribute to compare by is provided and elements in array are objects
							var originalCopy = angular.copy(original);
							var matchedAll = true;
							suggestion.forEach(function (suggestionItem) { // for each suggestion item
								var match = false;
								for (var i = 0; i < originalCopy.length; i++) { // compare with each original item
									if (suggestionItem[attribute] === originalCopy[i][attribute]) { // if the property to compare matches on both
										match = true;
										originalCopy.splice(i, 1); // remove from the copy, so several suggestions items can't match against one original
										break; // break the loop
									}
								}
								if (!match) { // if no match found for this suggestion item
									matchedAll = false;
								}
							});
							return !matchedAll;
						} else { // plain types in array
							var originalCopy = angular.copy(original);
							var matchedAll = true;
							suggestion.forEach(function (suggestionItem) { // for each suggestion item
								var match = false;
								for (var i = 0; i < originalCopy.length; i++) { // compare with each original item
									if (suggestionItem === originalCopy[i]) { // if the property to compare matches on both
										match = true;
										originalCopy.splice(i, 1); // remove from the copy, so several suggestions items can't match against one original
										break; // break the loop
									}
								}
								if (!match) { // if no match found for this suggestion item
									matchedAll = false;
								}
							});
							return !matchedAll;
						}
					}
					return false; // no change - both empty arrays
				}
				return true; // change - arrays of different length
			} else if (attribute && angular.isObject(suggestion) && angular.isObject(original)) {
				if (suggestion[attribute] === original[attribute]) {
					return false; // no change - objects with an equal attribute
				}
				return true; // change - objects without an equal attribute
			}
			return true; // change - not completely equal, not both arrays and not both objects
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