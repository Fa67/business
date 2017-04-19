'use strict';

angular
		.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(
				function($stateProvider, $urlRouterProvider) {
					$urlRouterProvider.when('/app/safety/safetybar/group',
							'/app/safety/safetybar/group/list');
					$stateProvider
							.state('app.safety.safetybar.group', {
								url : '/group',
								template : '<div data-ui-view=""></div>'
							})
							.state(
									'app.safety.safetybar.group.list',
									{
										url : '/list',
										templateUrl : 'views/safety/securitygroup/groupmng.html',
										controller : 'CloudhostGroupCtrl'
									})
							.state(
									"app.safety.groupRule",
									{
										url : '/groupRule/:dcId/:prjId/:sgId',
										templateUrl : 'views/safety/securitygroup/rulemng.html',
										controller : 'groupRuleCtrl'
									});
				})

		/**
		 * @ngdoc function
		 * @name eayunApp.controller:CloudhostGroupCtrl
		 * @description # CloudhostGroupCtrl 安全组Controller
		 */
		.controller(
				'CloudhostGroupCtrl',
				function($scope, $rootScope, $state, eayunHttp, eayunModal,
						toast, powerService) {
					powerService.powerRoutesList()
							.then(
									function(powerList) {
										$scope.buttonPower = {
											isAdd : powerService
													.isPower('group_add'), // 创建
											isEdit : powerService
													.isPower('group_edit'), // 编辑
											isGroupRule : powerService
													.isPower('group_rulemng'), // 管理规则
											isTag : powerService
													.isPower('group_tag'), // 标签
											isDelete : powerService
													.isPower('group_drop'),
											isruleAdd:powerService
														.isPower('group_rule_add'),//添加规则
											isruleDelete:powerService
														.isPower('group_rule_delete'),//删除规则
											isHostAdd:powerService
														.isPower('group_host_add'),//添加云主机
											isHostDetele:powerService
														.isPower('group_host_delete')//移除
														
														
										// 删除

										};
									});

					 
					 
					  $scope.cancel = function (){
							 $modalInstance.dismiss();
						  };
					
					$rootScope.safeState = null;
					var list=[];
					$rootScope.navList(list,'安全组');
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
					// HTML页面model值双向绑定到getParams统一传到后台Java代码Controller中
					$scope.myTable = {
						source : 'cloud/securitygroup/getSecurityGroupList.do',
						api : {},
						getParams : function() {
							return {
								prjId : sessionStorage["dcPrj"] ? JSON
										.parse(sessionStorage["dcPrj"]).projectId
										: '',
								dcId : sessionStorage["dcPrj"] ? JSON
										.parse(sessionStorage["dcPrj"]).dcId
										: '',
								name : $scope.sgName || ''
							};
						}
					};
					$scope.parseJson = function(tagsStr) {
						var json = {};
						if (tagsStr) {
							json = JSON.parse(tagsStr);
						}
						return json;
					};
					// 监视器[监视数据中心、项目id变化]
					$scope.$watch('model.projectvoe', function(newVal, oldVal) {
						if (newVal !== oldVal) {
							$scope.myTable.api.draw();
						}
					});
					/**
					 * 查询当前sessionStore 是否存在用户信息
					 */
					$scope.checkUser = function() {
						var user = sessionStorage["userInfo"]
						if (user) {
							user = JSON.parse(user);
							if (user && user.userId) {
								return true;
							}
						}
						return false;
					};
					//查询按钮
					$scope.sourceName=function(){
						$scope.myTable.api.draw();
					}
					
					// 页面中回车键触发查询事件；
					$(function() {
						document.onkeydown = function(event) {
							var e = event || window.event
									|| arguments.callee.caller.arguments[0];
							if (!$scope.checkUser()) {
								return;
							}
							if (e && e.keyCode == 13) {
								$scope.myTable.api.draw();
							}
						};
					});
					// 创建安全组
					$scope.createGroup = function() {
						var result = eayunModal
								.open({
									
									templateUrl : 'views/safety/securitygroup/addgroup.html',
									controller : 'cloudhostSGAddCtrl',
									resolve : {
										prjId : function() {
											return {
												prjId : sessionStorage["dcPrj"] ? JSON
														.parse(sessionStorage["dcPrj"]).projectId
														: ''
											};
										}
									}
								}).result;
						result.then(
										function(value) {
											// 创建页面点击提交执行后台Java代码
											eayunHttp
													.post(
															'cloud/securitygroup/addSecurityGroup.do',
															value)
													.then(
															function(response) {

																if (response.data.code != "010120") {
																	var name = "";
																	if (response.data.sgName.length > 9) {
																		name = response.data.sgName
																				.substr(0,8)
																				+ "...";
//																		name = response.data.sgName;
																		toast
																		.success('添加安全组'+name+'成功');
																	} else {
																		name = response.data.sgName;
																		toast
																		.success('添加安全组'+name+'成功');
																	}
																	
																}
																$scope.myTable.api
																		.draw();
															});
										}, function() {
											// console.info('取消');
										});

					};

					// 修改安全组
					$scope.editGroup = function(item) {

						var result = eayunModal
								.open({
									
									templateUrl : 'views/safety/securitygroup/editgroup.html',
									controller : 'cloudhostSGEditCtrl',
									resolve : {
										group : function() {
											return item;

										}
									}
								}).result;
						// 修改点击提交后
						result
								.then(
										function(value) {
											// 创建页面点击提交执行后台Java代码
											eayunHttp
													.post(
															'cloud/securitygroup/updateSecurityGroup.do',
															value)
													.then(
															function(response) {
																if (response.data.code != "010120") {// response.data.code!="010120"
																										// 代表后台返回出错
																	var name = "";
																	if (response.data.sgName.length > 9) {
																		name = response.data.sgName
																				.substring(
																						0,
																						8)
																				+ "...";
//name = response.data.sgName;
																		
																		toast
																		.success('修改安全组'+name+'成功');
																	} else {
																		name = response.data.sgName;
																		
																		toast
																		.success('修改安全组'+name+'成功');
																	}
																	
																	
																}
																;
																$scope.myTable.api
																		.draw();
															});

										}, function() {
											// console.info('取消');
										});
					};

					// 列表页名称查询
					$scope.getGroup = function() {
						$scope.myTable.api.draw();
					};
					$scope.options = {
						searchFn: function () {
							$scope.myTable.api.draw();
			            }
					};

					// 删除安全组
					$scope.deleteGroup = function(item) {
						// var name = "";
						// if(item.sgName.length>7){
						// name = item.sgName.substring(0,7)+"...";
						// }else{
						// name = item.sgName;
						// }
						eayunModal
								.confirm('确定要删除安全组' + item.sgName + '?')
								.then(
										function() {
											eayunHttp
													.post(
															'cloud/securitygroup/deleteGroup.do',
															{
																dcId : item.dcId,
																prjId : item.prjId,
																sgId : item.sgId,
																sgName : item.sgName
															})
													.then(
															function(response) {
																if (response.data.code != "010120") {
																	toast
																			.success('删除安全组成功');
																}
																$scope.myTable.api
																		.draw();
															});
										});
					};
					// 管理规则
					$scope.manageGroupRule = function(item) {
						$state.go('app.safety.groupRule', {
							"dcId" : item.dcId,
							"prjId" : item.prjId,
							"sgId" : item.sgId
						}); // 跳转后的URL;
					};
					/* 标签 */
					$scope.tagResource = function(resType, resId) {
						var result = eayunModal.open({
							//showBtn : false,
							//title : '标记资源',
							//width : '600px',
							//height : '400px',
							templateUrl : 'views/tag/tagresource.html',
							controller : 'TagResourceCtrl',
							resolve : {
								resType : function() {
									return resType;
								},
								resId : function() {
									return resId;
								}
							}
						}).result;
						result.then(function() {
							$scope.myTable.api.draw();
						}, function() {
							$scope.myTable.api.draw();
						});
					};

				})
		/**
		 * @ngdoc function
		 * @name eayunApp.controller:cloudhostSGEditCtrl
		 * @description # cloudhostSGEditCtrl 安全组--安全组--编辑安全组
		 */
		.controller(
				'cloudhostSGEditCtrl',
				function($scope, eayunModal, eayunHttp, group) {
					// angular复制对象赋给新建页面的model
					$scope.model = angular.copy(group);

					$scope.commit = function() {

						$scope.ok($scope.model);
					};

					$scope.cancel = function (){
						  $modalInstance.close();
					  };
					// ajax验证 编辑安全组判断重名
					$scope.validGroupName = function() {
						var title = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
						$scope.flag = false;
						if ($("#name").val().match(title)) {
							$scope.model.name = $("#name").val();// 作为编辑后的名称
							return eayunHttp.post(
									'cloud/securitygroup/getGroupById.do',
									$scope.model).then(function(response) {
								if (true == response.data) {
									return false;
								} else {
									return true;
								}

							});

						} else {
							$scope.flag = true;
						}
					};

				})
		/**
		 * @ngdoc function
		 * @name eayunApp.controller:cloudhostSGAddCtrl
		 * @description # cloudhostSGAddCtrl 安全组--安全组--创建安全组
		 */
		.controller(
				'cloudhostSGAddCtrl',
				function($scope, eayunModal, eayunHttp, prjId,$modalInstance) {
					// 直接将创建页面所有的项目放入当前的$scope.model中 开始
					$scope.model = {};
					eayunHttp.post('cloud/vm/getProListByCustomer.do').then(
							function(response) {
								$scope.projectList = response.data;
								angular.forEach($scope.projectList, function(
										value, key) {
									if (value.projectId == prjId.prjId) {
										$scope.model.project = value;

									}
								});
							});
					 $scope.cancel = function (){
						 $modalInstance.dismiss();
					  };
					
					
					// 直接将创建页面所有的项目放入当前的$scope.model中 结束
					$scope.commit = function() {
						//var data=$scope.model;
						//$scope.ok=function (data){
							$modalInstance.close($scope.model);
						//}
					};

					// 验证 创建重名
					$scope.checkGroupName = function() {
						
						$scope.checkNameMoren=true;
						$scope.checkNamedefault=true;
						if($scope.model.name=='默认安全组'){
							//console.info('moren');
							$scope.checkNameMoren=false;
							$scope.checkName=true;
							return 
							
						}
						
						if($scope.model.name=='default'){
							//console.info('default');
							$scope.checkNamedefault=false;
							$scope.checkName=true;
							return 
						}
						var cloudGroup = {
							prjId : $scope.model.project.projectId,
							sgName : $scope.model.name
						};
						
						eayunHttp.post('cloud/securitygroup/getGroupByName.do',
								cloudGroup).then(function(response) {
							$scope.checkName = response.data;
						});
					};
					// 创建页面---》切换项目，判断名称重复
					$scope.changePrj = function() {
						$scope.checkGroupName();
					};

				})
		/** **************************************安全组规则路由Controller开始************************************* */
		/**
		 * @ngdoc function
		 * @name eayunApp.controller:groupRuleCtrl
		 * @description # groupRuleCtrl 安全组规则Controller
		 */
		.controller(
				'groupRuleCtrl',
				function($scope, $rootScope, eayunHttp, eayunModal,
						$stateParams, $state, toast, powerService) {
					var list=[{route:'app.safety.safetybar.group',name:'安全组'}];
					$rootScope.navList(list,'安全组详情','detail');
					$scope.cancel = function (){
						  $modalInstance.close();
					  };
					  $scope.vmNameEditable = false;
					  $scope.vmDescEditable = false;
					  $scope.checkNameMoren=true;
					$scope.checkNamedefault=true;
					$scope.checkVmNameFlag = true;
					// pop框方法
					$scope.openPopBox = function(obj) {
						if (obj.type == 'safeDesc') {
							$scope.safeShow = true;
						}
						if (obj.type == 'tagName') {
							$scope.tagShow = true;
						}
						$scope.description = obj.value;
						$scope.ellipsis = obj.value;
					};
					$scope.closePopBox = function(type) {
						if (type == 'safeDesc') {
							$scope.safeShow = false;
						}
						if (type == 'tagName') {
							$scope.tagShow = false;
						}
					};
					powerService.powerRoutesList().then(
							function(powerList) {
								$scope.buttonPower = {
									isGroupRule : powerService
											.isPower('group_rulemng'), // 管理规则
									isDelete : powerService
											.isPower('group_drop'),
											isruleAdd:powerService
											.isPower('group_rule_add'),//添加规则
								isruleDelete:powerService
											.isPower('group_rule_delete'),//删除规则
								isHostAdd:powerService
											.isPower('group_host_add'),//添加云主机
								isHostDetele:powerService
											.isPower('group_host_delete'),
											isEdit : powerService
											.isPower('group_edit') // 编辑
											
								// 删除

								};
							});
					// 详情页--用于当前位置
					$rootScope.safeState = "#/app/safety/safetybar/group/list";
					$rootScope.safeName = '安全组';
					// HTML页面model值双向绑定到getParams统一传到后台Java代码Controller中

					$scope.model.sgId = $stateParams.sgId;
					eayunHttp.post('cloud/securitygroup/getGroup.do', {
						dcId : $stateParams.dcId,
						prjId : $stateParams.prjId,
						sgId : $stateParams.sgId
					}).then(function(response) {
						if (null != response.data) {
							$scope.model = response.data;
							$scope.testmodel = response.data;
						}
					});
					// 查询安全组的标签
					$scope.resourceTags = {};
					eayunHttp.post('tag/getResourceTagForShowcase.do', {
						resType : 'securityGroup',
						resId : $stateParams.sgId
					}).then(function(response) {
						$scope.resourceTags = response.data;
					});

					/* 将名称、描述变为可编辑状态 */
					$scope.editNameOrDesc = function(type) {
						if (type == 'fwName') {
							$scope.vmNameEditable = true;
							$scope.vmDescEditable = false;
							$scope.hintNameShow = true;
							$scope.hintDescShow = false;
						}
						if (type == 'fwDesc') {
							$scope.vmNameEditable = false;
							$scope.vmDescEditable = true;
							$scope.hintNameShow = false;
							$scope.hintDescShow = true;
						}

						$scope.vmEditModel = angular.copy($scope.model, {});
						
						

					};
					/* 校验编辑名称是否存在 */
					$scope.checkFwNameExist = function() {
						
						
						
						
						var value = $scope.vmEditModel.sgName;
						
						
						if(value=='默认安全组'){
							//console.info('moren');
							$scope.checkNameMoren=false;
							$scope.checkVmNameFlag = true;
							
							return 
							
						}
						
						if(value=='default'){
							//console.info('default');
							$scope.checkNamedefault=false;
							$scope.checkVmNameFlag = true;
							return 
						}
						
						var nameTest = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
						$scope.flag = false;
						if(typeof(value) == "undefined"){
							value="";
						}
						if (value.match(nameTest)) {
							
							
							$scope.model.dcId = $stateParams.dcId;
							$scope.model.prjId = $stateParams.prjId;
							$scope.model.sgId = $stateParams.sgId;
							$scope.checkNameModel=angular.copy($scope.model, {});
							$scope.checkNameModel.sgName=value;
							return eayunHttp.post(
									'cloud/securitygroup/getGroupByName.do',
									$scope.checkNameModel).then(function(response) {

								if (true == response.data) {
									$scope.checkVmNameFlag = true;
								} else {
									$scope.checkVmNameFlag = false;
								}

							});

						} else {
							$scope.checkVmNameFlag = true;
						}
						
						
						
						
					};
					/* 保存编辑的名称、描述,并刷新界面 */
					$scope.saveEdit = function(type) {
						var value=$scope.model.sgName;
						// if($scope.vmEditModel.sgName!=undefined){
						// 
						// }else{
						// $scope.model.sgName = $stateParams.sgName;
						// }
						if($scope.vmEditModel.sgDescription!=undefined){
							$scope.model.sgDescription = $scope.vmEditModel.sgDescription;
						}else{
							$scope.vmEditModel.sgDescription=$scope.model.sgDescription ;
						}
						
						$scope.model.dcId = $stateParams.dcId;
						$scope.model.prjId = $stateParams.prjId;
						$scope.model.sgId = $stateParams.sgId;
					
						$scope.model.sgName = $scope.vmEditModel.sgName;
						eayunHttp.post(
								'cloud/securitygroup/updateSecurityGroup.do',
								$scope.model).then(
								function(response) {
									if (null != response.data
											&& 'null' != response.data
											&& response.data.code != "010120") {
										var safetyName =$scope.model.sgName.length>9?$scope.model.sgName
										.substring(
												0,
												8)
										+ "...":$scope.model.sgName;
										toast.success('安全组'+safetyName+'修改成功', 1000);
										if (type == 'fwName') {
											$scope.vmNameEditable = false;
											$scope.hintNameShow = false;
										}
										if (type == 'fwDesc') {
											$scope.vmDescEditable = false;
											$scope.hintDescShow = false;
										}
										$scope.model = angular.copy($scope.vmEditModel, {});
										
									}

									
									
									
								});
						
					};
					/* 取消名称、描述的可编辑状态 */
					$scope.cancelEdit = function(type) {
						if (type == 'fwName') {
							$scope.vmNameEditable = false;
							$scope.hintNameShow = false;
						}
						if (type == 'fwDesc') {
							$scope.vmDescEditable = false;
							$scope.hintDescShow = false;
						}
						
					};

					$scope.myGroupRuleTable = {
						source : 'cloud/securitygroup/getRules.do',
						api : {},
						getParams : function() {
							return {
								dcId : $stateParams.dcId,
								prjId : $stateParams.prjId,
								sgId : $stateParams.sgId
							};
						}
					};

					$scope.myGroupcloudhostTable = {
						source : 'cloud/grouprule/queryCloudHostList.do',
						api : {},
						getParams : function() {
							return {
								dcId : $stateParams.dcId,
								prjId : $stateParams.prjId,
								sgId : $stateParams.sgId
							};
						}
					};
					// 防火墙状态显示
					$scope.checkVmStatus = function(Model) {
						$scope.vmStatusClass = '';
						  if(Model.cheang_st=='1'||Model.cheang_st=='2'||Model.cheang_st=='3')
			                {
			                    return 'gray';
			                }
						if (Model.vm_status && Model.vm_status == 'ACTIVE') {
							return 'green';
						} else if (Model.vm_status == 'SHUTOFF' ) {
							return 'gray';
						} else if (Model.vm_status == 'SUSPENDED'||Model.vm_status=='SOFT_DELETED'||Model.vm_status == 'ERROR') {
							return 'red';
						} else {
							return 'yellow';
						}
					};

					$scope.createRule = function() {
						var result = eayunModal
								.open({
									
									templateUrl : 'views/safety/securitygroup/addrule.html',
									controller : 'cloudhostSGRuleAddCtrl',
									resolve : {
										originalSgId : function() {
											return $stateParams.sgId;
										}
									}

								}).result;
						result.then(function(value) {
							// 创建页面点击提交执行后台Java代码
							eayunHttp.post('cloud/grouprule/addRule.do', value)
									.then(function(response) {
										// 如果创建成功，刷新当前列表页
										if (null!=response.data&&'null'!=response.data&&response.data.code != "010120") {
											toast.success('添加规则成功');
										}
										$scope.myGroupRuleTable.api.draw();
									});
						}, function() {
							// console.info('取消');
						});

					};
					// 删除规则
					//$scope.deletefag=true;
					$scope.deleteGroupRule = function(item) {

						var direction = item.direction == 'ingress' ? '入方向'
								: '出方向';
						var min = item.portRangeMin == 'null' ? ''
								: item.portRangeMin;
						var max = item.portRangeMax == 'null' ? ''
								: item.portRangeMax;
						
						
						var duankou=(item.portRangeMin==item.portRangeMax?item.protocolExpand:item.portRangeMin+'-'+item.portRangeMax)==''?'-':(item.portRangeMin==item.portRangeMax?item.protocolExpand:item.portRangeMin+'-'+item.portRangeMax);
						var pro=item.protocol==null?'所有':item.protocol =='null' ?'所有':item.protocol.toLowerCase()=='all'?'所有':item.protocol;

						
					
						if(duankou=='-'){
							if(item.portRangeMin!='null'){
								duankou=item.portRangeMin+'-'+item.portRangeMax;
								
							}else{
								
								duankou='--'
									}
							
						}
						if(duankou=='null'){
							duankou='--'
						}
						var icmp=item.icMp;
						if(icmp==''){
							icmp='--'
						}
						if(icmp!='--'){
							duankou='--'
						}
						
						var yuan = item.remoteIpPrefix!='null'&& null!=item.remoteIpPrefix ? item.remoteIpPrefix + "(CIDR)": item.remoteGroupName!=''?item.remoteGroupName :'0.0.0.0/0 (CIDR)';
						if(item.remoteIpPrefix=='null'&&item.remoteGroupName==''){
							yuan='0.0.0.0/0 (CIDR)';
						}
						eayunModal
								.confirm(
										"确定要删除规则“ ‘" + direction + "’/‘" 
												+ pro+"’/‘"+duankou+"’/‘"
												+icmp +"’/‘"
												+yuan+"’”？")
								.then(
										function() {
											 item.deletefag=true;
											eayunHttp
													.post(
															'cloud/grouprule/deleteGroupRule.do',
															{
																dcId : item.dcId,
																prjId : item.prjId,
																sgrId : item.sgrId,
																
												
															}   )
													.then(
															function(response) {
																item.deletefag=false;
																if (response.data
																		&& response.status == 200) {
																	toast
																			.success('删除规则成功');
																}
																$scope.myGroupRuleTable.api
																		.draw();

															});
										});

					};

					/**
					 * 删除云主机
					 */
					$scope.deletecloudhost = function(sgId, vmid, prjid, sgname,vmname) {
						eayunModal
								.confirm('确定要把云主机'+vmname+'从安全组中移除吗？')
								.then(
										function() {
											
											eayunHttp
													.post(
															'cloud/grouprule/securityGroupRemoveCloudHost.do',
															{
																sgId : sgId,
																vmid : vmid,
																prjid : prjid,
																dcid : $stateParams.dcId,
																sgname : sgname,
																vmname:vmname
															})
													.then(
															function(data) {
																
																if (data.data.respCode == '000000') {
																	toast
																			.success('移除云主机成功');
																	$scope.myGroupcloudhostTable.api
																			.draw();
																}

															})

										})

					};

					$scope.manage = function(sgid, sgname) {

						var result = eayunModal
								.open({
									
									templateUrl : 'views/safety/securitygroup/selectcloudhost.html',
									controller : 'ToDoFwRuleCtrl',
									resolve : {
										model : function() {
											return eayunHttp
													.post(
															'cloud/grouprule/getaddcloudhostlist.do',
															{
																sgId : sgid,
																projectId : $stateParams.prjId,
																sgname : sgname,
																cusorg : ''
															});
										}

									}

								}).result;
						result
								.then(function(resultData) {
									eayunHttp
											.post(
													'cloud/grouprule/securitygroupaddcloudHost.do',
													resultData)
											.then(
													function(response) {
														
														if (response.data.respCode == '000000') {
															toast
																	.success('添加云主机成功');
															$scope.myGroupcloudhostTable.api
																	.draw();

														} else {
															if(undefined!=response.message){
																eayunModal
																.error(response.message);
															}
															
															
														}
													});
								})

					};
					
					
				})
		/**
		 * @ngdoc function
		 * @name eayunApp.controller:cloudhostSGRuleAddCtrl
		 * @description # cloudhostSGRuleAddCtrl 创建安全组规则
		 */
		.controller(
				'cloudhostSGRuleAddCtrl',
				function($scope, eayunModal, eayunHttp, $stateParams,
						originalSgId,$modalInstance) {
					 $scope.cancel = function (){
						 $modalInstance.dismiss();
					  };
					$scope.model = {};
					$scope.model.dcId = $stateParams.dcId;
					$scope.model.prjId = $stateParams.prjId;
					$scope.model.sgId = $stateParams.sgId;
					$scope.model.originalSgId = originalSgId;
					// 去除创建页面打开默认js验证空
					
					//切换
					$scope.checktype=function(){
						//console.info($scope.model.icmpcode);
						//if($scope.model.icmpcode=='99'){
							//console.info($scope.model.icmptype);
							$scope.model.icmpcode=undefined;
							$scope.model.port_range_min = '';
							$scope.model.port_range_max = '';
							$scope.model.protocolExpand='';
							
						//}
						
						
					};
					$scope.chektcporicmp=function(){
						$scope.model.icmpcode=undefined;
						$scope.model.icmptype=undefined;
						$scope.model.port_range_min = '';
						$scope.model.port_range_max = '';
						$scope.model.protocolExpand='';
					};
					$scope.model.protocol = null;// 协议
					if ($scope.model.ICMPType == '99'
							&& $scope.model.ICMPCode == '00') {
						$scope.model.icmp = '所有';

					};
					$scope.model.Direction = 'ingress';
					$scope.model.from = 'CIDR';
					// $scope.model.direction = null;//方向
					// $scope.model.sgId = null;//
					// $scope.model.ethertype = null;//ipv4 ipv6
					/** **************端口验证开始****************** */
					$scope.small = false;
					$scope.big = false;
					$scope.smallPort = function(type) {
						if(type=='type'){
							$scope.model.protocolExpand='';
						}
						
						$scope.model.icmpcode=undefined;
						$scope.model.icmptype=undefined;
						$scope.minMsg = "";
						var nameTest = /^(^[1-9]\d{0,3}$)|(^[1-5]\d{4}$)|(^6[0-4]\d{3}$)|(^65[0-4]\d{2}$)|(^655[0-2]\d$)|(^6553[0-5]$)$/;
						var min = $("#port_range_min").val();
						var max = $("#port_range_max").val();
						if (min == null) {
							$scope.small = true;
							$scope.minMsg = "请输入1-65535的整数";

						} else {// 小的有值
							if (!min.match(nameTest)) {// 小的有值 且符合正则
								$scope.small = true;
								$scope.minMsg = "请输入1-65535的整数";
							} else {
								$scope.small = false;
								if (max != null && max.match(nameTest)) {
									if (parseInt(min) > parseInt(max)) {
										$scope.small = true;
										$scope.minMsg = "请输入小于终止端口的整数";
										$scope.big = false;
									} else if (parseInt(min) <= parseInt(max)) {
										$scope.small = false;
										$scope.big = false;
										$scope.minMsg = "";
									}
								}
							}
						}
						
					};
					
					$scope.bigPort = function(type) {
						if(type=='type'){
							$scope.model.protocolExpand='';
						}
					
						$scope.maxMsg = "";
						var nameTest = /^(^[1-9]\d{0,3}$)|(^[1-5]\d{4}$)|(^6[0-4]\d{3}$)|(^65[0-4]\d{2}$)|(^655[0-2]\d$)|(^6553[0-5]$)$/;

						var min = $("#port_range_min").val();
						var max = $("#port_range_max").val();
						if (max == null) {
							$scope.big = true;
							$scope.maxMsg = "请输入1-65535的整数";

						} else {// 大的有值
							if (!max.match(nameTest)) {// 大的有值 且符合正则
								$scope.big = true;
								$scope.maxMsg = "请输入1-65535的整数";
							} else {
								$scope.big = false;
								if (min != null && min.match(nameTest)) {
									if (parseInt(min) > parseInt(max)) {
										$scope.big = true;
										$scope.maxMsg = "请输入大于起始端口的整数";
										$scope.small = false;
									} else if (parseInt(min) <= parseInt(max)) {
										$scope.big = false;
										$scope.small = false;
										$scope.maxMsg = "";
									}
								}
							}
						}

					};
					/** ***************端口验证结束***************** */
					// 创建安全组规则的选择的远程安全组
					$scope.querySecurityGroups = function() {
						if ($scope.model.from == "SecurityGroup") {
							eayunHttp
									.post(
											'cloud/securitygroup/getGroupsByProjectId.do',
											{
												dcId : $stateParams.dcId,
												prjId : $stateParams.prjId
											})
									.then(
											function(response) {

												$scope.securityGroupItems = response.data;
											});

						}
					};

					var item = function(_value, _readonly, _min, _max) {
						this.value = _value;
						this.readonly = _readonly;
						this.min = _min;
						this.max = _max;
						this.valid = true;
					};

					$scope.commit = function() {
						$scope.commitfag=true;
						

						$scope.model.icmp = '--';

						if ($scope.model.icmptype == '99'
								&& $scope.model.icmpcode == '99') {

							$scope.model.icmp = '所有';
						}
						
						if ($scope.model.icmptype!='' && $scope.model.icmptype != '99' &&$scope.model.icmptype!=undefined) {
						
							$scope.model.icmp = $scope.model.icmptype + "/"
									+ $scope.model.icmpcode;
						}

						if($scope.model.icmptype==0){
							$scope.model.icmp = $scope.model.icmptype + "/"
							+ $scope.model.icmpcode;
						}
						 $modalInstance.close($scope.model);
						
					};
					
					$scope.butt=function (){
						$scope.model.Direction = 'ingress';
						$scope.buttclasstcp=false;
						$scope.buttclassudp=false;
						$scope.buttclassicmp=false;
						$scope.buttclassdns=false;
						$scope.buttclasshttp=false;
						$scope.buttclasshttps=false;
						$scope.buttclassimap=false;
						$scope.buttclassimaps=false;
						$scope.buttclassladp=false;
						$scope.buttclassmssql=false;
						$scope.buttclassmysql=false;
						$scope.buttclasspop3=false;
						$scope.buttclasspop3s=false;
						$scope.buttclassrdp=false;
						$scope.buttclasssmtp=false;
						$scope.buttclasssmtps=false;
						$scope.buttclassssh=false;
						
						$scope.buttclassfalse=false;
						$scope.buttclasstrue=false;
						
					}
					
					$scope.alltcp = function() {
					
						$scope.butt();
						$scope.buttclasstcp=true;
						
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '1';
						$scope.model.port_range_max = '65535';
						$scope.smallPort();
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;
						
					}
					$scope.alludp = function() {
						$scope.butt();
						$scope.buttclassudp=true;
						$scope.model.protocol = 'UDP';
						$scope.model.port_range_min = '1';
						$scope.model.port_range_max = '65535';
						$scope.smallPort();
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;
						
					}
					$scope.allicmp = function() {
						
						$scope.butt();
						$scope.buttclassicmp=true;
						$scope.model.protocol = 'ICMP';
						$scope.model.icmpcode = '99';
						$scope.model.icmptype = '99';
						$scope.model.port_range_min ='';
						$scope.model.port_range_max = '';
						
					}
					$scope.dns = function() {
						$scope.butt();
						$scope.buttclassdns=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '53';
						$scope.model.port_range_max = '53';

						$scope.model.protocolExpand = '53(DNS)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;
					}
					$scope.http = function() {
						$scope.butt();
						$scope.buttclasshttp=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '80';
						$scope.model.port_range_max = '80';
						$scope.model.protocolExpand = '80(HTTP)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.https = function() {
						$scope.butt();
						$scope.buttclasshttps=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '443';
						$scope.model.port_range_max = '443';
						$scope.model.protocolExpand = '443(HTTPS)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.imap = function() {
						$scope.butt();
						$scope.buttclassimap=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '143';
						$scope.model.port_range_max = '143';
						$scope.model.protocolExpand = '143(IMAP)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.imaps = function() {
						$scope.butt();
						$scope.buttclassimaps=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '993';
						$scope.model.port_range_max = '993';
						$scope.model.protocolExpand = '993(IMAPS)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.ladp = function() {
						$scope.butt();
						$scope.buttclassladp=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '389';
						$scope.model.port_range_max = '389';
						$scope.model.protocolExpand = '389(LDAP)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.mssql = function() {
						$scope.butt();
						$scope.buttclassmssql=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '1443';
						$scope.model.port_range_max = '1443';
						$scope.model.protocolExpand = '1443(MS SQL)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;
					}
					$scope.mysql = function() {

						$scope.butt();
						$scope.buttclassmysql=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '3306';
						$scope.model.port_range_max = '3306';
						$scope.model.protocolExpand = '3306(MYSQL)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.pop3 = function() {
						$scope.butt();
						$scope.buttclasspop3=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '110';
						$scope.model.port_range_max = '110';
						$scope.model.protocolExpand = '110(POP3)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.pop3s = function() {
						$scope.butt();
						$scope.buttclasspop3s=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '995';
						$scope.model.port_range_max = '995';
						$scope.model.protocolExpand = '995(POP3S)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;
					}
					$scope.rdp = function() {
						$scope.butt();
						$scope.buttclassrdp=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '3389';
						$scope.model.port_range_max = '3389';
						$scope.model.protocolExpand = '3389(RDP)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}

					$scope.smtp = function() {

						$scope.butt();
						$scope.buttclasssmtp=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '25';
						$scope.model.port_range_max = '25';
						$scope.model.protocolExpand = '25(SMTP)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.smtps = function() {
						$scope.butt();
						$scope.buttclasssmtps=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '465';
						$scope.model.port_range_max = '465';
						$scope.model.protocolExpand = '465(SMTPS)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					}
					$scope.ssh = function() {
						$scope.butt();
						$scope.buttclassssh=true;
						$scope.model.protocol = 'TCP';
						$scope.model.port_range_min = '22';
						$scope.model.port_range_max = '22';
						$scope.model.protocolExpand = '22(SSH)';
						$scope.smallPort('a');
						$scope.bigPort();
						$scope.small = false;
						$scope.big = false;

					};

					/** *************************网络地址规则******************************** */
					$scope.model.source_ip_address1 = 0;
					$scope.model.source_ip_address2 = 0;
					$scope.model.source_ip_address3 = 0;
					$scope.model.source_ip_address4 = 0;
					$scope.model.source_ip_address5 = 0;
					$scope.model.destination_ip_address1 = 0;
					$scope.model.destination_ip_address2 = 0;
					$scope.model.destination_ip_address3 = 0;
					$scope.model.destination_ip_address4 = 0;
					$scope.model.destination_ip_address5 = 0;
					var regx = /^(0|[1-9]\d*)$/;
					$scope.sourrange = "0.0.0.0/0表示所有IP地址";
					$scope.destrange = "0.0.0.0/0表示所有IP地址";
					$scope.cidraError = true;
					$scope.cidrbError = true;
					function checkCidr0_255(val, fromFunc, type) {
						if (fromFunc == "focus") {
							if (type == "a") {
								$scope.sourrange = "请输入0到255的整数！";
							} else {
								$scope.destrange = "请输入0到255的整数！";
							}
						} else {
							$scope.sourrange = "0.0.0.0/0表示所有IP地址";
							$scope.destrange = "0.0.0.0/0表示所有IP地址";
						}
						if (val >= 0 && val <= 255 && regx.test(val)) {
							return false;
						} else {
							return true;
						}
					}
					;
					function checkCidr0_32(val, fromFunc, type) {
						if (fromFunc == "focus") {
							if (type == "a") {
								$scope.sourrange = "请输入0到32的整数！";
							} else {
								$scope.destrange = "请输入0到32的整数！";
							}
						} else {
							$scope.sourrange = "0.0.0.0/0表示所有IP地址";
							$scope.destrange = "0.0.0.0/0表示所有IP地址";
						}
						if (val >= 0 && val <= 32 && regx.test(val)) {
							return false;
						} else {
							return true;
						}
					}
					;
				
					
					$scope.checkTypeCidr = function(position, fromFunc) {
						if (position == '' || position == null) {
							$scope.a1Error = false;
							$scope.a2Error = false;
							$scope.a3Error = false;
							$scope.a4Error = false;
							$scope.a5Error = false;
							$scope.b1Error = false;
							$scope.b2Error = false;
							$scope.b3Error = false;
							$scope.b4Error = false;
							$scope.b5Error = false;
							$scope.cidraError = false;
							$scope.cidrbError = false;
							return;
						}
						if (position == 'a1') {
							$scope.a1Error = checkCidr0_255(
									$scope.model.source_ip_address1, fromFunc,
									'a');
						} else if (position == 'a2') {
							$scope.a2Error = checkCidr0_255(
									$scope.model.source_ip_address2, fromFunc,
									'a');
						} else if (position == 'a3') {
							$scope.a3Error = checkCidr0_255(
									$scope.model.source_ip_address3, fromFunc,
									'a');
						} else if (position == 'a4') {
							$scope.a4Error = checkCidr0_255(
									$scope.model.source_ip_address4, fromFunc,
									'a');
						} else if (position == 'a5') {
							$scope.a5Error = checkCidr0_32(
									$scope.model.source_ip_address5, fromFunc,
									'a');
						} else if (position == 'b1') {
							$scope.b1Error = checkCidr0_255(
									$scope.model.destination_ip_address1,
									fromFunc, 'b');
						} else if (position == 'b2') {
							$scope.b2Error = checkCidr0_255(
									$scope.model.destination_ip_address2,
									fromFunc, 'b');
						} else if (position == 'b3') {
							$scope.b3Error = checkCidr0_255(
									$scope.model.destination_ip_address3,
									fromFunc, 'b');
						} else if (position == 'b4') {
							$scope.b4Error = checkCidr0_255(
									$scope.model.destination_ip_address4,
									fromFunc, 'b');
						} else if (position == 'b5') {
							$scope.b5Error = checkCidr0_32(
									$scope.model.destination_ip_address5,
									fromFunc, 'b');
						}
						if (!$scope.a1Error && !$scope.a2Error
								&& !$scope.a3Error && !$scope.a4Error
								&& !$scope.a5Error) {
							$scope.model.source_ip_address = parseInt($scope.model.source_ip_address1)
									+ "."
									+ parseInt($scope.model.source_ip_address2)
									+ "."
									+ parseInt($scope.model.source_ip_address3)
									+ "."
									+ parseInt($scope.model.source_ip_address4)
									+ "/"
									+ parseInt($scope.model.source_ip_address5);
							$scope.cidraError = true;
						} else {
							$scope.cidraError = false;
						}
						if (!$scope.b1Error && !$scope.b2Error
								&& !$scope.b3Error && !$scope.b4Error
								&& !$scope.b5Error) {
							$scope.model.destination_ip_address = parseInt($scope.model.destination_ip_address1)
									+ "."
									+ parseInt($scope.model.destination_ip_address2)
									+ "."
									+ parseInt($scope.model.destination_ip_address3)
									+ "."
									+ parseInt($scope.model.destination_ip_address4)
									+ "/"
									+ parseInt($scope.model.destination_ip_address5);
							$scope.cidrbError = true;
						} else {
							$scope.cidrbError = false;
						}
					};
					$scope.alltcp();

				}).controller('ToDoFwRuleCtrl',
				function($scope, $state, eayunHttp, eayunModal, model,$modalInstance) {
					$scope.model={};
					$scope.list = [];
					$scope.ruleListIsSelected=[];
					
					$scope.list = model.data.data;
					
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
					
					 $scope.cancel = function (){
						 $modalInstance.dismiss();
					  };
					$scope.commit = function() {
						
						for(var i=0;i<$scope.list.length;i++){
							if($scope.list[i].$$selected){
								
								$scope.ruleListIsSelected.push($scope.list[i]);
								
							}
						}
						
						$scope.model.cloudhostlist = $scope.ruleListIsSelected;
						
						 $modalInstance.close($scope.model);
					};

				});
