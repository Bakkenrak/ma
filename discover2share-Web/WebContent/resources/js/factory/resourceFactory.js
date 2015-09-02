(function () {
	'use strict';
	
	var d2sApp = angular.module('d2sApp');
	
	/**
	 * Provides methods to lookup external data regarding resource types.
	 */
	d2sApp.factory('resourceFactory', function ($http) {
		var resourceFactory = {};
		
		/**
		 * Query the DBPedia REST API with the given search term to find adequate resources.
		 * 
		 * @param searchTerm
		 *			Term to search resources by on DBpedia.
		 * @return AJAX call promise to retrieve the response data from
		 */
		resourceFactory.dbpedia = function (searchTerm) {
			var query = "http://lookup.dbpedia.org/api/search/KeywordSearch?MaxHits=100&QueryString=" + searchTerm;
			return $http.get(query);
		};
		
		return resourceFactory;
	});
	
})();