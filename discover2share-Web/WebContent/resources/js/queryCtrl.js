(function () {
	'use strict';
	
	var d2sApp = angular.module("d2sApp");
	
	/**
	 * Controller for the query builder form.
	 */
	d2sApp.controller('queryCtrl', function ($scope, $rootScope, $http, $timeout, platformFactory) {
		if (angular.isUndefined($rootScope.countries)) { // if countries weren't retrieved in this app before
			platformFactory.getCountries().success(function (data) {
				$rootScope.countries = data.countries;
			});
		}
		if (angular.isUndefined($rootScope.cities)) { // if cities weren't retrieved in this app before
			platformFactory.getCities().success(function (data) {
				$rootScope.cities = data;
			});
		}
		if (angular.isUndefined($rootScope.languages)) { // if languages weren't retrieved in this app before
			platformFactory.getLanguages().success(function (data) {
				$rootScope.languages = data.languages;
			});
		}
		if (angular.isUndefined($rootScope.resourceTypes)) { // if resource types weren't retrieved in this app before
			platformFactory.getResourceTypes().success(function (data) {
				$rootScope.resourceTypes = data;
			});
		}
		
		$scope.marketMediations = [ 
		    { resource: "Profit_from_peer_consumers", label: "Profit from peer consumers" }, 
		    { resource: "Profit_from_peer_providers", label: "Profit from peer providers" },
		    { resource: "Profit_from_both", label: "Profit from both" },
		    { resource: "Indirect_profit", label: "Indirect profit" },
		    { resource: "Profit_from_advertisement", label: "Profit from advertisement" },
		    { resource: "Profit_from_user data", label: "Profit from user data" }
		];
		$scope.consumerisms = [ "None", "Social", "Environmental", "Economic" ];
		$scope.smartphoneApps = [ 
		    { resource: "Android_app", label: "Android app" }, 
		    { resource: "iOS_app", label: "iOS app" }, 
		    { resource: "Windows_Phone_app", label: "Windows Phone app" }
		];
		$scope.trustContributions = [ 
		    { resource: "Provider_ratings", label: "Provider ratings" }, 
		    { resource: "Provider_and_consumer_ratings", label: "Provider and consumer ratings" }, 
		    { resource: "Referral", label: "Referral" }, 
		    { resource: "Vouching", label: "Vouching" }, 
		    { resource: "Value-added_services", label: "Value-added services" }
		];
		
		$scope.getYears = function () { // generate a list of all years between today and 1990
			var currentYear = new Date().getFullYear();
			var output = [];
			for (var i = currentYear; i > 1989; i--) {
				output.push(i);
			}
			return output;
		};
		
		// maintains the given 'selected' array containing all options in a multi-selection dimension that are currently selected
		$scope.toggleSelection = function (current, selected) {
			var idx = selected.indexOf(current);
			if (~idx) { // is currently selected
				selected.splice(idx, 1); // remove selection
			} else { // is newly selected
				selected.push(current); //add current selection to array
			}
		};
		
		$scope.filter = { // will hold all model attributes to which the form elements are bound
				marketMediations: [],
				consumerisms: [],
				apps: [],
				trustContributions: []
			};
		
		$scope.yasqeConfig = { // config options for the YASQE query editor
				// standard query to show when no previous user input is cached
				value: "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX dbpp: <http://dbpedia.org/property/>\nPREFIX dbpr: <http://dbpedia.org/resource/>\nPREFIX dbpo: <http://dbpedia.org/ontology/>\nPREFIX d2s: <http://www.discover2share.net/d2s-ont/>\n\nSELECT * WHERE {\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n}",
				sparql: {
					showQueryButton: true,
					endpoint: "api/ontology/query"
				}
			};
		
		$scope.yasrConfig = { // config options for the YASR query result viewer
				persistency: {
					results: {
						key: null // do not cache the results
					}
				}
			};
		
		$scope.queryParts = { // contains predicates for the different dimension and commonly used RegExps
				bases: {
					d2s: {
						prefix: "d2s",
						path: "http://www.discover2share.net/d2s-ont/",
						pattern: "http:\\/\\/www\\.discover2share\\.net\\/d2s-ont\\/"
					},
					dbpp: {
						prefix: "dbpp",
						path: "http://dbpedia.org/property/",
						pattern: "http:\\/\\/dbpedia\\.org\\/property\\/"
					},
					rdf: {
						prefix: "rdf",
						path: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
						pattern: "http:\\/\\/www\\.w3\\.org\\/1999\\/02\\/22-rdf-syntax-ns#"
					}
				},
				anyResourceName: "[^\\s}.]+",
				trailingRemovableChars: "[^\\w]*?[ ]*\\.?[ ]*\\n?",
				
				resourceType: "d2s:has_resource_type",
				consumerInvolvement: "d2s:has_consumer_involvement",
				launch: "d2s:launched_in",
				residence: "d2s:operator_resides_in",
				country: "dbpp:locationCountry",
				city: "dbpp:locationCity",
				yearLaunch: "dbpp:launchYear",
				language: "dbpp:language",
				marketMediation: "d2s:has_market_mediation",
				integration: "d2s:has_market_integration",
				marketOffering: "d2s:markets_are",
				geographicScope: "d2s:has_scope",
				moneyFlow: "d2s:has_money_flow",
				p2pSccPattern: "d2s:has_p2p_scc_pattern",
				temporality: "d2s:has_temporality",
				consumerism: "d2s:promotes",
				resourceOwner: "d2s:has_resource_owner",
				serviceDurationMin: "d2s:min_service_duration",
				serviceDurationMax: "d2s:max_service_duration",
				app: "d2s:has_app",
				trustContribution: "d2s:has_trust_contribution",
				typeOfAccessedObject: "d2s:accessed_object_has_type"
			};
		
		$scope.$watch('filter', function (newValue, oldValue) { // triggered when a user changes any input on the form elements
			$scope.doFilter(newValue, oldValue);
		}, true);
		
		$scope.doFilter = function (newValue, oldValue) { // executed when a user changes any form input, applies change to the query
			if (newValue === oldValue || $scope.userChange) { // if no actual change happened or it was triggered by the setFilterFromQuery method
				$scope.userChange = false; // reset flag attribute
				return;
			}
			
			//make sure prefixes are available
			var queryLength = $scope.query.length;
			angular.forEach($scope.queryParts.bases, function (base) {
				if (!$scope.query.match(new RegExp("(PREFIX[ ]+" + base.prefix + ":[ ]+<" + base.pattern + ">)", "i"))) { // check if current pattern doesn't already exist
					// insert at the top of the query string
					$scope.query = "PREFIX " + base.prefix + ": <" + base.path + ">\n" + $scope.query.replace(new RegExp("(PREFIX[ ]+" + base.prefix + ":[ ]+<.*?>[ ]*\n?)", "i"), "");
				}
			});
			if ($scope.platformVar) { // if the platform variable object had been determined in previous executions
				$scope.platformVar.openingBracket += $scope.query.length - queryLength; // adjust bracket pointers
				$scope.platformVar.closingBracket += $scope.query.length - queryLength;
			}
			
			// check each dimension for changes
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
			// market mediations
			$scope.arrayFilter(newValue.marketMediations, oldValue.marketMediations, $scope.queryParts.marketMediation);
			// integration
			$scope.integrationFilter(newValue, oldValue);
			// money flow
			$scope.simplePattern(newValue.moneyFlow, oldValue.moneyFlow, $scope.queryParts.moneyFlow);
			// P2P SCC pattern
			$scope.p2pSccPatternFilter(newValue, oldValue);
			// consumerism
			$scope.arrayFilter(newValue.consumerisms, oldValue.consumerisms, $scope.queryParts.consumerism);
			// resource owner
			$scope.simplePattern(newValue.resourceOwner, oldValue.resourceOwner, $scope.queryParts.resourceOwner);
			// service duration min
			$scope.simplePattern(newValue.serviceDurationMin, oldValue.serviceDurationMin, $scope.queryParts.serviceDurationMin, undefined, true);
			// service duration max
			$scope.simplePattern(newValue.serviceDurationMax, oldValue.serviceDurationMax, $scope.queryParts.serviceDurationMax, undefined, true);
			// smartphone app
			$scope.arrayFilter(newValue.apps, oldValue.apps, $scope.queryParts.app);
			// trust contribution
			$scope.arrayFilter(newValue.trustContributions, oldValue.trustContributions, $scope.queryParts.trustContribution);
			// type of accessed object
			$scope.simplePattern(newValue.typeOfAccessedObject, oldValue.typeOfAccessedObject, $scope.queryParts.typeOfAccessedObject);
			
			var lastBracket, endPart;
			// limit
			if (newValue.limit !== oldValue.limit) {
				lastBracket = $scope.query.lastIndexOf("}"); // find last closing bracket
				if (~lastBracket) { // if a closing bracket exists
					// only look after the last closing bracket and remove already existing limit constraints
					endPart = $scope.query.substr(lastBracket).replace(new RegExp("([ ]+LIMIT[ ]*[0-9]*)", "i"), "");
					if (newValue.limit) { // if a new limit was selected
						endPart += " LIMIT " + newValue.limit; // append constraint to the end of the query
					}
					$scope.query = $scope.query.substr(0, lastBracket) + endPart;
				}
			}
			//order by
			if (newValue.orderBy !== oldValue.orderBy) {
				lastBracket = $scope.query.lastIndexOf("}"); // find last closing bracket
				if (~lastBracket) { // if a closing bracket exists
					// only look after the last closing bracket and remove already existing order by constraints
					endPart = $scope.query.substr(lastBracket + 1).replace(new RegExp("([ ]*ORDER[ ]+BY[ ]*(?:\\?[\\w]+)?)", "i"), "");
					if (newValue.orderBy) { // if a new order by variable was selected
						endPart = " ORDER BY " + $scope.filter.orderBy + endPart; // insert constraint directly after the closing bracket
					}
					$scope.query = $scope.query.substr(0, lastBracket + 1) + endPart;
				}
			}
			
			$scope.getAllVars(); // refresh the list of variables used in the query to account for changes above
			
			$scope.computedQuery = $scope.query; // save query state so the setFilterFromQuery method can determine if it was triggered from here
		};
		
		// apply input changes on a dimension's form elements to the query
		$scope.arrayFilter = function (newValue, oldValue, queryPart) {
			if (angular.equals(newValue, oldValue)) { // abort if no changes were made in thi dimension
				return;
			}
			// retrieve platform variable
			var platformVar = $scope.platformVar || $scope.getPlatformVar();
			
			// regexp to search for occurrences of old value in the query 
			var regexp = new RegExp("([ ]{0,4}\\" + platformVar.name + "[ ]+" + queryPart + "[ ]+" + "(?:d2s:" + $scope.queryParts.anyResourceName + "|<" + $scope.queryParts.bases.d2s.pattern + $scope.queryParts.anyResourceName + ">)" + $scope.queryParts.trailingRemovableChars + ")", "g");
			// only look within the brackets surrounding the platform variable
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			var queryLength = $scope.query.length;
			// remove old value's occurrence in the query
			$scope.query = $scope.query.substr(0, platformVar.openingBracket) + section.replace(regexp, "") + $scope.query.substr(platformVar.closingBracket);
			$scope.platformVar.closingBracket -= queryLength - $scope.query.length; // adjust closing bracket pointer to changes
			
			// add each selected value
			newValue.forEach(function (item) {
				$scope.simplePattern(item, undefined, queryPart); // oldValue as undefined to avoid unnecessary removal pattern matching
			});
		};
		
		// takes care of input changes to the P2P SCC Pattern dimension form elements
		$scope.p2pSccPatternFilter = function (newValue, oldValue) {
			// P2P SCC pattern
			$scope.intermediatePattern(newValue.p2pSccPattern, oldValue.p2pSccPattern, $scope.queryParts.p2pSccPattern, "rdf:type", "?pattern");
			// temporality
			$scope.intermediatePattern(newValue.temporality, oldValue.temporality, $scope.queryParts.p2pSccPattern, $scope.queryParts.temporality, "?pattern");
			// remove pattern node statement if neither a pattern nor a temporality is defined
			if ((newValue.p2pSccPattern !== oldValue.p2pSccPattern || newValue.temporality !== oldValue.temporality) &&
					!$scope.filter.p2pSccPattern && !$scope.filter.temporality) {
				$scope.removeIntermediateNode($scope.queryParts.p2pSccPattern);
			}
		};
		
		// takes care of input changes to the Integration dimension form elements
		$scope.integrationFilter = function (newValue, oldValue) {
			// market offering
			$scope.intermediatePattern(newValue.marketOffering, oldValue.marketOffering, $scope.queryParts.integration, $scope.queryParts.marketOffering, "?integration");
			// geographic scope
			$scope.intermediatePattern(newValue.geographicScope, oldValue.geographicScope, $scope.queryParts.integration, $scope.queryParts.geographicScope, "?integration");
			// remove integration node statement if neither market offering nor scope are requested
			if ((newValue.marketOffering !== oldValue.marketOffering || newValue.geographicScope !== oldValue.geographicScope) &&
					!$scope.filter.marketOffering && !$scope.filter.geographicScope) {
				$scope.removeIntermediateNode($scope.queryParts.integration);
			}
		};
		
		// takes care of input changes to the location dimensions form elements
		$scope.locationFilter = function (newValue, oldValue) {	
			// launch country
			$scope.intermediatePattern(newValue.countryLaunch, oldValue.countryLaunch, $scope.queryParts.launch, $scope.queryParts.country, "?launchLocation");
			// launch city
			$scope.intermediatePattern(newValue.cityLaunch, oldValue.cityLaunch, $scope.queryParts.launch, $scope.queryParts.city, "?launchLocation");
			// remove launch node statement if neither city nor country are requested
			if ((newValue.cityLaunch !== oldValue.cityLaunch || newValue.countryLaunch !== oldValue.countryLaunch) &&
					!$scope.filter.cityLaunch && !$scope.filter.countryLaunch) {
				$scope.removeIntermediateNode($scope.queryParts.launch);
			}
			
			// residence country
			$scope.intermediatePattern(newValue.countryResidence, oldValue.countryResidence, $scope.queryParts.residence, $scope.queryParts.country, "?residenceLocation");
			// residence city
			$scope.intermediatePattern(newValue.cityResidence, oldValue.cityResidence, $scope.queryParts.residence, $scope.queryParts.city, "?residenceLocation");
			// remove residence node statement if neither city nor country are requested
			if ((newValue.cityResidence !== oldValue.cityResidence || newValue.countryResidence !== oldValue.countryResidence) &&
					!$scope.filter.cityResidence && !$scope.filter.countryResidence) {
				$scope.removeIntermediateNode($scope.queryParts.residence);
			}
		};
		
		// removes any statement that describes the platform variable using the given predicate
		$scope.removeIntermediateNode = function (queryPartLocation) {
			var platformVar = $scope.platformVar || $scope.getPlatformVar();
			// regexp to find relevant statements
			var regexp = new RegExp("([ ]{0,4}\\" + platformVar.name + "[ ]+" + queryPartLocation + "[ ]+\\?[\\w]+" + $scope.queryParts.trailingRemovableChars + ")", "g");
			// Cut out the query part in which the platform variable resides. All operations will be done in this section.
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			var queryLength = $scope.query.length;
			// remove found statements
			$scope.query = $scope.query.substr(0, platformVar.openingBracket) + section.replace(regexp, "") + $scope.query.substr(platformVar.closingBracket);
			$scope.platformVar.closingBracket -= queryLength - $scope.query.length; // adjust the closing bracket pointer
		};
		
		// creates or replaces a statement in the query
		$scope.simplePattern = function (newVal, oldVal, queryPart, firstVar, isCustomUrl) {
			if (newVal === oldVal) { // when the attribute's value hasn't changed
				return; // abort
			}
			// take platform variable scope or compute anew if not yet done
			var platformVar = $scope.platformVar || $scope.getPlatformVar();
			firstVar = firstVar || platformVar.name; // if firstVar is not provided, use platform variable's name
			// Cut out the query part in which the platform variable resides. All operations will be done in this section.
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			
			var regexp;
			if (oldVal) { // remove old occurrence if an old value existed
				regexp = "([ ]{0,4}\\" + firstVar + "[ ]+" + queryPart + "[ ]+"; // build regexp
				if (isCustomUrl) { // if the given values are URLs that do not have the d2s base path
					regexp += "<" + oldVal + ">";
				} else {
					regexp += "(?:d2s:" + oldVal + "|<" + $scope.queryParts.bases.d2s.pattern + oldVal + ">)";
				}
				regexp += $scope.queryParts.trailingRemovableChars + ")";
				var sectionLength = section.length;
				section = section.replace(new RegExp(regexp, "g"), ""); // remove all occurrences that match the regexp
				// rebuild query string with altered section
				$scope.query = $scope.query.substr(0, platformVar.openingBracket) + section + $scope.query.substr(platformVar.closingBracket);
				$scope.platformVar.closingBracket -= (sectionLength - section.length); // adjust closing bracket pointer
			}
			// create new occurrence
			if (newVal) {
				regexp = "(\\" + firstVar + "[ ]+" + queryPart + "[ ]+"; // build regexp
				if (isCustomUrl) { // if the given values are URLs that do not have the d2s base path
					regexp += "<" + newVal + ">)";
				} else {
					regexp += "(?:d2s:" + newVal + "|<" + $scope.queryParts.bases.d2s.pattern + newVal + ">))";
				}
				var match = section.match(new RegExp(regexp, "")); // check if an expression matching the new value already exists
				if (!match) { //otherwise add it
					var queryLength = $scope.query.length;
					var queryEnd = $scope.query.substr(platformVar.closingBracket);
					// add new statement before closing bracket
					$scope.query = $scope.query.substr(0, platformVar.closingBracket) + "    " + firstVar + " " + queryPart;
					if (isCustomUrl) { // if the given values are URLs that do not have the d2s base path
						$scope.query += " <" + newVal + ">";
					} else {
						$scope.query += " d2s:" + newVal;
					}
					$scope.query += ".\n" + queryEnd;
					$scope.platformVar.closingBracket += ($scope.query.length - queryLength); //adjust closing bracket pointer
				}
			}
		};
		
		// For dimensions like Market Integration that require an intermediate node. Creates this then calls the simplePattern method for the actual value
		$scope.intermediatePattern = function (newVal, oldVal, queryPartIntermediate, queryPartDetail, intermediateVar) {
			if (newVal === oldVal) { // if no change was made in this dimension
				return;
			}
			var platformVar = $scope.platformVar || $scope.getPlatformVar(); // take platform variable from scope. Compute anew if not yet in scope.
			var intVar = $scope.getIntermediateVar(platformVar, queryPartIntermediate); // check if a variable describing the intermediate node already exists
			intermediateVar = intVar || intermediateVar; // if a variable was found, take that, otherwise take the one given as parameter
			if (newVal && !intVar) { // if the new value is not null/undefined and no intermediate variable was found in the query
				var queryLength = $scope.query.length;
				// add statement to describe the intermediate node
				$scope.query = $scope.query.substr(0, platformVar.closingBracket) + "    " + platformVar.name + " " + queryPartIntermediate + " " + intermediateVar + ".\n" + $scope.query.substr(platformVar.closingBracket);
				$scope.platformVar.closingBracket += ($scope.query.length - queryLength); //adjust closing bracket pointer
			}
			// call simplePattern with the actual new value and pass the intermediate variable to use as the subject instead of the platform variable
			$scope.simplePattern(newVal, oldVal, queryPartDetail, intermediateVar);
		};
		
		// finds the (first) object variable in the query describing the given platform variable using the given predicate
		$scope.getIntermediateVar = function (platformVar, queryPart) {
			// Cut out the query part in which the platform variable resides. All operations will be done in this section.
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			var regexp;
			regexp = new RegExp("\\" + platformVar.name + "[ ]+" + queryPart + "[ ]+(\\?[\\w]+)", ""); // regexp to find intermediate variable
			var match = regexp.exec(section); // execute regexp
			return match ? match[1] : null; // if a variable was found return it, otherwise null
		};
		
		// finds the (first) platform variable (... rdf:type d2s:P2P_SCC_Platform) in the query
		$scope.getPlatformVar = function () {
			//determine the position of the platform variable
			var pos = $scope.query.search("[?][\\w]+ rdf:type d2s:P2P_SCC_Platform");
			if (~pos) { // if a variable was found
				var substring = $scope.query.substr(pos);
				$scope.platformVar = { // set object in scope to avoid unnecessary calculations in the future
					name: substring.substr(0, substring.indexOf(" ")), // variable name bounded by the next space
					openingBracket: $scope.query.substr(0, pos).lastIndexOf("{"), // the last opening bracket before the platform variable
					closingBracket: pos + $scope.findClosingBracket(substring) // find the closing bracket in the query part after the variable
				};
			} else { // no variable found
				$scope.platformVar = null;
			}
			return $scope.platformVar;
		};
		
		// finds the first 'unopened' closing bracket in the given string
		$scope.findClosingBracket = function (str) {
			var substr = str;
			var openings = 0;
			var offset = 0;
			do {
				var nextOpening = substr.indexOf("{"); // find the position of the next opening bracket
				var nextClosing = substr.indexOf("}"); // find the position of the next closing bracket
				if (!~nextClosing) { // if no next closing bracket exists
					throw new Error("Couldn't find closing bracket in query.");
				}
				// no next opening bracket found or next opening bracket is situated only after the next closing bracket
				if (!~nextOpening || nextOpening > nextClosing) { 
					if (openings-- === 0) { // if all previously opening brackets were closed (= zero openings), decrease for next iteration
						return offset + nextClosing; // return the position of the next closing bracket, it's the one sought after
					}
					offset += nextClosing + 1; // continue the search after the next closing bracket
					substr = substr.substr(nextClosing + 1); // adjust the substring to search in in next iteration
				} else { // the next opening bracket is situated before the next closing bracket 
					openings++;
					offset += nextOpening + 1; // continue the search after the next opening bracket
					substr = substr.substr(nextOpening + 1); // adjust substring
				}
			} while (openings >= 0); // continue while there are still openings
			throw new Error("Couldn't find closing bracket in query."); // if the closing bracket was not found through the iterations, throw error
		};
		
		// executed whenever the SPARQL query string is altered
		$scope.$watch('query', function (newValue, oldValue) {
			if (newValue === oldValue || $scope.computedQuery === $scope.query) { // if value hasn't changed or change was done programatically by doFilter method
				return; // abort
			}
			$scope.computedQuery = ""; // reset to prevent this method from not triggering wrongfully when user makes and directly afterwards undoes a change			
			$scope.setFilterFromQuery();
		});
		
		// sets the filter form elements according to the information found in the query string
		$scope.setFilterFromQuery = function () {
			var platformVar = $scope.getPlatformVar(); // find platform variable
			if (platformVar) { // if a platform variable was detected in the query
				// copy query section in which the platform variable resides
				var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
				
				var match, count;
				
				// resource types
				$scope.filter.resourceType = $scope.findPattern($scope.queryParts.resourceType);		
				// consumer involvement
				$scope.filter.consumerInvolvement = $scope.findPattern($scope.queryParts.consumerInvolvement);		
				//launch
				var locationVar = $scope.getIntermediateVar(platformVar, $scope.queryParts.launch); // find intermediate location variable
				if (locationVar) { // if location variable detected
					// city
					$scope.filter.cityLaunch = $scope.findPattern($scope.queryParts.city, locationVar); // find city statement with location variable as subject
					// country
					$scope.filter.countryLaunch = $scope.findPattern($scope.queryParts.country, locationVar);
				}
				// residence
				locationVar = $scope.getIntermediateVar(platformVar, $scope.queryParts.residence); // find intermediate residence variable
				if (locationVar) { // if residence variable detected
					// city
					$scope.filter.cityResidence = $scope.findPattern($scope.queryParts.city, locationVar); // find city statement with residence variable as subject
					// country
					$scope.filter.countryResidence = $scope.findPattern($scope.queryParts.country, locationVar);
				}				
				// year
				$scope.filter.yearLaunch = $scope.findPattern($scope.queryParts.yearLaunch);				
				// language
				$scope.filter.language = $scope.findPattern($scope.queryParts.language);				
				// market mediation
				$scope.findPatternArray($scope.queryParts.marketMediation, $scope.filter.marketMediations);				
				// integration
				var integrationVar = $scope.getIntermediateVar(platformVar, $scope.queryParts.integration); // find intermediate integration variable
				if (integrationVar) {
					// market offering
					$scope.filter.marketOffering = $scope.findPattern($scope.queryParts.marketOffering, integrationVar);
					// geographic scope
					$scope.filter.geographicScope = $scope.findPattern($scope.queryParts.geographicScope, integrationVar);
				}				
				// money flow
				$scope.filter.moneyFlow = $scope.findPattern($scope.queryParts.moneyFlow);				
				// p2p scc pattern
				var patternVar = $scope.getIntermediateVar(platformVar, $scope.queryParts.p2pSccPattern);
				if (patternVar) {
					// market offering
					$scope.filter.p2pSccPattern = $scope.findPattern("rdf:type", patternVar);
					// geographic scope
					$scope.filter.temporality = $scope.findPattern($scope.queryParts.temporality, patternVar);
				}				
				// consumerism
				$scope.findPatternArray($scope.queryParts.consumerism, $scope.filter.consumerisms);				
				// resource owner
				$scope.filter.resourceOwner = $scope.findPattern($scope.queryParts.resourceOwner);				
				// service duration min
				// set custom url to true as the object is not a d2s resource
				$scope.filter.serviceDurationMin = $scope.findPattern($scope.queryParts.serviceDurationMin, undefined, true);
				// service duration max
				// set custom url to true as the object is not a d2s resource
				$scope.filter.serviceDurationMax = $scope.findPattern($scope.queryParts.serviceDurationMax, undefined, true);			
				// smartphone apps
				$scope.findPatternArray($scope.queryParts.app, $scope.filter.apps);
				// trust contribution
				$scope.findPatternArray($scope.queryParts.trustContribution, $scope.filter.trustContributions);
				// type of accessed object
				$scope.filter.typeOfAccessedObject = $scope.findPattern($scope.queryParts.typeOfAccessedObject);
			}
			
			// limit
			// find a possible limit constraint in the query part after the last closing bracket
			var limit = new RegExp("LIMIT[ ]*([0-9]*)", "i").exec($scope.query.substr($scope.query.lastIndexOf("}")));
			if (limit && !isNaN(limit[1])) { // if a limit is found with a numeric value
				$scope.filter.limit = parseInt(limit[1], 10); // set value to form element
			}
			
			//order by
			$scope.getAllVars(); // retrieve all variables used in the query
			// find a possible order by constraint in the query part after the last closing bracket
			var orderBy = new RegExp("[ ]*ORDER[ ]+BY[ ]*(\\?[\\w]+)", "i").exec($scope.query.substr($scope.query.lastIndexOf("}")));
			$scope.filter.orderBy = orderBy ? orderBy[1] : ""; // if an order by was found set the variable as value to the form element, otherwise empty
			
			$scope.userChange = true; // signal that changes were invoked by the user to avoid an unnecessary run of the filter method
			// force filter watch event for those cases where it would not have been triggered because no changes were made here
			// this sets back $scope.userChange and thus avoids aborting the doFilter method when it is next rightfully executed
			$scope.filter.trigger = !$scope.filter.trigger; 
		};
		
		// finds a statement with the given predicate (queryPart) and subject (firstVar) and returns its object resource
		$scope.findPattern = function (queryPart, firstVar, isCustomUrl) {
			var platformVar = $scope.platformVar || $scope.getPlatformVar(); // find platform variable
			// copy query section in which the platform variable resides
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			firstVar = firstVar || platformVar.name; // if firstVar is null/undefined, use platform variable
			var regexp = "\\" + firstVar + "[ ]+" + queryPart + "[ ]+"; // build regexp
			if (isCustomUrl) { // in case the object is expected not to be a d2s:resource
				regexp += "<(.+?)>";
			} else {
				regexp += "(?:d2s:(" + $scope.queryParts.anyResourceName + ")|<" + $scope.queryParts.bases.d2s.pattern + "(" + $scope.queryParts.anyResourceName + ")>)";
			}
			var match = new RegExp(regexp, "").exec(section); // search using the regex
			return match ? match[1] || match[2] : ""; // if a match was found return either the first or the second value (d2s:... or <...> vs <http://www.discover2share.net/d2s/ont/...)
		};
		
		// finds statements with the given predicate (queryPart) for dimensions with multiple values
		$scope.findPatternArray = function (queryPart, array) {
			var platformVar = $scope.platformVar || $scope.getPlatformVar(); // find platform variable
			// copy query section in which the platform variable resides
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			// build regexp to find said statements
			var regexp = new RegExp("\\" + platformVar.name + "[ ]+" + queryPart + "[ ]+" + "(?:d2s:(" + $scope.queryParts.anyResourceName + ")|<" + $scope.queryParts.bases.d2s.pattern + "(" + $scope.queryParts.anyResourceName + ")>)", "g");
			array.length = 0; // empty selected values array
			var match;
			while (match = regexp.exec(section)) { // for each pattern match
				array.push(match[1] || match[2]); // add match to array, either the first or the second value (d2s:... or <...> vs <http://www.discover2share.net/d2s/ont/...)
			}
		};
		
		// finds all variables used in the query between the first opening bracket and the last closing bracket
		$scope.getAllVars = function () {
			$scope.allVars = [];
			var firstBracket = $scope.query.indexOf("{");
			var lastBracket = $scope.query.lastIndexOf("}");
			if (!~firstBracket || !~lastBracket) { // if no opening or no last bracket was found
				return; // abort
			}
			var section = $scope.query.substr(firstBracket, lastBracket - firstBracket); // section between the brackets
			var regexp = new RegExp("[ ]*(\\?[\\w]+)", "g"); // regexp to find variables
			var match;
			while (match = regexp.exec(section)) { // for each match
				if (!~$scope.allVars.indexOf(match[1])) { // check if current variable was not already added in previous iteration
					$scope.allVars.push(match[1]); // add to array
				}
			}
		};
		
		// replace the current SPARQL query by a basic one only querying for all platforms and their labels
		$scope.setBasicQuery = function () {
			$scope.query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX d2s: <http://www.discover2share.net/d2s-ont/>\nPREFIX dbpp: <http://dbpedia.org/property/>\n\nSelect * WHERE {\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n  	?platform rdfs:label ?platformLabel.\n} ORDER BY ?platformLabel";
		};
		
		// replace the SPARQL query by one that retrieves all dimension information for all platforms
		$scope.setAllDetailsQuery = function () {
			$scope.query = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/>\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX dbpp: <http://dbpedia.org/property/>\nPREFIX dbpo: <http://dbpedia.org/ontology/>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\n\nSelect ?platform ?label ?url ?resourceType ?consumerInvolvement ?launchCityName ?launchCountryName ?yearLaunch ?residenceCityName ?residenceCountryName ?marketMediation ?offering ?geographicScope ?moneyFlow ?pattern ?temporality ?consumerism ?resourceOwner ?serviceDurationMin ?serviceDurationMax ?app ?trustContribution ?typeOfAccessedObject WHERE {\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n    ?platform rdfs:label ?label.\n    ?platform dbpp:url ?url.\n    OPTIONAL {\n        ?platform d2s:has_resource_type ?rt.\n        ?rt rdfs:label ?resourceType.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_consumer_involvement ?ci.\n        ?ci rdfs:label ?consumerInvolvement.\n    }.\n    OPTIONAL {\n        ?platform d2s:launched_in ?launch.\n        OPTIONAL {\n            ?launch dbpp:locationCity ?launchCity.\n            ?launchCity rdfs:label ?launchCityName.\n        }.\n        OPTIONAL {\n            ?launch dbpp:locationCountry ?launchCountry.\n            ?launchCountry rdfs:label ?launchCountryName.\n        }.\n    }. \n    OPTIONAL {\n        ?platform dbpp:launchYear ?yearLaunch.\n    }.\n    OPTIONAL {\n        ?platform d2s:operator_resides_in ?residence.\n        OPTIONAL {\n            ?residence dbpp:locationCity ?residenceCity.\n            ?residenceCity rdfs:label ?residenceCityName.\n        }.\n        OPTIONAL {\n            ?residence dbpp:locationCountry ?residenceCountry.\n            ?residenceCountry rdfs:label ?residenceCountryName.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_market_mediation ?me.\n        ?me rdfs:label ?marketMediation.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_market_integration ?integration.\n        OPTIONAL {\n            ?integration d2s:markets_are ?of.\n            ?of rdfs:label ?offering.\n        }.\n        OPTIONAL {\n            ?integration d2s:has_scope ?sc.\n            ?sc rdfs:label ?geographicScope.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_money_flow ?mf.\n        ?mf rdfs:label ?moneyFlow.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_p2p_scc_pattern ?patternNode.\n        ?patternNode rdf:type ?pa.\n        ?pa rdfs:label ?pattern.\n        OPTIONAL {\n            ?patternNode d2s:has_temporality ?te.\n            ?te rdfs:label ?temporality.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:promotes ?co.\n        ?co rdfs:label ?consumerism.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_resource_owner ?ro.\n        ?ro rdfs:label ?resourceOwner.\n    }.\n    OPTIONAL {\n        ?platform d2s:min_service_duration ?serviceDurationMin.\n    }.\n    OPTIONAL {\n        ?platform d2s:max_service_duration ?serviceDurationMax.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_app ?ap.\n        ?ap rdfs:label ?app.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_trust_contribution ?tc.\n        ?tc rdfs:label ?trustContribution.\n    }.\n    OPTIONAL {\n        ?platform d2s:accessed_object_has_type ?ot.\n        ?ot rdfs:label ?typeOfAccessedObject.\n    }.\n} ORDER BY ?platform";
		};
		
		// inserts a platform variable as used in most other methods of this controller (?platform rdf:type d2s:P2P_SCC_Platform.) after the first opening bracket
		$scope.insertPlatformVar = function () {
			var firstBracket = $scope.query.indexOf("{");
			if (~firstBracket) { // if an opening bracket was found
				// insert platform variable behind that bracket
				$scope.query = $scope.query.substr(0, firstBracket + 1) + "\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n    ?platform rdfs:label ?platformLabel.\n" + $scope.query.substr(firstBracket + 1);
			} else { // otherwise append platform variable to the end of the query string
				$scope.query += "{\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n    ?platform rdfs:label ?platformLabel.\n}";
			}
		};

		$timeout(function () { // execute when the page is fully loaded
		    $scope.setFilterFromQuery();
		});
	});
	
})();