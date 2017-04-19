/**
 * Created by eayun on 2017/2/27.
 */
'use strict';

angular.module('eayunApp.controllers')
    .config(function () {

    })
    .controller('RdsBuyInstanceCtrl', ['$scope', 'DatacenterService', '$stateParams', 'RDSInstanceService', 'VpcService', 'eayunHttp', 'eayunStorage', '$state', 'PWService',
        function ($scope, DatacenterService, $stateParams, RDSInstanceService, VpcService, eayunHttp, eayunStorage, $state, PWService) {
    	var vm = this;

    	vm.init = function(){
    		vm.instance = {};
            if ($stateParams.orderNo) {
            	var orderNo = $stateParams.orderNo;
                if(orderNo == '000000'){
  				  vm.instance.payType = '1';
  			  	}
  			    else if(orderNo == '000001'){
  				  vm.instance.payType = '2';
  			    }
            }
            if(orderNo == '000000' || orderNo =='000001'){
	  			  var item = eayunStorage.get('order_back_instance');
	  			  eayunStorage.delete('order_back_instance');
	  			  if(!item){
	  				  vm.newData();
	  			  }
	  			  else{
	  				  //返回修改配置
	  				  vm.instance = angular.copy(item);
	  			  }
	  			  vm.initData();
	  			  vm.initDatacenter();
  		  	}else if(orderNo && orderNo != '000000' && orderNo !='000001'){
  		  		RDSInstanceService.getInstanceByOrderNo(orderNo).then(function (response) {
  		  			vm.instance = response.data;
					vm.instance.password = null;
  		  			vm.initData();
  		  			vm.initDatacenter();
  		  		});
  		  	}
        };
        vm.newData = function () {
        	var dcPrj = JSON.parse(sessionStorage['dcPrj']);
            var userInfo = JSON.parse(sessionStorage['userInfo']);
            vm.instance.orderType = '0';
            vm.instance.dcId = dcPrj.dcId;
            vm.instance.dcName = dcPrj.dcName;
            vm.instance.prjId = dcPrj.projectId;
            vm.instance.cusId = userInfo.cusId;
            vm.instance.createName = userInfo.userName;
            vm.instance.isMaster = '1';
        };
        /**
         * 初始化数据
         */
        vm.initData = function () {
            vm.hasGotPrice = true;  // 是否获取到价格
            vm.checkRdsName = true; // 名称校验
            vm.checkQuota = true; // 配额校验

            // 初始化网络信息
            vm.initNetworkList();
            // 初始化购买周期的选项
            vm.buyCycleType();
            // 获取CPU信息
            vm.queryCpuList();
            // 获取数据盘类型
            vm.getVolumeTypeList(vm.instance.dcId);
            // 初始化MYSQL 版本
            vm.getVersionList();
            
            if(vm.instance.payType == '2'){
  			  vm.queryAccount();
  		  	}
            // 查询配额
            vm.checkInstanceQuota();
			// 名称是否重名
			vm.checkRdsNameExist();
            if($stateParams.backupId){
            	vm.instance.backupId = $stateParams.backupId;
            	RDSInstanceService.getInfoByBackupId(vm.instance.backupId).then(function (response) {
            		vm.instance.versionId = response.versionId;
            		vm.instance.versionName = response.versionInfo;
            		vm.instance.configId = response.configurationId;
            		vm.instance.configName = response.configurationName;
            		vm.instance.backupSize = response.size;
            	});
            }
        };
        /*初始化数据中心*/
        vm.initDatacenter = function () {
            DatacenterService.getDcPrjList().then(function (dataList) {
                vm.datacenters = dataList;
                if (vm.datacenters.length > 0) {
                    angular.forEach(vm.datacenters, function (value, key) {
                        if (vm.instance.prjId == value.projectId) {
                            vm.instance.dcId = value.dcId;
                            vm.instance.dcName = value.dcName;
                        }
                    });
                }
            });
        };
        /*选择数据中心和项目*/
        vm.selectDcPrj = function (_datacenter) {
            vm.instance.dcId = _datacenter.dcId;
            vm.instance.dcName = _datacenter.dcName;
            vm.instance.prjId = _datacenter.projectId;
            vm.checkRdsNameExist();
            vm.initNetworkList();
            vm.getVersionList();
			vm.getVolumeTypeList(vm.instance.dcId);
            vm.calcBillingFactor();// 计算价格
			vm.checkInstanceQuota();//重新获取配额信息
        };

  	  vm.typeChoose = function (payType){
  		var orderNo = '000000';
		if(payType == '2'){
			orderNo = '000001';
		}
		vm.instance.payType = payType;
		var data ={};
		vm.assemblyData(data);
		eayunStorage.set('order_back_instance',data);
		$state.go('buy.buyinstance',{orderNo:orderNo});
  	  };
  	  /**
  	   * 获取私有网络（设置网关）
  	   */
  	  vm.initNetworkList = function (){
  		  vm.networkList = [];
  		  VpcService.getNetworkListHaveGatewayIp(vm.instance.prjId).then(function (response) {
  			  var i = 0;
  			  angular.forEach(response, function (value, key) {
				  vm.networkList[i] = value;
				  if(value.netId == vm.instance.netId){
					  var data = null;
					  data = vm.networkList[0];
					  vm.networkList[0] = value;
					  vm.networkList[i] = data;
				  }
				  i++;
  			  });
	          if(vm.networkList && vm.networkList.length > 0){
				  vm.instance.netId = vm.networkList[0].netId;
				  vm.instance.netName = vm.networkList[0].netName;
	          }else{
	            	vm.instance.netId = null;
	            	vm.instance.netName = null;
	          }
	          vm.getSubnetList(vm.instance.netId);
        });
  	  };
  	  /**
  	   * 获取子网列表
  	   */
  	  vm.getSubnetList = function (_netId){
  		  vm.subnetList = [];
  		  if(!_netId) {
  			  vm.instance.subnetId = null;
  			  vm.instance.subnetName = null;
  			  vm.instance.subnetCidr = null;
  			  return ;
  		  }
          VpcService.getSubnetListByNetId(_netId, '1').then(function (response) {
              vm.subnetList = response;
              var i = 0;
              angular.forEach(response, function (value, key) {
    			  vm.subnetList[i] = value;
    			  if(value.subnetId == vm.instance.subnetId){
					  var data = null;
					  data = vm.subnetList[0];
					  vm.subnetList[0] = value;
					  vm.subnetList[i] = data;
    			  }
				  i++;
    		  });
              if (vm.subnetList && vm.subnetList.length > 0) {
				  vm.instance.subnetId = vm.subnetList[0].subnetId;
				  vm.instance.subnetName = vm.subnetList[0].subnetName;
				  vm.instance.subnetCidr = vm.subnetList[0].cidr;
              }else {
            	  vm.instance.subnetId = null;
        		  vm.instance.subnetName = null;
        		  vm.instance.subnetCidr = null;
              }
          });
      
  	  };
  	  /*
  	   * 选择私有网络
  	   */
  	  vm.changeNetwork = function () {
	    angular.forEach(vm.networkList, function (value, key) {
		   if(value.netId == vm.instance.netId){
			   vm.instance.netName = value.netName;
		   }
	    });
  		vm.instance.subnetId = null;
        vm.instance.subnetName = null;
        vm.instance.subnetCidr = null;
  		vm.getSubnetList(vm.instance.netId);
  	  };
	/**
	 * 子网改变
	 */
	  vm.changeSubnet = function () {
		  angular.forEach(vm.subnetList, function (value, key) {
			  if(value.subnetId == vm.instance.subnetId){
				  vm.instance.subnetName = value.subnetName;
				  vm.instance.subnetCidr = value.cidr;
			  }
		  });
	  };
      /**
       * 获取MYSQL版本列表
       */
      vm.getVersionList = function () {
    	  vm.versionList = [];
    	  RDSInstanceService.getVersionList(vm.instance.dcId).then(function (response) {
    		  var i = 0;
			  var flag = false;
			  var data = null;
    		  angular.forEach(response.data, function (value, key) {
    			  vm.versionList[i++] = value;
    			  if(value.versionId == vm.instance.versionId){
					  data = value;
					  flag = true;
				  }
    		  });
    		  if(vm.versionList && vm.versionList.length > 0){
				  if(!flag){
					  vm.instance.versionId = vm.versionList[0].versionId;
					  vm.instance.versionName = vm.versionList[0].versionName;
				  }else{
					  vm.instance.versionId = data.versionId;
					  vm.instance.versionName = data.versionName;
				  }
    		  }else {
    			  vm.instance.versionId = null;
				  vm.instance.versionName = null;
    		  }
    		  vm.getConfigList(vm.instance.versionId);
    	  });
      };
      /**
       * 版本变更事件
       */
      vm.selectVersion = function (data){
    	  vm.instance.versionId = data.versionId;
		  vm.instance.versionName = data.versionName;
		  vm.getConfigList(data.versionId);
      };
      /**
       * 获取配置文件列表
       */
      vm.getConfigList = function (versionId){
    	  if(!versionId){
    		  vm.instance.configId = null;
    		  return ;
    	  }
    	  vm.configList = [];
    	  RDSInstanceService.getConfigList(versionId, vm.instance.prjId).then(function (response) {
    		  var i = 0;
    		  angular.forEach(response.data, function (value, key) {
    			  vm.configList[i] = value;
    			  if(value.configId == vm.instance.configId){
					  var data = null;
					  data = vm.configList[0];
					  vm.configList[0] = value;
					  vm.configList[i] = data;
				  }
				  i++;
    		  });
    		  if(vm.configList && vm.configList.length > 0){
				  vm.instance.configId = vm.configList[0].configId;
	    	  }else{
				  vm.instance.configId = null;
	    	  }
    	  });
      };
      /**
	   * 查询CPU列表
	   */
	  vm.queryCpuList = function(){
		  vm.cpuList = [];
		  vm.ramList = [];
		  RDSInstanceService.getCpuList().then(function (response) {
			  var i = 0;
			  var data = null;
			  angular.forEach(response, function (value, key) {
				  vm.cpuList[i++] = value;
				  if(vm.cpuList[i-1].nodeName.substring(0,vm.cpuList[i-1].nodeName.length-1) == vm.instance.cpu){
					  data = value;
				  }
			  });
			  if(vm.cpuList && vm.cpuList.length > 0){
				  if(!data){
					  data = vm.cpuList[0];
				  }
				  vm.selectCpu(data);
			  }
		  });
	  };
	  /**
	   * 选择CPU
	   */
	  vm.selectCpu = function (data){
		  vm.instance.cpu = data.nodeName.substring(0,data.nodeName.length-1);
		  vm.queryRamList(data.nodeId);
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
				  vm.ramList[i++] = value;
				  if(value.nodeName.substring(0,value.nodeName.length-2) == vm.instance.ram)
					  flag = true;
			  });
			  if(vm.ramList && vm.ramList.length > 0){
				  if(!flag){
					  vm.instance.ram = vm.ramList[0].nodeName.substring(0,vm.ramList[0].nodeName.length-2);
				  }
			  }
			  vm.calcBillingFactor();// 计算价格
    	  });
      };
      vm.selectRam = function (data) {
    	  vm.instance.ram = data.nodeName.substring(0,data.nodeName.length-2);
    	  vm.calcBillingFactor();// 计算价格
      };
      /**
       * 获取数据盘类型
       */
      vm.getVolumeTypeList = function (dcId){
    	  vm.volumeTypeList = [];
    	  RDSInstanceService.getVolumeTypeList(dcId).then(function (response) {
    		  var i = 0;
			  var flag = false;
			  var data = null;
    		  angular.forEach(response, function (value, key) {
    			  vm.volumeTypeList[i++] = value;
    			  if(value.typeId == vm.instance.volumeType){
					  data = value;
					  flag = true;
    			  }
    		  });
    		  if(vm.volumeTypeList && vm.volumeTypeList.length > 0){
				  if(!flag){
					  vm.instance.volumeType = vm.volumeTypeList[0].typeId;
					  vm.instance.volumeTypeName = vm.volumeTypeList[0].typeName;
					  vm.maxSize = vm.volumeTypeList[0].maxSize;
				  }else{
					  vm.instance.volumeType = data.typeId;
					  vm.instance.volumeTypeName = data.typeName;
					  vm.maxSize = data.maxSize;
				  }
    		  }
    	  });
      };
      /**
       * 选择云硬盘类型
       */
      vm.selectVolumeType = function (data){
    	  vm.instance.volumeType = data.typeId;
    	  vm.instance.volumeTypeName = data.typeName;
    	  vm.instance.volumeSize = 10;
		  vm.maxSize = data.maxSize;
    	  vm.calcBillingFactor();
      };
      vm.changeVolumeSize = function (volumeSize) {
    	  vm.calcBillingFactor(); // 计算价格
      };
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
	   * 初始化购买周期类型
	   */
	  vm.buyCycleType = function(){
		  vm.cycleTypeList = [];
		  var flag = false;
		  eayunHttp.post('cloud/vm/queryBuyCycleType.do').then(function (response){
			  var i = 0;
			  angular.forEach(response.data.data, function (value, key) {
				  vm.cycleTypeList[i++] = value;
				  if(value.nodeId == vm.instance.cycleType)
					  flag = true;
			  });
			  if(vm.cycleTypeList.length > 0){
				  if(!flag)
					  vm.instance.cycleType = vm.cycleTypeList[0].nodeId;
			  }
			  vm.queryBuyCycle();
		  });
		  
	  };
	  /**
	   * 选择购买周期类型
	   */
	  vm.changeCycleType = function () {
		  vm.instance.buyCycle = null;
		  vm.queryBuyCycle();
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
				  vm.calcBuyCycle();
			  }
		  });
	  };
	  
	  /**
	   * 计算购买周期
	   */
	  vm.calcBuyCycle = function(){
		  vm.calcBillingFactor();
	  };
	  
	  /**
	   * 计算价格
	   */
	  vm.calcBillingFactor = function () {
		  vm.hasGotPrice = false;
		  // CPU，内存，数据盘类型，数据盘大小，购买周期
		  if(vm.instance.cpu && vm.instance.ram
				  && vm.instance.volumeType && vm.instance.volumeSize){
			  if(vm.instance.payType == '1' && !vm.instance.buyCycle){ // 预付费
				  return ;
			  }
			  RDSInstanceService.getPriceDetails(vm.instance).then(function (response) {
				  vm.hasGotPrice = true;
				  vm.priceDetails = response.data;
				  
			  }, function (message) {
				  vm.hasGotPrice = false;
				  vm.priceMsg = message;
			  });
		  }
	  };
	  /**
	   * 查看配额是否充足
	   */
	  vm.checkInstanceQuota = function () {
		  var data = {
			  prjId: vm.instance.prjId,
			  isMaster: 1
		  };
		  RDSInstanceService.checkInstanceQuota(data).then(function (response) {
			  vm.checkQuota = true;
		  }, function (message) {
			  vm.checkQuota = false;
			  if(message == 'OUT_OF_MASTER_QUOTA'){
				  vm.checkQuotaMsg = '您的主库配额不足，请提交工单申请配额';
			  }
		  });
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
	   * 查看账户余额
	   */
	  vm.queryAccount = function (){
		  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
			  vm.payAfpterPayment = response.data;
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
	   * 点击立即购买
	   */
	  vm.commitBuyRds = function () {
		  vm.isNSF = false;
		  if(vm.instance.payType == '2'){
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
					  $state.go('buy.verifyInstance',{source:'buy_instance'});
				  });
			  });
		  }
		  else{
			  var data = {}
			  vm.assemblyData(data); // 组装参数
			  eayunStorage.set('order_confirm_rds',data); // 将参数存储到storage中
			  $state.go('buy.verifyInstance',{source:'buy_instance'}); // 标记订单确认页的来源（购买和升级）
		  }
	  };
	  vm.assemblyData = function (data) {
		  data.dcId = vm.instance.dcId; // 数据中心ID
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
		  if(vm.instance.backupId){
			  data.backupId = vm.instance.backupId;
		  }
		  if(data.isMaster == '0'){
			  data.masterId = vm.instance.masterId;
		  }
		  if(vm.priceDetails){
			  data.price = vm.priceDetails.totalPrice;
			  data.price = data.price.toString();
		  }
		  data.prodName = (data.isMaster == '1' ? "MySQL主库实例-":"MySQL从库实例-") + (data.payType == '1' ? "包年包月" : "按需付费");
		  data.cpu = vm.instance.cpu;
		  data.ram  = vm.instance.ram;
		  data.volumeSize = vm.instance.volumeSize;
		  data.volumeType = vm.instance.volumeType;
		  data.volumeTypeName = vm.instance.volumeTypeName;
		  data.password = vm.instance.password;
		  data.repassword = vm.instance.repassword;
	  };
	 /**
	  * 跳转到网络详情页
	  */
	  vm.goNetDetailPage = function () {
		  $state.go('app.net.datilNetWork', {netId: vm.instance.netId});
	  };
	  vm.focus = function (type) {
		  if(type == 'pwd'){
			  vm.isPwdOnFocus = false;
		  }
		  if(type == 'rePwd'){
			  vm.isRePwdOnFocus = false;
		  }
	  };
	  vm.passBlur = function () {
		  vm.isPwdOnFocus = true;
		  vm.repassBlur();
		  vm.checkPasswordFlag = PWService.threeRules(vm.instance.password);
	  };
	  vm.repassBlur = function () {
		  vm.isRePwdOnFocus = true;
		  vm.isRePwdOnFocus = vm.instance.password != vm.instance.repassword;
	  };
      vm.init();
}]);