'use strict';

angular.module('eayunApp')
    .config(['$httpProvider', function ($httpProvider) {
        $httpProvider.defaults.headers.common.Accept = 'application/json, text/plain,*/*';
    }])
    .config(['$httpProvider', function ($httpProvider) {
        //$httpProvider.interceptors.push('SubmitHandler');
        $httpProvider.interceptors.push('httpInterceptor');
        $httpProvider.interceptors.push('AuthHandler');
        $httpProvider.interceptors.push('LoadingHandler');
    }]);
