'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
/**
 * @ngdoc function
 * @name appRouter
 * @description
 * 配置路由
 * 模块加载
 */
  .config(function ($stateProvider, $urlRouterProvider) {
	  $stateProvider
	  .state('pay.result', {
		  url: '/result/:subject',
		  templateUrl: 'views/pay/result.html',
      	  controller: 'PayResultCtrl'
	  }).state('pay.repeatpayerr', {
	      url: '/repeatpayerr',
          templateUrl: 'views/pay/repeatpayerr.html'
      }).state('pay.resourcenotexistserr', {
          url: '/resourcenotexistserr/:resourceType',
          templateUrl: 'views/pay/resourcenotexistserr.html',
          controller: 'ResourceNotExistsErrCtrl',
          controllerAs:"resourceNotExistsErr"
      });
  })
  .controller('PaidCtrl', ['$scope','$http','$state','$routeParams',
                               function ($scope, $http , $state, $routeParams) {
  }])
  .controller('PayResultCtrl', ['$scope','$http','$state','$routeParams',
                               function ($scope, $http , $state, $routeParams) {
	  $scope.subject = $state.params.subject;
  }])
  .controller("ConfirmPaidCtrl",['$scope','eayunModal', '$state','$modalInstance', '$window','backUrl',
                                  function ($scope, eayunModal, $state, $modalInstance, $window, backUrl){
	  $scope.confirmPaid = function(){
		  $modalInstance.close(true);
		  $state.go(backUrl);
	  };
	  
	  $scope.hasProblem = function(){
		  //新窗口打开FAQ页面
		  $window.open('http://www.eayun.com/document/faq/pay.html', '_blank');
	  };
	  
	  $scope.close = function(){
		  $modalInstance.close(true);
	  }
  }])
  .controller("ResourceNotExistsErrCtrl",['$stateParams','eayunModal', '$state',
                                           function ($stateParams, eayunModal, $state){
      var that = this;
      var resourceType = $stateParams.resourceType;
      if(resourceType == 0){
          that.resourceTypeName = "云主机";
      }else if(resourceType == 1){
          that.resourceTypeName = "云硬盘";
      }else if(resourceType == 2){
          that.resourceTypeName = "云硬盘备份";
      }else if(resourceType == 3){
          that.resourceTypeName = "私有网络";
      }else if(resourceType == 4){
          that.resourceTypeName = "负载均衡器";
      }else if(resourceType == 5){
          that.resourceTypeName = "弹性公网IP";
      }else if(resourceType == 6){
          that.resourceTypeName = "对象存储";
      }else if(resourceType == 7){
          that.resourceTypeName = "VPN";
      }else if(resourceType == 8){
          that.resourceTypeName = "云数据库";
      }
  }]);
