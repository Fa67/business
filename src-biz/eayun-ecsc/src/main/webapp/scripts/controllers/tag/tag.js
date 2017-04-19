'use strict';
angular.module('eayunApp.controllers')
/**
 * @ngdoc function
 * @name appRouter
 * @description 配置路由 模块加载
 */
  .config(function ($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.when('/app/business', '/app/business/tag');
  $urlRouterProvider.when('/app/business/tag', '/app/business/tag/tagGroupList');
  	/** 定义路由，加载的页面放入上一级路由(即app.business)所加载的页面的data-ui-view里面 */
    $stateProvider
    .state('app.business.tag', {
    	url: '/tag',
    	templateUrl: 'views/tag/tagmng.html',
    	controller: 'TagMngCtrl'
    }).state('app.business.tag.tagGroupList',{
    	url:'/tagGroupList',
    	templateUrl:'views/tag/taggrouplist.html',
    	controller:'TagGroupListCtrl'
    }).state('app.business.tag.tagDetail',{
    	url:'/tagDetail/:tagGroupId',
    	templateUrl:'views/tag/taggroupdetail.html',
    	controller:'TagGroupDetailCtrl'
    });
  })
  
  /**
   *  为资源标记标签
   *  注：目前所有的controller都在服务启动时注册到了angular.module('eayunApp.controllers')中，那么在其他的页面也好，JS文件中也好，所有的controller完全共享。
   */
  .controller('TagResourceCtrl', function($scope,$state,toast,eayunModal,eayunHttp,resType, resId, $modalInstance){

      $scope.cancel = function () {
          $modalInstance.dismiss();
      };

      $scope.commit = function () {
          $modalInstance.close();
      };

	  $scope.resType = resType;
	  $scope.resId = resId;
	  $scope.tagGroupID = {};
	  $scope.tagID = {};
	  
	  //如果重新选择了标签组，则清空tagID这个字段
	  $scope.$watch('tagGroupID' , function(newVal,oldVal){
			if(newVal !== oldVal){
				  $scope.tagID = {};
			}
	  });
	  
	  $scope.tagGroupList = {};
	  eayunHttp.post('tag/getEnabledTagGroupList.do',{resType:resType}).then(function(response){
		  $scope.tagGroupList = response.data;
	  });
	  
	  $scope.tagList = {};
	  $scope.getTagListById = function(tagGroupId){
		  if(tagGroupId===''){
			  return;
		  }
		  eayunHttp.post('tag/getAvailableTagList.do',{tagGroupId : tagGroupId, resType:resType, resId:resId}).then(function(response){
			  $scope.tagList = response.data;
		  });
	  };
	  $scope.resourceTags = {};
	  eayunHttp.post('tag/getResourceTag.do',{resType:resType,resId:resId}).then(function(response){
		  $scope.resourceTags = response.data;
	  });
	  
	  /*关闭当前窗口*/
	  $scope.closeWindow=function(){
		  $scope.ok();
		  $scope.cancel();
	  };
	  
	  /*在资源标记标签页面，创建标签类别*/
	  $scope.createTagGroup=function(){
		  var result=eayunModal.open({
		        title: '创建标签类别',
		        width: '600px',
		        height: '400px',
		        templateUrl: 'views/tag/addtaggroup.html',
		        controller: 'createTagGroupCtrl',
		        resolve: {
		        }
		  });
		  result.result.then(function (taggroupModel) {
			  eayunHttp.post('tag/addTagGroup.do',taggroupModel).then(function(response){
				  toast.success('创建标签类别'+taggroupModel.abbreviation+'成功');
				  $scope.tagGroupID = response.data.id;
				  $scope.tagList = {};
				  eayunHttp.post('tag/getEnabledTagGroupList.do',{resType:resType}).then(function(response){
					  $scope.tagGroupList = response.data;
				  });
			  });
		  },function(){
			  $scope.tagGroupID = {};
		  });
	  };
	  
	  /*在资源标记标签页面，创建标签*/
	  $scope.createTag=function(tagGroupId){
		  var result=eayunModal.open({
		        title: '创建标签',
		        width: '600px',
		        height: '400px',
		        templateUrl: 'views/tag/addtag.html',
		        controller: 'createTagCtrl',
		        resolve: {
		        	tagGroupId : function () {
		                return tagGroupId;
		            },
		  			tagGroupList:function(){
		  				return eayunHttp.post('tag/getEnabledTagGroupListByCusID.do',{}).then(function(response){
		      			  	return response.data;
		      		   });
		  			}
		        }
		  });
		  result.result.then(function (tagModel) {
			  eayunHttp.post('tag/addTag.do',tagModel).then(function(response){
				  toast.success('创建标签'+tagModel.name+'成功');
				  
				  $scope.tagID = response.data.id;
				  eayunHttp.post('tag/getAvailableTagList.do',{tagGroupId : tagGroupId, resType:resType, resId:resId}).then(function(response){
					  $scope.tagList = response.data;
				  });
			  });
		  },function(){
			  $scope.tagID = {};
		  });
	  };
	  /*确认标记*/
	  $scope.doTagResource = function(resType, resId, tagGroupID, tagID){
		  if($scope.tagList.length==0){//TODO 和下面的无任何标签冲突，考虑如何折中。
			  eayunModal.info("当前标签类别下无可用标签，请添加后重试");
		  }else{
			  eayunHttp.post('tag/tagResource.do',{resType:resType,resId:resId,tagGroupId:tagGroupID,tagId:tagID}).then(function(response){
				  if(response.data.result){
					  if(!response.data.tagResource){
						  eayunModal.error('当前标签类别下无任何标签，请添加后重试');
					  }else{
						  $scope.tagID = "{}";
						  toast.success('标记资源成功');
						  eayunHttp.post('tag/getResourceTag.do',{resType:resType,resId:resId}).then(function(response){
							  $scope.resourceTags = response.data;
						  }); 
						  eayunHttp.post('tag/getAvailableTagList.do',{tagGroupId : tagGroupID, resType:resType, resId:resId}).then(function(response){
							  $scope.tagList = response.data;
						  });
					  }
				  }else{
					  eayunModal.error(response.data.message);
				  }
			  });
		  }
		  
	  };
	  /*取消标记*/
	  $scope.untagResource=function(tagId, tagGroupId, resType, resId){
		  eayunModal.confirm('确定要取消标记？').then(function () {
		        eayunHttp.post('tag/untagResource.do',{tagId : tagId, resType:resType, resId:resId}).then(function(response){
		        	if(response.data){
		        		toast.success('取消标记成功');
		        		eayunHttp.post('tag/getResourceTag.do',{resType:resType,resId:resId}).then(function(response){
							  $scope.resourceTags = response.data;
						  });
		        		eayunHttp.post('tag/getAvailableTagList.do',{tagGroupId : tagGroupId, resType:resType, resId:resId}).then(function(response){
							  $scope.tagList = response.data;
						  });
		        	}else{
		        		toast.error('response.data.message');
		        	}
		        });
		      });
	  };
	  
  })
  
  /**
	 * @ngdoc function
	 * @name eayunApp.controller:CloudhostCtrl
	 * @description # BusinessCtrl 业务管理 main.js—>可以做进入模块之前的一些公共操作
	 */
  .controller('BusinessCtrl', function ($scope , powerService) {
	  powerService.powerRoutesList().then(function(powerList){
		  $scope.modulePower = {
				  isSyssetup : powerService.isPower('syssetup_view'),//查看云主机型号
				  isSysMng : powerService.isPower('syssetup_mng'), 	//管理云主机型号
				  isTagView : powerService.isPower('tag_view'),	//查看标签
				  isTagMng : powerService.isPower('tag_mng')		//标签管理
		  };
	  });
  })
  /**
   * 标签类别列表控制器
   */
  .controller('TagGroupListCtrl', function ($scope,eayunHttp) {
	  $scope.tagGroupTable = {
			  source: 'tag/getTagGroupList.do',
		      api:{},
		      getParams: function () {
		        return {};
		      }
	  };
  })
  /**
   * 标签类别详情控制器
   */
  .controller('TagGroupDetailCtrl', function ($scope,$state,toast,eayunModal,eayunHttp,$stateParams) {
	//pop框方法
	  	$scope.hintDescShow = [];
	    $scope.openPopBox = function(obj){
	    	if(obj.type == 'tagDesc'){
	    		$scope.descShow = true;
	    	}
	    	$scope.ellipsis = obj.value;
	    };
	    $scope.closePopBox = function(type){
	    	if(type == 'tagDesc'){
	    		$scope.descShow = false;
	    	}
	    };
	    $scope.openTableBox = function(obj){
	    	if(obj.type == 'tagDesc'){
	    		$scope.hintDescShow[obj.index] = true;
	    	}
	    	$scope.ellipsis = obj.value;
	    };
	    $scope.closeTableBox = function(obj){
	    	if(obj.type == 'tagDesc'){
	    		$scope.hintDescShow[obj.index] = false;
	    	}
	    };
	  $scope.tagGroupTable = {
			  source: 'tag/getTagGroupById.do',
		      api:{},
		      getParams: function () {
		        return {
		        	tagGroupId : $stateParams.tagGroupId || ''
		        };
		      }
	  };
	  //分页的方式进行查询
	  $scope.tagTable = {
			  source: 'tag/getPagedTagList.do',
		      api:{},
		      getParams: function () {
		        return {
		        	tagGroupId : $stateParams.tagGroupId || ''
		        };
		      }
	  };
	  
	  //不采取分页的形式进行查询
//	  $scope.tagTable = {
//			  source: 'tag/getTagList.do',
//			  api:{},
//			  getParams: function () {
//				  return {
//					  tagGroupId : $stateParams.tagGroupId || ''
//				  };
//			  }
//	  };
	  
	  /*创建标签*/
	  $scope.createTag=function(tagGroupId){
		  var result=eayunModal.open({
		        title: '创建标签',
		        width: '600px',
		        height: '400px',
		        templateUrl: 'views/tag/addtag.html',
		        controller: 'createTagCtrl',
		        resolve: {
		        	tagGroupId : function () {
		                return tagGroupId;
		            },
		  			tagGroupList:function(){
		  				return eayunHttp.post('tag/getEnabledTagGroupListByCusID.do',{}).then(function(response){
		      			  	return response.data;
		      		   });
		  			}
		        }
		  });
		  result.result.then(function (tagModel) {
			  eayunHttp.post('tag/addTag.do',tagModel).then(function(response){
//				  eayunModal.success('保存成功');
				  toast.success('创建标签'+tagModel.name+'成功');
				  $scope.tagGroupTable.api.draw();
				  $scope.tagTable.api.draw();
			  });
		  });
	  };
	  /*编辑标签*/
	  $scope.editTag=function(tagId,tagGroupId){
		  var result=eayunModal.open({
		        title: '编辑标签',
		        width: '600px',
		        height: '400px',
		        templateUrl: 'views/tag/edittag.html',
		        controller: 'editTagCtrl',
		        resolve: {
		        	tagId : function () {
		                return tagId;
		            },
		            tagGroup : function(){
		            	 return eayunHttp.post('tag/getTagGroupById.do',{tagGroupId : tagGroupId}).then(function(response){
		           		  return response.data;
		                 });
		            }
		        }
		  });
		  result.result.then(function (editTagModel) {
			  eayunHttp.post('tag/editTag.do',editTagModel).then(function(response){
//				  eayunModal.success('保存成功');
				  toast.success('编辑标签'+editTagModel.name+'成功');
				  $scope.tagGroupTable.api.draw();
				  $scope.tagTable.api.draw();
			  });
		  });
	  };
	  /*删除标签*/
	  $scope.deleteTag = function(tagName, tagId, tagGroupId){
		  eayunModal.confirm('确定要删除标签'+tagName+'？').then(function () {
		        eayunHttp.post('tag/deleteTag.do',{tagId:tagId, tagGroupId:tagGroupId}).then(function(response){
		        	if(response.data){
//		        		eayunModal.success('删除成功');
		        		toast.success('删除标签成功');
		        		$scope.tagGroupTable.api.draw();
		        		$scope.tagTable.api.draw();
		        	}else{
		        		toast.error(response.data.message);
		        	}
		        });
		      });
	  };
	  /*查看已标记的资源*/
	  $scope.viewTaggedResource = function(tagId, tagGroupId){
		  var result=eayunModal.open({
		        title: '已标记资源列表',
		        width: '800px',
		        height: '600px',
		        templateUrl: 'views/tag/viewtaggedresource.html',
		        controller: 'viewTaggedResourceCtrl',
		        resolve: {
		        	tagId : function () {
		                return tagId;
		            },
		            tagGroupId: function(){
		            	return tagGroupId;
		            }
		        }
		  });
		  result.result.then(function () {
			  $scope.tagGroupTable.api.draw();
			  $scope.tagTable.api.draw();
		  },function(){
			  $scope.tagGroupTable.api.draw();
			  $scope.tagTable.api.draw();
		  });
	  };
	  
	  
	  /*标签测试页面*/
	  $scope.tagResource = function(resType, resId){
		  var result=eayunModal.open({
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
	 * 标签管理 页面Controller
	 */
  .controller('TagMngCtrl', function ($scope,$state,toast,eayunModal,eayunHttp) {
	  $scope.leftTagGroupList = {};
	  eayunHttp.post('tag/getTagGroupList.do',{}).then(function(response){
		  $scope.leftTagGroupList = response.data;
	  });

	  $scope.syncRedisWithDB = function(){
          eayunHttp.post('tag/syncRedisWithDB.do' ,{}).then(function(response){
          });
    };

	  
	  /*创建标签类别*/
	  $scope.createTagGroup=function(){
		  var result=eayunModal.open({
		        title: '创建标签类别',
		        width: '600px',
		        height: '400px',
		        templateUrl: 'views/tag/addtaggroup.html',
		        controller: 'createTagGroupCtrl',
		        resolve: {
		        }
		  });
		  result.result.then(function (taggroupModel) {
			  toast.success('创建标签类别'+taggroupModel.abbreviation+'成功');
			  eayunHttp.post('tag/addTagGroup.do',taggroupModel).then(function(response){
				  $state.go('app.business.tag',{},{reload:true});
			  });
		  });
	  };
	  /*编辑标签类别*/
	  $scope.editTagGroup=function(tagGroupId){
		  var result=eayunModal.open({
			  showBtn: false,
		        title: '编辑标签类别',
		        width: '600px',
		        height: '400px',
		        templateUrl: 'views/tag/edittaggroup.html',
		        controller: 'editTagGroupCtrl',
		        resolve: {
		        	tagGroupId : function () {
		                return tagGroupId;
		            },
		  			tagGroup: function(){
		  				return eayunHttp.post('tag/getTagGroupById.do',{tagGroupId : tagGroupId}).then(function(response){
		  			      return response.data;
		  			    });
		  			}
		        }
		  });
		  result.result.then(function (editTagGroupModel) {
			  eayunHttp.post('tag/editTagGroup.do',editTagGroupModel).then(function(response){
				  toast.success('编辑标签类别'+editTagGroupModel.abbreviation+'成功');
				  $state.go('app.business.tag',{},{reload:true});
			  });
		  });
	  };
	  /*删除标签类别*/
	  $scope.deleteTagGroup = function(tagGroupAbbr, tagGroupId, cusId){
		  eayunModal.confirm('确定要删除标签类别'+tagGroupAbbr+'?').then(function () {
		        eayunHttp.post('tag/deleteTagGroup.do',{tagGroupId : tagGroupId,cusId:cusId }).then(function(response){
		        	if(response.data){
		        		toast.success('删除标签类别成功');
		        	}else{
		        		toast.error(response.data.message);
		        	}
		        	$state.go('app.business.tag',{},{reload:true});
		        });
		      });
	  };
  })

  /*创建标签类别controller*/
  .controller('createTagGroupCtrl',function ($scope,eayunModal,eayunHttp,$modalInstance){
      $scope.cancel = function () {
          $modalInstance.dismiss();
      };

      $scope.commit = function () {
          $modalInstance.close($scope.taggroupModel);
      };

	  $scope.taggroupModel = {
			  	description:'',
				enabled : '1',
				unique : '0'
	  };
	  $scope.checkTagGroupName = function (value) {
		  $scope.nameflag=false;
		  var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  if(value.match(inputFormat)){
			  if(null != value && value !=""){
				  return eayunHttp.post('tag/checkTagGroupName.do',{tagGroupName : value}).then(function(response){
					  return response.data;
				  });
			  }else{
				  return false;
			  }
		  }else {
			  $scope.nameflag=true;
		  }
		  
	  };
	  $scope.checkTagGroupAbbr = function (value) {
		  $scope.abbrflag=false;
		  var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,8}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  if(value.match(inputFormat)){
			  if(null != value && value !=""){
				  return eayunHttp.post('tag/checkTagGroupAbbr.do',{tagGroupAbbr : value}).then(function(response){
					  return response.data;
				  });
			  }else{
				  return false;
			  }
		  }else {
			  $scope.abbrflag=true;
		  }
		  
	  };
	  
  })
  /*编辑标签类别controller*/
  .controller('editTagGroupCtrl',function ($scope,eayunModal,toast,eayunHttp,tagGroupId, tagGroup, $modalInstance){

      $scope.cancel = function () {
          $modalInstance.dismiss();
      };

      $scope.commit = function () {
          $modalInstance.close($scope.editTagGroupModel);
      };

	  $scope.editTagGroupModel = tagGroup;
	  $scope.checkTagGroupAbbr = function (value) {
		  $scope.abbrflag=false;
		  var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  if(value.match(inputFormat)){
			  if(null != value && value !=""){
				  return eayunHttp.post('tag/checkTagGroupAbbr.do',{tagGroupAbbr : value, tagGroupId:tagGroupId}).then(function(response){
					  return response.data;
				  });
			  }else{
				  return false;
			  }
		  }else {
			  $scope.abbrflag=true;
		  }
		  
	  };
	  
	  $scope.doCommit = function(){
		  //0.首先判断当前选中的是否启用是"是"还是"否"
		  //1.如果是否启用选中了否，首先检查如果当前标签类别下是否有标签已经标记了资源
		  //2.如果否，直接编辑即可
		  //3.如果是，执行$scope.commit时需要给出提示，并且先清空该标签类别下所有标签标记的资源，在进行更新标签类别
		  if($scope.editTagGroupModel.enabled==1){
              $scope.commit();
          }else{
			  if(tagGroup.tgrpResNum==0){
                  $scope.commit();
			  }else {
				  eayunModal.confirm('禁用后，该类别下的标签将会从资源已有标签中移除，是否立即禁用？').then(function () {
					  eayunHttp.post('tag/clearTagResByTagGroupId.do',{tagGroupId:tagGroupId}).then(function(response){
						  if(response.data){
                              $scope.commit();
						  }
					  });
					  
				  }, function () {
				  });
			  }
		  }
	  };
  })
  /*创建标签控制器*/
  .controller('createTagCtrl',function($scope,eayunModal,eayunHttp,tagGroupId,tagGroupList, $modalInstance){
      $scope.cancel = function () {
          $modalInstance.dismiss();
      };
      $scope.commit = function () {
          $modalInstance.close($scope.tagModel);
      };

	  $scope.tagGroupList = tagGroupList;
	  $scope.tagModel = {
			  groupId:tagGroupId
	  };
	  $scope.validate = function(){
		  $scope.addtag.name.$validate();  
	  };
	  
	  $scope.$watch('tagModel.groupId' , function(newVal,oldVal){
			if(newVal !== oldVal){
				$scope.validate();
			}
	  });
	  
	  $scope.checkTagName = function (tagGroupId, value) {
		  $scope.nameflag=false;
		  var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,8}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  if(value.match(inputFormat)){
			  if(null != value && value !=""){
				  return eayunHttp.post('tag/checkTagName.do',{tagGroupId:tagGroupId, tagName : value}).then(function(response){
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
  /*编辑标签controller*/
  .controller('editTagCtrl',function ($scope,eayunModal,eayunHttp,tagId,tagGroup,$modalInstance){

      $scope.cancel = function () {
          $modalInstance.dismiss();
      };
      $scope.commit = function () {
          $modalInstance.close($scope.editTagModel);
      };

	  eayunHttp.post('tag/getTagById.do',{tagId : tagId}).then(function(response){
	      $scope.editTagModel = response.data;
	  });
	  $scope.tagGroup = tagGroup;
	  $scope.tagGroupId = tagGroup.id;
	  $scope.checkTagName = function (value) {
		  $scope.nameflag=false;
		  var inputFormat = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
		  if(value.match(inputFormat)){
			  if(null != value && value !=""){
				  return eayunHttp.post('tag/checkTagName.do',{tagGroupId:$scope.tagGroupId, tagName:value, tagId:tagId}).then(function(response){
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
  .controller('viewTaggedResourceCtrl',function($scope,$state, toast, eayunHttp,eayunModal,tagId,tagGroupId,$modalInstance){
      $scope.cancel = function () {
          $modalInstance.dismiss();
          $state.go('app.business.tag.tagDetail',{'tagGroupId':tagGroupId},{reload:true});
      };

	  $scope.model = {
			  prjName : ''
	  };
	  $scope.tagResTable = {
			  source: 'tag/getPagedTagResourceList.do',
		      api:{},
		      getParams: function () {
		        return {
		        	tagId : tagId || '',
		        	prjName : $scope.model.prjName || '',
		        	
		        };
		      }
	  };
	  $scope.prjNames = {};
	  $scope.tagName = '';
	  eayunHttp.post('tag/getParamsByTagId.do',{tagId: tagId}).then(function(response){
		  $scope.tagName = response.data.tagName;
		  $scope.prjNames = response.data.prjNames;
	  });
	  $scope.dcStatus = [
			           		{pId: '', text: '所属数据中心（全部）'}
		           		];
	  $scope.dcItemClicked = function (item, event) {
		  	$scope.model.prjName=item.pId;
			$scope.tagResTable.api.draw();
	  };
	  $scope.$watch('prjNames' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		angular.forEach(newVal, function (value,key) {
	    			$scope.dcStatus.push({pId: value, text: value});
	    		});
	    	}
	    });
	  $scope.tagId=tagId;
	  $scope.tagGroupId = tagGroupId;
	  
	  $scope.getSpecificPrjList = function(){
		  $scope.$broadcast('a',$scope.tagResTable);
	  };
	  
	  $scope.closeWindow=function(tagGroupId){
		  $scope.cancel();
		  $state.go('app.business.tag.tagDetail',{'tagGroupId':tagGroupId},{reload:true});
	  };
	  
	  $scope.untagResource=function(tagId, tagGroupId, resType, resId){
		  eayunModal.confirm('确定要取消标记？').then(function () {
		        eayunHttp.post('tag/untagResource.do',{tagId : tagId, resType:resType, resId:resId}).then(function(response){
		        	if(response.data){
		        		toast.success('取消标记成功');
		        		$scope.$broadcast('a',$scope.tagResTable);
		        	}else{
		        		eayunModal.error('response.data.message');
		        	}
		        });
		  });
	  };
  });
