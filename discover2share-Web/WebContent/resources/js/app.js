var d2sApp = angular.module('d2sApp', []);

d2sApp.controller('indexCtrl', function ($scope, $http) {
  $scope.phones = [
    {'name': 'Nexus S',
     'snippet': 'Fast just got faster with Nexus S.'},
    {'name': 'Motorola XOOM™ with Wi-Fi',
     'snippet': 'The Next, Next Generation tablet.'},
    {'name': 'MOTOROLA XOOM™',
     'snippet': 'The Next, Next Generation tablet.'}
  ];
  
  $scope.larifari = "Hihihi";
  console.log("heyyy");
  //call to relative path
  $http.get('api/ind')
  	.success(function(data, status, headers, config) {
  		$scope.persons = data;
  	});
});