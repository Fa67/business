/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.service')
    .service('DatabaseService', ['eayunHttp', '$q', 'SysCode', function (eayunHttp, $q, SysCode) {

        var vm = this;

        vm.createDatabase = function (_database) {
            var deferred = $q.defer();
            eayunHttp.post('rds/database/create.do', _database).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve();
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        vm.checkDBNameExist = function (_instanceId, _dataName) {
            var deferred = $q.defer();
            eayunHttp.post('rds/database/checkname.do', {
                databaseName: _dataName,
                instanceId: _instanceId
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        vm.deleteDatabase = function (_database) {
            var deferred = $q.defer();
            eayunHttp.post('rds/database/delete.do', _database).then(function (response) {
                if (response.data) {
                    switch (response.data.respCode) {
                        case SysCode.success:
                            deferred.resolve(response.data.data);
                            break;
                        case SysCode.warning:
                            if ('database does not exist' === response.data.message) {
                                deferred.notify('数据库已经不存在');
                            } else {
                                deferred.notify(response.data.message);
                            }
                            break;
                        default:
                            deferred.reject(response);
                            break;
                    }
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        }

    }]);