'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')

  .config(function ($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.when('/app/log', '/app/log/log');
    $stateProvider
    .state('app.log.log', {
      url: '/log',
      templateUrl: 'views/log/logmng.html',
      controller: 'LogLogCtrl'
    });
  })
	.controller('LogCtrl', function ($scope,powerService) {
		powerService.powerRoutesList().then(function(powerList){
			$scope.buttonPower = {
				isExport : powerService.isPower('log_export'),	//导出报表
				};
		});
	})
	
	/**直接由main.js定义的路由指定的controller操作*/
  .controller('LogLogCtrl', function ($scope , eayunHttp,eayunModal) {
	//pop框方法
	  $scope.hintResShow = [];
	  $scope.openTableBox = function(obj){
		  if(obj.type == 'resName'){
			  $scope.hintResShow[obj.index] = true;
		  }
		  $scope.ellipsis = obj.value;
	  };
	  $scope.closeTableBox = function(obj){
		  if(obj.type == 'resName'){
			  $scope.hintResShow[obj.index] = false;
		  }
	  };
      eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
    	  $scope.cloudprojectList = response.data.data;
      });
      /**禁用周末方法，date:当前的那个日期（如42个），mode：当前日期的类型，如年月日*/
      $scope.disabled = function (date, mode) {
        return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
      };
      $scope.now = new Date();
      $scope.last = new Date($scope.now.getTime() - 30*24*60*60*1000); 
	  $scope.data = {
				actItem : '',
	        	statu : '',
	        	beginTime : $scope.last,
	        	endTime : $scope.now,
				prjId : ''
		};
	  $scope.myTable = {
		      source: 'sys/log/getLogList.do',
		      api:{},
		      getParams: function () {
		        return {
					actItem : $scope.data.actItem || '',
		        	statu : $scope.data.statu || '',
		        	prjId : $scope.data.prjId || '',
					operator : $scope.data.operator || '' ,
		        	beginTime :  $scope.data.beginTime ? $scope.data.beginTime.getTime() : '',
		        	endTime :  $scope.data.endTime ? $scope.data.endTime.getTime() : ''
			};
		      }
		  };
	  $scope.status = [
		           		{value: '', text: '执行状态(全部)'},
		           		{value: '1', text: '已完成'},
		           		{value: '0', text: '失败'}];

	  $scope.oper = [
		  {value: '0', text: '操作者'},
		  {value: '1', text: 'API'}
	  ];

   	  $scope.itemClicked = function (item, event) {
   		  	$scope.data.statu=item.value;
   			$scope.myTable.api.draw();
   	  };

	  $scope.operClicked = function (item, event) {
		  $scope.data.operator=item.value;
		  $scope.myTable.api.draw();
	  };
   	$scope.dcStatus = [
		           		{pId: '', text: '数据中心（全部）'},
		           		{pId: 'NO', text: '--'}
	           		];
	  $scope.dcItemClicked = function (item, event) {
		  	$scope.data.prjId=item.pId;
			$scope.myTable.api.draw();
	  };
	  $scope.$watch('cloudprojectList' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		angular.forEach(newVal, function (value,key) {
	    			$scope.dcStatus.push({pId: value.projectId, text: value.dcName});
	    		});
	    	}
	    });
	  $scope.queryLogList = function(){
		  if($scope.data.beginTime==null&&$scope.data.endTime==null){
			  eayunModal.warning("请选择时间范围");
				return;
			}
			if($scope.data.beginTime==null){
				eayunModal.warning("请选择开始时间");
				return;
			}
			if($scope.data.endTime==null){
				eayunModal.warning("请选择截止时间");
				return;
			}
			var ms = $scope.data.endTime.getTime()-$scope.data.beginTime.getTime();
			var days = ms/1000/60/60/24;
			if(days > 30){
				eayunModal.warning("时间范围不能大于30天");
				return;
			}else{
				$scope.myTable.api.draw();
			}
	  };
	  
	  /**导出事件*/
	  $scope.createExcel = function () {
		  if($scope.data.beginTime==null&&$scope.data.endTime==null){
			  eayunModal.warning("请选择时间范围");
				return;
			}
			if($scope.data.beginTime==null){
				eayunModal.warning("请选择开始时间");
				return;
			}
			if($scope.data.endTime==null){
				eayunModal.warning("请选择截止时间");
				return;
			}
			var ms = $scope.data.endTime.getTime()-$scope.data.beginTime.getTime();
			var days = ms/1000/60/60/24;
			if(days > 30){
				eayunModal.warning("时间范围不能大于30天");
				return;
			}else{
				$scope.myTable.api.draw();
			}
		  $scope.createdata = {
				  	actItem : $scope.data.actItem || '',
		        	statu : $scope.data.statu || '',
		        	prjId : $scope.data ? $scope.data.prjId : '',
		        	beginTime :  $scope.data.beginTime ? $scope.data.beginTime.getTime() : '',
		        	endTime :  $scope.data.endTime ? $scope.data.endTime.getTime() : ''
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
		  $("#file-export-iframe").attr("src", encodeURI("sys/log/createExcel.do?actItem="+$scope.createdata.actItem+"&statu="
				  +$scope.createdata.statu+"&prjId="+$scope.createdata.prjId+"&begin="+$scope.createdata.beginTime+"&end="+$scope.createdata.endTime+"&browser="+browser));
	    };
	    $scope.$watch('data.prjId+data.statu' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.myTable.api.draw();
	    	}
	    });
  });