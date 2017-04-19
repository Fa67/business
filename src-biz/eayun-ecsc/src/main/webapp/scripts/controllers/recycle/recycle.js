'use strict';
angular.module('eayunApp.controllers')
/**
 * @ngdoc function
 * @name appRouter
 * @description
 * 配置路由
 */
  .config(function ($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.when('/app/recycle', '/app/recycle/host');
  	$stateProvider
    .state('app.recycle.host', {
		url: '/host',
		templateUrl: 'views/recycle/host/recyclehostmng.html',
		controller: 'RecycleHostCtrl'
    })
   .state('app.recycle.volume', {
    	url: '/volume',
    	templateUrl: 'views/recycle/volume/recyclevolumemng.html',
   	    controller: 'RecycleVolumeCtrl'
    }).state('app.recycle.snapshot', {
    	url: '/snapshot',
   	    templateUrl: 'views/recycle/snapshot/recyclesnapshotmng.html',
   	    controller: 'RecycleSnapshotCtrl'
   		
    });
  })
 .controller("RecycleCtrl",function($rootScope,$scope, $stateParams,eayunHttp, eayunModal,powerService){
	 /**
      * 当前位置的导航栏
      */
     $rootScope.navList = function(cucrentName){
    	 $scope.cucrentName = cucrentName;
     };
     
     powerService.powerRoutesList().then(function(powerList){
		$scope.vmListPermissions = {
				isHost : powerService.isPower('recycle_host_view'),	    //云主机列表
				isVolume : powerService.isPower('recycle_disk_view'),	//云硬盘
				isSnap : powerService.isPower('recycle_snap_view')	    //云硬盘备份
		};
	 });
  })
  .controller("RecycleHostCtrl",function($rootScope,$scope, $stateParams,eayunHttp,powerService,eayunModal,toast,$timeout){
	  
	  $scope.myTable = {
	      source: 'cloud/vm/getRecycleVmList.do',
	      api : {},
	      getParams: function () {
	        return {
	        	'name':$scope.vmName?$scope.vmName:'',
	        	'dcId':$scope.dcId?$scope.dcId:''
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
      * 云主机状态 显示
      */
     $scope.checkVmStatus =function (model){
 		if(model.vmStatus&&model.vmStatus=='SOFT_DELETED'){
 			return 'ey-square-error';
 		}  
 		else{
 			return'ey-square-warning';
 		}
     };
     
     $scope.init = function (){
    	 $rootScope.navList('云主机');
    	 var allDc = {
    			 'dcId':'-1',
    			 'dcName':'数据中心(全部)'
    	 };
    	 $scope.dcList = [allDc];
    	 eayunHttp.post('cloud/vm/getDatacenterProjectList.do').then(function (response){
			  if(response.data && response.data.length>0){
				  var i = 1;
				  angular.forEach(response.data, function (value, key) {
					  $scope.dcList[i++] = {
							  'dcId':value.dcId,
							  'dcName':value.dcName
					  };
				  });
			  }
		  });
    	 
    	 powerService.powerRoutesList().then(function(powerList){
    			$scope.vmListPermissions = {
    					isRestore : powerService.isPower('recycle_host_reset'),	    //恢复
    					isDelete : powerService.isPower('recycle_host_drop')	//删除
    			};
    		 });
     };
     
     $scope.selectDc = function (item,event){
    	 $scope.dcId = null;
    	 if(item.dcId != '-1'){
    		 $scope.dcId = item.dcId;
    	 }
    	 $scope.myTable.api.draw();
     };
     
	 $scope.$watch("myTable.result",function (newVal,oldVal){
    	if(newVal !== oldVal){
	    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
	    			for(var i=0;i<$scope.myTable.result.length;i++){
	    				var status=$scope.myTable.result[i].vmStatus.toString().toLowerCase();
	    				if("soft_resume"==status){
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
	     * 查询
	     */
	    $scope.search = function(_item,_event){
	    	$scope.vmName = _item;
	    	$scope.myTable.api.draw();
	    };
	    
	  /**
	   * 云主机详情页
	   */
	  $scope.detail = function(item){
    	 var result = eayunModal.dialog({
		    	showBtn: false,
		        title: '云主机详情',
		        width: '550px',
		        templateUrl: 'views/recycle/host/hostdetail.html',
		        controller: 'HostDetailCtrl',
		        resolve: {
		        	item:function(){
		        	  return item;
		        	}
		        }
		      });
		      result.then(function (value){
		    	  $scope.myTable.api.draw();
		      }, function () {
		    	  $scope.myTable.api.draw();
		      });
    	};
	  
	  /**
	   * 删除回收站数据
	   */
	  $scope.deleteVm = function (item){
		  var result = eayunModal.open({
			  backdrop: "static",
			  templateUrl: 'views/recycle/host/delete.html',
			  controller: 'RecycleVmDeleteCtrl',
			  resolve:{
				  item:function (){
					  return item;
				  }
			  }
		  }).result;
		  
		  result.then(function(_data){
			  $scope.myTable.api.draw();
		  },function(){
			  $scope.myTable.api.draw();
		  });
		  
	  };
	  
	  /**
	   * 恢复删除中的数据
	   */
	  $scope.restoreVm = function(event,item){
		  var cloudVm ={};
		  event.target.disabled = true;
		  cloudVm.dcId = item.dcId;
		  cloudVm.prjId = item.prjId;
		  cloudVm.vmId = item.vmId;
		  cloudVm.vmName = item.vmName;
		  
		  eayunHttp.post('cloud/vm/restoreVm.do',cloudVm).then(function(response){
			  if(null!=response.data&&response.data.respCode == '400000'){
				  toast.success('云主机恢复中',2000); 
				  $scope.myTable.api.draw();
			  }
			  else{
				  $scope.myTable.api.draw();
			  }
		  });
	  };
	  
	  $scope.init();
  })
  .controller("RecycleVmDeleteCtrl",function($scope, $stateParams,eayunHttp,$modalInstance, eayunModal,toast,$timeout,item){
	  $scope.vmName = item.vmName;
	  $scope.cancel = function(){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function(){
		  $scope.checkBtn = true;
		  var cloudVm ={};
		  cloudVm.deleteType = '0';
		  
		  cloudVm.dcId = item.dcId;
		  cloudVm.prjId = item.prjId;
		  cloudVm.vmId = item.vmId;
		  cloudVm.vmName = item.vmName;
		  cloudVm.deleteType = '2';
		  
		  eayunHttp.post('cloud/vm/deleteVm.do',cloudVm).then(function(response){
			  if(null!=response.data&&response.data.respCode == '100000'){
				  toast.success('云主机已删除',2000); 
				  $scope.cancel();
			  }
			  else{
				  $scope.checkBtn = false;
			  }
		  });
	  };
  })
  .controller("RecycleVolumeCtrl",function($rootScope,$scope, $stateParams,eayunHttp, eayunModal,toast,$timeout,powerService){
	  
	  
	 
	  
	  $scope.myTable = {
		      source: 'cloud/volume/getRecycleVolList.do',
		      api : {},
		      getParams: function () {
		        return {
		        	dcId :  $scope.dcId,
		        	name :  $scope.name ,
		        };
		      }
		  };
	  
	  
	  
	  
	  $scope.init = function (){
		  $rootScope.navList('云硬盘');
	    	 var allDc = {
	    			 'dcId':'-1',
	    			 'dcName':'数据中心(全部)'
	    	 };
	    	 $scope.dcList = [allDc];
	    	 eayunHttp.post('cloud/vm/getDatacenterProjectList.do').then(function (response){
				  if(response.data && response.data.length>0){
					  var i = 1;
					  angular.forEach(response.data, function (value, key) {
						  $scope.dcList[i++] = {
								  'dcId':value.dcId,
								  'dcName':value.dcName
						  };
					  });
				  }
			  });
	    	 
	    	 //回收站权限
	    	 powerService.powerRoutesList().then(function(powerList){
	 			$scope.volListPermissions = {
	 					isRestore : powerService.isPower('recycle_disk_reset'),	    //恢复
	 					isDelete : powerService.isPower('recycle_disk_drop')	//删除
	 			};
	 	     });
	     };
	     
	     
	     $scope.selectDc = function (item,event){
	    	 $scope.dcId = null;
	    	 if(item.dcId != '-1'){
	    		 $scope.dcId = item.dcId;
	    	 }
	    	 $scope.myTable.api.draw();
	     };
 	 
	  
	  
		  
	  	/**
	     * 查询
	     */
	    $scope.search = function(_item,_event){
	    	$scope.name = _item;
	    	$scope.myTable.api.draw();
	    };
		  
		  
	    /**
	     * 云硬盘状态 显示
	     */
	    $scope.getVolumeStatus =function (model){
	    	if(model.isDeleted=='2'&&model.volStatus!='DELETING'){
	    		return 'ey-square-error';
	    	}
			else{
				return'ey-square-warning';
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
		  
		  
		  $scope.$watch("myTable.result",function (newVal,oldVal){
		    	if(newVal !== oldVal){
		    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
		    			for(var i=0;i<$scope.myTable.result.length;i++){
		    				var status=$scope.myTable.result[i].volStatus.toString().toLowerCase();
		    				if("deleting"==status){
		    					$timeout($scope.refreshList,5000);
		    					break;
		    				}
		    				
		    			}
		    		}
		    	}
		    });
		  
		  $scope.refreshList = function (){
		    	if(!$scope.checkUser()){
		        	return ;
		        }
		    	$scope.myTable.api.refresh();
		  };
		  
		  
		  
		  
		  /**
		   * 云硬盘详情页
		   */
		  $scope.detail = function(item){
	    	 var result = eayunModal.dialog({
			    	showBtn: false,
			        title: '云硬盘详情',
			        width: '550px',
			        templateUrl: 'views/recycle/volume/volumedetail.html',
			        controller: 'VolumeDetailCtrl',
			        resolve: {
			        	item:function(){
			        	  return item;
			        	}
			        }
			      });
			      result.then(function (value){
			    	  $scope.myTable.api.draw();
			      }, function () {
			    	  $scope.myTable.api.draw();
			      });
	    	};
	    	
	    	
	    	
	    	/**
			   * 云硬盘中云主机详情页
			   */
			  $scope.detailVm = function(vmId){
		    	 var result = eayunModal.dialog({
				    	showBtn: false,
				        title: '云主机详情',
				        width: '550px',
				        templateUrl: 'views/recycle/host/hostdetail.html',
				        controller: 'HostDetailCtrl',
				        resolve: {
				          item:function(){
				        	  return eayunHttp.post('cloud/vm/getRecycleVmById.do',vmId).then(function (response){
				      			  if(response.data!=null&&response.data.data!=null){
				      				  return response.data.data;
				      			  }
				      		  });
				        	}
				          }
				      });
				      result.then(function (value){
				    	  $scope.myTable.api.draw();
				      }, function () {
				    	  $scope.myTable.api.draw();
				      });
		    	};
	    	
	    	
		  
		    	
		    	
		    	
		    	
		    	
		  /**
		   * 删除回收站数据
		   */
	  	  $scope.deleteVol = function (item){
	  		  var result = eayunModal.open({
	  			  backdrop: "static",
	  			  templateUrl: 'views/recycle/volume/delete.html',
	  			  controller: 'RecycleVolDeleteCtrl',
	  			  resolve:{
	  				  volume:function (){
	  					 var cloudVol ={};
	  					  cloudVol.dcId = item.dcId;
	  					  cloudVol.prjId = item.prjId;
	  					  cloudVol.volId = item.volId;
	  					  cloudVol.volName = item.volName;
	  					  cloudVol.isDeleted = '1';
	  					  return cloudVol;
	  				  }
	  			  }
	  		  }).result;
	  		  
	  		  result.then(function(_data){
	  			  $scope.myTable.api.draw();
	  		  },function(){
	  			  $scope.myTable.api.draw();
	  		  });
	  		  
	  	  };
		    	

		  
		  
		  /**
		   * 恢复回收站中的数据
		   */
		  $scope.recoverVol = function(item){
			  if('2'==item.payType){
				  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
					  $scope.account = response.data.data.money;
					  if($scope.account<0){
						  eayunModal.warning("您的账户已欠费，请充值后操作!");
						  return;
					  }
					  
					  var cloudVol ={};
					  cloudVol.dcId = item.dcId;
					  cloudVol.prjId = item.prjId;
					  cloudVol.volId = item.volId;
					  cloudVol.volName = item.volName;
					  
					  eayunHttp.post('cloud/volume/recoverVolume.do',cloudVol).then(function(response){
						  if(null!=response.data&&response.data.respCode == '400000'){
							  toast.success('云硬盘恢复成功',2000); 
							  $scope.myTable.api.draw();
						  }
					  });
					  
				  });
			  }else{
				  var cloudVol ={};
				  cloudVol.dcId = item.dcId;
				  cloudVol.prjId = item.prjId;
				  cloudVol.volId = item.volId;
				  cloudVol.volName = item.volName;
				  
				  eayunHttp.post('cloud/volume/recoverVolume.do',cloudVol).then(function(response){
					  if(null!=response.data&&response.data.respCode == '400000'){
						  toast.success('云硬盘恢复成功',2000); 
						  $scope.myTable.api.draw();
					  }
				  });
			  }
			
		  };
		  
		  
		  $scope.init();
	  
  }).controller("RecycleVolDeleteCtrl",function($scope, $stateParams,eayunHttp,$modalInstance, eayunModal,toast,$timeout,volume){
	  $scope.volName = volume.volName;
	  $scope.cancel = function(){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function(){
		  $scope.checkBtn = true;
		  eayunHttp.post('cloud/volume/deleteVolume.do',volume).then(function(response){
			  if(null!=response.data&&response.data==true){
				  toast.success('云硬盘已删除',2000); 
				  $scope.cancel();
			  }else{
				  $scope.checkBtn = false;
			  }
		  });
	  };
  })
  .controller("RecycleSnapshotCtrl",function($rootScope,$scope, $stateParams,eayunHttp, eayunModal,toast,$timeout,powerService){

	  $scope.myTable = {
		      source: 'cloud/snapshot/getRecycleSnapList.do',
		      api : {},
		      getParams: function () {
		        return {
		        	name :  $scope.name || '',
		        	dcId:   $scope.dcId||'',
		        };
		      }
		  };
		  
	  
	  /**
	     * 云硬盘备份状态 显示
	     */
	    $scope.getSnapshotStatus =function (model){
	    	if(model.isDeleted=='2'&&model.snapStatus!='DELETING'){
	    		return 'ey-square-error';
	    	}
			else{
				return'ey-square-warning';
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
		    
		    
		    
			  $scope.init = function (){
				  $rootScope.navList('云硬盘备份');
			    	 var allDc = {
			    			 'dcId':'-1',
			    			 'dcName':'数据中心(全部)'
			    	 };
			    	 $scope.dcList = [allDc];
			    	 eayunHttp.post('cloud/vm/getDatacenterProjectList.do').then(function (response){
						  if(response.data && response.data.length>0){
							  var i = 1;
							  angular.forEach(response.data, function (value, key) {
								  $scope.dcList[i++] = {
										  'dcId':value.dcId,
										  'dcName':value.dcName
								  };
							  });
						  }
					  });
			    	 
			    	 //回收站权限
			    	 powerService.powerRoutesList().then(function(powerList){
			 			$scope.snapListPermissions = {
			 					isRestore : powerService.isPower('recycle_snap_reset'),	    //恢复
			 					isDelete : powerService.isPower('recycle_snap_drop')	//删除
			 			};
			 	     });
			     };
			     
			     
			     $scope.selectDc = function (item,event){
			    	 $scope.dcId = null;
			    	 if(item.dcId != '-1'){
			    		 $scope.dcId = item.dcId;
			    	 }
			    	 $scope.myTable.api.draw();
			     };
		    
		    
		    
		    
		    
		    
		    
		  
		    /**
		     * 查询
		     */
		    $scope.search = function(_item,_event){
		    	$scope.name = _item;
		    	$scope.myTable.api.draw();
		    };
		  
		  $scope.$watch("myTable.result",function (newVal,oldVal){
		    	if(newVal !== oldVal){
		    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
		    			for(var i=0;i<$scope.myTable.result.length;i++){
		    				var status=$scope.myTable.result[i].snapStatus.toString().toLowerCase();
		    				if("deleting"==status){
		    					$timeout($scope.refreshList,5000);
		    					break;
		    				}
		    				
		    			}
		    		}
		    	}
		    });
		  
		  $scope.refreshList = function (){
		    	if(!$scope.checkUser()){
		        	return ;
		        }
		    	$scope.myTable.api.refresh();
		  };
		  
		  
		  
		  
		  /**
		   * 备份详情页
		   */
		  $scope.detail = function(item){
	    	 var result = eayunModal.dialog({
			    	showBtn: false,
			        title: '云硬盘备份详情',
			        width: '550px',
			        templateUrl: 'views/recycle/snapshot/snapshotdetail.html',
			        controller: 'SnapshotDetailCtrl',
			        resolve: {
			        	item:function(){
			        	  return item;
			        	}
			        }
			      });
			      result.then(function (value){
			    	  $scope.myTable.api.draw();
			      }, function () {
			    	  $scope.myTable.api.draw();
			      });
	    	};
	    	
	    	
	    	
	    	/**
			   * 备份中云硬盘详情页
			   */
			  $scope.detailVolume = function(snapshot){
		    	 var result = eayunModal.dialog({
				    	showBtn: false,
				        title: '云硬盘详情',
				        width: '550px',
				        templateUrl: 'views/recycle/snapshot/volumedetail.html',
				        controller: 'SnapVolDetailCtrl',
				        resolve: {
				          item:function(){
				        	  return eayunHttp.post('cloud/volume/getVolumeById.do',{dcId:snapshot.dcId,prjId:snapshot.prjId,volId:snapshot.volId}).then(function(response){
				        		 return response.data;
				        	   });
				        	}
				          }
				      });
				      result.then(function (value){
				    	  $scope.myTable.api.draw();
				      }, function () {
				    	  $scope.myTable.api.draw();
				      });
		    	};
		    	
		    	
		    	
		    	
		    	
		    	/**
				   * 删除回收站数据
				   */
			  	  $scope.deleteSnap = function (item){
			  		  var result = eayunModal.open({
			  			  backdrop: "static",
			  			  templateUrl: 'views/recycle/snapshot/delete.html',
			  			  controller: 'RecycleSnapDeleteCtrl',
			  			  resolve:{
			  				  snapshot:function (){
			  					 var cloudSnap ={};
			  					  cloudSnap.dcId = item.dcId;
			  					  cloudSnap.prjId = item.prjId;
			  					  cloudSnap.snapId = item.snapId;
			  					  cloudSnap.snapName = item.snapName;
			  					  cloudSnap.isDeleted = '1';
			  					  return cloudSnap;
			  				  }
			  			  }
			  		  }).result;
			  		  
			  		  result.then(function(_data){
			  			  $scope.myTable.api.draw();
			  		  },function(){
			  			  $scope.myTable.api.draw();
			  		  });
			  		  
			  	  };
	    	
	
		  
		  /**
		   * 恢复回收站中的数据
		   */
		  $scope.recoverSnap = function(item){
			  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
				  $scope.account = response.data.data.money;
				  if($scope.account<0){
					  eayunModal.warning("您的账户已欠费，请充值后操作!");
					  return;
				  }
				  
				  var cloudSnap ={};
				  cloudSnap.dcId = item.dcId;
				  cloudSnap.prjId = item.prjId;
				  cloudSnap.snapId = item.snapId;
				  cloudSnap.snapName = item.snapName;
				  
				  eayunHttp.post('cloud/snapshot/recoverSnapshot.do',cloudSnap).then(function(response){
					  if(null!=response.data&&response.data.respCode == '400000'){
						  toast.success('云硬盘备份恢复成功',2000); 
						  $scope.myTable.api.draw();
					  }
				  });
			  });
			  
			  
		  };
		  
		  $scope.init();
	  
  }).controller("RecycleSnapDeleteCtrl",function($scope, $stateParams,eayunHttp,$modalInstance, eayunModal,toast,$timeout,snapshot){
	  $scope.snapName = snapshot.snapName;
	  $scope.cancel = function(){
		  $modalInstance.close();
	  };
	  
	  $scope.ok = function(){
		  $scope.checkBtn = true;
		  eayunHttp.post('cloud/snapshot/deleteSnap.do',snapshot).then(function(response){
			  if(null!=response.data&&response.data==true){
				  toast.success('备份已删除',2000); 
				  $scope.cancel();
			  }else{
				  $scope.checkBtn = false;
			  }
		  });
		  
	  };
  })
  .controller("HostDetailCtrl",function($scope, $stateParams,eayunHttp, eayunModal,item){
	  
	  $scope.item = angular.copy(item);
	  
	  /**
	   * 云主机状态
	   */
	  $scope.getVmStatusClass = function (){
	    	$scope.vmStatusClass = '';
			if($scope.item.vmStatus&&$scope.item.vmStatus=='SOFT_DELETED'){
				$scope.vmStatusClass = 'ey-square-error';
			}  
			else{
				$scope.vmStatusClass = 'ey-square-warning';
			}
	    };
	    
		$scope.getVmStatusClass();
	  
  })
  .controller("VolumeDetailCtrl",function($scope, $stateParams,eayunHttp, eayunModal,item){
	  $scope.item = angular.copy(item);
	  
	  /**
	     * 云硬盘状态 显示
	     */
	    $scope.getVolumeStatus =function (model){
	    	$scope.volStatusClass = '';
	    	if(model.isDeleted=='2'&&model.volStatus!='DELETING'){
	    		$scope.volStatusClass= 'ey-square-error';
	    	}
			else{
				$scope.volStatusClass='ey-square-warning';
			}
	    };
	    
	    $scope.getVolumeStatus($scope.item);
	  
  })
  .controller("SnapshotDetailCtrl",function($scope, $stateParams,eayunHttp, eayunModal,item){
	  $scope.item = angular.copy(item);
	  
	  /**
	     * 云硬盘备份状态 显示
	     */
	    $scope.getSnapshotStatus =function (model){
	    	$scope.snapStatusClass='';
	    	if(model.isDeleted=='2'&&model.snapStatus!='DELETING'){
	    		$scope.snapStatusClass='ey-square-error';
	    	}
			else{
				$scope.snapStatusClass='ey-square-warning';
			}
	    };
	  
	    $scope.getSnapshotStatus($scope.item);
  }).controller("SnapVolDetailCtrl",function($scope, $stateParams,eayunHttp, eayunModal,item){
	  $scope.item = angular.copy(item);

	    /**
		   * 云硬盘状态 显示
		   */
	     $scope.getVolumeStatus =function (model){
	    	$scope.volStatusClass = '';
	    	if(model.isDeleted == '2'){
	    		if(model.volStatus != 'DELETING'){
	    			$scope.volStatusClass = 'ey-square-error';
	    		}else {
	    			$scope.volStatusClass = 'ey-square-warning';
	    		}
	    	} else if('1'==model.chargeState||'2'==model.chargeState||'3'==model.chargeState){
	    		$scope.volStatusClass ='ey-square-disable';
	    	}else if(model.volStatus&&model.volStatus=='AVAILABLE'){
	    		$scope.volStatusClass ='ey-square-space';
	    	}
	    	else if(model.volStatus=='IN-USE'){
	    		$scope.volStatusClass ='ey-square-right';
			}  
			else if(model.volStatus=='ERROR'){
				$scope.volStatusClass ='ey-square-error';
			}
			else{
				$scope.volStatusClass ='ey-square-warning';
			}
	     };
	     
	     $scope.getVolumeStatus($scope.item);
  })
;

