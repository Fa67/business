/**
 * Created by ZHL on 2016/4/6.
 */
'use strict';

angular.module('eayunApp.service')
    .factory('LoadingHandler', function LoadingHandlerFactory($rootScope, $q) {
        var LoadingHandler = {};
        var ref = 0;

        var progress = function (ref) {
            $rootScope.$broadcast('loading.level', ref);
        };
        var start = function () {
            $rootScope.$broadcast('loading.begin');
        };
        var stop = function () {
            $rootScope.$broadcast('loading.end');
        };
        var incRef = function () {
            ++ref;
            if (ref === 1) {
                start();
            }
            progress(ref);
        };

        var decRef = function () {
            --ref;
            progress(ref);
            if (ref === 0) {
                stop();
            }
        };

        LoadingHandler.request = function (config) {
            if (config.data && config.data.$showLoading) {
                config.$showLoading = true;
                delete config.data.$showLoading;
            }
            if (config.params && config.params.$showLoading) {
                config.$showLoading = true;
                delete config.params.$showLoading;
            }
            if (config.$showLoading) {
                incRef();
            }
            return $q.when(config);
        };

        LoadingHandler.response = function (resp) {
            if (resp.config && resp.config.$showLoading) {
                decRef();
            }
            return $q.when(resp);
        };

        LoadingHandler.responseError = function (rejection) {
            if (rejection && rejection.config && rejection.config.$showLoading) {
                decRef();
            }
            return $q.reject(rejection);
        };
        return LoadingHandler;
    });
