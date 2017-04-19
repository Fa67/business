'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
    .config(function ($stateProvider, $urlRouterProvider) {
        $stateProvider.state('app.net.netbar.floatip', {//弹性公网ip列表
                url: '/list',
                templateUrl: 'views/net/floatip/floatmng.html',
                controller: 'FloatCtrl',
                controllerAs: 'list'
            })
            .state("buy.createFloatIp", {//弹性公网ip申请
                url: '/createFloatIp:orderNo',
                templateUrl: 'views/net/floatip/addFloatIp.html',
                controller: 'BuyFloatIp',
                controllerAs: 'buyFloatIpCtrl'
            })
            .state("buy.createFloatIp.payPake", {//包年包月购买方式
                url: '/payPake',
                templateUrl: 'views/net/floatip/buyfloatip/paypake.html',
                controller: 'PayPakeCtrl',
                controllerAs: 'payPake'
            })
            .state("buy.createFloatIp.payRequired", {//按需付费购买方式
                url: '/payRequired',
                templateUrl: 'views/net/floatip/buyfloatip/payrequired.html',
                controller: 'PayRequiredCtrl',
                controllerAs: 'payRequired'
            })
            .state('buy.buyFloatIp', {
                url: '/buyfloatip/:orderNo',
                templateUrl: 'views/net/floatip/buyfloatip.html',
                controller: 'BuyFloatIpCtrl',
                controllerAs: 'buyFloatIp'
            })
            .state("buy.verifyFloatIp", {
                url: "/verifyFloatIp/:source",
                templateUrl: "views/net/floatip/verifyfloatip.html",
                controller: "VerifyFloatIpCtrl",
                controllerAs: "verifyFloatIp"
            })
            .state('renew.renewFloatIp', {
                url: '/renewFloatIp',
                templateUrl: 'views/net/floatip/floatiprenewconform.html',
                controller: 'RenewConformFloatIpController'

            });
    })
    /**
     * 列表
     */
    .controller('FloatCtrl', function ($rootScope, $scope, eayunModal, eayunHttp, toast, powerService, FloatIpService, $state) {
        var list = [];
        var prjId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '';
        $rootScope.navList(list, '弹性公网IP');

        //pop框方法
        $scope.hintTagShow = [];
        $scope.openTableBox = function (obj) {
            if (obj.type == 'tagName') {
                $scope.hintTagShow[obj.index] = true;
            }
            $scope.ellipsis = obj.value;
        };
        $scope.closeTableBox = function (obj) {
            if (obj.type == 'tagName') {
                $scope.hintTagShow[obj.index] = false;
            }
        };
        powerService.powerRoutesList().then(function (powerList) {
            $scope.buttonPower = {
                isBind: powerService.isPower('float_bind'),	//绑定/解除绑定
                isTag: powerService.isPower('float_tag'),		//标签功能
                isAdd: powerService.isPower('float_add'),		//申请
                isRelease: powerService.isPower('float_release'),	//释放
                isRenew: powerService.isPower('float_renew')	//续费
            };
        });
        /*表格获取数据*/
        $scope.IpTable = {
            source: 'cloud/floatip/getIpList.do',
            api: {},
            getParams: function () {
                return {
                    projectId: prjId
                };
            }
        };
        /*切换数据中心*/
        $scope.$watch('model.projectvoe', function (newValue, oldValue) {
            if (oldValue !== newValue) {
                prjId = newValue.projectId;
                $scope.IpTable.api.draw();
            }
        });

        $scope.floatJson = function (tagsStr) {
            var json = {};
            if (tagsStr) {
                json = JSON.parse(tagsStr);
            }
            return json;
        };
        $scope.$watch('model.dcProject.projectId', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.IpTable.api.draw();
            }
        });

        /**
         * 解绑资源
         */
        $scope.unbundingResource = function (item) {
            $scope.data = {
                'dcId': item.dcId,
                'prjId': item.prjId,
                'floId': item.floId,
                'floIp': item.floIp,
                'resourceId': item.resourceId,
                'resourceType': item.resourceType
            };
            eayunModal.confirm('确定解绑公网IP？').then(function () {
                eayunHttp.post('cloud/floatip/unbundingResource.do', $scope.data).then(function (response) {
                    if (response && response.data && response.data.respCode == '400000') {
                        toast.success("解绑公网IP成功");
                        $scope.IpTable.api.draw();
                    } else {
                        $scope.IpTable.api.draw();
                    }
                });
            });
        };

        /**
         *绑定资源
         */
        $scope.bindingResource = function (item) {
            var result = eayunModal.open({
                title: '绑定',
                backdrop: 'static',
                templateUrl: 'views/net/floatip/bindingresource.html',
                controller: 'BindResourceController',
                resolve: {
                    item: function () {
                        return item;
                    }
                }
            });
            result.result.then(function (data) {
                $scope.IpTable.api.draw();
            }, function () {
                $scope.IpTable.api.draw();
            });
        };
        /**
         * 公网ip--续费
         */
        $scope.renewFloatIp = function (item) {
            var result = eayunModal.open({
                backdrop: 'static',
                templateUrl: 'views/net/floatip/floatiprenew.html',
                controller: 'cloudFloatIpRenewCtrl',
                resolve: {
                    item: function () {
                        return item;
                    }
                }
            });
            result.result.then(function (value) {
                FloatIpService.checkIfOrderExist(item.floId).then(function (response) {
                    eayunModal.warning("资源正在调整中或您有未完成的订单，请稍后再试。");
                }, function () {
                    $state.go('renew.renewFloatIp');

                });

            }, function () {
//    		        console.info('取消');
            });
        };
        /**
         * 释放公网IP
         */
        $scope.releaseFloatIp = function (floatIp) {
            if(floatIp.resourceId=="null" ||floatIp.resourceId==null || floatIp.resourceId==""){
            	eayunHttp.post('cloud/floatip/checkflowebsite.do', {'floIp':floatIp.floIp}).then(function (response) {
                    if (response && response.data && response.data.respCode == '000000') {
                    	var waring = '确定释放公网IP：' + floatIp.floIp + '？';
                        if(response.data.data){
                        	waring = '此公网IP已绑定备案服务，确定释放公网IP：'+ floatIp.floIp + '？';
                        }
                        eayunModal.confirm(waring).then(function () {
                            eayunHttp.post('cloud/floatip/releaseFloatIp.do', floatIp).then(function (response) {
                                if (response && response.data && response.data.respCode == '100000') {
                                    toast.success("释放公网IP成功！");
                                    $scope.IpTable.api.draw();
                                } else {
                                    $scope.IpTable.api.draw();
                                }
                            });
                        });
                    }
                });
                
            }else{
                eayunModal.warning("公网IP已绑定了 "+floatIp.resourceName+"，请解绑后操作。");
            }
        };

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
                $scope.IpTable.api.draw();
            }, function () {
                $scope.IpTable.api.draw();
            });
        };
        $scope.checkFloatIpStatus = function (model) {
            //0：正常，1：余额不足，2：已到期
            if (model.chargeState == '0') {//正常
                //model.floStatus暂时不全
                if(model.resourceId != 'null' || model.floStatus=="0"){
                    return "ey-square-right";
                }else{
                    return "ey-square-space";
                }
            }
            else if (model.chargeState != '0') {//余额不足或者到期
                return 'ey-square-disable';
            }
            else {
                return 'ey-square-warning';
            }
        };
    })
    /**
     * 购买
     */
    .controller('BuyFloatIp', ['$scope', 'eayunHttp', '$stateParams', 'eayunStorage',
        function ($scope, eayunHttp, $stateParams, eayunStorage) {
            var vm = this;
            var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
            var orderNo = $stateParams.orderNo;
            if (orderNo != null && orderNo != "") {//重新下单的
                eayunHttp.post("cloud/floatip/getcloudorderbyorderno.do").then(function (response) {
                    response.data.cofId=null;
                    eayunStorage.set("floatOrderIp", response.data);
                    eayunStorage.set("isCre", "2");//重新下单

                });
            }
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
     * 包年包月
     */
    .controller('PayPakeCtrl', ['eayunModal', 'eayunHttp', 'toast', '$state', 'eayunStorage',
        function (eayunModal, eayunHttp, toast, $state, eayunStorage) {
            var vm = this;
            var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
            var userInfo = JSON.parse(sessionStorage["userInfo"]);
            vm.goToOrder = function () {
                eayunStorage.set("floatOrderIp", vm.floatOrder);
                $state.go("buy.verifyFloatIp", {source: 'create_before'});
            };
            vm.floatOrder = eayunStorage.get("floatOrderIp");
            vm.isCre = eayunStorage.get("isCre");

            vm.hasGotPrice = true;
            if (vm.isCre == null) {
                vm.floatOrder = {
                    dcId: dcPrj.dcId,
                    dcName: dcPrj.dcName,
                    prjId: dcPrj.projectId,
                    cusId: userInfo.cusId,
                    creUser: userInfo.userId,
                    payType: '1',
                    orderType: '0',
                    productCount: 1,
                    buyCycle: 1,
                    $$buyCycleYear: 1,
                    $$buyCycleMonth: 1
                };
            } else {
                eayunStorage.delete("isCre");
            }
            vm.selectDc = function (data) {
                vm.floatOrder.dcId = data.dcId;
                vm.floatOrder.dcName = data.dcName;
                vm.prjId = data.projectId;
                vm.getPrice();
            };
            vm.getPrice = function () {
                vm.list = [];
                if (vm.floatOrder.$$buyCycleYear == '1') {
                    vm.list = [
                        {
                            value: '1',
                            text: '1个月'
                        }, {
                            value: '2',
                            text: '2个月'
                        }, {
                            value: '3',
                            text: '3个月'
                        }, {
                            value: '4',
                            text: '4个月'
                        }, {
                            value: '5',
                            text: '5个月'
                        }, {
                            value: '6',
                            text: '6个月'
                        }, {
                            value: '7',
                            text: '7个月'
                        }, {
                            value: '8',
                            text: '8个月'
                        }, {
                            value: '9',
                            text: '9个月'
                        }, {
                            value: '10',
                            text: '10个月'
                        }, {
                            value: '11',
                            text: '11个月'
                        }
                    ];
                } else {
                    vm.list = [
                        {
                            value: '1',
                            text: '1年'
                        }, {
                            value: '2',
                            text: '2年'
                        }, {
                            value: '3',
                            text: '3年'
                        }
                    ];
                }
                vm.floatOrder.buyCycle = vm.floatOrder.$$buyCycleYear * vm.floatOrder.$$buyCycleMonth;
                if (vm.$$buyCycleYear == "1") {
                    vm.floatOrder.$$buyCycleName = vm.floatOrder.$$buyCycleMonth + "个月";
                } else {
                    vm.floatOrder.$$buyCycleName = vm.floatOrder.$$buyCycleMonth + "年";
                }
                eayunHttp.post("cloud/floatip/getprice.do", vm.floatOrder).then(function (response) {
                    if (response.data.respCode == '000000') {
                        vm.hasGotPrice = true;
                        vm.floatOrder.price = response.data.data;
                    } else if (response.data.respCode == '010120') {
                        vm.hasGotPrice = false;
                        vm.priceMsg = response.data.message;
                    }
                });
            };
            vm.getPrice();
        }])
    /**
     *按需付费
     */
    .controller('PayRequiredCtrl', ['eayunModal', 'eayunHttp', 'toast', '$state', 'eayunStorage',
        function (eayunModal, eayunHttp, toast, $state, eayunStorage) {
            var vm = this;

            var initial = function () {
                initData();
                vm.getPrice();
            };

            var initData = function () {
                var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
                var userInfo = JSON.parse(sessionStorage["userInfo"]);
                vm.isShow = false;
                vm.isCre = eayunStorage.get("isCre");
                vm.floatOrder = eayunStorage.get("floatOrderIp");
                vm.hasGotPrice = true;
                if (vm.isCre == null) {
                    vm.floatOrder = {
                        dcId: dcPrj.dcId,
                        dcName: dcPrj.dcName,
                        prjId: dcPrj.projectId,
                        cusId: userInfo.cusId,
                        creUser: userInfo.userId,
                        payType: "2",
                        orderType: '0',
                        productCount: 1,
                        buyCycle: 1,
                        $$buyCycleName: '小时'
                    };
                } else {
                    eayunStorage.delete("isCre");
                }
            };

            vm.selectDc = function (data) {
                vm.floatOrder.dcId = data.dcId;
                vm.floatOrder.dcName = data.dcName;
                vm.prjId = data.projectId;
                vm.getPrice();
            };
            //获取剩余额度
            vm.getAccountBalance = function () {
                eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (data) {
                    vm.floatOrder.$$balance = data.data.data.money;
                });
            };
            //  获取购买最低额度
            vm.getBuyCondition = function () {
                eayunHttp.post('sysdatatree/getbuycondition.do').then(function (data) {
                    vm.floatOrder.$$limit = data.data;
                });
            };
            vm.getPrice = function () {
                if (vm.floatOrder.productCount > 0) {
                    eayunHttp.post("cloud/floatip/getprice.do", vm.floatOrder).then(function (response) {
                        if (response.data.respCode == '000000') {
                            vm.hasGotPrice = true;
                            vm.floatOrder.price = response.data.data;
                        } else if (response.data.respCode == '010120') {
                            vm.hasGotPrice = false;
                            vm.priceMsg = response.data.message;
                        }
                    });
                } else {
                    vm.floatOrder.price = 0;
                }
            };

            /*立即购买*/
            vm.recharge = function () {
                var routeUrl = "app.costcenter.guidebar.account";
                var rechargeNavList = [{route: routeUrl, name: '账户总览'}];
                eayunStorage.persist('rechargeNavList', rechargeNavList);
                $state.go('pay.recharge');
            };
            /*跳转订单确认页面*/
            vm.goToOrder = function () {
                vm.getAccountBalance();
                vm.getBuyCondition();
                eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (response) {
                    vm.floatOrder.$$balance = response.data.data.money;
                    eayunHttp.post('sysdatatree/getbuycondition.do').then(function (data) {
                        vm.floatOrder.$$limit = data.data;
                        if (vm.floatOrder.$$balance < vm.floatOrder.$$limit) {
                            vm.isShow = true;
                        } else {
                            vm.isShow = false;
                            eayunStorage.set("floatOrderIp", vm.floatOrder);
                            $state.go("buy.verifyFloatIp", {source: 'create_after'});
                        }
                    });
                });
            };

            initial();
        }])
    .controller('BuyFloatIpCtrl', ['DatacenterService', 'FloatIpService', 'BuyCycle', 'eayunStorage', '$state', '$stateParams',
        function (DatacenterService, FloatIpService, BuyCycle, eayunStorage, $state, $stateParams) {
            var vm = this;
            /*加载初始化函数包*/
            var initial = {
                /*初始化页面数据*/
                initData: function () {
                    vm.fromOrder = '';
                    vm.buyCycleTemp = 1;
                    vm.hasGotPrice = true;
                    vm.isNSF = false;
                    api.getBuyCondition();
                    var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
                    var userInfo = JSON.parse(sessionStorage["userInfo"]);
                    vm.productCount = 1;
                    vm.floatIp = {
                        dcId: dcPrj.dcId,
                        dcName: dcPrj.dcName,
                        prjId: dcPrj.projectId,
                        cusId: userInfo.cusId,
                        creUser: userInfo.userId,
                        orderType: '0',
                        payType: '1',
                        productCount: 1,
                        buyCycle: 1
                    };
                    var temp = eayunStorage.get('buy_floatip_again');
                    eayunStorage.delete('buy_floatip_again');
                    if (angular.isDefined(temp)) {
                        api.getDataFromOrder(temp);
                    }
                    if ($stateParams.orderNo) {
                        initial.initDataFromOrder($stateParams.orderNo);
                    } else {
                        initial.initDatacenter();
                        initial.initBuyCycleOption();
                    }
                },
                /*初始化重新下单的数据*/
                initDataFromOrder: function (_orderNo) {
                    FloatIpService.getOrderFloatIpByOrderNo(_orderNo).then(function (floatIp) {
                        if (floatIp.payType == '1') {
                            floatIp.cycleType = floatIp.buyCycle < 12 ? 'month' : 'year';
                        }
                        delete floatIp.cofId;
                        api.getDataFromOrder(floatIp);
                        initial.initDatacenter();
                        initial.initBuyCycleOption();
                    });
                },
                /*初始化数据中心以及项目*/
                initDatacenter: function () {
                    DatacenterService.getDcPrjList().then(function (response) {
                        vm.datacenters = response;
                        var initialized = false;
                        if (vm.datacenters.length > 0) {
                            angular.forEach(vm.datacenters, function (value, key) {
                                if (vm.floatIp.prjId == value.projectId) {
                                    initialized = true;
                                    vm.viewInterAction.selectDcPrj(value);
                                }
                            });
                            if (!initialized) {
                                vm.viewInterAction.selectDcPrj(vm.datacenters[0]);
                            }
                        }
                    });
                },
                /*初始化购买周期的选项*/
                initBuyCycleOption: function () {
                    vm.cycleTypeList = BuyCycle.cycleTypeList;
                    if (vm.fromOrder != 'backFromOrder' || (vm.fromOrder == 'backFromOrder' && vm.floatIp.payType == '2')) {
                        vm.floatIp.cycleType = 'month';
                        vm.viewInterAction.changeCycleType();
                    } else {
                        vm.viewInterAction.changeCycleType(vm.fromOrder);
                    }
                }
            };
            /*view互动函数包*/
            vm.viewInterAction = {
                /*选择付款方式*/
                typeChoose: function (_payType) {
                    vm.floatIp.payType = _payType;
                    if (_payType == '2') {
                        vm.buyCycleTemp = vm.floatIp.buyCycle;
                        vm.floatIp.buyCycle = 1;
                    } else {
                        vm.floatIp.buyCycle = vm.buyCycleTemp;
                    }
                    api.getPrice(vm.floatIp);
                },
                /*选择数据中心和项目*/
                selectDcPrj: function (_datacenter) {
                    vm.floatIp.dcId = _datacenter.dcId;
                    vm.floatIp.dcName = _datacenter.dcName;
                    vm.floatIp.prjId = _datacenter.projectId;
                    api.getFloatIpQuotasByPrjId(vm.floatIp.prjId);

                    vm.floatIp.price = api.getPrice(vm.floatIp);
                },
                /*更改弹性公网IP购买数量*/
                changeProductCount: function () {
                    if (!api.testPositiveInteger(vm.productCount) && vm.productCount < 999999999) {
                        vm.floatIp.productCount = 0;
                    } else {
                        vm.floatIp.productCount = vm.productCount;
                    }
                    api.getPrice(vm.floatIp);
                },
                /*改变付款方式的年月选项*/
                changeCycleType: function (_fromOrder) {
                    var flag = _fromOrder == 'backFromOrder';
                    vm.cycleList = [];
                    angular.forEach(BuyCycle.cycleList, function (value, key) {
                        if (vm.floatIp.cycleType == key) {
                            vm.cycleList = value;
                        }
                    });
                    if (vm.cycleList.length > 0 && !flag) {
                        vm.floatIp.buyCycle = vm.cycleList[0].value;
                    }
                    api.getPrice(vm.floatIp);
                },
                /*更改购买周期*/
                changeBuyCycle: function () {
                    api.getPrice(vm.floatIp);
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
                    DatacenterService.queryAccount().then(function (money) {
                        DatacenterService.getBuyCondition().then(function (condition) {
                            vm.isNSF = money < condition;
                            if (!(vm.isNSF && vm.floatIp.payType == '2')) {
                                eayunStorage.set('floatOrderIp', vm.floatIp);
                                $state.go('buy.verifyFloatIp', {source: 'buy_floatip'});
                            }
                        });
                    });
                }
            };
            /*逻辑辅助函数包*/
            var api = {
                /*获取订单确认页返回购买页的数据记录*/
                getDataFromOrder: function (_temp) {
                    vm.floatIp = angular.copy(_temp, {});
                    vm.fromOrder = 'backFromOrder';
                    vm.buyCycleTemp = vm.floatIp.buyCycle;
                    vm.viewInterAction.typeChoose(vm.floatIp.payType);
                    vm.productCount = vm.floatIp.productCount;
                },
                /*获取弹性公网IP在项目下的可使用量*/
                getFloatIpQuotasByPrjId: function (_prjId) {
                    FloatIpService.getFloatIpQuotasByPrjId(_prjId).then(function (response) {
                        vm.quotas = response;
                    });
                },
                /*校验正整数*/
                testPositiveInteger: function (_input) {
                    var re = new RegExp(/^[1-9][0-9]*$/);
                    return re.test(_input);
                },
                /*获取价格*/
                getPrice: function (_order) {
                    FloatIpService.getPrice(_order).then(function (price) {
                        vm.hasGotPrice = true;
                        vm.floatIp.price = price;
                    }, function (message) {
                        vm.hasGotPrice = false;
                        vm.priceMsg = message;
                    });
                },
                /*获取当前信用额度*/
                getBuyCondition: function () {
                    DatacenterService.getBuyCondition().then(function (condition) {
                        vm.buyCondition = condition;
                    });
                }
            };

            initial.initData();
            /*initial.initDatacenter();
            initial.initBuyCycleOption();*/
        }])
    /**
     * 订单确认
     */
    .controller('VerifyFloatIpCtrl', ['$scope', 'eayunHttp', 'toast', 'eayunStorage', '$state', '$stateParams', 'DatacenterService', 'eayunMath',
        function ($scope, eayunHttp, toast, eayunStorage, $state, $stateParams, DatacenterService, eayunMath) {
            var vm = this;

            var initial = function () {
                initOperation();
                initData();
                initPrice();
            };

            var initOperation = function () {
                vm.source = $stateParams.source;
                vm.floatOrder = eayunStorage.get("floatOrderIp");
                if (vm.floatOrder == null) {
                    if (vm.source == 'buy_floatip') {
                        $state.go('buy.buyFloatIp');
                    } else if (vm.source == 'create_before') {
                        $state.go("buy.createFloatIp.payPake");
                    } else if (vm.source == 'create_after') {
                        $state.go('buy.createFloatIp.payRequired');
                    }
                }
            };
            var getPrice = function () {
                if (vm.floatOrder.productCount > 0) {
                    eayunHttp.post("cloud/floatip/getprice.do", vm.floatOrder).then(function (response) {
                        if (response.data.respCode == '000000') {
                            vm.hasGotPrice = true;
                            vm.floatOrder.price = response.data.data;
                            vm.floatOrder
                        } else if (response.data.respCode == '010120') {
                            vm.hasGotPrice = false;
                            vm.priceMsg = response.data.message;
                        }
                        usePrice();
                    });
                } else {
                    vm.floatOrder.price = 0;
                }
            };

            var initData = function () {
                vm.isBalance = false;
                vm.isError = false;
                vm.commitOrderFlag = true;
            };

            var initPrice = function () {
                DatacenterService.queryAccount().then(function(money){
                    vm.floatOrder.$$balance = money>0 ? money : 0;
                    if (vm.floatOrder.payType == "1") {
                        vm.floatOrder.$$orderName = "公网IP-包年包月";
                        vm.floatOrder.$$payTypeName = "预付费";
                    } else {
                        vm.floatOrder.$$orderName = "公网IP-按需付费";
                        vm.floatOrder.$$payTypeName = "后付费";
                    }
                    usePrice();
                });
                /*eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (data) {
                    vm.floatOrder.$$balance = data.data.data.money ? data.data.data.monoey : ;
                    if (vm.floatOrder.payType == "1") {
                        vm.floatOrder.$$orderName = "弹性公网IP-包年包月";
                        vm.floatOrder.$$payTypeName = "预付费";
                    } else {
                        vm.floatOrder.$$orderName = "弹性公网IP-按需付费";
                        vm.floatOrder.$$payTypeName = "后付费";
                    }
                    usePrice();
                });*/
            };
            //计算价钱--true 使用余额支付，false-不使用余额支付
            function usePrice() {
                if (vm.isBalance) {//使用余额支付
                    if (vm.floatOrder.$$balance >= vm.floatOrder.price) {
                        vm.floatOrder.accountPayment = vm.floatOrder.price;
                        vm.floatOrder.thirdPartPayment = 0;
                    } else {
                        vm.floatOrder.accountPayment = vm.floatOrder.$$balance;//余额支付
                        /*vm.floatOrder.thirdPartPayment = vm.floatOrder.price - vm.floatOrder.accountPayment;*///第三方支付
                        vm.floatOrder.thirdPartPayment = eayunMath.sub(Number(vm.floatOrder.price), vm.floatOrder.accountPayment);
                    }
                } else {
                    vm.floatOrder.accountPayment = 0;//余额支付
                    vm.floatOrder.thirdPartPayment = vm.floatOrder.price;//第三方支付
                }
            }

            /*使用余额支付*/
            vm.useBalance = function () {
                vm.isBalance = !vm.isBalance;
                usePrice();
            };
            /*提交订单*/
            vm.commitOrderFloatIp = function () {
                vm.commitOrderFlag = false;
                eayunHttp.post("cloud/floatip/buyfloatip.do", vm.floatOrder).then(function (data) {
                    if (data.data.respCode == '010120') {
                        vm.isErrMessage = data.data.message;
                        vm.commitOrderFlag = (data.data.btnFlag == '1');
                        vm.isError = true;
                        DatacenterService.queryAccount().then(function(money){
                        	vm.floatOrder.$$balance = money>0 ? money : 0;
                        	vm.isBalance = false;
                        	getPrice();
                        });
                        
                    } else {
                        if (vm.floatOrder.payType == "1") {//包年包月
                            if (vm.floatOrder.thirdPartPayment == 0) {
                                //余额足够付费，付款成功
                                $state.go('pay.result', {subject: "私有网络-更改带宽"});
                            } else {
                                if (data.data.orderNo) {
                                    var orderPayNavList = [{
                                        route: 'app.net.netbar.floatip',
                                        name: '弹性公网IP'
                                    }, {route: 'buy.createFloatIp.payPake', name: '申请弹性公网IP'}];
                                    var ordersIds = [data.data.orderNo];
                                    eayunStorage.persist("orderPayNavList", orderPayNavList);
                                    eayunStorage.persist("payOrdersNo", ordersIds);
                                    $state.go('pay.order');
                                }
                            }
                        } else {//按需付费
                            $state.go("app.order.list");
                        }
                    }
                });
            };
            /*返回修改配置*/
            vm.goToCreateFloatIp = function () {
                /*var route = "buy.createFloatIp.payRequired";
                 if (vm.floatOrder.payType == "1") {
                 route = "buy.createFloatIp.payPake";
                 }
                 eayunStorage.set("isCre", "0");
                 $state.go(route);*/
                eayunStorage.set('buy_floatip_again', vm.floatOrder);
                $state.go('buy.buyFloatIp');
            };

            initial();
        }])
    /**
     * 绑定资源
     */
    .controller('BindResourceController', function ($scope, eayunModal, eayunHttp, item, toast, $modalInstance) {
        $scope.model = {};
        $scope.model.resourceType = 'vm';

        $scope.substrSubnetName = function (text) {
            var testSubstr = text;
            var perText = text.substr(0, text.indexOf('('));
            if (perText.length > 12) {
                perText = perText.substr(0, 12) + "...";
            }
            testSubstr = perText + text.substr(text.indexOf('('), text.length - 1);
            return testSubstr;
        };

        /**
         * 初始化
         */
        $scope.init = function () {
            //查询项目下的网络
            eayunHttp.post('cloud/floatip/getNetworkByPrj.do', item.prjId).then(function (response) {
                $scope.networks = response.data;
            });
        };

        /**
         * 查询网络下的子网
         */
        $scope.changeSubnet = function () {
            $scope.subnets = [];
            $scope.model.subnet = {};
            var values = {};
            values.netId = $scope.model.network;
            if ($scope.model.network) {//getsubnetlist
                eayunHttp.post('cloud/subnetwork/getmanagedsubnetlist.do', values).then(function (response) {
                    console.info(response.data)
                    $scope.subnets = response.data.data;
                });
            }

        };

        /**
         * 查询子网下的指定资源类型的资源
         */
        $scope.changeResource = function () {
            $scope.model.resource = {};
            $scope.resources = [];
            if ($scope.model.subnet && $scope.model.resourceType) {
                eayunHttp.post('cloud/floatip/getResourceBySubnet.do', {
                    'subnetIp': $scope.model.subnet,
                    'resourceType': $scope.model.resourceType
                }).then(function (response) {
                    $scope.resources = response.data;
                });
            }

        };

        /**
         * 保存
         */
        $scope.commit = function () {
            $scope.checked = true;
            var isRate=true;
            $scope.data = {
                'dcId': item.dcId,
                'prjId': item.prjId,
                'floId': item.floId,
                'floIp': item.floIp,
                'vmIp':$scope.model.resource.subnetIp,
                'resourceId': $scope.model.resource.resourceId,
                'resourceType': $scope.model.resourceType,
                'portId': $scope.model.resource.portId
            };
            eayunHttp.post('cloud/floatip/bindResource.do', $scope.data).then(function (response) {
                if (response && response.data && response.data.respCode == '400000') {
                    toast.success('公网IP绑定资源成功', 2000);
                    $modalInstance.close();
                    //$scope.ok();
                }
                else {
                    $scope.checked = false;
                }
            });
        };
        $scope.cancel = function () {
            $modalInstance.dismiss();
        };

        $scope.init();
    })
    .controller('cloudFloatIpRenewCtrl', function ($scope, eayunHttp, item, eayunModal, eayunStorage, $modalInstance) {
    //重写确定 取消按钮方法
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
    $scope.commit = function () {
        $modalInstance.close();
    };

    eayunStorage.set('payType', item.payType);
    eayunStorage.set('floId', item.floId);
    eayunStorage.set('floIp', item.floIp);
    eayunStorage.set('ipCount', 1);
    eayunStorage.set('dcId', item.dcId);

    $scope.model = angular.copy(item);
    $scope.model.renewType = 'month';
    $scope.model.renewTime = '1';
    eayunStorage.set('cycle', $scope.model.renewTime);
    initGetPriceForRenew();
    /**
     * 切换付费类型
     */
    $scope.changePayType = function () {
        if ($scope.model.renewType == 'year') {
            $scope.model.renewTime = '12';
            eayunStorage.set('cycle', $scope.model.renewTime);
        } else {
            $scope.model.renewTime = '1';
            eayunStorage.set('cycle', $scope.model.renewTime);
        }
        initGetPriceForRenew();
    };
    /**
     * 切换时间选择
     */
    $scope.changeTime = function (renewType, renewTime) {
        if (renewTime == '0') {
            $scope.model.chargeMoney = null;
            $scope.model.lastTime = null;
        }
        if (renewType != 'zero' && renewTime != '0') {
            eayunStorage.set('cycle', renewTime);
            //调用计费算法得出需要支付的费用
            var cycleCount = renewTime;
            var paramBean = {
                'dcId': item.dcId,
                'payType': '1',
                'number': 1,
                'cycleCount': cycleCount,
                'ipCount': 1
            };
            eayunStorage.set('paramBean', paramBean);
            eayunHttp.post('billing/factor/getPriceDetails.do', paramBean).then(function (response) {
                $scope.responseCode = response.data.respCode;
                if ($scope.responseCode == '010120') {
                    $scope.respMsg = response.data.message;
                } else {
                    $scope.model.chargeMoney = response.data.data.totalPrice;
                    eayunStorage.set('needPay', response.data.data.totalPrice);
                }

            });
            //计算续费后的到期时间
            eayunHttp.post('order/computeRenewEndTime.do', {
                'original': $scope.model.endTime,
                'duration': renewTime
            }).then(function (response) {
                $scope.model.lastTime = response.data;
            });

        }

    };
    function initGetPriceForRenew() {
        var paramBean = {
            'dcId': item.dcId,
            'payType': '1',
            'number': 1,
            'cycleCount': $scope.model.renewTime,
            'ipCount': 1
        };
        eayunStorage.set('paramBean', paramBean);
        eayunHttp.post('billing/factor/getPriceDetails.do', paramBean).then(function (response) {
            $scope.responseCode = response.data.respCode;
            if ($scope.responseCode == '010120') {
                $scope.respMsg = response.data.message;
            } else {
                $scope.model.chargeMoney = response.data.data.totalPrice;
                eayunStorage.set('needPay', response.data.data.totalPrice);
            }
        });
        //计算续费后的到期时间
        eayunHttp.post('order/computeRenewEndTime.do', {
            'original': $scope.model.endTime,
            'duration': $scope.model.renewTime
        }).then(function (response) {
            $scope.model.lastTime = response.data;
        });
    };

})
    .controller('RenewConformFloatIpController', function ($scope, eayunHttp, eayunModal, eayunStorage, $state, FloatIpService,eayunMath) {
      //如果F5刷新  直接跳路由
      if('undefined'== eayunStorage.get('needPay') || null == eayunStorage.get('needPay')){
  		  $state.go("app.net.netbar.floatip");
  	  }	
    	
    var needPay = eayunStorage.get('needPay');
    var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
    $scope.model = {
        payType: eayunStorage.get('payType'),
        dcName: dcPrj.dcName,
        vmId: eayunStorage.get('vmId'),
        vmName: eayunStorage.get('vmName'),
        cycle: eayunStorage.get('cycle'),
        needPay: needPay,
        dcId: eayunStorage.get('dcId'),
        floIp: eayunStorage.get('floIp'),
        floId: eayunStorage.get('floId')
    };
    //查询账户金额
    eayunHttp.post('costcenter/accountoverview/getaccountbalance.do', {}).then(function (response) {
        var accountMoney = response.data.data.money;
        $scope.model.accountMoney = accountMoney;
    });

    $scope.isUse = false;
    $scope.model.deductMoney = 0.00;//formatFloat(0, 2);
    $scope.model.actualPay = $scope.model.needPay;
    //计算余额支付价钱
    $scope.useBalance = function () {
        if ($scope.model.isCheck) {//选中
            $scope.isUse = true;
            if ($scope.model.accountMoney - $scope.model.needPay >= 0) {
                $scope.model.deductMoney = $scope.model.needPay;
                $scope.model.actualPay = 0.00;//formatFloat(0.00, 2);
            } else {
                $scope.model.deductMoney = $scope.model.accountMoney;
                var payable = eayunMath.sub($scope.model.needPay,$scope.model.accountMoney);//$scope.model.needPay - $scope.model.accountMoney;
                $scope.model.actualPay = payable;


            }
        } else {
            $scope.isUse = false;
            $scope.model.deductMoney = 0.00;//formatFloat(0.00, 2);
            $scope.model.actualPay = $scope.model.needPay;//formatFloat($scope.model.needPay, 2);
        }
    };

    //续费订单确认页面刷新“产品金额”，“账户余额”
    function refreshMoney() {
        var b1, b2;
        //获取账户余额
        eayunHttp.post('costcenter/accountoverview/getaccountbalance.do', {}).then(function (response) {
            b1 = true;
            var balance = response.data.data.money;
            $scope.model.accountMoney = balance;
            if (b1 && b2) {
                $scope.useBalance();
            }
        });

        eayunHttp.post('billing/factor/getPriceDetails.do', eayunStorage.get('paramBean')).then(function (response) {
            b2 = true;
            $scope.responseCode = response.data.respCode;
            if ($scope.responseCode == '010120') {
                $scope.errorMsg = response.data.message;
            } else {
                $scope.model.needPay = response.data.data.totalPrice;//formatFloat(response.data.message,2);
            }
            if (b1 && b2) {
                $scope.useBalance();
            }
        });
    };
    //提交订单
    $scope.isLight = false;
    $scope.isError = false;
    $scope.errorMsg = '';
    $scope.submitOrder = function () {
        FloatIpService.checkIfOrderExist($scope.model.floId).then(function (response) {
            $scope.errorMsg = '资源正在调整中或您有未完成的订单，请稍后再试。';
            $scope.isError = true;
        }, function () {
            // 获取当前余额，判断此时账户余额是否大于余额支付金额，以确定能否提交订单
            var map = {
                floId: $scope.model.floId,
                aliPay: $scope.model.actualPay,
                accountPay: $scope.model.deductMoney,
                totalPay: $scope.model.needPay,
                isCheck: $scope.model.isCheck,
                dcName: $scope.model.dcName,
                buyCycle: $scope.model.cycle,
                dcId: $scope.model.dcId,
                payType: '1',
                number: 1,
                cycleCount: $scope.model.cycle
            };
            eayunHttp.post('cloud/floatip/renewFloatIpOrderConfirm.do', map).then(function (response) {
                if (response && response.data) {
                    //订单支付成功
                    if (response.data.respCode == '1') {//您当前有未完成订单，不允许提交新订单！
                        $scope.isError = true;
                        $scope.errorMsg = response.data.message;
                    }
                    else if (response.data.respCode == '2') {//您的产品金额发生变动，请重新确认订单！
                        $scope.isLight = true;
                        $scope.errorMsg = response.data.message;
                        refreshMoney();
                    }
                    else if (response.data.respCode == '3') {//您的账户余额发生变动，请重新确认订单！
                        $scope.isLight = true;
                        $scope.errorMsg = response.data.message;
                        refreshMoney();
                    }
                    else if (response.data.respCode == '0') {//完全支付宝支付，跳向支付宝支付页面！
                        var orderPayNavList = [{route: 'app.net.netbar.floatip', name: '弹性公网IP'}];
                        eayunStorage.persist("orderPayNavList", orderPayNavList);
                        eayunStorage.persist("payOrdersNo", response.data.message);
                        $state.go('pay.order');
                    }
                    else if (response.data.respCode == '10') {//完全余额支付，跳向支付成功页面！
                        $state.go('pay.result', {subject: response.data.message});
                    }
                }

            });


        });

    };


});

