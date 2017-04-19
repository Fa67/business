/**
 * Created by eayun on 2017/2/27.
 */
'use strict';

angular.module('eayunApp.controllers')
    .config(function () {

    })
    .controller('RdsBuySlaveInstanceCtrl', ['$scope', 'DatacenterService', 'RDSInstanceService', 'eayunStorage', '$state', '$stateParams', 'eayunHttp',   
        function ($scope, DatacenterService, RDSInstanceService, eayunStorage, $state, $stateParams, eayunHttp) {
    	var vm = this;
    	
    	vm.init = function (){
    		vm.checkRdsName = true; // 初始化不重名
    		vm.checkQuota = true;  // 初始化不超配
    		var item = eayunStorage.get('order_back_instance');
			  eayunStorage.delete('order_back_instance');
			  if(!item){
				  vm.newData();
			  }
			  else{
				  //返回修改配置
				  vm.instance = angular.copy(item);
				  vm.calcBillingFactor(); //计算价格
	    		  vm.checkInstanceQuota(); // 查看配额
	    		  vm.queryAccount(); // 查询账户余额
	    		  vm.checkRdsNameExist(); // 重名校验
			}
    	};
    	vm.newData = function () {
    		RDSInstanceService.getRdsById($stateParams.rdsId).then(function (response) {
    			vm.instance = response.data;
    			vm.instance.masterRdsName = response.data.rdsName;
    			vm.instance.versionName = 'MySQL ' + response.data.version;
    			vm.instance.payType = '2';// 后付费
    			vm.instance.rdsName = null;
    			vm.instance.isMaster = '0';
    			vm.instance.masterId = $stateParams.rdsId;
    			vm.instance.ram = response.data.ram / 1024;
				if($stateParams.orderNo){ // 重新下单
					RDSInstanceService.getInstanceByOrderNo($stateParams.orderNo).then(function (response) {
						vm.instance.rdsName = response.data.rdsName;
						vm.checkRdsNameExist(); // 重名校验
					});
				}
    			vm.calcBillingFactor(); //计算价格
    			vm.checkInstanceQuota(); // 查看配额
    			vm.queryAccount(); // 查询账户余额
    		}, function (message) {
    			$state.go('app.rds.instance');
    		});
    	}
    	/**
    	 * 名称的校验
    	 */
    	vm.checkRdsNameExist = function (rdsName){
      	    var cloudRdsInstance = {
  				  prjId:vm.instance.prjId,
  				  rdsName:vm.instance.rdsName, 
  		    };
      	    RDSInstanceService.checkRdsNameExist(cloudRdsInstance).then(function (response){
  			    vm.checkRdsName = response;
  		    });
        };
        /**
  	   * 计算价格
  	   */
  	  vm.calcBillingFactor = function () {
  		  if(vm.instance.cpu && vm.instance.ram){
  			  RDSInstanceService.getPriceDetails(vm.instance).then(function (response) {
  				  vm.hasGotPrice = true;
  				  vm.priceDetails = response.data;
  				  
  			  }, function (message) {
  				  vm.hasGotPrice = false;
  				  vm.priceMsg = message;
  			  });
  		  }
  	  };
  	  vm.checkInstanceQuota = function () {
		  var data = {
			  prjId: vm.instance.prjId,
			  masterId: vm.instance.masterId,
			  isMaster: 0
		  };
		  RDSInstanceService.checkInstanceQuota(data).then(function (response) {
			  vm.checkQuota = true;
		  }, function (message) {
			  vm.checkQuota = false;
			  if(message == 'OUT_OF_SLAVE_QUOTA'){
				  vm.checkQuotaMsg = '从库数量超过最大限额，可提交工单进行扩充';
			  }
		  });
	  };
	  /**
	   * 立即充值
	   */
	  vm.recharge = function () {
		  var routeUrl = "app.costcenter.guidebar.account";
		  var rechargeNavList = [{route:routeUrl,name:'账户总览'}];
		  eayunStorage.persist('rechargeNavList',rechargeNavList);
		  $state.go('pay.recharge');
	  };
	  /**
	   * 查看账户余额
	   */
	  vm.queryAccount = function (){
		  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
			  vm.payAfpterPayment = response.data;
		  });
	  };
	  /**
	   * 点击立即购买
	   */
	  vm.commitBuyRds = function () {
		  vm.isNSF = false;
		  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
			  vm.account = response.data.data;
			  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
				  vm.payAfpterPayment = response.data;
				  vm.isNSF = vm.account.money < vm.payAfpterPayment;
				  if(vm.isNSF){
					  return ;
				  }
				  var data = {}
				  vm.assemblyData(data);
				  eayunStorage.set('order_confirm_rds',data);
				  $state.go('buy.verifyInstance',{source:'buy_slave_instance',rdsId: data.rdsId});
			  });
		  });
	  };
	  vm.assemblyData = function (data) {
		  data.dcId = vm.instance.dcId; // 数据中心ID
		  data.rdsId = vm.instance.rdsId;
		  data.masterRdsName = vm.instance.masterRdsName;
		  data.dcName = vm.instance.dcName;
		  data.prjId = vm.instance.prjId;
		  data.rdsName = vm.instance.rdsName;
		  data.netId = vm.instance.netId;
		  data.netName = vm.instance.netName;
		  data.subnetId = vm.instance.subnetId;
		  data.subnetName = vm.instance.subnetName;
		  data.subnetCidr = vm.instance.subnetCidr;
		  data.versionId = vm.instance.versionId;
		  data.versionName = vm.instance.versionName;
		  data.configId = vm.instance.configId;
		  data.orderType = '0';
		  data.payType = vm.instance.payType;
		  data.buyCycle = vm.instance.buyCycle;
		  data.cycleType = vm.instance.cycleType;
		  data.isMaster = vm.instance.isMaster;
		  data.masterId = vm.instance.masterId;
		  if(vm.priceDetails){
			  data.price = vm.priceDetails.totalPrice;
			  data.price = data.price.toString();
		  }
		  data.prodName = "MySQL从库实例-按需付费";
		  data.cpu = vm.instance.cpu;
		  data.ram  = vm.instance.ram;
		  data.volumeSize = vm.instance.volumeSize;
		  data.volumeType = vm.instance.volumeType;
		  data.volumeTypeName = vm.instance.volumeTypeName;
		  data.configName = vm.instance.configName;
	  };
	 /**
	  * 跳转到详情页
	  * @param _rdsId
	  */
	  vm.goToDetail = function (_rdsId){
		  $state.go('app.rds.detail.dbinstance',{"rdsId":_rdsId});
	  };
      vm.init();
    }]);