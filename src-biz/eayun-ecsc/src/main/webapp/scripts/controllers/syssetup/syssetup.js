'use strict';
/**
 * angular.module(name, [requires], [configFn]);创建、获取、注册angular中的模块
 * name：字符串类型，代表模块的名称；
 * requires：字符串的数组，代表该模块依赖的其他模块列表，如果不依赖其他模块，用空数组；
 * configFn：用来对该模块进行一些配置。
 */
angular.module('eayunApp.controllers')
/**
 * @ngdoc function
 * @name appRouter
 * @description
 * 配置路由
 * 模块加载
 */
  .config(function ($stateProvider, $urlRouterProvider) {
	  /**定义路由，加载的页面放入上一级路由(即app.business)所加载的页面的data-ui-view里面*/
	  $stateProvider.state('app.business.syssetup', {	//配置路由
      url: '/syssetup',									//地址URL
      templateUrl: 'views/syssetup/sysmng.html',		//所转到页面
      controller: 'CloudmodelCtrl'						//操作controller
    });
  })

/**
 * 业务参数配置
 * controller操作
 */
  .controller('CloudmodelCtrl', function ($scope, eayunModal,eayunHttp,toast) {
	  var user = sessionStorage["userInfo"];
	  if(user){
 		 user = JSON.parse(user);
 		 };
 	$scope.model = {
 			userName : user.userName
 	};
	/**验证添加按钮是否可用*/
    $scope.checkNum = function () {
    	eayunHttp.post('syssetup/checkCloudNumlByCus.do',{}).then(function(response){
        	if(response.data == true){
        		$("#newModel").attr("disabled",false);
        	}else{
        		$("#newModel").attr("disabled",true);
        	}
        });
    };
    $scope.checkNum();
    /**定义模型myTable,js加载完成之后执行eayun_table标签的指令，得到myTable.result*/
    $scope.myTable = {
      source: 'syssetup/getModelListByCustomer.do',
      api:{},
      getParams: function () {}
    };
    /**新建云主机模型*/
    $scope.newModel = function () {
      var result = eayunModal.dialog({   		//打开对话框
        title: '新建云主机类型',
        width: '600px',
        templateUrl: 'views/syssetup/addsys.html',	//加载页面
        controller: 'CloudmodelAddCtrl',  		//执行CloudmodelAddCtrl
        resolve: {}
      });
      /**完成执行then(function(success),function(error))*/
      result.then(function (model) {
        eayunHttp.post('syssetup/addCloudModel.do',model).then(function(response){
        	if(!response.data.code){
        		if(response.data.errorMessage){
        			toast.error(response.data.errorMessage);
        		}else{
        			toast.success('创建类型'+(response.data.modelName.length>10?response.data.modelName.substring(0,9)+'...':response.data.modelName)+'成功');
        		}
        		$scope.myTable.api.draw();			//刷新模型列表
        		$scope.checkNum();
        	}
        });
      }, function () {
        //console.info('取消');
      });
    };
    /**
     * delete点击事件
     * @param modelId
     */
    $scope.deleteModel = function(modelId,modelName){
      eayunModal.confirm('确定要删除类型'+modelName+'？').then(function () {
        eayunHttp.post('syssetup/deleteCloudModel.do',{modelId : modelId,modelName : modelName}).then(function(response){
        	if(!response.data.code){
        		toast.success('删除类型成功');
            	$scope.myTable.api.draw();
            	$("#newModel").attr("disabled",false);
        	}
        });
      }, function () {
        //console.info('取消');
      });
    };
    /**编辑按钮事件*/
    $scope.updateModel = function (id) {
      var result = eayunModal.dialog({
    	showBtn: true,				//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
        title: '编辑云主机类型',
        width: '600px',
        templateUrl: 'views/syssetup/editsys.html',
        controller: 'CloudmodelEditCtrl',
        resolve: {
          modelId: function () {
            return id;
          }
        }
      });
      result.then(function (model) {
        eayunHttp.post('syssetup/updateCloudModel.do',model).then(function(response){
        	if(!response.data.code){
        		if(response.data.errorMessage){
        			eayunModal.error(response.data.errorMessage);
        		}else{
        			toast.success('更新类型'+(response.data.modelName.length>10?response.data.modelName.substring(0,9)+'...':response.data.modelName)+'成功');
        		}
        		$scope.myTable.api.draw();
        	}
        });
      });
    };
  /**默认子网模型定义*/
  $scope.subnetTable = {
	      source: 'syssetup/subnet/getProjectByCustomer.do',
	      api:{},
	      getParams: function () {
	        return {};
	      }
	  };
    
    
  })
/**
 * @ngdoc function
 * @name eayunApp.controller:CloudmodelAddCtrl
 * @description
 * # CloudmodelAddCtrl
 * 云主机--模板--新增模板
 */
  .controller('CloudmodelAddCtrl', function ($scope , eayunModal , eayunHttp) {
	  $scope.model = {
		      modelName: '',
		      modelVcpus: '',
		      modelRam: '',
		      modelCusid: ''
		    };
	  $scope.cpumodel = {};	//与修改时传回的cpu的value不同，修改时需根据核数确定nodeId
	  $scope.cpuList = {};
	  $scope.ramList = {};
	  eayunHttp.post('cloud/vm/getCpuList.do',{}).then(function(response){
		  $scope.cpuList = response.data;
		  $scope.cpumodel = $scope.cpuList[0];
		  eayunHttp.post('cloud/vm/getRamListByCpu.do',$scope.cpumodel.nodeId).then(function(response){
				  $scope.ramList = response.data;
				  $scope.model.modelRam = $scope.ramList[0].nodeName;
			  });
	  });
	/**定义模型，有以下属性*/
    $scope.checkName = function(value){		//校验重名
    	if(null != value && value !=""){
    		return eayunHttp.post('syssetup/checkNamelByCusAndName.do',
    				{modelId : '',modelName : value}).then(function(response){
    			return response.data;
    	    });
    	}else{
    		return false;
    	}
     };
     $scope.$watch('cpumodel' , function(newVal,oldVal){
	    	if(newVal !== oldVal){
	    		eayunHttp.post('cloud/vm/getRamListByCpu.do',$scope.cpumodel.nodeId).then(function(response){
					  $scope.ramModel = response.data;
					  $scope.ramList = response.data;
					  $scope.model.modelRam = $scope.ramList[0].nodeName;
				  });
	    	
	    	}
	    });
    /**所打开对话框的提交操作*/
    $scope.commit = function () {
    	$scope.model.modelVcpus = $scope.cpumodel.nodeName.substr(0, $scope.cpumodel.nodeName.length-1);
    	if($scope.model.modelRam.indexOf('M')!=-1){
    		$scope.model.modelRam = $scope.model.modelRam.substr(0, $scope.model.modelRam.length-2);
    	}else if($scope.model.modelRam.indexOf('G')!=-1){
    		$scope.model.modelRam = 1024*($scope.model.modelRam.substr(0, $scope.model.modelRam.length-2));
    	}
    	$scope.ok($scope.model);    //方法确认，传回参数
    };
  })
  /**编辑controller*/
  .controller('CloudmodelEditCtrl', function ($scope, modelId, eayunModal,eayunHttp) {
    eayunHttp.post('syssetup/findCloudModelById.do',{modelId : modelId}).then(function(response){
      $scope.model = {
        modelName: response.data.modelName,
        modelVcpus: response.data.modelVcpus,
        modelRam: response.data.modelRam,
        modelCusid: response.data.modelCusid,
        modelId: response.data.modelId,
      };
    });
	$scope.cpuList = {};
	$scope.ramList = {
			viewName:''
	};
    eayunHttp.post('cloud/vm/getCpuList.do',{}).then(function(response){
		  $scope.cpuList = response.data;
		  var nodeId = "";
		  angular.forEach($scope.cpuList, function (value,key) {
			  if($scope.model.modelVcpus == value.nodeName.substr(0, value.nodeName.length-1)){
				  nodeId = value.nodeId;
			  }
		  });
		  eayunHttp.post('cloud/vm/getRamListByCpu.do',nodeId).then(function(response){
				  $scope.ramList = response.data;
				  angular.forEach($scope.ramList, function (value,key) {
					  value.viewName = value.nodeName;
					  if(value.nodeName.indexOf('M')!=-1){
						  value.nodeName = value.nodeName.substr(0, value.nodeName.length-2);
				    	}else if(value.nodeName.indexOf('G')!=-1){
				    		value.nodeName = 1024*(value.nodeName.substr(0, value.nodeName.length-2));
				    	}
				  });
			  });
	  });
    $scope.checkName = function(value){
    	if(null != value && value !=""){
    		return eayunHttp.post('syssetup/checkNamelByCusAndName.do',
    				{modelId : $scope.model.modelId,modelName : value}).then(function(response){
    			return response.data;
    	    });
    	}else{
    		return false;
    	}
       };
   $scope.changeCPU = function(cpu){
	   	var nodeId = "";
		  angular.forEach($scope.cpuList, function (value,key) {
			  if(cpu == value.nodeName.substr(0, value.nodeName.length-1)){
				  nodeId = value.nodeId;
			  }
		  });
		  eayunHttp.post('cloud/vm/getRamListByCpu.do',nodeId).then(function(response){
				  $scope.ramList = response.data;
				  angular.forEach($scope.ramList, function (value,key) {
					  value.viewName = value.nodeName;
					  if(value.nodeName.indexOf('M')!=-1){
						  value.nodeName = value.nodeName.substr(0, value.nodeName.length-2);
				    	}else if(value.nodeName.indexOf('G')!=-1){
				    		value.nodeName = 1024*(value.nodeName.substr(0, value.nodeName.length-2));
				    	}
				  });
				  $scope.model.modelRam = $scope.ramList[0].nodeName;
			  });
   };
   $scope.$watch('model.modelId' , function(newVal,oldVal){
    	if(newVal !== oldVal){
    		var nodeId = "";
			angular.forEach($scope.cpuList, function (value,key) {
				if($scope.model.modelVcpus == value.nodeName.substr(0, value.nodeName.length-1)){
					nodeId = value.nodeId;
				}
			});
  		  	eayunHttp.post('cloud/vm/getRamListByCpu.do',nodeId).then(function(response){
  				  $scope.ramList = response.data;
  				  angular.forEach($scope.ramList, function (value,key) {
  					value.viewName = value.nodeName;
  					  if(value.nodeName.indexOf('M')!=-1){
  						  value.nodeName = value.nodeName.substr(0, value.nodeName.length-2);
  				    	}else if(value.nodeName.indexOf('G')!=-1){
  				    		value.nodeName = 1024*(value.nodeName.substr(0, value.nodeName.length-2));
  				    	}
  				  });
  			  });
    	}
    });
    /** 定义eayunModal弹出框的提交操作*/
    $scope.commit = function () {
    	$scope.ok($scope.model);
    };
  });

