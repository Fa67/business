'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
  .config(function ($stateProvider, $urlRouterProvider) {
	  $stateProvider.state('app.net.netbar.loadbalance.member', {//路由
      url: '/member',
      templateUrl: 'views/net/loadbalance/member/membermng.html',
      controller: 'MemberCtrl'
    });
  })
  
  .controller('MemberCtrl', function ($rootScope, $scope, eayunModal,eayunHttp ,$timeout,toast) {
	  var list = [{'router': 'app.net.netbar.loadbalance', 'name': '负载均衡'}];
	  $rootScope.navList(list, '成员');

	  $scope.myTable = {
			  source: 'cloud/loadbalance/member/getMemberList.do',
		      api:{},
		      getParams: function () {
		        return {
		        	prjId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '',
		        	dcId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
		        	name :  $scope.memberName || ''
			};
		      }
	   };
	  $scope.memJson = function (tagsStr){
		  var json ={};
		  if(tagsStr){
			  json= JSON.parse(tagsStr);
		  }
		  return json;
	  };
	//成员状态显示
		$scope.getMemberStatus = function(model){
		  $scope.memberStatusClass = '';
			if(model.memberStatus=='ACTIVE'){
				return 'green';
			}  
			else if(model.memberStatus=='ERROR'){
				return 'gray';
			}
			else if(model.memberStatus=='PENDING_CREATE'||model.memberStatus=='PENDING_UPDATE'||model.memberStatus=='PENDING_DELETE'){
				return 'yellow';
			}
		};
	//监视器[监视数据中心、项目id变化]
	  $scope.$watch('model.dcProject' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		$scope.myTable.api.draw();
	    	}
	    });
  //pop框方法
	  $scope.hintTagShow = [];
	  $scope.openTableBox = function(obj){
		  if(obj.type == 'tagName'){
			  $scope.hintTagShow[obj.index] = true;
		  }
		  $scope.ellipsis = obj.value;
	  };
	  $scope.closeTableBox = function(obj){
		  if(obj.type == 'tagName'){
			  $scope.hintTagShow[obj.index] = false;
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
	  * 如果列表中有中间状态的成员，间隔5s刷新列表
	  */
	  $scope.refreshList = function (){
		  $scope.myTable.api.draw();
	  };
	  //成员列表的状态字段，刷新页面
	  $scope.$watch("myTable.result",function (newVal,oldVal){
	    	if(newVal !== oldVal){
	    		if($scope.myTable.result!=null&&$scope.myTable.result.length>0){
	    			for(var i=0;i<$scope.myTable.result.length;i++){
	    				var status=$scope.myTable.result[i].memberStatus.toString().toLowerCase();
	    				if("active"!=status&&"error"!=status){
	    					$timeout($scope.refreshList,5000);
	    					break;
	    				}
	    				
	    			}
	    		}
	    	}
	    });
	  
	  
	  
	 /*查询成员*/
	  $scope.getMember = function(){
		  $scope.myTable.api.draw();
	  };
	  /*创建成员*/
	  $scope.createMember = function(){
		  var result = eayunModal.dialog({
			    showBtn: false,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: '创建成员',
		        width: '600px',
		        templateUrl: 'views/net/loadbalance/member/addmember.html',
		        controller: 'memberAddCtrl',
		        resolve: {
		        	prjId:function (){
	            		return {prjId:sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : ''};
	            	},
		            vmMemberList: function () {
		            	return eayunHttp.post('cloud/loadbalance/pool/getVmListByPrjId.do',{prjId:$scope.model.dcProject.projectId}).then(function(response){
		            		return response.data;
		            	});
		            },
		            poolList: function () {
		            	return eayunHttp.post('cloud/loadbalance/pool/getPoolsByDcIdAndPrjId.do',{dcId:$scope.model.dcProject.dcId,prjId:$scope.model.dcProject.projectId}).then(function(response){
		            		return response.data;
		            	});
		            }
		            
		            
		        }
		      });
		      result.then(function (value) {
		    	  //创建页面点击提交执行后台Java代码
		    	  eayunHttp.post('cloud/loadbalance/member/addMember.do',value).then(function(response){
		    		  //如果创建成功，刷新当前列表页
		    		  if(response.data.code!="010120"){
		    			  toast.success('添加成员'+response.data.memberAddress+' : '+response.data.protocolPort+'成功');
		    		  }
		    		  $scope.myTable.api.draw();
	              });
		      }, function () {
//		        console.info('取消');
		      });
	  };
	  $scope.editMember = function(item){
		  var result = eayunModal.dialog({
			    showBtn: false,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
		        title: '编辑成员',
		        width: '600px',
		        templateUrl: 'views/net/loadbalance/member/editmember.html',
		        controller: 'editMemberCtrl',
		        resolve: {
		        	item: function () {
		        	  return item;
		            },
		            poolList: function () {
		            	return eayunHttp.post('cloud/loadbalance/pool/getPoolsByDcIdAndPrjId.do',{dcId:item.dcId,prjId:item.prjId}).then(function(response){
		            		return response.data;
		            	});
			            }
		        }
		      });
		  //修改点击提交后
		  result.then(function (value) {
	    	  //创建页面点击提交执行后台Java代码
	    	  eayunHttp.post('cloud/loadbalance/member/updateMember.do',value).then(function(response){
	    		  if(response.data.code!="010120"){
	    			  toast.success('成员'+response.data.memberAddress+' : '+response.data.protocolPort+'修改成功');
	    		  };
	    		  $scope.myTable.api.draw();
        });
	    	  
	      }, function () {
//	        console.info('取消');
	      });  
	  };
	  /*删除成员*/
	  $scope.deleteMember = function (item){
		  eayunModal.confirm('确定要删除成员'+item.memberAddress+' : '+item.protocolPort+'?').then(function () {
		        eayunHttp.post('cloud/loadbalance/member/deleteMember.do',{dcId : item.dcId,prjId :item.prjId,memberId:item.memberId}).then(function(response){
		        	if(response.data.code!="010120"){
		        		toast.success('删除成员成功');
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
   *创建成员
   *
   */
   .controller('memberAddCtrl', function ($scope, eayunModal,eayunHttp ,prjId,vmMemberList, poolList) {
	 //直接将创建页面所有的项目放入当前的$scope.model中 开始
		  $scope.model={};
	   //将项目绑定addProject页面下拉框
		  $scope.vag = false;//设置未选VM成员时提交按钮置灰 
		  $scope.poolList=poolList;
		//循环显示当前位置的项目
		  eayunHttp.post('cloud/vm/getProListByCustomer.do').then(function (response){
			  $scope.projectList = response.data;
			  angular.forEach($scope.projectList, function (value, key) {
		        if(value.projectId == prjId.prjId){
		        	$scope.model.project=value;
		        	
		        }
			  });
		  });
		  
		  $scope.members=vmMemberList;
		  //用于add页面判断错误提示的显示
		  $scope.vmMemberList=vmMemberList;
		  
		//去除创建页面打开默认js验证空
		  $scope.model.poolId = null;
		  
		  
		  /*切换项目查询*/
		  $scope.getPoolAndVmList = function (){
			  $scope.model.poolId=null;
			  $scope.selectedRules = [];//清空已选的vm成员
			  eayunHttp.post('cloud/loadbalance/pool/getPoolsByDcIdAndPrjId.do',
					  {dcId:$scope.model.project.dcId,prjId:$scope.model.project.projectId}).then(function(response){
				  $scope.poolList = response.data;
          	  });
			  
			  eayunHttp.post('cloud/loadbalance/pool/getVmListByPrjId.do',
					  {prjId:$scope.model.project.projectId}).then(function(response){
				  $scope.members = response.data;
				  //用于add页面判断错误提示的显示
				  $scope.vmMemberList=response.data;
          	});
			  
		  };
		  
		  $scope.selectedRules=new Array();
		  $scope.newmember=new Array();
		  
		//点击增加规则
		  $scope.popRule= function(record){
		    	var arr=new Array();			    
		    	for(var i=0;i<$scope.members.length;i++){
		    		if(i==record){
		    			$scope.selectedRules.push($scope.members[i]);
		    			$scope.newmember.push($scope.members[i].vmIp);
		    			
		    		}else{
		    			arr.push($scope.members[i]);
		    		}
		    	}
		    	$scope.members =arr;
		    	//用于限制提交按钮置灰与高亮
		    	if($scope.newmember.length>0){
	        		$scope.vag=true;
	        	}else{
	        		$scope.vag=false;
	        	}
      	};
      //点击删除规则
      	$scope.romoveRule = function(row){			    	
	    	var Arrs=new Array();
	    	for(var i=0;i<$scope.selectedRules.length;i++){
	    		if(i==row){
	    			$scope.members.push($scope.selectedRules[i]);
	    			$scope.newmember.splice(i,1);
	    		}else{
	    			Arrs.push($scope.selectedRules[i]);
	    		}
	    		//用于限制提交按钮置灰与高亮
	    		if($scope.newmember.length>0){
	        		$scope.vag=true;
	        	}else{
	        		$scope.vag=false;
	        	}
	    	};
	    	$scope.selectedRules = Arrs;
	    };
	    
	  //直接将创建页面所有的项目放入当前的$scope.model中 结束
	    $scope.commit = function () {
	    	var rules=new Array();
        	for(var i=0;i<$scope.newmember.length;i++){
        		rules.push($scope.newmember[i]);
        	}
        	$scope.model.rules = rules.join(",");
	      $scope.ok($scope.model);
	      
	    };
	  
  })
    /**
 * @ngdoc function
 * @name eayunApp.controller:editMemberCtrl
 * @description
 * # editMemberCtrl
 * 成员列表页-->编辑成员
 */
  .controller('editMemberCtrl', function ($scope, eayunModal,eayunHttp,item,poolList) {
	  //将项目绑定addProject页面下拉框
	  $scope.model={};
	  //angular复制对象赋给新建页面的model
	  $scope.model=angular.copy(item);
	  $scope.poolList = poolList;
	  
	  
	  
	//直接将创建页面所有的项目放入当前的$scope.model中 结束
    $scope.commit = function () {
      $scope.ok($scope.model);
      
    };
   
  })
  
  
  
  ;
