'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state("app.record.accessList", {
				url : '/accessList',
				templateUrl : 'views/record/apply/accessList.html',
				controller : 'accessListCtrl',
				controllerAs : 'accessList'
			}).state("app.record.access", {
				url : '/access',
				templateUrl : 'views/record/apply/addaccess.html',
				controller : 'accessCtrl',
				controllerAs : 'access'
			});
		}).controller('accessListCtrl', function ($scope,$state,eayunHttp,eayunStorage,$rootScope,toast,eayunModal) {
			var vm = this;
			var list=[{route:'app.record.main.apply',name:'开始备案'}];
			$rootScope.navList(list,'新增接入列表');
			// 添加主体信息
			vm.addFireRecord=function(){
				 $state.go('app.record.addFireRecordUnit',{toType:'access'});
			};
			//新增接入信息
			vm.addRecordWeb=function(){
				 $state.go('app.record.access');
			};
			vm.istijiao = true;
			vm.unit = eayunStorage.get("unit") == undefined ? null : eayunStorage.get("unit");
			var web = eayunStorage.get("web") == undefined ? null : eayunStorage.get("web");
			vm.webList = eayunStorage.get("webList") == undefined ? [] : eayunStorage.get("webList");
			
			if(web!=null && web!=undefined && web!='undefined'){
				vm.webList.push(web);
				eayunStorage.set("webList",vm.webList);
				eayunStorage.set("web",null);
			}
			//修改网站
			vm.updateRecordWeb = function (web){
				eayunStorage.set("web",web);//设置网站信息
				for(var i=0;i<vm.webList.length;i++){
                    var web1 = vm.webList[i];
                    if(web1==web){
                    	vm.webList.splice(i, 1);
                    }
                }
				$state.go('app.record.access');
			};
			vm.deleteRecord = function(unit){
				eayunModal.confirm('确定删除备案主体信息？').then(function() {
					if(vm.unit == unit){
						vm.unit = null;
						eayunStorage.set("unit",null);
					}
				});
			};
			vm.deleteWeb = function(web){
				eayunModal.confirm('确认删除网站信息？').then(function() {
					for(var j=0;j<vm.webList.length;j++){
						var w = vm.webList[j];
						if(w==web){
							vm.webList.splice(j, 1);
						}
					}
				});
			};
			vm.isNull = function(obj){
				if(obj===null || obj==="null" ){
					return true;
				}
				return false;
			}
			//添加新增接入
			vm.commit = function(){
				vm.istijiao = false;
				eayunModal.confirm('提交后不可修改，请确认提交').then(function() {
					vm.unit.webList = vm.webList;
					eayunHttp.post("ecsc/record/addAccessRecord.do",vm.unit).then(function(data){
						if(data.data!=null){
							toast.success("新增接入信息提交成功");
							$state.go('app.record.main.apply');
						}else{
							eayunModal.warning("新增接入信息提交失败");
						}
						vm.istijiao = true;
					});
				},function(){vm.istijiao = true;});
			};
			vm.cancel = function(){
				eayunModal.confirm('离开页面后，信息将不会被保存').then(function() {
					eayunStorage.set("unit",null);
					eayunStorage.set("web",null);
					eayunStorage.set("webList",null);
					$state.go('app.record.main.apply');
				});
			}
		}).controller('accessCtrl', function ($scope,$state,eayunHttp,eayunStorage,$rootScope,eayunModal) {
			var vm = this;
			var list=[{route:'app.record.main.apply',name:'开始备案'},{route:'app.record.accessList',name:'新增接入列表'}];
			$rootScope.navList(list,'新增接入');
			vm.cloudprojectList = {};
			eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
				vm.cloudprojectList = response.data.data;
		    });
			//定义多个IP服务
	        vm.ipList = [{}];
	        vm.copyIPservice = function () {
	            vm.ipList.push({});
	        };
	        vm.deleteIPservice = function (index) {
	        	var serviceIp = vm.ipList[index];
	        	for(var i=0;i<vm.ipList.length;i++){
	        		if(i!=index && vm.ipList[i].dcId == serviceIp.dcId && vm.ipList[i].webService == serviceIp.webService){
	        			if(undefined!=vm.ips[i]){
	        				vm.ips[i].push(serviceIp.ip);
	        			}
	        		}
	        	}
	        	if (index == 0) {
	                vm.ipList.shift();
	                vm.ips.shift();
	            } else if (index == vm.ipList.length) {
	                vm.ipList.pop();
	                vm.ips.pop();
	            } else {
	                vm.ipList.splice(index, 1);
	                vm.ips.splice(index, 1);
	            }
	        };
			vm.model = angular.copy(eayunStorage.get("web") == null ? {} : eayunStorage.get("web"),{});
			vm.webList = eayunStorage.get("webList") == undefined ? [] : eayunStorage.get("webList");
			if(vm.model!=null && vm.model!={ }){
				if(vm.model.ipList!=null && vm.model.ipList.length>0){
					vm.ipList = vm.model.ipList;
				}
			}
			vm.ips = new Array();

			vm.initIps = function(ipsnum){
				if(ipsnum<vm.model.ipList.length){
					var ipservice = vm.model.ipList[ipsnum];
					var type = "vm";
					if(ipservice.webService==2){
						type = "lb";
					}
					eayunHttp.post('ecsc/record/getIP.do',{"resource_type":type,"dc_Id":ipservice.dcId}).then(function(response){
						vm.ips[ipsnum] = response.data;
						if(vm.webList!=null && vm.webList!=undefined && vm.webList.length>0){//排除网站集合里面的IP
							for(var i=0;i<vm.webList.length;i++){
								for(var k=0;k<vm.webList[i].ipList.length;k++){
									var webip = vm.webList[i].ipList[k].ip;
									for(var j=0;j<vm.ips[ipsnum].length;j++){
										if(webip==vm.ips[ipsnum][j]){
											vm.ips[ipsnum].splice(j, 1);
					                    }
									}
								}
							}
						}
						if(vm.ipList!=null && vm.ipList!=undefined && vm.ipList.length>0){//排除当前网站ip集合里面的IP
							for(var i=0;i<vm.ipList.length;i++){
								var webip = vm.ipList[i].ip;
								for(var j=0;j<vm.ips[ipsnum].length;j++){
									if(webip==vm.ips[ipsnum][j] && i!=ipsnum){
										vm.ips[ipsnum].splice(j, 1);
				                    }
								}
							}
						}
						ipsnum ++ ;
					}).then(function(){
						vm.initIps(ipsnum);
					});
				}
			};
			var ipsnum = 0;
			if(vm.model!=null && vm.model!={} && vm.model.ipList!=null){
				vm.initIps(ipsnum);
			}
			
			vm.selectIP = function(dcId,service,index){
				var type = "vm";
				if(service==2){
					type = "lb";
				}
				if(dcId==null || dcId==""){
					eayunModal.warning("请选择数据中心");
				}else{
					vm.ipList[index].ip=null;
					eayunHttp.post('ecsc/record/getIP.do',{"resource_type":type,"dc_Id":dcId}).then(function(response){
						vm.ips[index]=response.data;
						if(vm.webList!=null && vm.webList!=undefined && vm.webList.length>0){//排除网站集合里面的IP
							for(var i=0;i<vm.webList.length;i++){
								for(var k=0;k<vm.webList[i].ipList.length;k++){
									var webip = vm.webList[i].ipList[k].ip;
									for(var j=0;j<vm.ips[index].length;j++){
										if(webip==vm.ips[index][j]){
											vm.ips[index].splice(j, 1);
					                    }
									}
								}
							}
						}
						if(vm.ipList!=null && vm.ipList!=undefined && vm.ipList.length>0){//排除当前网站ip集合里面的IP
							for(var i=0;i<vm.ipList.length;i++){
								var webip = vm.ipList[i].ip;
								for(var j=0;j<vm.ips[index].length;j++){
									if(webip==vm.ips[index][j]){
										vm.ips[index].splice(j, 1);
				                    }
								}
							}
						}
				    });
				}
			};
			var cahngeip = null;
			vm.reomveIP = function(ip,index){
				var dcserviceip = vm.ipList[index];
				if(vm.ips!=null){//排除ip集合里面选中的IP
					for(var i=0;i<vm.ips.length;i++){
						if(vm.ips[i]!=null && vm.ips[i]!=undefined && i!=index){
							var serviceip = vm.ipList[i];
							if(serviceip!=null && serviceip!=undefined && dcserviceip.dcId == serviceip.dcId && dcserviceip.webService == serviceip.webService){
								for(var j=0;j<vm.ips[i].length;j++){
									if(ip == vm.ips[i][j] && ip!=cahngeip){
										vm.ips[i].splice(j, 1);
										if(cahngeip!=null){
											vm.ips[i].push(cahngeip);
										}
									}
								}
							}
						}
					}
				}
			};
			vm.getIP = function(ip){
				cahngeip = ip;
			}
			
			vm.changeServiceIP = function(index){
				vm.ips[index] = new Array();
				vm.ipList[index].webService=null;
				vm.ipList[index].ip=null;
			};
			
			vm.doFocus = function($event){
				$($event.target).parent().find('.ey-content-notice').show();
			};

			vm.doBlur = function($event,isShow){
				if(!isShow){
					$($event.target).parent().find('.ey-content-notice').hide();
				}
			};
			vm.commit=function(){
				vm.model.ipList = vm.ipList;
				eayunStorage.set("web",vm.model);//设置新增接入
				$state.go('app.record.accessList');
			}
			vm.cancel = function(){
				if(eayunStorage.get("web") == null){
					eayunModal.confirm('离开页面后，信息将不会被保存').then(function() {
						eayunStorage.set("web",null);
						$state.go('app.record.accessList');
					});
				}else{
					$state.go('app.record.accessList');
				}
			}
			
		});

