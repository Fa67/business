'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state("app.record.changeweb", {
				url : '/changeweb',
				templateUrl : 'views/record/website/changeWeb.html',
				controller : 'changeWebCtrl',
				controllerAs : 'change'
			});
		}).controller("changeWebCtrl", function ($scope,$stateParams,eayunHttp,eayunStorage,$state,eayunModal,$rootScope,toast) {
			var vm = this;
			var list=[{route:'app.record.main.apply',name:'开始备案'},{route:'app.record.unitList',name:'已备案列表'}];
			$rootScope.navList(list,'变更网站列表');
			var isChangeone = eayunStorage.get("isChangeone") == true ? true : false;
			vm.models = eayunStorage.get("changeList") == undefined ? [] : eayunStorage.get("changeList");
			vm.ischangetijiao = true;
			if(isChangeone){
				eayunStorage.set("isChangeone",null);
				eayunStorage.set("web",null);//设置网站信息
			}
			vm.webList = eayunStorage.get("webList") == undefined ? [] : eayunStorage.get("webList");
			var web = eayunStorage.get("web") == undefined ? null : eayunStorage.get("web");
			
			vm.ips = [];
			if(web!=null && web!=undefined && web!='undefined'){//修改后跳回页面替换网站对象
				for(var j=0;j<vm.webList.length;j++){
					var w = vm.webList[j];
					if(w.webId==web.webId){
						vm.webList[j]=web;
					}
				}
				for(var i=0;i<vm.models.length;i++){
					var oldweb = vm.models[i];
					if(oldweb.webId==web.webId){
						oldweb.isChange=2;
					}
				}
			}
			vm.change = function (web){//选择
				
				vm.webList.push(web);
				
				eayunStorage.set("isChangeone",true);
				
				eayunStorage.set("web",web);//设置网站信息
				eayunStorage.set("webList",vm.webList);//设置网站信息
				$state.go("app.record.addweb",{toType:'changeone'});
			};
			vm.updateChange = function(web){
				
				eayunStorage.set("web",web);//设置网站信息
				eayunStorage.set("webList",vm.webList);//设置网站信息
				$state.go("app.record.addweb",{toType:'change'});
			}
			vm.deleteChange = function(newweb){
				for(var i=0;i<vm.models.length;i++){
					var oldweb = vm.models[i];
					if(oldweb.webId==newweb.webId){
						oldweb.isChange=1;
					}
				}
				for(var j=0;j<vm.webList.length;j++){
					var w = vm.webList[j];
					if(w.webId==newweb.webId){
						vm.webList.splice(j, 1);
					}
				}
			};
			
			vm.commit = function(){
				vm.ischangetijiao = false;
				eayunModal.confirm('提交后不可修改，请确认提交').then(function() {
					var unitId;
					if(vm.webList.length>0){
						var w = vm.webList[0];
						unitId = w.unitId;
						eayunHttp.post('ecsc/website/changeWebsite.do',{"webList":vm.webList,"unitId":unitId}).then(function(response){
							if(response.data!=null){
								eayunStorage.set("web",null);//设置网站信息
								eayunStorage.set("webList",null);//设置网站信息
								eayunStorage.set("changeList",null);
								toast.success("变更备案信息提交成功");
								$state.go("app.record.unitList");
							}else{
								eayunModal.warning("变更备案信息提交失败");
							}
							vm.ischangetijiao = true;
					    });
					}else{
						eayunModal.warning("数据为空，不能提交");
					}
				},function(){vm.ischangetijiao = true;});
			}
			vm.cancel =function(){
				eayunModal.confirm('离开页面后，信息将不会被保存').then(function() {
					eayunStorage.set("web",null);//设置网站信息
					eayunStorage.set("webList",null);//设置网站信息
					eayunStorage.set("changeList",null);
					$state.go('app.record.unitList');
				});
			}
		});

