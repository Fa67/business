/**
 * Created by eayun on 2017/2/22.
 */
'use strict';

angular.module('eayunApp.controllers')
    .config(function () {

    })
    .controller('RdsBackupCtrl', function ($rootScope, $scope, $state, eayunModal, toast, RDSBackupService, powerService, $stateParams, eayunHttp) {

        var routeList = [];
        $rootScope.navList(routeList, '备份');

        powerService.powerRoutesList().then(function(powerList){
            $scope.modulePower = {
                rdsBackupView: powerService.isPower('rds_backup_view'),
                rdsBackupCreateInstance: powerService.isPower('rds_backup_createinstance'),
                rdsBackupDelete: powerService.isPower('rds_backup_delete')
            };
        });

        var bak = this;

        bak.backupCategoryList = [{
            'nodeNameEn':'-1',
            'nodeName':'分类'
        },{
            'nodeNameEn':'MANUAL',
            'nodeName':'手动备份'
        },{
            'nodeNameEn':'AUTO',
            'nodeName':'自动备份'
        }];

        bak.rdsVersionList = [{
            'nodeNameEn':'-1',
            'nodeName':'DB版本'
        },{
            'nodeNameEn':'5.5',
            'nodeName':'MySQL5.5'
        },{
            'nodeNameEn':'5.6',
            'nodeName':'MySQL5.6'
        }];

        $scope.checkUser = function (){
            var user = sessionStorage["userInfo"];
            if(user){
                user = JSON.parse(user);
                if(user&&user.userId){
                    return true;
                }
            }
            return false;
        };

        $scope.query ={};
        $scope.options = {
            searchFn: function () {
                if(!$scope.checkUser()){
                    return ;
                }
                bak.table.api.draw();
            },
            placeholder:"请输入名称搜索",
            select: [{backupName: '名称'}, {instanceName: '实例名称'}]
        };

        $scope.dcPrj = JSON.parse(sessionStorage["dcPrj"]);

        var instanceId = '';
        var category= '';
        if($stateParams.params!=''){
            var params = $stateParams.params.split(',');
            instanceId = params[0];
            category=params[1]=='0'?'MANUAL':'AUTO';
            $scope.query.category = category;
        }
        bak.table = {
            api: {},
            source: 'rds/backup/getBackups.do',
            getParams: function () {
                return {
                    datacenterId:  $scope.model.projectvoe ? $scope.model.projectvoe.dcId : '',
                    projectId:$scope.model.projectvoe ? $scope.model.projectvoe.projectId : '',
                    searchKey:$scope.search ? $scope.search.key :'',
                    searchValue:$scope.search ? $scope.search.value:'',
                    category:$scope.query ? $scope.query.category :'',
                    instanceId:instanceId,
                    versionName:$scope.query ? $scope.query.version :''
                };
            }
        };

        $scope.checkBackupStatus =function (model){
            if(model.status&&model.status=='COMPLETED'){
                return 'ey-square-right';
            }
            else if(model.status=='FAILED'){
                return 'ey-square-error';
            }
            else if(model.status=='NEW'|| model.status=='BUILDING'){
                return'ey-square-warning';
            }
        };

        $scope.selectBackupCategory = function(item,event){
            $scope.query.category = null;
            if(item.nodeNameEn != '-1'){
                $scope.query.category = item.nodeNameEn;
            }
        };

        $scope.selectRdsVersion = function (item, event) {
            $scope.query.version = null;
            if(item.nodeNameEn != '-1'){
                $scope.query.version = item.nodeNameEn;
            }
        };

        $scope.$watch('model.projectvoe', function(newVal,oldVal){
            if(newVal !== oldVal){
                bak.table.api.draw();
            }
        });

        $scope.$watch('query.category' , function(newVal,oldVal){
            if(newVal !== oldVal){
                bak.table.api.draw();
            }
        });

        $scope.$watch('query.version' , function(newVal,oldVal){
            if(newVal !== oldVal){
                bak.table.api.draw();
            }
        });

        $scope.jumpIntoInstance = function(_instanceId){
            $state.go('app.rds.detail.dbinstance',{"rdsId":_instanceId});
        };

        $scope.createInstanceByBackup = function(_bak){
            $state.go('buy.buyinstance',{orderNo:'000000', backupId : _bak.backupId});
        };

        $scope.deleteBackup = function(_bak){
            eayunModal.confirm('确认删除手动备份' + _bak.name + '？').then(function () {
                RDSBackupService.deleteBackup(_bak).then(function () {
                    toast.success('删除手动备份成功');
                    bak.table.api.draw();
                }, function () {
                    toast.success('删除手动备份成功');
                    bak.table.api.draw();
                });
            });
        };

        $scope.refresh = function () {
            var i, item;
            var middleStatue = ['NEW','BUILDING'];
            if(bak.table.result!=undefined){
                for (i = 0; i < bak.table.result.length; i++) {
                    item = bak.table.result[i];
                    if (middleStatue.indexOf(item.status.toUpperCase()) >= 0) {
                        bak.table.api.refresh();
                        break;
                    }
                }
            }

        };

    });