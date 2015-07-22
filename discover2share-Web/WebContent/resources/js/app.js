(function(){
	'use strict';

	var d2sApp = angular.module('d2sApp', ['ngRoute', 'ngCookies', 'ui.bootstrap']);
	
	d2sApp.config(function($routeProvider, $locationProvider){
		
		$routeProvider.when('/', {
			controller: 'indexCtrl',
			templateUrl: 'resources/html/index.html'
		}).when('/platforms', {
			controller: 'platformsCtrl',
			templateUrl: 'resources/html/platforms.html'
		}).when('/platforms/:platform', {
			controller: 'platformDetailCtrl',
			templateUrl: 'resources/html/platformDetails.html'
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