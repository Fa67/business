'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
  angular.module('eayunApp.Controllers')
  .config(function($stateProvider,$urlRouterProvider){
	  $stateProvider.state('app.cloud.cloudhost', {//路由
	      url: '/machinebox',
	      templateUrl: 'views/auth/physicalManger/machineBox.html',
	      controller: 'PhysicalCtrl'
	    });
	  
  })
  /**
   * 机柜管理
   */
  .controller('physicalCtrl',function($scope,eayunModal,eayunHttp,$state,$timeout,toast,powerService){
	  /**定义模型userMagTable,js加载完成之后执行eayun_table标签的指令，得到userMagTable.result*/
	$scope.machineboxTable{
		source="sys/machinebox/getListMachinebox.do",
		api="",
		getparams=function () {
		}
		
		/**
		 * 查询用户是否存在
		 */
		 $scope.checkUser = function (){
		    	var user = sessionStorage["userInfo"];
		    	 if(user){
		    		 user = JSON.parse(user);
		    		 if(user&&user.userId){
		    			 return true;
		    		 }
		    	 }
		    	return false;
		    };
		
		/**
		 * 查询事件
		 * 
		 */
		$(function () {
	        document.onkeydown = function (event) {
	            var e = event || window.event ;
	            if(!$scope.checkMachineBox()){
	            	return ;
	            }
	            if (e && e.keyCode == 13) {
	          	  $scope.queryMachineList;
	            }
	        };
	    });
	    
		/**
		 *按名称查询
		 */
		$scope.queryMachineList=function(){
			$scope.myTable.api.draw();
		}
	};
	 /**
	  * 新增机柜
	  */
	$scope.addMachineBox= function(){
		if(){}
		
	}
  })