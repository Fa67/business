'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $urlRouterProvider.when('/app/costcenter/guidebar/report','/app/costcenter/guidebar/report/postpay');
	    $stateProvider
	    .state('app.costcenter.guidebar.report.postpay', {
	    	url: '/postpay',
	    	templateUrl: 'views/costcenter/report/postpaymng.html',
	    	controller: 'CostCenterPostPayCtrl',
	    	controllerAs:'postPay'
	    }).state('app.costcenter.guidebar.report.prepayment', {
	    	url: '/prepayment',
	    	templateUrl: 'views/costcenter/report/prepaymentmng.html',
	    	controller: 'CostCenterPrepaymentCtrl'
	    }).state("app.costcenter.postpayDetail",{
			url: '/reportPostPayDetail/:id',
		    templateUrl: 'views/costcenter/report/postpaydetail.html',
		    controller: 'PostpayDetailCtrl',
		    controllerAs:'pyDetail'
		}).state("app.costcenter.prepaymentDetail",{
			url: '/reportPrepaymentDetail/:no',
		    templateUrl: 'views/costcenter/report/prepaymentdetail.html',
		    controller: 'PrepaymentDetailCtrl',
		    controllerAs:'details'
		});
  })
.controller('CostCenterReportCtrl', function ($scope, eayunModal,eayunHttp,toast) {
	
})

.controller('CostCenterPostPayCtrl', ['powerService','eayunStorage','$scope','$state','eayunModal','eayunHttp',function (powerService,eayunStorage,$scope,$state ,eayunModal,eayunHttp) {
	powerService.powerRoutesList().then(function (powerList) {
        $scope.buttonPower = {
        		isReportExcel: powerService.isPower('report_excel'),			//导出Excel
        		isReportView: powerService.isPower('report_view')			//查看
        };
    });
	var py=this;
	var list=eayunStorage.get('navLists');
	list.length=0;
	list.push({route:'app.costcenter.guidebar.report.postpay',name:'费用报表'});
	py.searchType='2';
	py.productName='';
	py.resourceName='';
	var firstDate = new Date();
	firstDate.setDate(1); //第一天
	firstDate.setHours(0, 0, 0, 0);
	py.beginTime=firstDate;
	var end=new Date();
	end.setHours(0, 0, 0, 0);
	py.endTime=end;
	py.monMonth=end;		
	py.begin=py.beginTime;
	py.end=py.endTime;
	py.maxDate=new Date();
	py.myTable = {
		      source: 'costcenter/costreport/getreportlist.do',
		      api:{},
		      getParams: function () {
		        return {
		        	type : '2',
		        	searchType : py.searchType,
		        	beginTime : py.beginTime.getTime()|| '',
					endTime : py.endTime ? py.endTime.getTime() + 86400000  : '',
		        	monMonth :  py.monMonth.getTime() || '',
		        	productName :  py.productName || '',
		        	resourceName : py.resourceName || ''
		        };
		      }
		  };
	 var getTotalCost=function(){
		 eayunHttp.post("costcenter/costreport/gettotalcost.do",{type:'2',searchType:py.searchType,beginTime : py.beginTime.getTime()|| '',endTime : py.endTime ? py.endTime.getTime() + 86400000  : '',monMonth :  py.monMonth.getTime() || '',productName :  py.productName || '',resourceName : py.resourceName || ''}).then(function (response) {
			 py.totalCost=response.data.totalCost;
		 });
	 }
	 getTotalCost();
	 py.change=function(num){
		 if(num=='1'){
			 var firstDate = new Date();
				firstDate.setDate(1); //第一天
				firstDate.setHours(0, 0, 0, 0);
				py.beginTime=firstDate;
				var end=new Date();
				end.setHours(0, 0, 0, 0);
				py.endTime=end;
				py.begin=py.beginTime;
				py.end=py.endTime;
				py.maxDate=new Date();
		 }else{
			 var end=new Date();
				end.setHours(0, 0, 0, 0);
				py.monMonth=end;		
		 }
	 }
	py.queryReport=function(){
		if(py.beginTime==null&&py.endTime==null){
			  eayunModal.error("请选择时间范围");
				return;
			}
			if(py.beginTime==null){
				eayunModal.error("请选择开始时间");
				return;
			}
			if(py.endTime==null){
				eayunModal.error("请选择截止时间");
				return;
			}
			if(py.monMonth==null){
				eayunModal.error("请选择账期");
				return;
			}
		getTotalCost();
		py.myTable.api.draw();
		if(py.searchType=='1'){
			//这里需要改成所选账期时间内
			var first=py.monMonth;
			first.setDate(1);
			first.setHours(0, 0, 0, 0);
			py.begin=first;
			var date=new Date(first);
			var now=new Date();
			date.setDate(1);
			date.setHours(0, 0, 0, 0);
			var month=date.getMonth()+1;
			if(now.getMonth()+1==month){
				now.setHours(0, 0, 0, 0);
				py.end=now;
			}else{
				date.setMonth(month)
				py.end=new Date(date.getTime()-1000*60*60*24);
			}
		}else{
			py.begin=py.beginTime;
			py.end=py.endTime;
		}
		
	}
	/**导出事件*/
	  py.createExcel = function () {
		  if(py.beginTime==null&&py.endTime==null){
			  eayunModal.error("请选择时间范围");
				return;
			}
			if(py.beginTime==null){
				eayunModal.error("请选择开始时间");
				return;
			}
			if(py.endTime==null){
				eayunModal.error("请选择截止时间");
				return;
			}
			if(py.monMonth==null){
				eayunModal.error("请选择账期");
				return;
			}
			py.queryReport();
		  py.createdata = {
				  	type : '2',
		        	searchType : py.searchType,
		        	beginTime : py.begin.getTime()|| '',
					endTime : py.end ? py.end.getTime() + 86400000  : '',
		        	monMonth :  py.monMonth.getTime() || '',
		        	productName :  py.productName || '',
		        	resourceName : py.resourceName || ''
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
		  $("#report-export-iframe").attr("src", "costcenter/costreport/createpostpayexcel.do?type="+py.createdata.type+"&searchType="
				  +py.createdata.searchType+"&beginTime="+py.createdata.beginTime+"&endTime="+py.createdata.endTime+"&monMonth="
				  +py.createdata.monMonth+"&productName="+py.createdata.productName+"&resourceName="+py.createdata.resourceName+"&browser="+browser);
	    };
	    
	    py.detail=function(id){
	    	$state.go('app.costcenter.postpayDetail',{"id":id}); // 跳转后的URL;
	    }
}])

.controller('CostCenterPrepaymentCtrl', ['powerService','eayunStorage','$scope','$state','eayunModal','eayunHttp','toast',function (powerService,eayunStorage,$scope,$state, eayunModal,eayunHttp,toast) {
	powerService.powerRoutesList().then(function (powerList) {
        $scope.buttonPower = {
        		isReportExcel: powerService.isPower('report_excel'),			//导出Excel
        		isReportView: powerService.isPower('report_view')			//查看
        };
    });
	var list=eayunStorage.get('navLists');
	list.length=0;
	list.push({route:'app.costcenter.guidebar.report.prepayment',name:'费用报表'});
	$scope.productName='';
	var firstDate = new Date();
	firstDate.setDate(1); //第一天
	firstDate.setHours(0, 0, 0, 0);
	$scope.beginTime=firstDate;
	var end=new Date();
	end.setHours(0, 0, 0, 0);
	$scope.endTime=end;
	$scope.begin=$scope.beginTime;
	$scope.end=$scope.endTime;
	$scope.maxDate=end;
	$scope.myTable = {
		      source: 'costcenter/costreport/getreportlist.do',
		      api:{},
		      getParams: function () {
		        return {
		        	type : '1',
					beginTime : $scope.beginTime.getTime()|| '',
					endTime : $scope.endTime ? $scope.endTime.getTime() + 86400000  : '',
		        	productName :  $scope.productName || '',
		        };
		      }
		  };
	 var getTotalCost=function(){
		 eayunHttp.post("costcenter/costreport/gettotalcost.do",{type:'1',beginTime : $scope.beginTime.getTime()|| '',endTime : $scope.endTime ? $scope.endTime.getTime() + 86400000  : '',productName :  $scope.productName || ''}).then(function (response) {
			 $scope.totalCost=response.data.totalCost;
		 });
	 }
	 getTotalCost();
	$scope.queryReport=function(){
		if($scope.beginTime==null&&$scope.endTime==null){
			  eayunModal.error("请选择时间范围");
				return;
			}
			if($scope.beginTime==null){
				eayunModal.error("请选择开始时间");
				return;
			}
			if($scope.endTime==null){
				eayunModal.error("请选择截止时间");
				return;
			}
		getTotalCost();
		$scope.myTable.api.draw();
		$scope.begin=$scope.beginTime;
		$scope.end=$scope.endTime;
	}
	/**导出事件*/
	  $scope.createExcel = function () {
		  if($scope.beginTime==null&&$scope.endTime==null){
			  eayunModal.error("请选择时间范围");
				return;
			}
			if($scope.beginTime==null){
				eayunModal.error("请选择开始时间");
				return;
			}
			if($scope.endTime==null){
				eayunModal.error("请选择截止时间");
				return;
			}
			getTotalCost();
			$scope.myTable.api.draw();
		  $scope.createdata = {
				  	type : '1',
		        	beginTime : $scope.beginTime.getTime()|| '',
					endTime : $scope.endTime ? $scope.endTime.getTime() + 86400000  : '',
		        	productName :  $scope.productName || ''
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
		  $("#report-export-iframe").attr("src", "costcenter/costreport/createprepaymentexcel.do?type="+$scope.createdata.type+
				  "&beginTime="+$scope.createdata.beginTime+"&endTime="+$scope.createdata.endTime+
				  "&productName="+$scope.createdata.productName+"&browser="+browser);
	    };
	    $scope.detail=function(no){
	    	eayunHttp.post("costcenter/costreport/orderisbelong.do",{"orderNo":no}).then(function(data){
	    		if(data.data.data==true){
	    			$state.go('app.costcenter.prepaymentDetail',{"no":no}); // 跳转后的URL;
	    		}else{
	    			eayunModal.error("很抱歉，您访问的页面出现错误！");
	    		}
	    	});
	    }
}])
.controller('PostpayDetailCtrl', ['eayunStorage','eayunHttp','$stateParams',function (eayunStorage,eayunHttp,$stateParams) {
	var py=this;
	var list = eayunStorage.get('navLists');
	list.length=0;
	list.push({route:'app.costcenter.guidebar.report.postpay',name:'费用报表'});
	list.push({route:'app.costcenter.postpayDetail({id:"'+$stateParams.id+'"})',name:'费用报表详情'});
	findReportById();
	function findReportById(){
		eayunHttp.post("costcenter/costreport/getpostpaydetail.do",{"id":$stateParams.id}).then(function(data){
			py.model=data.data;
		});
	}
}])
.controller('PrepaymentDetailCtrl', ['eayunStorage','eayunHttp','eayunModal','$stateParams','toast','$state',function (eayunStorage,eayunHttp,eayunModal,$stateParams,toast,$state) {
	var details=this;
	findReportById();
	function findReportById(){
		eayunHttp.post("costcenter/costreport/getprepaymentdetail.do",{"orderNo":$stateParams.no}).then(function(data){
			if(data.data.respCode=='000000'){
				var list=eayunStorage.get('navLists');
				list.length=0;
				list.push({route:'app.costcenter.guidebar.report.prepayment',name:'费用报表'});
				list.push({route:'app.costcenter.prepaymentDetail({no:"'+$stateParams.no+'"})',name:'费用报表详情'});
				details.model=data.data.data;
				details.model.money=details.model.paymentAmount-details.model.accountPayment;
				details.model.paymentAmount=details.model.paymentAmount;
				details.model.thirdPartPayment=details.model.thirdPartPayment;
				details.model.accountPayment=details.model.accountPayment;
				var config=details.model.prodConfig;
				config = config.split("<br>");
				details.config=config;
			}else{
				$state.go('app.costcenter.guidebar.report.prepayment');
			}
			
		});
	}
}]);