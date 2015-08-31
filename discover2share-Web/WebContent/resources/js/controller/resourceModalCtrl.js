(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
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
	
})();