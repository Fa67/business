'use strict';
angular
		.module('eayunApp.controllers')
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state('app.safety.rule', {
				url : '/fireWallRuleList/:fwId/:fwpId',
				templateUrl : 'views/safety/firewallrule/firewallrulemng.html',
				controller : 'FireWallRuleList'
			});
		})
		.controller('FireWallRuleList',
				function($scope, $state, eayunHttp, eayunModal, toast,powerService, $stateParams,$rootScope) {
					$scope.outModel = {};
					var list=[{route:'app.safety.safetybar.firewall',name:'防火墙'}];
					$rootScope.navList(list,'防火墙详情','detail');
					// pop框方法
					$scope.hintTagShow = [];
					$scope.openTableBox = function(obj) {
						if (obj.type == 'tagName') {
							$scope.hintTagShow[obj.index] = true;
						}
						if (obj.type == 'fweditDesc') {
							$scope.hintTagShow[obj.index] = true;
						}
						$scope.ellipsis = obj.value;
					};
					$scope.closeTableBox = function(obj) {
						if (obj.type == 'tagName') {
							$scope.hintTagShow[obj.index] = false;
						}
						if (obj.type == 'fweditDesc') {
							$scope.hintTagShow[obj.index] = false;
						}
					};
					// 查询防火墙信息
					eayunHttp.post("safety/firewall/queryFwById.do",
							$stateParams.fwId).then(function(response) {
						$scope.outModel = response.data;
					});
					$scope.parseJson = function(tagsStr) {
						var json = {};
						if (tagsStr) {
							json = JSON.parse(tagsStr);
						}
						return json;
					};
					// 查询列表
					$scope.myTable = {
						source : 'safety/firewallrule/getFireWallRuleList.do',
						api : {},
						getParams : function() {
							return {
								prjId : sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).projectId : '',
								dcId : sessionStorage["dcPrj"] ? JSON.parse(sessionStorage["dcPrj"]).dcId : '',
								fwpId : $stateParams.fwpId,
								name : $scope.fwrName || ''
							};
						}
					};

					/*将名称、描述变为可编辑状态*/
					$scope.editNameOrDesc = function (type) {
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
			            $scope.vmEditModel = angular.copy($scope.outModel, {});
			            $scope.vmEditModel.description = $scope.vmEditModel.description==null ? '' : $scope.vmEditModel.description;
			        };
			        /*校验编辑名称是否存在*/
			        $scope.checkFwNameExist = function () {
			        	var value = $scope.vmEditModel.fwName;
			        	var nameTest = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
						$scope.flag = false;
						if(typeof(value) == "undefined"){
							value="";
						}
						if (value.match(nameTest)) {
							$scope.model.fwName = value;
							$scope.model.dcId = $scope.vmEditModel.dcId;
							$scope.model.prjId = $scope.vmEditModel.prjId;
							$scope.model.fwId = $scope.vmEditModel.fwId;
							return eayunHttp.post(
									'safety/firewall/getFireWallByName.do',
									$scope.model).then(function(response) {
								if (true == response.data) {
									$scope.checkVmNameFlag = false;
								} else {
									$scope.checkVmNameFlag = true;
								}

							});

						} else {
							$scope.checkVmNameFlag = true;
						}
			        };
			        /*保存编辑的名称、描述,并刷新界面*/
			        $scope.saveEdit = function (type) {
			        	eayunHttp.post('safety/firewall/editFwNameorDesc.do',$scope.vmEditModel)
							.then(function(response) {
								if (null != response.data&& 'null' != response.data && response.data.code != "010120") {
									toast.success('防火墙修改成功',1000);
								}
								$scope.myTable.api.draw();
								if (type == 'fwName') {
				                	$scope.vmNameEditable = false;
				                	$scope.hintNameShow = false;
				                }
				                if (type == 'fwDesc') {
				                	$scope.vmDescEditable = false;
				                	$scope.hintDescShow = false;
				                }
				                $scope.outModel = angular.copy($scope.vmEditModel, {});
							});

			        };
			        /*取消名称、描述的可编辑状态*/
			        $scope.cancelEdit = function (type) {
			            if (type == 'fwName') {
			            	$scope.vmNameEditable = false;
			            	$scope.hintNameShow = false;
			            }
			            if (type == 'fwDesc') {
			            	$scope.vmDescEditable = false;
			            	$scope.hintDescShow = false;
			            }
			        };

					// 权限控制
					powerService.powerRoutesList().then(
							function(powerList) {
								$scope.buttonPower = {
									isCreate : powerService.isPower('firewallrule_add'),// 创建防火墙
									isEdit : powerService.isPower('firewall_edit'), // 编辑防火墙
									delFireWall : powerService.isPower('firewallrule_drop'),// 删防火墙
									fwRuleIsEnabled : powerService.isPower('firewallrule_enabled'),//禁用启用规则
									fwPolicySeq : powerService.isPower('firewall_policy'),//调整优先级
								};
							});

					// 监视器[监视数据中心、项目id变化]
					$scope.$watch('model.projectvoe', function(newVal, oldVal) {
						if (newVal !== oldVal) {
							$scope.myTable.api.draw();
						}
					});

					// 名称查询
					$scope.queryFwRule = function() {
						$scope.myTable.api.draw();
					};

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

					/**
					 * Enter查询事件
					 */
					$(function() {
						document.onkeydown = function(event) {
							var e = event || window.event
									|| arguments.callee.caller.arguments[0];
							if (!$scope.checkUser()) {
								return;
							}
							if (e && e.keyCode == 13) {
								$scope.queryFwRule();
							}
						};
					});

					// 删除防火墙规则
					$scope.delFwRule = function(cloudFwRule) {eayunModal
						.confirm('确定要删除规则' + cloudFwRule.fwrName+ '?')
						.then(function() {
							eayunHttp.post("safety/firewallrule/deleteFwRulePolicy.do",cloudFwRule)
							.then(function(response) {
								if (null != response.data&& response.data == true) {
									toast.success('删除规则成功',1000);
								}
								$scope.myTable.api.draw();
							});
						});
					};

					// 创建防火墙规则
					$scope.addFwRule = function() {
						var result = eayunModal
								.open({
									showBtn : false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
									backdrop:'static',
									templateUrl : 'views/safety/firewallrule/addfwrule.html',
									controller : 'AddFWRule',
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
						result.then(function(value) {
							value.fwpId = $stateParams.fwpId;
							eayunHttp.post('safety/firewallrule/addFwRule.do',value)
								.then(function(response) {
									if (null != response.data&& 'null' != response.data && response.data.code != "010120") {
										var fwname =value.name.length > 9 ? value.name.substring(0,9)+ '...' : value.name;
										toast.success('添加规则'+fwname+'成功',1000);
									}
									$scope.myTable.api.draw();
								});
							}, function() {
								// console.info('取消');
						});
					};

					// 编辑防火墙规则
					$scope.updateFwRule = function(cloudFwRule) {
						var result = eayunModal
								.dialog({
									// showBtn:
									// false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
									title : '编辑规则',
									width : '650px',
									templateUrl : 'views/safety/firewallrule/editfwrule.html',
									controller : 'UpdateFwRule',
									resolve : {
										fwRule : function() {
											return cloudFwRule;
										}
									}
								});
						result.then(function(value) {eayunHttp
							.post('safety/firewallrule/updateFwRule.do',value)
								.then(function(response) {
									if (null != response.data && 'null' != response.data && response.data.code != "010120") {
										toast.success('修改规则'+ (value.fwrName.length > 7 ? value.fwrName.substring(0,6)+ '...' : value.fwrName) + '成功',1000);
									}
									$scope.myTable.api.draw();
								});
							}, function() {
							// console.info('取消');
						});
					};
					// 防火墙规则详情页
					$scope.ruleDetail = function(cloudFwRule) {
						var result = eayunModal
								.dialog({
									showBtn: false,//使from里公用的确定或取消按钮隐藏或显示,默认是true(显示)
									title : '规则详情页',
									width : '550px',
									templateUrl : 'views/safety/firewallrule/detail.html',
									controller : 'ruleDetailCtr',
									resolve : {
										fwRule : function() {
											return cloudFwRule;
										}
									}
								});
					};
					//禁用启用规则
					$scope.isEnabled = function (_fwRule,enabled){
			        	var enabledstr = '禁用';
			        	if(enabled==1){enabledstr = '启用';}
			        	eayunModal.confirm('确定要'+enabledstr+'规则' + _fwRule.fwrName+ '?').then(function() {
			        		_fwRule.fwrEnabled=enabled;
				        	eayunHttp.post('safety/firewallrule/updateEnabled.do',_fwRule).then(function(response) {
								if (null != response.data&& 'null' != response.data && response.data.code != "010120") {
									toast.success('规则调整成功',1000);
								}
								$scope.myTable.api.draw();
							});
			        	});
			        };
			        //调整优先级
			        $scope.rulesequence = function(_fwRule) {
						var result = eayunModal
								.open({
									showBtn: false,
									backdrop:'static',
									templateUrl : 'views/safety/firewallrule/rulesequence.html',
									controller : 'RuleSequence',
									resolve : {
										fwRule : function() {
											return _fwRule;
										},
										fwpId : function() {
											return $stateParams.fwpId;
										}
									}
								}).result;
						result.then(function(value) {
							eayunHttp.post('safety/fwPolicy/updateRuleSequence.do',value).then(function(response) {
								if (null != response.data && 'null' != response.data && response.data.code != "010120") {
									toast.success('规则优先级调整成功',1000);
								}
								$scope.myTable.api.draw();
							});
						}, function() {
							// console.info('取消');
						});
					};

				})
		.controller(
				'AddFWRule',
				function($scope, prjId, eayunHttp, eayunModal,$modalInstance) {
					$scope.close = function(){
						$modalInstance.dismiss();
					};
					$scope.model = {};
					$scope.model.protocol = null;

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

					// 校验名称格式和唯一性
					$scope.checkFwRuleName = function(value) {
						var nameTest = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
						$scope.flag = false;
						if (value.match(nameTest)) {
							$scope.model.name = value;
							return eayunHttp.post(
									'safety/firewallrule/getFwRuleByName.do',
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

					// 切换项目校验重名
					$scope.changePrj = function() {
						$scope.myForm.name.$validate();
					};

					$scope.selectICMP = function () {
			        	if("icmp"==$scope.model.protocol || "any"==$scope.model.protocol){
			        		$scope.model.source_port="";
			        		$scope.model.destination_port="";
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
					$scope.commit = function() {
						eayunModal.confirm('规则' + $scope.model.name + '已设置正确，立即添加？').then(
							function() {
								$modalInstance.close($scope.model);
							}, function() {
								// console.info('取消');
							});
					};

				})
		.controller(
				'UpdateFwRule',
				function($scope, fwRule, eayunHttp, eayunModal) {
					$scope.model = angular.copy(fwRule, {});
					if (fwRule.fwrEnabled == '1') {
						$scope.model.fwrEnabled = true;
					}
					if (null == fwRule.protocol || '' == fwRule.protocol
							|| 'null' == fwRule.protocol) {
						$scope.model.protocol = 'any';
					}
					if (fwRule.isShared == '1') {
						$scope.model.isShared = true;
					}
					if ('null' == fwRule.sourcePort || '' == fwRule.sourcePort) {
						$scope.model.sourcePort = null;
					}
					if ('null' == fwRule.destinationPort
							|| '' == fwRule.destinationPort) {
						$scope.model.destinationPort = null;
					}
					if ('null' == fwRule.sourceIpaddress
							|| '' == fwRule.sourceIpaddress) {
						$scope.model.sourceIpaddress = null;
					}
					if ('null' == fwRule.destinationIpaddress
							|| '' == fwRule.destinationIpaddress) {
						$scope.model.destinationIpaddress = null;
					}

					$scope.changeProtocol = function(protocol) {
						if ('icmp' == protocol || 'any' == protocol) {
							$scope.model.sourcePort = null;
							$scope.model.destinationPort = null;
						}
					};

					// 校验名称格式和唯一性
					$scope.checkFwRuleName = function(value) {
						var nameTest = /^[\u4e00-\u9fa5a-zA-Z0-9]([\u4e00-\u9fa5_a-zA-Z0-9\s]{0,18}[\u4e00-\u9fa5a-zA-Z0-9]){0,1}$/;
						$scope.flag = false;
						if (value.match(nameTest)) {
							$scope.model.fwrName = value;
							return eayunHttp.post(
									'safety/firewallrule/getFwRuleByName.do',
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
						// 目的IP
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
								} else if (str.test(strs[0])
										&& str.test(strs[1])) {
									$scope.isflag3 = false;
									return true;
								} else {
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
								} else if (str.test(strs[0])
										&& str.test(strs[1])) {
									$scope.isflag4 = false;
									return true;
								} else {
									$scope.isflag4 = true;
									return false;
								}

							} else {
								//没有":"的验证
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

					$scope.commit = function() {
						$scope.ok($scope.model);
					};

}).controller(
		'RuleSequence',
		function($scope, fwRule, eayunHttp, eayunModal,fwpId,$modalInstance,$timeout) {
			$scope.pRuleList = {};
			$scope.fwRule = fwRule;
			$scope.reference={};
			$scope.local='pre';
			$scope.target = {};
			$scope.close = function(){
				$modalInstance.dismiss();
			};
			eayunHttp.post('safety/fwPolicy/getRuleByfwpId.do',fwpId).then(function(response) {
				var i,ruleModel;
				if (null != response.data&& 'null' != response.data && response.data.code != "010120") {
					$scope.pRuleList = response.data;
					for(i=0;i<$scope.pRuleList.length;i++){
						ruleModel = $scope.pRuleList[i];
						if($scope.fwRule.fwrName!=ruleModel.fwrName){
							$scope.reference = ruleModel.fwrId;
							break;
						}
					}
				}
			});
			$scope.RuleSQCommit = function (_fwpId,_reference,_target,_local) {
	            var data = {
	            	fwpId: _fwpId,
	                reference: _reference,
	                target: _target,
	                fwrName: $scope.fwRule.fwrName,
	                local: _local
	            };
	            return data;
	        };
			$scope.commit = function() {
				var data = $scope.RuleSQCommit(fwpId, $scope.reference, $scope.fwRule.fwrId, $scope.local)
				$modalInstance.close(data);
			};
}).controller('ruleDetailCtr',
		function($scope, fwRule, eayunHttp, eayunModal) {
	$scope.fwRule = fwRule;
});