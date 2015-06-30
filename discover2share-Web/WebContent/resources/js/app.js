(function(){
	'use strict';

	var d2sApp = angular.module('d2sApp', ['ngRoute']);
	
	d2sApp.config(function($routeProvider, $locationProvider){
		
		$routeProvider.when('/', {
			controller: 'indexCtrl',
			templateUrl: 'resources/html/index.html'
		}).when('/add', {
			controller: 'indexCtrl',
			templateUrl: 'resources/html/add.html'
		}).otherwise({ 
			templateUrl: 'resources/html/404.html'
		});
		
		$locationProvider.html5Mode({enabled: true});
		
	});

})();