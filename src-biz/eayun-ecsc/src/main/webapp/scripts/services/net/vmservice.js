/**
 * 云主机JS service
 * @author liyanchao
 */
'use strict';

angular.module('eayunApp.service')
    .service('VmService', ['eayunHttp', '$q', function (eayunHttp, $q) {
        var vm = this;

        vm.checkIfOrderExist = function (vmId){
            var deferred = $q.defer();
            //检查当前是否有正在续费、正在升级、支付未结束的订单或操作，如果有，则不允许续费；
            eayunHttp.post('cloud/vm/checkVmOrderExist.do',vmId).then(function(response){
                if(response.data.flag){
                    deferred.resolve();
                }else{
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

    }]);