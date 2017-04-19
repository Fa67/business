/**
 * Created by eayun on 2017/2/22.
 */
'use strict';

angular.module('eayunApp.controllers')

    .config(function ($stateProvider) {

    })

    .controller('ResizeInstanceController', ['$rootScope', '$scope', '$state', 'item', 'RDSInstanceService', '$modalInstance', 'eayunHttp', 'eayunStorage', 'eayunModal', 
        function ($rootScope, $scope, $state, item, RDSInstanceService, $modalInstance, eayunHttp, eayunStorage, eayunModal) {
    	var vm = this;
  	  	vm.init = function (){
  	  		vm.isResize = false; // 是否满足升级条件
  	  		vm.checkBtn = false; // 预防表单的重复提交
  	  		vm.instance = angular.copy(item);
  	  		vm.instance.rdsInstanceCpu = item.cpu;
  	  		vm.instance.rdsInstanceRam = item.ram/1024;
  	  		vm.instance.diskSize = item.volumeSize;
  	  		vm.instance.ram = vm.instance.ram / 1024;
  	  		vm.instance.versionName = vm.instance.type.toLowerCase() == 'mysql' ? 'MySQL ' + vm.instance.version:'';
			vm.getVolumeTypeList(vm.instance.dcId);
  	  		vm.queryCpuList();
  	  		if(vm.instance.payType == '1'){
  	  			vm.getCycleCount(); //预付费资源获取剩余天数
  	  		}
  	  	};
  	  	vm.getCycleCount = function (){
  	  		RDSInstanceService.queryRdsInstanceChargeById(vm.instance.rdsId).then(function (response) {
  	  			vm.instance.cycleCount = response.data;
  	  		});
  	  	}
  	    $scope.cancel = function (){
		    $modalInstance.close();
	    };
	  
	    $scope.ok = function (){
		    $modalInstance.close();
	    };
	    /**
	     * 选择CPU
	     */
	    vm.selectCpu = function (data){
	    	vm.instance.cpu = data.nodeName.substring(0,data.nodeName.length-1);
			vm.queryRamList(data.nodeId);
			vm.checkResize();
		};
	    /**
		 * 查询CPU列表
		 */
		vm.queryCpuList = function(){
		    vm.cpuList = [];
			RDSInstanceService.getCpuList().then(function (response) {
				var i = 0;
				var data = null;
				angular.forEach(response, function (value, key) {
					var cpu = value.nodeName.substring(0,value.nodeName.length-1);
					if(cpu >= vm.instance.rdsInstanceCpu){
						vm.cpuList[i++] = value;
						if(cpu == vm.instance.cpu){
							data = value;
						}
					}
					// 注释代码为，如果付费类型为包年包月，则不允许降级；付费类型为按需付费，允许降级
					/*if(vm.instance.payType == '1' && cpu >= vm.instance.rdsInstanceCpu){
						vm.cpuList[i++] = value;
						if(cpu == vm.instance.cpu){
							data = value;
						}
					}else if(vm.instance.payType == '2'){
						vm.cpuList[i++] = value;
						if(cpu == vm.instance.cpu){
							data = value;
						}
					}*/
				  });
				if(vm.cpuList && vm.cpuList.length > 0){
					if(!data){
					  data = vm.cpuList[0];
					}
					vm.selectCpu(data);
			    }
		   });
	   };
	   vm.selectRam = function (data) {
		   vm.instance.ram = data.nodeName.substring(0,data.nodeName.length-2);
	       vm.calcBillingFactor();// 计算价格
	       vm.checkResize();
	   };
	   /**
		 * 查询内存列表
		 */
	   vm.queryRamList = function (nodeId) {
		   vm.ramList = [];
	       var flag = false;
	       RDSInstanceService.getRamListByCpu(nodeId).then(function (response) {
	    	   var i = 0;
			   angular.forEach(response, function (value, key) {
				   var ram = value.nodeName.substring(0,value.nodeName.length-2);
				   if(ram >= vm.instance.rdsInstanceRam){
					   vm.ramList[i++] = value;
					   if(ram == vm.instance.ram)
						   flag = true;
				   }
				   // 注释代码为，如果付费类型为包年包月，则不允许降级；付费类型为按需付费，允许降级
				   /*if(vm.instance.payType == '1' && ram >= vm.instance.rdsInstanceRam){
					   vm.ramList[i++] = value;
					   if(ram == vm.instance.ram)
						   flag = true;
				   }else if(vm.instance.payType == '2'){
					   vm.ramList[i++] = value;
					   if(ram == vm.instance.ram)
						   flag = true;
				   }*/
			   });
			   if(vm.ramList && vm.ramList.length > 0){
				   if(!flag){
					   vm.instance.ram = vm.ramList[0].nodeName.substring(0,vm.ramList[0].nodeName.length-2);
				   }
			   }
			   vm.calcBillingFactor();// 计算价格
	       });
	   };
		/**
		 * 获取数据盘类型
		 */
		vm.getVolumeTypeList = function (dcId){
			vm.volumeTypeList = [];
			RDSInstanceService.getVolumeTypeList(dcId).then(function (response) {
				var i = 0;
				angular.forEach(response, function (value, key) {
					vm.volumeTypeList[i++] = value;
					if(value.typeId == vm.instance.volumeType){
						vm.maxSize = value.maxSize;
					}
				});
			});
		};
	   vm.calcBillingFactor = function (){
		   vm.hasGotPrice = false;
		   if(vm.instance.cpu && vm.instance.ram && 
				   ((vm.instance.payType == '1' && vm.instance.diskSize <= vm.instance.volumeSize) || vm.instance.payType == '2')){
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
				   data.cloudMySQLCPU = vm.instance.ram;
				   data.cloudMySQLRAM = vm.instance.cpu;
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
					  vm.hasGotPrice = true;
					  if(vm.instance.payType == '1'){
						  vm.instance.price = response.data;
					  }else if (vm.instance.payType == '2'){
						  vm.instance.price = response.data.totalPrice;
					  }
			   }, function (message) {
				   vm.hasGotPrice = false;
				   vm.priceMsg = message;
			  });
		  }
	   };
	   /**
		 * 滚动条
		 */
	   $scope.formate = function (step){
		   return Number((step*100).toFixed());
	   };
	   $scope.parse = function (value) {
		   return Number((value/100+0.044444).toFixed(1));
	   };
	   /**
	    * 判断是否满足升级条件
	    */
	   vm.checkResize = function (){
		   if(vm.instance.volumeSize < vm.instance.diskSize){ // 数据盘不允许调小
			   vm.isResize = false;
			   return ;
		   }else if(vm.instance.volumeSize == vm.instance.diskSize){ // 数据盘不改变大小
			   if(vm.instance.cpu == vm.instance.rdsInstanceCpu && vm.instance.ram == vm.instance.rdsInstanceRam){
				   vm.isResize = false;
				   return ;
			   }else {
				   vm.isResize = true;
				   return ;
			   }
		   }else {
			   vm.isResize = true;
			   return ;
		   }
	   };
	   /**
	    * 更改存储容量
	    */
	   vm.changeVolumeSize = function () {
		   vm.calcBillingFactor();
		   vm.checkResize();
	   };
	   /**
	    * 点击确定按钮
	    */
	   $scope.commit = function () {
		   vm.checkBtn = true;
		   if(vm.instance.payType == '2'){
			   vm.isNSF = false;
			   eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
				   vm.account = response.data.data;
				   if(vm.account.money <= 0){
					   vm.isNSF = true;
					   vm.checkBtn = false;
					   var model = eayunModal.warning("您的账户已欠费，请充值后操作");
					   model.then(function(){
						   $scope.ok();
					   },function(){
							  $scope.ok();
						  });
				   }else{
					   vm.resize();
					   }
			   });
		    } else if (vm.instance.payType == '1'){
				  vm.resize();
			}
	   };
	   /**
	    * 升级操作
	    */
	   vm.resize = function () {
		   // 是否有未完成的订单
		   RDSInstanceService.checkRdsInstanceOrderExsit(vm.instance.rdsId, true, true).then(function (response) {
			   vm.instance.prodName = vm.instance.isMaster == '1'? 'MySQL主库实例-升降规格':'MySQL从库实例-升降规格';
			   vm.instance.orderType = '2';// 升级
			   eayunStorage.set('order_confirm_rds',vm.instance);
			   $scope.ok();
			   $state.go('buy.verifyInstance',{source:'buy_confirminstance'});
		   }, function (message) {
			   $scope.ok();
	  		   eayunModal.warning(message);
		   });
	   };
	   vm.init();
}]);
