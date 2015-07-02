(function(){
	'use strict';

	var d2sApp = angular.module('d2sApp', ['ngRoute', 'ngCookies']);
	
	d2sApp.config(function($routeProvider, $locationProvider){
		
		$routeProvider.when('/', {
			controller: 'indexCtrl',
			templateUrl: 'resources/html/index.html'
		}).when('/login', {
			controller: 'LoginCtrl',
			templateUrl: 'resources/html/login.html'
		}).when('/logout', {
			templateUrl: 'resources/html/logout.html'
		}).when('/add', {
			templateUrl: 'resources/html/add.html'
		}).otherwise({ 
			templateUrl: 'resources/html/404.html'
		});
		
		$locationProvider.html5Mode({enabled: true});
		
	});

})();