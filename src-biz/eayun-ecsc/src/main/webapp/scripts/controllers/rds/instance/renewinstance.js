/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('RenewInstanceCtrl', ['$scope', '$modalInstance', 'item', 'RDSInstanceService', 'eayunHttp', 'eayunModal', 'eayunStorage', '$state', 
        function ($scope, $modalInstance, item, RDSInstanceService, eayunHttp, eayunModal, eayunStorage, $state) {
    	
    	var vm = this;
    	vm.init = function () {
    		vm.instance = angular.copy(item);
    		vm.checkBtn = false; // 预防表单的重复提交
    		vm.hasGotPrice = true; //是否获取到价格，默认获取到
    		vm.buyCycleType();
    	};
    	
    	/**
         * 云数据库状态 显示
         */
        vm.checkRdsStatus =function (model){
        	if('1'==model.chargeState||'2'==model.chargeState||'3'==model.chargeState){
        		return 'ey-square-disable';
        	}
        	else if(model.rdsStatus && (model.rdsStatus=='ACTIVE' || model.rdsStatus=='RESTART_REQUIRED')){
    			return 'ey-square-right';
    		}  
    		else if(model.rdsStatus=='SHUTOFF'){
    			return 'ey-square-disable';
    		}
    		else if(model.rdsStatus=='ERROR'){
    			return 'ey-square-error';
    		}
    		else{
    			return'ey-square-warning';
    		}
        };
        /**
  	   	 * 初始化购买周期类型
  	     */
  	    vm.buyCycleType = function(){
  	    	vm.cycleTypeList = [];
  		    eayunHttp.post('cloud/vm/queryBuyCycleType.do').then(function (response){
  		    	vm.cycleTypeList = response.data.data;
  			    if(vm.cycleTypeList.length > 0){
  			    	vm.instance.cycleType = vm.cycleTypeList[0].nodeId;
  			    }
  			    vm.queryBuyCycle();
  		   });
  		};
  		/**
  	     * 购买周期选择
  	     */
  	    vm.queryBuyCycle = function(){
  	    	vm.cycleList = [];
  		    eayunHttp.post('cloud/vm/queryBuyCycleList.do',vm.instance.cycleType).then(function (response){
  		    	if(response && response.data){
  		    		vm.cycleList = response.data.data;
  		    	}
  			  
  			    if(vm.cycleList.length>0 && !vm.instance.buyCycle){
  			    	vm.instance.buyCycle = vm.cycleList[0].nodeNameEn;
  			    	vm.getLastTimeAndPrice(); // 获取续费后的到期时间和价格
  			    }
  		    });
  	    };
  	  /**
  	   * 选择购买周期类型
  	   */
  	    vm.changeCycleType = function () {
  	    	vm.instance.buyCycle = null;
  		    vm.queryBuyCycle();
  	    };
    	$scope.cancel = function (){
  		  	$modalInstance.close();
  	  	};
  	  
	  	$scope.ok = function (){
	  		$modalInstance.close();
	  	};
	  	
	  	$scope.commit = function (){
	  		vm.checkBtn = true; // 避免重复提交
	  		// 查看该资源是否有正在处理的订单
	  		RDSInstanceService.checkRdsInstanceOrderExsit(vm.instance.rdsId, true, true).then(function (response) {
	  			vm.instance.prodName = 'MySQL主库实例-续费';
	 		    vm.instance.orderType = '1';// 续费
	 		    vm.instance.ram = vm.instance.ram / 1024;
	 		    vm.instance.versionName = vm.instance.type.toLowerCase() == 'mysql' ? 'MySQL' + vm.instance.version:'';
	 		    eayunStorage.set('order_confirm_rds',vm.instance);
	 		    $scope.ok();
	 		    $state.go('buy.verifyInstance',{source:'renew_instance'});
	  		}, function (message) {
	  			$scope.ok();
	  			eayunModal.warning(message);
	  			vm.checkBtn = false;
	  		});
	  	};
	  	/**
	  	 * 获取续费后的到期时间和价格
	  	 */
	  	vm.getLastTimeAndPrice = function () {
	  		var data = {
	  				dcId: vm.instance.dcId,
	  				payType: '1',
	  				number: 1,
	  				cycleCount:vm.instance.buyCycle,
	  				cloudMySQLCPU: vm.instance.ram / 1024,
	  				cloudMySQLRAM: vm.instance.cpu
	  		};
		    if(vm.instance.volumeTypeName == 'Normal'){
		    	data.storageMySQLOrdinary = vm.instance.volumeSize;
		    }else if (vm.instance.volumeTypeName == 'Medium'){
			    data.storageMySQLBetter = vm.instance.volumeSize;
		    }
	  		var url = 'billing/factor/getPriceDetails.do';
	  		RDSInstanceService.getTotalPrice(url, data).then(function (response) {
	  			vm.instance.price = response.data.totalPrice;
	  			vm.hasGotPrice = true;
	  		}, function (message) {
	  			vm.hasGotPrice = false;
	  			vm.priceMsg = message;
	  		});
	  		//计算续费后的到期时间
	  		RDSInstanceService.computeRenewEndTime(vm.instance.endTime, vm.instance.buyCycle).then(function (response){
	  			vm.lastTime = response;
	  		});
	  	};
	  	vm.init();
    }]);