/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';

angular.module('eayun.components', [
        'ui.bootstrap.modal',
        'ui.bootstrap.datepicker',
        'ui.bootstrap.dateparser',
        'ui.bootstrap.position',
        'ui.bootstrap.bindHtml',
        'ui.bootstrap.tooltip'
    ])
    .config(function ($provide) {
        $provide.value("$locale", {
            "DATETIME_FORMATS": {
                "AMPMS": [
                    "AM",
                    "PM"
                ],
                "DAY": [
                    "周日",
                    "周一",
                    "周二",
                    "周三",
                    "周四",
                    "周五",
                    "周六"
                ],
                "ERANAMES": [
                    "Before Christ",
                    "Anno Domini"
                ],
                "ERAS": [
                    "BC",
                    "AD"
                ],
                "FIRSTDAYOFWEEK": 6,
                "MONTH": [
                    "1月",
                    "2月",
                    "3月",
                    "4月",
                    "5月",
                    "6月",
                    "7月",
                    "8月",
                    "9月",
                    "10月",
                    "11月",
                    "12月"
                ],
                "SHORTDAY": [
                    "Sun",
                    "Mon",
                    "Tue",
                    "Wed",
                    "Thu",
                    "Fri",
                    "Sat"
                ],
                "SHORTMONTH": [
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
                ],
                "WEEKENDRANGE": [
                    5,
                    6
                ],
                "fullDate": "EEEE, MMMM d, y",
                "longDate": "MMMM d, y",
                "medium": "MMM d, y h:mm:ss a",
                "mediumDate": "MMM d, y",
                "mediumTime": "h:mm:ss a",
                "short": "M/d/yy h:mm a",
                "shortDate": "M/d/yy",
                "shortTime": "h:mm a"
            },
            "NUMBER_FORMATS": {
                "CURRENCY_SYM": "$",
                "DECIMAL_SEP": ".",
                "GROUP_SEP": ",",
                "PATTERNS": [
                    {
                        "gSize": 3,
                        "lgSize": 3,
                        "maxFrac": 3,
                        "minFrac": 0,
                        "minInt": 1,
                        "negPre": "-",
                        "negSuf": "",
                        "posPre": "",
                        "posSuf": ""
                    },
                    {
                        "gSize": 3,
                        "lgSize": 3,
                        "maxFrac": 2,
                        "minFrac": 2,
                        "minInt": 1,
                        "negPre": "-\u00a4",
                        "negSuf": "",
                        "posPre": "\u00a4",
                        "posSuf": ""
                    }
                ]
            },
            "id": "en-us",
            "pluralCat": function (n, opt_precision) {
                var i = n | 0;
                var vf = getVF(n, opt_precision);
                if (i == 1 && vf.v == 0) {
                    return PLURAL_CATEGORY.ONE;
                }
                return PLURAL_CATEGORY.OTHER;
            }
        });
    });
'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:ajaxValid
     * @description
     * # ajaxValid
     * ajax校验
     */
    .directive('ajaxValid', ['$q', '$log', function ($q, $log) {
        return {
            require: '?ngModel',
            scope: {
                ajaxValid: '&'
            },
            link: function postLink(scope, element, attrs, ngModel) {
                if (!ngModel) {
                    $log.error('ajaxValid 缺少 ngModel 指令');
                    return;
                }
                ngModel.$asyncValidators.ajaxValid = function (modelValue, viewValue) {
                    return $q.when(scope.ajaxValid({value: viewValue})).then(function (result) {
                        if (!result)
                            return $q.reject('');
                        return true;
                    });
                };
            }
        };
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';

angular.module('eayun.components')
    .directive('eayunBanner', [function () {
        return {
            restrict: 'EA',
            controller: 'BannerCtrl',
            require: ['eayunBanner'],
            link: function postLink(scope, element, attrs, ctrls) {
                var banner = ctrls[0];
                banner.init(attrs.eayunBanner, attrs.name, attrs.interval);
            }
        }
    }])
    .controller('BannerCtrl', ['$scope', '$parse', '$timeout', 'SingleSelection',
        function ($scope, $parse, $timeout, SingleSelection) {
            var ctrl = this,
                watch,
                timer,
                single,
                interval;

            function flush() {
                ctrl.next();
                timer = $timeout(flush, interval);
            };

            ctrl.init = function (dataField, name, _interval) {
                name = name || 'banner';
                interval = $parse(_interval)($scope) || 3000;
                single = SingleSelection.getSingle();
                watch = $scope.$watch(dataField, function (value) {
                        single.init(value);
                }, true);
                $scope[name] = angular.extend($scope[name] || {}, ctrl);
                ctrl.start();
                $scope.$on('$destroy', function () {
                    watch();
                    $timeout.cancel(timer);
                });
            };
            ctrl.select = function (index) {
                single.select(index);
            };
            ctrl.next = function () {
                single.next();
            };
            ctrl.back = function () {
                single.back();
            };
            ctrl.stop = function () {
                $timeout.cancel(timer);
            };
            ctrl.start = function () {
                timer = $timeout(flush, interval);
            };
        }])
    .factory('SingleSelection', ['$log', function ($log) {
        function SingleSelection() {
            var single = this,
                activeData = {$$selected: false, $$index: -1},
                listData;

            single.init = function (_listData) {
                if (!angular.isArray(_listData)) {
                    $log.log("SingleSelection: listData必须为数组对象");
                    $log.debug(_listData);
                    return;
                }

                if (_listData.length > 0) {
                    listData = _listData;
                    single.select(0);
                    angular.forEach(listData, function (item, index) {
                        if (item.$$select) {
                            single.select(index);
                        }
                    });
                }
                return single;
            };
            single.select = function (index) {
                if (index >= 0 && index < listData.length) {
                    activeData.$$selected = false;
                    activeData = listData[index];
                    activeData.$$selected = true;
                    activeData.$$index = index;
                }
            };
            single.next = function () {
                var index = (activeData.$$index + 1) % listData.length;
                single.select((index + listData.length) % listData.length);
            };
            single.back = function () {
                var index = (activeData.$$index - 1) % listData.length;
                single.select((index + listData.length) % listData.length);
            };
        };

        return {
            getSingle: function () {
                return new SingleSelection();
            }
        };
    }]);
/**
 * Created by ZHL on 2016/3/30.
 */
'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    .directive('echarts', [function () {
        var id = 0;
        return {
            restrict: 'A',
            scope: {
                echarts: '='
            },
            link: function postLink(scope, element, attrs) {
                id++;
                $(element).attr('id', 'echarts_' + id);
                scope.echarts = echarts.init(document.getElementById('echarts_' + id));
            }
        }
    }]);
'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:dateTimePicker
     * @description
     * # dateTimePicker
     * 日期选择组件
     */
    .directive('dateTimePicker', ['$document', function ($document) {
        return {
            templateUrl: 'components/datepicker/datetimepicker.html',
            restrict: 'EA',
            replace: true,
            controller: 'dateTimePickerCtrl',
            scope: {
                datepickerMode: '@',
                minMode: '@',
                dateDisabled: '=',
                customClass: '&',
                shortcutPropagation: '&?',
                format: '@',
                minDate: '=',
                maxDate: '='
            },
            require: '?ngModel',
            link: function postLink(scope, element, attrs, ngModel) {
                scope.showTime = angular.isDefined(attrs.showTime) ? scope.$parent.$eval(attrs.showTime) : false;
                scope.minMode = scope.minMode || 'day';
                scope.datepickerMode = scope.datepickerMode || 'day';
                scope.require = !!attrs.required;
                if (!scope.format) {
                    scope.format = scope.showTime ? 'yyyy-MM-dd HH:mm:ss' : 'yyyy-MM-dd';
                }
                if (ngModel) {
                    ngModel.$render = function () {
                        scope.value = ngModel.$modelValue;
                    };
                    scope.$watch('value', function () {
                        ngModel.$setViewValue(scope.value);
                    });
                }
                if (angular.isDefined(attrs.init)) {
                    var init = scope.$parent.$eval(attrs.init) || [0, 0, 0, 0, 0, 0];
                    var date = new Date();
                    scope.showTime || date.setHours(0, 0, 0, 0);
                    date.setFullYear((init[0] || 0) + date.getFullYear(), (init[1] || 0) + date.getMonth(), (init[2] || 0) + date.getDate());
                    date.setHours((init[3] || 0) + date.getHours(), (init[4] || 0) + date.getMinutes(), (init[5] || 0) + date.getSeconds(), 0);
                    scope.value = date;
                    ngModel && ngModel.$setViewValue(date);
                }
                scope.close = function () {
                    element.removeClass('open');
                };
            }
        }
    }])
    .controller('dateTimePickerCtrl', ['$scope', '$timeout', function ($scope, $timeout) {
        $scope.status = {
            opened: false
        };
        $scope.today = function () {
            var current = new Date();
            if (!$scope.showTime)
                current.setHours(0, 0, 0, 0);
            $scope.value = current;
            $scope.toggle();
        };

        $scope.ok = function () {
            if (!$scope.value) {
                $scope.today();
            }
            $scope.close();
        };

        $scope.reset = function () {
            var current = new Date($scope.value);
            current.setHours(0, 0, 0, 0);
            $scope.value = current;
            $timeout(function () {
                $scope.value = angular.noop();
            }, 20);
            $scope.toggle();
        };
    }]);
/**
 * Created by ZHL on 2016/8/8.
 */
'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive
     * @description
     * 时间范围选择控件
     */
    .directive('eayunTimeFrame', [function () {
        return {
            templateUrl: 'components/datepicker/datetimepicker.html',
            restrict: 'EA',
            replace: true,
            controller: function ($scope) {
                $scope.status = {
                    opened: false
                };
                $scope.open = function () {
                    $scope.status.opened = true;
                };
                $scope.dateOptions = {
                    formatYear: 'yy',
                    startingDay: 1
                };
            },
            controllerAs: 'timeFrame',
            scope: {
                datepickerMode: '=?',
                dateDisabled: '=',
                customClass: '&',
                shortcutPropagation: '&?',
                format: '@',
                minDate: '=',
                maxDate: '='
            },
            require: '?ngModel',
            link: function postLink(scope, element, attrs, ngModel) {
                scope.showTime = angular.isDefined(attrs.showTime) ? scope.$parent.$eval(attrs.showTime) : false;
                if (!scope.format) {
                    scope.format = scope.showTime ? 'yyyy-MM-dd HH:mm:ss' : 'yyyy-MM-dd';
                }
                if (ngModel) {
                    ngModel.$render = function () {
                        scope.value = ngModel.$modelValue;
                    };
                    scope.$watch('value', function () {
                        ngModel.$setViewValue(scope.value);
                    });
                }
                if (angular.isDefined(attrs.init)) {
                    var init = scope.$parent.$eval(attrs.init) || [0, 0, 0, 0, 0, 0];
                    var date = new Date();
                    scope.showTime || date.setHours(0, 0, 0, 0);
                    date.setFullYear((init[0] || 0) + date.getFullYear(), (init[1] || 0) + date.getMonth(), (init[2] || 0) + date.getDate());
                    date.setHours((init[3] || 0) + date.getHours(), (init[4] || 0) + date.getMinutes(), (init[5] || 0) + date.getSeconds(), 0);
                    scope.value = date;
                    ngModel && ngModel.$setViewValue(date);
                }
            }
        };
    }]);
'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * ҳ�����ģ��
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:hint
     * @description
     * # hint
     * ð�ݿ�
     */
    .directive('hint', ['$templateRequest', '$compile', function ($templateRequest, $compile) {
        return {
            restrict: 'A',
            link: function postLink(scope, element, attrs) {
                $templateRequest(attrs.hint).then(function (template) {
                    var init = function () {
                        $(element).tooltip({
                            placement: attrs.tooltipPlacement || 'bottom',
                            html: true,
                            trigger: 'none',
                            title: $compile(template)(scope),
                            template: '<div class="tooltip ' + attrs.tooltipClass + '" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
                        });
                    };
                    var distroy = function () {
                        $(element).tooltip('destroy');
                    };
                    init();
                    scope.$watch(attrs.hintShow, function (show) {
                        init();
                        show ? $(element).tooltip('show') : distroy();
                    });
                });
            }
        }
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';

angular.module('eayun.components')
    .directive('accordion', [function () {
        return {
            restrict: 'EA',
            replace: true,
            transclude: true,
            template: '<div ng-transclude></div>',
            controller: function () {
                var vm = this;
                var api = {
                    expanders: [],

                    closeExpander: function (expander) {
                        $(expander.target).collapse('hide');
                    }
                };

                vm.addExpander = function (expander) {
                    api.expanders.push(expander);
                };

                vm.accordionOthers = function (selectedExpander) {
                    angular.forEach(api.expanders, function (expander) {
                        if (expander != selectedExpander) {
                            api.closeExpander(expander);
                            expander.active = false;
                        }
                    });
                };
            }
        }
    }])
    /**
     * @ngdoc directive
     * @name eayunApp.directive:expander
     * @description
     * # expander
     * 显示隐藏组件
     */
    .directive('expander', [function () {
        return {
            templateUrl: 'components/expander/expander.html',
            restrict: 'EA',
            replace: true,
            transclude: true,
            scope: {
                target: '@'
            },
            require: '^?accordion',
            link: function postLink(scope, element, attrs, accordionCtrl) {
                var target = scope.target;
                scope.active = false;
                scope.show = angular.isDefined(attrs.expanderShow) && attrs.expanderShow == 'true';
                $(target).addClass("collapse");
                if (accordionCtrl != null) {
                    accordionCtrl.addExpander(scope);
                }
                if (scope.show) {
                    $(target).addClass("in");
                    scope.active = true;
                }

                element.click(function () {
                    scope.active = !scope.active;
                    if (accordionCtrl != null) {
                        accordionCtrl.accordionOthers(scope);
                    }
                });
            }
        };
    }])
    .directive('eayunToggle', ['$document', function ($document) {
        return {
            restrict: 'A',
            link: function postLink(scope, element, attrs) {
                var click = false;//用于document判断是否本节点冒泡的点击事件
                var hasClass = element.hasClass(scope.eayunToggle);
                element.click(function () {
                    element.toggleClass(attrs.eayunToggle);
                    click = true;
                    hasClass = !hasClass;
                    if (hasClass) {
                        $document.bind('click', documentClickBind);
                    } else {
                        $document.unbind('click', documentClickBind);
                    }
                });

                function documentClickBind() {
                    if (!click) {
                        hasClass = false;
                        element.removeClass(attrs.eayunToggle);
                        $document.unbind('click', documentClickBind);
                    }
                    click = false;
                };

                scope.toggle = function () {
                    element.click();
                };

                scope.$on('$destroy', function () {
                    $document.unbind('click', documentClickBind);
                });
            }
        };
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:progress
     * @description
     * 添加输入框组件
     */
    .directive('inputAdd', ['$document', function ($document) {
        return {
            templateUrl: 'components/inputadd/inputadd.html',
            restrict: 'EA',
            replace: true,
            scope: {
                maxLength: '=',
                items: '=',
                placeholder: '=',
                pattern: '=',
                className:'=',
                onFocus: '&',
                onBlur: '&',
                name: '='
            },
            controller: ['$scope', function ($scope) {

                if (!$scope.items) {
                    $scope.items = [];
                }

                $scope.focus = function () {
                    $scope.onFocus(
                        {}
                    );
                };

                $scope.blur = function () {
                    $scope.onBlur(
                        {}
                    );
                };

                $scope.modify = function (index) {
                    if (index == 0) {
                        if ($scope.items.length > $scope.maxLength) return;
                        $scope.items.push('');
                    } else {
                        $scope.items.splice(index, 1);
                    }
                };
            }]
        };
    }]);


/**
 * Created by ZHL on 2016/5/4.
 */
'use strict';

angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunInputFilter
     * @description
     * # eayunInputFilter
     * 输入框过滤
     */
    .directive('eayunInputFilter', ['$document', '$timeout', function ($document, $timeout) {
        return {
            templateUrl: 'components/input/inputFilter.html',
            restrict: 'EA',
            replace: true,
            scope: {
                placeholder: '@',
                filterData: '=',
                maxlength:'=',
                textField: '@'
            },
            require: 'ngModel',
            link: function postLink(scope, element, attrs, ngModelCtrl) {
                var map;
                function getMap() {
                    var map = {};
                    angular.forEach(scope.filterData, function (option) {
                        map[option[scope.textField]] = option;
                    });
                    return map;
                };
                function getKeys() {
                    var i;
                    scope.options = Object.keys(map);
                    scope.keys = scope.$eval('options | filter:(text===null?undefined:text)');
                    for (i = 0; i < scope.keys.length; i++) {
                        if(map[scope.keys[i]].$$selected){
                            scope.keys.splice(i,1);
                            break;
                        }
                    }
                };
                function init() {
                    map = getMap();
                    getKeys();
                };
                init();

                ngModelCtrl.$render = function () {
                    scope.text = ngModelCtrl.$modelValue;
                };
                scope.$watch('text', function (value) {
                    ngModelCtrl.$setViewValue(value);
                    getKeys();
                });
                scope.$watch('filterData', function (value) {
                    init();
                });
                scope.select = function (key, $event) {
                    scope.showMenu = false;
                    scope.text = key;
                    $event.stopPropagation();
                };
                scope.open = function () {
                    scope.showMenu = scope.disabled ? false : true;
                };
                attrs.$observe('disabled', function (value) {
                    scope.disabled = !!value;
                });
                var documentClickBind = function () {
                    scope.$apply(function () {
                        scope.showMenu = false;
                    });
                };
                scope.$watch('showMenu', function (value) {
                    if (value) {
                        $timeout(function () {
                            $document.bind('click', documentClickBind);
                        }, 0, false);
                    } else {
                        $document.unbind('click', documentClickBind);
                    }
                });
            }
        }
    }]);
'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:inputCheck
     * @description
     * # inputCheck
     * 输入框在体现view值前校验的组件
     */
    .directive('inputCheck', [function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                check: '&inputCheck'
            },
            link: function postLink(scope, element, attrs, ngModel) {
                ngModel.$parsers.unshift(
                    function (newValue) {
                        var value = scope.check({value: ngModel.$modelValue, newValue: newValue});
                        ngModel.$setViewValue(value);
                        element.val(value);
                        return value;
                    }
                );
            }
        }
    }]);
/**
 * Created by eayun on 2016/4/24.
 */
'use strict'

angular.module('eayun.components')
    .directive('eayunInputSelect', ['$timeout', '$document', function ($timeout, $document) {
        return {
            templateUrl: 'components/inputselect/inputselect.html',
            restrict: 'EA',
            replace: true,
            transclude: true,
            controller: function ($scope) {
                $scope.options = new Array();
                $scope.open = function () {
                    $scope.showMenu = !$scope.showMenu;
                };
                var curOption = null;
                var ngModelCtrl = '';
                var api = {
                    select: function (_option) {
                        curOption && (curOption.selected = false);
                        curOption = _option;
                        curOption.selected = true;
                        $scope.text = curOption.text;
                        $scope.showMenu = false;
                    },
                    addOption: function (_text, _value, _selected) {
                        var option = {
                            text: _text,
                            value: _value,
                            selected: _selected
                        };
                        if (ngModelCtrl && curOption && (option.value == curOption.value)) {
                            option.selected = true;
                        }
                        $scope.options.push(option);
                        option.selected && this.select(option);
                        return option;
                    },
                    getOptionByValue: function (_value) {
                        if (typeof _value === 'undefined') {
                            return {};
                        }
                        var option = null;
                        angular.forEach($scope.options, function (_option) {
                            if (_option.value == _value) {
                                option = _option;
                            }
                        });
                        return option || {value: null};
                    },
                    init: function (_ngModelCtrl) {
                        ngModelCtrl = _ngModelCtrl;
                        ngModelCtrl.$render = function () {
                            //var option = api.getOptionByValue(ngModelCtrl.$modelValue);
                            var option = {};
                            option.value = ngModelCtrl.$modelValue;
                            api.select(option);
                        };
                        ngModelCtrl.$parsers.unshift(
                            function (_newValue) {
                                var option = api.getOptionByValue(_newValue);
                                if (option.value == null) {
                                    option.value = '';
                                    option.text = _newValue;
                                }
                                api.select(option);
                                return _newValue;
                            }
                        );
                        $scope.$watch('text', function (_newV, _oldV) {
                            if (_newV !== _oldV) {
                                ngModelCtrl.$setViewValue(_newV);
                            }
                        });
                    }
                };
                return api;
            },
            scope: {
                name: '@',
                maxlength: '@',
                placeholder: '@',
                text: '@initialValue'
            },
            require: ['eayunInputSelect', '?ngModel'],
            link: function postLink(scope, element, attrs, ctrls) {
                var inputSelectCtrl = ctrls[0], ngModelCtrl = ctrls[1];
                if (ngModelCtrl) {
                    inputSelectCtrl.init(ngModelCtrl);
                }
                var documentClickBind = function () {
                    scope.$apply(function () {
                        scope.showMenu = false;
                    });
                };
                scope.$watch('showMenu', function (_value) {
                    if (_value) {
                        $timeout(function () {
                            $document.bind('click', documentClickBind);
                        }, 0, false);
                    } else {
                        $document.unbind('click', documentClickBind);
                    }
                });
            }
        };
    }])
    .directive('eayunInputOption', [function () {
        return {
            template: '<li data-ng-hide="option.selected"><a data-ng-bind="text"></a></li>',
            restrict: 'EA',
            replace: true,
            transclude: false,
            require: ['^eayunInputSelect', '?^ngModel'],
            scope: {
                text: '@'
            },
            link: function postLink(scope, element, attrs, ctrls) {
                var inputSelectCtrl = ctrls[0], ngModelCtrl = ctrls[1];
                var selected = angular.isDefined(attrs.selected);
                scope.value = scope.$parent.$eval(attrs.value);
                scope.option = inputSelectCtrl.addOption(scope.text, scope.value, selected);
                element.click(function () {
                    inputSelectCtrl.select(scope.option);
                    ngModelCtrl && ngModelCtrl.$setViewValue(scope.option.value);
                });
                if (selected && ngModelCtrl) {
                    ngModelCtrl.$setViewValue(scope.value);
                }
            }
        };
    }]);
'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:ajaxValid
     * @description
     * # tree
     * 树形控件
     */
    .directive('eayunLoading', ['$timeout', '$parse', function ($timeout, $parse) {
        return {
            restrict: 'EA',
            templateUrl: 'components/loading/loading.html',
            controller: 'eayunLoadingCtrl'
        };
    }])
    .controller('eayunLoadingCtrl', ['$rootScope', '$scope', '$document', function ($rootScope, $scope, $document) {
        $rootScope.$on('loading.begin', function () {
            $document.find('body').css('overflow','hidden');
            $scope.show = true;
        });
        $rootScope.$on('loading.end', function () {
            $document.find('body').css('overflow','');
            $scope.show = false;
        });
    }]);


'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunDialog
     * @description
     * # eayunDialog
     * 对话框组件
     */
    .directive('eayunDialog', ['$templateRequest', '$compile', '$controller', '$injector', '$q', '$sce', function ($templateRequest, $compile, $controller, $injector, $q, $sce) {
        return {
            templateUrl: 'components/modal/dialog.html',
            restrict: 'EA',
            replace: true,
            link: function postLink(scope, element, attrs) {

                var ctrlInstance, ctrlLocals = {};
                var resolveIter = 0;
                var modalOptions = scope.options;
                var modalScope = scope;
                var template = $('<div>' + scope.template + '</div>');
                delete scope.options;
                delete scope.template;
                //加载HTML模板并且绑定scope
                element.find('.eayun-modal-dialog').empty();
                var form = template.find('form');
                form.attr('name') || form.attr('name', 'dialogForm');
                element.find('.eayun-modal-dialog').append($compile(template.html())(modalScope));
                modalScope.dialogForm = modalScope[form.attr('name')];
                /*function getTemplatePromise(options) {
                 return options.template ? $q.when(options.template) :
                 $templateRequest(angular.isFunction(options.templateUrl) ? (options.templateUrl)() : options.templateUrl);
                 }*/

                //同步获取resolves中的数据
                function getResolvePromises(resolves) {
                    var promisesArr = [];
                    angular.forEach(resolves, function (value) {
                        if (angular.isFunction(value) || angular.isArray(value)) {
                            promisesArr.push($q.when($injector.invoke(value)));
                        } else if (angular.isString(value)) {
                            promisesArr.push($q.when($injector.get(value)));
                        } else {
                            promisesArr.push($q.when(value));
                        }
                    });
                    return promisesArr;
                }

                if (modalOptions.controller) {
                    //resolves中数据全部加载完成
                    $q.all(getResolvePromises(modalOptions.resolve)).then(function (values) {
                        //构造controller执行环境
                        ctrlLocals.$scope = modalScope;
                        angular.forEach(modalOptions.resolve, function (value, key) {
                            ctrlLocals[key] = values[resolveIter++];
                        });
                        //构造controller
                        ctrlInstance = $controller(modalOptions.controller, ctrlLocals);
                        //处理controller起别名的情况
                        if (modalOptions.controllerAs) {
                            if (modalOptions.bindToController) {
                                angular.extend(ctrlInstance, modalScope);
                            }
                            modalScope[modalOptions.controllerAs] = ctrlInstance;
                        }

                        //重新绑定scope
                        var form = template.find('form');
                        modalScope.dialogForm = modalScope[form.attr('name')];
//          //加载HTML模板并且绑定scope
//            element.find('.eayun-modal-dialog').empty();
//            var form = template.find('form');
//            form.attr('name') || form.attr('name','dialogForm');
//            element.find('.eayun-modal-dialog').append($compile(template.html())(modalScope));
//            modalScope.dialogForm = modalScope[form.attr('name')];

                    });
                }

            }
        };
    }])
    .controller('EayunModalCtrl', ['$scope', '$state', '$modalInstance', 'options', 'template', function ($scope, $state, $modalInstance, options, template) {
        $scope.options = options;
        $scope.template = template;
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
        $scope.ok = function (data) {
            $modalInstance.close(data);
        };
        $scope.commit = function () {
            $scope.ok(true);
        };
    }])
    .controller('EayunConfirmCtrl', ['$scope', '$modalInstance', '$timeout', 'msg', 'type', 'timeout', '$sce',
        function ($scope, $modalInstance, $timeout, msg, type, timeout, $sce) {
            $scope.msg = $sce.trustAsHtml(msg);
            $scope.type = type;
            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
            $scope.ok = function () {
                $modalInstance.close(true);
            };
            if (timeout) {
                $timeout(function () {
                    $scope.cancel();
                }, timeout, false);
            }
        }]);
'use strict';
/**
 * @ngdoc service
 * @name eayunApp.commonservice
 * @description
 * # commonservice
 * eayun公共服务
 */
angular.module('eayun.components')
    /**
     * @ngdoc service
     * @name eayunApp.eayunModal
     * @description
     * # eayunModal
     * 模态窗口服务
     */
    .service('eayunModal', ['$modal', '$rootScope', function ($modal, $rootScope) {
        var eayunModal = {};
        var modalArray = [];

        function addModal(modal) {
            modal.result.then(function () {
                modalArray.pop();
            }, function () {
                modalArray.pop();
            });
            modalArray.push(modal);
        };

        function dismissAll() {
            angular.forEach(modalArray, function (modal) {
                modal.opened.then(function () {
                    modal.dismiss('dismissAll');
                });
            });
        };

        //私有方法  默认项
        eayunModal.extendOptions = function (options) {
            var defaultOpt = {
                title: '',
                showBtn: true
            };
            return angular.extend(defaultOpt, options);
        };

        //私有方法  构造scope
        eayunModal.getScope = function (options) {
            var scope = (options.scope || $rootScope).$new();
            scope.title = options.title;
            scope.showBtn = options.showBtn;
            scope.width = (options.width || 0);
            return scope;
        };

        //原生$modal方法
        eayunModal.open = function (options) {
            options.backdrop = "static";
            options.keyboard = false;
            var modal = $modal.open(options);
            addModal(modal);
            return modal;
        };

        //封装后的对话框方法
        eayunModal.dialog = function (options) {
            options = eayunModal.extendOptions(options);
            var modal = $modal.open({
                template: '<eayun-dialog></eayun-dialog>',
                controller: 'EayunModalCtrl',
                backdrop: "static",
                scope: eayunModal.getScope(options),
                resolve: {
                    options: function () {
                        return options;
                    },
                    template: function ($templateRequest) {
                        return options.template ? $q.when(options.template) :
                            $templateRequest(angular.isFunction(options.templateUrl) ? (options.templateUrl)() : options.templateUrl);

                    }
                }
            });
            addModal(modal);
            return modal.result;
        };

        //封装后的确认框方法
        eayunModal.confirm = function (msg) {
            var modal = $modal.open({
                templateUrl: 'components/modal/confirm.html',
                controller: 'EayunConfirmCtrl',
                resolve: {
                    msg: function () {
                        return msg;
                    },
                    type: function () {
                        return "confirm";
                    },
                    timeout: undefined
                }
            });
            addModal(modal);
            return modal.result;
        };

        //封装后的消息框方法
        eayunModal.alert = function (msg, type, timeout) {
            var modal = $modal.open({
                templateUrl: 'components/modal/alert.html',
                controller: 'EayunConfirmCtrl',
                resolve: {
                    msg: function () {
                        return msg;
                    },
                    type: function () {
                        return type;
                    },
                    timeout: timeout
                }
            });
            addModal(modal);
            return modal.result;
        };
        eayunModal.successalert = function (msg) {
            var modal = $modal.open({
                templateUrl: 'components/modal/alert.html',
                controller: 'EayunConfirmCtrl',
                resolve: {
                    msg: function () {
                        return msg;
                    },
                    type: function () {
                        return "success";
                    },
                    timeout: undefined
                }
            });
            addModal(modal);
            return modal.result;
        };
        return {
            open: eayunModal.open,
            dialog: eayunModal.dialog,
            confirm: eayunModal.confirm,
            info: function (msg, timeout) {
                return eayunModal.alert(msg, 'info', timeout);
            },
            warning: function (msg, timeout) {
                return eayunModal.alert(msg, 'warning', timeout);
            },
            error: function (msg, timeout) {
                return eayunModal.alert(msg, 'error', timeout);
            },
            success: function (msg, timeout) {
                return eayunModal.alert(msg, 'success', timeout);
            },
            dismissAll: function () {
                dismissAll();
            },
            successalert: eayunModal.successalert
        };
    }]);
/**
 * Created by ZHL on 2017/3/1.
 */
'use strict';
angular.module('eayun.components')
    .directive('eayunProgressStep', ['$document', '$window', function ($document, $window) {
        return {
            templateUrl: 'components/progress/progress-step.html',
            restrict: 'EA',
            replace: true,
            controller: 'ProgressStepCtrl',
            controllerAs: 'progress',
            require: ['eayunProgressStep', 'ngModel'],
            scope: {
                maxValue: '=',
                minValue: '=',
                onValueChange: '&',
                totalStep: '=',
                precision: '=',
                format: '=',
                parse: '=',
                text: '@'
            },
            link: function postLink(scope, element, attrs, ctrl) {
                var progressCtrl = ctrl[0],
                    ngModel = ctrl[1],
                    increment = 1,
                    i = 0,
                    watch;
                for (; i < scope.precision; i++) {
                    increment = increment / 10;
                }
                scope.increment = scope.format ? scope.format(increment) : increment;

                function mousemove(event) {
                    scope.$apply(function () {
                        var offsetLeft = element[0].getBoundingClientRect().left;
                        progressCtrl.setLeft(Math.max(0, event.clientX - offsetLeft));
                    });
                };
                function mouseup() {
                    $(document).unbind('mousemove', mousemove);
                    $(document).unbind('mouseup', mouseup);
                };

                scope.mousedown = function ($event) {
                    $window.getSelection().removeAllRanges();
                    $document.bind('mousemove', mousemove);
                    $document.bind('mouseup', mouseup);
                    $event.stopPropagation();
                };

                scope.click = function ($event) {
                    var offsetLeft = element[0].getBoundingClientRect().left;
                    progressCtrl.setLeft(Math.max(0, $event.clientX - offsetLeft));
                };

                scope.$on('$destroy', function () {
                    mouseup();
                    watch();
                });

                watch = scope.$watch(function () {
                    return element.width();
                }, function () {
                    scope.width = parseInt(element.find(".ey-form-progress-bg").width() - element.find(".ey-form-progress-bt").width());
                    progressCtrl.init(ngModel, scope);
                });
            }
        };
    }])
    .controller('ProgressStepCtrl', ['$scope', '$timeout', function ($scope, $timeout) {
        var option = {
            step: 0,
            totalStep: 0,
            width: 0,
            left: 0,
            precision: 0
        };

        var ctrl = this,
            fixed = 0.44444444,
            ngModel,
            timer,
            scale;


        ctrl.init = function (_ngModel, _option) {
            ngModel = _ngModel;
            option.__proto__ = _option.__proto__;
            _option.__proto__ = option;
            option = _option;
            scale = option.totalStep / option.width;
            var i = 0;
            for (; i < option.precision; i++) {
                fixed = fixed / 10;
            }
            ngModel.$render = function () {
                ctrl.setValue(ngModel.$modelValue);
            };
            ngModel.$render();
        };

        function leftToStep(left) {
            return left * scale;
        };

        function stepToLeft(step) {
            return step / scale;
        };

        ctrl.setLeft = function (left) {
            ctrl.setStep(leftToStep(Math.min(option.width, left)));
        };

        ctrl.getLeft = function () {
            return option.left;
        };

        ctrl.setStep = function (step) {
            option.step = Number((Math.min(option.totalStep, step) + fixed).toFixed(option.precision));
            var value = ctrl.getValue(option.step);
            if (option.minValue !== undefined && value < option.minValue) {
                ctrl.setValue(option.minValue);
                return;
            } else if (option.maxValue !== undefined && value > option.maxValue) {
                ctrl.setValue(option.maxValue);
                return;
            }
            option.left = stepToLeft(option.step);
            ctrl.value = value;
            ngModel.$setViewValue(value);

            if (timer) {
                $timeout.cancel(timer);
            }
            timer = $timeout(function () {
                ctrl.warpCallback('onValueChange', value);
            }, 200);
        };

        ctrl.getStep = function () {
            return option.step;
        };

        ctrl.setValue = function (value) {
            ctrl.setStep(option.parse ? option.parse(value) : value);
        };

        ctrl.getValue = function (_step) {
            var step = _step === undefined ? option.step : _step;
            return option.format ? option.format(step) : step;
        };

        ctrl.warpCallback = function (callback, value, $event) {
            ($scope[callback] || angular.noop)({
                $value: value,
                $event: $event
            });
        };
    }])
    .directive('eayunNumberInput', [function () {
        return {
            templateUrl: 'components/progress/number-input.html',
            restrict: 'EA',
            replace: true,
            controller: 'NumberInputCtrl',
            controllerAs: 'ctrl',
            require: ['eayunNumberInput', '^ngModel'],
            scope: {
                suffix: '=',
                maxValue: '=',
                minValue: '=',
                increment: '=',
                onValueChange: '&'
            },
            link: function postLink(scope, element, attrs, ctrl) {
                var progressCtrl = ctrl[0],
                    ngModel = ctrl[1];
                progressCtrl.init(ngModel, scope);
            }
        };
    }])
    .controller('NumberInputCtrl', ['$scope', function ($scope) {
        var option = {
            maxValue: 0,
            minValue: 0,
            increment: 1
        };

        var ctrl = this,
            watch,
            ngModel;

        ctrl.init = function (_ngModel, _option) {
            ngModel = _ngModel;
            option.__proto__ = _option.__proto__;
            _option.__proto__ = option;
            option = _option;
            var _render = _ngModel.$render;
            ngModel.$render = function () {
                _render();
                $scope.value = ngModel.$modelValue;
            };
        };

        ctrl.warpCallback = function (callback, value, $event) {
            ($scope[callback] || angular.noop)({
                $value: value,
                $event: $event
            });
        };

        ctrl.up = function () {
            $scope.value += option.increment;
            if ($scope.value > option.maxValue) {
                $scope.value = option.maxValue;
            }
            ctrl.warpCallback('onValueChange',  $scope.value);
        };

        ctrl.down = function () {
            $scope.value -= option.increment;
            if ($scope.value < option.minValue) {
                $scope.value = option.minValue;
            }
            ctrl.warpCallback('onValueChange',  $scope.value);
        };

        ctrl.blur=function () {
                if ($scope.value + '' == 'NaN') {
                    $scope.value = 0;
                }
                if ($scope.value < option.minValue) {
                    $scope.value = option.minValue;
                }
                if ($scope.value > option.maxValue) {
                    $scope.value = option.maxValue;
                }
                ngModel.$setViewValue($scope.value);
                ctrl.warpCallback('onValueChange', $scope.value);
        };

        watch = $scope.$watch('value', function (newValue, oldValue) {
            if (newValue !== oldValue) {
                newValue = Number(newValue);
                if (newValue + '' == 'NaN') {
                    newValue = option.minValue;
                    $scope.value = option.minValue;
                    ctrl.warpCallback('onValueChange', newValue);
                }
                if ($scope.value > option.maxValue) {
                    $scope.value = option.maxValue;
                }
            }
        });

        $scope.$on('$destroy', function () {
            watch();
        });
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:progress
     * @description
     * # progress
     * 进度条组件
     */
    .directive('eayunProgress', ['$document', '$window', function ($document, $window) {
        return {
            templateUrl: 'components/progress/progress.html',
            restrict: 'EA',
            replace: true,
            controller: 'ProgressCtrl',
            controllerAs: 'progress',
            require: ['eayunProgress', 'ngModel'],
            scope: {
                maxValue: '=',
                minValue: '=',
                onValueChange: '&',
                text: '@'
            },
            link: function postLink(scope, element, attrs, ctrl) {
                var progressCtrl = ctrl[0],
                    ngModel = ctrl[1],
                    mousedown = false,
                    width = parseInt(element.width() * 0.7 - element.find(".ey-form-progress-bt").width());

                function mousemove($event) {
                    scope.$apply(function () {
                        var width = parseInt(element.width() * 0.7 - element.find(".ey-form-progress-bt").width()),
                            offsetLeft = element[0].getBoundingClientRect().left;
                        var left = Math.min(width, Math.max(0, $event.clientX - offsetLeft));
                        progressCtrl.move(left);
                    });
                };

                function mouseup() {
                    mousedown = false;
                    $document.unbind('mousemove', mousemove);
                };

                scope.click = function ($event) {
                    if (!mousedown) {
                        var offsetLeft = element[0].getBoundingClientRect().left;
                        progressCtrl.move(Math.min(width, Math.max(0, ($event.clientX - offsetLeft))));
                    }
                    mouseup()
                };

                scope.mousedown = function ($event) {
                    $window.getSelection().removeAllRanges();
                    mousedown = true;
                    $document.bind('mousemove', mousemove);
                };

                scope.keypress = function ($event) {
                    if($event.keyCode === 46){ // 小数点
                        //ngModel.$setViewValue(parseInt(scope.value));
                    }
                };

                scope.up = function(){
                    scope.value++;
                };

                scope.down = function(){
                    scope.value--;
                };

                scope.$on('$destroy', function () {
                    $document.unbind('mousemove', mousemove);
                    $document.unbind('mouseup', mouseup);
                });

                $document.bind('mouseup', mouseup);
                progressCtrl.init(ngModel, width);
            }
        };
    }])
    .controller('ProgressCtrl', ['$scope', '$timeout', function ($scope, $timeout) {
        var ctrl = this,
            ngModel,
            scale,
            timer,
            watch;

        ctrl.init = function (_ngModel, width) {
            ngModel = _ngModel;
            scale = $scope.maxValue / width;
            ngModel.$render = function () {
                $scope.value = ngModel.$modelValue || '';
                ctrl.left = $scope.value / scale;
            };
        };

        ctrl.move = function (left) {
            ctrl.left = left;
            $scope.value = parseInt(ctrl.left * scale);
            ngModel.$setViewValue($scope.value);
        };

        ctrl.warpCallback = function (callback, value, $event) {
            ($scope[callback] || angular.noop)({
                $value: value,
                $event: $event
            });
        };

        watch = $scope.$watch('value', function (newValue, oldValue) {
            if (newValue !== oldValue) {
                newValue = parseInt(newValue);
                if(newValue+'' == 'NaN'){
                    newValue = 0;
                }
                if (newValue < $scope.minValue) {
                    ctrl.left = 0;
                    newValue = $scope.minValue;
                }
                if (newValue > $scope.maxValue) {
                    ctrl.left = parseInt($scope.maxValue / scale);
                    newValue = $scope.maxValue;
                }
                ctrl.left = newValue / scale;
                ngModel.$setViewValue(newValue);
                $scope.value = newValue;

                if (timer) {
                    $timeout.cancel(timer);
                }
                timer = $timeout(function () {
                    ctrl.warpCallback('onValueChange', newValue);
                }, 200);

            }
        });

        $scope.$on('$destroy', function () {
            $timeout.cancel(timer);
            watch();
        });
    }]);

'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:ajaxValid
     * @description
     * # tree
     * 树形控件
     */
    .directive('eayunAutoRefresh', ['$timeout', '$parse', function ($timeout, $parse) {
        return {
            restrict: 'EA',
            link: function postLink(scope, element, attrs) {
                var timer;
                var refresh = $parse(attrs.refresh)(scope);
                var interval = $parse(attrs.interval)(scope);
                interval = angular.isNumber(interval) ? parseInt(interval) : 5000;
                var flush = function () {
                    (refresh || angular.noop)();
                    timer = $timeout(flush, interval);
                };
                timer = $timeout(flush, 0);
                scope.$on('$destroy', function () {
                    $timeout.cancel(timer);
                });
            }
        };
    }]);


'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:ajaxValid
     * @description
     * # tree
     * 树形控件
     */
    .directive('eyAssertSameAs', [function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function postLink(scope, element, attrs, ngModel) {
                var isSame = function (value) {
                    var anotherValue = scope.$eval(attrs.eyAssertSameAs);
                    return value === anotherValue;
                };
                ngModel.$validators.same = isSame;
                scope.$watch(function () {
                    return scope.$eval(attrs.eyAssertSameAs);
                }, function () {
                    ngModel.$setValidity('same', isSame(ngModel.$viewValue));
                })
            }
        };
    }]);


/**
 * Created by ZHL on 2016/3/22.
 */
'use strict';

angular.module('eayun.components')
    .filter('multiFilter', ['$filter', function ($filter) {
        return function (data, text) {
            if (angular.isArray(data) && typeof text === 'string') {
                var select = text.split(',');
                var array = $filter('filter')(data, select.pop());
                var count = 0;
                for (var i = 0; i < array.length; i++) {
                    if ((',' + text).indexOf(',' + array[i] + ',') === -1) {
                        count++;
                        select.push(array[i]);
                    }
                }
                return select;
            } else {
                return data;
            }
        };
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:search
     * @description
     * # search
     * 搜索组件
     */
    .directive('eayunSearch', function () {
        return {
            templateUrl: 'components/search/search.html',
            restrict: 'EA',
            replace: true,
            controller: 'SearchCtrl',
            controllerAs: 'search',
            require: ['eayunSearch', 'ngModel'],
            scope: {
                options: '='
            },
            link: function postLink(scope, element, attrs, ctrl) {
                var searchCtrl = ctrl[0],
                    ngModel = ctrl[1];
                searchCtrl.init(ngModel, scope.options.searchFn, scope.options.select,
                    scope.options.series, scope.options.placeholder);
            }
        };
    })
    .controller('SearchCtrl', ['$injector', '$q', function ($injector, $q) {
        var vm = this;
        var ctrl = {
            parseSelectArray: function (select) {
                var selectArray = [];
                angular.forEach(select, function (item) {
                    angular.forEach(item, function (value, key) {
                        selectArray.push({
                            key: key,
                            value: value
                        });
                    });
                });
                return selectArray;
            },
            series: {},
            getSeries: function () {
                return vm.showSelectBtn ? ctrl.series[vm.selected] : ctrl.series;
            },
            setSeries: function (_series) {
                if (_series && Object.keys(_series).length > 0) {
                    if (vm.showSelectBtn) {
                        angular.forEach(_series, function (value, key) {
                            ctrl.series[key] = {};
                            ctrl.series[key].multi = !!value.multi;
                            $q.when($injector.invoke(value.data)).then(function (data) {
                                ctrl.series[key].data = data;
                            });
                        });
                    } else {
                        ctrl.series.multi = !!_series.multi;
                        $q.when($injector.invoke(_series.data)).then(function (data) {
                            ctrl.series.data = data;
                        });
                    }
                }
            }
        };
        vm.placeholder = "";
        /**
         *
         * @param _ngModel
         * @param _searchFn 查询回调函数
         * @param _select   搜索类型数组
         * @param _series     搜索项
         * @param _checkbox 是否显示checkbox
         */
        vm.init = function (_ngModel, _searchFn, _select, _series, _placeholder) {
            vm.placeholder = _placeholder;
            ctrl.ngModelCtrl = _ngModel;
            ctrl.ngModelCtrl.$render = function () {
                vm.value = ctrl.ngModelCtrl.$modelValue || '';
            };
            ctrl.searchFn = _searchFn;
            if (_select && _select instanceof Array && _select.length > 0) {
                vm.showSelectBtn = true;
                vm.selectArray = ctrl.parseSelectArray(_select);
                vm.selected = vm.selectArray[0].key;
            }
            ctrl.setSeries(_series);
            if (_series) {
                vm.hasFilter = true;
                vm.showFilter = false;
                vm.series = ctrl.getSeries();
            }
        };
        vm.doSearch = function () {
            var value = [];
            if (vm.hasFilter && vm.series && vm.series.multi) {
                //多选情况移除输入框末尾未选中项
                value = vm.value.split(',');
                value.pop();
                vm.value = value.length === 0 ? '' : (value.join(',') + ',');
            } else {
                value.push(vm.value);
            }
            if (vm.showSelectBtn) {
                ctrl.ngModelCtrl.$setViewValue({
                    key: vm.selected,
                    value: value.join(',')
                });
            } else {
                ctrl.ngModelCtrl.$setViewValue(value.join(','));
            }
            ctrl.searchFn();
        };
        vm.select = function (item) {
            if (vm.series.multi) {
                //多选
                if ((',' + vm.value).indexOf(',' + item + ',') !== -1) {
                    //    条目已选中，从搜索框中移除
                    vm.value = (',' + vm.value).replace(',' + item + ',', ',').replace(',','');
                } else {
                    //    条目未选中，加入搜索框
                    var array = vm.value.split(',');
                    var str = array.pop();
                    array.push(item);
                    array.push(str);
                    vm.value = array.join(',');
                }
            } else {
                //单选
                vm.showFilter = false;
                vm.value = item;
            }
        };
        vm.changeSelect = function () {
            if (vm.hasFilter) {
                vm.series = ctrl.getSeries();
            }
            vm.value = "";
        };
    }])
    .directive('eayunSearchText', function () {
        return {
            templateUrl: 'components/search/text.html',
            restrict: 'EA',
            replace: true,
            scope: {
                value: '=',
                placeholder: '@',
                search: '&'
            },
            link: function postLink(scope, element, attrs, ctrl) {
                scope.warpCallback = function (callback, value, $event) {
                    (scope[callback] || angular.noop)({
                        $value: value,
                        $event: $event
                    });
                };
            }
        };
    });

'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * ҳ�����ģ��
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunTable
     * @description
     * # eayunTable
     * ���
     */
    .directive('eayunTable', ['tableService', function (tableService) {
        var eayunTable = {
            template: '<div ng-transclude></div>',
            restrict: 'EA',
            replace: true,
            transclude: true,
            controller: ['$scope', function ($scope) {
                var tableCtrl = this;
                tableCtrl.api = {
                    draw: function () {
                        tableCtrl.api.isLoading = true;
                        tableService.query(tableCtrl.getSource(), tableCtrl.getParam()).then(function (data) {
                            $scope.result = data;
                            tableCtrl.api.isLoading = false;
                        },function(){
                            tableCtrl.api.isLoading = false;
                        });
                    },
                    setSource: function (source) {
                        $scope.ajaxSource = source;
                    }
                };
                tableCtrl.getParam = function () {
                    return typeof $scope.param === 'function' ? $scope.param() : {};
                };
                tableCtrl.getSource = function () {
                    return $scope.ajaxSource;
                };
                tableCtrl.setResult = function (result) {
                    $scope.result = result;
                };
                $scope.api = tableCtrl.api;

            }],
            controllerAs:'table',
            scope: {api: '=api', ajaxSource: '=ajaxSource', param: '=param', result: '=result'},
            link: function postLink(scope, element, attrs) {
                if (scope.ajaxSource && element.find('eayun-table-page').length == 0) {
                    scope.api.draw();
                }
                if (!scope.ajaxSource) {
                    scope.$watch('ajaxSource', function (a) {
                        var tableCtrl = eayunTable.controller[eayunTable.controller.length - 1](scope);
                        scope.$broadcast('a', tableCtrl);
                        if (element.find('eayun-table-page').length == 0) {
                            scope.api.draw();
                        }
                    });
                }
            }
        };
        return eayunTable;
    }])
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunTablePage
     * @description
     * # eayunTablePage
     * ����ҳ���
     */
    .directive('eayunTablePage', ['tableService', '$timeout', function (tableService, $timeout) {
        return {
            templateUrl: 'components/table/tablepage.html',
            restrict: 'EA',
            replace: true,
            require: '^eayunTable',
            scope: false,
            link: function postLink(scope, element, attrs, tableCtrl) {
                scope.$on('a', function (event, tableCtrl) {
                    tableCtrl.api.draw = function () {
                        pageApi.first();
                    };
                    tableCtrl.api.refresh = function () {
                        pageApi.goto(options.pageNumber);
                    };
                    tableCtrl.api.options = options;
                    tableCtrl.api.draw();
                });
                var options = {
                    pageSize: attrs.pageSize || 10,//ÿҳ��ʾ����
                    pageNumber: 1,//��ǰҳ
                    totalPage: 1,//��ҳ��
                    $$totalCount: 1,//��ҳ��
                    params: {}//ҵ����ز�ѯ����
                };
                scope.buttons = [2, 3, 4, 5];
                var changeButtons = function () {
                    if (options.pageNumber < 5) {
                        scope.buttons = [2, 3, 4, 5];
                    } else if (options.pageNumber <= (options.totalPage - 3)) {
                        scope.buttons = [options.pageNumber - 1, options.pageNumber, options.pageNumber + 1, options.pageNumber + 2];
                    } else if (options.pageNumber > (options.totalPage - 3)) {
                        scope.buttons = [options.totalPage - 4, options.totalPage - 3, options.totalPage - 2, options.totalPage - 1];
                    } else if (options.pageNumber == scope.buttons[0] || options.pageNumber == scope.buttons[3]) {
                        scope.buttons = [options.pageNumber - 1, options.pageNumber, options.pageNumber + 1, options.pageNumber + 2];
                    }
                };
                var pageApi = {
                    draw: function () {
                        tableCtrl.api.isLoading = true;
                        options.params = tableCtrl.getParam();
                        if (options.params && options.params.$showLoading) {
                            options.$showLoading = true;
                            delete options.params.$showLoading;
                        }
                        if (tableCtrl.getSource()) {
                            tableService.queryPage(tableCtrl.getSource(), options).then(function (page) {
                                scope.isLoad = true;
                                tableCtrl.api.isLoading = false;
                                options.$$totalCount = page.totalCount;
                                options.totalPage = (page.totalCount == 0 ? 1 : Math.ceil(page.totalCount / (null == page.pageSize || 0 == page.pageSize ? options.pageSize : page.pageSize)));//����ȡ����������ҳ��,����1ҳ
                                tableCtrl.setResult(page.result);
                                if (options.pageNumber > options.totalPage)
                                    pageApi.goto(options.totalPage);
                            },function(){
                                tableCtrl.api.isLoading = false;
                            });
                        }
                    },
                    goto: function (index) {
                        if (angular.isString(index)) {
                            index = parseInt(index);
                        }
                        options.pageNumber = index;
                        if (index < 1)
                            options.pageNumber = 1;
                        if (index > options.totalPage)
                            options.pageNumber = options.totalPage;
                        changeButtons();
                        this.draw();
                    },
                    first: function () {
                        this.goto(1);
                    },
                    next: function () {
                        this.goto(options.pageNumber + 1);
                    },
                    back: function () {
                        this.goto(options.pageNumber - 1);
                    },
                    last: function () {
                        this.goto(options.totalPage);
                    }
                };
                tableCtrl.api.draw = function () {
                    pageApi.first();
                };
                tableCtrl.api.refresh = function () {
                    pageApi.goto(options.pageNumber);
                };
                tableCtrl.api.options = options;
                tableCtrl.api.draw();
                scope.pageApi = pageApi;
                scope.options = options;
                scope.isLoad = false;
                scope.canShowButton = function (value) {
                    return value > 1 && value < options.totalPage;
                };
                scope.isCurrent = function (num) {
                    return options.pageNumber === num;
                };
                scope.number = '';
                var valid = /^[1-9]+[0-9]*]*$/;
                scope.$watch('number', function (newV, oldV) {
                    scope.number = oldV;
                    if ((newV > 0 && newV <= options.totalPage && valid.test(newV)) || newV == '') {
                        scope.number = newV;
                    }
                });
            }
        };
    }]);
'use strict';
/**
 * @ngdoc service
 * @name eayunApp.components.tableService
 * @description
 * # tableService
 * tableService
 */
angular.module('eayun.components')
    /**
     * @ngdoc service
     * @name eayunApp.tableService
     * @description
     * # tableService
     * tableService
     */
    .service('tableService', ['$http', '$q', function ($http, $q) {
        var tableService = {};

        tableService.query = function (source, data) {
            if (source) {
                return $http.post(source, data).then(function (response) {
                    return response.data;
                });
            } else {
                return $q.reject();
            }
        };

        tableService.queryPage = function (source, data) {
            if (source) {
                return $http.post(source, data).then(function (response) {
                    return response.data;
                });
            } else {
                return $q.reject();
            }
        };

        return {
            query: tableService.query,
            queryPage: tableService.queryPage
        };
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';

angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunSelect
     * @description
     * # eayunSelect
     * 下拉选择组件
     */
    .directive('eayunSelect', ['$document', '$timeout', function ($document, $timeout) {
        return {
            templateUrl: function (element, attrs) {
                return attrs.type === 'btn' ? 'components/select/selectbtn.html' : 'components/select/select.html';
            },
            restrict: 'EA',
            replace: true,
            transclude: true,
            controller: function ($scope, $log) {
                $scope.options = new Array();
                $scope.selectFlag = false;
                $scope.open = function () {
                    $scope.showMenu = $scope.disabled ? false : !$scope.showMenu;
                };
                var getShowText = function (text) {
                    if (!text) {
                        return '';
                    } else if ($scope.showLength && text.length > $scope.showLength) {
                        return text.substr(0, $scope.showLength) + '...';
                    } else if ($scope.showFormat) {
                        return $scope.showFormat({text: text});
                    }
                    return text;
                };
                var curOption = null;
                var ngModelCtrl;
                var api = {
                    select: function (option) {
                        curOption && (curOption.selected = false);
                        curOption = option;
                        if (curOption.value != 'stay') {
                            curOption.selected = true;
                            $scope.text = getShowText(curOption.text);
                            $scope.textTip = curOption.text;
                        }
                        $scope.showMenu = false;
                        $scope.selectFlag = true;
                    },
                    addOption: function (value_, text_, selected_) {
                        var option = {
                            value: value_,
                            text: text_,
                            selected: selected_
                        };
                        if (ngModelCtrl && curOption && (option.value == curOption.value)) {
                            option.selected = true;
                            ngModelCtrl.$setValidity('outRange', true);
                        }
                        $scope.options.push(option);
                        option.selected && this.select(option);
                        return option;
                    },
                    removeOption: function (_option) {
                        for (var i = 0; i <= $scope.options.length; i++) {
                            if (_option === $scope.options[i]) {
                                $scope.options.splice(i, 1);
                                break;
                            }
                        }
                    },
                    getOptionByValue: function (value) {
                        if (typeof value === "undefined")
                            return {};
                        var option = null;
                        angular.forEach($scope.options, function (option_) {
                            if (option_.value == value) {
                                option = option_;
                            }
                        });
                        return option || {value: null};
                    },
                    init: function (ngModelCtrl_) {
                        ngModelCtrl = ngModelCtrl_;
                        ngModelCtrl.$render = function () {
                            var option = api.getOptionByValue(ngModelCtrl.$modelValue);
                            ngModelCtrl.$setValidity('outRange', option.value !== null);
                            //if (option.value === null)
                            //  $log.error(ngModelCtrl.$modelValue, '不在下拉选项之中！');
                            option.value = ngModelCtrl.$modelValue;
                            api.select(option);
                        };
                        ngModelCtrl.$parsers.unshift(
                            function (newValue) {
                                var option = api.getOptionByValue(newValue);
                                ngModelCtrl.$setValidity('outRange', option.value !== null);
                                return newValue;
                            }
                        );
                    }
                };
                return api;
            },
            scope: {placeholder: '@placeholder', showFormat: '&?'},
            require: ['eayunSelect', '?ngModel'],
            link: function postLink(scope, element, attrs, ctrls) {
                var selectCtrl = ctrls[0], ngModelCtrl = ctrls[1];
                if (ngModelCtrl) {
                    selectCtrl.init(ngModelCtrl);
                }
                if (attrs.showLength) {
                    var length = parseInt(attrs.showLength);
                    scope.showLength = length ? length : undefined;
                }

                var documentClickBind = function () {
                    scope.$apply(function () {
                        scope.showMenu = false;
                    });
                };

                scope.$watch('showMenu', function (value) {
                    if (value) {
                        $timeout(function () {
                            $document.bind('click', documentClickBind);
                        }, 0, false);
                    } else {
                        $document.unbind('click', documentClickBind);
                    }
                });
                attrs.$observe('disabled', function (value) {
                    scope.disabled = !!value;
                });
            }
        };
    }])
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunOption
     * @description
     * # eayunOption
     * 下拉选项组件
     */
    .directive('eayunOption', [function () {
        return {
            template: '<li data-ng-hide="option.selected" data-ng-bind="text"></li>',
            restrict: 'EA',
            require: ['^eayunSelect', '?^ngModel'],
            scope: {
                text: '@'
            },
            replace: true,
            transclude: false,
            link: function postLink(scope, element, attrs, ctrls) {
                var selectCtrl = ctrls[0], ngModelCtrl = ctrls[1];
                var selected = angular.isDefined(attrs.selected);
                scope.value = scope.$parent.$eval(attrs.value);
                scope.option = selectCtrl.addOption(scope.value, scope.text, selected);
                element.click(function () {
                    selectCtrl.select(scope.option);
                    ngModelCtrl && ngModelCtrl.$setViewValue(scope.option.value);
                });
                if (selected && ngModelCtrl)
                    ngModelCtrl.$setViewValue(scope.value);
                scope.$on('$destroy', function () {
                    selectCtrl.removeOption(scope.option);
                });
            }
        };
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';

angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunSelect
     * @description
     * # eayunSelect
     * 下拉选择组件
     */
    .directive('eayunSelectInput', ['$document', '$timeout', function ($document, $timeout) {
        return {
            templateUrl: 'components/select/selectInput.html',
            restrict: 'EA',
            replace: true,
            scope: {
                placeholder: '@',
                optionsData: '=',
                textField: '@',
                valueField: '@',
                firstKey: '@'
            },
            controller: 'eayunSelectCtrl',
            require: 'ngModel',
            /*link: function postLink(scope, element, attrs, ngModelCtrl) {
             /!* keys for show *!/
             scope.curKeys = [];
             /!* options which has been selected or not *!/
             scope.curSelected = {};
             /!* K-V *!/
             var curOptions = {};
             scope.optionClick = function ($option, $event) {
             scope.showMenu = false;
             ngModelCtrl.$setViewValue(scope.value);
             };
             /!* override the select function *!/
             scope.select = function (key, $event) {
             scope.clear(key);
             scope.curSelected[key] = true;
             scope.text = key;
             scope.value = curOptions[key];
             (scope.optionClick || angular.noop)({
             $option: key,
             $event: $event
             });
             };
             /!* override the clear function *!/
             scope.clear = function(key){
             scope.curSelected = [];
             scope.value = null;
             };

             scope.open = function ($event) {
             scope.showMenu = scope.disabled ? false : true;
             };

             attrs.$observe('disabled', function (value) {
             scope.disabled = !!value;
             });
             /!* attrs cant-input determine the select-input component whether can be input to change its model value *!/
             scope.change = function () {
             if (!scope.cantInput) {
             ngModelCtrl.$setViewValue(scope.text);
             } else {
             ngModelCtrl.$setViewValue(scope.value);
             }
             };

             var documentClickBind = function () {
             scope.$apply(function () {
             scope.showMenu = false;
             });
             };

             scope.$watch('showMenu', function (value) {
             if (value) {
             $timeout(function () {
             $document.bind('click', documentClickBind);
             }, 0, false);
             } else {
             $document.unbind('click', documentClickBind);
             }
             });

             scope.$watch('optionsData', function (value) {
             ngModelCtrl.$render();
             });

             ngModelCtrl.$render = function () {
             angular.forEach(scope.optionsData, function (option) {
             curOptions[option[scope.textField]] = option[scope.valueField];
             scope.curKeys.push(option[scope.textField]);
             if (option[scope.valueField] == ngModelCtrl.$modelValue) {
             scope.select(option[scope.textField]);
             }
             })
             };
             }*/
            link: function postLink(scope, element, attrs, ngModelCtrl) {
                var map;

                function getMap() {
                    var map = {};
                    angular.forEach(scope.optionsData, function (option) {
                        map[option[scope.textField]] = option;
                    });
                    return map;
                };
                function getKeys() {
                    var i;
                    scope.options = Object.keys(map);
                    scope.keys = scope.$eval('options | filter:text');
                    for (i = 0; i < scope.keys.length; i++) {
                        if(map[scope.keys[i]].$$selected){
                            scope.keys.splice(i,1);
                            break;
                        }
                    }
                };
                function init() {
                    map = getMap();
                    getKeys();
                };
                init();
                scope.open = function () {
                    scope.showMenu = scope.disabled ? false : true;
                };
                scope.optionClick = function ($option, $event) {
                    scope.showMenu = false;
                    ngModelCtrl.$setViewValue(scope.value);
                    getKeys();
                };
                scope.click = function (key) {
                    scope.select(map[key]);
                };
                scope.change = function () {
                    ngModelCtrl.$setViewValue(scope.value);
                    scope.open();
                    getKeys();
                };

                attrs.$observe('disabled', function (value) {
                    scope.disabled = !!value;
                });
                var documentClickBind = function () {
                    scope.$apply(function () {
                        scope.showMenu = false;
                    });
                };
                scope.$watch('showMenu', function (value) {
                    if (value) {
                        $timeout(function () {
                            $document.bind('click', documentClickBind);
                        }, 0, false);
                    } else {
                        $document.unbind('click', documentClickBind);
                    }
                });

                scope.$watch('optionsData', function (value) {
                    ngModelCtrl.$render();
                    init();
                });

                ngModelCtrl.$render = function () {
                    angular.forEach(scope.optionsData, function (option) {
                        if (option[scope.valueField] == ngModelCtrl.$modelValue) {
                            scope.select(option);
                        }
                    })
                };
            }
        };
    }])
    .controller('eayunSelectCtrl', ['$scope', function ($scope) {
        var curOption = {};
        $scope.select = function (option, $event) {
            $scope.clear();
            option.$$selected = true;
            curOption = option;
            $scope.text = curOption[$scope.textField];
            $scope.value = curOption[$scope.valueField];
            $event && $event.stopPropagation();

            ($scope.optionClick || angular.noop)({
                $option: option,
                $event: $event
            });

        };
        $scope.clear = function () {
            curOption.$$selected = false;
            $scope.value = null;
            curOption = {};
        };
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';

angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunSelect
     * @description
     * # eayunSelect
     * 下拉选择组件
     */
    .directive('eayunSelectTree', ['$document', '$timeout', function ($document, $timeout) {
        return {
            templateUrl: 'components/select/selectTree.html',
            restrict: 'EA',
            replace: true,
            scope: {
                placeholder: '@',
                optionsData: '=',
                textField: '@',
                valueField: '@'
            },
            controller: 'eayunSelectCtrl',
            require: 'ngModel',
            link: function postLink(scope, element, attrs, ngModelCtrl) {
                scope.optionClick = function (data) {
                    scope.showMenu = false;
                    ngModelCtrl.$setViewValue(data.$option[scope.valueField]);
                };

                scope.open = function ($event) {
                    scope.showMenu = scope.disabled ? false : true;
                    $event.stopPropagation();
                };

                attrs.$observe('disabled', function (value) {
                    scope.disabled = !!value;
                });

                var documentClickBind = function () {
                    scope.$apply(function () {
                        scope.showMenu = false;
                    });
                };
                scope.$watch('showMenu', function (value) {
                    if (value) {
                        $timeout(function () {
                            $document.bind('click', documentClickBind);
                        }, 0, false);
                    } else {
                        $document.unbind('click', documentClickBind);
                    }
                });

                scope.$watch('optionsData', function (value) {
                    ngModelCtrl.$render();
                });
                ngModelCtrl.$render = function () {
                    var item = getTreeItemByValue(ngModelCtrl.$modelValue, scope.valueField);
                    if (item) {
                        scope.select(item);
                    }
                };
                var isLeaf = function (item) {
                    return !item.children || !item.children.length;
                };

                var getTreeItemByValue = function (value, valueField) {
                    var items = [];
                    items = items.concat(scope.optionsData);
                    var curItem;
                    while (items.length > 0) {
                        curItem = items.pop();
                        if (!curItem) {
                            continue;
                        }
                        if (curItem && curItem[valueField] == value) {
                            return curItem;
                        } else if (!isLeaf(curItem)) {
                            items = items.concat(curItem.children)
                        }
                    }

                };
            }
        };
    }]);
/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';

angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:tabs
     * @description
     * # tabs
     * 导航页签
     */
    .directive('tabs', [function () {
        return {
            template: '<nav><ul ng-transclude class="nav" role="tablist"></ul></nav>',
            restrict: 'EA',
            replace: true,
            transclude: true,
            link: function postLink(scope, element, attrs) {
                element.removeClass(attrs.class);
                element.children().addClass(attrs.class);
            }
        };
    }])
    /**
     * @ngdoc directive
     * @name eayunApp.directive:tab
     * @description
     * # tab
     * 导航页签项
     */
    .directive('tab', [function () {
        return {
            template: '<li role="presentation"><a ng-transclude role="tab"></a></li>',
            restrict: 'EA',
            replace: true,
            transclude: true,
            link: function postLink(scope, element, attrs) {

            }
        };
    }])
    /**
     * @ngdoc directive
     * @name eayunApp.directive:tabUnderline
     * @description
     * # tabUnderline
     * 导航页签项--下划线
     */
    .directive('tabUnderline', [function () {
        return {
            template: '<li role="presentation"><a ng-transclude role="tab"></a><div class="triangle"></div></li>',
            restrict: 'EA',
            replace: true,
            transclude: true,
            link: function postLink(scope, element) {

            }
        };
    }]);
'use strict';
/**
 * @ngdoc service
 * @name eayunApp.commonservice
 * @description
 * # commonservice
 * eayun公共服务
 */
angular.module('eayun.components')
    /**
     * @ngdoc service
     * @name liadApp.toast
     * @description
     * # toast
     * 模态窗口服务
     */
    .service('toast', ['$modal', function ($modal) {
        var toast = function (type, msg) {
            return $modal.open({
                templateUrl: 'components/toast/toast.html',
                backdrop: false,
                controller: 'toastCtrl',
                resolve: {
                    msg: function () {
                        return msg;
                    },
                    type: function () {
                        return type;
                    }
                }
            }).result;
        };

        return {
            success: function (msg) {
                toast('success', msg);
            },
            error: function (msg) {
                toast('error', msg);
            },
            running: function (msg) {
                toast('running', msg);
            }
        };
    }])

    /**
     * @ngdoc function
     * @name eayunApp.controller:toastCtrl
     * @description
     * # toastCtrl
     * toast提示框
     */
    .controller('toastCtrl', ['$scope', '$modalInstance', '$timeout', 'msg', 'type', function ($scope, $modalInstance, $timeout, msg, type) {
        $scope.msg = msg;
        $scope.type = type;
        $scope.isIE = ("ActiveXObject" in window);//区分当前浏览器是否是IE
        $timeout(function () {
            $modalInstance.close(true);
        }, 2000, false);

    }]);
/**
 * Created by ZHL on 2017/1/18.
 */
'use strict';
/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 所见即所得--富文本编辑器
 */
angular.module('eayun.components')
/**
 * @ngdoc directive
 * @name eayunApp.directive:ajaxValid
 * @description
 * # tree
 * 树形控件
 */
    .directive('wysiwyg', ['$q', '$log', function ($q, $log) {
        return {
            restrict: 'E',
            template: '<div class="wysiwyg"></div>',
            scope: {
                content: '='
            },
            link: function postLink(scope, element, attrs) {
                var trumbowyg = $(element.find('.wysiwyg')).trumbowyg({
                    lang: 'zh_cn',
                    btns: [
                        ['viewHTML'],
                        ['formatting'],
                        'btnGrp-semantic',
                        ['superscript', 'subscript'],
                        ['link'],
                        ['insertImage'],
                        'btnGrp-justify',
                        'btnGrp-lists',
                        ['horizontalRule'],
                        ['removeformat']
                    ]
                });
                trumbowyg.on('tbwblur',function() {
                    scope.$apply(function () {
                        scope.content = trumbowyg.trumbowyg('html');
                    });
                });
                trumbowyg.trumbowyg('html',scope.content);
            }
        };
    }]);

'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:ajaxValid
     * @description
     * # tree
     * 树形控件
     */
    .directive('treeView', ['$q', '$log', function ($q, $log) {
        return {
            restrict: 'E',
            templateUrl: 'components/tree/treeView.html',
            scope: {
                treeData: '=',
                canChecked: '=',
                textField: '@',
                itemClicked: '&',
                itemCheckedChanged: '&',
                itemTemplateUrl: '@',
                expendLevel: '='
            },
            controller: 'TreeCtrl'
        };
    }])
    .controller('TreeCtrl', ['$scope', 'eayunTreeService', function ($scope, eayunTreeService) {
        $scope.itemExpended = function (item, $event) {
            item.$$isExpend = !item.$$isExpend;
            $event.stopPropagation();
        };

        $scope.getItemIcon = function (item) {
            var isLeaf = $scope.isLeaf(item);

            if (isLeaf) {
                return 'glyphicon glyphicon-leaf';
            }

            return item.$$isExpend ? 'glyphicon glyphicon-minus' : 'glyphicon glyphicon-plus';
        };

        $scope.isLeaf = function (item) {
            return !item.children || !item.children.length;
        };

        $scope.$watch('treeData', function (data) {
            if (data) {
                eayunTreeService.expendToLevel(data, $scope.expendLevel === undefined ? 1 : $scope.expendLevel);
            }
        });

        $scope.warpCallback = function (callback, item, $event) {
            ($scope[callback] || angular.noop)({
                $item: item,
                $event: $event
            });
        };
    }])
    .service('eayunTreeService', [function () {
        var tree = this;

        function breadthFirst(item, visitFn, maxDepth, _depth) {
            var depth = _depth || 0;
            depth++;
            if (depth > maxDepth && maxDepth !== -1) {
                return;
            }
            var i, curItem, items = [], children = [];
            items = items.concat(item);

            for (i = 0; i < items.length; i++) {
                curItem = items[i];
                visitFn(curItem);
                if (!tree.isLeaf(curItem)) {
                    children = children.concat(tree.getChildren(curItem))
                }
            }
            if (children.length > 0) {
                breadthFirst(children, visitFn, maxDepth, depth);
            }
            depth--;
        };

        tree.expend = function (item) {
            if (!tree.isLeaf(item)) {
                item.$$isExpend = true;
            }
        };
        tree.expendAll = function (item) {
            if (angular.isArray(item)) {
                angular.forEach(item, function (value) {
                    tree.depthFirst(value, tree.expend);
                });
            } else {
                tree.depthFirst(item, tree.expend);
            }

        };
        tree.expendToLevel = function (item, level) {
            tree.breadthFirst(item, tree.expend, level);
        };
        tree.collapse = function (item) {
            if (!tree.isLeaf(item)) {
                item.$$isExpend = false;
            }
        };
        tree.collapseAll = function (item) {
            tree.depthFirst(item, tree.collapse);
        };
        /**
         * 深度优先遍历
         * @param item  树节点
         * @param visitFn 访问函数
         */
        tree.depthFirst = function (item, visitFn) {
            var i, children;
            visitFn(item);
            if (!tree.isLeaf(item)) {
                children = tree.getChildren(item);
                for (i = 0; i < children.length; i++) {
                    tree.depthFirst(children[i], visitFn);
                }
            }
        };
        tree.breadthFirst = function (item, visitFn, maxDepth) {
            breadthFirst(item, visitFn, maxDepth);
        };
        tree.getChildren = function (item) {
            return item ? item.children : undefined;
        };
        tree.isLeaf = function (item) {
            return !item.children || !item.children.length;
        };
    }]);

'use strict';
angular.module('ui.bootstrap.dateparser', [])

.service('dateParser', ['$log', '$locale', 'orderByFilter', function($log, $locale, orderByFilter) {
  // Pulled from https://github.com/mbostock/d3/blob/master/src/format/requote.js
  var SPECIAL_CHARACTERS_REGEXP = /[\\\^\$\*\+\?\|\[\]\(\)\.\{\}]/g;

  this.parsers = {};

  var formatCodeToRegex = {
    'yyyy': {
      regex: '\\d{4}',
      apply: function(value) { this.year = +value; }
    },
    'yy': {
      regex: '\\d{2}',
      apply: function(value) { this.year = +value + 2000; }
    },
    'y': {
      regex: '\\d{1,4}',
      apply: function(value) { this.year = +value; }
    },
    'MMMM': {
      regex: $locale.DATETIME_FORMATS.MONTH.join('|'),
      apply: function(value) { this.month = $locale.DATETIME_FORMATS.MONTH.indexOf(value); }
    },
    'MMM': {
      regex: $locale.DATETIME_FORMATS.SHORTMONTH.join('|'),
      apply: function(value) { this.month = $locale.DATETIME_FORMATS.SHORTMONTH.indexOf(value); }
    },
    'MM': {
      regex: '0[1-9]|1[0-2]',
      apply: function(value) { this.month = value - 1; }
    },
    'M': {
      regex: '[1-9]|1[0-2]',
      apply: function(value) { this.month = value - 1; }
    },
    'dd': {
      regex: '[0-2][0-9]{1}|3[0-1]{1}',
      apply: function(value) { this.date = +value; }
    },
    'd': {
      regex: '[1-2]?[0-9]{1}|3[0-1]{1}',
      apply: function(value) { this.date = +value; }
    },
    'EEEE': {
      regex: $locale.DATETIME_FORMATS.DAY.join('|')
    },
    'EEE': {
      regex: $locale.DATETIME_FORMATS.SHORTDAY.join('|')
    },
    'HH': {
      regex: '(?:0|1)[0-9]|2[0-3]',
      apply: function(value) { this.hours = +value; }
    },
    'hh': {
      regex: '0[0-9]|1[0-2]',
      apply: function(value) { this.hours = +value; }
    },
    'H': {
      regex: '1?[0-9]|2[0-3]',
      apply: function(value) { this.hours = +value; }
    },
    'h': {
      regex: '[0-9]|1[0-2]',
      apply: function(value) { this.hours = +value; }
    },
    'mm': {
      regex: '[0-5][0-9]',
      apply: function(value) { this.minutes = +value; }
    },
    'm': {
      regex: '[0-9]|[1-5][0-9]',
      apply: function(value) { this.minutes = +value; }
    },
    'sss': {
      regex: '[0-9][0-9][0-9]',
      apply: function(value) { this.milliseconds = +value; }
    },
    'ss': {
      regex: '[0-5][0-9]',
      apply: function(value) { this.seconds = +value; }
    },
    's': {
      regex: '[0-9]|[1-5][0-9]',
      apply: function(value) { this.seconds = +value; }
    },
    'a': {
      regex: $locale.DATETIME_FORMATS.AMPMS.join('|'),
      apply: function(value) {
        if (this.hours === 12) {
          this.hours = 0;
        }

        if (value === 'PM') {
          this.hours += 12;
        }
      }
    }
  };

  function createParser(format) {
    var map = [], regex = format.split('');

    angular.forEach(formatCodeToRegex, function(data, code) {
      var index = format.indexOf(code);

      if (index > -1) {
        format = format.split('');

        regex[index] = '(' + data.regex + ')';
        format[index] = '$'; // Custom symbol to define consumed part of format
        for (var i = index + 1, n = index + code.length; i < n; i++) {
          regex[i] = '';
          format[i] = '$';
        }
        format = format.join('');

        map.push({ index: index, apply: data.apply });
      }
    });

    return {
      regex: new RegExp('^' + regex.join('') + '$'),
      map: orderByFilter(map, 'index')
    };
  }

  this.parse = function(input, format, baseDate) {
    if (!angular.isString(input) || !format) {
      return input;
    }

    format = $locale.DATETIME_FORMATS[format] || format;
    format = format.replace(SPECIAL_CHARACTERS_REGEXP, '\\$&');

    if (!this.parsers[format]) {
      this.parsers[format] = createParser(format);
    }

    var parser = this.parsers[format],
        regex = parser.regex,
        map = parser.map,
        results = input.match(regex);

    if (results && results.length) {
      var fields, dt;
      if (angular.isDate(baseDate) && !isNaN(baseDate.getTime())) {
        fields = {
          year: baseDate.getFullYear(),
          month: baseDate.getMonth(),
          date: baseDate.getDate(),
          hours: baseDate.getHours(),
          minutes: baseDate.getMinutes(),
          seconds: baseDate.getSeconds(),
          milliseconds: baseDate.getMilliseconds()
        };
      } else {
        if (baseDate) {
          $log.warn('dateparser:', 'baseDate is not a valid date');
        }
        fields = { year: 1900, month: 0, date: 1, hours: 0, minutes: 0, seconds: 0, milliseconds: 0 };
      }

      for (var i = 1, n = results.length; i < n; i++) {
        var mapper = map[i-1];
        if (mapper.apply) {
          mapper.apply.call(fields, results[i]);
        }
      }

      if (isValid(fields.year, fields.month, fields.date)) {
        dt = new Date(fields.year, fields.month, fields.date,
          fields.hours, fields.minutes, fields.seconds,
          fields.milliseconds || 0);
      }

      return dt;
    }
  };

  // Check if date is valid for specific month (and year for February).
  // Month: 0 = Jan, 1 = Feb, etc
  function isValid(year, month, date) {
    if (date < 1) {
      return false;
    }

    if (month === 1 && date > 28) {
      return date === 29 && ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0);
    }

    if (month === 3 || month === 5 || month === 8 || month === 10) {
      return date < 31;
    }

    return true;
  }
}]);

'use strict';
angular.module('ui.bootstrap.datepicker', ['ui.bootstrap.dateparser', 'ui.bootstrap.position'])

    .value('$datepickerSuppressError', false)

    .constant('datepickerConfig', {
        formatDay: 'dd',
        formatMonth: 'MMMM',
        formatYear: 'yyyy',
        formatDayHeader: 'EEE',
        formatDayTitle: 'yyyy MMMM',
        formatMonthTitle: 'yyyy',
        datepickerMode: 'day',
        minMode: 'day',
        maxMode: 'year',
        showWeeks: true,
        startingDay: 0,
        yearRange: 20,
        minDate: null,
        maxDate: null,
        shortcutPropagation: false
    })

    .controller('DatepickerController', ['$scope', '$attrs', '$parse', '$interpolate', '$log', 'dateFilter', 'datepickerConfig', '$datepickerSuppressError', function ($scope, $attrs, $parse, $interpolate, $log, dateFilter, datepickerConfig, $datepickerSuppressError) {
        var self = this,
            ngModelCtrl = {$setViewValue: angular.noop}; // nullModelCtrl;

        // Modes chain
        this.modes = ['day', 'month', 'year'];

        // Configuration attributes
        angular.forEach(['formatDay', 'formatMonth', 'formatYear', 'formatDayHeader', 'formatDayTitle', 'formatMonthTitle',
            'showWeeks', 'startingDay', 'yearRange', 'shortcutPropagation'], function (key, index) {
            self[key] = angular.isDefined($attrs[key]) ? (index < 6 ? $interpolate($attrs[key])($scope.$parent) : $scope.$parent.$eval($attrs[key])) : datepickerConfig[key];
        });

        // Watchable date attributes
        angular.forEach(['minDate', 'maxDate'], function (key) {
            if ($attrs[key]) {
                $scope.$parent.$watch($parse($attrs[key]), function (value) {
                    self[key] = value ? new Date(value) : null;
                    self.refreshView();
                });
            } else {
                self[key] = datepickerConfig[key] ? new Date(datepickerConfig[key]) : null;
            }
        });

        angular.forEach(['minMode', 'maxMode'], function (key) {
            if ($attrs[key]) {
                $scope.$parent.$watch($parse($attrs[key]), function (value) {
                    self[key] = angular.isDefined(value) ? value : $attrs[key];
                    $scope[key] = self[key];
                    if ((key == 'minMode' && self.modes.indexOf($scope.datepickerMode) < self.modes.indexOf(self[key])) || (key == 'maxMode' && self.modes.indexOf($scope.datepickerMode) > self.modes.indexOf(self[key]))) {
                        $scope.datepickerMode = self[key];
                    }
                });
            } else {
                self[key] = datepickerConfig[key] || null;
                $scope[key] = self[key];
            }
        });

        $scope.datepickerMode = $scope.datepickerMode || datepickerConfig.datepickerMode;
        $scope.uniqueId = 'datepicker-' + $scope.$id + '-' + Math.floor(Math.random() * 10000);

        if (angular.isDefined($attrs.initDate)) {
            this.activeDate = $scope.$parent.$eval($attrs.initDate) || new Date();
            $scope.$parent.$watch($attrs.initDate, function (initDate) {
                if (initDate && (ngModelCtrl.$isEmpty(ngModelCtrl.$modelValue) || ngModelCtrl.$invalid)) {
                    self.activeDate = initDate;
                    self.refreshView();
                }
            });
        } else {
            this.activeDate = new Date();
        }

        $scope.isActive = function (dateObject) {
            if (self.compare(dateObject.date, self.activeDate) === 0) {
                $scope.activeDateId = dateObject.uid;
                return true;
            }
            return false;
        };

        this.init = function (ngModelCtrl_) {
            ngModelCtrl = ngModelCtrl_;


            ngModelCtrl.$render = function () {
                self.render();
            };
        };
        $scope.time = {
            hours: 0,
            minutes: 0,
            seconds: 0
        };

        this.render = function () {
            if (ngModelCtrl.$viewValue) {
                var date = new Date(ngModelCtrl.$viewValue),
                    isValid = !isNaN(date);

                if (isValid) {
                    this.activeDate = date;
                    $scope.time.hours = date.getHours();
                    $scope.time.minutes = date.getMinutes();
                    $scope.time.seconds = date.getSeconds();
                } else if (!$datepickerSuppressError) {
                    $log.error('Datepicker directive: "ng-model" value must be a Date object, a number of milliseconds since 01.01.1970 or a string representing an RFC2822 or ISO 8601 date.');
                }
            }
            this.refreshView();
        };

        this.refreshView = function () {
            if (this.element) {
                this._refreshView();
                if (ngModelCtrl.$viewValue) {
                    var date = new Date(ngModelCtrl.$viewValue),
                        isValid = !isNaN(date);

                    if (isValid) {
                        $scope.time.hours = date.getHours();
                        $scope.time.minutes = date.getMinutes();
                        $scope.time.seconds = date.getSeconds();
                    } else if (!$datepickerSuppressError) {
                        $log.error('Datepicker directive: "ng-model" value must be a Date object, a number of milliseconds since 01.01.1970 or a string representing an RFC2822 or ISO 8601 date.');
                    }
                }
                var date = ngModelCtrl.$viewValue ? new Date(ngModelCtrl.$viewValue) : null;
                ngModelCtrl.$setValidity('dateDisabled', !date || (this.element && !this.isDisabled(date)));
            }
        };

        this.createDateObject = function (date, format) {
            var model = ngModelCtrl.$viewValue ? new Date(ngModelCtrl.$viewValue) : null;
            return {
                date: date,
                label: dateFilter(date, format),
                selected: model && this.compare(date, model) === 0,
                disabled: this.isDisabled(date),
                current: this.compare(date, new Date()) === 0,
                customClass: this.customClass(date)
            };
        };

        this.isDisabled = function (date) {
            return ((this.minDate && this.compare(date, this.minDate) < 0) || (this.maxDate && this.compare(date, this.maxDate) > 0) || ($attrs.dateDisabled && $scope.dateDisabled({
                date: date,
                mode: $scope.datepickerMode
            })));
        };

        this.customClass = function (date) {
            return $scope.customClass({date: date, mode: $scope.datepickerMode});
        };

        // Split array into smaller arrays
        this.split = function (arr, size) {
            var arrays = [];
            while (arr.length > 0) {
                arrays.push(arr.splice(0, size));
            }
            return arrays;
        };

        // Fix a hard-reprodusible bug with timezones
        // The bug depends on OS, browser, current timezone and current date
        // i.e.
        // var date = new Date(2014, 0, 1);
        // console.log(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours());
        // can result in "2013 11 31 23" because of the bug.
        this.fixTimeZone = function (date) {
            var hours = date.getHours();
            date.setHours(hours === 23 ? hours + 2 : 0);
        };

        $scope.select = function (date) {
            if ($scope.datepickerMode === self.minMode) {
                var dt = ngModelCtrl.$viewValue ? new Date(ngModelCtrl.$viewValue) : new Date(0, 0, 0, 0, 0, 0, 0);
                dt.setFullYear(date.getFullYear(), date.getMonth(), date.getDate());
                dt.setHours($scope.time.hours, $scope.time.minutes, $scope.time.seconds, 0);
                ngModelCtrl.$setViewValue(dt);
                ngModelCtrl.$render();
            } else {
                self.activeDate = date;
                $scope.datepickerMode = self.modes[self.modes.indexOf($scope.datepickerMode) - 1];
            }
        };

        $scope.select = function (date) {
            if ($scope.datepickerMode === self.minMode) {
                var dt = ngModelCtrl.$viewValue ? new Date(ngModelCtrl.$viewValue) : new Date(0, 0, 0, 0, 0, 0, 0);
                var hours = parseInt($scope.time.hours) || 0,
                    minutes = parseInt($scope.time.minutes) || 0,
                    seconds = parseInt($scope.time.seconds) || 0;
                dt.setFullYear(date.getFullYear(), date.getMonth(), date.getDate());
                if (hours < 24) {
                    dt.setHours(hours);
                }
                if (minutes < 60) {
                    dt.setMinutes(minutes);
                }
                if (seconds < 60) {
                    dt.setSeconds(seconds);
                }
                ngModelCtrl.$setViewValue(dt);
                ngModelCtrl.$render();
            } else {
                self.activeDate = date;
                $scope.datepickerMode = self.modes[self.modes.indexOf($scope.datepickerMode) - 1];
            }
        };

        $scope.setTime = function () {
            if (ngModelCtrl.$viewValue) {
                $scope.select(new Date(ngModelCtrl.$viewValue));
            }
        };

        $scope.move = function (direction) {
            var year = self.activeDate.getFullYear() + direction * (self.step.years || 0),
                month = self.activeDate.getMonth() + direction * (self.step.months || 0);
            self.activeDate.setFullYear(year, month, 1);
            self.refreshView();
        };

        $scope.toggleMode = function (direction) {
            direction = direction || 1;

            if (($scope.datepickerMode === self.maxMode && direction === 1) || ($scope.datepickerMode === self.minMode && direction === -1)) {
                return;
            }

            $scope.datepickerMode = self.modes[self.modes.indexOf($scope.datepickerMode) + direction];
        };

        // Key event mapper
        $scope.keys = {
            13: 'enter',
            32: 'space',
            33: 'pageup',
            34: 'pagedown',
            35: 'end',
            36: 'home',
            37: 'left',
            38: 'up',
            39: 'right',
            40: 'down'
        };

        var focusElement = function () {
            self.element[0].focus();
        };

        // Listen for focus requests from popup directive
        $scope.$on('datepicker.focus', focusElement);

        $scope.keydown = function (evt) {
            var key = $scope.keys[evt.which];

            if (!key || evt.shiftKey || evt.altKey) {
                return;
            }

            evt.preventDefault();
            if (!self.shortcutPropagation) {
                evt.stopPropagation();
            }

            if (key === 'enter' || key === 'space') {
                if (self.isDisabled(self.activeDate)) {
                    return; // do nothing
                }
                $scope.select(self.activeDate);
                focusElement();
            } else if (evt.ctrlKey && (key === 'up' || key === 'down')) {
                $scope.toggleMode(key === 'up' ? 1 : -1);
                focusElement();
            } else {
                self.handleKeyDown(key, evt);
                self.refreshView();
            }
        };
    }])

    .directive('datepicker', function () {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: function (element, attrs) {
                return attrs.templateUrl || 'components/datepicker/datepicker.html';
            },
            scope: {
                datepickerMode: '=?',
                dateDisabled: '&',
                customClass: '&',
                shortcutPropagation: '&?',
                showTime: '='
            },
            require: ['datepicker', '^ngModel'],
            controller: 'DatepickerController',
            controllerAs: 'datepicker',
            link: function (scope, element, attrs, ctrls) {
                var datepickerCtrl = ctrls[0], ngModelCtrl = ctrls[1];
                datepickerCtrl.init(ngModelCtrl);
            }
        };
    })

    .directive('daypicker', ['dateFilter', function (dateFilter) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'components/datepicker/day.html',
            require: '^datepicker',
            link: function (scope, element, attrs, ctrl) {
                scope.showWeeks = ctrl.showWeeks;
                ctrl.step = {months: 1};
                ctrl.element = element;

                var DAYS_IN_MONTH = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

                function getDaysInMonth(year, month) {
                    return ((month === 1) && (year % 4 === 0) && ((year % 100 !== 0) || (year % 400 === 0))) ? 29 : DAYS_IN_MONTH[month];
                }

                function getDates(startDate, n) {
                    var dates = new Array(n), current = new Date(startDate), i = 0, date;
                    while (i < n) {
                        date = new Date(current);
                        ctrl.fixTimeZone(date);
                        dates[i++] = date;
                        current.setDate(current.getDate() + 1);
                    }
                    return dates;
                }

                ctrl._refreshView = function () {
                    var year = ctrl.activeDate.getFullYear(),
                        month = ctrl.activeDate.getMonth(),
                        firstDayOfMonth = new Date(year, month, 1),
                        difference = ctrl.startingDay - firstDayOfMonth.getDay(),
                        numDisplayedFromPreviousMonth = (difference > 0) ? 7 - difference : -difference,
                        firstDate = new Date(firstDayOfMonth);

                    if (numDisplayedFromPreviousMonth > 0) {
                        firstDate.setDate(-numDisplayedFromPreviousMonth + 1);
                    }

                    // 42 is the number of days on a six-month calendar
                    var days = getDates(firstDate, 42);
                    for (var i = 0; i < 42; i++) {
                        days[i] = angular.extend(ctrl.createDateObject(days[i], ctrl.formatDay), {
                            secondary: days[i].getMonth() !== month,
                            uid: scope.uniqueId + '-' + i
                        });
                    }

                    scope.labels = new Array(7);
                    for (var j = 0; j < 7; j++) {
                        scope.labels[j] = {
                            abbr: dateFilter(days[j].date, ctrl.formatDayHeader),
                            full: dateFilter(days[j].date, 'EEEE')
                        };
                    }

                    scope.title = dateFilter(ctrl.activeDate, ctrl.formatDayTitle);
                    scope.rows = ctrl.split(days, 7);

                    if (scope.showWeeks) {
                        scope.weekNumbers = [];
                        var thursdayIndex = (4 + 7 - ctrl.startingDay) % 7,
                            numWeeks = scope.rows.length;
                        for (var curWeek = 0; curWeek < numWeeks; curWeek++) {
                            scope.weekNumbers.push(
                                getISO8601WeekNumber(scope.rows[curWeek][thursdayIndex].date));
                        }
                    }
                };

                ctrl.compare = function (date1, date2) {
                    return (new Date(date1.getFullYear(), date1.getMonth(), date1.getDate()) - new Date(date2.getFullYear(), date2.getMonth(), date2.getDate()));
                };

                function getISO8601WeekNumber(date) {
                    var checkDate = new Date(date);
                    checkDate.setDate(checkDate.getDate() + 4 - (checkDate.getDay() || 7)); // Thursday
                    var time = checkDate.getTime();
                    checkDate.setMonth(0); // Compare with Jan 1
                    checkDate.setDate(1);
                    return Math.floor(Math.round((time - checkDate) / 86400000) / 7) + 1;
                }

                ctrl.handleKeyDown = function (key, evt) {
                    var date = ctrl.activeDate.getDate();

                    if (key === 'left') {
                        date = date - 1;   // up
                    } else if (key === 'up') {
                        date = date - 7;   // down
                    } else if (key === 'right') {
                        date = date + 1;   // down
                    } else if (key === 'down') {
                        date = date + 7;
                    } else if (key === 'pageup' || key === 'pagedown') {
                        var month = ctrl.activeDate.getMonth() + (key === 'pageup' ? -1 : 1);
                        ctrl.activeDate.setMonth(month, 1);
                        date = Math.min(getDaysInMonth(ctrl.activeDate.getFullYear(), ctrl.activeDate.getMonth()), date);
                    } else if (key === 'home') {
                        date = 1;
                    } else if (key === 'end') {
                        date = getDaysInMonth(ctrl.activeDate.getFullYear(), ctrl.activeDate.getMonth());
                    }
                    ctrl.activeDate.setDate(date);
                };

                ctrl.refreshView();
            }
        };
    }])

    .directive('monthpicker', ['dateFilter', function (dateFilter) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'components/datepicker/month.html',
            require: '^datepicker',
            link: function (scope, element, attrs, ctrl) {
                ctrl.step = {years: 1};
                ctrl.element = element;

                ctrl._refreshView = function () {
                    var months = new Array(12),
                        year = ctrl.activeDate.getFullYear(),
                        date;

                    for (var i = 0; i < 12; i++) {
                        date = new Date(year, i, 1);
                        ctrl.fixTimeZone(date);
                        months[i] = angular.extend(ctrl.createDateObject(date, ctrl.formatMonth), {
                            uid: scope.uniqueId + '-' + i
                        });
                    }

                    scope.title = dateFilter(ctrl.activeDate, ctrl.formatMonthTitle);

                    scope.rows = ctrl.split(months, 3);
                };

                ctrl.compare = function (date1, date2) {
                    return new Date(date1.getFullYear(), date1.getMonth()) - new Date(date2.getFullYear(), date2.getMonth());
                };

                ctrl.handleKeyDown = function (key, evt) {
                    var date = ctrl.activeDate.getMonth();

                    if (key === 'left') {
                        date = date - 1;   // up
                    } else if (key === 'up') {
                        date = date - 3;   // down
                    } else if (key === 'right') {
                        date = date + 1;   // down
                    } else if (key === 'down') {
                        date = date + 3;
                    } else if (key === 'pageup' || key === 'pagedown') {
                        var year = ctrl.activeDate.getFullYear() + (key === 'pageup' ? -1 : 1);
                        ctrl.activeDate.setFullYear(year);
                    } else if (key === 'home') {
                        date = 0;
                    } else if (key === 'end') {
                        date = 11;
                    }
                    ctrl.activeDate.setMonth(date);
                };

                ctrl.refreshView();
            }
        };
    }])

    .directive('yearpicker', ['dateFilter', function (dateFilter) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'components/datepicker/year.html',
            require: '^datepicker',
            link: function (scope, element, attrs, ctrl) {
                var range = ctrl.yearRange;

                ctrl.step = {years: range};
                ctrl.element = element;

                function getStartingYear(year) {
                    return parseInt((year - 1) / range, 10) * range + 1;
                }

                ctrl._refreshView = function () {
                    var years = new Array(range), date;

                    for (var i = 0, start = getStartingYear(ctrl.activeDate.getFullYear()); i < range; i++) {
                        date = new Date(start + i, 0, 1);
                        ctrl.fixTimeZone(date);
                        years[i] = angular.extend(ctrl.createDateObject(date, ctrl.formatYear), {
                            uid: scope.uniqueId + '-' + i
                        });
                    }

                    scope.title = [years[0].label, years[range - 1].label].join(' - ');
                    scope.rows = ctrl.split(years, 5);
                };

                ctrl.compare = function (date1, date2) {
                    return date1.getFullYear() - date2.getFullYear();
                };

                ctrl.handleKeyDown = function (key, evt) {
                    var date = ctrl.activeDate.getFullYear();

                    if (key === 'left') {
                        date = date - 1;   // up
                    } else if (key === 'up') {
                        date = date - 5;   // down
                    } else if (key === 'right') {
                        date = date + 1;   // down
                    } else if (key === 'down') {
                        date = date + 5;
                    } else if (key === 'pageup' || key === 'pagedown') {
                        date += (key === 'pageup' ? -1 : 1) * ctrl.step.years;
                    } else if (key === 'home') {
                        date = getStartingYear(ctrl.activeDate.getFullYear());
                    } else if (key === 'end') {
                        date = getStartingYear(ctrl.activeDate.getFullYear()) + range - 1;
                    }
                    ctrl.activeDate.setFullYear(date);
                };

                ctrl.refreshView();
            }
        };
    }])

    .constant('datepickerPopupConfig', {
        datepickerPopup: 'yyyy-MM-dd',
        datepickerPopupTemplateUrl: 'components/datepicker/popup.html',
        datepickerTemplateUrl: 'components/datepicker/datepicker.html',
        html5Types: {
            date: 'yyyy-MM-dd',
            'datetime-local': 'yyyy-MM-ddTHH:mm:ss.sss',
            'month': 'yyyy-MM'
        },
        currentText: 'Today',
        clearText: 'Clear',
        closeText: 'Done',
        closeOnDateSelection: true,
        appendToBody: false,
        showButtonBar: true,
        onOpenFocus: true
    })

    .directive('datepickerPopup', ['$compile', '$parse', '$document', '$rootScope', '$position', 'dateFilter', 'dateParser', 'datepickerPopupConfig', '$timeout',
        function ($compile, $parse, $document, $rootScope, $position, dateFilter, dateParser, datepickerPopupConfig, $timeout) {
            return {
                restrict: 'EA',
                require: 'ngModel',
                scope: {
                    isOpen: '=?',
                    currentText: '@',
                    clearText: '@',
                    closeText: '@',
                    dateDisabled: '&',
                    customClass: '&',
                    showTime: '='
                },
                link: function (scope, element, attrs, ngModel) {
                    var dateFormat,
                        closeOnDateSelection = angular.isDefined(attrs.closeOnDateSelection) ? scope.$parent.$eval(attrs.closeOnDateSelection) : datepickerPopupConfig.closeOnDateSelection,
                        appendToBody = angular.isDefined(attrs.datepickerAppendToBody) ? scope.$parent.$eval(attrs.datepickerAppendToBody) : datepickerPopupConfig.appendToBody,
                        onOpenFocus = angular.isDefined(attrs.onOpenFocus) ? scope.$parent.$eval(attrs.onOpenFocus) : datepickerPopupConfig.onOpenFocus,
                        datepickerPopupTemplateUrl = angular.isDefined(attrs.datepickerPopupTemplateUrl) ? attrs.datepickerPopupTemplateUrl : datepickerPopupConfig.datepickerPopupTemplateUrl,
                        datepickerTemplateUrl = angular.isDefined(attrs.datepickerTemplateUrl) ? attrs.datepickerTemplateUrl : datepickerPopupConfig.datepickerTemplateUrl,
                        cache = {};

                    scope.showButtonBar = angular.isDefined(attrs.showButtonBar) ? scope.$parent.$eval(attrs.showButtonBar) : datepickerPopupConfig.showButtonBar;

                    scope.getText = function (key) {
                        return scope[key + 'Text'] || datepickerPopupConfig[key + 'Text'];
                    };

                    scope.isDisabled = function (date) {
                        if (date === 'today') {
                            date = new Date();
                        }

                        return ((scope.watchData.minDate && scope.compare(date, cache.minDate) < 0) ||
                        (scope.watchData.maxDate && scope.compare(date, cache.maxDate) > 0));
                    };

                    scope.compare = function (date1, date2) {
                        return (new Date(date1.getFullYear(), date1.getMonth(), date1.getDate()) - new Date(date2.getFullYear(), date2.getMonth(), date2.getDate()));
                    };

                    var isHtml5DateInput = false;
                    if (datepickerPopupConfig.html5Types[attrs.type]) {
                        dateFormat = datepickerPopupConfig.html5Types[attrs.type];
                        isHtml5DateInput = true;
                    } else {
                        dateFormat = attrs.datepickerPopup || datepickerPopupConfig.datepickerPopup;
                        attrs.$observe('datepickerPopup', function (value, oldValue) {
                            var newDateFormat = value || datepickerPopupConfig.datepickerPopup;
                            // Invalidate the $modelValue to ensure that formatters re-run
                            // FIXME: Refactor when PR is merged: https://github.com/angular/angular.js/pull/10764
                            if (newDateFormat !== dateFormat) {
                                dateFormat = newDateFormat;
                                ngModel.$modelValue = null;

                                if (!dateFormat) {
                                    throw new Error('datepickerPopup must have a date format specified.');
                                }
                            }
                        });
                    }

                    if (!dateFormat) {
                        throw new Error('datepickerPopup must have a date format specified.');
                    }

                    if (isHtml5DateInput && attrs.datepickerPopup) {
                        throw new Error('HTML5 date input types do not support custom formats.');
                    }

                    // popup element used to display calendar
                    var popupEl = angular.element('<div datepicker-popup-wrap><div datepicker></div></div>');
                    popupEl.attr({
                        'ng-model': 'date',
                        'ng-change': 'dateSelection(date)',
                        'template-url': datepickerPopupTemplateUrl
                    });

                    function cameltoDash(string) {
                        return string.replace(/([A-Z])/g, function ($1) {
                            return '-' + $1.toLowerCase();
                        });
                    }

                    // datepicker element
                    var datepickerEl = angular.element(popupEl.children()[0]);
                    datepickerEl.attr('template-url', datepickerTemplateUrl);

                    if (isHtml5DateInput) {
                        if (attrs.type === 'month') {
                            datepickerEl.attr('datepicker-mode', '"month"');
                            datepickerEl.attr('min-mode', 'month');
                        }
                    }

                    if (attrs.datepickerOptions) {
                        var options = scope.$parent.$eval(attrs.datepickerOptions);
                        if (options && options.initDate) {
                            scope.initDate = options.initDate;
                            datepickerEl.attr('init-date', 'initDate');
                            delete options.initDate;
                        }
                        angular.forEach(options, function (value, option) {
                            datepickerEl.attr(cameltoDash(option), value);
                        });
                    }

                    scope.watchData = {};
                    angular.forEach(['minMode', 'maxMode', 'minDate', 'maxDate', 'datepickerMode', 'initDate', 'shortcutPropagation'], function (key) {
                        if (attrs[key]) {
                            var getAttribute = $parse(attrs[key]);
                            scope.$parent.$watch(getAttribute, function (value) {
                                scope.watchData[key] = value;
                                if (key === 'minDate' || key === 'maxDate') {
                                    cache[key] = new Date(value);
                                }
                            });
                            datepickerEl.attr(cameltoDash(key), 'watchData.' + key);

                            // Propagate changes from datepicker to outside
                            if (key === 'datepickerMode') {
                                var setAttribute = getAttribute.assign;
                                scope.$watch('watchData.' + key, function (value, oldvalue) {
                                    if (angular.isFunction(setAttribute) && value !== oldvalue) {
                                        setAttribute(scope.$parent, value);
                                    }
                                });
                            }
                        }
                    });
                    if (attrs.dateDisabled) {
                        datepickerEl.attr('date-disabled', 'dateDisabled({ date: date, mode: mode })');
                    }

                    if (attrs.showWeeks) {
                        datepickerEl.attr('show-weeks', attrs.showWeeks);
                    }

                    if (attrs.customClass) {
                        datepickerEl.attr('custom-class', 'customClass({ date: date, mode: mode })');
                    }

                    function parseDate(viewValue) {
                        if (angular.isNumber(viewValue)) {
                            // presumably timestamp to date object
                            viewValue = new Date(viewValue);
                        }

                        if (!viewValue) {
                            return null;
                        } else if (angular.isDate(viewValue) && !isNaN(viewValue)) {
                            return viewValue;
                        } else if (angular.isString(viewValue)) {
                            var date = dateParser.parse(viewValue, dateFormat, scope.date);
                            if (isNaN(date)) {
                                return undefined;
                            } else {
                                return date;
                            }
                        } else {
                            return undefined;
                        }
                    }

                    function validator(modelValue, viewValue) {
                        var value = modelValue || viewValue;

                        if (!attrs.ngRequired && !value) {
                            return true;
                        }

                        if (angular.isNumber(value)) {
                            value = new Date(value);
                        }
                        if (!value) {
                            return true;
                        } else if (angular.isDate(value) && !isNaN(value)) {
                            return true;
                        } else if (angular.isString(value)) {
                            var date = dateParser.parse(value, dateFormat);
                            return !isNaN(date);
                        } else {
                            return false;
                        }
                    }

                    if (!isHtml5DateInput) {
                        // Internal API to maintain the correct ng-invalid-[key] class
                        ngModel.$$parserName = 'date';
                        ngModel.$validators.date = validator;
                        ngModel.$parsers.unshift(parseDate);
                        ngModel.$formatters.push(function (value) {
                            scope.date = value;
                            return ngModel.$isEmpty(value) ? value : dateFilter(value, dateFormat);
                        });
                    } else {
                        ngModel.$formatters.push(function (value) {
                            scope.date = value;
                            return value;
                        });
                    }

                    // Inner change
                    scope.dateSelection = function (dt) {
                        if (angular.isDefined(dt)) {
                            scope.date = dt;
                        }
                        var date = scope.date ? dateFilter(scope.date, dateFormat) : null; // Setting to NULL is necessary for form validators to function
                        element.val(date);
                        ngModel.$setViewValue(date);

                        if (closeOnDateSelection) {
                            scope.isOpen = false;
                            element[0].focus();
                        }
                    };

                    // Detect changes in the view from the text box
                    ngModel.$viewChangeListeners.push(function () {
                        scope.date = dateParser.parse(ngModel.$viewValue, dateFormat, scope.date);
                    });

                    var documentClickBind = function (event) {
                        if (scope.isOpen && !(element[0].contains(event.target) || popupEl[0].contains(event.target))) {
                            scope.$apply(function () {
                                scope.isOpen = false;
                            });
                        }
                    };

                    var inputKeydownBind = function (evt) {
                        if (evt.which === 27 && scope.isOpen) {
                            evt.preventDefault();
                            evt.stopPropagation();
                            scope.$apply(function () {
                                scope.isOpen = false;
                            });
                            element[0].focus();
                        } else if (evt.which === 40 && !scope.isOpen) {
                            evt.preventDefault();
                            evt.stopPropagation();
                            scope.$apply(function () {
                                scope.isOpen = true;
                            });
                        }
                    };
                    element.bind('keydown', inputKeydownBind);

                    scope.keydown = function (evt) {
                        if (evt.which === 27) {
                            scope.isOpen = false;
                            element[0].focus();
                        }
                    };

                    scope.$watch('isOpen', function (value) {
                        if (value) {
                            scope.position = appendToBody ? $position.offset(element) : $position.position(element);
                            scope.position.top = scope.position.top + element.prop('offsetHeight');

                            $timeout(function () {
                                if (onOpenFocus) {
                                    scope.$broadcast('datepicker.focus');
                                }
                                $document.bind('click', documentClickBind);
                            }, 0, false);
                        } else {
                            $document.unbind('click', documentClickBind);
                        }
                    });

                    scope.select = function (date) {
                        if (date === 'today') {
                            var today = new Date();
                            if (angular.isDate(scope.date)) {
                                date = new Date(scope.date);
                                date.setFullYear(today.getFullYear(), today.getMonth(), today.getDate());
                            } else {
                                date = new Date(today.setHours(0, 0, 0, 0));
                            }
                        }
                        scope.dateSelection(date);
                    };

                    scope.close = function () {
                        scope.isOpen = false;
                        element[0].focus();
                    };

                    var $popup = $compile(popupEl)(scope);
                    // Prevent jQuery cache memory leak (template is now redundant after linking)
                    popupEl.remove();

                    if (appendToBody) {
                        $document.find('body').append($popup);
                    } else {
                        element.after($popup);
                    }

                    scope.$on('$destroy', function () {
                        if (scope.isOpen === true) {
                            if (!$rootScope.$$phase) {
                                scope.$apply(function () {
                                    scope.isOpen = false;
                                });
                            }
                        }

                        $popup.remove();
                        element.unbind('keydown', inputKeydownBind);
                        $document.unbind('click', documentClickBind);
                    });
                }
            };
        }])

    .directive('datepickerPopupWrap', function () {
        return {
            restrict: 'EA',
            replace: true,
            transclude: true,
            templateUrl: function (element, attrs) {
                return attrs.templateUrl || 'components/datepicker/popup.html';
            }
        };
    });

'use strict';
angular.module('ui.bootstrap.position', [])

/**
 * A set of utility methods that can be use to retrieve position of DOM elements.
 * It is meant to be used where we need to absolute-position DOM elements in
 * relation to other, existing elements (this is the case for tooltips, popovers,
 * typeahead suggestions etc.).
 */
  .factory('$position', ['$document', '$window', function($document, $window) {
    function getStyle(el, cssprop) {
      if (el.currentStyle) { //IE
        return el.currentStyle[cssprop];
      } else if ($window.getComputedStyle) {
        return $window.getComputedStyle(el)[cssprop];
      }
      // finally try and get inline style
      return el.style[cssprop];
    }

    /**
     * Checks if a given element is statically positioned
     * @param element - raw DOM element
     */
    function isStaticPositioned(element) {
      return (getStyle(element, 'position') || 'static' ) === 'static';
    }

    /**
     * returns the closest, non-statically positioned parentOffset of a given element
     * @param element
     */
    var parentOffsetEl = function(element) {
      var docDomEl = $document[0];
      var offsetParent = element.offsetParent || docDomEl;
      while (offsetParent && offsetParent !== docDomEl && isStaticPositioned(offsetParent) ) {
        offsetParent = offsetParent.offsetParent;
      }
      return offsetParent || docDomEl;
    };

    return {
      /**
       * Provides read-only equivalent of jQuery's position function:
       * http://api.jquery.com/position/
       */
      position: function(element) {
        var elBCR = this.offset(element);
        var offsetParentBCR = { top: 0, left: 0 };
        var offsetParentEl = parentOffsetEl(element[0]);
        if (offsetParentEl != $document[0]) {
          offsetParentBCR = this.offset(angular.element(offsetParentEl));
          offsetParentBCR.top += offsetParentEl.clientTop - offsetParentEl.scrollTop;
          offsetParentBCR.left += offsetParentEl.clientLeft - offsetParentEl.scrollLeft;
        }

        var boundingClientRect = element[0].getBoundingClientRect();
        return {
          width: boundingClientRect.width || element.prop('offsetWidth'),
          height: boundingClientRect.height || element.prop('offsetHeight'),
          top: elBCR.top - offsetParentBCR.top,
          left: elBCR.left - offsetParentBCR.left
        };
      },

      /**
       * Provides read-only equivalent of jQuery's offset function:
       * http://api.jquery.com/offset/
       */
      offset: function(element) {
        var boundingClientRect = element[0].getBoundingClientRect();
        return {
          width: boundingClientRect.width || element.prop('offsetWidth'),
          height: boundingClientRect.height || element.prop('offsetHeight'),
          top: boundingClientRect.top + ($window.pageYOffset || $document[0].documentElement.scrollTop),
          left: boundingClientRect.left + ($window.pageXOffset || $document[0].documentElement.scrollLeft)
        };
      },

      /**
       * Provides coordinates for the targetEl in relation to hostEl
       */
      positionElements: function(hostEl, targetEl, positionStr, appendToBody) {
        var positionStrParts = positionStr.split('-');
        var pos0 = positionStrParts[0], pos1 = positionStrParts[1] || 'center';

        var hostElPos,
          targetElWidth,
          targetElHeight,
          targetElPos;

        hostElPos = appendToBody ? this.offset(hostEl) : this.position(hostEl);

        targetElWidth = targetEl.prop('offsetWidth');
        targetElHeight = targetEl.prop('offsetHeight');

        var shiftWidth = {
          center: function() {
            return hostElPos.left + hostElPos.width / 2 - targetElWidth / 2;
          },
          left: function() {
            return hostElPos.left;
          },
          right: function() {
            return hostElPos.left + hostElPos.width;
          }
        };

        var shiftHeight = {
          center: function() {
            return hostElPos.top + hostElPos.height / 2 - targetElHeight / 2;
          },
          top: function() {
            return hostElPos.top;
          },
          bottom: function() {
            return hostElPos.top + hostElPos.height;
          }
        };

        switch (pos0) {
          case 'right':
            targetElPos = {
              top: shiftHeight[pos1](),
              left: shiftWidth[pos0]()
            };
            break;
          case 'left':
            targetElPos = {
              top: shiftHeight[pos1](),
              left: hostElPos.left - targetElWidth
            };
            break;
          case 'bottom':
            targetElPos = {
              top: shiftHeight[pos0](),
              left: shiftWidth[pos1]()
            };
            break;
          default:
            targetElPos = {
              top: hostElPos.top - targetElHeight,
              left: shiftWidth[pos1]()
            };
            break;
        }

        return targetElPos;
      }
    };
  }]);

'use strict';
angular.module('ui.bootstrap.bindHtml', [])

  .value('$bindHtmlUnsafeSuppressDeprecated', false)

  .directive('bindHtmlUnsafe', ['$log', '$bindHtmlUnsafeSuppressDeprecated', function ($log, $bindHtmlUnsafeSuppressDeprecated) {
    return function (scope, element, attr) {
      if (!$bindHtmlUnsafeSuppressDeprecated) {
        $log.warn('bindHtmlUnsafe is now deprecated. Use ngBindHtml instead');
      }
      element.addClass('ng-binding').data('$binding', attr.bindHtmlUnsafe);
      scope.$watch(attr.bindHtmlUnsafe, function bindHtmlUnsafeWatchAction(value) {
        element.html(value || '');
      });
    };
  }]);
'use strict';
/**
 * The following features are still outstanding: animation as a
 * function, placement as a function, inside, support for more triggers than
 * just mouse enter/leave, html tooltips, and selector delegation.
 */
angular.module('ui.bootstrap.tooltip', ['ui.bootstrap.position', 'ui.bootstrap.bindHtml'])

/**
 * The $tooltip service creates tooltip- and popover-like directives as well as
 * houses global options for them.
 */
.provider('$tooltip', function() {
  // The default options tooltip and popover.
  var defaultOptions = {
    placement: 'top',
    animation: true,
    popupDelay: 0,
    useContentExp: false
  };

  // Default hide triggers for each show trigger
  var triggerMap = {
    'mouseenter': 'mouseleave',
    'click': 'click',
    'focus': 'blur',
    'none': ''
  };

  // The options specified to the provider globally.
  var globalOptions = {};

  /**
   * `options({})` allows global configuration of all tooltips in the
   * application.
   *
   *   var app = angular.module( 'App', ['ui.bootstrap.tooltip'], function( $tooltipProvider ) {
   *     // place tooltips left instead of top by default
   *     $tooltipProvider.options( { placement: 'left' } );
   *   });
   */
	this.options = function(value) {
		angular.extend(globalOptions, value);
	};

  /**
   * This allows you to extend the set of trigger mappings available. E.g.:
   *
   *   $tooltipProvider.setTriggers( 'openTrigger': 'closeTrigger' );
   */
  this.setTriggers = function setTriggers(triggers) {
    angular.extend(triggerMap, triggers);
  };

  /**
   * This is a helper function for translating camel-case to snake-case.
   */
  function snake_case(name) {
    var regexp = /[A-Z]/g;
    var separator = '-';
    return name.replace(regexp, function(letter, pos) {
      return (pos ? separator : '') + letter.toLowerCase();
    });
  }

  /**
   * Returns the actual instance of the $tooltip service.
   * TODO support multiple triggers
   */
  this.$get = ['$window', '$compile', '$timeout', '$document', '$position', '$interpolate', '$rootScope', '$parse', function($window, $compile, $timeout, $document, $position, $interpolate, $rootScope, $parse) {
    return function $tooltip(type, prefix, defaultTriggerShow, options) {
      options = angular.extend({}, defaultOptions, globalOptions, options);

      /**
       * Returns an object of show and hide triggers.
       *
       * If a trigger is supplied,
       * it is used to show the tooltip; otherwise, it will use the `trigger`
       * option passed to the `$tooltipProvider.options` method; else it will
       * default to the trigger supplied to this directive factory.
       *
       * The hide trigger is based on the show trigger. If the `trigger` option
       * was passed to the `$tooltipProvider.options` method, it will use the
       * mapped trigger from `triggerMap` or the passed trigger if the map is
       * undefined; otherwise, it uses the `triggerMap` value of the show
       * trigger; else it will just use the show trigger.
       */
      function getTriggers(trigger) {
        var show = (trigger || options.trigger || defaultTriggerShow).split(' ');
        var hide = show.map(function(trigger) {
          return triggerMap[trigger] || trigger;
        });
        return {
          show: show,
          hide: hide
        };
      }

      var directiveName = snake_case(type);

      var startSym = $interpolate.startSymbol();
      var endSym = $interpolate.endSymbol();
      var template =
        '<div '+ directiveName +'-popup '+
          'title="'+startSym+'title'+endSym+'" '+
          (options.useContentExp ?
            'content-exp="contentExp()" ' :
            'content="'+startSym+'content'+endSym+'" ') +
          'placement="'+startSym+'placement'+endSym+'" '+
          'popup-class="'+startSym+'popupClass'+endSym+'" '+
          'animation="animation" '+
          'is-open="isOpen"'+
          'origin-scope="origScope" '+
          '>'+
        '</div>';

      return {
        restrict: 'EA',
        compile: function(tElem, tAttrs) {
          var tooltipLinker = $compile( template );

          return function link(scope, element, attrs, tooltipCtrl) {
            var tooltip;
            var tooltipLinkedScope;
            var transitionTimeout;
            var popupTimeout;
            var positionTimeout;
            var appendToBody = angular.isDefined(options.appendToBody) ? options.appendToBody : false;
            var triggers = getTriggers(undefined);
            var hasEnableExp = angular.isDefined(attrs[prefix + 'Enable']);
            var ttScope = scope.$new(true);
            var repositionScheduled = false;
            var isOpenExp = angular.isDefined(attrs[prefix + 'IsOpen']) ? $parse(attrs[prefix + 'IsOpen']) : false;

            var positionTooltip = function() {
              if (!tooltip) { return; }

              if (!positionTimeout) {
                positionTimeout = $timeout(function() {
                  // Reset the positioning and box size for correct width and height values.
                  tooltip.css({ top: 0, left: 0, width: 'auto', height: 'auto' });

                  var ttBox = $position.position(tooltip);
                  var ttCss = $position.positionElements(element, tooltip, ttScope.placement, appendToBody);
                  ttCss.top += 'px';
                  ttCss.left += 'px';

                  ttCss.width = ttBox.width + 'px';
                  ttCss.height = ttBox.height + 'px';

                  // Now set the calculated positioning and size.
                  tooltip.css(ttCss);

                  positionTimeout = null;

                }, 0, false);
              }
            };

            // Set up the correct scope to allow transclusion later
            ttScope.origScope = scope;

            // By default, the tooltip is not open.
            // TODO add ability to start tooltip opened
            ttScope.isOpen = false;

            function toggleTooltipBind() {
              if (!ttScope.isOpen) {
                showTooltipBind();
              } else {
                hideTooltipBind();
              }
            }

            // Show the tooltip with delay if specified, otherwise show it immediately
            function showTooltipBind() {
              if (hasEnableExp && !scope.$eval(attrs[prefix + 'Enable'])) {
                return;
              }

              prepareTooltip();

              if (ttScope.popupDelay) {
                // Do nothing if the tooltip was already scheduled to pop-up.
                // This happens if show is triggered multiple times before any hide is triggered.
                if (!popupTimeout) {
                  popupTimeout = $timeout(show, ttScope.popupDelay, false);
                }
              } else {
                show();
              }
            }

            function hideTooltipBind () {
              hide();
              if (!$rootScope.$$phase) {
                $rootScope.$digest();
              }
            }

            // Show the tooltip popup element.
            function show() {
              popupTimeout = null;

              // If there is a pending remove transition, we must cancel it, lest the
              // tooltip be mysteriously removed.
              if (transitionTimeout) {
                $timeout.cancel(transitionTimeout);
                transitionTimeout = null;
              }

              // Don't show empty tooltips.
              if (!(options.useContentExp ? ttScope.contentExp() : ttScope.content)) {
                return angular.noop;
              }

              createTooltip();

              // And show the tooltip.
              ttScope.isOpen = true;
              if (isOpenExp) {
                isOpenExp.assign(ttScope.origScope, ttScope.isOpen);
              }

              if (!$rootScope.$$phase) {
                ttScope.$apply(); // digest required as $apply is not called
              }

              tooltip.css({ display: 'block' });

              positionTooltip();
            }

            // Hide the tooltip popup element.
            function hide() {
              // First things first: we don't show it anymore.
              ttScope.isOpen = false;
              if (isOpenExp) {
                isOpenExp.assign(ttScope.origScope, ttScope.isOpen);
              }

              //if tooltip is going to be shown after delay, we must cancel this
              $timeout.cancel(popupTimeout);
              popupTimeout = null;

              $timeout.cancel(positionTimeout);
              positionTimeout = null;

              // And now we remove it from the DOM. However, if we have animation, we
              // need to wait for it to expire beforehand.
              // FIXME: this is a placeholder for a port of the transitions library.
              if (ttScope.animation) {
                if (!transitionTimeout) {
                  transitionTimeout = $timeout(removeTooltip, 500);
                }
              } else {
                removeTooltip();
              }
            }

            function createTooltip() {
              // There can only be one tooltip element per directive shown at once.
              if (tooltip) {
                removeTooltip();
              }
              tooltipLinkedScope = ttScope.$new();
              tooltip = tooltipLinker(tooltipLinkedScope, function(tooltip) {
                if (appendToBody) {
                  $document.find('body').append(tooltip);
                } else {
                  element.after(tooltip);
                }
              });

              if (options.useContentExp) {
                tooltipLinkedScope.$watch('contentExp()', function(val) {
                  if (!val && ttScope.isOpen) {
                    hide();
                  }
                });

                tooltipLinkedScope.$watch(function() {
                  if (!repositionScheduled) {
                    repositionScheduled = true;
                    tooltipLinkedScope.$$postDigest(function() {
                      repositionScheduled = false;
                      if (ttScope.isOpen) {
                        positionTooltip();
                      }
                    });
                  }
                });

              }
            }

            function removeTooltip() {
              transitionTimeout = null;
              if (tooltip) {
                tooltip.remove();
                tooltip = null;
              }
              if (tooltipLinkedScope) {
                tooltipLinkedScope.$destroy();
                tooltipLinkedScope = null;
              }
            }

            function prepareTooltip() {
              prepPopupClass();
              prepPlacement();
              prepPopupDelay();
            }

            ttScope.contentExp = function() {
              return scope.$eval(attrs[type]);
            };

            /**
             * Observe the relevant attributes.
             */
            if (!options.useContentExp) {
              attrs.$observe(type, function(val) {
                ttScope.content = val;

                if (!val && ttScope.isOpen) {
                  hide();
                } else {
                  positionTooltip();
                }
              });
            }

            attrs.$observe('disabled', function(val) {
              if (popupTimeout && val) {
                $timeout.cancel(popupTimeout);
                popupTimeout = null;
              }

              if (val && ttScope.isOpen) {
                hide();
              }
            });

            attrs.$observe(prefix + 'Title', function(val) {
              ttScope.title = val;
              positionTooltip();
            });

            attrs.$observe(prefix + 'Placement', function() {
              if (ttScope.isOpen) {
                prepPlacement();
                positionTooltip();
              }
            });

            if (isOpenExp) {
              scope.$watch(isOpenExp, function(val) {
                if (val !== ttScope.isOpen) {
                  toggleTooltipBind();
                }
              });
            }

            function prepPopupClass() {
              ttScope.popupClass = attrs[prefix + 'Class'];
            }

            function prepPlacement() {
              var val = attrs[prefix + 'Placement'];
              ttScope.placement = angular.isDefined(val) ? val : options.placement;
            }

            function prepPopupDelay() {
              var val = attrs[prefix + 'PopupDelay'];
              var delay = parseInt(val, 10);
              ttScope.popupDelay = !isNaN(delay) ? delay : options.popupDelay;
            }

            var unregisterTriggers = function() {
              triggers.show.forEach(function(trigger) {
                element.unbind(trigger, showTooltipBind);
              });
              triggers.hide.forEach(function(trigger) {
                element.unbind(trigger, hideTooltipBind);
              });
            };

            function prepTriggers() {
              var val = attrs[prefix + 'Trigger'];
              unregisterTriggers();

              triggers = getTriggers(val);

              if (triggers.show !== 'none') {
                triggers.show.forEach(function(trigger, idx) {
                  // Using raw addEventListener due to jqLite/jQuery bug - #4060
                  if (trigger === triggers.hide[idx]) {
                    element[0].addEventListener(trigger, toggleTooltipBind);
                  } else if (trigger) {
                    element[0].addEventListener(trigger, showTooltipBind);
                    element[0].addEventListener(triggers.hide[idx], hideTooltipBind);
                  }
                });
              }
            }
            prepTriggers();

            var animation = scope.$eval(attrs[prefix + 'Animation']);
            ttScope.animation = angular.isDefined(animation) ? !!animation : options.animation;

            var appendToBodyVal = scope.$eval(attrs[prefix + 'AppendToBody']);
            appendToBody = angular.isDefined(appendToBodyVal) ? appendToBodyVal : appendToBody;

            // if a tooltip is attached to <body> we need to remove it on
            // location change as its parent scope will probably not be destroyed
            // by the change.
            if (appendToBody) {
              scope.$on('$locationChangeSuccess', function closeTooltipOnLocationChangeSuccess() {
                if (ttScope.isOpen) {
                  hide();
                }
              });
            }

            // Make sure tooltip is destroyed and removed.
            scope.$on('$destroy', function onDestroyTooltip() {
              $timeout.cancel(transitionTimeout);
              $timeout.cancel(popupTimeout);
              $timeout.cancel(positionTimeout);
              unregisterTriggers();
              removeTooltip();
              ttScope = null;
            });
          };
        }
      };
    };
  }];
})

// This is mostly ngInclude code but with a custom scope
.directive('tooltipTemplateTransclude', [
         '$animate', '$sce', '$compile', '$templateRequest',
function ($animate ,  $sce ,  $compile ,  $templateRequest) {
  return {
    link: function(scope, elem, attrs) {
      var origScope = scope.$eval(attrs.tooltipTemplateTranscludeScope);

      var changeCounter = 0,
        currentScope,
        previousElement,
        currentElement;

      var cleanupLastIncludeContent = function() {
        if (previousElement) {
          previousElement.remove();
          previousElement = null;
        }
        if (currentScope) {
          currentScope.$destroy();
          currentScope = null;
        }
        if (currentElement) {
          $animate.leave(currentElement).then(function() {
            previousElement = null;
          });
          previousElement = currentElement;
          currentElement = null;
        }
      };

      scope.$watch($sce.parseAsResourceUrl(attrs.tooltipTemplateTransclude), function(src) {
        var thisChangeId = ++changeCounter;

        if (src) {
          //set the 2nd param to true to ignore the template request error so that the inner
          //contents and scope can be cleaned up.
          $templateRequest(src, true).then(function(response) {
            if (thisChangeId !== changeCounter) { return; }
            var newScope = origScope.$new();
            var template = response;

            var clone = $compile(template)(newScope, function(clone) {
              cleanupLastIncludeContent();
              $animate.enter(clone, elem);
            });

            currentScope = newScope;
            currentElement = clone;

            currentScope.$emit('$includeContentLoaded', src);
          }, function() {
            if (thisChangeId === changeCounter) {
              cleanupLastIncludeContent();
              scope.$emit('$includeContentError', src);
            }
          });
          scope.$emit('$includeContentRequested', src);
        } else {
          cleanupLastIncludeContent();
        }
      });

      scope.$on('$destroy', cleanupLastIncludeContent);
    }
  };
}])

/**
 * Note that it's intentional that these classes are *not* applied through $animate.
 * They must not be animated as they're expected to be present on the tooltip on
 * initialization.
 */
.directive('tooltipClasses', function() {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      if (scope.placement) {
        element.addClass(scope.placement);
      }
      if (scope.popupClass) {
        element.addClass(scope.popupClass);
      }
      if (scope.animation()) {
        element.addClass(attrs.tooltipAnimationClass);
      }
    }
  };
})

.directive('tooltipPopup', function() {
  return {
    restrict: 'EA',
    replace: true,
    scope: { content: '@', placement: '@', popupClass: '@', animation: '&', isOpen: '&' },
    templateUrl: 'components/tooltip/tooltip-popup.html'
  };
})

.directive('tooltip', [ '$tooltip', function($tooltip) {
  return $tooltip('tooltip', 'tooltip', 'mouseenter');
}])

.directive('tooltipTemplatePopup', function() {
  return {
    restrict: 'EA',
    replace: true,
    scope: { contentExp: '&', placement: '@', popupClass: '@', animation: '&', isOpen: '&',
      originScope: '&' },
    templateUrl: 'components/tooltip/tooltip-template-popup.html'
  };
})

.directive('tooltipTemplate', ['$tooltip', function($tooltip) {
  return $tooltip('tooltipTemplate', 'tooltip', 'mouseenter', {
    useContentExp: true
  });
}])

.directive('tooltipHtmlPopup', function() {
  return {
    restrict: 'EA',
    replace: true,
    scope: { contentExp: '&', placement: '@', popupClass: '@', animation: '&', isOpen: '&' },
    templateUrl: 'components/tooltip/tooltip-html-popup.html'
  };
})

.directive('tooltipHtml', ['$tooltip', function($tooltip) {
  return $tooltip('tooltipHtml', 'tooltip', 'mouseenter', {
    useContentExp: true
  });
}])

/*
Deprecated
*/
.directive('tooltipHtmlUnsafePopup', function() {
  return {
    restrict: 'EA',
    replace: true,
    scope: { content: '@', placement: '@', popupClass: '@', animation: '&', isOpen: '&' },
    templateUrl: 'components/tooltip/tooltip-html-unsafe-popup.html'
  };
})

.value('tooltipHtmlUnsafeSuppressDeprecated', false)
.directive('tooltipHtmlUnsafe', [
          '$tooltip', 'tooltipHtmlUnsafeSuppressDeprecated', '$log',
function($tooltip ,  tooltipHtmlUnsafeSuppressDeprecated ,  $log) {
  if (!tooltipHtmlUnsafeSuppressDeprecated) {
    $log.warn('tooltip-html-unsafe is now deprecated. Use tooltip-html or tooltip-template instead.');
  }
  return $tooltip('tooltipHtmlUnsafe', 'tooltip', 'mouseenter');
}]);

'use strict';
angular.module('ui.bootstrap.modal', [])

/**
 * A helper, internal data structure that acts as a map but also allows getting / removing
 * elements in the LIFO order
 */
  .factory('$$stackedMap', function() {
    return {
      createNew: function() {
        var stack = [];

        return {
          add: function(key, value) {
            stack.push({
              key: key,
              value: value
            });
          },
          get: function(key) {
            for (var i = 0; i < stack.length; i++) {
              if (key == stack[i].key) {
                return stack[i];
              }
            }
          },
          keys: function() {
            var keys = [];
            for (var i = 0; i < stack.length; i++) {
              keys.push(stack[i].key);
            }
            return keys;
          },
          top: function() {
            return stack[stack.length - 1];
          },
          remove: function(key) {
            var idx = -1;
            for (var i = 0; i < stack.length; i++) {
              if (key == stack[i].key) {
                idx = i;
                break;
              }
            }
            return stack.splice(idx, 1)[0];
          },
          removeTop: function() {
            return stack.splice(stack.length - 1, 1)[0];
          },
          length: function() {
            return stack.length;
          }
        };
      }
    };
  })

/**
 * A helper, internal data structure that stores all references attached to key
 */
  .factory('$$multiMap', function() {
    return {
      createNew: function() {
        var map = {};

        return {
          entries: function() {
            return Object.keys(map).map(function(key) {
              return {
                key: key,
                value: map[key]
              };
            });
          },
          get: function(key) {
            return map[key];
          },
          hasKey: function(key) {
            return !!map[key];
          },
          keys: function() {
            return Object.keys(map);
          },
          put: function(key, value) {
            if (!map[key]) {
              map[key] = [];
            }

            map[key].push(value);
          },
          remove: function(key, value) {
            var values = map[key];

            if (!values) {
              return;
            }

            var idx = values.indexOf(value);

            if (idx !== -1) {
              values.splice(idx, 1);
            }

            if (!values.length) {
              delete map[key];
            }
          }
        };
      }
    };
  })

/**
 * A helper directive for the $modal service. It creates a backdrop element.
 */
  .directive('modalBackdrop', [
           '$animate', '$injector', '$modalStack',
  function($animate ,  $injector,   $modalStack) {
    var $animateCss = null;

    if ($injector.has('$animateCss')) {
      $animateCss = $injector.get('$animateCss');
    }

    return {
      restrict: 'EA',
      replace: true,
      templateUrl: 'components/modal/backdrop.html',
      compile: function(tElement, tAttrs) {
        tElement.addClass(tAttrs.backdropClass);
        return linkFn;
      }
    };

    function linkFn(scope, element, attrs) {
      if (attrs.modalInClass) {
        if ($animateCss) {
          $animateCss(element, {
            addClass: attrs.modalInClass
          }).start();
        } else {
          $animate.addClass(element, attrs.modalInClass);
        }

        scope.$on($modalStack.NOW_CLOSING_EVENT, function(e, setIsAsync) {
          var done = setIsAsync();
          if ($animateCss) {
            $animateCss(element, {
              removeClass: attrs.modalInClass
            }).start().then(done);
          } else {
            $animate.removeClass(element, attrs.modalInClass).then(done);
          }
        });
      }
    }
  }])

  .directive('modalWindow', [
           '$modalStack', '$q', '$animate', '$injector',
  function($modalStack ,  $q ,  $animate,   $injector) {
    var $animateCss = null;

    if ($injector.has('$animateCss')) {
      $animateCss = $injector.get('$animateCss');
    }

    return {
      restrict: 'EA',
      scope: {
        index: '@'
      },
      replace: true,
      transclude: true,
      templateUrl: function(tElement, tAttrs) {
        return tAttrs.templateUrl || 'components/modal/window.html';
      },
      link: function(scope, element, attrs) {
        element.addClass(attrs.windowClass || '');
        scope.size = attrs.size;

        scope.close = function(evt) {
          var modal = $modalStack.getTop();
          if (modal && modal.value.backdrop && modal.value.backdrop !== 'static' && (evt.target === evt.currentTarget)) {
            evt.preventDefault();
            evt.stopPropagation();
            $modalStack.dismiss(modal.key, 'backdrop click');
          }
        };

        // This property is only added to the scope for the purpose of detecting when this directive is rendered.
        // We can detect that by using this property in the template associated with this directive and then use
        // {@link Attribute#$observe} on it. For more details please see {@link TableColumnResize}.
        scope.$isRendered = true;

        // Deferred object that will be resolved when this modal is render.
        var modalRenderDeferObj = $q.defer();
        // Observe function will be called on next digest cycle after compilation, ensuring that the DOM is ready.
        // In order to use this way of finding whether DOM is ready, we need to observe a scope property used in modal's template.
        attrs.$observe('modalRender', function(value) {
          if (value == 'true') {
            modalRenderDeferObj.resolve();
          }
        });

        modalRenderDeferObj.promise.then(function() {
          var animationPromise = null;

          if (attrs.modalInClass) {
            if ($animateCss) {
              animationPromise = $animateCss(element, {
                addClass: attrs.modalInClass
              }).start();
            } else {
              animationPromise = $animate.addClass(element, attrs.modalInClass);
            }

            scope.$on($modalStack.NOW_CLOSING_EVENT, function(e, setIsAsync) {
              var done = setIsAsync();
              if ($animateCss) {
                $animateCss(element, {
                  removeClass: attrs.modalInClass
                }).start().then(done);
              } else {
                $animate.removeClass(element, attrs.modalInClass).then(done);
              }
            });
          }


          $q.when(animationPromise).then(function() {
            var inputsWithAutofocus = element[0].querySelectorAll('[autofocus]');
            /**
             * Auto-focusing of a freshly-opened modal element causes any child elements
             * with the autofocus attribute to lose focus. This is an issue on touch
             * based devices which will show and then hide the onscreen keyboard.
             * Attempts to refocus the autofocus element via JavaScript will not reopen
             * the onscreen keyboard. Fixed by updated the focusing logic to only autofocus
             * the modal element if the modal does not contain an autofocus element.
             */
            if (inputsWithAutofocus.length) {
              inputsWithAutofocus[0].focus();
            } else {
              element[0].focus();
            }
          });

          // Notify {@link $modalStack} that modal is rendered.
          var modal = $modalStack.getTop();
          if (modal) {
            $modalStack.modalRendered(modal.key);
          }
        });
      }
    };
  }])

  .directive('modalAnimationClass', [
    function () {
      return {
        compile: function(tElement, tAttrs) {
          if (tAttrs.modalAnimation) {
            tElement.addClass(tAttrs.modalAnimationClass);
          }
        }
      };
    }])

  .directive('modalTransclude', function() {
    return {
      link: function($scope, $element, $attrs, controller, $transclude) {
        $transclude($scope.$parent, function(clone) {
          $element.empty();
          $element.append(clone);
        });
      }
    };
  })

  .factory('$modalStack', [
             '$animate', '$timeout', '$document', '$compile', '$rootScope',
             '$q',
             '$injector',
             '$$multiMap',
             '$$stackedMap',
    function($animate ,  $timeout ,  $document ,  $compile ,  $rootScope ,
              $q,
              $injector,
              $$multiMap,
              $$stackedMap) {
      var $animateCss = null;

      if ($injector.has('$animateCss')) {
        $animateCss = $injector.get('$animateCss');
      }

      var OPENED_MODAL_CLASS = 'modal-open';

      var backdropDomEl, backdropScope;
      var openedWindows = $$stackedMap.createNew();
      var openedClasses = $$multiMap.createNew();
      var $modalStack = {
        NOW_CLOSING_EVENT: 'modal.stack.now-closing'
      };

      //Modal focus behavior
      var focusableElementList;
      var focusIndex = 0;
      var tababbleSelector = 'a[href], area[href], input:not([disabled]), ' +
        'button:not([disabled]),select:not([disabled]), textarea:not([disabled]), ' +
        'iframe, object, embed, *[tabindex], *[contenteditable=true]';

      function backdropIndex() {
        var topBackdropIndex = -1;
        var opened = openedWindows.keys();
        for (var i = 0; i < opened.length; i++) {
          if (openedWindows.get(opened[i]).value.backdrop) {
            topBackdropIndex = i;
          }
        }
        return topBackdropIndex;
      }

      $rootScope.$watch(backdropIndex, function(newBackdropIndex) {
        if (backdropScope) {
          backdropScope.index = newBackdropIndex;
        }
      });

      function removeModalWindow(modalInstance, elementToReceiveFocus) {
        var body = $document.find('body').eq(0);
        var modalWindow = openedWindows.get(modalInstance).value;

        //clean up the stack
        openedWindows.remove(modalInstance);

        removeAfterAnimate(modalWindow.modalDomEl, modalWindow.modalScope, function() {
          var modalBodyClass = modalWindow.openedClass || OPENED_MODAL_CLASS;
          openedClasses.remove(modalBodyClass, modalInstance);
          body.toggleClass(modalBodyClass, openedClasses.hasKey(modalBodyClass));
        });
        checkRemoveBackdrop();

        //move focus to specified element if available, or else to body
        if (elementToReceiveFocus && elementToReceiveFocus.focus) {
          elementToReceiveFocus.focus();
        } else {
          body.focus();
        }
      }

      function checkRemoveBackdrop() {
          //remove backdrop if no longer needed
          if (backdropDomEl && backdropIndex() == -1) {
            var backdropScopeRef = backdropScope;
            removeAfterAnimate(backdropDomEl, backdropScope, function() {
              backdropScopeRef = null;
            });
            backdropDomEl = undefined;
            backdropScope = undefined;
          }
      }

      function removeAfterAnimate(domEl, scope, done) {
        var asyncDeferred;
        var asyncPromise = null;
        var setIsAsync = function() {
          if (!asyncDeferred) {
            asyncDeferred = $q.defer();
            asyncPromise = asyncDeferred.promise;
          }

          return function asyncDone() {
            asyncDeferred.resolve();
          };
        };
        scope.$broadcast($modalStack.NOW_CLOSING_EVENT, setIsAsync);

        // Note that it's intentional that asyncPromise might be null.
        // That's when setIsAsync has not been called during the
        // NOW_CLOSING_EVENT broadcast.
        return $q.when(asyncPromise).then(afterAnimating);

        function afterAnimating() {
          if (afterAnimating.done) {
            return;
          }
          afterAnimating.done = true;

          if ($animateCss) {
            $animateCss(domEl, {
              event: 'leave'
            }).start().then(function() {
              domEl.remove();
            });
          } else {
            $animate.leave(domEl);
          }
          scope.$destroy();
          if (done) {
            done();
          }
        }
      }

      $document.bind('keydown', function(evt) {
        if (evt.isDefaultPrevented()) {
          return evt;
        }

        var modal = openedWindows.top();
        if (modal && modal.value.keyboard) {
          switch (evt.which){
            case 27: {
              evt.preventDefault();
              $rootScope.$apply(function() {
                $modalStack.dismiss(modal.key, 'escape key press');
              });
              break;
            }
            case 9: {
              $modalStack.loadFocusElementList(modal);
              var focusChanged = false;
              if (evt.shiftKey) {
                if ($modalStack.isFocusInFirstItem(evt)) {
                  focusChanged = $modalStack.focusLastFocusableElement();
                }
              } else {
                if ($modalStack.isFocusInLastItem(evt)) {
                  focusChanged = $modalStack.focusFirstFocusableElement();
                }
              }

              if (focusChanged) {
                evt.preventDefault();
                evt.stopPropagation();
              }
              break;
            }
          }
        }
      });

      $modalStack.open = function(modalInstance, modal) {
        var modalOpener = $document[0].activeElement,
          modalBodyClass = modal.openedClass || OPENED_MODAL_CLASS;

        openedWindows.add(modalInstance, {
          deferred: modal.deferred,
          renderDeferred: modal.renderDeferred,
          modalScope: modal.scope,
          backdrop: modal.backdrop,
          keyboard: modal.keyboard,
          openedClass: modal.openedClass
        });

        openedClasses.put(modalBodyClass, modalInstance);

        var body = $document.find('body').eq(0),
            currBackdropIndex = backdropIndex();

        if (currBackdropIndex >= 0 && !backdropDomEl) {
          backdropScope = $rootScope.$new(true);
          backdropScope.index = currBackdropIndex;
          var angularBackgroundDomEl = angular.element('<div modal-backdrop="modal-backdrop"></div>');
          angularBackgroundDomEl.attr('backdrop-class', modal.backdropClass);
          if (modal.animation) {
            angularBackgroundDomEl.attr('modal-animation', 'true');
          }
          backdropDomEl = $compile(angularBackgroundDomEl)(backdropScope);
          body.append(backdropDomEl);
        }

        var angularDomEl = angular.element('<div modal-window="modal-window"></div>');
        angularDomEl.attr({
          'template-url': modal.windowTemplateUrl,
          'window-class': modal.windowClass,
          'size': modal.size,
          'index': openedWindows.length() - 1,
          'animate': 'animate'
        }).html(modal.content);
        if (modal.animation) {
          angularDomEl.attr('modal-animation', 'true');
        }

        var modalDomEl = $compile(angularDomEl)(modal.scope);
        openedWindows.top().value.modalDomEl = modalDomEl;
        openedWindows.top().value.modalOpener = modalOpener;
        body.append(modalDomEl);
        body.addClass(modalBodyClass);

        $modalStack.clearFocusListCache();
      };

      function broadcastClosing(modalWindow, resultOrReason, closing) {
          return !modalWindow.value.modalScope.$broadcast('modal.closing', resultOrReason, closing).defaultPrevented;
      }

      $modalStack.close = function(modalInstance, result) {
        var modalWindow = openedWindows.get(modalInstance);
        if (modalWindow && broadcastClosing(modalWindow, result, true)) {
          modalWindow.value.modalScope.$$uibDestructionScheduled = true;
          modalWindow.value.deferred.resolve(result);
          removeModalWindow(modalInstance, modalWindow.value.modalOpener);
          return true;
        }
        return !modalWindow;
      };

      $modalStack.dismiss = function(modalInstance, reason) {
        var modalWindow = openedWindows.get(modalInstance);
        if (modalWindow && broadcastClosing(modalWindow, reason, false)) {
          modalWindow.value.modalScope.$$uibDestructionScheduled = true;
          modalWindow.value.deferred.reject(reason);
          removeModalWindow(modalInstance, modalWindow.value.modalOpener);
          return true;
        }
        return !modalWindow;
      };

      $modalStack.dismissAll = function(reason) {
        var topModal = this.getTop();
        while (topModal && this.dismiss(topModal.key, reason)) {
          topModal = this.getTop();
        }
      };

      $modalStack.getTop = function() {
        return openedWindows.top();
      };

      $modalStack.modalRendered = function(modalInstance) {
        var modalWindow = openedWindows.get(modalInstance);
        if (modalWindow) {
          modalWindow.value.renderDeferred.resolve();
        }
      };

      $modalStack.focusFirstFocusableElement = function() {
        if (focusableElementList.length > 0) {
          focusableElementList[0].focus();
          return true;
        }
        return false;
      };
      $modalStack.focusLastFocusableElement = function() {
        if (focusableElementList.length > 0) {
          focusableElementList[focusableElementList.length - 1].focus();
          return true;
        }
        return false;
      };

      $modalStack.isFocusInFirstItem = function(evt) {
        if (focusableElementList.length > 0) {
          return (evt.target || evt.srcElement) == focusableElementList[0];
        }
        return false;
      };

      $modalStack.isFocusInLastItem = function(evt) {
        if (focusableElementList.length > 0) {
          return (evt.target || evt.srcElement) == focusableElementList[focusableElementList.length - 1];
        }
        return false;
      };

      $modalStack.clearFocusListCache = function() {
        focusableElementList = [];
        focusIndex = 0;
      };

      $modalStack.loadFocusElementList = function(modalWindow) {
        if (focusableElementList === undefined || !focusableElementList.length0) {
          if (modalWindow) {
            var modalDomE1 = modalWindow.value.modalDomEl;
            if (modalDomE1 && modalDomE1.length) {
              focusableElementList = modalDomE1[0].querySelectorAll(tababbleSelector);
            }
          }
        }
      };

      return $modalStack;
    }])

  .provider('$modal', function() {
    var $modalProvider = {
      options: {
        animation: true,
        backdrop: true, //can also be false or 'static'
        keyboard: true
      },
      $get: ['$injector', '$rootScope', '$q', '$templateRequest', '$controller', '$modalStack',
        function ($injector, $rootScope, $q, $templateRequest, $controller, $modalStack) {
          var $modal = {};

          function getTemplatePromise(options) {
            return options.template ? $q.when(options.template) :
              $templateRequest(angular.isFunction(options.templateUrl) ? (options.templateUrl)() : options.templateUrl);
          }

          function getResolvePromises(resolves) {
            var promisesArr = [];
            angular.forEach(resolves, function(value) {
              if (angular.isFunction(value) || angular.isArray(value)) {
                promisesArr.push($q.when($injector.invoke(value)));
              } else if (angular.isString(value)) {
                promisesArr.push($q.when($injector.get(value)));
              } else {
                promisesArr.push($q.when(value));
              }
            });
            return promisesArr;
          }

          var promiseChain = null;
          $modal.getPromiseChain = function() {
            return promiseChain;
          };

          $modal.open = function (modalOptions) {

            var modalResultDeferred = $q.defer();
            var modalOpenedDeferred = $q.defer();
            var modalRenderDeferred = $q.defer();

            //prepare an instance of a modal to be injected into controllers and returned to a caller
            var modalInstance = {
              result: modalResultDeferred.promise,
              opened: modalOpenedDeferred.promise,
              rendered: modalRenderDeferred.promise,
              close: function (result) {
                return $modalStack.close(modalInstance, result);
              },
              dismiss: function (reason) {
                return $modalStack.dismiss(modalInstance, reason);
              }
            };

            //merge and clean up options
            modalOptions = angular.extend({}, $modalProvider.options, modalOptions);
            modalOptions.resolve = modalOptions.resolve || {};

            //verify options
            if (!modalOptions.template && !modalOptions.templateUrl) {
              throw new Error('One of template or templateUrl options is required.');
            }

            var templateAndResolvePromise =
              $q.all([getTemplatePromise(modalOptions)].concat(getResolvePromises(modalOptions.resolve)));

            // Wait for the resolution of the existing promise chain.
            // Then switch to our own combined promise dependency (regardless of how the previous modal fared).
            // Then add to $modalStack and resolve opened.
            // Finally clean up the chain variable if no subsequent modal has overwritten it.
            var samePromise;
            samePromise = promiseChain = $q.all([promiseChain])
              .then(function() { return templateAndResolvePromise; }, function() { return templateAndResolvePromise; })
              .then(function resolveSuccess(tplAndVars) {

                var modalScope = (modalOptions.scope || $rootScope).$new();
                modalScope.$close = modalInstance.close;
                modalScope.$dismiss = modalInstance.dismiss;

                modalScope.$on('$destroy', function() {
                  if (!modalScope.$$uibDestructionScheduled) {
                    modalScope.$dismiss('$uibUnscheduledDestruction');
                  }
                });

                var ctrlInstance, ctrlLocals = {};
                var resolveIter = 1;

                //controllers
                if (modalOptions.controller) {
                  ctrlLocals.$scope = modalScope;
                  ctrlLocals.$modalInstance = modalInstance;
                  angular.forEach(modalOptions.resolve, function(value, key) {
                    ctrlLocals[key] = tplAndVars[resolveIter++];
                  });

                  ctrlInstance = $controller(modalOptions.controller, ctrlLocals);
                  if (modalOptions.controllerAs) {
                    if (modalOptions.bindToController) {
                      angular.extend(ctrlInstance, modalScope);
                    }

                    modalScope[modalOptions.controllerAs] = ctrlInstance;
                  }
                }

                $modalStack.open(modalInstance, {
                  scope: modalScope,
                  deferred: modalResultDeferred,
                  renderDeferred: modalRenderDeferred,
                  content: tplAndVars[0],
                  animation: modalOptions.animation,
                  backdrop: modalOptions.backdrop,
                  keyboard: modalOptions.keyboard,
                  backdropClass: modalOptions.backdropClass,
                  windowClass: modalOptions.windowClass,
                  windowTemplateUrl: modalOptions.windowTemplateUrl,
                  size: modalOptions.size,
                  openedClass: modalOptions.openedClass
                });
                modalOpenedDeferred.resolve(true);

            }, function resolveError(reason) {
              modalOpenedDeferred.reject(reason);
              modalResultDeferred.reject(reason);
            })
            .finally(function() {
              if (promiseChain === samePromise) {
                promiseChain = null;
              }
            });

            return modalInstance;
          };

          return $modal;
        }]
    };

    return $modalProvider;
  });

/**
 * Created by ZHL on 2016/8/15.
 */
'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:dateTimePicker
     * @description
     * # dateTimePicker
     * 日期选择组件
     */
    .directive('dateRange', ['$document', '$timeout', function ($document, $timeout) {
        return {
            templateUrl: 'components/datepicker/new/daterange.html',
            restrict: 'EA',
            replace: true,
            require: ['dateRange'],
            controller: 'dateRangeCtrl',
            controllerAs: 'dateRange',
            scope: {
                format: '@',
                startDate: "=",
                endDate: "=",
                showTime: "=",
                minDate: "=",
                datepickerMode: '@',
                minMode: '@',
                maxDate: "="
            },
            link: function postLink(scope, element, attrs, ctrls) {
                var datepickerCtrl = ctrls[0];
                datepickerCtrl.init();

                var documentClickBind = function () {
                    scope.$apply(function () {
                        scope.showMenu = false;
                    });
                };

                scope.$watch('showMenu', function (value) {
                    if (value) {
                        $timeout(function () {
                            $document.bind('click', documentClickBind);
                        }, 0, false);
                    } else {
                        $document.unbind('click', documentClickBind);
                    }
                });
                attrs.$observe('disabled', function (value) {
                    scope.disabled = !!value;
                });

            }
        };
    }])
    .controller('dateRangeCtrl', ['$scope', function ($scope) {
        var self = this,
            startDate = new Date($scope.startDate),
            endDate = new Date($scope.endDate);
        $scope.minMode = $scope.minMode || 'day';
        $scope.datepickerMode = $scope.datepickerMode || 'day';
        self.init = function () {
            if (!$scope.format) {
                $scope.format = $scope.showTime ? 'yyyy-MM-dd HH:mm:ss' : 'yyyy-MM-dd';
            }
            $scope.startDate = new Date($scope.startDate);
            $scope.endDate = new Date($scope.endDate);
        };

        self.open = function () {
            $scope.showMenu = $scope.disabled ? false : !$scope.showMenu;
        };

        self.reset = function () {
            $scope.startDate = new Date(startDate);
            $scope.endDate = new Date(endDate);
        };

        self.stopPropagation = function ($event) {
            $event.stopPropagation();
        };
    }])
;




/**
 * Created by ZHL on 2016/3/17.
 */
'use strict';

angular.module('eayun.components')
    /**
     * @ngdoc directive
     * @name eayunApp.directive:eayunSelect
     * @description
     * # eayunSelect
     * 下拉选择组件
     */
    .directive('eayunTableFilter', ['$document', '$timeout', function ($document, $timeout) {
        return {
            templateUrl: 'components/select/filter/filter.html',
            restrict: 'EA',
            replace: true,
            controller: ['$scope', '$log', function ($scope, $log) {
                var ctrl = this;
                ctrl.init = function () {
                    if (!angular.isArray($scope.listData)) {
                        $log.log("eayunTableFilter: listData必须为数组对象");
                        $log.debug($scope.listData);
                    }

                    angular.forEach($scope.listData, function (item) {
                        if (item.$$select) {
                            $scope.text = item[$scope.textField];
                        }
                    });
                    //默认选中第一项
                    if (!$scope.text) {
                        $scope.text = $scope.listData[0][$scope.textField];
                    }
                };

                ctrl.open = function () {
                    $scope.showMenu = !$scope.showMenu;
                };

                ctrl.select = function (item, $event) {
                    $scope.text = item[$scope.textField];
                    ctrl.warpCallback('itemClicked', item, $event);
                };

                ctrl.warpCallback = function (callback, item, $event) {
                    ($scope[callback] || angular.noop)({
                        $item: item,
                        $event: $event
                    });
                };
            }],
            controllerAs: 'ctrl',
            scope: {
                listData: '=',
                textField: '@',
                itemClicked: '&'
            },
            require: ['eayunTableFilter'],
            link: function postLink(scope, element, attrs, ctrls) {
                var ctrl = ctrls[0];

                function documentClickBind() {
                    scope.$apply(function () {
                        scope.showMenu = false;
                    });
                };

                scope.$watch('listData', function (value) {
                    ctrl.init();
                });

                scope.$watch('showMenu', function (value) {
                    if (value) {
                        $timeout(function () {
                            $document.bind('click', documentClickBind);
                        }, 0, false);
                    } else {
                        $document.unbind('click', documentClickBind);
                    }
                });

                ctrl.init();
            }
        };
    }]);

/**
 * Created by ZHL on 2016/8/9.
 */
'use strict';

angular.module('eayun.components')
/**
 * @ngdoc directive
 * @name eayunApp.directive:eayunSelect
 * @description
 * # eayunSelect
 * 下拉选择组件
 */
    .directive('eayunSelectTwoWay', ['$document', '$timeout', function ($document, $timeout) {
        return {
            templateUrl: 'components/select/twoway/twoway.html',
            restrict: 'EA',
            replace: true,
            transclude: true,
            controller: ['$scope', '$log', '$filter', function ($scope, $log, $filter) {
                var vm = this;
                $scope.preArray = $scope.preTitle === undefined ? ['待选', '已选'] : $scope.preTitle.split(',');

                (function init() {
                    if (!angular.isArray($scope.listData)) {
                        $log.log("eayunSelectTowWay: listData必须为数组对象");
                        $log.debug($scope.listData);
                    }

                    if (!angular.isDefined($scope.textField)) {
                        $log.log("eayunSelectTowWay: textField不能为空");
                        $log.debug($scope.textField);
                    }

                    $scope.headers = $scope.headers || "";
                    $scope.headerWidth = $scope.headerWidth || "";
                    vm.search = "";
                    vm.fields = $scope.textField.split(',');
                    vm.headerArray = $scope.headers.split(',');
                    vm.width = $scope.headerWidth.split(',');

                    if ($scope.listDate === undefined) {
                        $scope.listDate = [];
                    }
                })();


                vm.select = function (item) {
                    if (item.$$selected) {
                        item.$$selected = false;
                        $scope.date.count--;
                    } else {
                        item.$$selected = true;
                        $scope.date.count++;
                    }
                };

                vm.addAll = function () {
                    angular.forEach($filter('filter')($scope.listData, vm.filter), function (item) {
                        if (!item.$$selected) {
                            $scope.date.count++;
                            item.$$selected = true;
                        }
                    });
                };

                vm.removeAll = function () {
                    angular.forEach($scope.listData, function (item) {
                        item.$$selected = false;
                    });
                    $scope.date.count = 0;
                };

                vm.filter = function (value, index, array) {
                    var b = false;
                    angular.forEach(vm.fields, function (field) {
                        if ((value[field] + '').indexOf(vm.search) >= 0)
                            b = true;
                    });
                    return b;
                };
            }],
            controllerAs: 'two',
            scope: {
                listData: '=',
                textField: '@',
                headers: '@',
                headerWidth: '@',
                title: '@',
                preTitle: '@',
                itemTemplateUrl: '@'
            },
            link: function postLink(scope, element, attrs, ctrls) {
                scope.date = {};
                scope.$watch('listData', function (value, v) {
                    var i = 0;
                    angular.forEach(value, function (item) {
                        if (item.$$selected) {
                            i++;
                        }
                    });
                    scope.date.count = i;
                }, true);
                element.removeAttr('title');//避免鼠标悬浮时提示title
                if (attrs.name) {
                    scope.$parent[attrs.name] = scope.date;
                }
            }
        };
    }]);