/**
 * Created by eayun on 2017/2/22.
 */
'use strict';
angular.module('eayunApp.controllers')
    .config(function () {

    })
    .controller('RdsConfigCtrl',                  ['$rootScope','$scope','RDSConfigService','eayunModal','toast','SysCode','powerService', function ($rootScope,$scope, RDSConfigService,eayunModal,toast,SysCode,powerService) {
        var config = this ;
        var routeList = [];
        $rootScope.navList(routeList, '配置文件') ;
        powerService.powerRoutesList().then(function(powerList){
            config.canCreate = powerService.isPower('rds_config_create') ;
            config.canDelete = powerService.isPower('rds_config_delete') ;
        }) ;
        var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
        //数据中心ID
        var dcId = dcPrj.dcId ;
        //项目ID
        var projectId = dcPrj.projectId ;
        /**
         * 验证用户的合法性
         * @returns {boolean}
         */
        config.checkUser = function () {
            var user = sessionStorage["userInfo"];
            if (user) {
                user = JSON.parse(user);
                if (user && user.userId) {
                    return true;
                }
            }
            return false;
        };

        $scope.$watch('model.projectvoe', function(newVal,oldVal){
            if(newVal !== oldVal){
                config.table.api.draw();
            }
        });

        /**
         * 表单选择框输入名称搜索
         * @type {{searchFn: config.options.searchFn, placeholder: string, select: *[]}}
         */
        config.fileNameKeyword = "" ;   //配置文件关键字
        config.dbVersion = "" ;         //数据库版本
        config.fileType = "" ;          //配置文件类型
        config.options = {
            searchFn: function () {
                if(!config.checkUser()){
                    return ;
                }
                config.table.api.draw();
            },
            placeholder:"请输入查询内容",
            select: [{configFileNameKey: '名称'},{configFileVersion: '版本'}]
        };
        /**
         * 分页展示列表数据Table
         * @type {{api: {}, source: string, getParams: bak.table.getParams}}
         */
        config.table = {
            api: {},
            source: 'rds/config/listConfigFile.do',
            getParams: function () {
                return {
                    dcId: $scope.model.projectvoe ? $scope.model.projectvoe.dcId : '',                           //数据中心ID
                    projectId: $scope.model.projectvoe ? $scope.model.projectvoe.projectId : '',                  //项目ID
                    dbVersion: config.dbVersion,          //数据库版本
                    fileNameKeyword: config.fileNameKeyword.value == null ? "" : config.fileNameKeyword.value,     //配置文件查询关键字
                    fileType: config.fileType , //配置文件类型,
                    searchType : config.fileNameKeyword.key ? config.fileNameKeyword.key : ""  //查询关键字的数据类型
                };
            }
        };
        /**
         * 定义配置文件的可选类型
         * @type {*[]}
         */
        config.fileCategoryList = [{
            'nodeNameEn':'0',
            'nodeName':'类型'
        },{
            'nodeNameEn':'1',
            'nodeName':'默认配置文件'
        },{
            'nodeNameEn':'2',
            'nodeName':'客户配置文件'
        }];
        config.selectFileCategory = function(item,event){
            config.fileType = item.nodeNameEn ;
            config.table.api.draw();
        };
        /**
         * 定义配置全部的数据库版本信息
         * @type {null}
         */
        config.dbVersionList = [{
            'nodeNameEn':'0',
            'nodeName':'DB版本'
        }] ;
        config.selectDbVersionCategory = function(item,event){
            config.dbVersion = item.nodeNameEn ;
            config.table.api.draw();
        };
        RDSConfigService.getAllDBVersion().then(function(data){
            for (var i=0; i < data.length; i++) {
                var eachObj = data[i] ;
                if (eachObj.nodeName == 'mysql5.5'){
                    eachObj.nodeName = 'MySQL5.5' ;
                }
                if (eachObj.nodeName == 'mysql5.6'){
                    eachObj.nodeName = 'MySQL5.6'
                }
                config.dbVersionList.push(eachObj);
            }
        }) ;
        /**
         * 创建配置文件
         */
        config.createTagGroup=function(){
            var result=eayunModal.open({
                title: '创建配置文件',
                width: '600px',
                height: '400px',
                templateUrl: 'views/rds/config/addconfiggroup.html',
                controller: 'RdsCreateConfigCtrl',
                controllerAs: 'createCon',
                resolve: {
                }
            });
            result.result.then(function (taggroupModel) {//configId
                RDSConfigService.existConfigFile(taggroupModel.configId).then(function(data) {
                    if (SysCode.success == data.respCode) {
                        RDSConfigService.createConfigFile(taggroupModel).then(function(data){
                            if (SysCode.success == data.respCode){
                                toast.success('配置文件创建成功');
                                config.table.api.draw();
                            }else if (SysCode.error == data.respCode){
                                toast.error('配置文件创建失败');
                            }else {
                                eayunModal.warning(data.message);
                            }
                        }) ;
                    }
                    if (SysCode.error == data.respCode) {
                        eayunModal.warning("模板配置文件 " + taggroupModel.groupName + " 不存在或已删除")
                    }
                });
            });
        };
        /**
         * 删除指定的配置组以及配置文件
         * @param groupId
         */
        config.deleteTagGroup=function(groupId, name){
            RDSConfigService.checkConfigCanBeDelete(dcId,projectId,groupId).then(function(data){
                if (SysCode.success == data.respCode){
                    eayunModal.warning('该配置文件已应用到实例，请更换实例的配置文件后再进行操作');
                }else {
                    eayunModal.confirm('确认删除配置文件 '+name+'？').then(function (){
                        RDSConfigService.deleteConfigFile(groupId).then(function(data){
                            if (SysCode.success == data.respCode){
                                toast.success('配置文件删除成功');
                                config.table.api.draw();
                            }else if (SysCode.error == data.respCode) {
                                toast.error('配置文件删除失败');
                            }else {
                                eayunModal.warning(data.message);
                            }
                        }) ;
                    });
                }
            }) ;
        }
        /**
         * 查看指定配置文件内容详情
         */
        config.showDetailGroup=function(configFile){
            RDSConfigService.existConfigFile(configFile.configId).then(function(data){
                if (SysCode.success == data.respCode){
                    //存在指定的配置文件
                    var result=eayunModal.open({
                        title: '查看配置文件',
                        width: '820px',
                        //height: '400px',
                        templateUrl: 'views/rds/config/configdetailandedit.html',
                        controller: 'RdsShowDetailConfigCtrl',
                        controllerAs: 'showDetail',
                        resolve: {
                            ConfigFile : function(){
                                return configFile ;
                            },
                            Table : function(){
                                return config.table ;
                            }
                        }
                    });
                }
                if (SysCode.error == data.respCode) {
                    //不存在指定的配置文件或者已经被删除
                    eayunModal.warning("配置文件 " + configFile.configName + " 不存在或已删除")
                }
            });
        };
        config.showDetailDefault=function(configFile){
            var result=eayunModal.open({
                title: '查看配置文件',
                width: '820px',
                //height: '400px',
                templateUrl: 'views/rds/config/configdetaildefault.html',
                controller: 'RdsShowDetailDefaultConfigCtrl',
                controllerAs: 'showDetailDefault',
                resolve: {
                    ConfigFile : function(){
                        return configFile ;
                    }
                }
            });
        };
    }])
    .controller('RdsCreateConfigCtrl',            ['$scope','$rootScope','RDSConfigService','$modalInstance','SysCode', function ($scope,$rootScope, RDSConfigService,$modalInstance,SysCode) {
        var createCon = this ;
        var dcPrj = JSON.parse(sessionStorage["dcPrj"]);
        //数据中心ID
        var dcId = dcPrj.dcId ;
        //项目ID
        var projectId = dcPrj.projectId ;
        createCon.configEntity = {} ;
        //创建配置文件的类型
        createCon.configEntity.configType = "2" ;
        //配置文件所在的数据中心
        createCon.configEntity.configDatacenterid = dcId ;
        //配置中心所处的项目
        createCon.configEntity.configProjectid = projectId ;
        createCon.cancel = function () {
            $modalInstance.dismiss();
        };
        createCon.commit = function () {
            createCon.configEntity.groupName = createCon.configFileListRelations[createCon.configEntity.configId];
            $modalInstance.close(createCon.configEntity);
        };
        RDSConfigService.getAllDBVersion().then(function(data){
            createCon.versionList = data ;
            for (var i = 0 ;i<createCon.versionList.length ; i++){
                var eachObj = createCon.versionList[i] ;
                if (eachObj.nodeName == 'mysql5.5'){
                    eachObj.nodeName = 'MySQL5.5' ;
                }
                if (eachObj.nodeName == 'mysql5.6'){
                    eachObj.nodeName = 'MySQL5.6'
                }
            }
        }) ;
        //存储对应关系
        createCon.configFileListRelations = {} ;
        createCon.versionChange = function(){
            RDSConfigService.queryConfigFileByVersion({
                versionId : createCon.configEntity.configVersion,
                dcId : dcId,
                projectId : projectId
            }).then(function(data){
                createCon.configFileList = data ;
                for (var i = 0 ; i< createCon.configFileList.length ; i++){
                    var eachObj = createCon.configFileList[i] ;
                    if (eachObj.configType=='1') {
                        createCon.configEntity.configId = eachObj.configId;
                        //break ;
                    }
                    createCon.configFileListRelations[createCon.configFileList[i].configId] = createCon.configFileList[i].configName;
                }
            }) ;
        };
        /**
         * 验证是否有同名文件存在
         */
        createCon.fileNameIsCanbeUsed = false ;
        /**
         * 设置一个标识标识表示是否为第一次进入页面
         * @type {boolean}
         */
        createCon.isFirstComein = true ;
        createCon.ajaxConfirm = function(value){
            createCon.isFirstComein = false ;
                return RDSConfigService.queryCusSelfConfigFileByFilename(dcId, projectId, value)
                    .then(function (data) {
                        if (SysCode.success == data.respCode) {
                            createCon.fileNameIsCanbeUsed = true;
                            return false;
                        } else {
                            createCon.fileNameIsCanbeUsed = false;
                            return true;
                        }
                    })
        };
    }])
    .controller('RdsShowDetailConfigCtrl',        ['$scope','$rootScope','RDSConfigService','$modalInstance','ConfigFile','powerService','SysCode','toast','Table','eayunModal', function ($scope,$rootScope, RDSConfigService,$modalInstance,ConfigFile,powerService,SysCode,toast,Table,eayunModal) {
        var showDetail = this ;
        powerService.powerRoutesList().then(function(powerList){
            showDetail.canUpdate = powerService.isPower('rds_config_update') ;
        }) ;
        /**
         * 关闭当前的弹出提示以及内容编辑框
         */
        showDetail.cancel = function () {
            $modalInstance.dismiss();
        };
        /**
         * 按钮取消编辑的操作
         */
        showDetail.cancelEdit = function(){
            showDetail.isNowEdit = false ;
        }
        showDetail.confirmUpdate = function(){
            var updatedParams = showDetail.paramsList ;
            for (var i = 0; i < updatedParams.length; i++) {
                var obj = updatedParams[i] ;
                if (obj.type == "int"){
                    if (/^([1-9]\d*|[0]{1,1})$/.test(obj.currentParamValue)){
                        if (Number(obj.currentParamValue) < Number(obj.minSize) ||
                            Number(obj.currentParamValue)> Number(obj.maxSize)){
                            obj.currentParamValue = obj.defaultValue ;
                        }
                    }else{
                        obj.currentParamValue = obj.defaultValue ;
                    }
                }
            };
            RDSConfigService.updateCusConfigFile({
                "configGroupId":ConfigFile.configId,
                "editParams":angular.toJson(updatedParams)
                //"editParams":angular.toJson(showDetail.paramsList)
            }).then(function(data){
                if (SysCode.success == data.respCode){
                    toast.success('配置文件修改成功');
                    Table.api.draw();
                }
                if (SysCode.error == data.respCode){
                    if (data.message) {
                        var errMsg = data.message;
                        RDSConfigService.queryConfigParamsByGroupId(ConfigFile.configId).then(function(data){
                            showDetail.paramsList = data.data ;
                            eayunModal.warning(errMsg);
                        }) ;
                    }else {
                        toast.error('配置文件修改失败');
                    }
                    Table.api.draw();
                }
                showDetail.isNowEdit = false ;
            }) ;
        }
        /**
         * 判断当前编辑的配置文件是默认配置文件还是客户配置文件的类别
         * @type {boolean}
         */
        showDetail.isDefaultConfigFile = (ConfigFile.configType != '2') ;
        //需要显示的参数 ：配置文件类型名称
        showDetail.configType = (ConfigFile.configType == '2' ? '客户配置文件' : '默认配置文件') ;
        //需要显示的参数 ：配置文件名称信息
        showDetail.configName = ConfigFile.configName ;
        //表示当前是否正在编辑配置文件的标识
        showDetail.isNowEdit = false ;
        /**
         * 存储所有配置参数信息的数组集合
         * @type {Array}
         */
        showDetail.paramsList = [] ;
        /**
         * 查询对应当前配置文件的详细配置参数信息，目的为提供页面展示
         */
        RDSConfigService.queryConfigParamsByGroupId(ConfigFile.configId).then(function(data){
            showDetail.paramsList = data.data ;
        }) ;
        /**
         * 开始编辑，更改当前的状态为编辑状态
         */
        showDetail.editFunction = function(){
            showDetail.isNowEdit = true ;
        }
        /**
         * 保存所有编辑过的参数，用于编辑传给后台进行数据操作
         * @type {Array}
         */
        showDetail.allUpdatedParams = [] ;
    }])
    .controller('RdsShowDetailDefaultConfigCtrl', ['$scope','$rootScope','RDSConfigService','$modalInstance','ConfigFile','powerService', function ($scope,$rootScope, RDSConfigService,$modalInstance,ConfigFile,powerService) {
        var showDetailDefault = this ;
        /**
         * 关闭当前的弹出提示以及内容编辑框
         */
        showDetailDefault.cancel = function () {
            $modalInstance.dismiss();
        };
        /**
         * 判断当前编辑的配置文件是默认配置文件还是客户配置文件的类别
         * @type {boolean}
         */
        showDetailDefault.isDefaultConfigFile = (ConfigFile.configType != '2') ;
        //需要显示的参数 ：配置文件类型名称
        showDetailDefault.configType = (ConfigFile.configType == '2' ? '客户配置文件' : '默认配置文件') ;
        //需要显示的参数 ：配置文件名称信息
        showDetailDefault.configName = ConfigFile.configName ;
        /**
         * 存储所有配置参数信息的数组集合
         * @type {Array}
         */
        showDetailDefault.paramsList = [] ;
        /**
         * 查询对应当前配置文件的详细配置参数信息，目的为提供页面展示
         */
        RDSConfigService.queryConfigParamsByGroupId(ConfigFile.configId).then(function(data){
            showDetailDefault.paramsList = data.data ;
        }) ;
    }]);