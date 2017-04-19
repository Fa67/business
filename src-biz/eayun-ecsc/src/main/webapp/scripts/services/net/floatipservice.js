/**
 * 公网ip JS service
 * @author liyanchao
 */
'use strict';

angular.module('eayunApp.service')
    .service('FloatIpService', ['eayunHttp', '$q', 'SysCode', function (eayunHttp, $q, SysCode) {
        var floatIp = this;
        /*根据订单编号获取订单配置信息*/
        floatIp.getOrderFloatIpByOrderNo = function (_orderNo) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/floatip/getorderfloatipbyorderno.do', {
                orderNo: _orderNo
            }).then(function(response){
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };
        floatIp.checkIfOrderExist = function (floId){
            var deferred = $q.defer();
            //检查当前是否有正在续费、正在升级、支付未结束的订单或操作，如果有，则不允许续费；
            eayunHttp.post('cloud/floatip/checkFloatIpOrderExist.do',floId).then(function(response){
                if(response.data.flag){
                    deferred.resolve();
                }else{
                    deferred.reject();
                }
            });
            return deferred.promise;
        };
        /*获取项目下弹性公网ip的使用情况*/
        floatIp.getFloatIpQuotasByPrjId = function (_prjId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/floatip/findfloipsurplus.do', _prjId).then(function(response){
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };
        /*获取价格*/
        floatIp.getPrice = function (_order) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/floatip/getprice.do', {
                dcId: _order.dcId,
                payType: _order.payType,
                buyCycle: _order.buyCycle,
                productCount: _order.productCount
            }).then(function(response){
                switch(response.data.respCode){
                    case SysCode.success:
                        deferred.resolve(response.data.data);
                        break;
                    case SysCode.error:
                        deferred.reject(response.data.message);
                        break;
                    default:
                        deferred.reject();
                }
            });
            return deferred.promise;
        };
    }]);