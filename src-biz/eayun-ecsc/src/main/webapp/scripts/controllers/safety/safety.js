'use strict';
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $urlRouterProvider.when('/app/safety', '/app/safety/safetybar');
}).controller('SafetyCtrl', function ($scope ,$state, eayunHttp , cloudprojectList,$window,eayunModal,$rootScope,powerService) {
	  $scope.model = {};
	  $scope.cloudprojectList = cloudprojectList;
    	  var daPrj = sessionStorage["dcPrj"];
    	  if(daPrj){
    		  daPrj = JSON.parse(daPrj);
    		  angular.forEach($scope.cloudprojectList, function (value,key) {
    			  if(value.projectId == daPrj.projectId)
    				  $scope.model.projectvoe = value;	  
    		  });
    	  }else{
    		  angular.forEach($scope.cloudprojectList, function (value) {
  				if(value.projectId!=null&&value.projectId!=''&&value.projectId!='null'){
  					$scope.model.projectvoe = value;
  					$window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
  					return false;
  				}
  			});
    	  }
    	// 权限控制
		powerService.powerRoutesList().then(
			function(powerList) {
				$scope.buttonPower = {
					isSecretKeyList : powerService.isPower('secretkey_list'),// SSH密钥列表
			};
		});
    	  /*if(!$scope.model.projectvoe || $scope.model.projectvoe==null||$scope.model.projectvoe==undefined){
    		  $state.go('login');
    		  sessionStorage.clear();
    	  }*/
	  $scope.$watch('model.projectvoe.projectId' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		if(newVal==null||newVal==''||newVal=='null'){
	    			$scope.dcId = $scope.model.projectvoe.dcId;
	    			angular.forEach($scope.cloudprojectList, function (value) {
	    				if(oldVal == value.projectId){
	    					$scope.model.projectvoe = value;
	    					return false;
	    				}
	    			});
	    			eayunHttp.post('cloud/project/findProByDcId.do',{dcId : $scope.dcId}).then(function (response){
	    	    		if(response.data){
	    	    			eayunModal.warning("您在该数据中心下没有具体资源的访问权限");
	    	    		}else{
	    	    			eayunModal.warning("您在该数据中心下没有任何项目");
	    	    		}
	    	    	});
	    		}else{
	    			$window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
	    		}
	    	}
	    });
 // })
/*  .controller('FireWallCtrl', function ($scope,$state,eayunHttp,eayunModal){*/
	  
	  /*$scope.myTable = {
		      source: 'safety/firewall/getFireWallList.do',
		      api : {},
		      getParams: function () {
			        return {
			        	prjId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '',
			        	dcId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
			        	name :  $scope.model.name || ''
				};
		      }
		    };*/
	  /**
       * 当前位置的导航栏
       */
      $rootScope.navList = function(list,cucrentName,type){
    	  $scope.navLists = list;
    	  $scope.cucrentName = cucrentName;
    	  $scope.type = type;
      };
  });