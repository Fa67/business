'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $stateProvider.state('app.auth.usermanage', {//路由
      url: '/manage',
      templateUrl: 'views/auth/usermanage/usermng.html',
      controller: 'ManageCtrl'
    });
  })

/**
 * 用户管理
 * controller
 */
  .controller('ManageCtrl', function (eayunStorage,$scope, eayunModal,eayunHttp,toast) {
	  var navLists=eayunStorage.get('navLists');
	  navLists.length=0;
	  navLists.push({route:'app.auth.usermanage',name:'用户管理'});
	//pop框方法
	  $scope.hintDescShow = [];
	  $scope.openTableBox = function(obj){
		  if(obj.type == 'descName'){
			  $scope.hintDescShow[obj.index] = true;
		  }
		  $scope.ellipsis = obj.value;
	  };
	  $scope.closeTableBox = function(obj){
		  if(obj.type == 'descName'){
			  $scope.hintDescShow[obj.index] = false;
		  }
	  };
	  /**定义模型userMagTable,js加载完成之后执行eayun_table标签的指令，得到userMagTable.result*/
	  $scope.userMagTable = {
		      source: 'sys/user/getListByCustomer.do',
		      api:{},
		      getParams: function () {}
		    };
	  /**新建用户*/
	  $scope.addUser = function () {
		  var result = eayunModal.open({
		        backdrop:'static',
		        templateUrl: 'views/auth/usermanage/adduser.html',
		        controller: 'addUserCtrl',
		        resolve: {}
		      }).result;
		  result.then(function (addmodel) {
		        eayunHttp.post('sys/user/addUser.do',addmodel).then(function(response){
		        	if(response.data.code){
		        		eayunModal.error("创建用户失败");
			        	$scope.userMagTable.api.draw();
		        	}else{
		        		toast.success("创建用户成功");
			        	$scope.userMagTable.api.draw();
		        	}
		        });
		      },function(){
		    	  
		      });
	    };
	  /**编辑用户*/
	  $scope.updateUser = function (userId) {
		  var result = eayunModal.open({
			  	backdrop:'static',
		        templateUrl: 'views/auth/usermanage/edituser.html',
		        controller: 'updateUserCtrl',
		        resolve: {
		        	userId : function () {
		                return userId;
		            }
		        }
		      }).result;
		  result.then(function (usermodel) {
		        eayunHttp.post('sys/user/updateUserExplain.do',usermodel).then(function(response){
		        	if(!response.data.code){
		        		toast.success("修改用户"+(response.data.userAccount.length>10?response.data.userAccount.substring(0,9)+'...':response.data.userAccount)+"成功");
			        	$scope.userMagTable.api.draw();
		        	}
		        });
		      },function(){
		    	  
		      });
	    };
	    /**删除用户*/
	    $scope.deleteUser = function (model) {
	    	eayunModal.confirm('确定要删除用户'+model.userAccount+'？').then(function () {
	    		eayunHttp.post('sys/news/whetherHasCollect.do',{userAccount : model.userAccount}).then(function(response){
	            	$scope.isNews = response.data;
	            	if($scope.isNews){
			    		eayunModal.warning("用户下有收藏的消息，不允许删除");
			    	}else{
			    		eayunHttp.post('sys/work/unHandleWorkCount.do',{userId : model.userId,range : model.userId}).then(function(response){
			            	$scope.count = response.data;
			            	if($scope.count > 0){
					    		eayunModal.warning("用户下有正在处理的工单，不允许删除！");
					    	}else{
					    		eayunHttp.post('sys/user/deleteUser.do',{userId : model.userId,userAccount : model.userAccount}).then(function(response){
					            	toast.success("删除用户成功");
					            	$scope.userMagTable.api.draw();
					            });
					    	}
			            });
				    	
			    	}
	            });
	          }, function () {
	            //console.info('取消');
	          });
		    };
		/**重置密码*/
	    $scope.resetPassword = function (userId,account) {
	    	eayunModal.confirm('确定要重置用户'+account+'的密码？').then(function () {
	            eayunHttp.post('sys/user/resetPassword.do',{userId : userId,userAccount:account}).then(function(response){
	            	if(!response.data.code){
	            		toast.success("重置密码成功");
		            	$scope.userMagTable.api.draw();
	            	}
	            });
	          }, function () {
	            //console.info('取消');
	          });
		    };
		/**管理角色*/
	    $scope.manageUser = function (userId) {
			  var result = eayunModal.open({
				  	backdrop:'static',
			        templateUrl: 'views/auth/usermanage/mnguserrole.html',
			        controller: 'ManageUserCtrl',
			        resolve: {
			        	userId : function () {
			                return userId;
			            }
			        }
			      }).result;
			  result.then(function (usermodel) {
				  //radio模型可以直接得到选中的值
			        eayunHttp.post('sys/user/setUserRole.do',usermodel).then(function(response){
			        	if(!response.data.code){
			        		toast.success("更新"+(usermodel.userAccount.length>9?usermodel.userAccount.substring(0,8)+'...':usermodel.userAccount)+"的角色成功");
				        	$scope.userMagTable.api.draw();
			        	}
			        });
			      });
		    };
	    /**管理数据中心*/
	    $scope.manageProject = function (userId,Account) {
	    	/**获取用户已有的项目id*/
	    	$scope.prolistmodel = {};
		  	  eayunHttp.post('sys/userPrj/getListByUser.do',{userId : userId}).then(function(response){
		  		  $scope.prolistmodel = response.data;
			  		var result = eayunModal.open({
			  			backdrop:'static',
				        templateUrl: 'views/auth/usermanage/mngproject.html',
				        controller: 'ManageProCtrl',
				        resolve: {
				        	userId : function () {
				                return userId;
				            },
				            prolist : function () {
				                return $scope.prolistmodel;
				            }
				        }
				      }).result;
				  result.then(function (params) {
				        eayunHttp.post('sys/userPrj/setUserProjects.do',params).then(function(response){
				        	if(!response.data.code){
				        		toast.success("更新"+(Account.length>9?Account.substring(0,8)+'...':Account)+"的数据中心成功");
					        	$scope.userMagTable.api.draw();
				        	}
				        });
				      });
	        });
		    };
  })
/**
 * 按钮-controller
 */
  .controller('updateUserCtrl', function ($scope , eayunModal , eayunHttp , userId,$modalInstance) {
	  eayunHttp.post('sys/user/findUserById.do',{userId : userId}).then(function(response){
		  $scope.usermodel = response.data;
      });
	  $scope.cancel = function (){
			$modalInstance.close();
		};
	    /** 定义eayunModal弹出框的提交操作*/
	    $scope.commit = function () {
	    	$modalInstance.close($scope.usermodel);
	    };
  })
    .controller('addUserCtrl', function ($scope , eayunModal , eayunHttp,$modalInstance) {
    	$scope.addmodel = {
    			userAccount : '',
    			userExplain : '',
    			roleId : ''
    	};
    	$scope.addusermodel = {
    			isHaveCheck : false	//是否已选择角色
    	};
    	/**查询角色列表*/
  	    $scope.roleTable = {
  		      source: 'sys/role/getListByCustomer.do',
  		      api:{},
  		      getParams: function () {}
  		};
	  	$scope.$watch('addmodel.roleId' , function(newVal,oldVal){
			  if(newVal != oldVal){
				  if(newVal!=null && newVal!=''){
					  $scope.addusermodel.isHaveCheck = true;
				  }
			  }
		  });
    	$scope.checkAccount = function (value) {
    		if(null != value && value !=""){
        		return eayunHttp.post('sys/user/checkUserName.do',
        				{userName : value}).then(function(response){
        			return response.data;
        	    });
        	}else{
        		return false;
        	}
    	};
    	$scope.cancel = function (){
			$modalInstance.close();
		};
	    /** 定义eayunModal弹出框的提交操作*/
	    $scope.commit = function () {
	    	if($scope.addmodel.roleId == ''){
	    		eayunModal.error("请给新建用户分配角色");
	    		return;
	    	}
	    	$modalInstance.close($scope.addmodel);
	    };
  })
  /**管理角色*/
  .controller('ManageUserCtrl', function ($scope , eayunModal , eayunHttp , userId,$modalInstance) {
	  /**获取选择用户信息*/
	  $scope.usermodel = {};//此步也可不写，考虑到异步处理问题，响应未返回之前后面需要使用
	  eayunHttp.post('sys/user/findUserById.do',{userId : userId}).then(function(response){
		  $scope.usermodel = response.data;
      });
	  /**查询角色列表*/
	  $scope.roleTable = {
		      source: 'sys/role/getListByCustomer.do',
		      api:{},
		      getParams: function () {}
		    };
	  $scope.cancel = function (){
			$modalInstance.dismiss();
		};
    $scope.commit = function () {
    	$modalInstance.close($scope.usermodel);
    };
  })
  /**管理数据中心*/
  .controller('ManageProCtrl', function ($scope , eayunModal , eayunHttp , userId , prolist,$modalInstance) {
	  /**获取选择用户信息*/
	  eayunHttp.post('sys/user/findUserById.do',{userId : userId}).then(function(response){
		  $scope.usermodel = response.data;
      });
	  /**查询项目列表*/
	  $scope.proTable = {
		      source: 'cloud/project/getListByCustomer.do',
		      api:{},
		      getParams: function () {}
		    };
	  $scope.cancel = function (){
			$modalInstance.dismiss();
		};
	  /**默认选项*/
	  $scope.$watch('proTable.result' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.leastOne = false;
	    		$scope.proTable.isAllChecked = true;
	    		angular.forEach(newVal, function (value,key) {
	    			angular.forEach(prolist, function (pvalue,pkey) {
	    				if(value.projectId==pvalue.projectId){
	    					value.isCheck=true;
	    				}
	    	          });
	    			if(!value.isCheck){
	    				$scope.proTable.isAllChecked = false;
	    			}
	    			if(value.isCheck){
	    				$scope.leastOne = true;
	    			}
	              });
	    	}
	    });
	  
	/**单个按钮*/
	$scope.getChecked= function () {
		$scope.leastOne = false;
		$scope.proTable.isAllChecked = true;
		angular.forEach($scope.proTable.result, function (value,key) {
			if(!value.isCheck){
				$scope.proTable.isAllChecked = false;
			}
			if(value.isCheck){
				$scope.leastOne = true;
			}
        });
	 };
	/**全选（取消）按钮*/
	$scope.checkAll= function () {
		angular.forEach($scope.proTable.result, function (value,key) {
			value.isCheck = $scope.proTable.isAllChecked;
          });
		$scope.leastOne = $scope.proTable.isAllChecked;
	 };
	 /**提交*/
    $scope.commit = function () {
    	var checkedvalue = [];	//获取选中的值
		 angular.forEach($scope.proTable.result, function (value,key) {
			if(value.isCheck){
				checkedvalue.push(value.projectId);
			}
	     });
		 $scope.params = {
				 userId : $scope.usermodel.userId,
				 userAccount : $scope.usermodel.userAccount,
				 projectIds : checkedvalue
		 };
		 $modalInstance.close($scope.params);
    };
  });