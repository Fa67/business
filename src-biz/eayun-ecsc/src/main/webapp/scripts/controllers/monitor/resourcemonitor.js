'use strict';
angular.module('eayunApp.controllers')

.config(function ($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.when('/app/monitor/monitorbar/resourcemonitor', '/app/monitor/monitorbar/resourcemonitor/vm');
	/*$urlRouterProvider.when('/app/monitor/detail','app/monitor/detail/resmntdetail');*/
	$urlRouterProvider.when('/app/monitor/detail/resmntdetail', '/app/monitor/detail/resmntdetail/resmntimg');
	$urlRouterProvider.when('/app/monitor/detail/:detailType/resmntdetail/resmntimg/:vmId', 
			'/app/monitor/detail/:detailType/resmntdetail/resmntimg/:vmId/monitorcpu');
    $stateProvider
    .state('app.monitor.monitorbar.resourcemonitor.vm', {
    	url: '/vm',
    	templateUrl: 'views/monitor/resourcemonitor/resmntmng.html',	//主机监控列表页
    	controller: 'ResourceMonitorMngCtrl',
    	resolve:{
      	  projects:function (eayunHttp){
      		  return eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
      	    	  return  response.data.data;
      	      });
      	  }
        }
    }).state('app.monitor.detail.resmntdetail',{
    	url: '/resmntdetail',
    	templateUrl: 'views/monitor/resourcemonitor/detailresmnt.html',	//左侧栏列表
    	controller: 'DetailResourceMonitorCtrl'
    }).state('app.monitor.detail.resmntdetail.resmntimg',{		//主机监控
    	url: '/resmntimg/:vmId',
    	templateUrl: 'views/monitor/resourcemonitor/imgresmnt.html',
    	controller: 'ImgResourceMonitorCtrl',
    })
    .state('app.monitor.detail.resmntdetail.resmntimg.monitorCPU',{//CPU
    	url: '/monitorcpu',
    	templateUrl: 'views/monitor/resourcemonitor/monitorCPU.html',
    	controller: 'GoMonitorCPUCtrl'
    })
    .state('app.monitor.detail.resmntdetail.resmntimg.monitorRam',{//内存
    	url: '/monitorram',
    	templateUrl: 'views/monitor/resourcemonitor/monitorRam.html',
    	controller: 'GoMonitorRamCtrl'
    })
    .state('app.monitor.detail.resmntdetail.resmntimg.monitorDisk',{//硬盘
    	url: '/monitordisk',
    	templateUrl: 'views/monitor/resourcemonitor/monitorDisk.html',
    	controller: 'GoMonitorDiskCtrl'
    })
    .state('app.monitor.detail.resmntdetail.resmntimg.monitorNet',{	//网络
    	url: '/monitornet',
    	templateUrl: 'views/monitor/resourcemonitor/monitorNet.html',
    	controller: 'GoMonitorNetCtrl'
    });
})
/**
 * 资源监控列表页
 */
.controller('ResourceMonitorMngCtrl',function(eayunStorage,$scope,$state,eayunHttp,projects,$interval,$location,$window){
	var navLists=eayunStorage.get('navLists');
	  navLists.length=0;
	  navLists.push({route:'app.monitor.monitorbar.resourcemonitor',name:'资源监控'});
	//项目列表
	$scope.cloudproList = projects;
	var monPrj = sessionStorage["dcPrj"];
	  if(monPrj){
		  monPrj = JSON.parse(monPrj);
		  angular.forEach($scope.cloudproList, function (value,key) {
			  if(value.projectId == monPrj.projectId){
				  $scope.ProModel = value;
			  }
		  });
	  }else{
		  $scope.ProModel = $scope.cloudproList[0];
		  $window.sessionStorage["dcPrj"] = JSON.stringify($scope.ProModel);
	  }
	$scope.params = {
			frequency : 20,		//刷新时间20秒
			vmName : ''
	};
	$scope.resourceTable = {
		      source: 'monitor/resourcemonitor/getvmListforMonitor.do',
		      api:{},
		      getParams: function () {
		        return {
		        	projectId : $scope.ProModel.projectId || '',
		        	vmName : $scope.params.vmName || ''
		        };
		      }
		  };
	var promise = null;
	$scope.updateClock = function(){
		if($location.absUrl().lastIndexOf("monitor/monitorbar/resourcemonitor/vm") != -1){
			$scope.params.frequency--;
			if($scope.params.frequency == 0){
				$scope.resourceTable.api.refresh();
				$scope.params.frequency = 20;
			}
		}else{
			$interval.cancel(promise);
		}
	};
	promise = $interval(function(){
		$scope.updateClock();
		},1000);
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
    $scope.query = function(){
    	$scope.params.frequency = 20;
    	$scope.resourceTable.api.draw();
    };
    $scope.$watch('ProModel.projectId' , function(newVal,oldVal){
    	if(newVal !== oldVal){
    		$scope.resourceTable.api.draw();
    		$window.sessionStorage["dcPrj"] = JSON.stringify($scope.ProModel);
    	}
    });
    //跳转主机监控详情页
	$scope.gotoResourceMonitorDetail = function(vmId){
		$state.go('app.monitor.detail.resmntdetail.resmntimg',{vmId:vmId,detailType:'resourceMonitor'}); // 跳转后的URL;
	};
})
.controller('DetailResourceMonitorCtrl',function($scope){
	
})

/**
 * 主机监控详情
 */
.controller('ImgResourceMonitorCtrl',function(eayunStorage,$scope,$stateParams,eayunHttp,$state){
	var navLists=eayunStorage.get('navLists');
	if(navLists){
		navLists.length=0;
		navLists.push({route:'app.monitor.monitorbar.resourcemonitor.vm',name:'资源监控'});
		navLists.push({route:'app.monitor.detail.resmntdetail.resmntimg',name:'资源监控详情'});
	}
	$scope.today = new Date();
	eayunHttp.post('monitor/resourcemonitor/getVmdetailById.do',{vmId:$stateParams.vmId}).then(function(response){
		$scope.VmModel = response.data;
	});
	$scope.data = {
		endTime : $scope.today,
		timeRange : 3,
		vmId : $stateParams.vmId
	};
	$scope.getData = function(type){
		var xTime = [];
		var yData = [];
		eayunHttp.post('monitor/resourcemonitor/getMonitorDataById.do',
				{vmId:$scope.data.vmId,endTime:$scope.data.endTime.getTime(),count:$scope.data.timeRange,type:type}).then(function(response){
			if(null == response.data){
				$state.go('app.monitor.monitorbar.resourcemonitor.vm');
			}else{
				$scope.dataModelList = response.data;
				angular.forEach($scope.dataModelList, function (value,key) {
					var time = new Date(value.timestamp).Format("MM-dd hh:mm");
					var newvalue = '';
					if(type=='cpu'){
						newvalue = value.cpu;
					}else if(type=='ram'){
						newvalue = value.ram;
					}
					xTime.push(time);
					yData.push(newvalue.toFixed(1));
				});
				$scope.pushChart(xTime,yData,type);
			}
			
		});
	};
	$scope.pushChart = function(xTime,yData,type){
		var title = '';
		if(type=='cpu'){
			title = 'CPU利用率(%)';
		}else if(type=='ram'){
			title = '内存占用率(%)';
		}
		require(
	            [
	                'echarts',
	                'echarts/chart/line', // 使用柱状图就加载bar模块，按需加载
	                'echarts/theme/macarons'	//主题
	            ],
	            function (ec) {
	                // 基于准备好的dom，初始化echarts图表
	                var myChart = ec.init(document.getElementById(type));
	                window.onresize = myChart.resize;
	                myChart.showLoading({
	                    text: '正在努力的读取数据中...',
	                });
	                myChart.hideLoading();
	                var option = {
	                	    /*title : {
	                	        text: title,
	                	    },*/
	                	    tooltip : {
	                	        trigger: 'axis'
	                	    },
	                	    toolbox: {
	                	        show : false,		//图标是否显示
	                	        feature : {
	                	            mark : {show: true},	//辅助图标
	                	            dataView : {show: true, readOnly: false},
	                	            magicType : {show: true, type: ['line']},
	                	            restore : {show: true},
	                	            saveAsImage : {show: true}
	                	        }
	                	    },
	                	    calculable : false,//false，是否启用拖拽重计算特性，默认关闭
	                	    grid :{
	                	    	x:40,
	                	    	y:10,
	                	    },
	                	    xAxis : [
	                	        {
	                	            type : 'category',
	                	            boundaryGap : false,//类目起始和结束两端空白策略，见下图，默认为true留空，false则顶头
	                	            data : xTime,
	                	            axisLabel : {
	                	                show:true,
	                	                interval: 0,
	                	                rotate:-30
	                	            },
	                	        }
	                	    ],
	                	    yAxis : [
	                	        {
	                	            type : 'value',
	                	            axisLabel : {		//坐标轴文本标签选项
	                	                formatter: '{value}'//间隔名称格式器
	                	            }
	                	        }
	                	    ],
	                	    series : [
	                	        {
	                	            name:title,
	                	            type:'line',
	                	            data:yData,
	                	            smooth: true		//false为line，true为平滑曲线
	                	        }
	                	    ]
	                	};
	                myChart.setOption(option);
	            }
	    );
	};
	
	//日期格式化。
	Date.prototype.Format = function(fmt){
	  var o = {   
	    "M+" : this.getMonth()+1,                 //月份   
	    "d+" : this.getDate(),                    //日   
	    "h+" : this.getHours(),                   //小时   
	    "m+" : this.getMinutes(),                 //分   
	    "s+" : this.getSeconds(),                 //秒   
	    "q+" : Math.floor((this.getMonth()+3)/3), //季度   
	    "S"  : this.getMilliseconds()             //毫秒   
	  };   
	  if(/(y+)/.test(fmt))   
	    fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));   
	  for(var k in o)   
	    if(new RegExp("("+ k +")").test(fmt))   
	  fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));   
	  return fmt;   
	};
})
.controller('GoMonitorCPUCtrl',function($scope,eayunHttp){//CPU
	$scope.getData("cpu");
	$scope.$watch('data.endTime+data.timeRange' , function(newVal,oldVal){
		  if(newVal != oldVal){
			  $scope.getData("cpu");
		  }
	  });
})
.controller('GoMonitorRamCtrl',function($scope){//内存
	$scope.getData("ram");
	$scope.$watch('data.endTime+data.timeRange' , function(newVal,oldVal){
		  if(newVal != oldVal){
			  $scope.getData("ram");
		  }
	  });
})
.controller('GoMonitorDiskCtrl',function($scope,eayunHttp){//硬盘
	var xTime = [];
	var yRead = [];
	var yWrite = [];
	$scope.$watch('data.endTime+data.timeRange' , function(newVal,oldVal){
		  if(newVal != oldVal){
			  xTime = [];
			  yRead = [];
			  yWrite = [];
			  $scope.firstData();
		  }
	  });
	$scope.firstData = function(){
		eayunHttp.post('monitor/resourcemonitor/getMonitorDataById.do',
				{vmId:$scope.data.vmId,endTime:$scope.data.endTime.getTime(),count:$scope.data.timeRange,type:'read'}).then(function(response){
			if(null == response.data){
				$state.go('app.monitor.monitorbar.resourcemonitor');
			}else{
				$scope.dataModelList = response.data;
				angular.forEach($scope.dataModelList, function (value,key) {
					var time = new Date(value.timestamp).Format("MM-dd hh:mm");
					var read = value.diskRead;
					xTime.push(time);
					yRead.push(read.toFixed(4));
				});
				$scope.secondData();
			}
		});
	};
	$scope.firstData();
	$scope.secondData = function(){
		eayunHttp.post('monitor/resourcemonitor/getMonitorDataById.do',
				{vmId:$scope.data.vmId,endTime:$scope.data.endTime.getTime(),count:$scope.data.timeRange,type:'write'}).then(function(response){
			if(null == response.data){
				$state.go('app.monitor.monitorbar.resourcemonitor');
			}else{
				$scope.dataModelList = response.data;
				angular.forEach($scope.dataModelList, function (value,key) {
					var write = value.diskWrite;
					yWrite.push(write.toFixed(4));
				});
				$scope.pushChart(xTime,yRead,yWrite);
			}
		});
	};
	$scope.pushChart = function(xTime,yRead,yWrite){
		require(
	            [
	                'echarts',
	                'echarts/chart/line' // 使用柱状图就加载bar模块，按需加载
	            ],
	            function (ec) {
	                // 基于准备好的dom，初始化echarts图表
	                var myChart = ec.init(document.getElementById('disk'));
	                window.onresize = myChart.resize;
	                myChart.showLoading({
	                    text: '正在努力的读取数据中...',
	                });
	                myChart.hideLoading();
	                var option = {
	                	    /*title : {
	                	        text: '磁盘流量(MB/s)',
	                	    },*/
	                	    tooltip : {
	                	        trigger: 'axis'
	                	    },
	                	    legend: {
	                	        data:['磁盘读吞吐','磁盘写吞吐']
	                	    },
	                	    toolbox: {
	                	        show : false,		//图标是否显示
	                	        feature : {
	                	            mark : {show: true},	//辅助图标
	                	            dataView : {show: true, readOnly: false},
	                	            magicType : {show: true, type: ['line']},
	                	            restore : {show: true},
	                	            saveAsImage : {show: true}
	                	        }
	                	    },
	                	    calculable : false,//false，是否启用拖拽重计算特性，默认关闭
	                	    grid :{
	                	    	x:60,
	                	    	y:25,
	                	    },
	                	    xAxis : [
	                	        {
	                	            type : 'category',
	                	            boundaryGap : false,//类目起始和结束两端空白策略，见下图，默认为true留空，false则顶头
	                	            data : xTime,
	                	            axisLabel : {
	                	                show:true,
	                	                interval: 0,
	                	                rotate:-30
	                	            },
	                	        }
	                	    ],
	                	    yAxis : [
	                	        {
	                	            type : 'value',
	                	            axisLabel : {		//坐标轴文本标签选项
	                	                formatter: '{value}'//间隔名称格式器
	                	            }
	                	        }
	                	    ],
	                	    series : [
								{
								    name:'磁盘读吞吐',
								    type:'line',
								    data:yRead,
								    smooth: true		//false为line，true为平滑曲线
								},
	                	        {
	                	            name:'磁盘写吞吐',
	                	            type:'line',
	                	            data:yWrite,
	                	            smooth: true		//false为line，true为平滑曲线
	                	        }
	                	    ]
	                	};
	                myChart.setOption(option);
	            }
	    );
	};
})
.controller('GoMonitorNetCtrl',function($scope,eayunHttp){//网络流速
	var xTime = [];
	var yNetIn = [];
	var yNetOut = [];
	$scope.$watch('data.endTime+data.timeRange' , function(newVal,oldVal){
		  if(newVal != oldVal){
			  xTime = [];
			  yNetIn = [];
			  yNetOut = [];
			  $scope.firstData();
		  }
	  });
	$scope.firstData = function(){
		eayunHttp.post('monitor/resourcemonitor/getMonitorDataById.do',
				{vmId:$scope.data.vmId,endTime:$scope.data.endTime.getTime(),count:$scope.data.timeRange,type:'incomming'}).then(function(response){
			if(null == response.data){
				$state.go('app.monitor.monitorbar.resourcemonitor');
			}else{
				$scope.dataModelList = response.data;
				angular.forEach($scope.dataModelList, function (value,key) {
					var time = new Date(value.timestamp).Format("MM-dd hh:mm");
					var netin = value.netIn;
					xTime.push(time);
					yNetIn.push(netin.toFixed(4));
				});
				$scope.secondData();
			}
		});
	};
	$scope.firstData();
	$scope.secondData = function(){
		eayunHttp.post('monitor/resourcemonitor/getMonitorDataById.do',
				{vmId:$scope.data.vmId,endTime:$scope.data.endTime.getTime(),count:$scope.data.timeRange,type:'outgoing'}).then(function(response){
			if(null == response.data){
				$state.go('app.monitor.monitorbar.resourcemonitor');
			}else{
				$scope.dataModelList = response.data;
				angular.forEach($scope.dataModelList, function (value,key) {
					var netout = value.netOut;
					yNetOut.push(netout.toFixed(4));
				});
				$scope.pushChart(xTime,yNetIn,yNetOut);
			}
		});
	};

	$scope.pushChart = function(xTime,yNetIn,yNetOut){
		require(
	            [
	                'echarts',
	                'echarts/chart/line' // 使用柱状图就加载bar模块，按需加载
	            ],
	            function (ec) {
	                // 基于准备好的dom，初始化echarts图表
	                var myChart = ec.init(document.getElementById('net'));
	                window.onresize = myChart.resize;
	                myChart.showLoading({
	                    text: '正在努力的读取数据中...',
	                });
	                myChart.hideLoading();
	                var option = {
	                	    /*title : {
	                	        text: '网卡流量(Mb/s)',
	                	    },*/
	                	    tooltip : {
	                	        trigger: 'axis'
	                	    },
	                	    legend: {
	                	        data:['网卡下行速率','网卡上行速率']
	                	    },
	                	    toolbox: {
	                	        show : false,		//图标是否显示
	                	        feature : {
	                	            mark : {show: true},	//辅助图标
	                	            dataView : {show: true, readOnly: false},
	                	            magicType : {show: true, type: ['line']},
	                	            restore : {show: true},
	                	            saveAsImage : {show: true}
	                	        }
	                	    },
	                	    calculable : false,//false，是否启用拖拽重计算特性，默认关闭
	                	    grid :{
	                	    	x:60,
	                	    	y:25,
	                	    },
	                	    xAxis : [
	                	        {
	                	            type : 'category',
	                	            boundaryGap : false,//类目起始和结束两端空白策略，见下图，默认为true留空，false则顶头
	                	            //data : ['1','2','三','四','五','六','七','八','九','十','11','12','13'],
	                	            data : xTime,
	                	            axisLabel : {
	                	                show:true,
	                	                interval: 0,
	                	                rotate:-30
	                	            },
	                	        }
	                	    ],
	                	    yAxis : [
	                	        {
	                	            type : 'value',
	                	            axisLabel : {			//坐标轴文本标签选项
	                	                formatter: '{value}'//间隔名称格式器
	                	            }
	                	        }
	                	    ],
	                	    series : [
								{
								    name:'网卡下行速率',
								    type:'line',
								    //data:[5,4,7,31,3,4,15,5,3,4,24,5,3],
								    data:yNetOut,		//下行换为out
								    smooth: true		//false为line，true为平滑曲线
								},
	                	        {
	                	            name:'网卡上行速率',
	                	            type:'line',		//上行换为in
	                	            //data:[1,13,30,22,33,25,8,10,19,33,25,27,0],
	                	            data:yNetIn,
	                	            smooth: true		//false为line，true为平滑曲线
	                	        }
	                	    ]
	                	};
	                myChart.setOption(option);
	            }
	    );
	};
});