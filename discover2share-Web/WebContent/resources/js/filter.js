(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	d2sApp.filter('countryContainsFilter', function () {
	    return function (items, searchText) {
			var filtered = [];
			angular.forEach(items, function (item) {
				if (item.countryCode === searchText.toUpperCase()) {
					filtered.push(item);
				} else if (~item.countryName.toLowerCase().indexOf(searchText.toLowerCase())) {
					filtered.push(item);
				} else if (~item.resourceName.toLowerCase().indexOf(searchText.toLowerCase())) {
					filtered.push(item);
				}
			});
			return filtered;
		};
	});
	
	d2sApp.filter('resourceContainsFilter', function () {
	    return function (items, searchText) {
			var filtered = [];
			angular.forEach(items, function (item) {
				if (~item.name.toLowerCase().indexOf(searchText.toLowerCase())) {
					filtered.push(item);
				} else if (~item.resourceName.toLowerCase().indexOf(searchText.toLowerCase())) {
					filtered.push(item);
				}
			});
			return filtered;
		};
	});
	
})();