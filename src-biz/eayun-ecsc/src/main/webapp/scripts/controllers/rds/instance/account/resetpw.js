/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')
    .controller('RdsAccountResetPWCtrl', ['$scope', '$modalInstance', 'PWService', 'AccountService', 'account', 'instance',
        function ($scope, $modalInstance, PWService, AccountService, account, instance) {

            var vm = this;

            vm.root = {};

            var init = function () {
                api.assembleAccount();
            };

            vm.checkPW = function () {
                vm.pwFlag = PWService.threeRules(vm.root.password);
                vm.checkConfirm();
            };

            vm.checkConfirm = function () {
                vm.confirmFlag = angular.isDefined(vm.root.password)
                    && !(angular.isDefined(vm.root.confirm)
                    && vm.root.password == vm.root.confirm);
            };

            $scope.cancel = function () {
                if (!vm.btnWait) {
                    $modalInstance.dismiss();
                }
            };

            $scope.commit = function () {
                vm.btnWait = true;
                AccountService.resetPW(vm.root).then(function () {
                    $modalInstance.close();
                    vm.btnWait = false;
                }, function () {
                    vm.btnWait = false;
                });
            };

            var api = {
                assembleAccount: function () {
                    vm.root = angular.copy(account, {});
                    vm.root.instanceName = instance.rdsName;
                }
            };

            init();

        }]);