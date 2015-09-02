(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	/**
	 * Controller providing functionality for the resource type section of the addEdit form.
	 */
	d2sApp.controller('resourceTypeCtrl', function ($scope, $rootScope, $timeout, platformFactory, $modal, toaster) {
		if (angular.isUndefined($rootScope.resourceTypes)) { // if resource types weren't retrieved in this app before
			platformFactory.getResourceTypes().success(function (data, status) { // do so
				if (status >= 400) { // error
					toaster.pop('error', 'Code ' + status, 'There was an error retrieving available resource types from the ontology.');
				}
				$rootScope.resourceTypes = data; // save in root scope
			});
		}
		
		// $scope.addOption method is inherited from parent scope (addPlatformCtrl)
		
		/**
		 * Opens the modal to add external concepts to the given resource type.
		 * 
		 * @param resourceType
		 *			The resource type to add external concepts for
		 * @param size
		 *			The desired size of the modal
		 */
		$scope.openModal = function (resourceType, size) {
			var modalInstance = $modal.open({ // open modal
				animation: true,
				templateUrl: 'myModalContent.html',
				controller: 'resourceModalCtrl',
				size: size,
				resolve: {
					resourceType: function () {
						return resourceType; // pass resource type
					}
				}
			});
	
			modalInstance.result.then(function (selected) { // called when the modal closes
				resourceType.externals = selected; // set concepts selected in model to resource type object
			});
		};
	});
	
})();