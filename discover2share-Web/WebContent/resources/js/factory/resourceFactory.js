(function () {
	'use strict';
	
	var d2sApp = angular.module('d2sApp');
	
	d2sApp.factory('resourceFactory', function ($http) {
		var resourceFactory = {};
		
		resourceFactory.dbpedia = function (searchTerm) {
			var query = "http://lookup.dbpedia.org/api/search/KeywordSearch?MaxHits=100&QueryString=" + searchTerm;
			return $http.get(query);
		};
		
		return resourceFactory;
	});
	
})();