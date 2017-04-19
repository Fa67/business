'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state('app.record.main.apply', {
				url : '/apply',
				templateUrl : 'views/record/apply/main.html',
				controller : 'applyCtrl',
				controllerAs:'apply'
			});
		}).controller('applyCtrl', function ($scope,$state,eayunHttp,$rootScope,powerService, eayunStorage) {
			var vm = this;
			var list=[];
			$rootScope.navList(list,'开始备案');
			eayunStorage.set("unit",null);
			eayunStorage.set("web",null);
			eayunStorage.set("webList",null);
			// 权限控制
			powerService.powerRoutesList().then(
				function(powerList) {
					vm.buttonPower = {
						isNewRecord : powerService.isPower('record_add_new'),// 新增备案
						isAccess : powerService.isPower('record_add_access'), // 新增接入
						isList : powerService.isPower('record_list'),// 以备案列表
					};
			});
			vm.recharge=function(){
				 $state.go('app.record.recordList');
			};
			vm.unitList = function(){
				$state.go('app.record.unitList');
			};
			vm.access=function(){
				 $state.go('app.record.accessList');
			};
			vm.isShowUnit = false;
			vm.isTrue = false;
			eayunHttp.post('ecsc/record/isSelect.do','-1').then(function(response) {
				if(response.data*1>0){
					vm.isTrue = true;
				}
			});
			eayunHttp.post('ecsc/record/isSelect.do','8').then(function(response) {
				if(response.data*1>0){
					vm.isShowUnit = true;
				}
			});
			//查询列表
			vm.myTable = {
			  source: 'ecsc/record/getApplyList.do',
			  api : {},
			  getParams: function () {
			        return {
			        	recordType :  '',
			        	status :  ''
				};
		      }
			};
			vm.unitDetail = function (applyId){
				$state.go('app.record.unitDetail',{applyId:applyId});
			};
		});

