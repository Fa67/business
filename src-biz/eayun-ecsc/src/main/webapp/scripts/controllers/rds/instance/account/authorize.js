/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('RdsAccountAuthorizeCtrl', ['$scope', '$modalInstance', 'AccountService', 'account', 'instance', 'eayunModal',
        function ($scope, $modalInstance, AccountService, account, instance, eayunModal) {

            var vm = this;

            vm.accountName = account.accountName;

            var init = function () {
                api.assembleListData();
            };

            $scope.cancel = function () {
                if (!vm.btnWait) {
                    $modalInstance.dismiss();
                }
            };

            $scope.commit = function () {
                vm.btnWait = true;
                api.assembleListForSubmit();
                AccountService.updateAccess(account).then(function () {
                    $modalInstance.close();
                    vm.btnWait = false;
                }, function () {
                    vm.btnWait = false;
                }, function (message) {
                    eayunModal.warning(message);
                    vm.btnWait = false;
                    api.assembleListData();
                });
            };

            var api = {
                assembleListData: function () {
                    vm.listData = [];
                    AccountService.getDBListManaged(account.accountId).then(function (list) {
                        vm.rightList = list;
                        AccountService.getAllDBList(account.instanceId).then(function (list) {
                            vm.allList = list;
                            angular.forEach(vm.allList, function (database) {
                                database.$$selected = false;
                                angular.forEach(vm.rightList, function (relation) {
                                    if (database.databaseId == relation.databaseId) {
                                        database.$$selected = true;
                                    }
                                });
                                vm.listData.push(database);
                            });
                        });
                    });
                },
                assembleListForSubmit: function () {
                    var dbIdList = [], dbNameList = [];
                    angular.forEach(vm.listData, function (database) {
                        if (database.$$selected) {
                            dbIdList.push(database.databaseId);
                            dbNameList.push(database.databaseName);
                        }
                    });
                    account.dbIdList = dbIdList;
                    account.dbNameList = dbNameList;
                    account.instanceName = instance.rdsName;
                }
            };

            init();

        }]);