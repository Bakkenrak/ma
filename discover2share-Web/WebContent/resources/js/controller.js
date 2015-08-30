(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('headerController', function ($scope, $rootScope,	$location, $cookieStore, authFactory, toaster) {
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

	d2sApp.controller('indexCtrl', function ($scope, $http) {
		$http.get('api/ind/vip')
			.success(function (data, status, headers, config) {
				$scope.vipMessage = data + " " + status;
			}).error(function (data, status, headers, config) {
				$scope.vipMessage = data + " " + status;
			});

		var x = [ 52, 62, 98, 217, 227, 273, 313, 351, 355, 356, 358, 368, 376,
				377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 389, 390,
				397, 398, 399, 402, 404, 406, 407, 408, 411, 412, 413, 414,
				415, 416, 417, 418, 419, 421 ];
		x.forEach(function (i) {
			// $http.get('api/platforms/platform_' + i);
		});
	});

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
						$scope.registerFailed = (status !== 200);
						$scope.registerSuccess = (status === 200);
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

	d2sApp.controller('platformsCtrl', function ($scope, $location, toaster, platforms) {
		if (platforms.status === 401) {
			toaster.pop("error", "Unauthorized!", "You need to be logged in to access the information on that page.");
			$location.path("login/");
		}
		
		$scope.pagination = {
			itemsPerPage : 25
		};
		$scope.searchTerm = "";

		// triggered when the current page of pagination, the
		// items per page or the filtered platform array change
		$scope.$watch("pagination.currentPage + pagination.itemsPerPage + filteredPlatforms", function () {
			$scope.pagination.begin = (($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage);
			$scope.pagination.end = $scope.pagination.begin	+ $scope.pagination.itemsPerPage;
			// adjust end if it exceeds the actual numer of platforms
			if ($scope.pagination.end > $scope.filteredPlatforms.length) {
				$scope.pagination.end = $scope.filteredPlatforms.length;
			}
			// slice the platforms for display according to currently selected page
			$scope.paginatedPlatforms = $scope.filteredPlatforms.slice($scope.pagination.begin, $scope.pagination.end);
		});

		// triggered when the search term input changes
		$scope.$watch("searchTerm",	function () {
			$scope.filteredPlatforms = []; // empty filtered list
			angular.forEach($scope.platforms, function (platform) {
				// add those whose label contains the search term
				if (platform.label !== null	&& ~platform.label.toLowerCase().indexOf($scope.searchTerm)) {
					$scope.filteredPlatforms.push(platform);
				}
			});
			$scope.pagination.currentPage = 1; // set pagination to first page
		});

		$scope.filteredPlatforms = $scope.platforms = platforms.data;
	});

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
	
	d2sApp.controller('resourceTypeCtrl', function ($scope, $rootScope, $timeout, platformFactory, $modal, toaster) {
		if (angular.isUndefined($rootScope.resourceTypes)) { // if resource types weren't retrieved in this app before
			platformFactory.getResourceTypes().success(function (data, status) {
				if (status >= 400) {
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving available resource types from the ontology.');
				}
				$rootScope.resourceTypes = data;
			});
		}
		
		// $scope.addOption method is inherited from parent scope (addPlatformCtrl)
		
		$scope.openModal = function (resourceType, size) {
			var modalInstance = $modal.open({
				animation: true,
				templateUrl: 'myModalContent.html',
				controller: 'resourceModalCtrl',
				size: size,
				resolve: {
					resourceType: function () {
						return resourceType;
					}
				}
			});
	
			modalInstance.result.then(function (selected) {
				resourceType.externals = selected;
			});
		};
	});
	
	d2sApp.controller('resourceModalCtrl', function ($scope, $modalInstance, $timeout, resourceFactory, toaster, resourceType) {		
		$scope.dbpediaResources = [];
		$scope.selected = (resourceType.externals && resourceType.externals.length > 0) ? resourceType.externals : [];
		$scope.searchTerm = resourceType.label;

		$scope.close = function () {
			$modalInstance.close($scope.selected);
		};
		
		$scope.checkNew = function (label) {
			if (!label) {
				return false;
			}
			var labelLower = label.toLowerCase();
			var isNew = true;
			$scope.resourceTypes.forEach(function (type) {
				if (type.name.toLowerCase() === labelLower || type.resourceName.toLowerCase() === labelLower) {
					isNew = false;
				}
			});
			return isNew;
		};
		
		$scope.isNew = $scope.checkNew(resourceType.label);
		
		var _timeout;
		$scope.retrieveExternal = function (value, delay) {
			if (isNaN(delay) || delay < 0) {
				delay = 500;
			}
			if (_timeout) { //if there is already a timeout in process cancel it
				$timeout.cancel(_timeout);
			}
			if (value) {
				_timeout = $timeout(function () {
					resourceFactory.dbpedia(value).success(function (data, status) {
						if (status >= 400) {
							toaster.pop('error', 'Code ' + status, 'There was an error retrieving resources from DBPedia.');
						}
						$scope.dbpediaResources.length = 0;
						if (data && data.results) {
							$scope.dbpediaResources = data.results;
						}
					});
				    _timeout = null;
				}, delay); // call with a delay
			} else {
				$scope.dbpediaResources.length = 0;
			}
		};

		$scope.retrieveExternal($scope.searchTerm, 0); // on modal startup
		
		$scope.addConcept = function (resource) {
			var duplicate = false;
			$scope.selected.forEach(function (item) {
				if (angular.equals(item, resource)) {
					duplicate = true;
				}
			});
			if (!duplicate) {
				$scope.selected.push(resource);
			}
		};
		
		$scope.addDbpediaConcept = function (resource) {
			$scope.addConcept({
				label: resource.label,
				resource: resource.uri,
				description: resource.description
			});
		};
		
		$scope.addCustomConcept = function (uri) {
			if (uri) {
				$scope.addConcept({
					resource: uri,
					description: ""
				});
			}
		};
	});
	
	d2sApp.controller('editPlatformCtrl', function ($scope, $rootScope, toaster, platformFactory, platform) {
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
		if (angular.isUndefined($rootScope.languages)) {
			platformFactory.getLanguages().success(function (data, status) {
				if (status >= 400) {
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving the list of languages.');
				}
				$rootScope.languages = data.languages;
			});
		}
		$scope.getYears = function () {
			var currentYear = new Date().getFullYear();
			var output = [];
			for (var i = currentYear; i > 1989; i--) {
				output.push(i);
			}
			return output;
		};
		
		$scope.platform = platform.data;
		console.log($scope.platform);
	});

})();