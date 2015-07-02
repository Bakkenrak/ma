(function() {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	d2sApp.controller('headerController', function($scope, $rootScope, $location){		
		/** listener for route change. sets scope variable to current route */
		$rootScope.$on('$locationChangeSuccess', function(event){
	        $scope.currentTab = $location.path().substr(1);
		});
	});

	d2sApp.controller('indexCtrl', function($scope, $http) {
		$scope.phones = [ {
			'name' : 'Nexus S',
			'snippet' : 'Fast just got faster with Nexus S.'
		}, {
			'name' : 'Motorola XOOM™ with Wi-Fi',
			'snippet' : 'The Next, Next Generation tablet.'
		}, {
			'name' : 'MOTOROLA XOOM™',
			'snippet' : 'The Next, Next Generation tablet.'
		} ];

		$scope.larifari = "Hihihi";
		console.log("heyyy");
		// call to relative path
		$http.get('api/ind').success(function(data, status, headers, config) {
			$scope.persons = data;
		});
	});

})();