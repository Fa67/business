'use strict';
angular.module('eayunApp.controllers')

  .config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider.state('register', {
      url: '/sys/register',
      templateUrl: 'views/sys/register/register.html',
      controller: 'RegisterController'
    }).state('registersuccess',{
    	url: '/sys/registersuccess',
    	templateUrl: 'views/sys/register/registersuccess.html',
    	controller: 'RegisterSuccessController'
    });
  })
  .controller('RegisterSuccessController',function($scope){
  })
  .controller('RegisterController', function ($scope, $timeout,eayunModal, eayunHttp, $state) {
	  $scope.checkEmail = true;
	  $scope.checkPhone = true;
	  $scope.checkCompanyCn = true;
	  $scope.teleCodeFlag = true;
	  $scope.teleCodeName = '获取验证码';
	  $scope.model={};
	  
	  $scope.changeCode = function (){
		  $("#imageCode").attr("src", basePath + "action/validetImg?type=register&random=" + Math.random());
	  };
	  
	  $scope.refreshTelephone = function (){
		  $scope.teleCodeFlag = false;
		  $scope.time =60;
		  eayunHttp.post('sys/register/getTeleCode.do',{'telephone':$scope.model.cusPhone,'type':'register'}).
		  then(function (response){
			  if(response.data&&response.data.respCode=='400000'){
				  $scope.timeRefresh();
			  }
			  if(response.data&&response.data.respCode=='010120'){
				  eayunModal.error('验证码发送异常，请稍后重试！',2000);
			  }
			  
		  });
		  
	  };
	  
	  $scope.timeRefresh = function (time){
		  $scope.teleCodeName = '重新获取('+$scope.time+')';
		  if($scope.time>0){
			  $timeout($scope.timeRefresh,1000);
		  }
		  else{
			  $scope.teleCodeFlag = true; 
			  $scope.teleCodeName = '获取验证码';
		  }
		  $scope.time = $scope.time -1;
	  };
	  
	  $scope.cancle = function (){
		  $state.go('login');
	  };
	  
	  $scope.checkCondition = function (type){
		  var email = $scope.model.cusEmail;
		  var telephone = $scope.model.cusPhone;
		  var company = $scope.model.cusCpname;
		  var data = {};
		  if(type =='email'){
			  data = {
					  'cusEmail':email
			  };
		  }
		  if(type =='telephone'){
			  data = {
					  'cusPhone':telephone
			  };
		  }
		  if(type =='company'){
			  data = {
					  'cusCpname':company
			  };
		  }
		  eayunHttp.post('sys/register/checkCondition.do',data).then(function (response){
			  if(response.data){
				  if(type == 'email'){
					  $scope.checkEmail = response.data.flag;
				  }
				  if(type == 'telephone'){
					  $scope.checkPhone = response.data.flag;
				  }
				  if(type == 'company'){
					  $scope.checkCompanyCn = response.data.flag;
				  }
			  }
		  });
	  };
	  
	  $scope.commit = function (){
          $scope.isCommitted = true;
		  eayunHttp.post('sys/register/register.do',$scope.model).then(function (response){
			  if(response.data){
				  if(response.data.respCode&&response.data.respCode!='400000'){
					  if(response.data.respCode.indexOf('imageCodeTimeout')!=-1){
						  eayunModal.error('图片验证码超时，请重新输入');
					  }
					  else if(response.data.respCode.indexOf('imageCodeError')!=-1){
						  eayunModal.error('图片验证码错误，请重新输入');
					  }
					  else if(response.data.respCode.indexOf('phoneCodeError')!=-1){
						  eayunModal.error('手机验证码错误，请重新输入');
					  }
					  else if(response.data.respCode.indexOf('phoneCodeTimeout')!=-1){
						  eayunModal.error('手机验证码超时，请重新获取');
					  }
					  
					  $scope.changeCode();
                      $scope.isCommitted = false;
				  }
				  if(response.data.respCode=='400000') {
					  $state.go('registersuccess');
				  }
			  }
		  });
	  };
	  
	  $scope.changeCode();
	  
  })
  
  ;