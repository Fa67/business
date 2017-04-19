/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('RdsAccountUpdatePWCtrl', ['$scope', '$modalInstance', 'AccountService', 'account', 'instance', 'PWService',
        function ($scope, $modalInstance, AccountService, account, instance, PWService) {

            var vm = this;

            vm.pwFlag = false;
            vm.confirmFlag = false;

            var init = function () {
                api.assembleAccount();
            };

            vm.checkPW = function () {
                vm.pwFlag = PWService.threeRules(vm.account.password);
                vm.checkConfirm();
            };

            vm.checkConfirm = function () {
                vm.confirmFlag = angular.isDefined(vm.account.password)
                    && !(angular.isDefined(vm.account.confirm)
                    && vm.account.password == vm.account.confirm);
            };

            $scope.cancel = function () {
                if (!vm.btnWait) {
                    $modalInstance.dismiss();
                }
            };

            $scope.commit = function () {
                vm.btnWait = true;
                AccountService.updatePW(vm.account).then(function () {

                    $modalInstance.close();
                    vm.btnWait = false;
                }, function () {
                    vm.btnWait = false;
                });
            };

            var api = {
                assembleAccount: function () {
                    vm.account = account;
                    vm.account.password = '';
                    vm.account.instanceName = instance.rdsName;
                }
            };

            init();

        }]);