'use strict';
angular.module('eayunApp.controllers')

.config(function ($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.when('/app/monitor/monitorbar/alarm','/app/monitor/monitorbar/alarm/alarmList/:signStatus');
    $stateProvider
    .state('app.monitor.monitorbar.alarm', {
    	url: '/alarm',
    	templateUrl: 'views/monitor/alarm/main.html',
    }).state('app.monitor.monitorbar.alarm.list',{
    	url: '/alarmList/:signStatus',
    	templateUrl: 'views/monitor/alarm/alarmmsgmng.html',
    	controller: 'AlarmMngCtrl'
    }).state('app.monitor.monitorbar.alarm.rule',{
    	url: '/alarmRule',
    	templateUrl: 'views/monitor/alarm/alarmrulemng.html',
    	controller: 'AlarmRuleCtrl'
    }).state('app.monitor.detail.ruledetail',{
    	url: '/ruledetail/:alarmRuleId',
    	templateUrl: 'views/monitor/alarm/detailalarmrule.html',
    	controller: 'DetailAlarmRuleCtrl'
    })
    ;
})
/**
 * @description # BusinessCtrl 业务管理 main.js—>可以做进入模块之前的一些公共操作
 */
.controller('MonitorCtrl', function(eayunStorage,$scope, powerService){
	$scope.navLists=[];
	eayunStorage.set('navLists',$scope.navLists);
	powerService.powerRoutesList().then(function(powerList){
		  $scope.modulePower = {
				  isContactView : powerService.isPower('contact_view'),//查看联系人管理
				  isRuleMng : powerService.isPower('alarm_rulemng'),    //管理报警规则
				  isResMonitor : powerService.isPower('monitor_view'),//资源监控查看
		  };
	});
})

.controller('AlarmMngCtrl',function(eayunStorage ,$scope, eayunModal, eayunHttp, toast,$stateParams,$state){
	var navLists=eayunStorage.get('navLists');
	  navLists.length=0;
	  navLists.push({route:'app.monitor.monitorbar.alarm',name:'报警管理'});
	  navLists.push({route:'app.monitor.monitorbar.alarm',name:'报警信息'});
	$scope.model = {
			vmName:'',
			dcName:'',
			alarmType:'',
			processedSign:'',
			monitorType:''
	};
	$scope.alarmMsgTable = {
			source: 'monitor/alarm/getAlarmMsgList.do',
			api:{},
			getParams: function () {
				return {
					vmName: $scope.model.vmName ,
					dcName: $scope.model.dcName ,
					alarmType: $scope.model.alarmType ,
					processedSign : $scope.model.processedSign,
					monitorType : $scope.model.monitorType
				};
			},
			isAllChecked:false
	};
	/**数据中心名称下拉*/
	$scope.dcNames = {};
	eayunHttp.post('monitor/alarm/getPrjNamesBySession.do',{}).then(function(response){
    	$scope.dcNames = response.data;
    });
	$scope.dcStatus = [
		           		{paramId: '', text: '数据中心（全部）'}
	           		];
	  $scope.dcItemClicked = function (item, event) {
		  	$scope.model.dcName=item.paramId;
		  	$scope.alarmMsgTable.api.draw();
	  };
	  $scope.$watch('dcNames' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		angular.forEach(newVal, function (value,key) {
	    			$scope.dcStatus.push({paramId: value, text: value});
	    		});
	    	}
	    });
	  /**资源类型&报警类型下拉*/
	  $scope.alarmTypeStatus = [
			{nodeId: '', name: '报警类型（全部）'}
	  ];
	  $scope.alarmTypeClicked = function (item, event) {
		  	$scope.model.alarmType=item.nodeId;
		  	$scope.alarmMsgTable.api.draw();
	  };
	  
	  $scope.resTypeStatus = [
			{monitorType: '', text: '资源类型（全部）'}
	  ];
	  $scope.resTypeItemClicked = function (item, event) {
		  	$scope.model.monitorType=item.monitorType;
		  	$scope.model.alarmType='';
		  	$scope.alarmMsgTable.api.draw();
		  	if(item.monitorType != ''){
		  		eayunHttp.post('monitor/alarm/getalarmtypebymonitor.do',{monitorType:item.monitorType}).then(function(response){
		  			response.data.unshift({nodeId: '', name: '报警类型（全部）',$$select:true});
		  			$scope.alarmTypeStatus = response.data;
		  			
			    });
		  	}else{
		  		$scope.atStatus=[];
		  		$scope.atStatus.unshift({nodeId: '', name: '报警类型（全部）',$$select:true});
		  		$scope.alarmTypeStatus = $scope.atStatus;
		  	}
	  };
	  $scope.monitorList={};
	  eayunHttp.post('monitor/alarm/getMonitorItemList.do',{}).then(function(response){
	    	$scope.monitorList = response.data;
	    });
	  $scope.$watch('monitorList' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		angular.forEach(newVal, function (value,key) {
	    			$scope.resTypeStatus.push({monitorType: value.nodeId, text: value.name});
	    		});
	    	}
	    });
	  /**报警标示下拉*/
	  $scope.signStatus = [
				           		{paramId: '', text: '报警标识（全部）'},
				           		{paramId: '0', text: '未处理'},
				           		{paramId: '1', text: '已处理'}
			           		];
	  $scope.signClicked = function (item, event) {
		  	$scope.model.processedSign=item.paramId;
		  	if(item.paramId===''){
		  		$state.go('app.monitor.monitorbar.alarm.list',{'signStatus':'all'});
		  	}else if(item.paramId==='0'){
		  		$state.go('app.monitor.monitorbar.alarm.list',{'signStatus':'untreated'});
		  	}else if(item.paramId==='1'){
		  		$state.go('app.monitor.monitorbar.alarm.list',{'signStatus':'handle'});
		  	}
	  };
	  if($stateParams.signStatus){
		  if($stateParams.signStatus==='untreated'){
			  $scope.model.processedSign='0';
			  angular.forEach($scope.signStatus, function (value, key) {
				  value.$$select = false;
				  if(value.paramId==='0'){
					  value.$$select = true;
				  }
	  			});
		  }else if($stateParams.signStatus==='all'){
			  $scope.model.processedSign='';
			  angular.forEach($scope.signStatus, function (value, key) {
				  value.$$select = false;
				  if(value.paramId===''){
					  value.$$select = true;
				  }
	  			});
		  }else if($stateParams.signStatus==='handle'){
			  $scope.model.processedSign='1';
			  angular.forEach($scope.signStatus, function (value, key) {
				  value.$$select = false;
				  if(value.paramId==='1'){
					  value.$$select = true;
				  }
	  			});
		  }
		  
	  }
	  
    $scope.selected = [];
    $scope.isSelected = function(msgId){
    	return $scope.selected.indexOf(msgId)>=0;
    };
    $scope.updateSelection = function($event, id){
        var checkbox = $event.target;
        var action = (checkbox.checked?'add':'remove');
        updateSelected(action,id);
    };
    var updateSelected = function(action,id){
        if(action == 'add' && $scope.selected.indexOf(id) == -1){
            $scope.selected.push(id);
        }
        if(action == 'remove' && $scope.selected.indexOf(id)!=-1){
            var idx = $scope.selected.indexOf(id);
            $scope.selected.splice(idx,1);
        }
    };
    
    function containsItem(array, item){
		var i = array.length;
		while(i--){
			if(array[i].contactName===item.contactName){
				return true;
			}
		}
		return false;
	}
    
    $scope.checkAll = function () {
    	if($scope.alarmMsgTable.isAllChecked){
    		angular.forEach($scope.alarmMsgTable.result, function (value, key) {
    			if(value.isProcessed!=1){
    				value.isChecked = $scope.alarmMsgTable.isAllChecked;
    				$scope.selected.push(value.id);
    			}
    		});
    	}else{
    		$scope.selected.splice(0,$scope.selected.length);
    		angular.forEach($scope.alarmMsgTable.result, function (value, key) {
    			if(value.isProcessed!=1){
    				value.isChecked = $scope.alarmMsgTable.isAllChecked;
    			}
    		});
    	}
	};
    
    $scope.eraseAlarmMsg = function(){
    	if($scope.selected.length == 0){
    		toast.error("请选择至少一条报警信息");
    		return;
    	}else {
    		eayunHttp.post('monitor/alarm/eraseAlarmMsgByIds.do',{checkedIds: $scope.selected}).then(function(response){
    	    	if(response.data){
    	    		toast.success("消除报警信息成功");
    	    		$scope.alarmMsgTable.api.draw();
    	    		$scope.$emit("RefreshUnhandledAlarmMsgCount");
    	    		$scope.selected.splice(0,$scope.selected.length);
    	    	}else{
    	    		eayunModal.error(response.data.message);
    	    	}
    	    });
    		$scope.alarmMsgTable.isAllChecked = false;
    	}
    };
    
    $scope.export2Excel = function(){
    	var explorer =navigator.userAgent;
		var browser = 'ie';
		if (explorer.indexOf("MSIE") >= 0) {
			browser="ie";
		}else if (explorer.indexOf("Firefox") >= 0) {
			browser = "Firefox";
		}else if(explorer.indexOf("Chrome") >= 0){
			browser="Chrome";
		}else if(explorer.indexOf("Opera") >= 0){
			browser="Opera";
		}else if(explorer.indexOf("Safari") >= 0){
			browser="Safari";
		}else if(explorer.indexOf("Netscape")>= 0) { 
			browser='Netscape'; 
		}
    	$("#file-export-iframe").attr("src", "monitor/alarm/export2Excel.do?browser="+browser);
    };
    $scope.queryAlarmMsg = function(){
    	$scope.alarmMsgTable.api.draw();
    };
	
})
.controller('AlarmRuleCtrl',function(eayunStorage ,$scope,$state, eayunModal, eayunHttp, toast){
	var navLists=eayunStorage.get('navLists');
	navLists.length=0;
	navLists.push({route:'app.monitor.monitorbar.alarm',name:'报警管理'});
	navLists.push({route:'app.monitor.monitorbar.alarm.rule',name:'报警规则'});
	//pop框方法
  	$scope.descShow = [];
  	
    $scope.openTableBox = function(obj){
    	if(obj.type == 'triggerCondition'){
    		$scope.descShow[obj.index] = true;
    	}
    	$scope.ellipsis = obj.value;
    };
    $scope.closeTableBox = function(obj){
    	if(obj.type == 'triggerCondition'){
    		$scope.descShow[obj.index] = false;
    	}
    };
    
    $scope.monitorItemList = {};
    eayunHttp.post('monitor/alarm/getMonitorItemList.do',{}).then(function(response){
    	$scope.monitorItemList = response.data;
    });
    
    $scope.model = {
    		monitorItemID:'',
    		alarmRuleName:''
	};
	$scope.alarmRuleTable = {
			source: 'monitor/alarm/getAlarmRuleList.do',
			api:{},
			getParams: function () {
				return {
					name : $scope.model.alarmRuleName,
					monitorItemID: $scope.model.monitorItemID
				};
			}
	};
	$scope.itemStatus = [
		           		{paramId: '', text: '监控项（全部）'}
	           		];
	  $scope.monitorItemClicked = function (item, event) {
		  $scope.model.monitorItemID=item.paramId;
			$scope.myTable.api.draw();
	  };
	  $scope.$watch('monitorItemList' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		angular.forEach(newVal, function (value,key) {
	    			$scope.itemStatus.push({paramId: value.nodeId, text: value.name});
	    		});
	    	}
	    });
	$scope.$watch('model.monitorItemID', function(newVal,oldVal){
    	if(newVal !== oldVal){
    		$scope.alarmRuleTable.api.draw();
    	}
    });
	
	$scope.queryAlarmRule = function(){
		$scope.alarmRuleTable.api.draw();
	};
	
	$scope.createAlarmRule = function(){
		var result=eayunModal.open({
		        title: '创建报警规则',
		        width: '750px',
		        height: '500px',
		        templateUrl: 'views/monitor/alarm/addalarmrule.html',
		        controller: 'createAlarmRuleCtrl',
		        resolve: {
		        }
		  });
		  result.result.then(function (params) {
			  eayunHttp.post('monitor/alarm/addAlarmRule.do',params).then(function(response){
				  toast.success('创建报警规则'+(params.alarmRuleModel.name.length>8?params.alarmRuleModel.name.substring(0,7)+'...':params.alarmRuleModel.name)+'成功');
				  $scope.alarmRuleTable.api.draw();
			  });
		  });
	};
	
	$scope.copyAlarmRule = function(alarmRule){
		eayunHttp.post('monitor/alarm/copyAlarmRule.do',alarmRule).then(function(response){
			if(response.data){
				toast.success('复制报警规则成功');
				$scope.alarmRuleTable.api.draw();
			}else{
				eayunModal.error(response.data.message);
			}
		});
	};
	
	$scope.deleteAlarmRule = function(alarmRule){
		if(alarmRule.alarmObjectNumber>0){
			eayunModal.confirm('该规则已关联报警对象，是否删除？').then(function () {
				eayunHttp.post('monitor/alarm/deleteAlarmRule.do',alarmRule).then(function(response){
					if(response.data){
						toast.success('删除报警规则成功');
						$scope.$emit("RefreshUnhandledAlarmMsgCount");
						$scope.alarmRuleTable.api.draw();
					}else{
						eayunModal.error(response.data.message);
					}
				});
			});
		}else{
			eayunModal.confirm('确定删除选定报警规则？').then(function () {
				eayunHttp.post('monitor/alarm/deleteAlarmRule.do',alarmRule).then(function(response){
					if(response.data){
						toast.success('删除报警规则成功');
						$scope.$emit("RefreshUnhandledAlarmMsgCount");
						$scope.alarmRuleTable.api.draw();
					}else{
						eayunModal.error(response.data.message);
					}
				});
			});
		}
	};
	
	$scope.showAlarmRuleDetail = function(alarmRuleId){
		$state.go('app.monitor.detail.ruledetail',{"detailType":"rule",'alarmRuleId':alarmRuleId}); // 跳转后的URL;
	};
})
/**报警规则详情*/
.controller('DetailAlarmRuleCtrl',function(eayunStorage ,$scope, eayunModal, eayunHttp, toast, $stateParams,$state){
	var navLists=eayunStorage.get('navLists');
	navLists.length=0;
	navLists.push({route:'app.monitor.monitorbar.alarm',name:'报警管理'});
	navLists.push({route:'app.monitor.monitorbar.alarm.rule',name:'报警规则'});
	navLists.push({route:'app.monitor.detail.ruledetail',name:'报警规则详情'});
	$scope.alarmRuleId = $stateParams.alarmRuleId;
	
	$scope.alarmRuleModel = {};
	eayunHttp.post('monitor/alarm/getAlarmRuleById.do',{alarmRuleId:$stateParams.alarmRuleId}).then(function(response){
		if(null==response.data){
			$state.go('app.monitor.monitorbar.alarm.rule');
		}else{
			$scope.alarmRuleModel = response.data;
			
			$scope.alarmObjectTable = {
					source: 'monitor/alarm/getAlarmObjectListByRuleId.do',
					api:{},
					getParams: function () {
						return {
							alarmRuleId : $stateParams.alarmRuleId,
							monitorType : $scope.alarmRuleModel.monitorItem
						};
					}
			};
			$scope.titlename = "主机名称";
		    if($scope.alarmRuleModel.monitorItem=="0008001"){
		    	$scope.titlename = "主机名称";
		    }else if($scope.alarmRuleModel.monitorItem=="0008002"){
		    	$scope.titlename = "实例名称";
		    }else if($scope.alarmRuleModel.monitorItem=="0008003"){
		    	$scope.titlename = "负载均衡名称";
		    }else if($scope.alarmRuleModel.monitorItem=="0008004"){
		    	$scope.titlename = "负载均衡名称";
		    }
		}
		
	});
	$scope.alarmContactTable = {
			source: 'monitor/alarm/getAlarmContactListByRuleId.do',
			api:{},
			getParams: function () {
				return {
					alarmRuleId : $stateParams.alarmRuleId
				};
			}
	};
	
	//pop框方法
  	//用于详情页或只有一行数据的展现
    $scope.openPopBox = function(obj){
    	if(obj.type == 'triggerCondition'){
    		$scope.hintTagShow = true;
    	}
    	$scope.ellipsis = obj.value;
    };
    $scope.closePopBox = function(type){
    	if(type == 'triggerCondition'){
    		$scope.hintTagShow = false;
    	}
    };
	
	$scope.editAlarmRule = function(alarmRuleId){
		var result=eayunModal.open({
		        title: '编辑报警规则',
		        width: '750px',
		        height: '500px',
		        templateUrl: 'views/monitor/alarm/editalarmrule.html',
		        controller: 'editAlarmRuleCtrl',
		        resolve: {
		        	alarmRuleParams:function(){
		        		return eayunHttp.post('monitor/alarm/getAlarmRuleParamsForEdit.do',{alarmRuleId:alarmRuleId}).then(function(response){
		      			  	return response.data;
			      		});
		        	}
		        }
		  });
		  result.result.then(function (params) {
			  eayunHttp.post('monitor/alarm/editAlarmRule.do',params).then(function(response){
				  toast.success('修改报警规则'+(params.alarmRuleModel.name.length>9?params.alarmRuleModel.name.substring(0,8)+'...':params.alarmRuleModel.name)+'成功');
				  //刷新报警规则详情
				  eayunHttp.post('monitor/alarm/getAlarmRuleById.do',{alarmRuleId:$stateParams.alarmRuleId}).then(function(response){
						$scope.alarmRuleModel = response.data;
				  });
			  });
		  });
	};
	
	$scope.addAlarmObject = function(alarmRuleId){
		var result=eayunModal.open({
	        title: '添加报警对象',
	        width: '850px',
	        height: '800px',
	        templateUrl: 'views/monitor/alarm/addalarmobject.html',
	        controller: 'addAlarmObjectCtrl',
	        resolve: {
	        	alarmRuleId:function(){
	        		return alarmRuleId;
	        	},
	        	monitorType:function(){
	        		return $scope.alarmRuleModel.monitorItem;
	        	},
	        	alarmRuleName:function(){
	        		return $scope.alarmRuleModel.name;
	        	},
	        	availableAlarmObjects:function(){
	        		return eayunHttp.post("monitor/alarm/getAvailableAlarmObjectsByCustomer.do",
	        				{alarmRuleId:alarmRuleId,monitorType : $scope.alarmRuleModel.monitorItem}).then(function(response){
						return response.data;
					});
	        	},
	        	selectedAlarmObjects:function(){
	        		return eayunHttp.post("monitor/alarm/getAlarmObjectListByRuleId.do",
	        				{alarmRuleId:alarmRuleId,monitorType : $scope.alarmRuleModel.monitorItem}).then(function(response){
						return response.data;
					});
	        	}
	        }
	  });
	  result.result.then(function (params) {
		  eayunHttp.post('monitor/alarm/addAlarmObject.do',params).then(function(response){
			  if(response.data){
				  toast.success('添加报警对象成功');
				  $scope.alarmObjectTable.api.draw();
			  }
		  });
	  });
	};
	
	$scope.deleteAlarmObject = function(alarmObject){
		alarmObject.alarmRuleName = $scope.alarmRuleModel.name;
		eayunModal.confirm('确认删除报警对象？').then(function () {
			eayunHttp.post('monitor/alarm/deleteAlarmObject.do',alarmObject).then(function(response){
				if(response.data){
					toast.success('删除报警对象成功');
					$scope.$emit("RefreshUnhandledAlarmMsgCount");
					$scope.alarmObjectTable.api.draw();
				}else{
					eayunModal.error(response.data.message);
				}
			});
		});
	};
	
	$scope.addAlarmContact = function(alarmRuleId){
		var result=eayunModal.open({
	        title: '添加报警联系人',
	        width: '640px',
	        height: '800px',
	        templateUrl: 'views/monitor/alarm/addalarmcontact.html',
	        controller: 'addAlarmContactCtrl',
	        resolve: {
	        	alarmRuleId:function(){
	        		return alarmRuleId;
	        	},
	        	alarmRuleName:function(){
	        		return $scope.alarmRuleModel.name;
	        	},
	        	availableContactGroups:function(){
	        		return eayunHttp.post("monitor/alarm/getAvailableContactGroupsList.do",{}).then(function(response){
						return response.data;
					});
	        	},
	        	selectedAlarmContacts:function(){
	        		return $scope.alarmContactTable.result;
	        	},
	        	allContactsList:function(){
	        		return eayunHttp.post("monitor/alarm/getAllAlarmContactList.do",{}).then(function(response){
						return response.data;
					});
	        	}
	        }
	  });
	  result.result.then(function (params) {
		  eayunHttp.post('monitor/alarm/addAlarmContact.do',params).then(function(response){
			  if(response.data){
				  toast.success('添加报警联系人成功');
				  $scope.alarmContactTable.api.draw();
			  }
		  });
	  });
	};
	
	$scope.unbindContact = function(alarmContact){
		alarmContact.alarmRuleName = $scope.alarmRuleModel.name;
		eayunModal.confirm('确认解绑报警联系人？').then(function () {
			eayunHttp.post('monitor/alarm/unbindContact.do',alarmContact).then(function(response){
				if(response.data){
					toast.success('解绑报警联系人成功');
					$scope.alarmContactTable.api.draw();
				}else{
					eayunModal.error(response.data.message);
				}
			});
		});
	};
	
})
/**创建报警规则*/
.controller('createAlarmRuleCtrl',function($scope, eayunModal, eayunHttp, toast, $modalInstance){
	$scope.alarmRuleModel = {
			monitorItem:''
	};
	
	$scope.monitorItemList = {};
	eayunHttp.post('monitor/alarm/getMonitorItemList.do',{}).then(function(response){
		  $scope.monitorItemList = response.data;
		  $scope.alarmRuleModel.monitorItem = $scope.monitorItemList[0].nodeId;//默认选中云主机
	});
	
	$scope.monitorZBList = {};
	$scope.$watch('alarmRuleModel.monitorItem', function(newVal,oldVal){
    	if(newVal !== oldVal){
    		$scope.triggerArray = [];
    		eayunHttp.post('monitor/alarm/getMonitorZBList.do',{monitorItemNodeId:$scope.alarmRuleModel.monitorItem}).then(function(response){
    			$scope.monitorZBList = response.data;
    		});
    	}
    });
	
	$scope.triggerModel = {};
	$scope.triggerArray = [];
	$scope.addAlarmTrigger =  function(){
		var triggerModel = angular.copy($scope.triggerModel);
		$scope.triggerArray.push(triggerModel);
	};
	$scope.del = function(index){
		$scope.triggerArray.splice(index,1);
	};
	
	$scope.checkRuleName = function(value){
		$scope.nameflag=false;
		var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		if(inputFormat.test(value)){
			return true;
		}else {
			$scope.nameflag=true;
		}
		
	};
	
	$scope.commit = function () {
		for(var i=0; i<$scope.triggerArray.length; i++){
			$scope.triggerArray[i].zb=$scope.triggerArray[i].zb.nodeId;
		}
		$scope.params = {
				alarmRuleModel : $scope.alarmRuleModel,
				triggerArray : $scope.triggerArray
		};
        $modalInstance.close($scope.params);
	};

    $scope.cancel = function () {
        $modalInstance.dismiss();
    };

})
/**编辑报警规则*/
.controller('editAlarmRuleCtrl',function($scope, eayunModal, eayunHttp, alarmRuleParams, $modalInstance){
	$scope.alarmRuleModel = alarmRuleParams.alarmRuleModel;
	$scope.monitorItemList = alarmRuleParams.monitorItemList;
	$scope.monitorZBList = alarmRuleParams.monitorZBList;
	$scope.triggerArray = alarmRuleParams.triggerArray;
	$scope.triggerModel = {};
	
	$scope.setTriggerUnit = function(trigger){
		angular.forEach($scope.monitorZBList,function(monitorZB){
			if(monitorZB.nodeId == trigger.zb){
				trigger.unit = monitorZB.zbUnit;
			}
		});
	};
	
	/*angular.forEach($scope.triggerArray,function(trigger){
		$scope.$watch('trigger.threshold',function(newVal,oldVal){
			console.log(newVal >= 0 && newVal <= 100);
			trigger.threshold = oldVal;
			var valid = /^[1-9]+[0-9]*]*$/;
			if(newVal >= 0 && newVal <= 100 && valid.test(newVal) || newVal == ''){
				trgger.threshold = newVal;
			}
		});
	});*/
	
	$scope.$watch($scope.triggerArray.length, function(newVal, oldVal){
		if(newVal!=oldVal){
			angular.forEach($scope.triggerArray,function(trigger){
				$scope.$watch('trigger.threshold',function(newVal,oldVal){
					trigger.threshold = oldVal;
					if(trigger.unit=='%'){
						var regex = /^([1]?\d{1,2})$/;
						if(newVal.match(regex)){
							trigger.threshold = newVal;
						}
					}else{
						var regex = /^[1-9]\d{0,4}$/;
						if(newVal.match(regex)){
							trigger.threshold = newVal;
						}
					}
				});
			});
		}
	});
	
	$scope.addAlarmTrigger =  function(){
		var triggerModel = angular.copy($scope.triggerModel);
		$scope.triggerArray.push(triggerModel);
	};
	$scope.del = function(index){
		$scope.triggerArray.splice(index,1);
	};
	
	$scope.checkRuleName = function(value){
		$scope.nameflag=false;
		var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		if(inputFormat.test(value)){
			return true;
		}else {
			$scope.nameflag=true;
		}
		
	};

    $scope.cancel = function () {
        $modalInstance.dismiss();
    };

	$scope.commit = function () {
		 $scope.params = {
				 alarmRuleModel : $scope.alarmRuleModel,
				 triggerArray : $scope.triggerArray
		 };
        $modalInstance.close($scope.params);
	};
	
})
/**添加报警对象*/
.controller('addAlarmObjectCtrl',function($scope, eayunModal, eayunHttp, alarmRuleId, monitorType,
		alarmRuleName ,availableAlarmObjects, selectedAlarmObjects, $modalInstance){
    $scope.alarmRuleId = alarmRuleId;
    $scope.titles = "主机名称,IP";
    $scope.values = "vmName,vmIp";
    if(monitorType=="0008001"){
    	$scope.titles = "主机名称,IP";
    }else if(monitorType=="0008002"){
    	$scope.titles = "实例名称,IP";
    }else if(monitorType=="0008003"){
    	$scope.titles = "负载均衡名称,IP";
    }else if(monitorType=="0008004"){
    	$scope.titles = "负载均衡名称,IP";
    }
    $scope.alarmObjectsList = new Array();
    if(availableAlarmObjects!=null && availableAlarmObjects.length>0){
        for(var i=0;i<availableAlarmObjects.length;i++){
            $scope.alarmObjectsList.push(availableAlarmObjects[i]);
        }
    }
    if(selectedAlarmObjects!=null && selectedAlarmObjects.length>0){
        //$scope.selectedNumber = selectedAlarmObjects.length;
        for(var i=0;i<selectedAlarmObjects.length;i++){
            selectedAlarmObjects[i].$$selected = true;
            $scope.alarmObjectsList.push(selectedAlarmObjects[i]);
        }
    }

    $scope.selectedAlarmObjectsList = new Array();
	$scope.commit = function () {
        for(var i=0;i<$scope.alarmObjectsList.length;i++){
            if($scope.alarmObjectsList[i].$$selected==true){
                $scope.selectedAlarmObjectsList.push($scope.alarmObjectsList[i]);
            }
        }

		$scope.params = {
		    alarmRuleId : $scope.alarmRuleId,
		    alarmRuleName : alarmRuleName,
		    monitorType : monitorType,
			selectedAlarmObject : $scope.selectedAlarmObjectsList
		};

        $modalInstance.close($scope.params);
	};

    $scope.cancel = function () {
        $modalInstance.dismiss();
    };

})
/**添加报警联系人*/
.controller('addAlarmContactCtrl', function($scope, eayunModal, eayunHttp, alarmRuleId,alarmRuleName, 
		availableContactGroups,selectedAlarmContacts, allContactsList, $modalInstance){
	$scope.selectedNumber = 0;
	$scope.allContactsList = allContactsList;
	
	$scope.alarmRuleId = alarmRuleId;
	$scope.availableContactGroups = new Array();
	$scope.totalAvailableContacts = 0;
	if(availableContactGroups!=null && availableContactGroups.length>0){
		for(var i=0;i<availableContactGroups.length;i++){
			$scope.availableContactGroups.push(availableContactGroups[i]);
			$scope.totalAvailableContacts+=availableContactGroups[i].contactList.length;
		}
	}
	$scope.selectedAlarmContacts = new Array();
	if(selectedAlarmContacts!=null && selectedAlarmContacts.length>0){
		$scope.selectedNumber = selectedAlarmContacts.length;
		for(var i=0;i<selectedAlarmContacts.length;i++){
			$scope.selectedAlarmContacts.push(selectedAlarmContacts[i]);
		}
	}
	
	//判断右侧已选列表是否已存在要选的报警联系人，如果要选的报警联系人不存在右侧已选列表则添加
	function containsItem(array, item){
		var i = array.length;
		while(i--){
			if(array[i].contactName===item.contactName){
				return true;
			}
		}
		return false;
	}
	
	$scope.addAllContactsIgnoringGroup = function(){
		for(var i=0; i<allContactsList.length; i++){
			$scope.add(allContactsList[i]);
		}
	};
	
	$scope.addAllContactsInGroup = function(contactList){
		//FIXME 整组添加也会执行expander的收起或者展开，需要FIX。
		for(var i=0; i<contactList.length; i++){
			$scope.add(contactList[i]);
		}
	};
	
	$scope.add = function(item){
		if(!containsItem($scope.selectedAlarmContacts, item)){
			$scope.selectedAlarmContacts.push(item);
			$scope.selectedNumber = $scope.selectedAlarmContacts.length;
		}
	};
	
	$scope.del = function(item){
		for(var i=0; i<$scope.selectedAlarmContacts.length;i++){
			if($scope.selectedAlarmContacts[i].contactId==item.contactId){
				$scope.selectedAlarmContacts.splice(i, 1);
				break;
//				$scope.selectedAlarmContacts.pop(item);
			}
		}
		$scope.selectedNumber = $scope.selectedAlarmContacts.length;
	};

    $scope.cancel = function () {
        $modalInstance.dismiss();
    };

	$scope.commit = function () {
		 $scope.params = {
			alarmRuleId : $scope.alarmRuleId,
			alarmRuleName : alarmRuleName,
			selectedAlarmContacts : $scope.selectedAlarmContacts
		 };
        $modalInstance.close($scope.params);
	};
	
});