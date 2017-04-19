'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state("app.record.main.unit", {
				url : '/unit',
				templateUrl : 'views/record/unit/main.html',
				controller : 'unitCtrl',
				controllerAs:'unitlist'
			});
		}).controller('unitCtrl', function ($scope,$state,eayunModal,eayunStorage,$rootScope,eayunHttp) {
			var $scope=this;
			var list=[];
			$rootScope.navList(list,'已备案列表');
			$scope.myunitTable = {
				source : 'ecsc/record/getrecordList.do',
				api : {},
				getParams : function() {
					return {
						dcid:""
					};
				}
			};
				
			
			
			$scope.unitwebsdetail = function(model) {
				
				var result = eayunModal.open({
					templateUrl : 'views/record/unit/unitWebsdetail.html',
					controller : 'sessecunitWebsCtrl',
					resolve : {
						model:  model
					}

				}).result;
				result.then(function(value) {
//						// 创建页面点击提交执行后台Java代码
//						eayunHttp.post('cloud/grouprule/addRule.do', value)
//								.then(function(response) {
//									// 如果创建成功，刷新当前列表页
//									console.info(response);
//									if (null!=response.data&&'null'!=response.data&&response.data.code != "010120") {
//										toast.success('添加规则成功');
//									}
//									$scope.myGroupRuleTable.api.draw();
//								});
				}, function() {
					// console.info('取消');
				});

			};	
			
			
		}).controller('sessecunitWebsCtrl', function ($scope,$state,model,$modalInstance,eayunModal) {
			$scope.model=model;
			
		
			
			$scope.recursiveReplace = function(str){
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
						return $scope.recursiveReplace(str);
					}
				}else{
					return '';
				}
			}
			
			
			for(var i=0;i<model.webs.length;i++){
				$scope.model.webs[i].serviceContent=$scope.recursiveReplace($scope.model.webs[i].serviceContent);
				
				$scope.model.webs[i].webLanguage=$scope.recursiveReplace($scope.model.webs[i].webLanguage); 
			}
			
			$scope.commit = function() {

				$scope.ok($scope.model);
			};
			$scope.showFag=0;


			$scope.showorNo=function(s){

		            if(s==0){
		            	$scope.showFag=1
		            }else{
		            	$scope.showFag=0
		            }


		        };
		        
		        $scope.businessFileId=false;
		        $scope.dutyFileId=false;
		        $scope.dutywebFileId=true;
		        $scope.domainFileId=true;
		        $scope.specialFileId=true;
		        if( $scope.model.businessFileId==''|| $scope.model.businessFileId==null|| $scope.model.businessFileId==undefined){
		            $scope.businessFileId=true;
		           
		        }
		        if( $scope.model.dutyFileId==''|| $scope.model.dutyFileId==null|| $scope.model.dutyFileId==undefined){
		            $scope.dutyFileId=true;
		           
		        }
		        if($scope.model.webs.length>0){
		            for(var i=0;i<$scope.model.webs.length;i++){
		                $scope.webs=$scope.model.webs;
		              
		                if( $scope.webs[i].dutyFileId!=''&& $scope.webs[i].dutyFileId!=null&& $scope.webs[i].dutyFileId!=undefined){
		                    $scope.dutywebFileId=false;
		                  
		                }
		                if( $scope.webs[i].domainFileId!=''&&  $scope.webs[i].domainFileId!=null&& $scope.webs[i].domainFileId!=undefined){
		                    $scope.domainFileId=false;
		                   
		                }
		                if( $scope.webs[i].specialFileId!=''&&  $scope.webs[i].specialFileId!=null&&$scope.webs[i].specialFileId!=undefined){

		                    $scope.specialFileId=false;
		                  
		                }
		            }
		        }
			  $scope.cancel = function (){
					 $modalInstance.dismiss();
				  };
				  
				  
				  $scope.unitimg=function() {

			            var result = eayunModal.open({
			               
			               
			             
			                templateUrl: 'views/record/img/unitfzr.html',
			                controller: 'unitfzrCtrl1',
			               
			                resolve: {
			                    models: $scope.model
			                }

			            });
			        };
			        $scope.yinyeimg=function() {

			            var result = eayunModal.open({
			             
			                templateUrl: 'views/record/img/yinye.html',
			                controller: 'yinyeCtrl1',
			           
			                resolve: {
			                    models: $scope.model
			                }

			            });
			        };
			        $scope.weburlimg=function() {

			            var result = eayunModal.open({
			               
			                templateUrl: 'views/record/img/weburl.html',
			                controller: 'weburlCtrl1',
			              
			                resolve: {
			                    models: $scope.model
			                }

			            });
			        };
			        $scope.webfzrimg=function() {
			        	
			            var result = eayunModal.open({
			               
			                templateUrl: 'views/record/img/webfzr.html',
			                controller: 'webfzrCtrl1',
			              
			                resolve: {
			                    models: $scope.model
			                }

			            });
			        }
			        $scope.qianzhiimg=function() {

			            var result = eayunModal.open({
			              
			                templateUrl: 'views/record/img/qianzhi.html',
			                controller: 'qianzhiCtrl1',
			               
			                resolve: {
			                    models: $scope.model
			                }

			            });
			        }

//			
		}).controller('qianzhiCtrl1', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model=models;

		}).controller('unitfzrCtrl1', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model=models;

		}).controller('yinyeCtrl1', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model=models;

		}).controller('weburlCtrl1', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model=models;

		}).controller('webfzrCtrl1', function ($scope,$state,models,$modalInstance) {
			 $scope.cancel = function (){
				 $modalInstance.dismiss();
			  };
			$scope.model=models;

		});

