(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	/**
	 * Filters out of the given array of elements those whose country code equals or name or resource name contain the given search text.
	 */
	d2sApp.filter('countryContainsFilter', function () {
	    return function (items, searchText) {
			var filtered = [];
			angular.forEach(items, function (item) { // for each item in the array
				if (item.countryCode === searchText.toUpperCase()) { // if country code equals the search text
					filtered.push(item); // add to output
				} else if (~item.countryName.toLowerCase().indexOf(searchText.toLowerCase())) { // if country name contains the search text
					filtered.push(item);
				} else if (~item.resourceName.toLowerCase().indexOf(searchText.toLowerCase())) { // if resource name contains the search text
					filtered.push(item);
				}
			});
			return filtered;
		};
	});
	
	/**
	 * Filters out of the given array of elements those whose name or resource name contain the given search text
	 */
	d2sApp.filter('resourceContainsFilter', function () {
	    return function (items, searchText) {
			var filtered = [];
			angular.forEach(items, function (item) { // for each item in the array
				if (~item.name.toLowerCase().indexOf(searchText.toLowerCase())) { // if name contains the search text
					filtered.push(item); // add to output
				} else if (~item.resourceName.toLowerCase().indexOf(searchText.toLowerCase())) { // if resource name contains the search text
					filtered.push(item);
				}
			});
			return filtered;
		};
	});
	
})();