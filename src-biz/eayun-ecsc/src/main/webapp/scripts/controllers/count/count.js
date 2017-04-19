'use strict';
angular.module('eayunApp.controllers')

  .config(function ($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.when('/app/count', '/app/count/count');
    $stateProvider
    .state('app.count.count', {
      url: '/count',
      templateUrl: 'views/count/countmng.html',
      controller: 'CountMngCtrl',
      resolve:{
      	  dcList:function (eayunHttp){
      		  return eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
      	    	  return  response.data.data;
      	      });
      	  }
        }
    });
  })
	.controller('CountCtrl', function ($scope,powerService) {
		powerService.powerRoutesList().then(function(powerList){
			$scope.buttonPower = {
				isExport : powerService.isPower('result_export'),	//导出报表
				};
		});
	})
	.controller('CountMngCtrl', function ($scope , eayunHttp , eayunModal , toast,dcList) {
		$scope.showVm = true;
		$scope.showVol = false;
		$scope.showNet = false;
		$scope.showTab = function(show,tag){
			if('0' == tag){
				$scope.showVm = true;
				$scope.showVol = false;
				$scope.showNet = false;
			}else if('1' == tag){
				$scope.showVm = false;
				$scope.showVol = true;
				$scope.showNet = false;
			}else if('2' == tag){
				$scope.showVm = false;
				$scope.showVol = false;
				$scope.showNet = true;
			}
		};
		$scope.now = new Date();
		$scope.last = new Date($scope.now.getTime() - 7*24*60*60*1000); 
		$scope.data = {
				startTime : $scope.last,
				endTime : $scope.now
		};
		if(null==dcList||dcList.length==0){
			return;
		}
		$scope.dcList = dcList;
  	  	$scope.dataDc = $scope.dcList[0];
		$scope.histogram = {};
		$scope.showCon = false;
		/*初始化没有排序*/
		$scope.initialize  = function () {
			$scope.resourceType = '';
			$scope.sort='';
			$scope.sortClass = 'glyphicon glyphicon-sort';
			$scope.sortCpuClass = 'glyphicon glyphicon-sort';
			$scope.sortRamClass = 'glyphicon glyphicon-sort';
			$scope.sortStartClass = 'glyphicon glyphicon-sort';
			$scope.sortHoursClass = 'glyphicon glyphicon-sort';
        };
        $scope.initialize();
        $scope.initializeVol  = function () {
			$scope.volType = '';
			$scope.volSort='';
			$scope.volSortClass = 'glyphicon glyphicon-sort';
			$scope.sortVolStartClass = 'glyphicon glyphicon-sort';
			$scope.sortVolHoursClass = 'glyphicon glyphicon-sort';
			$scope.sortVolSizeClass = 'glyphicon glyphicon-sort';
        };
        $scope.initializeVol();
        /*查询*/
		$scope.queryList = function(){
			if($scope.data.endTime==null&&$scope.data.startTime==null){
				eayunModal.warning("请选择时间范围");
				return;
			}
			if($scope.data.startTime==null){
				eayunModal.warning("请选择开始时间");
				return;
			}
			if($scope.data.endTime==null){
				eayunModal.warning("请选择截止时间");
				return;
			}
			var ms = $scope.data.endTime.getTime()-$scope.data.startTime.getTime();
			var days = ms/1000/60/60/24;
			if(days > 90){
				eayunModal.warning("时间范围不能大于90天");
				return;
			}
			if(days <= 0){
				eayunModal.warning("开始时间必须小于截止时间");
				return;
			}
			$scope.initialize();
			$scope.initializeVol();
			if(!$scope.showCon){
				$scope.vmTable = {
					      source: 'statistics/getVmResources.do',
					      api:{},
					      getParams: function () {
					        return {
					        	dcId : $scope.dataDc.dcId,
					        	startTime : $scope.data.startTime.getTime(),
					        	endTime : $scope.data.endTime.getTime(),
					        	orderBy : $scope.resourceType,
								sort : $scope.sort
					        };
					      }
					  };
				$scope.volTable = {
					      source: 'statistics/getVolumeResources.do',
					      api:{},
					      getParams: function () {
					        return {
					        	dcId : $scope.dataDc.dcId,
								startTime : $scope.data.startTime.getTime(),
								endTime : $scope.data.endTime.getTime(),
								orderBy : $scope.volType,
								sort : $scope.volSort
						};
					      }
					  };
			}else{
				$scope.vmTable.api.draw();
				$scope.volTable.api.draw();
			}
			$scope.param = {
					projectId : $scope.dataDc.projectId,
					startTime : $scope.data.startTime.getTime(),
					endTime : $scope.data.endTime.getTime()
			};
			eayunHttp.post('statistics/getnetResources.do',$scope.param).then(function(response){
				$scope.netRes = response.data;
				$scope.histogram = angular.copy($scope.netRes,{});
		    });
			$scope.showCon = true;
		};
		/*排序*/
		$scope.changeVmSort = function (_resourceType) {
            if($scope.resourceType!=_resourceType){
            	$scope.initialize();
            }
            $scope.resourceType = _resourceType;
            switch ($scope.sort) {
            case ''     :
            	$scope.sort = 'DESC';
            	$scope.sortClass = 'glyphicon glyphicon-arrow-down';
                break;
            case 'DESC' :
            	$scope.sort = 'ASC';
            	$scope.sortClass = 'glyphicon glyphicon-arrow-up';
                break;
            case 'ASC'  :
            	$scope.sort = 'DESC';
            	$scope.sortClass = 'glyphicon glyphicon-arrow-down';
                break;
        	};
            switch (_resourceType) {
                case 'CPU'     :
                	$scope.sortCpuClass = $scope.sortClass;
                    break;
                case 'RAM' :
                	$scope.sortRamClass = $scope.sortClass;
                    break;
                case 'START'  :
                	$scope.sortStartClass = $scope.sortClass;
                    break;
                case 'HOURS'     :
                	$scope.sortHoursClass = $scope.sortClass;
                    break;
            };
            $scope.vmTable.api.draw();
        };
        $scope.changeVolSort = function (_volType) {
            if($scope.volType!=_volType){
            	$scope.initializeVol();
            }
            $scope.volType = _volType;
            switch ($scope.volSort) {
            case ''     :
            	$scope.volSort = 'DESC';
            	$scope.volSortClass = 'glyphicon glyphicon-arrow-down';
                break;
            case 'DESC' :
            	$scope.volSort = 'ASC';
            	$scope.volSortClass = 'glyphicon glyphicon-arrow-up';
                break;
            case 'ASC'  :
            	$scope.volSort = 'DESC';
            	$scope.volSortClass = 'glyphicon glyphicon-arrow-down';
                break;
        	};
            switch (_volType) {
                case 'VOL'     :
                	$scope.sortVolSizeClass = $scope.volSortClass;
                    break;
                case 'START'  :
                	$scope.sortVolStartClass = $scope.volSortClass;
                    break;
                case 'HOURS'     :
                	$scope.sortVolHoursClass = $scope.volSortClass;
                    break;
            };
            $scope.volTable.api.draw();
        };
        /*导出EXCEL*/
		$scope.createExcel = function(){
			if($scope.data.endTime==null&&$scope.data.startTime==null){
				eayunModal.warning("请选择时间范围");
				return;
			}
			if($scope.data.startTime==null){
				eayunModal.warning("请选择开始时间");
				return;
			}
			if($scope.data.endTime==null){
				eayunModal.warning("请选择截止时间");
				return;
			}
			var ms = $scope.data.endTime.getTime()-$scope.data.startTime.getTime();
			  var days = ms/1000/60/60/24;
			  if(days > 90){
				  eayunModal.warning("时间范围不能大于90天");
				  return;
			  }
			  if(days <= 0){
				  eayunModal.warning("开始时间必须小于截止时间");
				  return;
			  }
			  $scope.paramExcel = {
						dcId : $scope.dataDc.dcId,
						projectId : $scope.dataDc.projectId,
						startTime : $scope.data.startTime.getTime(),
						endTime : $scope.data.endTime.getTime()
				};
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
			  eayunHttp.post('statistics/getResourcesForExcel.do',$scope.paramExcel).then(function(response){
					if(!response.data.code){
						if(response.data){
							$("#excel-export-iframe").attr("src", "statistics/createExcel.do?projectId="+$scope.dataDc.projectId+"&dcId="+$scope.dataDc.dcId
									  +"&startTime="+$scope.data.startTime.getTime()+"&endTime="+$scope.data.endTime.getTime()+"&browser="+browser
									  +"&orderBy="+$scope.resourceType+"&sort="+$scope.sort+"&orderByVol="+$scope.volType+"&sortVol="+$scope.volSort);
						}else{
							toast.success("当前条件范围无数据");
						}
					}
			      });
		};
		/**
		 * 显示柱状图
		 */
		$scope.showHistogram = function(){
		      var result = eayunModal.dialog({
		    	showBtn: false,
		        title: '柱状图',
		        width: '1000px',
		        templateUrl: 'views/count/gram.html',
		        controller: 'showHistogramCtrl',
		        resolve: {
		        	hismodel: function () {
		                return $scope.histogram;
		              }
		        }
		      });
		      result.then(function () {
		    	  
		      });
		    
		};
	})
	.controller('showHistogramCtrl', function ($scope,hismodel,$interval,$location) {
		var xTime = [];
		var yUp = [];
		var yDown = [];
		$scope.model = hismodel.detailsList;
		var num = 0;
		var hight = 25;
		if(hismodel.detailsList.length<=9){
			num = 0;
			hight = 25;
		}else if(hismodel.detailsList.length>9&&hismodel.detailsList.length<=30){
			num = -30;
			hight = 60;
		}else{
			num = -90;
			hight = 80;
		}
		angular.forEach($scope.model, function (value,key) {
			var time = value.everyDate.substring(0,10).split('-').join('/');
			var up = value.upCount;
			var down = value.downCount;
			xTime.push(time);
			yUp.push(up);
			yDown.push(down);
		});
		$scope.pushChart = function(){
			require(
		            [
		             	'echarts',
		                'echarts/chart/bar' // 使用柱状图就加载bar模块，按需加载
		            ],
		            function (ec) {
		                // 基于准备好的dom，初始化echarts图表
		                var myChart = ec.init(document.getElementById('histog'));
		                window.onresize = myChart.resize;
		                myChart.showLoading({
		                    text: '正在努力的读取数据中...',
		                });
		                myChart.hideLoading();
		                var option = {
		                	    tooltip : {
		                	        trigger: 'axis'
		                	    },
		                	    legend: {
		                	        data:['上行流量','下行流量']
		                	    },
		                	    toolbox: {
		                	        show : false,
		                	        feature : {
		                	            mark : {show: true},
		                	            dataView : {show: true, readOnly: false},
		                	            magicType : {show: true, type: ['bar']},
		                	            restore : {show: true},
		                	            saveAsImage : {show: true}
		                	        }
		                	    },
		                	    calculable : false,//false，是否启用拖拽重计算特性，默认关闭
		                	    grid :{
		                	    	y:30,
		                	    	y2:hight
		                	    },
		                	    xAxis : [
		                	        {
		                	            type : 'category',
		                	            boundaryGap : true,//类目起始和结束两端空白策略，见下图，默认为true留空，false则顶头
		                	            data : xTime,
		                	            axisLabel : {
		                	                show:true,
		                	                rotate:num
		                	            },
		                	        }
		                	    ],
		                	    yAxis : [
		                	        {
		                	            type : 'value',
		                	        }
		                	    ],
		                	    series : [
									{
									    name:'上行流量',
									    type:'bar',
									    data:yDown,		//上行：换为down
									},
		                	        {
		                	            name:'下行流量',
		                	            type:'bar',
		                	            data:yUp,		//下行：换为up
		                	        }
		                	    ]
		                	};
		                myChart.setOption(option);
		            }
		    );
		};
		$scope.pushChart();
		var promise = null;
		var sec = 1;
		$scope.updateClock = function(){
			if($location.absUrl().lastIndexOf("app/count/count") != -1){
				sec--;
				if(sec <= 0){
					$scope.pushChart();
					$interval.cancel(promise);
				}
			}else{
				$interval.cancel(promise);
			}
		};
		promise = $interval(function(){
			$scope.updateClock();
			},500);
	});