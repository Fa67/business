'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $stateProvider.state('app.net.netbar.loadbalance.healthmonitor', {//路由
      url: '/healthmonitor',
      templateUrl: 'views/net/loadbalance/healthmonitor/monitormng.html',
      controller: 'HealthMonitorCtrl'
    });
  })
  
  .controller('HealthMonitorCtrl', function ($rootScope, $scope, powerService,eayunModal,eayunHttp,toast) {
	  var list = [
		  {'router': 'app.net.netbar.loadbalance', 'name': '负载均衡'}
	  ];
	  var dcId = sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '';
	  var prjId = sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '';
	  $rootScope.navList(list, '健康检查');
	  $scope.searchFn = function () {
		  $scope.myTable.api.draw();
	  };
	  $scope.myTable = {
			  source: 'cloud/loadbalance/healthmonitor/getMonitorList.do',
		      api:{},
		      getParams: function () {
		        return {
		        	prjId :  prjId,
		        	dcId :  dcId,
		        	name :  $scope.search || ''
			};
		      }
	   };
	  /*更改数据中心*/
	  $scope.$watch('model.projectvoe', function(newValue, oldValue){
		  if (newValue !== oldValue) {
			  dcId = newValue.dcId;
			  prjId = newValue.projectId;
			  $scope.myTable.api.draw();
		  }
	  });

	  $scope.options = {
		  searchFn: function () {
			  if (!$scope.checkUser()) {
				  return;
			  }
			  $scope.myTable.api.draw();
		  }
	  };
	  
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.buttonPower = {
			isAddMonitor:powerService.isPower('loadmonitor_add'),	//添加成员
			isEditMonitor:powerService.isPower('loadmonitor_edit'),	//边界成员
			isDeleteMonitor:powerService.isPower('loadmonitor_delete'),	//删除成员
			isTagMonitor:powerService.isPower('loadmonitor_tag')	//标签
			};
	  });
	  
	  $scope.memJson = function (tagsStr){
		  var json ={};
		  if(tagsStr){
			  json= JSON.parse(tagsStr);
		  }
		  return json;
	  };
	//监视器[监视数据中心、项目id变化]
	  $scope.$watch('model.dcProject' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.myTable.api.draw();
	    	}
	    });
	//pop框方法
	  $scope.hintTagShow = [];
	  $scope.openTableBox = function(obj){
		  if(obj.type == 'tagName'){
			  $scope.hintTagShow[obj.index] = true;
		  }
		  $scope.ellipsis = obj.value;
	  };
	  $scope.closeTableBox = function(obj){
		  if(obj.type == 'tagName'){
			  $scope.hintTagShow[obj.index] = false;
		  }
	  };
  /**
     * 查询当前sessionStore 是否存在用户信息
     */
    $scope.checkUser = function (){
    	var user = sessionStorage["userInfo"]
    	 if(user){
    		 user = JSON.parse(user);
    		 if(user&&user.userId){
    			 return true;
    		 }
    	 }
    	return false;
    };
	//页面中回车键触发查询事件；
	  $(function () {
          document.onkeydown = function (event) {
       	   var e = event || window.event || arguments.callee.caller.arguments[0];
	       	if(!$scope.checkUser()){
	    		   return ;
	        }
       	   if (e && e.keyCode == 13) {
       	   $scope.myTable.api.draw();
       	   }
          };
      });
	  
	  
	 /*查询监控*/
	  $scope.getHealthMonitor = function(){
		  $scope.myTable.api.draw();
	  };
	  
	  /**
	   *创建健康检查 
	   */
	  $scope.createMonitor = function(){
		  var result = eayunModal.open({
		        title: '创建健康检查',
		        width: '600px',
		        templateUrl: 'views/net/loadbalance/healthmonitor/addmonitor.html',
		        controller: 'monitorAddCtrl',
		        resolve: {
		        	prjId:function (){
	            		return {prjId:sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : ''};
	            	}
		        }
		      });
		      result.result.then(function (value) {
	    		  $scope.myTable.api.draw();
		      }, function () {
		    	  $scope.myTable.api.draw();
		      });
	  };
	  
	  /**
	   * 编辑健康检查
	   */
	  $scope.editHealthMonitor = function(item){
		  var result = eayunModal.open({
		        title: '编辑健康检查',
		        width: '600px',
		        templateUrl: 'views/net/loadbalance/healthmonitor/editmonitor.html',
		        controller: 'editMonitorCtrl',
		        resolve: {
		        	item: function () {
		        	  return item;
		            }
		        }
		      });
		  
		  result.result.then(function (value) {
    		  $scope.myTable.api.draw();
	      }, function () {
	    	  $scope.myTable.api.draw();
	      });  
	  };
	  
	  /**
	   * 删除健康检查
	   */
	  $scope.deleteHealthMonitor = function (item){
		  eayunModal.confirm('确定要删除健康检查'+item.ldmName+'?').then(function () {
		        eayunHttp.post('cloud/loadbalance/healthmonitor/deleteHealthMonitor.do',item).then(function(response){
		        	if(response && response.data && response.data.respCode=='100000'){
		        		toast.success('删除健康检查成功');
		        	}
		        	$scope.myTable.api.draw();
		        });
		      });
	  };
	  
	  
	  /*标签*/
		$scope.tagResource = function(resType, resId){
			var result=eayunModal.open({
			    templateUrl: 'views/tag/tagresource.html',
			    controller: 'TagResourceCtrl',
			    	resolve: {
			    		resType : function () {
			                return resType;
			            },
			            resId : function(){
			            	return resId;
			            }
			        }
				});
				result.result.then(function () {
					$scope.myTable.api.draw();
			},function () {
				$scope.myTable.api.draw();
			});
		}; 
	   
  })
 /**
   *创建健康检查Controller
   *
   */
   .controller('monitorAddCtrl', function ($scope, eayunModal,eayunHttp ,prjId,toast, $modalInstance) {
	   $scope.model={};
	   $scope.model.delay = 15;
	   $scope.model.timeout = 10;
	   $scope.model.max_retries = 3;
	   $scope.checkName = true;
	   eayunHttp.post('cloud/vm/getProListByCustomer.do').then(function (response){
		  $scope.projectList = response.data;
		  angular.forEach($scope.projectList, function (value, key) {
	        if(value.projectId == prjId.prjId){
	        	$scope.model.project=value;
	        }
		  });
	   });  
	 
	   /**
	    * 确定
	    */
       $scope.commit = function () {
    	   var data = {
    			   dcId:$scope.model.project.dcId,
    			   prjId:$scope.model.project.projectId,
    			   ldmName:$scope.model.ldmName,
    			   ldmType:$scope.model.type,
    			   ldmDelay:$scope.model.delay,
    			   ldmTimeout:$scope.model.timeout,
    			   maxRetries:$scope.model.max_retries,
    			   urlPath:$scope.model.url_path
    	   };
    	   
    	   $scope.checkBtn = true;
    	   eayunHttp.post('cloud/loadbalance/healthmonitor/addHealthMonitor.do',data).then(function(response){
    		   if(response && response.data && response.data.respCode=='000000'){
    			   var name = $scope.model.ldmName.length>7?$scope.model.ldmName.substr(0,7)+'...':$scope.model.ldmName;
    			   toast.success("添加健康检查"+name+"成功");
				   $modalInstance.close();
    		   }
    		   else{
    			   $scope.checkBtn = false;
    		   }
    	   },function(){
    		   $scope.checkBtn = false;
    	   });
       };

	   $scope.cancel = function () {
		   $modalInstance.dismiss();
	   };
       
       /**
        * 校验名称重复
        */
       $scope.checkNameExsit = function (){
    	   var mname = $scope.model.ldmName;
    	   eayunHttp.post('cloud/loadbalance/healthmonitor/checkMonitorExsit.do',{ldmName:$scope.model.ldmName,prjId:$scope.model.project.projectId}).then(function (response){
    		   if(response && response.data){
    			   if(mname ===$scope.model.ldmName){
    				   $scope.checkName = response.data.data;
    			   }
    		   }
    	   });
       };
	  
  })
   /**
 * @ngdoc function
 * @name eayunApp.controller:editMemberCtrl
 * @description
 * # editMemberCtrl
 * 监控列表页-->编辑监控
 */
  .controller('editMonitorCtrl', function ($scope, eayunModal,eayunHttp,item,toast, $modalInstance) {
	  $scope.checkName = true;
	  $scope.model={};
	  $scope.model=angular.copy(item);
	  
	  /**
	    * 确定
	    */
      $scope.commit = function () {
   	   var data = {
   			   dcId:$scope.model.dcId,
   			   prjId:$scope.model.prjId,
   			   ldmName:$scope.model.ldmName,
   			   ldmId:$scope.model.ldmId,
   			   ldmDelay:$scope.model.ldmDelay,
   			   ldmTimeout:$scope.model.ldmTimeout,
   			   maxRetries:$scope.model.maxRetries,
   			   urlPath:$scope.model.urlPath
   	   };
   	   
   	   $scope.checkBtn = true;
   	   eayunHttp.post('cloud/loadbalance/healthmonitor/updateMonitor.do',data).then(function(response){
   		   if(response && response.data && response.data.respCode=='200000'){
   			   var name = $scope.model.ldmName.length>7?$scope.model.ldmName.substr(0,7)+'...':$scope.model.ldmName;
   			   toast.success("编辑健康检查"+name+"成功");
			   $modalInstance.close();
   		   }
   		   else{
   			   $scope.checkBtn = false;
   		   }
   	   },function(){
   		   $scope.checkBtn = false;
   	   });
      };

	  $scope.cancel = function () {
		  $modalInstance.dismiss();
	  };

    /**
     * 校验名称重复
     */
    $scope.checkNameExsit = function (){
    	var mname = $scope.model.ldmName;
 	   eayunHttp.post('cloud/loadbalance/healthmonitor/checkMonitorExsit.do',{ldmId:$scope.model.ldmId,ldmName:$scope.model.ldmName,prjId:item.prjId}).then(function (response){
 		   if(response && response.data){
 			  if(mname ===$scope.model.ldmName){
 				  $scope.checkName = response.data.data;
 			  }
 		   }
 	   });
    };
   
  })
  
  
  ;
