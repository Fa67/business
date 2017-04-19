'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state("app.record.website", {
				url : '/website',
				templateUrl : 'views/record/website/main.html',
				controller : 'websiteCtrl'
			});
		}).controller('websiteCtrl', function ($scope) {
			
			
			
			
		});

