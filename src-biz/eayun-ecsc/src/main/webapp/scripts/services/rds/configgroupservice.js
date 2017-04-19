'use strict';
/**
 * 配置组管理专用服务Service
 */
angular.module('eayunApp.service')
    .service('RDSConfigService', ['eayunHttp', '$q', 'SysCode','$http', function (eayunHttp, $q, SysCode,$http) {
        var con = this ;
        con.getAllDBVersion = function () {
            return eayunHttp.post('rds/config/getAllDatabaseVersion.do',{})
                .then(function (response) {
                    return response.data;
            });
        };
        con.queryConfigFileByVersion = function(postParam){
            return eayunHttp.post('rds/config/queryConfigFileForPage.do',postParam)
                .then(function (response) {
                    return response.data;
                });
        }
        con.createConfigFile = function(postParam){
            delete postParam.groupName ;
            return eayunHttp.post('rds/config/createCusConfigFile.do',postParam)
                .then(function (response) {
                    return response.data;
                });
        }
        con.deleteConfigFile = function(configId){
            return eayunHttp.post('rds/config/deleteCusConfigFile.do',{
                "configId":configId
            }).then(function (response) {
                return response.data;
            });
        }
        con.deleteConfigFile = function(groupId){
            return eayunHttp.post('rds/config/deleteCusConfigFile.do',{
                "groupId":groupId
            }).then(function (response) {
                return response.data;
            });
        }
        con.checkConfigCanBeDelete = function(dcId,projectId,groupId){
            return eayunHttp.post('rds/config/queryConfigurationGroupInstances.do',{
                "dcId":dcId,
                "projectId":projectId,
                "groupId":groupId
            }).then(function (response) {
                return response.data;
            });
        }
        con.queryConfigParamsByGroupId = function(groupId){
            return eayunHttp.post('rds/config/queryConfigParamsByGroupId.do',{
                "groupId":groupId
            }).then(function (response) {
                return response.data;
            });
        }
        con.existConfigFile = function(groupId){
            return eayunHttp.post('rds/config/existConfigFile.do',{
                "groupId":groupId
            }).then(function (response) {
                return response.data;
            });
        }
        /**
         * 更新客户配置文件
         * @param groupId
         * @returns {*}
         */
        con.updateCusConfigFile = function(postParams){
            return eayunHttp.post('rds/config/updateCusConfigFile.do',postParams).then(function (response) {
                return response.data;
            });
        }
        /**
         * 检查同一数据中心，同一项目下是否已经存在同名的配置文件基本信息了。
         * @param postParams
         * @returns {*}
         */
        con.queryCusSelfConfigFileByFilename = function(dcId,projectId,newFileName){
            return eayunHttp.post('rds/config/queryCusSelfConfigFileByFilename.do',{
                dcId:dcId,
                projectId:projectId,
                newFileName:newFileName
            }).then(function (response) {
                return response.data;
            });
        }

    }])
    .filter("reverseName",function(){
        return function (input){
            if (input == 'mysql5.5'){
                return 'MySQL5.5' ;
            }
            if (input == 'mysql5.6'){
                return 'MySQL5.6' ;
            }
        }
    })
    .filter("showInfo",function(){
        return function (input){
            if (input.length<=20){
                return input ;
            }else {
                return input.substring(0,20) + "......" ;
            }
        }
    })
    .filter("showInfoSp",function(){
        return function (input){
            if (input){
                var originShowValue = "" ;
                var infos = input.split(";");
                for (var i = 0 ; i<infos.length;i++){
                    if (i > 4){
                        originShowValue += "<span>......</span><br/>" ;
                        break ;
                    }else {
                        originShowValue += "<span>"+infos[i]+"</span><br/>" ;
                    }
                }
                return originShowValue.substring(0,originShowValue.lastIndexOf("<br/>")) ;
            }else {
                return "" ;
            }
        }
    });