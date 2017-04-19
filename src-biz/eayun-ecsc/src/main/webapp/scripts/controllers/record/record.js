'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$urlRouterProvider.when('/app/record', '/app/record/main');
			$urlRouterProvider.when('/app/record/main', '/app/record/main/apply');
			$stateProvider.state("app.record.main", {
				url : '/main',
				templateUrl : 'views/record/main.html'
			}).state("app.record.main.question", {
				url : '/question',
				templateUrl : 'views/record/question.html',
				controller : 'questionCtrl'
			});
		}).controller('RecordCtrl', function ($scope ,$state, eayunHttp , cloudprojectList,$window,eayunModal,$rootScope) {
			$scope.cloudprojectList = cloudprojectList;
		      //当前位置的导航栏
		      $rootScope.navList = function(list,cucrentName,type){
		    	  $scope.navLists = list;
		    	  $scope.cucrentName = cucrentName;
		    	  $scope.type = type;
		      };
		}).controller('questionCtrl', function ($scope ,$state,$rootScope,$location, $anchorScroll,eayunHttp) {
			var list=[];
			$rootScope.navList(list,'常见问题');
			$scope.gotoQuestion = function(question) {
				$location.hash(question);
				$anchorScroll();
			};
			$scope.gettestmongo = function() {
				eayunHttp.post('ecsc/record/gettestmongo.do',{}).then(function(response){
					console.info(response.data);
			    });
			};
		});

