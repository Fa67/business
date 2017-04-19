'use strict';
angular
    .module('eayunApp.controllers')
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.when('/app/net', '/app/net/netbar');
        $urlRouterProvider.when('/app/net/netbar', 'app/net/netbar/net');
        $urlRouterProvider.when('/buy/buyNetWork', '/buy/buyNetWork/payPake');
        //$urlRouterProvider.when("/buy", "/buy/createFloatIp");
        $urlRouterProvider.when('/buy/createFloatIp', '/buy/createFloatIp/payPake');
        /** 定义路由，加载的页面放入上一级路由(app.auth)所加载的页面的data-ui-view里面 */
        $stateProvider.state('app.net.netbar', {
                url: '/netbar',
                templateUrl: 'views/net/netbar.html',
            })
            .state('app.net.netbar.net', {
                url: '/net',
                templateUrl: 'views/net/net/netmng.html',
                controller: 'NetMngCtrl'
            })
            .state("buy.buyNetWork", {
                url: '/buyNetWork:orderNo',
                templateUrl: 'views/net/net/addnetwork.html',
                controller: 'BuyNetCtrl',
                controllerAs: 'buyNetWork'
            })
            .state("app.net.datilNetWork", {
                url: "/datilNetWork:netId",
                templateUrl: 'views/net/net/datilnetwork.html',
                controller: 'DatilNetWorkCtrl'
            })
            .state("app.net.addSubNetWork", {
                url: "/addNetWork",
                templateUrl: 'views/net/net/addsubnet.html',
                controller: 'addSubNetWorkCtrl'
            })
            .state("buy.buyNetWork.payPake", {
                url: "/payPake",
                templateUrl: 'views/net/net/buynet/paypake.html',
                controller: 'payPakeNetCtrl',
                controllerAs: 'payPakeNet'
            })
            .state("buy.buyNetWork.payRequired", {
                url: "/payRequired",
                templateUrl: 'views/net/net/buynet/payrequired.html',
                controller: 'payRequiredNetCtrl',
                controllerAs: 'payRequiredNet'
            })
            .state('buy.vpcBuy', {
                url: '/vpcBuy/:orderNo',
                templateUrl: 'views/net/net/buyvpc.html',
                controller: 'BuyVpcCtrl',
                controllerAs: 'buyVpc'
            })
            .state("buy.verifyNetWork", {
                url: "/verifyNetWork/:source/:netId",
                templateUrl: "views/net/net/verifynetwork.html",
                controller: "VerifyNetWorkCtrl",
                controllerAs: "verifyNet"
            })
            .state('buy.vpcRenew', {
                url: '/networkRenew',
                templateUrl: 'views/net/net/networkrenewconfirm.html',
                controller: 'NetworkRenewConfirmController'
            });
    })
    /*
     * 标签级别的ctrl
     */
    .controller('NetCtrl', function ($scope, $rootScope, $state, eayunModal, eayunHttp, cloudprojectList, $window, powerService, toast) {
        powerService.powerRoutesList().then(function (powerList) {
            $scope.modulePower = {
                isLdBalance: powerService.isPower('load_view'), // 负载均衡
                isRoute: powerService.isPower('route_view'), // 路由等...
                isFloatView: powerService.isPower('float_view'), // 浮动IP查看功能
                isNetWorkView: powerService.isPower('net_view'), // 网络
                isSubNetView: powerService.isPower('subnet_view'), // 子网
                isVpnView: powerService.isPower('vpn_view'),    //VPN
            };
        });
        $scope.model = {};
        $scope.cloudprojectList = cloudprojectList;
        var daPrj = sessionStorage["dcPrj"];
        if (daPrj) {
            daPrj = JSON.parse(daPrj);
            angular.forEach($scope.cloudprojectList, function (value, key) {
                if (value.projectId == daPrj.projectId) {
                    $scope.model.projectvoe = value;
                }
            });
        } else {
            angular.forEach($scope.cloudprojectList, function (value) {
                if (value.projectId != null && value.projectId != '' && value.projectId != 'null') {
                    $scope.model.projectvoe = value;
                    $window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
                }
            });
        }

        $scope.$watch('model.projectvoe.projectId', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                if (newVal == null || newVal == '' || newVal == 'null') {
                    $scope.dcId = $scope.model.projectvoe.dcId;
                    angular.forEach($scope.cloudprojectList, function (value) {
                        if (oldVal == value.projectId) {
                            $scope.model.projectvoe = value;
                            return false;
                        }
                    });
                    eayunHttp.post('cloud/project/findProByDcId.do', {dcId: $scope.dcId}).then(function (response) {
                        if (response.data) {
                            eayunModal.warning("您在该数据中心下没有具体资源的访问权限");
                        } else {
                            eayunModal.warning("您在该数据中心下没有任何项目");
                        }
                    });
                } else {
                    $window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
                }
            }
        }, true);

        /*var daPrj = sessionStorage["dcPrj"];
         if (daPrj) {
         daPrj = JSON.parse(daPrj);
         angular.forEach($scope.cloudprojectList, function (value, key) {
         if (value.projectId == daPrj.projectId)
         $scope.model.dcProject = value;
         });
         } else {
         angular.forEach($scope.cloudprojectList, function (value) {
         if (value.projectId != null && value.projectId != '' && value.projectId != 'null') {
         $scope.model.dcProject = value;
         $window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.dcProject);
         return false;
         }
         });
         }*/

        $rootScope.navList = function (_routerList, _routerName, _viewType) {
            $scope.routerList = _routerList;
            $scope.routerName = _routerName;
            $scope.viewType = _viewType;
        };

        $scope.$watch('model.dcProject.projectId', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                if (newVal == null || newVal == '' || newVal == 'null') {
                    $scope.dcId = $scope.model.dcProject.dcId;
                    angular.forEach($scope.cloudprojectList, function (value) {
                        if (oldVal == value.projectId) {
                            $scope.model.dcProject = value;
                            return false;
                        }
                    });
                    eayunHttp.post('cloud/project/findProByDcId.do', {dcId: $scope.dcId}).then(function (response) {
                        if (response.data) {
                            eayunModal.warning("您在该数据中心下没有具体资源的访问权限");
                        } else {
                            eayunModal.warning("您在该数据中心下没有任何项目");
                        }
                    });
                } else {
                    $window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.dcProject);
                }
            }
        });
        $scope.shortName = function (name) {
            if (name && name.length > 9)
                return name.substring(0, 9) + '...';
            return name;
        };
        // ---新增和编辑子网
        $scope.addOrEditSubNetWork = function (netWork, subNetWork, type) {
            var title = "编辑子网";
            if (type != "edit") {
                title = "增加子网";
            }
            var result = eayunModal.open({
                title: title,
                backdrop: 'static',
                templateUrl: 'views/net/net/addsubnet.html',
                controller: 'addSubNetWorkCtrl',
                resolve: {
                    netWork: function () {
                        return netWork;
                    },
                    subNetWork: function () {
                        if (type == "edit") {// 编辑
                            return subNetWork;
                        } else {
                            return {};
                        }
                    },
                    type: function () {
                        return type;
                    }
                }
            });
            result.result.then(function (subNetWork) {
                var route = "app.net.datilNetWork";
                if (subNetWork.type == "add") {
                    route = "app.net.netbar.net";
                }
                $state.go(route, {"netId": subNetWork.netId}, {reload: true});
            }, function () {
                // console.info('取消');
            });
        };
    })
    /*
     * 列表ctrl
     */
    .controller('NetMngCtrl', ['$scope', '$rootScope', '$state', 'eayunModal', 'eayunHttp', 'toast', 'powerService', 'VpcService', 'eayunStorage',
        function ($scope, $rootScope, $state, eayunModal, eayunHttp, toast, powerService, VpcService, eayunStorage) {
            var list = [];
            $rootScope.navList(list, '私有网络');

            $scope.model.dcProject = JSON.parse(sessionStorage["dcPrj"]);

            powerService.powerRoutesList().then(function (powerList) {
                $scope.modulePower = {
                    isCreNet: powerService.isPower('net_add'), // 添加网络
                    isAddSub: powerService.isPower('net_addsubnet'), // 添加子网
                    isEditNet: powerService.isPower('net_edit'), // 编辑网络
                    isBandWidth: powerService.isPower('net_bandwidth'), //更改带宽
                    isNetTag: powerService.isPower('net_tag'), // 标签
                    isDelNet: powerService.isPower('net_delete'), // 删除网络
                    isSetGateWay: powerService.isPower('net_setup'), // 设置网关/清除网关
                    isRenew: powerService.isPower('net_renew'), //续费
                    isPortMapping: powerService.isPower('net_port_view'), //端口映射
                };
            });
            $rootScope.netRoute = null;
            /*搜索方法*/
            $scope.search = function () {
                $scope.myTable.api.draw();
            };
            /*表格刷新方法*/
            $scope.myTable = {
                source: 'cloud/netWork/getNetWorkListByPrjId.do',
                api: {},
                getParams: function () {
                    return {
                        keyWord: $scope.netName || '',
                        prjId: $scope.model.dcProject ? $scope.model.dcProject.projectId
                            : ''// 这个不可能为空
                    };
                }
            };
            /*切换数据中心*/
            $scope.$watch('model.projectvoe', function (newValue, oldValue) {
                if (oldValue !== newValue) {
                    $scope.model.dcProject = newValue;
                    $scope.myTable.api.draw();
                }
            });
            /**
             * 网络状态 显示
             */
            $scope.getNetStatus = function (model) {
                $scope.vmStatusClass = '';
                if (model.netStatus && model.netStatus == 'ACTIVE' && model.chargeState == '0') {
                    return 'green';
                } else if (model.netStatus && model.netStatus != 'ACTIVE' && model.chargeState == '0') {
                    return 'yellow';
                } else if (model.chargeState != '0') {
                    return 'gray';
                }
            };
            // --------回车事件
            $scope.checkUser = function () {
                var user = sessionStorage["userInfo"];
                if (user) {
                    user = JSON.parse(user);
                    if (user && user.userId) {
                        return true;
                    }
                }
                return false;
            };
            $(function () {
                document.onkeydown = function (event) {
                    var e = event || window.event
                        || arguments.callee.caller.arguments[0];
                    if (!$scope.checkUser()) {
                        return;
                    }
                    // 用户登录判断
                    if (e.keyCode == 13) {
                        $scope.myTable.api.draw();
                    }
                };
            });
            /*删除私有网络*/
            $scope.delNetWorkByNetId = function (netWork) {
                if (netWork.subNetCount > 0) {
                    eayunModal.warning("请先删除私有网络内的子网");
                    return false;
                }
                eayunModal.confirm('确定要删除私有网络' + netWork.netName + '？').then(function () {
                    eayunHttp.post("cloud/netWork/delNetWorkByNetId.do", netWork).then(function (respose) {
                        if (respose.data) {
                            if (respose.data.code != "010120") {
                                toast.success("删除私有网络成功！");
                                $scope.myTable.api.draw();
                            }
                        } else {
                            eayunModal.warning("私有网络已被使用，不允许删除");
                        }
                    });
                });
            };
            /*私有网络续费*/
            $scope.renewNetWork = function (netWork) {
                var result = eayunModal.open({
                    title: '私有网络续费',
                    backdrop: 'static',
                    templateUrl: 'views/net/net/networkrenew.html',
                    controller: 'vpcRenewCtrl',
                    resolve: {
                        item: function () {
                            return netWork;
                        }
                    }
                });
                result.result.then(function (value) {
                    VpcService.checkIfOrderExist(netWork).then(function (response) {
                        //isOrderExisted = true;
                        eayunModal.warning("资源正在调整中或您有未完成的订单，请稍后再试。");
                    }, function () {
                        $state.go('buy.vpcRenew');
                    });
                }, function () {

                });
            };
            /*更改带宽*/
            $scope.addOrEditNetWork = function (netWork) {
                var result = eayunModal.open({
                    title: '更改带宽',
                    backdrop: 'static',
                    templateUrl: 'views/net/net/editnetwork.html',
                    controller: 'EditNetWork',
                    resolve: {
                        haveBandCount: function () {
                            return eayunHttp.post('cloud/route/getHaveBandCount.do', {prjId: $scope.model.dcProject.projectId}).then(function (response) {
                                return response.data;
                            });
                        },
                        prjBandCount: function () {
                            return eayunHttp.post('cloud/route/getPrjBandCount.do', {prjId: $scope.model.dcProject.projectId}).then(function (response) {
                                return response.data;
                            });
                        },
                        netWork: function () {
                            return netWork;
                        }
                    }
                });
                result.result.then(function (netWork) {
                    eayunStorage.set('netWork', {
                        orderType: '2',
                        buyCycle: 1,
                        payType: netWork.payType,
                        rate: netWork.rate,
                        rateOld: netWork.rateOld,
                        prjId: netWork.prjId,
                        dcId: netWork.dcId,
                        dcName: netWork.dcName,
                        netId: netWork.netId,
                        netName: netWork.netName,
                        endTime: netWork.endTime,
                        price: netWork.price
                    });
                    $state.go("buy.verifyNetWork", {source: "change_network_list"});
                }, function () {
                    // console.info('取消');
                });
            };
            /*跳转详情页*/
            $scope.findNetWorkByNetId = function (netId) {
                $state.go("app.net.datilNetWork", {
                    "detailType": 'net',
                    "netId": netId
                });
            };
            /* 标签 */
            $scope.tagResource = function (resType, resId) {
                var result = eayunModal.open({
                    title: '标记资源',
                    backdrop: 'static',
                    templateUrl: 'views/tag/tagresource.html',
                    controller: 'TagResourceCtrl',
                    resolve: {
                        resType: function () {
                            return resType;
                        },
                        resId: function () {
                            return resId;
                        }
                    }
                });
                result.result.then(function () {
                });
            };
            /*设置网关*/
            $scope.changeGateway = function (netWork) {
                if (netWork.extNetName != null && netWork.extNetName != "") {
                    eayunModal.confirm('确定要清除' + netWork.netName + '的网关？').then(function () {
                        VpcService.checkClearNet(netWork.netId).then(function () {
                            VpcService.cleanOutGateway(
                                netWork.dcId,
                                netWork.prjId,
                                netWork.routeId,
                                netWork.netName
                            ).then(function (response) {
                                toast.success('清除网关成功');
                                $scope.myTable.api.draw();
                            });
                            /*eayunHttp.post('cloud/route/deleteGateway.do', netWork).then(
                             function (response) {
                             // 如果创建成功，刷新当前列表页
                             if (response.data.code != "010120") {
                             toast.success('清除网关成功');
                             }
                             $scope.myTable.api.draw();
                             });*/
                        }, function (message) {
                            eayunModal.warning(message);
                        });
                    }, function () {
                    });
                } else {
                    eayunModal.confirm('确定要设置' + netWork.netName + '的网关？').then(function () {
                        VpcService.getOutNetList(netWork.dcId).then(function (response) {
                            var outNetList = angular.copy(response, []);
                            if (outNetList && outNetList.length == 1) {
                                VpcService.setGateway(
                                    netWork.dcId,
                                    netWork.prjId,
                                    netWork.routeId,
                                    outNetList[0].value,
                                    netWork.netName
                                ).then(function (response) {
                                    toast.success('设置网关成功');
                                    $scope.myTable.api.draw();
                                }, function () {
                                    /*eayunModal.warning(response.data.message);*/
                                });
                            } else {
                                eayunModal.warning('当前有多个外网，需要指定！');
                            }
                        });
                    }, function () {

                    });
                    /*var result = eayunModal.dialog({
                     showBtn: false, // 使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
                     title: '设置网关',
                     width: '600px',
                     templateUrl: 'views/net/route/setgateway.html',
                     controller: 'routeSetGatewayCtrl',
                     resolve: {
                     outNetWorkList: function () {
                     return eayunHttp.post('cloud/route/getOutNetList.do', {dcId: netWork.dcId}).then(function (response) {
                     return response.data;
                     });
                     },
                     item: function () {
                     return netWork;
                     }

                     }
                     });
                     result.then(function (value) {
                     // 创建页面点击提交执行后台Java代码
                     eayunHttp.post('cloud/route/addGateWay.do', value).then(function (response) {
                     // 如果创建成功，刷新当前列表页
                     if (response.data.code != "010120") {
                     toast.success('设置网关成功');
                     }
                     $scope.myTable.api.draw();
                     });
                     }, function () {
                     });*/
                }
            };
            /*跳转端口映射*/
            $scope.portMapping = function (netWork) {
                if (netWork.extNetName != null && netWork.extNetName != '') {
                    $state.go('app.net.portmapping', {
                        routeId: netWork.routeId
                    });
                } else {
                    eayunModal.warning('请先为私有网络设置网关！');
                }
            };
        }])
    .controller('BuyNetCtrl', ['$scope', 'eayunHttp', '$stateParams', 'eayunStorage', function ($scope, eayunHttp, $stateParams, eayunStorage) {
        var vm = this;
        var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
        var orderNo = $stateParams.ordersNo;
        eayunHttp.post('cloud/vm/getDatacenterProjectList.do').then(function (response) {
            vm.datacenters = response.data;
            if (response.data.length > 0) {
                var initialize = false;
                angular.forEach(vm.datacenters, function (value, key) {
                    if (value.projectId == dcPrj.projectId) {
                        initialize = true;
                    }
                });
                if (!initialize) {
                    var value = vm.datacenters[0];
                }
            }
        });
    }])
    /**
     * 私有网络续费订单确认页controller
     */
    .controller('NetworkRenewConfirmController', ['$scope', '$state', 'eayunModal', 'eayunHttp', 'eayunStorage', 'VpcService','eayunMath',
        function ($scope, $state, eayunModal, eayunHttp, eayunStorage, VpcService, eayunMath) {
            //读取页面共享数据，用于页面展示
            var chargeMoney = eayunStorage.get('vpc_chargeMoney');
            if(chargeMoney == null){
                $state.go('app.net.netbar.net');//如果刷新页面，则eayunStorage清空，则需要跳转到列表页
            }
            $scope.model = {
                dcName: eayunStorage.get('vpc_dcName'),
                netId: eayunStorage.get('vpc_id'),
                netName: eayunStorage.get('vpc_name'),
                bandValue: eayunStorage.get('vpc_bandValue'),
                cycle: eayunStorage.get('vpc_cycle'),
                chargeMoney: chargeMoney,
                paramBean: eayunStorage.get('vpc_paramBean'),
                lastTime: eayunStorage.get('vpc_expireTime'),
                endTime: eayunStorage.get('vpc_endTime')
            };

            eayunHttp.post('costcenter/accountoverview/getaccountbalance.do', {}).then(function (response) {
                var balance = response.data.data.money;
                $scope.model.balance = balance;
                doCalc();
            });

            //refreshMoney();

            function refreshMoney() {
                var b1, b2;
                //获取账户余额
                eayunHttp.post('costcenter/accountoverview/getaccountbalance.do', {}).then(function (response) {
                    b1 = true;
                    var balance = response.data.data.money;
                    $scope.model.balance = balance;
                    if (b1 && b2) {
                        doCalc();
                    }
                });

                eayunHttp.post('billing/factor/getPriceDetails.do', $scope.model.paramBean).then(function (response) {
                    b2 = true;
                    $scope.responseCode = response.data.respCode;
                    if ($scope.responseCode == '010120') {
                        $scope.warningMessage = response.data.message;
                    } else {
                        $scope.model.chargeMoney = response.data.data.totalPrice;
                    }
                    if (b1 && b2) {
                        doCalc();
                    }
                });
            };

            function doCalc() {
                //处理余额支付选中、实付、余额支付、应付的金额
                $scope.model.isSelected = false;
                $scope.model.deduction = formatFloat(0, 2);
                $scope.model.payable = $scope.model.chargeMoney;
                $scope.canSubmit = true;

                $scope.calculateDeduction = function () {
                    $scope.warningMessage = '';
                    $scope.canSubmit = true;
                    $scope.model.deduction = formatFloat(0, 2);
                    if ($scope.model.isSelected) {
                        if ($scope.model.chargeMoney - $scope.model.balance > 0) {
                            $scope.model.deduction = $scope.model.balance;
                        } else {
                            $scope.model.deduction = $scope.model.chargeMoney;
                        }
                        $scope.model.payable = eayunMath.sub($scope.model.chargeMoney, $scope.model.deduction);
                    } else {
                        $scope.model.deduction = formatFloat(0, 2);
                        $scope.model.payable = $scope.model.chargeMoney;
                    }
                };
            };


            //格式化小数num，保留小数点后pos位
            function formatFloat(num, pos) {
                var n = Math.floor(num * Math.pow(10, pos)) / Math.pow(10, pos);
                var n_s = n.toString();
                var pos_decimal = n_s.indexOf('.');
                if (pos_decimal < 0) {
                    pos_decimal = n_s.length;
                    n_s += '.';
                }
                while (n_s.length <= pos_decimal + 2) {
                    n_s += '0';
                }
                return n_s;
            };

            //提交订单
            $scope.canSubmit = true;
            $scope.warningMessage = '';
            $scope.submitOrder = function () {
                VpcService.checkIfOrderExist($scope.model).then(function (response) {
                    $scope.warningMessage = '资源正在调整中或您有未完成的订单，请稍后再试。';
                    $scope.canSubmit = false;
                    //eayunModal.info("资源正在调整中或您有未完成的订单，请稍后再试。");
                    //$state.go('app.net.netbar.net');
                }, function () {
                    //当前无未完成订单，可以提交订单
                    //获取当前余额，判断此时账户余额是否大于余额支付金额，以确定能否提交订单
                    //直接调用续费接口，在接口中进行一系列校验，如果校验不通过，页面进行响应的展现，如果校验通过，则完成订单。
                    eayunHttp.post('cloud/netWork/renewnetwork.do', $scope.model, {$showLoading: true}).then(function (response) {
                        var respCode = response.data.respCode;
                        var respMsg = response.data.message;
                        var respOrderNo = response.data.orderNo;
                        var bandwidth = response.data.bandwidth;
                        if (respCode == '010110') {
                            $scope.warningMessage = respMsg;
                            $scope.model.isSelected = false;
                            //当提余额、产品金额发生变动时，除了刷新金额，还需要刷新订单确认页的配置
                            $scope.model.bandValue = new Number(bandwidth);
                            $scope.model.paramBean.bandValue = new Number(bandwidth);
                            refreshMoney();

                        } else if (respCode == '000000') {
                            if (respMsg == 'BALANCE_PAY_ALL') {
                                //直接跳转到订单完成界面
                                $state.go('pay.result', {subject: '私有网络-续费'});
                            } else if (respMsg == 'RENEW_SUCCESS' || respMsg == 'BALANCE_PAY_PART') {
                                //跳转到第三方支付页面
                                var orderPayNavList = [{route: 'app.net.netbar.net', name: '私有网络'}];
                                eayunStorage.persist("orderPayNavList", orderPayNavList);
                                eayunStorage.persist("payOrdersNo", respOrderNo);
                                $state.go('pay.order');
                            }
                        }
                    });

                });
            }

        }])
    /**
     * 私有网络续费controller
     */
    .controller('vpcRenewCtrl', function ($scope, eayunModal, eayunHttp, item, eayunStorage, $modalInstance) {
        $scope.model = angular.copy(item);

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };
        $scope.commit = function () {
            $modalInstance.close();
        };

        //给定默认值
        $scope.model.renewType = 'month';
        $scope.model.renewTime = '1';
        doGetTimeAndPrice($scope.model.renewType, $scope.model.renewTime);

        /**
         * 切换付费类型
         */
        $scope.changePayType = function () {
            if ($scope.model.renewType == 'month') {
                $scope.model.renewTime = '1';
            } else if ($scope.model.renewType == 'year') {
                $scope.model.renewTime = '12';
            }
            $scope.model.lastTime = null;
            doGetTimeAndPrice($scope.model.renewType, $scope.model.renewTime);
        };

        /**
         * 切换时间选择
         */
        $scope.changeTime = function (renewType, renewTime) {
            doGetTimeAndPrice(renewType, renewTime);
        };
        function doGetTimeAndPrice(renewType, renewTime) {
            if (renewTime == '0') {
                $scope.model.chargeMoney = null;
                $scope.model.lastTime = null;
            }
            if (renewType != 'zero' && renewTime != '0') {
                //调用计费算法得出需要支付的费用
                var cycleCount = renewTime;
                var paramBean = {
                    dcId: item.dcId,
                    payType: '1',
                    number: 1,
                    cycleCount: cycleCount,
                    bandValue: item.rate
                };
                //将参数注入到eayunStorage用于传递给订单确认页面
                eayunStorage.set('vpc_dcName', item.dcName);
                eayunStorage.set('vpc_id', item.netId);
                eayunStorage.set('vpc_name', item.netName);
                eayunStorage.set('vpc_bandValue', item.rate);
                eayunStorage.set('vpc_cycle', cycleCount);
                eayunStorage.set('vpc_paramBean', paramBean);
                eayunStorage.set('vpc_endTime', item.endTime);

                eayunHttp.post('billing/factor/getPriceDetails.do', paramBean).then(function (response) {
                    $scope.responseCode = response.data.respCode;
                    if ($scope.responseCode == '010120') {
                        $scope.respMsg = response.data.message;
                    } else {
                        $scope.model.chargeMoney = response.data.data.totalPrice;
                        eayunStorage.set('vpc_chargeMoney', $scope.model.chargeMoney);
                    }

                });
                //计算续费后的到期时间
                eayunHttp.post('order/computeRenewEndTime.do', {
                    'original': $scope.model.endTime,
                    'duration': renewTime
                }).then(function (response) {
                    $scope.model.lastTime = response.data;
                    eayunStorage.set('vpc_expireTime', $scope.model.lastTime);
                });
            }
        };
    })

    .controller('payPakeNetCtrl', ['$scope', 'eayunHttp', 'eayunStorage', '$state', 'VpcService', 'DatacenterService',
        function ($scope, eayunHttp, eayunStorage, $state, VpcService, DatacenterService) {
            var vm = this;
            /*总体初始化页面*/
            var initial = function () {
                initData();
                initDatacenter();
                vm.buyCycleType();
                //vm.getPrice();
            };
            /*初始化数据*/
            var initData = function () {
                var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
                var userInfo = JSON.parse(sessionStorage["userInfo"]);
                vm.isNameExist = false;
                vm.hasGotPrice = true;
                vm.isCre = eayunStorage.get("isCre");
                vm.netWork = eayunStorage.get("netWork");
                if (vm.isCre == null) {
                    vm.netWork = {
                        orderType: "0",
                        payType: "1",
                        rate: 1,
                        prjId: dcPrj.projectId,
                        dcId: dcPrj.dcId,
                        dcName: dcPrj.dcName,
                        cusId: userInfo.cusId,
                        createName: userInfo.userName
                    };
                } else {
                    vm.netWork = eayunStorage.get('buy_network_again');
                    eayunStorage.delete('buy_network_again');
                    eayunStorage.delete("isCre");
                }
            };
            /*初始化数据中心*/
            var initDatacenter = function () {
                DatacenterService.getDcPrjList().then(function (response) {
                    vm.datacenters = response;
                    var initialized = false;
                    if (vm.datacenters != null) {
                        angular.forEach(vm.datacenters, function (value, key) {
                            if (vm.netWork.prjId == value.projectId) {
                                initialized = true;
                                vm.selectDcPrj(value);
                            }
                        });
                        if (!initialized) {
                            vm.selectDcPrj(vm.datacenters[0]);
                        }
                    }
                });
            };
            /*跳转订单确认页面*/
            vm.goToOrder = function () {
                eayunStorage.set("netWork", vm.netWork);
                $state.go("buy.verifyNetWork", {source: "cre_before"});
            };
            /*选择数据中心的接口*/
            vm.selectDcPrj = function (_datacenter) {
                vm.netWork.dcId = _datacenter.dcId;
                vm.netWork.dcName = _datacenter.dcName;
                vm.netWork.prjId = _datacenter.projectId;
                vm.getPrice();
                VpcService.getNetworkQuotasByPrjId(vm.netWork.prjId).then(function (response) {
                    vm.netQuotas = response;
                    vm.outOfQuota = vm.netQuotas <= 0;
                });
                vm.checkNetworkNameExist();
                VpcService.getQosNumByPrjId(vm.netWork.prjId).then(function (response) {
                    vm.bandQuotas = response;
                });
            };

            vm.getPrice = function () {
                /*if (vm.netWork.$$buyCycleYear == '1') {
                 vm.list = [
                 {
                 text: '1个月',
                 value: '1'
                 },
                 {
                 text: '2个月',
                 value: '2'
                 },
                 {
                 text: '3个月',
                 value: '3'
                 },
                 {
                 text: '4个月',
                 value: '4'
                 },
                 {
                 text: '5个月',
                 value: '5'
                 },
                 {
                 text: '6个月',
                 value: '6'
                 },
                 {
                 text: '7个月',
                 value: '7'
                 },
                 {
                 text: '8个月',
                 value: '8'
                 },
                 {
                 text: '9个月',
                 value: '9'
                 },
                 {
                 text: '9个月',
                 value: '9'
                 },
                 {
                 text: '10个月',
                 value: '9'
                 },
                 {
                 text: '10个月',
                 value: '10'
                 },
                 {
                 text: '11个月',
                 value: '11'
                 }
                 ];
                 } else if (vm.netWork.$$buyCycleYear == '12') {
                 vm.list = [
                 {
                 text: '1年',
                 value: '1'
                 },
                 {
                 text: '2年',
                 value: '2'
                 },
                 {
                 text: '3年',
                 value: '3'
                 }
                 ];
                 }
                 vm.netWork.buyCycle = vm.netWork.$$buyCycleYear * vm.netWork.$$buyCycleMonth;
                 if (vm.netWork.$$buyCycleYear == "12") {
                 vm.netWork.$$buyCycleName = vm.netWork.$$buyCycleMonth + "年";
                 } else {
                 vm.netWork.$$buyCycleName = vm.netWork.$$buyCycleMonth + "个月";
                 }*/
                if (vm.netWork.buyCycle) {
                    /*VpnService.getPrice(vm.vpnModel).then(function (response) {
                     vm.totalPrice = response;
                     vm.vpnModel.price = response;
                     });*/
                    VpcService.getPrice(vm.netWork).then(function (price) {
                        vm.hasGotPrice = true;
                        vm.netWork.price = price;
                    }, function (message) {
                        vm.hasGotPrice = false;
                        vm.priceMsg = message;
                    });
                }
            };
            /*更改带宽数*/
            vm.changeRate = function () {
                vm.getPrice();
                if (!vm.outOfQuota) {
                    VpcService.getQosNumByPrjId(vm.netWork.prjId).then(function (response) {
                        vm.bandQuotas = response;
                    });
                }
            };
            /**
             * 购买周期类型
             */
            vm.buyCycleType = function () {
                vm.cycleTypeList = [];
                eayunHttp.post('cloud/vm/queryBuyCycleType.do').then(function (response) {
                    if (response && response.data) {
                        vm.cycleTypeList = response.data.data;
                    }
                    if (vm.cycleTypeList.length > 0) {
                        if (!vm.netWork.cycleType) {
                            vm.netWork.cycleType = vm.cycleTypeList[0].nodeId;
                        }
                        vm.queryBuyCycle();
                    }
                });

            };

            /**
             * 选择购买周期类型
             */
            vm.changeCycleType = function () {
                vm.netWork.buyCycle = null;
                vm.queryBuyCycle();
            };

            /**
             * 购买周期选择
             */
            vm.queryBuyCycle = function () {
                vm.cycleList = [];
                eayunHttp.post('cloud/vm/queryBuyCycleList.do', vm.netWork.cycleType).then(function (response) {
                    if (response && response.data) {
                        vm.cycleList = response.data.data;
                    }

                    if (vm.cycleList.length > 0) {
                        if (!vm.netWork.buyCycle) {
                            vm.netWork.buyCycle = vm.cycleList[0].nodeNameEn;
                            vm.getPrice();
                        }
                    }
                });

            };
            vm.checkNetworkNameExist = function () {
                VpcService.checkNetWorkName(vm.netWork).then(function (response) {
                    vm.isNameExist = !response;
                });
            };

            initial();
        }])
    .controller('payRequiredNetCtrl', ['$scope', 'eayunHttp', 'toast', 'eayunStorage', '$state', 'VpcService', 'DatacenterService',
        function ($scope, eayunHttp, toast, eayunStorage, $state, VpcService, DatacenterService) {
            var vm = this;
            /*总体初始化页面*/
            var initial = function () {
                initData();
                initDatacenter();
                vm.getPrice();
                vm.getBuyCondition();
                //vm.getAccountBalance();
            };
            /*初始化页面数据*/
            var initData = function () {
                var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
                var userInfo = JSON.parse(sessionStorage["userInfo"]);
                vm.isCre = eayunStorage.get("isCre");
                vm.netWork = eayunStorage.get("netWork");
                vm.hasGotPrice = true;
                if (vm.isCre == null) {
                    vm.netWork = {
                        orderType: "0",
                        buyCycle: 1,
                        payType: "2",
                        rate: 1,
                        prjId: dcPrj.projectId,
                        dcId: dcPrj.dcId,
                        dcName: dcPrj.dcName,
                        cusId: userInfo.cusId,
                        createName: userInfo.userName
                    };
                } else {
                    eayunStorage.delete("isCre");
                }
            };
            /*初始化数据中心*/
            var initDatacenter = function () {
                DatacenterService.getDcPrjList().then(function (response) {
                    vm.datacenters = response;
                    var initialized = false;
                    if (vm.datacenters != null) {
                        angular.forEach(vm.datacenters, function (value, key) {
                            if (vm.netWork.prjId == value.projectId) {
                                initialized = true;
                                vm.selectDcPrj(value);
                            }
                        });
                        if (!initialized) {
                            vm.selectDcPrj(vm.datacenters[0]);
                        }
                    }
                });
            };
            //获取剩余额度
            vm.getAccountBalance = function () {
                eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (data) {
                    vm.balance = data.data.data.money;
                });
            };
            //获取购买最低剩余额度
            vm.getBuyCondition = function () {
                eayunHttp.post('sysdatatree/getbuycondition.do').then(function (data) {
                    vm.limit = data.data;
                });
            };
            /*获取价格*/
            vm.getPrice = function () {
                VpcService.getPrice(vm.netWork).then(function (data) {
                    vm.hasGotPrice = true;
                    vm.netWork.price = data;
                }, function (message) {
                    vm.hasGotPrice = false;
                    vm.priceMsg = message;
                });
            };
            /*更改带宽数*/
            vm.changeRate = function () {
                vm.getPrice();
                if (!vm.outOfQuota) {
                    VpcService.getQosNumByPrjId(vm.netWork.prjId).then(function (response) {
                        vm.bandQuotas = response;
                    });
                }
            };
            /*立即充值*/
            vm.recharge = function () {
                var routeUrl = "app.costcenter.guidebar.account";
                var rechargeNavList = [{route: routeUrl, name: '账户总览'}];
                eayunStorage.persist('rechargeNavList', rechargeNavList);
                $state.go('pay.recharge');
            };
            /*跳转订单确认页面*/
            vm.goToOrder = function () {
                vm.getBuyCondition();
                vm.getAccountBalance();
                VpcService.queryAccount().then(function (account) {
                    vm.balance = account;
                    DatacenterService.getBuyCondition().then(function (condition) {
                        vm.limit = condition;
                        if (vm.balance < vm.limit) {
                            vm.isShow = true;
                        } else {
                            vm.isShow = false;
                            eayunStorage.set("netWork", vm.netWork);
                            $state.go("buy.verifyNetWork", {source: "cre_after"});
                        }
                    });
                });
            };
            /*选择数据中心的接口*/
            vm.selectDcPrj = function (_datacenter) {
                vm.netWork.dcId = _datacenter.dcId;
                vm.netWork.dcName = _datacenter.dcName;
                vm.netWork.prjId = _datacenter.projectId;
                vm.getPrice();
                VpcService.getNetworkQuotasByPrjId(vm.netWork.prjId).then(function (response) {
                    vm.netQuotas = response;
                    vm.outOfQuota = vm.netQuotas <= 0;
                });
                vm.checkNetworkNameExist();
                VpcService.getQosNumByPrjId(vm.netWork.prjId).then(function (response) {
                    vm.bandQuotas = response;
                });
            };

            vm.checkNetworkNameExist = function () {
                VpcService.checkNetWorkName(vm.netWork).then(function (response) {
                    vm.isNameExist = !response;
                });
            };

            initial();
        }])
    .controller('BuyVpcCtrl', ['eayunStorage', 'DatacenterService', 'VpcService', 'BuyCycle', '$state', '$stateParams',
        function (eayunStorage, DatacenterService, VpcService, BuyCycle, $state, $stateParams) {
            var vm = this;
            /*初始化总方法*/
            var initial = function () {
                if ($stateParams.orderNo) {
                    vm.checkBuyBtn = false;
                    vm.isNameExist = false;
                    vm.hasGotPrice = true;
                    vm.fromOrder = '';
                    getBuyCondition();
                    vm.buyCycleTemp = 1;

                    VpcService.getNetworkByOrderNo($stateParams.orderNo).then(function (network) {
                        if (network.payType == '1') {
                            network.cycleType = (network.buyCycle < 12) ? 'month' : 'year';
                        }
                        /*后台获取的cloudordernetwork的数据包当中，去掉主键id，以防止save入库抹掉已有数据*/
                        delete network.orderNetWorkId;
                        initDataOfOrderBack(network);
                        initDatacenter();
                        initBuyCycleOption();
                    });
                } else {
                    initData();
                    initDatacenter();
                    initBuyCycleOption();
                }
            };
            /*初始化数据*/
            var initData = function () {
                vm.checkBuyBtn = false;
                vm.isNameExist = false;
                vm.hasGotPrice = true;
                vm.rateValid = true;
                vm.fromOrder = '';
                getBuyCondition();
                vm.buyCycleTemp = 1;

                var dcPrj = JSON.parse(sessionStorage['dcPrj']);
                var userInfo = JSON.parse(sessionStorage['userInfo']);

                vm.vpc = {
                    orderType: '0',
                    payType: '1',
                    dcId: dcPrj.dcId,
                    dcName: dcPrj.dcName,
                    prjId: dcPrj.projectId,
                    cusId: userInfo.cusId,
                    createName: userInfo.userName,
                    rate: 1
                };

                var temp = eayunStorage.get('buy_vpc_again');
                eayunStorage.delete('buy_vpc_again');
                if (angular.isDefined(temp)) {
                    initDataOfOrderBack(temp);
                }
            };
            /*初始化返回配置的购买页数据*/
            var initDataOfOrderBack = function (_temp) {
                vm.vpc = angular.copy(_temp, {});
                vm.fromOrder = 'backFromOrder';
                vm.buyCycleTemp = vm.vpc.buyCycle;
                vm.typeChoose(vm.vpc.payType);
            };
            /*初始化数据中心*/
            var initDatacenter = function () {
                DatacenterService.getDcPrjList().then(function (dataList) {
                    vm.datacenters = dataList;
                    var initialized = false;
                    if (vm.datacenters.length > 0) {
                        angular.forEach(vm.datacenters, function (value, key) {
                            if (vm.vpc.prjId == value.projectId) {
                                initialized = true;
                                vm.selectDcPrj(value);
                            }
                        });
                        if (!initialized) {
                            vm.selectDcPrj(vm.datacenters[0]);
                        }
                    }
                });
            };
            /*初始化购买周期的选项*/
            var initBuyCycleOption = function () {
                vm.cycleTypeList = BuyCycle.cycleTypeList;
                if (vm.fromOrder != 'backFromOrder' || (vm.fromOrder == 'backFromOrder' && vm.vpc.payType == '2')) {
                    vm.vpc.cycleType = 'month';
                    vm.changeCycleType();
                } else {
                    vm.changeCycleType(vm.fromOrder);
                }
            };
            /*选择付款方式*/
            vm.typeChoose = function (_payType) {
                vm.vpc.payType = _payType;
                //initDatacenter();
                if (_payType == '2') {
                    vm.buyCycleTemp = vm.vpc.buyCycle;
                    vm.vpc.buyCycle = 1;
                } else {
                    vm.vpc.buyCycle = vm.buyCycleTemp;
                }
                getPrice();
            };
            /*选择数据中心和项目*/
            vm.selectDcPrj = function (_datacenter) {
                vm.vpc.dcId = _datacenter.dcId;
                vm.vpc.dcName = _datacenter.dcName;
                vm.vpc.prjId = _datacenter.projectId;
                getPrice();
                getNetworkQuotasByPrjId(vm.vpc.prjId);
                vm.checkVpcNameExist();
                getQosNumByPrjId(vm.vpc.prjId);
            };
            /*获取项目下私有网络的数量配额剩余*/
            var getNetworkQuotasByPrjId = function (_prjId) {
                VpcService.getNetworkQuotasByPrjId(_prjId).then(function (netQuotas) {
                    vm.netQuotas = netQuotas;
                });
            };
            /*校验私有网络名称是否重复*/
            vm.checkVpcNameExist = function () {
                vm.checkBuyBtn = true;
                VpcService.checkNetWorkName(vm.vpc).then(function (response) {
                    vm.isNameExist = !response;
                    vm.checkBuyBtn = false;
                });
            };
            /*获取项目下私有网络的带宽配额剩余*/
            var getQosNumByPrjId = function (_prjId) {
                VpcService.getQosNumByPrjId(_prjId).then(function (bandQuotas) {
                    vm.bandQuotas = bandQuotas;
                });
            };
            /*更改带宽数*/
            vm.changeRate = function () {
                var regExp = new RegExp("^[1-9][0-9]{0,8}$");
                if (regExp.test(vm.vpc.rate)) {
                    vm.rateValid = true;
                    getPrice();
                    getQosNumByPrjId(vm.vpc.prjId);
                } else {
                    vm.rateValid = false;
                    vm.vpc.price = 0;
                }

            };
            /*选择付款方式为年付或月付*/
            vm.changeCycleType = function (fromOrder) {
                var flag = (fromOrder == 'backFromOrder' && vm.vpc.payType == '1');
                vm.cycleList = [];
                angular.forEach(BuyCycle.cycleList, function (value, key) {
                    if (vm.vpc.cycleType == key) {
                        vm.cycleList = value;
                    }
                });
                if (vm.cycleList.length > 0 && !flag) {
                    vm.vpc.buyCycle = vm.cycleList[0].value;
                }
                getPrice();
            };
            /*更改购买周期*/
            vm.changeBuyCycle = function () {
                getPrice();
            };
            /*获取价格*/
            var getPrice = function () {
                if (vm.vpc.buyCycle) {
                    VpcService.getPrice(vm.vpc).then(function (price) {
                        vm.hasGotPrice = true;
                        vm.vpc.price = price;
                    }, function (message) {
                        vm.hasGotPrice = false;
                        vm.priceMsg = message;
                    });
                }
            };
            /*获取后付费最小余额值*/
            var getBuyCondition = function () {
                DatacenterService.getBuyCondition().then(function (condition) {
                    vm.buyCondition = condition;
                });
            };
            /*立即充值*/
            vm.recharge = function () {
                var routeUrl = "app.costcenter.guidebar.account";
                var rechargeNavList = [{route: routeUrl, name: '账户总览'}];
                eayunStorage.persist('rechargeNavList', rechargeNavList);
                $state.go('pay.recharge');
            };
            /*立即购买*/
            vm.buyAtOnce = function () {

                VpcService.queryAccount().then(function (money) {
                    DatacenterService.getBuyCondition().then(function (condition) {
                        vm.buyCondition = condition;
                        vm.isNSF = money < vm.buyCondition;
                        if (!(vm.vpc.payType == '2' && vm.isNSF)) {
                            /*配额校验，暂时不做*/
                            /*VpcService.getNetworkQuotasByPrjId(vm.vpc.prjId).then(function(netQuotas){

                             });*/
                            eayunStorage.set('vpc', vm.vpc);
                            $state.go('buy.verifyNetWork', {source: 'buy_vpc'});
                        }
                    });
                });
            };

            initial();
        }])
    .controller('VerifyNetWorkCtrl', ['$scope', 'eayunHttp', 'toast', 'eayunStorage', '$state', '$stateParams', 'DatacenterService', 'VpcService', 'SysCode', 'eayunMath',
        function ($scope, eayunHttp, toast, eayunStorage, $state, $stateParams, DatacenterService, VpcService, SysCode, eayunMath) {
            var vm = this;

            var initial = function () {
                initOperation();
                //initData();

            };

            var initOperation = function () {
                vm.isBalance = false;
                vm.source = $stateParams.source;
                vm.network = {};
                if (vm.source == 'buy_vpc') {
                    vm.netWork = eayunStorage.get('vpc');
                } else {
                    vm.netWork = eayunStorage.get("netWork");
                }
                if (vm.netWork == null) {
                    if (vm.source == 'buy_vpc') {//来源创建
                        $state.go("buy.vpcBuy");
                    } else if (vm.source == 'cre_after') {
                        $state.go('buy.buyNetWork.payRequired');
                    } else if (vm.source == 'change_network_list') {
                        $state.go("app.net.netbar.net");
                    } else if (vm.source == 'change_network_detail') {
                        $state.go('app.net.datilNetWork', {netId: $stateParams.netId});
                    }
                } else {
                    initData();
                }
            };

            var initData = function () {
                vm.checkBtn = true;
                vm.netWork.$$productCount = 1;
                eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (data) {
                    vm.netWork.$$balance = data.data.data.money > 0 ? data.data.data.money : 0;
                    if (vm.netWork.payType == "1") {
                        if (vm.netWork.orderType == '0') {
                            vm.netWork.$$orderName = "私有网络-包年包月";
                        } else {
                            vm.netWork.$$orderName = '私有网络-更改带宽';
                        }
                        vm.netWork.$$payTypeName = "预付费";
                    } else {
                        if (vm.netWork.orderType == '0') {
                            vm.netWork.$$orderName = "私有网络-按需付费";
                        } else {
                            vm.netWork.$$orderName = '私有网络-更改带宽';
                        }
                        vm.netWork.$$payTypeName = "后付费";
                    }
                    usePrice();
                });
            };
            //计算价钱--true 使用余额支付，false-不使用余额支付
            function usePrice() {
                if (vm.isBalance) {//使用余额支付
                    if (vm.netWork.$$balance >= vm.netWork.price) {
                        vm.netWork.accountPayment = vm.netWork.price;
                        vm.netWork.thirdPartPayment = 0;
                    } else {
                        vm.netWork.accountPayment = vm.netWork.$$balance;
                        vm.netWork.thirdPartPayment = eayunMath.sub(Number(vm.netWork.price), vm.netWork.accountPayment);
                        /*vm.netWork.thirdPartPayment = vm.netWork.price - vm.netWork.accountPayment;*/
                    }
                } else {
                    vm.netWork.accountPayment = 0;//余额支付
                    vm.netWork.thirdPartPayment = vm.netWork.price;//第三方支付
                }
            }

            vm.useBalance = function () {
                vm.isBalance = !vm.isBalance;
                usePrice();
            };

            vm.commitOrderNetWork = function () {
                vm.checkBtn = false;
                if (vm.netWork.orderType == '2') {//升级-修改带宽
                    eayunHttp.post("cloud/netWork/changenetwork.do", vm.netWork).then(function (data) {
                        if (data.data.respCode == '010110') {
                            var errMsg = data.data.respMsg;
                            vm.isError = true;
                            if ('RATE_OUT_OF_QUOTA' == errMsg) {
                                vm.errorMsg = '您的带宽配额不足，请提交工单申请配额';
                            } else if ('UPGRADING_OR_RENEWING' == errMsg) {
                                vm.errorMsg = '资源正在调整中或您有未完成的订单，请您稍后再试';
                            } else if ('BALANCE_OF_ARREARS' == errMsg) {
                                DatacenterService.getBuyCondition().then(function (response) {
                                    vm.payAfterCondition = response;
                                    vm.errorMsg = '您的账户已欠费，请充值后操作';
                                });
                            } else if ('CHANGE_OF_BILLINGFACTORY' == errMsg) {
                                VpcService.getNetworkByNetId(vm.netWork.netId).then(function (_network) {
                                    vm.netWork.endTime = _network.endTime;
                                    VpcService.getPrice(vm.netWork).then(function (response) {
                                        vm.errorMsg = '您的订单金额发生变动，请重新确认订单';
                                        vm.netWork.price = response;
                                        usePrice();
                                        vm.checkBtn = true;
                                    });
                                });
                            } else if ('CHANGE_OF_BALANCE' == errMsg) {
                                VpcService.queryAccount().then(function (money) {
                                    vm.errorMsg = '您的余额发生变动，请重新确认订单';
                                    vm.netWork.$$balance = money > 0 ? money : 0;
                                    usePrice();
                                    vm.checkBtn = true;
                                });
                            } else if ('CHANGE_OF_CONFIGURATION' == errMsg) {
                                vm.errorMsg = '您的订单规格发生变动，请重新确认订单';
                            }
                        } else if (data.data.respCode == SysCode.success) {
                            if (vm.netWork.payType == "1") {//包年包月
                                if (vm.netWork.thirdPartPayment == 0) {
                                    //余额足够付费，付款成功
                                    $state.go('pay.result', {subject: "私有网络-更改带宽"});
                                } else {
                                    if (data.data.orderNo) {
                                        var orderPayNavList = [{route: 'app.net.netbar.net', name: '私有网络'}];
                                        var ordersIds = [data.data.orderNo];
                                        eayunStorage.persist("payOrdersNo", ordersIds);
                                        eayunStorage.persist("orderPayNavList", orderPayNavList);
                                        $state.go('pay.order');
                                    }
                                }
                            } else {//按需计费
                                $state.go("app.order.list");
                            }
                        } else if (data.data.respCode == SysCode.error) {
                            $state.go('app.order.list');
                        }
                    });
                } else {//购买
                    eayunHttp.post("cloud/netWork/buynetwork.do", vm.netWork).then(function (data) {
                        if (data.data.respCode == SysCode.warning) {
                            var errMsg = data.data.respMsg;
                            vm.isError = true;
                            if ('COUNT_OUT_OF_QUOTA' == errMsg) {
                                vm.errorMsg = '您的私有网络数量配额不足，请提交工单申请配额';
                            } else if ('RATE_OUT_OF_QUOTA' == errMsg) {
                                vm.errorMsg = '您的带宽配额不足，请提交工单申请配额';
                            } else if ('COUNTRATE_OUT_OF_QUOTA' == errMsg) {
                                vm.errorMsg = '您的私有网络数量、带宽配额不足，请提交工单申请配额';
                            } else if ('NOT_SUFFICIENT_FUNDS' == errMsg) {
                                DatacenterService.getBuyCondition().then(function (response) {
                                    vm.payAfterCondition = response;
                                    vm.errorMsg = '您的账户余额不足' + vm.payAfterCondition + '元，请充值后操作';
                                });
                            } else if ('CHANGE_OF_BILLINGFACTORY' == errMsg) {
                                VpcService.getPrice(vm.netWork).then(function (price) {
                                    vm.errorMsg = '您的订单金额发生变动，请重新确认订单';
                                    vm.netWork.price = price;
                                    vm.isBalance = false;
                                    usePrice();
                                    vm.checkBtn = true;
                                });
                            } else if ('CHANGE_OF_BALANCE' == errMsg) {
                                VpcService.queryAccount().then(function (money) {
                                    vm.errorMsg = '您的余额发生变动，请重新确认订单';
                                    vm.netWork.$$balance = money > 0 ? money : 0;
                                    vm.isBalance = false;
                                    usePrice();
                                    vm.checkBtn = true;
                                });
                            }
                        } else  if (data.data.respCode == SysCode.success) {
                            if (vm.netWork.payType == "1") {//包年包月
                                if (vm.netWork.thirdPartPayment == 0) {
                                    //余额足够付费，付款成功
                                    $state.go('pay.result', {subject: "私有网络-包年包月"});
                                } else {
                                    if (data.data.orderNo) {
                                        var orderPayNavList = [{
                                            route: 'app.net.netbar.net',
                                            name: '私有网络'
                                        }, {
                                            route: 'buy.vpcBuy',
                                            name: '创建私有网络'
                                        }];
                                        var ordersIds = [data.data.orderNo];
                                        eayunStorage.persist("orderPayNavList", orderPayNavList);
                                        eayunStorage.persist("payOrdersNo", ordersIds);
                                        $state.go('pay.order');
                                    }
                                }
                            } else {//按需计费
                                $state.go("app.order.list");
                            }
                        } else if (data.data.respCode == SysCode.error) {
                            $state.go("app.order.list");
                        }
                    });
                }
            };
            /*返回修改配置*/
            vm.goToCreateNetWork = function () {
                eayunStorage.set("isCre", "0");
                eayunStorage.set('buy_vpc_again', vm.netWork);
                $state.go('buy.vpcBuy');
            };

            initial();
        }])
    /*
     * 编辑网络的ctrl
     */
    .controller("EditNetWork", ['$scope', 'eayunModal', 'eayunHttp', 'haveBandCount', 'prjBandCount', 'netWork', '$state', 'eayunStorage', 'VpcService', 'DatacenterService', '$modalInstance',
        function ($scope, eayunModal, eayunHttp, haveBandCount, prjBandCount, netWork, $state, eayunStorage, VpcService, DatacenterService, $modalInstance) {
            var initial = function () {
                initData();
                getBandHavePrj();
                api.getPrice($scope.netWork);
            };
            /*初始化界面数据*/
            var initData = function () {
                $scope.netWork = angular.copy(netWork, {});
                $scope.netWork.orderType = '2';
                $scope.netWork.buyCycle = 1;
                $scope.netWork.rateOld = netWork.rate;
                // 带宽显示
                VpcService.getQosNumByPrjId($scope.netWork.prjId).then(function (response) {
                    $scope.bandQuotas = response;
                });
                $scope.prjBandCount = prjBandCount;//配额量
                $scope.haveBandCount = haveBandCount;//使用量
                $scope.rateCount = angular.copy(haveBandCount);//预计的已使用量
                /*初始化校验*/
                $scope.hasGotPrice = true;
                $scope.rateValid = true;
            };
            // 输入带宽
            $scope.computeBand = function () {
                var rate = netWork != null ? netWork.rate : 0;
                if (angular.isDefined($scope.netWork.rate) && null != $scope.netWork.rate) {
                    $scope.rateCount = $scope.haveBandCount + parseInt($scope.netWork.rate) - rate;
                    $scope.syRate = $scope.prjBandCount - parseInt($scope.haveBandCount) + parseInt(rate);
                } else {
                    $scope.rateCount = angular.copy($scope.haveBandCount - rate);//预计的已使用量
                }
                api.getPrice($scope.netWork);
            };
            /*获取价格接口 & 更改带宽获取变化后的价格*/
            $scope.getPrice = function () {
                if (!($scope.netWork.payType == '1' && $scope.netWork.rate <= $scope.netWork.rateOld)) {
                    VpcService.getPrice($scope.netWork).then(function (price) {
                        $scope.hasGotPrice = true;
                        $scope.netWork.price = price;
                    }, function (message) {
                        $scope.hasGotPrice = false;
                        $scope.priceMsg = message;
                    });
                } else {
                    $scope.netWork.price = 0;
                }
            };
            /*更改带宽的交互接口*/
            $scope.changeBand = function () {
                var regExp = new RegExp("^[1-9][0-9]{0,8}$");
                console.log($scope.netWork.rate);
                if (regExp.test($scope.netWork.rate)) {
                    $scope.rateValid = true;
                    if (!($scope.netWork.payType == '1' && $scope.netWork.rate <= $scope.netWork.rateOld)) {
                        api.getPrice($scope.netWork);
                        return;
                    }
                } else {
                    $scope.rateValid = false;
                }
                $scope.netWork.price = 0;
            };

            function getBandHavePrj() {
                eayunHttp.post('cloud/route/getPrjBandCount.do', {prjId: $scope.netWork.prjId}).then(function (response) {
                    $scope.prjBandCount = response.data;
                });
                eayunHttp.post('cloud/route/getHaveBandCount.do', {prjId: $scope.netWork.prjId}).then(function (response) {
                    $scope.haveBandCount = response.data;
                    $scope.rateCount = angular.copy(response.data);
                });
            };
            /*提交更改带宽的数据*/
            $scope.commit = function () {
                VpcService.checkIfOrderExist($scope.netWork).then(function () {
                    $modalInstance.dismiss();
                    eayunModal.warning('资源正在调整中或您有未完成的订单，请您稍后再试。');
                }, function () {
                    DatacenterService.queryAccount().then(function (account) {
                        if (account <= 0 && '2' == $scope.netWork.payType) {
                            $modalInstance.dismiss();
                            eayunModal.warning('您的账户已欠费，请充值后操作');
                        } else {
                            VpcService.getQosNumByPrjId($scope.netWork.prjId).then(function (bandQuotas) {
                                $scope.bandQuotas = bandQuotas;
                                if ($scope.netWork.rate - $scope.netWork.rateOld <= $scope.bandQuotas) {
                                    $modalInstance.close($scope.netWork);
                                }
                            });
                        }
                    });
                });
            };
            /*关闭窗口*/
            $scope.cancel = function () {
                $modalInstance.dismiss();
            };
            /*api接口函数包*/
            var api = {
                getPrice: function (_network) {
                    VpcService.getPrice(_network).then(function (price) {
                        $scope.hasGotPrice = true;
                        $scope.netWork.price = price;
                    }, function (message) {
                        $scope.hasGotPrice = false;
                        $scope.priceMsg = message;
                    });
                }
            };

            initial();
        }])
    /*
     * 网络详情的ctrl
     */
    .controller("DatilNetWorkCtrl", function ($scope, $rootScope, $state, eayunModal, eayunHttp, $stateParams, toast, powerService, VpcService, eayunStorage, DatacenterService) {

        var initData = function () {
            var list = [{'router': 'app.net.netbar.net', 'name': '私有网络'}];
            $rootScope.navList(list, '网络详情', 'detail');

            $rootScope.netRoute = "#/app/net/netbar/net";
            $rootScope.netname = '私有网络';
            $scope.hintTagShow = [];
            $scope.checkEditBtn = true;
            $scope.resourceTags = {};
            eayunHttp.post('tag/getResourceTagForShowcase.do', {
                resType: 'network',
                resId: $stateParams.netId
            }).then(function (response) {
                $scope.resourceTags = response.data;
            });
            eayunHttp.post("cloud/netWork/findNetWorkByNetId.do", $stateParams.netId).then(function (respose) {
                $scope.netWork = respose.data;
                $scope.netWork.rateOld = $scope.netWork.rate;
                $scope.getNetStatus($scope.netWork);
            });

        };

        var temp = '';
        /*网络名称编辑框*/
        $scope.editNetworkName = function () {
            temp = $scope.netWork.netName;
            $scope.netNameEditable = true;
            $scope.hintNameShow = true;
        };
        /*校验网络名称重复*/
        $scope.checkNetworkNameExist = function (_network) {
            $scope.checkEditBtn = false;
            VpcService.checkNetWorkName(_network).then(function (response) {
                $scope.checkNetworkName = !response;
                $scope.checkEditBtn = true;
            });
        };
        /*保存修改名称*/
        $scope.saveEdit = function () {
            VpcService.updateNetworkName($scope.netWork).then(function (response) {
                if (response != null) {
                    $scope.netNameEditable = false;
                    $scope.hintNameShow = false;
                    toast.success('修改' + DatacenterService.toastEllipsis($scope.netWork.netName, 9) +'网络成功');
                }
            });
        };
        /*取消修改名称*/
        $scope.cancelEdit = function () {
            $scope.netNameEditable = false;
            $scope.hintNameShow = false;
            $scope.netWork.netName = temp;
        };
        // pop框方法
        $scope.openPopBox = function (net) {
            if (net.type == 'tagName') {
                $scope.tagShow = true;
            }
            $scope.description = net.value;
        };
        $scope.closePopBox = function (type) {
            if (type == 'tagName') {
                $scope.tagShow = false;
            }
        };
        $scope.openTableBox = function (net) {
            if (net.type == 'tagName') {
                $scope.hintTagShow[net.index] = true;
            }
            $scope.ellipsis = net.value;
        };
        $scope.closeTableBox = function (net) {
            if (net.type == 'tagName') {
                $scope.hintTagShow[net.index] = false;
            }
        };

        // 网络状态显示
        $scope.getNetStatus = function (model) {
            $scope.vmStatusClass = '';
            if (model.netStatus && model.netStatus == 'ACTIVE' && model.chargeState == '0') {
                $scope.vmStatusClass = 'green';
            } else if (model.netStatus && model.netStatus != 'ACTIVE' && model.chargeState == '0') {
                $scope.vmStatusClass = 'yellow';
            } else if (model.chargeState != '0') {
                $scope.vmStatusClass = 'gray';
            }
        };
        /*更改带宽*/
        $scope.changeBand = function (netWork) {
            if (netWork.routeId != null) {
                var result = eayunModal.open({
                    title: '更改带宽',
                    backdrop: 'static',
                    templateUrl: 'views/net/net/editnetwork.html',
                    controller: 'EditNetWork',
                    resolve: {
                        haveBandCount: function () {
                            return eayunHttp.post('cloud/route/getHaveBandCount.do', {prjId: netWork.prjId}).then(function (response) {
                                return response.data;
                            });
                        },
                        prjBandCount: function () {
                            return eayunHttp.post('cloud/route/getPrjBandCount.do', {prjId: netWork.prjId}).then(function (response) {
                                return response.data;
                            });
                        },
                        netWork: function () {
                            return netWork;
                        }
                    }
                });
                result.result.then(function (netWork) {
                    eayunStorage.set('netWork', {
                        orderType: '2',
                        buyCycle: 1,
                        payType: netWork.payType,
                        rate: netWork.rate,
                        rateOld: netWork.rateOld,
                        prjId: netWork.prjId,
                        dcId: netWork.dcId,
                        dcName: netWork.dcName,
                        netId: netWork.netId,
                        netName: netWork.netName,
                        endTime: netWork.endTime,
                        price: netWork.price
                    });
                    $state.go("buy.verifyNetWork", {source: "change_network_detail", netId: netWork.netId});
                }, function () {
                    // console.info('取消');
                });
            }
        };

        powerService.powerRoutesList().then(function (powerList) {
            $scope.modulePower = {
                isEditNet: powerService.isPower('net_edit'), // 编辑网络
                isBandWidth: powerService.isPower('net_bandwidth'), //更改带宽
                isCreSubNet: powerService.isPower('subnet_add'), // 添加子网
                isEditSubNet: powerService.isPower('subnet_edit'), // 编辑子网
                isSubNetTag: powerService.isPower('subnet_tag'), // 标签
                isDelSubNet: powerService.isPower('subnet_delete'), // 删除子网
                isSubNetRoute: powerService.isPower('subnet_route'), // 绑定、解绑路由
            };
        });

        $scope.myTable = {
            source: 'cloud/subnetwork/getSubNetListByNetId.do',
            api: {},
            getParams: function () {
                return {
                    netId: $stateParams.netId
                };
            }
        };

        $scope.netJson = function (tagsStr) {
            var json = {};
            if (tagsStr) {
                json = JSON.parse(tagsStr);
            }
            return json;
        };

        // ---子网绑定路由
        $scope.bindRoute = function (netWork, subnet) {
            eayunModal.confirm('确定连接路由?').then(function () {
                eayunHttp.post('cloud/route/connectSubnet.do', {
                    dcId: subnet.dcId,
                    prjId: subnet.prjId,
                    routeId: netWork.routeId,
                    subNetId: subnet.subnetId,
                    subnetName: subnet.subnetName,
                    subnetCidr: subnet.cidr,
                    routeName: netWork.routeName
                }).then(function (response) {
                    if (response.data.code != '010120') {
                        toast.success('连接路由成功！');
                        $scope.myTable.api.draw();
                    }
                });
            });
        };
        // ---子网解绑路由
        $scope.unbindRoute = function (netWork, subnet) {
            eayunModal.confirm('确定要断开子网' + subnet.subnetName + '同路由的连接？').then(function () {
                VpcService.checkDetachSubnet(subnet.subnetId).then(function (data) {
                    eayunHttp.post('cloud/route/detachSubnet.do', {
                        dcId: subnet.dcId,
                        prjId: subnet.prjId,
                        routeId: subnet.routeId,
                        subnetId: subnet.subnetId,
                        subnetName: subnet.subnetName,
                        subnetCidr: subnet.cidr
                    }).then(function (response) {
                        if(response.data.code != '010120'){
                            toast.success('断开路由成功！');
                            $scope.myTable.api.draw();
                        }
                    });
                }, function (message) {
                    eayunModal.warning(message);
                });
            });
        };
        /*删除子网*/
        $scope.deleteCloudSubNet = function (subNetWork) {
            var result = eayunModal.open({
                templateUrl: 'views/net/net/deletesubnet.html',
                controller: 'DeleteSubnetCtrl',
                controllerAs : 'delSub',
                resolve: {
                    item: function () {
                        return subNetWork;
                    }
                }
            });
            result.result.then(function () {
                $scope.myTable.api.draw();
            });
        };

        $scope.daleteCloudSubNet = function (subNetWork) {
            eayunModal.confirm('确定要删除子网' + subNetWork.subnetName + "？").then(function () {
            	VpcService.checkForDel(subNetWork).then(function () {
            		eayunHttp.post("cloud/subnetwork/deleteCloudSubNet.do", subNetWork).then(function (respose) {
            			if (respose.data) {
            				if (respose.data.code != "010120") {
            					toast.success("删除子网成功！");
            					$scope.myTable.api.draw();
            				}
            			} else {
            				eayunModal.warning("子网已和负载均衡关联，无法删除");
            			}
            			
            		});
            	}, function (message) {
            		eayunModal.warning(message);
            	});
            });
        };
        /* 标签 */
        $scope.tagResource = function (resType, resId) {
            var result = eayunModal.open({
                title: '标记资源',
                backdrop: 'static',
                templateUrl: 'views/tag/tagresource.html',
                controller: 'TagResourceCtrl',
                resolve: {
                    resType: function () {
                        return resType;
                    },
                    resId: function () {
                        return resId;
                    }
                }
            });
            result.result.then(function () {
                // 操作完成点击“关闭”按钮，页面后刷新table
                $scope.myTable.api.draw();
            }, function () {
                // 操作完成，点击“×”按钮，关闭页面后刷新table
                $scope.myTable.api.draw();
            });
        };

        $scope.commit = function () {
            // eayunModal.confirm('确认保存？').then(function () {
            $scope.subNetWork.prjId = netWork.prjId;
            $scope.subNetWork.dcId = netWork.dcId;
            $scope.subNetWork.netId = netWork.netId;
            $scope.subNetWork.type = type;
            $scope.ok($scope.subNetWork);
            // });
        };

        initData();
    })
    /*
     * 新增和编辑子网的ctrl
     */
    .controller("addSubNetWorkCtrl", function ($scope, eayunModal, eayunHttp, netWork, subNetWork, type, toast, VpcService, $modalInstance) {
        $scope.type = type;
        VpcService.getDnsByDcId(JSON.parse(sessionStorage["dcPrj"]).dcId).then(function (response) {
            $scope.dns = response;
        });
        // countSubNetWorkUsed();
        // function countSubNetWorkUsed(){
        // eayunHttp.post("cloud/subnetwork/findSubNetCountByPrjId.do",netWork.prjId).then(function(respose){
        // if(respose.data>= netWork.prjQuotaSubNet){
        // eayunModal.warning("子网数量超过项目规定限额，请先申请配额");
        // }
        // });
        // }
        // 选择网络地址类型
//			$scope.cidr = "A";
        $scope.ab = $scope.ac = $scope.bc = $scope.cc = "0";
        $scope.ae = $scope.bb = $scope.be = "16";
        $scope.changeRadio = function () {
            $scope.range = null;
            $scope.ab = $scope.ac = $scope.bc = $scope.cc = "0";
            $scope.ae = $scope.bb = $scope.be = "16";
            if ($scope.cidr == "A") {
                $scope.aTypeDis = false;
                $scope.bTypeDis = true;
                $scope.cTypeDis = true;
                $scope.subNetWork.cidr = '10.' + $scope.ab + "." + $scope.ac + ".0/" + $scope.ae;
            } else if ($scope.cidr == "B") {
                $scope.aTypeDis = true;
                $scope.bTypeDis = false;
                $scope.cTypeDis = true;
                $scope.subNetWork.cidr = '172.' + $scope.bb + "." + $scope.bc + ".0/" + $scope.ae;
            } else if ($scope.cidr == "C") {
                $scope.aTypeDis = true;
                $scope.bTypeDis = true;
                $scope.cTypeDis = false;
                $scope.subNetWork.cidr = '192.168.' + $scope.cc + ".0/24";
            }
            $scope.checkTypeCidr(null);
//				$scope.checkCidr();
        };
        /***************************网络地址规则*********************************/
        var regx = /^(0|[1-9]\d*)$/;

        function checkCidr0_255(val, fromFunc) {
            if (fromFunc == "focus") {
                $scope.range = "可填写0-255之间的整数！";
            } else {
                $scope.range = null;
            }
            if (val < 0 || val > 255 || !regx.test(val) || val == "") {
                return true;
            } else {
                return false;
            }
        }
        ;
        function checkCidr16_24(val, fromFunc) {
            if (fromFunc == "focus") {
                $scope.range = "可填写16-24之间的整数！";
            } else {
                $scope.range = null;
            }
            if (val < 16 || val > 24 || !regx.test(val) || val == "") {
                return true;
            } else {
                return false;
            }
        }

        function checkCidr16_31(val, fromFunc) {
            if (fromFunc == "focus") {
                $scope.range = "可填写16-31之间的整数！";
            } else {
                $scope.range = null;
            }
            if (val < 16 || val > 31 || !regx.test(val) || val == "") {
                return true;
            } else {
                return false;
            }
        }

        /**********************************************************************/
        $scope.checkTypeCidr = function (position, fromFunc) {
            if (position == '' || position == null) {
                $scope.abError = false;
                $scope.acError = false;
                $scope.aeError = false;
                $scope.bbError = false;
                $scope.bcError = false;
                $scope.beError = false;
                $scope.ccError = false;
                $scope.cidrError = false;
                return;
            }
            if (position == 'ab') {
                $scope.abError = checkCidr0_255($scope.ab, fromFunc);
                $scope.cidrShowError = $scope.abError;
            } else if (position == 'ac') {
                $scope.acError = checkCidr0_255($scope.ac, fromFunc);
                $scope.cidrShowError = $scope.acError;
            } else if (position == 'ae') {
                $scope.aeError = checkCidr16_24($scope.ae, fromFunc);
                $scope.cidrShowError = $scope.aeError;
            } else if (position == 'bb') {
                $scope.bbError = checkCidr16_31($scope.bb, fromFunc);
                $scope.cidrShowError = $scope.bbError;
            } else if (position == 'bc') {
                $scope.bcError = checkCidr0_255($scope.bc, fromFunc);
                $scope.cidrShowError = $scope.bcError;
            } else if (position == 'be') {
                $scope.beError = checkCidr16_24($scope.be, fromFunc);
                $scope.cidrShowError = $scope.beError;
            } else if (position == 'cc') {
                $scope.ccError = checkCidr0_255($scope.cc, fromFunc);
                $scope.cidrShowError = $scope.ccError;
            }
            $scope.cidrError = false;
            /*$scope.cidrShowError = false;*/
            if ($scope.cidr == "A" && !$scope.abError && !$scope.acError && !$scope.aeError) {
                $scope.subNetWork.cidr = '10.' + parseInt($scope.ab) + "." + parseInt($scope.ac) + ".0/" + parseInt($scope.ae);
//					$scope.checkCidr();
            } else if ($scope.cidr == "B" && !$scope.bbError && !$scope.bcError && !$scope.beError) {
                $scope.subNetWork.cidr = '172.' + parseInt($scope.bb) + "." + parseInt($scope.bc) + ".0/" + parseInt($scope.be);
//					$scope.checkCidr();
            } else if ($scope.cidr == "C" && !$scope.ccError) {
                $scope.subNetWork.cidr = '192.168.' + parseInt($scope.cc) + ".0/24";
//					$scope.checkCidr();
            } else {
                $scope.cidrError = true;
            }
        };
        // 验证子网名称 || subNetName ==subNetWork.subnetName
        $scope.checkSubNetName = function (subNetName) {
            if (subNetName == null) {
                return false;
            }
            var sunNetId = null;
            if ($scope.subNetWork != null) {
                sunNetId = $scope.subNetWork.subnetId;
            }
            return eayunHttp.post("cloud/subnetwork/checkSubNetName.do", {
                "subnetId": sunNetId, "subNetName": subNetName,
                "prjId": netWork.prjId
            }).then(function (respose) {
                return respose.data;
            });
        };
        $scope.isSelected = function () {
            if ($scope.subNetWork.isforbidgateway == false || $scope.subNetWork.isforbidgateway == undefined) {
                $scope.subNetWork.gatewayIp = '';
            }
        };
        if (type == "edit") {
            subNetWork.prjId = netWork.prjId;
            subNetWork.dcId = netWork.dcId;
            subNetWork.netId = netWork.netId;
            $scope.subNetWork = angular.copy(subNetWork);
            eayunHttp.post("cloud/subnetwork/getSubnetDNS.do", {"subnetId": $scope.subNetWork.subnetId}).then(function (respose) {
                if (respose.data) {
                    var dnses = respose.data;
                    var dns = dnses.split(';')
                    $scope.dnsa = {};
                    $scope.dnsb = {};
                    $scope.dnsc = {};
                    $scope.initData($scope.dnsa, dns[0]);
                    $scope.initData($scope.dnsb, dns[1]);
                    $scope.initData($scope.dnsc, dns[2]);
                }
            });
        } else {
            $scope.subNetWork = {
                prjId: netWork.prjId,
                dcId: netWork.dcId,
                netId: netWork.netId,
                subnetType: '1'
            };
            $scope.cidrError = false;
            $scope.aTypeDis = true;
            $scope.bTypeDis = true;
            $scope.cTypeDis = true;
//				$scope.changeRadio();//默认走一次
        }
        $scope.shortName = function (name) {
            if (name && name.length > 9)
                return name.substring(0, 9) + '...';
            return name;
        };
        //验证网络地址是否重复
//			$scope.checkCidr=function(){
//				eayunHttp.post( "cloud/subnetwork/checkCidr.do", { "cidr" : $scope.subNetWork.cidr, "netId" : netWork.netId }).then(function(respose){
//					if(!respose.data){
//						$scope.cidrError=true;
//						$scope.range="网络地址："+$scope.subNetWork.cidr+"已存在！";
//						return true;
//					}else{
//						$scope.cidrError=false;
//						return false;
//					};
//				});
//			};

        /**
         * 初始化数据
         */
        $scope.initData = function (data, addr) {
            if (addr) {
                data.check = true;
                var data1 = addr.split(".");
                if (data1 && data1.length == 4) {
                    data.addr1 = {};
                    data.addr2 = {};
                    data.addr3 = {};
                    data.addr4 = {};
                    data.addr1.val = data1[0]
                    data.addr2.val = data1[1]
                    data.addr3.val = data1[2]
                    data.addr4.val = data1[3]
                }
            }
        }

        /**
         * 选择复选框
         */
        $scope.changeDnsBox = function (item, type) {
            if (!item.check) {
                if (type === 'a') {
                    $scope.dnsa = {};
                }
                else if (type === 'b') {
                    $scope.dnsb = {};
                }
                else if (type === 'c') {
                    $scope.dnsc = {};
                }
                if ((!$scope.dnsa || !$scope.dnsa.check) && (!$scope.dnsb || !$scope.dnsb.check) && (!$scope.dnsc || !$scope.dnsc.check)) {
                    $scope.dnsError = false;
                }
            }
            else {
                if (type === 'a') {
                    $scope.dnsa = {
                        check: true,
                        addr1: {val: 0},
                        addr2: {val: 0},
                        addr3: {val: 0},
                        addr4: {val: 0}
                    };
                }
                else if (type === 'b') {
                    $scope.dnsb = {
                        check: true,
                        addr1: {val: 0},
                        addr2: {val: 0},
                        addr3: {val: 0},
                        addr4: {val: 0}
                    };
                }
                else if (type === 'c') {
                    $scope.dnsc = {
                        check: true,
                        addr1: {val: 0},
                        addr2: {val: 0},
                        addr3: {val: 0},
                        addr4: {val: 0}
                    };
                }

            }
            $scope.checkAllDNS();
            $scope.checkExsitDNS();
        };

        /**
         * 校验DNS数据
         */
        $scope.checkAllDNS = function () {
            $scope.data = [];
            $scope.dnsError = false;
            if ($scope.dnsa && $scope.dnsa.check) {
                if ((!$scope.dnsa.addr1 || $scope.dnsa.addr1.error)
                    || (!$scope.dnsa.addr2 || $scope.dnsa.addr2.error)
                    || (!$scope.dnsa.addr3 || $scope.dnsa.addr3.error)
                    || (!$scope.dnsa.addr4 || $scope.dnsa.addr4.error)) {
                    $scope.dnsError = true;
                }
                else {
                    var data = $scope.dnsa.addr1.val + '.' + $scope.dnsa.addr2.val + '.' + $scope.dnsa.addr3.val + '.' + $scope.dnsa.addr4.val;
                    $scope.data.push(data);
                }
            }
            if ($scope.dnsb && $scope.dnsb.check) {
                if ((!$scope.dnsb.addr1 || $scope.dnsb.addr1.error)
                    || (!$scope.dnsb.addr2 || $scope.dnsb.addr2.error)
                    || (!$scope.dnsb.addr3 || $scope.dnsb.addr3.error)
                    || (!$scope.dnsb.addr4 || $scope.dnsb.addr4.error)) {
                    $scope.dnsError = true;
                }
                else {
                    var data = $scope.dnsb.addr1.val + '.' + $scope.dnsb.addr2.val + '.' + $scope.dnsb.addr3.val + '.' + $scope.dnsb.addr4.val;
                    $scope.data.push(data);
                }
            }
            if ($scope.dnsc && $scope.dnsc.check) {
                if ((!$scope.dnsc.addr1 || $scope.dnsc.addr1.error)
                    || (!$scope.dnsc.addr2 || $scope.dnsc.addr2.error)
                    || (!$scope.dnsc.addr3 || $scope.dnsc.addr3.error)
                    || (!$scope.dnsc.addr4 || $scope.dnsc.addr4.error)) {
                    $scope.dnsError = true;
                }
                else {
                    var data = $scope.dnsc.addr1.val + '.' + $scope.dnsc.addr2.val + '.' + $scope.dnsc.addr3.val + '.' + $scope.dnsc.addr4.val;
                    $scope.data.push(data);
                }
            }
        };

        /**
         * 校验单个DNS数据
         */
        $scope.checkDNS = function (item, type) {
            $scope.dnsInfoMsg = '';
            var regExp = new RegExp("^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$");
            if (!item) {
                item = {};
            }
            if (item && regExp.test(item.val)) {
                item.error = false;
                $scope.dnsNonformat = false;
            }
            else {
                item.error = true;
                $scope.dnsNonformat = true;
            }
            if (type != 'dis') {
                $scope.dnsInfoMsg = '请输入0-255之间的整数';
            }
            $scope.checkAllDNS();
            $scope.checkExsitDNS();
        };

        /**
         * 校验重复的DNS数据
         */
        $scope.checkExsitDNS = function () {
            $scope.dnsShowError = false;
            $scope.dnsErrMsg = '';
            $scope.dnsa.checkDNS = false;
            $scope.dnsb.checkDNS = false;
            $scope.dnsc.checkDNS = false;
            if ($scope.data && $scope.data.length > 1) {
                var dataa = $scope.data[0];
                var datab = $scope.data[1];
                var datac = $scope.data[2];

                if (datab && !datac && dataa === datab) {
                    $scope.dnsShowError = true;
                    $scope.dnsError = true;
                    if ($scope.dnsa && $scope.dnsa.check) {
                        $scope.dnsa.checkDNS = true;
                    }
                    if ($scope.dnsb && $scope.dnsb.check) {
                        $scope.dnsb.checkDNS = true;
                    }
                    if ($scope.dnsc && $scope.dnsc.check) {
                        $scope.dnsc.checkDNS = true;
                    }
                    $scope.dnsErrMsg = 'DNS配置不能重复';
                }
                else if (dataa && datab && datac) {
                    if (dataa === datab) {
                        $scope.dnsShowError = true;
                        $scope.dnsError = true;
                        $scope.dnsa.checkDNS = true;
                        $scope.dnsb.checkDNS = true;
                        $scope.dnsErrMsg = 'DNS配置不能重复';
                    }
                    if (dataa === datac) {
                        $scope.dnsShowError = true;
                        $scope.dnsError = true;
                        $scope.dnsa.checkDNS = true;
                        $scope.dnsc.checkDNS = true;
                        $scope.dnsErrMsg = 'DNS配置不能重复';
                    }
                    if (datab === datac) {
                        $scope.dnsb.checkDNS = true;
                        $scope.dnsc.checkDNS = true;
                        $scope.dnsShowError = true;
                        $scope.dnsError = true;
                        $scope.dnsErrMsg = 'DNS配置不能重复';
                    }
                }
            }
        };

        if (type == 'edit') {
            $scope.dnsa = {};
            $scope.dnsb = {};
            $scope.dnsc = {};
        }

        /**
         * 转化DNS为字符串结构
         */
        $scope.formatData = function () {
            $scope.subNetWork.dns = null;
            if ($scope.data && $scope.data.length > 0) {
                var str = '';
                angular.forEach($scope.data, function (value, key) {
                    str = str + value + ";";
                });
                if (str) {
                    $scope.subNetWork.dns = str.substr(0, str.length - 1);
                }
            }
        };

        $scope.commit = function () {
            if (type == "edit") {
                $scope.checkAllDNS();
                $scope.checkExsitDNS();
                if ($scope.dnsError) {
                    return;
                }
                $scope.formatData();
            }
            $scope.btnDis = true;
            $scope.visible = true;
            if (type == "edit") {
                eayunHttp.post("cloud/subnetwork/updateSubNetWork.do", $scope.subNetWork).then(function (response) {
                    if (!(angular.isDefined(response.data.code) && response.data.code != "010120")) {
                        $scope.subNetWork.type = type;
                        $modalInstance.close($scope.subNetWork);
                        toast.success("修改子网" + $scope.shortName(response.data.subnetName) + "成功");
                    } else {
//							$scope.cidrError=true;
                    }
                    $scope.btnDis = false;
                    $scope.visible = false;
                });
            } else {
                eayunHttp.post("cloud/subnetwork/checkCidr.do", {
                    "cidr": $scope.subNetWork.cidr,
                    "netId": netWork.netId
                }).then(function (cidrIsHave) {
                    if (!cidrIsHave.data) {
                        $scope.btnDis = false;
                        $scope.visible = false;
                        $scope.cidrError = true;
                        $scope.cidrShowError = true;
                        $scope.range = "网络地址：" + $scope.subNetWork.cidr + "已存在！";
                    } else {
                        $scope.cidrError = false;
                        $scope.cidrShowError = false;
                        $scope.subNetWork.dns = $scope.dns;
                        eayunHttp.post("cloud/subnetwork/addSubNetWork.do", $scope.subNetWork).then(function (respose) {
                            if (respose.data.code != "010120") {
                                $scope.btnDis = false;
                                $scope.visible = false;
                                $scope.subNetWork.type = type;
                                $modalInstance.close($scope.subNetWork);
                                toast.success("添加子网" + $scope.shortName(respose.data.subnetName) + "成功");
                            } else {
//							$scope.cidrError=true;
                            }
                            $scope.btnDis = false;
                            $scope.visible = false;
                        });
                    }
                });
            }

        };
        /*取消增加或者编辑子网*/
        $scope.cancel = function () {
            if (!$scope.btnDis) {
                $modalInstance.dismiss();
            }
        };
    })
    /**
     * 删除子网的页面
     */
    .controller('DeleteSubnetCtrl', ['item', '$modalInstance', 'VpcService', 'eayunHttp', 'eayunModal', 'toast',
        function (item, $modalInstance, VpcService, eayunHttp, eayunModal, toast) {
            var vm = this;
            vm.checkBtn = false;
            vm.subnet = angular.copy(item, {});
            vm.commit = function () {
                vm.visible = true;
                vm.checkBtn = true;
                VpcService.checkForDel(vm.subnet).then(function () {
                    eayunHttp.post('cloud/subnetwork/deleteCloudSubNet.do', vm.subnet).then(function (response) {
                        if (response.data) {
                            if (response.data.code != '010120') {
                                $modalInstance.close();
                                toast.success('删除子网成功！');
                            } else {
                                /*这里还有一种情况：底层删除子网报异常信息，直接弹窗提示转义后的错误，一并做关窗处理*/
                                $modalInstance.dismiss();
                            }
                        } else {
                            $modalInstance.dismiss();
                            eayunModal.warning('子网已和负载均衡关联，无法删除');
                        }
                    });
                }, function (message) {
                    $modalInstance.dismiss();
                    eayunModal.warning(message);
                });
            };
            vm.cancel = function () {
                if (!vm.checkBtn) {
                    $modalInstance.dismiss();
                }
            };
    }])
    /**
     * 设置网关
     */
    .controller('routeSetGatewayCtrl', function ($scope, eayunModal, eayunHttp, outNetWorkList, item) {
        // 将项目绑定addProject页面下拉框
        $scope.outNetWorkList = outNetWorkList;
        $scope.model = {};
        // angular复制对象赋给新建页面的model
        $scope.model = angular.copy(item);
        // 直接将创建页面所有的项目放入当前的$scope.model中 开始
        $scope.model.outNet = $scope.outNetWorkList[0];
        if ($scope.outNetWorkList && $scope.outNetWorkList.length == 1) {
            $scope.model.outNetId = $scope.outNetWorkList[0].value;
        }
        // 直接将创建页面所有的项目放入当前的$scope.model中 结束
        $scope.commit = function () {
            $scope.ok($scope.model);
        };

    });
