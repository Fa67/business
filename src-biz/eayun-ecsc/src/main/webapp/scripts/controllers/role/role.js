'use strict';
angular.module('eayunApp.controllers')

  .config(function ($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.when('/app/role', '/app/role/role');
    $stateProvider
    .state('app.role.role', {
      url: '/role',
      templateUrl: 'views/role/rolemng.html',
      controller: 'RoleListCtrl'
    });
  })
	.controller('RoleCtrl', function ($scope) {
	})
	
	/**直接由main.js定义的路由指定的controller操作*/
  .controller('RoleListCtrl', function ($scope, eayunModal , eayunHttp) {
	  $scope.roleTable = {
		      source: 'sys/role/getListByCustomer.do',
		      api:{},
		      getParams: function () {
		        return {};
		      }
		  };
	  /**管理角色-查看详情*/
	  $scope.manageRole = function (roleId) {
		  eayunHttp.post('sys/rolePower/getListByRole.do',{roleId : roleId}).then(function(response){
			  $scope.powermodel = response.data;
			  var result = eayunModal.open({
				  	backdrop:'static',
			        templateUrl: 'views/role/rolepower.html',
			        controller: 'RolePowerCtrl',
			        resolve: {
		        		roleId : function () {
			                return roleId;
			            },
			            powermodel : function () {
			                return $scope.powermodel;
			            }
			        }
			      }).result;
			  result.then(function (params) {
			        eayunHttp.post('sys/rolePower/setRolePower.do',params).then(function(response){
			        	if(!response.data.code){
			        		eayunModal.success("角色权限编辑成功");
			        		$scope.roleTable.api.draw();
			        	}
			        });
			      });
		  });
	  };
  })
  /**分配角色权限(查看详情)*/
  .controller('RolePowerCtrl', function ($scope , eayunModal , eayunHttp , roleId , powermodel,$modalInstance) {
	  var FirstAllValue = [];
	  angular.forEach(powermodel, function (value,key) {
		  FirstAllValue.push(value.powerId);
	  });
	  var checkAllValue = angular.copy(FirstAllValue);
	  
	  $scope.isShow = true;
	  $scope.paramId = {
			  powerId : '',
			  powerSecondId : '',
			  powerThirdId : ''
	  };
	  $scope.modelone = {};
	  eayunHttp.post('sys/power/getChildrenList.do',{powerId : $scope.paramId.powerId}).then(function(response){
		  $scope.modelone = response.data;
		  $scope.paramId.powerSecondId = $scope.modelone[1].powerId;
		  $scope.modelone[1].isThis = true;
		  $scope.clickFirst($scope.paramId.powerSecondId);
	  });
	  /**点击一级权限菜单事件*/
	  $scope.modeltwo = {};
	  $scope.clickFirst = function (powerId) {
		  angular.forEach($scope.modelone, function (onevalue,ckey) {
				if(onevalue.powerId==powerId){
					onevalue.isThis = true;
				}else{
					onevalue.isThis = false;
				}
			});
		  $scope.paramId.powerSecondId = powerId;
		  eayunHttp.post('sys/power/getChildrenList.do',{powerId : $scope.paramId.powerSecondId}).then(function(response){
			  $scope.modeltwo = response.data;
			  $scope.paramId.powerThirdId = $scope.modeltwo[0].powerId;
			  $scope.modeltwo[0].isThis = true;
			  $scope.clickSecond($scope.paramId.powerThirdId);
		  });
	  };
	  /**点击二级权限菜单事件*/
	  $scope.modelthree = {};
	  $scope.clickSecond = function (powerId) {
		  angular.forEach($scope.modeltwo, function (twovalue,ckey) {
				if(twovalue.powerId==powerId){
					twovalue.isThis = true;
				}else{
					twovalue.isThis = false;
				}
			});
		  $scope.paramId.powerThirdId = powerId;
		  eayunHttp.post('sys/power/getChildrenList.do',{powerId : $scope.paramId.powerThirdId}).then(function(response){
			  $scope.modelthree = response.data;
		  });
	  };
	  
	  /**监听第三级的权限列表，返回数据后给选择框赋初值*/
	  $scope.$watch('modelthree' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		angular.forEach(newVal, function (value,key) {
	    			angular.forEach(checkAllValue, function (cvalue,ckey) {
	    				if(value.powerId==cvalue){
	    					value.isCheck = true;
		    			}
	    			});
	    		});
	    	}
	    });
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
  });