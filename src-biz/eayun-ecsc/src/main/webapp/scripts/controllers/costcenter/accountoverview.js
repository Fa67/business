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
  $urlRouterProvider.when('/app/costcenter', '/app/costcenter/guidebar');
  $urlRouterProvider.when('/app/costcenter/guidebar', '/app/costcenter/guidebar/account');
    $stateProvider
    .state('app.costcenter.guidebar', {
    	url: '/guidebar',
    	templateUrl: 'views/costcenter/costcenter.html',
    	controller: 'CostCenterBarCtrl'
    })
    .state('app.costcenter.guidebar.account', {
      url: '/account',
      templateUrl: 'views/costcenter/account/accountmng.html',
      controller: 'CostCenterAccountCtrl',
      controllerAs:'costCenterAccount'
    }).state('app.costcenter.guidebar.report', {
        url: '/report',
        templateUrl: 'views/costcenter/report/main.html',
        controller: 'CostCenterReportCtrl'
      })
  })
  .controller('CostCenterBarCtrl', function (powerService,$scope) {
	  powerService.powerRoutesList().then(function (powerList) {
          $scope.buttonPower = {
          		isAccountView: powerService.isPower('account_view'),			//查看账户总览
          		isReportView: powerService.isPower('report_view')			//查看费用报表
          };
      });
  })
  .controller('CostCenterAccountCtrl', function (eayunStorage,$rootScope,$scope, eayunModal,eayunHttp , $state,powerService) {
	  powerService.powerRoutesList().then(function (powerList) {
          $scope.buttonPower = {
          		isAccountExcel: powerService.isPower('account_excel'),			//导出Excel
          		isAccountRecord: powerService.isPower('account_record'),			//查看交易记录
          		isAccountRecharge: powerService.isPower('account_recharge'),			//充值
          		isAccountView: powerService.isPower('account_view')			//查看余额
          };
      });
	  var navLists=eayunStorage.get('navLists');
	  navLists.length=0;
	  navLists.push({route:'app.costcenter.guidebar.account',name:'账户总览'});
	  var account=this;
	  var list=[];
	  var firstDate = new Date();
	  firstDate.setDate(1); //第一天
      firstDate.setHours(0, 0, 0, 0);
	  var end=new Date();
	  end.setHours(0, 0, 0, 0);
	  account.time={
			  beginTime:firstDate,
			  endTime:end,
			  maxTime:end
	  };
	  account.incomeType='';
//	  $rootScope.navList(list,'费用中心','costcenter');
	  getBalance();
      function getBalance(){
			eayunHttp.post("costcenter/accountoverview/getaccountbalance.do").then(function(data){
				account.balance=data.data.data;
			});
	  }
      $scope.status = [
		{value: '', text: '收支类型(全部)'},
		{value: 1, text: '收入'},//如不指定，默认选中第一项
		{value: 2, text: '支出'}];
		$scope.itemClicked = function (item, event) {
			account.incomeType=item.value;
			$scope.myTable.api.draw();
		};
	  $scope.myTable = {
	      source: 'costcenter/accountoverview/getrecordlist.do',
	      api:{},
	      getParams: function () {
	        return {
	        	incomeType:account.incomeType,
				beginTime : account.time.beginTime?account.time.beginTime.getTime(): '',
				endTime : account.time.endTime ? account.time.endTime.getTime() + 86400000  : ''
	        };
	      }
	  };
	  account.query=function(){
		  if(account.time.beginTime==null&&account.time.endTime==null){
			  eayunModal.error("请选择时间范围");
				return;
			}
			if(account.time.beginTime==null){
				eayunModal.error("请选择开始时间");
				return;
			}
			if(account.time.endTime==null){
				eayunModal.error("请选择截止时间");
				return;
			}
		  $scope.myTable.api.draw();
	  }
	  /**导出事件*/
	  account.createExcel = function () {
		  if(account.time.beginTime==null&&account.time.endTime==null){
			  eayunModal.error("请选择时间范围");
				return;
			}
			if(account.time.beginTime==null){
				eayunModal.error("请选择开始时间");
				return;
			}
			if(account.time.endTime==null){
				eayunModal.error("请选择截止时间");
				return;
			}
			$scope.myTable.api.draw();
			account.createdata = {
					beginTime : account.time.beginTime?account.time.beginTime.getTime(): '',
					endTime : account.time.endTime ? account.time.endTime.getTime() + 86400000  : ''
			};
		  	var explorer =navigator.userAgent;
			var browser = 'ie';
			if (explorer.indexOf("MSIE") >= 0) {
				browser="ie";
			}else if (explorer.indexOf("Firefox") >= 0) {
				browser = "Firefox";
			}else if(explorer.indexOf("Chrome") >= 0){
				browser="Chrome";
			}else if(explorer.indexOf("Opera") >= 0){
				browser="Opera";
			}else if(explorer.indexOf("Safari") >= 0){
				browser="Safari";
			}else if(explorer.indexOf("Netscape")>= 0) { 
				browser='Netscape'; 
			}
		  $("#record-export-iframe").attr("src", "costcenter/accountoverview/createrecordexcel.do?beginTime="+account.createdata.beginTime+
				  "&endTime="+account.createdata.endTime+"&incomeType="+account.incomeType+"&browser="+browser);
	    };
	    
	  account.recharge=function(){
		  var rechargeNavList=[{route:'app.costcenter',name:'账户总览'}];
		    eayunStorage.persist("rechargeNavList",rechargeNavList);
		    $state.go('pay.recharge');
	  };
  })