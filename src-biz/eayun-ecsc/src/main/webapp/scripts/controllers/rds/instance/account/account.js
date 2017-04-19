/**
 * Created by eayun on 2017/2/22.
 */
'use strict';

angular.module('eayunApp.controllers')

    .config(function () {

    })

    .controller('RdsInstanceDetailAccountCtrl', ['$rootScope', '$injector', '$stateParams', 'eayunModal', 'toast', 'powerService', 'AccountService', 'DatacenterService', 'RDSInstanceService',
        function ($rootScope, $injector, $stateParams, eayunModal, toast, powerService, AccountService, DatacenterService, RDSInstanceService) {

            var vm = this;
            /*初始化总方法*/
            var init = function () {
                api.setRouteList();
                api.getPowerList();
                api.getInstanceInfo();
                api.setStatusList();
                vm.status = '';
            };
            /*列表*/
            vm.table = {
                api: {},
                source: 'rds/account/getlist.do',
                getParams: function () {
                    return {
                        instanceId: $stateParams.rdsId,
                        status: vm.status
                    };
                }
            };
            /*改变筛选状态*/
            vm.changeStatus = function (item, event) {
                vm.status = item.value;
                api.getInstanceInfo();
                vm.table.api.draw();
            };
            /*备注展示限制*/
            vm.showRemark = function (_remark) {
                return DatacenterService.toastEllipsis(_remark, 50);
            };
            /*创建账户*/
            vm.create = function () {
                var result = eayunModal.open({
                    templateUrl: 'views/rds/instance/account/create.html',
                    controller: 'RdsAccountCreateCtrl',
                    controllerAs: 'create',
                    resolve: {
                        instance: function () {
                            return vm.instance;
                        }
                    }
                });
                result.result.then(function (_account) {
                    toast.success('成功' + DatacenterService.toastEllipsis(_account.accountName) + '创建账号');
                    api.getInstanceInfo();
                    vm.table.api.draw();
                }, function () {
                    api.getInstanceInfo();
                });
            };
            /*修改权限*/
            vm.authorize = function (_account) {
                var result = eayunModal.open({
                    templateUrl: 'views/rds/instance/account/authorize.html',
                    controller: 'RdsAccountAuthorizeCtrl',
                    controllerAs: 'authorize',
                    resolve: {
                        account: function () {
                            return _account;
                        },
                        instance: function () {
                            return vm.instance;
                        }
                    }
                });
                result.result.then(function () {
                    toast.success('权限修改成功');
                    api.getInstanceInfo();
                    vm.table.api.draw();
                }, function () {
                    api.getInstanceInfo();
                });
            };
            /*修改密码*/
            vm.updatePW = function (_account) {
                var result = eayunModal.open({
                    templateUrl: 'views/rds/instance/account/updatepw.html',
                    controller: 'RdsAccountUpdatePWCtrl',
                    controllerAs: 'change',
                    resolve: {
                        account: function () {
                            return _account;
                        },
                        instance: function () {
                            return vm.instance;
                        }
                    }
                });
                result.result.then(function () {
                    toast.success('密码修改成功');
                    api.getInstanceInfo();
                    vm.table.api.draw();
                }, function () {
                    api.getInstanceInfo();
                });
            };
            /*删除账户*/
            vm.delete = function (_account) {
                eayunModal.confirm('确认删除账号' + _account.accountName + '？').then(function () {
                    AccountService.deleteAccount(_account).then(function () {
                        toast.success('账号' + DatacenterService.toastEllipsis(_account.accountName, 9) + '删除成功');
                        api.getInstanceInfo();
                        vm.table.api.draw();
                    }, function () {
                        api.getInstanceInfo();
                    }, function (_message) {
                        eayunModal.warning(_message);
                        api.getInstanceInfo();
                        vm.table.api.draw();
                    });
                }, function () {
                    api.getInstanceInfo();
                });
            };
            /*重置密码*/
            vm.resetPW = function (_account) {
                var result = eayunModal.open({
                    templateUrl: 'views/rds/instance/account/resetpw.html',
                    controller: 'RdsAccountResetPWCtrl',
                    controllerAs: 'reset',
                    resolve: {
                        account: function () {
                            return _account;
                        },
                        instance: function () {
                            return vm.instance;
                        }
                    }
                });
                result.result.then(function () {
                    toast.success('root密码重置成功');
                    api.getInstanceInfo();
                    vm.table.api.draw();
                }, function () {
                    api.getInstanceInfo();
                });
            };
            /*辅助函数*/
            var api = {
                /*设置面包屑路径*/
                setRouteList: function () {
                    var routeList = [{'router': 'app.rds.instance', 'name': 'MySQL'}];
                    $rootScope.navList(routeList, '账号管理', 'detail');
                },
                /*获取账户权限*/
                getPowerList: function () {
                    powerService.powerRoutesList().then(function (powerList) {
                        vm.modulePower = {
                            accountList: powerService.isPower('rds_account_list'),
                            accountCreate: powerService.isPower('rds_account_create'),
                            accountAuthorize: powerService.isPower('rds_account_authorize'),
                            accountUpdatePW: powerService.isPower('rds_account_updatepw'),
                            accountDelete: powerService.isPower('rds_account_delete'),
                            accountResetPW: powerService.isPower('rds_account_resetpw')
                        };
                    });
                },
                /*获取所属云数据库实例信息*/
                getInstanceInfo: function () {
                    RDSInstanceService.getInstanceById($stateParams.rdsId).then(function (instance) {
                        vm.instance = instance;
                    }, function (response) {
                        console.info(response);
                    }, function (message) {
                        if (message === 'the instance is not exist') {
                            var state = $injector.get('$state');
                            state.go('app.rds.instance');
                        }
                    });
                },
                /*设置状态列表*/
                setStatusList: function () {
                    vm.statusList = [
                        {
                            key: '全部状态',
                            value: ''
                        },
                        {
                            key: '已激活',
                            value: '1'
                        },
                        {
                            key: '未激活',
                            value: '0'
                        }
                    ];
                }
            };

            init();

        }]);