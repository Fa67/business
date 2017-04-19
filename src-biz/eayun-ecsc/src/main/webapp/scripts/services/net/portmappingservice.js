/**
 * Created by eayun on 2016/8/8.
 */
'use strict';

angular.module('eayunApp.service')
    .service('PortMappingService', ['eayunHttp', '$q', 'SysCode', function (eayunHttp, $q, SysCode) {
        var portMapping = this;

        portMapping.getSubnetList = function (dcId, prjId, routeId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/subnetwork/getSubnetWorksByRouteId.do', {
                dcId: dcId,
                prjId: prjId,
                routeId: routeId
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        portMapping.getVmListBySubnetId = function (subnetId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vm/queryVmListBySubnetId.do', subnetId).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        portMapping.addPortMapping = function (_portMapping) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/portmapping/add.do', _portMapping).then(function (response) {
                if (response.data.code != SysCode.error) {
                    deferred.resolve(response.data);
                } else {
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

        portMapping.updatePortMapping = function (_portMapping) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/portmapping/update.do', _portMapping).then(function (response) {
                deferred.resolve(response.data)
            });
            return deferred.promise;
        };

        portMapping.deletePortMapping = function (_portMapping) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/portmapping/delete.do', _portMapping).then(function (response) {
                if (response.data.code != SysCode.error) {
                    deferred.resolve(response);
                } else {
                    deferred.reject();
                }
            });
            return deferred.promise;
        };
        portMapping.checkResourcePort = function ( _routeId, _resourcePort, _protocol, _pmId){
            return eayunHttp.post('cloud/netWork/portmapping/checkresourceport.do', {
                routeId: _routeId,
                protocol: _protocol,
                pmId: _pmId,
                resourcePort: _resourcePort
            }).then(function (response) {
                return response.data;
            });
        };
        
        portMapping.getNetInfo = function (_routeId) {
        	var deferred = $q.defer();
            eayunHttp.post('cloud/netWork/findNetWorkByRouteId.do', _routeId).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        }
    }]);