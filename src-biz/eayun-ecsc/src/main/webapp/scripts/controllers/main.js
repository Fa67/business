'use strict';

/**
 * @ngdoc function
 * @name eayunApp.controllers
 * @description
 * controller模块
 */
angular.module('eayunApp.controllers', [])
/**
 * @ngdoc function
 * @name appRouter
 * @description
 * 配置路由
 */
  .config(function ($stateProvider,$urlRouterProvider) {
    $urlRouterProvider.when('/app','/app/overview');
    /**
     * 对应左侧栏标签
     */
    $stateProvider
    .state('app.overview', {	/* 总览*/
      url: '/overview',
      templateUrl: 'views/overview.html',
      controller: 'OverviewCtrl',
      resolve:{
    	  noticeList:function(eayunHttp){
    		  return getNoticeList(eayunHttp);
    	  },
    	  cloudprojectList:function(eayunHttp){
    		  return getDcPrj(eayunHttp);
    	  }
      }
    })
    
    .state('app.styletest',{	/*页面样板*/
      url: '/styletest',
      templateUrl:'views/styletest/main.html',
      controller:'StyleCtrl'
    })
    
    
    .state('app.cloud',{	/*云主机*/
      url: '/cloud',
      templateUrl:'views/cloudhost/cloud.html',
      controller:'CloudCtrl',
      resolve:{
    	  projects:function (eayunHttp){
    		  return eayunHttp.post('cloud/vm/getDatacenterProjectList.do',{}).then(function(response){
    	    	  return  response.data;
    	      });
    	  }
      }
    })
	.state('app.rds', {
		url: '/rds',
		templateUrl: 'views/rds/main.html',
		controller: 'RdsCtrl',
		resolve: {
			cloudprojectList: function (eayunHttp) {
				return getDcPrj(eayunHttp);
			}
		}
	})
    .state('app.net',{			/*网络*/
      url: '/net',
      templateUrl:'views/net/main.html',
      controller:'NetCtrl',
      resolve:{
    	  cloudprojectList:function(eayunHttp){
    		  return getDcPrj(eayunHttp);
    	  }
      }
    })
    .state('app.safety',{			/*安全*/
      url: '/safety',
      templateUrl:'views/safety/safetymain.html',
      controller:'SafetyCtrl',
	  resolve:{
    	  cloudprojectList:function(eayunHttp){
    		  return getDcPrj(eayunHttp);
    	  }
      }
    	  
    })
    .state('app.monitor',{		/*监控*/
      url: '/monitor',
      templateUrl:'views/monitor/monitormain.html',
      controller:'MonitorCtrl'
    })
    .state('app.auth',{		/*账号管理*/
      url: '/auth',
      templateUrl:'views/auth/main.html',
      controller:'AuthCtrl'
    })
    .state('app.role',{			/*角色管理*/
      url: '/role',
      templateUrl:'views/role/main.html',
      controller:'RoleCtrl'
    })
     .state('app.accesskey',{			/*api管理*/
      url: '/accesskey',
      templateUrl:'views/accesskey/main.html',
      controller:'accesskeyCtrl'
    })
    .state('app.business',{		/*业务管理*/
      url: '/business',
      templateUrl:'views/business/main.html',
      controller:'BusinessCtrl'
    })
    .state('app.count',{		/*资源统计*/
      url: '/count',
      templateUrl:'views/count/main.html',
      controller:'CountCtrl'
    })
    .state('app.recycle',{		/*回收站*/
    	url: '/recycle',
    	templateUrl:'views/recycle/main.html',
    	controller:'RecycleCtrl'
    })
    .state('app.work',{			/*工单管理*/
      url: '/work',
      templateUrl:'views/work/main.html',
      controller:'workCtrl'
    })
    .state('app.message',{			/*消息管理*/
      url: '/message',
      templateUrl:'views/message/main.html',
      controller:'MessagCtrl'
    })
    .state('app.record',{			/*备案管理*/
      url: '/record',
      templateUrl:'views/record/index.html',
      controller:'RecordCtrl',
      resolve:{
    	  cloudprojectList:function(eayunHttp){
    		  return getDcPrj(eayunHttp);
    	  }
      }
    })
	.state('app.log',{			/*操作日志*/
      url: '/log',
      templateUrl:'views/log/main.html',
      controller:'LogCtrl'
    })
    .state('app.obs',{			/*对象存储*/
      url: '/obs',
      templateUrl:'views/obs/obsmain.html',
      controller:'ObsCtrl',
      resolve: {
          isOpen: function (eayunHttp) {

              return eayunHttp.post('obs/obsOpen/getObsState.do').then(function (response) {
              	if(response.data!=null){
              		if(response.data.obsState=='1'){
              			return true;
              		}else{
              			return false;
              		}
              	}else{
              		return false;
              	}
              });
          }
      }
    })
    .state('app.costcenter',{			/*费用中心*/
        url: '/costcenter',
        templateUrl:'views/costcenter/main.html'
    })
    .state('app.invoice',{			/*发票管理*/
		url: '/invoice',
		templateUrl:'views/invoice/main.html',
		controller:'InvoiceCtrl'
	})
    .state('app.order',{            /*订单*/
          url: '/order',
          templateUrl:'views/order/main.html',
          controller : 'OrderCtrl',
          controllerAs : 'order'
      });
    //获取当前启用的公告
    function getNoticeList(eayunHttp){
    	return eayunHttp.post('sys/notice/getNoticeList.do').then(function(response){
    		return response.data.notice;
    	});
    };
    //获取当前位置中的信息
    /**查询当前登录用户的有效数据中心，即有项目且有权限*/
    function getDcPrj(eayunHttp){
    	return eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
	    	  return response.data.data;
	    });
    };
  })
/**
 * @ngdoc function
 * @name eayunApp.controller:NavbarCtrl
 * @description
 * # NavbarCtrl
 * 管理控制台导航条
 */
  .controller('NavbarCtrl', function ($scope, $timeout,eayunHttp,eayunModal,$state,$rootScope) {
    /** 定时刷新系统时间 */
	  eayunHttp.post('sys/notice/getNowTime.do').then(function(response){
		 $scope.today = response.data.nowTime;
	  });
    var getNowTime = function () {
    	$scope.today += 1000;
    	$timeout(getNowTime, 1000);
    };
    getNowTime();
  })
/**
 * @ngdoc function
 * @name eayunApp.controller:MainCtrl
 * @descriptionER
 * # MainCtrl
 * 左侧导航菜单
 */
  .controller('MainCtrl', function ($scope , eayunModal , eayunHttp , $state , $location,powerService) {
	powerService.powerRoutesList().then(function(powerList){
		$scope.modulePower = {
				isCloudView : powerService.isPower('host_view'),	//云主机
				isRoleMng : powerService.isPower('role_mng'),	//管理角色
				isCount : powerService.isPower('result_view'),	//资源统计
				isWorkView : powerService.isPower('work_view'),	//工单管理
				isMesgView : powerService.isPower('message_view'),	//消息查看
				isLogView : powerService.isPower('log_view'),		//日志查看
				isTagView : powerService.isPower('tag_view'),//标签管理
				isDiskView : powerService.isPower('disk_view'),//云硬盘
				isSnapView : powerService.isPower('snap_view'),//云硬盘备份
				isImageView : powerService.isPower('mirror_view'),//镜像
				isPublicImageView : powerService.isPower('pubimage_view'),//公共镜像
				isMarketImageView : powerService.isPower('marketimage_view'),//市场镜像
				isFireWallView:powerService.isPower('firewall_view'),//防火墙
				isSeGroup : powerService.isPower('group_view'),	//安全组查看功能//防火墙
				isAPIView : powerService.isPower('api_mng'),	//密钥管理
				isInvoiceView : powerService.isPower('invoice_list'), //发票管理
				isOrderView:powerService.isPower('order_list'), //订单管理
				isAccountView:powerService.isPower('account_view'),	//账户总览
				isReportView:powerService.isPower('report_view'),	//费用报表
				isRecycle:powerService.isPower('recycle_host_view') || powerService.isPower('recycle_disk_view') || powerService.isPower('recycle_snap_view')	//回收站
		};
	});
	$scope.user ='';
	var userInfo = sessionStorage["userInfo"];
	 if(userInfo){
		 var user = JSON.parse(userInfo);
		 if(user&&user.userId){
			 $scope.user = user;
		 }
	 }else{
		 $state.go("login", {}, {reload: true});
	 }
    $scope.selected = 'overview';
    $scope.isSelected = function (id) {
      return $scope.selected == id;
    };
    $scope.select = function (id) {
      $scope.selected == 'host';
    };
    /**
     * 监听当前用户未处理的报警信息数量的变化
     */
    $scope.$on("RefreshUnhandledAlarmMsgCount",function(event){
    	eayunHttp.post('monitor/alarm/getUnhandledAlarmMsgNumberByCusId.do').then(function(response){
    		$scope.unhandledAlarmMsgNumber = response.data;
    		if($scope.unhandledAlarmMsgNumber != 0){
    			$("#alarmdot").html($scope.unhandledAlarmMsgNumber > 99 ? '99+' : $scope.unhandledAlarmMsgNumber);
    			$("#alarmdot").addClass("alarmdot");
    		}else{
    			$("#alarmdot").html("");
    			$("#alarmdot").removeClass("alarmdot");
    		}
    	});
    });
    /**
     * 查询当前用户未处理的报警信息数量
     */
    eayunHttp.post('monitor/alarm/getUnhandledAlarmMsgNumberByCusId.do').then(function(response){
		$scope.unhandledAlarmMsgNumber = response.data;
		if($scope.unhandledAlarmMsgNumber != 0){
			$("#alarmdot").html($scope.unhandledAlarmMsgNumber > 99 ? '99+' : $scope.unhandledAlarmMsgNumber);
			$("#alarmdot").addClass("alarmdot");
		}
	});
    /**跳转报警信息*/
	  $scope.toUnMsgList = function(){
		  $state.go('app.monitor.monitorbar.alarm.list',{'signStatus':'untreated'});
	  };
    /**
     * 监听当前用户未读信息数量的变化
     */
    $scope.$on("RefreshUnreadMsgCount",function(event){
    	/*eayunHttp.post('sys/news/unreadCount.do',{"userAccount":$scope.user.userName}).then(function(response){
        	$scope.unreadCount = response.data;
        });*/
    	eayunHttp.post('sys/news/getUnreadList.do').then(function(response){
        	$scope.unreadList = response.data.unreadList;
        });
    });
    /**
     * 查询当前用户未读信息
     */
    eayunHttp.post('sys/news/unreadCount.do',{"userAccount":$scope.user.userName}).then(function(response){
    	$scope.unreadCount = response.data;
    });
    eayunHttp.post('sys/news/getUnreadList.do').then(function(response){
    	$scope.unreadList = response.data.unreadList;
    });
    /**
     * 查看详情
     */
	  $scope.openMsgDetail = function (data) {
		  $scope.news = data;
		  $state.go('app.message.msgDetail', {
				id : $scope.news.id
			}); // 跳转后的URL;
		// 
		
//		  var result = eayunModal.dialog({
//			  showBtn:false,
//			  title:'消息详情',
//			  width:'800px',
//			
//			  templateUrl:'views/message/timmsg.html',
//			  controller:'msgDetailCtrlone',
//			  controllerAs : 'model',
//			  resolve: {
//				  newsId:function(){
//					  if($scope.news.statu == 0){
//						  return eayunHttp.post('sys/news/statu.do',$scope.news).then(function(data){
//							  return data.data.id;
//						  });
//					  }
//					  return $scope.news.id;
//				  }
//			  },
//		  });
//		  result.then(function(){},function (){
//		  	  $scope.$broadcast("RefreshTitleUnreadMsgCount");
//			  eayunHttp.post('sys/news/unreadCount.do',{"userAccount":$scope.user.userName}).then(function(response){
//				  $scope.unreadCount = response.data;
//			  });
//			  eayunHttp.post('sys/news/getUnreadList.do').then(function(response){
//				  $scope.unreadList = response.data.unreadList;
//			  });
//		  });
	  };
    /**
     * 当前用户未处理的工单数量的变化
     */
    unHandleWorkCount();
    unHandleWorkNum();
    $scope.$on("RefreshUnHandleWorkCount",function(event){
    	unHandleWorkCount();
    	unHandleWorkNum();
    });
    function unHandleWorkCount(){
		eayunHttp.post('sys/work/unHandleWorkCount.do',{"userId":$scope.user.userId}).then(function(response){
			if(response.data==0){
				response.data=null;
			}
			$scope.unHandleWorkCount = response.data;
		});
    }
    function unHandleWorkNum(){
    	eayunHttp.post('sys/work/unHandleWorkNum.do',{"userId":$scope.user.userId}).then(function(response){
    		$scope.unHandleWorkNum = response.data;
    	});
    }
    
    /**
     * 退出
     */
    $scope.logout = function () {
    	eayunHttp.post('sys/login/logout.do',{}).then(function(response){
    		if(response.data&&response.data.respCode=='400000'){
    			sessionStorage.clear();
    			$state.go("login", {}, {reload: true});
    		}
        });
    };
    /**修改密码*/
    $scope.updatePassword = function (account) {
	  var result = eayunModal.open({
		  	backdrop:'static',
	        templateUrl: 'views/auth/user/editpassword.html',
	        controller: 'updatePasswordCtrl',
	        resolve: {
	        	userAccount : function () {
	                return account;
	            }
	        }
	      }).result;
	  result.then(function (passmodel) {
	        eayunHttp.post('sys/user/modifyPassword.do',passmodel).then(function(response){
	        	if(!response.data.code){
	        		eayunModal.successalert("个人密码修改成功，请重新登录").then(function () {
	        			eayunHttp.post('sys/login/logout.do',{}).then(function(response){/**退出接口*/
		        			if(response.data&&response.data.respCode=="400000"){
		        				sessionStorage.clear();
				        		$state.go("login");
		        			}
		        		});
	        		},function(){
	        			eayunHttp.post('sys/login/logout.do',{}).then(function(response){/**退出接口*/
		        			if(response.data&&response.data.respCode=="400000"){
		        				sessionStorage.clear();
				        		$state.go("login");
		        			}
		        		});
	        		});
	        	}
	        });
	      });
     };
   })
   .controller("msgDetailCtrl",function($scope, eayunHttp, eayunModal, model){
	   $scope.model = model;
	   $scope.$emit("RefreshUnreadMsgCount");
   })
   .controller('updatePasswordCtrl', function ($scope , eayunModal , eayunHttp , userAccount,$modalInstance) {
	   $scope.passKey = '';
		  $scope.getPassKey = function () {
		    	eayunHttp.post("sys/login/getPassKey.do", {}).then(function (response) {
		    		$scope.passKey = response.data;
		  	  });
		    };
		  $scope.getPassKey();
	  $scope.passmodel = {
			  userAccount : userAccount,
			  oldPassword : '',
			  newPassword : '',
			  secondPassword : ''
	  };
	  $scope.changeOld = function () {
		  $scope.checkcommit = true;
	  };
	  $scope.checkPassword = function (value) {	//校验旧密码
		  value = strEnc(value,$scope.passKey,'','');
		  return eayunHttp.post('sys/user/checkOldPassword.do',{oldPassword : value}).then(function(response){
			  return response.data;
	        });
	    };
	  $scope.IsSame = false;//两次输入密码是否一致
      $scope.checkNoSame = function () {
    	  $scope.IsSame = $scope.passmodel.secondPassword==$scope.passmodel.newPassword;//二次校验密码
	  };
	  $scope.checkIsSame = function () {
		  $scope.IsSame = $scope.passmodel.secondPassword==$scope.passmodel.newPassword;//二次校验密码
	  };
	    $scope.checkcommit = true;
	    /** 定义eayunModal弹出框的提交操作*/
	    $scope.cancel = function (){
			$modalInstance.dismiss();
		};
	    $scope.commit = function () {
	    	if($scope.passmodel.newPassword != $scope.passmodel.secondPassword){
	    		eayunModal.error("两次输入密码不一致，请重新输入");
	    		return;
	    	}
	    	$scope.passmodel.oldPass = strEnc($scope.passmodel.oldPassword,$scope.passKey,'','');
	    	$scope.passmodel.newPass = strEnc($scope.passmodel.newPassword,$scope.passKey,'','');
	    	eayunHttp.post('sys/user/checkOldPassword.do',{oldPassword : $scope.passmodel.oldPass}).then(function(response){
	    		$scope.checkcommit = response.data;
	    		if($scope.checkcommit){
	    			eayunModal.confirm('确认修改密码？').then(function () {
	    				$modalInstance.close($scope.passmodel);
	    		      });
	    		}
	    	});
	    };
  })
  .controller('EayunConfirmCtrl', function ($scope, $modalInstance, $timeout, msg, type, timeout) {
    $scope.msg = msg;
    $scope.type = type;
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
    $scope.ok = function () {
      $modalInstance.close(true);
    };
    if (timeout) {
      $timeout(function () {
        $scope.cancel();
      }, timeout, false);
    }
  })
  .controller('EayunModalCtrl', function ($scope,$state, eayunHttp,$modalInstance, options, template) {
    $scope.options = options;
    $scope.template = template;
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
    $scope.ok = function (data) {
      $modalInstance.close(data);
    };
    $scope.commit = function () {
      $scope.ok(true);
    };
  })
  /*占位*/
  .controller('CommonCtrl', function ($scope) {
  });
