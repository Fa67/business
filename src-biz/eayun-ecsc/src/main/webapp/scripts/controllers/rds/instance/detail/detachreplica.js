/**
 * Created by eayun on 2017/2/23.
 */
'use strict';

angular.module('eayunApp.controllers')

    .controller('DetachReplicaCtrl', ['$scope', '$modalInstance', 'item', 'RDSInstanceService', 'toast', '$state', 
        function ($scope, $modalInstance, item, RDSInstanceService, toast, $state) {
    	
    	$scope.checkBtn = false; // 避免重复点击
    	$scope.model = angular.copy(item);
    	
    	$scope.cancel = function (){
  		  	$modalInstance.close();
  	  	};
  	    
	  	$scope.ok = function (){
	  		$modalInstance.close();
	  	};
	  	$scope.commit = function (){
	  		$scope.checkBtn = true;
			var rds = {
				prjId: $scope.model.prjId,
				isMaster: 1
			};
			RDSInstanceService.checkInstanceQuota(rds).then(function (response) {
				RDSInstanceService.detachReplica($scope.model).then(function (response){
					$scope.ok();
					toast.success('云数据库实例分离中',2000);
					$state.go('app.rds.instance');
				}, function (message) {
					$scope.cancel();
				});
			}, function (message) {
				vm.isError = true;
				if(message == 'OUT_OF_MASTER_QUOTA'){
					vm.errorMsg = '主库数量超过最大限额，可提交工单进行扩充';
				}
			});
	  	}
    }]);