(function () {
	'use strict';
	
	var d2sApp = angular.module("d2sApp");
	
	d2sApp.controller('queryCtrl', function ($scope, $rootScope, $http, platformFactory) {
		if (angular.isUndefined($rootScope.countries)) {
			platformFactory.getCountries().success(function (data) {
				$rootScope.countries = data.countries;
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
		
		$scope.toggleSelection = function (current, selected) {
			var idx = selected.indexOf(current);
			if (~idx) { // is currently selected
				selected.splice(idx, 1); // remove selection
			} else { // is newly selected
				selected.push(current); //add current selection to array
			}
		};
		
		$scope.filter = {
				marketMediations: [],
				consumerisms: [],
				apps: [],
				trustContributions: []
			};
		
		$scope.yasqeConfig = {
				value: "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX dbpp: <http://dbpedia.org/property/>\nPREFIX d2s: <http://www.discover2share.net/d2s-ont/>\n\nSELECT * WHERE {\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n}",
				sparql: {
					showQueryButton: true,
					endpoint: "api/ontology/query"
				}
			};
		
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
			if (!prefixMatch) {
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
				
				// limit
				if (newValue.limit !== oldValue.limit) {
					var lastBracket = $scope.query.lastIndexOf("}");
					if (~lastBracket) {
						var endPart = $scope.query.substr(lastBracket).replace(new RegExp("([ ]+LIMIT[ ]*[0-9]*)", "i"), "");
						if (newValue.limit) {
							endPart += " LIMIT " + newValue.limit;
						}
						$scope.query = $scope.query.substr(0, lastBracket) + endPart;
					}
				}
			}
			
			$scope.computedQuery = $scope.query;
		};
		
		$scope.arrayFilter = function (newValue, oldValue, queryPart) {
			if (angular.equals(newValue, oldValue)) {
				return;
			}
			// remove
			var platformVar = $scope.platformVar || $scope.getPlatformVar();
			
			var regexp = new RegExp("([ ]{0,4}\\" + platformVar.name + "[ ]+" + queryPart + "[ ]+" + "(?:d2s:" + $scope.queryParts.anyResourceName + "|<" + $scope.queryParts.d2sBase + $scope.queryParts.anyResourceName + ">)" + $scope.queryParts.trailingRemovableChars + ")", "g");
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			var queryLength = $scope.query.length;
			$scope.query = $scope.query.substr(0, platformVar.openingBracket) + section.replace(regexp, "") + $scope.query.substr(platformVar.closingBracket);
			$scope.platformVar.closingBracket -= queryLength - $scope.query.length;
			
			// add anew
			newValue.forEach(function (item) {
				$scope.simplePattern(item, undefined, queryPart); // oldValue as undefined to avoid unnecessary removal pattern matching
			});
		};
		
		$scope.p2pSccPatternFilter = function (newValue, oldValue) {
			// pattern
			$scope.intermediatePattern(newValue.p2pSccPattern, oldValue.p2pSccPattern, $scope.queryParts.p2pSccPattern, "rdf:type", "?pattern");
			// geographic scope
			$scope.intermediatePattern(newValue.temporality, oldValue.temporality, $scope.queryParts.p2pSccPattern, $scope.queryParts.temporality, "?pattern");
			// remove launch node statement if neither city nor country are requested
			if ((newValue.p2pSccPattern !== oldValue.p2pSccPattern || newValue.temporality !== oldValue.temporality) &&
					!$scope.filter.p2pSccPattern && !$scope.filter.temporality) {
				$scope.removeLocationNode($scope.queryParts.p2pSccPattern);
			}
		};
		
		$scope.integrationFilter = function (newValue, oldValue) {
			// market offering
			$scope.intermediatePattern(newValue.marketOffering, oldValue.marketOffering, $scope.queryParts.integration, $scope.queryParts.marketOffering, "?integration");
			// geographic scope
			$scope.intermediatePattern(newValue.geographicScope, oldValue.geographicScope, $scope.queryParts.integration, $scope.queryParts.geographicScope, "?integration");
			// remove launch node statement if neither city nor country are requested
			if ((newValue.marketOffering !== oldValue.marketOffering || newValue.geographicScope !== oldValue.geographicScope) &&
					!$scope.filter.marketOffering && !$scope.filter.geographicScope) {
				$scope.removeLocationNode($scope.queryParts.integration);
			}
		};
		
		$scope.locationFilter = function (newValue, oldValue) {	
			// launch country
			$scope.intermediatePattern(newValue.countryLaunch, oldValue.countryLaunch, $scope.queryParts.launch, $scope.queryParts.country, "?launchLocation");
			// launch city
			$scope.intermediatePattern(newValue.cityLaunch, oldValue.cityLaunch, $scope.queryParts.launch, $scope.queryParts.city, "?launchLocation");
			// remove launch node statement if neither city nor country are requested
			if ((newValue.cityLaunch !== oldValue.cityLaunch || newValue.countryLaunch !== oldValue.countryLaunch) &&
					!$scope.filter.cityLaunch && !$scope.filter.countryLaunch) {
				$scope.removeLocationNode($scope.queryParts.launch);
			}
			
			// residence country
			$scope.intermediatePattern(newValue.countryResidence, oldValue.countryResidence, $scope.queryParts.residence, $scope.queryParts.country, "?residenceLocation");
			// residence city
			$scope.intermediatePattern(newValue.cityResidence, oldValue.cityResidence, $scope.queryParts.residence, $scope.queryParts.city, "?residenceLocation");
			if ((newValue.cityResidence !== oldValue.cityResidence || newValue.countryResidence !== oldValue.countryResidence) &&
					!$scope.filter.cityResidence && !$scope.filter.countryResidence) {
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
			// remove old occurrence
			if (oldVal) {
				regexp = "([ ]{0,4}\\" + firstVar + "[ ]+" + queryPart + "[ ]+";
				if (isCustomUrl) {
					regexp += "<" + oldVal + ">";
				} else {
					regexp += "(?:d2s:" + oldVal + "|<" + $scope.queryParts.d2sBase + oldVal + ">)";
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
				regexp = "(\\" + firstVar + "[ ]+" + queryPart + "[ ]+";
				if (isCustomUrl) {
					regexp += "<" + newVal + ">)";
				} else {
					regexp += "(?:d2s:" + newVal + "|<" + $scope.queryParts.d2sBase + newVal + ">))";
				}
				var match = section.match(new RegExp(regexp, "")); // check if an expression matching the new value already exists
				if (!match) { //otherwise add it
					var queryLength = $scope.query.length;
					var queryEnd = $scope.query.substr(platformVar.closingBracket);
					$scope.query = $scope.query.substr(0, platformVar.closingBracket) + "    " + firstVar + " " + queryPart;
					if (isCustomUrl) {
						$scope.query += " <" + newVal + ">";
					} else {
						$scope.query += " d2s:" + newVal;
					}
					$scope.query += ".\n" + queryEnd;
					$scope.platformVar.closingBracket += ($scope.query.length - queryLength); //adjust closing bracket pointer
				}
			}
		};
		
		$scope.intermediatePattern = function (newVal, oldVal, queryPartIntermediate, queryPartDetail, locationVar) {
			if (newVal === oldVal) {
				return;
			}
			var platformVar = $scope.platformVar || $scope.getPlatformVar(); // take platform variable from scope. Compute anew if not yet done
			var locVar = $scope.getIntermediateVar(platformVar, queryPartIntermediate);
			locationVar = locVar || locationVar;
			if (newVal && !locVar) {
				var queryLength = $scope.query.length;
				$scope.query = $scope.query.substr(0, platformVar.closingBracket) + "    " + platformVar.name + " " + queryPartIntermediate + " " + locationVar + ".\n" + $scope.query.substr(platformVar.closingBracket);
				$scope.platformVar.closingBracket += ($scope.query.length - queryLength); //adjust closing bracket pointer
			}
			
			$scope.simplePattern(newVal, oldVal, queryPartDetail, locationVar);
		};
		
		$scope.getIntermediateVar = function (platformVar, queryPartLocation) {
			// Cut out the query part in which the platform variable resides. All operations will be done in this section.
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			
			var regexp;
			// find/insert location variable
			regexp = new RegExp("\\" + platformVar.name + "[ ]+" + queryPartLocation + "[ ]+(\\?[\\w]+)", "");
			var match = regexp.exec(section);
			return match ? match[1] : null;
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
			var locationVar = $scope.getIntermediateVar(platformVar, $scope.queryParts.launch);
			if (locationVar) {
				// city
				$scope.filter.cityLaunch = $scope.findPattern($scope.queryParts.city, locationVar);
				// country
				$scope.filter.countryLaunch = $scope.findPattern($scope.queryParts.country, locationVar);
			}
			
			// residence
			locationVar = $scope.getIntermediateVar(platformVar, $scope.queryParts.residence);
			if (locationVar) {
				// city
				$scope.filter.cityResidence = $scope.findPattern($scope.queryParts.city, locationVar);
				// country
				$scope.filter.countryResidence = $scope.findPattern($scope.queryParts.country, locationVar);
			}
			
			// year
			$scope.filter.yearLaunch = $scope.findPattern($scope.queryParts.yearLaunch);
			
			// language
			regexp = new RegExp("\\" + platformVar.name + "[ ]+" + $scope.queryParts.language + "[ ]+" + "(?:d2s:(" + $scope.queryParts.anyResourceName + ")|<" + $scope.queryParts.d2sBase + "(" + $scope.queryParts.anyResourceName + ")>)", "g");
			count = 0;
			while (match = regexp.exec(section)) {
				$scope.filter.language = match[1] || match[2];
				count++;
			}
			if (count === 0) {
				$scope.filter.language = "";
			}
			
			// market mediation
			$scope.findPatternArray($scope.queryParts.marketMediation, $scope.filter.marketMediations);
			
			// integration
			var integrationVar = $scope.getIntermediateVar(platformVar, $scope.queryParts.integration);
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
			$scope.filter.serviceDurationMin = $scope.findPattern($scope.queryParts.serviceDurationMin, undefined, true);
			// service duration max
			$scope.filter.serviceDurationMax = $scope.findPattern($scope.queryParts.serviceDurationMax, undefined, true);
			
			// smartphone apps
			$scope.findPatternArray($scope.queryParts.app, $scope.filter.apps);
			
			// trust contribution
			$scope.findPatternArray($scope.queryParts.trustContribution, $scope.filter.trustContributions);
			
			// type of accessed object
			$scope.filter.typeOfAccessedObject = $scope.findPattern($scope.queryParts.typeOfAccessedObject);
			
			
			// limit
			match = new RegExp("LIMIT[ ]*([0-9]*)", "i").exec($scope.query.substr($scope.query.lastIndexOf("}")));
			if (match && !isNaN(match[1])) {
				$scope.filter.limit = parseInt(match[1]);
			}
			
			$scope.userChange = true; // signal that changes were invoked by the user to avoid an unnecessary run of the filter method
			// force filter watch event for those cases where it would not have been triggered because no changes were made here
			// this sets back $scope.userChange and thus avoids aborting the filter watch method when it is next rightfully executed
			$scope.filter.trigger = !$scope.filter.trigger; 
		});
		
		$scope.findPattern = function (queryPart, firstVar, isCustomUrl) {
			var platformVar = $scope.getPlatformVar(); // find platform variable
			// copy query section in which the platform variable resides
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			firstVar = firstVar || platformVar.name;
			var regexp = "\\" + firstVar + "[ ]+" + queryPart + "[ ]+";
			if (isCustomUrl) {
				regexp += "<(.+?)>";
			} else {
				regexp += "(?:d2s:(" + $scope.queryParts.anyResourceName + ")|<" + $scope.queryParts.d2sBase + "(" + $scope.queryParts.anyResourceName + ")>)";
			}
			var match = new RegExp(regexp, "").exec(section);
			return match ? match[1] || match[2] : "";
		};
		
		$scope.findPatternArray = function (queryPart, array) {
			var platformVar = $scope.getPlatformVar(); // find platform variable
			// copy query section in which the platform variable resides
			var section = $scope.query.substr(platformVar.openingBracket, platformVar.closingBracket - platformVar.openingBracket);
			
			var regexp = new RegExp("\\" + platformVar.name + "[ ]+" + queryPart + "[ ]+" + "(?:d2s:(" + $scope.queryParts.anyResourceName + ")|<" + $scope.queryParts.d2sBase + "(" + $scope.queryParts.anyResourceName + ")>)", "g");
			array.length = 0;
			var match;
			while (match = regexp.exec(section)) {
				array.push(match[1] || match[2]);
			}
		};
		
		$scope.setAllDetailsQuery = function () {
			$scope.query = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/>\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX dbpp: <http://dbpedia.org/property/>\nPREFIX dbpo: <http://dbpedia.org/ontology/>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\n\nSelect ?platform ?label ?url ?resourceType ?consumerInvolvement ?launchCityName ?launchCountryName ?yearLaunch ?residenceCityName ?residenceCountryName ?marketMediation ?offering ?geographicScope ?moneyFlow ?pattern ?temporality ?consumerism ?resourceOwner ?serviceDurationMin ?serviceDurationMax ?app ?trustContribution ?typeOfAccessedObject WHERE {\n    ?platform rdf:type d2s:P2P_SCC_Platform.\n    ?platform rdfs:label ?label.\n    ?platform dbpp:url ?url.\n    OPTIONAL {\n        ?platform d2s:has_resource_type ?rt.\n        ?rt rdfs:label ?resourceType.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_consumer_involvement ?ci.\n        ?ci rdfs:label ?consumerInvolvement.\n    }.\n    OPTIONAL {\n        ?platform d2s:launched_in ?launch.\n        OPTIONAL {\n            ?launch dbpp:locationCity ?launchCity.\n            ?launchCity rdfs:label ?launchCityName.\n        }.\n        OPTIONAL {\n            ?launch dbpp:locationCountry ?launchCountry.\n            ?launchCountry rdfs:label ?launchCountryName.\n        }.\n    }. \n    OPTIONAL {\n        ?platform dbpp:launchYear ?yearLaunch.\n    }.\n    OPTIONAL {\n        ?platform d2s:operator_resides_in ?residence.\n        OPTIONAL {\n            ?residence dbpp:locationCity ?residenceCity.\n            ?residenceCity rdfs:label ?residenceCityName.\n        }.\n        OPTIONAL {\n            ?residence dbpp:locationCountry ?residenceCountry.\n            ?residenceCountry rdfs:label ?residenceCountryName.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_market_mediation ?me.\n        ?me rdfs:label ?marketMediation.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_market_integration ?integration.\n        OPTIONAL {\n            ?integration d2s:markets_are ?of.\n            ?of rdfs:label ?offering.\n        }.\n        OPTIONAL {\n            ?integration d2s:has_scope ?sc.\n            ?sc rdfs:label ?geographicScope.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_money_flow ?mf.\n        ?mf rdfs:label ?moneyFlow.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_p2p_scc_pattern ?patternNode.\n        ?patternNode rdf:type ?pa.\n        ?pa rdfs:label ?pattern.\n        OPTIONAL {\n            ?patternNode d2s:has_temporality ?te.\n            ?te rdfs:label ?temporality.\n        }.\n    }.\n    OPTIONAL {\n        ?platform d2s:promotes ?co.\n        ?co rdfs:label ?consumerism.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_resource_owner ?ro.\n        ?ro rdfs:label ?resourceOwner.\n    }.\n    OPTIONAL {\n        ?platform d2s:min_service_duration ?serviceDurationMin.\n    }.\n    OPTIONAL {\n        ?platform d2s:max_service_duration ?serviceDurationMax.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_app ?ap.\n        ?ap rdfs:label ?app.\n    }.\n    OPTIONAL {\n        ?platform d2s:has_trust_contribution ?tc.\n        ?tc rdfs:label ?trustContribution.\n    }.\n    OPTIONAL {\n        ?platform d2s:accessed_object_has_type ?ot.\n        ?ot rdfs:label ?typeOfAccessedObject.\n    }.\n} ORDER BY ?platform";
		};
	});
	
})();