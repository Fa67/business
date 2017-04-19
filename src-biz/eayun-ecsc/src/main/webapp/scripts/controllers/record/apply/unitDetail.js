'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state("app.record.unitDetail", {
				url : '/unitDetail/:applyId',
				templateUrl : 'views/record/apply/unitDetail.html',
				controller : 'unitDetailCtrl',
				controllerAs : 'unitdetail'
			});
		}).controller('unitDetailCtrl', function ($scope,$state,eayunHttp,$stateParams,$rootScope,eayunModal) {
			var vm = this;
			var applyId = $stateParams.applyId;
			var list=[{route:'app.record.main.apply',name:'开始备案'}];
			$rootScope.navList(list,'申请详情');
			vm.model = {};
			vm.recursiveReplace = function(str){
				if (str!=null && str != undefined && str!='') {
					if(str.indexOf(",,") < 0){
						if(str.length>1){
							var strx = str.substring(0,1);
							if(strx==","){
								str = str.substring(1,str.length);
							}
						}
						return str;
					}else{
						str = str.replace(",,",",");
						return vm.recursiveReplace(str);
					}
				}else{
					return '';
				}
			}
			

			vm.businessFileId=false;
	        vm.dutyFileId=false;
	        vm.dutywebFileId=true;
	        vm.domainFileId=true;
	        vm.specialFileId=true;
	        
			
			eayunHttp.post('ecsc/record/getApplyOne.do',applyId).then(function(response) {
				if(response.data!=null && response.data!=""){
					vm.model = response.data;
					if( vm.model.businessFileId==''|| vm.model.businessFileId==null|| vm.model.businessFileId==undefined){
			            vm.businessFileId=true;
			        }
			        if( vm.model.dutyFileId==''|| vm.model.dutyFileId==null|| vm.model.dutyFileId==undefined){
			            vm.dutyFileId=true;
			        }
			        if(vm.model.websiteList.length>0){
			            for(var i=0;i<vm.model.websiteList.length;i++){
			                vm.webs=vm.model.websiteList;
	
			                if( vm.webs[i].dutyFileId!=''&& vm.webs[i].dutyFileId!=null&& vm.webs[i].dutyFileId!=undefined){
			                    vm.dutywebFileId=false;
			                }
			                if( vm.webs[i].domainFileId!=''&&  vm.webs[i].domainFileId!=null&& vm.webs[i].domainFileId!=undefined){
			                    vm.domainFileId=false;
	
			                }
			                if( vm.webs[i].specialFileId!=''&&  vm.webs[i].specialFileId!=null&&vm.webs[i].specialFileId!=undefined){
	
			                    vm.specialFileId=false;
			                }
			            }
			        }
		        }else{
		        	eayunModal.warning("此数据已被删除，请刷新后尝试");
		        }
			});
			vm.cancel=function(){
				$state.go("app.record.main");
			}
			$scope.unitimg=function() {

	            var result = eayunModal.open({
	               
	                templateUrl: 'views/record/img/unitfzr.html',
	                controller: 'unitfzrCtrl',
	               
	                resolve: {
	                    models: vm.model
	                }

	            });
	        };
	        $scope.yinyeimg=function() {

	            var result = eayunModal.open({
	             
	                templateUrl: 'views/record/img/yinye.html',
	                controller: 'yinyeCtrl',
	           
	                resolve: {
	                    models: vm.model
	                }

	            });
	        };
	        $scope.weburlimg=function() {

	            var result = eayunModal.open({
	               
	                templateUrl: 'views/record/img/weburl.html',
	                controller: 'weburlCtrl',
	              
	                resolve: {
	                    models: vm.model
	                }

	            });
	        };
	        $scope.webfzrimg=function() {

	            var result = eayunModal.open({
	               
	                templateUrl: 'views/record/img/webfzr.html',
	                controller: 'webfzrCtrl',
	              
	                resolve: {
	                    models: vm.model
	                }

	            });
	        }
	        $scope.qianzhiimg=function() {

	            var result = eayunModal.open({
	              
	                templateUrl: 'views/record/img/qianzhi.html',
	                controller: 'qianzhiCtrl',
	               
	                resolve: {
	                    models: vm.model
	                }

	            });
	        }
		}).controller('qianzhiCtrl', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model = models;
			$scope.model.webs=models.websiteList;

		}).controller('unitfzrCtrl', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model=models;

		}).controller('yinyeCtrl', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model=models;

		}).controller('weburlCtrl', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			  $scope.model = models;
			  $scope.model.webs=models.websiteList;
			
		}).controller('webfzrCtrl', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model=models;
			$scope.model.webs=models.websiteList;
		});

