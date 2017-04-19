/**
 * Created by eayun on 2017/2/22.
 */
'use strict';

angular.module('eayunApp.controllers')

    .config(function ($stateProvider, $urlRouterProvider) {

    })

    .controller('VerifyInstanceCtrl', ['$rootScope', '$scope', '$state', 'eayunStorage', '$stateParams', 'eayunMath', 'eayunHttp', 'RDSInstanceService', 
                                    function ($rootScope, $scope, $state, eayunStorage, $stateParams, eayunMath, eayunHttp, RDSInstanceService) {
    	var vm = this;
    	vm.initial = function () {
            vm.initOperation();
        };
        vm.initOperation = function () {
            vm.isBalance = false; //默认不勾选使用余额支付
            vm.isError = false;
            vm.isWarn = false;
            vm.checkBtn = false; // 避免表单的重复提交
            vm.source = $stateParams.source;
            vm.instance = {};
            vm.instance = eayunStorage.get('order_confirm_rds');
            if (vm.instance == null) {
                if (vm.source == 'buy_instance') {//创建主库
                    $state.go("buy.buyinstance", {orderNo:'000000'}); // 跳转到购买页
                }
                if (vm.source == 'buy_slave_instance'){ // 创建从库
                	$state.go("buy.buyslaveinstance", {rdsId:$stateParams.rdsId}); // 跳转到购买页
                }
                if(vm.source == 'buy_confirminstance'){ // 升级
                	$state.go('app.rds.instance'); // 跳转到列表页
                }
                if(vm.source == 'renew_instance'){ // 续费
                	$state.go('app.rds.instance');
                }
            } else {
                vm.initData(); // 初始化数据
            }
        };
        vm.initData = function (){
        	vm.instance.$$payTypeName = vm.instance.payType == '1'?'预付费':'后付费';
        	vm.instance.$$productCount = 1;
        	vm.queryAccount(); // 查询余额
        };
        vm.queryAccount = function () {
        	eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (data) {
                vm.instance.$$balance = data.data.data.money > 0 ? data.data.data.money : 0;
                vm.usePrice();
                //vm.reCalculateBillingFactory();
            });
        };
        //计算价钱--true 使用余额支付，false-不使用余额支付
        vm.usePrice = function () {
            if (vm.isBalance) {//使用余额支付
                if (vm.instance.$$balance >= vm.instance.price) { // 余额大于订单总金额
                    vm.instance.accountPayment = vm.instance.price; // 余额支付金额
                    vm.instance.thirdPartPayment = 0; // 第三方支付金额
                } else { // 余额小于订单总金额
                    vm.instance.accountPayment = vm.instance.$$balance; // 余额支付金额
                    vm.instance.thirdPartPayment = eayunMath.sub(Number(vm.instance.price), vm.instance.accountPayment);
                }
            } else { // 使用第三方支付
                vm.instance.accountPayment = 0;//余额支付
                vm.instance.thirdPartPayment = vm.instance.price;//第三方支付
            }
        };
        /**
         * 点击复选框
         */
        vm.useBalance = function () {
            vm.isBalance = !vm.isBalance;
            vm.usePrice();
        };
        /*
         * 返回修改配置
         */
        vm.goToCreateInstance = function () {
        	var orderNo ='000000';
  		    if(vm.instance.payType == '2'){
  		     	 orderNo = '000001';
  		    }
            eayunStorage.set('order_back_instance', vm.instance);
            var data = {
            		orderNo:orderNo
            };
            if(vm.instance.backupId){
            	data.backupId = vm.instance.backupId;
            }
            if(vm.instance.isMaster == '1'){
            	$state.go('buy.buyinstance', data);
            }else if(vm.instance.isMaster == '0'){
            	 $state.go('buy.buyslaveinstance', {rdsId: vm.instance.rdsId});
            }
        };
        /**
         * 查询配额信息
         */
        vm.checkQuota = function (){
        	var rds = {
        			prjId: vm.instance.prjId,
        			isMaster: vm.instance.isMaster,
        			masterId: vm.instance.masterId
        	};
        	RDSInstanceService.checkInstanceQuota(rds).then(function (response) {
        		vm.isError = false; 
        	}, function (message) {
        		vm.isError = true;
        		if(message == 'OUT_OF_MASTER_QUOTA'){
        			vm.errorMsg = '您的主库配额不足，请提交工单申请配额';
        		}else if(message == 'OUT_OF_SLAVE_QUOTA'){
        			vm.errorMsg = '从库数量超过最大限额，可提交工单进行扩充';
        		}
        	});
        };
        /**
         * 重新计算价格
         */
        vm.reCalculateBillingFactory = function (){
  		  	if(vm.instance.orderType == '0'){ // 购买操作
  		  		RDSInstanceService.getPriceDetails(vm.instance).then(function (response) {
  		  			vm.instance.price = response.data.totalPrice;
					vm.usePrice();
  		  		});
  		  	}else if(vm.instance.orderType == '1'){// 续费
	  		  	var data = {
		  				dcId: vm.instance.dcId,
		  				payType: '1',
		  				number: 1,
		  				cycleCount:vm.instance.buyCycle,
		  				cloudMySQLCPU: vm.instance.cpu,
		  				cloudMySQLRAM: vm.instance.ram
		  		};
			    if(vm.instance.volumeTypeName == 'Normal'){
			    	data.storageMySQLOrdinary = vm.instance.volumeSize;
			    }else if (vm.instance.volumeTypeName == 'Medium'){
				    data.storageMySQLBetter = vm.instance.volumeSize;
			    }
			  	RDSInstanceService.getTotalPrice('billing/factor/getPriceDetails.do', data).then(function (response) {
		  			vm.instance.price = response.data.totalPrice;
					vm.usePrice();
		  		});
  		  	}else if(vm.instance.orderType == '2'){ //　升级
 			   var url = 'billing/factor/getUpgradePrice.do'; // 预付费的URL
 			   var data = {};
 			   data.dcId = vm.instance.dcId;
 			   if(vm.instance.payType == '1'){ // 预付费
 				   if(vm.instance.volumeTypeName == 'Normal'){
 					   data.storageMySQLOrdinary = vm.instance.volumeSize - vm.instance.diskSize;
 				   }else if (vm.instance.volumeTypeName == 'Medium'){
 					   data.storageMySQLBetter = vm.instance.volumeSize - vm.instance.diskSize;
 				   }
 				   data.cloudMySQLCPU = vm.instance.cpu - vm.instance.rdsInstanceCpu;
 				   data.cloudMySQLRAM = vm.instance.ram - vm.instance.rdsInstanceRam;
 				   data.cycleCount = vm.instance.cycleCount;
 			   }else if (vm.instance.payType == '2'){ // 后付费
 				   url = 'billing/factor/getPriceDetails.do';
 				   data.cloudMySQLCPU = vm.instance.cpu;
 				   data.cloudMySQLRAM = vm.instance.ram;
 				   if(vm.instance.volumeTypeName == 'Normal'){
 					   data.storageMySQLOrdinary = vm.instance.volumeSize;
 				   }else if (vm.instance.volumeTypeName == 'Medium'){
 					   data.storageMySQLBetter = vm.instance.volumeSize;
 				   }
 				   data.cycleCount = 1;
 				   data.number = 1;
 				   data.payType = vm.instance.payType;
 			   }
 			   RDSInstanceService.getTotalPrice(url, data).then(function (response) {
				  if(vm.instance.payType == '1'){
					  vm.instance.price = response.data;
				  }else if (vm.instance.payType == '2'){
					  vm.instance.price = response.data.totalPrice;
				  }
				  vm.usePrice();
 			   });
 		  }
        };
		/**
		 * 配置更改，获取更改后的配置
		 * @param _rdsId
		 */
		vm.getStandard = function(_rdsId){
			RDSInstanceService.getStandard(_rdsId).then(function (response) {
				if(vm.instance.orderType == '1'){ //续费
					vm.instance.cpu = response.cpu;
					vm.instance.ram = response.ram;
					vm.instance.volumeSize = response.volumeSize;
				}else if(vm.instance.orderType == '2'){ // 升级
					vm.instance.rdsInstanceCpu = response.cpu;
					vm.instance.rdsInstanceRam = response.ram;
					vm.instance.diskSize = response.volumeSize;
				}
				vm.reCalculateBillingFactory();
			});
		};
        /**
         * 提交订单
         */
        vm.commitOrderInstance = function () {
        	vm.checkBtn = true;
        	var data = angular.copy(vm.instance);
        	delete data.cycleType;
        	delete data.masterRdsName;
  		    url = 'rds/instance/resizeInstance.do';
  		    if(data.orderType == '0'){ // 新购
  		    	var url = 'rds/instance/buyInstance.do';
  		    	delete data.rdsId;
  		    	delete data.masterName;
				delete data.configName;
				delete data.repassword;
  		    }
  		    if(data.orderType == '2'){ // 升级
  		    	delete data.chargeState;
  		    	delete data.configName;
  		    	delete data.count;
  		    	delete data.createTime;
  		    	delete data.payTypeStr;
  		    	delete data.rdsDescription;
  		    	delete data.rdsIp;
  		    	delete data.rdsStatus;
  		    	delete data.slaveCount;
  		    	delete data.statusStr;
  		    	delete data.type;
  		    	delete data.version;
  		    	delete data.prjName;
  		    	delete data.subnetCidr;
  		    	delete data.masterName;
				delete data.vmId;
  		    }
  		    if(data.orderType == '1'){ // 续费操作
  		    	var _data = {};
  		    	_data.rdsId = vm.instance.rdsId;
  		    	_data.dcId = vm.instance.dcId;
  		    	_data.dcName = vm.instance.dcName;
  		    	_data.payType = vm.instance.payType;
  		    	_data.aliPay = vm.instance.thirdPartPayment;// 第三方支付金额
  		    	_data.accountPay = vm.instance.accountPayment; //余额支付金额
  		    	_data.isCheck = vm.isBalance; // 收否勾选余额
  		    	_data.totalPay = vm.instance.price; // 总价
  		    	_data.buyCycle = vm.instance.buyCycle;
  		    	_data.cpu = vm.instance.cpu;
  		    	_data.ram = vm.instance.ram;
  		    	_data.volumeTypeName = vm.instance.volumeTypeName;
  		    	_data.volumeSize = vm.instance.volumeSize;
  		    	_data.versionName = vm.instance.versionName;
  		    	_data.rdsName = vm.instance.rdsName;
  		    	_data.endTime = vm.instance.endTime;
  		    	RDSInstanceService.renewInstance(_data).then(function (response) {
  		    		vm.checkBtn = false;
  		    		 //订单支付成功
  				  if(response.respCode == '1'){//您当前有未完成订单，不允许提交新订单！
  					  vm.isError = true;
  					  vm.errorMsg = response.message;
  				  }
  				  else if(response.respCode == '2'){//您的产品金额发生变动，请重新确认订单！
  					  vm.isWarn = true;
  					  vm.errorMsg = response.message;
					  vm.reCalculateBillingFactory();// 重新获取价格
  					  //vm.queryAccount();
  				  }
  				  else if(response.respCode == '3'){//您的账户余额发生变动，请重新确认订单！
  					  vm.isWarn = true;
  					  vm.errorMsg = response.message;
  					  vm.queryAccount(); // 获取余额
  				  }
				  else if(response.respCode == '11'){  // 您的订单规格发生变动，请重新确认订单！
					  vm.isWarn = true;
					  vm.errorMsg = response.message;
					  vm.getStandard(vm.instance.rdsId);
				  }
				  else if(response.respCode == '0'){//完全支付宝支付，跳向支付宝支付页面！
  					  var orderPayNavList = [{route:'app.rds.instance',name:'MySQL'}];
                      eayunStorage.persist("orderPayNavList",orderPayNavList);
                      eayunStorage.persist("payOrdersNo",response.message);
  					  $state.go('pay.order');
  				  }
  				  else if(response.respCode == '10'){//完全余额支付，跳向支付成功页面！
  					  $state.go('pay.result', {subject:response.message});
  				  }
  		    	});
  		    }else {
				RDSInstanceService.buyInstance(url, data).then(function (response) {
					if(data.payType == '1'){// 预付费
						if(!data.thirdPartPayment || data.price == 0){
							$state.go('pay.result', {subject:data.prodName});
						}
						else{
							if(data.orderType == '0'){// 新购
								var routeUrl = "buy.buyinstance({'orderNo':'000000'})";
								var orderPayNavList = [{route:'app.rds.instance',name:'MySQL'},
									{route:routeUrl,name:'创建实例'}];
								eayunStorage.persist("orderPayNavList",orderPayNavList);
								eayunStorage.persist("payOrdersNo",response.orderNo);
								$state.go('pay.order');
							}else if(data.orderType == '2'){// 升级
								var orderPayNavList = [{route:'app.rds.instance',name:'MySQL'}];
								eayunStorage.persist("orderPayNavList",orderPayNavList);
								eayunStorage.persist("payOrdersNo",response.data);
								$state.go('pay.order');
							}
						}
					}else if(data.payType == '2'){// 后付费
						$state.go('app.order.list'); //直接跳转到订单列表页
					}
				}, function (data) {
					vm.checkBtn = false;
					var message = data.message;
					if(!message){
						if('1' == vm.instance.payType){
							if(data.orderNo){
								eayunStorage.persist("payOrdersNo",data.orderNo);
								$state.go('pay.order');
							}
						}else if ('2' == vm.instance.payType){ //后付费创建资源失败直接跳转到订单列表页
							$state.go('app.order.list');
						}
					}
					if(message == 'OUT_OF_MASTER_QUOTA' || message == 'OUT_OF_SLAVE_QUOTA'){
						vm.checkQuota();
					}else if(message == 'CHANGE_OF_BILLINGFACTORY'){
						vm.isWarn = true;
						if (vm.instance.orderType == '2'){// 升级，如果对实例进行了续费，需要重新获取剩余天数用于计算变动后的价格
							RDSInstanceService.queryRdsInstanceChargeById(vm.instance.rdsId).then(function (response) {
								vm.instance.cycleCount = response.data;
								vm.reCalculateBillingFactory();
							});
						}else{
							vm.reCalculateBillingFactory();// 对于创建来说，直接重新计算价格即可
						}
						vm.errorMsg = "您的订单金额发生变动，请重新确认订单";
					}else if(message == 'CHANGE_OF_BALANCE'){
						vm.isWarn = true;
						vm.queryAccount(); // 重新查询余额
						vm.errorMsg = "您的余额发生变动，请重新确认订单";
					}else if(message == 'UPGRADING_OR_INORDER'){ // 升级时的提示语
						vm.isWarn = true;
						// vm.queryAccount();
						vm.errorMsg = "资源正在调整中或您有未完成的订单，请您稍后重试";
					}else if(message == 'NOT_SUFFICIENT_FUNDS'){// 创建订单时抛出此异常信息
						eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
							vm.payAfpterPayment = response.data;
							vm.isError = true;
							vm.errorMsg = "您的账户余额不足"+vm.payAfpterPayment+"元，请充值后操作";
						});
					}else if(message == 'ARREARS_OF_BALANCE'){ // 针对升级的判断
						vm.isError = true;
						vm.queryAccount(); // 重新查询账户余额
						vm.errorMsg = "您的账户已欠费，请充值后操作";
					}else if(message == 'CHANGE_OF_STANDARD'){  // 针对升级的判断
						vm.isError = true;
						vm.errorMsg = "您的订单规格发生变动，请重新确认订单";
						//vm.getStandard(vm.instance.rdsId);
					}else if(message == 'CHANGE_OF_MASTER_STANDARD'){
						vm.isError = true;
						vm.errorMsg = "您的主库实例规格发生变动，请重新确认订单";
					}
				});
			}
        };
        vm.initial();
    }]);
