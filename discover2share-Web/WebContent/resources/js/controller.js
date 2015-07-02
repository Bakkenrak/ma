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
		
		$http.get('api/ind/vip').success(function(data, status, headers, config) {
			$scope.vipMessage = data + " " + status;
		})
		.error(function(data, status, headers, config){
			$scope.vipMessage = data + " " + status;
		});

		$scope.larifari = "Hihihi";
		console.log("heyyy");
		// call to relative path
		$http.get('api/ind').success(function(data, status, headers, config) {
			$scope.persons = data;
		});
	});
	
	d2sApp.controller('LoginCtrl', ['$scope', 'authFactory', function LoginCtrl($scope, authFactory) {
		$scope.user = {
				username: "",
				password: ""
		};
		
	    $scope.login = function () {
	        authFactory.login($scope.user).success(function (data) {
	            authFactory.setAuthData(data);
	            // Redirect etc.
	        }).error(function () {
	            // Error handling
	        });
	    };
	}]);
	
	

})();