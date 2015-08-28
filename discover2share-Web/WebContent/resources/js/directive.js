(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Executes the given attribute statement when the Enter key is pressed on the respective elemnt.
	 * Source: http://eric.sau.pe/angularjs-detect-enter-key-ngenter/
	 */
	d2sApp.directive('cuEnter', function () {
		return function (scope, element, attrs) {
			element.bind("keydown keypress", function (event) {
				var key = typeof event.which === "undefined" ? event.keyCode : event.which; // browser compatibility
				if (key === 13) { // enter keycode is 13
					scope.$apply(function () {
						scope.$eval(attrs.cuEnter); // run given attribute statement in scope
					});
					event.preventDefault(); // prevent default action on enter key press
				}
			});
		};
	});
	
	/**
	 * Puts input focus on respective element if it is the last and not first in a list of objects
	 */
	d2sApp.directive('cuFocuslast', function () {
		return function (scope, element, attrs) {
			if (scope.$last && !scope.$first) {
				element[0].focus();
			}
		};
	});
	
	/**
	 * Creates a YASQE (http://yasqe.yasgui.org) instance on the selected element.
	 * Two-way binds the query value to a ng-model value if given.
	 */
	d2sApp.directive('cuYasqe', function () {
		function init(scope, element, attrs, ngModel) {
			if (angular.isUndefined(YASQE)) {
				throw new Error('YASQE library required.');
			}
			scope.yasqe = YASQE(element[0], scope.$eval(attrs.cuYasqe)); // instantiate YASQE
			configNgModelLink(ngModel, scope);
		}
		
		function configNgModelLink(ngModel, scope) {
			if (!ngModel) {
				return;
			}
			scope.$evalAsync(function () { // set query loaded by YASQE from cache to ngModel
				ngModel.$setViewValue(scope.yasqe.getValue());
			});
			
			// YASQE expects a string, so make sure it gets one.
			// This does not change the model.
			ngModel.$formatters.push(function (value) {
				if (angular.isUndefined(value) || value === null) {
					return '';
				} else if (angular.isObject(value) || angular.isArray(value)) {
					throw new Error('YASQE cannot use an object or an array as a model');
				}
				return value;
			});
			// Override the ngModelController $render method, which is what gets called when the model is updated.
			// This takes care of the synchronizing the YASQE element with the underlying model, in the case that it is changed by something else.
			ngModel.$render = function () {
				// YASQE expects a string so make sure it gets one
				// Although the formatter have already done this, it can be possible that another formatter returns undefined (for example the required directive)
				var safeViewValue = ngModel.$viewValue || '';
				scope.yasqe.setValue(safeViewValue);
			};
			// Keep the ngModel in sync with changes from YASQE
			scope.yasqe.on('change', function (instance) {
				var newValue = instance.getValue();
				if (newValue !== ngModel.$viewValue) {
					scope.$evalAsync(function () {
						ngModel.$setViewValue(newValue);
					});
				}
			});
		}
		
		return { 
			require: "?ngModel",
			compile: function compile() {
				return init;
			}
		};
	});
	
	/**
	 * Creates a YASR (http://yasr.yasgui.org) instance on the selected element.
	 * Binds the result set to the ng-model value if given.
	 * Links to a YASQE instance if existing in the scope.
	 */
	d2sApp.directive('cuYasr', function (toaster) {
		function init(scope, element, attrs, ngModel) {
			if (angular.isUndefined(YASR)) {
				throw new Error('YASR library required.');
			}			
			
			var config = scope.$eval(attrs.cuYasr);
			if (!config) { // if no config object is provided
				config = {}; // initialize empty object
			}
			if (scope.getPlatformVar) { // if the scope offers the getPlatformVar method
				var originalGetCell = YASR.plugins.table.defaults.getCellContent; // original YASR cell drawing method
				YASR.plugins.table.defaults.getCellContent = function (yasr, plugin, bindings, sparqlVar, context) { // overwrite cell drawing
					var newCell = originalGetCell(yasr, plugin, bindings, sparqlVar, context); // run original method
					if (scope.queryPlatformVar && "?" + sparqlVar === scope.queryPlatformVar.name) { // if current cell contains a value for the platform var
						var urlStart = newCell.indexOf("href") + 6;
						newCell = newCell.substr(0, urlStart) + "platforms" + newCell.substr(urlStart + 37); // adjust link to platform's detail page
					}
					return newCell;
				};
			}
			
			if (!angular.isUndefined(scope.yasqe)) { // if YASQE object exists in current scope, link them up
				config.getUsedPrefixes = scope.yasqe.getPrefixesFromQuery; // take prefixes
		
				scope.yasr = YASR(element[0], config); // initialize YASR
				
				scope.yasqe.options.sparql.callbacks.complete = function (response) { // when YASQE completes SPARQL query
					scope.queryPlatformVar = scope.platformVar || scope.getPlatformVar(); // save platform variable used at query time
					
					if (response.status >= 400) { // if an error occured
						toaster.pop('error', 'Code ' + response.status, 'There was an error processing this SPARQL query: ' + response.responseJSON.error);
						
						if (ngModel) { // reset ngModel if supplied for this directive
							scope.$evalAsync(function () {
								ngModel.$setViewValue(null);
							});
						}
					} else {
						scope.yasr.setResponse(response.responseJSON); // set response to YASR (linking of YASR and YASQE)
						
						if (ngModel) { // if an ng-model attribute is provided in the view
							// Keep the ngModel in sync with changes in the result set
							var newValue = response.responseJSON;
							if (newValue !== ngModel.$viewValue) {
								scope.$evalAsync(function () {
									ngModel.$setViewValue(newValue);
								});
							}
						}
					}
				};
			} else {
				scope.yasr = YASR(element[0], config); // initialize YASR
			}
		}			
		return { 
			require: "?ngModel",
			compile: function compile() {
				return init;
			}
		};
	});
	
	d2sApp.directive('cuOverlay', function ($timeout) {
		return function (scope, element, attrs) {
			if (attrs.cuOverlay) {				
				var dim = element[0].getBoundingClientRect();
				var overlay = angular.element(document.createElement("div"));
				element.append(overlay);
				$timeout(function() { // timeout to await the possible rendering of the browser scrollbar
					overlay.css({
						position: "absolute",
						top: element[0].offsetTop + "px",
						left: element[0].offsetLeft + "px",
						width: element[0].offsetWidth + "px",
						height: element[0].offsetHeight + "px",
						display: scope.$eval(attrs.cuOverlay) ? "none" : "block"
					});
				});
				overlay.addClass("overlay");
				
				angular.element(window).on("resize", function () {
					overlay[0].style.top = element[0].offsetTop + "px";
					overlay[0].style.left = element[0].offsetLeft + "px";
					overlay[0].style.width = element[0].offsetWidth + "px";
					overlay[0].style.height = element[0].offsetHeight + "px";
				});
				
				scope.$watch(attrs.cuOverlay, function (newValue, oldValue) {
					if(!newValue && newValue !== oldValue) {
						overlay[0].style.display = "block";
					} else {
						overlay[0].style.display = "none";
					}
				});
			} else {
				throw new Error("Please provide a value for the cu-overlay directive.");
			}
		};
	});

})();