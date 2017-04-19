'use strict';
angular.module('eayunApp.controllers').config( function($stateProvider, $urlRouterProvider) {
			$urlRouterProvider.when("/app/work","/app/work/workList/handle/");
			$stateProvider.state("app.work.workList",{
		    	url: '/workList/:type/:workFalg',
			    templateUrl: 'views/work/workmng.html',
		    	controller: 'workListCtrl'
		    }).state("app.work.addWork",{
				url: '/addWork',
			    templateUrl: 'views/work/addwork.html',
			    controller: 'addWorkCtrl'
		    }).state("app.work.workDatil",{
				url: '/workDatil/:workId',
			    templateUrl: 'views/work/datilwork.html',
			    controller: 'workDatilCtrl'
			});
		})
		/**
		 * 父类级别ctrl
		 */
		.controller('workCtrl', function($scope,eayunModal,eayunHttp,$state,Upload,toast) {
			$scope.name = '工单';
			//关闭或者删除工单
			$scope.closeWorkorder = function(workorder){
				var mess= "";
				if(workorder.workEcscFalg=="4" || workorder.workEcscFalg=="5"||workorder.workEcscFalg=="7"){//待评价、已关闭，已取消的工单，删除操作。
					var workWorkNum=workorder.workNum;
					mess="删除工单"+workWorkNum;
				}else{
					mess= "取消工单";
				}
				eayunModal.confirm("确定要"+mess+"?").then(function () {
					if(workorder.workEcscFalg=="4" || workorder.workEcscFalg=="5" ||workorder.workEcscFalg=="7"){//待评价、已关闭或者已取消的工单，删除操作。
						workorder.workEcscFalg ="6";
					}else{
						workorder.workFalg="4";
						workorder.workEcscFalg="7";
					}
					eayunHttp.post("sys/work/updateWorkorderForFalg.do",workorder).then(function(data){
						if(data.status=="200"){
							toast.success(mess+"成功");
							$state.go('^.workList',{},{reload:true});
						}else{
							
						}
					});
				});
			};
			
			//上传附件
		    $scope.shortName = function (name) {
		      if (name && name.length > 2)
		        return name.substring(0, 2) + '...';
		      return name;
		    };
		})
		/**
		 * 列表ctrl
		 */
		.controller("workListCtrl",function($rootScope,$scope,$state,eayunHttp,eayunModal,$stateParams){
			$rootScope.childname = '';
			$rootScope.workRoute = null;
			eayunHttp.post("sys/work/getDataTree.do","0007001002").then(function(data){//获取工单问题类型
				$scope.dataTree = data.data;
			});
			$scope.workorder = {
					workFalg:null
			};
			var firstDate = new Date();
            firstDate.setDate(1); //第一天
            firstDate.setHours(0, 0, 0, 0);
            $scope.beginTime = firstDate;
            var end=new Date();
            end.setHours(0, 0, 0, 0);
            $scope.endTime = end;
            $scope.maxTime=end;
			//初始化列表加载
			$scope.myTable = {
				source: 'sys/work/getWorkorderList.do',
				api:{},
				getParams: function () {
					return {
						workNum : $scope.workNum || '' ,
			        	beginTime :  $scope.beginTime ? $scope.beginTime.getTime() : '',
			        	endTime :  $scope.endTime ? $scope.endTime.getTime()+86400000 : '',//结束时间加一天
			        	keyWord:$scope.keyWord || '',
			        	workFalg:$scope.workorder.workFalg==null?'':$scope.workorder.workFalg,
	        			workType:$scope.workorder.workType == null?'':$scope.workorder.workType,
			        	type:$stateParams.type==null?'':$stateParams.type
			        };
				}
		    };
			if($stateParams.workFalg!=null && $stateParams.workFalg!='null' && $stateParams.workFalg!=''){
				$scope.workorder={
					workFalg:$stateParams.workFalg
				};
			}
			$scope.typeStatus = [
				           		{pId: '0007001003001', text: '配额'}
			           		];
			$scope.typeItemClicked = function (item, event) {
			  	$scope.workorder.workType=item.pId;
				$scope.myTable.api.draw();
			};
			$scope.$watch('dataTree' , function(newVal,oldVal){
		    	if(newVal !== oldVal){
		    		angular.forEach(newVal, function (value,key) {
		    			$scope.typeStatus.push({pId: value.nodeId, text: value.nodeName});
		    		});
		    	}
		    });
			
			$scope.status = [
					           		{pId: '', text: '状态(全部)'},
					           		{pId: '0', text: '待受理'},
					           		{pId: '1', text: '处理中'},
					           		{pId: '2', text: '待反馈'},
					           		{pId: '3', text: '待确认'},
					           		{pId: '4', text: '待评价'},
					           		{pId: '5', text: '已关闭'},
					           		{pId: '7', text: '已取消'}
				           		];
			$scope.statusItemClicked = function (item, event) {
			  	$scope.workorder.workFalg=item.pId;
				$scope.myTable.api.draw();
			};
			//查看工单
			$scope.findWorkByWorkId=function(workId){
				$state.go('^.workDatil',{"workId":workId}); // 跳转后的URL;
			};
			$scope.$watch('workorder.workFalg' , function(newVal,oldVal){
		    	if(newVal !== oldVal){
		    		$scope.myTable.api.draw();
		    	}
		    });
			/*$scope.$watch('workorder.workType' , function(newVal,oldVal){
				if(newVal !== oldVal){
					$scope.myTable.api.draw();
				}
			});*/
			
            // --------回车事件
            $scope.checkUser = function () {
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
                    var e = event || window.event
                        || arguments.callee.caller.arguments[0];
                    if (!$scope.checkUser()) {
                        return;
                    }
                    // 用户登录判断
                    if (e.keyCode == 13) {
                        $scope.myTable.api.draw();
                    }
                };
            });
		})
		/**
		 * 新增ctrl
		 */
		.controller("addWorkCtrl",function($rootScope,$scope,eayunHttp,$state,eayunModal,Upload,toast){//添加工单
			$rootScope.workRoute = '#/app/work';
			$rootScope.childname = '创建工单';
			eayunHttp.post("sys/work/getDataTree.do","0007001002").then(function(data){//获取工单问题类型
				$scope.dataTree = data.data;
				
			});
			eayunHttp.post("sys/work/getUserInfo.do").then(function(data){//获取工单问题类型
				$scope.workorder={//给单选框默认值
						workType:null,
						workPhone:data.data.phone,
						workEmail:data.data.email,
						workPhoneTime:"2",
						workCreUserName:data.data.userName
				};
				if(data.data.phone==null && data.data.email==null){
					$scope.mailPhoneBtn=true;
				}else{
					$scope.mailPhoneBtn=false;
				}
			});
			$scope.files = [];
			$scope.fileTypes = ['jpg','bmp','png','gif','txt','rar','zip','doc','docx','ini','conf','eml','pdf'];
			$scope.uploadFiles = function (file) {
				if(file){
					if($scope.files.length>=3){
						eayunModal.warning("附件总数不能超过3个");
						return;
					}
					if(file.size > (2*1024*1024)){
						eayunModal.warning("附件大小不得超过2M");
						return;
					}
					$scope.files.push(file);
				}
		    };
		    $scope.deleteWorkFile=function(file){
				for(var i=0;i<$scope.files.length;i++){
					var file1 = $scope.files[i];
					if(file1==file){
						$scope.files.splice(i, 1);
					}
				}
			};
			//添加工单
			$scope.addWorkorder=function(){
				var workorder = $scope.workorder;
				$scope.checkBtn = true;
				eayunHttp.post("sys/work/addWorkorder.do",{"workorder":$scope.workorder}).then(function(data){
					$scope.checkBtn = true;
			        Upload.upload({
			          url: 'sys/work/addWorkFile.do',//提交后台的
			          data: {"file": $scope.files,"opinionId":data.data.opinionId}
			        }).then(function (response) {
			        	$scope.files=[];
			        	toast.success("工单已提交成功，请等待受理");
			        	$state.go("^.workList");
			        }, function (response) {
			        	$scope.checkBtn = false;
//			          console.info('上传文件失败');
			        }, function (evt) {
//			          file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
			        });
				});
			};
			$scope.mailPhone=false;
		  	$scope.phone=false;
			$scope.mail=false;
			$scope.checkMailPhone=function(){
				var tel = $scope.workorder.workPhone;
				var email = $scope.workorder.workEmail;
				if(email==""){
					email=null;
				}
				if(tel==""){
					tel=null;
				}
				var regxPhone=/^1[3|5|8|7][0-9]\d{8}$/;
				var regxMail=/^[a-zA-Z0-9_-]+[\.a-zA-Z0-9_-]+[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
				
				$scope.mailPhone=false;
				$scope.mailPhoneBtn=false;
				if(tel!=null&& email!=null){//都不为空
					if(regxPhone.test(tel)){//电话通过验证
						$scope.phone=false;
					}else{
						$scope.phone=true;
						$scope.mailPhoneBtn=true;
					}
					if(regxMail.test(email)){//邮箱通过验证
						$scope.mail=false;
					}else{
						$scope.mail=true;
						$scope.mailPhoneBtn=true;
					}
				}else if(tel!=null && email==null){//手机不为空，邮箱为空
					$scope.mail=false;
					if(regxPhone.test(tel)){//电话通过验证
						$scope.phone=false;
					}else{
						$scope.phone=true;
						$scope.mailPhoneBtn=true;
					}
				}else if(tel==null && email!=null){//手机为空，邮箱不为空
					$scope.phone=false;
					if(regxMail.test(email)){//邮箱通过验证
						$scope.mail=false;
					}else{
						$scope.mail=true;
						$scope.mailPhoneBtn=true;
					}
				}else{//都为空
					$scope.phone=false;
					$scope.mail=false;
					$scope.mailPhone=true;
					$scope.mailPhoneBtn=true;
				}
			};		
		})
		/**
		 * 详情
		 */
		.controller("workDatilCtrl",function($rootScope,$scope,eayunHttp,eayunModal,$stateParams,Upload,toast){
			$rootScope.childname = ' 工单详情';
			$rootScope.workRoute = '#/app/work';
			/*
			 * 初始化页面
			 */
			findWorkByWorkId();

//			getWorkFileListByWorkId();
			//已解决单选框默认值
			$scope.workFalg="3";
			//评价单选框默认值
			$scope.workHighly="1";
			/*
			 * 初始化结束
			 */
			function findWorkByWorkId(){
				eayunHttp.post("sys/work/findWorkByWorkId.do",{"workId":$stateParams.workId}).then(function(data){
					$scope.workorder=data.data;
					$scope.step = data.data.workEcscFalg;
					if(data.data.workType=='0007001003001'){//配额类工单，得到配额信息
						statPrj(data.data.prjId,data.data.workId);
					}else{
						getWorkOpinionList();
					}
				});
			}
			function getWorkOpinionList(){
				eayunHttp.post("sys/work/getWorkOpinionList.do",{"workId":$stateParams.workId}).then(function(data){
					$scope.workOpinionList=data.data;
					var num = 0;
					if($scope.workorder.workType=='0007001003001' && !$scope.$$isOld){ // 配额类工单不显示第一条
						$scope.workOpinionList.shift();
					}
					angular.forEach($scope.workOpinionList, function (value, key) {
				        if(value.workQuota != null){
				        	var workQuota=value.workQuota;
				        	workQuota.quotaVm!=0? num++ :'';
							workQuota.quotaCpu!=0? num++ :'';
							workQuota.quotaMemory!=0? num++ :'';
							workQuota.quotaDisk!=0? num++ :'';
							workQuota.quotaSnapshot!=0? num++ :'';
							workQuota.quotaBand!=0? num++ :'';
							workQuota.quotaNet!=0? num++ :'';
							workQuota.quotaSubnet!=0? num++ :'';
							workQuota.quotaFloatIp!=0? num++ :'';
							workQuota.quotaRoute!=0? num++ :'';
							workQuota.quotaSecGroup!=0? num++ :'';
							workQuota.quotaBalance!=0? num++ :'';
							workQuota.quotaSms!=0? num++ :'';
							workQuota.quotaPortMapping!=0? num++ :'';
							workQuota.quotaDiskSize!=0? num++ :'';
							workQuota.quotaShotSize!=0? num++ :'';
							return false;//跳出循环
				        }
					});
					if(num>5){
						$scope.showMoreDiv=true;
					}else{
						$scope.showMoreDiv=false;
					}
				});
			}
			
			
			function statPrj(prjId, workId){
				  eayunHttp.post("/sys/work/getStatisticsByWorkId.do",workId).then(function(respone){
					  if(respone.data){
						  $scope.prj=respone.data;
						  $scope.$$isOld = false;
						  getWorkOpinionList();
					  }else{
						  eayunHttp.post("sys/overview/getStatisticsByPrjId.do",prjId).then(function(respone){
							  $scope.prj=respone.data;
							  $scope.$$isOld = true;
							  getWorkOpinionList();
						  });
					  }
				  });
			  }
			//附件下载
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
			$scope.down=function(fileId){
				$("#file-export-iframe").attr("src","file/down.do?fileId="+fileId+"&browser="+browser);
			};
			//显示更多
			$scope.open=false;
			$scope.seeOrUp="查看";
			$scope.showMore=function(){
				$scope.open=!$scope.open;
				if($scope.open){
					$scope.seeOrUp="收起";
				}else{
					$scope.seeOrUp="查看";
				}
			};
			//确认工单 updateWorkorderForFalg
			$scope.confirmWorkorder=function(){
				//隐藏参数
				var workorder = $scope.workorder;
				workorder.workFalg=$scope.workFalg;
				//---结束
				if($scope.workFalg=="3"){//已解决
					workorder.workEcscFalg="4";
				}else{//未解决
					workorder.workEcscFalg="2";
				}
				return workorder;
			};
			//评价工单 
			$scope.judgeWorkorder=function(){
				//隐藏参数
				var workorder = $scope.workorder;
				//---结束
				workorder.workHighly=$scope.workHighly;
				workorder.workEcscFalg="5";
				return workorder;
			};
			$scope.files = [];
			$scope.fileTypes = ['jpg','bmp','png','gif','txt','rar','zip','doc','docx','ini','conf','eml','pdf'];
			$scope.uploadFiles = function (file) {
				if(file){
					if($scope.files.length>=3){
						eayunModal.warning("附件总数不能超过3个");
						return;
					}
					if(file.size > (2*1024*1024)){
						eayunModal.warning("附件大小不得超过2M");
						return;
					}
					$scope.files.push(file);
				}	
		    };
		    $scope.deleteWorkFile=function(file){
				for(var i=0;i<$scope.files.length;i++){
					var file1 = $scope.files[i];
					if(file1==file){
						$scope.files.splice(i, 1);
					}
				}
			};
			//添加回复--沟通记录
			$scope.addWorkOpinion=function(){
				$scope.checkBtn = true;
				var workorder = $scope.workorder;
				if((workorder.workEcscFalg=='3' && $scope.workFalg=="3") || workorder.workEcscFalg=='4'){
					//待反馈，并且确认解决。或者评价，回复内容不是必输的。
				}else{
					if($scope.content==null || $scope.content==""){
						$scope.checkBtn = false;
						eayunModal.warning("请输入回复信息");
						return ;
					}
				}
				if(workorder.workEcscFalg=='3'){
					workorder=$scope.confirmWorkorder();
				}else if(workorder.workEcscFalg=='4'){
					workorder=$scope.judgeWorkorder();
				}
				if($scope.content==null || $scope.content==""){
					$scope.content="";
				}
				eayunHttp.post("sys/work/addWorkOpinion.do",{"workorder":workorder,"content":$scope.content}).then(function(data){//添加沟通记录
					$scope.$emit("RefreshUnHandleWorkCount");
					Upload.upload({
			          url: 'sys/work/addWorkFile.do',//提交后台的
			          data: {"file": $scope.files,"opinionId":data.data.opinionId}
			        }).then(function (response) {
			        	$scope.checkBtn = false;
			        	$scope.files=[];
			        	//更新记录显示.
						findWorkByWorkId();//重新初始化
						getWorkOpinionList();//重新初始化
						$scope.content="";
						if( workorder.workEcscFalg=='5'){
							eayunModal.info("感谢您对我们工作的支持！");
						}
			        }, function (response) {
			        	$scope.checkBtn = false;
			        }, function (evt) {
//			          file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
			        });
				});
			};
			//一键投诉
			$scope.updateWorkForFc=function(workorder){
				eayunModal.confirm("确定要一键投诉？").then(function(){
					eayunHttp.post("sys/work/updateWorkFlowForFc.do",{"workId":workorder.workId, "workTitle":workorder.workTitle}).then(function(data){
						toast.success("投诉成功。");
						findWorkByWorkId();//重新初始化
						getWorkOpinionList();//重新初始化
					});
				});
			};
			$scope.$watch('workorder.workEcscFalg' , function(newVal,oldVal){
		    	if(newVal !== oldVal){
		    		$scope.$emit("RefreshUnHandleWorkCount");
		    	}
		    });
		});
