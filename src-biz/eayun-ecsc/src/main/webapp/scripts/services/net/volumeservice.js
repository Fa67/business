/**
 * 云硬盘JS service
 * @author liyanchao
 */
'use strict';

angular.module('eayunApp.service')
    .service('VolumeService', ['eayunHttp', '$q', function (eayunHttp, $q) {
        var volume = this;

        volume.checkIfOrderExist = function (volumeId){
            var deferred = $q.defer();
            //检查当前是否有正在续费、正在升级、支付未结束的订单或操作，如果有，则不允许续费；
            eayunHttp.post('cloud/volume/checkVolumeOrderExist.do',volumeId).then(function(response){
                if(response.data.flag){
                    deferred.resolve();
                }else{
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

    }]);