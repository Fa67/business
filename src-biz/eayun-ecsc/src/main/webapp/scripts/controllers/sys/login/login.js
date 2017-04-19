/**
 * Created by chenhao on 2015/9/24.
 */
'use strict';

angular.module('eayunApp.controllers')

  .config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider.state('login', {
      url: '/sys/login',
      templateUrl: 'views/sys/login/login.html',
      controller: 'LoginCtrl'
    });
  })
  .controller('LoginCtrl', function ($scope, eayunModal, eayunHttp, $state,$cookieStore,$window,powerService,$interval,$document) {
	powerService.clear();
    $scope.user = {};
    $scope.passuser = {};
    $scope.passKey = '';
    $scope.errorMsg='';
    try{
		$scope.user.userAccount=$cookieStore.get('userAccount');

	}catch(e){

	}

	  $document.find('body').css({
		  minWidth : 'auto'
	  });

	var promise=null;
	$scope.updateClock = function(){
		$scope.second--;
		if($scope.second <= 0){
			$scope.loginDis = false;
			$interval.cancel(promise);
		}
	}
    $scope.login = function () {
    	eayunHttp.post("sys/login/getPassKey.do", {}).then(function (response) {
            $scope.passKey = response.data;
	    	$scope.second = 1;
	    	promise = $interval(function(){
	 			$scope.updateClock();
	 			},1000);
	    	$scope.loginDis = true;
	    	$scope.errorMsg = "";
	      var url = "sys/login/login.do";
	      sessionStorage.clear();
	      if($scope.checkUser()){
		  $scope.user.Password = strEnc($scope.passuser.userPassword,$scope.passKey,'','');
	      $scope.user.type='login';
	    	  eayunHttp.post(url, $scope.user)
	    	  .then(function (response) {
	    		  if (response.data.respCode=='400000') {
	    			  if(response.data.userInfo.error){
	    				  if(response.data.userInfo.error.indexOf('项目')!=-1){
	    					  eayunModal.warning(response.data.userInfo.error);
	    				  }
	    				  else{
	    					  $scope.errorMsg = response.data.userInfo.error;
	    					  $scope.changeCode();
	    				  }
	    			  }
	    			  else {
	    				  $scope.errorMsg = '';
	    				  $scope.userInfo = response.data.userInfo;
	    				  $window.sessionStorage["userInfo"] = JSON.stringify($scope.userInfo);
	    				  if($scope.userInfo.lastTime == null||$scope.userInfo.lastTime == ""){
	    					  $state.go("init");
	    				  }else{
	    					  $state.go("app");
	    				  }
	    			  }
	    		  } else {
	    			  $scope.changeCode();
	    		  }
	    	  });
	      }
    	});
    };
    
    $scope.checkUser = function (){
    	if($scope.user.userAccount==null||$scope.user.userAccount==""||$scope.user.userAccount==undefined){
    		$scope.errorMsg = "请输入用户名";
    		return false;
    	}
    	else if($scope.passuser.userPassword==null||$scope.passuser.userPassword==""||$scope.passuser.userPassword==undefined){
    		$scope.errorMsg = "请输入密码";
    		return false;
    	}
    	else if($scope.user.idCode==null||$scope.user.idCode==""||$scope.user.idCode==undefined){
    		$scope.errorMsg = "请输入验证码";
    		return false;
    	}
    	return true;
    	
    };
    $scope.getPassKey = function () {
    	eayunHttp.post("sys/login/getPassKey.do", {}).then(function (response) {
    		$scope.passKey = response.data;
  	  });
    };
    $scope.changeCode = function () {
    	//$scope.getPassKey();
        $("#codeimg").attr("src", basePath + "action/validetImg?type=login&random=" + Math.random());
    };
    $scope.forgotcode = function(){
    	$state.go("forgotcode");
    };
    $scope.register = function (){
    	$state.go("register");
    };
    
    $scope.watchUpperCase = function (event){
    	var charCode = event.charCode ? event.charCode : event.which;
    	if(event.key == undefined){
    		charCode = window.event.charCode ? window.event.charCode : window.event.which;
    	}
    	
    	if((charCode >= 65 && charCode <= 90)){
    		$scope.checkUpper = true;
    	}
    	if((charCode >= 97 && charCode <= 122)){
    		$scope.checkUpper = false;
    	}
    };
    
    $scope.changeCode();
	$scope.$on("$destroy",function(){
		$document.find('body').css({
			minWidth : '1336px'
		})
	});

  });


