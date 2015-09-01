(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp', ['ngRoute', 'ngCookies', 'ui.bootstrap', 'toaster']);
	
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
		var getLanguages = function (platformFactory) {
			return platformFactory.getLanguages();
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
		}).when('/platforms/edit/:platform', {
			controller: 'addEditPlatformCtrl',
			templateUrl: 'resources/html/addEditPlatform.html',
			resolve: {
				platform: getPlatform,
				languages: getLanguages
			},
			isSuggestion: false,
			isEdit: true
		}).when('/platforms/:platform', {
			controller: 'platformDetailCtrl',
			templateUrl: 'resources/html/platformDetails.html',
			resolve: {
				platform: getPlatform,
				languages: getLanguages
			},
			isSuggestion: false
		}).when('/suggestions', {
			controller: 'platformsCtrl',
			templateUrl: 'resources/html/suggestions.html',
			resolve: {
				platforms: getAllSuggestions
			}
		}).when('/suggestions/edit/:id', {
			controller: 'addEditPlatformCtrl',
			templateUrl: 'resources/html/addEditPlatform.html',
			resolve: {
				platform: getSuggestion,
				languages: getLanguages
			},
			isSuggestion: true,
			isEdit: true
		}).when('/suggestions/:id', {
			controller: 'platformDetailCtrl',
			templateUrl: 'resources/html/platformDetails.html',
			resolve: {
				platform: getSuggestion,
				languages: getLanguages
			},
			isSuggestion: true
		}).when('/add', {
			controller: 'addEditPlatformCtrl',
			templateUrl: 'resources/html/addEditPlatform.html',
			resolve: {
				platform: function () {},
				languages: getLanguages
			}
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
			redirectTo: '/'
		}).otherwise({ 
			templateUrl: 'resources/html/404.html'
		});
		
		$locationProvider.html5Mode({enabled: true});
		
	});

})();