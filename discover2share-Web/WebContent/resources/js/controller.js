(function() {
	'use strict';

	var d2sApp = angular.module('d2sApp');
	
	d2sApp.controller('headerController', function($scope, $rootScope, $location, $cookieStore, authFactory){		
		// listener for route change
		$rootScope.$on('$locationChangeSuccess', function(event){
	        $scope.currentTab = $location.path().substr(1); // set scope variable to current route
	        //determine if logged in from cookie
	        $scope.loggedIn = authFactory.isAuthenticated();
	        if($scope.loggedIn)
	        	$scope.authRole = authFactory.getAuthData().authRole;
	        else
	        	$scope.authRole = undefined;
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
	
	d2sApp.controller('loginCtrl', function LoginCtrl($scope, $location, authFactory) {
		
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
	
	d2sApp.controller('registrationCtrl', function LoginCtrl($scope, $location, authFactory) {
		$scope.registerFailed = false;
		$scope.registerSuccess = false;
		
	    $scope.register = function () {
	    	if($scope.user.password === $scope.passwordConfirm){
		        authFactory.register($scope.user).success(function (data, status) {
		            $scope.registerFailed = !(status === 200);
		            $scope.registerSuccess = (status === 200);
		            $scope.user = {
		    				username: "",
		    				password: ""
		    		};
		            $scope.passwordConfirm = "";
		        }).error(function () {
		            $scope.registerFailed = true;
		            $scope.registerSuccess = false;
		        });
	    	}
	    };
	});

})();