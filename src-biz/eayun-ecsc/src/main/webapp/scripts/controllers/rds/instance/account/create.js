/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('RdsAccountCreateCtrl', ['$scope', '$modalInstance', 'AccountService', 'instance', 'PWService',
        function ($scope, $modalInstance, AccountService, instance, PWService) {

            var vm = this;

            vm.account = {};
            vm.account.dbIdList = [];
            vm.account.dbNameList = [];
            vm.databaseList = [];
            var dbList = [];
            vm.specialName = false;
            vm.pwFlag = false;
            vm.confirmFlag = false;

            /*初始化方法*/
            var init = function () {
                api.getDatabaseList();
            };
            /*校验账户名称是否重复*/
            vm.checkAccountNameExist = function (accountName) {
                return AccountService.checkAccountNameExist(instance.rdsId, accountName).then(function (response) {
                    return !response;
                });
            };
            /*特殊账户名称的校验*/
            vm.specialNameCheck = function () {
                vm.specialName = vm.account.accountName.substr(0, 6) === 'slave_';
            };

            vm.updateSelection = function (database) {
                var action = (dbList.indexOf(database) == -1 ? 'add' : 'remove');
                api.changeChecked(action, database);
            };
            /*校验密码格式*/
            vm.checkPW = function () {
                vm.pwFlag = PWService.threeRules(vm.account.password);
                vm.checkConfirm();
            };
            /*校验密码确认是否和原密码一致*/
            vm.checkConfirm = function () {
                vm.confirmFlag = angular.isDefined(vm.account.password)
                    && !(angular.isDefined(vm.account.confirm)
                    && vm.account.password == vm.account.confirm);
            };
            /*取消创建用户请求*/
            $scope.cancel = function () {
                if (!vm.btnWait) {
                    $modalInstance.dismiss();
                }
            };
            /*提交创建账户请求*/
            $scope.commit = function () {
                vm.btnWait = true;
                api.assembleAccount();
                AccountService.createAccount(vm.account).then(function () {
                    $modalInstance.close(vm.account);
                    vm.btnWait = false;
                }, function () {
                    vm.btnWait = false;
                });
            };
            /*辅助函数*/
            var api = {
                /*组织用于创建账户的数据*/
                assembleAccount: function () {
                    var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
                    vm.account.dcId = dcPrj.dcId;
                    vm.account.prjId = dcPrj.projectId;
                    vm.account.instanceId = instance.rdsId;
                    vm.account.instanceName = instance.rdsName;
                    angular.forEach(dbList, function (database) {
                        vm.account.dbIdList.push(database.databaseId);
                        vm.account.dbNameList.push(database.databaseName);
                    });
                    if (!angular.isDefined(vm.account.remark)) {
                        vm.account.remark = '';
                    }
                },
                getDatabaseList: function () {
                    AccountService.getAllDBList(instance.rdsId).then(function (list) {
                        vm.databaseList = list;
                    });
                },
                changeChecked: function (_action, _database) {
                    var idx = dbList.indexOf(_database);
                    if (_action === 'add' && idx == -1) {
                        dbList.push(_database);
                    } else if (_action === 'remove' && idx != -1) {
                        dbList.splice(idx, 1);
                    }
                }
            };
            /*调用初始化方法*/
            init();

        }]);