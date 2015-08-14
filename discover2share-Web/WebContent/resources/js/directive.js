(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/*
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
	
	/*
	 * Puts input focus on respective element if it is the last and not first in a list of objects
	 */
	d2sApp.directive('cuFocuslast', function () {
		return function (scope, element, attrs) {
			if (scope.$last && !scope.$first) {
				element[0].focus();
			}
		};
	});
	
	d2sApp.directive('cuYasqe', function () {
		function postLink(scope, element, attrs, ngModel) {
			if (angular.isUndefined(YASQE)) {
				throw new Error('YASQE library required.');
			}
			scope.yasqe = YASQE(element[0], scope.$eval(attrs.cuYasqe));
			scope.query = scope.yasqe.getValue();
						
			configNgModelLink(ngModel, scope);
		}
		
		function configNgModelLink(ngModel, scope) {
			if (!ngModel) {
				return;
			}
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
				return postLink;
			}
		};
	});
	
	d2sApp.directive('cuYasr', function () {
		return function (scope, element, attrs) {
			if (angular.isUndefined(YASR)) {
				throw new Error('YASR library required.');
			}
			var config = scope.$eval(attrs.cuYasr);
			if (angular.isUndefined(config) || config === null) {
				config = {};
			}
			if (!angular.isUndefined(scope.yasqe)) {
				config.getUsedPrefixes = scope.yasqe.getPrefixesFromQuery;
			}
			scope.yasr = YASR(element[0], config);
			if (!angular.isUndefined(scope.yasqe)) {
				scope.yasqe.options.sparql.callbacks.complete = scope.yasr.setResponse;
			}
		};
	});

})();