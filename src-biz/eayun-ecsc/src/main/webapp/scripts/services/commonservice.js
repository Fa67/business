'use strict';
//var basePath = "http://localhost:8080/eayun-eayun/";
var basePath = "";
/**
 * @ngdoc service
 * @name eayunApp.commonservice
 * @description
 * # commonservice
 * eayun公共服务
 */
angular.module('eayunApp.service', [])
    /**
     * @ngdoc service
     * @name eayunApp.authHttp
     * @description
     * # authHttp
     * Ajax服务
     */
    .factory('eayunHttp', function ($http, $cookieStore, eayunModal, toast) {
        var authHttp = {};

        // HTTP请求头
        /*
         * 改为使用AuthHandler
        var extendHeaders = function (config) {
            config.headers = config.headers || {};
            config.headers.Authorization = "angularJS";
            $cookieStore.put('$cookieStore', '$cookieStore');
        };
        */

        // GET DELETE HEAD JSONE 请求
        angular.forEach(['get', 'delete', 'head', 'jsonp'], function (name) {
            authHttp[name] = function (url, config) {
                config = config || {};
                //extendHeaders(config);
                return $http[name](basePath + url, config).then(function (response) {
                    if (response.data && response.data.code && response.data.type != 'CheckSessionFilter') {
                        eayunModal.warning(response.data.message);
                    }
                    return response;
                });
            };
        });

        //POST PUT 请求
        angular.forEach(['post', 'put'], function (name) {
            authHttp[name] = function (url, data, config) {
                config = config || {};
                //extendHeaders(config);
                return $http[name](basePath + url, data, config).then(function (response) {
                    if (response.data && response.data.code && response.data.type != 'CheckSessionFilter') {
                        eayunModal.warning(response.data.message);
                    }
                    return response;
                });
            };
        });

        return authHttp;
    })
    .factory('powerService', ['eayunHttp', '$q', '$injector', '$window', function (eayunHttp, $q, $window) {
        var getUserInfo = function () {
            var userInfo = sessionStorage["userInfo"];
            if (userInfo) {
                var user = JSON.parse(userInfo);
                if (user && user.userId) {
                    return user;
                }
            }
            return '';
        };

        var source = "sys/power/getRoutesByRole.do";
        var powerService = {};
        powerService.query = function () {
            if (getUserInfo() != '') {
                return eayunHttp.post(source, {roleId: getUserInfo().roleId}).then(function (response) {
                    powerService.powerList = response.data;
                    return powerService.powerList;
                });
            }
            return [];
        };
        powerService.getPowerList = function () {
            var deferred = $q.defer();
            var promise = deferred.promise;
//        promise.then(function (list) {
//        	return list;
//        });
            deferred.resolve(powerService.powerList || powerService.query());
            return promise;
        };
        powerService.isPower = function (routeName) {
            var ishave = false;
            angular.forEach(powerService.powerList, function (value, key) {
                if (value == routeName) {
                    ishave = true;
                }
            });
            return ishave;
        };
        powerService.isAdmin = function () {
            return getUserInfo().isAdmin;
        };
        powerService.clear = function () {
            powerService.powerList = '';
        };
        return {
            powerRoutesList: powerService.getPowerList,
            isPower: powerService.isPower,
            isAdmin: powerService.isAdmin,
            clear: powerService.clear
        };
    }])
    .factory('httpInterceptor', ['$q', '$injector','$timeout', function ($q, $injector,$timeout) {
        var version = Math.random();
        var count = 0;
        var interceptor = {
            'request': function (config) {
                /*var unFilterUrl = [
                 'views/directives/alert.html',
                 'template/modal/backdrop.html',
                 'template/modal/window.html',
                 'views/sys/top.html',
                 'views/sys/bottom.html',
                 'views/sys/login/login.html',
                 'views/sys/register/register.html',
                 'views/sys/register/registersuccess.html',
                 'views/sys/forgotcode/forgotcode.html',
                 'sys/login/login.do',
                 'sys/login/logout.do',
                 'sys/login/getPassKey.do',
                 'sys/register/getTeleCode.do',
                 'sys/register/checkCondition.do',
                 'sys/register/register.do',
                 'sys/forgotcode/firstCheck.do',
                 'sys/forgotcode/secondCheck.do',
                 'sys/forgotcode/modifyPassword.do',
                 'sys/forgotcode/getTeleCode.do',
                 'sys/user/validMail/',
                 'obs/storage/getEayunObsHost.do',
                 'index.html',
                 'views/sys/404.html'
                 ].join();
                 if (!getUserInfo() && unFilterUrl.indexOf(config.url) == -1) {
                 sessionStorage.clear();
                 var $state = $injector.get('$state');
                 location.href = "";
                 return '';
                 }*/

                if (config.url.indexOf('components/') === 0) {
                    return config;
                }

                if (config.url.indexOf('&v=') == -1 && config.url.indexOf('?v=') == -1) {
                    if (config.url.indexOf('?') == -1) {
                        config.url = config.url + "?v=" + version;
                    } else {
                        config.url = config.url + "&v=" + version;
                    }
                }

                return config;
            },
            'response': function (response) {
                var respData = response.data;
                if (respData) {
                    if (respData.code == "999999") {
                        sessionStorage.clear();
                        location.href = "";
//    			var $state = $injector.get('$state');
//    			$state.go("login");
                    }else if(respData.code == "666666"){//账户冻结，alert 然后跳转登录页
                    	sessionStorage.clear();
                    	var $state = $injector.get('$state');
                    	if (count === 0) {
                            count = count + 1;
                            var eayunModal = $injector.get('eayunModal');
                            var eayunHttp = $injector.get('eayunHttp');
                            //做延时处理，清除所有的弹出框，再给出需要的提示框
                            $timeout(function(){
                            	eayunModal.dismissAll();
                            	eayunModal.error('您的账户已冻结，请联系管理员！').then(function () {
                                	count = count - 1;
                                	eayunHttp.post('sys/login/logout.do',{}).then(function(response){
                                		if(response.data&&response.data.respCode=='400000'){
                                			sessionStorage.clear();
                                			$state.go("login");
                                		}
                                    });
                                }, function () {
                                	count = count - 1;
                                	eayunHttp.post('sys/login/logout.do',{}).then(function(response){
                                		if(response.data&&response.data.respCode=='400000'){
                                			sessionStorage.clear();
                                			$state.go("login");
                                		}
                                    });
                                });
                            },500);
                            
                        }

                    }
                }
                return response;
            },
            'requestError': function (rejection) {
                return rejection;
            },
            'responseError': function (response) {
                return $q.reject(response);
            }
        };

        var getUserInfo = function () {
            var userInfo = sessionStorage["userInfo"];
            if (userInfo) {
                var user = JSON.parse(userInfo);
                if (user && user.userId) {
                    return user;
                }
            }
            return '';
        };

        return interceptor;
    }])
    .factory('eayunStorage', ['$log',function ($log) {
        var eayunStorage = {},
            storage = {};
        eayunStorage.set = function (key, value) {
            storage[key] = value;
        };
        eayunStorage.get = function (key) {
            return storage[key];
        };
        eayunStorage.delete = function (key) {
            delete storage[key];
            delete sessionStorage[key];
        };
        eayunStorage.persist = function (key, value) {
            if (value === undefined) {
                try {
                    return JSON.parse(sessionStorage[key]);
                } catch (e) {
                    $log.log(e);
                    return undefined;
                }

            } else {
                sessionStorage[key] = JSON.stringify(value);
            }
        };
        return eayunStorage;
    }]);

