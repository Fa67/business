'use strict';
angular.module('eayunApp.controllers')
		/**
		 * @ngdoc function
		 * @name appRouter
		 * @description 配置路由
		 */
		.config(function($stateProvider, $urlRouterProvider) {
			$stateProvider.state("app.record.recordList", {
				url : '/recordList',
				templateUrl : 'views/record/apply/newRecordList.html',
				controller : 'recordListCtrl',
				controllerAs : 'recordList'
			}).state("app.record.addFireRecordUnit", {
				url : '/addFireRecordUnit/:toType',
				templateUrl : 'views/record/apply/addfirerecord.html',
				controller : 'addFireRecordUnitCtrl',
				controllerAs : 'add'
			})
			.state("app.record.addweb", {
				url : '/addweb/:toType',
				templateUrl : 'views/record/apply/addweb.html',
				controller : 'addwebCtrl',
				controllerAs : 'webmodel'
			});
		}).controller('recordListCtrl', function ($scope,$state,eayunHttp,eayunStorage,Upload,toast,$rootScope,eayunModal) {
			var vm = this;
			vm.unit = {};
			var list=[{route:'app.record.main.apply',name:'开始备案'}];
			$rootScope.navList(list,'新增备案列表');
			// 添加主体信息
			vm.addFireRecord=function(){
				 $state.go('app.record.addFireRecordUnit',{toType:'add'});
			};
			//添加网站信息
			vm.addRecordWeb=function(){
				 $state.go('app.record.addweb',{toType:'add'});
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
				$state.go('app.record.addweb',{toType:'add'});
			} 
			/**
			 * 程龙   判断网站数量
			 * vm.webList.length>=1
			 * */
			vm.isNull = function(obj){
				
			
				if(obj==null || obj=="null" ){
					return true;
				}
				
				
				
				return false;
			}
			
			
			
			vm.deleteRecord = function(unit){
				eayunModal.confirm('确定删除备案主体信息？').then(function() {
					if(vm.unit == unit){
						vm.unit = null;
						eayunStorage.set("unit",null);
					}
				});
			};
			vm.deleteWeb = function(web){
				eayunModal.confirm('确定删除备案网站信息？').then(function() {
					for(var j=0;j<vm.webList.length;j++){
						var w = vm.webList[j];
						if(w==web){
							vm.webList.splice(j, 1);
						}
					}
				});
			}
			//添加首次备案
			vm.addFirstRecord=function(){
				vm.istijiao = false;
				eayunModal.confirm('提交后不可修改，请确认提交').then(function() {
					vm.unit.webList = vm.webList;
					eayunHttp.post("ecsc/record/addfirstrecord.do",vm.unit).then(function(data){
						vm.istijiao = true;
						if(data.data!=null){
							eayunStorage.set("unit",null);
							eayunStorage.set("web",null);
							eayunStorage.set("webList",null);
							toast.success("首次备案信息提交成功");
							$state.go("app.record.main");
						}else{
							eayunModal.warning("首次备案信息提交失败");
						}
					});
				},function(){vm.istijiao = true;});
			};
			vm.cancel = function(){
				eayunModal.confirm('离开页面后，信息将不会被保存').then(function() {
					eayunStorage.set("unit",null);
					eayunStorage.set("web",null);
					eayunStorage.set("webList",null);
					$state.go("app.record.main");
				});
			}
			
		}).controller('addFireRecordUnitCtrl', function ($scope,$state,eayunHttp,eayunStorage,eayunModal,Upload,toast,$stateParams,$rootScope) {
			var vm = this;
			var toType = $stateParams.toType;
			var list = [];
			$scope.addList = [];
			$scope.pattern = "/^\w+\.((com)|(net)|(org)|(gov\.cn)|(info)|(cc)|(com\.cn)|(net\.cn)|(org\.cn)|(name)|(biz)|(tv)|(cn)|(mobi)|(name)|(sh)|(ac)|(io)|(tw)|(com\.tw)|(hk)|(com\.hk)|(ws)|(travel)|(us)|(tm)|(la)|(me\.uk)|(org\.uk)|(ltd\.uk)|(plc\.uk)|(in)|(eu)|(it)|(jp))$/";
			$scope.selectItems = [];
			$scope.placeholder = "请输入网站域名";
			if(toType=="add"){
				list=[{route:'app.record.main.apply',name:'开始备案'},{route:'app.record.recordList',name:'新增备案列表'}];
			}else{
				list=[{route:'app.record.main.apply',name:'开始备案'},{route:'app.record.accessList',name:'新增接入列表'}];
			}
			$rootScope.navList(list,'备案主体');
			vm.isdutydeleteFile = true;
			vm.isdutyshangchuan = true;
			vm.isdutyagain = true;
			vm.isbusinessdeleteFile = true;
			vm.isbusinessshangchuan = true;
			vm.isbusinessagain = true;
			vm.model = angular.copy(eayunStorage.get("unit") == null ? {} : eayunStorage.get("unit"),{});
			vm.areaP = angular.copy(eayunStorage.get("areaP") == null ? [] : eayunStorage.get("areaP"),[]);
			vm.areaCity = angular.copy(eayunStorage.get("areaCity") == null ? [] : eayunStorage.get("areaCity"),[]);
			vm.areaCounty = angular.copy(eayunStorage.get("areaCounty") == null ? [] : eayunStorage.get("areaCounty"),[]);
			vm.cloudprojectList = {};
			eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
				vm.cloudprojectList = response.data.data;
		    });
			eayunHttp.post('ecsc/record/getAreaList.do',{"parentcode": ""}).then(function(response) {
				vm.areaP = response.data;
			});
			vm.changeArea = function (type,parentcode){
				eayunHttp.post('ecsc/record/getAreaList.do',{"parentcode": parentcode}).then(function(response) {
					if(type=="city"){
						vm.areaCity = response.data;
						vm.areaCounty=null;
						vm.model.city = null;
						vm.model.unitArea = null;
					}
					if(type=="county"){
						vm.areaCounty = response.data;
						vm.model.unitArea = null;
					}
				});
			};
			if(vm.model==null || vm.model.dutyEmail==null || vm.model.dutyEmail==""){
				eayunHttp.post('ecsc/record/getCusEmail.do').then(function(response) {
					vm.model.dutyEmail = response.data;
				});
			}
			vm.fileTypes = ['jpg','JPG'];
            vm.uploadFiles = function (_file,type) {//添加文件
            	if(_file.size > (100*1024)){
					eayunModal.warning("图片大小不得超过100KB");
					return;
				}
            	if(type=="duty"){
            		vm.isdutyshangchuan = false;
            	}
            	if(type=="business"){
            		vm.isbusinessshangchuan = false;
            	}
                if(_file){
                	Upload.upload({
			            url: 'ecsc/record/uploadRecordFile.do',//提交后台的上传图片
			            data: {"file": _file,"type":type}
			        }).then(function (response) {
			        	if(type=="business"){
			        		vm.model.businessFile = _file.name;
	                    	vm.model.businessFileId=response.data[0].business;
	                    	vm.isbusinessshangchuan = true;
	                    }
	                    if(type=="duty"){
	                    	vm.model.dutyFile = _file.name;
	                    	vm.model.dutyFileId=response.data[0].duty;
	                    	vm.isdutyshangchuan = true;
	                    }
			        	toast.success("上传图片成功");
			        });
                    
                }
            };
			vm.deleteRecordFile=function(fileId,type){//删除文件
				if(type=="duty"){
            		vm.isdutydeleteFile = false;
            	}
            	if(type=="business"){
            		vm.isbusinessdeleteFile = false;
            	}
				eayunHttp.post('ecsc/record/deleteRecordFile.do',{"fileId": fileId}).then(function(response) {
					console.info(fileId+"文件删除："+response.data);
					if(response.data){
						toast.success("文件删除成功");
						if(type=="business"){
		                	vm.model.businessFileId="";
		                	vm.model.businessFile="";
		                }
		                if(type=="duty"){
		                	vm.model.dutyFileId="";
		                	vm.model.dutyFile="";
		                }
					}else{
						eayunModal.warning("文件删除失败");
					}
					if(type=="duty"){
	            		vm.isdutydeleteFile = true;
	            	}
	            	if(type=="business"){
	            		vm.isbusinessdeleteFile = true;
	            	}
				});
			};
			vm.updateRecordFile=function(_file,fileId,type){//重新上传
				if(_file.size > (100*1024)){
					eayunModal.warning("图片大小不得超过100KB");
					return;
				}
				if(type=="duty"){
            		vm.isdutyagain = false;
            	}
            	if(type=="business"){
            		vm.isbusinessagain = false;
            	}
				if(_file){
                	Upload.upload({
			            url: 'ecsc/record/uploadRecordFile.do',//提交后台的上传图片
			            data: {"file": _file,"type":type}
			        }).then(function (response) {
			        	if(type=="business"){
			        		vm.model.businessFile = _file.name;
	                    	vm.model.businessFileId=response.data[0].business;
	                    }
	                    if(type=="duty"){
	                    	vm.model.dutyFile = _file.name;
	                    	vm.model.dutyFileId=response.data[0].duty;
	                    }
	                    eayunHttp.post('ecsc/record/deleteRecordFile.do',{"fileId": fileId}).then(function(response) {//删除以前的文件
	    					console.info("文件删除："+response.data);
	    				});
			        	toast.success("上传图片成功");
			        	if(type=="duty"){
		            		vm.isdutyagain = true;
		            	}
		            	if(type=="business"){
		            		vm.isbusinessagain = true;
		            	}
			        });
                }
			};
			vm.doFocus = function($event){
				$($event.target).parent().find('.ey-content-notice').show();
			};
			vm.doFocusPhone = function(){
				vm.isdutyphone = true;
			};
			vm.doBlur = function($event,isShow){
				if(!isShow){
					$($event.target).parent().find('.ey-content-notice').hide();
				}
			};
			
			vm.isdutyphone = true;
			vm.tijiao = function (){
				if(vm.model.certificateAddress!=null && vm.model.certificateAddress!=""){
					eayunHttp.post('ecsc/record/getPhoneAddress.do',vm.model.dutyPhone).then(function(response) {
						if(response.data.phoneAddress!=null){
							if(vm.model.certificateAddress.indexOf(response.data.phoneAddress)<0){
								eayunModal.warning("手机归属地与主办单位证件所在地不一致，验证失败");
								vm.isdutyphone = false;
								return;
							}else{
								vm.isdutyphone = true;
								if(vm.model.businessFileId==null || vm.model.businessFileId==""){
									eayunModal.warning("请上传营业执照");
									return;
								}
								if(vm.model.dutyFileId==null || vm.model.dutyFileId==""){
									eayunModal.warning("请上传负责人身份证照");
									return;
								}
								if(vm.model.unitArea==null || vm.model.unitArea==""){
									if(vm.model.city==null || vm.model.city==""){
										if(vm.model.area==null || vm.model.area==""){
											eayunModal.warning("请选择主办单位所属区域");
											return;
										}else{
											vm.model.unitArea=vm.model.area;
										}
									}else{
										vm.model.unitArea=vm.model.city;
									}
								}
								
								eayunStorage.set("unit",vm.model);//设置主体信息
								eayunStorage.set("areaP",vm.areaP);
								eayunStorage.set("areaCity",vm.areaCity);
								eayunStorage.set("areaCounty",vm.areaCounty);
								vm.istijiao = false;
								if(toType=="add"){
									$state.go('app.record.recordList');
								}else{
									$state.go('app.record.accessList');
								}
							}
						}else{
							eayunModal.warning("手机归属地查询失败");
							vm.isdutyphone = false;
							return;
						}
					});
				}
				
			};
			vm.cancel = function(){
				if(eayunStorage.get("unit") == null){
					eayunModal.confirm('离开页面后，信息将不会被保存').then(function() {
						eayunStorage.set("unit",null);
						if(toType=="add"){
							$state.go('app.record.recordList');
						}else{
							$state.go('app.record.accessList');
						}
					});
				}else{
					if(toType=="add"){
						$state.go('app.record.recordList');
					}else{
						$state.go('app.record.accessList');
					}
				}
			}
		}).controller('addwebCtrl', function ($scope,$state,eayunHttp,eayunStorage,Upload,toast,$stateParams,eayunModal,$rootScope) {
			var vm = this;
			var toType = $stateParams.toType;
			var list=[];
			if(toType=="add"){
				list=[{route:'app.record.main.apply',name:'开始备案'},{route:'app.record.recordList',name:'新增备案列表'}];
			}else{
				list=[{route:'app.record.main.apply',name:'开始备案'},{route:'app.record.unitList',name:'已备案列表'},{route:'app.record.changeweb',name:'变更网站列表'}];
			}
			
			//定义多个域名
			vm.divlistvalue = [];
	        vm.copy = function () {
	            vm.divlistvalue.push({});
	        };
	        vm.deletediv = function (index) {
	        	vm.commitfag=false;
	            if (index == 0) {
	                vm.divlistvalue.shift();
	            } else if (index == vm.divlistvalue.length) {
	                vm.divlistvalue.pop();
	            } else {
	                vm.divlistvalue.splice(index, 1);
	            }
	        };
	       
	        
	        //定义多个首页
			vm.homePage = [];
	        vm.homePagecopy = function () {
	            vm.homePage.push({});
	        };
	        vm.deleteHomePage = function (index) {
	        	vm.commitfag=false;
	            if (index == 0) {
	                vm.homePage.shift();
	            } else if (index == vm.homePage.length) {
	                vm.homePage.pop();
	            } else {
	                vm.homePage.splice(index, 1);
	            }
	        };
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
			
			vm.isdutydeleteFile = true;
			vm.isdutyshangchuan = true;
			vm.isdutyagain = true;
			vm.isdomaindeleteFile = true;
			vm.isdomainshangchuan = true;
			vm.isdomainagain = true;
			vm.isspecialdeleteFile = true;
			vm.isspecialshangchuan = true;
			vm.isspecialagain = true;
			$rootScope.navList(list,'备案网站');
			vm.cloudprojectList = {};
			eayunHttp.post('sys/overview/getvaliddclist.do',{}).then(function(response){
				vm.cloudprojectList = response.data.data;
		    });
			vm.model = angular.copy(eayunStorage.get("web") == null ? {} : eayunStorage.get("web"),{});
			if(vm.model!=null && vm.model!={ }){
				if(vm.model.domainName!=null){
					var domains = vm.model.domainName.split(";");
					for(var i=0;i<domains.length;i++){
						if(i==0){
							vm.model.domainName=domains[0];
						}else{
							vm.divlistvalue.push({"domain":domains[i]});
						}
					}
				}
				if(vm.model.domainUrl!=null){
					var url = vm.model.domainUrl.split(";");
					for(var i=0;i<url.length;i++){
						if(i==0){
							vm.model.domainUrl=url[0];
						}else{
							vm.homePage.push({"domainUrl":url[i]});
						}
					}
				}
				if(vm.model.ipList!=null && vm.model.ipList.length>0){
					vm.ipList = vm.model.ipList;
				}
			}
			if(toType=="change" || toType=="changeone"){
				if(vm.model.dutyFileId!=null && vm.model.dutyFileId!=""){
					vm.model.dutyFile="负责人身份证照";
				}
				if(vm.model.domainFileId!=null && vm.model.domainFileId!=""){
					vm.model.domainFile="域名证书照";
				}
				if(vm.model.specialFileId!=null && vm.model.specialFileId!=""){
					vm.model.specialFile="审批文件扫描图片";
				}
				if(toType=="changeone"){
					vm.isdutydeleteFile = false;
					vm.isdomaindeleteFile = false;
					vm.isspecialdeleteFile = false;
					eayunStorage.set("dutyFileId",vm.model.dutyFileId);
					eayunStorage.set("domainFileId",vm.model.domainFileId);
					eayunStorage.set("specialFileId",vm.model.specialFileId);
				}else{
					var dutyFileId = eayunStorage.get("dutyFileId");
					var domainFileId = eayunStorage.get("domainFileId");
					var specialFileId = eayunStorage.get("specialFileId");
					if(dutyFileId == vm.model.dutyFileId){
						vm.isdutydeleteFile = false;
					}
					if(domainFileId == vm.model.domainFileId){
						vm.isdomaindeleteFile = false;
					}
					if(specialFileId == vm.model.specialFileId){
						vm.isspecialdeleteFile = false;
					}
				}
			}
			$(".more").click(function(e) {
		        $(this).parent().children(".detail").toggle();
		    });	
			vm.fileTypes = ['jpg','JPG'];
            vm.uploadFiles = function (_file,type) {//添加文件
            	if(_file.size > (100*1024)){
					eayunModal.warning("图片大小不得超过100KB");
					return;
				}
                if(_file){
                	if(type=="duty"){
                		vm.isdutyshangchuan = false;
                	}
                	if(type=="domain"){
                		vm.isdomainshangchuan = false;
                	}
                	if(type=="special"){
                		vm.isspecialshangchuan = false;
                	}
                	Upload.upload({
			            url: 'ecsc/record/uploadRecordFile.do',//提交后台的上传图片
			            data: {"file": _file,"type":type}
			        }).then(function (response) {
			        	if(type=="domain"){
			        		vm.model.domainFile = _file.name;
	                    	vm.model.domainFileId=response.data[0].domain;
	                    	vm.isdomainshangchuan = true;
	                    }
	                    if(type=="duty"){
	                    	vm.model.dutyFile = _file.name;
	                    	vm.model.dutyFileId=response.data[0].duty;
	                    	vm.isdutyshangchuan = true;
	                    }
	                    if(type=="special"){
	                    	vm.model.specialFile = _file.name;
	                    	vm.model.specialFileId=response.data[0].special;
	                    	vm.isspecialshangchuan = true;
	                    }
			        	toast.success("上传图片成功");
			        	if(toType=="change" || toType=="changeone"){
			        		if(type=="duty"){
		                		vm.isdutydeleteFile = true;
		                	}
		                	if(type=="domain"){
		                		vm.isdomaindeleteFile = true;
		                	}
		                	if(type=="special"){
		                		vm.isspecialdeleteFile = true;
		                	}
			        	}
			        });
                    
                }
            };
			vm.deleteRecordFile=function(fileId,type){//删除文件
				if(type=="duty"){
					vm.isdutydeleteFile = false;
				}
				if(type=="domain"){
					vm.isdomaindeleteFile = false;
				}
				if(type=="special"){
					vm.isspecialdeleteFile = false;
				}
				eayunHttp.post('ecsc/record/deleteRecordFile.do',{"fileId": fileId}).then(function(response) {
					console.info("文件删除："+response.data);
					if(response.data){
						toast.success("文件删除成功");
						if(type=="domain"){
		                	vm.model.domainFileId="";
		                	vm.model.domainFile="";
		                }
		                if(type=="duty"){
		                	vm.model.dutyFileId="";
		                	vm.model.dutyFile="";
		                }
		                if(type=="special"){
		                	vm.model.specialFileId="";
		                	vm.model.specialFile="";
		                }
					}else{
						toast.success("文件删除失败");
					}
					if(type=="duty"){
						vm.isdutydeleteFile = true;
					}
					if(type=="domain"){
						vm.isdomaindeleteFile = true;
					}
					if(type=="special"){
						vm.isspecialdeleteFile = true;
					}
				});
			};
			vm.updateRecordFile=function(_file,fileId,type){//重新上传
				if(_file.size > (100*1024)){
					eayunModal.warning("图片大小不得超过100KB");
					return;
				}
				if(_file){
					if(type=="duty"){
						vm.isdutyagain = false;
					}
					if(type=="domain"){
						vm.isdomainagain = false;
					}
					if(type=="special"){
						vm.isspecialagain = false;
					}
                	Upload.upload({
			            url: 'ecsc/record/uploadRecordFile.do',//提交后台的上传图片
			            data: {"file": _file,"type":type}
			        }).then(function (response) {
			        	if(type=="domain"){
			        		vm.model.domainFile = _file.name;
	                    	vm.model.domainFileId=response.data[0].domain;
	                    	vm.isdomainagain = true;
	                    }
	                    if(type=="duty"){
	                    	vm.model.dutyFile = _file.name;
	                    	vm.model.dutyFileId=response.data[0].duty;
	                    	vm.isdutyagain = true;
	                    }
	                    if(type=="special"){
	                    	vm.model.specialFile = _file.name;
	                    	vm.model.specialFileId=response.data[0].special;
	                    	vm.isspecialagain = true;
	                    }
	                    eayunHttp.post('ecsc/record/deleteRecordFile.do',{"fileId": fileId}).then(function(response) {//删除以前的文件
	    					console.info("文件删除："+response.data);
	    				});
			        	toast.success("上传图片成功");
			        	if(toType=="change" || toType=="changeone"){
			        		if(type=="duty"){
		                		vm.isdutydeleteFile = true;
		                	}
		                	if(type=="domain"){
		                		vm.isdomaindeleteFile = true;
		                	}
		                	if(type=="special"){
		                		vm.isspecialdeleteFile = true;
		                	}
			        	}
			        });
                }
			};
			vm.doFocus = function($event){
				$($event.target).parent().find('.ey-content-notice').show();
			};

			vm.doBlur = function($event,isShow){
				if(!isShow){
					$($event.target).parent().find('.ey-content-notice').hide();
				}
			};
			vm.dofFocus = function($event){
				$($event.target).parent().parent().find('.ey-content-notice').show();
			};
			
			vm.dofBlur = function($event,itme,type,index){
				vm.commitfag=false;
				
				//验证域名重复
				if(type=='domainname'){
					if(itme.domain!=undefined){
						$($event.target).parent().parent().find('.ey-content-notice').hide();
					}
					vm.divlistvalue[index].chekefag=false;
					if(vm.divlistvalue[index].domain!=undefined){
						if(vm.model.domainName==vm.divlistvalue[index].domain){
							vm.divlistvalue[index].chekefag=true;
							vm.commitfag=true;
						}
						for(var i=0;i<vm.divlistvalue.length;i++){
			        		if(i==index){
			        			continue;
			        		}
			        		if(vm.divlistvalue[i].domain==vm.divlistvalue[index].domain){
			        			vm.divlistvalue[index].chekefag=true;
			        			vm.commitfag=true;
			        			 break;
			        		}
			        	}
					}
				}else{
					//url
					if(itme.domainUrl!=undefined){
						$($event.target).parent().parent().find('.ey-content-notice').hide();
					}
					vm.homePage[index].chekefag=false;
					if(vm.homePage[index].domainUrl!=undefined){
						if(vm.model.domainUrl==vm.homePage[index].domainUrl){
							vm.homePage[index].chekefag=true;
							vm.commitfag=true;
						}
						for(var i=0;i<vm.homePage.length;i++){
			        		if(i==index){
			        			continue;
			        		}
			        		if(vm.homePage[i].domainUrl==vm.homePage[index].domainUrl){
			        			vm.homePage[index].chekefag=true;
			        			vm.commitfag=true;
			        			 break;
			        		}
			        	}
					}
				}
	        	
			};
			vm.changeServiceIP = function(index){
				vm.ips[index] = new Array();
				vm.ipList[index].webService=null;
				vm.ipList[index].ip=null;
			};
			
			vm.webList = eayunStorage.get("webList") == undefined ? [] : eayunStorage.get("webList");
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
								if(toType=="change" || toType=="changeone"){
									var ifaddold = 0;
									for(var j=0;j<vm.ips[ipsnum].length;j++){
										if(webip==vm.ips[ipsnum][j] && i!=ipsnum){
											ifaddold++;
					                    }
									}
									if(ifaddold==0){//一个都没有重复的说明原IP需要添加
										vm.ips[ipsnum].push(webip);
									}
								}
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
			vm.serviceContents = vm.model.serviceContent==undefined?[]:vm.model.serviceContent.split(",");
			vm.webLanguages = vm.model.webLanguage==undefined?[]:vm.model.webLanguage.split(",");
			
			vm.recursiveReplace = function(str){
				if (str!=null && str != undefined && str!='') {
					if(str.indexOf(",,") < 0){
						if(str.length>1){
							var strx = str.substring(0,1);
							if(strx==","){
								str = str.substring(1,str.length);
							}
						}
						return str;
					}else{
						str = str.replace(",,",",");
						return vm.recursiveReplace(str);
					}
				}else{
					return '';
				}
			}
			

			vm.copyunit=function(){
				if(toType=="add"){
					vm.unitDeail =angular.copy( eayunStorage.get("unit") == undefined ? {} : eayunStorage.get("unit"));
				
					vm.model.webDutyName=vm.unitDeail.dutyName;
					vm.model.dutyCertificateType=vm.unitDeail.dutyCertificateType;
					vm.model.dutyCertificateNo=vm.unitDeail.dutyCertificateNo;
					vm.model.phone=vm.unitDeail.phone;
					vm.model.dutyPhone=vm.unitDeail.dutyPhone;
					vm.model.dutyEmail=vm.unitDeail.dutyEmail;
					vm.model.dutyQQ=vm.unitDeail.dutyQQ;
				}else{
					eayunHttp.post('ecsc/record/getUnitOne.do',vm.model.unitId).then(function(response){
						vm.unitDeail = response.data;
						
						vm.model.webDutyName=vm.unitDeail.dutyName;
						vm.model.dutyCertificateType=vm.unitDeail.dutyCertificateType;
						vm.model.dutyCertificateNo=vm.unitDeail.dutyCertificateNo;
						vm.model.phone=vm.unitDeail.phone;
						vm.model.dutyPhone=vm.unitDeail.dutyPhone;
						vm.model.dutyEmail=vm.unitDeail.dutyEmail;
						vm.model.dutyQQ=vm.unitDeail.dutyQQ;
				    });
				}
			}
			
			vm.tijiao = function (){
				if(vm.model.domainFileId==null || vm.model.domainFileId==""){
					eayunModal.warning("请上传网站域名照");
					return;
				}
				if(vm.model.dutyFileId==null || vm.model.dutyFileId==""){
					eayunModal.warning("请上传负责人身份证照");
					return;
				}
				if(vm.model.webSpecial!=null && vm.model.webSpecial!=""){
					if(vm.model.specialFileId==null || vm.model.specialFileId==""){
						eayunModal.warning("请上传前置审批照");
						return;
					}
				}
				vm.model.serviceContent = vm.serviceContents.join();
				vm.model.webLanguage = vm.webLanguages.join();
				vm.isShowLanguages = false;
				vm.isShowContent = false;
				if(vm.recursiveReplace(vm.model.serviceContent)=="," || vm.recursiveReplace(vm.model.serviceContent)==""){
					$(window).scrollTop(370);
					vm.isShowContent = true;
					return;
				}
				if(vm.recursiveReplace(vm.model.webLanguage)=="," || vm.recursiveReplace(vm.model.webLanguage)==""){
					$(window).scrollTop(400);
					vm.isShowLanguages = true;
					return;
				}
				for(var i=0;i<vm.homePage.length;i++){
					if(undefined != vm.homePage[i].domainUrl && "" != vm.homePage[i].domainUrl){
						vm.model.domainUrl += ";"+vm.homePage[i].domainUrl;
					}
				}
				for(var i=0;i<vm.divlistvalue.length;i++){
					if(undefined != vm.divlistvalue[i].domain && "" != vm.divlistvalue[i].domain){
						vm.model.domainName += ";"+vm.divlistvalue[i].domain;
					}
				}
				vm.model.ipList = vm.ipList;
				eayunStorage.set("web",vm.model);//设置网站信息
				if(toType=="add"){
					$state.go('app.record.recordList');
				}else{
					eayunStorage.set("isChangeone",false);
					$state.go('app.record.changeweb',{unitId:vm.model.unitId});
				}
			};
			vm.cancel = function(){
				if(eayunStorage.get("web") == null){
					eayunModal.confirm('离开页面后，信息将不会被保存').then(function() {
						eayunStorage.set("web",null);
						if(toType=="add"){
							$state.go('app.record.recordList');
						}else{
							$state.go('app.record.changeweb',{unitId:vm.model.unitId});
						}
					});
				}else{
					if(toType=="add"){
						$state.go('app.record.recordList');
					}else{
						if(toType=="changeone"){
							eayunModal.confirm('离开页面后，信息将不会被保存').then(function() {
								eayunStorage.set("web",null);//设置网站信息
								vm.models = eayunStorage.get("changeList") == undefined ? [] : eayunStorage.get("changeList");
								//eayunStorage.set("webList",null);//设置网站信息
								for(var i=0;i<vm.models.length;i++){
									var oldweb = vm.models[i];
									if(oldweb.webId==vm.model.webId){
										oldweb.isChange=1;
									}
								}
								for(var j=0;j<vm.webList.length;j++){
									var w = vm.webList[j];
									if(w.webId==vm.model.webId){
										vm.webList.splice(j, 1);
									}
								}
								$state.go('app.record.changeweb',{unitId:vm.model.unitId});
							});
						}else{
							$state.go('app.record.changeweb',{unitId:vm.model.unitId});
						}
					}
				}
			}
		});

