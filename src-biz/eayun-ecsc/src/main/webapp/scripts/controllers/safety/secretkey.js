'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state("app.safety.safetybar.secretkeylist", {
				url : '/secretkey',
				templateUrl : 'views/safety/secretkey/secretkeylist.html',
				controller : 'secretkeylistCtrl'
			});
		}).controller('secretkeylistCtrl', function ($scope ,$state,$rootScope,$location, $anchorScroll,eayunModal,eayunHttp,toast,powerService) {
			var vm = this;
			vm.model={};
			$rootScope.safeState = null;
			var list=[];
			$rootScope.navList(list,'SSH秘钥');
			
			// 查询列表
			$scope.myTable = {
				source : 'safety/secretKey/getSecretKeyList.do',
				api : {},
				getParams : function() {
					return {
						prjId : sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '',
						dcId : sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : '',
						name : $scope.name || ''
					};
				}
			};
			// 监视器[监视数据中心、项目id变化]
			$scope.$watch('model.projectvoe', function(newVal, oldVal) {
				if (newVal !== oldVal) {
					$scope.myTable.api.draw();
				}
			});
			// 名称查询
			$scope.queryFireWall = function() {
				$scope.myTable.api.draw();
			};
			//测试查询虚拟机user_date
			eayunHttp.post('safety/secretKey/getVmUserDate.do',{"dcId":sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : '',"prjId":sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '',"vmId":"1a4c8982-5cb5-4996-bd32-fc8889755bcf"}).then(function(response){
				console.info(response.data);
		    });
			// 权限控制
			powerService.powerRoutesList().then(
				function(powerList) {
					$scope.buttonPower = {
						isSecretKeyList : powerService.isPower('secretkey_list'),// SSH密钥列表
						isSecretKeyAdd : powerService.isPower('secretkey_add'),// SSH密钥创建
						isSecretKeyUpdate : powerService.isPower('secretkey_update'),// SSH密钥修改
						isSecretKeyDetail : powerService.isPower('secretkey_details'),// SSH密钥详情
						isSecretKeyBind : powerService.isPower('secretkey_bind'),// SSH密钥绑定
						isSecretKeyDelete : powerService.isPower('secretkey_delete'),// SSH密钥删除
						isSecretKeyUnBind : powerService.isPower('secretkey_unbind'),// SSH密钥解绑
				};
			});
			// 创建密钥
			$scope.addSecretKey = function() {
				var result = eayunModal.open({
							showBtn:false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
							backdrop:'static',
							templateUrl : 'views/safety/secretkey/addsecretkey.html',
							controller : 'AddSecretKeyCtrl',
							resolve : {
								prjId : function() {
									
									return {
										prjId : sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : ''
									};
								},
								dcId : function() {
									return {
										dcId : sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : ''
									};
								}
							}
						}).result;
					result.then(function(value) {eayunHttp.post('safety/secretKey/addSecretKey.do',value).then(function(response) {
							if (null != response.data && 'null' != response.data) {
								if(response.data!=null && response.data!="" && response.data.code!="010120"){
									if(response.data.privateKeyFileId!=null && response.data.privateKeyFileId!=""){
										var down = eayunModal.open({
											showBtn:false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
											backdrop:'static',
											templateUrl : 'views/safety/secretkey/downprivatekey.html',
											controller : 'DownSecretKeyCtrl',
											resolve : {
												privateKeyFileId : function() {
													return response.data.privateKeyFileId;
												},
												secretkeyName : function() {
													return response.data.secretkeyName;
												}
											}
										}).result;
										down.then(function() {
											$scope.myTable.api.draw();
										}, function() {
											$scope.myTable.api.draw();
										});
										console.info("开始自动下载");
										$("#excel-export-iframe").attr("src", "safety/secretKey/downPrivateKeyFile.do?fileid="+response.data.privateKeyFileId);
									}else{
										toast.success('导入SSH密钥成功');
									}
								}
							}
							$scope.myTable.api.draw();
						});
					}, function() {
						// console.info('取消');
					});
			};
			//密钥绑定云主机
			$scope.bindVm = function(secretkeyId) {
				var result = eayunModal.open({
					templateUrl : 'views/safety/secretkey/bindvm.html',
					controller : 'secretKeyBindVmCtrl',
					resolve : {
						unmodel : function() {
							return eayunHttp.post('safety/secretKey/getVmList.do',
							{
								secretkeyId : secretkeyId,
								prjId : sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '',
								type : 'unbind'
							});
						},
						model : function(){
							return eayunHttp.post('safety/secretKey/getVmList.do',
							{
								secretkeyId : secretkeyId,
								prjId : sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '',
								type : 'bind'
							});
						},
						secretkeyId : function(){
							return secretkeyId;
						}
					}
				}).result;
				result.then(function(resultData) {
					resultData.prjId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '';
					resultData.dcId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : '';
					eayunHttp.post('safety/secretKey/bindSecretkeyTovm.do',resultData).then(function(response) {
						if (response.data != null && response.data != "") {
							if(response.data.errornum!="0" && response.data.errornum>0){
								eayunModal.warning("绑定/解绑成功"+response.data.successnum+"台云主机、"+response.data.errornum+"台云主机操作失败，" +
									"请确定云主机为关机状态并且所在子网已连接路由");
							}else{
								toast.success('绑定/解绑云主机成功');
							}
							$scope.myTable.api.draw();
						} else {
							eayunModal.error(response.message);
						}
					});
				});
			};
			
			$scope.downFile = function(){
				$("#excel-export-iframe").attr("src", "safety/secretKey/downPrivateKeyFile.do?fileid="+vm.model.secretkey.privatekey);
			};
			

			$scope.edtimSk = function(secretkeyId) {
				var result = eayunModal.open({
					templateUrl : 'views/safety/secretkey/updatesecretkey.html',
					controller : 'secretKeyUpdateCtrl',
					resolve : {
						model : function() {
							return  eayunHttp.post('safety/secretKey/getsecrekeyByid.do',{secretkeyId:secretkeyId})
						}
					}
				}).result;
				result.then(function(resultData) {
					eayunHttp.post('safety/secretKey/updateSecretKey.do',resultData).then(
						function(response) {
							if (response) {
								toast.success('修改SSH密钥成功');
								$scope.myTable.api.draw();
							} else {
								eayunModal.warning(response.message);
							}
						});
					});
			};
			
			$scope.deletadSk = function(secretkeyId,secretkeyName) {
				var prjId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '';
				var dcId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : '';
				eayunModal.confirm('确定删除选定SSH密钥？').then(function() {
					eayunHttp.post('safety/secretKey/deleteSecretKey.do',
						{
							secretkeyId : secretkeyId,
							secretkeyName : secretkeyName,
							prjId : prjId,
							dcId : dcId
						}).then(function(data) {
						
							if (data.data.respCode == '000000') {
								toast.success('删除密钥成功');
								$scope.myTable.api.draw();
							}else{
								eayunModal.warning(data.data.message);
								$scope.myTable.api.draw();
							}
							
							
						});

					});
			};
		}).controller('AddSecretKeyCtrl',function($scope, prjId, dcId, eayunHttp, eayunModal,$modalInstance) {
			$scope.secretkey = {};
			$scope.model = {};
			$scope.close = function(){
				$modalInstance.dismiss();
			};
			// 校验名称格式和唯一性
			$scope.namemodel = {};
			$scope.checkName = function(value) {
				var nameTest = /^[a-zA-Z0-9_]{1,200}$/;
				$scope.nameflag = false;
				if (value.match(nameTest)) {
					$scope.namemodel.prjId = prjId.prjId;
					$scope.namemodel.dcId = dcId.dcId;
					$scope.namemodel.secretkeyId = "";
					$scope.namemodel.name = value;
					return eayunHttp.post('safety/secretKey/checkSecretKeyName.do',$scope.namemodel).then(function(response) {
						if (true == response.data) {
							return false;
						} else {
							return true;
						}
					});

				} else {
					$scope.nameflag = true;
				}
			};
			$scope.commit = function() {//提交创建数据
				$scope.model.prj_id = prjId.prjId;
				$scope.model.dc_id = dcId.dcId;
				$scope.model.secretkey = $scope.secretkey;
				$modalInstance.close($scope.model);
			};
		}).controller('secretKeyBindVmCtrl',function($scope, $state, eayunHttp, eayunModal,unmodel, model,$modalInstance,secretkeyId) {
			$scope.model={};
			$scope.list = [];
			$scope.changelist = [];
			$scope.Selected=[];
			$scope.list = unmodel.data;//未选中的云主机
			$scope.changelist = model.data;//已选择的云主机
			for(var a=0;a<$scope.list.length;a++){
				if($scope.list[a].vmip==''){
					$scope.list[a].vmip='--';
				}
				if($scope.list[a].floip==''){
					$scope.list[a].floip='--';
				}
				if($scope.list[a].seleip==''){
					$scope.list[a].seleip='--';
				}
			}
			for(var a=0;a<$scope.changelist.length;a++){
				if($scope.changelist[a].vmip==''){
					$scope.changelist[a].vmip='--';
				}
				if($scope.changelist[a].floip==''){
					$scope.changelist[a].floip='--';
				}
				if($scope.changelist[a].seleip==''){
					$scope.changelist[a].seleip='--';
				}
				$scope.changelist[a].$$selected = true;//设置为已经选中的
	            $scope.list.push($scope.changelist[a]);//添加到显示列表
			}
			
			$scope.cancel = function (){
				$modalInstance.dismiss();
			};
			$scope.commit = function() {
				eayunModal.confirm('确定绑定/解绑密钥？').then(function() {
					for(var i=0;i<$scope.list.length;i++){
						if($scope.list[i].$$selected){
							$scope.Selected.push($scope.list[i]);
						}
					}
					$scope.model.cloudhostlist = $scope.Selected;
					$scope.model.secretkeyId = secretkeyId;
					$modalInstance.close($scope.model);
				},function(){});
			};
		}).controller('secretKeyUpdateCtrl',function($scope, $state, eayunHttp, eayunModal,model,$modalInstance) {					
			$scope.model = angular.copy(model.data[0]);
//				var prjId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '';
//				var dcId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : '';
//
//				$scope.model.prjId = prjId;
//				$scope.model.dcId = dcId;
				$scope.namemodel = {};
				$scope.checkName = function(value) {
					var nameTest = /^[a-zA-Z0-9_]{1,200}$/;
					$scope.nameflag = false;
					if (value.match(nameTest)) {
						$scope.namemodel.prjId = $scope.model.prjId;
						$scope.namemodel.dcId = $scope.model.dcId;
						$scope.namemodel.secretkeyId = $scope.model.secretkeyId;
						$scope.namemodel.name = value;
						return eayunHttp.post(
								'safety/secretKey/checkSecretKeyName.do',
								$scope.namemodel).then(function(response) {
							if (true == response.data) {
								return false;
							} else {
								return true;
							}
						});

					} else {
						$scope.nameflag = true;
					}
				};

				$scope.cancel = function() {
					$modalInstance.dismiss();
				};

				$scope.commit = function() {

					$modalInstance.close($scope.model);
				};
		}).controller('DownSecretKeyCtrl',function($scope, $state, eayunHttp, privateKeyFileId,secretkeyName,$modalInstance,$timeout) {	
			$scope.cancel = function (){
				if(timers!=null){
					clearInterval(timers);
				}
				$modalInstance.dismiss();
			};
			$scope.secretkeyName = secretkeyName;
			$scope.downFile = function(){
				$("#excel-export-iframe").attr("src", "safety/secretKey/downPrivateKeyFile.do?fileid="+privateKeyFileId);
				$modalInstance.dismiss();
			};
			$scope.remainingtime = "10分00秒";
			var ts = 10*60*1000;//总共毫秒数
			var timers;
			function timer()  {  
                var mm = parseInt(ts / 1000 / 60 % 60, 10);//计算剩余的分钟数  
                var ss = parseInt(ts / 1000 % 60, 10);//计算剩余的秒数  
                mm = $scope.checkTime(mm);  
                ss = $scope.checkTime(ss);  
                $scope.remainingtime = mm + "分" + ss + "秒";  
                
                if(ts==0){
                	$modalInstance.dismiss();
                	clearInterval(timers);//取消计时
                }
                ts = ts - 1000;
            }  
			$scope.checkTime = function(i)    {    
               if (i < 10) {    
                   i = "0" + i;    
               }    
               return i;    
            }
			timers = setInterval(timer,1000); 
		});
