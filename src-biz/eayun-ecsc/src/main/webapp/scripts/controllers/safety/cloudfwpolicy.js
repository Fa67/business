'use strict';
angular.module('eayunApp.controllers').config( function($stateProvider, $urlRouterProvider) {
	$stateProvider.state('app.safety.safetybar.firewall.policy', {
			      url: '/fwlPolicyList',
			      templateUrl: 'views/safety/firewallpolicy/fwpolicymng.html',
			      controller: 'FwPolicyList'
	 		});
		}).controller('FwPolicyList',function($scope,$state,eayunHttp,eayunModal,toast,powerService){
			//pop框方法
			  $scope.hintRuleShow = [];
			  $scope.openTableBox = function(obj){
				  if(obj.type == 'ruleName'){
					  $scope.hintRuleShow[obj.index] = true;
				  }
				  $scope.ellipsis = obj.value;
			  };
			  $scope.closeTableBox = function(obj){
				  if(obj.type == 'ruleName'){
					  $scope.hintRuleShow[obj.index] = false;
				  }
			  };
			 //查询列表
			$scope.myTable = {
				      source: 'safety/fwPolicy/getFwpList.do',
				      api : {},
				      getParams: function () {
					        return {
					        	prjId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : '',
					        	dcId :  sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).dcId : '',
					        	name :  $scope.fwpName || ''
						};
				      }
				    };
			
			 //权限控制
			  powerService.powerRoutesList().then(function(powerList){
				  $scope.buttonPower = {
					isCreate : powerService.isPower('firewall_add'),//创建防火墙
					isEdit : powerService.isPower('firewall_edit'),	//编辑防火墙
					delFireWall:powerService.isPower('firewall_drop'),//删防火墙
				 };
			  }); 
			
			 //监视器[监视数据中心、项目id变化]
			  $scope.$watch('model.projectvoe' , function(newVal,oldVal){
			    	if(newVal !== oldVal){
			    		$scope.myTable.api.draw();
			    	}
			    });
			  
			  //名称查询
			  $scope.queryFwPolicy = function(){
				  $scope.myTable.api.draw();
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
			     * Enter查询事件
			     */
			    $(function () {
			        document.onkeydown = function (event) {
			            var e = event || window.event || arguments.callee.caller.arguments[0];
			            if(!$scope.checkUser()){
			            	return ;
			            }
			            if (e && e.keyCode == 13) {
			          	  $scope.queryFwPolicy();
			            }
			        };
			    });
			
			  
			  
			  //删除策略
			    $scope.delFwPolicy = function (cloudFwp) {
			    	eayunModal.confirm('确定要删除策略'+cloudFwp.fwpName+'?').then(function () {
						eayunHttp.post("safety/fwPolicy/deleteFwp.do",cloudFwp).then(function(response){
							if(null!=response.data&&response.data==true){
								toast.success('删除策略'+(cloudFwp.fwpName.length>10?cloudFwp.fwpName.substring(0,9)+'...':cloudFwp.fwpName)+'成功',1000);
				        	 }
							$scope.myTable.api.draw();
						});
					});
			    };
			
			    
			//创建策略
			$scope.addFwPolicy= function () {
			      var result = eayunModal.dialog({
			    	//showBtn: false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
			        title: '创建防火墙策略',
			        width: '600px',
			        templateUrl: 'views/safety/firewallpolicy/addfwpolicy.html',
			        controller: 'AddFwPolicy',
			        resolve: {
			        	prjId:function (){
		            		return {prjId:sessionStorage["dcPrj"]?JSON.parse(sessionStorage["dcPrj"]).projectId : ''};
		            	}
			        }
			      });
			      result.then(function (value){
			    	  eayunHttp.post('safety/fwPolicy/addFwPolicy.do',value).then(function(response){
			    			 eayunModal.confirm('您刚刚创建了一个防火墙策略，是否现在为该策略绑定规则').then(function () {
			    				  $scope.toDoFwRule(response.data);
							 });
			    		  $scope.myTable.api.draw();
		              });
			      }, function () {
			        //console.info('取消');
			      });
			    };
			    
			    
			   
			    
			   
			    //编辑策略
			    $scope.updateFwPolicy = function (fwPolicy) {
				      var result = eayunModal.dialog({
				    	//showBtn: false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示) 
				        title: '编辑防火墙策略',
				        width: '600px',
				        templateUrl: 'views/safety/firewallpolicy/editfwpolicy.html',
				        controller: 'UpdateFwPolicy',
				        resolve: {
				          fwPolicy: function () {
				            return fwPolicy;
				          }
				        }
				      });
				      result.then(function (value){
				    	 eayunHttp.post('safety/fwPolicy/updateFwPolicy.do',value).then(function(response){
				    		 if(null!=response.data&&response.data==true){
				    			 toast.success('修改策略'+(value.fwpName.length>10?value.fwpName.substring(0,9)+'...':value.fwpName)+'成功',1000); 
				    		 }
				    		  $scope.myTable.api.draw();
			              });
				      }, function () {
				        //console.info('取消');
				      });
				    };
				    
				    
				    //管理规则
				    $scope.toDoFwRule= function (fwPolicy) {
					      var result = eayunModal.dialog({
					    	showBtn: false,
					        title: '管理规则',
					        width: '600px',
					        templateUrl: 'views/safety/firewallpolicy/selectrule.html',
					        controller: 'ToDoFwRule',
					        resolve: {
					          fwPolicy: function () {
					            return fwPolicy;
					          },
					          fwRules: function () {
						            return eayunHttp.post("safety/firewallrule/getFwRulesByPrjId.do",{dcId:fwPolicy.dcId,prjId:fwPolicy.prjId}).then(function(response){
										return response.data;
							        	
									});
						      },
					          fwRuleUsed: function () {
						            return eayunHttp.post("safety/firewallrule/getFwRulesByfwpId.do",fwPolicy).then(function(response){
										return response.data;
							        	
									});
						      }
					        }
					      });
					      result.then(function (value){
					    	 eayunHttp.post('safety/fwPolicy/toDoFwRule.do',value).then(function(response){
					    		 if(null!=response.data&&response.data==true){
					    			 toast.success('策略'+(value.fwpName.length>8?value.fwpName.substring(0,7)+'...':value.fwpName)+'修改规则成功',1000); 
					    		 }
					    		  $scope.myTable.api.draw();
				              });
					      }, function () {
					    	$scope.myTable.api.draw();
					        //console.info('取消');
					      });
					    };
				    

	}).controller('AddFwPolicy', function ($scope,prjId,eayunHttp,eayunModal) {
		  $scope.model={};
		  
		  eayunHttp.post('cloud/vm/getProListByCustomer.do').then(function (response){
			  $scope.projects = response.data;
			  angular.forEach($scope.projects, function (value, key) {
		        if(value.projectId == prjId.prjId){
		        	$scope.model.project=value;
		        }
			  });
		  });
		  
		  //校验名称格式和唯一性
		  $scope.checkFwpName = function (value) {
			  var nameTest=/^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
			  $scope.flag=false;
			  if(value.match(nameTest)){
				$scope.model.name=value;
				  return eayunHttp.post('safety/fwPolicy/getFwpByName.do',$scope.model).then(function(response){
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
			
			
			//切换项目校验重名
			$scope.changePrj=function(){
				$scope.myForm.name.$validate();
			};
			
		
			
			$scope.commit = function () {
			      $scope.ok($scope.model);
			  };
			

		}).controller('UpdateFwPolicy', function ($scope,fwPolicy,eayunHttp,eayunModal) {
			$scope.model= angular.copy(fwPolicy,{});
			//校验名称格式和唯一性
			  $scope.checkFwpName = function (value) {
				  var nameTest=/^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
				  $scope.flag=false;
				  if(value.match(nameTest)){
					$scope.model.fwpName=value;
					  return eayunHttp.post('safety/fwPolicy/getFwpByName.do',$scope.model).then(function(response){
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
			
			
		}).controller('ToDoFwRule', function ($scope,$state,fwPolicy,fwRules,fwRuleUsed,eayunHttp,eayunModal) {
			
			$scope.model=fwPolicy;
			//设置当前防火墙策略绑定的规则信息
  			$scope.fwRuleUsed=new Array();
  			if(fwRuleUsed!=null&&fwRuleUsed.length>0){
  				for(var i=0;i<fwRuleUsed.length;i++){
  					if(null!=fwRuleUsed[i].fwrId&&''!=fwRuleUsed[i].fwrId){
  						$scope.fwRuleUsed.push(fwRuleUsed[i]);
  					}
  				}
  			}
  			
  			//设置全部规则信息
  			$scope.fwRules = new Array();
  			if(fwRules!=null&&fwRules.length>0){
  				for(var i=0;i<fwRules.length;i++){
  					var flag=true;
  					for(var j=0;j<$scope.fwRuleUsed.length;j++){
  						if(fwRules[i].fwrId==$scope.fwRuleUsed[j].fwrId){
  							flag=false;
  							break;
  						}
  					}
  					if(flag){
  						$scope.fwRules.push(fwRules[i]);
  					}
  				}
  			}
			
			
  			$scope.add = function(item, index) {
  				$scope.fwRuleUsed.push(item);
  				$scope.fwRules.splice(index, 1);
  				$scope.model.firewallRules=$scope.fwRuleUsed;
	  	      };
	  	      
	  	      $scope.del = function(item, index) {
	  	    	$scope.fwRules.push(item);
	  	    	$scope.fwRuleUsed.splice(index, 1);
	  	    	$scope.model.firewallRules=$scope.fwRuleUsed;
	  	      };
			
			
			
			$scope.commit = function () {
			     $scope.ok($scope.model);
			  };
			  
			  $scope.goToFwr=function(){
				  $scope.cancel();
				  $state.go('app.safety.safetybar.firewall.rule',{},{reload:true});
			  };
			
		});