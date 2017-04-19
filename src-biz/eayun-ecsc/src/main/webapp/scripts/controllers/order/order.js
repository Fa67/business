'use strict';
angular
        .module('eayunApp.controllers')

        .config(function($stateProvider, $urlRouterProvider) {
            $urlRouterProvider.when('/app/order', '/app/order/list/');
            $stateProvider.state('app.order.list', {
                url : '/list/:orderState',
                templateUrl : 'views/order/list.html',
                controller : 'OrderListCtrl',
                controllerAs : 'orderMng'
            }).state('app.order.detail', {
                url : '/detail/:orderId',
                templateUrl : 'views/order/detail.html',
                controller : 'DetailCtrl',
                controllerAs : 'detailMng'
            }).state('pay.order', {
                url : '/order/:ordersNo',
                templateUrl : 'views/order/orderpay.html',
                controller : 'OrderPayCtrl',
                controllerAs : 'orderPay'
            })
        })
        
        .controller('OrderCtrl', [ '$scope', 'eayunStorage', function($scope, eayunStorage) {
            $scope.orderNavLists = [];
            eayunStorage.set("orderNavLists", $scope.orderNavLists);
        } ])

        .controller(
                'OrderListCtrl',
                [
                        '$scope',
                        '$state',
                        'OrderService',
                        'eayunModal',
                        'toast',
                        '$stateParams',
                        'eayunStorage',
                        'powerService',
                        'RDSInstanceService',
                        function($scope, $state, OrderService, eayunModal,
                                toast, $stateParams, eayunStorage, powerService, RDSInstanceService) {
                            var that = this;
                            that.selected = [];
                            that.checkAll = false;
                            
                            var orderNavLists = eayunStorage.get('orderNavLists');
                            orderNavLists.length = 0;
                            orderNavLists.push({route:'app.order',name:'订单管理'});
                            
                            powerService.powerRoutesList().then(function(powerList){
                                that.permissions = {
                                        hasListPower : powerService.isPower('order_list'),  //查看
                                        hasReorderPower : powerService.isPower('order_reorder'),   //重新下单
                                        hasCancelPower : powerService.isPower('order_cancel'),   //取消订单
                                        hasPayPower : powerService.isPower('order_pay'),   //支付订单
                                        hasResourcePower : powerService.isPower('order_resource')  //查看资源
                                        
                                };
                            });

                            var firstDate = new Date();
                            firstDate.setDate(1); //第一天
                            firstDate.setHours(0, 0, 0, 0);
                            that.startTime = firstDate;
                            var end=new Date();
                            end.setHours(0, 0, 0, 0);
                            that.endTime = end;
                            that.maxTime=end;
                            
                            that.orderTypes = [ {
                                value : "",
                                text : '类型(全部)',
                                $$select : true
                            }, {
                                value : "0",
                                text : '新购'
                            }, {
                                value : "1",
                                text : '续费'
                            }, {
                                value : "2",
                                text : '升级'
                            } ];

                            that.typeSelected = function(item, event) {
                                that.orderType = item.value;
                                that.orderTable.api.draw();
                            };

                            that.stateTypes = [ {
                                value : "",
                                text : '状态(全部)',
                                $$select : true
                            }, {
                                value : "1",
                                text : '待支付'
                            }, {
                                value : "2",
                                text : '处理中'
                            }, {
                                value : "3",
                                text : '处理失败-已取消'
                            }, {
                                value : "4",
                                text : '已完成'
                            }, {
                                value : "5",
                                text : '已取消'
                            } ];

                            that.stateSelected = function(item, event) {
                                that.orderState = item.value;
                                that.orderTable.api.draw();
                            };

                            if($stateParams.orderState){
                                that.orderState =  $stateParams.orderState;
                                that.stateTypes.forEach(function(stateType){
                                    stateType.$$select = false;
                                    if(stateType.value == that.orderState){
                                        stateType.$$select = true;
                                    }
                                })
                            }
                            
                            that.orderTable = {
                                source : 'order/getorderlist.do',
                                api : {},
                                getParams : function() {
                                    that.selected = [];
                                    that.checkAll = false;
                                    return {
                                        startTime : that.startTime ? that.startTime
                                                .getTime()
                                                : '',
                                        endTime : that.endTime ? that.endTime
                                                .getTime() : '',
                                        orderType : that.orderType ? that.orderType
                                                : "",
                                        prodName : that.prodName,
                                        orderState : that.orderState ? that.orderState
                                                : "",
                                        orderNo : that.orderNo
                                    };
                                }
                            };

                            that.query = function() {
                                that.orderTable.api.draw();
                            };

                            // 复选框选择
                            var updateSelected = function(action, orderNo) {
                                if (action == 'add'
                                        && that.selected.indexOf(orderNo) == -1) {
                                    that.selected.push(orderNo);
                                }
                                if (action == 'remove'
                                        && that.selected.indexOf(orderNo) != -1) {
                                    var idx = that.selected.indexOf(orderNo);
                                    that.selected.splice(idx, 1);
                                }
                            }

                            that.updateSelection = function($event, orderNo) {
                                var checkbox = $event.target;
                                var action = (checkbox.checked ? 'add'
                                        : 'remove');
                                updateSelected(action, orderNo);
                            }

                            that.isSelected = function(orderNo) {
                                return that.selected.indexOf(orderNo) >= 0;
                            }
                            
                            //全选or非全选
                            that.checkOrUncheckAll = function($event){
                                if($event.target.checked){
                                    that.checkAll = true;
                                   var orders = that.orderTable.result;
                                   if(orders){
                                       that.selected = [];
                                       orders.forEach(function(order){
                                           if(order.orderState == '1'){
                                               that.selected.push(order.orderNo);
//                                               console.info(document.getElementById("checkbox_"+order.orderNo));
                                           }
                                       })
                                   }
                                }else{
                                    that.checkAll = false;
                                    that.selected = [];
                                }
                            }

                            // 立即支付（单笔）
                            that.payOneOrder = function(orderNo) {
                                var ordersNo = new Array();
                                ordersNo.push(orderNo);
                                that.orderPay(ordersNo);
                            }

                            // 合并支付
                            that.payOrders = function() {
                                that.orderPay(that.selected);
                            }

                            // 立即支付
                            that.orderPay = function(ordersNo) {
                                var orderPayNavList = [{route:'app.order.list',name:'订单管理'}];
                                eayunStorage.persist("orderPayNavList",orderPayNavList);
                                eayunStorage.persist("payOrdersNo",ordersNo);
                                $state.go('pay.order');
                            }

                            // 查看详情
                            that.detail = function(orderId) {
                                $state.go('app.order.detail', {
                                    orderId : orderId
                                });
                            }

                            // 取消订单
                            that.cancelOrder = function(orderId) {
                                eayunModal.confirm("确定取消订单？").then(function () {
                                    OrderService.cancelOrder(orderId).then(
                                            function(response) {
                                                if (response.respCode == '000000') {
                                                    toast.success("取消订单成功");
                                                } else {
                                                    eayunModal.error(response.message);
                                                }
                                                that.orderTable.api.draw();
                                      });
                                })
                            };
                            
                            // 重新下单
                            that.reorder = function(resourceType, orderNo){
                                if(resourceType == '8'){
                                    RDSInstanceService.getInstanceByOrderNo(orderNo).then(function (response) {
                                        if (response.data.isMaster == '1'){
                                            $state.go('buy.buyinstance', {orderNo : orderNo});
                                        }else{
                                            // 判断从库隶属的主库是否存在
                                            RDSInstanceService.getInstanceById(response.data.masterId).then(function (resp) {
                                                if(resp.rdsStatus == 'ACTIVE'){
                                                    $state.go('buy.buyslaveinstance', {orderNo : orderNo,rdsId : resp.rdsId});
                                                }else{
                                                    eayunModal.warning('MySQL主库实例'+ resp.rdsName+'非运行中，请稍后重试！');
                                                }
                                            }, function () {
                                            }, function () {
                                                eayunModal.warning('MySQL主库实例已被删除，请选择其他实例！');
                                            });
                                        }
                                    });
                                }else{
                                    var router = OrderService.getResourceRouter(resourceType);
                                    $state.go(router, {orderNo : orderNo});
                                }
                            }
                        } ])
        .controller(
                "OrderPayCtrl",
                [
                        '$scope',
                        '$state',
                        '$stateParams',
                        'OrderService',
                        'eayunModal',
                        'eayunStorage',
                        '$http',
                        function($scope, $state, $stateParams, OrderService, eayunModal, eayunStorage, $http) {
                            var that = this;
                            var ordersNo;
                            that.disablePayBtn = true;
                            
                            that.orderPayNavList = eayunStorage.persist("orderPayNavList");
                            
                            if($stateParams.ordersNo){
                                ordersNo = $stateParams.ordersNo;
                            }else{
                                ordersNo = eayunStorage.persist("payOrdersNo").toString();
                            }
                            if(ordersNo.indexOf(",")<0){
                                that.singleOrderNo = ordersNo;
                            }
                            
                            OrderService.payOrder(ordersNo).then(
                                    function(response) {
                                        if(response.code == "010120"){
                                            eayunModal.error(response.message);
                                            that.disablePayBtn = true;
                                            return;
                                        }
                                        that.payInfo = response;
                                        if(that.payInfo != null){
                                            var ordersNo = new Array();
                                            that.payInfo.orders.forEach(function(
                                                    order) {
                                                ordersNo.push(order.orderNo);
                                            })
                                            that.ordersNo = ordersNo.toString();
                                            that.disablePayBtn = false;
                                        }
                                    })

                            that.showModal = function() {
                                var result = eayunModal.open({
                                    templateUrl : 'views/pay/confirm.html',
                                    controller : 'ConfirmPaidCtrl',
                                    backdrop : 'static',
                                    resolve : {
                                        backUrl : function() {
                                            return "app.order";
                                        }
                                    }
                                }).result;
                            };
                            
                            that.formatProdConfig = function(configStr){
                                if(configStr){
                                    var returnStr = "";
                                    var lines = configStr.split("<br>");
                                    for(var i = 0; i<lines.length; i++){
                                        returnStr = returnStr + '<p class="ey-ellipsis ey-margin-top" title="'+lines[i]+'">' + lines[i] +'</p>';
                                    }
                                    return returnStr;
                                }
                                
                            }
                        } ])
        .controller(
                "DetailCtrl",
                [
                        '$scope',
                        '$state',
                        '$stateParams',
                        'OrderService',
                        'eayunModal',
                        'eayunStorage',
                        'powerService',
                        'RDSInstanceService',
                        'toast',
                        function($scope, $state, $stateParams, OrderService,
                                eayunModal, eayunStorage, powerService,RDSInstanceService, toast) {
                            var that = this;
                            that.thirdPartType = 0;
                            
                            var orderNavLists = eayunStorage.get('orderNavLists');
                            orderNavLists.length = 0;
                            orderNavLists.push({route:'app.order',name:'订单管理'});
                            orderNavLists.push({route:'app.order.detail({orderId:"'+$stateParams.orderId+'"})',name:'订单详情'});
                            
                            powerService.powerRoutesList().then(function(powerList){
                            that.permissions = {
                                    hasListPower : powerService.isPower('order_list'),  //查看
                                    hasReorderPower : powerService.isPower('order_reorder'),   //重新下单
                                    hasCancelPower : powerService.isPower('order_cancel'),   //取消订单
                                    hasPayPower : powerService.isPower('order_pay'),   //支付订单
                                    hasResourcePower : powerService.isPower('order_resource')  //查看资源
                                    
                            };
                            });
                            
                            OrderService.detail($stateParams.orderId).then(
                                    function(response) {
                                        if(response.code == "010120"){
                                            eayunModal.error(response.message);
                                            return;
                                        }
                                        that.order = response.data;
                                    });

                            // 立即支付
                            that.orderPay = function(orderNo) {
                                var orderPayNavList = [
                                                       {route:"app.order.list",name:"订单管理"},
                                                       {route:"app.order.detail({orderId:'"+that.order.orderId+"'})",name: "订单详情"}
                                ];
                                eayunStorage.persist("orderPayNavList",orderPayNavList);
                                var ordersNo = new Array();
                                ordersNo.push(orderNo);
                                eayunStorage.persist("payOrdersNo",ordersNo);
                                $state.go('pay.order');
                            }

                            // 取消订单
                            that.cancelOrder = function(orderId) {
                                eayunModal.confirm("确定取消订单？").then(function () {
                                    OrderService
                                            .cancelOrder(orderId)
                                            .then(
                                                    function(response) {
                                                        if (response.respCode == '000000') {
                                                            toast.success("取消订单成功");
                                                        } else {
                                                            eayunModal.error(response.message);
                                                        }
                                                        OrderService.detail(orderId).then(function(response) {
                                                            that.order = response.data;
                                                        });
                                                    })
                                })
                            }

                            that.orderResource = function(orderNo) {
                                var result = eayunModal.open({
                                    showBtn : false,
                                    templateUrl : 'views/order/resource.html',
                                    resolve : {
                                        orderNo : function(){
                                            return orderNo;
                                        }
                                    },
                                    backdrop : "static",
                                    controller : 'OrderResourceCtrl',
                                    controllerAs : 'resource'
                                }).result;
                            }
                            
                            // 重新下单
                            that.reorder = function(resourceType, orderNo){
                                if(resourceType == '8'){
                                    RDSInstanceService.getInstanceByOrderNo(orderNo).then(function (response) {
                                        if (response.data.isMaster == '1'){
                                            $state.go('buy.buyinstance', {orderNo : orderNo});
                                        }else{
                                            // 判断从库隶属的主库是否存在
                                            RDSInstanceService.getInstanceById(response.data.masterId).then(function (resp) {
                                                if(resp.rdsStatus == 'ACTIVE'){
                                                    $state.go('buy.buyslaveinstance', {orderNo : orderNo,rdsId : resp.rdsId});
                                                }else{
                                                    eayunModal.warning('MySQL主库实例'+ resp.rdsName+'非运行中，请稍后重试！');
                                                }
                                            }, function () {
                                            }, function () {
                                                eayunModal.warning('MySQL主库实例已被删除，请选择其他实例！');
                                            });
                                        }
                                    });
                                }else{
                                    var router = OrderService.getResourceRouter(resourceType);
                                    $state.go(router, {orderNo : orderNo});
                                }
                            }
                            
                            that.formatProdConfig = function(configStr){
                                if(configStr){
                                    var returnStr = "";
                                    var lines = configStr.split("<br>");
                                    for(var i = 0; i<lines.length; i++){
                                        returnStr = returnStr + '<p class="ey-ellipsis ey-margin-top" title="'+lines[i]+'">' + lines[i] +'</p>';
                                    }
                                    return returnStr;
                                }
                                
                            }
                        } ]).controller(
                "OrderResourceCtrl",
                [
                        '$scope',
                        '$state',
                        '$stateParams',
                        'OrderService',
                        'eayunModal',
                        '$modalInstance',
                        'orderNo',
                        function($scope, $state, $stateParams, OrderService,
                                eayunModal, $modalInstance, orderNo) {
                            var that = this;
                            
                            that.cancel = function(){
                                $modalInstance.dismiss();
                            }
                            that.ok = function(){
                                $modalInstance.close();
                            }

                            that.resourceTable = {
                                source : 'order/getorderresource.do',
                                api : {},
                                getParams : function() {
                                    return {
                                        orderNo : orderNo
                                    };
                                }
                            };
//                            that.resourceList = resourceList;
                        } ])
