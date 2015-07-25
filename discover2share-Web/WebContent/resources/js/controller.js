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
	});
	
	d2sApp.controller('loginCtrl', function($scope, $location, authFactory) {
		
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
	        authFactory.login($scope.user).success(function (data, status) {
	        	if(status !== 200){
	        		$scope.loginFailed = true;
	        	}else {
		            authFactory.setAuthData(data);
		            $scope.loginFailed = false;
		            $location.path("/");
	        	}
	        }).error(function () {
	            $scope.loginFailed = true;
	            $scope.loggedOut = false;
	        });
	    };
	});
	
	d2sApp.controller('registrationCtrl', function($scope, $location, authFactory) {
		$scope.isAdmin = (authFactory.isAuthenticated() && authFactory.getAuthData().authRole === "admin");
		$scope.user = {};
		$scope.registerFailed = false;
		$scope.registerSuccess = false;
		
	    $scope.register = function () {
	    	if($scope.user.password === $scope.user.passwordConfirm){
	    		delete $scope.user.passwordConfirm;
		        authFactory.register($scope.user).success(function (data, status) {
		            $scope.registerFailed = !(status === 200);
		            $scope.registerSuccess = (status === 200);
		            $scope.user = {};
		        }).error(function () {
		            $scope.registerFailed = true;
		            $scope.registerSuccess = false;
		        });
	    	}
	    };
	});
	
	d2sApp.controller('platformsCtrl', function($scope, platformFactory) {		
		$scope.pagination = {itemsPerPage: 40 };
		
		// retrieve all platforms from ontolgy
		platformFactory.getAll().success(function(data){
			$scope.filteredPlatforms = $scope.platforms = data;
			$scope.pagination.currentPage = 1; // Set pagination to first page. Causes update of the table display
		}).error(function(){});
		
		// triggered when the current page of pagination, the items per page or the filtered platform array change
		$scope.$watch("pagination.currentPage + pagination.itemsPerPage + filteredPlatforms", function() {
			$scope.pagination.begin = (($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage);
			$scope.pagination.end = $scope.pagination.begin + $scope.pagination.itemsPerPage;
			
			if(angular.isUndefined($scope.platforms)) return; //cancel function here on startup, to avoid errors
			
			//adjust end if it exceeds the actual numer of platforms
			if($scope.pagination.end > $scope.filteredPlatforms.length) $scope.pagination.end = $scope.filteredPlatforms.length;
			//slice the platforms for display according to currently selected page
			$scope.paginatedPlatforms = $scope.filteredPlatforms.slice($scope.pagination.begin, $scope.pagination.end);
		});
		
		// triggered when the search term input changes
		$scope.$watch("searchTerm", function(){
			if(angular.isUndefined($scope.platforms)) return; //cancel function here on startup, to avoid errors
			
			$scope.filteredPlatforms = []; //empty filtered list
			angular.forEach($scope.platforms, function(platform){
				if(~platform.label.toLowerCase().indexOf($scope.searchTerm)) //add those whose label contains the search term
					$scope.filteredPlatforms.push(platform);
			});
			$scope.pagination.currentPage = 1; //set pagination to first page
		});
	});
	
	d2sApp.controller('platformDetailCtrl', function($scope, platformFactory, $routeParams, $rootScope) {		
		platformFactory.getPlatform($routeParams.platform).success(function(data){
			$scope.platform = data;
			
			//retrieve names of cities and countries
			if(!angular.isUndefined(data.launchCity[0]))
				platformFactory.getGeoData(data.launchCity[0]).success(function(data){
					$scope.platform.launchCityName = data.toponymName;
					if($scope.platform.residenceCity[0] === $scope.platform.launchCity[0])
						$scope.platform.residenceCityName = data.toponymName;
				});
			if(!angular.isUndefined(data.launchCountry[0]))
				platformFactory.getGeoData(data.launchCountry[0]).success(function(data){
					$scope.platform.launchCountryName = data.countryName;
					if($scope.platform.residenceCountry[0] === $scope.platform.launchCountry[0])
						$scope.platform.residenceCountryName = data.countryName;
				});
			
			//when residence city or country equal launch city or country, save the extra calls
			if(data.residenceCity[0] !== data.launchCity[0] && !angular.isUndefined(data.residenceCity[0]))
				platformFactory.getGeoData(data.residenceCity[0]).success(function(data){
					$scope.platform.residenceCityName = data.toponymName;
				});
			if(data.residenceCountry[0] !== data.launchCountry[0] && !angular.isUndefined(data.residenceCountry[0]))
				platformFactory.getGeoData(data.residenceCountry[0]).success(function(data){
					$scope.platform.residenceCountryName = data.countryName;
				});
			
			//retrieve launchYear label
			if(!angular.isUndefined(data.launchYear[0])) 
				$scope.platform.launchYearName = data.launchYear[0].replace("http://dbpedia.org/resource/", "");		
		}).error(function(){});
				
		
		$scope.timeConverter = function(owlTime){
			switch(owlTime){
			case "http://www.w3.org/2006/time#unitMinute":
				return "Minutes";
			case "http://www.w3.org/2006/time#unitHour":
				return "Hours";
			case "http://www.w3.org/2006/time#unitDay":
				return "Days";
			case "http://www.w3.org/2006/time#unitWeek":
				return "Weeks";
			case "http://www.w3.org/2006/time#unitMonth":
				return "Months";
			}
			return owlTime;
		};
		
		//retrieve dimension comments and labels (only once)
		if(angular.isUndefined($rootScope.descriptions)) {
				platformFactory.getDescriptions().success(function(data){
					$rootScope.descriptions = data;
				});
		}
	});
	
	d2sApp.controller('addPlatformCtrl', function($scope, $rootScope, platformFactory){
		//retrieve dimension comments and labels (only once)
		if(angular.isUndefined($rootScope.descriptions)) {
				platformFactory.getDescriptions().success(function(data){
					$rootScope.descriptions = data;
				});
		}
		if(angular.isUndefined($rootScope.countries)){
			platformFactory.getCountries().success(function(data){
				$rootScope.countries = data.countries;
			});
		}
		
		$scope.platform = { launchCountryItem: {}};
		
		$scope.launchCitySelected = function(item){
			$scope.platform.launchCityName = item.toponymName;
			$scope.platform.launchCityItem = item;
			$scope.platform.launchCity = "http://www.geonames.org/" + item.geonameId;
			if(!angular.isUndefined($scope.platform.launchCountryItem) && 
					$scope.platform.launchCityItem.countryCode !== $scope.platform.launchCountryItem.countryCode)
				console.log("Selected City is not situated in the selected country.");
		}
		$scope.launchCountrySelected = function(item){
			$scope.platform.launchCountryName = item.countryName;
			$scope.platform.launchCountryItem = item
			$scope.platform.launchCountry = "http://www.geonames.org/" + item.countryId;
			if(!angular.isUndefined($scope.platform.launchCityItem) && 
					$scope.platform.launchCityItem.countryCode !== $scope.platform.launchCountryItem.countryCode)
				console.log("Selected City is not situated in the selected country.");
		}
		// triggered when the search term input changes
		$scope.findLaunchCity = function(){
			if($scope.platform.launchCityName === ""){
				$scope.platform.launchCity = "";
				return;
			}
			return platformFactory.findCity($scope.platform.launchCityName, $scope.platform.launchCountryItem.countryCode).then(function(response){
				return response.data.geonames;
			});
		};
		$scope.findLaunchCountry = function(){
			if($scope.platform.launchCountryName === ""){
				$scope.platform.launchCountry = "";
				return;
			}
			return platformFactory.findCountry($scope.platform.launchCountryName).then(function(response){
				return response.data.geonames;
			});
		};
	});

})();