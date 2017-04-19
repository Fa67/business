/**
 * Created by eayun on 2017/2/22.
 */
'use strict';

angular.module('eayunApp.controllers')

    .config(function ($stateProvider, $urlRouterProvider) {
    	$urlRouterProvider.when('/app/rds/monitor/:instanceId', '/app/rds/monitor/:instanceId/monitorcpu');
    	$stateProvider.state('app.rds.monitor',{
        	url: '/monitor/:instanceId',
        	templateUrl: 'views/rds/instance/monitor.html',	//左侧栏列表
        	controller: 'RdsMonitorController'
        })
        .state('app.rds.monitor.resmntimg',{
	    	templateUrl: 'views/monitor/resourcemonitor/clouddata/detail.html',	//查询条件
	    	controller: 'RdsDetailMonitorCtrl'
        })
        .state('app.rds.monitor.resmntimg.monitorCPU',{//CPU
	    	url: '/monitorcpu',
	    	templateUrl: 'views/monitor/resourcemonitor/clouddata/monitorCPU.html',
	    	controller: 'RDSCPUCtrl'
        })
        .state('app.rds.monitor.resmntimg.monitorRam',{//内存
	    	url: '/monitorram',
	    	templateUrl: 'views/monitor/resourcemonitor/clouddata/monitorRam.html',
	    	controller: 'RDSRamCtrl'
        })
	    .state('app.rds.monitor.resmntimg.monitorVol',{//磁盘使用率
	    	url: '/monitorvol',
	    	templateUrl: 'views/monitor/resourcemonitor/clouddata/monitorVol.html',
	    	controller: 'RDSVolCtrl'
	    })
	    .state('app.rds.monitor.resmntimg.monitorNet',{//网卡速率
	    	url: '/monitornet',
	    	templateUrl: 'views/monitor/resourcemonitor/clouddata/monitorNet.html',
	    	controller: 'RDSNetCtrl'
	    })
	    .state('app.rds.monitor.resmntimg.monitorDisk',{//磁盘速率
	    	url: '/monitordisk',
	    	templateUrl: 'views/monitor/resourcemonitor/clouddata/monitorDisk.html',
	    	controller: 'RDSDiskCtrl'
	    });
    })

    .controller('RdsInstanceDetailInstanceCtrl', ['$rootScope', '$scope', '$state', 'powerService', 'eayunHttp', 'eayunModal', '$stateParams', 'toast', 'RDSBackupService',
                                                  'RDSInstanceService','DatacenterService',
        function ($rootScope, $scope, $state, powerService, eayunHttp, eayunModal, $stateParams, toast, RDSBackupService, RDSInstanceService, DatacenterService ) {
            var routeList = [{'router': 'app.rds.instance', 'name': 'MySQL'}];
            $rootScope.navList(routeList, '实例详情', 'detail');

            powerService.powerRoutesList().then(function(powerList){
                $scope.modulePower = {
                	instanceRestart: powerService.isPower('rds_instance_restart'), // 重启
                	instanceCreateReplica: powerService.isPower('rds_instance_createreplica'),// 创建从库
                	instanceDetachReplica: powerService.isPower('rds_instance_detachreplica'),// 从库升级为主库
                	instanceMonitor: powerService.isPower('rds_instance_monitor'), //　查看监控
                	instanceResize: powerService.isPower('rds_instance_resize'), // 升降规格
                	instanceDelete: powerService.isPower('rds_instance_delete'), //　删除实例
                	instanceModifyConfiguration: powerService.isPower('rds_instance_modifyconfiguration'), // 修改配置
                	instanceEdit: powerService.isPower('rds_instance_edit'), // 修改名称和描述
                    rdsBackupView: powerService.isPower('rds_backup_view'),
                    rdsBackupViewAuto: powerService.isPower('rds_backup_view_auto'),
                    rdsBackupViewManual: powerService.isPower('rds_backup_view_manual'),
                    rdsBackupCreateInstance: powerService.isPower('rds_backup_createinstance'),
                    rdsBackupDelete: powerService.isPower('rds_backup_delete'),
                    rdsLogDownload: powerService.isPower('rds_log_download')				//RDS日志下载
                };
            });

            $scope.rdsNameEditable = false;  // 名称编辑
            $scope.rdsDescEditable = false;  // 描述编辑
            $scope.checkRdsName = true ;
            $scope.checkEditBtn = true;
            $scope.model = {};
            var item = {}; // 用于名称和描述的修改操作
            /**
             * 查询当前sessionStore 是否存在用户信息
             */
            $scope.checkUser = function () {
                var user = sessionStorage["userInfo"]
                if (user) {
                    user = JSON.parse(user);
                    if (user && user.userId) {
                        return true;
                    }
                }
                return false;
            };
            /**
             * 刷新界面
             */
            $scope.refreshRds = function () {
                if (!$scope.checkUser()) {
                    return;
                }
                $scope.refreshRdsDetail();
                $scope.getCurrentManualBackupNumber();
                $scope.getAutoBackupEnableStatus();

                // 标签功能 取消
                /*eayunHttp.post('tag/getResourceTagForShowcase.do', {
                    resType: 'rds',
                    resId: $stateParams.rdsId
                }).then(function (response) {
                    $scope.tag = response.data;
                });*/
            };
            /**
             * 中间状态的刷新
             */
            $scope.refresh = function () {
            	var i, item;
            	var middleStatue = ['REBOOT','RESIZE','BACKUP','SHUTDOWN','DETACH'];
        		if($scope.model.rdsStatus){
        			if(middleStatue.indexOf($scope.model.rdsStatus.toUpperCase()) >= 0){
        				$scope.refreshRds();
        			}
        		}
            };
            /**
             * 云数据库状态 显示
             */
            $scope.chooseRdsStatusClass = function (model) {
                $scope.rdsStatusClass = '';
                if ('1' == model.chargeState || '2' == model.chargeState || '3' == model.chargeState) {
                    $scope.rdsStatusClass = 'ey-square-disable';
                    return;
                }else if (model.rdsStatus && (model.rdsStatus == 'ACTIVE' || model.rdsStatus == 'RESTART_REQUIRED')) {
                    $scope.rdsStatusClass = 'ey-square-right';
                }else if (model.rdsStatus && (model.rdsStatus == 'ERROR' || model.rdsStatus == 'BLOCKED' || model.rdsStatus == 'FAILED')) {
                    $scope.rdsStatusClass = 'ey-square-error';
                }else {
                    $scope.rdsStatusClass = 'ey-square-warning';
                }
            };
            $scope.refreshRdsDetail = function () {
                if (!$scope.checkUser()) {
                    return;
                }
                $scope.rdsStatusClass = '';
                RDSInstanceService.getRdsById($stateParams.rdsId).then(function (response) {
                	$scope.model = response.data;
                	$scope.chooseRdsStatusClass($scope.model);
                    item = {
                        name:$scope.model.rdsName,
                        desc:$scope.model.rdsDescription
                    };
                }, function () {
                	$state.go('app.rds.instance');
                });
            };
            
          // ---------------------------------------------  修改名称以及描述操作开始  ---------------------------------------  
            
          /**
      	   * 将云数据库名称、描述变为可编辑状态
      	   */
      	  $scope.edit = function(type){
      		  if(type == 'rdsName'){
      			  $scope.rdsNameEditable = true;
      			  $scope.hintNameShow = true;
      			  $scope.rdsDescEditable = false;
      			  $scope.hintDescShow = false;
      			  $scope.model.rdsDescription = item.desc;
      		  }
      		  if(type == 'rdsDesc'){
      			  $scope.rdsNameEditable = false;
      			  $scope.hintNameShow = false;
      			  $scope.rdsDescEditable = true;
      			  $scope.hintDescShow = true;
      			  $scope.model.rdsName = item.name;
      		  }
      	  };
      	  /**
    	   * 取消云数据库名称、描述的可编辑状态
    	   */
    	  $scope.cancleEdit = function (type){
    		  if(type == 'rdsName'){
    			  $scope.rdsNameEditable = false;
    			  $scope.hintNameShow = false;
    			  $scope.model.rdsName = item.name;
    		  }
    		  if(type == 'rdsDesc'){
    			  $scope.rdsDescEditable = false;
    			  $scope.hintDescShow = false;
    			  $scope.model.rdsDescription = item.desc;
    		  }
    	  };
    	  /**
    	   * 校验云数据库实例修改重名
    	   */
    	  $scope.checkRdsNameExist = function (){
    		  if($scope.model && $scope.model.rdsName){
    			  var cloudRdsInstance = {
    					  prjId:$scope.model.prjId,
    					  rdsId:$scope.model.rdsId, 
    					  rdsName:$scope.model.rdsName, 
    			  };
    			  $scope.checkEditBtn = false;
    			  RDSInstanceService.checkRdsNameExist(cloudRdsInstance).then(function (response){
    				  $scope.checkRdsName = response;
    			  });
    			  $scope.checkEditBtn = true;
    		  }
    	  };
          $scope.saveEdit = function (type){
        	  RDSInstanceService.saveEdit($scope.model).then(function (response) {
        		  if(response.data!=null){
    				  if(type == 'rdsName'){
    					  $scope.rdsNameEditable = false;
    					  $scope.hintNameShow = false;
    					  item.name = $scope.model.rdsName;
    					  toast.success('MySQL实例' + DatacenterService.toastEllipsis(item.name, 8) +'修改成功');
    				  }
    				  if(type == 'rdsDesc'){
    					  $scope.rdsDescEditable = false;
    					  $scope.hintDescShow = false;
    					  item.desc = $scope.model.rdsDescription;
    					  toast.success('MySQL实例' + DatacenterService.toastEllipsis($scope.model.rdsName, 8) +'修改成功');
    				  }
    				  $scope.refreshRds();
    			  }
        	  }, function () {
        		  if(type == 'rdsName'){
					  $scope.rdsNameEditable = false;
					  $scope.hintNameShow = false;
				  }
				  if(type == 'rdsDesc'){
					  $scope.rdsDescEditable = false;
					  $scope.hintDescShow = false;
				  }
				  $scope.refreshRds();
        	  });
          };
    	 // ---------------------------------------------  修改名称以及描述操作结束  ---------------------------------------
          
       // ---------------------------------------------  修改数据库实例配置操作开始  -----------------------------------------
      /**
	   * 修改数据库实例配置
	   */
	  $scope.modifyConfiguration = function(item){
		  
	      var result = eayunModal.open({
	    	backdrop:'static',
	        templateUrl: 'views/rds/instance/detail/modifyconfiguration.html',
	        controller: 'ModifyConfigurationCtrl',
	        resolve: {
	      	  item:function(){
	      		  return item;
	      	  }
	        }
	      }).result;
	      result.then(function (value){
	    	 $scope.refreshRds();
	      }, function () {
	    	  $scope.refreshRds();
	      });
	  
	  };
       // ---------------------------------------------  修改数据库实例配置操作结束  -----------------------------------------
	  // ---------------------------------------------  删除数据库实例操作开始----------------------------------------------------
	  $scope.deleteRdsInstance = function(item){
		  if (item.rdsStatus == 'BACKUP' && item.chargeState != '0') {
			  eayunModal.warning("MySQL实例"+ item.rdsName +"正在备份中，请稍后重试");
			  return;
		  }
	      var result = eayunModal.open({
	    	backdrop:'static',
	        templateUrl: 'views/rds/instance/detail/deleterdsinstance.html',
	        controller: 'DeleteRdsInstanceCtrl',
	        resolve: {
	      	  item:function(){
	      		  return item;
	      	  }
	        }
	      }).result;
	      result.then(function (value){
	    	 $scope.refreshRds();
	      }, function () {
	      });
	  
	  };
	  // ---------------------------------------------  删除数据库实例操作结束----------------------------------------------------
          
          $scope.restart = function(rds) {
        	  eayunModal.confirm('确定要重启MySQL实例？重启期间无法提供服务').then(function (){
    			  RDSInstanceService.restart(rds).then(function (response){
    				  toast.success('云数据库实例重启中',2000);
    				  $scope.refreshRds();
    			  });
    		  }, function () {
    			  $scope.refreshRds();
    		  });
          }
       // ---------------------------------------------  从库升主库操作开始----------------------------------------------------
          $scope.detachReplica = function (item){
        	  var result = eayunModal.open({
      	    	backdrop:'static',
      	        templateUrl: 'views/rds/instance/detail/detachreplica.html',
      	        controller: 'DetachReplicaCtrl',
      	        resolve: {
      	      	  item:function(){
      	      		  return item;
      	      	  }
      	        }
      	      }).result;
      	      result.then(function (value){
      	    	 $scope.refreshRds();
      	      }, function () {
      	    	 $scope.refreshRds();
      	      });
          }
       // ---------------------------------------------  从库升主库操作结束----------------------------------------------------
          $scope.buySlaveInstance = function (rds) {
			  RDSInstanceService.checkRdsInstanceOrderExsit(rds.rdsId, true, false).then(function () {
				  $state.go('buy.buyslaveinstance', {rdsId: rds.rdsId});
			  }, function (message){
				  eayunModal.warning(message);
			  });
		  };
          $scope.resizeRdsInstance = function (rds) {
        	  var result = eayunModal.open({
    			  templateUrl: 'views/rds/instance/detail/resizeinstance.html',
    			  controller: 'ResizeInstanceController',
    			  controllerAs: 'resizeInstance',
    			  backdrop:'static',
    			  resolve:{
    				  item:function (){
    					  return rds;
    				  }
    			  }
    		  }).result;
    		  result.then(function (){
    			  $scope.refreshRds();
    		  });
          };
          
          /**
    	   * 打开云监控
    	   */
    	  $scope.showRdsMonitor = function (_rds) {
    	  	$state.go('app.rds.monitor',{instanceId: _rds.rdsId}); // 跳转后的URL;
    	  };
            $scope.getCurrentManualBackupNumber = function () {
                RDSBackupService.getCurrentManualBackupCount($stateParams.rdsId).then(function (response) {
                    $scope.currentManualBackupNumber = response;
                });
            };
            $scope.getAutoBackupEnableStatus = function(){
                RDSBackupService.getAutoBackupEnableStatus($stateParams.rdsId).then(function (response) {
                    $scope.isAutoBackupEnabled = response.isAutoBackupEnabled;
                    $scope.scheduleTime = response.scheduleTime;
                });
            };

            $scope.refreshRds();

			//pop框方法
			$scope.openPopBox = function(rds){
				$scope.descShow = true;
				$scope.description = rds.value;
			};
			$scope.closePopBox = function(){
				$scope.descShow = false;
			};

            $scope.createManualBackup = function () {
                var result = eayunModal.open({
                    templateUrl: 'views/rds/backup/createbackup.html',
                    controller: 'RDSManualBackupCreateCtrl',
                    controllerAs: 'mbc',
                    resolve: {
                        instanceId: function () {
                            return $scope.model.rdsId;
                        },
                        instanceName: function () {
                            return $scope.model.rdsName;
                        },
                        maxManualBackupCount: function () {
                            var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
                            var projectId = dcPrj.projectId;
                            return RDSBackupService.getMaxManualBackupCount(projectId).then(function (response) {
                                return response;
                            });
                        }

                    }
                });

                result.result.then(function () {
                    toast.success('手动备份创建中');
                    $scope.refreshRds();
                });
            };
            $scope.showBackups = function (_instanceId, _category) {
                $state.go("app.rds.backup", {params: _instanceId + ',' + _category});
            };

            $scope.enabledAutoBackup = function(_instanceId){
                var result = eayunModal.open({
                    templateUrl: 'views/rds/backup/enableautobackup.html',
                    controller: 'RDSAutoBackupEnableCtrl',
                    controllerAs: 'abe',
                    resolve: {
                        instanceId: function () {
                            return _instanceId;
                        },
                        maxAutoBackupCount: function(){
                            var projectId = JSON.parse(sessionStorage["dcPrj"]).projectId;
                            return RDSBackupService.getMaxManualBackupCount(projectId).then(function (response) {
                                return response;
                            });
                        }
                    }
                });
                result.result.then(function () {
                    toast.success('自动备份开启成功');
                    $scope.refreshRds();
                });
            };
            $scope.disabledAutoBackup = function(_instanceId){
                var result = eayunModal.open({
                    templateUrl: 'views/rds/backup/disableautobackup.html',
                    controller: 'RDSAutoBackupDisableCtrl',
                    controllerAs: 'abd',
                    resolve: {
                        instanceId: function () {
                            return _instanceId;
                        }
                    }
                });
                result.result.then(function () {
                    toast.success('自动备份关闭成功');
                    $scope.refreshRds();
                });
                //RDSBackupService.disabledAutoBackup(_instanceId).then(function () {
                //    toast.success('关闭自动备份成功');
                //    $scope.refreshRds();
                //}, function () {
                //    toast.success('关闭自动备份失败');
                //});

            };

            /**
             * RDS 下载日志的界面
             */
            $scope.rdsLog = function () {
                var result = eayunModal.open({
                    templateUrl: 'views/rds/instance/detail/log.html',
                    controller: 'RDSInstanceLogCtrl',
                    resolve: {
                        item: function () {
                            return $scope.model;
                        }
                    }
                }).result;

                result.then(function () {
                    $scope.refreshRds();
                });
            };
            
    }])
    /**
     * RDS Instance Log Manage
     */
    .controller('RDSInstanceLogCtrl',function ($scope,eayunHttp,toast,eayunModal,item,$modalInstance,$state,$http,$timeout){
		$scope.model = {};
		$scope.model.type = 'DBLog';
		var date = new Date();
		$scope.model.minDate =  new Date(date - 30*24*60*60*1000);
		$scope.model.startDate = new Date(date - 7*24*60*60*1000);
		$scope.model.endDate = date;
		$scope.item = angular.copy(item);
		
		$scope.myTable = {
	      source: 'rds/log/getloglist.do',
	      api : {},
	      getParams: function () {
	        return {
	        	rdsInstanceId : $scope.item.rdsId,
	        	logType : $scope.model.type,
	        	startDate : $scope.model.startDate,
	        	endDate : $scope.model.endDate
	        };
	      }
	    };
		
		/**
		 * 关闭界面
		 */
		$scope.cancel = function(){
			$modalInstance.close();
		};
		
		
		/**
		 * 选择日志类型
		 */
		$scope.selectType = function(type){
			$scope.model.type = type;
			
			$scope.queryLogList();
		};
		
		
		/**
		 * 发布某种类型的日志
		 */
		$scope.publish = function(){
			$scope.publishBtn = true;
			var data = {};
			data.dcId = $scope.item.dcId;
			data.prjId = $scope.item.prjId;
			data.rdsInstanceId = $scope.item.rdsId;
			data.logType = $scope.model.type;
			data.rdsName = $scope.item.rdsName;
			
			 eayunHttp.post('rds/log/publish.do',data).then(function (response){
				 if(response.data && response.data.respCode == '400000'){
					 $scope.publishBtn = false;
					 $scope.queryLogList();
				 }
				 else {
					 $scope.publishBtn = false;
				 }
			 },function(){
				 $scope.publishBtn = false;
			 });
		};
		
		$scope.queryLogList = function(){
			$scope.refresh();
			$scope.myTable.api.draw();
		};
		
		/**
		 * 下载日志
		 */
		$scope.download = function(params){
			var data = {};
			data.dcId = $scope.item.dcId;
			data.prjId = $scope.item.prjId;
			data.rdsName = $scope.item.rdsName;
			 eayunHttp.post('rds/log/download.do',data).then(function (response){
				 if(response.data && response.data.respCode == '400000'){
					 var host =response.data.data;
					 var url =host + params.url;
					 var a = document.createElement("a");  
					 document.body.appendChild(a);  
					 a.href = url;
					 a.download=params.logName + '.log';
					 a.click();
				 }
			 });
//			$http.get("http://192.168.8.80/swift/v1/database_logs_c2a2c9eec47f4756925b474c3250982d/23f504c3-c194-4691-bce4-da077b22a3c1/mysql-general/log-2017-03-28T07:25:23.551437");
		};
		
		 
		$scope.refresh = function(){
			 eayunHttp.post('rds/log/checkLogpublishing.do',$scope.item.rdsId).then(function (response){
				 if(response.data){
					 $scope.logPubishingState = response.data;
					 if($scope.logPubishingState && !$scope.logPubishingState.data){
						 $scope.myTable.api.draw();
					 }
				 }
			 });
		};
		
		$scope.$watch("logPubishingState",function (newVal,oldVal){
			if($scope.logPubishingState && $scope.logPubishingState.data){
				$timeout($scope.refresh,5000);
			}
		});
		
		$scope.refresh();
		
	})
   .controller('RdsMonitorController',function ($rootScope,$scope,eayunHttp,$stateParams, eayunModal){
	  var routeUrl = "app.rds.detail.dbinstance({'rdsId':'"+$stateParams.instanceId+"'})";
	  var list=[{'router' : 'app.rds.instance', 'name' : 'MySQL'}, {'router' : routeUrl, 'name' : '实例详情'}];
	  $rootScope.navList(list,'监控信息','detail');
	  $scope.instanceId = $stateParams.instanceId;
  });