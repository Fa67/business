'use strict';

angular.module('eayunApp.controllers')
/**
 * @ngdoc function
 * @name appRouter
 * @description
 * 配置路由
 */
  .config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('/app/cloud','/app/cloud/cloudhost');
    $stateProvider.state('app.cloud.cloudhost', {
      url: '/cloudhost',
      templateUrl:'views/cloudhost/main.html'
    })
    .state('app.cloud.detail',{
    	url: '/detail/:detailType',
        templateUrl: 'views/cloudhost/detail.html',
        controller:'DetailController'
    });
    
    
  }).controller('DetailController',function ($scope ,eayunHttp ,eayunModal,$stateParams,$state,$timeout){
	  if($stateParams.detailType=='host'){
		  $scope.route = 'app.cloud.cloudhost.host';
		  $scope.name = '云主机';
	  }
	  else if($stateParams.detailType=='volume'){
		  $scope.route = 'app.cloud.cloudhost.volume';
		  $scope.name = '云硬盘';
	  }else if($stateParams.detailType=='image'){
		  $scope.route = 'app.cloud.cloudhost.image';
		  $scope.name = '镜像';
	  }
  })
  /**
   * @ngdoc function
   * @name eayunApp.controller:CloudhostCtrl
   * @description
   * # CloudhostCtrl
   * 云主机
   */
    .controller('CloudCtrl', function ($rootScope,$scope,$state, eayunHttp,projects,$window,eayunModal,$stateParams,$timeout) {
  	  /*点击左侧树“云主机”先查询出所有的数据中心与项目  开始*/
  	  $scope.model = {};
  	  $scope.cloudprojectList = projects;
  	  var daPrj = sessionStorage["dcPrj"];
  	  if(daPrj){
  		  daPrj = JSON.parse(daPrj);
  		  angular.forEach($scope.cloudprojectList, function (value,key) {
  			  if(value.projectId == daPrj.projectId){
  				  $scope.model.projectvoe = value;
  			  }
  		  });
  	  }else{
  		  angular.forEach($scope.cloudprojectList, function (value) {
  				if(value.projectId!=null&&value.projectId!=''&&value.projectId!='null'){
  					$scope.model.projectvoe = value;
  					$window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
  				}
  			});
  	  }
  	  /**
  	  if(!$scope.model.projectvoe || $scope.model.projectvoe==null||$scope.model.projectvoe==undefined){
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
      },true);
    
  	/*点击左侧树“云主机”先查询出所有的数据中心与项目  结束*/
      $scope.tabs = [
        {title: '云主机', target: 'app.cloud.cloudhost.host'},
        {title: '云硬盘', target: 'app.cloud.cloudhost.disk'},
        {title: '镜像', target: 'app.cloud.cloudhost.mirror'},
        {title: '安全组', target: 'app.cloud.cloudhost.group'}
      ];
      
      /**
       * 当前位置的导航栏
       */
      $rootScope.navList = function(list,cucrentName,type){
    	  $scope.navLists = list;
    	  $scope.cucrentName = cucrentName;
    	  $scope.type = type;
      };
    });