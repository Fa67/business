'use strict';
angular.module('eayunApp.controllers').config( function($stateProvider, $urlRouterProvider) {
	  $urlRouterProvider.when('/app/cloud/cloudhost/image', '/app/cloud/cloudhost/image/publicImageList');
	  $stateProvider.state('app.cloud.cloudhost.image', {
	      url: '/image',
	      templateUrl: 'views/cloudhost/image/main.html'
	  }).state('app.cloud.cloudhost.image.customerimage', {
		      url: '/personimageList',
		      templateUrl: 'views/cloudhost/image/imagemng.html',
		      controller: 'cloudHostImageList'
	  }).state('app.cloud.cloudhost.image.publicimage', {
	      url: '/publicImageList',
	      templateUrl: 'views/cloudhost/image/publicimagemng.html',
	      controller: 'cloudHostPublicImageList'
      }).state('app.cloud.cloudhost.image.marketimage', {
	      url: '/marketImageList',
	      templateUrl: 'views/cloudhost/image/marketimagemng.html',
	      controller: 'cloudHostMarketImageList'
      }).state('app.cloud.cloudhost.marketimagedetail', {
	      url: '/marketDetail/:imageId',
	      templateUrl: 'views/cloudhost/image/marketimagedetail.html',
	      controller: 'marketImageDetail'
	});
   }).controller('cloudHostImageList',function($rootScope,$scope,$state,eayunHttp,eayunModal,$timeout,toast,powerService){
			var list=[];
			  $rootScope.navList(list,'镜像');
			 //查询列表
			$scope.myTable = {
				      source: 'cloud/image/getImageList.do',
				      api : {},
				      getParams: function () {
				        return {
				        	prjId : sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '',
						    dcId : sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
				        	name : $scope.imageName || ''
				        };
				      }
				    
				    };
			/**
			  * 镜像状态 显示
			  */
		    $scope.getImageStatus =function (model){
		    	$scope.imageStatusClass = '';
				if(model.imageStatus&&model.imageStatus=='ACTIVE'){
					return 'ey-square-right';
				}  
				else if(model.imageStatus=='ERROR'){
					return 'ey-square-disable';
				}
				else if(model.imageStatus=='SAVING'||model.imageStatus=='QUEUED'||model.imageStatus=='DELETING'){
					return 'ey-square-warning';
				}
				else if(model.imageStatus=='DELETED'){
					return 'ey-square-error';
				}
		    };
			
			//权限控制
			powerService.powerRoutesList().then(function(powerList){
				  $scope.buttonPower = {
					isEdit : powerService.isPower('mirror_edit'),//编辑镜像
					isTag : powerService.isPower('mirror_tag'),//标签
					delImage : powerService.isPower('mirror_delete'),//删除镜像
				 };
			  }); 
			
			
			 //监视器[监视数据中心、项目id变化]
			  $scope.$watch('model.projectvoe' , function(newVal,oldVal){
			    	if(newVal !== oldVal){
			    		$scope.myTable.api.draw();
			    	}
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
			    				var status=$scope.myTable.result[i].imageStatus.toString().toUpperCase();
			    				if("ACTIVE"!=status&&"ERROR"!=status&&"DELETED"!=status){
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
			  $scope.search = function(){
				  $scope.myTable.api.draw();
			  };
			  
			  //删除镜像
			  $scope.delImage=function(cloudImage){
				  
				  eayunModal.confirm('确定要删除镜像'+cloudImage.imageName+'?').then(function () {
					  if(cloudImage.vmNum>0){
						  eayunModal.warning('镜像有关联的云主机，不允许删除');
					  }else if(cloudImage.imageStatus=='DELETING'){
						  eayunModal.warning('镜像正在删除中，请稍后');
					  }else{
						  eayunHttp.post("cloud/image/deleteImage.do",cloudImage).then(function(response){
								if(null!=response.data&&response.data==true){
									toast.success('镜像正在删除中',1000);
					        	}
								$scope.myTable.api.draw();
							}); 
					  }
						
					});
			  };
			  
			  
			  //编辑镜像
			    $scope.updateImage = function (cloudImage) {
				      var result = eayunModal.open({
				        title: '编辑镜像',
				        width: '600px',
				        templateUrl: 'views/cloudhost/image/editimage.html',
				        controller: 'UpdateImage',
				        resolve: {
				          cloudImage: function () {
				            return cloudImage;
				          }
				          
				        }
				      });
				      result.result.then(function (value){
				    	 eayunHttp.post('cloud/image/updateImage.do',value).then(function(response){
				    		 if(null!=response.data&&response.data==true){
				    			 toast.success('镜像'+(value.imageName.length>10?value.imageName.substring(0,9)+'...':value.imageName)+'修改成功',1000); 
				    		 }
				    		  $scope.myTable.api.draw();
			              });
				      }, function () {
				        //console.info('取消');
				      });
				    };
				    
				    //镜像详情
				    $scope.detail=function(cloudImage){
				    	 var result = eayunModal.open({
						        title: '镜像详情',
						        width: '650px',
						        templateUrl: 'views/cloudhost/image/imagedetail.html',
						        controller: 'DetailImage',
						        resolve: {
						        	Image:function(){
						        	  return cloudImage;
						        	},
				    	 			tags:function(){
				    	 				return eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'privateImage',resId: cloudImage.imageId}).then(function(response){
				    	 					return  response.data;
				    	 				});
				    	 			}
						        }
						      });
						      result.result.then(function (value){
						    	 
						      }, function () {
						        //console.info('取消');
						      });
				    	};		
				    
				    /*标签*/
					$scope.tagResource = function(resType, resId){
						var result=eayunModal.open({
						    title: '标记资源',
						    width: '600px',
						    height: '400px',
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
							$scope.myTable.api.draw();
					},function () {
							$scope.myTable.api.draw();
					});
				 };
				    
				    
				    
}).controller('DetailImage', function ($scope,Image,tags,eayunHttp,eayunModal,$state, $modalInstance) {
    $scope.cancel = function () {
        $modalInstance.dismiss();
    };

	$scope.model=Image;
	$scope.tags=tags;
	
	
	//pop框方法
	$scope.openPopBox = function(image){
		if(image.type == 'imageName'){
			$scope.nameShow = true;
		}
		if(image.type == 'tagName'){
			$scope.tagShow = true;
		}
		if(image.type == 'imageDesc'){
			$scope.imageDescShow = true;
		}
		$scope.description = image.value;
	};
	$scope.closePopBox = function(type){
		if(type == 'imageName'){
			$scope.nameShow = false;
		}
		if(type == 'tagName'){
			$scope.tagShow = false;
		}
		if(type == 'imageDesc'){
			$scope.imageDescShow = false;
		}
	};
	
	
	/**
	  * 镜像状态 显示
	  */
   $scope.getImageStatus = function (model){
   		$scope.imageStatusClass = '';
		if(model.imageStatus&&model.imageStatus=='ACTIVE'){
			$scope.imageStatusClass = 'ey-square-right';
		}  
		else if(model.imageStatus=='ERROR'){
			$scope.imageStatusClass = 'ey-square-disable';
		}
		else if(model.imageStatus=='SAVING'||model.imageStatus=='QUEUED'||model.imageStatus=='DELETING'){
			$scope.imageStatusClass = 'ey-square-warning';
		}
		else if(model.imageStatus=='DELETED'){
			$scope.imageStatusClass = 'ey-square-error';
		}
   };
   $scope.getImageStatus($scope.model);
	$scope.commit = function () {
	    eayunModal.confirm('确认保存？').then(function () {
	      $scope.ok($scope.model);
	    }, function () {
	      //console.info('取消');
	    });
	  };
	

	
}).controller('UpdateImage', function ($scope,cloudImage,eayunHttp,eayunModal, $modalInstance) {
    $scope.cancel = function () {
        $modalInstance.dismiss();
    };

    $scope.commit = function () {
        $modalInstance.close($scope.model);
    };

	$scope.model= angular.copy(cloudImage,{});
	$scope.model.imageDescription=(null!=$scope.model.imageDescription&&'null'!=$scope.model.imageDescription&&''!=$scope.model.imageDescription)?$scope.model.imageDescription:'';
	
	//校验名称格式和唯一性
	  $scope.checkImageName = function (value) {
		  var nameTest=/^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  $scope.flag=false;
		  if(value.match(nameTest)){
			$scope.model.imageName=value;
			  return eayunHttp.post('cloud/image/getImageByName.do',$scope.model).then(function(response){
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
		

}).controller('cloudHostPublicImageList',function($rootScope,$scope,$state,eayunHttp,eayunModal,$timeout,toast,powerService){
	var list=[];
	  $rootScope.navList(list,'镜像');
	 //查询列表
	$scope.myTable = {
		      source: 'cloud/image/getPublicImageList.do',
		      api : {},
		      getParams: function () {
		        return {
				    dcId : sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
		        	name : $scope.imageName || '',
		        	isUse :$scope.isUse ||'',
		        	sysType : $scope.sysType || '',
		        };
		      }
      };
	
	

	
	/**
	  * 镜像状态 显示
	  */
  $scope.getImageStatus =function (model){
  	$scope.imageStatusClass = '';
		if(model.imageStatus&&model.imageStatus=='ACTIVE'&&model.isUse=='1'){
			return 'ey-square-right';
		}  
		else if(model.imageStatus=='ERROR'||model.isUse=='2'||model.isUse=='0'){
			return 'ey-square-disable';
		}
		else if(model.imageStatus=='SAVING'||model.imageStatus=='QUEUED'||model.imageStatus=='DELETING'){
			return 'ey-square-warning';
		}
		else if(model.imageStatus=='DELETED'){
			return 'ey-square-error';
		}
  };
	
  
  $scope.init=function(){
	  $scope.statusList = [{
	  		'nodeNameEn':'',
	  		'nodeName':'全部状态'
	  	},{
	  		'nodeNameEn':'1',
	  		'nodeName':'正常'
	  	},{
	  		'nodeNameEn':'2',
	  		'nodeName':'已停用'
	  	}];  
	  
	    
	  $scope.osList=[{
			'nodeId':'',
			'nodeName':'系统类型（全部）'
		}];
		eayunHttp.post('cloud/image/getostypelist.do').then(function(response){
			$scope.osList=  response.data.data;
			$scope.osList.unshift({
				'nodeId':'',
				'nodeName':'系统类型（全部）'
			});
			
		});  
	  
	  
  };

  
  

	
  
  /**
    * 选择云主机状态
    */
  $scope.selectImageStatus = function(item,event){
   	$scope.isUse = null;
   	$scope.isUse = item.nodeNameEn;
   	$scope.myTable.api.draw();
   	
  };

  
  /**
   * 选择云主机系统类型
   */
 $scope.selectImageOsType = function(item,event){
  	$scope.sysType = null;
  	$scope.sysType = item.nodeId;
  	$scope.myTable.api.draw();
  	
 };

  
	
	 //监视器[监视数据中心、项目id变化]
	  $scope.$watch('model.projectvoe' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.myTable.api.draw();
	    	}
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
	    				var status=$scope.myTable.result[i].imageStatus.toString().toUpperCase();
	    				if("ACTIVE"!=status&&"ERROR"!=status&&"DELETED"!=status){
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
	  $scope.search = function(){
		  $scope.myTable.api.draw();
	  };
	  
  
    //公共镜像详情
    $scope.detail=function(cloudImage){
    	 var result = eayunModal.open({
		        title: '镜像详情',
		        width: '650px',
		        templateUrl: 'views/cloudhost/image/publicimagedetail.html',
		        controller: 'PublicImageDetail',
		        resolve: {
		        	Image:function(){
		        	  return cloudImage;
		        	}
		        }
		      });
		      result.result.then(function (value){
		    	 
		      }, function () {
		        //console.info('取消');
		      });
    };		
		    
	
		    
	$scope.init();	    
		    
}).controller('PublicImageDetail', function ($scope,Image,eayunHttp,eayunModal,$state, $modalInstance) {
    $scope.cancel = function () {
        $modalInstance.dismiss();
    };

	$scope.model=Image;
	
	
	//pop框方法
	$scope.openPopBox = function(image){
		if(image.type == 'imageName'){
			$scope.nameShow = true;
		}
		if(image.type == 'sysDetail'){
			$scope.sysDetailShow = true;
		}
		if(image.type == 'imageDesc'){
			$scope.imageDescShow = true;
		}
		$scope.description = image.value;
	};
	$scope.closePopBox = function(type){
		if(type == 'imageName'){
			$scope.nameShow = false;
		}
		if(type == 'sysDetail'){
			$scope.sysDetailShow = false;
		}
		if(type == 'imageDesc'){
			$scope.imageDescShow = false;
		}
	};
	
	
	/**
	  * 镜像状态 显示
	  */
   $scope.getImageStatus = function (model){
   		$scope.imageStatusClass = '';
		if(model.imageStatus&&model.imageStatus=='ACTIVE'&&model.isUse=='1'){
			$scope.imageStatusClass = 'ey-square-right';
		}  
		else if(model.imageStatus=='ERROR'||model.isUse=='2'||model.isUse=='0'){
			$scope.imageStatusClass = 'ey-square-disable';
		}
		else if(model.imageStatus=='SAVING'||model.imageStatus=='QUEUED'||model.imageStatus=='DELETING'){
			$scope.imageStatusClass = 'ey-square-warning';
		}
		else if(model.imageStatus=='DELETED'){
			$scope.imageStatusClass = 'ey-square-error';
		}
   };
   $scope.getImageStatus($scope.model);
	$scope.commit = function () {
	      $scope.ok($scope.model);
	 };
	

	
}).controller('cloudHostMarketImageList',function($rootScope,$scope,$state,eayunHttp,eayunModal,$timeout,toast,powerService){
	var list=[];
	  $rootScope.navList(list,'镜像');
	 //查询列表
	$scope.myTable = {
		      source: 'cloud/image/getMarketImageList.do',
		      api : {},
		      getParams: function () {
		        return {
				    dcId : sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
		        	name : $scope.imageName || '',
		        	isUse :$scope.isUse ||'',
		        	sysType :$scope.sysType || '',
		        	professionType :$scope.professionType || '',
		        };
		      }
    };
	
	

	
/**
  * 镜像状态 显示
  */
$scope.getImageStatus =function (model){
	$scope.imageStatusClass = '';
		if(model.imageStatus&&model.imageStatus=='ACTIVE'&&model.isUse=='1'){
			return 'ey-square-right';
		}  
		else if(model.imageStatus=='ERROR'||model.isUse=='2'||model.isUse=='0'){
			return 'ey-square-disable';
		}
		else if(model.imageStatus=='SAVING'||model.imageStatus=='QUEUED'||model.imageStatus=='DELETING'){
			return 'ey-square-warning';
		}
		else if(model.imageStatus=='DELETED'){
			return 'ey-square-error';
		}
};
	

$scope.init=function(){
	  $scope.statusList = [{
	  		'nodeNameEn':'',
	  		'nodeName':'全部状态'
	  	},{
	  		'nodeNameEn':'1',
	  		'nodeName':'正常'
	  	},{
	  		'nodeNameEn':'2',
	  		'nodeName':'已停用'
	  	}];  
	  
	  $scope.osList=[{
			'nodeId':'',
			'nodeName':'系统类型（全部）'
		}];
		eayunHttp.post('cloud/image/getostypelist.do').then(function(response){
			$scope.osList=  response.data.data;
			$scope.osList.unshift({
				'nodeId':'',
				'nodeName':'系统类型（全部）'
			});
		});  
		
		
		$scope.marketTypeList=[{
			'nodeId':'',
			'nodeName':'业务类别（全部）'
		}];
		eayunHttp.post('cloud/image/getmarkettypelist.do').then(function(response){
			$scope.marketTypeList=  response.data.data;
			$scope.marketTypeList.unshift({
				'nodeId':'',
				'nodeName':'业务类别（全部）'
			});
		});

};




/**
  * 选择云主机状态
  */
$scope.selectImageStatus = function(item,event){
 	$scope.isUse = null;
 	$scope.isUse = item.nodeNameEn;
 	$scope.myTable.api.draw();
 	
};

/**
 * 选择云主机系统类型
 */
$scope.selectImageOsType = function(item,event){
	$scope.sysType = null;
	$scope.sysType = item.nodeId;
	$scope.myTable.api.draw();
	
};

/**
 * 选择市场镜像业务类型
 */
$scope.selectProfessionType = function(item,event){
	$scope.professionType = null;
	$scope.professionType = item.nodeId;
	$scope.myTable.api.draw();
	
};


//镜像详情页
$scope.findImageById=function(image){
	$state.go('app.cloud.cloudhost.marketimagedetail',{"detailType":'镜像',"imageId":image.imageId}); // 跳转后的URL;
};
	
	 //监视器[监视数据中心、项目id变化]
	  $scope.$watch('model.projectvoe' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.myTable.api.draw();
	    	}
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
	    				var status=$scope.myTable.result[i].imageStatus.toString().toUpperCase();
	    				if("ACTIVE"!=status&&"ERROR"!=status&&"DELETED"!=status){
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
	  $scope.search = function(){
		  $scope.myTable.api.draw();
	  };
	  
		    
	$scope.init();	    
		    
}).controller('marketImageDetail', function ($rootScope,$scope, $timeout,eayunHttp ,eayunModal,$stateParams,$state,toast,powerService,eayunStorage,DatacenterService) {
	
	var list=[{route:'app.cloud.cloudhost.image',name:'镜像'},{route:'app.cloud.cloudhost.image.marketimage',name:'市场镜像'}];
	$rootScope.navList(list,'镜像详情','detail');

	$scope.item = {};

	
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
		 
	  }); 
	  
	  
     
     
     /**
      * 镜像状态 显示
      */
    $scope.getImageStatus =function (model){
    	$scope.imageStatusClass = '';
    		if(model.imageStatus&&model.imageStatus=='ACTIVE'&&model.isUse=='1'){
    			$scope.imageStatusClass = 'ey-square-right';
    		}  
    		else if(model.imageStatus=='ERROR'||model.isUse=='2'||model.isUse=='0'){
    			$scope.imageStatusClass = 'ey-square-disable';
    		}
    		else if(model.imageStatus=='SAVING'||model.imageStatus=='QUEUED'||model.imageStatus=='DELETING'){
    			$scope.imageStatusClass ='ey-square-warning';
    		}
    		else if(model.imageStatus=='DELETED'){
    			$scope.imageStatusClass ='ey-square-error';
    		}
    };
	  
	

	eayunHttp.post('cloud/image/getMarketImagebyId.do',{imageId:$stateParams.imageId}).then(function(response){
		$scope.item=response.data.data;  
		$scope.getImageStatus($scope.item);
    });
	
	
	
	//pop框的方法
	$scope.tableNameShow = [];
    $scope.openPopBox = function(obj){
    	if(obj.type == 'imageName'){
    		$scope.nameShow = true;
    	}
    	if(obj.type == 'imageDesc'){
			$scope.descShow = true;
    	}
    	if(obj.type == 'imageSys'){
    		$scope.imageSys = true;
    	}
    	if(obj.type == 'imageProvider'){
    		$scope.imageProviderShow = true;
    	}
    	$scope.description = obj.value;
    };
    $scope.closePopBox = function(type){
    	if(type == 'imageName'){
    		$scope.nameShow = false;
    	}
    	if(type == 'imageDesc'){
    		$scope.descShow = false;
    	}
    	if(type == 'imageSys'){
    		$scope.imageSys = false;
    	}
    	if(type == 'imageProvider'){
    		$scope.imageProviderShow = false;
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
    
    

});
		
  
		
		
		
		