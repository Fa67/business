'use strict';

/**
 * @ngdoc function
 * @name eayunApp.controller:OverviewCtrl
 * @description
 * # OverviewCtrl
 * 总览
 */
angular.module('eayunApp.controllers').controller('OverviewCtrl', function ($scope,toast,$state,eayunModal, eayunHttp ,noticeList,cloudprojectList,$window,powerService,eayunStorage) {
	//权限控制
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.buttonPower = {
				  costcenter : powerService.isPower('account_view'),//费用中心（充值）
				  LogView : powerService.isPower('log_view'),		//查看日志
				  orderList : powerService.isPower('order_list'),		//查看订单
				  orderPay : powerService.isPower('order_pay'),		//支付订单
				  orderCancel : powerService.isPower('order_cancel')		//取消订单
		 };
	  });
	$scope.model = {};
	$scope.noticeList = noticeList;
	$scope.threeDay = new Date().getTime() - 3*24*60*60*1000;;
	/**打开公告弹出框*/
	$scope.openNoticePage = function(){
		var result = eayunModal.open({
			templateUrl: 'views/notice/noticemng.html',
			controller: 'noticeMngCtrl',
			resolve: {
				list: function () {
					return $scope.noticeList;
				}
			}
		});
	};
	$scope.removeThisNotice = function(thisNotice){
		angular.forEach($scope.noticeList, function (value,key) {
			  if(value.id == thisNotice.id){
				  $scope.noticeList.splice(key,1);
				  return false;
			  }
		});
		$scope.noticefield.start();
	};
	/**公告详情*/
	$scope.openNoticeDetail = function(data){
		$scope.notice = data;
		eayunHttp.post('sys/notice/getNoticeDetail.do',{noticeId:data.id}).then(function(response){
			if(response.data.notice == undefined){
				toast.error("公告已删除");
				$scope.removeThisNotice(data);
				return;
			}else {
				var _notice = response.data.notice;
				if (_notice && _notice.invalidTime <= new Date()) {
					toast.error("公告已过期");
					$scope.removeThisNotice(_notice);
					return;
				} else if (_notice && _notice.isUsed == '0') {
					toast.error("公告已停用");
					$scope.removeThisNotice(_notice);
					return;
				} else {
					var result = eayunModal.open({
						backdrop:'static',
						templateUrl: 'views/notice/noticedetail.html',
						controller: 'noticeDetailCtrl',
						resolve: {
							model: function () {
								return _notice;
							}
						}
					}).result;
					result.then(function () {
					}, function () {
					});
				}
			}
		});
	};
	  $scope.cloudprojectList = cloudprojectList;
	  var daPrj = sessionStorage["dcPrj"];
	  if(daPrj){
		  daPrj = JSON.parse(daPrj);
		  angular.forEach($scope.cloudprojectList, function (value,key) {
			  if(value.projectId == daPrj.projectId)
				  $scope.model.dcProject = value;
		  });
	  }else{
		  angular.forEach($scope.cloudprojectList, function (value) {
				if(value.projectId!=null&&value.projectId!=''&&value.projectId!='null'){
					$scope.model.dcProject = value;
					$window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.dcProject);
					return false;
				}
			});
	  }
	  $scope.$watch('model.dcProject.projectId' , function(newVal,oldVal){
			if(newVal !== oldVal){
				$scope.toExpireTable.api.draw();
				$scope.getUnhandle(newVal);
				if(newVal==null||newVal==''||newVal=='null'){
					$scope.dcId = $scope.model.dcProject.dcId;
	    			angular.forEach($scope.cloudprojectList, function (value) {
	    				if(oldVal == value.projectId){
	    					$scope.model.dcProject = value;
	    					return false;
	    				}
	    			});
	    			eayunHttp.post('cloud/project/findProByDcId.do',{dcId : $scope.dcId}).then(function (response){
	    	    		if(response.data){
	    	    			eayunModal.warning("您在该数据中心下没有具体资源的访问权限");
	    	    		}else{
	    	    			eayunModal.warning("您没有关联该数据中心");
	    	    		}
	    	    	});
	    		}else{
	    			$window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.dcProject);
	    		}
			}
	  });
	  /**业务运行情况*/
	  $scope.getUnhandle=function(prjId){
		  eayunHttp.post('monitor/alarm/getUnhandledAlarmMsgList.do',{"prjId":prjId}).then(function(response){
			  $scope.unhandledAlarmMsgList = response.data.unhandledAlarmMsgList;
		  });
	  };
	  if($scope.model.dcProject){
		  $scope.getUnhandle($scope.model.dcProject.projectId);
	  }



	  /**展示客户基本信息*/
	  eayunHttp.post('sys/user/getusermessage.do').then(function(response){
		  $scope.userMessage = response.data;
	  });

	  /**客户最新8条日志*/
	  $scope.querylastLogs = function(){
		  eayunHttp.post('sys/log/getlastlogs.do').then(function(response){
			  $scope.lastLogs = response.data.data;
		  });
	  }
	  $scope.querylastLogs();

	  /**账户余额*/
	  $scope.isLowZero = false;
	  $scope.queryAccount = function(){
		  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
			  $scope.account = response.data.data;
			  $scope.isLowZero = $scope.account.money < 0;
		  });
	  }
	  /**跳转充值页*/
	  $scope.toPayAccount = function(){
		  var rechargeNavList = [{route:'app.overview',name:'总览'}];
          eayunStorage.persist("rechargeNavList",rechargeNavList);
          $state.go('pay.recharge');
	  };
	  $scope.queryAccount();
	  /**待支付订单列表*/
	  $scope.toPayOrderTable = {
	      source: 'sys/overview/gettopayorderpage.do',
	      api:{},
	      getParams: function () {

	      }
	  };
	  $scope.$watch('toPayOrderTable.result' , function(newVal,oldVal){
		  if(newVal !== oldVal){
		  }
	  });
	  /**即将到期资源列表*/
	  $scope.toExpireTable = {
	      source: 'sys/overview/gettoexpireresources.do',
	      api:{},
	      getParams: function () {
	    	  return {
		        	prjId : $scope.model.dcProject.projectId
		        };
	      }
	  };
	  /**跳转订单页*/
	  $scope.toPayList = function(){
		  $state.go('app.order.list',{'orderState':'1'});
	  };
	  /**取消订单*/
	  $scope.cancelOrder = function(orderId){
		  eayunModal.confirm('确定要取消此订单吗？').then(function () {
			  eayunHttp.post('order/cancelorder.do',{'orderId':orderId}).then(function(response){
				  if(response.data.respCode == '000000'){
					  toast.success("订单取消成功");
				  }else{
					  eayunModal.error("订单取消失败");
				  }
				  $scope.toPayOrderTable.api.draw();
				  $scope.queryAccount();
				  $scope.querylastLogs();
			  });
		  }, function () {
          });
	  };
	  /**立即支付*/
	  $scope.payment = function(orderId){
		  var ordersId = new Array();
          ordersId.push(orderId);
          var orderPayNavList = [{route:'app.overview',name:'总览'}];
          eayunStorage.persist("orderPayNavList",orderPayNavList);
          eayunStorage.persist("payOrdersNo", ordersId);
          $state.go('pay.order');
	  };
  })
  .controller("noticeDetailCtrl",function($scope,toast,$state,eayunModal,eayunHttp,model,$modalInstance){
	  $scope.notice = model;
	  $scope.cancel = function (){
			$modalInstance.dismiss();
		};
	  $scope.isOverdue = function(data){
		$scope.notice = data;
		$("#url").attr("href",data.url);
	  };
  })
  /**公告弹出框*/
   .controller("noticeMngCtrl",function($scope,$modalInstance,toast,$state,eayunModal,eayunHttp,list){

	   $scope.removeNotice = function(thisNotice){
			angular.forEach($scope.noticemngList, function (value,key) {
				  if(value.id == thisNotice.id){
					  $scope.noticemngList.splice(key,1);
					  return false;
				  }
			});
		};
	   $scope.close = function(){
		   $modalInstance.dismiss();
		  };
	   if(null == list){
		   return;
	   }
	   $scope.noticemngList = list;
	   angular.forEach($scope.noticemngList, function (value,key) {
		   value.isThis = false;
		});
	   $scope.noticeModel = $scope.noticemngList[0];
	   $scope.noticemngList[0].isThis = true;
	   $scope.isOverdue = function(data){
		   $("#url").attr("href",data.url);
		  };
	  $scope.clickNotice=function(model){
		  eayunHttp.post('sys/notice/getNoticeDetail.do',{noticeId:model.id}).then(function(response){
				if(response.data.notice == undefined){
					toast.error("公告已删除");
					$scope.removeNotice(model);
					$scope.noticeModel = $scope.noticemngList[0];
				}else {
					var _notice = response.data.notice;
					if (_notice && _notice.invalidTime <= new Date()) {
						toast.error("公告已过期");
						$scope.removeNotice(_notice);
						$scope.noticeModel = $scope.noticemngList[0];
					} else if (_notice && _notice.isUsed == '0') {
						toast.error("公告已停用");
						$scope.removeNotice(_notice);
						$scope.noticeModel = $scope.noticemngList[0];
					} else {
						$scope.noticeModel = model;
					}
				}
				angular.forEach($scope.noticemngList, function (value,key) {
					if(value.id==$scope.noticeModel.id){
						value.isThis = true;
					}else{
						value.isThis = false;
					}
				});
			});
	  };

   });
