'use strict';

/**
 * @ngdoc directive
 * @name eayunApp.directive
 * @description
 * 页面组件模块
 */
angular.module('eayunApp.directive', [])
/**
 * @ngdoc directive
 * @name eayunApp.directive:expander
 * @description
 * # expander
 * 显示隐藏组件
 */
  .directive('expander', [function () {
    return {
      template: '<div ng-transclude data-toggle="collapse"></div>',
      restrict: 'EA',
      replace: true,
      transclude: true,
      scope: true,
      link: function postLink(scope, element, attrs) {
        var target = attrs.target;
        scope.show = angular.isDefined(attrs.expanderShow);
        $(target).addClass("collapse");
        if (scope.show) {
          $(target).addClass("in");
          element.addClass("open");
        }
        element.click(function () {
          element.toggleClass("open");
        });
      }
    };
  }])
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
  }])
/**
 * @ngdoc directive
 * @name eayunApp.directive:eayunTable
 * @description
 * # eayunTable
 * 表格
 */
  .directive('eayunTable', ['tableService',function (tableService) {
    var eayunTable = {
      template: '<div ng-transclude></div>',
      restrict: 'EA',
      replace: true,
      transclude: true,
      controller: function ($scope) {
        var tableCtrl = {};
        tableCtrl.api = {
          draw: function () {
            tableService.query(tableCtrl.getSource(), tableCtrl.getParam()).then(function (data) {
              $scope.result = data;
            });
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
        return tableCtrl;
      },
      scope: {api: '=api', ajaxSource: '=ajaxSource', param: '=param', result: '=result'},
      link: function postLink(scope, element, attrs) {
        if (scope.ajaxSource && element.find('eayun-table-page').length == 0){
        	scope.api.draw();
        }
        if(!scope.ajaxSource){
        	scope.$watch('ajaxSource',function(a,b){
        		var tableCtrl = eayunTable.controller(scope);
        		scope.$broadcast('a',tableCtrl);
        		if(element.find('eayun-table-page').length == 0){
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
 * 表格分页组件
 */
  .directive('eayunTablePage', ['tableService','$timeout', function (tableService,$timeout) {
    return {
      templateUrl: 'views/directives/tablepage.html',
      restrict: 'EA',
      replace: true,
      require: '^eayunTable',
      scope: false,
      link: function postLink(scope, element, attrs, tableCtrl) {
	  scope.$on('a',function(event,tableCtrl){
		  tableCtrl.api.draw = function () {
	          pageApi.first();
	        };
	        tableCtrl.api.refresh = function () {
	          pageApi.goto(options.pageNumber);
	        };
		  tableCtrl.api.draw();
	  });
        var options = {
          pageSize: 10,//每页显示行数
          pageNumber: 1,//当前页
          totalPage: 1,//总页数
          params: {}//业务相关查询参数
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
            options.params = tableCtrl.getParam();
            if(tableCtrl.getSource()){
            	tableService.queryPage(tableCtrl.getSource(), options).then(function (page) {
            		scope.isLoad = true;
                    options.totalPage = (page.totalCount == 0 ? 1 : Math.ceil(page.totalCount / (null==page.pageSize||0==page.pageSize?options.pageSize:page.pageSize)));//向上取整，计算总页数,最少1页
                    tableCtrl.setResult(page.result);
                    if (options.pageNumber > options.totalPage)
                      pageApi.goto(options.totalPage);
                  });
            }
          },
          goto: function (index) {
            if(angular.isString(index)){
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
        scope.$watch('number',function(newV,oldV){
        	scope.number = oldV;
        	if((newV > 0 && newV <= options.totalPage && valid.test(newV)) || newV == ''){
        		scope.number = newV;
        	}
        });
        /*scope.isInt = function($event){
          var code = $event.keyCode;
          if(code>47&&code<58){
            var num = code - 48;
            scope.number = scope.number + num + "";
          }else if(code>95&&code<106){
            var num = code - 96;
            scope.number = scope.number + num + "";
          }else if(code == 8){
            scope.number = scope.number.substr(0,scope.number.length-1);
          }
          if(parseInt(scope.number) > options.totalPage)
            scope.number = options.totalPage + "";
        };*/
      }
    };
  }])
/**
 * @ngdoc directive
 * @name eayunApp.directive:eayunDialog
 * @description
 * # eayunDialog
 * 对话框组件
 */
  .directive('eayunDialog', ['$templateRequest', '$compile', '$controller', '$injector', '$q', '$sce',function ($templateRequest, $compile, $controller, $injector, $q,$sce) {
    return {
      templateUrl: 'views/directives/dialog.html',
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
        form.attr('name') || form.attr('name','dialogForm');
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
        return attrs.type === 'btn' ? 'views/directives/selectbtn.html' : 'views/directives/select.html';
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
        	if(!text){
        		return '';
        	}else if ($scope.showLength && text.length>$scope.showLength) {
        		return text.substr(0, $scope.showLength) + '...';
        	}else if ($scope.showFormat) {
        		return $scope.showFormat({text:text});
        	}
        	return text;
        };
        var curOption = null;
        var ngModelCtrl;
        var api = {
          select: function (option) {
            curOption && (curOption.selected = false);
            curOption = option;
            if(curOption.value != 'stay'){
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
      scope: {placeholder: '@placeholder',showFormat:'&?'},
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
        attrs.$observe('disabled',function(value){
        	scope.disabled = value;
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
      template: '<li data-ng-hide="option.selected"><a data-ng-bind="text"></a></li>',
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
      }
    };
  }])
/**
 * @ngdoc directive
 * @name eayunApp.directive:dateTimePicker
 * @description
 * # dateTimePicker
 * 日期选择组件
 */
  .directive('dateTimePicker', [function () {
    return {
      templateUrl: 'views/directives/datetimepicker.html',
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
    }
  }])
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
  }])
/**
 * @ngdoc directive
 * @name eayunApp.directive:toggle
 * @description
 * # toggle
 * ajax校验
 */
  .directive('toggle', ['$q', '$log', function ($q, $log) {
    return {
      restrict: 'C',
      link: function postLink(scope, element, attrs, ngModel) {
        element.children().click(function () {
          angular.forEach(element.children(), function (value, key) {
            $(value).removeClass('active');
          });
          $(this).addClass('active');
        });

      }
    };
  }])
  .directive('hint', ['$templateRequest', '$compile', function ($templateRequest, $compile) {
    return {
      restrict: 'A',
      link: function postLink(scope, element, attrs) {
        $templateRequest(attrs.hint).then(function (template) {
          var init = function(){
            $(element).tooltip({
              placement: attrs.tooltipPlacement || 'bottom',
              html: true,
              trigger: 'none',
              title: $compile(template)(scope),
              template: '<div class="tooltip ' + attrs.tooltipClass + '" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
            });
          };
          var distroy = function(){
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
  }])
/**
 * @ngdoc directive
 * @name eayunApp.directive:inputCheck
 * @description
 * # inputCheck
 * 输入框在体现view值前校验的组件
 */
  .directive('inputCheck', ['$timeout', function ($timeout) {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function postLink(scope, element, attrs, ngModel) {
        ngModel.$parsers.unshift(
          function (newValue) {
            var value = scope[attrs.inputCheck](newValue, ngModel.$modelValue);
            ngModel.$setViewValue(value);
            element.val(value);
            return value;
          }
        );
      }
    }
  }]);