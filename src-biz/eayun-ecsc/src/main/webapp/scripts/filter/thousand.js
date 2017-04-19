/**
 * Created by eayun on 2016/9/13.
 */
'use strict';
angular.module('eayunApp.filter', [])
    /*数字千分位格式，并且保留自定义位数的小数，默认两位，不四舍五入*/
    .filter('thousand', [function () {
        return function (number, _payAfter, _index) {

            var handleService = {
                negative: function (num, dex) {
                    return api.concatSameChar('0.', '0', dex);
                },
                int: function (num) {
                    return api.listThousands(num);
                },
                decimal: function (num, after, dex) {
                    var resultDec = '';
                    /*后付费价格小数做不足0.01时按0.01的展示处理*/
                    if (after && (num > 0 && num < 0.01)) {
                        resultDec = '.01';
                    } else {
                        /*判断是否有小数部分*/
                        var dot = num.toString().indexOf('.');
                        if (dot != -1) {
                            resultDec = num.toString().substring(dot, dot + dex + 1);
                            /*判断实际小数长度是否不足要求展示的长度，不足则补零*/
                            var length = num.toString().length;
                            if (length < dot + dex + 1) {
                                resultDec = api.concatSameChar(resultDec, '0', dot + dex + 1 - length);
                            }
                        } else {
                            resultDec = api.concatSameChar('.', '0', dex);
                        }
                    }
                    return resultDec;
                }
            };

            var api = {
                /*千分位*/
                listThousands: function (num) {
                    var count = 0;
                    var list = [];
                    var int = Math.floor(num);
                    int = int.toString().split('');
                    for (var i = int.length - 1; i >= 0; i--) {
                        count++;
                        list.unshift(int[i]);
                        if (!(count % 3) && i != 0) {
                            list.unshift(',');
                        }
                    }
                    return list.join('');
                },
                /*补0*/
                concatSameChar: function (str, char, dex) {
                    var list = [];
                    for (var i = 0; i < dex; i++) {
                        list.push(char);
                    }
                    return str + list.join('');
                }
            };

            if (angular.isDefined(number) && number != null) {
                var index = _index || 2;
                /*小于零时展示零，不展示负数*/
                if (number <= 0) {
                    return handleService.negative(number, index);
                } else {
                    /*整数部分处理*/
                    var resultInt = handleService.int(number);
                    /*小数部分处理*/
                    var payAfter = _payAfter || false;
                    var resultDec = handleService.decimal(number, payAfter, index);
                    return resultInt + resultDec;
                }
            } else {
                return number;
            }
        }
    }]);