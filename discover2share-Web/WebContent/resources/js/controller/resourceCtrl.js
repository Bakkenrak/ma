(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Controller for the ontology/resource/ route that retrieves the triple details for the D2S resource with the given name.
	 */
	d2sApp.controller('resourceCtrl', function ($scope, $route, platformFactory, toaster) {
		$scope.name = $route.current.params.name;
		
		platformFactory.getResourceDetails($scope.name).success(function (data, status) { // retrieve details
			if (status >= 400) { // if error
				toaster.pop("error", "Error " + status, "There was an error retrieving details for the resource d2s:" + $scope.name + ".");
			} else {
				$scope.details = data;
			}
		});
		
		/**
		 * Checks whether the object has any own properties (i.e. not inherited ones)
		 */
		$scope.checkEmpty = function (obj) {
			for (var key in obj) {
				if (obj.hasOwnProperty(key)) {
					return false;
				}
			}
			return true;
		};
	});
	
})();