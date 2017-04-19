'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称； requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular
		.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$urlRouterProvider.when('/app/message', '/app/message/message');
			$urlRouterProvider.when('/app/message', '/app/message/msgDetail');
			$stateProvider.state('app.message.message', {
				url : '/message',
				templateUrl : 'views/message/messagemng.html',

				controller : 'MessageListCtrl'
			}).state("app.message.msgDetail", {
				url : '/msgDetail/:id',
				templateUrl : 'views/message/detailmsg.html',
				controller : 'msgDetailCtrl',
				controllerAs : 'model'

			});
		})
		// .filter('trustHtml',function($sce){
		// return function (input) {
		// return $sce.trustAsHtml(input);
		// }
		// })

		.controller(
				"msgDetailCtrl",
				function($scope, $stateParams, eayunHttp, eayunModal,$rootScope) {
					var list=[{route:'app.message.message',name:'消息中心'}];
					$rootScope.navList(list,'消息详情','detail');
					
					
					var vm = this;
					$scope.news = {};
					eayunHttp.post('sys/news/getbyid.do', {
						id : $stateParams.id
					}).then(
							function(response) {

								vm.data = response.data;
								if (vm.data.statu == 0) {
									return eayunHttp.post('sys/news/statu.do',
											vm.data).then(function(data) {
//												
												  	 
													  eayunHttp.post('sys/news/unreadCount.do',{"userAccount":$scope.user.userName}).then(function(response){
														  $scope.unreadCount = response.data;
													  });
													  eayunHttp.post('sys/news/getUnreadList.do').then(function(response){
														  $scope.unreadList = response.data.unreadList;
													  });
													  $scope.$emit("RefreshUnreadMsgCount");
												  
										return $scope.news=data.data;
									});
								}

							});
					
					 

				})
				
				.controller(
				"msgDetailCtrlone",
				function($scope, eayunHttp, eayunModal,newsId) {
					var vm = this;
				
					vm.news = {};
					
					eayunHttp.post('sys/news/getbyid.do', {
						id : newsId
						
					}).then(
							function(response) {

								vm.data = response.data;
								if (vm.data.statu == 0) {
									return eayunHttp.post('sys/news/statu.do',
											vm.data).then(function(data) {
										return vm.news=data.data;
									});
								}

							});

				})
				
		/**
		 * @ngdoc function
		 * @name eayunApp.controller:CloudhostCtrl
		 * @description # MessagCtrl
		 */
		.controller('MessagCtrl', function($scope,$rootScope) {
			  $rootScope.navList = function(list,cucrentName,type){
		    	  $scope.navLists = list;
		    	  $scope.cucrentName = cucrentName;
		    	  $scope.type = type;
		      };
			
			$scope.tabs = [ {
				title : '消息中心',
				target : 'app.message.message'
			} ];
		})
		.controller(
				'MessageListCtrl',
				function($scope, $state, $stateParams, eayunModal, eayunHttp,$rootScope) {
					
					var list=[];
					$rootScope.navList(list,'消息中心');
					
					// 默认加载页面用到的参数默认设置为无条件查询
					$scope.data = {
						newsTitle : '',
						beginTime : '',
						endTime : ''

					};
					
					
					// HTML页面model值双向绑定到getParams统一传到后台Java代码Controller中
					$scope.myTable = {
						source : 'sys/news/getNewsList.do',
						api : {},
						getParams : function() {
							return {
								newsTitle : $scope.data.newsTitle,
								beginTime : $scope.data.beginTime ? $scope.data.beginTime
										.getTime()
										: '',
								endTime : $scope.data.endTime ? $scope.data.endTime
										.getTime() + 86400000
										: '',
								isCollect : $scope.data.isCollect ? $scope.data.isCollect
										: ''
							};
						}
					};

					// 收藏显示
					$scope.check = function(data) {
						if (data.$$selected == undefined) {
							data.$$selected = true;
						}

						if (data.$$selected) {
							data.$$selected = false;
							$scope.data.isCollect = '1';
							$scope.myTable.api.draw();

						} else if (!data.$$selected) {
							data.$$selected = true;
							$scope.data.isCollect = '';
							$scope.myTable.api.draw();

						}

					};

					// 查询
					$scope.queryNewsList = function() {
						$scope.myTable.api.draw();
					};
					// 收藏
					$scope.collect = function(model) {
						eayunHttp.post("sys/news/collect.do", model).then(
								function() {
									$scope.myTable.api.refresh();
								});
					};
					// 取消收藏
					$scope.uncollect = function(model) {
						eayunHttp.post('sys/news/uncollect.do', model).then(
								function() {
									$scope.myTable.api.refresh();
								});
					};
					// 查看详情
					$scope.openMsgDetail = function(data) {
						
						$scope.news = data;
						$state.go('app.message.msgDetail', {
							id : $scope.news.id
						}); // 跳转后的URL;

					};
					// 消息页面下通过上方导览页读取未读消息后，列表本页刷新
					$scope.$on("RefreshTitleUnreadMsgCount", function(event) {
						$scope.myTable.api.refresh();
					});
					
				})
/**
 * 
 */
;

