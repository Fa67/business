/**
 * 私有网络JS service
 * @author zhangfan
 */
'use strict';

angular.module('eayunApp.service')
    .service('VpcService', ['eayunHttp', '$q', 'SysCode', function (eayunHttp, $q, SysCode) {
        var vpc = this;
        /*通过项目id获取设置网关的私有网络*/
        vpc.getNetworkListHaveGatewayIp = function (_prjId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/getnetworklisthavegateway.do', {
                prjId: _prjId
            }).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve(response.data.data);
                } else {
                    deferred.reject();
                }
            });
            return deferred.promise;
        };
        /*通过网络id获得私有网络信息*/
        vpc.getNetworkByNetId = function (_netId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/findNetWorkByNetId.do', _netId).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };
        /*通过网络id和子网类型获取子网信息，其中子网类型：1是获取绑定路由的受管，0是获取自管*/
        vpc.getSubnetListByNetId = function (_netId, _subnetType) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/subnetwork/getsubnetlist.do', {
                netId: _netId,
                subnetType: _subnetType
            }).then(function (response) {
                deferred.resolve(response.data.resultData);
            });
            return deferred.promise;
        };
        /*通过订单编号获取订单数据*/
        vpc.getNetworkByOrderNo = function (_orderNo) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/getordernetworkbyorderno.do', {
                orderNo: _orderNo
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };

        vpc.checkIfOrderExist = function (_network) {
            var deferred = $q.defer();
            //检查当前是否有正在续费、正在升级、支付未结束的订单或操作，如果有，则不允许续费；
            eayunHttp.post('cloud/netWork/checkNetworkOrderExist.do', _network.netId).then(function (response) {
                if (response.data.flag) {
                    deferred.resolve();
                } else {
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

        vpc.checkNetWorkName = function (_network) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/checkNetWorkName.do', {
                prjId: _network.prjId,
                netId: _network.netId,
                netName: _network.netName
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        vpc.updateNetworkName = function (_network) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/updatenetworkname.do', {
                dcId: _network.dcId,
                prjId: _network.prjId,
                netId: _network.netId,
                netName: _network.netName
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };
        /*获取项目下私有网络的数量配额剩余接口*/
        vpc.getNetworkQuotasByPrjId = function (_prjId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/getnetworkquotasbyprjid.do', {
                prjId: _prjId
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.netQuotas);
                }
            });
            return deferred.promise;
        };
        /*获取项目下私有网络的带宽配额剩余接口*/
        vpc.getQosNumByPrjId = function (_prjId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/getbandquotasbyprjid.do', {
                prjId: _prjId
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.bandQuotas);
                }
            });
            return deferred.promise;
        };

        vpc.getDnsByDcId = function (_dcId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/subnetwork/getdatacenter.do', {
                dcId: _dcId
            }).then(function (response) {
                deferred.resolve(response.data.datacenter.dcDns);
            });
            return deferred.promise;
        };
        // TODO 清除网关的判断
        vpc.checkClearNet = function (_netId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/route/checkforclear.do', _netId)
                .then(function (resp) {
                    switch (resp.data.respCode) {
                        case SysCode.error:
                            deferred.reject(resp.data.message);
                            break;
                        case SysCode.success:
                            deferred.resolve(resp.data.data);
                            break;
                        default:
                            deferred.reject();
                            break;
                    }
                });
            return deferred.promise;
        };
        /*获取外网列表*/
        vpc.getOutNetList = function (_dcId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/route/getOutNetList.do', {
                dcId: _dcId
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };
        /*设置网关接口*/
        vpc.setGateway = function (_dcId, _prjId, _routeId, _outNetId, _netName) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/route/addGateWay.do', {
                dcId: _dcId,
                prjId: _prjId,
                routeId: _routeId,
                outNetId: _outNetId,
                netName: _netName
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };
        /*清除网关接口*/
        vpc.cleanOutGateway = function (_dcId, _prjId, _routeId, _netName) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/route/deleteGateway.do', {
                dcId: _dcId,
                prjId: _prjId,
                routeId: _routeId,
                netName: _netName
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };

        vpc.checkDetachSubnet = function (_subnetId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/route/checkDetachSubnet.do', {
                subnetId: _subnetId
            }).then(function (resp) {
                switch (resp.data.respCode) {
                    case SysCode.error:
                        deferred.reject(resp.data.message);
                        break;
                    case SysCode.success:
                        deferred.resolve(resp.data.data);
                        break;
                    default:
                        deferred.reject();
                        break;
                }
            });
            return deferred.promise;
        };
        /*获取价格*/
        vpc.getPrice = function (_network) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/getprice.do', {
                buyCycle: _network.payType == '1' ? _network.buyCycle : 1,
                dcId: _network.dcId,
                orderType: _network.orderType,
                payType: _network.payType,
                rate: _network.rate,
                rateOld: _network.rateOld || '',
                endTime: _network.endTime
            }).then(function (response) {
                switch (response.data.respCode) {
                    case SysCode.success:
                        deferred.resolve(response.data.data);
                        break;
                    case SysCode.error:
                        deferred.reject(response.data.message);
                        break;
                }
            });
            return deferred.promise;
        };
        /*获取账户余额*/
        vpc.queryAccount = function () {
            var deferred = $q.defer();
            eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (response) {
                deferred.resolve(response.data.data.money);
            });
            return deferred.promise;
        };
        /*通过子网id获取对应的私有网络id*/
        vpc.getNetIdBySubnetId = function (_subnetId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/subnetwork/getnetidbysubnetid.do', {
                subnetId: _subnetId
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };
        /*验证子网是否允许删除*/
        vpc.checkForDel = function (subnet) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/subnetwork/checkfordel.do', {
                subnetId : subnet.subnetId,
                subnetType : subnet.subnetType
            }).then(function (resp) {
                switch (resp.data.respCode) {
                    case SysCode.error:
                        deferred.reject(resp.data.message);
                        break;
                    case  SysCode.success:
                        deferred.resolve(resp.data.data);
                        break;
                    default:
                        deferred.reject();
                        break;
                }
            });
            return deferred.promise;
        };
    }]);