(function(){
	'use strict';

	var d2sApp = angular.module('d2sApp', ['ngRoute', 'ngCookies', 'ui.bootstrap']);
	
	d2sApp.config(function($routeProvider, $locationProvider){
		
		var getAllPlatforms = function(platformFactory){
			return platformFactory.getAllPlatforms();
		};
		var getAllSuggestions = function(platformFactory){
			return platformFactory.getAllSuggestions();
		}
		
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
			templateUrl: 'resources/html/platformDetails.html'
		}).when('/suggestions', {
			controller: 'platformsCtrl',
			templateUrl: 'resources/html/suggestions.html',
			resolve: {
				platforms: getAllSuggestions
			}
		}).when('/register', {
			controller: 'registrationCtrl',
			templateUrl: 'resources/html/register.html'
		}).when('/login', {
			controller: 'loginCtrl',
			templateUrl: 'resources/html/login.html'
		}).when('/logout', {
			templateUrl: 'resources/html/logout.html'
		}).when('/add', {
			controller: 'addPlatformCtrl',
			templateUrl: 'resources/html/addPlatform.html'
		}).otherwise({ 
			templateUrl: 'resources/html/404.html'
		});
		
		$locationProvider.html5Mode({enabled: true});
		
	});

})();