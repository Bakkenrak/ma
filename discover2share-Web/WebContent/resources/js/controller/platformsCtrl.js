(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	/**
	 * Controller that provides functionality for the platforms or suggestions overview views.
	 */
	d2sApp.controller('platformsCtrl', function ($scope, $location, $filter, authFactory, toaster, platforms) {
		if (platforms.status === 401) { // if user is unauthorized
			toaster.pop("error", "Unauthorized!", "You need to be logged in to access the information on that page.");
			authFactory.logout(); // remove auth data cookie
			$location.path("login/"); // redirect to login page
		}
		
		if (platforms.data) { // if the platforms data was resolved by the route provider
			platforms.data.forEach(function (platform) { // for each platform
				// order its resource types by label and join their labels into one comma-separated string
				platform.resourceTypesJoined = $filter('orderBy')(platform.resourceTypes, 'label')
													.map(function (item) {
														return item.label;
													}).join(", ");
			});
		}
		
		$scope.pagination = { // pagination configuration
			itemsPerPage : 15 // show 25 platforms per page
		};
		$scope.searchTerm = "";
		
		$scope.orderBy = { // ordering configuration
			attribute: 'label', // attribute to order the list of platforms by
			reverse: false // initially ascending
		};
		
		/**
		 * Reorders the list of platform objects by the given attribute.
		 * 
		 * @param attribute
		 *			The attribute to order the list by
		 */
		$scope.changeOrderBy = function (attribute) {
			if (attribute === $scope.orderBy.attribute) { // if list was already ordered by that attribute
				$scope.orderBy.reverse = !$scope.orderBy.reverse; // reverse the order
			} else { 
				$scope.orderBy.attribute = attribute;
				$scope.orderBy.reverse = false; // initially ascending
			}
			// reorder list
			$scope.filteredPlatforms = $filter('orderBy')($scope.platforms, $scope.orderBy.attribute, $scope.orderBy.reverse);
			$scope.doPagination(); // apply pagination again
		};

		// triggered when the current page of pagination, the
		// items per page or the filtered platform array change
		$scope.$watch("pagination.currentPage + pagination.itemsPerPage + filteredPlatforms", function () {
			$scope.doPagination();
		});
		
		/**
		 * Slices the list of all filtered platforms according to the current pagination settings.
		 */
		$scope.doPagination = function () {
			$scope.pagination.begin = (($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage);
			$scope.pagination.end = $scope.pagination.begin	+ $scope.pagination.itemsPerPage;
			// adjust end if it exceeds the actual numer of platforms
			if ($scope.pagination.end > $scope.filteredPlatforms.length) {
				$scope.pagination.end = $scope.filteredPlatforms.length;
			}
			// slice the platforms for display according to currently selected page
			$scope.paginatedPlatforms = $scope.filteredPlatforms.slice($scope.pagination.begin, $scope.pagination.end);
		};


		/**
		 * Filters the list of all platform objects to those whose label or resource types contain the entered search term.
		 * Triggered when the search term input changes.
		 */
		$scope.$watch("searchTerm",	function () {
			$scope.filteredPlatforms = []; // empty filtered list
			angular.forEach($scope.platforms, function (platform) {
				var searchTermLower = $scope.searchTerm.toLowerCase();
				// add those whose label or resource types contain the search term
				if ((platform.label !== null && ~platform.label.toLowerCase().indexOf(searchTermLower)) || 
						(platform.resourceTypesJoined !== null && ~platform.resourceTypesJoined.toLowerCase().indexOf(searchTermLower))) {
					$scope.filteredPlatforms.push(platform);
				}
			});
			$scope.pagination.currentPage = 1; // set pagination to first page
		});
		
		// initial ordering of the platforms by the intial order config
		$scope.filteredPlatforms = $scope.platforms = $filter('orderBy')(platforms.data, $scope.orderBy.attribute, $scope.orderBy.reverse);
	});
	
})();