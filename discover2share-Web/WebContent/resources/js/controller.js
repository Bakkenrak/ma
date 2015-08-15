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
				anyResourceName: "[^\\s}.]+",
				trailingRemovableChars: "[^\\w]*?[ ]*\\.?[ ]*\\n?",
				
				resourceType: "d2s:has_resource_type",
				consumerInvolvement: "d2s:has_consumer_involvement",
				launch: "d2s:launched_in",
				residence: "d2s:operator_resides_in",
				country: "dbpp:locationCountry",
				city: "dbpp:locationCity",
				yearLaunch: "dbpp:launchYear",
				language: "dbpp:language"
			};
		
		$scope.$watch('filter', function (newValue, oldValue) {
			$scope.doFilter(newValue, oldValue);
		}, true);
		
		$scope.doFilter = function (newValue, oldValue) {
			if (newValue === oldValue || $scope.userChange) {
				$scope.userChange = false;
				return;
			}
			
			//make sure d2s Prefix is available
			var prefixMatch = $scope.query.match(new RegExp("(PREFIX[ ]+d2s:[ ]+<" + $scope.queryParts.d2sBase + ">)", "i"));
			if (prefixMatch === null) {
				$scope.query = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/>\n" + $scope.query;
			}
			
			if (!angular.isUndefined(oldValue)) {
				// resource type
				$scope.simplePattern(newValue.resourceType, oldValue.resourceType, $scope.queryParts.resourceType);
				// consumer involvement
				$scope.simplePattern(newValue.consumerInvolvement, oldValue.consumerInvolvement, $scope.queryParts.consumerInvolvement);
				// locations
				$scope.locationFilter(newValue, oldValue);
				// year
				$scope.simplePattern(newValue.yearLaunch, oldValue.yearLaunch, $scope.queryParts.yearLaunch);
				// language
				$scope.simplePattern(newValue.language, oldValue.language, $scope.queryParts.language);
			}
			
			$scope.computedQuery = $scope.query;
		};
		
		$scope.locationFilter = function (newValue, oldValue) {	
			// launch country
			$scope.locationPattern(newValue.countryLaunch, oldValue.countryLaunch, $scope.queryParts.launch, $scope.queryParts.country, "?launchLocation");
			// launch city
			$scope.locationPattern(newValue.cityLaunch, oldValue.cityLaunch, $scope.queryParts.launch, $scope.queryParts.city, "?launchLocation");
			// remove launch node statement if neither city nor country are requested
			if ((newValue.cityLaunch !== oldValue.cityLaunch || newValue.countryLaunch !== oldValue.countryLaunch) &&
					$scope.filter.cityLaunch === "" && $scope.filter.countryLaunch === "") {
				$scope.removeLocationNode($scope.queryParts.launch);
			}
			
			// residence country
			$scope.locationPattern(newValue.countryResidence, oldValue.countryResidence, $scope.queryParts.residence, $scope.queryParts.country, "?residenceLocation");
			// residence city
			$scope.locationPattern(newValue.cityResidence, oldValue.cityResidence, $scope.queryParts.residence, $scope.queryParts.city, "?residenceLocation");
			if ((newValue.cityResidence !== oldValue.cityResidence || newValue.countryResidence !== oldValue.countryResidence) &&
					$scope.filter.cityResidence === "" && $scope.filter.countryResidence === "") {
				$scope.removeLocationNode($scope.queryParts.launch);
			}
		};
		
		$scope.removeLocationNode = function (queryPartLocation) {
			var platformVar = $scope.platformVar || $scope.getPlatformVar();
			
			var regexp = new RegExp("(\\" + platformVar.name + "[ ]+" + queryPartLocation + "[ ]+\\?[\\w]+" + $scope.queryParts.trailingRemovableChars + ")", "g");
			
			// Cut out the query part in which the platform variable resides. All operations will be done in this section.
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			var queryLength = $scope.query.length;
			$scope.query = $scope.query.substr(0, platformVar.openingBracket) + section.replace(regexp, "") + $scope.query.substr(platformVar.closingBracket);
			$scope.platformVar.closingBracket -= queryLength - $scope.query.length;
		};
		
		$scope.simplePattern = function (newVal, oldVal, queryPart, firstVar) {
			if (newVal === oldVal) { // when the attribute's value hasn't changed
				return; // abort
			}
			// take platform variable scope or compute anew if not yet done
			var platformVar = $scope.platformVar || $scope.getPlatformVar();
			firstVar = firstVar || platformVar.name; // if firstVar is not provided, use platform variable's name
			// Cut out the query part in which the platform variable resides. All operations will be done in this section.
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			
			var regexp;
			// remove old occurrence
			if (oldVal !== "" && !angular.isUndefined(oldVal)) {
				regexp = new RegExp("([ ]{0,4}\\" + firstVar + "[ ]+" + queryPart + "[ ]+" + "(?:d2s:" + oldVal + "|<" + $scope.queryParts.d2sBase + oldVal + ">)" + $scope.queryParts.trailingRemovableChars + ")", "g");
				var sectionLength = section.length;
				section = section.replace(regexp, ""); // remove all occurrences that match the regexp
				// rebuild query string with altered section
				$scope.query = $scope.query.substr(0, platformVar.openingBracket) + section + $scope.query.substr(platformVar.closingBracket);
				$scope.platformVar.closingBracket -= (sectionLength - section.length); // adjust closing bracket pointer
			}
			// create new occurrence
			if (newVal !== "" && !angular.isUndefined(newVal)) {
				regexp = new RegExp("(\\" + firstVar + "[ ]+" + queryPart + "[ ]+" + "(?:d2s:" + newVal + "|<" + $scope.queryParts.d2sBase + newVal + ">))", "g");
				var matches = section.match(regexp); // check if an expression matching the new value already exists
				if (matches === null) { //otherwise add it
					var queryLength = $scope.query.length;
					$scope.query = $scope.query.substr(0, platformVar.closingBracket) + "    " + firstVar + " " + queryPart + " d2s:" + newVal + ".\n" + $scope.query.substr(platformVar.closingBracket);
					$scope.platformVar.closingBracket += ($scope.query.length - queryLength); //adjust closing bracket pointer
				}
			}
		};
		
		$scope.locationPattern = function (newVal, oldVal, queryPartLocation, queryPartDetail, locationVar) {
			if (newVal === oldVal) {
				return;
			}
			var platformVar = $scope.platformVar || $scope.getPlatformVar(); // take platform variable from scope. Compute anew if not yet done
			var locVar = $scope.getLocationVar(platformVar, queryPartLocation);
			locationVar = locVar || locationVar;
			if (newVal !== "" && locVar === null) {
				var queryLength = $scope.query.length;
				$scope.query = $scope.query.substr(0, platformVar.closingBracket) + "    " + platformVar.name + " " + queryPartLocation + " " + locationVar + ".\n" + $scope.query.substr(platformVar.closingBracket);
				$scope.platformVar.closingBracket += ($scope.query.length - queryLength); //adjust closing bracket pointer
			}
			
			$scope.simplePattern(newVal, oldVal, queryPartDetail, locationVar);
		};
		
		$scope.getLocationVar = function (platformVar, queryPartLocation) {
			// Cut out the query part in which the platform variable resides. All operations will be done in this section.
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			
			var regexp;
			// find/insert location variable
			regexp = new RegExp("\\" + platformVar.name + "[ ]+" + queryPartLocation + "[ ]+(\\?[\\w]+)", "");
			var match = regexp.exec(section);
			if (match === null) { // add location statement if there is a new value that requires it
				return null;
			} else {
				return match[1];
			}
		};
		
		$scope.getPlatformVar = function () {
			//determine platform var
			var pos = $scope.query.search("[?][\\w]+ rdf:type d2s:P2P_SCC_Platform");
			if (~pos) {
				var substring = $scope.query.substr(pos);
				$scope.platformVar = { // set object in scope to avoid unnecessary calculations in the future
					name: substring.substr(0, substring.indexOf(" ")),
					openingBracket: $scope.query.substr(0, pos).lastIndexOf("{"),
					closingBracket: pos + $scope.findClosingBracket(substring)
				};
			} else { // no variable found
				$scope.platformVar = null;
			}
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
			if (newValue === oldValue || $scope.computedQuery === $scope.query) { // if value hasn't changed or change was done programatically
				return; // abort
			}
			
			var platformVar = $scope.getPlatformVar(); // find platform variable
			// copy query section in which the platform variable resides
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			
			// check for constraints matching any filters in the form
			var match, count;
			
			// resource types
			var regexp = new RegExp("\\" + platformVar.name + "[ ]+" + $scope.queryParts.resourceType + "[ ]+" + "(?:d2s:(" + $scope.queryParts.anyResourceName + ")|<" + $scope.queryParts.d2sBase + "(" + $scope.queryParts.anyResourceName + ")>)", "g");
			count = 0;
			while (match = regexp.exec(section)) {
				$scope.filter.resourceType = match[1] || match[2];
				count++;
			}
			if (count === 0) {
				$scope.filter.resourceType = "";
			}
			
			// consumer involvement
			$scope.filter.consumerInvolvement = $scope.findPattern($scope.queryParts.consumerInvolvement);
			
			//launch
			var locationVar = $scope.getLocationVar(platformVar, $scope.queryParts.launch);
			if (locationVar !== null) {
				// city
				$scope.filter.cityLaunch = $scope.findPattern($scope.queryParts.city, locationVar);
				// country
				$scope.filter.countryLaunch = $scope.findPattern($scope.queryParts.country, locationVar);
			}
			
			// residence
			locationVar = $scope.getLocationVar(platformVar, $scope.queryParts.residence);
			if (locationVar !== null) {
				// city
				$scope.filter.cityResidence = $scope.findPattern($scope.queryParts.city, locationVar);
				// country
				$scope.filter.countryResidence = $scope.findPattern($scope.queryParts.country, locationVar);
			}
			
			// year
			$scope.filter.yearLaunch = $scope.findPattern($scope.queryParts.yearLaunch);
			
			// language
			var regexp = new RegExp("\\" + platformVar.name + "[ ]+" + $scope.queryParts.language + "[ ]+" + "(?:d2s:(" + $scope.queryParts.anyResourceName + ")|<" + $scope.queryParts.d2sBase + "(" + $scope.queryParts.anyResourceName + ")>)", "g");
			count = 0;
			while (match = regexp.exec(section)) {
				$scope.filter.language = match[1] || match[2];
				count++;
			}
			if (count === 0) {
				$scope.filter.language = "";
			}
			
			$scope.userChange = true; // signal that changes were invoked by the user to avoid an unnecessary run of the filter method
		});
		
		$scope.findPattern = function (queryPart, firstVar) {
			var platformVar = $scope.getPlatformVar(); // find platform variable
			// copy query section in which the platform variable resides
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			firstVar = firstVar || platformVar.name;
			var regexp = new RegExp("\\" + firstVar + "[ ]+" + queryPart + "[ ]+" + "(?:d2s:(" + $scope.queryParts.anyResourceName + ")|<" + $scope.queryParts.d2sBase + "(" + $scope.queryParts.anyResourceName + ")>)", "");
			var match = regexp.exec(section);
			return (match !== null) ? match[1] || match[2] : "";
		};
		
		$scope.setAllDetailsQuery = function () {
			$scope.query = $scope.allDetailsQuery;
		};
	});

})();