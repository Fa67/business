'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $stateProvider.state('app.net.netbar.loadbalance.vip', {
      url: '/vip',
      templateUrl: 'views/net/loadbalance/vip/vipmng.html',
      controller: 'VipCtrl'
    });
  })
  
  .controller('VipCtrl', function ($scope, eayunModal,eayunHttp ,$timeout,toast) {
	  $scope.myTable = {
			  source: 'cloud/loadbalance/vip/getVipList.do',
		      api:{},
		      getParams: function () {
		        return {
		        	prjId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '',
		        	dcId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
		        	name :  $scope.vipName || ''
			};
		      }
	   };
	  $scope.vipJson = function (tagsStr){
		  var json ={};
		  if(tagsStr){
			  json= JSON.parse(tagsStr);
		  }
		  return json;
	  };
	//VIP状态显示
		$scope.getVipStatus = function(model){
		  $scope.vipStatusClass = '';
			if(model.vipStatus=='ACTIVE'){
				return 'green';
			}  
			else if(model.vipStatus=='ERROR'){
				return 'gray';
			}
			else if(model.vipStatus=='PENDING_CREATE'||model.vipStatus=='PENDING_UPDATE'||model.vipStatus=='PENDING_DELETE'){
				return 'yellow';
			}
		};
		
	//监视器[监视数据中心、项目id变化]
	  $scope.$watch('model.dcProject' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.myTable.api.draw();
	    	}
	    });
  //pop方法
	  $scope.hintTagShow = [];
	  $scope.openTableBox = function(vip){
		  if(vip.type == 'tagName'){
			  $scope.hintTagShow[vip.index] = true;
		  }
		  $scope.ellipsis = vip.value;
	  };
	  $scope.closeTableBox = function(vip){
		  if(vip.type == 'tagName'){
			  $scope.hintTagShow[vip.index] = false;
		  }
	  };
  /**
     * 查询当前sessionStore 是否存在用户信息
     */
    $scope.checkUser = function (){
    	var user = sessionStorage["userInfo"]
    	 if(user){
    		 user = JSON.parse(user);
    		 if(user&&user.userId){
    			 return true;
    		 }
    	 }
    	return false;
    };   
	//页面中回车键触发查询事件；
	  $(function () {
          document.onkeydown = function (event) {
	       	   var e = event || window.event || arguments.callee.caller.arguments[0];
		       if(!$scope.checkUser()){
	               return ;
	           }
	       	   if (e && e.keyCode == 13) {
	       	   $scope.myTable.api.draw();
	       	   }
          };
      });
	  
	  /**
	     * 如果列表中有中间状态的VIP，间隔5s刷新列表
	     */
	    $scope.refreshList = function (){
	    	$scope.myTable.api.draw();
	    };
	  //VIP列表的状态字段，刷新页面
	  $scope.$watch("myTable.result",function (newVal,oldVal){
	    	if(newVal !== oldVal){
	    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
	    			for(var i=0;i<$scope.myTable.result.length;i++){
	    				var status=$scope.myTable.result[i].vipStatus.toString().toLowerCase();
	    				if("active"!=status&&"error"!=status){
	    					$timeout($scope.refreshList,5000);
	    					break;
	    				}
	    				
	    			}
	    		}
	    	}
	    });
	   
	//vip详情
    $scope.detailVip=function(item){
    	 var result = eayunModal.dialog({
		    	showBtn: false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: 'VIP详情',
		        width: '650px',
		        templateUrl: 'views/net/loadbalance/vip/vipdetail.html',
		        controller: 'DetailVip',
		        resolve: {
		        	vip:function(){
		        	  return item;
		        	},
		        	tags:function(){
    	 				return eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'ldVIP',resId: item.vipId}).then(function(response){
    	 					return  response.data;
    	 				});
    	 			}
		        }
		      });
		      result.then(function (value){
		    	 
		      }, function () {
		        //console.info('取消');
		      });
    	};	 
	  
	  
	 /*查询vip*/
	  $scope.getVip = function(){
		  $scope.myTable.api.draw();
	  };
	  /*创建监控*/
	  $scope.createVip = function(){
		  var result = eayunModal.dialog({
			    showBtn: false,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: '创建VIP',
		        width: '600px',
		        templateUrl: 'views/net/loadbalance/vip/addvip.html',
		        controller: 'vipAddCtrl',
		        resolve: {
		        	prjId:function (){
	            		return {prjId:sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : ''};
	            	},
		            subNetWorkList: function () {
		            	return eayunHttp.post('cloud/subnetwork/getSubnetList.do',$scope.model.dcProject).then(function(response){
		            		return response.data;
		            	});
		            },
		            poolList: function () {
		            	return eayunHttp.post('cloud/loadbalance/pool/getPoolsNotHaveVip.do',{dcId:$scope.model.dcProject.dcId,prjId:$scope.model.dcProject.projectId}).then(function(response){
		            		return response.data;
		            	});
		            }
		            
		            
		        }
		      });
		      result.then(function (value) {
		    	  //创建页面点击提交执行后台Java代码
		    	  eayunHttp.post('cloud/loadbalance/vip/addVip.do',value).then(function(response){
		    		  //如果创建成功，刷新当前列表页
		    		  if(response.data.code!="010120"){
		    			  var name = "";
		    			  if(response.data.vipName.length>9){
		    				  name = response.data.vipName.substring(0,9)+"...";
		    			  }else{
		    				  name = response.data.vipName;
		    			  }
		    			  toast.success('添加vip'+name+'成功');
		    		  }
		    		  $scope.myTable.api.draw();
	              });
		      }, function () {
//		        console.info('取消');
		      });
	  };
	  /*编辑vip*/
	  $scope.editVip = function(item){
		  var result = eayunModal.dialog({
			    showBtn: false,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: '编辑VIP',
		        width: '600px',
		        templateUrl: 'views/net/loadbalance/vip/editvip.html',
		        controller: 'editVipCtrl',
		        resolve: {
		        	item: function () {
		        	  return item;
		            }
		        }
		      });
		  //修改点击提交后
		  result.then(function (value) {
	    	  //创建页面点击提交执行后台Java代码
	    	  eayunHttp.post('cloud/loadbalance/vip/updateVip.do',value).then(function(response){
	    		  if(response.data.code!="010120"){
	    			  var name = "";
	    			  if(response.data.vipName.length>9){
	    				  name = response.data.vipName.substring(0,9)+"...";
	    			  }else{
	    				  name = response.data.vipName;
	    			  }
	    			  toast.success('修改vip'+name+'成功');
	    		  };
	    		  $scope.myTable.api.draw();
        });
	    	  
	      }, function () {
//	        console.info('取消');
	      });  
	  }; 
	  /*删除VIP*/
	  $scope.deleteVip = function (item){
//		  var name = "";
//		  if(item.vipName.length>7){
//			  name = item.vipName.substring(0,7)+"...";
//		  }else{
//			  name = item.vipName;
//		  }
		  eayunModal.confirm('确定要删除vip'+item.vipName+'?').then(function () {
		        eayunHttp.post('cloud/loadbalance/vip/deleteVip.do',{dcId:item.dcId, prjId :item.prjId,vipId:item.vipId,vipName : item.vipName,poolId:item.poolId}).then(function(response){
		        	if(response.data.code!="010120"){
		        		toast.success('删除vip成功');
		        	}
		        	$scope.myTable.api.draw();
		        });
		      });
	  };
	  /*标签*/
		$scope.tagResource = function(resType, resId){
			var result=eayunModal.dialog({
				showBtn: false,
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
				result.then(function () {
					$scope.myTable.api.draw();
			},function () {
				$scope.myTable.api.draw();
			});
		};
	  
	  
  })
 /**
   *创建Vip
   *
   */
   .controller('vipAddCtrl', function ($scope, eayunModal,eayunHttp ,prjId,subNetWorkList,poolList) {
	 //直接将创建页面所有的项目放入当前的$scope.model中 开始
		  $scope.model={};
	   //将项目绑定addProject页面下拉框
		//循环显示当前位置的项目
		  eayunHttp.post('cloud/vm/getProListByCustomer.do').then(function (response){
			  $scope.projectList = response.data;
			  angular.forEach($scope.projectList, function (value, key) {
		        if(value.projectId == prjId.prjId){
		        	$scope.model.project=value;
		        	
		        }
			  });
		  });
		 
		  
		  $scope.model.poolId = null;
		  $scope.model.subnet_id = null;
		  $scope.poolList=poolList;
		  $scope.subNetWorkList=subNetWorkList;
		//切换项目
		  $scope.changePrj = function(){
			//项目切换名称重名验证
			  $scope.myForm.name.$validate();
			  $scope.model.subnet_id=null;
			  $scope.model.poolId=null;
			  eayunHttp.post('cloud/subnetwork/getSubnetList.do',$scope.model.project).then(function(response){
				  $scope.subNetWorkList= response.data;
          	});
			  eayunHttp.post('cloud/loadbalance/pool/getPoolsNotHaveVip.do',{dcId:$scope.model.project.dcId,prjId:$scope.model.project.projectId}).then(function(response){
				  $scope.poolList= response.data;
          	}); 
			  
		  }
	    /*创建页面检测名称唯一*/
		  $scope.checkVipName=function(value){
			  var title=/^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  	  $scope.flag=false;
		  	  if(value.match(title)){
		  		$scope.model.name=value;
		  		  return eayunHttp.post('cloud/loadbalance/vip/getVipByIdOrName.do',$scope.model).then(function(response){
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
		  
		  
		  
		  
	  //直接将创建页面所有的项目放入当前的$scope.model中 结束
	    $scope.commit = function () {
	    	$scope.ok($scope.model);
	    };
	  
  })
  /**
 * @ngdoc function
 * @name eayunApp.controller:editVipCtrl
 * @description
 * # editVipCtrl
 * VIP列表页-->编辑VIP
 */
  .controller('editVipCtrl', function ($scope, eayunModal,eayunHttp,item) {
	  //将项目绑定addProject页面下拉框
	  $scope.model={};
	  //angular复制对象赋给新建页面的model
	  $scope.model=angular.copy(item);
	//ajax验证 编辑安全组判断重名
	    $scope.checkVipName = function () {
	  	  var title=/^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
	  	  $scope.flag=false;
	  	  if($("#name").val().match(title)){
	  		$scope.model.name=$("#name").val();//作为编辑后的名称
	  		  return eayunHttp.post('cloud/loadbalance/vip/getVipByIdOrName.do',$scope.model).then(function(response){
	  			  if(true==response.data){
	  				  return false;
	  			  }else{
	  				  return true;
	  			  }
	  			  
	  	      });
	  	  
	  	  }else{
	  		  $scope.flag=true;  
	  	  }    
	  	};
	  
	//直接将创建页面所有的项目放入当前的$scope.model中 结束
    $scope.commit = function () {
    	$scope.ok($scope.model);
    };
   
  })
  .controller('DetailVip', function ($scope,vip,tags,eayunHttp,eayunModal) {
			$scope.model= vip;
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
		    * vip状态 显示
		    */
			$scope.getVipStatus =function (model){
		    	$scope.vipStatusClass = '';
				if(model.vipStatus&&model.vipStatus=='ACTIVE'){
					$scope.vipStatusClass = 'green';
				}  
				else if(model.vipStatus=='ERROR'){
					$scope.vipStatusClass = 'gray';
				}
				else if(model.vipStatus=='PENDING_CREATE'||model.vipStatus=='PENDING_UPDATE' ||model.vipStatus=='PENDING_DELETE'){
					$scope.vipStatusClass = 'yellow';
				}
		    };
			$scope.getVipStatus($scope.model);
			
			  $scope.commit = function () {
			     eayunModal.confirm('确认保存？').then(function () {
			       $scope.ok($scope.model);
			     }, function () {
			      // console.info('取消');
			     });
			   };

		})
  
  ;
