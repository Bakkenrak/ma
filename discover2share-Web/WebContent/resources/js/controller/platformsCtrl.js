(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('platformsCtrl', function ($scope, $location, toaster, platforms) {
		if (platforms.status === 401) {
			toaster.pop("error", "Unauthorized!", "You need to be logged in to access the information on that page.");
			$location.path("login/");
		}
		
		$scope.pagination = {
			itemsPerPage : 25
		};
		$scope.searchTerm = "";

		// triggered when the current page of pagination, the
		// items per page or the filtered platform array change
		$scope.$watch("pagination.currentPage + pagination.itemsPerPage + filteredPlatforms", function () {
			$scope.pagination.begin = (($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage);
			$scope.pagination.end = $scope.pagination.begin	+ $scope.pagination.itemsPerPage;
			// adjust end if it exceeds the actual numer of platforms
			if ($scope.pagination.end > $scope.filteredPlatforms.length) {
				$scope.pagination.end = $scope.filteredPlatforms.length;
			}
			// slice the platforms for display according to currently selected page
			$scope.paginatedPlatforms = $scope.filteredPlatforms.slice($scope.pagination.begin, $scope.pagination.end);
		});

		// triggered when the search term input changes
		$scope.$watch("searchTerm",	function () {
			$scope.filteredPlatforms = []; // empty filtered list
			angular.forEach($scope.platforms, function (platform) {
				// add those whose label contains the search term
				if (platform.label !== null	&& ~platform.label.toLowerCase().indexOf($scope.searchTerm)) {
					$scope.filteredPlatforms.push(platform);
				}
			});
			$scope.pagination.currentPage = 1; // set pagination to first page
		});

		$scope.filteredPlatforms = $scope.platforms = platforms.data;
	});
	
})();