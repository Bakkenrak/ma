(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Provides methods to add or edit a platform (change) suggestion.
	 */
	d2sApp.controller('addEditPlatformCtrl', function ($scope, $rootScope, $route, $location, $timeout, $modal, platformFactory, authFactory, toaster, platform, languages) {
		$scope.isEdit = $route.current.$$route.isEdit; // copy parameters passed in from route provider to scope
		$scope.isSuggestion = $route.current.$$route.isSuggestion;
		$scope.isExternal = $route.current.$$route.isExternal;
		
		if ($scope.isEdit) { // when editing a platform or suggestion
			if (platform.status === 204) { 
				if ($scope.isSuggestion) { // if the suggestion to edit was not found
					toaster.pop("error", "Not found!", "No suggestion with this ID found.");
					$location.path("suggestions/"); // redirect to suggestions overview
				} else { // if the platform to edit was not found
					toaster.pop("error", "Not found!", "No platform with this ID found.");
					$location.path("platforms/"); // redirect to platforms overview
				}
			} else if (platform.status === 401) { // if user is unauthorized
				toaster.pop("error", "Unauthorized!", "You need to be logged in to view the information on that page.");
				$location.path("login/"); // redirect to login page
			} else if (platform.status > 400) { // other type of error
				toaster.pop("error", "Code " + platform.status, "There was an error retrieving the platform information.");
			}
			// set submit bottom text
			if ($scope.isSuggestion || $scope.isExternal) { // editing a suggestion
				$scope.submitBtnText = "Save changes to platform suggestion";
			} else { // editing an ontology platform
				$scope.submitBtnText = "Submit platform change suggestion";
			}
		} else { // creating a new suggestion
			$scope.submitBtnText = "Submit new platform suggestion"; // set submit button text
		}
		
		if (languages && languages.status >= 400) { // if languages were not resolved by the route provider
			toaster.pop("error", "Code " + languages.status, "There was an error retrieving the list of languages.");
		} else {
			$rootScope.languages = languages.data.languages; // save list of languages in the root scope for availability in all scopes in the future
		}
		
		// retrieve dimension comments and labels (only once)
		if (angular.isUndefined($rootScope.descriptions)) {
			platformFactory.getDescriptions().success(function (data, status) { // call API
				if (status >= 400) { // error
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving descriptions from the ontology.');
				}
				$rootScope.descriptions = data; // save descriptions and labels in the root scope
			});
		}
		// retrieve list of all countries (if not already existing)
		if (angular.isUndefined($rootScope.countries)) {
			platformFactory.getCountries().success(function (data, status) { // call API
				if (status >= 400) { // error
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving the list of countries.');
				}
				$rootScope.countries = data.countries; // save list of countries in root scope
			});
		}
		
		if (platform) { //if edit view, platform object is provided by the route provider
			// transform data for use in the form
			platform = platform.data;
			platform.languageObjects = []; // create language objects array 
			platform.languages.forEach(function (language) { // for each language string
				platform.languageObjects.push({ // create an object to allow model binding in ng-repeat in the form
					resourceName: language
				});
			});
			// if geo unit objects are null or undefined, instantiate empty objects
			if (!platform.launchCity) {
				platform.launchCity = {}; 
			}
			if (!platform.residenceCity) {
				platform.residenceCity = {};
			}
			if (!platform.launchCountry) {
				platform.launchCountry = {}; 
			}
			if (!platform.residenceCountry) {
				platform.residenceCountry = {}; 
			}
		}

		$scope.platform = platform || { // set resolved platform to scope if provided, otherwise instantiate new object
			resourceTypes : [ {} ],
			languageObjects : [ {} ],
			languages : [],
			launchCountryItem : {},
			trustContributions : [],
			apps : [],
			consumerisms : [],
			marketMediations : [],
			launchCity : {},
			launchCountry : {},
			residenceCity : {},
			residenceCountry : {}
		};

		/**
		 * Sets the platforms launch city object using the provided geonames data.
		 * 
		 * @param item
		 *			geonames city data item
		 */
		$scope.launchCitySelected = function (item) {
			$scope.platform.launchCity = { // (re-)set launch city object
					search: "",
					item: item,
					geonames: "http://www.geonames.org/" + item.geonameId,
					label: item.toponymName,
					adminName1: item.adminName1,
					countryCode: item.countryCode
				};
			// if no launch country is selected yet or the previously selected country does not match the country the selected city is situated in
			if (!$scope.platform.launchCountry || $scope.platform.launchCountry.countryCode !== $scope.platform.launchCity.countryCode) {
				$scope.platform.launchCountry = { // (re-)set launch country object using the information from the given geonames data
						search: "",
						geonames: "http://www.geonames.org/" + item.countryId,
						label: item.countryName,
						countryCode: item.countryCode
					};
				toaster.pop("info", "Launch country adjusted!", "Selected launch city was not situated in the previously selected country. It was adjusted accordingly.");
			}
		};
		
		/**
		 * Sets the platforms residence city object using the provided geonames data.
		 * 
		 * @param item
		 *			geonames city data item
		 */
		$scope.residenceCitySelected = function (item) {
			$scope.platform.residenceCity = {
					search: "",
					item: item,
					geonames: "http://www.geonames.org/" + item.geonameId,
					label: item.toponymName,
					adminName1: item.adminName1,
					countryCode: item.countryCode
				};
			if (!$scope.platform.residenceCountry || $scope.platform.residenceCountry.countryCode !== $scope.platform.residenceCity.countryCode) {
				$scope.platform.residenceCountry = {
						search: "",
						geonames: "http://www.geonames.org/" + item.countryId,
						label: item.countryName,
						countryCode: item.countryCode
					};
				toaster.pop("info", "Residence country adjusted!", "Selected residence city was not situated in the previously selected country. It was adjusted accordingly.");
			}
		};
		
		/**
		 * Sets the platforms launch country object using the provided geonames data.
		 * 
		 * @param item
		 *			geonames country data item
		 */
		$scope.launchCountrySelected = function (item) {
			$scope.platform.launchCountry = {
					search: "",
					item: item,
					geonames: "http://www.geonames.org/" + item.countryId,
					label: item.countryName,
					countryCode: item.countryCode
				};
			// if a launch city is selected and its country code does not match this country's country code
			if ($scope.platform.launchCity.countryCode && $scope.platform.launchCity.countryCode !== $scope.platform.launchCountry.countryCode) {
				toaster.pop("warning", "Warning!", "Selected launch city is not situated in the selected country.");
			}
		};
		
		/**
		 * Sets the platforms residence country object using the provided geonames data.
		 * 
		 * @param item
		 *			geonames country data item
		 */
		$scope.residenceCountrySelected = function (item) {
			$scope.platform.residenceCountry = {
					search: "",
					item: item,
					geonames: "http://www.geonames.org/" + item.countryId,
					label: item.countryName,
					countryCode: item.countryCode
				};
			if ($scope.platform.residenceCity.countryCode && $scope.platform.residenceCity.countryCode !== $scope.platform.residenceCountry.countryCode) {
				toaster.pop("warning", "Warning!", "Selected residence city is not situated in the selected country.");
			}
		};


		/**
		 * Calls the platform factory method to search for a city matching the entered search term.
		 * Triggered when the search term input changes.
		 */
		$scope.findLaunchCity = function () {
			if (!$scope.platform.launchCity || $scope.platform.launchCity.search === "") { // if no launch city object is set or the search string is empty
				return;
			}
			$scope.findingLaunchCity = true;
			var timeout = $timeout(function() { // set a 5 second timer to show a message when the query takes a long time
				$scope.longLaunchGeoNamesQuery = true;
			}, 5000);
			// return the list of geoname data items representing cities that match the search term
			return platformFactory.findCity($scope.platform.launchCity.search, $scope.platform.launchCountry ? $scope.platform.launchCountry.countryCode : null)
					.then(function (response) {
						if (response.status !== 200) { // error
							toaster.pop('error', 'Code ' + response.status, 'There was an error connecting to the GeoNames database.');
						} else if (response.data.totalResultsCount === 0) { // no matches
							var message = 'Couldn\'t find a city \'' + $scope.platform.launchCity.search + '\'';
							message += ($scope.platform.launchCountry && $scope.platform.launchCountry.label) ?  'in ' + $scope.platform.launchCountry.label + '.' : '.';
							toaster.pop('warning', 'City not found!', message);
						}
						$scope.findingLaunchCity = false;
						$timeout.cancel(timeout); // stop timeout to display message for slow queries
						$scope.longLaunchGeoNamesQuery = false;
						return response.data.geonames;
					});
		};
		
		/**
		 * Calls the platform factory method to search for a city matching the entered search term.
		 * Triggered when the search term input changes.
		 */
		$scope.findResidenceCity = function () {
			if (!$scope.platform.residenceCity || $scope.platform.residenceCity.search === "") {
				return;
			}
			$scope.findingResidenceCity = true;
			var timeout = $timeout(function() { // set a 5 second timer to show a message when the query takes a long time
				$scope.longResidenceGeoNamesQuery = true;
			}, 5000);
			return platformFactory.findCity($scope.platform.residenceCity.search, $scope.platform.residenceCountry ? $scope.platform.residenceCountry.countryCode : null)
					.then(function (response) {
						if (response.status !== 200) {
							toaster.pop('error', 'Code ' + response.status, 'There was an error connecting to the GeoNames database.');
						} else if (response.data.totalResultsCount === 0) {
							var message = 'Couldn\'t find a city \'' + $scope.platform.residenceCity.search + '\'';
							message += ($scope.platform.residenceCountry && $scope.platform.residenceCountry.label) ?  'in ' + $scope.platform.residenceCountry.label + '.' : '.';
							toaster.pop('warning', 'City not found!', message);
						}
						$scope.findingResidenceCity = false;
						$timeout.cancel(timeout); // stop timeout to display message for slow queries
						$scope.longResidenceGeoNamesQuery = false;
						return response.data.geonames;
					});
		};


		/**
		 * @return An array of integers representing each year from the current back to 1990
		 */
		$scope.getYears = function () {
			var currentYear = new Date().getFullYear(); // get current year
			var output = [];
			for (var i = currentYear; i > 1989; i--) {
				output.push(i);
			}
			return output;
		};

		// dimension values those dimensions edited using checkboxes
		$scope.marketMediations = [ "Not-for-profit", "Profit from peer consumers", "Profit from peer providers", "Profit from both", "Indirect profit",
				"Profit from advertisement", "Profit from user data", "Per transaction", "Per listing", "Membership fee" ];
		$scope.consumerisms = [ "None", "Social", "Environmental", "Economic" ];
		$scope.smartphoneApps = [ "Android app", "iOS app",	"Windows Phone app" ];
		$scope.trustContributions = [ "Provider ratings", "Provider and consumer ratings", "Referral", "Vouching", "Value-added services" ];
		
		/**
		 * Adds or removes the given current option from the array of selected options depending on whether it was already in that array.
		 * 
		 * @param current
		 *			The currently clicked option to add to/remove from the array of selected options
		 * @param selected
		 *			The array of selected options
		 */
		$scope.toggleSelection = function (current, selected) {
			var idx = selected.indexOf(current); // get the index of the current option in the array
			if (~idx) { // index found -> option is already selected
				selected.splice(idx, 1); // remove selection
				// if nothing else is selected anymore and current isn't None, select None
				if (selected.length === 0 && current !== "None") {
					selected.push("None"); 
				}
			} else { // is newly selected
				if (current === "None" || current === "Not-for-profit") { // if current is None or Not-for-profit
					selected.length = 0; // empty array first
				} else {
					idx = selected.indexOf("None"); // check if None was previously selected
					if (~idx) {
						selected.splice(idx, 1); // remove None
					}
					idx = selected.indexOf("Not-for-profit"); // check if Not-for-profit was previously selected
					if (~idx) {
						selected.splice(idx, 1); // remove Not-for-profit
					}
				}
				selected.push(current); // add current selection to array
			}
		};
		
		/**
		 * Add an empty object to the given array, e.g. to display a new element in an ng-repeat.
		 * 
		 * @param array
		 *			The array to add an empty object to
		 */
		$scope.addOption = function (array) {
			array.push({});
		};

		/**
		 * Submits the platform object to the server using factory methods to persist the new data or changes.
		 */
		$scope.submit = function () {
			$scope.submitting = true;
			
			// prepare platform object for submission
			$scope.platform.resourceTypes = $scope.platform.resourceTypes
					.filter(function (i) { // filter out all resource types that have no label
						return !angular.isUndefined(i.label);
					});
			
			$scope.platform.languages = $scope.platform.languageObjects
					.filter(function (i) { // filter out all languages that have no name
						return i.resourceName;
					}).map(function (i) { // transform the array of objects into an array of plain strings
						return i.resourceName;
					});
			
			
			if (!$scope.isEdit) { // adding a new suggestion
				if ($scope.directSave) { // admin has selected direct addition to the ontology
					platformFactory.directSavePlatformSuggestion($scope.platform).success(function (data, status) {
						if (status === 200 || status === 204) { // success
							toaster.pop('success', 'Platform added!', 'The new platform was successfully added to the ontology.');
							$location.path("platforms/"); // redirect to platforms overview
						} else if (status >= 400) { // error
							toaster.pop('error', 'Code ' + status, 'There was an error saving this suggestion.');
						}
						$scope.submitting = false;
					});
				} else { // no direct addition to ontology -> save suggestion for review
					platformFactory.addPlatformSuggestion($scope.platform).success(function (data, status) {
						if (status === 200 || status === 204) { // success
							if ($scope.loggedIn()) { // if the user is logged in, simply notify
								toaster.pop('success', 'Platform suggestion added!', 'The new platform suggestion was successfully added for review by a moderator.');
							} else { // if the user is not logged in
								var modalInstance = $modal.open({ // open a modal to display the new suggestion's external link for further editing
								    animation: true,
								    templateUrl: 'editExternalModal.html',
								    controller: 'ModalInstanceCtrl',
								    resolve: {
								        externalId: function () {
											return data.success; // pass external id to the modal for display
								        }
								    }
							    });
							}
							$location.path("platforms/"); // redirect to platforms overview
						} else if (status >= 400) { // error
							toaster.pop('error', 'Code ' + status, 'There was an error adding this suggestion: ' + data.error);
						}
						$scope.submitting = false;
					});
				}
			} else { // editing
				if (!$scope.isSuggestion) { // editing an ontology platform
					$scope.platform.editFor = $route.current.params.platform; // retrieve the URI of the platform that is edited from route
					
					if ($scope.directSave) { // admin has selected direct application of changes to the ontology
						platformFactory.directSavePlatformSuggestion($scope.platform).success(function (data, status) {
							if (status === 200 || status === 204) { // success
								toaster.pop('success', 'Change persisted!', 'The platform change was successfully applied to the ontology.');
								$location.path("platforms/" + $scope.platform.editFor); // redirect to platform's detail view
							} else if (status >= 400) { // error
								toaster.pop('error', 'Code ' + status, 'There was an error adding this change suggestion.');
							}
							$scope.submitting = false;
						});
					} else { // no direct application to ontology -> save suggestion for review
						platformFactory.addPlatformSuggestion($scope.platform).success(function (data, status) {
							if (status === 200 || status === 204) { // success
								if ($scope.loggedIn()) { // if user is logged in, simply notify
									toaster.pop('success', 'Change suggestion added!', 'The platform change suggestion was successfully added for review by a moderator.');
								} else { // if user is not logged in
									var modalInstance = $modal.open({ // open modal to display external ID
									    animation: true,
									    templateUrl: 'editExternalModal.html',
									    controller: 'ModalInstanceCtrl',
									    resolve: {
									        externalId: function () {
												return data.success;
											}
									    }
								    });
								}
								$location.path("platforms/"); // redirect to platforms overview
							} else if (status >= 400) { // error
								toaster.pop('error', 'Code ' + status, 'There was an error adding this change suggestion: ' + data.error);
							}
							$scope.submitting = false;
						});
					}
				} else { //editing a suggestion
					if ($scope.isExternal) { // external editing a suggestion
						platformFactory.editPlatformSuggestionExternal($route.current.params.id, $scope.platform).success(function (data, status) {
							if (status === 200 || status === 204) { // success
								toaster.pop('success', 'Platform suggestion edited!', 'The platform suggestion was successfully edited.');
								$location.path("/"); // redirect to home page
							} else if (status >= 400) { // error
								toaster.pop('error', 'Code ' + status, 'There was an error editing this suggestion.');
							}
							$scope.submitting = false;
						});
					} else { // mod or admin editing a suggestion
						platformFactory.editPlatformSuggestion($scope.platform).success(function (data, status) {
							if (status === 200 || status === 204) { // success
								toaster.pop('success', 'Platform suggestion edited!', 'The platform suggestion was successfully edited.');
								$location.path("suggestions/" + $route.current.params.id); // redirect to suggestion's detail page
							} else if (status >= 400) { // error
								toaster.pop('error', 'Code ' + status, 'There was an error editing this suggestion.');
							}
							$scope.submitting = false;
						});
					}
				}
			}
		};
		
		/**
		 * @return Boolean whether an auth data cookie currently exists
		 */
		$scope.loggedIn = function () {
			return authFactory.isAuthenticated();
		};
	});
	
	/**
	 * The controller handling the modal to display a suggestion's external ID for user's that are not logged in.
	 */
	d2sApp.controller('ModalInstanceCtrl', function ($scope, $modalInstance, externalId) {
		$scope.internalUrl = "external/" + externalId; // construct relative link path to open this suggestion's edit form from
		
		/**
		 * Closes the modal.
		 */
		$scope.ok = function () {
		    $modalInstance.close();
		};
	});
	
})();