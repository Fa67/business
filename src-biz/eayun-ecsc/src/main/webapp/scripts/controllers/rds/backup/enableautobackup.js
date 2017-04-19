/**
 * Created by ZH.F on 2017/3/7.
 */
'use strict';

angular.module('eayunApp.controllers')
    .config(function ($stateProvider) {
    })
    .controller('RDSAutoBackupEnableCtrl', ['$scope', '$modalInstance', 'RDSBackupService','instanceId','maxAutoBackupCount',
        function ($scope, $modalInstance, RDSBackupService,instanceId, maxAutoBackupCount) {

            var abe = this;

            abe.isChecked = false;
            abe.maxAutoBackupCount = maxAutoBackupCount;
            $scope.cancel = function () {
                if (!abe.btnWait) {
                    $modalInstance.dismiss();
                }
            };

            $scope.commit = function () {
                abe.btnWait = true;
                RDSBackupService.enabledAutoBackup(instanceId).then(function () {
                    $modalInstance.close();
                    abe.btnWait = false;
                }, function () {
                    abe.btnWait = false;
                });
            };
        }])

    .controller('RDSAutoBackupDisableCtrl', ['$scope', '$modalInstance', 'RDSBackupService','instanceId',
        function ($scope, $modalInstance, RDSBackupService,instanceId) {

            var abd = this;

            abd.isChecked = false;
            $scope.cancel = function () {
                if (!abd.btnWait) {
                    $modalInstance.dismiss();
                }
            };

            $scope.commit = function () {
                abd.btnWait = true;
                RDSBackupService.disabledAutoBackup(instanceId).then(function () {
                    $modalInstance.close();
                    abd.btnWait = false;
                }, function () {
                    abd.btnWait = false;
                });
            };
        }]);