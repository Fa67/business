'use strict'

angular.module('eayunApp.service').service('OrderService',
        [ '$http', 'eayunModal', function($http, eayunModal) {
            var orderService = {};
            orderService.payOrder = function(ordersNo) {
                return $http.post('order/payorder.do', {
                    ordersNo : ordersNo
                }, {
                    $showLoading : true
                }).then(function(response) {
                    return response.data;
                });
            };

            orderService.detail = function(orderId) {
                return $http.post('order/getorderbyid.do', {
                    orderId : orderId
                }).then(function(response) {
                    return response.data;
                });
            };

            orderService.cancelOrder = function(orderId) {
                return $http.post('order/cancelorder.do', {
                    orderId : orderId
                }).then(function(response) {
                    return response.data;
                });
            };
            
            orderService.getResourceRouter = function(resourceType){
                var router ;
                switch (resourceType){
                    case "0":
                        router = "buy.host";
                        break;
                    case "1":
                        router = "buy.volume";
                        break;
                    case "2":
                        router = "buy.snapshot";
                        break;
                    case "3":
                        router = "buy.vpcBuy";
                        break;
                    case "4":
                        router = "buy.lbbuy";
                        break;
                    case "5":
                        router = "buy.buyFloatIp";
                        break;
                    case "6":
                        router = "app.obs.openservice";
                        break;
                    case "7":
                        router = "buy.vpnbuy";
                        break;
                    default:
                        router = "app.overview";
                }
                return router;
            }
            
            orderService.checkOrderState = function(ordersNo){
                return $http.post('order/checkorderstate.do', {
                    ordersNo : ordersNo
                }).then(function(response) {
                    return response.data;
                });
            }

            return {
                payOrder : orderService.payOrder,
                detail : orderService.detail,
                cancelOrder : orderService.cancelOrder,
                getResourceRouter : orderService.getResourceRouter,
                checkOrderState : orderService.checkOrderState
            }
        } ]);