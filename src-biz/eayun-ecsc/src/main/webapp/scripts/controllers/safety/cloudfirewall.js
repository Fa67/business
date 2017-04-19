'use strict';
angular
		.module('eayunApp.controllers')
		.config(
				function($stateProvider, $urlRouterProvider) {
					$urlRouterProvider.when('/app/safety/safetybar',
							'/app/safety/safetybar/firewall');
					$urlRouterProvider.when('/app/safety/safetybar/firewall',
							'/app/safety/safetybar/firewall/fireWallList');
					$stateProvider.state('app.safety.safetybar', {
						url : '/safetybar',
						templateUrl : 'views/safety/safetybar.html',
					}).state('app.safety.safetybar.firewall', {
						url : '/firewall',
						templateUrl : 'views/safety/firewall/main.html',
					}).state('app.safety.safetybar.firewall.list', {
						url : '/fireWallList',
						templateUrl : 'views/safety/firewall/firewallmng.html',
						controller : 'FireWallList'
					});

				})
		.controller(
				'FireWallList',
				function($scope, $rootScope, $state, eayunHttp, eayunModal,
						$timeout, $cookieStore, toast, powerService) {
					$rootScope.safeState = null;
					var list=[];
					$rootScope.navList(list,'防火墙');
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
					// 查询列表
					$scope.myTable = {
						source : 'safety/firewall/getFireWallList.do',
						api : {},
						getParams : function() {
							return {
								prjId : sessionStorage["dcPrj"] ? JSON
										.parse(sessionStorage["dcPrj"]).projectId
										: '',
								dcId : sessionStorage["dcPrj"] ? JSON
										.parse(sessionStorage["dcPrj"]).dcId
										: '',
								name : $scope.fwName || ''
							};
						}
					};
					$scope.parseJson = function(tagsStr) {
						var json = {};
						if (tagsStr) {
							json = JSON.parse(tagsStr);
						}
						// console.log(json);
						return json;
					};

					// 防火墙状态显示
					$scope.getFireWallStatus = function(model) {
						$scope.vmStatusClass = '';
						if (model.fwStatus == 'ACTIVE' || model.fwStatus == 'PENDING_CREATE') {
							return 'green';
						} else if (model.fwStatus == 'ERROR' || model.fwStatus == 'DOWN') {
							return 'gray';
						} else if ( model.fwStatus == 'PENDING_UPDATE' || model.fwStatus == 'PENDING_DELETE') {
							return 'yellow';
						}else{
							return 'yellow';
						}
					};

					// 权限控制
					powerService.powerRoutesList().then(
							function(powerList) {
								$scope.buttonPower = {
									isCreate : powerService
											.isPower('firewall_add'),// 创建防火墙
									isEdit : powerService
											.isPower('firewall_edit'), // 编辑防火墙
									isTag : powerService
											.isPower('firewall_tag'),// 标签
									delFireWall : powerService
											.isPower('firewall_drop'),// 删防火墙
								};
							});

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
						var user = sessionStorage["userInfo"];
						if (user) {
							user = JSON.parse(user);
							if (user && user.userId) {
								return true;
							}
						}
						return false;
					};

					$scope
							.$watch(
									"myTable.result",
									function(newVal, oldVal) {
										if (newVal !== oldVal) {
											if ($scope.myTable.result != null
													&& $scope.myTable.result.length > 0) {
												for (var i = 0; i < $scope.myTable.result.length; i++) {
													var status = $scope.myTable.result[i].fwStatus
															.toString()
															.toLowerCase();
													if ("active" != status
															&& "error" != status) {
														$timeout(
																$scope.refreshList,
																5000);
														break;
													}

												}
											}
										}
									});

					$scope.refreshList = function() {
						if (!$scope.checkUser()) {
							return;
						}
						$scope.myTable.api.refresh();
					};

					// 名称查询
					$scope.queryFireWall = function() {
						$scope.myTable.api.draw();
					};

					/* *//**
					 * Enter查询事件
					 */
					/*
					 * $(function () { document.onkeydown = function (event) {
					 * var e = event || window.event ||
					 * arguments.callee.caller.arguments[0];
					 * if(!$scope.checkUser()){ return ; } if (e && e.keyCode ==
					 * 13) { $scope.queryFireWall(); } }; });
					 */

					// 创建防火墙
					$scope.addFireWall = function() {
						var result = eayunModal.open({
									showBtn:false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
									backdrop:'static',
									templateUrl : 'views/safety/firewall/addfirewall.html',
									controller : 'AddFireWall',
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
						result.then(function(value) {
							eayunHttp.post('safety/firewall/createFireWall.do',value).then(function(response) {
									if (null != response.data && 'null' != response.data && response.data.code != "010120") {
										toast.success('添加防火墙'+ (value.name.length > 9 ? value.name.substring(0,8)+ '...' : value.name) + '成功',1000);
									}
									$scope.myTable.api.draw();
								});
							}, function() {
								// console.info('取消');
						});
					};

					// 删除防火墙
					$scope.delFireWall = function(cloudFireWall) {
						eayunModal.confirm('删除后网络下的流量将不受限制，确定要删除防火墙?')
							.then(function() {
								eayunHttp.post("safety/firewall/deleteFwAndFwpAndFwr.do",cloudFireWall)
									.then(function(response) {
										if (null != response.data&& response.data == true) {
											toast.success('删除防火墙成功',1000);
										}
										$scope.myTable.api.draw();
									});
								});
					};

					// 编辑防火墙
					$scope.updateFireWall = function(fireWall) {
						var result = eayunModal
								.dialog({
									// showBtn:
									// false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
									title : '编辑防火墙',
									width : '600px',
									templateUrl : 'views/safety/firewall/editfirewall.html',
									controller : 'UpdateFireWall',
									resolve : {
										firewall : function() {
											return fireWall;
										},
										fwPolicys : function() {
											return eayunHttp
													.post(
															'safety/fwPolicy/getFwpListByPrjId.do',
															{
																dcId : JSON
																		.parse(sessionStorage["dcPrj"]).dcId,
																prjId : JSON
																		.parse(sessionStorage["dcPrj"]).projectId
															})
													.then(function(response) {
														return response.data;
													});
										}
									}
								});
						result
								.then(
										function(value) {
											eayunHttp
													.post(
															'safety/firewall/updateFireWall.do',
															value)
													.then(
															function(response) {
																if (null != response.data
																		&& response.data == true) {
																	toast
																			.success(
																					'修改防火墙'
																							+ (value.fwName.length > 9 ? value.fwName
																									.substring(
																											0,
																											8)
																									+ '...'
																									: value.fwName)
																							+ '成功',
																					1000);
																}
																$scope.myTable.api
																		.draw();
															});
										}, function() {
											// console.info('取消');
										});
					};

					/* 标签 */
					$scope.tagResource = function(resType, resId) {
						var result = eayunModal.open({
							showBtn : false,
							backdrop:'static',
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
		.controller(
				'AddFireWall',
				function($scope, prjId, dcId, eayunHttp, eayunModal,$modalInstance) {
					$scope.model = {};
					$scope.model.fwpId = null;
					$scope.close = function(){
						$modalInstance.dismiss();
					};
					eayunHttp.post('cloud/vm/getProListByCustomer.do').then(
							function(response) {
								$scope.projects = response.data;
								angular.forEach($scope.projects, function(
										value, key) {
									if (value.projectId == prjId.prjId) {
										$scope.model.project = value;
									}
								});
							});

					eayunHttp
							.post('safety/fwPolicy/getFwpListByPrjId.do', {
								dcId : dcId.dcId,
								prjId : prjId.prjId
							})
							.then(
									function(response) {
										var fwPolicys = response.data;
										$scope.fwPolicys = new Array();
										if (fwPolicys != null
												&& fwPolicys.length > 0) {
											for (var i = 0; i < fwPolicys.length; i++) {
												if ('null' == fwPolicys[i].fwId
														|| '' == fwPolicys[i].fwId
														|| null == fwPolicys[i].fwId) {
													$scope.fwPolicys
															.push(fwPolicys[i]);
												}
											}
										}
									});

					$scope.changeFwPolicy = function() {
						$scope.model.fwpId = null;
						eayunHttp
								.post('safety/fwPolicy/getFwpListByPrjId.do', {
									dcId : $scope.model.project.dcId,
									prjId : $scope.model.project.projectId
								})
								.then(
										function(response) {
											var fwPolicys = response.data;
											$scope.fwPolicys = new Array();
											if (fwPolicys != null
													&& fwPolicys.length > 0) {
												for (var i = 0; i < fwPolicys.length; i++) {
													if ('null' == fwPolicys[i].fwId
															|| null == fwPolicys[i].fwId
															|| '' == fwPolicys[i].fwId) {
														$scope.fwPolicys
																.push(fwPolicys[i]);
													}
												}
											}

										});

						$scope.myForm.name.$validate();
					};
					
					$scope.selectICMP = function () {
			        	if("icmp"==$scope.model.protocol || "any"==$scope.model.protocol){
			        		$scope.model.source_port="";
			        		$scope.model.destination_port="";
			        	}
			        };
			        
					// 校验名称格式和唯一性
					$scope.checkFireWallName = function(value) {

						var nameTest = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
						$scope.flag = false;
						if (value.match(nameTest)) {
							$scope.model.name = value;
							return eayunHttp.post(
									'safety/firewall/getFireWallByName.do',
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
					// 校验名称格式和唯一性
					$scope.checkFwRuleName = function(value) {
						var nameTest = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
						$scope.ruleflag = false;
						if (value.match(nameTest)) {
							$scope.ruleModel = angular.copy($scope.model, {});
							$scope.ruleModel.name = value;
							return eayunHttp.post(
									'safety/firewallrule/getFwRuleByName.do',
									$scope.ruleModel).then(function(response) {
								if (true == response.data) {
									return false;
								} else {
									return true;
								}

							});

						} else {
							$scope.ruleflag = true;
						}
					};
					/** *************************************源IP************************************************* */
					// 源IP或子网验证方法
					$scope.checkSourceIp = function(value) {
						// 源IP
						$scope.isflag = false;
						if ('' == value || null == value) {
							return true;
						}
						var str = new RegExp(
								"^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
						var str2 = new RegExp("^\\d+$");
						var dipAdd = value;
						if (dipAdd == null || dipAdd == ""
								|| dipAdd == undefined) {
							$scope.isflag = false;
						} else {
							if (dipAdd.indexOf("/") != -1) {
								var strs = dipAdd.split("/");
								if (strs.length > 2) {
									$scope.isflag = true;
									return false;
								} else if (str.test(strs[0])
										&& str2.test(strs[1]) && strs[1] >= 1
										&& strs[1] <= 32) {
									$scope.isflag = false;
									return true;

								} else if (str.test(strs[0])
										&& str2.test(strs[1]) && strs[1] == 0
										&& strs[0] == "0.0.0.0") {
									// 用于判断0.0.0.0/0过滤
									$scope.isflag = false;
									return true;
								} else {
									$scope.isflag = true;
									return false;
								}
							} else {
								if (str.test(dipAdd)) {
									$scope.isflag = false;
									return true;
								} else {
									$scope.isflag = true;
									return false;
								}
							}
						}

					};

					/** *************************************目的IP************************************************* */
					$scope.checkDestinationIp = function(value) {
						$scope.isflag2 = false;
						if ('' == value || null == value) {
							return true;
						}
						var str = new RegExp(
								"^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
						var str2 = new RegExp("^\\d+$");
						var ipAdd = value;
						if (ipAdd == null || ipAdd == "" || ipAdd == undefined) {
							$scope.isflag2 = false;
						} else {
							if (ipAdd.indexOf("/") != -1) {
								var strs = ipAdd.split("/");
								if (strs.length > 2) {
									$scope.isflag2 = true;
									return false;
								} else if (str.test(strs[0])
										&& str2.test(strs[1]) && strs[1] >= 1
										&& strs[1] <= 32) {
									$scope.isflag2 = false;
									return true;

								} else if (str.test(strs[0])
										&& str2.test(strs[1]) && strs[1] == 0
										&& strs[0] == "0.0.0.0") {
									$scope.isflag2 = false;
									return true;
								} else {
									$scope.isflag2 = true;
									return false;
								}
							} else {
								if (str.test(ipAdd)) {
									$scope.isflag2 = false;
									return true;
								} else {
									$scope.isflag2 = true;
									return false;
								}
							}
						}

					};

					/** **************************************************源端口********************************************************* */
					$scope.checkSourcePort = function(value) {
						// 源端口
						$scope.isflag3 = false;
						if ('' == value || null == value) {
							return true;
						}
						var str = new RegExp(
								"^(^[1-9]\\d{0,3}$)|(^[1-5]\\d{4}$)|(^6[0-4]\\d{3}$)|(^65[0-4]\\d{2}$)|(^655[0-2]\\d$)|(^6553[0-5]$)$");
						var sourceport = value;
						if (sourceport == null || sourceport == ""
								|| sourceport == undefined) {
							$scope.isflag3 = false;
						} else {
							if (sourceport.indexOf(":") != -1) {
								var strs = sourceport.split(":");
								if (strs.length > 2) {
									$scope.isflag3 = true;
									return false;
								} else if (str.test(strs[0]) && str.test(strs[1])) {
									if(strs[0]*1<strs[1]*1){// 判断端口由小到大
										$scope.isflag3 = false;
										return true;
									}else{
										$scope.isflag3 = true;
										return false;
									}	
								} else {
									console.info("ERROR");
									$scope.isflag3 = true;
									return false;
								}

							} else {
								// 没有":"的验证
								if (str.test(sourceport)) {
									$scope.isflag3 = false;
									return true;
								} else {
									$scope.isflag3 = true;
									return false;
								}
							}
						}
					};

					/** ***********************************************目的端口************************************************************* */
					$scope.checkDestinationPort = function(value) {
						// 目的端口
						$scope.isflag4 = false;
						if ('' == value || null == value) {
							return true;
						}
						var str = new RegExp(
								"^(^[1-9]\\d{0,3}$)|(^[1-5]\\d{4}$)|(^6[0-4]\\d{3}$)|(^65[0-4]\\d{2}$)|(^655[0-2]\\d$)|(^6553[0-5]$)$");
						var destinationport = value;
						if (destinationport == null || destinationport == ""
								|| destinationport == undefined) {
							$scope.isflag4 = false;
						} else {
							if (destinationport.indexOf(":") != -1) {
								var strs = destinationport.split(":");
								if (strs.length > 2) {
									$scope.isflag4 = true;
									return false;
								} else if (str.test(strs[0]) && str.test(strs[1])) {
									if(strs[0]*1<strs[1]*1){// 判断端口由小到大
										$scope.isflag4 = false;
										return true;
									}else{
										$scope.isflag4 = true;
										return false;
									}	
								} else {
									$scope.isflag4 = true;
									return false;
								}

							} else {
								// 没有":"的验证
								if (str.test(destinationport)) {
									$scope.isflag4 = false;
									return true;
								} else {
									$scope.isflag4 = true;
									return false;
								}
							}
						}
					};
					/***************************网络地址规则*********************************/
					$scope.model.source_ip_address1=0;
					$scope.model.source_ip_address2=0;
					$scope.model.source_ip_address3=0;
					$scope.model.source_ip_address4=0;
					$scope.model.source_ip_address5=0;
					$scope.model.destination_ip_address1=0;
					$scope.model.destination_ip_address2=0;
					$scope.model.destination_ip_address3=0;
					$scope.model.destination_ip_address4=0;
					$scope.model.destination_ip_address5=0;
					var regx=/^(0|[1-9]\d*)$/;
					$scope.sourrange = "0.0.0.0/0代表所有IP地址";
					$scope.destrange = "0.0.0.0/0代表所有IP地址";
					$scope.cidraError = true;
					$scope.cidrbError = true;
					function checkCidr0_255(val,fromFunc,type) {
						if(fromFunc=="focus"){
							if(type=="a"){
								$scope.sourrange="请输入0-255之间的整数！";
							}else{
								$scope.destrange="请输入0-255之间的整数！";
							}
						}else{
							$scope.sourrange = "0.0.0.0/0代表所有IP地址";
							$scope.destrange = "0.0.0.0/0代表所有IP地址";
						}
						if (val >= 0 && val <= 255 && regx.test(val)) {
							return false;
						} else {
							return true;
						}
					};
					function checkCidr0_32(val,fromFunc,type) {
						if(fromFunc=="focus"){
							if(type=="a"){
								$scope.sourrange="请输入0-32之间的整数！";
							}else{
								$scope.destrange="请输入0-32之间的整数！";
							}
						}else{
							$scope.sourrange = "0.0.0.0/0代表所有IP地址";
							$scope.destrange = "0.0.0.0/0代表所有IP地址";
						}
						if (val >= 0 && val <= 32 && regx.test(val)) {
							return false;
						} else {
							return true;
						}
					};
					$scope.checkTypeCidr = function(position,fromFunc) {
						if(position=='' ||position==null){
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
							return ;
						}
						if(position=='a1'){
							$scope.a1Error=checkCidr0_255($scope.model.source_ip_address1,fromFunc,'a');
						}else if(position=='a2'){
							$scope.a2Error=checkCidr0_255($scope.model.source_ip_address2,fromFunc,'a');
						}else if(position=='a3'){
							$scope.a3Error=checkCidr0_255($scope.model.source_ip_address3,fromFunc,'a');
						}else if(position=='a4'){
							$scope.a4Error=checkCidr0_255($scope.model.source_ip_address4,fromFunc,'a');
						}else if(position=='a5'){
							$scope.a5Error=checkCidr0_32($scope.model.source_ip_address5,fromFunc,'a');
						}else if(position=='b1'){
							$scope.b1Error=checkCidr0_255($scope.model.destination_ip_address1,fromFunc,'b');
						}else if(position=='b2'){
							$scope.b2Error=checkCidr0_255($scope.model.destination_ip_address2,fromFunc,'b');
						}else if(position=='b3'){
							$scope.b3Error=checkCidr0_255($scope.model.destination_ip_address3,fromFunc,'b');
						}else if(position=='b4'){
							$scope.b4Error=checkCidr0_255($scope.model.destination_ip_address4,fromFunc,'b');
						}else if(position=='b5'){
							$scope.b5Error=checkCidr0_32($scope.model.destination_ip_address5,fromFunc,'b');
						}
						if(!$scope.a1Error && !$scope.a2Error && !$scope.a3Error && !$scope.a4Error && !$scope.a5Error){
							$scope.model.source_ip_address = parseInt($scope.model.source_ip_address1) + "." + parseInt($scope.model.source_ip_address2) + "." + parseInt($scope.model.source_ip_address3) + "." + parseInt($scope.model.source_ip_address4) + "/" + parseInt($scope.model.source_ip_address5);
							$scope.cidraError = true;
						}else{
							$scope.cidraError = false;
						} 
						if(!$scope.b1Error && !$scope.b2Error && !$scope.b3Error && !$scope.b4Error && !$scope.b5Error){
							$scope.model.destination_ip_address = parseInt($scope.model.destination_ip_address1) + "." + parseInt($scope.model.destination_ip_address2) + "." + parseInt($scope.model.destination_ip_address3) + "." + parseInt($scope.model.destination_ip_address4) + "/" + parseInt($scope.model.destination_ip_address5);
							$scope.cidrbError = true;
						}else{
							$scope.cidrbError = false;
						}
					};
					/**********************************************************************/
					$scope.commit = function() {
						//$scope.ok($scope.model);
						$modalInstance.close($scope.model);
					};

				})
		.controller(
				'UpdateFireWall',
				function($scope, firewall, fwPolicys, eayunHttp, eayunModal) {
					$scope.model = angular.copy(firewall, {});

					// 设置全部策略信息
					$scope.fwPolicys = new Array();
					if (fwPolicys != null && fwPolicys.length > 0) {
						for (var i = 0; i < fwPolicys.length; i++) {
							if ('null' == fwPolicys[i].fwId
									|| null == fwPolicys[i].fwId
									|| '' == fwPolicys[i].fwId
									|| fwPolicys[i].fwId == $scope.model.fwId) {
								$scope.fwPolicys.push(fwPolicys[i]);
							}
						}
					}

					// 校验名称格式和唯一性
					$scope.checkFireWallName = function(value) {
						var nameTest = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
						$scope.flag = false;
						if (value.match(nameTest)) {
							$scope.model.fwName = value;
							return eayunHttp.post(
									'safety/firewall/getFireWallByName.do',
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

					$scope.commit = function() {
						$scope.ok($scope.model);
					};
					
				});