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
	  $urlRouterProvider.when('/app/invoice', '/app/invoice/list');
	  $stateProvider
	  .state('app.invoice.list', {
		  url: '/list',
		  templateUrl: 'views/invoice/invoicemng.html',
      	  controller: 'InvoiceListCtrl',
      	  controllerAs: 'list'
	  })
	  .state('app.invoice.apply', {
		  url: '/apply',
		  templateUrl: 'views/invoice/apply/apply.html',
      	  controller: 'InvoiceApplyCtrl',
      	  controllerAs: 'apply'
	  })
	  .state('app.invoice.info', {
		  url: '/info',
		  templateUrl: 'views/invoice/info/list.html',
      	  controller: 'InvoiceInfoCtrl',
      	  controllerAs: 'info'
	  })
	  .state('app.invoice.address', {
		  url: '/address',
		  templateUrl: 'views/invoice/address/list.html',
      	  controller: 'DeliveryAddressCtrl',
      	  controllerAs: 'address'
	  });
  })
  .controller('InvoiceCtrl', ['$scope','$http','$state','$routeParams','eayunStorage','$rootScope','powerService',
                               function ($scope, $http , $state, $routeParams, eayunStorage,$rootScope, powerService) {
	  $rootScope.navList = function(list,cucrentName,type){
		  $rootScope.navLists = list;
		  $rootScope.cucrentName = cucrentName;
		  $rootScope.type = type;
      };
      powerService.powerRoutesList().then(function(powerList){
    	  $scope.modulePower = {
    			  isInvoiceList: powerService.isPower('invoice_list'),
    			  isInvoiceApply: powerService.isPower('invoice_apply'),
    			  isInvoiceCancel: powerService.isPower('invoice_cancel'),
    			  isInvoiceInfo: powerService.isPower('invoice_info'),
    			  isInvoiceAddress: powerService.isPower('invoice_address')
    	  };
      });
      
  }])
  .controller('InvoiceListCtrl', ['$scope','$http','$state','$routeParams','eayunStorage','$rootScope','eayunModal','toast',
                               function ($scope, $http , $state, $routeParams, eayunStorage, $rootScope, eayunModal, toast) {
	  var vm = this;
	  $rootScope.navList([],'发票管理');
	  //查询可开票金额
	  $http.post('invoice/getbillableamount.do').then( function (response) {
		  $scope.amount = response.data.data;
	  });
	  
	  $http.post('invoice/info/getdefaultinvoiceinfo.do').then( function (response) {
		  $scope.defaultInfo = response.data.data;
	  });
	  
	  $http.post('invoice/address/getdefaultdeliveryaddress.do').then( function (response) {
		  $scope.defaultAddress = response.data.data;
	  });
	  
	  var firstDate = new Date();
      firstDate.setDate(1); //第一天
      firstDate.setHours(0, 0, 0, 0);
//      vm.startTime = null;
      var end=new Date();
      end.setHours(0, 0, 0, 0);
//      vm.endTime = null;
      vm.maxTime=end;
	  
	  //申请状态列表
	  vm.statusList = [ {
          value : "",
          text : '状态(全部)',
          $$select : true
      }, {
          value : "0",
          text : '待开票'
      }, {
          value : "10",
          text : '处理中'
      }, {
          value : "20",
          text : '已开票'
      }, {
          value : "30,31",
          text : '已取消'
      }];
	  
	  //开票申请表格
	  vm.invoiceTable = {
          source : 'invoice/queryinvoiceapply.do',
          api : {},
          getParams : function() {
              vm.selected = [];
              return {
                  startTime : vm.startTime ? vm.startTime
                          .getTime()
                          : '',
                  endTime : vm.endTime ? vm.endTime
                          .getTime() : '',
                  status : vm.status ? vm.status
                          : ""
              };
          }
      };

      vm.query = function() {
          vm.invoiceTable.api.draw();
      };
      vm.selectStatus = function(item, event) {
          vm.status = item.value;
          vm.invoiceTable.api.draw();
      };
      
      $scope.cancel = function(_id){
    	  eayunModal.confirm("确认取消该条开票申请？").then(function () {
			  $http.post('invoice/cancelapply.do', {id: _id}).then(function(response){
	              if (response.data.respCode == '000000') {
	                  toast.success("取消发票成功");
	                  vm.query();
	              } else {
	                  eayunModal.error(response.data.message);
	              }
	        });
          })
      }
	  
	  $scope.toApply = function () {
		  $state.go('app.invoice.apply');
	  } 
	  $scope.toInfo = function () {
		  var list=[{route:'app.invoice.list',name:'发票管理'}];
		  $rootScope.navList(list,'开票信息','info');
		  $state.go('app.invoice.info');
	  }
	  $scope.toAddress = function () {
		  var navList = [{route:'app.invoice.list',name:'发票管理'}];
		  $rootScope.navList(navList,'邮寄地址','address');
		  $state.go('app.invoice.address');
	  }
  }])
  .controller('InvoiceApplyCtrl', ['$scope','eayunHttp','$state','$routeParams','toast','$rootScope',
                               function ($scope, eayunHttp , $state, $routeParams, toast, $rootScope) {
	  var vm = this;
	  var list=[{route:'app.invoice.list',name:'发票管理'}];
	  $rootScope.navList(list,'开票申请','info');
	  $scope.invoiceInfoList = null;
	  $scope.deliveryAddressList = null;
	  $scope.apply = {};
	  $scope.lowerLimit = "0.00";
	  $scope.cancel = function(){
		  $state.go('app.invoice.list');
	  };
	  
	  $scope.commit = function(data){
		  eayunHttp.post('invoice/applyinvoice.do', $scope.apply).then(function(response){
			  if(response.data.respCode == '000000'){
				  toast.success('发票申请已提交成功，工作人员会尽快处理！');
				  $state.go('app.invoice.list');
			  } else {
				  toast.error(response.data.message);
			  }
		  });
	  };
	  $scope.validAmount = function(value){
		  var floatVal = parseFloat(value);
		  var tempVal = String(floatVal);
		  if(tempVal.length < value.length && value.indexOf('.') < 0){
			  return false;
		  }
		  if(floatVal > 0 
			  && floatVal >= $scope.lowerLimit 
			  && floatVal <= $scope.amount.billableAmount 
			  && floatVal <= 500000){
			return true;
		}
		return false;
	  };
	  vm.loadBillableLowerLimit = function(){
		  eayunHttp.post('invoice/getbillablelowerlimit.do').then(function(response) {
			  $scope.lowerLimit = response.data.data;
		  });
	  }
	  vm.loadInvoiceInfoList = function () {
		  eayunHttp.post('invoice/info/getinvoiceinfolist.do').then(function(response) {
			  $scope.invoiceInfoList = response.data.data;
			  vm.checkDefaultInvoiceInfo();
		  });
	  };
	  vm.loadDeliveryAddressList = function () {
		  eayunHttp.post('invoice/address/getdeliveryaddresslist.do').then(function(response) {
			  $scope.deliveryAddressList = response.data.data;
			  //find 默认选中项
			  vm.checkDefaultDeliveryAddress();
		  });
	  };
	  vm.loadBillableAmount = function () {
		  eayunHttp.post('invoice/getbillableamount.do').then( function (response) {
			  $scope.amount = response.data.data;
		  });
	  };
	  vm.checkDefaultInvoiceInfo = function (){
		  if($scope.invoiceInfoList != null && $scope.invoiceInfoList.length > 0){
			  for (var i = 0; i < $scope.invoiceInfoList.length; i++) {
				  if($scope.invoiceInfoList[i].defaultItem == '1'){
					  $scope.apply.invoiceInfoId = $scope.invoiceInfoList[i].id;
					  vm.changeInvoiceInfo($scope.invoiceInfoList[i]);
					  break;
				  }
			  }
		  }
	  };
	  vm.checkDefaultDeliveryAddress = function (){
		  if($scope.deliveryAddressList != null && $scope.deliveryAddressList.length > 0){
			  for (var i = 0; i < $scope.deliveryAddressList.length; i++) {
				  if($scope.deliveryAddressList[i].defaultItem == '1'){
					  $scope.apply.deliveryAddressId = $scope.deliveryAddressList[i].id;
					  vm.changeDeliveryAddress($scope.deliveryAddressList[i]);
					  break;
				  }
			  }
		  }
	  };
	  vm.changeInvoiceInfo = function(_info) {
		  angular.extend($scope.apply, _info);
	  }
	  vm.changeDeliveryAddress = function(_address) {
		  angular.extend($scope.apply, _address);
	  }
	  vm.toInvoiceInfo = function () {
		  var list=[{route:'app.invoice.list',name:'发票管理'},{route:'app.invoice.apply',name:'开票申请'}];
		  $rootScope.navList(list,'开票信息','info');
		  $state.go('app.invoice.info');
	  }
	  vm.toDeliveryAddress = function () {
		  console.log('to delivery address');
		  var navList = [{route:'app.invoice.list',name:'发票管理'},{route:'app.invoice.apply',name:'开票申请'}];
		  $rootScope.navList(navList,'邮寄地址','address');
		  console.log($rootScope.navLists);
		  $state.go('app.invoice.address');
	  }
	  vm.loadBillableLowerLimit();
	  vm.loadBillableAmount();
	  vm.loadInvoiceInfoList();
	  vm.loadDeliveryAddressList();
  }]);
