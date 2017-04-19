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
	  
  })
  .controller('InvoiceInfoCtrl', ['$scope','$http','eayunModal','$rootScope','$state','$route','toast',
                               function ($scope, $http, eayunModal, $rootScope, $state, $route, toast) {
	  var vm = this;
	  $scope.list = null;
	  
	  vm.loadList = function () {
		  $http.post('invoice/info/getinvoiceinfolist.do').then(function(response){
			  $scope.list = response.data.data;
		  }); 
	  }
	  vm.loadList();
	  //添加开票信息
	  $scope.add = function() {
		  var _info = {invoiceType: '1'};
		  $http.post('invoice/info/getcuscpname.do').then(function(response){
			  if(response.data.respCode == '000000'){
				  _info.invoiceTitle = response.data.data;
			  }
		  }).then(function(){
			  var result = eayunModal.open({
				  backdrop: 'static',
				  templateUrl: 'views/invoice/info/edit.html',
				  controller:'InvoiceInfoSaveCtrl',
				  controllerAs:'edit',
				  resolve: {
					  info : function(){
						  return _info;
					  }
			      }
			  }).result;
			  result.then(function(_info){
				  var info = {};
				  //如果是普通发票，只取发票类型和发票抬头的值
				  if(_info.invoiceType == '1'){
					  info.invoiceTitle = _info.invoiceTitle;
					  info.invoiceType = _info.invoiceType;
				  }else{
					  info = angular.copy(_info, info);
				  }
				  $http.post('invoice/info/addinvoiceinfo.do', info).then(function(response){
					  if (response.data.respCode == '000000') {
		                  toast.success("创建开票信息成功");
		                  vm.loadList();
		              } else {
		                  eayunModal.error(response.data.message);
		              }
				  });
			  });
		  });
	  };
	  //编辑开票信息
	  $scope.edit = function(_info) {
		  var result = eayunModal.open({
			  backdrop: 'static',
			  templateUrl: 'views/invoice/info/edit.html',
			  controller:'InvoiceInfoSaveCtrl',
			  controllerAs:'edit',
			  resolve: {
				  info : function(){
					  return angular.copy(_info);
				  }
		      }
		  }).result;
		  result.then(function(_info){
			  var info = {};
			  //如果是普通发票，只取发票类型和发票抬头的值
			  if(_info.invoiceType == '1'){
				  info.cusId = _info.cusId;
				  info.invoiceTitle = _info.invoiceTitle;
				  info.invoiceType = _info.invoiceType;
				  info.defaultItem = _info.defaultItem;
				  info.id = _info.id;
			  }else{
				  info = angular.copy(_info, info);
			  }
			  $http.post('invoice/info/updateinvoiceinfo.do', info).then(function(response){
				  if (response.data.respCode == '000000') {
	                  toast.success("更新开票信息成功");
	                  vm.loadList();
	              } else {
	                  eayunModal.error(response.data.message);
	              }
			  });
		  });
	  };
	  $scope.del = function(_id) {
		  eayunModal.confirm("确认删除该条开票信息？").then(function () {
			  $http.post('invoice/info/deleteinvoiceinfo.do', {id: _id}).then(function(response){
	              if (response.data.respCode == '000000') {
	                  toast.success("删除开票信息成功");
	                  vm.loadList();
	              } else {
	                  eayunModal.error(response.data.message);
	              }
	        });
          })
		  
	  };
	  $scope.setDefault = function(_id) {
		  $http.post('invoice/info/setdefaultinvoiceinfo.do', {id: _id}).then(function(response){
              if (response.data.respCode == '000000') {
                  toast.success("设置默认成功");
                  vm.loadList();
              } else {
                  eayunModal.error(response.data.message);
              }
		  });
	  }
  }])
  .controller('InvoiceInfoSaveCtrl',['$scope','$http','$state','$routeParams','$modalInstance','Upload','toast','info','eayunModal',
                                    function($scope, $http , $state, $routeParams, $modalInstance,Upload,toast,_info,eayunModal){
	  var vm = this;
	  vm.model = {};
	  $scope.info = _info;
	  $scope.cancel = function(){
		  $modalInstance.dismiss('cancel');
	  }
	  $scope.commit = function(){
		  $modalInstance.close($scope.info);
	  }
	  $scope.fileTypes = ['jpg','JPG','png','PNG'];
      vm.uploadFiles = function (_file,type) {//添加文件
    	  if(_file.size > (2*1024*1024)){
    		  eayunModal.warning("图片大小不得超过2M");
    		  return;
    	  }
    	  if(_file){
    		  Upload.upload({
    			  url: 'invoice/info/uploadimagefile.do',//提交后台的上传图片
    			  data: {"file": _file,"type":type}
    		  }).then(function (response) {
    			  var fileId = response.data.data;
    			  vm.changeFileId(fileId, type);
    			  toast.success("上传图片成功");
    		  });
    	  }
      };
      
      vm.removeFile = function(type){//删除文件
    	  vm.changeFileId(null, type);
      };
      
      vm.changeFileId = function (fileId, type){
    	  if(type == 'biz') {
    		  $scope.info.bizLicenseFileId = fileId;
    	  } else if(type == 'tax') {
    		  $scope.info.taxpayerLicenseFileId = fileId;
    	  } else if(type == 'bank') {
    		  $scope.info.bankLicenseFileId = fileId;
    	  }
      }
  }]);
