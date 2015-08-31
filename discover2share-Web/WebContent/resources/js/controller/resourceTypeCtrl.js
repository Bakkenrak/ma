(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	d2sApp.controller('resourceTypeCtrl', function ($scope, $rootScope, $timeout, platformFactory, $modal, toaster) {
		if (angular.isUndefined($rootScope.resourceTypes)) { // if resource types weren't retrieved in this app before
			platformFactory.getResourceTypes().success(function (data, status) {
				if (status >= 400) {
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving available resource types from the ontology.');
				}
				$rootScope.resourceTypes = data;
			});
		}
		
		// $scope.addOption method is inherited from parent scope (addPlatformCtrl)
		
		$scope.openModal = function (resourceType, size) {
			var modalInstance = $modal.open({
				animation: true,
				templateUrl: 'myModalContent.html',
				controller: 'resourceModalCtrl',
				size: size,
				resolve: {
					resourceType: function () {
						return resourceType;
					}
				}
			});
	
			modalInstance.result.then(function (selected) {
				resourceType.externals = selected;
			});
		};
	});
	
})();