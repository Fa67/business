/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('ModifyConfigurationCtrl', ['$scope', '$modalInstance', 'item', 'RDSInstanceService', '$state', 'eayunModal', 'toast',
        function ($scope, $modalInstance, item, RDSInstanceService, $state, eayunModal, toast) {
    	
    	$scope.model = angular.copy(item);
    	
    	$scope.cancel = function (){
  		  	$modalInstance.dismiss();
  	  	};
  	  
	  	$scope.ok = function (){
	  		$modalInstance.close();
	  	};
	  	
	  	$scope.commit = function (){
	  		$scope.checkBtn = true;
	  		RDSInstanceService.modifyCondfiguration($scope.model).then(function (response) {
	  			$scope.ok();
				toast.success('正在更改配置文件',2000);
				$state.go('app.rds.detail.dbinstance',{"rdsId":$scope.model.rdsId});
	  		}, function (message) {
	  			$scope.cancel();
	  			if(message){
	  				eayunModal.warning(message);
	  			}
	  		});
	  	};
	  	/**
	  	 * 查询该数据库版本的所有配置文件
	  	 */
	    $scope.init = function(){
	    	$scope.checkBtn = false;
			RDSInstanceService.getConfigList($scope.model.versionId, $scope.model.prjId).then(function (response) {
				$scope.configurations = response.data;
			});
	    };
    	$scope.init();
    }]);