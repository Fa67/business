'use strict';
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $stateProvider.state('app.auth.applymng', {//路由
      url: '/applymng',
      templateUrl: 'views/auth/applymng/applymng.html',
      controller: 'ApplyMngCtrl',
      resolve:{
    	  projects:function (eayunHttp){
    		  return eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
    	    	  return  response.data.data;
    	      });
    	  }
      }
    });
  })

/**
 * 配额信息
 * controller
 */
  .controller('ApplyMngCtrl', function (eayunStorage,$state,$scope, $window,eayunModal,eayunHttp,toast,projects,powerService) {
	  var navLists=eayunStorage.get('navLists');
	  navLists.length=0;
	  navLists.push({route:'app.auth.applymng',name:'配额信息'});
	  //权限控制
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.buttonPower = {
				  isApplyWork : powerService.isPower('overview_apply'),//申请配额
		 };
	  });
	  $scope.model = {};
	  $scope.cloudprojectList = projects;
  	  var daPrj = sessionStorage["dcPrj"];
  	  if(daPrj){
  		  daPrj = JSON.parse(daPrj);
  		  angular.forEach($scope.cloudprojectList, function (value,key) {
  			  if(value.projectId == daPrj.projectId){
  				  $scope.model.projectvoe = value;
  			  }
  		  });
  	  }else{
  		  angular.forEach($scope.cloudprojectList, function (value) {
  				if(value.projectId!=null&&value.projectId!=''&&value.projectId!='null'){
  					$scope.model.projectvoe = value;
  					$window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
  				}
  			});
  	  }
    $scope.$watch('model.projectvoe.projectId' , function(newVal,oldVal){
      	if(newVal !== oldVal){
      		if(newVal==null||newVal==''||newVal=='null'){
      			$scope.dcId = $scope.model.projectvoe.dcId;
      			angular.forEach($scope.cloudprojectList, function (value) {
      				if(oldVal == value.projectId){
      					$scope.model.projectvoe = value;
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
      			$window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
      			statPrj($scope.model.projectvoe.projectId);
      		}
      	}
      },true);
	$scope.item = {};
	$scope.openProPop = function(type){
		if($scope.prj){
			if(type=="Host"){
				$scope.numerator = $scope.prj.usedVmCount;
				$scope.denominator = $scope.prj.hostCount;
				$scope.hintHostShow = true;
			}
			if(type=="Cpu"){
				$scope.numerator = $scope.prj.usedCpuCount;
				$scope.denominator = $scope.prj.cpuCount;
				$scope.hintCpuShow = true;
			}
			if(type=="Mem"){
				$scope.numerator = $scope.prj.usedRam/1024;
				$scope.denominator = $scope.prj.memory;
				$scope.hintMemShow = true;
			}
			if(type=="Vol"){
				$scope.numerator = $scope.prj.diskCountUse;
				$scope.denominator = $scope.prj.diskCount;
				$scope.hintVolShow = true;
			}
			if(type=="VolSize"){
				$scope.numerator = $scope.prj.usedDiskCapacity;
				$scope.denominator = $scope.prj.diskCapacity;
				$scope.hintVolSizeShow = true;
			}
			if(type=="Snap"){
				$scope.numerator = $scope.prj.diskSnapshotUse;
				$scope.denominator = $scope.prj.diskSnapshot;
				$scope.hintSnapShow = true;
			}
			if(type=="SnapSize"){
				$scope.numerator = $scope.prj.usedSnapshotCapacity;
				$scope.denominator = $scope.prj.snapshotSize;
				$scope.hintSnapSizeShow = true;
			}
			if(type=="VPN"){
				$scope.numerator = $scope.prj.countVpnUse;
				$scope.denominator = $scope.prj.countVpn;
				$scope.hintVPNShow = true;
			}
			/*if(type=="Image"){
				$scope.numerator = $scope.prj.diskCapacityUse;
				$scope.denominator = $scope.prj.diskCapacity;
				$scope.hintImageShow = true;
			}*/
			if(type=="Band"){
				$scope.numerator = $scope.prj.countBandUse;
				$scope.denominator = $scope.prj.countBand;
				$scope.hintBandShow = true;
			}
			if(type=="Net"){
				$scope.numerator = $scope.prj.netWorkUse;
				$scope.denominator = $scope.prj.netWork;
				$scope.hintNetShow = true;
			}
			if(type=="Sub"){
				$scope.numerator = $scope.prj.subnetCountUse;
				$scope.denominator = $scope.prj.subnetCount;
				$scope.hintSubShow = true;
			}
			if(type=="Safe"){
				$scope.numerator = $scope.prj.safeGroupUse;
				$scope.denominator = $scope.prj.safeGroup;
				$scope.hintSafeShow = true;
			}
			if(type=="Float"){
				$scope.numerator = $scope.prj.outerIPUse;
				$scope.denominator = $scope.prj.outerIP;
				$scope.hintFloatShow = true;
			}
			if(type=="Load"){
				$scope.numerator = $scope.prj.usedPool;
				$scope.denominator = $scope.prj.quotaPool;
				$scope.hintLoadShow = true;
			}
			if(type=="Mapping"){
				$scope.numerator = $scope.prj.portMappingUse;
				$scope.denominator = $scope.prj.portMappingCount;
				$scope.hintMappingShow = true;
			}
			if(type=="Sms"){
				$scope.numerator = $scope.prj.smsQuota;
				$scope.denominator = $scope.prj.smsCount;
				$scope.hintSmsShow = true;
			}
		}
	};
	function statPrj(prjId){
		eayunHttp.post("sys/overview/getStatisticsByPrjId.do",prjId).then(function(respone){
			$scope.prj=respone.data;
		});
	};
	if($scope.model.projectvoe){
		statPrj($scope.model.projectvoe.projectId);
	}
	$scope.addApplyWork=function(prj){
		  var result = eayunModal.open({
			  	backdrop:'static',
		        templateUrl: 'views/work/addapplywork.html',
		        controller: 'addApplyWorkCtrl',
		        resolve:{
		        	prj:prj
	            }
		   }).result;
		  result.then(function (workorder){
			  var workQuota = workorder.workQuota;
			  delete workorder.workQuota;
			  eayunHttp.post("sys/work/addQuotaWork.do",{"workorder":workorder,"workQuota":workQuota}).then(function(respose){
				  toast.success("配额申请已提交");
				  $state.go("app.auth.applymng",{},{reload:true});
			  });
	      }, function () {
	    	  
	      });
	  };
  })
  .controller("addApplyWorkCtrl",function($scope ,$state,eayunModal,eayunHttp,prj,$modalInstance){
	  $scope.prj=prj;
	  $scope.checkQuotaFalg=false;
	  $scope.btnShow = true;
	  eayunHttp.post("sys/work/getUserInfo.do").then(function(data){//获取工单问题类型
			$scope.workorder={//给单选框默认值
					workType:'0007001003001',
					workTypeName:'配额',
					workTitle:"申请扩大"+prj.dcName+"配额",
					workPhone:data.data.phone,
					workEmail:data.data.email,
					workPhoneTime:"2",
					workCreUserName:data.data.userName,
					workQuota:{}
			};
			
			if(data.data.phone==null && data.data.email==null){
				$scope.mailPhoneBtn=true;
			}else{
				$scope.mailPhoneBtn=false;
			}
	  });
	  $scope.$watch('workQuota' , function(newVal,oldVal){
			if(newVal !== oldVal){
				$scope.workQuota=newVal;
				checkQuota();
			}
	  },true);
	  	$scope.mailPhone=false;
	  	$scope.phone=false;
		$scope.mail=false;
		$scope.checkMailPhone=function(){
			var tel = $scope.workorder.workPhone;
			var email = $scope.workorder.workEmail;
			if(email==""){
				email=null;
			}
			if(tel==""){
				tel=null;
			}
			var regxPhone=/^1[3|5|8|7][0-9]\d{8}$/;
			var regxMail=/^[a-zA-Z0-9_-]+[\.a-zA-Z0-9_-]+[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
			
			$scope.mailPhone=false;
			$scope.mailPhoneBtn=false;
			if(tel!=null&& email!=null){//都不为空
				if(regxPhone.test(tel)){//电话通过验证
					$scope.phone=false;
				}else{
					$scope.phone=true;
					$scope.mailPhoneBtn=true;
				}
				if(regxMail.test(email)){//邮箱通过验证
					$scope.mail=false;
				}else{
					$scope.mail=true;
					$scope.mailPhoneBtn=true;
				}
			}else if(tel!=null && email==null){//手机不为空，邮箱为空
				$scope.mail=false;
				if(regxPhone.test(tel)){//电话通过验证
					$scope.phone=false;
				}else{
					$scope.phone=true;
					$scope.mailPhoneBtn=true;
				}
			}else if(tel==null && email!=null){//手机为空，邮箱不为空
				$scope.phone=false;
				if(regxMail.test(email)){//邮箱通过验证
					$scope.mail=false;
				}else{
					$scope.mail=true;
					$scope.mailPhoneBtn=true;
				}
			}else{//都为空
				$scope.phone=false;
				$scope.mail=false;
				$scope.mailPhone=true;
				$scope.mailPhoneBtn=true;
			}
		};
		//配额总体验证
	    function displayProp(obj){      
            var names="";
            for(var name in obj){         
               names+=name+": "+obj[name]+", ";    
            }
            if(names.match(/\d+/g)) {
            	return true;
            }else{
            	return false;
            }
        }  
	  //配额验证
	  function checkQuota(){
		  var workQuota=$scope.workQuota;
		  var bool= displayProp(workQuota);
		  var rgex=new RegExp("^\\+?[1-9][0-9]*$");
		  var slaveRegex=new RegExp("^([3-9]|10)$");
		  var backupRegex=new RegExp("^[3-7]$");
		  $scope.checkQuotaVmFalg=false;
		  $scope.checkQuotaCpuFalg=false;  
		  $scope.checkQuotaMemoryFalg=false;
		  $scope.checkQuotaDiskFalg=false;
		  $scope.checkQuotaShotFalg=false;
		  $scope.checkQuotaDiskSizeFalg=false;	//云硬盘容量（GB）
		  $scope.checkQuotaShotSizeFalg=false;	//云硬盘备份容量（GB）
		  $scope.checkQuotaBandFalg=false;
		  $scope.checkQuotaVPNFalg=false;		//VPN
		  $scope.checkQuotaNetFalg=false;
		  $scope.checkQuotaSubnetFalg=false;
		  $scope.checkQuotaGroupFalg=false;
		  $scope.checkQuotaFloatIpFalg=false;
		  $scope.checkQuotaBalanceFalg=false;
		  $scope.checkQuotaMappingFalg=false;	//端口映射
		  $scope.checkSmsQuotaFalg=false;
		  $scope.checkInstanceQuotaFlag = false; 		//RDS 主实例
		  $scope.checkSlaveInstanceQuotaFlag = false; 	//RDS 主实例可创建从实例
		  $scope.checkBackupByHandQuotaFlag = false;	//RDS 手动备份数量/实例
		  $scope.checkBackupByAutoQuotaFlag = false;	//RDS 自动备份数量/实例
		  
		  if(workQuota.quotaVm !=null && workQuota.quotaVm !="" && (!rgex.test(workQuota.quotaVm) || workQuota.quotaVm<=prj.hostCount)){
			  $scope.checkQuotaVmFalg=true;  
		  }
		  if(workQuota.quotaCpu!=null && workQuota.quotaCpu!="" && (!rgex.test(workQuota.quotaCpu) || workQuota.quotaCpu<=prj.cpuCount)){
			  $scope.checkQuotaCpuFalg=true;  
		  }
		  if(workQuota.quotaMemory!=null && workQuota.quotaMemory!="" && (!rgex.test(workQuota.quotaMemory) || workQuota.quotaMemory<=prj.memory)){
			  $scope.checkQuotaMemoryFalg=true;  
		  }
		  if(workQuota.quotaDisk!=null && workQuota.quotaDisk!="" && (!rgex.test(workQuota.quotaDisk) || workQuota.quotaDisk<=prj.diskCount)){
			  $scope.checkQuotaDiskFalg=true;  
		  }
		  if(workQuota.quotaSnapshot!=null && workQuota.quotaSnapshot!="" && (!rgex.test(workQuota.quotaSnapshot) || workQuota.quotaSnapshot<=prj.diskSnapshot)){
			  $scope.checkQuotaShotFalg=true;  
		  }
		  if(workQuota.quotaDiskSize!=null && workQuota.quotaDiskSize!="" && (!rgex.test(workQuota.quotaDiskSize) || workQuota.quotaDiskSize<=prj.diskCapacity)){
			  $scope.checkQuotaDiskSizeFalg=true;	//云硬盘容量（GB）
		  }
		  if(workQuota.quotaShotSize!=null && workQuota.quotaShotSize!="" && (!rgex.test(workQuota.quotaShotSize) || workQuota.quotaShotSize<=prj.snapshotSize)){
			  $scope.checkQuotaShotSizeFalg=true;	//云硬盘备份容量（GB）
		  }
		  if(workQuota.quotaBand!=null && workQuota.quotaBand!="" && (!rgex.test(workQuota.quotaBand) || workQuota.quotaBand<=prj.countBand)){
			  $scope.checkQuotaBandFalg=true;  
		  }
		  if(workQuota.quotaVpn!=null && workQuota.quotaVpn!="" && (!rgex.test(workQuota.quotaVpn) || workQuota.quotaVpn<=prj.countVpn)){
			  $scope.checkQuotaVPNFalg=true;		//VPN
		  }
		  if(workQuota.quotaNet!=null && workQuota.quotaNet!="" && (!rgex.test(workQuota.quotaNet) || workQuota.quotaNet<=prj.netWork)){
			  $scope.checkQuotaNetFalg=true;  
		  }
		  if(workQuota.quotaSubnet!=null && workQuota.quotaSubnet!="" && (!rgex.test(workQuota.quotaSubnet) || workQuota.quotaSubnet<=prj.subnetCount)){
			  $scope.checkQuotaSubnetFalg=true;  
		  }
		  if(workQuota.quotaSecGroup!=null && workQuota.quotaSecGroup!="" && (!rgex.test(workQuota.quotaSecGroup) || workQuota.quotaSecGroup<=prj.safeGroup)){
			  $scope.checkQuotaGroupFalg=true;  
		  }
		  if(workQuota.quotaFloatIp!=null && workQuota.quotaFloatIp!="" && (!rgex.test(workQuota.quotaFloatIp) || workQuota.quotaFloatIp<=prj.outerIP)){
			  $scope.checkQuotaFloatIpFalg=true;  
		  }
		  if(workQuota.quotaBalance!=null && workQuota.quotaBalance!="" && (!rgex.test(workQuota.quotaBalance) || workQuota.quotaBalance<=prj.quotaPool)){
			  $scope.checkQuotaBalanceFalg=true;  
		  }
		  if(workQuota.quotaPortMapping!=null && workQuota.quotaPortMapping!="" && (!rgex.test(workQuota.quotaPortMapping) || workQuota.quotaPortMapping<=prj.portMappingCount)){
			  $scope.checkQuotaMappingFalg=true;		//端口映射
		  }
		  if(workQuota.quotaSms!=null && workQuota.quotaSms!="" &&(!rgex.test(workQuota.quotaSms) || workQuota.quotaSms<=prj.smsCount)){
			  $scope.checkSmsQuotaFalg=true;
		  }
		  if(workQuota.quotaMasterInstance!=null && workQuota.quotaMasterInstance!="" &&
				  (!rgex.test(workQuota.quotaMasterInstance) || workQuota.quotaMasterInstance<=prj.maxMasterInstance)){
			  $scope.checkInstanceQuotaFlag=true;
		  }
		  if(workQuota.quotaSlaveInstance!=null && workQuota.quotaSlaveInstance!="" &&
				  !slaveRegex.test(workQuota.quotaSlaveInstance)){
			  $scope.checkSlaveInstanceQuotaFlag=true;
		  }
		  if(workQuota.quotaBackupByHand!=null && workQuota.quotaBackupByHand!="" &&
				  !backupRegex.test(workQuota.quotaBackupByHand)){
			  $scope.checkBackupByHandQuotaFlag = true;
		  }
		  if(workQuota.quotaBackupByAuto!=null && workQuota.quotaBackupByAuto!="" &&
				  !backupRegex.test(workQuota.quotaBackupByAuto)){
			  $scope.checkBackupByAutoQuotaFlag=true;
		  }
		  if($scope.checkQuotaVmFalg || $scope.checkQuotaCpuFalg || $scope.checkQuotaMemoryFalg ||
			  $scope.checkQuotaDiskFalg || $scope.checkQuotaShotFalg || $scope.checkQuotaDiskSizeFalg ||
			  $scope.checkQuotaNetFalg || $scope.checkQuotaSubnetFalg || $scope.checkQuotaVPNFalg ||
			  $scope.checkQuotaGroupFalg || $scope.checkQuotaFloatIpFalg || $scope.checkQuotaBalanceFalg|| 
			  $scope.checkQuotaMappingFalg || $scope.checkQuotaShotSizeFalg || 
			  $scope.checkQuotaBandFalg || $scope.checkSmsQuotaFalg || $scope.checkInstanceQuotaFlag || !bool){
			  //所有验证都是false的时候总的也为false
			  $scope.checkQuotaFalg=true;
			  $scope.btnShow = true;//按钮可点击
		  }else{
			  $scope.checkQuotaFalg=false;
			  if($scope.checkSlaveInstanceQuotaFlag || $scope.checkBackupByHandQuotaFlag || $scope.checkBackupByAutoQuotaFlag){
				  $scope.btnShow = true;
			  }
			  else{
				  $scope.btnShow = false;//按钮不可点击
			  }
		  }
	  };
	  $scope.cancel = function (){
		  $modalInstance.dismiss();
	  };
	  $scope.commit = function () {
		  eayunModal.confirm('配额已设置正确，立即提交？').then(function () {
			  eayunHttp.post("cloud/project/findProjectByPrjId.do",prj.projectId).then(function(respose){
				  if(respose.data.isHaswork=='1'){
					  eayunModal.warning("当前存在未处理完的配额申请，请点击左侧‘工单管理’查看详情");
				  }else{
					  $scope.workQuota.prjId=prj.projectId;
					  $scope.workorder.workQuota=$scope.workQuota;
					  $modalInstance.close($scope.workorder);
				  }
			  });
	     });
	   };
  });