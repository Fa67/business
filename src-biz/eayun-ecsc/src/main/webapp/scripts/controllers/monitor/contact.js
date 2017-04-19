'use strict';
angular.module('eayunApp.controllers')

.config(function ($stateProvider, $urlRouterProvider) {
//	$urlRouterProvider.when('/app/monitor', '/app/monitor/contact');
	$urlRouterProvider.when('/app/monitor/monitorbar/contact', '/app/monitor/monitorbar/contact/contactList');
    $stateProvider
    .state('app.monitor.monitorbar.contact', {
    	url: '/contact',
    	templateUrl: 'views/monitor/contact/main.html'
    }).state('app.monitor.monitorbar.contact.list',{
    	url: '/contactList',
    	templateUrl: 'views/monitor/contact/contactmng.html',
    	controller: 'ContactListCtrl'
    }).state('app.monitor.monitorbar.contact.group',{
    	url: '/contactGroupList',
    	templateUrl: 'views/monitor/contact/contactgroupmng.html',
    	controller: 'ContactGroupCtrl'
    });
})
/**
 * 联系人管理Controller
 */
.controller('ContactListCtrl', function (eayunStorage,$scope,$state,eayunHttp,eayunModal,toast) {
	var navLists=eayunStorage.get('navLists');
	navLists.length=0;
	navLists.push({route:'app.monitor.monitorbar.contact',name:'联系人管理'});
	navLists.push({route:'app.monitor.monitorbar.contact.list',name:'管理联系人'});
	//查询联系人列表
	$scope.contactTable = {
			source: 'monitor/contact/getContactList.do',
			api:{},
			getParams: function () {
				return {
					name : $scope.contactName || ''
				};
			}
	};
	//创建联系人
	$scope.addContact = function(){
		var result=eayunModal.open({
		        templateUrl: 'views/monitor/contact/addcontact.html',
		        controller: 'createContactCtrl',
		        resolve: {
		        }
		  });
		result.result.then(function (contactModel) {
			  eayunHttp.post('monitor/contact/addContact.do',contactModel).then(function(response){
				  toast.success('创建联系人'+(contactModel.name.length>9?contactModel.name.substring(0,8)+'...':contactModel.name)+'成功');
				  $scope.contactTable.api.draw();
			  });
		  });
	};
	//编辑联系人
	$scope.editContact = function(contact){
		var result=eayunModal.open({
//			  showBtn: false,
//		        title: '编辑联系人',
//		        width: '600px',
//		        height: '400px',
		        templateUrl: 'views/monitor/contact/editcontact.html',
		        controller: 'editContactCtrl',
		        resolve: {
		        	contact:function(){
		        		return contact;
		        	}
		        }
		  });
		result.result.then(function (contactModel) {
			  eayunHttp.post('monitor/contact/editContact.do',contactModel).then(function(response){
				  if(response.data){
					  toast.success('编辑联系人'+(contactModel.name.length>9?contactModel.name.substring(0,8)+'...':contactModel.name)+'成功');
				  }
				  $scope.contactTable.api.draw();
			  });
		  }, function () {
			  $scope.contactTable.api.draw();
		  });
	};
	
	//删除联系人
	$scope.deleteContact = function(contact){
		eayunModal.confirm('确定要删除联系人'+contact.name+'吗？').then(function () {
	        eayunHttp.post('monitor/contact/deleteContact.do',{contactId:contact.id,contactName:contact.name}).then(function(response){
	        	if(response.data){
	        		toast.success('删除联系人成功');
	        		$scope.contactTable.api.draw();
	        	}else{
	        		eayunModal.error(response.data.message);
	        	}
	        });
		});
	};
	
	$scope.queryContact = function(){
		$scope.contactTable.api.draw();
	};
	
	$scope.isSmsNotify = function(smsNotify){
		if(smsNotify==1){
			return true;
		}else return false;
	};
	
	$scope.isMailNotify = function(mailNotify){
		if(mailNotify==1){
			return true;
		}else return false;
	};
	
	$scope.updateSmsSelection = function($event,contactId,name){
		var checkbox = $event.target;
		var isChecked = checkbox.checked?'1':'0';
		eayunHttp.post('monitor/contact/updateSmsSelection.do',{isChecked:isChecked, contactId: contactId,contactName:name}).then(function(response){
			if(response.data){  
				toast.success('更新通知方式成功');
				$scope.contactTable.api.draw();
			}
		});
	};
	
	$scope.updateMailSelection = function($event,contactId,name){
		var checkbox = $event.target;
		var isChecked = checkbox.checked?'1':'0';
		eayunHttp.post('monitor/contact/updateMailSelection.do',{isChecked:isChecked, contactId: contactId,contactName:name}).then(function(response){
			if(response.data){  
				toast.success('更新通知方式成功');
				$scope.contactTable.api.draw();
			}
		});
	};
})

.controller('createContactCtrl',function($scope,$state,eayunHttp,eayunModal,toast,$modalInstance){
	//重写确定 取消按钮方法
	$scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
    $scope.commit = function () {
        $modalInstance.close($scope.contactModel);
    };
    
	$scope.contactModel = {
			smsNotify:'1',
			mailNotify:'1'
	};
	$scope.checkContactName = function(value){
		$scope.nameflag=false;
		var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		if(value.match(inputFormat)){
			//TODO 同一个customer下重名校验
			if(null != value && value !=""){
				  return eayunHttp.post('monitor/contact/checkContactName.do',{contactId:$scope.contactModel.id, contactName : value}).then(function(response){
					  return response.data;
				  });
			  }else{
				  return false;
			  }
		}else {
			$scope.nameflag=true;
		}
		
	};
	
})

.controller('editContactCtrl',function($scope,$state,eayunHttp,eayunModal,toast, contact,$modalInstance){
	//重写确定 取消按钮方法
	$scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
    $scope.commit = function () {
        $modalInstance.close($scope.contactModel);
    };
    
	$scope.contactModel = contact;
	$scope.checkContactName = function(value){
		$scope.nameflag=false;
		var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		if(value.match(inputFormat)){
			if(null != value && value !=""){
				  return eayunHttp.post('monitor/contact/checkContactName.do',{contactId:$scope.contactModel.id,contactName : value}).then(function(response){
					  return response.data;
				  });
			  }else{
				  return false;
			  }
		}else {
			$scope.nameflag=true;
		}
		
	};
//	$scope.commit = function () {
//		$scope.ok($scope.contactModel);
//	};
	
})

/**
 * 联系组管理Controller
 */
.controller('ContactGroupCtrl', function (eayunStorage,$scope,$state,eayunHttp,eayunModal,toast) {
	var navLists=eayunStorage.get('navLists');
	navLists.length=0;
	navLists.push({route:'app.monitor.monitorbar.contact',name:'联系人管理'});
	navLists.push({route:'app.monitor.monitorbar.contact.group',name:'管理联系组'});
	//管理联系组页面中间的联系人，默认展示左侧默认分组的超级管理员的联系方式
	$scope.contactGroupName="default";
	$scope.contactTable = {
			source: 'monitor/contact/getContactListInGroup.do',
			api:{},
			getParams: function () {
				return {
					name : $scope.contactGroupName || ''
				};
			}
	};
	
	$scope.contactGroupList = {};
	eayunHttp.post('monitor/contact/getContactGroupList.do',{}).then(function(response){
		  $scope.contactGroupList = response.data;
	});
	
	$scope.contactsOutOfGroup = {};
	eayunHttp.post('monitor/contact/getContactListOutOfGroup.do',{contactGroupName:$scope.contactGroupName}).then(function(response){
		$scope.contactsOutOfGroup = response.data;
	});
	$scope.addContactGroup = function(){
		var result=eayunModal.open({
		    templateUrl: 'views/monitor/contact/addcontactgroup.html',
		    controller: 'createContactGroupCtrl',
		    resolve: {
		    }
		  });
		result.result.then(function (contactGroupModel) {
			eayunHttp.post('monitor/contact/addContactGroup.do',contactGroupModel).then(function(response){
				toast.success('创建联系组'+(contactGroupModel.name.length>9?contactGroupModel.name.substring(0,8)+'...':contactGroupModel.name)+'成功');
				eayunHttp.post('monitor/contact/getContactGroupList.do',{}).then(function(response){
					  $scope.contactGroupList = response.data;
				});
			});
		});
	};
	
	$scope.editContactGroup = function(ctcGrp){
		var result=eayunModal.open({
		    templateUrl: 'views/monitor/contact/editcontactgroup.html',
		    controller: 'editContactGroupCtrl',
		    resolve: {
		    	ctcGrp:function(){
	        		return ctcGrp;
	        	}
		    }
		  }).result;
		result.then(function (contactGroupModel) {
			eayunHttp.post('monitor/contact/editContactGroup.do',contactGroupModel).then(function(response){
				toast.success('编辑联系组'+(contactGroupModel.name.length>9?contactGroupModel.name.substring(0,8)+'...':contactGroupModel.name)+'成功');
				eayunHttp.post('monitor/contact/getContactGroupList.do',{}).then(function(response){
					  $scope.contactGroupList = response.data;
				});
			});
			$scope.contactGroupName = contactGroupModel.name;
		}, function () {
			eayunHttp.post('monitor/contact/getContactGroupList.do',{}).then(function(response){
				  $scope.contactGroupList = response.data;
			});
		});
	};
	
	//删除联系组
	$scope.deleteContactGroup = function(ctcGrp){
		eayunModal.confirm('确定要删除联系组'+ctcGrp.name+'吗？').then(function () {
	        eayunHttp.post('monitor/contact/deleteContactGroup.do',{contactGroupId:ctcGrp.id,groupName:ctcGrp.name}).then(function(response){
	        	if(response.data){
	        		//FIXME 这部分刷新为异步POST请求，可能较慢，以致左侧联系组列表的联系人数量未刷新
	        		eayunHttp.post('monitor/contact/getContactGroupList.do',{}).then(function(response){
	        			$scope.contactGroupList = response.data;
	        		});
	        		//删除联系组后，将左侧组列表索引指向默认，联系组指向默认，并刷新中间联系人列表和右侧组外联系人列表
	        		$scope.lineIndex = 'default';
	        		$scope.contactGroupName="default";
	        		$scope.contactTable.api.draw();
	        		eayunHttp.post('monitor/contact/getContactListOutOfGroup.do',{contactGroupName:$scope.contactGroupName}).then(function(response){
	        			$scope.contactsOutOfGroup = response.data;
	        		});
	        		toast.success('删除联系组成功');
	        	}else{
	        		eayunModal.error(response.data.message);
	        	}
	        });
		});
	};
	$scope.lineIndex = 'default';
	//获取默认联系组中的成员
	$scope.getContactInDefaultGroup = function(obj){
		$scope.contactGroupName = obj.value;
		$scope.contactTable.api.draw();
		eayunHttp.post('monitor/contact/getContactListOutOfGroup.do',{contactGroupName:obj.value}).then(function(response){
			$scope.contactsOutOfGroup = response.data;
		});
		$scope.lineIndex = obj.index;
	};
	
	$scope.removeContactFromGroup = function(contact){
		eayunModal.confirm('确定将联系人'+contact.name+'在组中移除吗？').then(function () {
			eayunHttp.post('monitor/contact/removeContactFromGroup.do',{groupId:contact.currentCtcGrpId,contactId:contact.id,groupName:$scope.contactGroupName}).then(function(response){
				if(response.data){
					toast.success('移除联系人成功');
					eayunHttp.post('monitor/contact/getContactListOutOfGroup.do',{contactGroupName:$scope.contactGroupName}).then(function(response){
						$scope.contactsOutOfGroup = response.data;
					});
					$scope.contactTable.api.draw();
					//FIXME 这部分刷新为异步POST请求，可能较慢，以致左侧联系组列表的联系人数量未刷新
	        		eayunHttp.post('monitor/contact/getContactGroupList.do',{}).then(function(response){
	        			$scope.contactGroupList = response.data;
	        		});
				}else{
					eayunModal.error(response.data.message);
				}
			});
		});
	};
	
	$scope.addToGroup = function(contact){
		eayunHttp.post('monitor/contact/addContct2Group.do',{contactGroupName:$scope.contactGroupName, contactId:contact.id}).then(function(response){
			if(response.data){
//				toast.success('向组内添加联系人'+(contact.name.length>6?contact.name.substring(0,5)+'...':contact.name)+'成功');
				$scope.contactTable.api.draw();
				eayunHttp.post('monitor/contact/getContactListOutOfGroup.do',{contactGroupName:$scope.contactGroupName}).then(function(response){
					$scope.contactsOutOfGroup = response.data;
				});
				eayunHttp.post('monitor/contact/getContactGroupList.do',{}).then(function(response){
        			$scope.contactGroupList = response.data;
        		});
			}else{
				eayunModal.error(response.data.message);
			}
		});
		
	};
	
})

.controller('createContactGroupCtrl',function($scope,$state,eayunHttp,eayunModal,toast,$modalInstance){
	//重写确定 取消按钮方法
	$scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
    $scope.commit = function () {
        $modalInstance.close($scope.contactGroupModel);
    };
    
	$scope.contactGroupModel = {};
	$scope.checkContactGroupName=function(value){
		$scope.nameflag=false;
		var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  if(value.match(inputFormat)){
			  if(null != value && value !=""){
				  return eayunHttp.post('monitor/contact/checkContactGroupName.do',{contactGroupId:$scope.contactGroupModel.id, contactGroupName : value}).then(function(response){
					  return response.data;
				  });
			  }else{
				  return false;
			  }
		  }else {
			  $scope.nameflag=true;
		}
	};
	
	  
//	$scope.commit = function () {
//		$scope.ok($scope.contactGroupModel);
//	};
	
})

.controller('editContactGroupCtrl',function($scope,$state,eayunHttp,eayunModal,toast,ctcGrp,$modalInstance){
	//重写确定 取消按钮方法
	$scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
    $scope.commit = function () {
        $modalInstance.close($scope.contactGroupModel);
    };
    
	$scope.contactGroupModel = ctcGrp;
	$scope.checkContactGroupName=function(value){
		$scope.nameflag=false;
		var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  if(value.match(inputFormat)){
			  if(null != value && value !=""){
				  return eayunHttp.post('monitor/contact/checkContactGroupName.do',{contactGroupId: $scope.contactGroupModel.id, contactGroupName : value}).then(function(response){
					  return response.data;
				  });
			  }else{
				  return false;
			  }
		  }else {
			  $scope.nameflag=true;
		}
	};
	
	
});