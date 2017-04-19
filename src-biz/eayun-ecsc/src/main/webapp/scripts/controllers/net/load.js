'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.when('/app/net/netbar/loadbalance', '/app/net/netbar/loadbalance/poolList');
        $stateProvider.state('app.net.netbar.loadbalance', {//路由
            url: '/loadbalance',
            templateUrl: 'views/net/loadbalance/loadmng.html',
            controller: 'LoadCtrl'
        }).state('app.net.netbar.loadbalance.poollist', {
            url: '/poolList',
            templateUrl: 'views/net/loadbalance/pool/poolmng.html',
            controller: 'PoolListCtrl'
        }).state('app.net.loadbalaceDetail', {
            url: '/loadbalaceDetail:poolId',
            templateUrl: 'views/net/loadbalance/pool/pooldetail.html',
            controller: 'LoadBalancerDetailController'
        }).state('buy.lbrenew', {
            url: '/loadbalaceRenew',
            templateUrl: 'views/net/loadbalance/pool/lbrenewconfirm.html',
            controller: 'LoadBalancerRenewConfirmController'
        }).state('buy.lbbuy', {
            url: '/loadbalanceBuy/:orderNo',
            templateUrl: 'views/net/loadbalance/pool/buypool.html',
            controller: 'BuyLoadBalancerCtrl',
            controllerAs: 'buyPool'
        }).state('buy.lbbuyconfirm', {
            url: '/loadbalanceBuyConfirm/:source/:poolId',
            templateUrl: 'views/net/loadbalance/pool/buyconfirm.html',
            controller: 'BuyConfirmLoadBalancerCtrl',
            controllerAs: 'confirmBuy'
        })/*.state('buy.lbupdateconfirm', {
         url: '/loadbalanceUpdateConfirm',
         templateUrl: 'views/net/loadbalance/pool/updateconfirm.html',
         controller: 'UpdateConfirmLoadBalancerCtrl',
         controllerAs: 'confirmUpdate'
         })*/;
    })

    .controller('LoadCtrl', function ($scope, powerService) {
        powerService.powerRoutesList().then(function (powerList) {
            $scope.buttonPower = {
                isAdd: powerService.isPower('load_add'),	//创建
                isEdit: powerService.isPower('load_edit'),	//编辑
                isSetDetachHealthMonitor: powerService.isPower('load_bindmng'),		//解绑/绑定健康检查
                isTag: powerService.isPower('load_tag'),	//标签
                isBind: powerService.isPower('load_bindfloatip'),	//绑定公网IP或解绑公网IP
                isAddMember: powerService.isPower('loadmember_add'),	//添加成员
                isDelete: powerService.isPower('load_drop'),		//删除
                isChange: powerService.isPower('load_editcount'),		//更改最大连接数
                isRenew: powerService.isPower('load_renew')		//续费
            };
        });
    })
    .controller('PoolListCtrl', ['$scope', '$rootScope', 'eayunModal', 'eayunHttp', '$state', '$timeout', 'toast', 'PoolService', 'DatacenterService',
        function ($scope, $rootScope, eayunModal, eayunHttp, $state, $timeout, toast, PoolService, DatacenterService) {
            var list = [
                {'router': 'app.net.netbar.loadbalance', 'name': '负载均衡'}
            ];
            var dcId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : '';
            var prjId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '';
            $rootScope.navList(list, '负载均衡器');
            $rootScope.netRoute = null;
            /*搜索方法*/
            $scope.search = function () {
                $scope.myTable.api.draw();
            };
            /*表格数据*/
            $scope.myTable = {
                source: 'cloud/loadbalance/pool/getPoolList.do',
                api: {},
                getParams: function () {
                    return {
                        prjId: prjId,
                        dcId: dcId,
                        name: $scope.poolName || ''
                    };
                }
            };
            /*切换数据中心*/
            $scope.$watch('model.projectvoe', function (newValue, oldValue) {
                if (oldValue !== newValue) {
                    dcId = newValue.dcId;
                    prjId = newValue.projectId;
                    $scope.myTable.api.draw();
                }
            });

            $scope.poolJson = function (tagsStr) {
                var json = {};
                if (tagsStr) {
                    json = JSON.parse(tagsStr);
                }
                return json;
            };

            $scope.getPoolStatus = function (model) {
                $scope.poolStatusClass = '';
                if (model.poolStatus == 'ACTIVE' && model.chargeState == '0') {
                    return 'green';
                }
                else if (model.poolStatus == 'ERROR' || model.chargeState != '0') {
                    return 'gray';
                }
                else if ((model.poolStatus == 'PENDING_CREATE' || model.poolStatus == 'PENDING_UPDATE' || model.poolStatus == 'PENDING_DELETE') && model.chargeState == '0') {
                    return 'yellow';
                }
            };
            //监视器[监视数据中心、项目id变化]
            $scope.$watch('model.dcProject', function (newVal, oldVal) {
                if (newVal !== oldVal) {
                    $scope.myTable.api.draw();
                }
            });
            //pop框方法
            $scope.hintTagShow = [];
            $scope.openTableBox = function (pool) {
                if (pool.type == 'tagName') {
                    $scope.hintTagShow[pool.index] = true;
                }
                $scope.ellipsis = pool.value;
            };
            $scope.closeTableBox = function (pool) {
                if (pool.type == 'tagName') {
                    $scope.hintTagShow[pool.index] = false;
                }
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
            $(function () {
                document.onkeydown = function (event) {
                    var e = event || window.event || arguments.callee.caller.arguments[0];
                    if (!$scope.checkUser()) {
                        return;
                    }
                    if (e && e.keyCode == 13) {
                        $scope.myTable.api.draw();
                    }
                };
            });


            /**
             * 如果列表中有中间状态的资源池，间隔5s刷新列表
             */
            $scope.refreshList = function () {
                if (!$scope.checkUser()) {
                    return;
                }
                $scope.myTable.api.refresh();
            };
            /*刷新状态的方法*/
            $scope.refresh = function () {
                var keepgoing = true;
                angular.forEach($scope.myTable.result, function (_poolModel) {
                    var status = _poolModel.poolStatus.toString().toLowerCase();
                    if ('active' != status && 'error' != status && keepgoing) {
                        $scope.refreshList();
                        keepgoing = false;
                    }
                });
            };
            //资源池详情
            $scope.detailPool = function (item) {
                $state.go('app.net.loadbalaceDetail', {"poolId": item.poolId});
            };
            /*购买负载均衡器*/
            $scope.buyPool = function () {
                $state.go('buy.lbbuy');
            };
            /*创建负载均衡器*/
            $scope.createPool = function () {
                var result = eayunModal.dialog({
                    showBtn: false,
                    title: '创建负载均衡器',
                    width: '600px',
                    templateUrl: 'views/net/loadbalance/pool/addpool.html',
                    controller: 'poolAddCtrl',
                    resolve: {
                        prjId: function () {
                            return {prjId: sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : ''};
                        }
                    }
                });
                result.result.then(function (value) {
                    $scope.myTable.api.draw();
                }, function () {
                    $scope.myTable.api.draw();
                });
            };

            /**
             * 查询列表
             */
            $scope.getPool = function () {
                $scope.myTable.api.draw();

            };
            /*更改最大连接数*/
            $scope.changeConnectionLimit = function (item) {
                var result = eayunModal.open({
                    title: '负载均衡续费',
                    backdrop: 'static',
                    templateUrl: 'views/net/loadbalance/pool/changeconnlimit.html',
                    controller: 'ChangeConnectionLimitCtrl',
                    controllerAs: 'changeConn',
                    resolve: {
                        item: function () {
                            return item;
                        }
                    }
                });
                result.result.then(function () {
                    $state.go('buy.lbbuyconfirm', {source: 'change_pool_list'});
                }, function () {
                    $scope.myTable.api.draw();
                });
            };
            /**
             * 修改负载均衡器
             */
            $scope.editPool = function (item) {
                var result = eayunModal.dialog({
                    showBtn: false,
                    title: '编辑负载均衡器',
                    width: '600px',
                    templateUrl: 'views/net/loadbalance/pool/editpool.html',
                    controller: 'editPoolCtrl',
                    resolve: {
                        item: function () {
                            return item;

                        }
                    }
                });

                result.then(function (value) {
                    $scope.myTable.api.draw();
                }, function () {
                    $scope.myTable.api.draw();
                });
            };

            /**
             * 删除负载均衡器
             */
            $scope.deletePool = function (item) {
                eayunModal.confirm('确定要删除负载均衡器' + item.poolName + '?').then(function () {
                    eayunHttp.post('cloud/loadbalance/pool/deleteBalancer.do', item).then(function (response) {
                        if (response && response.data && response.data.respCode == "100000") {
                            toast.success('删除负载均衡器成功');
                        }
                        $scope.myTable.api.draw();
                    });
                });
            };

            /**
             * 负载均衡续费
             * @param item
             */
            $scope.renewLoadBalancer = function (item) {
                var result = eayunModal.open({
                    title: '负载均衡续费',
                    backdrop: 'static',
                    templateUrl: 'views/net/loadbalance/pool/lbrenew.html',
                    controller: 'loadBalancerRenewCtrl',
                    resolve: {
                        item: function () {
                            return item;
                        }
                    }
                });
                result.result.then(function () {
                    PoolService.checkIfOrderExist(item).then(function (response) {
                        //isOrderExisted = true;
                        eayunModal.warning("资源正在调整中或您有未完成的订单，请稍后再试。");
                    }, function () {
                        $state.go('buy.lbrenew');

                    });
                }, function () {

                });

            };

            /*标签*/
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
                    $scope.myTable.api.draw();
                }, function () {
                    $scope.myTable.api.draw();
                });
            };

            /**
             * 健康检查
             */
            $scope.bindMonitor = function (item) {
                var result = eayunModal.open({
                    showBtn: false,
                    title: '健康检查',
                    backdrop: 'static',
                    templateUrl: 'views/net/loadbalance/pool/bindhealthmonitor.html',
                    controller: 'bindHealthMonitorCtrl',
                    resolve: {
                        item: function () {
                            return item;
                        }
                    }
                });
                result.result.then(function (value) {
                    $scope.myTable.api.draw();
                }, function () {
                    $scope.myTable.api.draw();
                });
            };
            /**
             * 解除健康检查
             */
            $scope.unBindMonitor=function(item){
            	eayunModal.confirm('确定解除健康检查关联?').then(function () {
            		var data={
            				prjId:item.prjId,
            				dcId:item.dcId,
            				poolId:item.poolId,
            		}
                    eayunHttp.post('cloud/loadbalance/healthmonitor/unBindHealthMonitor.do', data).then(function (response) {
                        if (response && response.data && response.data.respCode == "400000") {
                            toast.success('解除健康检查成功');
                        }
                        $scope.myTable.api.draw();
                    });
                });
            };
            /**
             * 添加成员
             */
            $scope.addMember = function (item) {
                var result = eayunModal.open({
                    title: '添加成员',
                    backdrop: 'static',
                    templateUrl: 'views/net/loadbalance/member/addmember.html',
                    controller: 'AddMemeberController',
                    resolve: {
                        item: function () {
                            return item;
                        }
                    }
                });
                result.result.then(function (value) {
                    $scope.myTable.api.draw();
                }, function () {
                    $scope.myTable.api.draw();
                });
            };

            /**
             * 绑定公网IP
             */
            $scope.bindFloatIp = function (item) {
                var result = eayunModal.open({
                    title: '绑定公网IP',
                    backdrop: 'static',
                    templateUrl: 'views/net/loadbalance/pool/bindFloatIp.html',
                    controller: 'BindFloatIpController',
                    resolve: {
                        item: function () {
                            return item;
                        }
                    }
                });
                result.result.then(function (value) {
                    $scope.myTable.api.draw();
                }, function () {
                    $scope.myTable.api.draw();
                });
            };

            /**
             * 解绑公网IP
             */
            $scope.unbundingFloatIp = function (item) {
                $scope.data = {
                    dcId: item.dcId,
                    prjId: item.prjId,
                    resourceId: item.poolId,
                    resourceType: 'lb',
                    floId: item.floatId,
                    floIp: item.floatIp
                };

                eayunModal.confirm('确定要解绑' + item.poolName + '的公网IP ?').then(function () {
                    eayunHttp.post('cloud/floatip/unbundingResource.do', $scope.data).then(function (response) {
                        if (response && response.data && response.data.respCode == '400000') {
                            toast.success("解绑公网IP:" + DatacenterService.toastEllipsis(item.floatIp, 7) + "成功");
                            $scope.myTable.api.draw();
                        } else {
                            $scope.myTable.api.draw();
                        }
                    });
                });
            };

        }])
    /**
     * 负载均衡续费订单确认页controller
     */
    .controller('LoadBalancerRenewConfirmController', ['$scope', '$state', 'eayunModal', 'eayunHttp', 'eayunStorage', 'PoolService','eayunMath',
        function ($scope, $state, eayunModal, eayunHttp, eayunStorage, PoolService, eayunMath) {
            //读取页面共享数据，用于页面展示
            var chargeMoney = eayunStorage.get('lb_chargeMoney');
            if (chargeMoney == null) {
                $state.go('app.net.netbar.loadbalance.poollist');//如果刷新页面，则eayunStorage清空，则需要跳转到列表页
            }
            $scope.model = {
                dcName: eayunStorage.get('lb_dcName'),
                poolId: eayunStorage.get('lb_id'),
                poolName: eayunStorage.get('lb_name'),
                connLimit: eayunStorage.get('lb_connLimit'),
                cycle: eayunStorage.get('lb_cycle'),
                chargeMoney: chargeMoney,
                paramBean: eayunStorage.get('lb_paramBean'),
                lastTime: eayunStorage.get('lb_expireTime'),
                endTime: eayunStorage.get('lb_endTime')
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
                        $scope.model.payable = eayunMath.sub($scope.model.chargeMoney,$scope.model.deduction);
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
                PoolService.checkIfOrderExist($scope.model).then(function (response) {
                    $scope.warningMessage = '资源正在调整中或您有未完成的订单，请稍后再试。';
                    $scope.canSubmit = false;
                    //eayunModal.info("资源正在调整中或您有未完成的订单，请稍后再试。");
                    //$state.go('app.net.netbar.loadbalance.poollist');
                }, function () {
                    //当前无未完成订单，可以提交订单
                    //获取当前余额，判断此时账户余额是否大于余额支付金额，以确定能否提交订单
                    //直接调用续费接口，在接口中进行一系列校验，如果校验不通过，页面进行响应的展现，如果校验通过，则完成订单。
                    eayunHttp.post('cloud/loadbalance/pool/renewbalancer.do', $scope.model, {$showLoading: true}).then(function (response) {
                        var respCode = response.data.respCode;
                        var respMsg = response.data.message;
                        var respOrderNo = response.data.orderNo;
                        var connections = response.data.connections;
                        if (respCode == '010110') {
                            $scope.warningMessage = respMsg;
                            $scope.model.isSelected = false;
                            $scope.model.connLimit = new Number(connections);
                            $scope.model.paramBean.connCount = new Number(connections);
                            refreshMoney();

                        } else if (respCode == '000000') {
                            if (respMsg == 'BALANCE_PAY_ALL') {
                                //直接跳转到订单完成界面
                                $state.go('pay.result', {subject: '负载均衡器-续费'});
                            } else if (respMsg == 'RENEW_SUCCESS' || respMsg == 'BALANCE_PAY_PART') {
                                //跳转到第三方支付页面
                                var orderPayNavList = [{route: 'app.net.netbar.loadbalance.poollist', name: '负载均衡器'}];
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
     * 负载均衡续费controller
     */
    .controller('loadBalancerRenewCtrl', ['$scope', 'eayunModal', 'eayunHttp', 'item', 'eayunStorage', '$modalInstance',
        function ($scope, eayunModal, eayunHttp, item, eayunStorage, $modalInstance) {
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
                        connCount: item.connectionLimit
                    };
                    //将参数注入到eayunStorage用于传递给订单确认页面
                    eayunStorage.set('lb_dcName', item.dcName);
                    eayunStorage.set('lb_id', item.poolId);
                    eayunStorage.set('lb_name', item.poolName);
                    eayunStorage.set('lb_connLimit', item.connectionLimit);
                    eayunStorage.set('lb_cycle', cycleCount);
                    eayunStorage.set('lb_paramBean', paramBean);
                    eayunStorage.set('lb_endTime', item.endTime);

                    eayunHttp.post('billing/factor/getPriceDetails.do', paramBean).then(function (response) {
                        $scope.responseCode = response.data.respCode;
                        if ($scope.responseCode == '010120') {
                            $scope.respMsg = response.data.message;
                        } else {
                            $scope.model.chargeMoney = response.data.data.totalPrice;
                            eayunStorage.set('lb_chargeMoney', $scope.model.chargeMoney);
                        }
                    });
                    //计算续费后的到期时间
                    eayunHttp.post('order/computeRenewEndTime.do', {
                        'original': $scope.model.endTime,
                        'duration': renewTime
                    }).then(function (response) {
                        $scope.model.lastTime = response.data;
                        eayunStorage.set('lb_expireTime', $scope.model.lastTime);
                    });
                }
            };
        }])

    .controller('BuyLoadBalancerCtrl', ['DatacenterService', 'VpcService', 'PoolService', '$state', 'eayunStorage', 'BuyCycle', '$stateParams',
        function (DatacenterService, VpcService, PoolService, $state, eayunStorage, BuyCycle, $stateParams) {
            var vm = this;
            /*全部初始化*/
            var initial = function () {
                vm.dcInited = false;
                if ($stateParams.orderNo) {
                    PoolService.getOrderLdPoolByOrderNo($stateParams.orderNo).then(function (pool) {
                        VpcService.getNetIdBySubnetId(pool.subnetId).then(function (subnet) {
                            pool.netId = subnet.netId;
                            if (pool.payType == '1') {
                                pool.cycleType = (pool.buyCycle < 12) ? 'month' : 'year';
                            }
                            /*后台获取的cloudorderldpool的数据包当中，去掉主键id，以防止save入库抹掉已有数据*/
                            delete pool.orderPoolId;
                            initDataOfOrderBack(pool);
                            initDatacenter();
                            initBuyCycleOption();
                        });
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
                vm.isNSF = false;
                vm.totalPrice = 0;
                vm.isNameExist = false;
                vm.hasGotPrice = true;
                vm.changeOfBilling = false;
                vm.buyCycleTemp = 1;
                getBuyCondition();
                var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
                vm.pool = {
                    dcId: dcPrj.dcId,
                    dcName: dcPrj.dcName,
                    prjId: dcPrj.projectId,
                    orderType: 0,
                    payType: 1,
                    connectionLimit: 5000,
                    buyCycle: 1,
                    mode:0
                };
                var temp = eayunStorage.get('buy_pool_again');
                eayunStorage.delete('buy_pool_again');
                if (angular.isDefined(temp)) {
                    initDataOfOrderBack(temp);
                }
            };
            /*初始化返回配置的购买页面数据*/
            var initDataOfOrderBack = function (_temp) {
                vm.pool = angular.copy(_temp, {});
                vm.fromOrder = 'backFromOrder';
                vm.buyCycleTemp = vm.pool.buyCycle;
                vm.typeChoose(vm.pool.payType);
                PoolService.getNetworkListByPrjId(vm.pool.prjId).then(function (response) {
                    vm.networkList = response;
                    if (vm.networkList) {
                        angular.forEach(vm.networkList, function (network) {
                            if (vm.pool.netId == network.netId) {
                                vm.network = network;
                            }
                        });
                    }
                    if (angular.isDefined(vm.network.netId)) {
                        PoolService.getSubnetListByNetId(vm.network.netId, '1').then(function (response) {
                            vm.subnetList = response;
                            angular.forEach(vm.subnetList, function (subnet) {
                                if (vm.pool.subnetId == subnet.subnetId) {
                                    vm.subnet = subnet;
                                }
                            });
                        });
                    }
                });
                vm.changeLbMethod();
            };
            /*初始化数据中心*/
            var initDatacenter = function () {
                DatacenterService.getDcPrjList().then(function (response) {
                    vm.datacenters = response;
                    var initialized = false;
                    if (vm.datacenters.length > 0) {
                        angular.forEach(vm.datacenters, function (value, key) {
                            if (vm.pool.prjId == value.projectId) {
                                initialized = true;
                                vm.selectDcPrj(value);
                            }
                        });
                        if (!initialized) {
                            vm.selectDcPrj(vm.datacenters[0]);
                        }
                    }
                });
                initNetworkList();
            };
            /*初始化购买周期的选项*/
            var initBuyCycleOption = function () {
                vm.cycleTypeList = BuyCycle.cycleTypeList;
                if (vm.fromOrder != 'backFromOrder' || (vm.fromOrder == 'backFromOrder' && vm.pool.payType == '2')) {
                    vm.pool.cycleType = 'month';
                    vm.changeCycleType();
                } else {
                    vm.changeCycleType(vm.fromOrder);
                }
            };
            /*初始化项目下的网络列表*/
            var initNetworkList = function () {
                PoolService.getNetworkListByPrjId(vm.pool.prjId).then(function (response) {
                    vm.networkList = response;
                });
            };
            /*选择付款方式*/
            vm.typeChoose = function (_payType) {
                vm.pool.payType = _payType;
                //initDatacenter();
                if (_payType == '2') {
                    vm.buyCycleTemp = vm.pool.buyCycle;
                    vm.pool.buyCycle = 1;
                } else {
                    vm.pool.buyCycle = vm.buyCycleTemp;
                }
                getPrice();
            };
            /*选择数据中心和项目*/
            vm.selectDcPrj = function (_datacenter) {
                vm.pool.dcId = _datacenter.dcId;
                vm.pool.dcName = _datacenter.dcName;
                vm.pool.prjId = _datacenter.projectId;
                vm.getPoolQuotasByPrjId();
                vm.checkNameExist();
                getPrice();
                if (vm.dcInited) {
                    api.cleanNetwork();
                } else {
                    vm.dcInited = true;
                }
                initNetworkList();

            };
            /*查询当前配额*/
            vm.getPoolQuotasByPrjId = function () {
                PoolService.getPoolQuotasByPrjId(vm.pool.prjId).then(function (response) {
                    vm.quotas = response;
                });
            };
            /*校验重名*/
            vm.checkNameExist = function () {
                vm.checkBuyBtn = true;
                var pname =vm.pool.poolName;
                PoolService.checkPoolNameExist(vm.pool).then(function (isTrue) {
                	 if(pname === vm.pool.poolName){
                		  vm.isNameExist = !isTrue;
                          vm.checkBuyBtn = false;	
                	 }
                  
                });
            };
            /*更改最大连接数*/
            vm.changeConnLimit = function () {
                getPrice();
            };
            /*改变付款方式的年月选项*/
            vm.changeCycleType = function (fromOrder) {
                var flag = (fromOrder == 'backFromOrder' && vm.pool.payType == '1');
                vm.cycleList = [];
                angular.forEach(BuyCycle.cycleList, function (value, key) {
                    if (vm.pool.cycleType == key) {
                        vm.cycleList = value;
                    }
                });
                if (vm.cycleList.length > 0 && !flag) {
                    vm.pool.buyCycle = vm.cycleList[0].value;
                }
                getPrice();
            };
            /*更改购买周期*/
            vm.changeBuyCycle = function () {
                getPrice();
            };
            /*更换网络*/
            vm.changeNetwork = function () {
                vm.pool.subnetId = '';
                vm.subnetList = [];
                if (vm.network) {
                    /*提交购买接口不需要pool当中有netId，但是从支付订单界面返回购买页面时，需要netId来选择之前用户选定的配置*/
                    vm.pool.netId = vm.network.netId;
                    vm.subnet = {};
                    PoolService.getSubnetListByNetId(vm.network.netId).then(function (response) {
                        vm.subnetList = response;
                    });
                }
            };
            /*更换子网*/
            vm.changeSubnet = function () {
                vm.pool.subnetId = vm.subnet.subnetId;
            };
            /*更换负载均衡方式*/
            vm.changeLbMethod = function () {
                if (vm.pool.lbMethod === 'ROUND_ROBIN') {
                    vm.lbMethod = '轮询';
                } else if (vm.pool.lbMethod === 'LEAST_CONNECTIONS') {
                    vm.lbMethod = '最小连接数';
                } else if (vm.pool.lbMethod === 'SOURCE_IP') {
                    vm.lbMethod = '源地址';
                }
            };
            /*获取价格*/
            var getPrice = function () {
                if (vm.pool.connectionLimit && vm.pool.buyCycle) {
                    PoolService.getPrice(vm.pool).then(function (response) {
                        vm.hasGotPrice = true;
                        vm.totalPrice = response;
                        vm.pool.price = response;
                    }, function (message) {
                        vm.hasGotPrice = false;
                        vm.priceMsg = message;
                    });
                }
            };
            /*查询当前余额*/
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
            /*提交订单*/
            vm.buyAtOnce = function () {
                /*valid 1*/
                PoolService.queryAccount().then(function (money) {
                    DatacenterService.getBuyCondition().then(function (condition) {
                        vm.buyCondition = condition;
                        vm.isNSF = money < vm.buyCondition;
                        if (!(vm.isNSF && vm.pool.payType == '2')) {
                            /*valid 2*/
                            PoolService.getPoolQuotasByPrjId(vm.pool.prjId).then(function (response) {
                                vm.quotas = response;
                                if (vm.quotas > 0) {
                                    eayunStorage.set('pool', vm.pool);
                                    $state.go('buy.lbbuyconfirm', {source: 'buy_pool'});
                                }
                            });
                        }
                    });
                });
            };

            var api = {
                cleanNetwork: function () {
                    vm.network = {};
                    vm.pool.netId = '';
                    vm.subnet = {};
                    vm.pool.subnetId = '';
                }
            };
            initial();
        }])
    /**
     * 更改最大连接数页面ctrl
     */
    .controller('ChangeConnectionLimitCtrl', ['$state', 'item', '$scope', 'eayunStorage', 'PoolService', 'DatacenterService', '$modalInstance', 'eayunModal',
        function ($state, item, $scope, eayunStorage, PoolService, DatacenterService, $modalInstance, eayunModal) {
            var vm = this;

            var initial = function () {
                initData();
                vm.getPrice();
            };

            var initData = function () {
                vm.pool = angular.copy(item, {});
                vm.pool.orderType = 2;
                vm.pool.connectionLimitOld = item.connectionLimit;
                vm.pool.buyCycle = 1;
            };

            vm.getPrice = function () {
                PoolService.getPrice(vm.pool).then(function (price) {
                    vm.hasGotPrice = true;
                    vm.pool.price = price;
                }, function (message) {
                    vm.hasGotPrice = false;
                    vm.priceMsg = message;
                });
            };

            vm.commit = function () {
                PoolService.checkIfOrderExist(vm.pool).then(function () {
                    $modalInstance.dismiss();
                    eayunModal.warning("资源正在调整中或您有未完成的订单，请稍后再试。");
                }, function () {
                    DatacenterService.queryAccount().then(function (account) {
                        if (account <= 0 && '2' == vm.pool.payType) {
                            $modalInstance.dismiss();
                            eayunModal.warning('您的账户已欠费，请充值后操作');
                        } else {
                            eayunStorage.set('pool', vm.pool);

                            $modalInstance.close();
                        }
                    });
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss();
            };

            initial();
        }])
    .controller('BuyConfirmLoadBalancerCtrl', ['$state', '$stateParams', 'eayunStorage', 'PoolService', 'DatacenterService', 'eayunMath',
        function ($state, $stateParams, eayunStorage, PoolService, DatacenterService, eayunMath) {
            var vm = this;
            /*初始化订单确认页面数据*/
            var initial = function () {
                vm.temp = eayunStorage.get('pool');
                vm.source = $stateParams.source;
                if (!vm.temp) {
                    if (vm.source == 'buy_pool') {
                        $state.go('buy.lbbuy');
                    } else if (vm.source == 'change_pool_list') {
                        $state.go('app.net.netbar.loadbalance.poollist');
                    } else if (vm.source == 'change_pool_detail') {
                        $state.go('app.net.loadbalaceDetail', {"poolId": $stateParams.poolId});
                    }
                } else {
                    vm.commitOrderFlag = true;
                    vm.temp.accountPayment = 0;
                    vm.temp.thirdPartPayment = vm.temp.price - vm.temp.accountPayment;
                    /*获取账户余额*/
                    PoolService.queryAccount().then(function (response) {
                        vm.accountQuota = response > 0 ? response : 0;
                    });
                    /*获取信用额度*/
                    DatacenterService.getBuyCondition().then(function (condition) {
                        vm.payAfterCondition = condition;
                    });
                }
            };
            /*选择是否使用余额支付*/
            vm.useBalancerPay = function () {
                if (vm.useBalancer) {
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
                PoolService.commitOrder(vm.temp).then(function (data) {
                    if ('1' == vm.temp.payType) {
                        if (0 != vm.temp.thirdPartPayment) {
                            if (data.orderNo) {
                                var orderPayNavList = '';
                                if (vm.temp.orderType == '0') {
                                    orderPayNavList = [{
                                        route: 'app.net.netbar.loadbalance',
                                        name: '负载均衡器'
                                    }, {
                                        route: 'buy.lbbuy',
                                        name: '创建负载均衡器'
                                    }];
                                } else if (vm.temp.orderType == '2') {
                                    orderPayNavList = [{route: 'app.net.netbar.loadbalance', name: '负载均衡器'}];
                                }
                                var ordersIds = [data.orderNo];
                                eayunStorage.persist("orderPayNavList", orderPayNavList);
                                eayunStorage.persist("payOrdersNo", ordersIds);
                                $state.go('pay.order');
                            }
                        } else {
                            $state.go('pay.result');
                        }
                    } else if ('2' == vm.temp.payType) {
                        $state.go('app.order.list');
                    }
                }, function (respMsg) {
                    vm.errFlag = true;
                    if ('OUT_OF_QUOTA' == respMsg) {
                        vm.errMsg = '您的负载均衡数量配额不足，请提交工单申请配额';
                    } else if ('BALANCE_OF_ARREARS' == respMsg) {
                        vm.errMsg = '您的账户已欠费，请充值后操作';
                    } else if ('NOT_SUFFICIENT_FUNDS' == respMsg) {
                        DatacenterService.getBuyCondition().then(function (condition) {
                            vm.payAfterCondition = condition;
                            vm.errMsg = '您的账户余额不足' + vm.payAfterCondition + '元，请充值后操作';
                        });
                    } else if ('CHANGE_OF_BILLINGFACTORY' == respMsg) {
                        if ('0' == vm.temp.orderType) {
                            PoolService.getPrice(vm.temp).then(function (price) {
                                vm.temp.price = price;
                                vm.useBalancer = false;
                                vm.useBalancerPay();
                                vm.errMsg = '您的订单金额发生变动，请重新确认订单';
                                vm.commitOrderFlag = true;
                            });
                        } else {
                            PoolService.getLoadBalanceById(vm.temp.poolId).then(function (_pool) {
                                vm.temp.endTime = _pool.endTime;
                                PoolService.getPrice(vm.temp).then(function (price) {
                                    vm.temp.price = price;
                                    vm.useBalancer = false;
                                    vm.useBalancerPay();
                                    vm.errMsg = '您的订单金额发生变动，请重新确认订单';
                                    vm.commitOrderFlag = true;
                                });
                            });
                        }
                    } else if ('CHANGE_OF_BALANCE' == respMsg) {
                        PoolService.queryAccount().then(function (money) {
                            vm.accountQuota = money > 0 ? money : 0;
                            vm.useBalancer = false;
                            vm.useBalancerPay();
                            vm.errMsg = '您的余额发生变动，请重新确认订单';
                            vm.commitOrderFlag = true;
                        });
                    } else if ('UPGRADING_OR_RENEWING' == respMsg) {
                        vm.errMsg = '资源正在调整中或您有未完成的订单，请您稍后再试';
                    } else if ('CHANGE_OF_CONFIGURATION' == respMsg) {
                        vm.errMsg = '您的订单规格发生变动，请重新确认订单';
                    } else if ('500' == respMsg && '2' == vm.temp.payType) {
                        $state.go('app.order.list');
                    }
                });
            };
            /*返回修改配置*/
            vm.returnToConfig = function () {
                eayunStorage.set('buy_pool_again', vm.temp);
                $state.go('buy.lbbuy');
            };

            initial();
        }])
    /**
     * @ngdoc function
     * @name eayunApp.controller:poolAddCtrl
     * @description
     * # poolAddCtrl
     * 创建负载均衡器
     */
    .controller('poolAddCtrl', function ($scope, eayunModal, eayunHttp, toast, prjId) {
        $scope.model = {};
        $scope.checkName = true;

        /**
         * 初始化 项目
         */
        $scope.init = function () {
            eayunHttp.post('cloud/vm/getProListByCustomer.do').then(function (response) {
                $scope.projectList = response.data;
                angular.forEach($scope.projectList, function (value, key) {
                    if (value.projectId == prjId.prjId) {
                        $scope.model.project = value;
                        $scope.changeNetworkByPrjId(value);
                    }
                });
            });
        };


        /**
         * 更改项目校验
         */
        $scope.changePrj = function () {
            if ($scope.model.project) {
                $scope.changeNetworkByPrjId($scope.model.project);
                if ($scope.model.poolName) {
                    $scope.checkNameExsit();
                }
            }
        };

        /**
         * 根据项目查询网络
         */
        $scope.changeNetworkByPrjId = function (item) {
            $scope.networks = [];
            $scope.model.network = {};
            eayunHttp.post('cloud/floatip/getNetworkByPrj.do', item.projectId).then(function (response) {
                $scope.networks = response.data;
            });
        };

        /**
         * 查询网络下的子网
         */
        $scope.changeSubnet = function () {
            $scope.model.subnet = {};
            $scope.subnets = [];
            if ($scope.model.network) {
                eayunHttp.post('cloud/floatip/getSubnetByNetwork.do', $scope.model.network).then(function (response) {
                    $scope.subnets = response.data;
                });
            }

        };

        /**
         * 校验负载均衡器的项目下的重名校验
         */
        $scope.checkNameExsit = function () {
            if ($scope.model.project && $scope.model.poolName) {
                $scope.item = {
                    'poolName': $scope.model.poolName,
                    'dcId': $scope.model.project.dcId,
                    'prjId': $scope.model.project.projectId
                };
                var name = $scope.model.poolName;
                eayunHttp.post('cloud/loadbalance/pool/checkPoolNameExsit.do', $scope.item).then(function (response) {
                    if (name === $scope.model.poolName) {
                        $scope.checkName = response.data;
                    }
                });
            }
        };

        /**
         * 保存
         */
        $scope.commit = function () {
            $scope.checkButton = true;
            $scope.item = {
                'dcId': $scope.model.project.dcId,
                'prjId': $scope.model.project.projectId,
                'poolName': $scope.model.poolName,
                'subnetId': $scope.model.subnet,
                'lbMethod': $scope.model.lbMethod,
                'poolProtocol': $scope.model.poolProtocol,
                'vipPort': $scope.model.port,
                'connectionLimit': $scope.model.limitNum
            };

            eayunHttp.post('cloud/loadbalance/pool/createBalancer.do', $scope.item).then(function (response) {
                if (response && response.data && response.data.respCode == '000000') {
                    var name = $scope.model.poolName.length > 6 ? $scope.model.poolName.substr(0, 6) + '...' : $scope.model.poolName;
                    toast.success('创建负载均衡器' + name + '成功', 2000);
                    $scope.ok();
                }
                else {
                    $scope.checkButton = false;
                }
            });
        };

        $scope.substrSubnetName = function (text) {
            var testSubstr = text;
            var perText = text.substr(0, text.indexOf('('));
            if (perText.length > 12) {
                perText = perText.substr(0, 12) + "...";
            }
            testSubstr = perText + text.substr(text.indexOf('('), text.length - 1);
            return testSubstr;
        };

        $scope.init();

    })
    /**
     * @ngdoc function
     * @name eayunApp.controller:editRouteCtrl
     * @description
     * # editRouteCtrl
     * 负载均衡器列表页-->编辑负载均衡器
     */
    .controller('editPoolCtrl', function ($scope, eayunModal, eayunHttp, item, toast) {
        $scope.model = {};
        $scope.checkName = true;
        $scope.model = angular.copy(item);
        if ($scope.model.connectionLimit === 0) {
            $scope.model.connectionLimit = null;
        }

        /**
         * 校验负载均衡器的项目下的重名校验
         */
        $scope.checkNameExsit = function () {
            if ($scope.model.poolName) {
                $scope.item = {
                    'poolName': $scope.model.poolName,
                    'dcId': $scope.model.dcId,
                    'prjId': $scope.model.prjId,
                    'poolId': $scope.model.poolId
                };
                var name = $scope.model.poolName;
                eayunHttp.post('cloud/loadbalance/pool/checkPoolNameExsit.do', $scope.model).then(function (response) {
                    if (name === $scope.model.poolName) {
                        $scope.checkName = response.data;
                    }
                });
            }
        };

        /**
         * 提交
         */
        $scope.commit = function () {
            $scope.checkButton = true;
            eayunHttp.post('cloud/loadbalance/pool/updateBalancer.do', $scope.model).then(function (response) {
                if (response && response.data && response.data.respCode == '200000') {
                    var name = $scope.model.poolName.length > 6 ? $scope.model.poolName.substring(0, 6) + "..." : $scope.model.poolName;
                    toast.success('修改负载均衡器' + name + '成功');
                    $scope.ok();
                }
                else {
                    $scope.checkButton = false;
                }
            });

        };

    })
    .controller('LoadBalancerDetailController', function ($scope, $rootScope, $state, powerService, eayunHttp, $timeout, $stateParams, eayunModal, toast, PoolService, DatacenterService) {
        var list = [
            {'router': 'app.net.netbar.loadbalance', 'name': '负载均衡'},
            {'router': 'app.net.netbar.loadbalance.poollist', 'name': '负载均衡器'}
        ];
        $rootScope.navList(list, '负载均衡器详情', 'detail');

        $rootScope.netRoute = "#/app/net/netbar/loadbalance/poolList";
        $rootScope.netname = '负载均衡器';

        var temp = '';
        /*负载均衡名称编辑框*/
        $scope.editPoolName = function () {
            temp = $scope.item.poolName;
            $scope.poolNameEditable = true;
            $scope.hintNameShow = true;
        };

        $scope.checkPoolNameExist = function (_network) {
            $scope.checkEditBtn = false;
            PoolService.checkPoolNameExist(_network).then(function (response) {
                $scope.checkPoolName = response;
                $scope.checkEditBtn = true;
            });
        };
        /*保存修改名称*/
        $scope.saveEdit = function () {
            PoolService.updateBalancerName($scope.item).then(function (response) {
                if (response != null) {
                    $scope.poolNameEditable = false;
                    $scope.hintNameShow = false;
                    toast.success('修改' + DatacenterService.toastEllipsis($scope.item.poolName, 6) + '负载均衡器成功');
                    $scope.init();
                }
            });
        };
        /*取消修改名称*/
        $scope.cancelEdit = function () {
            $scope.poolNameEditable = false;
            $scope.hintNameShow = false;
            $scope.item.poolName = temp;
        };

        powerService.powerRoutesList().then(function (powerList) {
            $scope.buttonPower = {
                isEdit: powerService.isPower('load_edit'),	//编辑
                isChange: powerService.isPower('load_editcount'),   //更改连接数
                isSetDetachHealthMonitor: powerService.isPower('load_bindmng'),		//解绑/绑定健康检查
                isAddMember: powerService.isPower('loadmember_add'),	//添加成员
                isEditMember: powerService.isPower('loadmember_edit'),	//边界成员
                isDeleteMember: powerService.isPower('loadmember_delete')	//删除成员
            };
        });
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

        /**
         * 刷新界面zhuang
         */
        $scope.$watch("item", function (newVal, oldVal) {
            if (newVal !== oldVal) {
                if ("ACTIVE" != $scope.item.poolStatus && "ERROR" != $scope.item.poolStatus) {
                    $timeout($scope.init, 5000);
                }
            }
        });
        /*刷新状态的方法*/
        $scope.refresh = function () {
            var keepgoing = true;
            var status = $scope.item.poolStatus.toString().toLowerCase();
            if ('active' != status && 'error' != status && keepgoing) {
                $scope.init();
                keepgoing = false;
            }
        };
        /*更改最大连接数*/
        $scope.changeConnectionLimit = function (item) {
            var result = eayunModal.open({
                title: '负载均衡续费',
                backdrop: 'static',
                templateUrl: 'views/net/loadbalance/pool/changeconnlimit.html',
                controller: 'ChangeConnectionLimitCtrl',
                controllerAs: 'changeConn',
                resolve: {
                    item: function () {
                        return item;
                    }
                }
            });
            result.result.then(function () {
                $state.go('buy.lbbuyconfirm', {source: 'change_pool_detail', poolId: item.poolId});
            }, function () {
            });
        };

        $scope.init = function () {
            if (!$scope.checkUser()) {
                return;
            }
            eayunHttp.post('cloud/loadbalance/member/getMemberList.do', {poolId: $stateParams.poolId,checkRole:$scope.checkRole}).then(function (respose) {
                $scope.members = respose.data.data;
            });
            eayunHttp.post('cloud/loadbalance/pool/getLoadBalanceById.do', $stateParams.poolId).then(function (respose) {
                $scope.item = respose.data.data;
                $scope.item.connectionLimitOld = $scope.item.connectionLimit;
                $scope.getPoolStatus($scope.item);
            });
            $scope.checkEditBtn = true;
            eayunHttp.post('tag/getResourceTagForShowcase.do', {
                resType: 'ldPool',
                resId: $stateParams.poolId
            }).then(function (response) {
                $scope.resourceTags = response.data;
            });
        };
        /**
         * 修改负载均衡器
         */
        $scope.editPool = function (item) {
            var result = eayunModal.dialog({
                showBtn: false,
                title: '编辑负载均衡器',
                width: '600px',
                templateUrl: 'views/net/loadbalance/pool/editpool.html',
                controller: 'editPoolCtrl',
                resolve: {
                    item: function () {
                        return item;
                    }
                }
            });

            result.then(function (value) {
                $scope.init();
            }, function () {
                $scope.init();
            });
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

        $scope.netJson = function (tagsStr) {
            var json = {};
            if (tagsStr) {
                json = JSON.parse(tagsStr);
            }
            return json;
        };

        /**
         * 资源池状态 显示
         */
        $scope.getPoolStatus = function (model) {
            $scope.poolStatusClass = '';
            if (model.poolStatus && model.poolStatus == 'ACTIVE' && model.chargeState == '0') {
                $scope.poolStatusClass = 'green';
            }
            else if (model.poolStatus == 'ERROR' || model.chargeState != '0') {
                $scope.poolStatusClass = 'gray';
            }
            else if ((model.poolStatus == 'PENDING_CREATE'
                || model.poolStatus == 'PENDING_UPDATE'
                || model.poolStatus == 'PENDING_DELETE')
                && model.chargeState == '0') {
                $scope.poolStatusClass = 'yellow';
            }
        };

        $scope.getMemberStatus = function (_member) {
            if (_member.memberStatus == 'ACTIVE') {
                return 'green';
            } else if (_member.memberStatus == 'ERROR') {
                return 'gray';
            } else {
                return 'yellow';
            }
        };
        /*刷新成员列表*/
        $scope.refreshMember = function () {
            var keepgoing = true;
            angular.forEach($scope.members, function (value, key) {
                var status = value.memberStatus.toString().toLowerCase();
                if ('active' != status && 'error' != status && keepgoing) {
                	eayunHttp.post('cloud/loadbalance/member/getMemberList.do', {poolId: $stateParams.poolId,checkRole:$scope.checkRole}).then(function (respose) {
                        $scope.members = respose.data.data;
                    });
                    keepgoing = false;
                }
            });
        };
        $scope.checkRole="";
        $scope.roleList = [
                 		{value: '', text: '角色(全部)'},
                 		{value: 'Active', text: '主节点'},//如不指定，默认选中第一项
                 		{value: 'ActiveIsUndertaker', text: '主节点（流量承担者）'},
                 		{value: 'Backup', text: '从节点'},
                 		{value: 'BackupIsUndertaker', text: '从节点（流量承担者）'},
                 		];
                 		$scope.selectRole = function (item, event) {
                 			$scope.checkRole=item.value;
                 			$scope.init();
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

        /**
         * 健康检查
         */
        $scope.bindMonitor = function (item) {
            var result = eayunModal.open({
                showBtn: false,
                title: '健康检查',
                backdrop: 'static',
                templateUrl: 'views/net/loadbalance/pool/bindhealthmonitor.html',
                controller: 'bindHealthMonitorCtrl',
                resolve: {
                    item: function () {
                        return item;
                    }
                }
            });
            result.result.then(function (value) {
                $scope.init();
            }, function () {
                $scope.init();
            });
        };
        $scope.unBindMonitor=function(item){
        	eayunModal.confirm('确定解除健康检查关联?').then(function () {
        		var data={
        				prjId:item.prjId,
        				dcId:item.dcId,
        				poolId:item.poolId,
        		}
                eayunHttp.post('cloud/loadbalance/healthmonitor/unBindHealthMonitor.do', data).then(function (response) {
                    if (response && response.data && response.data.respCode == "400000") {
                        toast.success('解除健康检查成功');
                    }
                    $scope.init();
                });
            });
        };
        /**
         * 添加成员
         */
        $scope.addMember = function (item) {
            var result = eayunModal.open({
                title: '添加成员',
                backdrop: 'static',
                templateUrl: 'views/net/loadbalance/member/addmember.html',
                controller: 'AddMemeberController',
                resolve: {
                    item: function () {
                        return item;
                    }
                }
            });
            result.result.then(function (value) {
                $scope.init();
            }, function () {
                $scope.init();
            });
        };
        
        $scope.gotoMonitor=function(item){
        	if("1"==item.mode){
                $state.go('app.monitor.detail.ldpoolmaster', {poolId: item.poolId});
            }else{
                $state.go('app.monitor.detail.ldpoolcommon', {poolId: item.poolId});
            }
        }

        /**
         * 修改成员信息
         */
        $scope.updateMember = function (member,pool) {
            var result = eayunModal.open({
                showBtn: false,
                title: '编辑成员',
                backdrop: 'static',
                templateUrl: 'views/net/loadbalance/member/editmember.html',
                controller: 'EditMemeberController',
                resolve: {
                	member: function () {
                        return member;
                    },
                    pool:function(){
                    	return pool;
                    }
                }
            });
            result.result.then(function (value) {
                $scope.init();
            }, function () {
                $scope.init();
            });

        };

        /**
         * 删除成员
         */
        $scope.deleteMember = function (item) {
            eayunModal.confirm('确定删除成员' + item.vmName + '(' + item.memberAddress + ':' + item.protocolPort + ')?').then(function () {
                eayunHttp.post('cloud/loadbalance/member/deleteMember.do', item).then(function (response) {
                    if (response && response.data && response.data.respCode == "100000") {
                        toast.success('删除成员成功');
                    }
                    $scope.init();
                });
            });
        };


        $scope.init();
    })
    /**
     * 健康检查
     */
    .controller('bindHealthMonitorCtrl', function ($scope, eayunModal, eayunHttp, item, toast, $modalInstance) {
        /**
         * 初始化数据
         */
    	$scope.checkRadio="";
        $scope.init = function () {
            eayunHttp.post('cloud/loadbalance/healthmonitor/getMonitorListByPool.do', {
                poolId: item.poolId,
                prjId: item.prjId
            }).then(function (response) {
                $scope.monitors = response.data.data;
            });
        };
        $scope.changeRadio=function(ldmId){
        	$scope.checkMonitor=true;
        	$scope.checkRadio=ldmId;
        }
//        /**
//         * checkbox 选择数据
//         */
//        $scope.selectData = function () {
//            var data = [$scope.checkRadio];
//            return data;
//        };

        /**
         * 确定
         */
        $scope.commit = function () {
            $scope.checkBtn = true;
//            var monitors = $scope.selectData();
//            if (monitors && monitors.length > 10) {
//                eayunModal.warning('最多只能关联10条健康检查');
//                $scope.checkBtn = false;
//                return;
//            }
            var data = {
                dcId: item.dcId,
                prjId: item.prjId,
                poolId: item.poolId,
                poolName: item.poolName,
                monitors: [$scope.checkRadio]
            };
            eayunHttp.post('cloud/loadbalance/healthmonitor/bindHealthMonitor.do', data).then(function (response) {
                if (response && response.data && response.data.respCode == '400000') {
                    toast.success('绑定健康检查成功', 2000);
                    $modalInstance.close();
                }
                else {
                    $scope.checkBtn = false;
                }
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };

        $scope.init();
    })
    /**
     * 绑定公网IP
     */
    .controller('BindFloatIpController', function ($scope, eayunModal, eayunHttp, item, toast, $modalInstance, DatacenterService) {
        $scope.init = function () {
            eayunHttp.post('cloud/floatip/getUnBindFloatIp.do', {prjId: item.prjId}).then(function (response) {
                $scope.floatIps = response.data;
            });
        };

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
         * 保存
         */
        $scope.commit = function () {
            $scope.checked = true;
            $scope.data = {
                'dcId': item.dcId,
                'prjId': item.prjId,
                'floId': $scope.model.floatIp.floId,
                'floIp': $scope.model.floatIp.floIp,
                'resourceId': item.poolId,
                'resourceType': 'lb',
                'portId': item.portId
            };

            eayunHttp.post('cloud/floatip/bindResource.do', $scope.data).then(function (response) {
                if (response && response.data && response.data.respCode == '400000') {
                    toast.success('绑定公网IP:' + DatacenterService.toastEllipsis($scope.model.floatIp.floIp, 7) + '成功', 2000);
                    $modalInstance.close();
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

    /**
     * 添加成员
     */
    .controller('AddMemeberController', function ($scope, eayunModal, eayunHttp, item, toast, $modalInstance) {
    	$scope.pool = angular.copy(item);
        $scope.subnet = {};
        if (item.subnetName && item.subnetIp) {
            $scope.subnet.subnetName = item.subnetName;
            $scope.subnet.cidr = item.subnetCidr;
        }
        /**
         * 初始化成员列表
         */
        $scope.init = function () {
            eayunHttp.post('cloud/loadbalance/member/getMemeberListBySubnet.do', {subnetId: item.subnetId}).then(function (response) {
                $scope.members = response.data.data;
                angular.forEach($scope.members,function(value){
                    value.role='Active';
                    value.priority=null;
                });
            });
        };
        $scope.changeRole = function (item) {
            if (item.isCheck&&(item.role=='Active'||!item.role)) {
                item.isEdit = true;
                item.priority=null;
                item.memberWeight = 10;
            }else if(item.isCheck&&item.role=='Backup'){
                item.isEdit = true;
                item.memberWeight = null;
                item.priority=10;
            }
            else {
                item.isEdit = false;
                item.protocolPort = null;
                item.memberWeight = null;
                item.isPortExsit = false;
                item.isPortError = false;
                item.priority=null;
            }
            item.isWeightError = null;
            item.isPriorityError = null;
            $scope.checkWeight(item);
            $scope.checkPriority(item);
            $scope.checkPort(item);
            $scope.checkSelected();
        };
        /**
         * 校验选择项
         */
        $scope.changeCheckMember = function (item) {
            if (item.isCheck&&(item.role=='Active'||!item.role)) {
            	item.isRadio=true;
                item.isEdit = true;
                item.protocolPort = 80;
                item.memberWeight = 10;
                item.priority=null;
            }else if(item.isCheck&&item.role=='Backup'){
            	item.isRadio=true;
                item.isEdit = true;
                item.protocolPort = 80;
                item.memberWeight = null;
                item.priority=10;
            }
            else {
            	item.isRadio=false;
                item.isEdit = false;
                item.protocolPort = null;
                item.memberWeight = null;
                item.priority=null;
                item.isPortExsit = false;
                item.isPortError = false;
                item.role=='Active';
                item.priority=null;
            }
            item.isWeightError = null;
            item.isPriorityError = null;
            $scope.checkWeight(item);
            $scope.checkPriority(item);
            $scope.checkPort(item);
            $scope.checkSelected();
        };

        /**
         * 校验端口
         */
        $scope.checkPort = function (member) {
            member.isPortError = true;
            $scope.showErrMsg = null;
            var regex = /^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]{1}|6553[0-5])$/;
            if (member.isCheck) {
                if (member.protocolPort && regex.test(member.protocolPort)) {
                    member.isPortError = false;
                    var data = {
                        poolId: item.poolId,
                        memberAddress: member.memberAddress,
                        protocolPort: member.protocolPort,
                        vmId: member.vmId
                    };
                    eayunHttp.post('cloud/loadbalance/member/checkMemberExsit.do', data).then(function (response) {
                        if (response && response.data && response.data.data == true) {
                            member.isPortExsit = false;
                        }
                        else {
                            $scope.showErrMsg = '监听端口重复';
                            member.isPortExsit = true;
                            $scope.isErr = true;
                        }
                        $scope.checkError();
                    });
                }
                else {
                    $scope.isErr = true;
                    $scope.showErrMsg = '请输入1-65535的正整数！';
                }
            } else {
                member.isPortError = false;
            }
            $scope.checkError();
        };

        /**
         * 校验权重
         */
        $scope.checkWeight = function (item) {
            if(item.role=='Active'||!item.role){
                item.isWeightError = true;
                $scope.showErrMsg = null;
                var regex = /^([1-9][0-9]{0,1}|100)$/;
                if (item.isCheck) {
                    if (item.memberWeight && regex.test(item.memberWeight)) {
                        item.isWeightError = false;
                        $scope.isErr = false;
                    }
                    else {
                        $scope.showErrMsg = '请输入1-100的正整数';
                        $scope.isErr = true;
                    }
                }
                else {
                    item.isWeightError = false;
                    $scope.isErr = false;
                }
                $scope.checkError();
            }

        };
        /**
         * 校验优先级
         */
        $scope.checkPriority = function (item) {
            if(item.role=='Backup'){
                item.isPriorityError = true;
                $scope.showErrMsg = null;
                var regex = /^(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5]))))$/;
                if (item.isCheck) {
                    if (item.priority && regex.test(item.priority)) {
                        item.isPriorityError = false;
                        $scope.isErr = false;
                    }
                    else {
                        $scope.showErrMsg = '请输入0-255的整数';
                        $scope.isErr = true;
                    }
                }
                else {
                    item.isPriorityError = false;
                    $scope.isErr = false;
                }
                $scope.checkError();
            }

        };

        /**
         * 查询是否存在的选择项
         */
        $scope.checkSelected = function () {
            $scope.isSelected = false;
            for (var i = 0; i < $scope.members.length; i++) {
                if ($scope.members[i].isCheck) {
                    $scope.isSelected = true;
                    break;
                }
            }
        };

        /**
         * 校验是否出错
         */
        $scope.checkError = function () {
            $scope.isTabErr = false;
            for (var i = 0; i < $scope.members.length; i++) {
                if ($scope.members[i].isCheck) {
                    if ($scope.members[i].isWeightError
                    	|| $scope.members[i].isPriorityError
                        || $scope.members[i].isPortExsit
                        || $scope.members[i].isPortError) {
                        $scope.isTabErr = true;
                        break;
                    }
                }
            }
        };

        /**
         * 获取选择的数据
         */
        $scope.selectData = function () {
            var data = [];
            var index = 0;
            for (var i = 0; i < $scope.members.length; i++) {
                var mem = $scope.members[i];
                if (mem.isCheck) {
                    data[index++] = {
                        memberAddress: mem.memberAddress,
                        memberWeight: mem.memberWeight,
                        protocolPort: mem.protocolPort,
                        vmId: mem.vmId,
                        role:mem.role,
                        priority:mem.priority
                    };
                }
            }
            return data;
        };

        /**
         * 确定
         */
        $scope.commit = function () {
            $scope.checkBtn = true;
            var data = {
                dcId: item.dcId,
                prjId: item.prjId,
                poolId: item.poolId,
                poolName: item.poolName,
                members: $scope.selectData(),
                mode:item.mode
            };

            eayunHttp.post('cloud/loadbalance/member/addMember.do', data).then(function (response) {
                if (response && response.data && response.data.respCode == '000000') {
                    toast.success('添加成员成功', 2000);
                    $modalInstance.close();
                }
                else {
                    $scope.checkBtn = false;
                }
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };

        $scope.init();
    })
    /**
     * 修改成员
     */
    .controller('EditMemeberController', function ($scope, eayunModal, eayunHttp, member,pool, toast, $modalInstance) {
        $scope.checkExist = true;
        $scope.model = angular.copy(member);
        $scope.pool = angular.copy(pool);
        /**
         * 校验端口是否重复
         */
        $scope.checkPort = function () {
            if ($scope.model) {
                var wport = $scope.model;
                eayunHttp.post('cloud/loadbalance/member/checkMemberExsit.do', $scope.model).then(function (response) {
                    if (response && response.data) {
                        $scope.checkExist = response.data.data;
                    }
                });
            }
        };

        /**
         * 确定
         */
        $scope.commit = function () {
            $scope.checkBtn = true;

            eayunHttp.post('cloud/loadbalance/member/updateMember.do', $scope.model).then(function (response) {
                if (response && response.data && response.data.respCode == '200000') {
                    toast.success('成员' + member.memberAddress + ':' + member.protocolPort + '修改成功', 2000);
                    $modalInstance.close();
                }
                else {
                    $scope.checkBtn = false;
                }
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };

    })
;
