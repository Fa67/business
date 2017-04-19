'use strict';
angular.module('eayunApp.controllers')
/**
 * @ngdoc function
 * @name appRouter
 * @description 配置路由
 */
.config(function($stateProvider, $urlRouterProvider) {
	$stateProvider.state('app.safety.secretkeyDetail', {
		url : '/secretkey/secretkeyDetail/:skid',
		templateUrl : 'views/safety/secretkey/secrekeydetail.html',
		controller : 'secretkeyDetaillCtrl'
	});
}).controller(
		'secretkeyDetaillCtrl',
		function($scope, $state, $rootScope, $location, $anchorScroll,
				eayunModal, eayunHttp, toast, $stateParams,powerService) {

			$scope.model = {};

			// 查询列表
			$scope.myTable = {
				source : 'safety/secretKey/getsecrekeyByid.do',
				api : {},
				getParams : function() {
					return {
						secretkeyId : $stateParams.skid
					};
				}
			};
			eayunHttp.post('safety/secretKey/getsecrekeyByid.do',{secretkeyId : $stateParams.skid}).then(function(response){
				$scope.model = response.data[0];
		    });
			// 权限控制
			powerService.powerRoutesList().then(
				function(powerList) {
					$scope.buttonPower = {
							isSecretKeyUnBind : powerService.isPower('secretkey_unbind'),// SSH密钥解绑
				};
			});
		
			
			$scope.skvmmyTable = {
				source : 'safety/secretKey/getsecretkeyByIdAndVmList.do',
				api : {},
				getParams : function() {
					return {
						secretkeyId : $stateParams.skid
					};
				}
			};
			/**
		     * 云主机状态 显示
		     */
		    $scope.checkVmStatus =function (model){
		    	if('余额不足'==model.status || '已到期'==model.status){
		    		return 'ey-square-disable';
		    	}
		    	else if(model.status=='运行中'){
					return 'ey-square-right';
				}  
				else if(model.status=='已关机'){
					return 'ey-square-disable';
				}
				else if(model.status=='暂停服务' || model.status=='故障'){
					return 'ey-square-error';
				}
				else{
					return'ey-square-warning';
				}
		    };
			
			$scope.copyToClipboard =function (){
				var clipboard = new Clipboard('.ey-copy-sk');
			    clipboard.on('success', function(e) {
		            //console.info('Action:', e.action);
		           // console.info('Text:', e.text);
		           // console.info('Trigger:', e.trigger);
		            toast.success('复制成功');
		        });

		        clipboard.on('error', function(e) {
		           // console.error('Action:', e.action);
		            //console.error('Trigger:', e.trigger);
		        	eayunModal.warning('复制失败');
		        });
			    }
			
			var list=[{route:'app.safety.safetybar.secretkeylist',name:'SSH秘钥'}];
			$rootScope.navList(list,'SSH秘钥详情','detail');
			// pop框方法
			$scope.hintTagShow = [];
			$scope.openTableBox = function(obj) {
				if (obj.type == 'tagName') {
					$scope.hintTagShow[obj.index] = true;
				}
				$scope.ellipsis = obj.value;
			};
			$scope.closeTableBox = function(obj) {
				if (obj.type == 'tagName') {
					$scope.hintTagShow[obj.index] = false;
				}
			};
			
			
			$scope.Unbundling = function(secretkeyId,vmId,secretkeyName) {
				var prjId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '';
				var dcId = sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : '';
				eayunModal.confirm('确定解绑云主机？').then(function() {
					eayunHttp.post('cloud/vm/queryRouteInfoByVm.do',vmId).then(function (response){
						  if(response.data && response.data.data){
							  var data = response.data.data;
							  if (data.vmStatus != 'SHUTOFF'){
								  eayunModal.warning("请将云主机关机");
								  $scope.skvmmyTable.api.draw();
								  return;
							  }
							  if(!data.routeId){
								  eayunModal.warning("请将云主机所在子网连接路由");
								  $scope.skvmmyTable.api.draw();
								  return;
							  }
							  eayunHttp.post('safety/secretKey/UnbundlingKeyTovm.do',
									{											
										secretkeyId:secretkeyId,
										secretkeyName:secretkeyName,
										prjId:prjId,
										dcId:dcId,
										vmId:vmId
									}).then(function(data) {
										if (data.data.respCode=='000000') {
											toast.success('解绑成功');
											$scope.skvmmyTable.api.draw();
										}
									});

						  }
					})
				})

			};
			
			

		});
