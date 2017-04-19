/**
 * Created by ZHL on 2016/9/29.
 */
angular.module('eayunApp.service')
    /**
     * @ngdoc eayunMath
     * @name eayunApp.eayunMath
     * @description
     * # eayunMath
     * 精确计算
     */
    .service('eayunMath', function () {
        var eayunMath = {},
            precision = 3;//精度为小数点后3位

        function checkNumber(arg) {
            return typeof arg === 'number';
        };

        eayunMath.plus = function (arg1, arg2) {
            if (checkNumber(arg1) && checkNumber(arg2)) {
                return Number((arg1 + arg2).toFixed(precision));
            } else {
                return NaN;
            }
        };

        eayunMath.sub = function (arg1, arg2) {
            return eayunMath.plus(arg1, 0 - arg2);
        };

        eayunMath.multi = function (arg1, arg2) {
            if (checkNumber(arg1) && checkNumber(arg2)) {
                return Number((arg1 * arg2).toFixed(precision));
            } else {
                return NaN;
            }
        };

        eayunMath.div = function (arg1, arg2) {
            if (checkNumber(arg1) && checkNumber(arg2)) {
                return Number((arg1 / arg2).toFixed(precision));
            } else {
                return NaN;
            }
        };

        return eayunMath;
    });