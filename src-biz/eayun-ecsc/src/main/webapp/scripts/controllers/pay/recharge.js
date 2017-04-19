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
  	/**定义路由，加载的页面放入上一级路由(app.auth)所加载的页面的data-ui-view里面*/
    $stateProvider
    .state('pay.recharge', {
      url: '/recharge',
      templateUrl: 'views/pay/recharge/main.html',
      controller: 'RechargeCtrl'
    })
    .state('pay.recharge.redirect', {
      url: '/redirect',
      templateUrl: 'pay/recharge/rechargeBalance.do',
      params: {}
    });
  })
  
/**
 * 充值controller
 */
  .controller('RechargeCtrl', ['$scope','eayunModal','eayunHttp','$http','$state','$stateParams', 'eayunStorage',
                               function ($scope, eayunModal,eayunHttp, $http , $state, $stateParams, eayunStorage) {
	  var defaultNavList = [{route:'app.costcenter',name:'账户总览'}];
	  var storeNavList = eayunStorage.persist("rechargeNavList");
      $scope.navList = storeNavList == undefined ? defaultNavList : storeNavList;
	  //查询余额
	  $http.post('costcenter/accountoverview/getaccountbalance.do', $scope.model).then( function (response) {
		  $scope.model.balance = response.data.data.money;
	  });
	  
	 $scope.commit = function(){
		 var result = eayunModal.open({
		        templateUrl: 'views/pay/confirm.html',
		        controller: 'ConfirmPaidCtrl',
		        backdrop: 'static',
		        resolve: {
		        	backUrl : function(){
		        		return $scope.navList[0].route;
		        	}
		        }
		  }).result;
	 };
	//验证充值金额是否大于0
	$scope.validAmount = function(amount){
		var floatVal = parseFloat(amount);
		if(floatVal > 0 && floatVal <= 100000000){
			return true;
		}
		return false;
	};
    }]);

