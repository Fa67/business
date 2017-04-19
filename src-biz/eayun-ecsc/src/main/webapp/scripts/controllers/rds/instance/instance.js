/**
 * Created by eayun on 2017/2/22.
 */
'use strict';

angular.module('eayunApp.controllers')

    .config(function ($stateProvider, $urlRouterProvider) {

		$stateProvider.state('buy.buyinstance', {
			url: '/buyinstance/:orderNo/:backupId',
			templateUrl: 'views/rds/instance/buyinstance.html',
			controller: 'RdsBuyInstanceCtrl',
			controllerAs: 'buyInstance'
		})
		.state('buy.buyslaveinstance', {
			url: '/buyslaveinstance/:rdsId/:orderNo',
			templateUrl: 'views/rds/instance/buyslaveinstance.html',
			controller: 'RdsBuySlaveInstanceCtrl',
			controllerAs: 'buySlaveInstance'
		})
		.state("buy.verifyInstance", {
                url: "/verifyInstance/:source/:rdsId",
                templateUrl: "views/rds/instance/verifyinstance.html",
                controller: "VerifyInstanceCtrl",
                controllerAs: "verifyInstance"
        });

    })

    .controller('RdsInstanceCtrl', ['$rootScope', '$scope', '$state', 'powerService', 'RDSInstanceService', 'eayunModal', 
                                    function ($rootScope, $scope, $state, powerService, RDSInstanceService, eayunModal) {

        var routeList = [];
        $rootScope.navList(routeList, 'MySQL');
        $scope.openRdsId = null; // 展开从库的主库的ID
        $scope.isShow = false;  // 是否展开从库
        $scope.query = {};

      /**
  	   * TODO 需要维护按钮的权限
  	   * 新增按钮权限控制
  	   */
  	  powerService.powerRoutesList().then(function(powerList){
  			$scope.instanceListPermissions = {
  					createRdsInstance : powerService.isPower('rds_instance_create'),	//创建数据库实例
  					renewRdsInstance : powerService.isPower('rds_instance_renew')	//数据库实例续费
  			};
  		});
        /**
         * 查询当前sessionStore 是否存在用户信息
         */
        $scope.checkUser = function (){
        	var user = sessionStorage["userInfo"];
        	 if(user){
        		 user = JSON.parse(user);
        		 if(user&&user.userId){
        			 return true;
        		 }
        	 }
        	return false;
        };
        
        $scope.options = {
                searchFn: function () {
                	if(!$scope.checkUser()){
                    	return ;
                    }
                	$scope.myTable.api.draw();
                },
                placeholder:"请输入查询内容",
                select: [{name: '名称'}, {ips: 'IP地址'}]
            };
        
        var rds = this;
        
        $scope.myTable = {
        	      source: 'rds/instance/getList.do',
        	      api : {},
        	      getParams: function () {
        	        return {
        	        	prjId :  $scope.model.projectvoe ? $scope.model.projectvoe.projectId : '',
        	        	dcId :   $scope.model.projectvoe ? $scope.model.projectvoe.dcId : '',
        				queryType : $scope.search ? $scope.search.key :'',
        	        	title :  $scope.search ? $scope.search.value:'',
        	    		status : $scope.query ? $scope.query.status :'',
        	    		versionId : $scope.query ?$scope.query.versionId :''
        	        };
        	      },
        	      result: []
        };
        /**
         * 监听列表上的查询状态
         */
        $scope.$watch('model.projectvoe', function(newVal,oldVal){
        	if(newVal !== oldVal){
        		$scope.myTable.api.draw(); // 重新加载列表
				$scope.init();// 获取版本
        	}
        });
        /**
         * 监听实例状态的变更
         */
        $scope.$watch('query.status' , function(newVal,oldVal){
        	if(newVal !== oldVal){
        		$scope.myTable.api.draw();
        	}
        });
        /**
         * 监听版本的变更
         */
        $scope.$watch('query.versionId', function (newVal, oldVal){
        	if(newVal != oldVal){
        		$scope.myTable.api.draw();
        	}
        });
        /**
         * 选择数据库实例状态
         */
        $scope.selectRdsStatus = function(item,event){
        	$scope.query.status = null;
        	if(item.nodeNameEn != '-1'){
        		$scope.query.status = item.nodeNameEn;
        	}
        };
        /**
         * 选择数据库版本
         */
        $scope.selectRdsVersion = function (item, event) {
        	$scope.query.versionId = null;
        	if(item.nodeNameEn != '-1'){
        		$scope.query.versionId = item.nodeNameEn;
        	}
        };
        $scope.init = function (){
        	// 实例状态
        	$scope.rdsStatusList = [{
        		'nodeNameEn':'-1',
        		'nodeName':'全部状态'
        	},{
        		'nodeNameEn':'ACTIVE',
        		'nodeName':'运行中'
        	},{
        		'nodeNameEn':'ERROR',
        		'nodeName':'故障'
        	},{
        		'nodeNameEn':'1',
        		'nodeName':'余额不足'
        	},{
        		'nodeNameEn':'2',
        		'nodeName':'已到期'
        	}];
        	// 实例版本
        	$scope.rdsVersionList = [];
        	$scope.rdsVersionList[0] = {
        			'nodeNameEn':'-1',
        			'nodeName':'全部版本'
        	};
        	RDSInstanceService.getVersionList($scope.model.projectvoe.dcId).then(function (response) {
        		var i = 1;
        		angular.forEach(response.data, function (value, key) {
        			$scope.rdsVersionList[i++] = {
                			'nodeNameEn':value.versionId,
                			'nodeName': value.versionName
                	};
      		  	});
        	});
        };
        /**
         * 云数据库状态 显示
         */
        $scope.checkrdsStatus =function (model){
        	if('1' == model.chargeState || '2' == model.chargeState || '3' == model.chargeState){
        		return 'ey-square-disable';
        	}else if(model.rdsStatus && model.rdsStatus == 'ACTIVE' || model.rdsStatus&&model.rdsStatus == 'RESTART_REQUIRED'){
    			return 'ey-square-right';
    		}else if(model.rdsStatus && (model.rdsStatus == 'ERROR' || model.rdsStatus == 'BLOCKED' || model.rdsStatus == 'FAILED')){
    			return 'ey-square-error';
    		}else{
    			return'ey-square-warning';
    		}
        };
        
        /**
         * 展开和关闭操作
         */
        $scope.isShowSlave = function (rds){
        	rds.isShow = !rds.isShow;
        }
        /**
         * 管理云主机
         */
        $scope.managerds = function (rdsId){
        	$state.go('app.rds.detail.dbinstance',{"rdsId":rdsId});
        };
        /**
         * 点击创建实例
         */
        $scope.buyInstance = function () {
        	$state.go('buy.buyinstance',{orderNo:'000000'});
        };
        /**
         * 续费操作
         */
        $scope.renewInstance = function (rds) {
        	var result = eayunModal.open({
    	        templateUrl: 'views/rds/instance/renewinstance.html',
    	        controller: 'RenewInstanceCtrl',
    	        controllerAs: 'renewInstance',
    	        backdrop:'static',
    	        resolve: {
    	            item:function (){
                		return rds;
                	}
    	        }
    	      });
        	result.result.then(function (value) {
        	}, function () {
        	});
        };
        /**
         * 中间状态的刷新
         */
        $scope.refresh = function () {
        	var i, item;
        	var middleStatue = ['REBOOT','RESIZE','BACKUP','SHUTDOWN','DETACH','RESTART_REQUIRED'];
            for (i = 0; i < $scope.myTable.result.length; i++) {
                item = $scope.myTable.result[i];
                if (middleStatue.indexOf(item.rdsStatus.toUpperCase()) >= 0) {
                	$scope.myTable.api.refresh();
                    break;
                }else if(item.children){
                	var itemChildrens = item.children;
                	var j, itemChildren;
                	for(j = 0; j < itemChildrens.length; j++){
                		itemChildren = itemChildrens[j];
                		if(middleStatue.indexOf(itemChildren.rdsStatus.toUpperCase()) >= 0){
                			$scope.myTable.api.refresh();
                			break;
                		}
                	}
                }
            }
        };
        $scope.init();
    }]);
