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
  .controller('DeliveryAddressCtrl', ['$scope','$http','eayunModal','$rootScope','$state','$route','toast',
                               function ($scope, $http, eayunModal, $rootScope, $state, $route, toast) {
	  var vm = this;
	  console.log($rootScope.navLists[0]);
	  $scope.list = null;
	  vm.loadList = function () {
		  $http.post('invoice/address/getdeliveryaddresslist.do').then(function(response){
			  $scope.list = response.data.data;
		  });
	  }
	  vm.loadList();
	  
	  $scope.add = function() {
		  var adminInfoPromise = $http.post('invoice/address/getadmininfo.do').then(function(response){
			  if(response.data.respCode == '000000'){
				  vm.adminInfo = response.data.data;
			  }
		  });
		  adminInfoPromise.then(function(){
			  var result = eayunModal.open({
				  backdrop: 'static',
				  templateUrl: 'views/invoice/address/edit.html',
				  controller:'DeliveryAddressSaveCtrl',
				  controllerAs:'edit',
				  resolve: {
					  address : function(){
						  return {
							  	receiverName: vm.adminInfo.userPerson,
							  	receiverTel: vm.adminInfo.userPhone
						  };
					  }
			      }
			  }).result;
			  result.then(function(_data){
				  $http.post('invoice/address/adddeliveryaddress.do', _data).then(function(response){
					  if (response.data.respCode == '000000') {
		                  toast.success("创建邮寄地址成功");
		                  vm.loadList();
		              } else {
		                  eayunModal.error(response.data.message);
		              }
				  });
			  });
		  });
	  };
	  //编辑邮寄地址
	  $scope.edit = function(_address) {
		  var result = eayunModal.open({
			  backdrop: 'static',
			  templateUrl: 'views/invoice/address/edit.html',
			  controller:'DeliveryAddressSaveCtrl',
			  controllerAs:'edit',
			  resolve: {
				  address : function(){
					  return angular.copy(_address);
				  }
		      }
		  }).result;
		  result.then(function(data){
			  $http.post('invoice/address/updatedeliveryaddress.do', data).then(function(response){
				  if (response.data.respCode == '000000') {
	                  toast.success("更新邮寄地址成功");
	                  vm.loadList();
	              } else {
	                  eayunModal.error(response.data.message);
	              }
			  });
		  });
	  };
	  $scope.del = function(_id) {
		  eayunModal.confirm("确认删除该条邮寄地址？").then(function () {
			  $http.post('invoice/address/deletedeliveryaddress.do', {id: _id}).then(function(response){
	              if (response.data.respCode == '000000') {
	                  toast.success("删除邮寄地址成功");
	                  vm.loadList();
	              } else {
	                  eayunModal.error(response.data.message);
	              }
	        });
          })
		  
	  };
	  $scope.setDefault = function(_id) {
		  $http.post('invoice/address/setdefaultdeliveryaddress.do', {id: _id}).then(function(response){
              if (response.data.respCode == '000000') {
                  toast.success("设置默认成功");
                  vm.loadList();
              } else {
                  eayunModal.error(response.data.message);
              }
		  });
	  }
  }]).controller('DeliveryAddressSaveCtrl',['$scope','$http','$state','$routeParams','$modalInstance','Upload','toast','address','eayunModal',
                                    function($scope, $http , $state, $routeParams, $modalInstance,Upload,toast,_address,eayunModal){
	  var vm = this;
	  $scope.address = _address;
	  $scope.cancel = function(){
		  $modalInstance.dismiss('cancel');
	  }
	  $scope.commit = function(){
		  $modalInstance.close($scope.address);
	  }
  }])
  ;
