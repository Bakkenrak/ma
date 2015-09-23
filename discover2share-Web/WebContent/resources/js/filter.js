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
	
	/**
	 * Replaces common namespace strings by their prefix representation in the given string.
	 */
	d2sApp.filter('nameFilter', function () {
		return function (item) {
			item = item.replace("http://www.discover2share.net/d2s-ont/", "d2s:");
			item = item.replace("http://www.w3.org/2002/07/owl#", "owl:");
			item = item.replace("http://linkedgeodata.org/ontology/", "lgd:");
			item = item.replace("http://www.w3.org/2001/XMLSchema#", "xsd:");
			item = item.replace("http://www.w3.org/2004/02/skos/core#", "skos:");
			item = item.replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
			item = item.replace("http://dbpedia.org/resource/", "dbpr:");
			item = item.replace("http://dbpedia.org/property/", "dbpp:");
			item = item.replace("http://dbpedia.org/ontology/", "dbpo:");
			item = item.replace("http://purl.org/dc/terms/", "dct:");
			item = item.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
			item = item.replace("http://wordnet-rdf.princeton.edu/wn31/", "wordnet:");
			item = item.replace("http://www.w3.org/2006/time#", "time:");
			
			return item;
		};
	});
	
	/**
	 * Replaces the D2S namespace string by a local path to the respective resource detail view.
	 */
	d2sApp.filter('linkFilter', function () {
		return function (item) {
			return item.replace("http://www.discover2share.net/d2s-ont/", "ontology/resource/");
		};
	});
	
})();