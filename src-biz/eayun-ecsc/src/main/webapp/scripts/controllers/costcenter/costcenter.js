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
	/** 多标签时默认路径*/
//  $urlRouterProvider.when('/app/costcenter', '/app/costcenter/account');
  	/**定义路由，加载的页面放入上一级路由(app.auth)所加载的页面的data-ui-view里面*/
    $stateProvider
    .state('app.costcenter.detail',{
    	url: '/detail/:detailType',
        templateUrl: 'views/costcenter/detail.html',
        controller:'CostCenterDetailController'
    })
  })
  
  .controller('CostCenterDetailController',function ($scope ,eayunHttp ,eayunModal,$stateParams,$state,$timeout){
	  if($stateParams.detailType=='postpay'){
		  $scope.route = 'app.costcenter.report.postpay.detail';
		  $scope.name = '后付费';
	  }
	  else if($stateParams.detailType=='prepayment'){
		  $scope.route = 'app.costcenter.report.prepayment.detail';
		  $scope.name = '预付费';
	  }
  })
  
  .controller('CostCenterCtrl',['eayunStorage','$scope',function (eayunStorage,$scope){
	  $scope.navLists=[{route:'app.costcenter.guidebar.account',name:'费用中心'}];
	  eayunStorage.set('navLists',$scope.navLists);
  }]);