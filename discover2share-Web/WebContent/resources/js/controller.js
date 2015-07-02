(function() {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	d2sApp.controller('headerController', function($scope, $rootScope, $location, $cookieStore, authFactory){		
		// listener for route change
		$rootScope.$on('$locationChangeSuccess', function(event){
	        $scope.currentTab = $location.path().substr(1); // set scope variable to current route
	        //determine if logged in from cookie
	        $scope.loggedIn = authFactory.isAuthenticated();
		});
		
		$scope.logout = function(){
			authFactory.logout();
		}
	});

	d2sApp.controller('indexCtrl', function($scope, $http) {
		
		$http.get('api/ind/vip').success(function(data, status, headers, config) {
			$scope.vipMessage = data + " " + status;
		})
		.error(function(data, status, headers, config){
			$scope.vipMessage = data + " " + status;
		});

		$http.get('api/ind').success(function(data, status, headers, config) {
			$scope.persons = data;
		});
	});
	
	d2sApp.controller('LoginCtrl', function LoginCtrl($scope, $location, authFactory) {
		
		$scope.loggedOut = false;
		
		if($location.path().substr(1) === "logout"){
			authFactory.logout();
			$scope.loggedOut = true;
		}
		
		$scope.user = {
				username: "",
				password: ""
		};
		
		$scope.loginFailed = false;
		
	    $scope.login = function () {
	        authFactory.login($scope.user).success(function (data) {
	            authFactory.setAuthData(data);
	            $scope.loginFailed = false;
	            $location.path("/");
	        }).error(function () {
	            $scope.loginFailed = true;
	            $scope.loggedOut = false;
	        });
	    };
	});

})();