/**
 * Created by ZH.F on 2017/2/27.
 */
'use strict';

angular.module('eayunApp.service')
    .service('RDSBackupService', ['eayunHttp', '$q', 'SysCode', function (eayunHttp, $q, SysCode) {

        var bak = this;

        bak.createManualBackup = function (_database) {
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/createBackup.do', _database).then(function (response) {
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

        bak.verifyBackupName = function (_dcId, _prjId, _backupName) {
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/verifyBackupName.do', {
                datacenterId: _dcId,
                projectId: _prjId,
                backupName:_backupName
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        bak.getMaxManualBackupCount = function(_projectId){
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/getMaxManualBackupCount.do', {
                projectId: _projectId
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };
        bak.getCurrentManualBackupCount = function(_instanceId){
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/getCurrentManualBackupCount.do', {
                instanceId: _instanceId
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        bak.deleteBackup = function (_bak) {
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/deleteBackup.do', {backupId: _bak.backupId}).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve(response.data);
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        bak.enabledAutoBackup = function(_instanceId){
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/enableAutomaticBackup.do', {instanceId: _instanceId}).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve(response.data);
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };
        bak.disabledAutoBackup = function(_instanceId){
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/disableAutomaticBackup.do', {instanceId: _instanceId}).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve(response.data);
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        bak.getAutoBackupEnableStatus = function(_instanceId){
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/getAutoBackupEnableStatus.do', {instanceId: _instanceId}).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve(response.data);
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        bak.getConfigurationInfo = function(_backupId){
            var deferred = $q.defer();
            eayunHttp.post('rds/backup/getConfigurationInfo.do', {backupId: _backupId}).then(function (response) {
                if (response.data.respCode === SysCode.success) {
                    deferred.resolve(response.data);
                } else if (response.data.code === SysCode.error) {
                    deferred.reject(response.data.message);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };


    }]);