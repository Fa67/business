/**
 * Created by ZHL on 2016/4/6.
 */
'use strict';

angular.module('eayunApp.service')
    .factory('SubmitHandler', ['$q', '$log', function ($q, $log) {
        var SubmitHandler = {};
        var map = {};

        SubmitHandler.request = function (config) {
            if (config.url.indexOf('.do', config.url.length - 3) !== -1) {
                var time = map[config.url];
                var now = (new Date()).getTime();
                if (time && (now - time) < 100) {
                    $log.warn('重复提交URL:' + config.url);
                    return $q.reject(config);
                } else {
                    map[config.url] = now;
                }
            }
            return $q.when(config);
        };

        return SubmitHandler;
    }]);
