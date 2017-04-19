/**
 * Created by eayun on 2016/8/18.
 */
'use strict';

angular.module('eayunApp.service')
    .service('PoolService', ['eayunHttp','eayunModal', '$q', 'SysCode', function (eayunHttp, eayunModal, $q, SysCode) {
        var pool = this;
        /*通过负载均衡id获取负载均衡详情*/
        pool.getLoadBalanceById = function (_poolId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/loadbalance/pool/getLoadBalanceById.do', _poolId).then(function (response) {
                deferred.resolve(response.data.data);
            });
            return deferred.promise;
        };
        /*通过订单编号获取订单数据*/
        pool.getOrderLdPoolByOrderNo = function (_orderNo) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/loadbalance/pool/getorderldpoolbyorderno.do', {
                orderNo: _orderNo
            }).then(function(response){
                if(response.data.respCode == SysCode.success){
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };

        pool.checkIfOrderExist = function (_pool){
            var deferred = $q.defer();
            //检查当前是否有正在续费、正在升级、支付未结束的订单或操作，如果有，则不允许续费；
            eayunHttp.post('cloud/loadbalance/pool/checkLbOrderExist.do',_pool.poolId).then(function(response){
                if(response.data.flag){
                    deferred.resolve();
                }else{
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

        pool.checkPoolNameExist = function (_pool) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/loadbalance/pool/checkPoolNameExsit.do', {
                prjId: _pool.prjId,
                poolId: _pool.poolId,
                poolName: _pool.poolName
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        pool.updateBalancerName = function (_pool) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/loadbalance/pool/updatebalancername.do', {
                dcId: _pool.dcId,
                prjId: _pool.prjId,
                poolId: _pool.poolId,
                poolName: _pool.poolName
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        pool.getPoolQuotasByPrjId = function (_prjId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/loadbalance/pool/getpoolquotasbyprjid.do', {
                prjId: _prjId
            }).then(function (response) {
                deferred.resolve(response.data.quotas);
            });
            return deferred.promise;
        };

        pool.queryAccount = function () {
            var deferred = $q.defer();
            eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (response) {
                deferred.resolve(response.data.data.money);
            });
            return deferred.promise;
        };

        pool.getPrice = function (_order) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/loadbalance/pool/getprice.do', {
                dcId: _order.dcId,
                payType: _order.payType,
                orderType: _order.orderType,
                connectionLimit: _order.connectionLimit,
                connectionLimitOld: _order.connectionLimitOld,
                buyCycle: _order.buyCycle,
                endTime: _order.endTime
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

        pool.getNetworkListByPrjId = function (_prjId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/floatip/getNetworkByPrj.do', _prjId).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        pool.getSubnetListByNetId = function (_netId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/subnetwork/getmanagedsubnetlist.do', {
                netId: _netId
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };

        pool.commitOrder = function (_order) {
            var deferred = $q.defer();
            if (_order.orderType == 0) {
                eayunHttp.post('cloud/loadbalance/pool/buybalancer.do', {
                    dcId: _order.dcId,
                    dcName: _order.dcName,
                    prjId: _order.prjId,
                    payType: _order.payType,
                    orderType: 0,
                    buyCycle: _order.buyCycle,
                    price: _order.price,
                    accountPayment: _order.accountPayment,
                    thirdPartPayment: _order.thirdPartPayment,
                    poolName: _order.poolName,
                    poolProtocol: _order.poolProtocol,
                    subnetId: _order.subnetId,
                    lbMethod: _order.lbMethod,
                    vipPort: _order.vipPort,
                    connectionLimit: _order.connectionLimit,
                    mode:_order.mode
                }).then(function (response) {
                    if (response.data.respCode == SysCode.success) {
                        deferred.resolve(response.data);
                    } else if (response.data.respCode == SysCode.warning) {
                        deferred.reject(response.data.respMsg);
                    } else if (response.data.respCode == SysCode.error) {
                        deferred.reject('500');
                    }
                });
            } else {
                eayunHttp.post('cloud/loadbalance/pool/changebalancer.do', {
                    dcId: _order.dcId,
                    dcName: _order.dcName,
                    prjId: _order.prjId,
                    payType: _order.payType,
                    orderType: 2,
                    endTime: _order.endTime,
                    price: _order.price,
                    accountPayment: _order.accountPayment,
                    thirdPartPayment: _order.thirdPartPayment,
                    poolId: _order.poolId,
                    poolName: _order.poolName,
                    vipId: _order.vipId,
                    connectionLimit: _order.connectionLimit,
                    connectionLimitOld: _order.connectionLimitOld
                }).then(function(response){
                    if (response.data.respCode == SysCode.success) {
                        deferred.resolve(response.data);
                    } else if (response.data.respCode == SysCode.warning) {
                        deferred.reject(response.data.respMsg);
                    } else if (response.data.respCode == SysCode.error) {
                        deferred.reject('500');
                    }
                });
            }
            return deferred.promise;
        };
    }]);