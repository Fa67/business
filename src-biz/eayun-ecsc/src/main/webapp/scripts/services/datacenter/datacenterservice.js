/**
 * Created by eayun on 2016/8/27.
 */
'use strict';

angular.module('eayunApp.service')
    .service('DatacenterService', ['$q', 'eayunHttp', function ($q, eayunHttp) {
        var datacenter = this;
        /*获取所有数据中心和项目信息*/
        datacenter.getDcPrjList = function () {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vm/getDatacenterProjectList.do').then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };
        /*获取信用额度*/
        datacenter.getBuyCondition = function () {
            var deferred = $q.defer();
            eayunHttp.post('sysdatatree/getbuycondition.do', {}).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };
        /*获取当前账户余额*/
        datacenter.queryAccount = function () {
            var deferred = $q.defer();
            eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (response) {
                deferred.resolve(response.data.data.money);
            });
            return deferred.promise;
        };
        /*截取两位小数的数值*/
        datacenter.moneyFloor = function (_money) {
            return Math.floor(100 * _money) / 100;
        };
        /*toast框提示语的字符省略*/
        datacenter.toastEllipsis = function (_name, _index) {
            if (_name && _name.length > _index) {
                return _name.substring(0, _index) + '...';
            }
            return _name;
        };
    }]);