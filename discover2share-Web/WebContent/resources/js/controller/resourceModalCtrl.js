(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	/**
	 * Controller providing functionality for the resource type modal to add external concepts to a resource type in the addEdit form.
	 */
	d2sApp.controller('resourceModalCtrl', function ($scope, $modalInstance, $timeout, resourceFactory, toaster, resourceType) {
		$scope.dbpediaResources = [];
		// if external concepts have been selected previously, save them in scope, otherwise initiate empty array
		$scope.selected = (resourceType.externals && resourceType.externals.length > 0) ? resourceType.externals : [];
		$scope.searchTerm = resourceType.label; // do first search for concepts using the resource type's label

		/**
		 * Closes the model. Passes the array of selected external concepts to the modal instantiating controller.
		 */
		$scope.close = function () {
			$modalInstance.close($scope.selected);
		};
		
		/**
		 * @return Whether the resource type to which external concepts are supposed to be added here is actually a new resource type.
		 */
		$scope.checkNew = function (label) {
			if (!label) { // if the label is empty or undefined
				return false; // not new
			}
			var labelLower = label.toLowerCase();
			var isNew = true;
			$scope.resourceTypes.forEach(function (type) { // compare label with that of every resource type in the ontology
				if (type.name.toLowerCase() === labelLower || type.resourceName.toLowerCase() === labelLower) {
					isNew = false; // is not new if already existing in the ontology
				}
			});
			return isNew;
		};
		
		$scope.isNew = $scope.checkNew(resourceType.label); // check if new right on startup
		
		var _timeout;
		/**
		 * Calls the resource factory method to search for DBpedia resources by the current search term.
		 * If not otherwise passed per parameter, the lookup will be executed with a delay to await the end of the user's typing.
		 * 
		 * @param value
		 *			The search term
		 * @param delay
		 *			The delay after which to execute the search
		 */
		$scope.retrieveExternal = function (value, delay) {
			if (isNaN(delay) || delay < 0) {
				delay = 500;
			}
			if (_timeout) { //if there is already a timeout in process cancel it
				$timeout.cancel(_timeout);
			}
			if (value) {
				_timeout = $timeout(function () { // save timeout in variable to enable its cancellation
					resourceFactory.dbpedia(value).success(function (data, status) { // do lookup
						if (status >= 400) { // error
							toaster.pop('error', 'Code ' + status, 'There was an error retrieving resources from DBPedia.');
						}
						$scope.dbpediaResources.length = 0; // reset length of results array
						if (data && data.results) { // success
							$scope.dbpediaResources = data.results; // save results to scope
						}
					});
				    _timeout = null;
				}, delay); // call with a delay
			} else { // empty search term
				$scope.dbpediaResources.length = 0;
			}
		};

		$scope.retrieveExternal($scope.searchTerm, 0); // on modal startup do direct retrieval of concepts
		
		/**
		 * Adds a concept to the list of selected concepts if it is not already in there.
		 * 
		 * @param resource
		 *			The concept to add to the list
		 */
		$scope.addConcept = function (resource) {
			var duplicate = false;
			$scope.selected.forEach(function (item) { // for each already selected concept
				if (angular.equals(item, resource)) { // compare
					duplicate = true; // if equal -> duplicate
				}
			});
			if (!duplicate) {
				$scope.selected.push(resource); // add concept to the list of selected
			}
		};
		
		/**
		 * Extracts the relevant information from the given DBpedia resource object to then add it to the list of selected concepts.
		 * 
		 * @param resource
		 *			DBpedia resource object to add to the list of selected
		 */
		$scope.addDbpediaConcept = function (resource) {
			$scope.addConcept({
				label: resource.label,
				resource: resource.uri,
				description: resource.description
			});
		};
		
		/**
		 * Adds the given uri, entered by the user, to the list of selected concepts.
		 * 
		 * @param uri
		 *			The URI to add to the list of selected concepts
		 */
		$scope.addCustomConcept = function (uri) {
			if (uri) { // if not empty
				$scope.addConcept({ // turn into object and add it
					resource: uri,
					description: ""
				});
			}
		};
	});
	
})();