'use strict';

angular.module('eayunApp.controllers')
/**
 * @ngdoc function
 * @name appRouter
 * @description
 * 配置路由
 */
  .config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('/app/cloud/cloudhost', '/app/cloud/cloudhost/host');
    $urlRouterProvider.when('/app/cloud/cloudhost/monitor/:vmId', '/app/cloud/cloudhost/monitor/:vmId/monitorcpu');
    $stateProvider.state('app.cloud.cloudhost.host', {
      url: '/host',
      templateUrl: 'views/cloudhost/host/hostmng.html',
      controller: 'CloudhostHostCtrl'
    })
    .state('app.cloud.cloudhost.hostdetail',{
    	url: '/hostdetail/:vmId',
        templateUrl: 'views/cloudhost/host/vmdetail.html',
        controller: 'VmDetailController'
    })
    .state('app.cloud.cloudhost.monitor',{
    	url: '/monitor/:vmId',
    	templateUrl: 'views/cloudhost/monitor.html',	//左侧栏列表
    	controller: 'MonitorController'
    })
    .state('app.cloud.cloudhost.monitor.resmntimg',{
    	templateUrl: 'views/monitor/resourcemonitor/imgresmnt.html',	//查询条件
    	controller: 'ImgResourceMonitorCtrl'
    })
    .state('app.cloud.cloudhost.monitor.resmntimg.monitorCPU',{//CPU
    	url: '/monitorcpu',
    	templateUrl: 'views/monitor/resourcemonitor/monitorCPU.html',
    	controller: 'GoMonitorCPUCtrl'
    })
    .state('app.cloud.cloudhost.monitor.resmntimg.monitorRam',{//内存
    	url: '/monitorram',
    	templateUrl: 'views/monitor/resourcemonitor/monitorRam.html',
    	controller: 'GoMonitorRamCtrl'
    })
    .state('app.cloud.cloudhost.monitor.resmntimg.monitorDisk',{//硬盘
    	url: '/monitordisk',
    	templateUrl: 'views/monitor/resourcemonitor/monitorDisk.html',
    	controller: 'GoMonitorDiskCtrl'
    })
    .state('app.cloud.cloudhost.monitor.resmntimg.monitorNet',{	//网络
    	url: '/monitornet',
    	templateUrl: 'views/monitor/resourcemonitor/monitorNet.html',
    	controller: 'GoMonitorNetCtrl'
    }).state('buy.host',{
    	url: '/host/:orderNo',
        templateUrl: 'views/cloudhost/host/buyhost.html',
        controller: 'BuyVmController'
    }).state('buy.confirmvm',{
    	url: '/confirmvm/:orderType',
        templateUrl: 'views/cloudhost/host/orderconfirm.html',
        controller: 'ConfirmVmOrderController'
    }).state('renew.renewhost',{
    	url: '/renewhost',
        templateUrl: 'views/cloudhost/host/vmrenewconform.html',
        controller: 'RenewConformVmController'
        
    });
  })
/**
 * @ngdoc function
 * @name eayunApp.controller:CloudhostHostCtrl
 * @description
 * # CloudhostHostCtrl
 * 云主机--云主机
 */
  .controller('CloudhostHostCtrl', function ($rootScope,$scope,$state,$timeout,eayunHttp,eayunModal,powerService,eayunStorage,VmService,$http) {
	  var list=[];
	  $rootScope.navList(list,'云主机');
	  
	  /**
	   * 
	   * 新增按钮权限控制
	   */
	  powerService.powerRoutesList().then(function(powerList){
			$scope.vmListPermissions = {
					isAddHost : powerService.isPower('host_add'),	//创建云主机
					renewHost : powerService.isPower('host_renew')	//续费
			};
		});
	  
    $scope.myTable = {
      source: 'cloud/vm/listVm.do',
      api : {},
      getParams: function () {
        return {
        	prjId :  $scope.model.projectvoe ? $scope.model.projectvoe.projectId : '',
        	dcId :   $scope.model.projectvoe ? $scope.model.projectvoe.dcId : '',
			queryType : $scope.search ? $scope.search.key :'',
        	title :  $scope.search ? $scope.search.value:'',
    		status : $scope.query ? $scope.query.status :'',
    		system : $scope.query ?$scope.query.system :''
        };
      }
    };
    /**
     * 查询当前sessionStore 是否存在用户信息
     */
    $scope.checkUser = function (){
    	var user = sessionStorage["userInfo"];
    	 if(user){
    		 user = JSON.parse(user);
    		 if(user&&user.userId){
    			 return true;
    		 }
    	 }
    	return false;
    };
    
    /**
     * 查询云主机列表
     */
    $scope.queryHostList = function(){
    	$scope.myTable.api.draw();
    };
    
    /**
     * 监听列表上的查询状态
     */
    $scope.$watch('model.projectvoe', function(newVal,oldVal){
    	if(newVal !== oldVal){
    		$scope.myTable.api.draw();
    	}
    });
    
    $scope.$watch('query.status' , function(newVal,oldVal){
    	if(newVal !== oldVal){
    		$scope.myTable.api.draw();
    	}
    });
    
    $scope.init = function (){
    	$scope.vmStatusList = [{
    		'nodeNameEn':'-1',
    		'nodeName':'全部状态'
    	},{
    		'nodeNameEn':'ACTIVE',
    		'nodeName':'运行中'
    	},{
    		'nodeNameEn':'SHUTOFF',
    		'nodeName':'已关机'
    	},{
    		'nodeNameEn':'ERROR',
    		'nodeName':'故障'
    	},{
    		'nodeNameEn':'1',
    		'nodeName':'余额不足'
    	},{
    		'nodeNameEn':'2',
    		'nodeName':'已到期'
    	}];
    };
    
    /**
     * 选择云主机状态
     */
    $scope.selectVmStatus = function(item,event){
    	$scope.query ={};
    	$scope.query.status = null;
    	if(item.nodeNameEn != '-1'){
    		$scope.query.status = item.nodeNameEn;
    	}
    };
    
    $scope.options = {
            searchFn: function () {
            	if(!$scope.checkUser()){
                	return ;
                }
            	$scope.myTable.api.draw();
            },
            placeholder:"请输入查询内容",
            select: [{name: '名称'}, {tag: '标签'}, {ips: 'IP地址'}]
        };
    
    /**
     * 购买云主机
     */
    $scope.createVm=function (){
    	if($scope.model.projectvoe.projectId){
			  eayunHttp.post('cloud/vm/getSubNetList.do',$scope.model.projectvoe.projectId)
			  .then(function (response){
				  if(response.data&&response.data.length>0){
					  var page = eayunModal.open({
					        templateUrl: 'views/cloudhost/host/edithost.html',
					        controller: 'AddVmController',
					        resolve:{
				            	prjId:function (){
				            		return {prjId:$scope.model.projectvoe ? $scope.model.projectvoe.projectId : ''};
				            	}
				            }
					      });
					     page.result.then(function (value){
					    	 $scope.myTable.api.draw();
				    		  
					      }, function () {
					    	  $scope.myTable.api.draw();
					      });
				  }
				  else{
					  eayunModal.confirm('当前项目下无子网，是否立即创建？').then(function () {
						  $state.go('app.net.netbar');
					  });
				  }
			  }); 
		  }
    	
	    
    };
    
    
    $scope.$watch("myTable.result",function (newVal,oldVal){
    	if(newVal !== oldVal){
    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
    			for(var i=0;i<$scope.myTable.result.length;i++){
    				var status=$scope.myTable.result[i].vmStatus.toString().toLowerCase();
    				if("active"!=status
    						&&"error"!=status
    						&&"shutoff"!=status
    						&&"suspended"!=status
    						&&"verify_resize"!=status){
    					$timeout($scope.refreshList,5000);
    					break;
    				}
    				
    			}
    		}
    	}
    });
    /**
     * 如果列表中有中间状态的云主机，间隔5s刷新列表
     */
    $scope.refreshList = function (){
    	if(!$scope.checkUser()){
        	return ;
        }
    	$scope.myTable.api.refresh();
    };
    
    /**
     * 管理云主机
     */
    $scope.manageVm = function (cloudvm){
    	$state.go('app.cloud.cloudhost.hostdetail',{"detailType":'host',"vmId":cloudvm.vmId});
    };
    
    /**
     * 云主机状态 显示
     */
    $scope.checkVmStatus =function (model){
    	if('1'==model.chargeState||'2'==model.chargeState||'3'==model.chargeState){
    		return 'ey-square-disable';
    	}
    	else if(model.vmStatus&&model.vmStatus=='ACTIVE'){
			return 'ey-square-right';
		}  
		else if(model.vmStatus=='SHUTOFF'){
			return 'ey-square-disable';
		}
		else if(model.vmStatus=='SUSPENDED' || model.vmStatus=='ERROR'){
			return 'ey-square-error';
		}
		else{
			return'ey-square-warning';
		}
    };
    
    /**
     * 购买云主机
     */
    
    $scope.buyVm = function (){
    	$state.go('buy.host',{orderNo:'000000'})
    };
    
    /**
     * 云主机--续费
     */
    $scope.renewVm = function(item){
    	var result = eayunModal.open({
	        templateUrl: 'views/cloudhost/host/vmrenew.html',
	        controller: 'cloudhostRenewCtrl',
	        resolve: {
	            item:function (){
            		return item;
            	},
    	        volume:function(){
    	        	return eayunHttp.post('cloud/volume/getSysVolumeByVmId.do',item.vmId).then(function(response){
    	       		   return response.data;
    	       	    });
    	        }
	        }
	      });
    	result.result.then(function (value) {
    		VmService.checkIfOrderExist(item.vmId).then(function(response){
                eayunModal.warning("资源正在调整中或您有未完成的订单，请稍后再试。");
            },function(){
                $state.go('renew.renewhost');

            });
	    	  
	      }, function () {
	      });
    };
    
    $scope.init();
  })
  .controller('BuyVmController', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage,eayunMath){
	  $scope.model={};
	  $scope.vmMaxlength=20;
	  $scope.checkVmName = true;
	  $scope.model.loginType = 'pwd';
	  $scope.typeSure=false;
	  
	  $scope.init = function(){
		  var orderNo = $stateParams.orderNo;
		  if(orderNo){
			  if(orderNo == '000000'){
				  $scope.model.payType = '1';
			  }
			  else if(orderNo == '000001'){
				  $scope.model.payType = '2';
			  }
			  else{
				  eayunHttp.post('cloud/vm/queryCloudOrderByOrderNo.do',orderNo).then(function (response){
					  if(response && response.data && response.data.data){
						  var data = response.data.data;
						  $scope.model = angular.copy(data);
						  $scope.formData();
						  $scope.model.password = null;
						  $scope.model.repassword = null;
						  $scope.model.secretKey = null;
						  $scope.isEdit = true;
						  if($scope.model.vmFrom === 'publicImage'){
							  if(!$scope.model.os){
								  $scope.model.os={};
								  if($scope.model.osType === '0007002002001'){
									  $scope.model.os.nodeId= $scope.model.osType;
								  }
								  else{
									  $scope.model.os.nodeId= $scope.model.sysType;
								  }
							  }
						  }
						  if($scope.model.vmFrom === 'privateImage'){
							  $scope.model.sourceType ='all';
						  }
						  else if($scope.model.vmFrom === 'marketImage'){
							  $scope.model.professionType ='all';
						  }
						  if($scope.model.loginType == 'ssh'){
							  $scope.querySecretKeyList();
						  }
					  }
					  else{
						  orderNo == '000000';
						  $scope.model.payType = '1';
						  
						  $scope.newData();
					  }
					  $scope.initData();
				  });
			  }
		  }
		  if(orderNo == '000000' || orderNo =='000001' ){
			  var item = eayunStorage.get('order_back_vm');
			  eayunStorage.delete('order_back_vm');
			  if(!item){
				  $scope.newData();
			  }
			  else{
				  //返回修改配置
				  $scope.model = angular.copy(item);
				  if(!$scope.model.os){
					  $scope.model.os={};
					  if($scope.model.osType === '0007002002001'){
						  $scope.model.os.nodeId= $scope.model.osType;
					  }
					  else{
						  $scope.model.os.nodeId= $scope.model.sysType;
					  }
				  }
				  if(item.from && item.from == '1'){
					  $scope.isEdit = false;
				  }
				  else{
					  $scope.isEdit = true;
				  }
				  if($scope.model.loginType == 'ssh'){
					  $scope.querySecretKeyList();
				  }
				  $scope.formData();
			  }
			  $scope.initData();
		  }
	  };
	  
	  /**
	   * 新进入界面
	   */
	  $scope.newData = function(){
		  var dcPrj = sessionStorage["dcPrj"];
		  if(dcPrj){
			  dcPrj = JSON.parse(dcPrj);
			  $scope.model.prjId = dcPrj.projectId;
		  }
		  $scope.model.number=1;
		  $scope.model.vmFrom ='publicImage';
		  
		  //初始化系统盘数据盘类型
		  $scope.model.sysDiskType='1';
		  $scope.model.dataDiskType='1';
		  $scope.model.dataDisk=0;
	  };
	  
	  /**
	   * 从有值的地方赋值过来
	   */
	  $scope.formData = function(){
		  if("1" == $scope.model.buyFloatIp){
			  $scope.model.isBuyFloatIp = true;
		  }
		  $scope.model.vmFrom = $scope.model.imageType;
		  $scope.model.ram = $scope.model.ram/1024;
		  $scope.model.repassword = $scope.model.password;
		  $scope.model.number = $scope.model.count;
		  if(!$scope.model.number || $scope.model.number>20){
			  $scope.model.number = 1;
		  }
		  $scope.queryBuyCycle();
		  if($scope.model.payType == '1'){
			  $scope.model.buyCycle = $scope.model.buyCycle;
		  }
	  };
	  /**
	   * 购买初始化数据
	   */
	  $scope.initData = function(){
		  $scope.queryDcAnd();
		  $scope.buyCycleType();
		  if($scope.model.payType == '2'){
			  $scope.queryAccount();
		  }
	  };
	  
	  /**
	   * 查询数据中心和项目
	   */
	  $scope.queryDcAnd = function(){
		  eayunHttp.post('cloud/vm/getDatacenterProjectList.do').then(function (response){
			  $scope.datacenters = response.data;
			  if(response.data.length>0){
				  var initialize = false;
				  angular.forEach($scope.datacenters, function (value, key) {
					  if(value.projectId == $scope.model.prjId){
						  initialize = true;
						  $scope.selectDc(value);
					  }
				  });
				  if(!initialize){
					  var value = $scope.datacenters[0];
					  $scope.selectDc(value);
				  }
			  }
		  });
	  };
	  
	  /**
	   * 购买类型
	   */
	  $scope.buyVm = function(type){
		  var orderNo = '000000';
		  if(type == '2'){
			  orderNo = '000001';
		  }
		  $scope.model.payType = type;
		  var data ={};
		  $scope.setData(data);
		  data.from = '1';
		  eayunStorage.set('order_back_vm',data);
		  $state.go('buy.host',{orderNo:orderNo});
	  };
	  
	  /**
	   * 选择数据中心
	   */
	  $scope.selectDc = function (data){
		  $scope.model.dcId = data.dcId;
		  $scope.model.prjId = data.projectId;
		  $scope.model.dcName = data.dcName;
		  
		  $scope.getVolumeTypes($scope.model.dcId);
		  
		  $scope.queryNetListByPrj($scope.model.prjId);
		  $scope.querySecurityByPrj($scope.model.prjId);
		  $scope.checkVmNameExist();
		  if($scope.model.vmFrom && $scope.model.vmFrom==='publicImage'){
			  $scope.queryOsList();
		  }
		  if($scope.model.vmFrom && $scope.model.vmFrom === 'marketImage'){
			  $scope.selectVmFrom('marketImage');
		  }
		  $scope.queryPrjQuota();
		  if(($scope.model.os && $scope.model.vmFrom==='publicImage')
				  ||($scope.model.sourceType && $scope.model.vmFrom==='privateImage')
				  ||($scope.model.professionType && $scope.model.vmFrom==='marketImage')){
			  $scope.queryImageList();
		  }
		  
		  $scope.calcBillingFactor();
	  };
	  
	  
	  /**
	   * 选择系统盘类型
	   */
	  $scope.selectSysType=function(data){
		  $scope.model.sysDiskType=data.volumeType;
		  $scope.model.sysTypeId=data.typeId;
		  $scope.model.sysTypeAs=data.volumeTypeAs;
		  $scope.calcBillingFactor();
	  };
	  
	  /**
	   * 选择数据盘类型
	   */
	  $scope.selectDataType=function(data){
		  $scope.model.dataDiskType=data.volumeType;
		  $scope.model.dataTypeId=data.typeId;
		  $scope.model.dataTypeAs=data.volumeTypeAs;
		  $scope.maxDisk=data.maxSize;
		  $scope.calcBillingFactor();
	  };
	  
	  
	  
	  /**
	   * 查询当前数据中心下可用的云硬盘类型
	   */
	  $scope.getVolumeTypes=function(dcId){
		  eayunHttp.post('cloud/volume/getVolumeTypesByDcId.do',dcId).then(function (response){
			  $scope.volumeTypeList = response.data;
			  if(response.data.length>0){
				  $scope.typeSure=false;
				  var initialize = false;
				  var initialize1 = false;
				  angular.forEach($scope.volumeTypeList, function (value, key) {
					  if(value.typeId == $scope.model.sysTypeId){
						  initialize = true;
						  $scope.selectSysType(value);  
					  }
					  if(value.typeId == $scope.model.dataTypeId){
						  initialize1 = true;
						  $scope.selectDataType(value);  
					  }
				  });
				  
				  if(!initialize){
					  $scope.selectSysType($scope.volumeTypeList[0]);
				  }
				  if(!initialize1){
					  $scope.selectDataType($scope.volumeTypeList[0]);
				  }
				 
			  }else{
				  $scope.typeSure=true;
			  }
	     }); 
		  
	  };
	  
	  
	  
	  //拖动条
	  $scope.formate=function(step){
		  return Number((step*100).toFixed());
	  };
	  
	  
	  $scope.parse=function(value){
		  return Number((value/100+0.044444).toFixed(1));
	  };
	  
	  
	  /**
	   * 选择私有网络
	   */
	  
	  $scope.selectNetwork = function (){
		  $scope.model.netId = $scope.model.network.netId;
		  $scope.model.netName = $scope.model.network.netName;
		  if($scope.model.network.gatewayIp == 'null'){
			  $scope.model.isBuyFloatIp = false;
			  $scope.selectBuyFloatIp();
		  }
		  $scope.querySubnetListByNetId($scope.model.netId);
	  };
	  
	  /**
	   * 选择镜像类型
	   */
	  $scope.selectVmFrom = function(imageType){
		$scope.model.vmFrom = imageType;
		if('publicImage'=== imageType){
			$scope.queryOsList();
		}
		if('privateImage'=== imageType && !$scope.model.sourceType){
			$scope.model.sourceType = 'all';
		}
		if('marketImage'=== imageType){
			eayunHttp.post('cloud/vm/getMarketImageTypeList.do').then(function(response){
				  $scope.professionTypeList =  response.data;
				  
			});
			if(!$scope.model.professionType){
				$scope.model.professionType = 'all';
			}
		}
		
		if(($scope.model.os && imageType==='publicImage')||
				($scope.model.sourceType && imageType==='privateImage')||
				($scope.model.professionType && imageType==='marketImage')){
			$scope.queryImageList();
		}
	  };
	  
	  /**
	   * 选择自定义镜像的来源镜像类型
	   */
	  $scope.selectPrivateImage = function(imageType){
		$scope.model.sourceType = imageType;
		$scope.queryImageList();
	  };
	  
	  /**
	   * 选择市场镜像的业务类型
	   */
	  $scope.selectMarketImage= function(data){
		  if(data){
			  $scope.model.professionType = data.nodeId;
		  }
		  else{
			  $scope.model.professionType = 'all';
		  }
		  $scope.queryImageList();
	  };
	  
	  /**
	   * 选择是否购买公网IP
	   */
	  $scope.selectBuyFloatIp = function(){
		  $scope.calcBillingFactor();
		  $scope.checkVmQuota();
	  };
	  
	  
	  /**
	   * 选择CPU
	   */
	  $scope.selectCpu = function (data){
		  $scope.model.cpu = data.nodeName.substr(0,data.nodeName.length-1);
		  $scope.queryRamList(data.nodeId);
		  $scope.checkVmQuota();
	  };
	  
	  /**
	   * 选择内存
	   */
	  $scope.selectRam = function (data){
		  $scope.model.ram = data.nodeName.substr(0,data.nodeName.length-2);
		  $scope.calcBillingFactor();
		  $scope.checkVmQuota();
	  };
	  
	  /**
	   * 选择镜像类型
	   */
	  $scope.selectOs = function(data){
		  $scope.model.os = data;
		  if("Windows" == data.nodeName){
			  $scope.model.osType = data.nodeId;
		  }
		  else{
			  $scope.model.sysType = data.nodeId;
			  $scope.model.osType = data.paraentId;
		  }
		  $scope.queryImageList();
	  };
	  
	  /**
	   * 资源计费查询
	   */
	  $scope.calcBillingFactor = function(){
		  $scope.priceError = null;
		  $scope.priceDetails = null;
		  var data = {};
		  
		  data.dcId = $scope.model.dcId;
		  data.payType = $scope.model.payType;
		  data.number = $scope.model.number;
		  data.cpuSize = $scope.model.cpu;
		  data.ramCapacity = $scope.model.ram;
		  
		  //分类计价系统盘
		  if('1'==$scope.model.sysDiskType){
			  data.sysDiskOrdinary=$scope.model.disk;
		  }else if('2'==$scope.model.sysDiskType){
			  data.sysDiskBetter=$scope.model.disk;
		  }else if('3'==$scope.model.sysDiskType){
			  data.sysDiskBest=$scope.model.disk;
		  }else{
			  data.sysDiskCapacity = $scope.model.disk;
		  }
		  
		  //分类计价数据盘
		  if('1'==$scope.model.dataDiskType){
			  data.dataDiskOrdinary=$scope.model.dataDisk;
		  }else if('2'==$scope.model.dataDiskType){
			  data.dataDiskBetter=$scope.model.dataDisk;
		  }else if('3'==$scope.model.dataDiskType){
			  data.dataDiskBest=$scope.model.dataDisk;
		  }else{
			  data.dataDiskCapacity = $scope.model.dataDisk;
		  }
		  
		  
		  data.cycleCount = 1;
		  if($scope.model.image){
			  if($scope.model.vmFrom==='publicImage'|| $scope.model.vmFrom==='marketImage'){
				  data.imageId = $scope.model.image.imageId;
			  }
			  else if($scope.model.vmFrom==='privateImage'){
				  data.imageId = $scope.model.image.sourceId;
			  }
		  }
		  if(data.payType == '1' && $scope.model.buyCycle){
			  data.cycleCount = $scope.model.buyCycle;
		  }
		  if($scope.model.isBuyFloatIp){
			  data.ipCount = 1;
		  }
		  if(data.number && data.number){
			  eayunHttp.post('cloud/vm/getPriceDetails.do',data).then(function (response){
				  if(response&&response.data){
					  if(response.data.respCode == '010120'){
						  $scope.priceError = response.data.message;
					  }
					  else{
						  $scope.priceDetails = response.data.data;
						  
						  $scope.unitPrice();
					  }
				  }
			  });
		  }
	  };
	  
	  /**
	   * 单位处理
	   */
	  $scope.unitPrice = function(){
		  $scope.specPrice = 0;
		  $scope.sysDiskPrice = 0;
		  $scope.imagePrice = 0;
		  $scope.floatPrice = 0;
		  $scope.dataDiskPrice=0;
		  
		  if($scope.model.number && $scope.priceDetails && $scope.priceDetails.cpuPrice){
			  $scope.specPrice = $scope.priceDetails.cpuPrice;
		  }
		  if($scope.model.number && $scope.priceDetails && $scope.priceDetails.sysDiskPrice){
			  $scope.sysDiskPrice = $scope.priceDetails.sysDiskPrice;
		  }
		  if($scope.model.number && $scope.priceDetails && $scope.priceDetails.imagePrice){
			  $scope.imagePrice = $scope.priceDetails.imagePrice;
		  }
		  if($scope.model.number && $scope.priceDetails && $scope.priceDetails.ipPrice){
			  $scope.floatPrice = $scope.priceDetails.ipPrice; 
		  }
		  if($scope.model.number && $scope.priceDetails && $scope.priceDetails.dataDiskPrice){
			  $scope.dataDiskPrice = $scope.priceDetails.dataDiskPrice; 
		  }
	  };
	  
	  /**
	   * 查看账户余额
	   */
	  $scope.queryAccount = function (){
		  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
			  $scope.payAfpterPayment = response.data;
		  });
	  };
	  
	  
	  /**
	   * 选择镜像
	   */
	  
	  $scope.selectImage = function(data,isRestore){
		  $scope.model.image = data;
		  if(!isRestore){
			  $scope.model.cpu = data.minCpu;
			  $scope.model.ram = data.minRam/1024;
		  }
		  if(data.imageIspublic ==='1' && data.osType==='0007002002001'){
			  $scope.model.disk = 60;
		  }
		  else if(data.imageIspublic ==='1' && data.osType==='0007002002002'){
			  $scope.model.disk = 20;
		  }
		  if(data.imageIspublic ==='2'){
			  $scope.model.disk = data.minDisk;
		  }
		  else if(data.imageIspublic ==='3'){
			  $scope.model.disk = data.sysdiskSize;
		  }
		  if(data.osType==='0007002002001'){
			  $scope.model.username = 'Administrator';
			  if($scope.model.loginType == 'ssh'){
				  $scope.model.loginType = 'pwd';
			  }
		  }
		  else{
			  $scope.model.username = 'root';
		  }
		  
		  $scope.queryCpuList();
		  $scope.checkVmQuota();
	  };
	  
	  /**
	   * 查询项目下的网络列表
	   */
	  $scope.queryNetListByPrj = function(prjId){
		  $scope.subnetList = [];
		  $scope.selfSubnetList = [];
		  $scope.model.subnet = null;
		  $scope.model.selfSubnet = null;
		  
		  $scope.netList = [];
		  $scope.model.network = null;
		  eayunHttp.post('cloud/vm/queryNetListByPrjId.do',prjId).then(function (response){
			  $scope.netList = response.data.data;
			  if($scope.netList.length>0){
				  var initialize = false;
				  angular.forEach($scope.netList, function (value, key){
					  if($scope.model.netId == value.netId){
						  initialize = true;
						  
						  $scope.model.network = value;
					  }
				  });
				  if(!initialize){
					  $scope.model.network = $scope.netList[0];
				  }
				  $scope.selectNetwork();
			  }
			  else{
				  $scope.model.network = null;
			  }
		  });
	  };
	  
	  
	  /**
	   * 查询网络下的自管子网和受管子网
	   */
	  $scope.querySubnetListByNetId = function (netId){
		  $scope.subnetList = [];
		  $scope.selfSubnetList = [];
		  $scope.model.selfSubnet = null;
		  $scope.model.subnet = null;
		  
		  //查询网络的受管子网列表 
		  eayunHttp.post('cloud/subnetwork/getsubnetlist.do',{ 'netId':netId,'subnetType':'1'}).then(function(response){
			  $scope.subnetList = response.data.resultData;
			  if($scope.subnetList.length>0){
				  var initialize = false;
				  angular.forEach($scope.subnetList, function (value, key){
					  if($scope.model.subnetId == value.subnetId){
						  initialize = true;
						  $scope.model.subnet = value;
					  }
				  });
				  if(!initialize){
					  $scope.model.subnet = $scope.subnetList[0];
				  }
			  }
			  else{
				  $scope.model.subnet = null;
			  }
		  });
		  
		  //查询网络的自管子网列表 
		  eayunHttp.post('cloud/subnetwork/getsubnetlist.do',{ 'netId':netId,'subnetType':'0'}).then(function(response){
			  $scope.selfSubnetList = response.data.resultData;
			  if($scope.selfSubnetList.length>0){
				  var initialize = false;
				  angular.forEach($scope.selfSubnetList, function (value, key){
					  if($scope.model.selfSubnetId == value.subnetId){
						  initialize = true;
						  
						  $scope.model.selfSubnet = value ;
					  }
				  });
				  if(!initialize){
					  $scope.model.selfSubnet ='-1';
				  }
			  }else{
				  $scope.model.selfSubnet ='-1';
			  }
		  });
	  };
	  
	  
	  /**
	   * 子网名称显示
	   */
	  $scope.substrSubnetName = function (text){
		  var testSubstr = text;
		  if(text.indexOf('(')!= -1){
			  var perText = text.substr(0,text.indexOf('('));
			  if(perText.length>9){
				  perText = perText.substr(0,9)+"...";
			  }
			  testSubstr = perText + text.substr(text.indexOf('('),text.length-1);
		  }
		  return testSubstr;
	  };
	  
	  /**
	   * 查询CPU列表
	   */
	  $scope.queryCpuList = function(){
		  $scope.cpuList = [];
		  $scope.ramList = [];
		  var selCpu = false;
		  
		  eayunHttp.post('cloud/vm/getCpuList.do')
		  .then(function (response){
			  if(response.data.length>0){
				  var i = 0;
				  angular.forEach(response.data, function (value, key) {
					  var cpuVal = value.nodeName.substr(0,value.nodeName.length-1);
					  if(($scope.model.image && cpuVal >= $scope.model.image.minCpu) && 
							  ((cpuVal <= $scope.model.image.maxCpu && $scope.model.image.maxCpu>0) 
									  || $scope.model.image.maxCpu == 0 )){
						  $scope.cpuList[i++] = value;
					  }
					  else if(!$scope.model.image){
						  $scope.cpuList[i++] = value;
					  }
				  });
				  angular.forEach($scope.cpuList, function (value, key){
					  var cpuVal = value.nodeName.substr(0,value.nodeName.length-1);
					  if(cpuVal == $scope.model.cpu){
						  $scope.selectCpu(value);
						  selCpu = true;
					  }
				  });
				  
				  if($scope.cpuList && $scope.cpuList.length > 0 && !selCpu){
					  $scope.selectCpu($scope.cpuList[0]);
				  }
			  }
		  });
	  };
	  
	  
	  /**
	   * 根据CPU-nodeId查询内存列表
	   */
	  $scope.queryRamList = function (nodeId){
		  $scope.ramList = [];
		  var selRam = false;
		  eayunHttp.post('cloud/vm/getRamListByCpu.do',nodeId)
		  .then(function (response){
			  if(response.data.length>0){
				  var i = 0;
				  var sameValue = false;
				  angular.forEach(response.data, function (value, key) {
					  var ramVal = value.nodeName.substr(0,value.nodeName.length-2);
					  if(($scope.model.image &&ramVal >= $scope.model.image.minRam/1024) && 
							  ((ramVal <= $scope.model.image.maxRam/1024 && $scope.model.image.maxRam >0) 
									  || $scope.model.image.maxRam == 0)){
						  $scope.ramList[i++] = value;
					  }
					  else if(!$scope.model.image){
						  $scope.ramList[i++] = value;
					  }
				  });
				  angular.forEach($scope.ramList, function (value, key){
					  var ramVal = value.nodeName.substr(0,value.nodeName.length-2);
					  if(ramVal == $scope.model.ram){
						  $scope.selectRam(value);
						  selRam = true;
					  }
				  });
				  
				  if($scope.ramList && $scope.ramList.length>0 && !selRam){
					  $scope.selectRam($scope.ramList[0]);
				  }
			  }
			  $scope.calcBillingFactor();
		  });
	  };
	  
	  /**
	   * 查询操作系统类型
	   */
	  $scope.queryOsList = function(){
		  if(!$scope.model.os){
			  $scope.model.os = {};
		  }
		  $scope.osList = [];
		  
		  eayunHttp.post('cloud/vm/getOsList.do')
		  .then(function (response){
			  $scope.osList = response.data;
			  if($scope.osList.length>0){
				  var initialize = false;
				  angular.forEach($scope.osList, function (value, key) {
					  if($scope.model.os && $scope.model.os.nodeId === value.nodeId){
						  initialize = true;
						  $scope.selectOs(value);
					  }
				  });
				  
				  if(!initialize){
					  angular.forEach($scope.osList, function (value, key) {
						  if(value.nodeName == 'Windows'){
							  $scope.selectOs(value);
						  }
					  });
				  }
				  
			  }
		  });
	  };
	  
	  /**
	   * 查询镜像列表
	   */
	  $scope.queryImageList = function (){
		  var data = angular.copy($scope.model.os);
		  $scope.imageList = [];
		  $scope.model.image = null;
		  var params = {};
		  params.dcId = $scope.model.dcId;
		  params.prjId = $scope.model.prjId;
		  if($scope.model.vmFrom === 'publicImage'){
			  params.vmFrom= '1';
			  if("Windows" === data.nodeName){
				  params.osType = data.nodeId;
			  }
			  else{
				  params.osType = data.parentId;
				  params.sysType = data.nodeId;
			  }
		  }
		  else if($scope.model.vmFrom === 'privateImage'){
			  params.vmFrom= '2';
			  if($scope.model.sourceType && $scope.model.sourceType!='all'){
				  params.sourceType = $scope.model.sourceType;
			  }
		  }
		  else if($scope.model.vmFrom === 'marketImage'){
			  params.vmFrom= '3';
			  if($scope.model.professionType && $scope.model.professionType!='all'){
				  params.professionType = $scope.model.professionType;
			  }
		  }
		  
		  eayunHttp.post('cloud/vm/getImageList.do',params)
		  .then(function (response){
			  $scope.imageList = response.data;
			  if($scope.imageList.length>0){
				  var initialize = false;
				  angular.forEach($scope.imageList, function (value, key) {
					  if($scope.model.imageId == value.imageId){
						  initialize = true;
						  $scope.selectImage(value,true)
					  }
				  });
				  if(!initialize){
					  $scope.selectImage($scope.imageList[0],false);
				  }
			  }
			  else{
				  $scope.model.cpu = 1;
				  $scope.model.ram = 1;
				  $scope.model.disk = 0;
				  $scope.queryCpuList();
				  $scope.checkVmQuota();
			  }
				  
		  });
	  };
	  
	  $scope.focus = function(_value){
		  if(_value == 'rePwd'){
			  $scope.isRePwdOnFocus= false;
		  }
		  else if(_value == 'pwd'){
			  $scope.isPwdOnFocus = false;
		  }
	  };
	  
	  $scope.passBlur = function(){
		  $scope.checkPassword();
		  $scope.repassBlur();
		  $scope.isPwdOnFocus = true;
	  };
	  
	  $scope.repassBlur = function(){
		  $scope.isRePwdOnFocus = $scope.model.password != $scope.model.repassword;
	  };
	  
	  /**
	   * 校验密码的合法性和约束性
	   */
	  $scope.checkPassword = function (){
		  var pwd = $scope.model.password;
		  var numFlag = 0;
		  var lowerCharFlag = 0;
		  var upperCharFlag = 0;
		  var specCharFlag = 0;
		  var regex =new RegExp("^[0-9a-zA-Z~@#%+-=\/\(_\)\*\&\<\>\[\\]\\\\\"\;\'\|\$\^\?\!.\{\}\`/\,]{8,30}$");
		  var regexNum =new RegExp("^[0-9]$");
		  var regexLowerChar =new RegExp("^[a-z]$");
		  var regexUpperChar =new RegExp("^[A-Z]$");
		  var regexSpecChar =new RegExp("^[~@#%+-=\/\(_\)\*\&\<\>\[\\]\\\\\"\;\'\|\$\^\?\!.\{\}\`/\,]$");
		  if(pwd&&regex.test(pwd)){
			  for(var i=0;i<pwd.length;i++){
				  if(pwd[i]&&regexNum.test(pwd[i])){
					  numFlag = 1;
					  continue;
				  }
				  else if(pwd[i]&&regexLowerChar.test(pwd[i])){
					  lowerCharFlag = 1;
					  continue;
				  }
				  else if(pwd[i]&&regexUpperChar.test(pwd[i])){
					  upperCharFlag = 1;
					  continue;
				  }
				  else if(pwd[i]&&regexSpecChar.test(pwd[i])){
					  specCharFlag = 1;
					  continue;
				  }
			  }
		  }
		  $scope.checkPasswordFlag = (numFlag+lowerCharFlag+upperCharFlag+specCharFlag)<3;
	  };
	  
	  /**
	   * 查询SSH密钥列表
	   */
	  $scope.querySecretKeyList = function (){
		  var initialize = false;
		  var _data = {
				  'dcId':$scope.model.dcId,
				  'prjId':$scope.model.prjId
		  };
		  eayunHttp.post('safety/secretKey/getsecretlistbyprj.do',_data).then(function(response){
			  if(response && response.data){
				  $scope.secretKeyList = response.data;
			  }
		  })
	  };
	  
	  /**
	   * 校验云主机重名（项目维度、云主机数量）
	   */
	  $scope.checkVmNameExist = function (){
		  $scope.checkVmName = true;
		  var vmNum=$scope.model.number;
		  if(vmNum==""||vmNum==null||vmNum==undefined){
			  vmNum = 1;
		  }
		  if($scope.model.prjId && $scope.model.dcId && $scope.model.vmName && $scope.model.number<21){
			  var cloudVm ={}
			  cloudVm.dcId= $scope.model.dcId;
			  cloudVm.prjId = $scope.model.prjId;
			  cloudVm.vmName = $scope.model.vmName;
			  cloudVm.number = vmNum;
			  var vname = $scope.model.vmName;
			  eayunHttp.post('cloud/vm/checkVmExistByName.do',cloudVm)
			  .then(function (response){
				  if(vname === $scope.model.vmName){
					  $scope.checkVmName = response.data;
				  }
			  });
		  }
	  };
	  
	  /**
	   * 查询项目下默认的安全组
	   */
	  $scope.querySecurityByPrj = function (prjId){
		  $scope.sgList = [];
		  
		  eayunHttp.post('cloud/vm/querySgListByPrjId.do',prjId)
		  .then(function (response){
				  $scope.sgList = response.data.data;
				  if($scope.sgList.length>0){
					  var initialize = false;
					  angular.forEach($scope.sgList, function (value, key) {
						  if($scope.model.sgId == value.sgId){
							  initialize = true;
						  }
					  });
					  
					  if(!initialize){
						  angular.forEach($scope.sgList, function (value, key) {
							  if(value.sgName == '默认安全组'){
								  $scope.model.sgId = value.sgId;
							  }
						  });
					  }
				  }
		  });
	  };
	  
	  
	  /**
	   * 检验创建云主机的数量(1-20)
	   * 检验当前项目下可创建的云主机数量
	   * 检验批量创建云主机名称重名
	   */
	  $scope.checkVmNum = function(){
		  var vmNum=$scope.model.number;
		  if(vmNum && vmNum>1){
			  vmNum=vmNum+"";
			  $scope.vmMaxlength=19-vmNum.length;
		  }
		  else{
//			  $scope.model.number = 1;
		  }
		  if(vmNum && vmNum==1){
			  $scope.vmMaxlength=20;
		  }
		  $scope.checkVmNameExist();
		  $scope.checkVmQuota();
		  $scope.calcBillingFactor();
	  };
	  
	  
	  /**
	   * 购买周期类型
	   */
	  $scope.buyCycleType = function(){
		  $scope.cycleTypeList = [];
		  eayunHttp.post('cloud/vm/queryBuyCycleType.do').then(function (response){
			  if(response && response.data){
				  $scope.cycleTypeList = response.data.data;
			  }
			  
			  if($scope.cycleTypeList.length>0){
				  if(!$scope.model.cycleType){
					  $scope.model.cycleType = $scope.cycleTypeList[0].nodeId;
					  $scope.queryBuyCycle();
				  }
			  }
		  });
		  
	  };
	  
	  /**
	   * 选择购买周期类型
	   */
	  $scope.changeCycleType = function(){
		  $scope.model.buyCycle = null;
		  
		  $scope.queryBuyCycle();
	  };
	  
	  /**
	   * 购买周期选择
	   */
	  $scope.queryBuyCycle = function(){
		  $scope.cycleList = [];
		  eayunHttp.post('cloud/vm/queryBuyCycleList.do',$scope.model.cycleType).then(function (response){
			  if(response && response.data){
				  $scope.cycleList = response.data.data;
			  }
			  
			  if($scope.cycleList.length>0){
				  if(!$scope.model.buyCycle){
					  $scope.model.buyCycle = $scope.cycleList[0].nodeNameEn;
					  $scope.calcBuyCycle();
				  }
			  }
		  });
		  
	  };
	  
	  /**
	   * 计算购买周期
	   */
	  $scope.calcBuyCycle = function(){
		  $scope.calcBillingFactor();
	  };
	  
	  /**
	   * 立即充值
	   */
	  $scope.recharge = function(){
		  var routeUrl = "app.costcenter.guidebar.account";
		  var rechargeNavList = [{route:routeUrl,name:'账户总览'}];
		  eayunStorage.persist('rechargeNavList',rechargeNavList);
		  $state.go('pay.recharge');
	  };
	  
	  /**
	   * 购买云主机，调整到提交订单的页面
	   */
	  $scope.commitBuyVm = function (){
		  $scope.isNSF = false;
		  if($scope.model.payType == '2'){
			  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
				  $scope.account = response.data.data;
				  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
					  $scope.payAfpterPayment = response.data;
					  $scope.isNSF = $scope.account.money < $scope.payAfpterPayment;
					  if($scope.isNSF){
						  return ;
					  }
					  
					  var data = {}
					  $scope.setData(data);
					  eayunStorage.set('order_confirm_vm',data);
					  $state.go('buy.confirmvm',{orderType:'0'});
				  });
			  });
		  }
		  else{
			  var data = {}
			  $scope.setData(data);
			  eayunStorage.set('order_confirm_vm',data);
			  $state.go('buy.confirmvm',{orderType:'0'});
		  }
		  
	  };
	  
	  /**
	   * 组装数据
	   */
	  $scope.setData = function (data){
		  data.dcId = $scope.model.dcId;
		  data.dcName = $scope.model.dcName;
		  data.prjId = $scope.model.prjId;
		  if($scope.model.network){
			  data.netId = $scope.model.network.netId;
			  data.netName = $scope.model.network.netName;
		  }
		  if($scope.model.subnet){
			  data.subnetId = $scope.model.subnet.subnetId;
			  data.subnetName = $scope.model.subnet.subnetName;
			  data.cidr = $scope.model.subnet.cidr;
		  }
		  if($scope.model.selfSubnet && $scope.model.selfSubnet != '-1'){
			  data.selfSubnetId = $scope.model.selfSubnet.subnetId;
			  data.selfSubnetName = $scope.model.selfSubnet.subnetName;
			  data.selfCidr = $scope.model.selfSubnet.cidr;
		  }
		  data.buyFloatIp = $scope.model.isBuyFloatIp?'1':'0';
		  data.imageType = $scope.model.vmFrom;
		  if($scope.model.image){
			  data.osType = $scope.model.image.osType;
			  data.sysType = $scope.model.image.sysType;
			  data.imageId = $scope.model.image.imageId;
			  data.sourceId = $scope.model.image.sourceId;
			  data.imageName = $scope.model.image.imageName;
			  
			  if($scope.model.image.sysTypeEn && 'null' != $scope.model.image.sysTypeEn){
				  data.sysTypeEn = $scope.model.image.sysTypeEn;
			  }
		  }
		  data.cpu = $scope.model.cpu;
		  data.ram = $scope.model.ram*1024;
		  data.disk = $scope.model.disk;
		  data.dataDisk=$scope.model.dataDisk;
		  
		  data.sysDiskType=$scope.model.sysDiskType;
		  data.sysTypeId=$scope.model.sysTypeId;
		  data.sysTypeAs=$scope.model.sysTypeAs;
		  data.dataDiskType=$scope.model.dataDiskType;
		  data.dataTypeId=$scope.model.dataTypeId;
		  data.dataTypeAs=$scope.model.dataTypeAs;
		  
		  data.username = $scope.model.username;
		  data.loginType = $scope.model.loginType;
		  if(data.loginType == 'ssh'){
			  data.secretKey = $scope.model.secretKey;
		  }
		  else if(data.loginType == 'pwd'){
			  data.password = $scope.model.password;
		  }
		  
		  data.vmName = $scope.model.vmName;
		  data.sgId = $scope.model.sgId;
		  data.count = $scope.model.number;
		  data.buyCycle = $scope.model.buyCycle;
		  data.orderType = '0';
		  data.payType = $scope.model.payType
		  data.sourceType = $scope.model.sourceType;
		  data.professionType = $scope.model.professionType;
		  if($scope.priceDetails){
			  data.paymentAmount = $scope.priceDetails.totalPrice;
			  data.paymentAmount = data.paymentAmount.toString();
		  }
		  data.cycleType = $scope.model.cycleType;
		  data.prodName = "云主机-包年包月";
		  if('2' == data.payType){
			  data.prodName = "云主机-按需付费";
		  }
	  };
	  
	  /**
	   * 获取项目配额
	   */
	  $scope.queryPrjQuota = function(){
		  eayunHttp.post('cloud/vm/getProjectjQuotaById.do',$scope.model.prjId).then(function(response){
			  $scope.project = response.data.data;
			  $scope.checkVmQuota();
		  });
	  };
	  
	  
	  /**
	   * 检验创建云硬盘的大小
	   * 检验当前项目下可创建的云硬盘容量
	   */
	  $scope.changeSize= function(value){
		  if(value<=0){
			  $scope.model.dataDisk=0;
		  }else{
			  $scope.model.dataDisk=value;
		  } 
		  
		  $scope.checkVmQuota();
		  $scope.calcBillingFactor();
	  };
	  
	  
	  /**
	   * 校验云主机的配额
	   */
	  $scope.checkVmQuota = function(){
		  
		  var num = $scope.model.number;
		  var cpu = $scope.model.cpu;
		  var ram = $scope.model.ram;
		  var disk = $scope.model.disk;
		  var dataDisk=$scope.model.dataDisk;
		  var prj = $scope.project;
		  var buyFloat = $scope.model.isBuyFloatIp;
		  
		  
		  $scope.floatIpQuotaMsg = '';//公网IP个数
		  $scope.volCapacityQuotaMsg = '';//云硬盘容量超配
		  $scope.countQuotaMsg = '';//云主机、云硬盘个数超配
		  $scope.cpuQuotaMsg = '';//cpu、内存超配
		  
		  if(prj&&num && num<21){
			  if(buyFloat && num>(prj.outerIP - prj.outerIPUse)){
				  $scope.floatIpQuotaMsg = $scope.floatIpQuotaMsg+'公网IP数量配额不足';
			  }
			  if(num>(prj.hostCount - prj.usedVmCount)){
				  $scope.countQuotaMsg = $scope.countQuotaMsg+'主机数量、';
			  }
			  if(dataDisk<0||dataDisk==0){
				  if(num>(prj.diskCount - prj.diskCountUse)){
					  $scope.countQuotaMsg = $scope.countQuotaMsg+'云硬盘数量、';
				  }
			  }else{
				  if(num*2>(prj.diskCount - prj.diskCountUse)){
					  $scope.countQuotaMsg = $scope.countQuotaMsg+'云硬盘数量、';
				  }
			  }
			  
			  
			  if(cpu&&(num*cpu>(prj.cpuCount - prj.usedCpuCount))){
				  $scope.cpuQuotaMsg = $scope.cpuQuotaMsg +'CPU、';
			  }
			  if(ram&&((num*1024*ram)>(prj.memory - prj.usedRam))){
				  $scope.cpuQuotaMsg = $scope.cpuQuotaMsg +'内存、';
			  }
			  
			  if(disk&&(((num*disk)+(num*dataDisk))>(prj.diskCapacity - prj.usedDiskCapacity))){
				  $scope.volCapacityQuotaMsg = '云硬盘容量配额不足';
			  }
		  }
		  
		  if('' != $scope.countQuotaMsg){
			  $scope.countQuotaMsg = $scope.countQuotaMsg.substr(0,$scope.countQuotaMsg.length-1)+'配额不足';
		  }
		  if('' != $scope.cpuQuotaMsg){
			  $scope.cpuQuotaMsg = $scope.cpuQuotaMsg.substr(0,$scope.cpuQuotaMsg.length-1)+'配额不足';
		  }
	  };
	    
	  $scope.init();
  })
  /**
   * 订单确认界面
   */
  .controller('ConfirmVmOrderController', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage,eayunMath) {
	  $scope.model={};
	  
	  $scope.init = function(){
		  $scope.item = eayunStorage.get('order_confirm_vm');
		  eayunStorage.delete('order_confirm_vm');
		  if(!$scope.item){
			  if($stateParams.orderType == '0'){
				  $state.go('buy.host',{orderNo:'000000'});
			  }
			  else if($stateParams.orderType == '2'){
				  $state.go('app.cloud.cloudhost.host');
			  }
		  }
		  else{
			  if('0'== $scope.item.orderType){
				  $scope.title = '创建云主机';
			  }
			  else if('2'== $scope.item.orderType){
				  $scope.title = '调整配置';
			  }
			  $scope.queryAccount();
		  } 
		  
	  };
	  
	  /**
	   * 返回修改配置
	   */
	  $scope.backToVm = function(){
		  eayunStorage.set('order_back_vm',$scope.item);
		  var orderNo ='000000';
		  if($scope.item.payType == '2'){
			  orderNo = '000001';
		  }
		  $state.go('buy.host',{orderNo:orderNo});
	  };
	  
	  /**
	   * 查看账户余额
	   */
	  $scope.queryAccount = function (){
		  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
			  $scope.account = response.data.data.money;
			  if($scope.account<0){
				  $scope.account = 0;
			  }
			  $scope.useAccountPay();
		  });
	  };
	  
	  /**
	   * 使用余额支付
	   */
	  $scope.useAccountPay = function (){
		  $scope.model.accountPayment = null;
		  $scope.model.thirdPartPayment = null;
		  if($scope.model.useAccount){
			  if($scope.account>=$scope.item.paymentAmount){
				  $scope.model.accountPayment = $scope.item.paymentAmount;
				  $scope.model.thirdPartPayment = 0;
			  }
			  else{
				  $scope.model.accountPayment = $scope.account;
				  $scope.model.thirdPartPayment = eayunMath.sub(Number($scope.item.paymentAmount),$scope.account);
			  }
		  }
		  else{
			  $scope.model.accountPayment = 0;
			  $scope.model.thirdPartPayment = $scope.item.paymentAmount;
		  }
	  };
	  
	  /**
	   * 获取项目配额
	   */
	  $scope.queryPrjQuota = function(){
		  eayunHttp.post('cloud/vm/getProjectjQuotaById.do',$scope.item.prjId).then(function(response){
			  $scope.project = response.data.data;
			  $scope.checkVmQuota($scope.item.orderType);
		  });
	  };
	  
	  /**
	   * 校验云主机的配额(新增)
	   */
	  $scope.checkVmQuota = function(orderType){
		  $scope.errorMsg = '';
		  var cpu = $scope.item.cpu;
		  var ram = $scope.item.ram;
		  var quotaErrorMsg = '';//超配提示语
		  var prj = $scope.project;
		  if(orderType == '0'){
			  var num = $scope.item.count;
			  var disk = $scope.item.disk;
			  var buyFloat = $scope.item.buyFloatIp;
			  
			  if(prj&&num){
				  if(buyFloat && '1' == buyFloat && num>(prj.outerIP - prj.outerIPUse)){
					  quotaErrorMsg = quotaErrorMsg+'公网IP、';
				  }
				  if(disk&&((num*disk)>(prj.diskCapacity - prj.usedDiskCapacity))){
					  quotaErrorMsg = quotaErrorMsg+'云硬盘容量、';
				  }
				  if(cpu&&(num*cpu>(prj.cpuCount - prj.usedCpuCount))){
					  quotaErrorMsg = quotaErrorMsg +'CPU、';
				  }
				  if(ram&&((num*ram)>(prj.memory - prj.usedRam))){
					  quotaErrorMsg = quotaErrorMsg +'内存、';
				  }
				  if(num>(prj.hostCount - prj.usedVmCount)){
					  quotaErrorMsg = quotaErrorMsg+'主机数量、';
				  }
				  if(num>(prj.diskCount - prj.diskCountUse)){
					  quotaErrorMsg = quotaErrorMsg +'云硬盘数量、';
				  }
			  }
			  
			  if('' != quotaErrorMsg){
				  $scope.errorMsg = '您的'+quotaErrorMsg.substr(0,quotaErrorMsg.length-1)+'配额不足，请提交工单申请配额';
			  }
		  }
		  else if(orderType == '2'){
			  var vmCpu = $scope.item.vmCpu;
			  var vmRam = $scope.item.vmRam;
			  if(prj&&cpu&&(cpu>(prj.cpuCount - prj.usedCpuCount + vmCpu))){
				  quotaErrorMsg = quotaErrorMsg +'CPU、';
			  }
			  if(prj&&ram&&(ram>(prj.memory - prj.usedRam + vmRam))){
				  quotaErrorMsg = quotaErrorMsg +'内存、';
			  }
			  if('' != quotaErrorMsg){
				  $scope.errorMsg = '您的'+quotaErrorMsg.substr(0,quotaErrorMsg.length-1)+'配额不足，请提交工单申请配额';
			  }
		  }
	  };
	  
	  
	  
	  /**
	   * 计费因子价格发生变化
	   */
	  $scope.reCalculateBillingFactory = function (){
		  var data = {};
		  var url =null;
		  $scope.errorMsg = null;
		  data.dcId = $scope.item.dcId;
		  if($scope.item.orderType == '0'){
			  url ='billing/factor/getPriceDetails.do';
			  data.payType = $scope.item.payType;
			  data.number = $scope.item.count;
			  data.cpuSize = $scope.item.cpu;
			  data.ramCapacity = $scope.item.ram/1024;
			  //data.sysDiskCapacity = $scope.item.disk;
			  
			  //分类计价系统盘
			  if('1'==$scope.item.sysDiskType){
				  data.sysDiskOrdinary=$scope.item.disk;
			  }else if('2'==$scope.item.sysDiskType){
				  data.sysDiskBetter=$scope.item.disk;
			  }else if('3'==$scope.item.sysDiskType){
				  data.sysDiskBest=$scope.item.disk;
			  }else{
				  data.sysDiskCapacity = $scope.item.disk;
			  }
			  
			  //分类计价数据盘
			  if('1'==$scope.item.dataDiskType){
				  data.dataDiskOrdinary=$scope.item.dataDisk;
			  }else if('2'==$scope.item.dataDiskType){
				  data.dataDiskBetter=$scope.item.dataDisk;
			  }else if('3'==$scope.item.dataDiskType){
				  data.dataDiskBest=$scope.item.dataDisk;
			  }else{
				  data.dataDiskCapacity = $scope.item.dataDisk;
			  }
			  
			  
			  data.cycleCount = 1;
			  data.imageId = $scope.item.imageId;
			  if($scope.item.sourceId){
				  data.imageId = $scope.item.sourceId;
			  }
			  if(data.payType == '1'){
				  data.cycleCount = $scope.item.buyCycle;
			  }
			  if($scope.item.buyFloatIp == '1'){
				  data.ipCount = 1;
			  }
		  }
		  else if($scope.item.orderType == '2'){
			  data.cycleCount = $scope.item.cycleCount;
			  if(($scope.item.cpu - $scope.item.vmCpu)>0){
				  data.cpuSize = $scope.item.cpu - $scope.item.vmCpu;
			  }
			  if(($scope.item.ram/1024 - $scope.item.vmRam/1024)>0){
				  data.ramCapacity = $scope.item.ram/1024 - $scope.item.vmRam/1024;
			  }
			  url = 'billing/factor/getUpgradePrice.do';
		  }
		  
		  eayunHttp.post(url,data).then(function (response){
			  if(response && response.data){
				  if(response.data.respCode == '010120'){
					  $scope.errorMsg = response.data.message;
				  }
				  else{
					  $scope.priceDetails = response.data.data;
					  if($scope.item.orderType == '2'){
						  $scope.item.paymentAmount = $scope.priceDetails; 
					  }
					  else if($scope.item.orderType == '0'){
						  $scope.item.paymentAmount = $scope.priceDetails.totalPrice;
					  }
					  
					  $scope.useAccountPay();
				  }
			  }
		  });
	  };
	  
	  /**
	   * 提交订单信息
	   */
	  $scope.commitOrder = function(){
		  $scope.warnMsg = '';
		  $scope.checkBtn = true;
		  var data = angular.copy($scope.item);
		  delete data.sourceType ;
		  delete data.professionType;
		  delete data.sourceId;
		  var url = 'cloud/vm/buyVm.do';
		  if($scope.item.orderType == '2'){
			  url = 'cloud/vm/resizeVm.do';
		  }
		  if('1' == $scope.item.payType){
			  data.accountPayment = $scope.model.accountPayment;
			  data.thirdPartPayment = $scope.model.thirdPartPayment;
		  }
		  
		  eayunHttp.post(url,data).then(function(response){
			  if(response && response.data){
				  if(response.data.respCode == '000000' || response.data.respCode =='400000'){
					  if('1' == data.payType){
						  if(!data.thirdPartPayment || data.paymentAmount == 0){
							  $state.go('pay.result', {subject:data.prodName});
						  }
						  else{
							  if(data.orderType == '0'){
								  var routeUrl = "buy.host({'orderNo':'000000'})";
								  var orderPayNavList = [{route:'app.cloud.cloudhost.host',name:'云主机'},
								                         {route:routeUrl,name:'创建云主机'}];
								  eayunStorage.persist("orderPayNavList",orderPayNavList);
								  eayunStorage.persist("payOrdersNo",response.data.orderNo);
								  $state.go('pay.order');
							  }
							  else if(data.orderType == '2'){
								  var orderPayNavList = [{route:'app.cloud.cloudhost.host',name:'云主机'}];
								  eayunStorage.persist("orderPayNavList",orderPayNavList);
								  eayunStorage.persist("payOrdersNo",response.data.orderNo);
								  $state.go('pay.order');
							  }
						  }
					  }
					  else if ('2' == data.payType){
						  $state.go('app.order.list');
					  }
				  }
				  else if(response.data.respCode == '010110'){
					  $scope.checkBtn = false;
					  if(response.data.message == 'OUT_OF_QUOTA'){
						  $scope.queryPrjQuota();
					  }
					  else if(response.data.message == 'CHANGE_OF_BILLINGFACTORY'){
						  eayunHttp.post('cloud/vm/queryVmChargeById.do',$scope.item.vmId).then(function(response){
							  if(response && response.data && response.data.data){
								  $scope.item.cycleCount = response.data.data.cycleCount;
								  $scope.reCalculateBillingFactory();
							  }
						  });
						  $scope.warnMsg = "您的订单金额发生变动，请重新确认订单";
					  }
					  else if(response.data.message == 'CHANGE_OF_BALANCE'){
						  $scope.queryAccount();
						  $scope.warnMsg = "您的余额发生变动，请重新确认订单";
					  }
					  else if(response.data.message == 'UPGRADING_OR_INORDER'){
						  $scope.queryAccount();
						  $scope.errorMsg = "资源正在调整中或您有未完成的订单，请您稍后重试";
					  }
					  else if(response.data.message == 'IMAGE_OF_CREATING'){
						  $scope.errorMsg = "云主机正在创建自定义镜像，请稍后重试";
					  }
					  else if(response.data.message == 'NOT_SUFFICIENT_FUNDS'){
						  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
							  $scope.payAfpterPayment = response.data;
							  $scope.errorMsg = "您的账户余额不足"+$scope.payAfpterPayment+"元，请充值后操作";
						  });
					  }
					  else if(response.data.message == 'ARREARS_OF_BALANCE'){
						  $scope.payAfpterPayment = response.data;
						  $scope.errorMsg = "您的账户已欠费，请充值后操作";
					  }
					  else if(response.data.message == 'CHANGE_OF_RESOURCESIZE'){
						  $scope.errorMsg = "您的订单规格发生变动，请重新确认订单";
					  }
				  }
				  else{
					  if('1' == data.payType){
						  if(response.data.orderNo){
							  eayunStorage.persist("payOrdersNo",response.data.orderNo);
							  $state.go('pay.order');
						  }
					  }
					  
					  else if ('2' == data.payType){
						  $state.go('app.order.list');
					  }
				  }
			  }
			  
		  });
	  };
	  
	  $scope.init();
  })
  /**
   * 云主机详情的Controller
   */
  .controller('VmDetailController',function ($rootScope,$scope ,eayunHttp ,eayunModal,$stateParams,$state,$timeout,toast,powerService,eayunStorage,DatacenterService){
	  var list=[{route:'app.cloud.cloudhost.host',name:'云主机'}];
	  $rootScope.navList(list,'云主机详情','detail');
	  
	  $scope.vmNameEditable = false;
	  $scope.vmDescEditable = false;
	  $scope.checkVmName = true ;
	  $scope.checkEditBtn = true;
	  var item = {};
	  
	  powerService.powerRoutesList().then(function(powerList){
			$scope.vmDetailPermissions = {
				isVmEdit : powerService.isPower('host_edit'),					//编辑云主机
				isVmStartOrShut : powerService.isPower('host_setup'),			//启动云主机/关闭云主机
				isVmSoftRestart : powerService.isPower('host_softRestart'),		//重启云主机
				isBindVolume : powerService.isPower('host_bindVolume'),         //挂载云硬盘new
				isShowMonitor : powerService.isPower('host_showMonitor'),       //查看监控new
				isVmResize : powerService.isPower('host_resize'),				//升级配置
				isVmOpenConsole : powerService.isPower('host_openVm'),			//云主机控制台
				isVmEditSG : powerService.isPower('host_editSg'),				//编辑安全组
				isVmBindFloatIp : powerService.isPower('host_bindFloatIp'),		//绑定浮动 IP
				isVmUnBindFloatIp : powerService.isPower('host_unbindFloatIp'),	//解绑浮动 IP
				isVmAddImage : powerService.isPower('host_addSnap'),			//创建镜像
				isVmLog : powerService.isPower('host_getVmLog'),				//查看日志
				isVmTag : powerService.isPower('host_tag'),						//标签
				isVmDelete : powerService.isPower('host_delete'),	 			//删除云主机
				isAddVolumeSnpashot : powerService.isPower('disk_addSnap'),	 	//创建备份
				isVmUnbindVolume : powerService.isPower('disk_unbind'),	 		//解绑云硬盘
				isModifySubnet : powerService.isPower('host_moreIp'),	 		//修改子网
				isVmEditSSH : powerService.isPower('secretkey_bind'),	 		//绑定/解绑SSH密钥
				isVmResetPwd : powerService.isPower('host_modify_pwd'),	 		//修改密码
			};
		});
	  
	  /**
	     * 查询当前sessionStore 是否存在用户信息
	     */
	    $scope.checkUser = function (){
	    	var user = sessionStorage["userInfo"];
	    	 if(user){
	    		 user = JSON.parse(user);
	    		 if(user&&user.userId){
	    			 return true;
	    		 }
	    	 }
	    	return false;
	    };
	  
	    /**
	     * 云主机状态 显示
	     */
	    $scope.chooseVmStatusClass =function (model){
	    	$scope.vmStatusClass = '';
	    	if('1'==model.chargeState||'2'==model.chargeState||'3'==model.chargeState){
	    		$scope.vmStatusClass ='ey-square-disable';
	    		return;
	    	}
	    	else if(model.vmStatus&&model.vmStatus=='ACTIVE'){
				$scope.vmStatusClass = 'ey-square-right';
			}  
			else if(model.vmStatus=='ERROR'|| model.vmStatus=='SUSPENDED'){
				$scope.vmStatusClass = 'ey-square-error';
			}
			else if(model.vmStatus=='SHUTOFF'){
				$scope.vmStatusClass = 'ey-square-disable';
			}
			else{
				$scope.vmStatusClass = 'ey-square-warning';
			}
	    };
	    /**
	      * 云硬盘状态 显示
	      */
	      $scope.getVolumeStatus =function (model){
	    	  if('1'==model.chargeState||'2'==model.chargeState||'3'==model.chargeState){
		    		return 'ey-square-disable';
		    	}
	    	  else if(model.volStatus&&model.volStatus=='AVAILABLE'||model.volStatus=='IN-USE'){
	    		  return 'ey-square-right';
	    	  }  
	    	  else if(model.volStatus=='ERROR'){
	    		  return 'ey-square-error';
	    	  }
	    	  else {
	    		  return 'ey-square-warning';
	    	  }
	      };
	      
	      /**
	       * 刷新详情界面的状态
	       */
	      $scope.$watch("model",function (newVal,oldVal){
	      	if(newVal != oldVal){
	      		 var status = $scope.model.vmStatus.toString().toLowerCase();
				  if("active"!=status
						  &&"error"!=status
						  &&"shutoff"!=status
						  &&"suspended"!=status
						  &&"verify_resize"!=status){
					  $timeout($scope.refreshVm,5000);
   				}
	      	}
	      });   
	      
	      /**
	       * 刷新详情界面的状态
	       */
	      $scope.$watch("volumes",function (newVal,oldVal){
	    	  if(newVal != oldVal){
	    		  if($scope.volumes!=null&&$scope.volumes.length>0){
	    			for(var i=0;i<$scope.volumes.length;i++){
	    				var status=$scope.volumes[i].volStatus.toString().toLowerCase();
    					if("attaching"==status){
	    					$timeout($scope.refreshVolume,5000);
	    					break;
	    				}
	    				
	    			}
		    	}
	    	  }
	      });   
	      
	  /**
	   * 刷新界面
	   */
	  $scope.refreshVm = function (){
		  if(!$scope.checkUser()){
          	return ;
          }
		  $scope.refreshVmDetail();
		  
		  $scope.refreshVolume();
		  
		  eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'vm',resId: $stateParams.vmId}).then(function(response){
				$scope.tag=response.data;
		  });
	  };
	  
	  $scope.refreshVolume = function (){
		  if(!$scope.checkUser()){
	          	return ;
	      }
		  eayunHttp.post('cloud/volume/queryVolumesByVm.do',$stateParams.vmId).then(function (response){
			  $scope.volumes = response.data;
		  });
	  };
	  
	  $scope.refreshVmDetail = function (){
		  if(!$scope.checkUser()){
	          	return ;
          }
		  $scope.vmStatusClass ='';
		  eayunHttp.post('cloud/vm/getVmById.do',$stateParams.vmId).then(function (response){
			  if(response.data!=null&&response.data.data!=null){
				  $scope.model = response.data.data;
				  $scope.chooseVmStatusClass($scope.model);
				  item = {
						  name:$scope.model.vmName,
						  desc:$scope.model.vmDescripstion
				  };
			  }
			  else if(response.data.respCode=='400000'&&response.data.data==null) {
				  $state.go('app.cloud.cloudhost.host');
			  }
		  });
	  };
	  
	  /**
	   * 保存编辑的云主机名称、描述,并刷新界面
	   */
	  $scope.saveEdit = function (type){
		  eayunHttp.post('cloud/vm/modifyVm.do',$scope.model).then(function (response){
			  if(response.data!=null){
				  if(type == 'vmName'){
					  $scope.vmNameEditable = false;
					  $scope.hintNameShow = false;
					  item.name = $scope.model.vmName;
					  toast.success('云主机' + DatacenterService.toastEllipsis(item.name, 8) +'修改成功');
				  }
				  if(type == 'vmDesc'){
					  $scope.vmDescEditable = false;
					  $scope.hintDescShow = false;
					  item.desc = $scope.model.vmDescripstion;
					  toast.success('云主机' + DatacenterService.toastEllipsis($scope.model.vmName, 8) +'修改成功');
				  }
				  $scope.refreshVm();
			  }
		  },function (){
			  
		  });
	  };
	  
	  /**
	   * 将云主机名称、描述变为可编辑状态
	   */
	  $scope.edit = function(type){
		  if(type == 'vmName'){
			  $scope.vmNameEditable = true;
			  $scope.hintNameShow = true;
			  $scope.vmDescEditable = false;
			  $scope.hintDescShow = false;
			  $scope.model.vmDescripstion = item.desc;
		  }
		  if(type == 'vmDesc'){
			  $scope.vmNameEditable = false;
			  $scope.hintNameShow = false;
			  $scope.vmDescEditable = true;
			  $scope.hintDescShow = true;
			  $scope.model.vmName = item.name;
		  }
	  };
	  
	  /**
	   * 添加标签
	   */
		$scope.tagResource = function(resType, resId){
			var result=eayunModal.open({
				backdrop:'static',
			    templateUrl: 'views/tag/tagresource.html',
			    controller: 'TagResourceCtrl',
			    	resolve: {
			    		resType : function () {
			                return resType;
			            },
			            resId : function(){
			            	return resId;
			            }
			        }
				});
				result.result.then(function () {
					eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'vm',resId: $stateParams.vmId}).then(function(response){
						$scope.tag=response.data;
					});
			},function () {
				eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'vm',resId: $stateParams.vmId}).then(function(response){
					$scope.tag=response.data;
				});
			});
		};
	  
	  /**
	   * 取消云主机名称、描述的可编辑状态
	   */
	  $scope.cancleEdit = function (type){
		  if(type == 'vmName'){
			  $scope.vmNameEditable = false;
			  $scope.hintNameShow = false;
			  $scope.model.vmName = item.name;
		  }
		  if(type == 'vmDesc'){
			  $scope.vmDescEditable = false;
			  $scope.hintDescShow = false;
			  $scope.model.vmDescripstion = item.desc;
		  }
	  };
	  
	  /**
	   * 校验云主机修改重名
	   */
	  $scope.checkVmNameExist = function (){
		  if($scope.model && $scope.model.vmName){
			  var cloudVm = {
					  dcId:$scope.model.dcId, 
					  prjId:$scope.model.prjId,
					  vmId:$scope.model.vmId, 
					  vmName:$scope.model.vmName, 
					  number:1
			  };
			  var vname = $scope.model.vmName;
			  $scope.checkEditBtn = false;
			  eayunHttp.post('cloud/vm/checkVmExistByName.do',cloudVm)
			  .then(function (response){
				  if(vname === $scope.model.vmName){
					  $scope.checkVmName = response.data;
					  $scope.checkEditBtn = true;
				  }
			  });
		  }
	  };
	  
	  /**
	   * 云主机的操作(没有弹出界面的操作)
	   */
	  $scope.vmOption = function (option){
		  var optionUrl = '';
		  if(option!=''&&option!=null&&option!=undefined){
			  optionUrl = 'cloud/vm/'+option+'.do';
			  
			  var cloudVm = {
					dcId:$scope.model.dcId,  
					prjId:$scope.model.prjId,  
					vmId:$scope.model.vmId,
					vmName:$scope.model.vmName
			  };
			  
			  eayunHttp.post(optionUrl,cloudVm)
			  .then(function (response){
				  if(response.data&&response.data.respCode=='400000'){
					  if(option=='restartVm'){toast.success("云主机开机中",2000);}
					  else if(option=='resumeVm'){toast.success("云主机恢复中",2000);}
					  else if(option=='suspendVm'){toast.success("云主机挂起中",2000);}
					  else if(option=='confirmResizeVm'){toast.success("开始调整云主机大小",2000);}
					  else if(option=='revertResizeVm'){toast.success("开始撤销调整云主机大小",2000);}
					  $scope.refreshVm();
				  }
			  });
		  }
		  
	  }; 
	  
	  /**
	   * 关闭云主机
	   */
	  $scope.shutdown = function (){
		  eayunModal.confirm('确定要关闭云主机？').then(function (){
			  var cloudVm = {
						dcId:$scope.model.dcId,  
						prjId:$scope.model.prjId,  
						vmId:$scope.model.vmId,
						vmName:$scope.model.vmName
				  };
	    	  eayunHttp.post('cloud/vm/shutdownVm.do',cloudVm).then(function(response){
	    		  if(null!=response.data&&response.data.respCode == '400000'){
	    			  toast.success('云主机关闭中',2000); 
	    			  $scope.refreshVm();
	    		  }
              })
		  });
	  };
	  
	  /**
	   * 重启云主机
	   */
	  $scope.restart = function (option){
		  eayunModal.confirm('确定要重启云主机？重启期间无法提供服务').then(function (){
			  var cloudVm = {
						dcId:$scope.model.dcId,  
						prjId:$scope.model.prjId,  
						vmId:$scope.model.vmId,
						vmName:$scope.model.vmName
				  };
			  var url = 'cloud/vm/'+option+'.do';
			  eayunHttp.post(url,cloudVm).then(function(response){
	    		  if(null!=response.data&&response.data.respCode == '400000'){
	    			  toast.success('云主机重启中',2000);
	    			  $scope.refreshVm();
	    		  }
              })
			  
		  });
	  };
	  
	  /**
	   * 创建自定义镜像
	   */
	  $scope.createSnapshot = function (item){
			  var result = eayunModal.open({
				  backdrop:'static',
				  templateUrl: 'views/cloudhost/host/addimage.html',
				  controller: 'AddImageController',
				  resolve:{
					  item:function (){
						  return item;
					  }
				  }
			  });
	  };
	  
	  /**
	   * 打开云主机控制台
	   */
	  $scope.openVmConsole = function (item){
		  eayunModal.open({
		        templateUrl: 'views/cloudhost/host/vmconsole.html',
		        controller: 'VmConsoleController',
		        resolve:{
	            	item:function (){
	            		return item;
	            	}
	            }
		      });
	  };
	  
	  /**
	   * 获取云主机日志
	   */
	  $scope.getVmLog = function (item){
		  eayunModal.open({
		  	  backdrop:'static',
			  templateUrl: 'views/cloudhost/host/log.html',
			  controller: 'VmLogController',
			  resolve:{
				  item:function (){
					  return item;
				  }
			  }
		  });
	  };
	  
	  /**
	   * 重建云主机
	   */
	  $scope.rebuildVm = function (item){
		  var result = eayunModal.dialog({
			  showBtn: false,
			  title: '重建云主机',
			  width: '600px',
			  templateUrl: 'views/cloudhost/host/rebuildvm.html',
			  controller: 'RebuildVmController',
			  resolve:{
				  item:function (){
					  return item;
				  }
			  }
		  });
		  result.then(function (){
			  $scope.refreshVm();
		  });
	  };
	  
	  /**
	   * 编辑安全组
	   */
	  $scope.editSg = function (item){
		  var result = eayunModal.open({
				backdrop:'static',
		        templateUrl: 'views/cloudhost/host/editsecuritygroup.html',
		        controller: 'EditSecGrpController',
		        resolve:{
	            	item:function (){
	            		return item;
	            	}
	            }
	      }).result;
		  result.then(function (){
			  $scope.refreshVm();
		  });
	  };
	  
	  /**
	   * 绑定浮动IP
	   */
	  $scope.bindFloatIp = function (item){
		  var result = eayunModal.open({
		        templateUrl: 'views/cloudhost/host/bindfloatip.html',
		        controller: 'VmBindFloatIpController',
		        backdrop:'static',
		        resolve:{
	            	item:function (){
	            		return item;
	            	}
	            }
	      }).result;
		  result.then(function (){
			  $scope.refreshVm();
		  });
	  };
	  
	  /**
	   * 解绑浮动IP
	   */
	  $scope.unbindFloatIp = function (item){
		  eayunModal.confirm('确定要解绑公网IP'+item.floatIp+'？').then(function (){
			  var floatIp ={
					  dcId:item.dcId,
					  prjId:item.prjId,
					  resourceId:item.vmId,
					  resourceType:'vm',
					  floId:item.floatId,
					  floIp:item.floatIp
			  };
			  eayunHttp.post('cloud/floatip/unbundingResource.do',floatIp).then(function (response){
				  if(response&&response.data&&response.data.respCode=='400000'){
					  toast.success("云主机解绑公网IP成功",2000);
					  $scope.refreshVm();
				  }
				  else{
					  $scope.refreshVm();
				  }
			  });
		  });
	  };
	  
	  /**
	   * 挂载云硬盘
	   */
	  $scope.bindDisks = function(item){
		  eayunHttp.post('cloud/volume/getUnUsedVolumes.do',
				  {prjId:item.prjId,dcId:item.dcId}).then(function (response){
			 if(!response.data||response.data.length==0){
				 eayunModal.warning("您暂无可用的云硬盘，请创建后再操作");
				 return;
			 }
			 else{
				 eayunHttp.post('cloud/volume/queryVolumeCountByVmId.do',item.vmId).then(function(response) {
					  if(response.data&&response.data>=5){
						  eayunModal.warning("可挂载的硬盘数量已满");
							 return;
					  }
					  else{
						  var result = eayunModal.open({
							  templateUrl: 'views/cloudhost/host/binddisks.html',
							  controller: 'bindDisksController',
							  backdrop:'static',
							  resolve: {
								  item: function () {
									  return item;
								  }
							  }
						  }).result;
						  result.then(function (value) {
							  $scope.refreshVm();
						  },function(){
							  $scope.refreshVm();
						  });
					  }
				  });
			 }
		  });
	  };

	  /**
	   * 打开云监控
	   */
	  $scope.showVmMonitor = function(vmId){
	  	$state.go('app.cloud.cloudhost.monitor',{vmId:vmId}); // 跳转后的URL;
	  };
	  /**
	   * 升级配置
	   */
	  $scope.resizeVm = function (item){
		  if(item&&item.vmStatus!='SHUTOFF'){
			  eayunModal.warning("请先关闭云主机");
			  return;
		  }
		  var result = eayunModal.open({
			  templateUrl: 'views/cloudhost/host/resizevm.html',
			  controller: 'ResizeVmController',
			  backdrop:'static',
			  resolve:{
				  item:function (){
					  return item;
				  },
				  volume:function(){
	    	          return eayunHttp.post('cloud/volume/getSysVolumeByVmId.do',item.vmId).then(function(response){
	    	       		 return response.data;
	    	       	  });
	    	      }
			  }
		  }).result;
		  result.then(function (){
			  $scope.refreshVm();
		  });
	  };
	  
	  /**
	   * 删除云主机
	   */
	  $scope.deleteVm = function (item){
		  var result = eayunModal.open({
			  templateUrl: 'views/cloudhost/host/deletevm.html',
			  controller: 'DeleteVmController',
			  resolve:{
				  item:function (){
					  return item;
				  }
			  }
		  }).result;
		  
		  result.then(function (data){
			  $scope.refreshVm();
		  },function(){
			  $scope.refreshVm();
		  });
	  };
	  
	  /**
	   * 创建云硬盘备份
	   */
	  $scope.addVolumeSnapshot = function (volume){
		  $state.go('buy.snapshot',{'payType':'2','volId':volume.volId,'fromVmId':$scope.model.vmId});
	  };
	  
	/**
	 * 
	 * 解绑云硬盘
	 */
    $scope.unbindVol = function (cloudvolume) {
    	 var result = eayunModal.open({
			  templateUrl: 'views/cloudhost/host/debindvolume.html',
			  controller: 'DebindvolumeCtrl',
			  resolve:{
				  item:function (){
					  return cloudvolume;
				  }
			  }
		  }).result;
    	 
    	 result.then(function(){
    		 $scope.refreshVm();
    	 });
    };
	  
    //pop框方法
    $scope.openPopBox = function(vmHost){
    	if(vmHost.type == 'tagName'){
    		$scope.hintTagShow = true;
    	}
    	if(vmHost.type == 'vmDesc'){
    		$scope.descShow = true;
    	}
    	if(vmHost.type == 'vmSgNames'){
    		$scope.sgShow = true;
    	}
    	$scope.description = vmHost.value;
    };
    $scope.closePopBox = function(type){
    	if(type == 'tagName'){
    		$scope.hintTagShow = false;
    	}
    	if(type == 'vmDesc'){
    		$scope.descShow = false;
    	}
    	if(type == 'vmSgNames'){
    		$scope.sgShow = false;
    	}
    };
	  
	  /**
	   * 修改子网
	   */
	  $scope.modifySubnet = function(item){
		  
	      var result = eayunModal.open({
	    	backdrop:'static',
	        templateUrl: 'views/cloudhost/host/modifysubnet.html',
	        controller: 'ModifySubnetCtrl',
	        resolve: {
	      	  item:function(){
	      		  return item;
	      	  }
	        }
	      }).result;
	      result.then(function (value){
	    	 $scope.refreshVm();
	      }, function () {
	    	  $scope.refreshVm();
	      });
	  
	  };
	  
	  /**
	   * 绑定/解绑SSH密钥
	   */
	  $scope.editSSH = function(item){
		  eayunHttp.post('cloud/vm/queryRouteInfoByVm.do',item.vmId).then(function (response){
			  if(response.data && response.data.data){
				  var data = response.data.data;
				  if (data.vmStatus != 'SHUTOFF'){
					  eayunModal.warning("请将云主机关机");
					  $scope.refreshVm();
					  return;
				  }
				  if(!data.routeId){
					  eayunModal.warning("请将云主机所在子网连接路由");
					  $scope.refreshVm();
					  return;
				  }
				  
				  var result = eayunModal.open({
						backdrop:'static',
				        templateUrl: 'views/cloudhost/host/editssh.html',
				        controller: 'EditSSHController',
				        resolve:{
			            	item:function (){
			            		return item;
			            	}
			            }
			      }).result;
				  result.then(function (){
					  $scope.refreshVm();
				  });
				  
			  }
		  });
		  
	  };
	  
	  /**
	   * 查看云硬盘详情
	   */
	  $scope.volumeDetail = function(volume){
		  $state.go('app.cloud.cloudhost.volumedetail',{"detailType":'volume',"dcId":volume.dcId,"prjId":volume.prjId,"volId":volume.volId});
	  };
	  
	  /**
	   * 修改密码
	   */
	  $scope.resetPwd = function(item){
		  eayunHttp.post('cloud/vm/queryRouteInfoByVm.do',item.vmId).then(function (response){
			  if(response.data && response.data.data){
				  var data = response.data.data;
				  if(!data.subnetId){
					  eayunModal.warning("请将主机加入受管子网，同时请确保受管子网连接到了路由");
					  $scope.refreshVm();
					  return;
				  }
				  if(data.subnetId && !data.routeId){
					  eayunModal.warning("请将主机所在的受管子网连接路由后，再修改密码");
					  $scope.refreshVm();
					  return;
				  }
				  
				  var result = eayunModal.open({
						backdrop:'static',
				        templateUrl: 'views/cloudhost/host/modifyPwd.html',
				        controller: 'ResetPwdController',
				        resolve:{
			            	item:function (){
			            		return item;
			            	}
			            }
			      }).result;
				  result.then(function (){
					  $scope.refreshVm();
				  });
				  
			  }
		  });
		  
	  };
	  
	  $scope.refreshVm();
  })

  /**
   * 自定义镜像 Controller
   */
  .controller('AddImageController',function ($scope,eayunHttp,toast,$state,eayunModal, item,$modalInstance){
	  $scope.model = {};
	  $scope.model.vmName=item.vmName;
	  $scope.model.sysType=item.sysType;
	  $scope.checkName = false ;
	  $scope.checkImageNameExist = function (){
		  var iname = $scope.model.imageName;
		  eayunHttp.post('cloud/image/getImageByName.do',{prjId:item.prjId,imageName:$scope.model.imageName}).then(function (response){
			  if(iname === $scope.model.imageName){
				  $scope.checkName = response.data;
			  }
		  });
	  };
	  
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
	  
	  
	  $scope.commit = function (){
		  eayunModal.confirm('创建过程会引起云主机重启，确定立即创建自定义镜像？').then(function (){
			  $scope.checkToken = true;
			  var cloudVm = {
	    			  dcId:item.dcId,
	    			  prjId:item.prjId,
	    			  vmId:item.vmId,
	    			  cpus:item.cpus,
	    			  imageName : $scope.model.imageName,
	    			  imageDesc : $scope.model.imageDesc
	    	  };
	    	  eayunHttp.post('cloud/vm/createSnapshot.do',cloudVm).then(function(response){
	    		  if(null!=response.data&&response.data.respCode == '400000'){
	    			  var name = $scope.model.imageName.length>8?$scope.model.imageName.substr(0,8)+'...':$scope.model.imageName;
	    			  toast.success('开始创建自定义镜像'+name,2000); 
	    			  $scope.checkToken = false;
	    			  $scope.ok();
	    			  $state.go('app.cloud.cloudhost.image.customerimage');
	    		  }
	    		  else{
	    			  $scope.checkToken = false;
	    		  }
	          });
		  });
	  };
  })
  /**
   * 云主机控制台Controller
   */
  .controller('VmConsoleController',function ($scope,eayunHttp, eayunModal, item,$sce,$modalInstance){
	  var cloudVm = {
			  dcId:item.dcId,
			  prjId:item.prjId,
			  vmId:item.vmId,
			  vmName:item.vmName
	  };
	  eayunHttp.post('cloud/vm/consoleVm.do',cloudVm).then(function(response){
		  if(null!=response.data&&response.data.respCode == '400000'){
			  $scope.consoleUrl = $sce.trustAsResourceUrl(response.data.url);
		  }
      });
	  
	  $scope.cancel = function (){
		  $modalInstance.close(true);
	  };
  })
  
  /**
   * 云主机日志Controller
   */
  .controller('VmLogController',function ($scope,eayunHttp, eayunModal, item,$modalInstance){
	  var cloudVm = {
			  dcId:item.dcId,
			  prjId:item.prjId,
			  vmId:item.vmId,
			  vmName:item.vmName
	  };
	  
	  eayunHttp.post('cloud/vm/getLog.do',cloudVm).then(function(response){
		  if(null!=response.data&&response.data.respCode == '400000'){
			  $scope.logs = response.data.logs;
		  }
	  });
	  
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
	  
  })
  
  /**
   * 编辑安全组 Controller
   */
  .controller('EditSecGrpController',function ($scope,eayunHttp, eayunModal, item,toast,$modalInstance){
	  
	  $scope.init = function () {
		  $scope.groups = [];
		  
		  
		  var list = [],b1,b2;
		  eayunHttp.post('cloud/vm/getSecurityGroupByPrj.do',{prjId:item.prjId,vmId:item.vmId}).then(function (response){
			 $scope.allGroups = response.data;
			 b1 = true;
			 if($scope.allGroups && $scope.allGroups.length>0){
				 angular.forEach($scope.allGroups, function (value, key) {
					 value.$$selected = false;
					 list.push(value);
				  });
			 }
			 if(b1&&b2){
				 $scope.groups = list;
			 }
		  }); 
		  eayunHttp.post('cloud/vm/getSecurityGroupByVm.do',item.vmId).then(function (response){
			 $scope.vmGroups = response.data;
			 b2 = true;	
			 if($scope.vmGroups && $scope.vmGroups.length>0){
				 angular.forEach($scope.vmGroups, function (value, key) {
					 value.$$selected = true;
					 list.push(value);
				  });
			 }
			 if(b1&&b2){
				 $scope.groups = list;
			 }
		  });
		  
	  };
	  
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.add = function(item, index) {
        $scope.vmGroups.push(item);
        $scope.allGroups.splice(index, 1);
      };
      
      $scope.del = function(item, index) {
        $scope.allGroups.push(item);
        $scope.vmGroups.splice(index, 1);
      };
	  
      $scope.selectData = function(){
    	  $scope.selectGroup = [];
    	  if($scope.groups && $scope.groups.length>0){
				 angular.forEach($scope.groups, function (value, key) {
					 if(value.$$selected){
						 $scope.selectGroup.push(value);
					 }
				  });
			 }
      }
      
      /**
	   * 确认 编辑安全组
	   */
	  $scope.commit = function (){
		  $scope.checkToken = true;
		  $scope.selectData();
		  var cloudVm = {
    			  dcId:item.dcId,
    			  prjId:item.prjId,
    			  vmId:item.vmId,
    			  vmName:item.vmName,
    			  bcsgs : $scope.selectGroup
    	  };
    	  eayunHttp.post('cloud/vm/editVmSecurityGroup.do',cloudVm).then(function(response){
    		  if(null!=response.data&&response.data.respCode == '400000'){
    			  toast.success('更新安全组成功',2000); 
    			  $scope.checkToken = false;
    			  $scope.ok();
    		  }
    		  else {
    			  $scope.checkToken = false;
    		  }
          },function (){
        	  $scope.checkToken = false;
          });
	  };
	  
	  
	  $scope.init();
  })
  
  /**
   * 绑定浮动IP Controller
   */
  .controller('VmBindFloatIpController',function ($scope,eayunHttp, eayunModal,$modalInstance,item,toast){
	  
	  $scope.init = function (){
		  eayunHttp.post('cloud/floatip/getUnBindFloatIp.do',{prjId:item.prjId}).then(function(response) {
			  $scope.floats = response.data;
		  });
	  };
	  
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.commit = function (){
		  $scope.checkToken = true;
		  var floatIp = $scope.model.float;
		  $scope.data = {
				  'dcId':item.dcId,
				  'prjId':item.prjId,
				  'floId':floatIp.floId,
				  'floIp':floatIp.floIp,
				  'vmIp':item.vmIp,
				  'resourceId':item.vmId,
				  'resourceType':'vm'
		  };
		  
		  eayunHttp.post('cloud/floatip/bindResource.do',$scope.data).then(function (response){
			  if(response&&response.data&&response.data.respCode=='400000'){
    			  toast.success('绑定'+floatIp.floIp+'成功',2000); 
    			  $scope.ok();
    		  }
    		  else {
    			  $scope.checkToken = false;
    		  }
          });
	  };
	  
	  $scope.init();
  })
  
  /**
   * 绑定云硬盘
   */
  .controller('bindDisksController', function ($scope, eayunModal,eayunHttp,$modalInstance,item,toast) {
	  $scope.model = {};
	  /**
	   * 初始化数据
	   */
	  $scope.init = function (){
		  $scope.initdisks();
		  $scope.initbindeddisks();
		  $scope.isSelected=0;//已选云硬盘数量
		  $scope.maxdisks = 5;//最大挂载云硬盘个数
	  };
	  
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
	  
	  /**
	   * 获取所有未使用的硬盘列表
	   */
	  $scope.initdisks = function (){
		  eayunHttp.post('cloud/volume/getUnUsedVolumes.do',
				  {prjId:item.prjId,dcId:item.dcId}).then(function(response) {
			  $scope.disks = response.data;
		  });
	  };
	  /**
	   * 获取当前云主机的硬盘数量
	   */
	  $scope.initbindeddisks = function(){
		  eayunHttp.post('cloud/volume/queryVolumeCountByVmId.do',item.vmId).then(function(response) {
			  $scope.bindeddisks = response.data;
			  if(!$scope.bindeddisks){
				  $scope.bindeddisks = 0;
			  }
		  });
	  };
	  
	  /**
	   * 全选
	   */
	  $scope.selectAll = function (){
		  for(var i=0;i<$scope.disks.length;i++){
				var disk = $scope.disks[i];
				disk.check = angular.copy($scope.model.allCheck);
				if($scope.model.allCheck){
					$scope.isSelected = $scope.disks.length;
				}
				else{
					$scope.isSelected = 0;
				}
			}
	  };

	  $scope.selectcurrent = function(index){
		  var disk = $scope.disks[index];
		  console.log(disk);
		  if(disk.check){
			  $scope.isSelected++;
		  }else{
			  $scope.isSelected--;
		  }
		  console.log($scope.isSelected == $scope.disks.length);
		  if($scope.isSelected == $scope.disks.length){
			  $scope.model.allCheck = true;
		  }
		  else{
			  $scope.model.allCheck = false;
		  }
	  };
	  
	  /**
	   * checkbox 选择数据
	   */
	  $scope.selectData = function (){
		  var data = [];
	    	var index = 0;
	    	for(var i=0;i<$scope.disks.length;i++){
				var disk = $scope.disks[i];
				if(disk.check){
					var selData = {
							volId:disk.volId,
							volName:disk.volName
					};
					data[index++] = selData;
				}
			}
	    	return data;
	  };
	  
	  /**
	     * 确定
	     */
	    $scope.commit = function (){
	    	$scope.checkBtn = true;
	    	var disks = $scope.selectData();
	    	if(!disks||disks.length==0){
	    		eayunModal.warning('您尚未选择要挂载的云硬盘');
		    	$scope.checkBtn = false;
	    		return ;
	    	}
//			$scope.initbindeddisks();
			
			eayunHttp.post('cloud/volume/queryVolumeCountByVmId.do',item.vmId).then(function(response) {
				  $scope.bindeddisks = response.data;
				  if(!$scope.bindeddisks){
					  $scope.bindeddisks = 0;
				  }
				  if($scope.bindeddisks<5){
					  if(disks.length+$scope.bindeddisks>$scope.maxdisks ){
						  eayunModal.warning('最多可挂载4块数据盘，选择块数不能超过'+($scope.maxdisks-$scope.bindeddisks));
						  $scope.checkBtn = false;
						  return ;
					  }
					  var data ={
							  dcId:item.dcId,
							  prjId:item.prjId,
							  vmId:item.vmId,
							  disks:disks
					  };
					  eayunHttp.post('cloud/volume/bindVolumes.do',data).then(function (response){
						  if(response&&response.data&&response.data.respCode=='000000'){
							  toast.success('云硬盘挂载中',2000);
							  $scope.ok();
						  }
						  else{
							  var successCount = response.data.data;
							  var failCount = disks.length - successCount;
							  eayunModal.warning('成功挂载了'+successCount+'块盘，'+failCount+'块挂载失败；若云主机正在创建自定义镜像，请稍候重试。').then(function(){
								  $scope.ok();
							  },function(){
								  $scope.ok();
							  });
							  $scope.checkBtn = false;
						  }
					  });
				  }
				  else if($scope.bindeddisks == 5){
					  $scope.ok();
					  eayunModal.warning('可挂载的硬盘数量已满');
				  }
			  });
			
	    };
	  
	  $scope.init();
  })
  /**
   * 调整云主机大小 Controller
   */
  .controller('ResizeVmController',function ($scope,eayunHttp, eayunModal,toast,$state, item,volume,eayunStorage,$modalInstance){
	  $scope.oldCpu = item.cpus;
	  $scope.oldRam = item.rams/1024;
	  $scope.model = angular.copy(item);
	  $scope.model.rams = $scope.model.rams/1024;
	  
	  
	  $scope.init = function () {
		  eayunHttp.post('cloud/vm/getImageByVm.do',item.fromImageId).then(function (response){
			  $scope.model.image = response.data.data;
			  
			  $scope.queryCpuList();
		  });
		  
		  if(item.payType == '1'){
			  eayunHttp.post('cloud/vm/queryVmChargeById.do',item.vmId).then(function(response){
				  if(response && response.data && response.data.data){
					  item.cycleCount = response.data.data.cycleCount;
				  }
			  });
		  }
		  
		  $scope.queryProjectQuota();
	  };
	  
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
	  
	  /**
	   * 查询项目下的CPU和内存配额
	   */
	  $scope.queryProjectQuota = function(){
		  eayunHttp.post('cloud/vm/getProjectjQuotaById.do',item.prjId).then(function (response){
			  $scope.project = response.data.data;
		  });
	  }
	  
	  /**
	   * 选择CPU
	   */
	  $scope.selectCpu = function (data){
		  $scope.model.cpus = data.nodeName.substr(0,data.nodeName.length-1);
		  $scope.queryRamList(data.nodeId);
	  };
	  
	  /**
	   * 选择内存
	   */
	  $scope.selectRam = function (data){
		  $scope.model.rams = data.nodeName.substr(0,data.nodeName.length-2);
		  
		  $scope.calcBillingFactor();
	  };
	  
	  /**
	   * 查询CPU列表
	   */
	  $scope.queryCpuList = function(){
		  $scope.cpuList = [];
		  $scope.ramList = [];
		  
		  eayunHttp.post('cloud/vm/getCpuList.do').then(function (response){
			  if(response.data.length>0){
				  var i = 0;
				  angular.forEach(response.data, function (value, key) {
					  var cpuVal = value.nodeName.substr(0,value.nodeName.length-1);
					  if( cpuVal == $scope.model.cpus){
						  $scope.cpuList[i++] = value;
						  $scope.queryRamList(value.nodeId);
					  }
					  else if(('2' == $scope.model.payType || cpuVal>=$scope.oldCpu) && 
							  (cpuVal>=$scope.model.image.minCpu) &&
							  ((cpuVal<=$scope.model.image.maxCpu && $scope.model.image.maxCpu>0)
									  || $scope.model.image.maxCpu ==0)){
						  $scope.cpuList[i++] = value;
					  }
				  });
			  }
		  });
	  };
	  
	 
	  /**
	   * 根据CPU选择内存大小
	   */
	  $scope.queryRamList = function(nodeId){
		  $scope.ramList = [];
		  var sameValue = false;
		  
		  eayunHttp.post('cloud/vm/getRamListByCpu.do',nodeId).then(function (response){
			  if(response.data.length>0){
				  var i = 0;
				  angular.forEach(response.data, function (value, key) {
					  var ramVal = value.nodeName.substr(0,value.nodeName.length-2);
					  if( ramVal == $scope.model.rams){
						  $scope.ramList[i++] = value;
						  sameValue = true;
					  }
					  else if(('2' == $scope.model.payType || ramVal>=$scope.oldRam) && 
							  (ramVal>=$scope.model.image.minRam/1024)&&
							  ((ramVal<=$scope.model.image.maxRam/1024 && $scope.model.image.maxRam >0) 
									  || $scope.model.image.maxRam == 0)){
						  $scope.ramList[i++] = value;
					  }
				  });
				  if($scope.ramList.length>0 && !sameValue){
					  $scope.model.rams = $scope.ramList[0].nodeName.substr(0,$scope.ramList[0].nodeName.length-2)
				  }
			  }
			  else{
				  $scope.model.rams = null;
			  }
			  $scope.calcBillingFactor();
		  });
	  };
	  
	  /**
	   * 资源计费查询
	   */
	  $scope.calcBillingFactor = function(){
		  $scope.priceDetails = null;
		  $scope.priceError = null;
		  var url = 'billing/factor/getPriceDetails.do';
		  var data = {};
		  data.dcId = $scope.model.dcId;
		  if('1' == $scope.model.payType){
			  data.cycleCount = item.cycleCount;
			  if(($scope.model.cpus - $scope.oldCpu)>0){
				  data.cpuSize = $scope.model.cpus - $scope.oldCpu;
			  }
			  if(($scope.model.rams - $scope.oldRam)>0){
				  data.ramCapacity = $scope.model.rams - $scope.oldRam;
			  }
			  url = 'billing/factor/getUpgradePrice.do';
		  }
		  else if('2' == $scope.model.payType){
			  data.payType = $scope.model.payType;
			  data.number = 1;
			  data.cpuSize = $scope.model.cpus;
			  data.ramCapacity = $scope.model.rams;
			  if(volume&&'1'==volume.volType){
				  data.sysDiskOrdinary=item.disks;
			  }else if(volume&&'2'==volume.volType){
				  data.sysDiskBetter=item.disks;
			  }else if(volume&&'3'==volume.volType){
				  data.sysDiskBest=item.disks;
			  }else{
				  data.sysDiskCapacity = item.disks;
			  }
			 
			  data.cycleCount = 1;
			  
			  if($scope.model.image){
				  data.imageId = $scope.model.image.imageId;
				  if($scope.model.image.sourceId){
					  data.imageId = $scope.model.image.sourceId;
				  }
			  }
		  }
		  eayunHttp.post(url,data).then(function (response){
			  if(response&&response.data){
				  if(response.data.respCode == '010120'){
					  $scope.priceError = response.data.message;
				  }
				  else{
					  $scope.priceDetails = response.data.data;
				  }
			  }
		  });
	  };
	  
	  $scope.resize = function (){
		  eayunHttp.post('cloud/vm/checkVmOrderExsit.do',item.vmId).then(function(response){
			  if(response && response.data && response.data.data){
				  var model = eayunModal.warning("资源正在调整中或您有未完成的订单，请您稍后重试。");
				  model.then(function(){
					  $scope.ok();
				  },function(){
					  $scope.ok();
				  });
			  }
			  else{
				  eayunHttp.post('cloud/vm/checkCreatingImage.do',item.vmId).then(function(response){
					  if(response && response.data && response.data.data){
						  var model = eayunModal.warning("云主机正在创建自定义镜像，请稍后重试");
						  model.then(function(){
							  $scope.ok();
						  },function(){
							  $scope.ok();
						  });
					  }
					  else {
						  var data = {};
						  data.sysDiskType=volume.volType;
						  data.dcId = item.dcId;
						  data.prjId = item.prjId;
						  data.vmId = item.vmId;
						  data.dcName = item.dcName;
						  data.vmName= item.vmName;
						  data.sysType = item.sysType;
						  data.sysTypeEn = item.sysTypeEn;
						  data.cpu = $scope.model.cpus;
						  data.ram = $scope.model.rams*1024; 
						  data.vmCpu = $scope.oldCpu;
						  data.vmRam = $scope.oldRam*1024;
						  data.orderType = '2';
						  data.count = 1;
						  if(item.payType == '1'){
							  data.cycleCount = item.cycleCount;
						  }
						  data.prodName = "云主机-调整配置";
						  data.payType = item.payType;
						  data.paymentAmount = $scope.priceDetails;
						  if('2' == data.payType){
							  data.paymentAmount = $scope.priceDetails.totalPrice;
						  }
						  
						  eayunStorage.set('order_confirm_vm',data);
						  $scope.ok();
						  $state.go('buy.confirmvm',{orderType:'2'});
					  }
				  });
				  
			  }
		  });
	  }
	  
	  /**
	   * 确认 调整云主机大小
	   */
	  $scope.commit = function (){
		  $scope.checkBtn = true;
		  if(item.payType == '2'){
			  $scope.isNSF = false;
			  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
				  $scope.account = response.data.data;
				  if($scope.account.money <= 0){
					  $scope.isNSF = true;
					  $scope.checkBtn = false;
					  var model = eayunModal.warning("您的账户已欠费，请充值后操作");
					  model.then(function(){
						  $scope.ok();
					  },function(){
						  $scope.ok();
					  });
				  }
				  else{
					  $scope.resize();
				  }
			  });
		  }
		  else if(item.payType == '1'){
			  $scope.resize();
		  }
	  
		  
	  };
	  
	  $scope.init();
  })
  .controller('MonitorController',function ($rootScope,$scope,eayunHttp,$stateParams, eayunModal){
	  var routeUrl = "app.cloud.cloudhost.hostdetail({'vmId':'"+$stateParams.vmId+"'})";
	  var list=[{route:'app.cloud.cloudhost.host',name:'云主机'},{route:routeUrl,name:'云主机详情'}];
	  $rootScope.navList(list,'监控信息','detail');
	  
	  $scope.vmId = $stateParams.vmId;
  })
  .controller('ModifySubnetCtrl',function ($scope,eayunHttp,item, eayunModal,toast,$modalInstance){
	  $scope.model = angular.copy(item);
	  if(!$scope.model.subnetId){
		  $scope.model.subnetId = -1;
	  }
	  if(!$scope.model.selfSubnetId){
		  $scope.model.selfSubnetId = -1;
	  }
	  
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
	  
	  /**
	   * 子网名称显示
	   */
	  $scope.substrSubnetName = function (text){
		  var testSubstr = text;
		  if(text.indexOf('(')!=-1){
			  var perText = text.substr(0,text.indexOf('('));
			  if(perText.length>9){
				  perText = perText.substr(0,9)+"...";
			  }
			  testSubstr = perText + text.substr(text.indexOf('('),text.length-1);
		  }
		  return testSubstr;
	  };
	  
	  /**
	   * 初始化
	   */
	  $scope.init = function(){
		  /**
		   *查询网络的受管子网列表 
		   */
		  eayunHttp.post('cloud/vm/querySubnetByNet.do',{ 'netId':item.netId,'subnetType':'1'}).then(function(response){
			  $scope.subnets = response.data.data;
		  });
		  
		  /**
		   *查询网络的自管子网列表 
		   */
		  eayunHttp.post('cloud/subnetwork/getsubnetlist.do',{ 'netId':item.netId,'subnetType':'0'}).then(function(response){
			  $scope.selfSubnets = response.data.resultData;
		  });
	  };
	  
	  /**
	   * 当前云主机的受管子网是否可以切换
	   */
	  $scope.canChangeVmIp = function(){
		  var data = {
				  'vmId':item.vmId,
				  'vmIp':item.vmIp,
				  'subnetId':$scope.model.subnetId,
				  'netId':item.netId
		  };
		  eayunHttp.post('cloud/vm/checkVmIpUsed.do',data).then(function(response){
			  $scope.canChangeVmIpFlag = response.data.data;
		  });
	  };
	  
	  /**
	   * 确认提交
	   */
	  $scope.commit = function(){
		  $scope.checkToken = true;
		  var data = {};
		  data.dcId = $scope.model.dcId;
		  data.prjId = $scope.model.prjId;
		  data.netId = $scope.model.netId;
		  data.subnetId = $scope.model.subnetId == '-1'?'':$scope.model.subnetId;
		  data.selfSubnetId = $scope.model.selfSubnetId == '-1'?'':$scope.model.selfSubnetId;
		  data.vmId = $scope.model.vmId;
		  data.vmName = $scope.model.vmName;
		  
		  eayunHttp.post('cloud/vm/modifysubnet.do',data).then(function(response){
			  if(null!=response.data&&response.data.respCode == '400000'){
    			  toast.success('修改子网成功'); 
    			  $scope.checkToken = false;
    			  $scope.ok();
    		  }
    		  else {
    			  $scope.checkToken = false;
    		  }
          },function (){
        	  $scope.checkToken = false;
		  });
	  };
	  
	  $scope.init();
  })
  .controller('cloudhostRenewCtrl',function ($scope,eayunHttp,item, volume,eayunModal,eayunStorage,$modalInstance){
	  $scope.model = angular.copy(item);
	  $scope.volume=angular.copy(volume);
	 
	  /**
	   * 云主机状态 显示
	   */
	  $scope.vmStatusClass ='';
	  $scope.checkVmStatus =function (item){
		  if('1'==item.chargeState||'2'==item.chargeState||'3'==item.chargeState){
			  return 'ey-square-disable';
		  }
		  else if(item.vmStatus&&item.vmStatus=='ACTIVE'){
			  return 'ey-square-right';
		  }  
		  else if(item.vmStatus=='SHUTOFF'){
			  return 'ey-square-disable';
		  }
		  else if(item.vmStatus=='SUSPENDED' || item.vmStatus=='ERROR'){
			  return 'ey-square-error';
		  }
		  else{
			  return 'ey-square-warning';
		  }
	  };  
	  
	  
	  
	  
	  $scope.cancel = function () {
          $modalInstance.dismiss('cancel');
      };
      $scope.commit = function () {
          $modalInstance.close();
      };
      
	  eayunStorage.set('payType' , item.payType);
	  eayunStorage.set('dcName'  , item.dcName);
	  eayunStorage.set('vmId'   , item.vmId);
	  eayunStorage.set('vmName' , item.vmName);
	  eayunStorage.set('cpus' , item.cpus);
	  eayunStorage.set('rams' , item.rams);
	  eayunStorage.set('disk' , item.disks);
	  eayunStorage.set('sysType' , item.sysType);
	  eayunStorage.set('imageId' , item.sourceId);//镜像类型
	  eayunStorage.set('dcId' , item.dcId);
	  eayunStorage.set('volType' , $scope.volume.volType);
	  eayunStorage.set('volumeTypeAs' , $scope.volume.volumeTypeAs);
	  
	  $scope.model.renewType = 'month';
	  $scope.model.renewTime = '1';
	  eayunStorage.set('cycle' , $scope.model.renewTime);
	  
	  initGetPriceForRenew();
	  /**
	   * 切换付费类型
	   */
	  $scope.changePayType = function (){
		  if($scope.model.renewType=='year'){
			  $scope.model.renewTime = '12';
			  eayunStorage.set('cycle' , $scope.model.renewTime);
		  }else{
			  $scope.model.renewTime = '1';
			  eayunStorage.set('cycle' , $scope.model.renewTime);
		  }
		  initGetPriceForRenew();
	  };
	  /**
	   * 切换时间选择
	   */
	  $scope.changeTime = function (renewType , renewTime){
		  if(renewTime == '0'){
			  $scope.model.chargeMoney = null;
			  $scope.model.lastTime = null;
		  }
		  if(renewType != 'zero' && renewTime != '0'){
			  eayunStorage.set('cycle'   , renewTime);
			//调用计费算法得出需要支付的费用
			  var cycleCount = renewTime;
			  var paramBean = {
					  'dcId':item.dcId,
					  'payType':'1',
					  'number':1,
					  'cycleCount':cycleCount,
					  'cpuSize':item.cpus,
					  'ramCapacity':item.rams/1024
					  };
			 
			  if('1'==$scope.volume.volType){
				  paramBean.sysDiskOrdinary=item.disks;
			  }else if('2'==$scope.volume.volType){
				  paramBean.sysDiskBetter=item.disks;
			  }else if('3'==$scope.volume.volType){
				  paramBean.sysDiskBest=item.disks;
			  }else{
				  paramBean.sysDiskCapacity = item.disks;
			  }
			 
			  if(null!=item.sourceId && 'null'!=item.sourceId){
				  paramBean.imageId = item.sourceId;
			  }
			  eayunStorage.set('paramBean' , paramBean);
			  eayunHttp.post('billing/factor/getPriceDetails.do',paramBean).then(function(response){
				  $scope.responseCode = response.data.respCode;
	              if($scope.responseCode =='010120'){
	                  $scope.respMsg = response.data.message;
	              }else{
	            	  $scope.model.chargeMoney = response.data.data.totalPrice;
					  eayunStorage.set('needPay' , response.data.data.totalPrice);
	              }
				  
			  });
			  //计算续费后的到期时间
			  eayunHttp.post('order/computeRenewEndTime.do',{'original':$scope.model.endTime ,'duration':renewTime}).then(function(response){
				  $scope.model.lastTime = response.data;
			  });
			  
		  }
		  
		  
	  };
	  function initGetPriceForRenew(){
		  var paramBean = {
				  'dcId':item.dcId,
				  'payType':'1',
				  'number':1,
				  'cycleCount':$scope.model.renewTime,
				  'cpuSize':item.cpus,
				  'ramCapacity':item.rams/1024
				  };
		  if('1'==$scope.volume.volType){
			  paramBean.sysDiskOrdinary=item.disks;
		  }else if('2'==$scope.volume.volType){
			  paramBean.sysDiskBetter=item.disks;
		  }else if('3'==$scope.volume.volType){
			  paramBean.sysDiskBest=item.disks;
		  }else{
			  paramBean.sysDiskCapacity = item.disks;
		  }
		  if(null!=item.sourceId && 'null'!=item.sourceId){
			  paramBean.imageId = item.sourceId;
		  }
		  eayunStorage.set('paramBean' , paramBean);
		  eayunHttp.post('billing/factor/getPriceDetails.do',paramBean).then(function(response){
			  $scope.responseCode = response.data.respCode;
              if($scope.responseCode =='010120'){
                  $scope.respMsg = response.data.message;
              }else{
				  $scope.model.chargeMoney = response.data.data.totalPrice;
				  eayunStorage.set('needPay' , response.data.data.totalPrice);
              }
			  
		  });
		//计算续费后的到期时间
		  eayunHttp.post('order/computeRenewEndTime.do',{'original':$scope.model.endTime ,'duration':$scope.model.renewTime}).then(function(response){
			  $scope.model.lastTime = response.data;
		  });
	  };
	  
  }).controller('RenewConformVmController',function ($scope,eayunHttp, eayunModal,eayunStorage,$state,VmService,eayunMath){
	  //如果F5刷新  直接跳路由
	  if('undefined'== eayunStorage.get('needPay') || null == eayunStorage.get('needPay')){
		  $state.go("app.cloud.cloudhost.host");
	  }
	  
	  var needPay = eayunStorage.get('needPay');
	  $scope.model = {
			  payType : eayunStorage.get('payType'),
			  dcName  : eayunStorage.get('dcName'),
			  vmId   : eayunStorage.get('vmId'),
			  vmName : eayunStorage.get('vmName'),
			  cpus : eayunStorage.get('cpus'),
			  rams : eayunStorage.get('rams'),
			  disk : eayunStorage.get('disk'),
			  sysType :eayunStorage.get('sysType'),
			  cycle   : eayunStorage.get('cycle'),
			  needPay : needPay,
			  imageId: eayunStorage.get('imageId'),
			  dcId : eayunStorage.get('dcId'),
			  volType : eayunStorage.get('volType'),
			  volumeTypeAs : eayunStorage.get('volumeTypeAs')
	        };
	  
	  
	  
	//查询账户金额
	  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do',{}).then(function(response){
          var accountMoney = response.data.data.money;
          $scope.model.accountMoney = accountMoney;
          
      });
	  $scope.isUse = false;
	  $scope.model.deductMoney = 0.00;//formatFloat(0,2);
	  $scope.model.actualPay = $scope.model.needPay;
	  //计算余额支付价钱
	  $scope.useBalance = function(){
		  if($scope.model.isCheck){//选中
              $scope.isLight = false;
			  $scope.isUse = true;
			  if($scope.model.accountMoney - $scope.model.needPay >= 0 ){
				  $scope.model.deductMoney = $scope.model.needPay;
				  $scope.model.actualPay = 0.00;//formatFloat(0.00,2);
			  }else{
				  $scope.model.deductMoney = $scope.model.accountMoney;
				  var payable = eayunMath.sub($scope.model.needPay,$scope.model.accountMoney);//$scope.model.needPay - $scope.model.accountMoney;
				  $scope.model.actualPay = payable;
				  
				  
			  }
		  }else{
			  $scope.isUse = false; 
			  $scope.model.deductMoney = 0.00;
			  $scope.model.actualPay = $scope.model.needPay;
		  }
	  };
	  //检查是否点击同意按钮
	  $scope.flag = true;
	  $scope.checkAgree = function(){
		  if($scope.model.isAgree){
			  $scope.flag = false;
		  }else{
			  $scope.flag = true;
		  }
	  };

    $scope.paramBean = eayunStorage.get('paramBean');
    //续费订单确认页面刷新“产品金额”，“账户余额”
      function refreshMoney() {
          var b1,b2;
          //获取账户余额
          eayunHttp.post('costcenter/accountoverview/getaccountbalance.do', {}).then(function (response) {
              b1 = true;
              var balance = response.data.data.money;
              $scope.model.accountMoney = balance;
              if(b1&&b2){
              	$scope.useBalance();
              }
          });

          eayunHttp.post('billing/factor/getPriceDetails.do',$scope.paramBean).then(function (response) {
              b2 = true;
              $scope.responseCode = response.data.respCode;
              if($scope.responseCode =='010120'){
                  $scope.errorMsg = response.data.message;
              }else{
                  $scope.model.needPay = response.data.data.totalPrice;
              }
              if(b1&&b2){
              	$scope.useBalance();
              }
          });
      };
      function refreshConfiguration(_vmId){
          eayunHttp.post('cloud/vm/getVmById.do',_vmId).then(function (response){
              if(response.data!=null&&response.data.data!=null){
                  var data = response.data.data;
                  $scope.model.vmName = data.vmName;
                  $scope.model.cpus = data.cpus;
                  $scope.model.disk = data.disks;
                  $scope.model.rams = data.rams;
                  $scope.paramBean.cpuSize = $scope.model.cpus;
                  if('1'==eayunStorage.get('volType')){
                      $scope.paramBean.sysDiskOrdinary = $scope.model.disk;
                  }else if('2'==eayunStorage.get('volType')){
                      $scope.paramBean.sysDiskBetter = $scope.model.disk;
                  }else if('3'==eayunStorage.get('volType')){
                      $scope.paramBean.sysDiskBest = $scope.model.disk;
                  }else{
                      $scope.paramBean.sysDiskCapacity = $scope.model.disk;
                  }
                  $scope.paramBean.ramCapacity = $scope.model.rams/1024;
                  refreshMoney();
              }
              else if(response.data.respCode=='400000'&&response.data.data==null) {
                  $state.go('app.cloud.cloudhost.host');
              }
          });
      };
      
    //提交订单
    $scope.isLight = false;
    $scope.isError = false;
    $scope.errorMsg ='';
      $scope.submitOrder = function(){
    	  VmService.checkIfOrderExist($scope.model.vmId).then(function(response){
    		  $scope.errorMsg ='资源正在调整中或您有未完成的订单，请稍后再试。';
    		  $scope.isLight = true;
          },function(){
        	// 获取当前余额，判断此时账户余额是否大于余额支付金额，以确定能否提交订单
              var map ={
            		  vmId   : $scope.model.vmId,
            		  aliPay : $scope.model.actualPay,
            		  accountPay : $scope.model.deductMoney,
            		  totalPay : $scope.model.needPay,
            		  isCheck : $scope.model.isCheck,
            		  dcName : $scope.model.dcName,
            		  cpuSize : $scope.model.cpus,
            		  rams : $scope.model.rams,
            		  disks : $scope.model.disks,
            		  sysType : $scope.model.sysType,
            		  buyCycle : $scope.model.cycle,
            		  dcId : $scope.model.dcId,
    				  payType : '1',
    				  number : 1,
    				  cycleCount : $scope.model.cycle,
    				  ramCapacity : $scope.model.rams/1024,
    				  sysDiskCapacity : $scope.model.disk,
    				  volType : $scope.model.volType,
      				  volumeTypeAs:$scope.model.volumeTypeAs
              };
              if(null!= eayunStorage.get('imageId') && 'null'!=eayunStorage.get('imageId')){
    			  map.imageId = eayunStorage.get('imageId');
    		  }
              eayunHttp.post('cloud/vm/renewVmOrderConfirm.do',map).then(function(response){
    			  if(response && response.data){
    				  //订单支付成功
    				  if(response.data.respCode == '1'){//您当前有未完成订单，不允许提交新订单！
    					  $scope.isLight = true;
    					  $scope.errorMsg =response.data.message;
    				  }
    				  else if(response.data.respCode == '2'){//您的产品金额发生变动，请重新确认订单！
    					  $scope.isLight = true;
                          $scope.model.isCheck = false;
    					  $scope.errorMsg =response.data.message;
                          //todo refresh configuration
                          refreshConfiguration($scope.model.vmId);
    				  }
    				  else if(response.data.respCode == '3'){//您的账户余额发生变动，请重新确认订单！
    					  $scope.isLight = true;
                          $scope.model.isCheck = false;
    					  $scope.errorMsg =response.data.message;
    					  refreshMoney(); 
    				  }
    				  else if(response.data.respCode == '0'){//完全支付宝支付，跳向支付宝支付页面！
    					  var orderPayNavList = [{route:'app.cloud.cloudhost.host',name:'云主机'}];
                          eayunStorage.persist("orderPayNavList",orderPayNavList);
                          eayunStorage.persist("payOrdersNo",response.data.message);
    					  $state.go('pay.order');
    				  }
    				  else if(response.data.respCode == '10'){//完全余额支付，跳向支付成功页面！
    					  $state.go('pay.result', {subject:response.data.message});
    				  }
    			  }
    			  
    		  });

          });
    	  
      };  
	  
	  
  }).controller('DeleteVmController',function ($scope,eayunHttp,toast,eayunModal,item,$modalInstance,$state){
	  $scope.item= {};
	  $scope.item.vmName = item.vmName;
	  if('0' != item.chargeState){
		  $scope.item.isSoftDelete = true;
		  $scope.item.isChange = true;
	  }
	  
	  /**
	   * 关闭界面
	   */
	  $scope.cancel = function(){
		  $modalInstance.close();
	  };
	  
	  /**
	   * 确定删除
	   */
	  $scope.commit = function (){
		  $scope.checkBtn = true;
		  var cloudVm ={};
		  cloudVm.deleteType = '0';
		  
		  cloudVm.dcId = item.dcId;
		  cloudVm.prjId = item.prjId;
		  cloudVm.vmId = item.vmId;
		  cloudVm.vmName = item.vmName;
		  if($scope.item.isSoftDelete){
			  cloudVm.deleteType = '1';
		  }
		  
		  eayunHttp.post('cloud/vm/deleteVm.do',cloudVm).then(function(response){
			  if(null!=response.data&&response.data.respCode == '100000'){
				  toast.success('云主机删除中',2000); 
				  $scope.$emit("RefreshUnhandledAlarmMsgCount");
				  $scope.cancel();
				  $state.go('app.cloud.cloudhost.host');
			  }
			  else{
				  $scope.checkBtn = false;
				  $scope.cancel();
			  }
		  },function(){
			  $scope.cancel();
		  });
	  };
}).controller('DebindvolumeCtrl',function ($scope,eayunHttp,toast,eayunModal,item,$modalInstance,$state){	
	$scope.name = item.volName;
	 /**
	   * 关闭界面
	   */
	  $scope.cancel = function(){
		  $modalInstance.close();
	  };
	  
	  /**
	   * 确定删除
	   */
	  $scope.commit = function (){
		  $scope.checkBtn = true;
		  
		  eayunHttp.post("cloud/volume/debindVolume.do",item).then(function(response){
				if(response.data!=null&&response.data==true){
					toast.success('开始解绑云硬盘',2000); 
					$scope.cancel();
				}
				else{
					  $scope.checkBtn = false;
				  }
			});
	  };
})
/**
 * 绑定/解绑SSH密钥
 */
.controller('EditSSHController',function ($scope,eayunHttp, eayunModal, item,toast,$modalInstance){
	  
	  $scope.init = function () {
		  $scope.secretList = [];
		  
		  
		  var list = [],b1,b2;
		  eayunHttp.post('cloud/vm/getUnbindSecretkeyByPrj.do',{prjId:item.prjId,vmId:item.vmId}).then(function (response){
			 $scope.allGroups = response.data;
			 b1 = true;
			 if($scope.allGroups && $scope.allGroups.length>0){
				 angular.forEach($scope.allGroups, function (value, key) {
					 value.$$selected = false;
					 list.push(value);
				  });
			 }
			 if(b1&&b2){
				 $scope.secretList = list;
			 }
		  }); 
		  eayunHttp.post('cloud/vm/getBindSecretkeyByVm.do',item.vmId).then(function (response){
			 $scope.vmGroups = response.data;
			 b2 = true;	
			 if($scope.vmGroups && $scope.vmGroups.length>0){
				 angular.forEach($scope.vmGroups, function (value, key) {
					 value.$$selected = true;
					 list.push(value);
				  });
			 }
			 if(b1&&b2){
				 $scope.secretList = list;
			 }
		  });
		  
	  };
	  
	  $scope.cancel = function (){
		  $modalInstance.close();
	  };
	  
	  $scope.selectData = function(){
    	  $scope.selectSecret = [];
    	  if($scope.secretList && $scope.secretList.length>0){
				 angular.forEach($scope.secretList, function (value, key) {
					 if(value.$$selected){
						 $scope.selectSecret.push(value.secretkeyId);
					 }
				  });
			 }
      }
	  
	  
      /**
	   * 绑定/解绑SSH密钥
	   */
	  $scope.commit = function (){
		  $scope.checkToken = true;
		  eayunModal.confirm('确定要绑定/解绑SSH密钥？').then(function () {
			  $scope.selectData();
			  var cloudVm = {
					  dcId:item.dcId,
					  prjId:item.prjId,
					  vmId:item.vmId,
					  vmName:item.vmName,
					  csks : $scope.selectSecret
			  };
			  eayunHttp.post('cloud/vm/editSecretKey.do',cloudVm).then(function(response){
				  if(null!=response.data&&response.data.respCode == '400000'){
					  toast.success('绑定/解绑SSH密钥成功',2000); 
					  $scope.checkToken = false;
					  $scope.cancel();
				  }
				  else {
					  $scope.checkToken = false;
				  }
			  },function (){
				  $scope.checkToken = false;
			  });
		  },function(){
			  $scope.checkToken = false;
			  $scope.cancel();
		  });
	  };
	  
	  $scope.init();
  })
  /**
   * 密码修改的Controller
   */
  .controller('ResetPwdController',function ($scope,eayunHttp, eayunModal, item,toast,$modalInstance){
	  
  	  $scope.cancel = function (){
  		  $modalInstance.close();
  	  };
  	  
  	 $scope.focus = function(_value){
		  if(_value == 'rePwd'){
			  $scope.isRePwdOnFocus= false;
		  }
		  else if(_value == 'pwd'){
			  $scope.isPwdOnFocus = false;
		  }
	  };
	  
	  $scope.passBlur = function(){
		  $scope.checkPassword();
		  $scope.repassBlur();
		  $scope.isPwdOnFocus = true;
	  };
	  
	  $scope.repassBlur = function(){
		  $scope.isRePwdOnFocus = $scope.model.password != $scope.model.repassword;
	  };
	  
	  /**
	   * 校验密码的合法性和约束性
	   */
	  $scope.checkPassword = function (){
		  var pwd = $scope.model.password;
		  var numFlag = 0;
		  var lowerCharFlag = 0;
		  var upperCharFlag = 0;
		  var specCharFlag = 0;
		  var regex =new RegExp("^[0-9a-zA-Z~@#%+-=\/\(_\)\*\&\<\>\[\\]\\\\\"\;\'\|\$\^\?\!.\{\}\`/\,]{8,30}$");
		  var regexNum =new RegExp("^[0-9]$");
		  var regexLowerChar =new RegExp("^[a-z]$");
		  var regexUpperChar =new RegExp("^[A-Z]$");
		  var regexSpecChar =new RegExp("^[~@#%+-=\/\(_\)\*\&\<\>\[\\]\\\\\"\;\'\|\$\^\?\!.\{\}\`/\,]$");
		  if(pwd&&regex.test(pwd)){
			  for(var i=0;i<pwd.length;i++){
				  if(pwd[i]&&regexNum.test(pwd[i])){
					  numFlag = 1;
					  continue;
				  }
				  else if(pwd[i]&&regexLowerChar.test(pwd[i])){
					  lowerCharFlag = 1;
					  continue;
				  }
				  else if(pwd[i]&&regexUpperChar.test(pwd[i])){
					  upperCharFlag = 1;
					  continue;
				  }
				  else if(pwd[i]&&regexSpecChar.test(pwd[i])){
					  specCharFlag = 1;
					  continue;
				  }
			  }
		  }
		  $scope.checkPasswordFlag = (numFlag+lowerCharFlag+upperCharFlag+specCharFlag)<3;
	  };
  	  
      /**
  	   * 密码修改
  	   */
  	  $scope.commit = function (){
  		  $scope.checkToken = true;
  		  var cloudVm = {
			  dcId:item.dcId,
			  prjId:item.prjId,
			  vmId:item.vmId,
			  vmName:item.vmName,
			  password : $scope.model.password
		  };
		  eayunHttp.post('cloud/vm/modifyPwd.do',cloudVm).then(function(response){
			  if(null!=response.data&&response.data.respCode == '400000'){
				  toast.success('密码修改成功',2000); 
				  $scope.checkToken = false;
				  $scope.cancel();
			  }
			  else {
				  $scope.checkToken = false;
				  $scope.cancel();
			  }
		  },function (){
			  $scope.checkToken = false;
		  });
  	  };
    })
;