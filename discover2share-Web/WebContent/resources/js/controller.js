(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('headerController', function ($scope, $rootScope,
			$location, $cookieStore, authFactory) {
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

	d2sApp.controller('loginCtrl', function ($scope, $location, authFactory) {
		$scope.loggedOut = false;

		if ($location.path().substr(1) === "logout") {
			authFactory.logout();
			$scope.loggedOut = true;
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
					}).error(function () {
						$scope.registerFailed = true;
						$scope.registerSuccess = false;
					});
			}
		};
	});

	d2sApp.controller('platformsCtrl', function ($scope, platforms) {
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

	d2sApp.controller('platformDetailCtrl', function ($scope, platformFactory, $rootScope, platform) {
		if (!angular.isUndefined(platform)) {
			$scope.platform = platform.data;

			// retrieve launchYear label
			if (!angular.isUndefined($scope.platform.yearLaunch)) {
				$scope.platform.yearLaunchName = $scope.platform.yearLaunch.replace("http://www.discover2share.net/d2s-ont/", "");
			}
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
			platformFactory.getDescriptions().success(function (data) {
				$rootScope.descriptions = data;
			});
		}
	});

	d2sApp.controller('addPlatformCtrl', function ($scope, $rootScope, platformFactory) {
		// retrieve dimension comments and labels (only once)
		if (angular.isUndefined($rootScope.descriptions)) {
			platformFactory.getDescriptions().success(function (data) {
				$rootScope.descriptions = data;
			});
		}
		if (angular.isUndefined($rootScope.countries)) {
			platformFactory.getCountries().success(function (data) {
				$rootScope.countries = data.countries;
			});
		}
		if (angular.isUndefined($rootScope.languages)) {
			platformFactory.getLanguages().success(function (data) {
				$rootScope.languages = data.languages;
			});
		}

		$scope.platform = {
			resourceTypeObjects : [ {} ],
			resourceTypes : [],
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
			$scope.platform.launchCity.search = "";
			$scope.platform.launchCity.item = item;
			$scope.platform.launchCity.geonames = "http://www.geonames.org/" + item.geonameId;
			$scope.platform.launchCity.label = item.toponymName;
			if (!angular.isUndefined($scope.platform.launchCountry.item) && 
					$scope.platform.launchCity.item.countryCode !== $scope.platform.launchCountry.item.countryCode) {
				console.log("Selected launch city is not situated in the selected country.");
			}
		};
		
		$scope.residenceCitySelected = function (item) {
			$scope.platform.residenceCity.search = "";
			$scope.platform.residenceCity.item = item;
			$scope.platform.residenceCity.geonames = "http://www.geonames.org/"	+ item.geonameId;
			$scope.platform.residenceCity.label = item.toponymName;
			if (!angular.isUndefined($scope.platform.residenceCountry.item)	&& 
					$scope.platform.residenceCity.item.countryCode !== $scope.platform.residenceCountry.item.countryCode) {
				console.log("Selected residence city is not situated in the selected country.");
			}
		};
		
		$scope.launchCountrySelected = function (item) {
			$scope.platform.launchCountry.search = "";
			$scope.platform.launchCountry.item = item;
			$scope.platform.launchCountry.geonames = "http://www.geonames.org/"	+ item.countryId;
			$scope.platform.launchCountry.label = item.countryName;
			if (!angular.isUndefined($scope.platform.launchCity.item) && 
					$scope.platform.launchCity.item.countryCode !== $scope.platform.launchCountry.item.countryCode) {
				console.log("Selected launch city is not situated in the selected country.");
			}
		};
		
		$scope.residenceCountrySelected = function (item) {
			$scope.platform.residenceCountry.search = "";
			$scope.platform.residenceCountry.item = item;
			$scope.platform.residenceCountry.geonames = "http://www.geonames.org/" + item.countryId;
			$scope.platform.residenceCountry.label = item.countryName;
			if (!angular.isUndefined($scope.platform.residenceCity.item) && 
					$scope.platform.residenceCity.item.countryCode !== $scope.platform.residenceCountry.item.countryCode) {
				console.log("Selected residence city is not situated in the selected country.");
			}
		};

		// triggered when the search term input changes
		$scope.findLaunchCity = function () {
			if ($scope.platform.launchCity.search === "") {
				return;
			}
			return platformFactory.findCity($scope.platform.launchCity.search, $scope.platform.launchCountry.item.countryCode)
					.then(function (response) {
						return response.data.geonames;
					});
		};
		
		$scope.findResidenceCity = function () {
			if ($scope.platform.residenceCity.search === "") {
				return;
			}
			return platformFactory.findCity($scope.platform.residenceCity.search, $scope.platform.residenceCountry.item.countryCode)
					.then(function (response) {
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
				"Profit from advertisement", "Profit from user data" ];
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
			$scope.platform.resourceTypes = $scope.platform.resourceTypeObjects
					.filter(function (i) {
						return !angular.isUndefined(i.label);
					}).map(function (i) {
						return i.label;
					});
			
			$scope.platform.languages = $scope.platform.languageObjects
					.filter(function (i) {
						return !angular.isUndefined(i.resourceName);
					}).map(function (i) {
						return i.resourceName;
					});
			platformFactory.addPlatformSuggestion($scope.platform);
		};
	});

})();