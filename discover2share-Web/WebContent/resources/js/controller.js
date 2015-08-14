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
				output.push(i--);
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
	
	d2sApp.controller('queryCtrl', function ($scope, $rootScope, $http) {
		$scope.filter = {};
		$scope.yasqeConfig = {
				value: "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX dbpp: <http://dbpedia.org/property/>\nPREFIX d2s: <http://www.discover2share.net/d2s-ont/>\n\nSELECT * WHERE {\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n}",
				sparql: {
					showQueryButton: true,
					endpoint: "api/ontology/query"
				}
			};
		
		$scope.allDetailsQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/>\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX dbpp: <http://dbpedia.org/property/>\nPREFIX dbpo: <http://dbpedia.org/ontology/>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\n\nSelect ?platform ?label ?url ?resourceType ?consumerInvolvement ?launchCityName ?launchCountryName ?yearLaunch ?residenceCityName ?residenceCountryName ?marketMediation ?offering ?geographicScope ?moneyFlow ?pattern ?temporality ?consumerism ?resourceOwner ?serviceDurationMin ?serviceDurationMax ?app ?trustContribution ?typeOfAccessedObject WHERE {\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n    ?platform rdfs:label ?label.\n    ?platform dbpp:url ?url.\n    OPTIONAL {\n        ?platform d2s:has_resource_type ?rt.\n        ?rt rdfs:label ?resourceType.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_consumer_involvement ?ci.\n        ?ci rdfs:label ?consumerInvolvement.\n    }.\n    OPTIONAL {\n        ?platform d2s:launched_in ?launch.\n        OPTIONAL {\n            ?launch dbpp:locationCity ?launchCity.\n            ?launchCity rdfs:label ?launchCityName.\n        }.\n        OPTIONAL {\n            ?launch dbpp:locationCountry ?launchCountry.\n            ?launchCountry rdfs:label ?launchCountryName.\n        }.\n    }. \n    OPTIONAL {\n        ?platform dbpp:launchYear ?yearLaunch.\n    }.\n    OPTIONAL {\n        ?platform d2s:operator_resides_in ?residence.\n        OPTIONAL {\n            ?residence dbpp:locationCity ?residenceCity.\n            ?residenceCity rdfs:label ?residenceCityName.\n        }.\n        OPTIONAL {\n            ?residence dbpp:locationCountry ?residenceCountry.\n            ?residenceCountry rdfs:label ?residenceCountryName.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_market_mediation ?me.\n        ?me rdfs:label ?marketMediation.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_market_integration ?integration.\n        OPTIONAL {\n            ?integration d2s:markets_are ?of.\n            ?of rdfs:label ?offering.\n        }.\n        OPTIONAL {\n            ?integration d2s:has_scope ?sc.\n            ?sc rdfs:label ?geographicScope.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_money_flow ?mf.\n        ?mf rdfs:label ?moneyFlow.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_p2p_scc_pattern ?patternNode.\n        ?patternNode rdf:type ?pa.\n        ?pa rdfs:label ?pattern.\n        OPTIONAL {\n            ?patternNode d2s:has_temporality ?te.\n            ?te rdfs:label ?temporality.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:promotes ?co.\n        ?co rdfs:label ?consumerism.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_resource_owner ?ro.\n        ?ro rdfs:label ?resourceOwner.\n    }.\n    OPTIONAL {\n        ?platform d2s:min_service_duration ?serviceDurationMin.\n    }.\n    OPTIONAL {\n        ?platform d2s:max_service_duration ?serviceDurationMax.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_app ?ap.\n        ?ap rdfs:label ?app.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_trust_contribution ?tc.\n        ?tc rdfs:label ?trustContribution.\n    }.\n    OPTIONAL {\n        ?platform d2s:accessed_object_has_type ?ot.\n        ?ot rdfs:label ?typeOfAccessedObject.\n    }.\n} ORDER BY ?platform";
		$scope.queryParts = {
				d2sBase: "http:\\/\\/www\\.discover2share\\.net\\/d2s-ont\\/",
				resourceType: "d2s:has_resource_type",
				consumerInvolvement: " d2s:has_consumer_involvement ",
				launch: " ?launched_in ?launch.",
				countryLaunch: "?launch dbpp:locationCountry ",
				cityLaunch: "?launch dbpp:locationCity "
			};
		
		$scope.$watch('filter', function (newValue, oldValue) {
			$scope.doFilter(newValue, oldValue);
		}, true);
		
		$scope.doFilter = function (newValue, oldValue) {
			//make sure d2s Prefix is available
			var prefixMatch = $scope.query.match(new RegExp("(PREFIX[ ]+d2s:[ ]+<" + $scope.queryParts.d2sBase + ">)", "i"));
			if (prefixMatch === null) {
				$scope.query = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/>\n" + $scope.query;
			}
			
			var platformVar = $scope.platformVar || $scope.getPlatformVar();
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
					
			//check resourceType
			if (!angular.isUndefined(oldValue) && newValue.resourceType !== oldValue.resourceType) {
				var regexp;
				//remove old occurrence
				if (oldValue.resourceType !== "") {
					regexp = new RegExp("([ ]{0,4}\\" + platformVar.name + "[ ]+" + $scope.queryParts.resourceType + "[ ]+" + "(?:d2s:" + oldValue.resourceType + "|<" + $scope.queryParts.d2sBase + oldValue.resourceType + ">)[^\w]*?[ ]*\\.?[ ]*\\n?)", "g");
					var sectionLength = section.length;
					section = section.replace(regexp, "");
					$scope.query = $scope.query.substr(0, platformVar.openingBracket) + section + $scope.query.substr(platformVar.closingBracket);
					platformVar.closingBracket -= (sectionLength - section.length);
				}
				
				regexp = new RegExp("(\\" + platformVar.name + "[ ]+" + $scope.queryParts.resourceType + "[ ]+" + "(?:d2s:" + newValue.resourceType + "|<" + $scope.queryParts.d2sBase + newValue.resourceType + ">))", "g");
				var matches = section.match(regexp);
				if (matches === null) {
					var queryLength = $scope.query.length;
					$scope.query = $scope.query.substr(0, platformVar.closingBracket) + "    " + platformVar.name + " " + $scope.queryParts.resourceType + " d2s:" + newValue.resourceType + ".\n" + $scope.query.substr(platformVar.closingBracket);
					platformVar.closingBracket += ($scope.query.length - queryLength);
				}
			}
			
			$scope.computedQuery = $scope.query;
		};
		
		$scope.getPlatformVar = function () {
			//determine platform var
			var pos = $scope.query.search("[?][a-z0-9]+ rdf:type d2s:P2P_SCC_Platform");
			var platformVar = $scope.query.substr(pos);
			$scope.platformVar = {
				name: platformVar.substr(0, platformVar.search(" ")),
				pos: pos,
				openingBracket: $scope.query.substr(0, pos).lastIndexOf("{"),
				closingBracket: pos + $scope.findClosingBracket(platformVar)
			};
			return $scope.platformVar;
		};
		
		$scope.findClosingBracket = function (str) {
			var substr = str;
			var openings = 0;
			var offset = 0;
			do {
				var nextOpening = substr.indexOf("{");
				var nextClosing = substr.indexOf("}");
				if (!~nextClosing) {
					throw new Error("Couldn't find closing bracket in query.");
				}
				if (!~nextOpening || nextOpening > nextClosing) {
					if (openings-- === 0) {
						return offset + nextClosing;
					}
					offset += nextClosing + 1;
					substr = substr.substr(nextClosing + 1);
				} else {
					openings++;
					offset += nextOpening + 1;
					substr = substr.substr(nextOpening + 1);
				}
			} while (openings >= 0);
			throw new Error("Couldn't find closing bracket in query.");
		};
		
		$scope.$watch('query', function (newValue, oldValue) {
			if (newValue === oldValue || $scope.computedQuery === $scope.query) {
				return;
			}
			
			var platformVar = $scope.getPlatformVar();
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			
			//check for constraints matching any filters in the form
			var regexp = new RegExp("\\" + platformVar.name + "[ ]+" + $scope.queryParts.resourceType + "[ ]+" + "(?:d2s:([a-z0-9]+)|<" + $scope.queryParts.d2sBase + "([a-z0-9]+)>)", "g");
			var match;
			while (match = regexp.exec(section)) {
				$scope.filter.resourceType = match[1] || match[2];
			}
		});
		
		$scope.setAllDetailsQuery = function () {
			$scope.query = $scope.allDetailsQuery;
		};
	});

})();