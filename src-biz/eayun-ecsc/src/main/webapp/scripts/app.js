'use strict';

/**
 * @ngdoc overview
 * @name eayunApp
 * @description
 * # eayunApp
 *
 * Main module of the application.
 */
angular
    .module('eayunApp', [
        'ngAnimate',
        'ngCookies',
        'ngResource',
        'ngRoute',
        'ngSanitize',
        'ui.router',
        'ui.bootstrap.modal',
        'ui.bootstrap.datepicker',
        'ui.bootstrap.dateparser',
        'ui.bootstrap.position',
        'ui.bootstrap.bindHtml',
        'ui.bootstrap.tooltip',
        'eayunApp.controllers',
        //'eayunApp.directive',
        'eayun.components',
        'eayunApp.service',
        'eayunApp.constant',
        'eayunApp.filter',
        'ngFileUpload'
    ])
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise('/error');
        $urlRouterProvider.when('', '/sys/login');
        //$urlRouterProvider.otherwise('/app');
        $stateProvider.state('app', {
            url: '/app',
            templateUrl: 'views/main.html',
            controller: 'MainCtrl'
        }).state('init', {
                url: '/init',
                templateUrl: 'views/initword.html',
                controller: 'InitCtrl'
            })
            .state('error', {
                url: '/error',
                templateUrl: 'views/sys/404.html'
            }).state('pay', {
                url: '/pay',
                controller: 'MainCtrl',
                templateUrl: 'views/paymain.html'
            }).state('buy', {
                url: '/buy',
                controller: 'MainCtrl',
                templateUrl: 'views/buymain.html'
            }).state('renew', {
                url: '/renew',
                controller: 'MainCtrl',
                templateUrl: 'views/buymain.html'
            }).state('tobuy', {
                url: '/tobuy',
                controller: 'MainCtrl',
                templateUrl: 'views/buymain.html'
            })
            ;
    })
    .run(['$rootScope', 'AuthState', '$state', '$log', 'powerService', function ($rootScope, AuthState, $state, $log, powerService) {
        $rootScope.$on('$stateChangeStart',
            function (event, toState, toParams, fromState, fromParams) {
                if (AuthState[toState.name]) {
                    powerService.powerRoutesList().then(function (authArray) {
                        var authMap = {};
                        angular.forEach(authArray, function (value) {
                            authMap[value] = true;
                        });
                        var auth = AuthState[toState.name].auth;
                        if (!authMap[auth]) {
                            event.preventDefault();
                            $state.go(AuthState[toState.name].jumpTarget);
                        }
                    });
                }
            })
    }])
    /*.run(['eayunHttp', 'ObsBasePath', function (eayunHttp, ObsBasePath) {
        eayunHttp.post('obs/storage/getEayunObsHost.do').then(function (response) {
            ObsBasePath = '.' + response.data;
        });
    }])*/
    .controller('InitCtrl', function ($scope, eayunHttp, $state, eayunModal) {
        var name = "";
        var userInfo = sessionStorage["userInfo"];
        if (userInfo) {
            var user = JSON.parse(userInfo);
            if (user && user.userId) {
                name = user.userName;
            }
        } else {
            $state.go("login", {}, {reload: true});
        }
        $scope.passKey = '';
        $scope.getPassKey = function () {
            eayunHttp.post("sys/login/getPassKey.do", {}).then(function (response) {
                $scope.passKey = response.data;
            });
        };
        $scope.getPassKey();
        $scope.pmodel = {
            userName: name,
            oldPassword: '',
            newPassword: '',
            secondPassword: ''
        };
        $scope.checkPassword = function (value) {	//校验旧密码
            value = strEnc(value, $scope.passKey, '', '');
            return eayunHttp.post('sys/user/checkOldPassword.do', {oldPassword: value}).then(function (response) {
                return response.data;
            });
        };
        $scope.NoSame = false;//新旧密码是否一致
        $scope.IsSame = false;//两次输入密码是否一致
        $scope.checkIsOld = function () {
            $scope.NoSame = $scope.pmodel.newPassword == $scope.pmodel.oldPassword;//新旧密码
            $scope.checkcommit = true;
        };
        $scope.checkNoSame = function () {
            $scope.NoSame = $scope.pmodel.newPassword == $scope.pmodel.oldPassword;//新旧密码
            $scope.IsSame = $scope.pmodel.secondPassword == $scope.pmodel.newPassword;//二次校验密码
        };
        $scope.checkIsSame = function () {
            $scope.IsSame = $scope.pmodel.secondPassword == $scope.pmodel.newPassword;//二次校验密码
        };
        $scope.cancel = function () {
            $state.go("login");
        };
        $scope.checkcommit = true;
        $scope.commit = function () {
            if ($scope.pmodel.oldPassword == $scope.pmodel.newPassword) {
                eayunModal.error("新旧密码不能相同，请更换密码");
                return;
            }
            if ($scope.pmodel.newPassword != $scope.pmodel.secondPassword) {
                eayunModal.error("两次输入密码不一致，请重新输入");
                return;
            }
            $scope.pmodel.oldPass = strEnc($scope.pmodel.oldPassword, $scope.passKey, '', '');
            $scope.pmodel.newPass = strEnc($scope.pmodel.newPassword, $scope.passKey, '', '');
            eayunHttp.post('sys/user/checkOldPassword.do', {oldPassword: $scope.pmodel.oldPass}).then(function (response) {
                $scope.checkcommit = response.data;
                if ($scope.checkcommit) {		//输入密码正确
                    eayunHttp.post('sys/user/modifyPassword.do', $scope.pmodel).then(function (response) {
                        if (!response.data.code) {
                            eayunModal.successalert("修改成功,请重新登录").then(function () {
                            	eayunHttp.post('sys/login/logout.do',{}).then(function(response){
                            		sessionStorage.clear();
    				        		$state.go("login");
        		        		});
                            }, function () {
                            	eayunHttp.post('sys/login/logout.do',{}).then(function(response){
                            		sessionStorage.clear();
    				        		$state.go("login");
        		        		});
                            });
                        }
                    });
                }
            });
        };
    });

