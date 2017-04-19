/**
 * Created by ZHL on 2016/4/6.
 */
'use strict';

angular.module('eayunApp.service')
    .factory('AuthHandler', function LoadingHandlerFactory($rootScope, $q) {
        var AuthHandler = {};

        AuthHandler.request = function (config) {
            config.headers = config.headers || {};
            if(!config.headers.Authorization){
            	config.headers.Authorization = "angularJS";
            }
            return $q.when(config);
        };

        return AuthHandler;
    });
