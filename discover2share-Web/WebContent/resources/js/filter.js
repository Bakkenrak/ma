(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	d2sApp.filter('containsFilter', [function () {
	    return function (items, searchText) {
			var filtered = [];
			angular.forEach(items, function (item) {
				if (item.countryCode === searchText.toUpperCase()) {
					filtered.push(item);
				} else if (~item.countryName.toLowerCase().indexOf(searchText.toLowerCase())) {
					filtered.push(item);
				}
			});
			return filtered;
		};
	}]);
	
})();