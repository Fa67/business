'use strict';
angular.module('eayunApp.controllers')

.config(function($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.when('/app/accesskey', '/app/accesskey/accesskey');
	$stateProvider.state('app.accesskey.accesskey', {
		url : '/accesskey',
		templateUrl : 'views/accesskey/accesskeymng.html',
		controller : 'accesskeyListCtrl',
		resolve:{
  			noShow:function(eayunHttp){
  				
  				return eayunHttp.post('obs/accessKey/flush.do').then(function(response){
  					return response.data;
	            });
  			}
  		}
	});
}).controller('accesskeyCtrl', function($scope, eayunModal, eayunHttp, toast) {
	
})

.controller(
		'accesskeyListCtrl',
		function($scope, eayunModal, eayunHttp, toast,$state) {
			
			eayunHttp.post('obs/accessKey/akOperator.do').then(function(response){
				if(response.data=='success'){
					$scope.akTable = {
							source : 'obs/accessKey/getAcckListPage.do',
							api : {},
							getParams : function() {
								return {};
							}
						};
				}else{
					eayunHttp.post('obs/accessKey/flush.do').then(function(response){
						if(response.data=="success"){
							$scope.akTable = {
									source : 'obs/accessKey/getAcckListPage.do',
									api : {},
									getParams : function() {
										return {};
									}
								};
						}
					});
				}
			});
			
			
		
			//显示状态
			$scope.changeIsShow=function(model){
				eayunHttp.post('obs/accessKey/checkphoneispass.do').then(function(response){
					if(response.data.respCode=='000000'){
						eayunHttp.post('obs/accessKey/akOperator.do').then(function(response){
							if(response.data=='success'){
								eayunHttp.post('obs/accessKey/changeShow.do',model).then(function(response){
									$scope.akTable.api.draw();
								});
							}else{
								eayunHttp.post('sys/user/findUser.do',{}).then(function(response){
							  		$scope.user=response.data;
							  		var result = eayunModal.open({
								        templateUrl: 'views/accesskey/editphone.html',
								        controller: 'editPhoneAccessKeyCtrl',
								        backdrop: "static",
								        resolve: {
								        	phone:function(){
								        		return $scope.user.userPhone;
								        	},
								        	flag:function(){
								        		return "changeShow";
								        	},
								        	param:function(){
								        		return model;
								        	},
								        	table:function(){
								        		return $scope.akTable;
								        	}
								        }
								      }).result;
								});
							}
						});
					}else{
						var result = eayunModal.open({
            			  	showBtn: false,
            		        templateUrl: 'views/accesskey/tip.html',
            		        controller:'akTipCtrl'
            		      }).result;
            		  result.then(function (usermodel) {
            			    $state.go('app.auth.user');
            		      },function(){
            		    	  
            		      });
                    }
				});
		}
			var add=function(){
								eayunHttp.post('obs/accessKey/addAcck.do').then(function(response){
    	        					
									if (response.data == null) {
    									eayunModal.error("密钥创建失败");
    									$scope.akCount='0';
    								} else if(response.data!=null&&response.data.code!='010120') {
    									toast.success("密钥创建成功");
    									$scope.akTable.api.draw();
    									$scope.akCount='0';
    								} 
									$scope.akCount='0';
    	        				});
						
				
			};
			// 创建密钥
			$scope.addAcck = function() {
				if($scope.akTable.result.totalCount>=3){
					eayunModal.warning("密钥数量已达上限");
				}else{
					eayunHttp.post('obs/accessKey/checkphoneispass.do').then(function(response){
						if(response.data.respCode=='000000'){
							eayunHttp.post('obs/accessKey/akOperator.do').then(function(response){
								if(response.data=='success'){
									eayunHttp.post('obs/accessKey/getAcckListPage.do').then(function(response){
										var count=-1;
										if(response.data==null){
											count=0;
										}else{
											count= response.data.totalCount;
										}
										if(count<3){
											$scope.akCount='1';
											add();
										}else{
											
											$scope.akTable.api.draw();
											eayunModal.warning("密钥数量已达上限");
										}
									});
									
								}else if(response.data=='notWhiteList'){
									eayunModal.warning("该功能尚在内测中，暂不开放，如需了解更多详情请联系客服。");
								}else{
									eayunHttp.post('sys/user/findUser.do',{}).then(function(response){
								  		$scope.user=response.data;
								  		var result = eayunModal.open({
									        templateUrl: 'views/accesskey/editphone.html',
									        controller: 'editPhoneAccessKeyCtrl',
									        backdrop: "static",
									        resolve: {
									        	phone:function(){
									        		return $scope.user.userPhone;
									        	},
									        	flag:function(){
									        		return "addAcck";
									        	},
									        	table:function(){
									        		return $scope.akTable;
									        	},
									        	param:function(){
									        		return "";
									        	}
									        }
									      });
									});
									
							    };
							});
						}else{
							var result = eayunModal.open({
	            			  	showBtn: false,
	            		        templateUrl: 'views/accesskey/tip.html',
	            		        controller:'akTipCtrl'
	            		      }).result;
	            		  result.then(function (usermodel) {
	            			    $state.go('app.auth.user');
	            		      },function(){
	            		    	  
	            		      });
	                    }
					});
				}
			}
			// 启用/停用
			$scope.updateAccessKey = function(ak) {
				eayunHttp.post('obs/accessKey/checkphoneispass.do').then(function(response){
					if(response.data.respCode=='000000'){
						eayunHttp.post('obs/accessKey/akOperator.do').then(function(response){
							if(response.data=='success'){
								var mapping = ak.acckState == 1 ? 'obs/accessKey/startAcck.do'
										: 'obs/accessKey/blockAcck.do';
								eayunHttp.post(mapping, ak).then(
										function(response) {
											if (response.data == null) {
												eayunModal.error(ak.acckState == 1 ? "密钥启用失败"
														: "密钥停用失败");
												$scope.akTable.api.draw();
											} else {
												toast.success(ak.acckState == 1 ? "密钥启用成功"
														: "密钥停用成功");
												$scope.akTable.api.draw();
											}
										});
							}else{
								eayunHttp.post('sys/user/findUser.do',{}).then(function(response){
							  		$scope.user=response.data;
							  		var result = eayunModal.open({
								        templateUrl: 'views/accesskey/editphone.html',
								        controller: 'editPhoneAccessKeyCtrl',
								        backdrop: "static",
								        resolve: {
								        	phone:function(){
								        		return $scope.user.userPhone;
								        	},
								        	flag:function(){
								        		return "updateAccessKey";
								        	},
								        	param:function(){
								        		return ak;
								        	},
								        	table:function(){
								        		return $scope.akTable;
								        	}
								        }
								      });
								
								});
								
							}
						});
					}else{
						var result = eayunModal.open({
            			  	showBtn: false,
            		        templateUrl: 'views/accesskey/tip.html',
            		        controller:'akTipCtrl'
            		      }).result;
            		  result.then(function (usermodel) {
            			    $state.go('app.auth.user');
            		      },function(){
            		    	  
            		      });
                    }
				});
			}

			// 删除
			$scope.deleteAccessKey = function(ak) {
				eayunHttp.post('obs/accessKey/checkphoneispass.do').then(function(response){
					if(response.data.respCode=='000000'){
						eayunHttp.post('obs/accessKey/akOperator.do').then(function(response){
							if(response.data=='success'){
								eayunModal.confirm('您确定要删除此密钥吗？（警告：删除密钥后使用此密钥的API接口将全部失效）').then(
										function() {
											eayunHttp.post('obs/accessKey/deleteAcck.do', ak)
													.then(function(response) {
														if (response.data == "success") {
															toast.success("删除密钥成功");
															$scope.akTable.api.draw();
														} else {
															$scope.akTable.api.draw();
														}
													});
										}, function() {
											// console.info('取消');
										});
							}else{
								eayunHttp.post('sys/user/findUser.do',{}).then(function(response){
							  		$scope.user=response.data;
							  		var result = eayunModal.open({
								        templateUrl: 'views/accesskey/editphone.html',
								        controller: 'editPhoneAccessKeyCtrl',
								        backdrop: "static",
								        resolve: {
								        	phone:function(){
								        		return $scope.user.userPhone;
								        	},
								        	flag:function(){
								        		return "deleteAccessKey";
								        	},
								        	param:function(){
								        		return ak;
								        	},
								        	table:function(){
								        		return $scope.akTable;
								        	}
								        }
								      });
								});
								
							}
						});
					}else{
						var result = eayunModal.open({
            			  	showBtn: false,
            		        templateUrl: 'views/accesskey/tip.html',
            		        controller:'akTipCtrl'
            		      }).result;
            		  result.then(function (usermodel) {
            			    $state.go('app.auth.user');
            		      },function(){
            		    	  
            		      });
                    }
				});
			}
		})
		.controller('akTipCtrl',function($scope ,$modalInstance) {
			$scope.commit=function(){
				$modalInstance.close('ok');
			}
			$scope.cancel=function(){
				$modalInstance.dismiss('cancel');
			}
		})
.controller('editPhoneAccessKeyCtrl',function($scope , eayunModal , eayunHttp , $state , $interval,$window,$location,toast,phone,flag,param,table,$modalInstance) {
	// --------回车事件
	$scope.checkUser = function() {
		var user = sessionStorage["userInfo"];
		if (user) {
			user = JSON.parse(user);
			if (user && user.userId) {
				return true;
			}
		}
		return false;
	};
	$(function () {
        document.onkeydown = function (event) {
            var e = event || window.event || arguments.callee.caller.arguments[0];
            if (!$scope.checkUser()) {
				return;
			}
            if (e && e.keyCode == 13 && $location.absUrl().lastIndexOf("accesskey/accesskey") != -1) {
            	return false;
            }
        };
    });
	var promise = null;
	  	var diff = 60;
	  	$scope.isSend = false;
	  	
	  	$scope.editPhone={
	  			userPhone : phone,
    			verCode : ''
	  	}
 	 
 	$scope.model = {
 			second : diff,
 			doSend : $scope.isSend,
 			isHavePhone : false
 	};
		$scope.updateClock = function(){
			$scope.model.second--;
			if($scope.model.second <= 0){
				$scope.model.doSend = false;
 			$interval.cancel(promise);
			}
		};
 	/**发送验证码*/
 	$scope.sendCode = function () {
	 		$scope.today = new Date();
	 		$scope.model.second = 60;
	 		promise = $interval(function(){
	 			$scope.updateClock();
	 			},1000);
	 		$scope.model.doSend = true;
	 		var pho=$scope.editPhone.userPhone;
	 		eayunHttp.post('obs/accessKey/sendSMS.do',{phone : $scope.editPhone.userPhone}).then(function(response){
	 			
	 		});
	    };
	  //验证 
    	$scope.commit = function () {
    		eayunHttp.post('obs/accessKey/checkCode.do',$scope.editPhone).then(function(response){
    			if(!response.data.code){
    				if(response.data){
    					if(flag=="changeShow"){
    						eayunHttp.post('obs/accessKey/changeShow.do',param).then(function(response){
        						$scope.cancel();
        						table.api.draw();
    						});
    					}else if(flag=="addAcck"){
    						eayunHttp.post('obs/accessKey/getAcckListPage.do').then(function(response){
    							var count=-1;
    							if(response.data==null){
    								count=0;
    							}else{
    								count= response.data.totalCount;
    							}
    							if(count<3){
    								eayunHttp.post('obs/accessKey/addAcck.do').then(function(response){
	    	        					if (response.data == null) {
	    	        						$scope.cancel();
	    									eayunModal.error("密钥创建失败");
	    								} else if(response.data!=null&&response.data.code!='010120') {
	    									$scope.cancel();
	    									toast.success("密钥创建成功");
	    									table.api.draw();
	    								}
	    	        					$scope.cancel();
	    	        				});
    							}else{
    								$scope.cancel();
    								table.api.draw();
    								eayunModal.warning("密钥数量已达上限");
    							}
    						});
    					}else if(flag=="updateAccessKey"){
    						var mapping = param.acckState == 1 ? 'obs/accessKey/startAcck.do'
    								: 'obs/accessKey/blockAcck.do';
    						eayunHttp.post(mapping, param).then(
    								function(response) {
    									if (response.data == null) {
    										eayunModal.error(param.acckState == 1 ? "密钥启用失败"
    												: "密钥停用失败");
    										$scope.cancel();
    										table.api.draw();
    									} else {
    										toast.success(param.acckState == 1 ? "密钥启用成功"
    												: "密钥停用成功");
    										$scope.cancel();
    										table.api.draw();
    									}
    								});
    					}else if(flag=="deleteAccessKey"){
    						$scope.cancel();
    						eayunModal.confirm('您确定要删除此密钥吗？（警告：删除密钥后使用此密钥的API接口将全部失效）').then(
    								function() {
    									eayunHttp.post('obs/accessKey/deleteAcck.do', param)
    											.then(function(response) {
    												if (response.data == "success") {
    													toast.success("删除密钥成功");
    													
    													table.api.draw();
    												} else {
    													toast.error("删除密钥失败");
    													table.api.draw();
    												}
    											});
    								}, function() {
    									// console.info('取消');
    								});
    					}
    				}else{
    					eayunModal.error("请检查输入的验证码");
    				}
    			}
    			
    		});
    	};
    	$scope.cancel=function(){
    		$modalInstance.dismiss('cancel');
    	};
})