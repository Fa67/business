/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('RdsDatabaseCreateCtrl', ['$scope', '$modalInstance', 'DatabaseService', 'instance',
        function ($scope, $modalInstance, DatabaseService, instance) {

            var vm = this;
            /*初始化设定dbName*/
            vm.database = {};
            vm.database.characterSet = 'utf8';
            vm.specialName = false;
            /*校验数据库名称是否重复*/
            vm.checkDBNameExist = function (dbName) {
                return DatabaseService.checkDBNameExist(instance.rdsId, dbName).then(function (response) {
                    return !response;
                });
            };
            /*校验数据库名称是否为特殊字符*/
            vm.specialNameCheck = function () {
                if (angular.isDefined(vm.database.databaseName)) {
                    var dbName = vm.database.databaseName;
                    vm.specialName = (dbName.toLowerCase() === 'mysql'
                        ||  dbName.toLowerCase() === 'performance_schema'
                        ||  dbName.toLowerCase() === 'lost+found'
                        ||  dbName.toLowerCase() === 'information_schema'
                        ||  dbName.substr(0, 4).toLowerCase() === 'test');
                } else {
                    vm.specialName = false;
                }
            };
            /*取消提交创建数据库*/
            $scope.cancel = function () {
                if (!vm.btnWait) {
                    $modalInstance.dismiss();
                }
            };
            /*确定提交创建数据库*/
            $scope.commit = function () {
                vm.btnWait = true;
                api.assembleDatabase();
                DatabaseService.createDatabase(vm.database).then(function () {
                    $modalInstance.close(vm.database);
                    vm.btnWait = false;
                }, function () {
                    vm.btnWait = false;
                });
            };
            /*辅助函数*/
            var api = {
                /*组装需要提交的数据库*/
                assembleDatabase: function () {
                    var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
                    vm.database.dcId = dcPrj.dcId;
                    vm.database.prjId = dcPrj.projectId;
                    vm.database.instanceId = instance.rdsId;
                    vm.database.instanceName = instance.rdsName;
                    if (!angular.isDefined(vm.database.remark)) {
                        vm.database.remark = '';
                    }
                }
            };

        }]);