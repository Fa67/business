'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $urlRouterProvider.when("/app/net/netbar/route",'/app/net/netbar/route/routeList');
	  $stateProvider.state('app.net.netbar.route', {//路由
      url: '/route',
      templateUrl: 'views/net/route/routemain.html'
    }).state('app.net.netbar.route.routeList', {
        url: '/routeList',
        templateUrl: 'views/net/route/routemng.html',
        controller: 'RouteCtrl'
      }).state("app.net.routeDatil",{
		url: '/routeDatil/:dcId/:prjId/:routeId',
	    templateUrl: 'views/net/route/routedetail.html',
	    controller: 'routeDatilCtrl'
	})
    
    ;
  })
  
  .controller('RouteCtrl', function ($scope,$rootScope, eayunModal,eayunHttp,$state,toast,powerService) {
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.buttonPower = {
			isAdd : powerService.isPower('route_add'),	//创建路由
			isEdit : powerService.isPower('route_edit'),	//编辑
			isSetGateWay : powerService.isPower('route_setup'),		//设置网关/清除网关
			isAddSubnet : powerService.isPower('route_addsubnet'),	//添加路由子网
			isTag : powerService.isPower('route_tag'),	//标签
			isDelete : powerService.isPower('route_delete')		//删除
			
			};
	  });
	  $rootScope.netRoute = null;
	//HTML页面model值双向绑定到getParams统一传到后台Java代码Controller中
	  $scope.myTable = {
			  source: 'cloud/route/getRouteList.do',
		      api:{},
		      getParams: function () {
		        return {
		        	prjId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '',
		        	dcId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
		        	name :  $scope.routeName || ''
			};
		      }
	   };
	 
	//路由状态显示
		$scope.getRouteStatus = function(model){
		  $scope.routeStatusClass = '';
			if(model.routeStatus=='ACTIVE'){
				return 'green';
			}  
			else if(model.routeStatus=='ERROR'){
				return 'gray';
			}
			else if(model.routeStatus=='PENDING_CREATE'||model.routeStatus=='PENDING_UPDATE'||model.routeStatus=='PENDING_DELETE'){
				return 'yellow';
			}
		};
	  
	  
	//监视器[监视数据中心、项目id变化]
	  $scope.$watch('model.dcProject' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.myTable.api.draw();
	    	}
	    });
	//列表页名称查询
	  $scope.getRoute = function(){
		  $scope.myTable.api.draw();
		  
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
		 
	  
	  $scope.createRoute = function(){

		  var result = eayunModal.dialog({
			    showBtn: false,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: '创建路由',
		        width: '600px',
		        templateUrl: 'views/net/route/addroute.html',
		        controller: 'routeAddCtrl',
		        resolve: {
		            prjId:function (){
	            		return {prjId:sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : ''};
	            	},
		            outNetWorkList: function () {
		            	return eayunHttp.post('cloud/route/getOutNetList.do',$scope.model.dcProject).then(function(response){
		            	    return response.data;
		            	});
		            },
		            haveBandCount: function () {
		            	return eayunHttp.post('cloud/route/getHaveBandCount.do',{prjId :$scope.model.dcProject.projectId}).then(function(response){
		            	    return response.data;
		            	});
		            },
		            prjBandCount: function () {
		            	return eayunHttp.post('cloud/route/getPrjBandCount.do',{prjId :$scope.model.dcProject.projectId}).then(function(response){
		            	    return response.data;
		            	});
		            }
		            
		            
		        }
		      });
		      result.then(function (value) {
		    	  //创建页面点击提交执行后台Java代码
		    	  eayunHttp.post('cloud/route/addRoute.do',value).then(function(response){
		    		  //如果创建成功，刷新当前列表页
		    		  
		    		  if(response.data.code!="010120"){
		    			  var name = "";
		    			  if(response.data.routeName.length>10){
		    				  name = response.data.routeName.substring(0,10)+"...";
		    			  }else{
		    				  name = response.data.routeName;
		    			  }
		    			  toast.success('添加路由'+name+'成功');
		    		  }
		    		  $scope.myTable.api.draw();
	              });
		      }, function () {
//		        console.info('取消');
		      });
	  };
	  $scope.setGateway = function (item){
		  var result = eayunModal.dialog({
			    showBtn: false,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: '设置网关',
		        width: '600px',
		        templateUrl: 'views/net/route/setgateway.html',
		        controller: 'routeSetGatewayCtrl',
		        resolve: {
		            outNetWorkList: function () {
		            	return eayunHttp.post('cloud/route/getOutNetList.do',{dcId : item.dcId}).then(function(response){
		            	    return response.data;
		            	});
		            },
		            item: function () {
		            	    return item;
		            }
		            
		        }
		      });
		      result.then(function (value) {
		    	  //创建页面点击提交执行后台Java代码
		    	  eayunHttp.post('cloud/route/addGateWay.do',value).then(function(response){
		    		  //如果创建成功，刷新当前列表页
		    		  if(response.data.code!="010120"){
		    			  toast.success('设置网关成功');
		    		  }
		    		  $scope.myTable.api.draw();
	              });
		      }, function () {
//		        console.info('取消');
		      });
	  };
	  $scope.deleteGateWay = function (item){
		  eayunModal.confirm('确定要清除'+item.routeName+'的网关？').then(function () {
			  eayunHttp.post('cloud/route/deleteGateway.do',item).then(function(response){
	    		  //如果创建成功，刷新当前列表页
	    		  if(response.data.code!="010120"){
	    			  toast.success('清除网关成功');
	    		  }
	    		  $scope.myTable.api.draw();
              });
	      }, function () {
//	        console.info('取消');
	      });
		  
		 
	  };
	  //编辑路由
	  $scope.editRoute =function (item){
		  var result = eayunModal.dialog({
			    showBtn: false,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: '编辑路由',
		        width: '600px',
		        templateUrl: 'views/net/route/editroute.html',
		        controller: 'editRouteCtrl',
		        resolve: {
		        	item: function () {
		        	  return item;
		        	
		            },
		            haveBandCount: function () {
		            	return eayunHttp.post('cloud/route/getHaveBandCount.do',{prjId :$scope.model.dcProject.projectId}).then(function(response){
		            	    return response.data;
		            	});
		            },
		            prjBandCount: function () {
		            	return eayunHttp.post('cloud/route/getPrjBandCount.do',{prjId :$scope.model.dcProject.projectId}).then(function(response){
		            	    return response.data;
		            	});
		            }
		        }
		      });
		  //修改点击提交后
		  result.then(function (value) {
	    	  //创建页面点击提交执行后台Java代码
	    	  eayunHttp.post('cloud/route/editRoute.do',value).then(function(response){
	    		  
	    		  if(response.data.code!="010120"){
	    			  var name = "";
	    			  if(response.data.routeName.length>10){
	    				  name = response.data.routeName.substring(0,10)+"...";
	    			  }else{
	    				  name = response.data.routeName;
	    			  }
	    			  toast.success('修改路由'+name+'成功');
	    		  };
	    		  $scope.myTable.api.draw();
            });
	    	  
	      }, function () {
//	        console.info('取消');
	      });
	  };
	//路由连接子网 
	  $scope.connectSubNet =function (item){
		  var result = eayunModal.dialog({
			    showBtn: false,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: '添加路由子网',
		        width: '600px',
		        templateUrl: 'views/net/route/concectsubnet.html',
		        controller: 'connectSubNetCtrl',
		        resolve: {
		        	InnerNetWorkList: function () {
		            	return eayunHttp.post('cloud/subnetwork/getSelectInnerNet.do',$scope.model.dcProject).then(function(response){
		            	    return response.data;
		            	});
		            },
		        	item: function () {
		        	  return item;
		          }
		        }
		      });
		  //修改点击提交后
		  result.then(function (value) {
	    	  //创建页面点击提交执行后台Java代码
	    	  eayunHttp.post('cloud/route/connectSubnet.do',value).then(function(response){
	    		  if(response.data.code!="010120"){
	    			  var name = "";
	    			  if(item.routeName.length>8){
	    				  name = item.routeName.substring(0,8)+"...";
	    			  }else{
	    				  name = item.routeName;
	    			  }
	    			  toast.success('路由'+name+'连接子网成功');
	    		  };
	    		  $scope.myTable.api.draw();
            });
	    	  
	      }, function () {
//	        console.info('取消');
	      });
	  };
	  $scope.goDetails = function(item){
		  $state.go('app.net.routeDatil',{"dcId":item.dcId,"prjId":item.prjId,"routeId":item.routeId}); // 跳转后的URL;
	  };
	  /*删除路由*/
	  $scope.deleteRoute = function(item){
		  var name = "";
		  if(item.routeName.length>10){
			  name = item.routeName.substring(0,10)+"...";
		  }else{
			  name = item.routeName;
		  }
		  eayunModal.confirm('确定删除路由'+item.routeName+'？').then(function () {
		        eayunHttp.post('cloud/route/deleteRoute.do',{dcId : item.dcId,prjId :item.prjId,routeId:item.routeId, routeName :item.routeName,qosId:item.qosId}).then(function(response){
		        	if(response.data.code!="010120"){
		        		toast.success('删除路由'+name+'成功');
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
			});
		};
  })
  /**
 * @ngdoc function
 * @name eayunApp.controller:routeAddCtrl
 * @description
 * # routeAddCtrl
 * 创建路由
 */
  .controller('routeAddCtrl', function ($scope, eayunModal,eayunHttp,outNetWorkList,haveBandCount,prjBandCount,prjId) {
	//直接将创建页面所有的项目放入当前的$scope.model中 开始
	  $scope.model={};
	//将项目绑定addProject页面下拉框
	  eayunHttp.post('cloud/vm/getProListByCustomer.do').then(function (response){
		  $scope.projectList = response.data;
		  angular.forEach($scope.projectList, function (value, key) {
	        if(value.projectId == prjId.prjId){
	        	$scope.model.project=value;
	        	
	        }
		  });
	  });
	
	  $scope.outNetWorkList=outNetWorkList;
	  $scope.model.outNet = $scope.outNetWorkList[0];
	//去除创建页面打开默认js验证空
	  //设置路由已有的带宽和项目配额带宽值
	  //带宽显示
	  $scope.haveCount = haveBandCount;
	  $scope.count = haveBandCount;
	  $scope.firstCount=angular.copy(haveBandCount);
	  $scope.prjBandCount = prjBandCount;
	  //输入带宽
	  $scope.computeBand = function(){
		  
		  if("undefined"==$scope.model.rate ||null ==$scope.model.rate){
			  var a = 0;
		  }else{
			  var a = parseInt($scope.model.rate); 
				
		  }
		  
		  var b = parseInt($scope.firstCount);
		  $scope.countNum = a + b;
		  $scope.count = a + b>prjBandCount ? prjBandCount : a+b;
		  
	  };
	  //切换项目查询带宽配额
	  $scope.changeProject = function(){
		  $scope.countNum = 0;
		  //项目切换名称重名验证
		  $scope.myForm.name.$validate();
		  $scope.model.rate = null;
		  eayunHttp.post('cloud/route/getPrjBandCount.do',{prjId :$scope.model.project.projectId}).then(function(response){
			  $scope.prjBandCount = response.data;
//			  console.info("项目总共数量"+$scope.prjBandCount);
      	});
		  eayunHttp.post('cloud/route/getHaveBandCount.do',{prjId :$scope.model.project.projectId}).then(function(response){
			  $scope.firstCount = response.data;
			  $scope.haveCount = response.data;
			  $scope.count = response.data;
//			  console.info("项目已有的数量"+$scope.firstCount);
      	}); 
	  };
	  $scope.checkRouteName=function(value){
		  var title=/^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
	  	  $scope.flag=false;
	  	  if(value.match(title)){
	  		$scope.model.name=value;
	  		  return eayunHttp.post('cloud/route/getRouteByIdOrName.do',$scope.model).then(function(response){
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
 * @name eayunApp.controller:routeSetGatewayCtrl
 * @description
 * # routeSetGatewayCtrl
 * 路由列表页-->设置网关
 */
  .controller('routeSetGatewayCtrl', function ($scope, eayunModal,eayunHttp,outNetWorkList,item) {
	  //将项目绑定addProject页面下拉框
	  $scope.outNetWorkList=outNetWorkList;
	  $scope.model={};
	  //angular复制对象赋给新建页面的model
	  $scope.model=angular.copy(item);
	  $scope.model.outNetId = null;
	  //直接将创建页面所有的项目放入当前的$scope.model中 开始
	  $scope.model.outNet = $scope.outNetWorkList[0];
	  if($scope.outNetWorkList && $scope.outNetWorkList.length==1){
			$scope.model.outNetId = $scope.outNetWorkList[0].value;
	  }
	  
	//直接将创建页面所有的项目放入当前的$scope.model中 结束
    $scope.commit = function () {
    	$scope.ok($scope.model);
    };
   
  
    

  })
 /**
 * @ngdoc function
 * @name eayunApp.controller:editRouteCtrl
 * @description
 * # editRouteCtrl
 * 路由列表页-->编辑路由
 */
  .controller('editRouteCtrl', function ($scope, eayunModal,eayunHttp,item,haveBandCount,prjBandCount) {
	  //将项目绑定addProject页面下拉框
	  $scope.model={};
	  //angular复制对象赋给新建页面的model
	  $scope.model=angular.copy(item);
	  //带宽设置
	  $scope.count = haveBandCount;
	  $scope.firstCount=angular.copy(haveBandCount);
//	  console.info("count已经占用了"+$scope.count);
	  
	  $scope.prjBandCount = prjBandCount;
//	  console.info("prjBandCount总数 "+$scope.prjBandCount);
	  $scope.computeBand = function(){
		  var inputValue = 0;
		  
		  
		  eayunHttp.post('cloud/route/getHaveBandCount.do',{prjId :item.prjId,routeId:item.routeId}).then(function(response){
			  $scope.x = parseInt(response.data);
//			  console.info("除此之外的所有的带宽值 "+$scope.x);
			  var a = $scope.x;
			  if("undefined"==$scope.model.rate ||null ==$scope.model.rate){
				  inputValue = parseInt(0);
			  }else{
				  inputValue = parseInt($scope.model.rate); 
					
			  }
			  $scope.count = a+inputValue >prjBandCount ? prjBandCount :a+inputValue;
			  $scope.countNum = a+inputValue;
      	});
		  
	  };
	  
	  
	  /*编辑路由校验名称*/
	  $scope.validRouteName = function (){
		  var title=/^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
	  	  $scope.flag=false;
	  	  if($("#name").val().match(title)){
	  		$scope.model.name=$("#name").val();//作为编辑后的名称
	  		  return eayunHttp.post('cloud/route/getRouteById.do',$scope.model).then(function(response){
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
    	$scope.model.prjId = item.prjId;
    	$scope.model.qosId = item.qosId;
    	
      $scope.ok($scope.model);
    };
   
  
    

  })
  /**
 * @ngdoc function
 * @name eayunApp.controller:connectSubNetCtrl
 * @description
 * # connectSubNetCtrl
 * 路由列表页-->路由连接子网
 */
  .controller('connectSubNetCtrl', function ($scope, eayunModal,eayunHttp,item,InnerNetWorkList) {
	  
	//将项目绑定addProject页面下拉框
	  $scope.InnerNetWorkList=InnerNetWorkList;
	  $scope.model={};
	  //angular复制对象赋给新建页面的model
	  $scope.model=angular.copy(item);
	  $scope.model.subNetId = null;
	  //直接将创建页面所有的项目放入当前的$scope.model中 开始
	  $scope.model.innerNet = $scope.InnerNetWorkList[0];
	  $scope.model.routeName = item.routeName;
	//直接将创建页面所有的项目放入当前的$scope.model中 结束
    $scope.commit = function () {
    	$scope.ok($scope.model);
      
    };
   
  
   
  }) 
  /********************************************路由详情页Controller开始**************************************************************/
  .controller('routeDatilCtrl', function ($scope,$rootScope, eayunModal,eayunHttp,$stateParams,toast,powerService) {
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.buttonPower = {
			isDetachSub : powerService.isPower('route_unsubnet'),		//删除
			isSubNetTag : powerService.isPower('subnet_tag')	//子网标签
			};
	  });
	
	  
	  //pop框方法
	  $scope.tableTagShow = [];
	  $scope.openPopBox = function(route){
		  if(route.type == 'tagName'){
			  $scope.hintTagShow = true;
		  }
		  $scope.description = route.value;
	  };
	  $scope.closePopBox = function(type){
		  if(type == 'tagName'){
			  $scope.hintTagShow = false;
		  }
	  };
	  $scope.openTableBox = function(route){
		  if(route.type == 'tagName'){
			  $scope.tableTagShow[route.index] = true;
		  }
		  $scope.ellipsis = route.value;
	  };
	  $scope.closeTableBox = function(route){
		  if(route.type == 'tagName'){
			  $scope.tableTagShow[route.index] = false;
		  }
	  };
	  //详情页--用于当前位置
	  $rootScope.netRoute = "#/app/net/netbar/route/routeList";
	  $rootScope.netname = '路由';
	//路由的标签查询
	  $scope.resourceTags = {}; 
	  eayunHttp.post('tag/getResourceTagForShowcase.do',{resType:'route',resId: $stateParams.routeId}).then(function(response){
	  	$scope.resourceTags = response.data;
	  });
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
					//操作完成点击“关闭”按钮，页面后刷新table
					$scope.routeDetailTable.api.draw();
			},function () {
				//操作完成，点击“×”按钮，关闭页面后刷新table
				$scope.routeDetailTable.api.draw();
		});
		};

	  
	  
	  //HTML页面model值双向绑定到getParams统一传到后台Java代码Controller中
	  $scope.routeDetailTable = {
			  source: 'cloud/subnetwork/getSubnetWorksByRouteId.do',
		      api:{},
		      getParams: function () {
		        return {
		        	dcId :  $stateParams.dcId || '',
		        	prjId :  $stateParams.prjId || '',
		        	routeId :  $stateParams.routeId || ''
			};
		      }
	   };
	  $scope.routeJson = function (tagsStr){
		  var json ={};
		  if(tagsStr){
			  json= JSON.parse(tagsStr);
		  }
		  return json;
	  };
	//路由状态显示
		$scope.getRouteStatus = function(model){
			$scope.routeStatusClass = '';
			if(model.routeStatus=='ACTIVE'){
			  $scope.routeStatusClass = 'green';
			}  
			else if(model.routeStatus=='ERROR'){
				$scope.routeStatus = 'gray';
			}
			else if(model.routeStatus=='PENDING_CREATE'||model.routeStatus=='PENDING_UPDATE'||model.routeStatus=='PENDING_DELETE'){
				$scope.routeStatus = 'yellow';
			}
		};
	//获取当前路由详情
	  eayunHttp.post('cloud/route/getRouteDetail.do',{dcId:$stateParams.dcId,prjId:$stateParams.prjId,routeId:$stateParams.routeId}).then(function(response){
			if(null!=response.data){
				$scope.model =  response.data;
				$scope.getRouteStatus($scope.model);
			  }
	    });
	
	  /*解绑子网*/
	  $scope.detachSubNet = function(item){
		  eayunModal.confirm('确定要解绑子网'+item.subnetName+'？').then(function () {
			  eayunHttp.post('cloud/route/detachSubnet.do',{ dcId:item.dcId, routeId:item.routeId, subnetId:item.subnetId, routeName :item.routeName,prjId :item.prjId}).then(function(response){
	    		  //如果创建成功，刷新当前列表页
	    		  if(response.data.code!="010120"){
	    			  toast.success('解绑子网成功');
	    		  }
    			  $scope.routeDetailTable.api.draw();
              });
	      }, function () {
//	        console.info('取消');
	      });
		  
	  };
	  
	  
	  
	  
	  
  });
