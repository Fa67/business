'use strict';
angular.module('eayunApp.controllers')

  .config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider.state('forgotcode', {
      url: '/sys/forgotcode',
      templateUrl: 'views/sys/forgotcode/forgotcode.html',
      controller: 'ForgotCodeController'
    });
  })
  .controller('ForgotCodeController', function ($scope, $timeout,eayunModal, eayunHttp, $state) {
	  $scope.user = {};
	  $scope.user.type='forgotcode';
	  $scope.step = 1;
	  $scope.teleCodeFlag = true;
	  $scope.teleCodeName = '发送验证码';
	  /* 初始化ng-model */
	  $scope.user.userAccount = '';
	  $scope.user.idCode = '';
	  $scope.user.phoneCode = '';
	  $scope.userPassword = '';
	  $scope.confirmPassword = '';
	  
	  $scope.changeCode = function(){
		  $("#imageCode").attr("src", basePath + "action/validetImg?type=forgotcode&random=" + Math.random());
	  };
	  
	  $scope.refreshTelephone = function(){
		  $scope.teleCodeFlag = false;
		  $scope.time = 60;
		  eayunHttp.post('sys/forgotcode/getTeleCode.do',{'userAccount':$scope.user.userAccount,'type':'forgotcode'}).
		  then(function (response){
			  if(response.data&&response.data.respCode=='400000'){
				  $scope.timeRefresh();
			  }
			  if(response.data&&response.data.respCode=='010120'){
				  eayunModal.error('验证码发送异常，请稍后重试！',2000);
			  }
			  
		  });
	  };
	  
	  $scope.timeRefresh = function(time){
		  $scope.teleCodeName = '重新发送('+$scope.time+')';
		  if($scope.time>0){
			  $timeout($scope.timeRefresh,1000);
		  }
		  else{
			  $scope.teleCodeFlag = true; 
			  $scope.teleCodeName = '发送验证码';
		  }
		  $scope.time = $scope.time -1;
	  };
	  
	  $scope.cancle = function (){
		  $state.go('login');
	  };
	  
	  $scope.resetAccountFlag = function(){
		  if($scope.accountFlag == false){
			  $scope.accountFlag = undefined;
		  }
	  };
	  
	  $scope.next = function(){
		  if($scope.step == 1){
			  eayunHttp.post('sys/forgotcode/firstCheck.do',$scope.user).then(function(response){
				  if(response.data.accountFlag == undefined){
					  $scope.changeCode();
				  } else {
					  if(response.data.accountFlag != undefined && $scope.user.userAccount != undefined){
						  $scope.changeCode();
						  $scope.codeFlag = response.data.codeFlag;
						  $scope.accountFlag = response.data.accountFlag;
					  }
					  if($scope.codeFlag && $scope.accountFlag){
						  if (response.data.userPhone != '' && response.data.userPhone != null) {
							  $scope.user.userPhone = response.data.userPhone;
							  $scope.step++;
						  } else {
							  eayunModal.warning("您尚未绑定手机号，请联系管理员找回密码");
						  }
					  }
				  }
			  });
		  }
		  else if($scope.step == 2){
			  eayunHttp.post('sys/forgotcode/secondCheck.do',$scope.user).then(function(response){
				  if(response.data.phoneFlag != undefined){
					  $scope.phoneFlag = response.data.phoneFlag;
				  }
				  if($scope.phoneFlag){
					  $scope.step++;
				  }
			  });
		  }
	  };
	  
	  $scope.done = function(){
		  eayunHttp.post('sys/forgotcode/modifyPassword.do',{userAccount : $scope.user.userAccount ,password : $scope.userPassword}).then(function(response){
			  if (response.data.done) {
				  $scope.step++;
			  } else {
				  $state.go('login');
				  eayunModal.warning(response.data.msg);
			  }
		  });
	  };
	  
	  $scope.changeCode();
  });