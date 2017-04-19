'use strict';
angular.module('eayunApp.controllers')
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.when('/app/obs', ['eayunHttp','$state', function (eayunHttp,$state) {
        	eayunHttp.post('obs/obsOpen/getObsState.do').then(function (response) {
        		if(response.data!=null){
        			if(response.data.obsState=='1'){
        				$state.go('app.obs.obsbar');
        			}else{
        				$state.go('app.obs.openservice');
        			}
        		}else{
        			$state.go('app.obs.openservice');
        		}
            });
        }]);
        $urlRouterProvider.when('/app/obs/obsbar', '/app/obs/obsbar/bucket');
        $stateProvider.state('app.obs.obsbar', {
            url: '/obsbar',
            templateUrl: 'views/obs/obsbar.html',
            controller: 'ObsBarCtrl'
        }).state('app.obs.obsbar.bucket', {
            url: '/bucket',
            templateUrl: 'views/obs/bucket/bucketmng.html',
            controller: 'BucketCtrl',
            resolve: {
                isOpenService: function (eayunHttp) {

                    return eayunHttp.post('obs/obsOpen/getObsState.do').then(function (response) {
                    	if(response.data!=null){
                    		if(response.data.obsState=='1'){
                    			return true;
                    		}else{
                    			return false;
                    		}
                    	}else{
                    		return false;
                    	}
                    });
                }
            }
        }).state('app.obs.obsdetail', {
            url: '/detail',
            templateUrl: 'views/obs/obsdetail.html',
            controller: 'ObsDetailCtrl'
        }).state('app.obs.obsdetail.obj', {
            url: '/obj/:name/*folderName',
            templateUrl: 'views/obs/bucket/detailbucket.html',
            controller: 'ObsObjCtrl',
            resolve:{
            	isStopService:function(eayunHttp){
            		return eayunHttp.post('obs/storage/getobsservicestate.do').then(function (response) {
                		return response.data;
                	});
            	}
            }
        }).state('app.obs.openservice', {
            url: '/openservice',
            templateUrl: 'views/obs/obsopen.html',
            controller: 'ObsOpenServiceCtrl',
            resolve: {
                isOpenService: function (eayunHttp) {

                    return eayunHttp.post('obs/obsOpen/getObsState.do').then(function (response) {
                    	if(response.data!=null){
                    		if(response.data.obsState=='1'){
                    			return true;
                    		}else{
                    			return false;
                    		}
                    	}else{
                    		return false;
                    	}
                    });
                }
            }
        })   
        .state('buy.verifyobs',{
        	url:'/order',
        	templateUrl:'views/obs/obsorder.html',
        	controller:'ObsOpenOrderCtrl'
        })

        ;
    })
    .controller('ObsCtrl', function ($scope,eayunStorage,$location,isOpen) {
    	$scope.isOpen=isOpen;
    	$scope.config={route:'app.obs.obsbar.bucket',name:'Bucket管理',folderNameAddress:'',bucketName:'',folderName:''};
  	  	eayunStorage.set('config',$scope.config);
  	  	$scope.objBaseUrl = '/app/obs/detail/obj/';
  	  	$scope.bucket = $scope.config.bucketName;
  	  $scope.gotoFolder = function (index) {
  		$scope.config=eayunStorage.get('config');
  		$scope.bucket=$scope.config.bucketName;
          var folderName = '';
          for (var i = 0; i < index; i++) {
              folderName += $scope.config.folderNameAddress[i] + '/';
          }
          $scope.openFolder(folderName);
      };
      $scope.openFolder = function (folderName) {
            $scope.folderName = folderName.substring(0, folderName.lastIndexOf("/"));
            $scope.folderNameAddress = $scope.folderName.split("/");
            $location.path($scope.objBaseUrl + $scope.bucket + '/' + $scope.folderName);
        };
    })
    .controller('ObsBarCtrl', function ($scope, $state, eayunHttp, eayunModal,powerService) {
    	powerService.powerRoutesList().then(function (powerList) {
            $scope.buttonPower = {
            		isBucketView: powerService.isPower('bucket_view')			//查看Bucket
            };
        });
    })
    .controller('ObsOpenOrderCtrl', function ($scope, $state, eayunHttp, eayunModal,eayunStorage,$interval) {
    	$scope.flag = true;
  	  $scope.checkAgree = function(){
  		  if($scope.isAgree){
  			  $scope.flag = false;
  		  }else{
  			  $scope.flag = true;
  		  }
  	  };
     	var promise=null;
    	$scope.updateClock = function(){
    		$scope.second--;
    		if($scope.second <= 0){
    			$scope.openDis = false;
    			$interval.cancel(promise);
    		}
    	}
    	$scope.open=function(){
	    	$scope.openDis = true;
    		eayunHttp.post('obs/obsOpen/addObsOpen.do').then(function (response) {
                if (response.data.state==="obsIsOpen") {
                	$state.go('app.order.list');
                } else if(response.data.state==="obsIsNotOpen") {
                    eayunModal.warning("该功能尚在内测中，暂不开放，如需了解更多详情请联系客服。");
                    $scope.openDis = false;
                }else if(response.data.state==="balanceIsNotEnough"){
                	$scope.isError=true;
                	$scope.isErrMessage='账户余额必须大于等于'+response.data.limit+'元才可以开通对象存储服务';
                	$scope.isLight=true;
                	$scope.openDis = false;
                }else if(response.data.state=='obsIsOpened'){
                	eayunModal.warning("对象存储服务已开通,请勿重复开通");
                	$scope.openDis = false;
                }else if(response.data.state=='isNotAdmin'){
                	eayunModal.warning("开通此功能需要超级管理员权限");
                	$scope.openDis = false;
                }else{
                	eayunModal.warning("由于网络异常，开通失败，请稍后重试");
                	$scope.openDis = false;
                }
            });
    	}
    })
    .controller('ObsOpenServiceCtrl', function ($scope, $state, toast, eayunHttp, eayunModal, isOpenService,powerService,eayunStorage) {
        if (isOpenService) {
            $state.go('app.obs.obsbar.bucket');
        } else {
            $scope.openService = function () {
            	if(powerService.isAdmin()){
            		eayunHttp.post('obs/obsOpen/obsIsAllowOpen.do').then(function (response) {
            		            			if(response.data.state=='obsIsAllowOpen'){
            		            				$state.go('buy.verifyobs');
            		            			}else if(response.data.state==="obsIsNotOpen") {
            		                            eayunModal.warning("该功能尚在内测中，暂不开放，如需了解更多详情请联系客服。");
            		                        }else if(response.data.state==="balanceIsNotEnough"){
            		                        	var result = eayunModal.open({
            		                		        templateUrl: 'views/obs/balanceerror.html',
            		                		        controller:'ObsTipCtrl',
            		                		        resolve: {
            		                		        	enoughMoney : function () {
            		                		                return response.data.limit;
            		                		            }
            		                		        }
            		                		      }).result;
            		                		  result.then(function (usermodel) {
            		                			  var rechargeNavList=[{route:'app.costcenter',name:'账户总览'}];
            		                			    eayunStorage.persist("rechargeNavList",rechargeNavList);
            		                			    $state.go('pay.recharge');
            		                		      },function(){
            		                		    	  
            		                		      });
            		                        }else if(response.data.state=='isNotAdmin'){
            		                        	eayunModal.warning("开通此功能需要超级管理员权限");
            		                        }
            		            		});
            	}else{
            		eayunModal.warning("开通此功能需要超级管理员权限");
            	}
            };
        }
    })
    .controller('ObsTipCtrl', function ($scope, $state, enoughMoney,$modalInstance) {
    	$scope.enoughMoney=enoughMoney;
    	$scope.commit=function(){
    		$modalInstance.close('ok');
    	};
    	$scope.cancel=function(){
    		$modalInstance.dismiss('cancel');
    	};
    })
    .controller('BucketCtrl', function (eayunStorage,$scope, $state, toast, eayunHttp, eayunModal, isOpenService, powerService) {
    	var config=eayunStorage.get('config');
    	config.route='app.obs.obsbar.bucket';
    	config.name='Bucket管理';
    	config.folderNameAddress='';
    	config.bucketName='';
    	config.folderName='';
    	//添加权限设置
        powerService.powerRoutesList().then(function (powerList) {
            $scope.buttonPower = {
                isBucketCreate: powerService.isPower('bucket_create'),	//创建
                isBucketDetails: powerService.isPower('bucket_details'),		//bucket详情
                isBucketDelete: powerService.isPower('bucket_delete'),		//删除
                isCDNConfig: powerService.isPower('bucket_cdnconfig'),		//CDN设置
                isBucketView: powerService.isPower('bucket_view')			//查看Bucket
            };
        });

        if (isOpenService) {
            $scope.myTable = {
                source: 'obs/bucket/getBucketPageList.do',
                api: {},
                getParams: function () {
                    return {
                        name: $scope.bucketName || ''
                    };
                }
            };

            $scope.checkStatusClass =function (model){
    	    	$scope.StatusClass = '';
    			if(model.cdnStatus&&model.cdnStatus=='2'){
    				$scope.StatusClass = 'green';
    			}  
    			else if(model.cdnStatus=='1'){
    				$scope.StatusClass = 'yellow';
    			}
    			else{
    				$scope.StatusClass = 'gray';
    			}
    			return $scope.StatusClass;
    	    };
    	    
            //HTML页面model值双向绑定到getParams统一传到后台Java代码Controller中

            //列表页名称查询
    	    $scope.search = function (value,event) {
                $scope.myTable.api.draw();

            };
            /**
             * 查询当前sessionStore 是否存在用户信息
             */
            $scope.checkUser = function () {
                var user = sessionStorage["userInfo"]
                if (user) {
                    user = JSON.parse(user);
                    if (user && user.userId) {
                        return true;
                    }
                }
                return false;
            };
            //页面中回车键触发查询事件；
           /* $(function () {
                document.onkeydown = function (event) {
                    var e = event || window.event || arguments.callee.caller.arguments[0];
                    if (!$scope.checkUser()) {
                        return;
                    }
                    if (e && e.keyCode == 13) {
                        $scope.myTable.api.draw();
                    }
                };
            });*/
            //创建Bucket
            $scope.createBucket = function () {
                eayunHttp.post('obs/bucket/getBucketList.do').then(function (response) {
                    if (response.data.length == 99 || response.data.length > 99) {
                        eayunModal.warning('当前Bucket数目已达上限');
                    } else {
                        var result = eayunModal.open({
                            templateUrl: 'views/obs/bucket/addbucket.html',
                            backdrop: "static",
                            controller: 'AddBucketCtrl'
                        }).result;
                        result.then(function (value) {
                            //刷新列表
                            $scope.myTable.api.draw();
                        }, function () {
//					        console.info('取消');
                        });
                    }
                });

            };

            //Bucket详情
            $scope.bucketDetail = function (bucketList, item) {
            	if(item.isOpencdn == '0'){
            		item.cdnTag = '0';
                }else{
                	item.cdnTag = '1';
                }
            	angular.forEach(bucketList, function (value, key) {
            		if(value.isOpencdn == '0'){
            			value.cdnTag = '0';
                    }else{
                    	value.cdnTag = '1';
                    }
                });
            	var result = eayunModal.open({
                    templateUrl: 'views/obs/bucket/bucketdetailbar.html',
                    controller: 'BucketDetailCtrl',
                    backdrop: "static",
                    resolve: {
                        bucketAclList: function () {
                            return bucketList;
                        },
                        item: function () {
                            return item;
                        },
                        isCDNConfig: function () {
                            return $scope.buttonPower.isCDNConfig;
                        }
                    }
                }).result;
                result.then(function (value) {
                    //创建页面点击提交执行后台Java代码
                    eayunHttp.post('obs/bucket/editBucket.do', value).then(function (response) {

//			    		  $scope.myTable.api.draw();
                    });
                }, function () {
                    $scope.myTable.api.draw();
//			        console.info('取消');
                });

            };

            //删除Bucket
            $scope.deleteBucket = function (item) {
                eayunModal.confirm('确定要删除Bucket ' + item.bucketName + '?').then(function () {
                    eayunHttp.post('obs/bucket/deleteBucket.do', {bucketName: item.bucketName}).then(function (response) {
                        var name = "";
                        if (item.bucketName.length > 6) {
                            name = item.bucketName.substring(0, 6) + "...";
                        }
                        if (response.data.resCode == "204") {
                            if (item.bucketName.length > 6) {
                                toast.success('删除Bucket ' + name + '成功');
                            } else {
                                toast.success('删除Bucket ' + item.bucketName + '成功');
                            }

                        } else if (response.data.resCode == "409") {
                            eayunModal.warning(response.data.value);
                        } else if (response.data.resCode == "404") {
                            eayunModal.warning(response.data.value);
                        }
                        $scope.myTable.api.draw();
                    });
                });
            };
            $scope.showBucketDetail = function (bucketName) {
                /*eayunHttp.post('obs/storage/hasObjOrNot.do',bucketName).then(function(response){
                 console.log(response.data);
                 });*/
                $state.go('app.obs.obsdetail.obj', {name: bucketName, folderName: ''});
            };
        } else {
            $state.go('app.obs.openservice');
        }

    })
    /**
     * @ngdoc function
     * @name eayunApp.controller:AddBucketCtrl
     * @description
     * # AddBucketCtrl
     * 创建Bucket
     */
    .controller('AddBucketCtrl', function ($scope, eayunModal, eayunHttp, toast,$modalInstance) {
        //直接将创建页面所有的项目放入当前的$scope.model中 结束
        $scope.flag = true;
        $scope.commitBucket = function () {
            $scope.flag = false;
            var resCode = "";
            eayunHttp.post('obs/bucket/addBucket.do', $scope.model).then(function (response) {
                resCode = response.data.resCode;
                if (resCode == 101) {//配额数已满
                    toast.error(response.data.resValue);
                    $scope.flag = true;
                    $modalInstance.close('');
                    $scope.cancel();
                    return;
                } else if (resCode == 102) {//Bucket名称已存在
                    toast.error(response.data.resValue);
                    $scope.flag = true;
                    return;
                } else if (resCode == 200) {
                    var name = "";
                    if ($scope.model.name.length > 6) {
                        name = $scope.model.name.substring(0, 6) + "...";
                    } else {
                        name = $scope.model.name;
                    }
                    toast.success('创建Bucket ' + name + '成功');
                    $modalInstance.close('');
                }
            });


        };
        $scope.cancel=function(){
        	$modalInstance.dismiss('cancel');
        };

    })

    /**
     * @ngdoc function
     * @name eayunApp.controller:BucketDetailCtrl
     * @description
     * # BucketDetailCtrl
     * Bucket详情
     */
    .controller('BucketDetailCtrl', function ($scope, toast,eayunModal, eayunHttp, bucketAclList, item,isCDNConfig,$modalInstance) {
    	$scope.cancel=function(){
    		$modalInstance.dismiss('cancel');
    	};
    	eayunHttp.post('obs/storage/getobsservicestate.do').then(function (response) {
    		$scope.isStop=response.data;
    	});
        //$scope.model = {
        //    preview: 'storage'
        //};
        $scope.model = item;
        $scope.model.preview='storage';
        //监听bucket预览项
        $scope.$watch('model.preview', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.get();
            }
        });
        /**************CDN服务*************/
        $scope.isHide = false;
        $scope.isCDNConfig = isCDNConfig;
        $scope.$watch('model.isOpencdn', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                if (newVal == $scope.model.cdnTag) {
                    $scope.isOpencdnChange = false;
                } else {
                    $scope.isOpencdnChange = true;
                }
            }
        });
    	 $scope.openOrCloseCDN= function () {
    		
    	 };
    	 /**************CDN服务*************/
        $scope.get = function () {
            var xTime = [];
            var yDataOne = [];
            var yDataTwo = [];
            var yDataThree = [];
            var yDataMin = 0;
            var yDataMax = 0;
            if ($scope.model.preview == 'storage') {
                eayunHttp.post('obs/bucket/getBucketStorage.do', {bucketName: $scope.model.bucketName}).then(function (response) {

                    angular.forEach(response.data, function (value, key) {
                        var time = new Date(value.timestamp).Format("yyyy-MM-dd hh:mm");
                        xTime.push(time);
                        yDataOne.push(value.bucketStorage.toFixed(2));//bucketStorage.toFixed(2)

                    });
                    yDataMin = response.data[0].minStorage;
                    yDataMax = response.data[0].maxStorage;
//				  console.info(yDataMin);
//				  console.info(yDataMax);
                    $scope.pushChart(xTime, yDataOne, yDataTwo, yDataThree, $scope.model.preview, yDataMin, yDataMax);
                });
            } else if ($scope.model.preview == 'used') {
                eayunHttp.post('obs/bucket/getBucketUsedAndRequest.do', {
                    bucketName: $scope.model.bucketName,
                    type: "used"
                }).then(function (response) {
                    angular.forEach(response.data, function (value, key) {
                        var time = new Date(value.timestamp).Format("yyyy-MM-dd hh:mm");
                        xTime.push(time);
                        yDataOne.push(value.netOut.toFixed(2));
                        yDataTwo.push(value.netIn.toFixed(2));
                    });
                    yDataMin = response.data[0].netMin;
                    yDataMax = response.data[0].netMax;
//				  console.info(yDataMin);
//				  console.info(yDataMax);
                    $scope.pushChart(xTime, yDataOne, yDataTwo, yDataThree, $scope.model.preview, yDataMin, yDataMax);//流量max，min暂不保留为整数
                });
            } else if ($scope.model.preview == 'request') {
                xTime = [];
                yDataOne = [];
                yDataTwo = [];
                yDataThree = [];
                eayunHttp.post('obs/bucket/getBucketUsedAndRequest.do', {
                    bucketName: $scope.model.bucketName,
                    type: "request"
                }).then(function (response) {
                    angular.forEach(response.data, function (value, key) {
                        var time = new Date(value.timestamp).Format("yyyy-MM-dd hh:mm");
                        xTime.push(time);
                        yDataOne.push(value.requestGet);
                        yDataTwo.push(value.requestPut);
                        yDataThree.push(value.requestDelete);
                    });
                    yDataMin = response.data[0].requestMinTimes;
                    yDataMax = response.data[0].requestMaxTimes;
//				  console.info(yDataMin);
//				  console.info(yDataMax);
                    $scope.pushChart(xTime, yDataOne, yDataTwo, yDataThree, $scope.model.preview, yDataMin, yDataMax);
                });
            }else if ($scope.model.preview == 'CDN') {
            	eayunHttp.post('obs/cdn/getcdnflowbybucket.do', {bucketName: $scope.model.bucketName}).then(function (response) {
            		if(response.data.respCode == "000000"){
            			angular.forEach(response.data.data, function (value, key) {
                            var time = new Date(value.timestamp).Format("yyyy-MM-dd hh:mm");
                            xTime.push(time);
                            yDataOne.push(value.cdnFlow.toFixed(2));
                        });
            			yDataMin = response.data.data[0].minStorage;
                        yDataMax = response.data.data[0].maxStorage;
            		}
                    $scope.pushChart(xTime, yDataOne, yDataTwo, yDataThree, $scope.model.preview, yDataMin, yDataMax);
                });
            }
        };
        $scope.get();

        $scope.bucketAclList = bucketAclList;
        $scope.model = {};
        //将当前位置的bucket赋值给当前页面model
        angular.forEach($scope.bucketAclList, function (value, key) {
            if (value.bucketName == item.bucketName && value.creationDate == item.creationDate) {
                $scope.bucket = value;
                $scope.model = value;
            }
        });

        $scope.tabFlag = "normal";
        $scope.tabChange = function (tab) {
            $scope.tabFlag = tab;
            if (tab == "normal") {
                $scope.get();
            }
        };
        //切换bucket后赋值给当前model
        $scope.getBucketDetail = function (bucket) {
            $scope.model = bucket;
            if($scope.model.cdnTag == '0'){
            	$scope.model.isOpencdn = '0';
            }else{
            	$scope.model.isOpencdn = '1';
            }
            $scope.isOpencdnChange = false;
        }
        $scope.permissionChange = false;
        //监听bucket权限类型
        $scope.$watch('model.permissionEn', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                if ($scope.model.permission == "公有读写" && $scope.model.permissionEn == "PublicReadWrite") {
                    $scope.permissionChange = false;
                } else if ($scope.model.permission == "私有读写" && $scope.model.permissionEn == "Private") {
                    $scope.permissionChange = false;
                } else if ($scope.model.permission == "公有读私有写" && $scope.model.permissionEn == "PublicRead") {
                    $scope.permissionChange = false;
                } else {
                    $scope.permissionChange = true;
                }
            }
        });


        //修改bucket公有、私有
        $scope.setPermission = function () {
            var value = {
                bucketName: $scope.model.bucketName,
                permissionEn: $scope.model.permissionEn
            };
            eayunHttp.post('obs/bucket/editBucket.do', value).then(function (response) {

            });
            $scope.permissionChange = false;
            if ($scope.model.permissionEn == "PublicReadWrite") {
                $scope.model.permission = "公有读写";
            } else if ($scope.model.permissionEn == "Private") {
                $scope.model.permission = "私有读写";
            } else if ($scope.model.permissionEn == "PublicRead") {
                $scope.model.permission = "公有读私有写";
            }
        }

        $scope.pushChart = function (xTime, yDataone, yDatatwo, yDatathree, type, yDataMin, yDataMax) {

            var legenddata = ['存储总量'];
            var two = false;
            var three = false;
            if (type == "storage") {

            } else if (type == "used") {
                legenddata = ['流入流量', '流出流量'];
                two = true;
            } else if (type == "request") {
                legenddata = ['get次数', 'put次数', 'delete次数'];
                two = true;
                three = true;
            }else if(type == "CDN"){
            	legenddata = ['CDN下载流量'];
            }
            require(
                [
                    'echarts',
                    'echarts/chart/line' // 使用柱状图就加载bar模块，按需加载
                ],
                function (ec) {
                    // 基于准备好的dom，初始化echarts图表
                    var myChart = ec.init(document.getElementById('zonglan'));
                    window.onresize = myChart.resize;
                    myChart.showLoading({
                        text: '正在努力的读取数据中...',
                    });
                    myChart.hideLoading();
                    var option = {
                        /*title : {
                         text: '网卡流量(Mb/s)',
                         },*/
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: legenddata
                        },
                        toolbox: {
                            show: false,		//图标是否显示
                            feature: {
                                mark: {show: true},	//辅助图标
                                dataView: {show: true, readOnly: false},
                                magicType: {show: true, type: ['line']},
                                restore: {show: true},
                                saveAsImage: {show: true}
                            }
                        },
                        calculable: false,//false，是否启用拖拽重计算特性，默认关闭
                        grid: {
                            x: 60,
                            y: 25,

                        },
                        xAxis: [
                            {
                                type: 'category',
                                boundaryGap: false,//类目起始和结束两端空白策略，见下图，默认为true留空，false则顶头
                                data: xTime,
                            }
                        ],
                        yAxis: [
                            {
                                type: 'value',
                                axisLabel: {			//坐标轴文本标签选项
                                    formatter: '{value}'//间隔名称格式器
                                },
                                min: yDataMin,
                                max: yDataMax
                            }
                        ],
                        series: [
                            {
                                name: legenddata[0],
                                type: 'line',
                                data: yDataone,
                                smooth: true		//false为line，true为平滑曲线
                            }/*,
                             {
                             name:legenddata[1],
                             type:'line',
                             data:yDatatwo,
                             smooth: true		//false为line，true为平滑曲线
                             },
                             {
                             name:legenddata[2],
                             type:'line',
                             data:yDatathree,
                             smooth: true		//false为line，true为平滑曲线
                             }*/
                        ]
                    };
                    if (two) {
                        option.series.push({
                            name: legenddata[1],
                            type: 'line',
                            data: yDatatwo,
                            smooth: true		//false为line，true为平滑曲线
                        });
                    }
                    if (three) {
                        option.series.push({
                            name: legenddata[2],
                            type: 'line',
                            data: yDatathree,
                            smooth: true		//false为line，true为平滑曲线
                        });
                    }

                    myChart.setOption(option);
                }
            );
        };


        //直接将创建页面所有的项目放入当前的$scope.model中 结束
        $scope.commit = function () {
            $scope.ok($scope.model);
        };

        //日期格式化。
        Date.prototype.Format = function (fmt) {
            var o = {
                "M+": this.getMonth() + 1,                 //月份
                "d+": this.getDate(),                    //日
                "h+": this.getHours(),                   //小时
                "m+": this.getMinutes(),                 //分
                "s+": this.getSeconds(),                 //秒
                "q+": Math.floor((this.getMonth() + 3) / 3), //季度
                "S": this.getMilliseconds()             //毫秒
            };
            if (/(y+)/.test(fmt))
                fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o)
                if (new RegExp("(" + k + ")").test(fmt))
                    fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            return fmt;
        };

        //监听bucket的改变
        $scope.$watch('bucket', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.model.preview = "storage";
                $scope.model.permissionEn = $scope.bucket.permissionEn;
                $scope.get();
                $scope.permissionChange = false;
                if ($scope.model.permission == "公有读写") {
                    $scope.model.permissionEn = "PublicReadWrite";
                } else if ($scope.model.permission == "私有读写") {
                    $scope.model.permissionEn = "Private";
                } else if ($scope.model.permission == "公有读私有写") {
                    $scope.model.permissionEn = "PublicRead";
                }

            }
        });

    })
    .controller('ObsDetailCtrl', function ($scope) {
        $scope.route = 'app.obs.obsbar.bucket';
        $scope.name = 'Bucket管理';
    })
    .controller('ObsObjCtrl', function (eayunStorage,$scope, $state, $location, eayunHttp, eayunModal, $stateParams, toast, powerService) {
    	//添加权限设置
        powerService.powerRoutesList().then(function (powerList) {
            $scope.buttonPower = {
                isObjectDown: powerService.isPower('object_down')			//下载文件
            };
        });
    	eayunHttp.post('obs/storage/getobsservicestate.do').then(function (response) {
    		$scope.isStop=response.data;
    	});
    	var config=eayunStorage.get('config');
    	config.folderNameAddress=$stateParams.folderName.split('/');
    	config.bucketName=$stateParams.name;
    	config.folderName=$stateParams.folderName;
        $scope.objBaseUrl = '/app/obs/detail/obj/';
        $scope.selected = [];
        $scope.bucketName = $stateParams.name;
        $scope.obsName = "";
        $scope.folderName = $stateParams.folderName;
        $scope.folderNameAddress = $scope.folderName.split('/');
        //pop框方法
        $scope.hintNameShow = [];
        $scope.openTableBox = function (obj) {
            if (obj.type == 'objName') {
                $scope.hintNameShow[obj.index] = true;
            }
            $scope.ellipsis = obj.value;
        };
        $scope.closeTableBox = function (obj) {
            if (obj.type == 'objName') {
                $scope.hintNameShow[obj.index] = false;
            }
        };

        powerService.powerRoutesList().then(function (powerList) {
            $scope.objListPermissions = {
                isUploadObj: powerService.isPower('object_add'),
                isAddObj: powerService.isPower('object_new'),
                isDeleteGroupObj: powerService.isPower('object_alldelete'),
                isDeleteObj: powerService.isPower('object_delete')
            };
        });

        $scope.gotoBucket = function (bucketName) {
            $state.go('app.obs.obsbar.bucket');
        };

        $scope.gotoFolder = function (index) {
            var folderName = '';
            for (var i = 0; i < index; i++) {
                folderName += $scope.folderNameAddress[i] + '/';
            }
            $scope.openFolder(folderName);
        };

        $scope.detailTable = {
            source: 'obs/storage/getStorageList.do',
            api: {},
            getParams: function () {
                return {
                    bucketName: $stateParams.name,
                    obsName: $scope.obsName,
                    folderName: $scope.folderName,
                };
            }
        };
//
        $scope.$watch('detailTable.result.code', function (oldV_, newV_) {
            if (typeof($scope.detailTable.result) != "undefined"&&$scope.detailTable.result.code == '404') {
                eayunModal.error($scope.detailTable.result.msg);
                $scope.gotoBucket();
            }
        });

        $scope.showDetail = function (obsName) {
            eayunHttp.post('obs/storage/getObject.do', {bucketName: $scope.bucketName, obsName: obsName});
        };

        $scope.addStorage = function () {
            var result = eayunModal.open({
                templateUrl: 'views/obs/bucket/addstorage.html',
                controller: 'AddStorageCtrl',
                backdrop: "static",
                resolve: {
                    objStorage: function () {
                        return {};
                    },
                    bucketName: function () {
                        return $stateParams.name;
                    },
                    address: function () {
                        return $scope.folderName;
                    }
                }
            }).result;
            result.then(function (response) {
                var folderName = response;
                eayunHttp.post('obs/storage/add.do', {
                    bucketName: $stateParams.name,
                    folderName: $scope.folderName,
                    obsName: response
                }).then(function (response) {
                    if (response.data.resCode == '200') {
                        toast.success('添加文件夹' + (folderName.length > 9 ? folderName.substring(0, 5) + '...' : folderName) + '成功');
                        $scope.detailTable.api.refresh();
                    } else if (response.data.resCode == '102') {
                        eayunModal.error(response.data.resMsg);
                    } else if (response.data.resCode == '400' || response.data.resCode == '403') {
                        eayunModal.error(response.data.resMsg);
                    } else if (response.data.resCode == '406') {
                        eayunModal.error(response.data.resMsg);
                        eayunHttp.post('obs/storage/getobsservicestate.do').then(function (response) {
                    		$scope.isStop=response.data;
                    	});
                    }
                });
            });
        };

        $scope.uploadStorage = function () {
            var result = eayunModal.open({
                templateUrl: 'views/obs/bucket/uploadstorage.html',
                controller: 'UploadStorageCtrl',
                backdrop: "static",
                resolve: {
                    bucketName: function () {
                        return $stateParams.name;
                    },
                    address: function () {
                        return $stateParams.folderName;
                    }
                }
            }).result;
            result.then(function () {
                $scope.detailTable.api.refresh();
            }, function () {
            });
        };

        $scope.deleteStorageGroup = function () {
        	angular.forEach($scope.detailTable.result.data, function (value, key) {
                if (value.isChecked) {
                    $scope.selected.push(value.obsName);
                }
            });
            if ($scope.selected.length == 0) {
                toast.error("请选择至少一个Object");
                return;
            } else {
                eayunModal.confirm('确定要删除所选的Object?').then(function () {
                    eayunHttp.post('obs/storage/delete.do', {
                        bucketName: $stateParams.name,
                        obsNames: $scope.selected
                    }).then(function (response) {
                        if (response.data) {
                            toast.success('删除成功');
                        } else {
                            eayunModal.warning('操作失败，不能删除非空文件夹');
                        }
                        $scope.detailTable.isAllChecked = false;
                        $scope.detailTable.api.refresh();
                        $scope.selected.splice(0, $scope.selected.length);
                    });
                }, function () {
                	$scope.selected.splice(0, $scope.selected.length);
                });
            }
        };

        /**
         * 查询当前sessionStore 是否存在用户信息
         */
        var checkUser = function () {
            var user = sessionStorage["userInfo"];
            if (user) {
                user = JSON.parse(user);
                if (user && user.userId) {
                    return true;
                }
            }
            return false;
        };

        $(function () {
            document.onkeydown = function (event) {
                var e = event || window.event || arguments.callee.caller.arguments[0];
                if (!checkUser()) {
                    return;
                }
                if (e && e.keyCode == 13) {
                    $scope.getObsStorage();
                }
            };
        });

        $scope.getObsStorage = function () {
            $scope.detailTable.api.draw();
        };

        $scope.openFolder = function (folderName) {
            $scope.folderName = folderName.substring(0, folderName.lastIndexOf("/"));
            $scope.folderNameAddress = $scope.folderName.split("/");
            $location.path($scope.objBaseUrl + $scope.bucketName + '/' + $scope.folderName);
        };

        $scope.updateSelection = function ($event, model) {
            var action = $event.target.checked ? 'add' : 'remove';
            if (action == 'add') {
            	model.isChecked = true;
//                $scope.selected.push(obsName);
            } else if (action == 'remove') {
            	model.isChecked = false;
//                $scope.selected.splice(obsName, 1);
            }
        };

        $scope.deleteStorage = function (storage) {
            eayunModal.confirm('确定要删除Object:	' + (storage.obsShowName.length > 58 ? storage.obsShowName.substring(0, 58) + '...' : storage.obsShowName) + '?').then(function () {
                eayunHttp.post('obs/storage/delete.do', {
                    bucketName: $stateParams.name,
                    obsNames: [storage.obsName]
                }).then(function (response) {
                    if (response.data) {
                        toast.success('删除' + (storage.obsShowName.length > 10 ? storage.obsShowName.substring(0, 10) + '...' : storage.obsShowName) + '成功');
                    } else {
                        eayunModal.error('操作失败，不能删除非空文件夹');
                    }
                    $scope.detailTable.api.refresh();
                });
            });
        };

        $scope.getUrl = function (storage) {
            eayunModal.open({
                templateUrl: 'views/obs/bucket/geturlstorage.html',
                controller: 'GetUrlStorageCtrl',
                backdrop: "static",
                resolve: {
                    bucketName: function () {
                        return $stateParams.name;
                    },
                    storage: function () {
                        return storage;
                    }
                }
            });
        };

        $scope.checkAll = function (isAllChecked) {
            if (isAllChecked) {
                angular.forEach($scope.detailTable.result.data, function (value, key) {
                    if (value.isChecked != isAllChecked) {
                        value.isChecked = isAllChecked;
//                        $scope.selected.push(value.obsName);
                    }
                });
            } else {
//                $scope.selected.splice(0, $scope.selected.length);
                angular.forEach($scope.detailTable.result.data, function (value, key) {
                    if (value.isChecked != isAllChecked) {
                        value.isChecked = isAllChecked;
                    }
                });
            }
        };
    })
    .controller('AddStorageCtrl', function ($scope, bucketName, address, eayunHttp,$modalInstance) {
        $scope.folderName = '';

        $scope.checkStorageName = function (folderName) {
            if (folderName != undefined) {
                eayunHttp.post('obs/storage/checkStorageName.do', {
                    bucketName: bucketName,
                    folderName: folderName,
                    address: address
                }).then(function (response) {
                    $scope.checkFlag = response.data.reName;
                    $scope.overLength = response.data.overLength;
                });
            }
        };

        $scope.commit = function () {
        	$modalInstance.close($scope.folderName);
        };
        $scope.cancel=function(){
        	$modalInstance.dismiss('cancel');
        };
    })
    .controller('UploadStorageCtrl', function ($scope, $http, bucketName, address, eayunHttp, eayunModal, obsService, $log,$modalInstance) {
    	$scope.ok=function(){
    		$modalInstance.close('cancel');
    	};
        $scope.bucketName = bucketName;
        $scope.files = {};
        $scope.isEmpty = true;
        var overload = false;
        address = encodeURI(address);
        if (address != '') {
            address += '/';
        }
        var uploadingFiles = {};
        var api = {
            isUploaded: function (files, file) {
                return !!$scope.files[file.name];
            },
            getLength: function (str) {
                var uriStr = encodeURI(str);
                var num = uriStr.split('%').length - 1;
                return (uriStr.length - num * 2);
            },
            delUploaded: function (file) {
                return eayunHttp.post('obs/storage/delete.do', {
                    bucketName: bucketName,
                    obsNames: [address + file.name]
                }).then(function (response) {
                    return response.data;
                });
            },
            upload: function (file) {
                file.done = 'unready';
                file.percent = 0;
                uploadingFiles[file.name] = file;
                $scope.files[file.name] = file;
                obsService.uploadFile(bucketName, address, file)
                    .then(function (response) {
                        if (response.status == '200') {
                            delete uploadingFiles[file.name];
                            file.done = 'done';
                        }
                    }, function (error) {
                        if (error == 'QuotaExceeded') {
                            eayunModal.error('上传文件超过配额大小！');
                            overload = true;
                            $scope.cancel();
                        } else {
                            $log.debug(error);
                        }
                    }, function (message) {
                        file.done = 'loading';
                        if (file.size != 0) {
                            file.percent = (message / file.size) * 100;
                        } else {
                            file.percent = 100;
                        }
                    });
            }
        };

        $scope.uploadFiles = function (file) {
            if (file) {
                if (file.size > 500 * 1024 * 1024) {
                    eayunModal.error('上传文件过大，请通过API方式上传！');
                    return;
                } else if (file.name.indexOf('%') != -1 || file.name.indexOf('+') != -1) {
                    eayunModal.error('上传文件名称中请不要包含字符%或+，请重新命名后上传！');
                    return;
                } else if (api.getLength(address + file.name) > 1023) {
                    eayunModal.error('上传文件名称所在文件路径过长，请缩短文件名或另择上传文件路径！');
                    return;
                } else if (uploadingFiles[file.name]) {
                    eayunModal.error('该路径下正在上传与所选文件重名的文件，请重新命名后上传！');
                    return;
                } else if (api.isUploaded($scope.files, file)) {
                    eayunModal.confirm('该文件已经被上传完成，确定要重新上传？').then(function () {
                        $scope.files[file.name].done = 'unready';
                        api.delUploaded(file).then(function (response) {
                            if (response) {
                                api.upload(file);
                            }
                        });
                    });
                } else {
                    $scope.isEmpty = false;
                    api.upload(file);
                }
            }
        };

        $scope.cancelUpload = function (file) {
            if (file.uploadId) {
                obsService.cancelUpload(bucketName, address, file);
                eayunHttp.post('obs/storage/junkUploadIdRecycling.do', {
                    uploadId: file.uploadId,
                    bucketName: bucketName,
                    obsName: address + file.name
                });
            }
            file.cancel = true;

            delete uploadingFiles[file.name];
            delete $scope.files[file.name];
            $scope.isEmpty = (Object.keys($scope.files).length == 0);
        };
        $scope.cancel = function () {
            if (Object.keys(uploadingFiles).length > 0 && overload == false) {
                eayunModal.confirm('仍有文件正在上传，退出将取消上传，是否继续？').then(function () {
                    for (var fileName in uploadingFiles) {
                        $scope.cancelUpload(uploadingFiles[fileName]);
                    }
                    $scope.ok();
                });
            } else if (overload == true) {
                for (var fileName in uploadingFiles) {
                    $scope.cancelUpload(uploadingFiles[fileName]);
                }
                $scope.ok();
            } else {
            	$scope.ok();
            }
        };
    })
    .controller('GetUrlStorageCtrl', function ($scope, bucketName, storage, eayunHttp,$modalInstance) {
        $scope.bucketName = bucketName;
        $scope.storage = storage;
        var encodeObsName = encodeURIComponent(storage.obsName);
        eayunHttp.post('obs/storage/getUrl.do', {
            bucketName: bucketName,
            obsName: encodeObsName
        }).then(function (response) {
            $scope.url = response.data.url;
        });
        $scope.cancel=function(){
    		$modalInstance.dismiss('cancel');
    	};
    });
