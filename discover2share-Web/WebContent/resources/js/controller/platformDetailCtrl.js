(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Controller that provides functionality for the platform or suggestion detail views.
	 */
	d2sApp.controller('platformDetailCtrl', function ($scope, $route, platformFactory, authFactory, $rootScope, $location, toaster, platform, languages) {
		if ($route.current.params.platform) {
			$scope.platformUri = $route.current.params.platform;
		}
		$scope.isSuggestion = $route.current.$$route.isSuggestion; // copy parameter passed in from route provider to scope 
				
		if (platform.status === 204) { // if the platform data was not resolved by the route provider
			if ($scope.isSuggestion) { // suggestion not found
				toaster.pop("error", "Error!", "No suggestion found with ID " + $route.current.params.id + ".");
				$location.path("suggestions/"); // redirect to suggestions overview
			} else { // ontology platform not found
				toaster.pop("error", "Error!", "No platform found with URI " + $route.current.params.platform + ".");
				$location.path("platforms/"); // redirect to platforms overview
			}
		} else if (platform.status === 401) { // user is not authorized
			toaster.pop("error", "Unauthorized!", "You need to be logged in to access the information on that page.");
			$location.path("login/"); // redirect to login page
		}
		
		// retrieve dimension comments and labels (if not yet in root scope)
		if (!$rootScope.descriptions) {
			platformFactory.getDescriptions().success(function (data, status) {
				if (status >= 400) { // error
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving descriptions from the ontology.');
				} else { // success
					$rootScope.descriptions = data; // save descriptions in root scope to avoid another retrieval in the future 
				}
			});
		}
		
		$scope.platform = platform.data; // save platform data to scope for display in the view
		
		if (languages) { // if array of all languages was resolved by the route provider
			languages = languages.data.languages;
				
			$scope.platform.languageLabels = []; // array of language labels for display in the view
			$scope.platform.languages.forEach(function (language) { // for each language of the platform
				var match = false;
				var resourceName = language.replace("http://www.discover2share.net/d2s-ont/", ""); // remove URI base
				for (var i = 0; i < languages.length; i++) { // iterate through all existing languages
					if (languages[i].resourceName === resourceName) { // if the resource names match
						$scope.platform.languageLabels.push(languages[i].name); // get the language's label and add it
						match = true;
						break;
					}
				}
				if (!match) { // when no resource name match was found 
					$scope.platform.languageLabels.push(language); // simply add the resource name
				}
			});
		}

		if ($scope.platform.yearLaunch) { // if the platform has a year of launch
			// remove its URI base for nicer display in view
			$scope.platform.yearLaunchName = $scope.platform.yearLaunch.replace("http://www.discover2share.net/d2s-ont/", "");
		}
		
		if ($scope.isSuggestion && $scope.platform.editFor) { // if viewing a change suggestion for an ontology platform
			platformFactory.getPlatform($scope.platform.editFor).success(function (data, status) { // retrieve that platform's data
				if (status === 200) { // success
					$scope.originalPlatform = data; // save data in scope
					if ($scope.originalPlatform.yearLaunch) { // improve year of launch's display
						$scope.originalPlatform.yearLaunchName = $scope.originalPlatform.yearLaunch.replace("http://www.discover2share.net/d2s-ont/", "");
					}
				} else {
					toaster.pop("error", "Code " + status, "There was an error retrieving the original platform data from the ontology. The actual changes of this suggestion can thus not be highlighted.");
				}
			});
		}

		/**
		 * @param owlTime
		 *			URI representing a time unit concept from the OWL Time ontology
		 * @return A more readable representation of the respective time unit
		 */
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
		
		/**
		 * @return Boolean whether auth data currently exists for the user
		 */
		$scope.loggedIn = function () {
			return authFactory.isAuthenticated();
		};
		
		/**
		 * Compares the given values to determine whether a change between the suggestion and the original
		 * has occurred.
		 * 
		 * @param suggestion
		 *			The current suggestion's value
		 * @param original
		 *			The original platform's value
		 * @param attribute
		 *			The attribute to compare by if the former parameters are objects
		 * @return True, if change occurred (values are equal), otherwise false
		 */
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
		
		/**
		 * Redirects to the edit view of the current suggestion
		 */
		$scope.editSuggestion = function () {
			$location.path("suggestions/edit/" + $route.current.params.id);
		};
		
		/**
		 * Redirects to the the edit form of the current platform
		 */
		$scope.editPlatform = function () {
			$location.path("platforms/edit/" + $route.current.params.platform);
		};
		
		/**
		 * Calls the platform factory's method to save the current suggestion in the ontology.
		 */
		$scope.saveSuggestion = function () {
			$scope.saving = true;
			platformFactory.savePlatformSuggestion($scope.platform.id).success(function (data, status) {
				if (status === 200 || status === 204) { // success
					$location.path("suggestions/"); // redirect to suggestions overview
					toaster.pop('success', 'Suggestion added!', 'The suggestion for platform ' + $scope.platform.label + ' was successfully added to the ontology.');
				} else {
					toaster.pop('error', 'Code ' + status, 'Sorry, there was an error adding the suggestion for platform ' + $scope.platform.label + ' to the ontology.');
				}
				$scope.saving = false;
			});
		};
		
		/**
		 * Calls the platform factory's method to remove the current suggestion.
		 */
		$scope.removeSuggestion = function () {
			$scope.removing = true;
			platformFactory.removeSuggestion($scope.platform.id).success(function (data, status) {
				if (status === 200 || status === 204) { // success
					$location.path("suggestions/"); // redirect to the suggestions overview
					toaster.pop('success', 'Suggestion removed!', 'The suggestion for platform ' + $scope.platform.label + ' was successfully removed.');
				} else { // error
					toaster.pop('error', 'Code ' + status, 'Sorry, there was an error removing the suggestion for platform ' + $scope.platform.label + '.');
				}
				$scope.removing = false;
			});
		};
		
		/**
		 * Calls the platform factory method to remove the current platform from the ontology.
		 */
		$scope.removePlatform = function () {
			$scope.removing = true;
			platformFactory.removePlatform($route.current.params.platform).success(function (data, status) {
				if (status === 200 || status === 204) {
					$location.path("platforms/");
					toaster.pop('success', 'Platform removed!', 'The platform ' + $scope.platform.label + ' was successfully removed from the ontology.');
				} else {
					toaster.pop('error', 'Code ' + status, 'Sorry, there was an error removing the platform ' + $scope.platform.label + ' from the ontology.');
				}
				$scope.removing = false;
			});
		};
	});
	
})();