'use strict';
angular.module('eayunApp.controllers')

.config(function ($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.when('/app/monitor','/app/monitor/monitorbar');
	$urlRouterProvider.when('/app/monitor/monitorbar', '/app/monitor/monitorbar/resourcemonitor');
	$urlRouterProvider.when('/app/monitor/detail/resmntdetail', '/app/monitor/detail/resmntdetail/resmntimg');
    $stateProvider
    .state('app.monitor.monitorbar',{
    	url: '/monitorbar',
    	templateUrl: 'views/monitor/monitorbar.html',
    	controller:'MonitorBarCtrl'
    }).state('app.monitor.detail',{
    	url: '/detail/:detailType',
    	templateUrl: 'views/monitor/monitordetail.html',
    	controller: 'DetailRMCtrl'
    }).state('app.monitor.monitorbar.resourcemonitor', {
    	url: '/resourcemonitor',
    	templateUrl: 'views/monitor/resourcemonitor/indexmonitor.html',	//云主机、云数据库页签
    	controller: 'IndexMonitorCtrl'
    });
})
.controller('MonitorBarCtrl',function($scope){
	
})
.controller('IndexMonitorCtrl',function($scope){
	
})
.controller('DetailRMCtrl',function($scope,$stateParams){
	if($stateParams.detailType=='resourceMonitor'){
		$scope.route = 'app.monitor.monitorbar.resourcemonitor';
		$scope.name = '资源监控';
	}
	else if($stateParams.detailType=='rule'){
		$scope.route = 'app.monitor.monitorbar.alarm.rule';
		$scope.name = '报警规则';
	}
});