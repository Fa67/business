/**
 * Created by ZH.F on 2017/2/27.
 */
'use strict';

angular.module('eayunApp.service')
    .service('RDSInstanceService', ['eayunHttp', '$q', 'SysCode', function (eayunHttp, $q, SysCode) {

        var rds = this;

        /**
         * 获取指定数据库实例的详情
         */
        rds.getRdsById = function (_rds_id){
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/getRdsById.do', _rds_id).then(function (resp) {
                switch (resp.data.respCode) {
                    case SysCode.error:
                        deferred.reject();
                        break;
                    case  SysCode.success:
                        deferred.resolve(resp.data);
                        break;
                    default:
                        deferred.reject();
                        break;
                }
        	});
        	return deferred.promise;
        };
        /**
         * 校验是否重名
         */
        rds.checkRdsNameExist = function (_cloudRdsInstance) {
            var deferred = $q.defer();
            eayunHttp.post('rds/instance/checkRdsNameExist.do', {
                prjId: _cloudRdsInstance.prjId,
                rdsId: _cloudRdsInstance.rdsId,
                rdsName: _cloudRdsInstance.rdsName
            }).then(function (resp) {
            	deferred.resolve(resp.data);
            });
            return deferred.promise;
        };
        
        /**
         * 修改名称和描述
         */
        rds.saveEdit = function (_cloudRdsInstance) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/modifyRdsInstance.do', {
            	dcId: _cloudRdsInstance.dcId,
                prjId: _cloudRdsInstance.prjId,
                rdsId: _cloudRdsInstance.rdsId,
                rdsName: _cloudRdsInstance.rdsName,
                rdsDescription: _cloudRdsInstance.rdsDescription
            }).then(function (resp) {
                switch (resp.data.respCode) {
                    case SysCode.error:
                        deferred.reject(resp.data.message);
                        break;
                    case  SysCode.success:
                        deferred.resolve(resp.data);
                        break;
                    default:
                        deferred.reject();
                        break;
                }
            });
            return deferred.promise;
        };
        
        /**
         * 重启数据库实例
         */
        rds.restart = function (_rds) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/restart.do', {
            	dcId: _rds.dcId,
                prjId: _rds.prjId,
                rdsId: _rds.rdsId,
                rdsName: _rds.rdsName
            }).then(function (resp) {
                switch (resp.data.respCode) {
                    case SysCode.error:
                        deferred.reject(resp.data.message);
                        break;
                    case  SysCode.success:
                        deferred.resolve(resp.data);
                        break;
                    default:
                        deferred.reject();
                        break;
                }
            });
            return deferred.promise;
        };
        
        /**
         * 删除数据库实例
         */
        rds.deleteRdsInstance = function (_rds) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/deleteRdsInstance.do', {
            	dcId: _rds.dcId,
                prjId: _rds.prjId,
                rdsId: _rds.rdsId,
                isMaster: _rds.isMaster
            }).then(function (resp) {
                switch (resp.data.respCode) {
                    case SysCode.error:
                        deferred.reject(resp.data.message);
                        break;
                    case  SysCode.success:
                        deferred.resolve(resp.data);
                        break;
                    default:
                        deferred.reject();
                        break;
                }
            });
            return deferred.promise;
        };
        
        /**
         * 从库升级为主库
         */
        rds.detachReplica = function (_rds) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/detachReplica.do', {
            	dcId: _rds.dcId,
                prjId: _rds.prjId,
                rdsId: _rds.rdsId,
                rdsName: _rds.rdsName
            }).then(function (resp) {
                switch (resp.data.respCode) {
                    case SysCode.error:
                        deferred.reject(resp.data.message);
                        break;
                    case  SysCode.success:
                        deferred.resolve(resp.data);
                        break;
                    default:
                        deferred.reject();
                        break;
                }
            });
            return deferred.promise;
        };
        /**
         * 获取CPU列表
         */
        rds.getCpuList = function (){
        	var deferred = $q.defer();
        	eayunHttp.post('cloud/vm/getCpuList.do').then(function (response){
				  if(response.data.length>0){
					deferred.resolve(response.data);
				  }else{
					deferred.reject();
				  }
  		  	});
        	return deferred.promise;
        };
        /**
         * 获取RAM列表
         */
        rds.getRamListByCpu = function (nodeId) {
        	var deferred = $q.defer();
        	eayunHttp.post('cloud/vm/getRamListByCpu.do',nodeId).then(function (response){
        		if(response.data.length>0){
					deferred.resolve(response.data);
				  }else{
					deferred.reject();
				  }
        	});
        	return deferred.promise;
        };
        
        /**
         * 获取数据盘类型列表
         */
        rds.getVolumeTypeList = function (dcId) {
        	var deferred = $q.defer();
        	eayunHttp.post('cloud/volume/getVolumeTypesByDcId.do',dcId).then(function (response){
        		if(response.data.length>0){
					deferred.resolve(response.data);
				}else{
					deferred.reject();
				}
        	});
        	return deferred.promise;
        };
        
        /**
         * 获取价格
         */
        rds.getPriceDetails = function (_rds) {
        	var deferred = $q.defer();
        	var data = {
        			cycleCount: _rds.payType == '1' ? _rds.buyCycle : 1,
	                dcId: _rds.dcId,
	                payType: _rds.payType,
	                cloudMySQLCPU: _rds.cpu,
	                cloudMySQLRAM: _rds.ram,
	                number: 1
        	};
        	if(_rds.volumeTypeName == 'Normal'){
        		data.storageMySQLOrdinary = _rds.volumeSize;
        	}else if (_rds.volumeTypeName == 'Medium'){
        		data.storageMySQLBetter = _rds.volumeSize;
        	}
            eayunHttp.post('rds/instance/getPriceDetails.do', data).then(function (response) {
                switch (response.data.respCode) {
                    case SysCode.success:
                        deferred.resolve(response.data);
                        break;
                    case SysCode.error:
                        deferred.reject(response.data.message);
                        break;
                }
            });
            return deferred.promise;
        };
        /**
         * 获取数据库版本列表
         */
        rds.getVersionList = function (_dcId) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/getVersionList.do', _dcId).then(function (resp) {
        		 switch (resp.data.respCode) {
                 case SysCode.error:
                     deferred.reject(resp.data.message);
                     break;
                 case  SysCode.success:
                     deferred.resolve(resp.data);
                     break;
                 default:
                     deferred.reject();
                     break;
             }
            });
            return deferred.promise;
        };
        
        /**
         * 获取配置文件列表
         */
        rds.getConfigList = function (versionId, prjId) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/getConfigList.do', {
        		versionId: versionId,
        		prjId: prjId
        	}).then(function (resp) {
       		 switch (resp.data.respCode) {
                case SysCode.error:
                    deferred.reject(resp.data.message);
                    break;
                case SysCode.success:
                    deferred.resolve(resp.data);
                    break;
                default:
                    deferred.reject();
                    break;
            }
           });
           return deferred.promise;
        };

        /**
         * 修改配置
         */
        rds.modifyCondfiguration = function (_rds) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/modifyConfiguration.do', {
        		prjId: _rds.prjId,
        		dcId: _rds.dcId,
        		rdsId: _rds.rdsId,
        		rdsName: _rds.rdsName,
        		configId: _rds.configId
        	}).then(function (resp) {
       		 switch (resp.data.respCode) {
                case SysCode.error:
                    deferred.reject(resp.data.message);
                    break;
                case  SysCode.success:
                    deferred.resolve(resp.data);
                    break;
                default:
                    deferred.reject();
                    break;
            }
           });
           return deferred.promise;
        };
        
        /**
         * 购买数据库实例
         */
        rds.buyInstance = function (url, data) {
        	var deferred = $q.defer();
        	eayunHttp.post(url, data).then(function (resp) {
       		 switch (resp.data.respCode) {
                case SysCode.error:
                    deferred.reject(resp.data);
                    break;
                case  SysCode.success:
                    deferred.resolve(resp.data);
                    break;
                default:
                    deferred.reject();
                    break;
            }
           });
           return deferred.promise;
        };
        /**
         * 查询配额（主库）
         */
        rds.checkInstanceQuota = function (_rds){
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/checkInstanceQuota.do', _rds).then(function (resp) {
       		 switch (resp.data.respCode) {
                case SysCode.error:
                    deferred.reject(resp.data.message);
                    break;
                case  SysCode.success:
                    deferred.resolve(resp.data);
                    break;
                default:
                    deferred.reject();
                    break;
            }
           });
           return deferred.promise;
        };
        /**
         * 根据备份ID获取配置信息（数据库版本和配置）
         */
        rds.getInfoByBackupId = function (_backupId) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/backup/getConfigurationInfo.do', {
        		backupId: _backupId
        	}).then(function (resp) {
       		 switch (resp.data.respCode) {
                case SysCode.error:
                    deferred.reject(resp.data.message);
                    break;
                case  SysCode.success:
                    deferred.resolve(resp.data);
                    break;
                default:
                    deferred.reject();
                    break;
            }
           });
           return deferred.promise;
        };
        /**
         * 查询后付费资源的剩余天数
         */
        rds.queryRdsInstanceChargeById = function (_rdsId) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/queryRdsInstanceChargeById.do', _rdsId).then(function (resp) {
       		 switch (resp.data.respCode) {
                case SysCode.error:
                    deferred.reject(resp.data.message);
                    break;
                case  SysCode.success:
                    deferred.resolve(resp.data);
                    break;
                default:
                    deferred.reject();
                    break;
            }
           });
           return deferred.promise;
        };
        /**
         * 获取总价，升级页面使用
         */
        rds.getTotalPrice = function (url, data) {
        	var deferred = $q.defer();
            eayunHttp.post(url, data).then(function (resp) {
            	if(resp.data.respCode && resp.data.respCode == SysCode.error)
        			deferred.reject(resp.data.message);
        		else if(resp.data.data || resp.data.data == 0)
        			deferred.resolve(resp.data);
        		else
        			deferred.reject();
            });
            return deferred.promise;
        };
        /**
         * 计算续费后的到期时间
         */
        rds.computeRenewEndTime = function (endTime, buyCycle) {
        	var deferred = $q.defer();
        	eayunHttp.post('order/computeRenewEndTime.do',{'original':endTime ,'duration':buyCycle}).then(function(response){
        		deferred.resolve(response.data);
			});
        	return deferred.promise;
        };
        /**
         * 是否已存在数据库实例续费或变配的未完成订单
         */
        rds.checkRdsInstanceOrderExsit = function (_rdsId, _isResize, _isRenew){
        	var deferred = $q.defer();
            var data = {
                rdsId:_rdsId,
                isResize: _isResize,
                isRenew: _isRenew
            };
        	eayunHttp.post('rds/instance/checkRdsInstanceOrderExsit.do', data).then(function (resp) {
        		switch (resp.data.respCode) {
        		case SysCode.error:
                    deferred.reject(resp.data.message);
                    break;
                case  SysCode.success:
                    deferred.resolve(resp.data);
                    break;
                default:
                    deferred.reject();
                    break;
        		}
        	});
        	return deferred.promise;
        };
        /**
         * 续费操作
         */
        rds.renewInstance = function (_data) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/renewInstance.do', _data).then(function (response) {
        		if(response && response.data)
        			deferred.resolve(response.data);
        		else
        			deferred.reject();
        	});
        	return deferred.promise;
        };
        /*通过rdsId获取实例信息*/
        rds.getInstanceById = function (_rdsId) {
            var deferred = $q.defer();
            eayunHttp.post('rds/instance/getinstancebyid.do', {
                rdsId: _rdsId
            }).then(function (response) {
                switch (response.data.respCode) {
                    case SysCode.success:
                        deferred.resolve(response.data.data);
                        break;
                    case SysCode.warning:
                        deferred.notify(response.data.message);
                        break;
                    default:
                        deferred.reject(response);
                        break;
                }
            });
            return deferred.promise;
        };
        /**
         * 根据订单编号获取订单信息，用于重新下单
         */
        rds.getInstanceByOrderNo = function (_orderNo) {
        	var deferred = $q.defer();
        	eayunHttp.post('rds/instance/getInstanceByOrderNo.do', _orderNo).then(function (resp) {
        		switch (resp.data.respCode) {
        		case SysCode.error:
                    deferred.reject(resp.data.message);
                	break;
        		case  SysCode.success:
        			deferred.resolve(resp.data);
        			break;
        		default:
        			deferred.reject();
                	break;
        		}
        	});
        	return deferred.promise;
        };
        /**
         * 获取实例的规格和数据盘大小的信息
         * @param _rdsId 实例ID
         */
        rds.getStandard = function (_rdsId) {
            var deferred = $q.defer();
            eayunHttp.post('rds/instance/getStandardByRdsId.do', _rdsId).then(function (response) {
                switch (response.data.respCode) {
                    case  SysCode.success:
                        deferred.resolve(response.data.data);
                        break;
                    default:
                        deferred.reject();
                        break;
                }
            });
            return deferred.promise;
        };
    }]);