(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/*
	 * Source: http://eric.sau.pe/angularjs-detect-enter-key-ngenter/
	 */
	d2sApp.directive('cuEnter', function () {
		return function (scope, element, attrs) {
			element.bind("keydown keypress", function (event) {
				var key = typeof event.which === "undefined" ? event.keyCode : event.which;
				if (key === 13) {
					scope.$apply(function () {
						scope.$eval(attrs.cuEnter);
					});
					event.preventDefault();
				}
			});
		};
	});
	
	d2sApp.directive('cuFocuslast', function () {
		return function (scope, element, attrs) {
			if (scope.$last && !scope.$first) {
				element[0].focus();
			}
		};
	});
	
	d2sApp.directive('cuYasgui', function () {
		return function (scope, element, attrs) {
			var config = scope.$eval(attrs.cuYasgui);
			if (!angular.isUndefined(YASQE)) {
				scope.yasqe = YASQE(element[0], config.yasqe);
			}
			if (!angular.isUndefined(YASR)) {
				config.yasr.getUsedPrefixes = scope.yasqe.getPrefixesFromQuery;
				scope.yasr = YASR(element[0], config.yasr);
			}
			scope.yasqe.options.sparql.callbacks.complete = scope.yasr.setResponse;
		};
	});

})();