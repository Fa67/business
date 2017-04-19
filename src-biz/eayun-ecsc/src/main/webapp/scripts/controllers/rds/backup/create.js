/**
 * Created by fan.zhang on 2017/2/27.
 */
'use strict';

angular.module('eayunApp.controllers')
    .config(function ($stateProvider) {

    })
    .controller('RDSManualBackupCreateCtrl', ['$scope', '$modalInstance', 'RDSBackupService', 'instanceId','instanceName','toast','maxManualBackupCount',
        function ($scope, $modalInstance, RDSBackupService, instanceId, instanceName, toast, maxManualBackupCount) {

            var mbc = this;

            var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
            mbc.backup = {
                dcId: dcPrj.dcId,
                prjId: dcPrj.projectId,
                instanceId: instanceId
            };

            mbc.instanceName = instanceName;
            mbc.manualBackupCount = maxManualBackupCount;

            mbc.verifyBackupName = function (backupName) {
                return RDSBackupService.verifyBackupName(dcPrj.dcId, dcPrj.projectId, backupName).then(function (response) {
                    if (response) {
                        return false;
                    } else {
                        return true;
                    }
                });
            };

            $scope.cancel = function () {
                if (!mbc.btnWait) {
                    $modalInstance.dismiss();
                }
            };

            $scope.commit = function () {
                mbc.btnWait = true;
                RDSBackupService.createManualBackup(mbc.backup).then(function () {
                    $modalInstance.close();
                    mbc.btnWait = false;
                }, function () {
                    mbc.btnWait = false;
                });
            };
        }]);
