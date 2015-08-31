(function () {
	'use strict';

	var d2sApp = angular.module('d2sApp');

	d2sApp.controller('indexCtrl', function ($scope, $http) {
		$http.get('api/ind/vip')
			.success(function (data, status, headers, config) {
				$scope.vipMessage = data + " " + status;
			}).error(function (data, status, headers, config) {
				$scope.vipMessage = data + " " + status;
			});

		var x = [ 52, 62, 98, 217, 227, 273, 313, 351, 355, 356, 358, 368, 376,
				377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 389, 390,
				397, 398, 399, 402, 404, 406, 407, 408, 411, 412, 413, 414,
				415, 416, 417, 418, 419, 421 ];
		x.forEach(function (i) {
			// $http.get('api/platforms/platform_' + i);
		});
	});
	
})();