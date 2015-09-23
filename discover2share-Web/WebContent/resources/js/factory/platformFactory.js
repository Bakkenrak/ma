(function () {
	'use strict';
	
	var d2sApp = angular.module('d2sApp');
	
	/**
	 * Provides methods to call perform API calls regarding platform data.
	 */
	d2sApp.factory('platformFactory', function ($http) {
		var platformFactory = {};

		platformFactory.getCountries = function () {
			return $http.get('resources/js/countries.json'); // retrieve JSON file
		};
		
		platformFactory.getCities = function () {
			return $http.get('api/ontology/cities'); // GET call to API
		};
		
		platformFactory.getLanguages = function () {
			return $http.get('resources/js/languages.json');
		};
		
		platformFactory.getResourceTypes = function () {
			return $http.get('api/ontology/resourceTypes');
		};
				
		platformFactory.getAllPlatforms = function () {
			return $http.get('api/ontology/platforms');
		};
		
		platformFactory.getPlatform = function (platform) {
			return $http.get('api/ontology/platforms/' + platform);
		};
		
		platformFactory.removePlatform = function (id) {
			return $http.delete('api/ontology/platforms/' + id);
		};
		
		platformFactory.getDescriptions = function () {
			return $http.get('api/ontology/descriptions');
		};
		
		platformFactory.getAllSuggestions = function () {
			return $http.get('api/ontology/platforms/suggestions/');
		};
		
		platformFactory.getSuggestion = function (id) {
			return $http.get('api/ontology/platforms/suggestions/' + id);
		};
		
		platformFactory.getSuggestionExternal = function (id) {
			return $http.get('api/ontology/external/' + id);
		};
		
		platformFactory.addPlatformSuggestion = function (platform) {
			return $http.post('api/ontology/platforms/suggestions/add', platform);
		};
		
		platformFactory.directSavePlatformSuggestion = function (platform) {
			return $http.post('api/ontology/platforms/suggestions/directSave', platform);
		};
		
		platformFactory.savePlatformSuggestion = function (id) {
			return $http.get('api/ontology/platforms/suggestions/save/' + id);
		};
		
		platformFactory.removeSuggestion = function (id) {
			return $http.delete('api/ontology/platforms/suggestions/' + id);
		};
		
		platformFactory.editPlatformSuggestion = function (platform) {
			return $http.post('api/ontology/platforms/suggestions/edit', platform);
		};
		
		platformFactory.editPlatformSuggestionExternal = function (id, platform) {
			return $http.post('api/ontology/external/' + id, platform);
		};
		
		platformFactory.getGeoData = function (geoUrl) {
			var geoId = geoUrl.replace("http://www.geonames.org/", ""); // remove base to get the ID only
			return $http.jsonp('http://api.geonames.org/getJSON?username=discover2share&geonameId=' + geoId + "&callback=JSON_CALLBACK");
		};
		
		platformFactory.findCity = function (cityName, countryCode) {
			var query = "http://api.geonames.org/searchJSON?username=discover2share&maxRows=10&featureClass=P&q=" + encodeURIComponent(cityName);
			if (countryCode) { // if a country code is provided
				query = query + "&country=" + countryCode; // add it to the query string
			}
			return $http.jsonp(query + "&callback=JSON_CALLBACK");
		};
		
		platformFactory.findCountry = function (countryName) {
			var query = "http://api.geonames.org/searchJSON?username=discover2share&maxRows=10&featureClass=A&isNameRequired&q=" + encodeURIComponent(countryName);
			return $http.get(query);
		};
		
		platformFactory.getResourceDetails = function (name) {
			return $http.get('api/ontology/resource/' + name);
		};
		
		return platformFactory;
	});
	
})();