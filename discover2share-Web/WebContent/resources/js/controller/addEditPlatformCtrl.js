(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('addEditPlatformCtrl', function ($scope, $rootScope, $route, $location, $timeout, platformFactory, authFactory, toaster, platform, languages) {
		$scope.isEdit = $route.current.$$route.isEdit;
		$scope.isSuggestion = $route.current.$$route.isSuggestion;
		if ($scope.isEdit) {
			if (platform.status === 204) {
				if ($scope.isSuggestion) {
					toaster.pop("error", "Not found!", "No suggestion with this ID found.");
					$location.path("suggestions/");
				} else {
					toaster.pop("error", "Not found!", "No platform with this ID found.");
					$location.path("platforms/");
				}
			} else if (platform.status === 401) {
				toaster.pop("error", "Unauthorized!", "You need to be logged in to view the information on that page.");
				$location.path("login/");
			} else if (platform.status > 400) {
				toaster.pop("error", "Code " + platform.status, "There was an error retrieving the platform information.");
			}

			if ($scope.isSuggestion) {
				$scope.submitBtnText = "Save changes to platform suggestion";
			} else {
				$scope.submitBtnText = "Submit platform change suggestion";
			}
		} else {
			$scope.submitBtnText = "Submit new platform suggestion";
		}
		
		if (languages && languages.status >= 400) {
			toaster.pop("error", "Code " + languages.status, "There was an error retrieving the list of languages.");
		} else {
			$rootScope.languages = languages.data.languages;
		}
		// retrieve dimension comments and labels (only once)
		if (angular.isUndefined($rootScope.descriptions)) {
			platformFactory.getDescriptions().success(function (data, status) {
				if (status >= 400) {
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving descriptions from the ontology.');
				}
				$rootScope.descriptions = data;
			});
		}
		if (angular.isUndefined($rootScope.countries)) {
			platformFactory.getCountries().success(function (data, status) {
				if (status >= 400) {
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving the list of countries.');
				}
				$rootScope.countries = data.countries;
			});
		}
		
		if (platform) { //if edit view, transform data for use in the form
			platform = platform.data;
			platform.yearLaunch = " " + platform.yearLaunch;
			platform.languageObjects = [];
			platform.languages.forEach(function (language) {
				platform.languageObjects.push({
					resourceName: " " + language // add a space that can be later removed to deal with a weird model binding bug
				});
			});
			$timeout(function () { // after the page is loaded
				$scope.platform.languageObjects.forEach(function (item) { // from each language object
					item.resourceName = item.resourceName.substr(1); // remove previously added space to produce a change Angular registers
				});
				$scope.platform.yearLaunch = $scope.platform.yearLaunch.substr(1);
				$scope.$apply(); // apply the change - only now will the language select boxes be set correctly...
			});
		}

		$scope.platform = platform || {
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

		$scope.launchCitySelected = function (item) {
			$scope.platform.launchCity = {
					search: "",
					item: item,
					geonames: "http://www.geonames.org/" + item.geonameId,
					label: item.toponymName,
					adminName1: item.adminName1,
					countryCode: item.countryCode
				};
			if (!$scope.platform.launchCountry || $scope.platform.launchCountry.countryCode !== $scope.platform.launchCity.countryCode) {
				$scope.platform.launchCountry = {
						search: "",
						geonames: "http://www.geonames.org/" + item.countryId,
						label: item.countryName,
						countryCode: item.countryCode
					};
				toaster.pop("info", "Launch country adjusted!", "Selected launch city was not situated in the previously selected country. It was adjusted accordingly.");
			}
		};
		
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
		
		$scope.launchCountrySelected = function (item) {
			$scope.platform.launchCountry = {
					search: "",
					item: item,
					geonames: "http://www.geonames.org/" + item.countryId,
					label: item.countryName,
					countryCode: item.countryCode
				};
			if (!angular.isUndefined($scope.platform.launchCity.item) && 
					$scope.platform.launchCity.item.countryCode !== $scope.platform.launchCountry.item.countryCode) {
				toaster.pop("warning", "Warning!", "Selected launch city is not situated in the selected country.");
			}
		};
		
		$scope.residenceCountrySelected = function (item) {
			$scope.platform.residenceCountry = {
					search: "",
					item: item,
					geonames: "http://www.geonames.org/" + item.countryId,
					label: item.countryName,
					countryCode: item.countryCode
				};
			if (!angular.isUndefined($scope.platform.residenceCity.item) && 
					$scope.platform.residenceCity.item.countryCode !== $scope.platform.residenceCountry.item.countryCode) {
				toaster.pop("warning", "Warning!", "Selected residence city is not situated in the selected country.");
			}
		};

		// triggered when the search term input changes
		$scope.findLaunchCity = function () {
			if (!$scope.platform.launchCity || $scope.platform.launchCity.search === "") {
				return;
			}
			return platformFactory.findCity($scope.platform.launchCity.search, $scope.platform.launchCountry ? $scope.platform.launchCountry.countryCode : null)
					.then(function (response) {
						if (response.status !== 200) {
							toaster.pop('error', 'Code ' + response.status, 'There was an error connecting to the GeoNames database.');
						} else if (response.data.totalResultsCount === 0) {
							var message = 'Couldn\'t find a city \'' + $scope.platform.launchCity.search + '\'';
							message += ($scope.platform.launchCountry && $scope.platform.launchCountry.label) ?  'in ' + $scope.platform.launchCountry.label + '.' : '.';
							toaster.pop('warning', 'City not found!', message);
						}
						return response.data.geonames;
					});
		};
		
		$scope.findResidenceCity = function () {
			if (!$scope.platform.residenceCity || $scope.platform.residenceCity.search === "") {
				return;
			}
			return platformFactory.findCity($scope.platform.residenceCity.search, $scope.platform.residenceCountry ? $scope.platform.residenceCountry.countryCode : null)
					.then(function (response) {
						if (response.status !== 200) {
							toaster.pop('error', 'Code ' + response.status, 'There was an error connecting to the GeoNames database.');
						} else if (response.data.totalResultsCount === 0) {
							var message = 'Couldn\'t find a city \'' + $scope.platform.residenceCity.search + '\'';
							message += ($scope.platform.residenceCountry && $scope.platform.residenceCountry.label) ?  'in ' + $scope.platform.residenceCountry.label + '.' : '.';
							toaster.pop('warning', 'City not found!', message);
						}
						return response.data.geonames;
					});
		};

		$scope.getYears = function () {
			var currentYear = new Date().getFullYear();
			var output = [];
			for (var i = currentYear; i > 1989; i--) {
				output.push(i);
			}
			return output;
		};

		$scope.marketMediations = [	"Profit from peer consumers", "Profit from peer providers",	"Profit from both", "Indirect profit",
				"Profit from advertisement", "Profit from user data", "Per transaction", "Per listing", "Membership fee" ];
		$scope.consumerisms = [ "None", "Social", "Environmental", "Economic" ];
		$scope.smartphoneApps = [ "Android app", "iOS app",	"Windows Phone app" ];
		$scope.trustContributions = [ "Provider ratings", "Provider and consumer ratings", "Referral", "Vouching", "Value-added services" ];
		
		$scope.toggleSelection = function (current, selected) {
			var idx = selected.indexOf(current);
			if (~idx) { // is currently selected
				selected.splice(idx, 1); // remove selection
				// if nothing else is selected anymore and current isn't None, select None
				if (selected.length === 0 && current !== "None") { 
					selected.push("None"); 
				}
			} else { // is newly selected
				if (current === "None") { // if current is None
					selected.length = 0; //empty array first
				} else {
					idx = selected.indexOf("None"); //check if None was previously selected
					if (~idx) {
						selected.splice(idx, 1); //remove None
					}
				}
				selected.push(current); //add current selection to array
			}
		};
		
		$scope.addOption = function (array) {
			array.push({});
		};

		$scope.submit = function () {
			$scope.platform.resourceTypes = $scope.platform.resourceTypes
					.filter(function (i) {
						return !angular.isUndefined(i.label);
					});
			
			$scope.platform.languages = $scope.platform.languageObjects
					.filter(function (i) {
						return !angular.isUndefined(i.resourceName);
					}).map(function (i) {
						return i.resourceName;
					});
			
			
			if (!$scope.isEdit) { // adding a new suggestion
				if ($scope.directSave) { // admin has selected direct addition to the ontology
					platformFactory.directSavePlatformSuggestion($scope.platform).success(function (data, status) {
						if (status === 200 || status === 204) {
							toaster.pop('success', 'Platform added!', 'The new platform was successfully added to the ontology.');
						}
						if (status >= 400) {
							toaster.pop('error', 'Code ' + status, 'There was an error saving this suggestion.');
						}
					});
				} else { // no direct addition to ontology -> save suggestion for review
					platformFactory.addPlatformSuggestion($scope.platform).success(function (data, status) {
						if (status === 200 || status === 204) {
							toaster.pop('success', 'Platform suggestion added!', 'The new platform suggestion was successfully added for review by a moderator.');
						}
						if (status >= 400) {
							toaster.pop('error', 'Code ' + status, 'There was an error adding this suggestion.');
						}
					});
				}
			} else { // editing
				if (!$scope.isSuggestion) { // editing an ontology platform
					$scope.platform.editFor = $route.current.params.platform;
					
					if ($scope.directSave) { // admin has selected direct application of changes to the ontology
						platformFactory.directSavePlatformSuggestion($scope.platform).success(function (data, status) {
							if (status === 200 || status === 204) {
								toaster.pop('success', 'Change persisted!', 'The platform change was successfully applied to the ontology.');
								$location.path("platforms/" + $scope.platform.editFor);
							}
							if (status >= 400) {
								toaster.pop('error', 'Code ' + status, 'There was an error adding this change suggestion.');
							}
						});
					} else { // no direct application to ontology -> save suggestion for review
						platformFactory.addPlatformSuggestion($scope.platform).success(function (data, status) {
							if (status === 200 || status === 204) {
								toaster.pop('success', 'Change suggestion added!', 'The platform change suggestion was successfully added for review by a moderator.');
								$location.path("platforms/" + $route.current.params.platform);
							}
							if (status >= 400) {
								toaster.pop('error', 'Code ' + status, 'There was an error adding this change suggestion.');
							}
						});
					}
				} else { //editing a suggestion
					platformFactory.editPlatformSuggestion($scope.platform).success(function (data, status) {
						if (status === 200 || status === 204) {
							toaster.pop('success', 'Platform suggestion edited!', 'The platform suggestion was successfully edited.');
						}
						if (status >= 400) {
							toaster.pop('error', 'Code ' + status, 'There was an error editing this suggestion.');
						}
					});
				}
			}
		};
		
		$scope.loggedIn = function () {
			return authFactory.isAuthenticated();
		};
	});
	
})();