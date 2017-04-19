'use strict';
angular.module('eayunApp.controllers').config( function($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.when('/app/cloud/cloudhost/volume', '/app/cloud/cloudhost/volume/volumeList');
	$stateProvider.state('app.cloud.cloudhost.volume', {
	      url: '/volume',
	      templateUrl: 'views/cloudhost/volume/main.html'
	})
	.state('app.cloud.cloudhost.volume.list', {
	      url: '/volumeList',
	      templateUrl: 'views/cloudhost/volume/volumemng.html',
	      controller: 'cloudHostVolumeList'
	})
	.state('buy.volume',{
    	url: '/volume/:payType/:orderNo',
        templateUrl: 'views/cloudhost/volume/buyvolume.html',
        controller: 'BuyVolumeController'
    })
    .state('buy.confirmvol',{
    	url: '/confirmvol/:orderType',
        templateUrl: 'views/cloudhost/volume/orderconfirm.html',
        controller: 'ConfirmVolOrderController'
    })
    .state('buy.snapshot',{
    	url: '/snapshot/:payType/:orderNo/:volId/:fromVolId/:fromVmId',
        templateUrl: 'views/cloudhost/volume/buysnapshot.html',
        controller: 'BuySnapshotController'
    })
    .state('buy.confirmsnap',{
    	url: '/confirmsnap/:prjId',
        templateUrl: 'views/cloudhost/snapshot/snaporderconfirm.html',
        controller: 'ConfirmSnapOrderController'
    })
    .state('app.cloud.cloudhost.volumedetail', {
	      url: '/volumeDetail/:dcId/:prjId/:volId',
	      templateUrl: 'views/cloudhost/volume/volumedetail.html',
	      controller: 'cloudHostVolumeDetail'
	})
	.state('renew.renewvolume',{
    	url: '/renewvolume',
        templateUrl: 'views/cloudhost/volume/volumerenewconform.html',
        controller: 'RenewVolumeConfirmController'
        
    });
		}).controller('cloudHostVolumeList',function($rootScope,$scope,$state,$stateParams,$timeout,eayunHttp,eayunModal,toast,powerService,eayunStorage,VolumeService){
			
			var list=[];
			$rootScope.navList(list,'云硬盘');
			
			
			 /**
		     * 云硬盘状态 显示
		     */
		    $scope.getVolumeStatus =function (model){
		    	if('DELETING'!=model.volStatus&&('1'==model.chargeState||'2'==model.chargeState||'3'==model.chargeState)){
		    		return 'ey-square-disable';
		    	}else if(model.volStatus&&model.volStatus=='AVAILABLE'){
		    		return 'ey-square-space';
		    	}
		    	else if(model.volStatus=='IN-USE'){
					return 'ey-square-right';
				}  
				else if(model.volStatus=='ERROR'){
					return 'ey-square-error';
				}
				else{
					return'ey-square-warning';
				}
		    };
			
			 
			 //查询列表
			$scope.myTable = {
				      source: 'cloud/volume/getVolumeList.do',
				      api : {},
				      getParams: function () {
				        return {
				        	prjId : sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '',
						    dcId : sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
						    queryType : $scope.search ? $scope.search.key :'',
						    name :  $scope.search ? $scope.search.value:'',
				        	status : $scope.query ? $scope.query.status :'',
				        	isDeleted:'0',
				        };
				      }
				    };
			
			 //监视器[监视数据中心、项目id变化]
			  $scope.$watch('model.projectvoe' , function(newVal,oldVal){
			    	if(newVal !== oldVal){
			    		$scope.myTable.api.draw(); 
			    	}
			    });
			  
			  //权限控制
			  powerService.powerRoutesList().then(function(powerList){
				  $scope.buttonPower = {
					isCreate : powerService.isPower('disk_add'),	//创建云硬盘
					isEdit : powerService.isPower('disk_edit'),	//编辑云硬盘
					isDebind : powerService.isPower('disk_unbind'),//解绑功能
					isBind : powerService.isPower('disk_bind'),//挂载功能
					addSnap : powerService.isPower('disk_addSnap'),//创建备份
					isTag : powerService.isPower('disk_tag'),//标签
					delDisk:powerService.isPower('disk_delete'),//删除
					isSetUp:powerService.isPower('disk_setup'),//扩容
					isRenew:powerService.isPower('disk_renew')//续费
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
			  
			  $scope.$watch("myTable.result",function (newVal,oldVal){
			    	if(newVal !== oldVal){
			    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
			    			for(var i=0;i<$scope.myTable.result.length;i++){
			    				var status=$scope.myTable.result[i].volStatus.toString().toLowerCase();
			    				if("deleting"==status||"creating"==status||"downloading"==status||"detaching"==status||"attaching"==status||"restoring-backup"==status||"backing-up"==status){
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
			  
			  
			    
			    
			    $scope.options = {
			            searchFn: function () {
			            	if(!$scope.checkUser()){
			                	return ;
			                }
			            	$scope.myTable.api.draw();
			            },
			            placeholder:"请输入查询内容",
			            select: [{name: '名称'}, {tag: '标签'}]
			       };
			    

			  
			  //名称查询
			  $scope.queryVolume = function(){
				  $scope.myTable.api.draw();
			  };
			  
			
			   //购买云硬盘 
			 $scope.buyVolume = function (){
			   $state.go('buy.volume',{payType:'1'});
			 };
			 
			 //购买备份
			 $scope.buySnapshot = function (cloudVolume){
			   $state.go('buy.snapshot',{'payType':'2','volId':cloudVolume.volId})
			 };
			  
			  //点击vm跳转至vm详情
			  $scope.goToVm=function(vmId){
				  if(null==vmId||'null'==vmId||''==vmId){
					  return;
				  }else{
					  $state.go('app.cloud.cloudhost.hostdetail',{"detailType":'host',"vmId":vmId}); // 跳转后的URL;
				  }
				  
			  };
			  
			
		    //扩容云硬盘
			$scope.extendVolume = function (cloudVolume) {
				if(null!=cloudVolume){
					eayunHttp.post('cloud/volume/getVolumeTypesByTypeId.do',{"dcId":cloudVolume.dcId,"typeId":cloudVolume.volTypeId}).then(function(response){
					   if(response && response.data){
						   if(null!=response.data&&null!=response.data.typeId){
							   cloudVolume.maxSize=response.data.maxSize;
						   }
					   }
					});
				}
				if(cloudVolume.maxSize<=cloudVolume.volSize){
					eayunModal.warning("该云硬盘容量已达最大值，无法再扩容");
		    		 return;
				}else if('IN-USE'==cloudVolume.volStatus&&null!=cloudVolume.vmId&&'null'!=cloudVolume.vmId){
					eayunModal.warning("云硬盘已挂载，请解绑后操作");
		    		 return;
				}else if('ERROR'==cloudVolume.volStatus){
					eayunModal.warning("云硬盘故障，无法扩容");
		    		 return;
				}
				
				if(cloudVolume.payType == '1'){
					  eayunHttp.post('cloud/volume/queryVolChargeById.do',cloudVolume.volId).then(function(response){
						  if(response && response.data && response.data.data){
							  cloudVolume.cycleCount = response.data.data.cycleCount;
						  }
					  });
				  }
				
			    var result = eayunModal.open({
			    	backdrop:'static',
			        templateUrl: 'views/cloudhost/volume/extendvolume.html',
			        controller: 'ExtendVolume',
			        resolve: {
				          items: function () {
				            return cloudVolume;
				          }
				    }
			    }).result;
			    result.then(function (){
			    	  
			    	  $scope.myTable.api.draw();
			    	  
			    }, function () {
			    	  $scope.myTable.api.draw();
			    });
		    };
			  
   
				    
		    //删除云硬盘
			$scope.deleteVolume = function (cloudVolume) {
				
					if('CREATING'==cloudVolume.volStatus){
				    	eayunModal.warning("云硬盘创建中，请稍候");
			    		return;
				    }
				    if('DELETING'==cloudVolume.volStatus){
				    	eayunModal.warning("云硬盘删除中，请稍候");
			    		return;
				    }
				    if('ATTACHING'==cloudVolume.volStatus){
				    	eayunModal.warning("云硬盘挂载中，请稍候");
			    		return;
				    }
				    if('DETACHING'==cloudVolume.volStatus){
				    	eayunModal.warning("云硬盘解绑中，请稍候");
			    		return;
				    }
				    
			    	if('IN-USE'==cloudVolume.volStatus&&null!=cloudVolume.vmId&&''!=cloudVolume.vmId&&'null'!=cloudVolume.vmId){
			    		eayunModal.warning("云硬盘已挂载，请解绑后操作");
			    		return;
			    	}
			    	if('RESTORING-BACKUP'==cloudVolume.volStatus){
						eayunModal.warning("云硬盘恢复数据中，请稍后再试");
			    		 return;
					}
			    	if('BACKING-UP'==cloudVolume.volStatus){
						eayunModal.warning("云硬盘备份创建中，请稍后再试");
			    		 return;
					}
			    	
					var result = eayunModal.open({
						backdrop: "static",
				        templateUrl: 'views/cloudhost/volume/deletevolume.html',
				        controller: 'DeleteVolume',
				        resolve: {
				      	  volume:function(){
				      		  return cloudVolume;
				      	  }
				        }
					}).result;
					result.then(function (value){
			    	  eayunHttp.post('cloud/volume/deleteVolume.do',value).then(function(response){
			    		  if(response.data!=null&&response.data==true){
				        		toast.success('删除云硬盘成功',1000);
				        	}
							$scope.myTable.api.draw();
		              });

			       }, function () {
			    	   $scope.myTable.api.draw();
			       });
			    };
			    
			    
			    /*标签*/
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
						}).result;
						result.then(function () {
							
					},function () {
						
					});
				};
			    
				    

				    //云硬盘详情页
				    $scope.findVolumeById=function(volume){
						$state.go('app.cloud.cloudhost.volumedetail',{"detailType":'volume',"dcId":volume.dcId,"prjId":volume.prjId,"volId":volume.volId}); // 跳转后的URL;
					};
					
					
					 //挂载云硬盘
				    $scope.bindVolume = function (item) {
					      var result = eayunModal.open({
					    	backdrop:'static',
					        templateUrl: 'views/cloudhost/volume/bindvolume.html',
					        controller: 'BindVolume',
					        resolve: {
					      	  vms:function(){
					      		 return eayunHttp.post('cloud/vm/getcanbindcloudvmList.do',item).then(function(response){
					      			  	return response.data;
					      		   });
					      	  },
					      	  
					      	  volume:function(){
					      		  return item;
					      	  }
					          
					        }
					      }).result;
					      result.then(function (value){
					    	  eayunHttp.post('cloud/volume/bindVolume.do',value).then(function(response){
					    		  if(response.data!=null&&response.data==true){
					    			  toast.success('云硬盘挂载至'+(value.vmName.length>8?value.vmName.substring(0,7)+'...':value.vmName)+'成功',1000); 
					    		  }
					    		  $scope.myTable.api.draw();
				              });
		
					      }, function () {
					    	  $scope.myTable.api.draw();
					      });
					    };
					    
					    
					    
					    
					    
			    //解绑云硬盘
			    $scope.debindVolume = function (cloudvolume) {
		    	 var result = eayunModal.open({
				    	backdrop:'static',
				        templateUrl: 'views/cloudhost/volume/debindvolume.html',
				        controller: 'DebindVolume',
				        resolve: {
				      	  volume:function(){
				      		  return cloudvolume;
				      	  }
				          
				        }
				      }).result;
				      result.then(function (value){
				    	  eayunHttp.post("cloud/volume/debindVolume.do",value).then(function(response){
								if(response.data!=null&&response.data==true){
									toast.success('云硬盘解绑成功',1000); 
								}
								$scope.myTable.api.draw();
							});
	
				      }, function () {
				    	  $scope.myTable.api.draw();
				      });

			    };
			    
					    
					   
					   
						    
						   
		    /**
	     * 云硬盘--续费
	     */
	    $scope.renewVolume = function(item){
	    	var result = eayunModal.open({
	    		backdrop:'static',
		        templateUrl: 'views/cloudhost/volume/volumerenew.html',
		        controller: 'cloudVolumeRenewCtrl',
		        resolve: {
		            item:function (){
	            		return item;
	            	}
		        }
		      });
	    	result.result.then(function (value) {
	    		VolumeService.checkIfOrderExist(item.volId).then(function(response){
	                eayunModal.info("资源正在调整中或您有未完成的订单，请稍后再试。");
	            },function(){
	                $state.go('renew.renewvolume');
	            });
		      }, function () {
	
		      });
	    };	
	    
	    //$scope.init();  
}).controller('cloudHostVolumeDetail', function ($rootScope,$scope, $timeout,eayunHttp ,eayunModal,$stateParams,$state,toast,powerService,eayunStorage,DatacenterService) {
	
	var list=[{route:'app.cloud.cloudhost.volume',name:'云硬盘'}];
	$rootScope.navList(list,'云硬盘详情','detail');

	$scope.item = {};
	$scope.checkVolName=true;
	$scope.checkEditBtn = true;
	$scope.volNameEditable = false;
	$scope.volDescEditable = false;
	$scope.isShow=false;
	
	 //权限控制
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.buttonPower = {
			isCreate : powerService.isPower('disk_add'),	//创建云硬盘
			isEdit : powerService.isPower('disk_edit'),	//编辑云硬盘
			isDebind : powerService.isPower('disk_unbind'),//解绑功能
			isBind : powerService.isPower('disk_bind'),//挂载功能
			addSnap : powerService.isPower('disk_addSnap'),//创建备份
			isTag : powerService.isPower('disk_tag'),//标签
			delDisk:powerService.isPower('disk_delete'),//删除
			isSnapView : powerService.isPower('snap_view'),//云硬盘备份
			isAddDisk : powerService.isPower('snap_adddisk'),//基于备份创建云硬盘
			rollBackVol: powerService.isPower('snap_rollback')//回滚云硬盘
			};
		  $scope.modulePower ={
				  isSnapView : powerService.isPower('snap_view'),//云硬盘备份
		  };
	  }); 
	  
	  
	   
      
	  
	  /**
	   * 云硬盘状态 显示
	   */
     $scope.getVolumeStatus =function (model){
    	 $scope.volStatusClass = '';
    	if(model.volStatus!='DELETING'&&('1'==model.chargeState||'2'==model.chargeState||'3'==model.chargeState)){
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
	  
	  
	
	
     /**
	  * 云硬盘备份状态 显示
	  */
    $scope.getSnapStatus =function (model){
    	
    	if('1'==model.chargeState){
    		return 'ey-square-disable';
    	}else if(model.snapStatus&&model.snapStatus=='AVAILABLE'){
    		return 'ey-square-right';
    	}
		else if(model.snapStatus=='ERROR'){
			return 'ey-square-error';
		}
		else{
			return'ey-square-warning';
		}
    };
	    
     
	
	
	var item = {};
	eayunHttp.post('cloud/volume/getVolumeById.do',{dcId:$stateParams.dcId,prjId:$stateParams.prjId,volId:$stateParams.volId}).then(function(response){
		  $scope.model=response.data;
		  $scope.isShow=true;
		  $scope.model.volDescription=$scope.model.volDescription=='null'?"":$scope.model.volDescription;
		  $scope.volNameForSnap=$scope.model.volName;
		  $scope.getVolumeStatus($scope.model);
		 
		  item = {
				  name:$scope.model.volName,
				  desc:$scope.model.volDescription
		  };
		  
		  if('1'==$scope.model.isDeleted||'2'==$scope.model.isDeleted){
			  $state.go('app.cloud.cloudhost.volume');
		  }
		  
    });
	
	eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'volume',resId: $stateParams.volId}).then(function(response){
			$scope.tag=response.data;
	});
	
	
	 /**
     * 刷新详情界面的状态
     */
    $scope.$watch("model",function (newVal,oldVal){
    	if(newVal != oldVal){
    		 var status = $scope.model.volStatus.toString().toLowerCase();
    		 if("deleting"==status||"creating"==status||"downloading"==status||"detaching"==status||"attaching"==status||"restoring-backup"==status||"backing-up"==status){
				$timeout($scope.refreshVolDetail,5000);
		  }
    	}
    }); 
	
	
	//刷新状态
	$scope.refreshVolDetail = function (){
		  if(!$scope.checkUser()){
	          	return ;
		  }
		  eayunHttp.post('cloud/volume/getVolumeById.do',{dcId:$stateParams.dcId,prjId:$stateParams.prjId,volId:$stateParams.volId}).then(function (response){
			  
			     $scope.model=response.data;
			     $scope.isShow=true;
				 $scope.model.volDescription=$scope.model.volDescription=='null'?"":$scope.model.volDescription;
				 $scope.volNameForSnap=$scope.model.volName;
				 $scope.getVolumeStatus($scope.model);
				 
				  item = {
						  name:$scope.model.volName,
						  desc:$scope.model.volDescription
				  };
				  
				  if('1'==$scope.model.isDeleted||'2'==$scope.model.isDeleted){
					  $state.go('app.cloud.cloudhost.volume');
				  }
		  });
		  
		  eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'volume',resId: $stateParams.volId}).then(function(response){
				$scope.tag=response.data;
		  });
	  };
	

	/**
	   * 保存编辑的云硬盘名称、描述,并刷新界面
	   */
	  $scope.saveEdit = function (type){
		  eayunHttp.post('cloud/volume/updateVolume.do',$scope.model).then(function (response){
			  if(response.data){
				  if(type == 'volName'){
					  $scope.volNameForSnap=$scope.model.volName;
					  $scope.hintNameShow = false;
					  $scope.volNameEditable = false;
					  item.name = $scope.model.volName;
					  toast.success('云硬盘'+DatacenterService.toastEllipsis($scope.model.volName, 8)+'修改成功',1000);  
				  }
				  if(type == 'volDesc'){
					  $scope.hintDescShow = false;
					  $scope.volDescEditable = false;
					  item.desc = $scope.model.volDescription;
					  toast.success('云硬盘'+DatacenterService.toastEllipsis($scope.model.volName, 8)+'修改成功',1000);  
				  }
				  
			  }
		  },function (){
			  
		  });
		  
	  };
	  
	  /**
	   * 将云硬盘名称、描述变为可编辑状态
	   */
	  $scope.edit = function (type){
		  if(type == 'volName'){
			  $scope.hintNameShow = true;
			  $scope.volNameEditable = true;
			  $scope.hintDescShow = false;
			  $scope.volDescEditable = false;
			  $scope.model.volDescription = item.desc;
		  }
		  if(type == 'volDesc'){
			  $scope.hintNameShow = false;
			  $scope.volNameEditable = false;
			  $scope.hintDescShow = true;
			  $scope.volDescEditable = true;
			  $scope.model.volName = item.name;
		  }
	  };
	  
	  /**
	   * 取消云主机名称、描述的可编辑状态
	   */
	  $scope.cancleEdit = function (type){
		  if(type == 'volName'){
			  $scope.hintNameShow = false;
			  $scope.volNameEditable = false;
			  $scope.model.volName = item.name;
		  }
		  if(type == 'volDesc'){
			  $scope.hintDescShow = false;
			  $scope.volDescEditable = false;
			  $scope.model.volDescription = item.desc;
		  }
	  };
	  
	  
	  
	  /**
	   * 校验云硬盘修改重名
	   */
	  $scope.checkVolumeName = function () {
		  $scope.checkEditBtn = false;
		  if($scope.model && $scope.model.volName){
			 $scope.model.volNumber=1;
			 eayunHttp.post('cloud/volume/getVolumeByName.do',$scope.model).then(function(response){
				 $scope.checkVolName=response.data;
				 $scope.checkEditBtn = true;
		      }); 
		  }
	  };
	
	
    
    /*标签*/
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
			}).result;
			result.then(function () {
				eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'volume',resId: $stateParams.volId}).then(function(response){
					$scope.tag=response.data;
				});
		},function () {
			eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'volume',resId: $stateParams.volId}).then(function(response){
				$scope.tag=response.data;
			});
		});
	};
	
	
	
	 //查询列表
	$scope.myTable = {
		      source: 'cloud/snapshot/getSnapListByVolId.do',
		      api : {},
		      getParams: function () {
		        return {
		        	prjId : $stateParams.prjId || '',
		        	dcId :  $stateParams.dcId || '',
		        	volId : $stateParams.volId || ''
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
  
  $scope.$watch("myTable.result",function (newVal,oldVal){
    	if(newVal !== oldVal){
    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
    			$scope.model.snapNum=$scope.myTable.result.length;
    			for(var i=0;i<$scope.myTable.result.length;i++){
    				var status=$scope.myTable.result[i].snapStatus.toString().toLowerCase();
    				if("deleting"==status||"creating"==status||"restoring"==status){
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
    	$scope.myTable.api.draw();
    };
  
    
    
    
     //购买备份
	 $scope.addSnapshot = function (cloudVolume){
	   $state.go('buy.snapshot',{'payType':'2','volId':cloudVolume.volId,'fromVolId':cloudVolume.volId})
	 };
	 
	 
	 //挂载云硬盘
    $scope.bindVolume = function (item) {
	      var result = eayunModal.open({
	    	backdrop:'static', 
	        templateUrl: 'views/cloudhost/volume/bindvolume.html',
	        controller: 'BindVolume',
	        resolve: {
	      	  vms:function(){
	      		 return eayunHttp.post('cloud/vm/getcanbindcloudvmList.do',item).then(function(response){
	      			  	return response.data;
	      		   });
	      	  },
	      	  
	      	  volume:function(){
	      		  return item;
	      	  }
	          
	        }
	      }).result;
	      result.then(function (value){
	    	  eayunHttp.post('cloud/volume/bindVolume.do',value).then(function(response){
	    		  if(response.data!=null&&response.data==true){
	    			  toast.success('云硬盘挂载至'+(value.vmName.length>8?value.vmName.substring(0,7)+'...':value.vmName)+'成功',1000); 
	    		  }
	    		  $scope.refreshVolDetail();
              });

	      }, function () {
	    	  $scope.refreshVolDetail();
	      });
	    };
	 
	    
	    
	  //解绑云硬盘
	    $scope.debindVolume = function (cloudvolume) {
	    	 var result = eayunModal.open({
			    	backdrop:'static',
			        templateUrl: 'views/cloudhost/volume/debindvolume.html',
			        controller: 'DebindVolume',
			        resolve: {
			      	  volume:function(){
			      		  return cloudvolume;
			      	  }
			          
			        }
			      }).result;
			      result.then(function (value){
			    	  eayunHttp.post("cloud/volume/debindVolume.do",value).then(function(response){
							if(response.data!=null&&response.data==true){
								toast.success('云硬盘解绑成功',1000); 
							}
							$scope.refreshVolDetail();
						});

			      }, function () {
			    	  $scope.refreshVolDetail();
			      });

	    };
	 
	    
	  //删除云硬盘
		$scope.deleteVolume = function (cloudVolume) {
			
			    if('CREATING'==cloudVolume.volStatus){
		    	   eayunModal.warning("云硬盘创建中，请稍候");
	    		   return;
		        }
			    if('DELETING'==cloudVolume.volStatus){
		    	   eayunModal.warning("云硬盘删除中，请稍候");
	    		   return;
		        }
			    if('ATTACHING'==cloudVolume.volStatus){
			    	eayunModal.warning("云硬盘挂载中，请稍候");
		    		return;
			    }
			    if('DETACHING'==cloudVolume.volStatus){
			    	eayunModal.warning("云硬盘解绑中，请稍候");
		    		return;
			    }
		    	if('IN-USE'==cloudVolume.volStatus&&null!=cloudVolume.vmId&&''!=cloudVolume.vmId&&'null'!=cloudVolume.vmId){
		    		eayunModal.warning("云硬盘已挂载，请解绑后操作");
		    		return;
		    	}
		    	if('RESTORING-BACKUP'==cloudVolume.volStatus){
					eayunModal.warning("云硬盘恢复数据中，请稍后再试");
		    		 return;
				}
		    	if('BACKING-UP'==cloudVolume.volStatus){
					eayunModal.warning("云硬盘备份创建中，请稍后再试");
		    		 return;
				}
		    	
				var result = eayunModal.open({
					backdrop: "static",
			        templateUrl: 'views/cloudhost/volume/deletevolume.html',
			        controller: 'DeleteVolume',
			        resolve: {
			      	  volume:function(){
			      		  return cloudVolume;
			      	  }
			        }
				}).result;
				result.then(function (value){
		    	  eayunHttp.post('cloud/volume/deleteVolume.do',value).then(function(response){
		    		  if(response.data!=null&&response.data==true){
			        		toast.success('删除云硬盘成功',1000);
			        		$state.go('app.cloud.cloudhost.volume');
			        	}
	              });

		       }, function () {
		    	   
		       });
		    };
	    
		    
	    
	    
	    //根据云硬盘备份创建云硬盘
		$scope.AddVolumeBySnap = function (item) {
			$state.go('tobuy.volumebysnap',{'payType':'1','snapId':item.snapId,'fromVolId':$scope.model.volId});
		};
		
		
		
		 //回滚云硬盘
	    $scope.rollBack = function (cloudSnapshot) {
	    	$scope.notice='';
	    	 eayunHttp.post('cloud/volume/getVolumeById.do',{dcId:cloudSnapshot.dcId,prjId:cloudSnapshot.prjId,volId:cloudSnapshot.volId}).then(function(response){
	  		  var volume=response.data;
	  		  if(volume.isDeleted=='1'||volume.isDeleted=='2'){
	  			$scope.notice="源硬盘已删除，无法回滚";
	  		  }else if(volume.chargeState=='1'){
	  			$scope.notice="云硬盘"+(volume.volName.length>20?(volume.volName.substring(0,19)+"..."):volume.volName)+"已欠费，请充值后操作";
	  			
	  		  }else if(volume.chargeState=='2'||volume.chargeState=='3'){
	  			$scope.notice="云硬盘"+(volume.volName.length>20?(volume.volName.substring(0,19)+"..."):volume.volName)+"已到期，请续费后操作";
	  			
	  		  }else if(volume.volSize!=cloudSnapshot.snapSize){
	  			$scope.notice="云硬盘已扩容，无法回滚";
	  			
	  		  }else if(null!=volume.vmId&&''!=volume.vmId&&'null'!=volume.vmId&&volume.volStatus=='IN-USE'){
	  			$scope.notice="云硬盘已挂载，请解绑后操作!";
	  			
	  		  }else if(volume.volStatus!='AVAILABLE'&&volume.volStatus!='IN-USE'){
	  			  if(volume.volStatus=='DELETING'){
	  				$scope.notice="源硬盘"+volume.statusForDis+"，无法回滚";
	  			  }else if(volume.volStatus=='ERROR'){
	  				$scope.notice="源硬盘"+volume.statusForDis+"，无法回滚";
	  			  }else if(volume.volStatus=='BACKING-UP'){
	  				$scope.notice="云硬盘备份创建中，请稍候";
	  			  }else{
	  				$scope.notice="源硬盘"+volume.statusForDis+"，请稍后再试";
	  			  }
	  			
	  		  }
	  		  
	  		if(''!=$scope.notice){
	    		 eayunModal.warning($scope.notice);
	    		 return;
	    	 }
	  		 var result = eayunModal.open({
	  			backdrop: "static",
	 	        templateUrl: 'views/cloudhost/snapshot/backvolume.html',
	 	        controller: 'RollBackVolume',
	 	        resolve: {
	 	      	  snapshot:function(){
	 	      		cloudSnapshot.volName=volume.volName;
	 	      		  return cloudSnapshot;
	 	      	  }
	 	          
	 	        }
	 	      }).result;
	 	      result.then(function (value){
	 	    	 eayunHttp.post('cloud/snapshot/rollbackvolume.do',value).then(function(response){
	 	    		  if(null!=response.data&&'null'!=response.data&&response.data.respCode=="000000"){
	 	    			  toast.success('云硬盘备份回滚中',2000); 
	 	    		  }
	 	    		  $scope.refreshVolDetail();
	 	    		  $scope.myTable.api.draw();
	               });

	 	      }, function () {
	 	    	 $scope.refreshVolDetail();
	    		 $scope.myTable.api.draw();
	 	      });
	  		  
	      });
	    	 
	    };
		
		
		
	
	//pop框的方法
	$scope.tableNameShow = [];
    $scope.openPopBox = function(obj){
    	if(obj.type == 'volName'){
    		$scope.nameShow = true;
    	}
    	if(obj.type == 'volDesc'){
			$scope.descShow = true;
    	}
    	if(obj.type == 'tagName'){
    		$scope.tagShow = true;
    	}
    	$scope.description = obj.value;
    };
    $scope.closePopBox = function(type){
    	if(type == 'volName'){
    		$scope.nameShow = false;
    	}
    	if(type == 'volDesc'){
    		$scope.descShow = false;
    	}
    	if(type == 'tagName'){
    		$scope.tagShow = false;
    	}
    };
    $scope.openTableBox = function(obj){
    	if(obj.type == 'volName'){
    		$scope.tableNameShow[obj.index] = true;
    	}
    	$scope.ellipsis = obj.value;
    };
    $scope.closeTableBox = function(obj){
    	if(obj.type == 'volName'){
    		$scope.tableNameShow[obj.index] = false;
    	}
    };

}).controller('UpdateVolume', function ($scope,items,eayunHttp,eayunModal) {
	$scope.model= angular.copy(items,{});
	$scope.model.volDescription=('null'!=items.volDescription&&null!=items.volDescription)?items.volDescription:'';
	
	//校验名称格式和唯一性
	  $scope.checkVolumeName = function (value) {
		  var nameTest=/^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,61}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  $scope.flag=false;
		  if(value.match(nameTest)){
			$scope.model.volName=value;
			  return eayunHttp.post('cloud/volume/getVolumeByName.do',$scope.model).then(function(response){
				  if(true==response.data){
					  return  false;
				  }else{
					  return true;
				  }
				  
		      });
		  
		  }else{
			  $scope.flag=true;  
		  }    
		};
		
		
		$scope.commit = function () {
		      $scope.ok($scope.model);
		  };
	

	
}).controller('BindVolume', function ($scope,vms,volume, eayunHttp,eayunModal,$modalInstance,toast) {
	$scope.model=volume;
	$scope.model.vmId=null;
	$scope.vm=null;
	$scope.vms=null;
	
	$scope.vms = new Array();	
	if(vms!=null&&vms.length>0){
	  for(var i=0;i<vms.length;i++){
	 		var status=vms[i].vmStatus.toString().toLowerCase();
	 		//只有如下状态的虚拟机能够挂载云硬盘
	 		if(status=="active"||status=="paused"||status=="shutoff"||status=="verify_resize"||status=="soft_deleted"){
	 			$scope.vms.push(vms[i]);
	 		}
	 }
	}
	
	 $scope.cancel = function (){
		 $modalInstance.dismiss();
	  };
	  
	  $scope.ok = function (){
		  $modalInstance.close();
	  };
  
	$scope.commit = function () {
		eayunHttp.post('cloud/volume/queryVolumeCountByVmId.do',$scope.vm.vmId).then(function(response) {
			  $scope.bindeddisks = response.data;
			  if($scope.bindeddisks>=5){
					eayunModal.error('该云主机已挂载5块数据盘');
		    		return ;
			  }else{
					$scope.model.vmId=$scope.vm.vmId;
					$scope.model.vmName=$scope.vm.vmName;
					$modalInstance.close($scope.model); 
			  }
		  });
		
	  };

}).controller('ExtendVolume', function ($scope,items,eayunHttp,eayunModal,$state,$stateParams,eayunStorage,$modalInstance) {
		$scope.model={};
		$scope.model.volId=items.volId;
		$scope.model.volName=items.volName;
		$scope.model.volDescription=items.volDescription;
		$scope.model.dcId=items.dcId;
		$scope.model.dcName=items.dcName;
		$scope.model.prjId=items.prjId;
		$scope.model.diskFrom=items.diskFrom;
		$scope.model.orderType='2';
		$scope.model.volType=items.volType;
		$scope.model.volTypeId=items.volTypeId;
		$scope.model.volumeTypeAs=items.volumeTypeAs;
		$scope.model.payType=items.payType;
		$scope.model.volOldSize=items.volSize;
		$scope.model.endTime=items.endTime;
		$scope.model.volNumber=1;
		$scope.model.prodName='云硬盘-扩容';
		
		$scope.maxSize=items.maxSize;
		$scope.stepCount=items.maxSize/100;
		$scope.model.volSize=items.volSize;
		
		if('1'==$scope.model.payType){
			$scope.priceDetails=0;
		}else{
			$scope.priceDetails={};
			$scope.priceDetails.totalPrice=0;
		}
		
		
		
	   $scope.checkVolSize = function (value) {
		 $scope.volCapacityQuotaMsg = '';//云硬盘容量超配
		 $scope.queryPrjQuota();
		 $scope.calcBillingFactor();
	  };
			
	  
	  
	  //拖动条
	  $scope.formate=function(step){
		  return Number((step*100).toFixed());
	  };
	  
	  $scope.parse=function(value){
		  //value=Math.ceil(value/10)*10;
		  return Number((value/100+0.044444).toFixed(1));
	  };
			
			
			 /**
			   * 获取项目配额
			   */
			  $scope.queryPrjQuota = function(){
				  eayunHttp.post('cloud/vm/getProjectjQuotaById.do',$scope.model.prjId).then(function(response){
					  $scope.project = response.data.data;
					  $scope.checkVolQuota();
				  });
			  };
			  
			  /**
			   * 校验云硬盘的配额
			   */
			  $scope.checkVolQuota = function(){
				  var disk = $scope.model.volSize-$scope.model.volOldSize;
				  var prj = $scope.project;
				  $scope.volCapacityQuotaMsg = '';//云硬盘容量超配
				  if(prj){
					  if(disk&&(disk>(prj.diskCapacity - prj.usedDiskCapacity))){
						  $scope.volCapacityQuotaMsg =$scope.volCapacityQuotaMsg+'云硬盘容量';
					  }
				  }
				  
				  if('' != $scope.volCapacityQuotaMsg){
					  $scope.volCapacityQuotaMsg = $scope.volCapacityQuotaMsg.substr(0,$scope.volCapacityQuotaMsg.length)+'配额不足';
				  }
			  };
			  
			  
	
			 /**
			   * 资源计费查询
			   */
			  $scope.calcBillingFactor = function(){
				  $scope.priceError=null;
				  $scope.priceDetails = null;
				  var url = 'billing/factor/getPriceDetails.do';
				  var param = {};
				  param.dcId = $scope.model.dcId;
				  param.cycleCount = 1;
				  if('1' == $scope.model.payType){
					  if('1'==$scope.model.volType){
						  param.dataDiskOrdinary=$scope.model.volSize - $scope.model.volOldSize;
					  }else if('2'==$scope.model.volType){
						  param.dataDiskBetter=$scope.model.volSize - $scope.model.volOldSize;
					  }else if('3'==$scope.model.volType){
						  param.dataDiskBest=$scope.model.volSize - $scope.model.volOldSize;
					  }else{
						  param.dataDiskCapacity = $scope.model.volSize - $scope.model.volOldSize;
					  }
					  param.cycleCount=items.cycleCount;
					  url = 'billing/factor/getUpgradePrice.do';
				  }
				  else if('2' == $scope.model.payType){
					  param.payType = $scope.model.payType;
					  param.number = 1;
					  if('1'==$scope.model.volType){
						  param.dataDiskOrdinary=$scope.model.volSize;
					  }else if('2'==$scope.model.volType){
						  param.dataDiskBetter=$scope.model.volSize;
					  }else if('3'==$scope.model.volType){
						  param.dataDiskBest=$scope.model.volSize;
					  }else{
						  param.dataDiskCapacity = $scope.model.volSize;
					  }
					  
				  }

				  eayunHttp.post(url,param).then(function (response){
					  if(response.data.respCode == '010120'){
						  $scope.priceError = response.data.message;
					  }
					  else{
					      $scope.priceDetails =response.data.data;
					    
					      if('2' == $scope.model.payType){
					    	  if($scope.priceDetails.totalPrice<=0.00){
								  $scope.priceDetails.totalPrice=$scope.minPrice($scope.priceDetails.dataDiskPrice);
							  }else{
								  $scope.priceDetails.totalPrice=$scope.minPrice($scope.priceDetails.totalPrice);
							  }
					      }else{
					    	  
							   $scope.priceDetails=$scope.priceDetails;
							  
					      }
					      
					      
					  }
					 
				  });
			  };

			  
			  $scope.minPrice = function(_num){
				  if(_num>0 && _num<0.01){
					  _num = 0.01;
				  }
				  return _num;
			  };
			  
			  
			  $scope.cancel = function (){
				  $modalInstance.dismiss();
			  };
			  
			  $scope.ok = function (){
				  $modalInstance.close();
			  };
			  

			  /**
			   * 确认云硬盘扩容
			   */
			  $scope.large = function (){
				  eayunHttp.post('cloud/volume/checkVolOrderExsit.do',$scope.model.volId).then(function(response){
					  if(response && response.data && response.data.data){
						  var model=eayunModal.warning("资源正在调整中或您有未完成的订单，请您稍后重试。");
						  model.then(function(){
							  $scope.ok();
						  },function(){
							  $scope.ok();
						  });
					  }
					  else{
						    $scope.model.paymentAmount = $scope.priceDetails;
						    $scope.model.cycleCount=items.cycleCount;
							if('2' == $scope.model.payType){
								$scope.model.paymentAmount = $scope.priceDetails.totalPrice;
							}
							eayunStorage.set('order_confirm_purchasesvolume',$scope.model);
							$scope.ok();
							$state.go('buy.confirmvol',{'orderType':$scope.model.orderType});
					  }
				  });
		
			  };
			  
			  
			  /**
			   * 确认 扩容云硬盘大小
			   */
			  $scope.commit = function (){
				  if($scope.model.payType == '2'){
					  $scope.isNSF = false;
					  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
						  $scope.account = response.data.data;
						  if($scope.account.money <= 0){
							  $scope.isNSF = true;
							  var model = eayunModal.warning("您的账户已欠费，请充值后操作");
							  model.then(function(){
								  $scope.ok();
							  },function(){
								  $scope.ok();
							  });
						  }
						  else{
							  $scope.large();
						  }
					  });
				  }
				  else if($scope.model.payType == '1'){
					  $scope.large();
				  }
			  };

	}).controller('BuyVolumeController', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage){
		var dcPrj = sessionStorage["dcPrj"];
		$scope.volCapacityQuotaMsg = '';//云硬盘容量超配
		$scope.countQuotaMsg = '';//云硬盘个数超配
		$scope.volMaxLength=63;
		 $scope.typeSure=false;
		
		 $scope.init = function(){
			 var orderNo=$stateParams.orderNo;
			  if(orderNo){
				  //TODO 重新下单,查看数据库的具体订单信息
				  $scope.model={};
				  eayunHttp.post('cloud/volume/queryCloudOrderByOrderNo.do',orderNo).then(function (response){
					  if(response && response.data && response.data.data){
						  var data = response.data.data;
						  $scope.model.dcId=data.dcId;
						  $scope.model.prjId=data.prjId;
						  $scope.model.volNumber=data.volNumber;
						  $scope.model.volSize=data.volSize;
						  $scope.model.volName=data.volName;
						  $scope.model.volDescription=data.volDescription;
						  $scope.model.diskFrom=data.diskFrom;
						  $scope.model.payType=data.payType;
						  $scope.model.volTypeId=data.volTypeId;
						  $scope.model.cycleType=data.cycleType;
						  $scope.model.buyCycle=data.buyCycle;
						  if($scope.model.payType == '1'){
							  $scope.buyCycleType();
							  $scope.model.cycleType=$scope.model.cycleType;
							  $scope.queryBuyCycle();
							  $scope.model.buyCycle = $scope.model.buyCycle;
						  }
						  $scope.isNSF=false;
						  $scope.checkVolName = true;
						  $scope.orderAgain = true;
						  $scope.initValue();
						 
						  
					  }
				  });
			  }
			  else{
				  var item = eayunStorage.get('order_back_volume');
				  eayunStorage.delete('order_back_volume');
				  if(!item){
					  $scope.model={};
					  $scope.model.payType = $stateParams.payType;
					  $scope.model.volType='1';
					  if(null==$scope.model.payType||''==$scope.model.payType||'null'==$scope.model.payType){
						  $scope.model.payType='1';
					  }
					  if(dcPrj){
						  dcPrj = JSON.parse(dcPrj);
						  $scope.model.prjId = dcPrj.projectId;
					  }
					  $scope.model.volNumber=1;
					  $scope.model.volSize=1;
					  $scope.model.diskFrom='blank';
					  $scope.checkVolName = true;
					  $scope.checkVolSize=false;
					  $scope.isNSF=false;
					  $scope.initValue();
					  
				  }
				  else{
					  //TODO 返回修改配置
					  $scope.model = angular.copy(item);
					  if($scope.model.payType == '1'){
						  $scope.queryBuyCycle();
						  $scope.model.buyCycle = $scope.model.buyCycle;
					  }
					  $scope.isNSF=false;
					  $scope.checkVolName = true;
					  $scope.orderAgain = true;
					  $scope.initValue();
					  
				  }
			  }
		  };
		
		  $scope.initValue = function(){
			  //数据中心、项目列表
			  $scope.queryDcAndProject();
			  
			  if('1'==$scope.model.payType){
				  $scope.buyCycleType();
			  }
			 
			  if('2'==$scope.model.payType){
				  $scope.queryAccount();
			  }
			    
		  };
		  
		  
		  
		  
		  
		  
		  /**
		   * 查询数据中心和项目
		   */
		  $scope.queryDcAndProject = function(){
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
						  $scope.selectDc($scope.datacenters[0]);
					  }
					 
				  }
			  });
			  
		  };
		  
		  
		  /**
		   * 选择数据中心
		   */
		  $scope.selectDc = function (data){
			  $scope.model.dcId = data.dcId;
			  $scope.model.prjId = data.projectId;
			  $scope.model.dcName = data.dcName;
			  
			  $scope.getVolumeTypes($scope.model.dcId);
			  $scope.checkVolNameExist();
			  $scope.queryPrjQuota();
			  //$scope.calcBillingFactor();
		  };
		  
		  

		  
		  /**
		   * 选择云硬盘类型
		   */
		  $scope.selectVolType=function(data){
			  $scope.model.volType=data.volumeType;
			  $scope.model.volTypeId=data.typeId;
			  $scope.model.volumeTypeAs=data.volumeTypeAs;
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
					  angular.forEach($scope.volumeTypeList, function (value, key) {
						  if(value.typeId == $scope.model.volTypeId){
							  initialize = true;
							  $scope.selectVolType(value);
							  
						  }
					  });
					  
					  if(!initialize){
						  $scope.selectVolType($scope.volumeTypeList[0]);
					  }
					 
				  }else{
					  $scope.typeSure=true;
				  }
		     }); 
			  
		  }
		  
		  
		  
		  /**
		   * 立即充值
		   */
		  $scope.recharge = function(){
			  var rechargeNavList=[{route:'app.costcenter',name:'账户总览'}];
			    eayunStorage.persist("rechargeNavList",rechargeNavList);
			    $state.go('pay.recharge');
		  };
		  
		  /**
		   * 购买类型
		   */
		  $scope.buyVol = function(type){
			  $scope.model.payType = type;
			  $scope.checkVolName = true;
			  $scope.checkVolSize=false;
			  $scope.isNSF=false;
			  $scope.initValue();
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
			  data.number = $scope.model.volNumber;
			  if('1'==$scope.model.volType){
				  data.dataDiskOrdinary=$scope.model.volSize;
			  }else if('2'==$scope.model.volType){
				  data.dataDiskBetter=$scope.model.volSize;
			  }else if('3'==$scope.model.volType){
				  data.dataDiskBest=$scope.model.volSize;
			  }else{
				  data.dataDiskCapacity = $scope.model.volSize; 
			  }
			  
			  data.cycleCount = 1;
			 
			  if(data.payType == '1'){
				  data.cycleCount = $scope.model.buyCycle;
			  }
			  
			  if(data.number){
				  eayunHttp.post('billing/factor/getPriceDetails.do',data).then(function (response){
					  if(response&&response.data){
						  if(response.data.respCode == '010120'){
							  $scope.priceError = response.data.message;
						  }
						  else{
							  $scope.priceDetails = response.data.data;
							  if('2'==data.payType){
								  if($scope.priceDetails.totalPrice<=0.00){
									  $scope.priceDetails.totalPrice=$scope.minPrice($scope.priceDetails.dataDiskPrice);
								  }else{
									  $scope.priceDetails.totalPrice=$scope.minPrice($scope.priceDetails.totalPrice);
								  }
								  
							  }
						  }
					  }
				  });
			  }
		  };
		  
		  
		  $scope.minPrice = function(_num){
			  if(_num>0 && _num<0.01){
				  _num = 0.01;
			  }
			  return _num;
		  };
		  
		  /**
		   * 查看账户余额
		   */
		  $scope.queryAccount = function (){
			  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
				  $scope.account = response.data.data;
				  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
					  $scope.payAfpterPayment = response.data;
				  });
			  });
		  };
		  

		  /**
		   * 校验云硬盘重名（项目维度、云硬盘数量）
		   */
		  $scope.checkVolNameExist = function (){
			  $scope.checkVolName = true;
			  var volNumber=$scope.model.volNumber;
			  
			  if(volNumber>=1&&volNumber<=20){
				  if($scope.model.prjId && $scope.model.dcId && $scope.model.volName){
					  var cloudVolume ={}
					  cloudVolume.dcId= $scope.model.dcId;
					  cloudVolume.prjId = $scope.model.prjId;
					  cloudVolume.volName = $scope.model.volName;
					  cloudVolume.volNumber = volNumber;
					  eayunHttp.post('cloud/volume/getVolumeByName.do',cloudVolume).then(function (response){
							  $scope.checkVolName = response.data;
					  });
				  }
			  }else{
				  return;
			  }
			
		  };
		  
		  
		  
		  
		 
		  /**
		   * 检验创建云硬盘的大小
		   * 检验当前项目下可创建的云硬盘容量
		   */
		  $scope.changeSize= function(value){
			  $scope.isError=false;
			  if(value<10){
				$scope.model.volSize=10;
			  }else{
				$scope.model.volSize=value; 
			  }
			  $scope.queryPrjQuota();
			  $scope.calcBillingFactor();
		  };
		  
		  
		  //拖动条
		  $scope.formate=function(step){
			  return Number((step*100).toFixed());
		  };
		  
		  
		  $scope.parse=function(value){
			  return Number((value/100+0.044444).toFixed(1));
		  };
		  
		  
		 
		  
		  
		  
		  /**
		   * 检验创建云硬盘的数量(1-20)
		   * 检验当前项目下可创建的云硬盘数量
		   * 检验批量创建云硬盘名称重名
		   */
		  $scope.checkVolNum = function(){
			  if($scope.model.volNumber!=null&&$scope.model.volNumber>1){
				  $scope.volMaxLength=60;
			  }else{
				  $scope.volMaxLength=63;
				  //$scope.model.volNumber=1;
			  }
			  if(null!=$scope.model.volNumber&&undefined!=$scope.model.volNumber&&''!=$scope.model.volNumber){
				  $scope.checkVolNameExist();
				  $scope.queryPrjQuota();
				  $scope.calcBillingFactor();
			  }
			 
		  };
		  
		  
		  /**
		   * 获取项目配额
		   */
		  $scope.queryPrjQuota = function(){
			  eayunHttp.post('cloud/vm/getProjectjQuotaById.do',$scope.model.prjId).then(function(response){
				  $scope.project = response.data.data;
				  $scope.checkVolQuota();
			  });
		  };
		  
		  /**
		   * 校验云硬盘的配额
		   */
		  $scope.checkVolQuota = function(){
			  
			  var num = $scope.model.volNumber;
			  var disk = $scope.model.volSize;
			  var prj = $scope.project;

			  $scope.volCapacityQuotaMsg = '';//云硬盘容量超配
			  $scope.countQuotaMsg = '';//云硬盘个数超配

			  if(prj&&num){
				 
				  if(num>(prj.diskCount - prj.diskCountUse)){
					  $scope.countQuotaMsg = $scope.countQuotaMsg+'云硬盘数量';
				  }

				  if(disk&&((num*disk)>(prj.diskCapacity - prj.usedDiskCapacity))){
					  $scope.volCapacityQuotaMsg =$scope.volCapacityQuotaMsg+'云硬盘容量';
				  }
			  }
			  
			  if('' != $scope.countQuotaMsg){
				  $scope.countQuotaMsg = $scope.countQuotaMsg.substr(0,$scope.countQuotaMsg.length)+'配额不足';
			  }
			  if('' != $scope.volCapacityQuotaMsg){
				  $scope.volCapacityQuotaMsg = $scope.volCapacityQuotaMsg.substr(0,$scope.volCapacityQuotaMsg.length)+'配额不足';
			  }
			  
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
		   * 购买周期
		   */
		  $scope.calcBuyCycle = function(){
			  $scope.calcBillingFactor();
		  };
		  
		  
		  /**
		   * 购买云硬盘，调整到提交订单的页面
		   */
		  $scope.commitBuyVolume = function (){
			  //TODO 提交逻辑
			  $scope.isNSF = false;
			  if('2'==$scope.model.payType){
				  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
					  $scope.account = response.data.data;
					  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
						  $scope.payAfpterPayment = response.data;
						  $scope.isNSF = $scope.account.money < $scope.payAfpterPayment;
						  if($scope.isNSF){
							  return ;
						  }
						  
						  var data = {}
						  data.dcId = $scope.model.dcId;
						  data.dcName = $scope.model.dcName;
						  data.prjId = $scope.model.prjId;
						  data.volNumber = $scope.model.volNumber;
						  data.volSize=$scope.model.volSize;
						  data.volType=$scope.model.volType;
						  data.volTypeId=$scope.model.volTypeId;
						  data.volumeTypeAs=$scope.model.volumeTypeAs;
						  data.volName=$scope.model.volName;
						  data.volDescription=$scope.model.volDescription;
						  data.diskFrom=$scope.model.diskFrom;
						  data.orderType = '0';
						  data.payType = $scope.model.payType;
						  if('1'==$scope.model.payType){
							  data.cycleType = $scope.model.cycleType;
							  data.buyCycle = $scope.model.buyCycle;
							  data.prodName='云硬盘-包年包月';
						  }else{
							  data.prodName='云硬盘-按需付费';
						  }
						  
						  data.paymentAmount = $scope.priceDetails.totalPrice;
						  eayunStorage.set('order_confirm_purchasesvolume',data);
						  $state.go('buy.confirmvol',{'orderType':data.orderType});
					  });
				  });
			  }
			  else{
				  var data = {}
				  data.dcId = $scope.model.dcId;
				  data.dcName = $scope.model.dcName;
				  data.prjId = $scope.model.prjId;
				  data.volNumber = $scope.model.volNumber;
				  data.volSize=$scope.model.volSize;
				  data.volType=$scope.model.volType;
				  data.volTypeId=$scope.model.volTypeId;
				  data.volumeTypeAs=$scope.model.volumeTypeAs;
				  data.volName=$scope.model.volName;
				  data.volDescription=$scope.model.volDescription;
				  data.diskFrom=$scope.model.diskFrom;
				  data.orderType = '0';
				  data.payType = $scope.model.payType;
				  if('1'==$scope.model.payType){
					  data.cycleType = $scope.model.cycleType;
					  data.buyCycle = $scope.model.buyCycle;
					  data.prodName='云硬盘-包年包月';
				  }else{
					  data.prodName='云硬盘-按需付费';
				  }
				  
				  data.paymentAmount = $scope.priceDetails.totalPrice;
				  eayunStorage.set('order_confirm_purchasesvolume',data);
				  $state.go('buy.confirmvol',{'orderType':data.orderType});
				  
			  }
		  };

		  
		  $scope.init();
		  
	  }).controller('ConfirmVolOrderController', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage,eayunMath) {

		  $scope.model={};
		  $scope.item = eayunStorage.get('order_confirm_purchasesvolume');

		  if(!$scope.item){
			  if($stateParams.orderType == '0'){
				  $state.go('buy.volume',{payType:'1'});
			  }
			  else if($stateParams.orderType == '2'){
				  $state.go('app.cloud.cloudhost.volume.list');
			  }
	
		  }else{
			  if('0'== $scope.item.orderType){
				  //TODO 新购订单
				  $scope.title = '创建云硬盘';
			  }
			  else if('2'== $scope.item.orderType){
				  //TODO 升级配置的订单
				  $scope.title = '扩容';
			  }
			  
		
		  } 
		  
		  
		  
		  
		  
		  /**
		   * 返回修改配置
		   */
		  $scope.backToVol = function(){
			  eayunStorage.set('order_back_volume',$scope.item);
			  $state.go('buy.volume',{payType:$scope.item.payType});
		  };
		  
		  
		  
		  $scope.initValue = function(){
			  if($scope.item.payType=='1'){
				  $scope.queryAccount();
			  }
			  
			//$scope.queryPrjQuota();
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
				  $scope.checkVolQuota();
			  });
		  };
		  
		  /**
		   * 校验云硬盘的配额
		   */
		  $scope.checkVolQuota = function(){
			  var num = $scope.item.volNumber;
			  var disk = $scope.item.volSize;
			  var prj = $scope.project;

			  $scope.errorMsg = '';
			  var quotaErrorMsg = '';//超配提示语
			  if(prj&&num){
				  if('2'== $scope.item.orderType){
					  disk=$scope.item.volSize-$scope.item.volOldSize;
				  }
				 
				  
				  if('0'== $scope.item.orderType){
					  if(num>(prj.diskCount - prj.diskCountUse)){
						  quotaErrorMsg = quotaErrorMsg +'云硬盘数量';
					  }
				  }
				  
				  if(disk&&((num*disk)>(prj.diskCapacity - prj.usedDiskCapacity))){
					  if(''==quotaErrorMsg){
						  quotaErrorMsg = quotaErrorMsg+'云硬盘容量';
					  }else{
						  quotaErrorMsg = quotaErrorMsg+'和容量';
					  }
					  
				  }
			  }
			  
			  if('' != quotaErrorMsg){
				  $scope.errorMsg = '您的'+quotaErrorMsg.substr(0,quotaErrorMsg.length)+'配额不足，请提交工单申请配额';
			  }
		  };
		  
		  
		
		  
		  /**
		   * 计费因子价格发生变化
		   */
		  $scope.reCalculateBillingFactory = function (){
			  var data = {};
			  var url =null;
			  data.dcId = $scope.item.dcId;
			  
			  if('1'==$scope.item.volType){
				  data.dataDiskOrdinary=$scope.item.volSize;
			  }else if('2'==$scope.item.volType){
				  data.dataDiskBetter=$scope.item.volSize;
			  }else if('3'==$scope.item.volType){
				  data.dataDiskBest=$scope.item.volSize;
			  }else{
				  data.dataDiskCapacity = $scope.item.volSize; 
			  }
			  
			  data.cycleCount = 1;
			  var url = 'billing/factor/getPriceDetails.do';
			  if('0'==$scope.item.orderType){
				  data.payType = $scope.item.payType;
				  data.number = $scope.item.volNumber;
				  if(data.payType == '1'){
					  data.cycleCount = $scope.item.buyCycle;
				  }
				  eayunHttp.post(url,data).then(function (response){
					  if(response && response.data && response.data.data){
						  $scope.item.paymentAmount = response.data.data.totalPrice;
						  
						  if($scope.item.orderType == '2'){
							  $scope.item.paymentAmount = response.data.data; 
						  }
						  else if($scope.item.orderType == '0'){
							  $scope.item.paymentAmount = response.data.data.totalPrice;
						  }
						  $scope.useAccountPay();
					  }
				  });
				  
			  }
			  
			  if('2'==$scope.item.orderType){
				  eayunHttp.post('cloud/volume/queryVolChargeById.do',$scope.item.volId).then(function(response){
					  if(response && response.data && response.data.data){
						  $scope.item.cycleCount = response.data.data.cycleCount;
						  $scope.item.volOldSize=response.data.data.volSize;
					  }
					  if($scope.item.payType == '1'){
						  var realitySize=$scope.item.volSize-$scope.item.volOldSize;
						  if('1'==$scope.item.volType){
							  data.dataDiskOrdinary=realitySize;
						  }else if('2'==$scope.item.volType){
							  data.dataDiskBetter=realitySize;
						  }else if('3'==$scope.item.volType){
							  data.dataDiskBest=realitySize;
						  }else{
							  data.dataDiskCapacity = realitySize; 
						  }
						  data.cycleCount = $scope.item.cycleCount;
						  url = 'billing/factor/getUpgradePrice.do';
					  }else{
						  data.payType = $scope.item.payType;
						  data.number=1;
					  }
					  
					  eayunHttp.post(url,data).then(function (response){
						  if(response && response.data && response.data.data){
							  $scope.item.paymentAmount = response.data.data.totalPrice;
							  
							  if($scope.item.orderType == '2'){
								  $scope.item.paymentAmount = response.data.data; 
							  }
							  else if($scope.item.orderType == '0'){
								  $scope.item.paymentAmount = response.data.data.totalPrice;
							  }
							  $scope.useAccountPay();
						  }
					  });

				  });  
				  
			  }
			  
		  };
		  
		  
		  

		  /**
		   * 提交订单信息
		   */
		  $scope.commitBuy = function(){
			  if('1' == $scope.item.payType){
				  $scope.item.accountPayment=$scope.model.accountPayment;
				  $scope.item.thirdPartPayment=$scope.model.thirdPartPayment;
			  }

			  $scope.warnMsg = '';
			  $scope.checkBtn = true;
			  var data = $scope.item;
			  var url = 'cloud/volume/buyVolume.do';
			  if($scope.item.orderType == '2'){
				  url = 'cloud/volume/extendVolume.do';
			  }


			  eayunHttp.post(url,data).then(function(response){
				  if(response && response.data){
					  //订单支付成功
					  if(response.data.respCode == '000000'){
						  //预付费订单提交成功
						  if('1' == data.payType){
							  if(!data.thirdPartPayment){
								  $state.go('pay.result', {subject:data.prodName});
							  }
							  else{
								  //TODO 跳转到订单支付界面
								  if('0'==data.orderType){
									  var routeUrl = "buy.volume({'payType':data.payType})";
									  var orderPayNavList = [{route:'app.cloud.cloudhost.volume',name:'云硬盘'},
									                         {route:routeUrl,name:'创建云硬盘'}];
			                          eayunStorage.persist("orderPayNavList",orderPayNavList);
			                          eayunStorage.persist("payOrdersNo",response.data.orderNo);
			    					  $state.go('pay.order');
								  }
								  if('2'==data.orderType){
									 var orderPayNavList = [{route:'app.cloud.cloudhost.volume',name:'云硬盘'}];
			                          eayunStorage.persist("orderPayNavList",orderPayNavList);
			                          eayunStorage.persist("payOrdersNo",response.data.orderNo);
			    					  $state.go('pay.order');
								  }
								  
								  
							  }

						  }
						  //后付费订单提交成功
						  else if ('2' == data.payType){
							  $state.go('app.order.list');
						  }
					  }
					  //TODO 订单提交失败
					  else if(response.data.respCode == '010110'){
						  $scope.checkBtn = false;
						  if(response.data.message == 'OUT_OF_QUOTA'){
							  $scope.queryPrjQuota();
						  }
						  else if(response.data.message == 'CHANGE_OF_BILLINGFACTORY'){
							  $scope.reCalculateBillingFactory();
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

					  }else{
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
		  
		  $scope.initValue();

	  })
	   .controller('cloudVolumeRenewCtrl',function ($scope,eayunHttp,item, eayunModal,eayunStorage,$modalInstance){
		   $scope.model = angular.copy(item);
		   /**
		     * 云硬盘状态 显示
		     */
		    $scope.getVolumeStatus =function (model){
		    	if('DELETING'!=model.volStatus&&('1'==model.chargeState||'2'==model.chargeState||'3'==model.chargeState)){
		    		return 'ey-square-disable';
		    	}else if(model.volStatus&&model.volStatus=='AVAILABLE'){
		    		return 'ey-square-space';
		    	}
		    	else if(model.volStatus=='IN-USE'){
					return 'ey-square-right';
				}  
				else if(model.volStatus=='ERROR'){
					return 'ey-square-error';
				}
				else{
					return'ey-square-warning';
				}
		    };
		   
		  //重写确定 取消按钮方法
		  $scope.cancel = function () {
	          $modalInstance.dismiss('cancel');
	      };
	      $scope.commit = function () {
	          $modalInstance.close();
	      };
	      
		  $scope.model = angular.copy(item);
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
					  };
			  
			  if("1"==item.volBootable){
				  if('1'==item.volType){
					  paramBean.sysDiskOrdinary=$scope.model.volSize;
				  }else if('2'==item.volType){
					  paramBean.sysDiskBetter=$scope.model.volSize;
				  }else if('3'==item.volType){
					  paramBean.sysDiskBest=$scope.model.volSize;
				  }else{
					  paramBean.sysDiskCapacity = $scope.model.volSize;
				  }
				  
			  }else if("0"==item.volBootable){
				  if('1'==item.volType){
					  paramBean.dataDiskOrdinary=$scope.model.volSize;
				  }else if('2'==item.volType){
					  paramBean.dataDiskBetter=$scope.model.volSize;
				  }else if('3'==item.volType){
					  paramBean.dataDiskBest=$scope.model.volSize;
				  }else{
					  paramBean.dataDiskCapacity = $scope.model.volSize;
				  }
				  
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
				  };
		  
		  if("1"==item.volBootable){
			  if('1'==item.volType){
				  paramBean.sysDiskOrdinary=$scope.model.volSize;
			  }else if('2'==item.volType){
				  paramBean.sysDiskBetter=$scope.model.volSize;
			  }else if('3'==item.volType){
				  paramBean.sysDiskBest=$scope.model.volSize;
			  }else{
				  paramBean.sysDiskCapacity = $scope.model.volSize;
			  }
		  }else if("0"==item.volBootable){
			  if('1'==item.volType){
				  paramBean.dataDiskOrdinary=$scope.model.volSize;
			  }else if('2'==item.volType){
				  paramBean.dataDiskBetter=$scope.model.volSize;
			  }else if('3'==item.volType){
				  paramBean.dataDiskBest=$scope.model.volSize;
			  }else{
				  paramBean.dataDiskCapacity = $scope.model.volSize;
			  }
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
	  eayunStorage.set('payType' , item.payType);
	  eayunStorage.set('dcName'  , item.dcName);
	  eayunStorage.set('volId'   , item.volId);
	  eayunStorage.set('volName' , item.volName);
	  eayunStorage.set('volSize' , item.volSize);
	  eayunStorage.set('dcId' , item.dcId);
	  eayunStorage.set('volType' , item.volType);
	  eayunStorage.set('volumeTypeAs' , item.volumeTypeAs);
  }).controller('RenewVolumeConfirmController',function ($scope,eayunHttp, eayunModal,eayunStorage,$state,VolumeService,eayunMath){
	  //如果F5刷新  直接跳路由
	  if('undefined'== eayunStorage.get('needPay') || null == eayunStorage.get('needPay')){
		  $state.go("app.cloud.cloudhost.volume.list");
	  }
	  var needPay = eayunStorage.get('needPay');
	  $scope.model = {
			  payType : eayunStorage.get('payType'),
			  dcName  : eayunStorage.get('dcName'),
			  volId   : eayunStorage.get('volId'),
			  volName : eayunStorage.get('volName'),
			  volSize : eayunStorage.get('volSize'),
			  cycle   : eayunStorage.get('cycle'),
			  needPay : needPay,
			  dcId    : eayunStorage.get('dcId'),
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
			  $scope.model.deductMoney = 0.00;//formatFloat(0.00,2);
			  $scope.model.actualPay = $scope.model.needPay;//formatFloat($scope.model.needPay,2);
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
                  $scope.model.needPay = response.data.data.totalPrice;//formatFloat(response.data.message,2);
              }
              if(b1&&b2){
              	$scope.useBalance();
              }
          });
      };

        function refreshConfiguration(_volId){
            eayunHttp.post('cloud/volume/getVolumeById.do',{volId:_volId}).then(function (response){
                if(response.data!=null){
                    var data = response.data;
                    $scope.model.volSize = data.volSize;
                    if('1'==$scope.model.volType){
                        $scope.paramBean.dataDiskOrdinary=$scope.model.volSize;
                    }else if('2'==$scope.model.volType){
                        $scope.paramBean.dataDiskBetter=$scope.model.volSize;
                    }else if('3'==$scope.model.volType){
                        $scope.paramBean.dataDiskBest=$scope.model.volSize;
                    }else{
                        $scope.paramBean.dataDiskCapacity = $scope.model.volSize;
                    }
                    refreshMoney();
                }
            });
        };
      
    //续费订单确认页 -- 提交订单
      $scope.isLight = false;
      $scope.isError = false;
      $scope.errorMsg ='';
        $scope.submitOrder = function(){
        	VolumeService.checkIfOrderExist($scope.model.volId).then(function(response){
                $scope.errorMsg ='资源正在调整中或您有未完成的订单，请稍后再试。';
      		    $scope.isError = true;
            },function(){
        		// 获取当前余额，判断此时账户余额是否大于余额支付金额，以确定能否提交订单
                    var map ={
                    	  volId   : $scope.model.volId,
                  		  aliPay : $scope.model.actualPay,
                  		  accountPay : $scope.model.deductMoney,
                  		  totalPay : $scope.model.needPay,
                  		  isCheck : $scope.model.isCheck,
                  		  dcName : $scope.model.dcName,
                  		  buyCycle : $scope.model.cycle,
                  		  dcId : $scope.model.dcId,
          				  payType : '1',
          				  number : 1,
          				  cycleCount : $scope.model.cycle,
          				  dataDiskCapacity : $scope.model.volSize,
          				  volType : $scope.model.volType,
          				  volumeTypeAs:$scope.model.volumeTypeAs
                    };

                    eayunHttp.post('cloud/volume/renewVolumeOrderConfirm.do',map).then(function(response){
          			  if(response && response.data){
          				  //订单支付成功
          				  if(response.data.respCode == '1'){//您当前有未完成订单，不允许提交新订单！
          					  $scope.isError = true;
          					  $scope.errorMsg =response.data.message;
          				  }
          				  else if(response.data.respCode == '2'){//您的产品金额发生变动，请重新确认订单！
          					  $scope.isLight = true;
                              $scope.model.isCheck = false;
          					  $scope.errorMsg =response.data.message;
                              refreshConfiguration($scope.model.volId);
          				  }
          				  else if(response.data.respCode == '3'){//您的账户余额发生变动，请重新确认订单！
          					  $scope.isLight = true;
                              $scope.model.isCheck = false;
          					  $scope.errorMsg =response.data.message;
          					  refreshMoney(); 
          				  }
          				  else if(response.data.respCode == '0'){//完全支付宝支付，跳向支付宝支付页面！
      					     var orderPayNavList = [{route:'app.cloud.cloudhost.volume.list',name:'云硬盘'}];
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
      
  }).controller('DeleteVolume', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage,volume,$modalInstance) {
	  $scope.model=volume;
	  $scope.deleteSnapshot=false;
	  $scope.overDelete=false;
	  $scope.isShow=false;
	  
	 
	  if('0'!=$scope.model.chargeState){
		  $scope.overDelete=true;
		  $scope.isShow=true;
	  }
	  
	  $scope.cancel = function (){
		  $modalInstance.dismiss();
	  };

	  $scope.commit= function (){
		  if($scope.deleteSnapshot&&$scope.overDelete){
			  $scope.model.isDeleted='1';
			  $scope.model.isDeSnaps='1';//代表彻底删除备份
		  }else if($scope.deleteSnapshot&&!$scope.overDelete){
			  $scope.model.isDeleted='2';
			  $scope.model.isDeSnaps='2';//代表放入回收站
		  }else if($scope.overDelete&&!$scope.deleteSnapshot){
			  $scope.model.isDeleted='1';
			  $scope.model.isDeSnaps=null;//代表不删除备份
		  }else if(!$scope.overDelete&&!$scope.deleteSnapshot){
			  $scope.model.isDeleted='2';
			  $scope.model.isDeSnaps=null;//代表不删除备份
		  }
		  
		  $modalInstance.close($scope.model);

	  };
	  
  }).controller('BuySnapshotController', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage){
	  
	  $scope.init = function(){
		  var orderNo=$stateParams.orderNo;
		  if(orderNo){
			  //TODO 重新下单,查看数据库的具体订单信息
			  $scope.model={};
			  eayunHttp.post('cloud/snapshot/queryCloudOrderByOrderNo.do',orderNo).then(function (response){
				  if(response && response.data && response.data.data){
					  var data = response.data.data;
					  $scope.model.payType = '2';
					  $scope.model.dcId=data.dcId;
					  $scope.model.dcName=data.dcName;
					  $scope.model.prjId=data.prjId;
					  $scope.model.volId=data.volId;
					  $scope.model.volName=data.volName;
					  $scope.model.snapName=data.snapName;
					  $scope.model.snapSize=data.snapSize;
					  $scope.model.snapType=data.snapType;
					  $scope.model.snapDescription=(data.snapDescription=='null'?'':data.snapDescription);
					  $scope.model.prodName='云硬盘备份-按需付费';
					  $scope.isNSF=false;
					  $scope.checkSnapName = true;
					  $scope.orderAgain = true;
					  
					  $scope.checkSnapshotExist();
					  $scope.initValue();
				  }
			  });
		  }
		  else{
			  var item = eayunStorage.get('order_back_snapshot');
			  eayunStorage.delete('order_back_snapshot');
			  
			  if(!item){
				  $scope.volume={};
				  $scope.fromVolId='';
				  $scope.fromVmId='';
				  var volId=$stateParams.volId;
				  $scope.fromVolId=$stateParams.fromVolId;
				  $scope.fromVmId=$stateParams.fromVmId;
				 
				  
				  if(null!=volId){
					  eayunHttp.post('cloud/volume/getVolumeById.do',{volId:volId}).then(function(response){
						  $scope.volume=response.data;
						  if(null==$scope.volume||''==$scope.volume||'null'==$scope.volume){
							  $state.go('app.cloud.cloudhost.volume.list');
						  }
						  $scope.model={};
						  $scope.model.payType = '2';
						  $scope.model.dcId=$scope.volume.dcId;
						  $scope.model.dcName=$scope.volume.dcName;
						  $scope.model.prjId=$scope.volume.prjId;
						  $scope.model.volId=$scope.volume.volId;
						  $scope.model.volName=$scope.volume.volName;
						  $scope.model.snapSize=$scope.volume.volSize;
						  $scope.model.prodName='云硬盘备份-按需付费';
						  
						  if('1'==$scope.volume.volBootable){
							  $scope.model.snapType='1';
						  }else{
							  $scope.model.snapType='0';
						  }
						  
						  $scope.checkSnapName = true;
						  $scope.isNSF=false;
						  
						  $scope.initValue();
				    });
				  }

			  }
			  else{
				  //TODO 返回修改配置
				  $scope.model = angular.copy(item);
				  $scope.checkSnapName = true;
				  $scope.orderAgain = true;
				  $scope.isNSF=false;
				  $scope.fromVolId='';
				  $scope.fromVmId='';
				  $scope.fromVolId= $scope.model.fromVolId;
				  $scope.fromVmId= $scope.model.fromVmId;
				  
				  $scope.checkSnapshotExist();
				  $scope.initValue();

			  }
		  }
		  
	  };
	  

	 
	  $scope.initValue = function(){
		  $scope.queryAccount();
		  $scope.checkSnapshotQuota();
		  $scope.calcBillingFactor();
	  };
	  
	  
	  $scope.goToDetail=function(){
		  $state.go('app.cloud.cloudhost.volumedetail',{"detailType":'volume',"dcId":$scope.model.dcId,"prjId":$scope.model.prjId,"volId":$scope.fromVolId}); // 跳转后的URL;
	  };
	  
	  $scope.goToVmDetail=function(){
		  $state.go('app.cloud.cloudhost.hostdetail',{"detailType":'host',"vmId":$scope.fromVmId});
	  };

	  
	  /**
	   * 资源计费查询
	   */
	  $scope.calcBillingFactor = function(){
		  $scope.priceError=null;
		  $scope.priceDetails = null;
		  var data = {};
		  data.dcId = $scope.model.dcId;
		  data.payType = $scope.model.payType;
		  data.number = 1;
		  data.snapshotSize = $scope.model.snapSize;
		  data.cycleCount = 1;
		  if(data.number){
			  eayunHttp.post('billing/factor/getPriceDetails.do',data).then(function (response){
				  if(response&&response.data){
					  if(response.data.respCode == '010120'){
						  $scope.priceError = response.data.message;
					  }
					  else{
						  $scope.priceDetails = response.data.data;
						  if('2'==data.payType){
							  if($scope.priceDetails.totalPrice<=0.00){
								  $scope.priceDetails.totalPrice=$scope.minPrice($scope.priceDetails.snapshotPrice);
							  }else{
								  $scope.priceDetails.totalPrice=$scope.minPrice($scope.priceDetails.totalPrice);
							  }
							  
						  }
					  }
				  }
			  });
		  }
	  };
	  
	  
	  $scope.minPrice = function(_num){
		  if(_num>0 && _num<0.01){
			  _num = 0.01;
		  }
		  return _num;
	  };
	  
	  
	  
	  /**
	   * 立即充值
	   */
	  $scope.recharge = function(){
		  var rechargeNavList=[{route:'app.costcenter',name:'账户总览'}];
		    eayunStorage.persist("rechargeNavList",rechargeNavList);
		    $state.go('pay.recharge');
	  };
	  
	  
	  /**
	   * 查看账户余额
	   */
	  $scope.queryAccount = function (){
		  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
			  $scope.account = response.data.data;
			  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
				  $scope.payAfpterPayment = response.data;
			  });
		  });
	  };
	  

	  /**
	   * 校验备份重名（项目维度）
	   */
	  $scope.checkSnapshotExist = function (){
		  if(null==$scope.model.snapName||'null'==$scope.model.snapName||''==$scope.model.snapName){
			  return;
		  }
		  $scope.checkSnapName = true;
		 
		  if($scope.model.prjId && $scope.model.dcId && $scope.model.snapName){
			  var cloudSnapshot={}
			  cloudSnapshot.dcId= $scope.model.dcId;
			  cloudSnapshot.prjId = $scope.model.prjId;
			  cloudSnapshot.snapName = $scope.model.snapName;
			  
			  eayunHttp.post('cloud/snapshot/getSnapByName.do',cloudSnapshot).then(function (response){
					  $scope.checkSnapName = response.data;
			  });
		  }
	  };
	  
	  
	  
	
	  
	 
	  /**
	   * 校验云硬盘备份的配额
	   */
	  $scope.checkSnapshotQuota = function(){
	
		  $scope.snapQuotaMsg = '';//云硬盘备份超配
		  $scope.countQuotaMsg='';
		  $scope.snapCapacityQuotaMsg='';

		  eayunHttp.post('cloud/snapshot/getProjectjQuotaById.do',$scope.model.prjId).then(function (response){
				  $scope.prject= response.data.data;  
	
				  if($scope.prject.diskSnapshot-$scope.prject.diskSnapshotUse<1){
					  $scope.countQuotaMsg ='备份数量';
				  }
				  if($scope.prject.snapshotSize-$scope.prject.usedSnapshotCapacity<$scope.model.snapSize){
					  $scope.snapCapacityQuotaMsg ='备份容量';
				  }
				  
				  if('' != $scope.countQuotaMsg&&''==$scope.snapCapacityQuotaMsg){
					  $scope.snapQuotaMsg = $scope.countQuotaMsg.substr(0,$scope.countQuotaMsg.length)+'配额不足';
				  }
				  if('' != $scope.snapCapacityQuotaMsg&&''==$scope.countQuotaMsg){
					  $scope.snapQuotaMsg = $scope.snapCapacityQuotaMsg.substr(0,$scope.snapCapacityQuotaMsg.length)+'配额不足';
				  }
				  if('' != $scope.countQuotaMsg&&'' != $scope.snapCapacityQuotaMsg){
					  $scope.snapQuotaMsg=$scope.countQuotaMsg.substr(0,$scope.countQuotaMsg.length)+'、'+$scope.snapCapacityQuotaMsg.substr(0,$scope.snapCapacityQuotaMsg.length)+'配额不足'
				  }
		  });
	
	  };
	  

	  
	/**
	   * 购买备份，调整到提交订单的页面
	   */
	  $scope.commitBuySnapshot = function (){
		  //TODO 提交逻辑
		  $scope.isNSF = false;
		  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
			  $scope.account = response.data.data;
			  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
				  $scope.payAfpterPayment = response.data;
				  $scope.isNSF = $scope.account.money < $scope.payAfpterPayment;
				  
				  if($scope.isNSF){
					  return ;
				  }
				  
				  var data = {}
				  data.dcId = $scope.model.dcId;
				  data.dcName = $scope.model.dcName;
				  data.prjId = $scope.model.prjId;
				  data.payType='2';
				  data.orderType = '0';
				  data.prodName='云硬盘备份-按需付费';
				  data.volId=$scope.model.volId;
				  data.volName=$scope.model.volName;
				  data.snapDescription=$scope.model.snapDescription;
				  data.snapSize=$scope.model.snapSize;
				  data.snapName=$scope.model.snapName;
				  data.snapType=$scope.model.snapType;
				  data.fromVolId=$scope.fromVolId;
				  data.fromVmId=$scope.fromVmId;
				  data.paymentAmount=$scope.priceDetails.totalPrice;
				  
				  eayunStorage.set('order_confirm_purchasessnapshot',data);
				  $state.go('buy.confirmsnap',{prjId:data.prjId});
				  
				  
			  });
		  });
		  
	  };

	  $scope.init();
	  
  }).controller('ConfirmSnapOrderController', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage) {
	  $scope.item = eayunStorage.get('order_confirm_purchasessnapshot');

	  if(!$scope.item){
		  $state.go('app.cloud.cloudhost.volume.list');
	  }
	  
	  
	  /**
	   * 返回修改配置
	   */
	  $scope.backToSnap = function(){
		  eayunStorage.set('order_back_snapshot',$scope.item);
		  if(null!=$scope.item.fromVolId&&''!=$scope.item.fromVolId&&'null'!=$scope.item.fromVolId){
			  $state.go('buy.snapshot',{payType:$scope.item.payType,volId:$scope.item.volId,fromVolId:$scope.item.fromVolId});
		  }else if(null!=$scope.item.fromVmId&&''!=$scope.item.fromVmId&&'null'!=$scope.item.fromVmId){
			  $state.go('buy.snapshot',{payType:$scope.item.payType,volId:$scope.item.volId,fromVmId:$scope.item.fromVmId});
		  }else{
			  $state.go('buy.snapshot',{payType:$scope.item.payType,volId:$scope.item.volId});
		  }
		  
	  };
	  
	  
	  $scope.goToDetail=function(){
		  $state.go('app.cloud.cloudhost.volumedetail',{"detailType":'volume',"dcId":$scope.item.dcId,"prjId":$scope.item.prjId,"volId":$scope.item.fromVolId}); // 跳转后的URL;
	  };
	  
	  $scope.goToVmDetail=function(){
		  $state.go('app.cloud.cloudhost.hostdetail',{"detailType":'host',"vmId":$scope.item.fromVmId});
	  };
	  
	  $scope.initValue = function(){
		  if($scope.item.payType=='1'){
			  $scope.queryAccount();
		  }
		  //$scope.queryPrjQuota();
	  };
	  
	  
	  /**
	   * 查看账户余额
	   */
	  $scope.queryAccount = function (){
		  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
			  $scope.account = response.data.data;
		  });
	  };
	  
	
	  
	  /**
	   * 获取项目配额
	   */
	  $scope.queryPrjQuota = function(){
		  eayunHttp.post('cloud/snapshot/getProjectjQuotaById.do',$scope.item.prjId).then(function(response){
			  $scope.project = response.data.data;
			  $scope.checkSnapshotQuota();
		  });
	  };
	  
	  /**
	   * 校验云硬盘备份的配额
	   */
	  $scope.checkSnapshotQuota = function(){
		  var num = 1;
		  var snapSize = $scope.item.snapSize;
		  var prj = $scope.project;

		  $scope.errorMsg = '';
		  var quotaErrorMsg = '';//超配提示语
		  if(prj&&num){
			  
			  if('0'== $scope.item.orderType){
				  if(num>(prj.diskSnapshot - prj.diskSnapshotUse)){
					  quotaErrorMsg = quotaErrorMsg +'备份数量';
				  }
			  }
			  
			  if(snapSize>(prj.snapshotSize - prj.usedSnapshotCapacity)){
				  if(''==quotaErrorMsg){
					  quotaErrorMsg = quotaErrorMsg+'备份容量';
				  }else{
					  quotaErrorMsg = quotaErrorMsg+'和容量';
				  }
				    
			  }
		  }
		  
		  if('' != quotaErrorMsg){
			  $scope.errorMsg = '您的'+quotaErrorMsg.substr(0,quotaErrorMsg.length)+'配额不足，请提交工单申请配额';
		  }
	  };
	  
	  
	  
	  /**
	   * 计费因子价格发生变化
	   */
	  $scope.reCalculateBillingFactory = function (){
		  var data = {};
		
		  data.dcId = $scope.item.dcId;
		  data.payType = $scope.item.payType;
		  data.number = 1;
		  data.snapshotSize = $scope.item.snapSize;
		  data.cycleCount = 1;
		  var url = 'billing/factor/getPriceDetails.do';
		  
		  if(data.count){
			  eayunHttp.post(url,data).then(function (response){
				  if(response && response.data && response.data.data){
					  $scope.item.paymentAmount = response.data.data.totalPrice;
				  }
			  });
		  }
	  };
	  
	  
	  
	  $scope.commitBuy = function (){
		  //TODO 提交逻辑
		  $scope.warnMsg = '';
		  $scope.checkBtn = true;
		  eayunHttp.post('cloud/snapshot/buySnapshot.do',$scope.item).then(function (response){
			  if(response.data.respCode == '000000'){
				  //后付费订单提交成功
				   if ('2' == $scope.item.payType){
					 //后付费订单提交成功  调到订单列表页
					 $state.go('app.order.list');
				  }
			  }
			  //TODO 订单提交失败
			  else if(response.data.respCode == '010110'){
				  $scope.checkBtn = false;
				  if(response.data.message == 'OUT_OF_QUOTA'){
					  $scope.queryPrjQuota();
				  }
				  else if(response.data.message == 'CHANGE_OF_BILLINGFACTORY'){
					  $scope.reCalculateBillingFactory();
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
				  else if(response.data.message == 'NOT_SUFFICIENT_FUNDS'){
					  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
						  $scope.payAfpterPayment = response.data;
						  $scope.errorMsg = "您的账户余额不足"+$scope.payAfpterPayment+"元，请充值后操作";
					  });
				  }
				  
			  }else{
				  if ('2' == $scope.item.payType){
					   $state.go('app.order.list');
				   }
			  }
			  
	       });
	  };
	  
	  
	  $scope.initValue();
	  
  }).controller('DebindVolume', function ($scope,volume,eayunHttp,eayunModal,$modalInstance) {
	    $scope.model= angular.copy(volume,{});
	    
	    $scope.isSure=false;
		
		$scope.cancel = function (){
			$modalInstance.dismiss();
		};

		$scope.commit = function () {
		    $modalInstance.close($scope.model);
		};
	
  });