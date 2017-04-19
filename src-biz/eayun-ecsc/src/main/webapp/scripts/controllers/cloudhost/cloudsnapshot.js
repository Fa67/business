'use strict';
angular.module('eayunApp.controllers').config( function($stateProvider, $urlRouterProvider) {
			$stateProvider.state('app.cloud.cloudhost.volume.snapshot', {
		      url: '/snapShotList',
		      templateUrl: 'views/cloudhost/snapshot/snapshotmng.html',
		      controller: 'cloudHostSnapshotList'
			}).state('tobuy.volumebysnap',{
		    	url: '/snapshot/:payType/:snapId/:fromVolId',
		        templateUrl: 'views/cloudhost/snapshot/buyvolbysnap.html',
		        controller: 'BuyVolumeBySnapController'
		    }).state('tobuy.confirmvol',{
		    	url: '/confirmvol',
		        templateUrl: 'views/cloudhost/snapshot/orderconfirm.html',
		        controller: 'ConfirmVolOrderSnapController'
		    });
		}).controller('cloudHostSnapshotList',function($rootScope,$scope,$state,$timeout,$sce,eayunHttp,eayunModal,toast,powerService,eayunStorage){
			  var navLists=[];
			  navLists.push({route:'app.cloud.cloudhost.volume.list',name:'云硬盘'});
			  $rootScope.navList(navLists,'云硬盘备份');
			  
			//查询列表
			$scope.myTable = {
				      source: 'cloud/snapshot/getSnapshotList.do',
				      api : {},
				      getParams: function () {
				        return {
				        	prjId : sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '',
						    dcId : sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
				        	name :  $scope.name || '',
				        	isDeleted:'0'
				        };
				      }
				    
				    };
			
			 //监视器[监视数据中心、项目id变化]
			  $scope.$watch('model.projectvoe' , function(newVal,oldVal){
			    	if(newVal !== oldVal){
			    		$scope.myTable.api.draw();
			    	}
			    });
			  
			    
			    
			    /**
			     * 云硬盘备份状态 显示
			     */
			    $scope.getSnapStatus =function (model){
			    	
			    	if('1'==model.chargeState&&model.snapStatus!='DELETING'){
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
			     * 查询
			     */
			    $scope.search = function(_item,_event){
			    	$scope.name = _item;
			    	$scope.myTable.api.draw();
			    };
			    
			    
			  //权限控制
				powerService.powerRoutesList().then(function(powerList){
					  $scope.buttonPower = {
						isEdit : powerService.isPower('snap_edit'),//更新备份
						isAddDisk : powerService.isPower('snap_adddisk'),//基于备份创建云硬盘
						isTag : powerService.isPower('snap_tag'),//标签
						delSnap : powerService.isPower('snap_delete'),//删除备份
						rollBackVol:powerService.isPower('snap_rollback'),//回滚云硬盘
					 };
				  }); 
			    
			    
			  
			  $scope.$watch("myTable.result",function (newVal,oldVal){
			    	if(newVal !== oldVal){
			    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
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
			    	$scope.myTable.api.refresh();
			    };
			  
			    
			    
			    
			  //名称查询
			  $scope.querySnapshot = function(){
				  $scope.myTable.api.draw();
			  };
			  
			  
			  
			  /**
			     * Enter查询事件
			     */
			   /* $(function () {
			        document.onkeydown = function (event) {
			            var e = event || window.event || arguments.callee.caller.arguments[0];
			            if(!$scope.checkUser()){
			            	return ;
			            }
			            if (e && e.keyCode == 13) {
			          	  $scope.querySnapshot();
			            }
			        };
			    });*/
			
			    
			 //点击云硬盘字段跳转至云硬盘详情
			 $scope.goToVolume = function(snapshot){
				 if(null==snapshot.volId||'null'==snapshot.volId||''==snapshot.volId){
					 return;
				 }else{
					 $state.go('app.cloud.cloudhost.volumedetail',{"detailType":'volume',"dcId":snapshot.dcId,"prjId":snapshot.prjId,"volId":snapshot.volId}); // 跳转后的URL; 
				 }
				 
			 }; 

			 
			 //备份创建云硬盘
			 $scope.AddVolume = function (item) {
				 $state.go('tobuy.volumebysnap',{'payType':'1','snapId':item.snapId});
			 }
				
				
			
			 
			 
			 
			 //回滚云硬盘
		    $scope.rollBack = function (cloudSnapshot) {
		    	$scope.notice='';
		    	 eayunHttp.post('cloud/volume/getVolumeById.do',{dcId:cloudSnapshot.dcId,prjId:cloudSnapshot.prjId,volId:cloudSnapshot.volId}).then(function(response){
		  		  var volume=response.data;
		  		  if(volume.isDeleted=='1'||volume.isDeleted=='2'){
		  			$scope.notice="源硬盘已删除，无法回滚";
		  		  }else if(volume.chargeState=='1'){
		  			$scope.notice="云硬盘"+(volume.volName.length>20?volume.volName.substring(0,19)+"...":volume.volName)+"已欠费，请充值后操作";
		  			
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
		  			backdrop:'static',
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
		 	    		 $scope.myTable.api.draw();
		              });

		 	      }, function () {
		 	    	 $scope.myTable.api.draw();
		 	      });
		  		  
		      });

		    };
			 
			    
	
			    
			//删除云硬盘备份
		   $scope.deleteSnap = function (cloudSnapshot) {
		    	if('RESTORING'==cloudSnapshot.snapStatus){
		    		eayunModal.warning("备份占用中，请稍后重试");
		    		return;
		    	}
		    	if('CREATING'==cloudSnapshot.snapStatus){
		    		eayunModal.warning("备份创建中，请稍后重试");
		    		return;
		    	}
		    	if('DELETING'==cloudSnapshot.snapStatus){
		    		eayunModal.warning("备份删除中，请稍后重试");
		    		return;
		    	}
			      var result = eayunModal.open({
			    	backdrop:'static',
			        templateUrl: 'views/cloudhost/snapshot/deletesnapshot.html',
			        controller: 'DeleteSnapshot',
			        resolve: {
			      	  snapshot:function(){
			      		  return cloudSnapshot;
			      	  }
			          
			        }
			      }).result;
			      result.then(function (value){
			    	  eayunHttp.post('cloud/snapshot/deleteSnap.do',value).then(function(response){
			    		  if(response.data!=null&&response.data==true){
				        		toast.success('删除云硬盘备份成功',1000);
				        	}
							$scope.myTable.api.draw();
		              });

			      }, function () {
			    	  $scope.myTable.api.draw();
			      });
				      
			};
			   
			    
			    
			    
			    
			  //编辑云硬盘备份
				$scope.updateSnapshot = function (item) {
				      var result = eayunModal.open({
				    	backdrop:'static', 
				        templateUrl: 'views/cloudhost/snapshot/editsnapshot.html',
				        controller: 'UpdateSnapshot',
				        resolve: {
				      	snapshot:function(){
				      		   return item;
				      	  }
				        }
				      
				      }).result;
				      result.then(function (value){
				    	  eayunHttp.post('cloud/snapshot/updateSnapshot.do',value).then(function(response){
				    		  if(null!=response.data&&response.data==true){
				    			  toast.success('编辑备份'+(value.snapName.length>10?value.snapName.substring(0,9)+'...':value.snapName)+'成功',1000); 
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
							$scope.myTable.api.draw();
					},function () {
							$scope.myTable.api.draw();
					});
				 };
			    
					
					 //备份详情
				    $scope.detail=function(cloudSnapshot){
				    	 var result = eayunModal.dialog({
						    	showBtn: false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
						        title: '备份详情',
						        width: '650px',
						        templateUrl: 'views/cloudhost/snapshot/snapshotdetail.html',
						        controller: 'DetailSnapshot',
						        resolve: {
						        	snapshot:function(){
						        	  return cloudSnapshot;
						        	},
						        	tags:function(){
				    	 				return eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'diskSnapshot',resId: cloudSnapshot.snapId}).then(function(response){
				    	 					return  response.data;
				    	 				});
				    	 			}
						        }
						      });
						      result.then(function (value){
						    	 
						      }, function () {
						        
						      });
				    	};		
			    
			   
}).controller('UpdateSnapshot', function ($scope,snapshot,eayunHttp,eayunModal,$modalInstance) {
		$scope.model= angular.copy(snapshot,{});
		$scope.model.snapDescription=('null'!=$scope.model.snapDescription&&null!=$scope.model.snapDescription)?$scope.model.snapDescription:'';
		$scope.checkSnapName = true;
		
		$scope.cancel = function (){
			  $modalInstance.dismiss();
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
				  cloudSnapshot.snapId=$scope.model.snapId;
				  cloudSnapshot.dcId= $scope.model.dcId;
				  cloudSnapshot.prjId = $scope.model.prjId;
				  cloudSnapshot.snapName = $scope.model.snapName;
				  
				  eayunHttp.post('cloud/snapshot/getSnapByName.do',cloudSnapshot).then(function (response){
						  $scope.checkSnapName = response.data;
				  });
			  }
		  };
		
		$scope.commit = function () {
		      $modalInstance.close($scope.model);
		  };
		
		
		}).controller('DetailSnapshot', function ($scope,snapshot,tags,eayunHttp,eayunModal) {
			$scope.model= snapshot;
			$scope.tags=tags;
			//pop框方法
			$scope.openPopBox = function(obj){
				if(obj.type == 'tagName'){
					$scope.tagShow = true;
				}
				if(obj.type == 'volName'){
					$scope.volNameShow = true;
				}
				if(obj.type == 'snapDesc'){
					$scope.snapDescShow = true;
				}
				$scope.description = obj.value;
			};
			$scope.closePopBox = function(type){
				if(type == 'tagName'){
					$scope.tagShow = false;
				}
				if(type == 'volName'){
					$scope.volNameShow = false;
				}
				if(type == 'snapDesc'){
					$scope.snapDescShow = false;
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
			
			
			
			/**
		    * 云硬盘状态 显示
		    */
			$scope.getSnapStatus =function (model){
		    	$scope.snapStatusClass = '';
		    	
		    	if('1'==model.chargeState){
		    		$scope.snapStatusClass = 'ey-square-disable';
		    	}else if(model.snapStatus&&model.snapStatus=='AVAILABLE'){
		    		$scope.snapStatusClass = 'ey-square-right';
		    	}
				else if(model.snapStatus=='ERROR'){
					$scope.snapStatusClass = 'ey-square-error';
				}
				else{
					$scope.snapStatusClass = 'ey-square-warning';
				}
		    };
		    
			$scope.getSnapStatus($scope.model);
			
			  $scope.commit = function () {
			    
			       $scope.ok($scope.model);
			     
			   };

		}).controller('DeleteSnapshot', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage,snapshot,$modalInstance) {
			  $scope.model=snapshot;
			  $scope.isDeleted=false;
			  
			  if('0'!=$scope.model.chargeState){
				  $scope.isDeleted=true;
				  $scope.isShow=true;
			  }
			  
			  $scope.cancel = function (){
				  $modalInstance.dismiss();
			  };
			  
			  $scope.commit= function (){
				  if($scope.isDeleted){
					  $scope.model.isDeleted='1';
				  }else{
					  $scope.model.isDeleted='2';
				  }
				  $modalInstance.close($scope.model);
			  };
			  
		}).controller('RollBackVolume', function ($scope,snapshot,eayunHttp,eayunModal,$modalInstance) {
			$scope.model= angular.copy(snapshot,{});
			
			$scope.cancel = function (){
				  $modalInstance.dismiss();
			  };
	
			$scope.commit = function () {
			     $modalInstance.close($scope.model);
			};
			
			
	  }).controller('BuyVolumeBySnapController', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage){
		  $scope.model={};
		  $scope.model.volType='1';
		  $scope.typeSure=false;
		  $scope.init = function(){
			  var orderNo = eayunStorage.get('reorder_volume_orderNo');
			  if(orderNo){
				  //TODO 重新下单,查看数据库的具体订单信息
			      //$scope.model = response.data.data;
			  }
			  else{
				  var item = eayunStorage.get('order_back_snapvolume');
				  eayunStorage.delete('order_back_snapvolume');
				  
				  if(!item){
					  $scope.item={};
					  $scope.fromVolId='';
					  var snapId=$stateParams.snapId;
					  $scope.fromVolId=$stateParams.fromVolId;
					  if(null!=snapId){
						  eayunHttp.post('cloud/snapshot/getSnapshotById.do',snapId).then(function(response){
							  
							  $scope.item=response.data.data;
							  if(null==$scope.item||''==$scope.item||'null'==$scope.item||undefined==$scope.item){
								  $state.go('app.cloud.cloudhost.volume.snapshot');
							  }else{
							  $scope.model.payType = $stateParams.payType;
							  if(null==$scope.model.payType||''==$scope.model.payType||'null'==$scope.model.payType){
								  $scope.model.payType='1';
							  }
							  $scope.model.dcId=$scope.item.dcId;
							  $scope.model.dcName=$scope.item.dcName;
							  $scope.model.prjId=$scope.item.prjId;
							  $scope.model.fromSnapId=$scope.item.snapId;
							  $scope.model.volNumber=1;
							  $scope.model.volSize=$scope.item.snapSize;
							  $scope.model.diskFrom='snapshot';
							  $scope.model.snapName=$scope.item.snapName;
							  $scope.checkVolName = true;
							  $scope.isNSF=false;
							  $scope.initValue();
							  }
							  
					    });
					  }
				  }
				  else{
					  //TODO 返回修改配置
					  $scope.model = angular.copy(item);
					  $scope.volId=$scope.model.volId;
					  $scope.checkVolName = true;
					  $scope.isNSF=false;
					  $scope.checkVolNameExist();
					  $scope.fromVolId='';
					  $scope.fromVolId=$scope.model.fromVolId;
					  
					  if($scope.model.payType == '1'){
						  $scope.queryBuyCycle();
						  $scope.model.buyCycle = $scope.model.buyCycle;
					  }
					  $scope.initValue();
				  }
			  }
		  };
		  
		  
		 
		  
		  $scope.initValue = function(){
			  if('1'==$scope.model.payType){
				  $scope.buyCycleType();
			  }
			  
			  if('2'==$scope.model.payType){
				  $scope.queryAccount();
			  }
			  
			  $scope.getVolumeTypes($scope.model.dcId);
			  $scope.queryPrjQuota();
			  //$scope.calcBillingFactor();
			 
		  };
		  
		  //返回详情页
		  $scope.goToDetail=function(){
			  $state.go('app.cloud.cloudhost.volumedetail',{"detailType":'volume',"dcId":$scope.model.dcId,"prjId":$scope.model.prjId,"volId":$scope.fromVolId}); // 跳转后的URL;
		  };
		  
		  
		  
		  
		  
		  /**
		   * 选择云硬盘类型
		   */
		  $scope.selectVolType=function(data){
			  $scope.model.volType=data.volumeType;
			  $scope.model.volTypeId=data.typeId;
			  $scope.model.volumeTypeAs=data.volumeTypeAs;
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
		   * 购买类型
		   */
		  $scope.buyVol = function(type){
			  $scope.model.payType = type;
			  $scope.checkVolName = true;
			  $scope.checkVolSize=false;
			  $scope.isNSF=false;
			  $scope.checkVolNameExist();
			  $scope.initValue();
		  };
		  
		 
		  
		  //资源计费查询
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
		  
		   //查看账户余额
		  $scope.queryAccount = function (){
			  eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function(response){
				  $scope.account = response.data.data;
				  eayunHttp.post('sysdatatree/getbuycondition.do').then(function(response){
					  $scope.payAfpterPayment = response.data;
				  });
			  });
		  };
		  

		
		   //校验云硬盘重名（项目维度、云硬盘数量）
		  $scope.checkVolNameExist = function (){
			  $scope.checkVolName = true;
			  var volNumber=$scope.model.volNumber;
			  if(volNumber==""||volNumber==null||volNumber==undefined){
				  volNumber = 1;
			  }
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
			  $scope.QuotaMsg='';

			  if(prj&&num){
				  if(num>(prj.diskCount - prj.diskCountUse)){
					  $scope.countQuotaMsg = $scope.countQuotaMsg+'云硬盘数量';
				  }

				  if(disk&&((num*disk)>(prj.diskCapacity - prj.usedDiskCapacity))){
					  $scope.volCapacityQuotaMsg = $scope.volCapacityQuotaMsg+'云硬盘容量';
				  }
			  }
			  
			  if('' != $scope.countQuotaMsg&&''==$scope.volCapacityQuotaMsg){
				  $scope.QuotaMsg = $scope.countQuotaMsg.substr(0,$scope.countQuotaMsg.length)+'配额不足';
			  }
			  if('' != $scope.volCapacityQuotaMsg&&''==$scope.countQuotaMsg){
				  $scope.QuotaMsg = $scope.volCapacityQuotaMsg.substr(0,$scope.volCapacityQuotaMsg.length)+'配额不足';
			  }
			  if('' != $scope.countQuotaMsg&&'' != $scope.volCapacityQuotaMsg){
				  $scope.QuotaMsg=$scope.countQuotaMsg.substr(0,$scope.countQuotaMsg.length)+'、'+$scope.volCapacityQuotaMsg.substr(0,$scope.volCapacityQuotaMsg.length)+'配额不足';
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
		   * 计算购买周期
		   */
		  $scope.calcBuyCycle = function(){
			  $scope.calcBillingFactor();
		  };
		  
		  /**
		   * 购买云硬盘，跳转到订单的确认页面
		   */
		  $scope.commitBuyVolume = function (){
			  //TODO 提交逻辑
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
						  data.fromVolId=$scope.fromVolId;
						  data.dcId = $scope.model.dcId;
						  data.dcName = $scope.model.dcName;
						  data.prjId = $scope.model.prjId;
						  data.volNumber = $scope.model.volNumber;
						  data.volSize=$scope.model.volSize;
						  data.volName=$scope.model.volName;
						  data.volDescription=$scope.model.volDescription;
						  data.diskFrom=$scope.model.diskFrom;
						  data.fromSnapId=$scope.model.fromSnapId;
						  data.snapName=$scope.model.snapName;
						  data.volType=$scope.model.volType;
						  data.volTypeId=$scope.model.volTypeId;
						  data.volumeTypeAs=$scope.model.volumeTypeAs;
						  data.orderType = '0';
						  data.payType = $scope.model.payType;
						  data.paymentAmount = $scope.priceDetails.totalPrice;
						  if('1'==$scope.model.payType){
							  data.prodName='云硬盘-包年包月';
							  data.cycleType = $scope.model.cycleType;
							  data.buyCycle = $scope.model.buyCycle;
						  }else{
							  data.prodName='云硬盘-按需付费';
						  }

						  eayunStorage.set('order_confirm_purchasesvolume',data);
						  $state.go('tobuy.confirmvol');
						 
					  });
				  });
			  }
			  else{
				  var data = {}
				  data.fromVolId=$scope.fromVolId;
				  data.dcId = $scope.model.dcId;
				  data.dcName = $scope.model.dcName;
				  data.prjId = $scope.model.prjId;
				  data.volNumber = $scope.model.volNumber;
				  data.volSize=$scope.model.volSize;
				  data.volName=$scope.model.volName;
				  data.volDescription=$scope.model.volDescription;
				  data.diskFrom=$scope.model.diskFrom;
				  data.fromSnapId=$scope.model.fromSnapId;
				  data.snapName=$scope.model.snapName;
				  data.volType=$scope.model.volType;
				  data.volTypeId=$scope.model.volTypeId;
				  data.volumeTypeAs=$scope.model.volumeTypeAs;
				  data.orderType = '0';
				  data.payType = $scope.model.payType;
				  data.paymentAmount = $scope.priceDetails.totalPrice;
				  if('1'==$scope.model.payType){
					  data.prodName='云硬盘-包年包月';
					  data.cycleType = $scope.model.cycleType;
					  data.buyCycle = $scope.model.buyCycle;
				  }else{
					  data.prodName='云硬盘-按需计费';
				  }

				  eayunStorage.set('order_confirm_purchasesvolume',data);
				  $state.go('tobuy.confirmvol');
			  }
		  };
		  
		  $scope.init();
		  
	  }).controller('ConfirmVolOrderSnapController', function ($rootScope,$scope, eayunModal,eayunHttp,toast,$state,$stateParams,eayunStorage,eayunMath) {
  
		  $scope.model={};
		  $scope.item = eayunStorage.get('order_confirm_purchasesvolume');
		  
		  console.info($scope.item);

		  if(!$scope.item){
			  $state.go('app.cloud.cloudhost.volume.snapshot');
		  }else{
			  if('0'== $scope.item.orderType){
				  $scope.title = '创建云硬盘';
			  }
			   
		  } 
		  
		  
		  /**
		   * 返回修改配置
		   */
		  $scope.backToVolSnap = function(){
			  eayunStorage.set('order_back_snapvolume',$scope.item);
			  if(null!=$scope.item.fromVolId&&''!=$scope.item.fromVolId&&'null'!=$scope.item.fromVolId){
				  $state.go('tobuy.volumebysnap',{payType:$scope.item.payType,snapId:$scope.item.fromSnapId,fromVolId:$scope.item.fromVolId});
			  }else{
				  $state.go('tobuy.volumebysnap',{payType:$scope.item.payType,snapId:$scope.item.fromSnapId});
			  }
			  
		  };
		  
		  
		  $scope.goToDetail=function(){
			  $state.go('app.cloud.cloudhost.volumedetail',{"detailType":'volume',"dcId":$scope.item.dcId,"prjId":$scope.item.prjId,"volId":$scope.item.fromVolId}); // 跳转后的URL;
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
			  }else{
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
			  data.dcId = $scope.item.dcId;
			  data.payType = $scope.item.payType;
			  if('1'==$scope.item.volType){
				  data.dataDiskOrdinary=$scope.item.volSize;
			  }else if('2'==$scope.item.volType){
				  data.dataDiskBetter=$scope.item.volSize;
			  }else if('3'==$scope.item.volType){
				  data.dataDiskBest=$scope.item.volSize;
			  }else{
				  data.dataDiskCapacity = $scope.item.volSize; 
			  }
			  data.number = $scope.item.volNumber;
			  data.cycleCount = 1;
			  var url = 'billing/factor/getPriceDetails.do';
			  if(data.payType == '1'){
				  data.cycleCount = $scope.item.buyCycle;
			  }
			  eayunHttp.post(url,data).then(function (response){
				  if(response && response.data && response.data.data){
					  $scope.item.paymentAmount = response.data.data.totalPrice;
					  $scope.useAccountPay();
				  }
			  });
			  
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
			  eayunHttp.post(url,data).then(function(response){
				  if(response && response.data){
					  //订单支付成功
					  if(response.data.respCode == '000000'){
						  if('1' == data.payType){
							  if(!data.thirdPartPayment){
								  $state.go('pay.result', {subject:data.prodName});
							  }
							  else{
								  //TODO 跳转到订单完成界面
								  
								  if(null!=data.fromVolId&&''!=data.fromVolId){
									  var routeUrl = "tobuy.volumebysnap({'payType':'"+$scope.item.payType+"','snapId':'"+$scope.item.fromSnapId+"','fromVolId':'"+$scope.item.fromVolId+"'})";
									  var routeUrl1="app.cloud.cloudhost.volumedetail({'dcId':'"
										  + $scope.item.dcId 
										  + "','prjId':'" 
										  + $scope.item.prjId 
										  + "','volId':'" 
										  + $scope.item.fromVolId+"'})";
									  var orderPayNavList = [{route:'app.cloud.cloudhost.volume',name:'云硬盘'},{route:routeUrl1,name:'云硬盘详情'},
										                         {route:routeUrl,name:'创建云硬盘'}];
				                      eayunStorage.persist("orderPayNavList",orderPayNavList);
				                      eayunStorage.persist("payOrdersNo",response.data.orderNo);
				    				  $state.go('pay.order');
								  }else{
									  var routeUrl = "tobuy.volumebysnap({'payType':'"+$scope.item.payType+"','snapId':'"+$scope.item.fromSnapId+"'})";
									  var orderPayNavList = [{route:'app.cloud.cloudhost.volume',name:'云硬盘'},{route:'app.cloud.cloudhost.volume.snapshot',name:'云硬盘备份'},
										                         {route:routeUrl,name:'创建云硬盘'}];
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

	});
