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
  $urlRouterProvider.when('/app/auth', '/app/auth/customer');
  	/**定义路由，加载的页面放入上一级路由(app.auth)所加载的页面的data-ui-view里面*/
    $stateProvider
    .state('app.auth.customer', {
      url: '/customer',
      templateUrl: 'views/auth/customer/custmng.html',
      controller: 'CustomerCtrl'
    })
  })
  
  /**
 * @ngdoc function
 * @name eayunApp.controller:CloudhostCtrl
 * @description
 * # AuthCtrl
 * 账号管理
 */
  .controller('AuthCtrl', function (eayunStorage,$scope , powerService,$state) {
	  $scope.navLists=[];
	  eayunStorage.set('navLists',$scope.navLists);
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.modulePower = {
				  isCustomer : powerService.isPower('customer_view'),//公司信息
				  isUser : powerService.isPower('user_view'), 		//个人账号信息
				  isUsermng : powerService.isPower('usermng_view'),	//用户管理
		  };
		  if(!$scope.modulePower.isCustomer){
			  $state.go("app.auth.user");
		  }
		});
  })
  /**公司信息*/
  .controller('CustomerCtrl', function (eayunStorage,$scope , eayunHttp,toast,$state,powerService) {
	  var navLists=eayunStorage.get('navLists');
	  navLists.length=0;
	  navLists.push({route:'app.auth.customer',name:'公司信息'});
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.modulePower = {
				  isCustomer : powerService.isPower('customer_view'),//公司信息
		  };
		  if(!$scope.modulePower.isCustomer){
			  $state.go("app.auth.user");
		  }
		});
	  $scope.isShow = true;
	  $scope.update = function () {
		  $scope.isShow = false;
	    };
	  eayunHttp.post('sys/customer/findCustomerByUser.do',{}).then(function(response){
    	  $scope.model = response.data;
    	  $scope.resetmodel = angular.copy($scope.model,{});
      });
	  $scope.checkcusCpname = function (value) {
		  if($scope.isShow){
			  return true;
		  }
		  if(null != value && value !=""){
	    		return eayunHttp.post('sys/customer/checkcusCpname.do',
	    				{cusCpname : value}).then(function(response){
	    			return response.data;
	    	    });
	    	}else{
	    		return false;
	    	}
	  }
	  /**controller对应页面form的提交操作*/
	  $scope.submit = function () {
		  $scope.isShow = true;
		  eayunHttp.post('sys/customer/updateCustomer.do',$scope.model).then(function(response){
			  if(!response.data.code){
				  toast.success("更新公司信息成功");
				  $scope.resetmodel = angular.copy($scope.model,{});
			  }
			  
	      });
	    };
	  $scope.reset = function (){
		  angular.copy($scope.resetmodel,$scope.model);
		  if($scope.model.cusCpname==null){
			  $scope.model.cusCpname='';
		  }
	    };
    $scope.cancel = function (){
		  $scope.isShow = true;
		  angular.copy($scope.resetmodel,$scope.model);
	    };
  });
