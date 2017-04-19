'use strict';
angular.module('eayunApp.controllers')

.config(function ($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.when('/app/monitor/detail/ldpoolcommon', 
	'/app/monitor/detail/:detailType/ldpoolcommon/:poolId');
	$urlRouterProvider.when('/app/monitor/detail/ldpoolmaster', 
	'/app/monitor/detail/:detailType/ldpoolmaster/:poolId');
    $stateProvider
    .state('app.monitor.monitorbar.resourcemonitor.ldpoolcommon', {
    	url: '/ldpoolcommon',
    	templateUrl: 'views/monitor/resourcemonitor/ldpool/common.html',	//负载均衡普通模式监控列表
    	controller: 'CommonMonitorMngCtrl',
    	resolve:{
        	  projects:function (eayunHttp){
        		  return eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
        	    	  return  response.data.data;
        	      });
        	  }
          }
    })
    .state('app.monitor.detail.ldpoolcommon',{
    	url: '/ldpoolcommon/:poolId',
    	templateUrl: 'views/monitor/resourcemonitor/ldpool/commondetail.html',
    	controller: 'CommonDetailMonitorCtrl'
    })
    .state('app.monitor.monitorbar.resourcemonitor.ldpoolmaster', {
    	url: '/ldpoolmaster',
    	templateUrl: 'views/monitor/resourcemonitor/ldpool/master.html',	//负载均衡主备模式监控列表
    	controller: 'MasterMonitorMngCtrl',
    	resolve:{
        	  projects:function (eayunHttp){
        		  return eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
        	    	  return  response.data.data;
        	      });
        	  }
          }
    })
    .state('app.monitor.detail.ldpoolmaster',{
    	url: '/ldpoolmaster/:poolId',
    	templateUrl: 'views/monitor/resourcemonitor/ldpool/masterdetail.html',
    	controller: 'MasterDetailMonitorCtrl'
    })
    ;
})
.controller('CommonMonitorMngCtrl',function(eayunStorage,$scope,$state,eayunHttp,projects,$interval,$location,$window){
	var navLists=eayunStorage.get('navLists');
	navLists.length=0;
	navLists.push({route:'app.monitor.monitorbar.resourcemonitor.ldpoolcommon',name:'资源监控'});
	$scope.cloudproList = projects;
	var monPrj = sessionStorage["dcPrj"];
	  if(monPrj){
		  monPrj = JSON.parse(monPrj);
		  angular.forEach($scope.cloudproList, function (value,key) {
			  if(value.projectId == monPrj.projectId){
				  $scope.ProModel = value;
			  }
		  });
	  }else{
		  $scope.ProModel = $scope.cloudproList[0];
		  $window.sessionStorage["dcPrj"] = JSON.stringify($scope.ProModel);
	  };
	  $scope.params={
			  poolName:'',
			  frequency : 20		//刷新时间20秒
	}
	$scope.commonPageTable = {
	      source: 'monitor/ldpool/getldpoolmonitorlist.do',
	      api:{},
	      getParams: function () {
	        return {
	        	projectId : $scope.ProModel.projectId || '',
	        	poolName : $scope.params.poolName || '',
	        	mode : '0'
	        };
	      }
	};
	var promise = null;
	$scope.updateClock = function(){
		if($location.absUrl().lastIndexOf("monitor/monitorbar/resourcemonitor/ldpoolcommon") != -1){
			$scope.params.frequency--;
			if($scope.params.frequency == 0){
				$scope.commonPageTable.api.refresh();
				$scope.params.frequency = 20;
			}
		}else{
			$interval.cancel(promise);
		}
	};
	promise = $interval(function(){
		$scope.updateClock();
	},1000);
	$scope.query = function(){
    	$scope.params.frequency = 20;
    	$scope.commonPageTable.api.draw();
    };
    $scope.$watch('ProModel.projectId' , function(newVal,oldVal){
    	if(newVal !== oldVal){
    		$scope.commonPageTable.api.draw();
    		$window.sessionStorage["dcPrj"] = JSON.stringify($scope.ProModel);
    	}
    });
	$scope.goCommonMonitorDetail = function(poolId){
		$state.go('app.monitor.detail.ldpoolcommon',{poolId:poolId,detailType:'resourceMonitor'});
	};
})
/** 负载均衡主备模式列表 */
.controller('MasterMonitorMngCtrl',function(eayunStorage,$scope,$state,eayunHttp,projects,$interval,$location,$window){
	var navLists=eayunStorage.get('navLists');
	navLists.length=0;
	navLists.push({route:'app.monitor.monitorbar.resourcemonitor.ldpoolmaster',name:'资源监控'});
	$scope.cloudproList = projects;
	var monPrj = sessionStorage["dcPrj"];
	  if(monPrj){
		  monPrj = JSON.parse(monPrj);
		  angular.forEach($scope.cloudproList, function (value,key) {
			  if(value.projectId == monPrj.projectId){
				  $scope.ProModel = value;
			  }
		  });
	  }else{
		  $scope.ProModel = $scope.cloudproList[0];
		  $window.sessionStorage["dcPrj"] = JSON.stringify($scope.ProModel);
	  };
	  $scope.params={
			  poolName:'',
			  frequency : 20		//刷新时间20秒
		}
		$scope.masterPageTable = {
		      source: 'monitor/ldpool/getldpoolmonitorlist.do',
		      api:{},
		      getParams: function () {
		        return {
		        	projectId : $scope.ProModel.projectId || '',
		        	poolName : $scope.params.poolName || '',
		        	mode : '1'
		        };
		      }
		};
		var promise = null;
		$scope.updateClock = function(){
			if($location.absUrl().lastIndexOf("monitor/monitorbar/resourcemonitor/ldpoolmaster") != -1){
				$scope.params.frequency--;
				if($scope.params.frequency == 0){
					$scope.masterPageTable.api.refresh();
					$scope.params.frequency = 20;
				}
			}else{
				$interval.cancel(promise);
			}
		};
		promise = $interval(function(){
			$scope.updateClock();
		},1000);
		$scope.query = function(){
	    	$scope.params.frequency = 20;
	    	$scope.masterPageTable.api.draw();
	    };
	    $scope.$watch('ProModel.projectId' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.masterPageTable.api.draw();
	    		$window.sessionStorage["dcPrj"] = JSON.stringify($scope.ProModel);
	    	}
	    });
		$scope.goMasterMonitorDetail = function(poolId){
			$state.go('app.monitor.detail.ldpoolmaster',{poolId:poolId,detailType:'resourceMonitor'});
		};
})
/** 负载均衡普通模式详情 */
.controller('CommonDetailMonitorCtrl',function(eayunStorage,$scope,$stateParams,eayunHttp,$state){
	var navLists=eayunStorage.get('navLists');
	if(navLists){
		navLists.length=0;
		navLists.push({route:'app.monitor.monitorbar.resourcemonitor.ldpoolcommon',name:'资源监控'});
		navLists.push({route:'app.monitor.detail.ldpoolcommon',name:'成员监控详情'});
	}
	$scope.today = new Date();
	eayunHttp.post('monitor/ldpool/getLdPoolDetailById.do',{ldPoolId:$stateParams.poolId}).then(function(response){
		$scope.commonModel = response.data;
	});
	$scope.data = {
		endTime : $scope.today,
		isRepair : '',
		memberName : '',
		healthName : '',
    	role : '',
		timeRange : 3
	};
	$scope.commonExpTable = {
	      source: 'monitor/ldpool/getldpoolexplist.do',
	      api:{},
	      getParams: function () {
	        return {
	        	endTime : $scope.data.endTime.getTime(),
	        	count : $scope.data.timeRange,
	        	poolId : $stateParams.poolId,
	        	role : $scope.data.role,
	        	memberName : $scope.data.memberName,
	        	healthName : $scope.data.healthName,
	        	isRepair : $scope.data.isRepair,
	        	mode : '0'
	        };
	      }
	};
	
	// 成员名称、负载均衡下拉
	$scope.memList = {};
	$scope.heaList = {};
	$scope.getNameList = function(poolId){
		eayunHttp.post('monitor/ldpool/getNameListById.do',{endTime : $scope.data.endTime.getTime(),
	    	count : $scope.data.timeRange,
	    	poolId : $stateParams.poolId,
	    	role : '',
	    	memberName : $scope.data.memberName,
	    	healthName : '',
	    	isRepair : $scope.data.isRepair,
	    	mode : '0'}).then(function(response){
	    	$scope.memList = response.data.memList;
	    	$scope.heaList = response.data.heaList;
	    });
	};
	$scope.getNameList();
	$scope.isChangeMemName = false;
	$scope.memNames = [
		           		{memberName: '', text: '成员名称'}
	           		];
	$scope.healths = [
		           		{healthName: '', text: '健康检查名称'}
	           		];
	$scope.memNameClicked = function (item, event) {
		$scope.isChangeMemName = true;
		$scope.data.memberName=item.memberName;
		$scope.data.healthName = '';
		$scope.commonExpTable.api.draw();
		$scope.getNameList();
	};
	$scope.healthClicked = function (item, event) {
		$scope.data.healthName=item.healthName;
		$scope.commonExpTable.api.draw();
	};
	$scope.$watch('memList' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.memNames = [{memberName: '', text: '成员名称'}];
	    		angular.forEach(newVal, function (value,key) {
	    			$scope.memNames.push({memberName: value.memberName, text: value.memberName});
	    		});
	    	}
	});
	$scope.$watch('heaList' , function(newVal,oldVal){
    	if(newVal !== oldVal){
    		if($scope.isChangeMemName){
    			$scope.healths=[];
    			$scope.healths.unshift({healthName: '', text: '健康检查名称',$$select:true});
    		}else{
    			$scope.healths = [{healthName: '', text: '健康检查名称'}];
    		}
    		angular.forEach(newVal, function (value,key) {
    			$scope.healths.push({healthName: value.healthName, text: value.healthName});
    		});
    		$scope.isChangeMemName = false;
    	}
	});
	// 是否需要修复
	$scope.repairStatus = [
		           		{isRepair: '', text: '是否需要修复'},
		           		{isRepair: '1', text: '是'},
		           		{isRepair: '0', text: '否'}
	           		];
	$scope.repairClicked = function (item, event) {
		$scope.data.isRepair=item.isRepair;
		$scope.commonExpTable.api.draw();
		$scope.getNameList();
	};
	$scope.$watch('data.endTime+data.timeRange' , function(newVal,oldVal){
		  if(newVal != oldVal){
			  $scope.commonExpTable.api.draw();
			  $scope.getNameList();
		  }
	  });
	$scope.gotoLdpooldetail = function(poolId){
		$state.go('app.net.loadbalaceDetail', {"poolId": poolId});
	};
})
/** 负载均衡主备模式详情 */
.controller('MasterDetailMonitorCtrl',function(eayunStorage,$scope,$stateParams,eayunHttp,$state){
	var navLists=eayunStorage.get('navLists');
	if(navLists){
		navLists.length=0;
		navLists.push({route:'app.monitor.monitorbar.resourcemonitor.ldpoolmaster',name:'资源监控'});
		navLists.push({route:'app.monitor.detail.ldpoolmaster',name:'成员监控详情'});
	}
	$scope.today = new Date();
	eayunHttp.post('monitor/ldpool/getLdPoolDetailById.do',{ldPoolId:$stateParams.poolId}).then(function(response){
		$scope.masterModel = response.data;
	});
	$scope.data = {
		endTime : $scope.today,
		isRepair : '',
		memberName : '',
    	healthName : '',
    	role : '',
		timeRange : 3
	};
	$scope.masterExpTable = {
	      source: 'monitor/ldpool/getldpoolexplist.do',
	      api:{},
	      getParams: function () {
	        return {
	        	endTime : $scope.data.endTime.getTime(),
	        	count : $scope.data.timeRange,
	        	poolId : $stateParams.poolId,
	        	role : $scope.data.role,
	        	memberName : $scope.data.memberName,
	        	healthName : $scope.data.healthName,
	        	isRepair : $scope.data.isRepair,
	        	mode : '1'
	        };
	      }
	};
	
	// 成员名称、负载均衡下拉
	$scope.memList = {};
	$scope.heaList = {};
	$scope.getNameList = function(poolId){
		eayunHttp.post('monitor/ldpool/getNameListById.do',{endTime : $scope.data.endTime.getTime(),
	    	count : $scope.data.timeRange,
	    	poolId : $stateParams.poolId,
	    	role : $scope.data.role,
	    	memberName : $scope.data.memberName,
	    	healthName : '',
	    	isRepair : $scope.data.isRepair,
	    	mode : '1'}).then(function(response){
	    	$scope.memList = response.data.memList;
	    	$scope.heaList = response.data.heaList;
	    });
	};
	$scope.getNameList();
	$scope.isChangeMemName = false;
	$scope.memNames = [
		           		{memberName: '', text: '成员名称'}
	           		];
	$scope.healths = [
		           		{healthName: '', text: '健康检查名称'}
	           		];
	$scope.memNameClicked = function (item, event) {
		$scope.isChangeMemName = true;
		$scope.data.memberName=item.memberName;
		$scope.data.healthName = '';
		$scope.masterExpTable.api.draw();
		$scope.getNameList();
	};
	$scope.healthClicked = function (item, event) {
		$scope.data.healthName=item.healthName;
		$scope.masterExpTable.api.draw();
	};
	$scope.$watch('memList' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.memNames = [{memberName: '', text: '成员名称'}];
	    		angular.forEach(newVal, function (value,key) {
	    			$scope.memNames.push({memberName: value.memberName, text: value.memberName});
	    		});
	    	}
	});
	$scope.$watch('heaList' , function(newVal,oldVal){
    	if(newVal !== oldVal){
    		if($scope.isChangeMemName){
    			$scope.healths=[];
    			$scope.healths.unshift({healthName: '', text: '健康检查名称',$$select:true});
    		}else{
    			$scope.healths = [{healthName: '', text: '健康检查名称'}];
    		}
    		angular.forEach(newVal, function (value,key) {
    			$scope.healths.push({healthName: value.healthName, text: value.healthName});
    		});
    		$scope.isChangeMemName = false;
    	}
	});
	// 是否需要修复
	$scope.repairStatus = [
		           		{isRepair: '', text: '是否需要修复'},
		           		{isRepair: '1', text: '是'},
		           		{isRepair: '0', text: '否'}
	           		];
	$scope.repairClicked = function (item, event) {
		$scope.data.isRepair=item.isRepair;
		$scope.masterExpTable.api.draw();
		$scope.getNameList();
	};
	// 角色
	$scope.roleStatus = [
		           		{role: '', text: '角色（全部）'},
		           		{role: 'Active', text: '主节点'},
		           		{role: 'Backup', text: '从节点'}
	           		];
	$scope.roleClicked = function (item, event) {
		$scope.data.role=item.role;
		$scope.masterExpTable.api.draw();
		$scope.getNameList();
	};
	$scope.$watch('data.endTime+data.timeRange' , function(newVal,oldVal){
		  if(newVal != oldVal){
			  $scope.masterExpTable.api.draw();
			  $scope.getNameList();
		  }
	  });
	
	$scope.gotoLdpooldetail = function(poolId){
		$state.go('app.net.loadbalaceDetail', {"poolId": poolId});
	};
})
;



