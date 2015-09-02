(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('platformsCtrl', function ($scope, $location, $filter, authFactory, toaster, platforms) {
		if (platforms.status === 401) {
			toaster.pop("error", "Unauthorized!", "You need to be logged in to access the information on that page.");
			authFactory.logout();
			$location.path("login/");
		}
		
		if (platforms.data) {
			platforms.data.forEach(function (platform) {		
				platform.resourceTypesJoined = $filter('orderBy')(platform.resourceTypes, 'label')
													.map(function (item) {
														return item.label;
													}).join(", ");
			});
		}
		
		$scope.pagination = {
			itemsPerPage : 25
		};
		$scope.searchTerm = "";
		
		$scope.orderBy = {
			attribute: 'label',
			reverse: false
		};
		
		$scope.changeOrderBy = function (attribute) {
			if (attribute === $scope.orderBy.attribute) {
				$scope.orderBy.reverse = !$scope.orderBy.reverse;
			} else {
				$scope.orderBy.attribute = attribute;
				$scope.orderBy.reverse = false;
			}
			$scope.filteredPlatforms = $filter('orderBy')($scope.platforms, $scope.orderBy.attribute, $scope.orderBy.reverse);
			$scope.doPagination();
		};

		// triggered when the current page of pagination, the
		// items per page or the filtered platform array change
		$scope.$watch("pagination.currentPage + pagination.itemsPerPage + filteredPlatforms", function () {
			$scope.doPagination();
		});
		
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

		// triggered when the search term input changes
		$scope.$watch("searchTerm",	function () {
			$scope.filteredPlatforms = []; // empty filtered list
			angular.forEach($scope.platforms, function (platform) {
				var searchTermLower = $scope.searchTerm.toLowerCase();
				// add those whose label or resource types contain the search term
				if ((platform.label !== null	&& ~platform.label.toLowerCase().indexOf(searchTermLower)) || 
						(platform.resourceTypesJoined !== null && ~platform.resourceTypesJoined.toLowerCase().indexOf(searchTermLower))) {
					$scope.filteredPlatforms.push(platform);
				}
			});
			$scope.pagination.currentPage = 1; // set pagination to first page
		});

		$scope.filteredPlatforms = $scope.platforms = $filter('orderBy')(platforms.data, $scope.orderBy.attribute, $scope.orderBy.reverse);
	});
	
})();