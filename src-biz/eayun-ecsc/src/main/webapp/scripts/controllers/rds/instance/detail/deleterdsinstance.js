/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('DeleteRdsInstanceCtrl', ['$scope', '$modalInstance', 'item', 'RDSInstanceService', 'eayunModal', 'toast', '$state', 
        function ($scope, $modalInstance, item, RDSInstanceService, eayunModal, toast, $state) {
    	
    	$scope.checkBtn = false; // 避免重复点击
    	$scope.model = angular.copy(item);
    	
    	$scope.cancel = function (){
  		  	$modalInstance.dismiss();
  	  	};
  	    
	  	$scope.ok = function (){
	  		$modalInstance.close();
	  	};
	  	$scope.commit = function (){
	  		$scope.checkBtn = true;
	  		RDSInstanceService.deleteRdsInstance($scope.model).then(function (response){
				  toast.success('云数据库实例删除中',2000); 
				  $scope.ok();
				  $state.go('app.rds.instance');
	  		}, function (message) {
	  			$scope.cancel();
	  			if(message){
	  				eayunModal.warning(message);
	  			}
	  		});
	  	}
    }]);