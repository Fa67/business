'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $stateProvider.state('app.auth.user', {
      url: '/user',
      templateUrl: 'views/auth/user/usermng.html',
      controller: 'AuthUserCtrl'
    });
  })

/**
 * 个人账户信息
 * controller
 */
  .controller('AuthUserCtrl', function (eayunStorage,$scope, eayunModal,eayunHttp , $state,$location) {
	  var navLists=eayunStorage.get('navLists');
	  navLists.length=0;
	  navLists.push({route:'app.auth.user',name:'个人账号信息'});
	//pop框方法
	  $scope.hintDescShow = false;
	  $scope.PersonDescShow = false;
	  $scope.openPopBox = function(obj){
		  if(obj.type == 'descName'){
			  $scope.hintDescShow = true;
		  }
		  if(obj.type == 'PersonName'){
			  $scope.PersonDescShow = true;
		  }
		  $scope.description = obj.value;
	  };
	  $scope.closePopBox = function(type){
		  if(type == 'descName'){
			  $scope.hintDescShow = false;
		  }
		  if(type == 'PersonName'){
			  $scope.PersonDescShow = false;
		  }
	  };
	  eayunHttp.post('sys/user/findUser.do',{}).then(function(response){
    	  $scope.usermodel = response.data;
    	  $scope.resetmodel = angular.copy($scope.usermodel,{});
      });
	  /**
	     * Enter查询事件
	     */
	    $(function () {
	        document.onkeydown = function (event) {
	            var e = event || window.event || arguments.callee.caller.arguments[0];
	            if (e && e.keyCode == 13 && $location.absUrl().lastIndexOf("auth/user") != -1) { 
	            	return false;
	            }
	        };
	    });
	  /**修改密码*/
	  $scope.updatePassword = function () {
		  var result = eayunModal.open({
			  	backdrop:'static',
		        templateUrl: 'views/auth/user/editpassword.html',
		        controller: 'updatePasswordCtrl',
		        resolve: {
		        	userAccount : function () {
		                return $scope.usermodel.userAccount;
		            }
		        }
		      }).result;
		  result.then(function (passmodel) {
		        eayunHttp.post('sys/user/modifyPassword.do',passmodel).then(function(response){
		        	if(!response.data.code){
		        		eayunModal.successalert("密码修改成功，请重新登录").then(function () {
		        			eayunHttp.post('sys/login/logout.do',{}).then(function(response){/**退出接口*/
			        			if(response.data&&response.data.respCode=="400000"){
			        				sessionStorage.clear();
					        		$state.go("login");
			        			}
			        		});
		        		},function(){
		        			eayunHttp.post('sys/login/logout.do',{}).then(function(response){/**退出接口*/
			        			if(response.data&&response.data.respCode=="400000"){
			        				sessionStorage.clear();
					        		$state.go("login");
			        			}
			        		});
		        		});
		        	}
		        });
		      },function(){
		    	  
		      });
	    };
	    /**修改用户姓名*/
	    $scope.personShow = true;
	    $scope.updatePerson = function () {
	    	$scope.personShow = false;
	    };
	    $scope.commitPerson = function () {
	    	$scope.personShow = true;
	    	eayunHttp.post('sys/user/updateUser.do',$scope.usermodel).then(function(response){
	    		$scope.resetmodel = angular.copy($scope.usermodel,{});
				  //eayunModal.success('修改成功！');
		      },
			  function(response){
				  eayunModal.error('修改失败');
		      });
	    };
	    $scope.cancel = function () {
	    	$scope.personShow = true;
	    	angular.copy($scope.resetmodel,$scope.usermodel);
	    };
	    /**修改绑定手机-首先校验旧手机号；下一步>打开绑定新手机页面*/
	    $scope.editPhone = function () {
	    	var result = eayunModal.open({
	    		backdrop:'static',
		        templateUrl: 'views/auth/user/checkphone.html',
		        controller: 'checkPhoneCtrl',
		        resolve: {
		        	userPhone : function () {
		                return $scope.usermodel.userPhone;
		            }
		        }
		      }).result;
	    	result.then(function () {
		      },function(){
		      });
	    };
	    /**未绑定时，直接打开绑定新手机页面；或有未通过验证的号码，打开验证页面*/
	    $scope.bindingPhone = function (isHavePhone) {
	    	var result = eayunModal.open({
	    		backdrop:'static',
		        templateUrl: 'views/auth/user/editphone.html',
		        controller: 'editPhoneCtrl',
		        resolve: {
		        	isHavePhone : function () {
		        		return isHavePhone;
		        	},
		        	newPhone : function () {
		        		return $scope.usermodel.userPhone;
		        	},
		        	title : function () {
		        		return isHavePhone?'验证手机号码':'绑定手机号码';
		        	}
		        }
		      }).result;
		  result.then(function (editphone) {
			  $state.go('^.user',{},{reload:true});
		      });
	    };
	    /**修改邮箱*/
	    $scope.editEmail = function () {
	    	var result = eayunModal.open({
	    		backdrop:'static',
		        templateUrl: 'views/auth/user/editemail.html',
		        controller: 'editEmailCtrl',
		        resolve: {
		        }
		      }).result;
	    	/**校验验证码暨发送验证邮件*/
		  result.then(function (newEmail) {
		        eayunHttp.post('sys/user/sendValidMail.do',newEmail).then(function(response){
		        });
		      });
	    };
	    $scope.againSend = function (userEmail){
	    	eayunModal.dialog({
	    		showBtn: false,
		        title: '修改邮箱',
		        width: '500px',
		        height: '600px',
		        templateUrl: 'views/auth/user/againsend.html',
		        controller: 'againSendCtrl',
		        resolve: {
		        	userEmail : function () {
		        		return userEmail;
		        	}
		        }
		      });
	    	eayunHttp.post('sys/user/againSendEmail.do',{'email' : userEmail}).then(function(response){
	        });
	    };
  })
/**
 * 按钮-controller
 */
  .controller('updatePasswordCtrl', function ($scope , eayunModal , eayunHttp , userAccount,$location,$modalInstance) {
	    $(function () {
	        document.onkeydown = function (event) {
	            var e = event || window.event || arguments.callee.caller.arguments[0];
	            if (e && e.keyCode == 13 && $location.absUrl().lastIndexOf("auth/user") != -1) {
	            	return false;
	            }
	        };
	    });
	    $scope.passKey = '';
	  	$scope.getPassKey = function () {
	    	eayunHttp.post("sys/login/getPassKey.do", {}).then(function (response) {
	    		$scope.passKey = response.data;
	  	  });
	    };
	    $scope.getPassKey();
	  	$scope.passmodel = {
			  userAccount : userAccount,
			  oldPassword : '',
			  newPassword : '',
			  secondPassword : ''
	  	};
	  	$scope.changeOld = function () {
		  	$scope.checkcommit = true;
	  	};
	  	$scope.checkPassword = function (value) {	//校验旧密码
		  value = strEnc(value,$scope.passKey,'','');
		  return eayunHttp.post('sys/user/checkOldPassword.do',{oldPassword : value}).then(function(response){
			  return response.data;
	        });
	    };
	    $scope.IsSame = false;//两次输入密码是否一致
	  	$scope.checkNoSame = function () {
		  	$scope.IsSame = $scope.passmodel.secondPassword==$scope.passmodel.newPassword;//二次校验密码
	  	};
	  	$scope.checkIsSame = function () {
		  	$scope.IsSame = $scope.passmodel.secondPassword==$scope.passmodel.newPassword;//二次校验密码
	  	};
		$scope.checkcommit = true;
		$scope.cancel = function (){
			$modalInstance.dismiss();
		};
		/** 定义eayunModal弹出框的提交操作*/
		$scope.commit = function () {
			if($scope.passmodel.newPassword != $scope.passmodel.secondPassword){
				eayunModal.error("两次输入密码不一致，请重新输入");
				return;
			}
			$scope.passmodel.oldPass = strEnc($scope.passmodel.oldPassword,$scope.passKey,'','');
			$scope.passmodel.newPass = strEnc($scope.passmodel.newPassword,$scope.passKey,'','');
			eayunHttp.post('sys/user/checkOldPassword.do',{oldPassword : $scope.passmodel.oldPass}).then(function(response){
				$scope.checkcommit = response.data;
				if($scope.checkcommit){
					eayunModal.confirm('确认修改密码？').then(function () {
						$modalInstance.close($scope.passmodel);
				      });
				}
			});
		};
  })
  	/**修改已绑定的手机时，第一步：验证旧手机*/
    .controller('checkPhoneCtrl', function ($scope , eayunModal , eayunHttp , userPhone , $state , $interval,$window,$location,$modalInstance) {
    	/**
	     * Enter查询事件
	     */
	    $(function () {
	        document.onkeydown = function (event) {
	            var e = event || window.event || arguments.callee.caller.arguments[0];
	            if (e && e.keyCode == 13 && $location.absUrl().lastIndexOf("auth/user") != -1) {
	            	return false;
	            }
	        };
	    });
    	var promise = null;
  	  	var diff = 60;
  	  	$scope.isSend = false;
    	$scope.checkPhone = {
    			userPhone : userPhone,
    			verCode : ''
    	};
    	$scope.model = {
    			second : diff,
    			doSend : $scope.isSend
    	};
		$scope.updateClock = function(){
			$scope.model.second--;
			if($scope.model.second <= 0){
				$scope.model.doSend = false;
    			$interval.cancel(promise);
			}
		};
    	/**发送验证码*/
    	$scope.sendCode = function (phone) {
    		$scope.today = new Date();
    		$scope.model.second = 60;
    		promise = $interval(function(){
    			$scope.updateClock();
    			},1000);
    		$scope.model.doSend = true;
    		eayunHttp.post('sys/user/sendValidSms.do',{phone : phone,type : 'old'}).then(function(response){
    			
    		});
	    };
	    $scope.cancel = function (){
			  $modalInstance.close();
		  };
    	$scope.commit = function () {
    		eayunHttp.post('sys/user/checkCode.do',$scope.checkPhone).then(function(response){
    			if(!response.data.code){
    				if(response.data){
    	        		$scope.cancel();
    	        		/**验证码通过，打开填写新号码页面*/
    	        		var result = eayunModal.open({
    	        			backdrop:'static',
    	    		        templateUrl: 'views/auth/user/editphone.html',
    	    		        controller: 'editPhoneCtrl',
    	    		        resolve: {
    	    		        	isHavePhone : function () {
    	    		        		return false;
    	    		        	},
    	    		        	newPhone : function () {
		    		        		return '';
		    		        	},
    	    		        	title : function () {
		    		        		return '修改手机号码';
		    		        	}
    	    		        }
    	    		      }).result;
    	    		  result.then(function () {
    	    			  $state.go('^.user',{},{reload:true});
    	    		      });
    	        	}else{
    	        		eayunModal.error("请检查输入的验证码");
    	        	}
    			}
	        	
	        });
    		//$scope.ok();执行返回和后台响应异步
	    };
  })
  
  /**
   * 打开此页面的三种情况
   * ① 从未绑定过手机，直接打开新手机确定页面（需输入新号码）
   * ② 存有手机号码，但未通过验证，打开验证页面（不需输入号码）
   * ③ 已经有验证过的号码，修改号码第一步通过后，打开此页面（需输入新号码）
   * 
   * 确定修改手机号码*/
  .controller('editPhoneCtrl', function ($scope , eayunModal , eayunHttp , $state , $interval ,toast , isHavePhone , newPhone,$window,$location,$modalInstance,title) {
	  /**
	     * Enter查询事件
	     */
	  $(function () {
	      document.onkeydown = function (event) {
	          var e = event || window.event || arguments.callee.caller.arguments[0];
	          if (e && e.keyCode == 13 && $location.absUrl().lastIndexOf("auth/user") != -1) {
	        	  return false;
	          }
	      };
	  });
	  $scope.title = title;
	  var promise = null;
	  var diff = 60;
	  $scope.isSend = false;
	  $scope.editPhone = {
  			userPhone : newPhone,
  			verCode : ''
  	  };
	  $scope.model = {
  			second : diff,
  			doSend : $scope.isSend,
  			isHavePhone : isHavePhone
  	  };
	  $scope.updateClock = function(){
		  $scope.model.second--;
		  if($scope.model.second <= 0){
			  $scope.model.doSend = false;
			  $interval.cancel(promise);
		  }
	  };
	  $scope.checknewPhone = function(value){
		  if(null != value && value !=""){
			  if($scope.model.isHavePhone){
				  return true;
			  }else{
				  return eayunHttp.post('sys/user/checknewPhone.do',{newPhone : value}).then(function(response){
					  return response.data;
				  });
			  }
		  }else{
			  return false;
		  }
	  };
	  $scope.sendCode = function(){
		  $scope.today = new Date();
  		  $scope.model.second = 60;
		  promise = $interval(function(){
  			$scope.updateClock();
  			},1000);
		  $scope.model.doSend = true;
		  /**给新手机号发送验证码，①新输入的验证码，②已存在号码，但未通过验证*/
		  eayunHttp.post('sys/user/sendValidSms.do',{phone : $scope.editPhone.userPhone,type : 'new'}).then(function(response){
			  if(!response.data.code){
			  }
		  });
	  };
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  $scope.commit = function () {
		  /**按填写的手机号码做修改，首先验证新手机号码的验证码*/
		  eayunHttp.post('sys/user/updatePhone.do',$scope.editPhone).then(function(response){
	        	if(!response.data.code){
	        		toast.success("修改手机号成功");
	        		$scope.cancel();
	        		$state.go('^.user',{},{reload:true});
	        	}
	        });
	    };
  })
  .controller('editEmailCtrl', function ($scope , eayunModal , eayunHttp,$sce,$state,$location,$modalInstance) {
	    $(function () {
	        document.onkeydown = function (event) {
	            var e = event || window.event || arguments.callee.caller.arguments[0];
	            if (e && e.keyCode == 13 && $location.absUrl().lastIndexOf("auth/user") != -1) {
	            	return false;
	            }
	        };
	    });
	  $scope.isShow = true;
	  $scope.newEmail = {
  			email : '',
  			imgCode : '',
  			type : 'email'
  	};
	  $scope.changeCode = function () {
	        $scope.src = $sce.trustAsResourceUrl(basePath + "action/validetImg?type=email&random=" + Math.random());
	    };
    $scope.checknewMail = function(value){
		if(null != value && value !=""){
    		return eayunHttp.post('sys/user/checknewMail.do',{newMail : value}).then(function(response){
    			return response.data;
    	    });
    	}else{
    		return false;
    	}
	};
	$scope.cancel = function (){
		  $modalInstance.close();
	  };
	  $scope.changeCode();
	  $scope.commit = function () {
		  eayunHttp.post('sys/user/sendValidMail.do',$scope.newEmail).then(function(response){
			  if(!response.data.code){
				  $scope.isShow = false;
			  }else{
				  $scope.changeCode();
			  }
	        },function(){
	        	$scope.changeCode();
	        });
	    };
      $scope.openEmail = function (newEmail) {
    	  $scope.cancel();
    	  $state.go('^.user',{},{reload:true});
      };
      $scope.nextOpen = function () {
    	  $scope.cancel();
    	  $state.go('^.user',{},{reload:true});
      };
  })
  .controller('againSendCtrl', function ($scope , eayunModal , eayunHttp , userEmail) {
	  $scope.newEmail = {
			  email : userEmail
	  };
	  $scope.nextOpen = function () {
    	  $scope.cancel();
    	  $state.go('^.user',{},{reload:true});
      };
	  $scope.openEmail = function (newEmail) {
    	  eayunHttp.post(newEmail,{}).then(function(response){
    	  });
    	  $scope.cancel();
    	  $state.go('^.user',{},{reload:true});
      };
  });