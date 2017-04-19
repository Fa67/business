/**
 * Created by eayun on 2016/8/8.
 */
'use strict';

angular.module('eayunApp.controllers')
    .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
        $stateProvider.state('app.net.portmapping', {
            url: '/portmapping/:routeId',
            templateUrl: 'views/net/net/portmapping/list.html',
            controller: 'PortMappingListCtrl',
            controllerAs: 'pm'
        });
    }])
    .controller('PortMappingListCtrl', ['eayunModal', '$stateParams', 'PortMappingService', 'eayunHttp', 'toast', '$rootScope', 'powerService',
        function (eayunModal, $stateParams, PortMappingService, eayunHttp, toast, $rootScope, powerService) {
            var list = [{'router': 'app.net.netbar.net', 'name': '私有网络'}];
            $rootScope.navList(list, '端口映射', 'detail');
            var vm = this;
            var dcId = JSON.parse(sessionStorage['dcPrj']).dcId,
                prjId = JSON.parse(sessionStorage["dcPrj"]).projectId,
                routeId = $stateParams.routeId;

            PortMappingService.getNetInfo($stateParams.routeId).then(function (data) {
            	vm.gatewayIp = data.gatewayIp;
            	vm.netName = data.netName;
                vm.chargeState = data.chargeState;
            });
            //vm.gatewayIp = $stateParams.gatewayIp;
            
            powerService.powerRoutesList().then(function (powerList) {
                vm.modulePower = {
                    isAddPort: powerService.isPower('net_port_view'), // 创建端口映射
                    isEditPort: powerService.isPower('net_port_edit'), // 编辑端口映射
                    isDelPort: powerService.isPower('net_port_drop'), // 删除端口映射
                };
            });

            vm.table = {
                api: {},
                source: 'cloud/netWork/portmapping/list.do',
                getParams: function () {
                    return {
                        dcId: dcId,
                        prjId: prjId,
                        routeId: routeId
                    };
                }
            };

            vm.addPM = function () {
                var page = eayunModal.open({
                    title: '创建端口映射',
                    templateUrl: 'views/net/net/portmapping/add.html',
                    controller: 'AddPortMappingCtrl',
                    controllerAs: 'addPM',
                    resolve: {
                        dcId: function () {
                            return dcId;
                        },
                        prjId: function () {
                            return prjId;
                        },
                        routeId: function () {
                            return routeId;
                        },
                        gatewayIp: function () {
                            return vm.gatewayIp;
                        }
                    }
                });
                page.result.then(function (portMapping) {
                    PortMappingService.addPortMapping(portMapping).then(function (resposne) {
                        toast.success('创建端口映射服务成功！');
                        vm.table.api.draw();
                    });
                });
            };

            vm.editPM = function (_portMapping) {
                var page = eayunModal.open({
                    title: '编辑端口映射',
                    templateUrl: 'views/net/net/portmapping/edit.html',
                    controller: 'EditPortMappingCtrl',
                    controllerAs: 'editPM',
                    resolve: {
                        dcId: function () {
                            return dcId;
                        },
                        prjId: function () {
                            return prjId;
                        },
                        portMapping: function () {
                            return _portMapping;
                        }
                    }
                });
                page.result.then(function (portMapping) {
                    PortMappingService.updatePortMapping(portMapping).then(function (response) {

                        toast.success('编辑端口映射服务成功！');
                        vm.table.api.draw();
                    });
                });
            };

            vm.deletePM = function (_portMapping) {
                eayunModal.confirm('确定删除端口映射服务吗？').then(function () {
                    PortMappingService.deletePortMapping(_portMapping).then(function (response) {
                        toast.success('删除端口映射服务成功！');
                        vm.table.api.draw();
                    });
                });
            };

            vm.listAll = function () {
                eayunHttp.post('cloud/netWork/portmapping/listall.do', {dcId: dcId}).then(function (response) {
                    //console.log(response.data);
                });
            };

        }])
    .controller('AddPortMappingCtrl', ['$scope', 'dcId', 'prjId', 'routeId', 'gatewayIp', 'PortMappingService', '$modalInstance', function ($scope, dcId, prjId, routeId, gatewayIp, PortMappingService, $modalInstance) {
        var vm = this;

        vm.gatewayIp = gatewayIp;

        vm.vm = {};

        vm.checkResourcePort = function () {
            if (vm.protocol) {
                PortMappingService.checkResourcePort(routeId, vm.resourcePort, vm.protocol, vm.pmId).then(function (response) {
                    vm.checkResourcePortFlag = response.data;
                });
            } else {
                return false;
            }
        }
        vm.changeProtocol = function () {
            vm.checkResourcePort();
        }
        PortMappingService.getSubnetList(dcId, prjId, routeId).then(function (response) {
            vm.subnetList = response;
        });

        vm.changeSubnetId = function () {
        	vm.vm = null;
            PortMappingService.getVmListBySubnetId(vm.subnetId).then(function (response) {
                vm.vmList = response.data;
            });
        };

        $scope.commit = function () {
            var portMapping = {
                dcId: dcId,
                prjId: prjId,
                protocol: vm.protocol,
                resourceId: routeId,
                resourceIp: gatewayIp,
                resourcePort: vm.resourcePort,
                destinyId: vm.vm.vmId,
                destinyIp: vm.vm.vmIp,
                destinyPort: vm.destinyPort
            };
            $modalInstance.close(portMapping);
        };
        $scope.cancel = function () {
            $modalInstance.dismiss();
        };
    }])
    .controller('EditPortMappingCtrl', ['$scope', 'dcId', 'prjId', 'portMapping', 'PortMappingService', '$modalInstance', function ($scope, dcId, prjId, portMapping, PortMappingService, $modalInstance) {
        var vm = this;

        vm.portMapping = angular.copy(portMapping, {});
        
        vm.vm = {};

        if (vm.portMapping.subnetId) {
            PortMappingService.getVmListBySubnetId(vm.portMapping.subnetId).then(function (response) {
                vm.vmList = response.data;
                angular.forEach(vm.vmList, function (value, key) {
                    if (value.vmId == vm.portMapping.destinyId) {
                        vm.vm = value;
                    }
                });
            });
        }
        vm.checkResourcePort = function () {
            if (vm.portMapping.protocol) {
                PortMappingService.checkResourcePort(vm.portMapping.resourceId, vm.portMapping.resourcePort, vm.portMapping.protocol, vm.portMapping.pmId).then(function (response) {
                    vm.checkResourcePortFlag = response.data;
                });
            } else {
                return false;
            }
        }
        vm.changeProtocol = function () {
            vm.checkResourcePort();
        }

        PortMappingService.getSubnetList(dcId, prjId, vm.portMapping.resourceId).then(function (response) {
            vm.subnetList = response;
        });

        vm.changeSubnetId = function () {
        	vm.vm = null;
            PortMappingService.getVmListBySubnetId(vm.portMapping.subnetId).then(function (response) {
                vm.vmList = response.data;
                /*angular.forEach(vm.vmList, function (value, key) {
                    if (value.vmId == vm.portMapping.destinyId) {
                        vm.vm = value;
                    }
                });*/
            });
        };

        vm.changeIp = function () {
            vm.portMapping.destinyId = vm.vm.vmId;
            vm.portMapping.destinyIp = vm.vm.vmIp;
        };

        $scope.commit = function () {
        	var portMapping = {
                    dcId: dcId,
                    prjId: prjId,
                    pmId: vm.portMapping.pmId,
                    protocol: vm.portMapping.protocol,
                    resourceId: vm.portMapping.resourceId,
                    resourceIp: vm.portMapping.resourceIp,
                    resourcePort: vm.portMapping.resourcePort,
                    destinyId: vm.portMapping.destinyId,
                    destinyIp: vm.portMapping.destinyIp,
                    destinyPort: vm.portMapping.destinyPort
            };
            $modalInstance.close(portMapping);
        };
        $scope.cancel = function () {
            $modalInstance.dismiss();
        };
    }]);