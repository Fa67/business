'use strict';
angular.module('eayunApp.controllers').config( function($stateProvider, $urlRouterProvider) {
	
	$stateProvider.state('app.obs.obsbar.obsUsed', {
			      url: '/obsUsedList',
			      templateUrl: 'views/obs/obsusedmng.html',
			      controller: 'obsUsedList'
	 		});
		}).controller('obsUsedList',function(eayunStorage,$scope,$state,$timeout,eayunHttp,eayunModal){
			var config=eayunStorage.get('config');
	    	config.route='app.obs.obsbar.obsUsed';
	    	config.name='资源报表';
	    	config.folderNameAddress='';
	    	config.bucketName='';
	    	config.folderName='';
			  var now = new Date();
			  $scope.today=now;
			  var startTime=new Date().setDate(now.getDate()-6);
			  $scope.data={
					  startTime:startTime,
					  endTime:new Date()
			  };
			  $scope.queryList= function(){
				  $scope.data.startTime.setHours(0, 0, 0, 0);
				  $scope.data.endTime.setHours(0, 0, 0, 0);
					if($scope.data.endTime==null&&$scope.data.startTime==null){
						eayunModal.error("请选择时间范围");
						return;
					}
					if($scope.data.startTime==null){
						eayunModal.error("请选择开始时间");
						return;
					}
					if($scope.data.endTime==null){
						eayunModal.error("请选择截止时间");
						return;
					}
					var ms = $scope.data.endTime.getTime()-$scope.data.startTime.getTime();
					  var days = ms/1000/60/60/24;
					  if(days >= 60){
						  eayunModal.error("时间范围不能大于60天");
						  return;
					  }
					  if(days < 0){
						  eayunModal.error("开始时间必须小于截止时间");
						  return;
					  }
					$scope.param = {
							startTime : $scope.data.startTime.getTime(),
							endTime : $scope.data.endTime.getTime()
					};
					eayunHttp.post('obs/used/getObsResources.do',$scope.param).then(function(response){
						$scope.typeList = response.data;
						var sumRequest=0;
						var sumLoad=0;
						var sumStorage=0;
						var sumCDN=0;
						var kb=1024;
						var mb=kb*1024;
						var gb=mb*1024;
						var tb=gb*1024;
						
						for(var i=0;i<response.data.length;i++){
							var requestSum=$scope.typeList[i].countRequest;
							var loadSum=$scope.typeList[i].download;
							var storageSum=$scope.typeList[i].storageUsed;
							var CDNSum=$scope.typeList[i].cdnFlow;
							
							sumRequest=sumRequest+requestSum;
							sumLoad=sumLoad+loadSum;
							sumStorage=sumStorage+storageSum;
							sumCDN = sumCDN+CDNSum;
						}
						
						if(sumRequest>10000){
							var countReq=(sumRequest/10000).toFixed(2);
							$scope.request=countReq+'万次';
						}else{
							var countReq=(sumRequest/100).toFixed(2);
							$scope.request=countReq+'百次';
						}
						
						if(sumLoad>=tb){
							var  countLoad=(sumLoad/tb).toFixed(2);
							$scope.load=countLoad+'TB';
						}else if(sumLoad>=gb&&sumLoad<tb){
							var  countLoad=(sumLoad/gb).toFixed(2);
							$scope.load=countLoad+'GB';
						}else if(sumLoad>=mb&&sumLoad<gb){
							var  countLoad=(sumLoad/mb).toFixed(2);
							$scope.load=countLoad+'MB';
						}else if(sumLoad>=kb&&sumLoad<mb){
							var  countLoad=(sumLoad/kb).toFixed(2);
							$scope.load=countLoad+'KB';
						}else{
							$scope.load=sumLoad+'B';
						}
						
						if(sumStorage>=gb){
							var  countStorage=(sumStorage/gb/$scope.typeList.length).toFixed(2);
							$scope.storage=countStorage+'TB/天';
						}else if(sumStorage>=mb&&sumStorage<gb){
							var  countStorage=(sumStorage/mb/$scope.typeList.length).toFixed(2);
							$scope.storage=countStorage+'GB/天';
						}else if(sumStorage>=kb && sumStorage<mb){
							var  countStorage=(sumStorage/kb/$scope.typeList.length).toFixed(2);
							$scope.storage=countStorage+'MB/天';
						}else{
							var  countStorage=(sumStorage/$scope.typeList.length).toFixed(2);
							if(sumStorage==0||null==sumStorage){
								countStorage=0;
							}
							$scope.storage=countStorage+'KB/天';
						}
						
						if(sumCDN>=tb){
							var  countCDN=(sumCDN/tb).toFixed(2);
							$scope.CDNStr=countCDN+'TB';
						}else if(sumCDN>=gb&&sumCDN<tb){
							var  countCDN=(sumCDN/gb).toFixed(2);
							$scope.CDNStr=countCDN+'GB';
						}else if(sumCDN>=mb&&sumCDN<gb){
							var  countCDN=(sumCDN/mb).toFixed(2);
							$scope.CDNStr=countCDN+'MB';
						}else if(sumCDN>=kb&&sumCDN<mb){
							var  countCDN=(sumCDN/kb).toFixed(2);
							$scope.CDNStr=countCDN+'KB';
						}else{
							$scope.CDNStr=sumCDN+'B';
						}						
						$scope.showCon=true;
				      });
					
			  };
			

			
		    
});
		
  
		
		
		
				