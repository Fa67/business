/**
 * Created by eayun on 2016/8/9.
 */
'use strict'

angular.module('eayunApp.controllers')
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider.state('app.net.netbar.vpn', {
                url: '/vpn',
                templateUrl: 'views/net/vpn/list.html',
                controller: 'VpnListCtrl',
                controllerAs: 'vpn'
            })
            .state('app.net.detailvpn', {
                url: '/detailvpn/:vpnId',
                templateUrl: 'views/net/vpn/detail.html',
                controller: 'DetailVpnCtrl',
                controllerAs: 'detail'
            })
            .state('buy.vpnbuy', {
                url: '/vpnbuy/:orderNo',
                templateUrl: 'views/net/vpn/buyvpn.html',
                controller: 'BuyVpnCtrl',
                controllerAs: 'buyVpn'
            })
            .state('buy.vpnbuyconfirm', {
                url: '/vpnbuyconfirm',
                templateUrl: 'views/net/vpn/confirm.html',
                controller: 'BuyConfirmVpnCtrl',
                controllerAs: 'confirmBuy'
            })
            .state('buy.vpnRenew', {
                url: '/vpnRenew',
                templateUrl: 'views/net/vpn/vpnrenewconfirm.html',
                controller: 'VpnRenewConfirmController'
            });
    }])
    .controller('VpnListCtrl', ['eayunModal', 'eayunHttp', 'VpnService', 'toast', '$state', 'powerService', '$scope', '$timeout', '$rootScope',
        function (eayunModal, eayunHttp, VpnService, toast, $state, powerService, $scope, $timeout, $rootScope) {
            var list = [];
            $rootScope.navList(list, 'VPN');

            var vm = this;

            powerService.powerRoutesList().then(function (powerList) {
                $scope.buttonPower = {
                    isRenew: powerService.isPower('vpn_renew'),		//续费
                    isAddVpn: powerService.isPower('vpn_add'),      //创建
                    isEditVpn: powerService.isPower('vpn_edit'),    //编辑
                    isDelVpn: powerService.isPower('vpn_drop')      //删除
                };
            });

            var dcId = JSON.parse(sessionStorage['dcPrj']).dcId,
                prjId = JSON.parse(sessionStorage["dcPrj"]).projectId;

            vm.vpnStatusClass = [];

            vm.search = function () {
                vm.table.api.draw();
            };

            vm.table = {
                api: {},
                source: 'cloud/vpn/getvpnlist.do',
                getParams: function () {
                    return {
                        vpnName: vm.vpnName || '',
                        prjId: prjId
                    };
                }
            };
            /*切换数据中心*/
            $scope.$watch('model.projectvoe', function (newValue, oldValue) {
                if (oldValue !== newValue) {
                    prjId = newValue.projectId;
                    vm.table.api.draw();
                }
            });
            /*获取vpn状态颜色框的颜色类*/
            vm.getVpnStatus = function (_vpn) {
                if (_vpn.vpnStatus == 'ACTIVE' && _vpn.chargeState == '0') {
                    return 'green';
                } else if (_vpn.vpnStatus == 'DOWN' || _vpn.chargeState != '0') {
                    return 'gray';
                } else if (_vpn.vpnStatus == 'PENDING_CREATE' && _vpn.chargeState == '0') {
                    return 'yellow';
                }
            };
            /*跳转详情页*/
            vm.getVpnInfo = function (vpnId) {
                $state.go('app.net.detailvpn', {vpnId: vpnId});
            };
            /*刷新表格*/
            var refreshTable = function () {
                vm.table.api.refresh();
            };
            /*刷新状态的方法*/
            vm.refresh = function () {
                var keepgoing = true;
                angular.forEach(vm.table.result, function (_vpnModel) {
                    var status = _vpnModel.vpnStatus.toString().toLowerCase();
                    if ('active' != status && 'down' != status && keepgoing) {
                        refreshTable();
                        keepgoing = false;
                    }
                });
            };
            /*跳转vpn购买页*/
            vm.buyVpn = function () {
                $state.go('buy.vpnbuy');
            };

            vm.addVpn = function () {
                eayunModal.dialog({
                    title: '创建VPN',
                    templateUrl: 'views/net/vpn/add.html',
                    controller: 'AddVpnCtrl',
                    controllerAs: 'addVpn',
                    showBtn: false,
                    width: '700px',
                    resolve: {
                        networkList: function () {
                            return VpnService.getNetworkListByPrjId(prjId).then(function (response) {
                                return response;
                            });
                        }
                    }
                }).then(function (vpnModel) {
                    /*eayunHttp.post('cloud/vpn/createvpn.do', vpnModel).then(function (response) {
                     toast.success('创建VPN服务成功！');
                     vm.table.api.draw();
                     });*/
                });
            };
            /*编辑vpn*/
            vm.editVpn = function (vpn) {
                var result = eayunModal.open({
                    title: '修改VPN',
                    backdrop: 'static',
                    templateUrl: 'views/net/vpn/edit.html',
                    controller: 'EditVpnCtrl',
                    controllerAs: 'editVpn',
                    resolve: {
                        vpnModel: function () {
                            return vpn;
                        }
                    }
                });
                result.result.then(function (vpnModel) {
                    VpnService.updateVpn(vpnModel).then(function () {
                        toast.success('编辑VPN服务成功！');
                        vm.table.api.draw();
                    }, function () {
                        vm.table.api.draw();
                    });
                });
            };
            /*删除vpn*/
            vm.deleteVpn = function (vpn) {
                eayunModal.confirm('确定删除VPN服务？').then(function () {
                    vpn.dcId = dcId;
                    VpnService.deleteVpn(vpn).then(function () {
                        toast.success('删除VPN服务成功！');
                        vm.table.api.draw();
                    }, function () {
                        vm.table.api.draw();
                    });
                });
            };
            /*续费vpn*/
            vm.renewVpn = function (vpn) {
                var result = eayunModal.open({
                    title: 'VPN续费',
                    backdrop: 'static',
                    templateUrl: 'views/net/vpn/vpnrenew.html',
                    controller: 'vpnRenewCtrl',
                    resolve: {
                        item: function () {
                            return vpn;
                        }
                    }
                });
                result.result.then(function (value) {
                    VpnService.checkIfOrderExist(vpn).then(function (response) {
                        eayunModal.warning("资源正在调整中或您有未完成的订单，请稍后再试。");
                    }, function () {
                        $state.go('buy.vpnRenew');
                    });
                }, function () {

                });
            };
            /**
             * 查询当前sessionStore 是否存在用户信息
             */
            $scope.checkUser = function () {
                var user = sessionStorage["userInfo"]
                if (user) {
                    user = JSON.parse(user);
                    if (user && user.userId) {
                        return true;
                    }
                }
                return false;
            };
            //页面中回车键触发查询事件；
            //$(function () {
            //    document.onkeydown = function (event) {
            //        var e = event || window.event || arguments.callee.caller.arguments[0];
            //        if (!$scope.checkUser()) {
            //            return;
            //        }
            //        if (e && e.keyCode == 13) {
            //            vm.table.api.draw();
            //        }
            //    };
            //});
        }])
    /**
     * VPN续费controller
     */
    .controller('vpnRenewCtrl', function ($scope, eayunModal, eayunHttp, item, eayunStorage, $modalInstance) {
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
                    vpnCount: 1
                };
                //将参数注入到eayunStorage用于传递给订单确认页面
                //eayunStorage.set('vpn_dcName', item.dcName);
                eayunStorage.set('vpn_id', item.vpnId);
                eayunStorage.set('vpn_name', item.vpnName);
                eayunStorage.set('vpn_network', item.networkName + '(' + item.gatewayIp + ')');//本端网络
                eayunStorage.set('vpn_subnet', item.subnetName + '(' + item.subnetCidr + ')');//本端子网
                eayunStorage.set('vpn_peerAddress', item.peerAddress);//对端网关
                eayunStorage.set('vpn_peerCidrs', item.peerCidrs);//对端网段
                eayunStorage.set('vpn_cycle', cycleCount);
                eayunStorage.set('vpn_paramBean', paramBean);
                eayunStorage.set('vpn_endTime', item.endTime);

                eayunHttp.post('billing/factor/getPriceDetails.do', paramBean).then(function (response) {
                    $scope.responseCode = response.data.respCode;
                    if ($scope.responseCode == '010120') {
                        $scope.respMsg = response.data.message;
                    } else {
                        $scope.model.chargeMoney =  response.data.data.totalPrice;
                        eayunStorage.set('vpn_chargeMoney', $scope.model.chargeMoney);
                    }
                });
                //计算续费后的到期时间
                eayunHttp.post('order/computeRenewEndTime.do', {
                    'original': $scope.model.endTime,
                    'duration': renewTime
                }).then(function (response) {
                    $scope.model.lastTime = response.data;
                    eayunStorage.set('vpn_expireTime', $scope.model.lastTime);
                });
            }
        };
    })
    /**
     * 负载均衡续费订单确认页controller
     */
    .controller('VpnRenewConfirmController', ['$scope', '$state', 'eayunModal', 'eayunHttp', 'eayunStorage', 'VpnService','eayunMath',
        function ($scope, $state, eayunModal, eayunHttp, eayunStorage, VpnService, eayunMath) {
            //读取页面共享数据，用于页面展示
            var chargeMoney = eayunStorage.get('vpn_chargeMoney');
            if (chargeMoney == null) {
                $state.go('app.net.netbar.vpn');//如果刷新页面，则eayunStorage清空，则需要跳转到列表页
            }
            var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
            $scope.model = {
                dcName: dcPrj.dcName,//eayunStorage.get('vpn_dcName'),
                vpnId: eayunStorage.get('vpn_id'),
                vpnName: eayunStorage.get('vpn_name'),
                vpnNetwork: eayunStorage.get('vpn_network'),
                vpnSubnet: eayunStorage.get('vpn_subnet'),
                vpnPeerAddress: eayunStorage.get('vpn_peerAddress'),
                vpnPeerCidrs: eayunStorage.get('vpn_peerCidrs'),
                cycle: eayunStorage.get('vpn_cycle'),
                chargeMoney: chargeMoney,
                paramBean: eayunStorage.get('vpn_paramBean'),
                lastTime: eayunStorage.get('vpn_expireTime'),
                endTime: eayunStorage.get('vpn_endTime')
            };

            eayunHttp.post('costcenter/accountoverview/getaccountbalance.do', {}).then(function (response) {
                var balance = response.data.data.money;
                $scope.model.balance = balance;
                doCalc();
            });

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

            //refreshMoney();

            //目前这个做法保证了在进入订单确认页时，用户看到的是最新的账户余额和产品金额
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
                        $scope.model.payable =  eayunMath.sub($scope.model.chargeMoney, $scope.model.deduction);;
                    } else {
                        $scope.model.deduction = formatFloat(0, 2);
                        $scope.model.payable = $scope.model.chargeMoney;
                    }
                };
            };

            //提交订单
            $scope.canSubmit = true;
            $scope.warningMessage = '';
            $scope.submitOrder = function () {
                VpnService.checkIfOrderExist($scope.model).then(function (response) {
                    $scope.warningMessage = '资源正在调整中或您有未完成的订单，请稍后再试。';
                    $scope.canSubmit = false;
                    //eayunModal.info("资源正在调整中或您有未完成的订单，请稍后再试。");
                    //$state.go('app.net.netbar.vpn');
                }, function () {
                    //当前无未完成订单，可以提交订单
                    //获取当前余额，判断此时账户余额是否大于余额支付金额，以确定能否提交订单
                    //直接调用续费接口，在接口中进行一系列校验，如果校验不通过，页面进行响应的展现，如果校验通过，则完成订单。
                    eayunHttp.post('cloud/vpn/renewvpn.do', $scope.model, {$showLoading: true}).then(function (response) {
                        var respCode = response.data.respCode;
                        var respMsg = response.data.message;
                        var respOrderNo = response.data.orderNo;
                        if (respCode == '010110') {
                            $scope.warningMessage = respMsg;
                            $scope.model.isSelected = false;
                            refreshMoney();

                        } else if (respCode == '000000') {
                            if (respMsg == 'BALANCE_PAY_ALL') {
                                //直接跳转到订单完成界面
                                $state.go('pay.result', {subject: 'VPN-续费'});
                            } else if (respMsg == 'RENEW_SUCCESS' || respMsg == 'BALANCE_PAY_PART') {
                                //跳转到第三方支付页面
                                var orderPayNavList = [{route: 'app.net.netbar.vpn', name: 'VPN'}];
                                eayunStorage.persist("orderPayNavList", orderPayNavList);
                                eayunStorage.persist("payOrdersNo", respOrderNo);
                                $state.go('pay.order');
                            }
                        }
                    });

                });
            }

        }])
    .controller('DetailVpnCtrl', ['$stateParams', '$timeout', '$scope', 'VpnService', '$rootScope', 'DatacenterService',
        function ($stateParams, $timeout, $scope, VpnService, $rootScope, DatacenterService) {
            var list = [{'router': 'app.net.netbar.vpn', 'name': 'VPN'}];
            $rootScope.navList(list, 'VPN详情', 'detail');

            var vm = this;

            var initial = function () {
                initData();
                getDetailInfo();
                refreshDetailInfo();
            };

            var initData = function () {
                vm.vpnId = $stateParams.vpnId;
                vm.vpnStatusClass = '';
            };

            var getDetailInfo = function () {
                VpnService.getVpnInfo(vm.vpnId).then(function (response) {
                    vm.vpnModel = response;
                    vm.vpnModel.routerId = DatacenterService.toastEllipsis(vm.vpnModel.peerId, 20);
                    getVpnStatus(vm.vpnModel);
                    getInitiator(vm.vpnModel.initiator);
                    getDpdActionStr(vm.vpnModel.dpdAction);
                    getIpSecEncapsulation(vm.vpnModel.ipSecEncapsulation);
                });
            };
            /*获取vpn状态颜色框的颜色类*/
            var getVpnStatus = function (_vpn) {
                if (_vpn.vpnStatus == 'ACTIVE' && _vpn.chargeState == '0') {
                    vm.vpnStatusClass = 'green';
                } else if (_vpn.vpnStatus == 'DOWN' || _vpn.chargeState != '0') {
                    vm.vpnStatusClass = 'gray';
                } else if (_vpn.vpnStatus == 'PENDING_CREATE' && _vpn.chargeState == '0') {
                    vm.vpnStatusClass = 'yellow';
                }
            };
            /*发起状态转义*/
            var getInitiator = function (_initiator) {
                switch (_initiator) {
                    case 'response-only':
                        vm.initiatorCn = '只应答';
                        break;
                    case 'bi-directional':
                        vm.initiatorCn = '双向';
                        break;
                }
            };
            /*失效处理状态转义*/
            var getDpdActionStr = function (_dpdAction) {
                switch (_dpdAction) {
                    case 'hold':
                        vm.dpdAction = '保留';
                        break;
                    case 'clear':
                        vm.dpdAction = '清除';
                        break;
                    case 'disabled':
                        vm.dpdAction = '禁用';
                        break;
                    case 'restart':
                        vm.dpdAction = '重启';
                        break;
                    case 'restart-by-peer':
                        vm.dpdAction = '被对端重启'
                    default :
                        break;
                }
            };
            var getIpSecEncapsulation = function (_ipSecEncapsulation) {
                switch (_ipSecEncapsulation) {
                    case 'tunnel':
                        vm.ipSecEncapsulation = '隧道模式';
                        break;
                    default :
                        break;
                }
            };
            var refreshDetailInfo = function () {
                var timer;
                var refreshTable = function () {
                    $timeout.cancel(timer);
                    timer = $timeout(function () {
                        var keepgoing = true;
                        var status = vm.vpnModel.vpnStatus.toString().toLowerCase();
                        if ('active' != status && 'error' != status && '0' == vm.vpnModel.chargeState && keepgoing) {
                            getDetailInfo();
                            keepgoing = false;
                        }
                    }, 5000);
                    timer.then(function () {
                        refreshTable();
                    });
                };
                refreshTable();
                $scope.$on('$destory', function () {
                    $timeout.cancel(timer);
                });
            };

            initial();
            //pop框方法
            $scope.openPopBox = function (_pskKey) {
                $scope.pskKeyShow = true;
                $scope.pskKey = _pskKey;
            };
            $scope.closePopBox = function (type) {
                $scope.pskKeyShow = false;
            };
        }])
    .controller('AddVpnCtrl', ['$scope', 'eayunHttp', 'networkList', 'VpnService', '$state', 'eayunStorage',
        function ($scope, eayunHttp, networkList, VpnService, $state, eayunStorage) {
            var vm = this;

            vm.step = 1;
            vm.networkList = networkList;
            vm.subnetList = [];

            vm.vpnModel = {
                dcId: JSON.parse(sessionStorage['dcPrj']).dcId,
                dcName: JSON.parse(sessionStorage['dcPrj']).dcName,
                prjId: JSON.parse(sessionStorage['dcPrj']).projectId,
                ikeEncryption: '3des',
                ikeVersion: 'v1',
                ikeAuth: 'sha1',
                ikeNegotiation: 'main',
                ikeLifetime: 7200,
                ikeDh: 'group5',

                ipsecEncryption: '3des',
                ipsecProtocol: 'esp',
                ipsecAuth: 'sha1',
                ipsecEncapsulation: 'tunnel',
                ipsecLifetime: 7200,
                ipsecDh: 'group5',

                mtu: 1500,
                dpdAction: 'hold',
                dpdInterval: 60,
                dpdTimeout: 240,
                initiator: 'bi-directional'
            };

            var initial = function () {

                initBuyCycleOption();
            };

            var initBuyCycleOption = function () {
                vm.cycleTypeList = [
                    {
                        text: '实时计费',
                        value: 'time'
                    },
                    {
                        text: '年付',
                        value: 'year'
                    },
                    {
                        text: '月付',
                        value: 'month'
                    }
                ];
                vm.cycleType = 'time';
                vm.changeCycleType();
            };
            /*改变付款方式的年月选项*/
            vm.changeCycleType = function () {
                vm.cycleList = [];
                vm.vpnModel.payType = 1;
                if (vm.cycleType == 'month') {
                    vm.cycleList = [
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
                } else if (vm.cycleType == 'year') {
                    vm.cycleList = [
                        {
                            text: '1年',
                            value: '12'
                        },
                        {
                            text: '2年',
                            value: '24'
                        },
                        {
                            text: '3年',
                            value: '36'
                        }
                    ];
                } else if (vm.cycType = 'time') {
                    vm.cycleList = [];
                    vm.vpnModel.payType = 2;
                }
                vm.vpnModel.buyCycle = vm.cycleList.length > 0 ? vm.cycleList[0].value : 1;
                vm.getPrice();
            };

            vm.getPrice = function () {
                VpnService.getPrice(vm.vpnModel).then(function (price) {
                    vm.price = price;
                    vm.vpnModel.price = vm.price;
                });
            };

            vm.changeNetwork = function (network) {
                vm.vpnModel.networkId = network.netId;
                vm.vpnModel.networkName = network.netName;
                vm.vpnModel.routeId = network.routeId;
                vm.vpnModel.gatewayIp = network.gatewayIp;
                VpnService.getSubnetListByNetId(network.netId, '1').then(function (response) {
                    vm.subnetList = response;
                });
            };

            vm.changeSubnet = function (subnet) {
                vm.vpnModel.subnetId = subnet.subnetId;
                vm.vpnModel.subnetName = subnet.subnetName;
                vm.vpnModel.subnetCidr = subnet.cidr;
            };

            vm.before = function () {
                vm.step--;
            };

            vm.after = function () {
                vm.step++;
            };

            $scope.commit = function () {
                eayunStorage.set('vpnTemp', vm.vpnModel);
                $state.go('buy.vpnbuyconfirm');
                $scope.ok();
            };

            /*提交订单*/
            vm.buyAtOnce = function () {
                /*valid 1*/
                PoolService.queryAccount().then(function (response) {
                    vm.isNSF = true;
                    if (!vm.isNSF) {
                        /*valid 2*/
                        PoolService.getVpnQuotasByPrjId(vm.vpnModel.prjId).then(function (response) {
                            vm.quotas = response;
                            vm.outOfQuota = vm.quotas <= 0;
                            if (!vm.outOfQuota) {
                                /*valid 3*/
                                PoolService.getPrice(vm.pool).then(function (response) {
                                    if (vm.totalPrice != response) {
                                        vm.changeOfBilling = true;
                                    } else {
                                        eayunStorage.set('pool', vm.pool);
                                        $state.go('buy.vpnbuyconfirm');
                                    }
                                });
                            }
                        });
                    }
                });
            };

            initial();
        }])
    .controller('BuyVpnCtrl', ['$state', 'DatacenterService', 'VpnService', 'eayunStorage', 'eayunHttp', 'BuyCycle', '$stateParams',
        function ($state, DatacenterService, VpnService, eayunStorage, eayunHttp, BuyCycle, $stateParams) {
            var vm = this;
            /*初始化购买页面数据*/
            var initial = function () {
                vm.dcInited = false;
                if ($stateParams.orderNo) {
                    vm.buyCycleTemp = 1;
                    vm.networkList = [];
                    vm.subnetList = [];
                    vm.checkBuyBtn = false;
                    vm.hasGotPrice = true;
                    vm.ikeSixty = false;
                    vm.ipsecSixty = false;
                    vm.mtuSixtyEight = false;
                    vm.checkTimeout = true;
                    //对端网管的四个输入框的状态和值
                    vm.$$addr1 = {};
                    vm.$$addr2 = {};
                    vm.$$addr3 = {};
                    vm.$$addr4 = {};
                    vm.$$addr1.flag = true;
                    vm.$$addr2.flag = true;
                    vm.$$addr3.flag = true;
                    vm.$$addr4.flag = true;
                    vm.$$addrTotal = false;
                    DatacenterService.getBuyCondition().then(function (condition) {
                        vm.payAfterCondition = condition;
                    });
                    VpnService.getOrderVpnByOrderNo($stateParams.orderNo).then(function (vpn) {
                        if (vpn.payType == '1') {
                            vpn.cycleType = vpn.buyCycle < 12 ? 'month' : 'year';
                        }
                        /*后台获取的cloudordervpn的数据包当中，去掉主键id，以防止save入库抹掉已有数据*/
                        delete vpn.ordervpnId;
                        init.initDataOfOrderBack(vpn);
                        init.initDatacenter();
                        init.initBuyCycleOption();
                    });
                } else {
                    init.initData();
                    init.initDatacenter();
                    init.initBuyCycleOption();
                }
            };

            var init = {
                /*初始化数据*/
                initData: function () {
                    var that = this;
                    vm.fromOrder = '';
                    vm.buyCycleTemp = 1;
                    vm.networkList = [];
                    vm.subnetList = [];
                    vm.checkBuyBtn = false;
                    vm.hasGotPrice = true;
                    vm.ikeSixty = true;
                    vm.ipsecSixty = true;
                    vm.mtuSixtyEight = true;
                    vm.checkTimeout = true;
                    //对端网管的四个输入框的状态和值
                    vm.$$addr1 = {};
                    vm.$$addr2 = {};
                    vm.$$addr3 = {};
                    vm.$$addr4 = {};
                    vm.$$addr1.flag = true;
                    vm.$$addr2.flag = true;
                    vm.$$addr3.flag = true;
                    vm.$$addr4.flag = true;
                    vm.$$addrTotal = false;
                    vm.vpnModel = {
                        dcId: JSON.parse(sessionStorage['dcPrj']).dcId,
                        dcName: JSON.parse(sessionStorage['dcPrj']).dcName,
                        prjId: JSON.parse(sessionStorage['dcPrj']).projectId,
                        payType: 1,
                        orderType: 0,
                        ikeEncryption: '3des',
                        ikeVersion: 'v1',
                        ikeAuth: 'sha1',
                        ikeNegotiation: 'main',
                        ikeLifetime: 86400,
                        ikeDh: 'group2',

                        ipsecEncryption: '3des',
                        ipsecProtocol: 'esp',
                        ipsecAuth: 'sha1',
                        ipsecEncapsulation: 'tunnel',
                        ipsecLifetime: 3600,
                        ipsecDh: 'group2',

                        mtu: 1500,
                        dpdAction: 'hold',
                        dpdInterval: 30,
                        dpdTimeout: 1500,
                        initiator: 'bi-directional',

                        accountPayment: 0,
                        thirdPartPayment: 0
                    };
                    DatacenterService.getBuyCondition().then(function (condition) {
                        vm.payAfterCondition = condition;
                    });
                    var temp = eayunStorage.get('buy_vpn_again');
                    eayunStorage.delete('buy_vpn_again');
                    if (angular.isDefined(temp)) {
                        that.initDataOfOrderBack(temp);
                    }
                },
                /*初始化返回配置的购买页面数据*/
                initDataOfOrderBack: function (_temp) {
                    vm.ikeSixty = true;
                    vm.ipsecSixty = true;
                    vm.mtuSixtyEight = true;
                    vm.vpnModel = angular.copy(_temp, {});
                    vm.fromOrder = 'backFromOrder';
                    vm.buyCycleTemp = vm.vpnModel.buyCycle;
                    VpnService.getNetworkListByPrjId(vm.vpnModel.prjId).then(function (response) {
                        vm.networkList = response;
                        if (vm.networkList) {
                            angular.forEach(vm.networkList, function (network) {
                                if (vm.vpnModel.networkId == network.netId) {
                                    vm.network = network;
                                    vm.vpnModel.networkName = network.netName;
                                    vm.vpnModel.routeId = network.routeId;
                                    vm.vpnModel.gatewayIp = network.gatewayIp;
                                }
                            });
                            if (vm.network) {
                                VpnService.getSubnetListByNetId(vm.network.netId, '1').then(function (response) {
                                    vm.subnetList = response;
                                    if (vm.subnetList) {
                                        angular.forEach(vm.subnetList, function (subnet) {
                                            if (vm.vpnModel.subnetId == subnet.subnetId) {
                                                vm.subnet = subnet;
                                                vm.viewInterAction.changeSubnet(subnet);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                    //对端网管的四个输入框的状态和值
                    vm.$$addr1 = {};
                    vm.$$addr2 = {};
                    vm.$$addr3 = {};
                    vm.$$addr4 = {};
                    vm.$$addr1.flag = true;
                    vm.$$addr2.flag = true;
                    vm.$$addr3.flag = true;
                    vm.$$addr4.flag = true;
                    vm.$$addrTotal = true;
                    vm.checkPeerCidrsFlag = true;
                    vm.$$addrs = vm.vpnModel.peerAddress.split(".");
                    vm.$$addr1.val = vm.$$addrs[0];
                    vm.$$addr2.val = vm.$$addrs[1];
                    vm.$$addr3.val = vm.$$addrs[2];
                    vm.$$addr4.val = vm.$$addrs[3];
                },
                /*初始化数据中心*/
                initDatacenter: function () {
                    var that = this;
                    DatacenterService.getDcPrjList().then(function (response) {
                        vm.datacenters = response;
                        var initialized = false;
                        if (vm.datacenters.length > 0) {
                            angular.forEach(vm.datacenters, function (value, key) {
                                if (vm.vpnModel.prjId == value.projectId) {
                                    initialized = true;
                                    vm.viewInterAction.selectDcPrj(value);
                                }
                            });
                            if (!initialized) {
                                vm.viewInterAction.selectDcPrj(vm.datacenters[0]);
                            }
                        }
                    });
                    that.initNetworkList();
                },
                /*初始化项目下的网络列表*/
                initNetworkList: function () {
                    VpnService.getNetworkListByPrjId(vm.vpnModel.prjId).then(function (response) {
                        vm.networkList = response;
                        if (vm.networkList.length == 1 && vm.fromOrder != 'backFromOrder') {
                            vm.network = vm.networkList[0];
                            vm.viewInterAction.changeNetwork(vm.network);
                        }
                    });
                },
                /*初始化购买周期的选项*/
                initBuyCycleOption: function () {
                    vm.cycleTypeList = BuyCycle.cycleTypeList;
                    if (vm.fromOrder != 'backFromOrder' || (vm.fromOrder == 'backFromOrder' && vm.vpnModel.payType == '2')) {
                        vm.vpnModel.cycleType = 'month';
                        vm.viewInterAction.changeCycleType();
                    } else {
                        vm.viewInterAction.changeCycleType(vm.fromOrder);
                    }
                }
            };
            /*view互动函数包*/
            vm.viewInterAction = {
                /*付费方式选择*/
                typeChoose: function (_payType) {
                    vm.vpnModel.payType = _payType;
                    if (_payType == '2') {
                        vm.buyCycleTemp = vm.vpnModel.buyCycle;
                        vm.vpnModel.buyCycle = 1;
                    } else {
                        vm.vpnModel.buyCycle = vm.buyCycleTemp;
                    }
                    api.getPrice(vm.vpnModel);
                },
                /*选择数据中心和项目*/
                selectDcPrj: function (_datacenter) {
                    var that = this;
                    vm.vpnModel.dcId = _datacenter.dcId;
                    vm.vpnModel.dcName = _datacenter.dcName;
                    vm.vpnModel.prjId = _datacenter.projectId;
                    api.getVpnQuotasByPrjId(vm.vpnModel.prjId);
                    that.checkVpnNameExist();
                    api.getPrice(vm.vpnModel);
                    if (vm.dcInited) {
                        api.cleanNetwork();
                    } else {
                        vm.dcInited = true;
                    }
                    init.initNetworkList();

                },
                /*校验vpn名称是否重复*/
                checkVpnNameExist: function () {
                    vm.checkBuyBtn = true;
                    VpnService.checkVpnNameExist(vm.vpnModel).then(function (response) {
                        vm.isNameExist = response;
                        vm.checkBuyBtn = false;
                    });
                },
                /*选择私有网络*/
                changeNetwork: function (_network) {
                    var that = this;
                    vm.vpnModel.networkId = _network.netId;
                    vm.vpnModel.networkName = _network.netName;
                    vm.vpnModel.routeId = _network.routeId;
                    vm.vpnModel.gatewayIp = _network.gatewayIp;
                    vm.subnet = {};
                    VpnService.getSubnetListByNetId(_network.netId, '1').then(function (response) {
                        vm.subnetList = response;
                        if (vm.subnetList.length == 1) {
                            vm.subnet = vm.subnetList[0];
                            that.changeSubnet(vm.subnetList[0]);
                        }
                    });
                },
                /*选择绑定路由的受管子网*/
                changeSubnet: function (_subnet) {
                    vm.vpnModel.subnetId = _subnet.subnetId;
                    vm.vpnModel.subnetName = _subnet.subnetName;
                    vm.vpnModel.subnetCidr = _subnet.cidr;
                },
                /*校验预共享密钥*/
                checkPskKey: function (_pskKey) {
                    if (_pskKey.indexOf('<') != -1
                        || _pskKey.indexOf('>') != -1
                        || _pskKey.indexOf('"') != -1
                        || _pskKey.indexOf('“') != -1
                        || _pskKey.indexOf('”') != -1
                        || _pskKey.indexOf('&') != -1) {
                        vm.pskKeyValid = true;
                    } else {
                        vm.pskKeyValid = false;
                    }
                },
                /*校验网关*/
                checkPeerAddress: function (key) {
                    switch (key) {
                        case '1' :
                            if (!api.checkNumber(vm.$$addr1.val))
                                vm.$$addr1.flag = false;
                            else
                                vm.$$addr1.flag = true;
                            break;
                        case '2' :
                            if (!api.checkNumber(vm.$$addr2.val))
                                vm.$$addr2.flag = false;
                            else
                                vm.$$addr2.flag = true;
                            break;
                        case '3' :
                            if (!api.checkNumber(vm.$$addr3.val))
                                vm.$$addr3.flag = false;
                            else
                                vm.$$addr3.flag = true;
                            break;
                        case '4' :
                            if (!api.checkNumber(vm.$$addr4.val))
                                vm.$$addr4.flag = false;
                            else
                                vm.$$addr4.flag = true;
                            break;
                    }
                    api.checkAllAddress();
                },
                /*对端子网校验*/
                checkPeerCidrs: function () {
                    if (!angular.isDefined(vm.vpnModel.peerCidrs)) {
                        return;
                    }
                    var inputFormat = /^(([01]?\d?\d|2[0-4]\d|25[0-5])\.){3}([01]?\d?\d|2[0-4]\d|25[0-5])\/(\d{1}|[0-2]{1}\d{1}|3[0-2])$/;
                    vm.checkPeerCidrsFlag = VpnService.checkPeerCidrs(vm.vpnModel, inputFormat);
                },
                /*校验ike生命周期*/
                checkIkeLifetime: function (_number) {
                    if (_number >= 60) {
                        vm.ikeSixty = true;
                    } else {
                        vm.ikeSixty = false;
                    }
                },
                /*校验ipsec生命周期*/
                checkIpsecLifetime: function (_number) {
                    if (_number >= 60) {
                        vm.ipsecSixty = true;
                    } else {
                        vm.ipsecSixty = false;
                    }
                },
                /*校验最大传输单元*/
                checkMtu: function () {
                    if (vm.vpnModel.mtu >= 68) {
                        vm.mtuSixtyEight = true;
                    } else {
                        vm.mtuSixtyEight = false;
                    }
                },
                /*校验时间间隔*/
                checkInterval: function () {
                    if (vm.vpnModel.dpdInterval == '' || vm.vpnModel.dpdTimeout == '')
                        return;
                    var regExp = new RegExp("^[1-9][0-9]{0,8}$");
                    if (!regExp.test(vm.vpnModel.dpdInterval) || !regExp.test(vm.vpnModel.dpdTimeout))
                        return;
                    if (parseInt(vm.vpnModel.dpdInterval) < parseInt(vm.vpnModel.dpdTimeout)) {
                        vm.checkTimeout = true;
                    } else {
                        vm.checkTimeout = false;
                    }
                },
                /*选择购买周期类型*/
                changeCycleType: function (_fromOrder) {
                    var flag = (_fromOrder == 'backFromOrder' && vm.vpnModel.payType == '1');
                    vm.cycleList = [];
                    angular.forEach(BuyCycle.cycleList, function (value, key) {
                        if (vm.vpnModel.cycleType == key) {
                            vm.cycleList = value;
                        }
                    });
                    if (vm.cycleList.length > 0 && !flag) {
                        vm.vpnModel.buyCycle = vm.cycleList[0].value;
                    }
                    api.getPrice(vm.vpnModel);
                },
                /*更改购买周期*/
                changeBuyCycle: function () {
                    api.getPrice(vm.vpnModel);
                },
                /*立即充值*/
                recharge: function () {
                    var routeUrl = "app.costcenter.guidebar.account";
                    var rechargeNavList = [{route: routeUrl, name: '账户总览'}];
                    eayunStorage.persist('rechargeNavList', rechargeNavList);
                    $state.go('pay.recharge');
                },
                /*立即购买*/
                buyAtOnce: function () {
                    /*valid 1*/
                    VpnService.queryAccount().then(function (money) {
                        vm.isNSF = money < vm.payAfterCondition;
                        if (!(vm.isNSF && vm.vpnModel.payType == '2')) {
                            /*valid 2*/
                            VpnService.getVpnQuotasByPrjId(vm.vpnModel.prjId).then(function (response) {
                                vm.quotas = response;
                                vm.outOfQuota = vm.quotas <= 0;
                                if (!vm.outOfQuota) {
                                    eayunStorage.set('vpnTemp', vm.vpnModel);
                                    $state.go('buy.vpnbuyconfirm');
                                }
                            });
                        }
                    });
                }
            };
            /*逻辑辅助函数包*/
            var api = {
                /*查询当前配额*/
                getVpnQuotasByPrjId: function (_prjId) {
                    vm.outOfQuota = false;
                    VpnService.getVpnQuotasByPrjId(_prjId).then(function (response) {
                        vm.quotas = response;
                        vm.outOfQuota = vm.quotas <= 0;
                    });
                },
                /*清空vpn网络信息*/
                cleanNetwork: function () {
                    vm.network = {};
                    vm.vpnModel.networkId = '';
                    vm.vpnModel.networkName = '';
                    vm.vpnModel.routeId = '';
                    vm.vpnModel.gatewayIp = '';
                    vm.subnet = {};
                    vm.vpnModel.subnetId = '';
                    vm.vpnModel.subnetName = '';
                    vm.vpnModel.subnetCidr = '';
                },
                /*校验网段输入的数字*/
                checkNumber: function (_number) {
                    var regExp = new RegExp("^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$");
                    return regExp.test(_number);
                },
                checkAllAddress: function () {
                    if ((vm.$$addr1.flag && vm.$$addr1.val != undefined)
                        && (vm.$$addr2.flag && vm.$$addr2.val != undefined)
                        && (vm.$$addr3.flag && vm.$$addr3.val != undefined)
                        && vm.$$addr4.flag && vm.$$addr4.val != undefined) {
                        vm.vpnModel.peerAddress = vm.$$addr1.val + '.' + vm.$$addr2.val + '.' +
                            vm.$$addr3.val + '.' + vm.$$addr4.val;
                        vm.$$addrTotal = true;
                    }
                    else {
                        vm.vpnModel.peerAddress = '';
                        vm.$$addrTotal = false;
                    }
                },
                /*获取价格*/
                getPrice: function (_orderModel) {
                    if (vm.vpnModel.buyCycle != 0) {
                        VpnService.getPrice(_orderModel).then(function (response) {
                            vm.hasGotPrice = true;
                            vm.totalPrice = response;
                            vm.vpnModel.price = response;
                        }, function (message) {
                            vm.hasGotPrice = false;
                            vm.priceMsg = message;
                        });
                    }
                }
            };

            initial();
        }])
    .controller('BuyConfirmVpnCtrl', ['eayunStorage', '$state', 'VpnService', 'DatacenterService', 'eayunMath',
        function (eayunStorage, $state, VpnService, DatacenterService, eayunMath) {
            var vm = this;
            /*初始化订单确认页面数据*/
            var initial = function () {
                vm.errMsg = '';
                vm.temp = eayunStorage.get('vpnTemp');
                if (angular.isDefined(vm.temp)) {
                    //console.log(vm.temp);
                }
                if (!angular.isDefined(vm.temp)) {
                    $state.go('buy.vpnbuy');
                } else {
                    initVerification();
                    //vm.temp.price = DatacenterService.moneyFloor(vm.temp.price);
                    vm.temp.accountPayment = 0;
                    vm.temp.thirdPartPayment = eayunMath.sub(Number(vm.temp.price), vm.temp.accountPayment);
                    /*vm.temp.thirdPartPayment = vm.temp.price - vm.temp.accountPayment;*/
                    VpnService.queryAccount().then(function (money) {
                        vm.accountQuota = money > 0 ? money : 0;
                    });
                    DatacenterService.getBuyCondition().then(function (condition) {
                        vm.payAfterCondition = condition;
                    });
                }
            };
            /*初始化校验数据*/
            var initVerification = function () {
                vm.commitOrderFlag = true;
                vm.errFlag = true;
            };
            /*选择是否使用余额支付*/
            vm.useVpnPay = function () {
                if (vm.useVpn) {
                    var array = [vm.accountQuota, vm.temp.price];
                    vm.temp.accountPayment = Math.min.apply(null, array);
                    vm.temp.thirdPartPayment = eayunMath.sub(Number(vm.temp.price), vm.temp.accountPayment);
                    /*vm.temp.thirdPartPayment = vm.temp.price - vm.temp.accountPayment;*/
                } else {
                    vm.temp.accountPayment = 0;
                    vm.temp.thirdPartPayment = vm.temp.price;
                }
            };
            /*提交订单*/
            vm.commitOrder = function () {
                vm.commitOrderFlag = false;
                VpnService.buyVpn(vm.temp).then(function (data) {
                    if ('1' == vm.temp.payType) {
                        if (0 == vm.temp.thirdPartPayment) {
                            $state.go('pay.result');
                        } else {
                            var orderPayNavList = '';
                            if (vm.temp.orderType == '0') {
                                orderPayNavList = [{route: 'app.net.netbar.vpn', name: 'VPN'}, {
                                    route: 'buy.vpnbuy',
                                    name: '创建VPN'
                                }];
                            } else if (vm.temp.orderType == '2') {
                                orderPayNavList = [{route: 'app.net.netbar.vpn', name: 'VPN'}];
                            }
                            var ordersIds = [data.orderNo];
                            eayunStorage.persist("orderPayNavList", orderPayNavList);
                            eayunStorage.persist("payOrdersNo", ordersIds);
                            $state.go('pay.order');
                        }
                    } else if ('2' == vm.temp.payType) {
                        $state.go('app.order.list');
                    }
                }, function (respMsg) {
                    vm.commitOrderFlag = true;
                    vm.errFlag = false;
                    if ('OUT_OF_QUOTA' == respMsg) {
                        vm.errMsg = '您的VPN数量配额不足，请提交工单申请配额';
                    } else if ('NOT_SUFFICIENT_FUNDS' == respMsg) {
                        vm.errMsg = '您的账户余额不足' + vm.payAfterCondition + '元，请充值后操作';
                    } else if ('CHANGE_OF_BILLINGFACTORY' == respMsg) {
                        VpnService.getPrice(vm.temp).then(function (response) {
                            vm.temp.price = response;
                            vm.useVpn = false;
                            vm.useVpnPay();
                            vm.errMsg = '您的订单金额发生变动，请重新确认订单';
                            vm.commitOrderFlag = true;
                        });
                    } else if ('CHANGE_OF_BALANCE' == respMsg) {
                        VpnService.queryAccount().then(function (money) {
                            vm.accountQuota = money;
                            vm.useVpn = false;
                            vm.useVpnPay();
                            vm.errMsg = '您的余额发生变动，请重新确认订单';
                            vm.commitOrderFlag = true;
                        });
                    } else if ('500' == respMsg && '2' == vm.temp.payType) {
                        $state.go('app.order.list');
                    }
                });
            };
            /*返回修改配置*/
            vm.returnToConfig = function () {
                eayunStorage.set('buy_vpn_again', vm.temp);
                $state.go('buy.vpnbuy');
            };

            initial();
        }])
    .controller('EditVpnCtrl', ['$scope', 'vpnModel', 'VpnService', '$modalInstance',
        function ($scope, vpnModel, VpnService, $modalInstance) {
            var vm = this;

            vm.vpnModel = angular.copy(vpnModel, {});
            vm.vpnModel.dcId = JSON.parse(sessionStorage['dcPrj']).dcId;
            //对端网管的四个输入框的状态和值
            vm.$$addr1 = {};
            vm.$$addr2 = {};
            vm.$$addr3 = {};
            vm.$$addr4 = {};
            vm.$$addr1.flag = true;
            vm.$$addr2.flag = true;
            vm.$$addr3.flag = true;
            vm.$$addr4.flag = true;
            vm.$$addrTotal = true;
            vm.checkTimeout = true;
            vm.checkPeerCidrsFlag = true;
            vm.checkVpnNameExist = false;
            vm.mtuSixtyEight = true;
            var addrs = vm.vpnModel.peerAddress.split(".");
            vm.$$addr1.val = addrs[0];
            vm.$$addr2.val = addrs[1];
            vm.$$addr3.val = addrs[2];
            vm.$$addr4.val = addrs[3];

            vm.cancel = function () {
                $modalInstance.dismiss();
            };

            vm.commit = function (_vpn) {
                $modalInstance.close(_vpn);
            };
            vm.checkPeerAddress = function (key) {
                switch (key) {
                    case '1' :
                        if (!vm.checkNumber(vm.$$addr1.val))
                            vm.$$addr1.flag = false;
                        else
                            vm.$$addr1.flag = true;
                        break;
                    case '2' :
                        if (!vm.checkNumber(vm.$$addr2.val))
                            vm.$$addr2.flag = false;
                        else
                            vm.$$addr2.flag = true;
                        break;
                    case '3' :
                        if (!vm.checkNumber(vm.$$addr3.val))
                            vm.$$addr3.flag = false;
                        else
                            vm.$$addr3.flag = true;
                        break;
                    case '4' :
                        if (!vm.checkNumber(vm.$$addr4.val))
                            vm.$$addr4.flag = false;
                        else
                            vm.$$addr4.flag = true;
                        break;
                }
                checkAllAddress();
            };
            vm.checkNumber = function (_number) {
                var regExp = new RegExp("^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$");
                return regExp.test(_number);
            };
            var checkAllAddress = function () {
                if ((vm.$$addr1.flag && vm.$$addr1.val != undefined)
                    && (vm.$$addr2.flag && vm.$$addr2.val != undefined)
                    && (vm.$$addr3.flag && vm.$$addr3.val != undefined)
                    && vm.$$addr4.flag && vm.$$addr4.val != undefined) {
                    vm.vpnModel.peerAddress = vm.$$addr1.val + '.' + vm.$$addr2.val + '.' +
                        vm.$$addr3.val + '.' + vm.$$addr4.val;
                    vm.$$addrTotal = true;
                }
                else {
                    vm.vpnModel.peerAddress = '';
                    vm.$$addrTotal = false;
                }
            };
            /*校验最大传输单元*/
            vm.checkMtu = function () {
                if (vm.vpnModel.mtu >= 68) {
                    vm.mtuSixtyEight = true;
                } else {
                    vm.mtuSixtyEight = false;
                }
            };
            /*对端子网校验*/
            vm.checkPeerCidrs = function () {
                if (vm.vpnModel.peerCidrs == '')
                    return;
                var inputFormat = /^(([01]?\d?\d|2[0-4]\d|25[0-5])\.){3}([01]?\d?\d|2[0-4]\d|25[0-5])\/(\d{1}|[0-2]{1}\d{1}|3[0-2])$/;
                vm.checkPeerCidrsFlag = VpnService.checkPeerCidrs(vm.vpnModel, inputFormat);
            };
            /*校验时间间隔*/
            vm.checkInterval = function () {
                if (vm.vpnModel.dpdInterval == '' || vm.vpnModel.dpdTimeout == '')
                    return;
                var regExp = new RegExp("^[1-9][0-9]{0,8}$");
                if (!regExp.test(vm.vpnModel.dpdInterval) || !regExp.test(vm.vpnModel.dpdTimeout))
                    return;
                if (parseInt(vm.vpnModel.dpdInterval) < parseInt(vm.vpnModel.dpdTimeout)) {
                    vm.checkTimeout = true;
                } else {
                    vm.checkTimeout = false;
                }
            };
            /*校验vpn名称是否重复*/
            vm.checkVpnNameExist = function () {
                VpnService.checkVpnNameExist(vm.vpnModel).then(function (response) {
                    vm.isNameExist = response;
                });
            };
        }]);