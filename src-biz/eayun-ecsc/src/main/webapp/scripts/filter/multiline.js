/**
 * Created by eayun on 2017/3/12.
 */
'use strict';

angular.module('eayunApp.filter')
    /*用于表格中，多行数据的展现*/
    .filter('multiline', [function () {
        return function (list, _showLength) {
            var length = angular.isNumber(_showLength) ? _showLength : 0;
            var ellipsis = list.length > length;
            length = ellipsis ? length : list.length;
            var html = '<span' + (ellipsis ? (' title="' + list + '"') : '') + '>';
            for (var i = 0; i < length; i++) {
                if (i < length - 1) {
                    html += '<span>' + list[i] + '</span><br/>';
                } else {
                    html += '<span>' + list[i] + (ellipsis ? '......' : '') + '</span>';
                }
            }
            html += '</span>';
            return html;
        };
    }]);