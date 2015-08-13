(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp', ['ngRoute', 'ngCookies', 'ui.bootstrap']);
	
	d2sApp.config(function ($routeProvider, $locationProvider) {
		
		var getAllPlatforms = function (platformFactory) {
			return platformFactory.getAllPlatforms();
		};
		var getAllSuggestions = function (platformFactory) {
			return platformFactory.getAllSuggestions();
		};
		var getPlatform = function ($route, platformFactory) {
			return platformFactory.getPlatform($route.current.params.platform);
		};
		var getSuggestion = function ($route, platformFactory) {
			return platformFactory.getSuggestion($route.current.params.id);
		};
		
		$routeProvider.when('/', {
			controller: 'indexCtrl',
			templateUrl: 'resources/html/index.html'
		}).when('/platforms', {
			controller: 'platformsCtrl',
			templateUrl: 'resources/html/platforms.html',
			resolve: {
				platforms: getAllPlatforms
			}
		}).when('/platforms/:platform', {
			controller: 'platformDetailCtrl',
			templateUrl: 'resources/html/platformDetails.html',
			resolve: {
				platform: getPlatform
			}
		}).when('/suggestions', {
			controller: 'platformsCtrl',
			templateUrl: 'resources/html/suggestions.html',
			resolve: {
				platforms: getAllSuggestions
			}
		}).when('/suggestions/:id', {
			controller: 'platformDetailCtrl',
			templateUrl: 'resources/html/platformDetails.html',
			resolve: {
				platform: getSuggestion
			}
		}).when('/add', {
			controller: 'addPlatformCtrl',
			templateUrl: 'resources/html/addPlatform.html'
		}).when('/query', {
			controller: 'queryCtrl',
			templateUrl: 'resources/html/query.html'
		}).when('/register', {
			controller: 'registrationCtrl',
			templateUrl: 'resources/html/register.html'
		}).when('/login', {
			controller: 'loginCtrl',
			templateUrl: 'resources/html/login.html'
		}).when('/logout', {
			templateUrl: 'resources/html/logout.html'
		}).otherwise({ 
			templateUrl: 'resources/html/404.html'
		});
		
		$locationProvider.html5Mode({enabled: true});
		
	});

})();