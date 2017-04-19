/**
 * Created by eayun on 2017/2/22.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('RdsInstanceDetailDatabaseCtrl', ['$rootScope', '$injector', '$stateParams', 'eayunModal', 'toast', 'powerService', 'DatabaseService', 'DatacenterService', 'RDSInstanceService',
        function ($rootScope, $injector, $stateParams, eayunModal, toast, powerService, DatabaseService, DatacenterService, RDSInstanceService) {

            var vm = this;
            /*初始化总方法*/
            var init = function () {
                api.setRouteList();
                api.getPowerList();
                api.getInstanceInfo();
                api.setCharacterSet();
                vm.characterSet = '';
            };
            /*获取表格数据*/
            vm.table = {
                api: {},
                source: 'rds/database/getlist.do',
                getParams: function () {
                    return {
                        instanceId: $stateParams.rdsId,
                        characterSet: vm.characterSet
                    };
                }
            };
            /*改变筛选字符集*/
            vm.changeCharSet = function (item, event) {
                vm.characterSet = item.value;
                api.getInstanceInfo();
                vm.table.api.draw();
            };
            /*描述展示限制*/
            vm.showRemark = function (_remark) {
                return DatacenterService.toastEllipsis(_remark, 50);
            };
            /*创建数据库*/
            vm.create = function () {
                var result = eayunModal.open({
                    templateUrl: 'views/rds/instance/database/create.html',
                    controller: 'RdsDatabaseCreateCtrl',
                    controllerAs: 'create',
                    resolve: {
                        instance: function () {
                            return vm.instance;
                        }
                    }
                });

                result.result.then(function (_database) {
                    toast.success('创建数据库' + DatacenterService.toastEllipsis(_database.databaseName, 8) +  '成功');
                    api.getInstanceInfo();
                    vm.table.api.draw();
                }, function () {
                    api.getInstanceInfo();
                });
            };
            /*删除数据库*/
            vm.delete = function (_database) {
                eayunModal.confirm('确认删除数据库' + _database.databaseName + '？').then(function () {
                    DatabaseService.deleteDatabase(_database).then(function () {
                        toast.success('删除数据库' + DatacenterService.toastEllipsis(_database.databaseName, 8) + '成功');
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
            /*辅助函数*/
            var api = {
                /*设置面包屑路径*/
                setRouteList: function () {
                    var routeList = [{'router': 'app.rds.instance', 'name': 'MySQL'}];
                    $rootScope.navList(routeList, '数据库管理', 'detail');
                },
                /*获取数据库权限*/
                getPowerList: function () {
                    powerService.powerRoutesList().then(function () {
                        vm.modulePower = {
                            dbList: powerService.isPower('rds_db_list'),
                            dbCreate: powerService.isPower('rds_db_create'),
                            dbDelete: powerService.isPower('rds_db_delete')
                        };
                    });
                },
                /*获取所在云数据库实例信息*/
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
                /*设置字符集列表*/
                setCharacterSet: function () {
                    vm.charList = [
                        {
                            key: '全部字符集',
                            value: ''
                        },
                        {
                            key: 'utf8',
                            value: 'utf8'
                        },
                        {
                            key: 'gbk',
                            value: 'gbk'
                        },
                        {
                            key: 'latin1',
                            value: 'latin1'
                        },
                        {
                            key: 'latin2',
                            value: 'latin2'
                        }
                    ];

                }
            };

            init();

        }]);