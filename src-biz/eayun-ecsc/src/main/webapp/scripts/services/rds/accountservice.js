/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.service')

    .service('AccountService', ['eayunHttp', '$q', 'SysCode', function (eayunHttp, $q, SysCode) {

        var vm = this;

        vm.createAccount = function (_account) {
            var deferred = $q.defer();
            eayunHttp.post('rds/account/create.do', {
                accountName: _account.accountName,
                instanceId: _account.instanceId,
                prjId: _account.prjId,
                dcId: _account.dcId,
                password: _account.password,
                remark: _account.remark,
                dbIdList: _account.dbIdList,
                dbNameList: _account.dbNameList,
                instanceName: _account.instanceName
            }).then(function (response) {
                if (!response.data.code) {
                    deferred.resolve(response.data);
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        vm.checkAccountNameExist = function (_instanceId, _accountName) {
            var deferred = $q.defer();
            eayunHttp.post('rds/account/checkname.do', {
                accountName: _accountName,
                instanceId: _instanceId
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        vm.deleteAccount = function (_account) {
            var deferred = $q.defer();
            eayunHttp.post('rds/account/delete.do', _account).then(function (response) {
                if (response.data) {
                    switch (response.data.respCode) {
                        case SysCode.success:
                            deferred.resolve(response.data.data);
                            break;
                        case SysCode.warning:
                            if ('account does not exist' === response.data.message) {
                                deferred.notify('账户已经不存在');
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
        };

        vm.updateAccess = function (_account) {
            var deferred = $q.defer();
            eayunHttp.post('rds/account/updateaccess.do', _account).then(function (response) {
                if (response.data) {
                    switch (response.data.respCode) {
                        case SysCode.success:
                            deferred.resolve(response.data.data);
                            break;
                        case SysCode.warning:
                            if ('database does not exist' === response.data.message) {
                                deferred.notify('关联数据库已经不存在');
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
        };

        vm.updatePW = function (_account) {
            var deferred = $q.defer();
            eayunHttp.post('rds/account/updatepw.do', {
                accountId: _account.accountId,
                accountName: _account.accountName,
                password: _account.password,
                instanceId: _account.instanceId,
                instanceName: _account.instanceName,
                prjId: _account.prjId,
                dcId: _account.dcId
            }).then(function (response) {
                if (response.data == true) {
                    deferred.resolve();
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        vm.resetPW = function (_root) {
            var deferred = $q.defer();
            eayunHttp.post('rds/account/resetpw.do', {
                password: _root.password,
                instanceId: _root.instanceId,
                instanceName: _root.instanceName,
                prjId: _root.prjId,
                dcId: _root.dcId
            }).then(function (response) {
                if (response.data == true) {
                    deferred.resolve();
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        vm.getAllDBList = function (_instanceId) {
            var deferred = $q.defer();
            eayunHttp.post('rds/account/getalldblist.do', {
                instanceId: _instanceId
            }).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };

        vm.getDBListManaged = function (_accountId) {
            var deferred = $q.defer();
            eayunHttp.post('rds/account/getdblistmanaged.do', {
                accountId: _accountId
            }).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };

    }]);