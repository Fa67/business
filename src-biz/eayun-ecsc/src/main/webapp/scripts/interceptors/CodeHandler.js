/**
 * Created by ZHL on 2016/4/6.
 */
'use strict';

angular.module('eayunApp.service').factory('CodeHandler', ['$q', '$injector',
    function ($q, $injector) {
        var CodeHandler = {};
        CodeHandler.response = function (response) {
            if (response.data && response.data.code && response.data.type != 'CheckSessionFilter') {
                var eayunModal = $injector.get('eayunModal');
                eayunModal.error(response.data.message);
                return $q.reject(response);
            } else {
                return $q.resolve(response);
            }
        };
        return CodeHandler;
    }]);