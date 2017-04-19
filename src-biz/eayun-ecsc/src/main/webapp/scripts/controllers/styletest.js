'use strict';

angular.module('eayunApp.controllers')
/**
 * @ngdoc function
 * @name appRouter
 * @description
 * 配置路由
 */
  .config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('/app/styletest', '/app/styletest/host');
    $stateProvider.state('app.styletest.host', {
      url: '/host',
      templateUrl: 'views/styletest/host.html',
      controller: 'styletestHostCtrl'
    }).state('app.styletest.hd', {
      url: '/hd',
      templateUrl: 'views/styletest/hd/hd.html',
      controller: 'styletestHDCtrl'
    }).state('app.styletest.mirror', {
      url: '/mirror',
      templateUrl: 'views/styletest/host.html',
      controller: 'styletestHostCtrl'
    }).state('app.styletest.group', {
      url: '/group',
      templateUrl: 'views/overview.html',
      controller: 'styletestHostCtrl'
    })
    /*.state('app.styletest', {
      url: '/styletest',
      templateUrl: 'views/styletest/main.html',
      controller: 'StyleCtrl'
    })*/
    ;
  })
/**
 * @ngdoc function
 * @name eayunApp.controller:styletestCtrl
 * @description
 * # styletestCtrl
 * 云主机
 */
  .controller('StyleCtrl', function ($scope) {
    $scope.tabs = [
      {title: '云主机', target: 'app.styletest.host'},
      {title: '云硬盘', target: 'app.styletest.disk'},
      {title: '镜像', target: 'app.styletest.mirror'},
      {title: '安全组', target: 'app.styletest.group'}
    ];
  })
/**
 * @ngdoc function
 * @name eayunApp.controller:styletestHostCtrl
 * @description
 * # styletestHostCtrl
 * 云主机--云主机
 */
  .controller('styletestHostCtrl', function ($scope, eayunModal) {
    $scope.addVM = function () {
      eayunModal.open({
        templateUrl: 'views/styletest/vm/add.html',
        controller: 'styletestHostAddCtrl'
      });
    };
    $scope.vm = {
      name: 'VM1',
      desc: '',
      id: 'hgfh54565',
      system: 'Windows 2008 64位',
      state: '正常',
      cpu: 4,
      hd: 60,
      memery: 2,
      vmid: 'fdf1972c-537c-11e4-9ec9-000c29e81e1c'
    };
    var editTarget;
    $scope.edit = function (target) {
      editTarget = target;
    };
    $scope.isEdit = function (target) {
      return target == editTarget;
    };
  })
/**
 * @ngdoc function
 * @name eayunApp.controller:styletestHostCtrl
 * @description
 * # styletestHostCtrl
 * 云主机--云主机--新建云主机
 */
  .controller('styletestHostAddCtrl', function ($scope, $modalInstance) {
    $scope.title = "新建云主机";
    $scope.step = 1;
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
    $scope.next = function () {
      $scope.step++;
    };
    $scope.back = function () {
      $scope.step--;
    };
    $scope.done = function () {
      $modalInstance.close(true);
    };

  })
/**
 * @ngdoc function
 * @name eayunApp.controller:styletestHDCtrl
 * @description
 * # styletestHDCtrl
 * 云主机--云硬盘
 */
  .controller('styletestHDCtrl', function ($scope, eayunModal,toast) {
    //测试数据
    $scope.items = [];
    for (var i = 1; i < 10; i++) {
      $scope.items.push({
          id: i,
          name: 'vm' + i,
          creatTime: new Date(),
          system: (i % 2 == 0 ? 'windows8.1' : 'Linux'),
          cpu: (i % 3 == 0 ? 4 : 2),
          memery: 2048,
          hd: 500,
          subnet: 'subnet1',
          privateIp: '192.168.16.' + i,
          publicIp: '10.1.12.' + i,
          table: {check: false}
        }
      );
    }
    //测试数据-结束
    $scope.filter = {};
    $scope.$watch('filter', function () {
      angular.forEach($scope.myTable.result, function (item) {
        item.show = true;
        angular.forEach($scope.filter, function (value, key) {
          if (item.show && value)
            item.show = (item[key] == value);
        });
      });
    }, true);
    $scope.checkAll = function () {
      angular.forEach($scope.myTable.result, function (value, key) {
        value.isChecked = $scope.myTable.isAllChecked;
      });
    };
    $scope.getChecked = function () {
      var checked = [];
      angular.forEach($scope.myTable.result, function (value, key) {
        if (value.isChecked)
          checked.push(value);
      });
      //console.info(checked);
    };
    $scope.myTable = {
      source: 'testSource',
      getParams: function () {
        return {};
      },
      result: $scope.items,
      isAllChecked: false
    };

    $scope.open = function () {
      var result = eayunModal.dialog({
        title: '新建云硬盘',
        width: '600px',
        templateUrl: 'views/styletest/hd/addhd.html',
        controller: 'styletestHDAddCtrl',
        resolve: {
          items: function () {
            return $scope.items;
          }
        }
      });
      result.then(function (value1) {
        //console.info(value1);
      }, function () {
        //console.info('取消');
      });
    };
    $scope.selectRules = function () {
      var result = eayunModal.dialog({
        title: '管理规则',
        width: '600px',
        templateUrl: 'views/styletest/selectrule.html',
        controller: 'selectRuleCtrl'
      });
      result.then(function (value1) {
        //console.info(value1);
      }, function () {
        //console.info('取消');
      });
    };

    $scope.tooltip = function () {
      var result = eayunModal.dialog({
        title: '管理规则',
        width: '600px',
        templateUrl: 'views/styletest/tooltip/demo.html',
        controller: 'tooltipDemoCtrl'
      });
      result.then(function (value1) {
        //console.info(value1);
      }, function () {
        //console.info('取消');
      });
    };

    $scope.toastRun = function () {
      toast.running('running');
    };
    $scope.toastSuccess = function () {
      toast.success('success');
    };
    $scope.toastError = function () {
      toast.error('error');
    };
  })
/**
 * @ngdoc function
 * @name eayunApp.controller:styletestHDAddCtrl
 * @description
 * # styletestHDAddCtrl
 * 云主机--云硬盘--新建云硬盘
 */
  .controller('styletestHDAddCtrl', function ($scope, items, eayunModal, $timeout, Upload) {
    $scope.items = items;
    //console.info($scope);
    $timeout(function () {
      $scope.projects = [{id: 1, name: 'project1'}, {id: '002', name: 'project2'}, {id: 3, name: 'project3'}];
      $scope.HD.projectId = $scope.projects[0];
    }, 100, false);


    $scope.commit = function () {
      eayunModal.confirm('确认保存？').then(function () {
        $scope.ok($scope.HD);
      }, function () {
        //console.info('取消');
      });
    };

    // 禁用周末
    $scope.disabled = function (date, mode) {
      return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
    };

    $scope.ajaxValid = function (value) {
      return true;
    };

    $scope.info = function () {
      eayunModal.info('info');
    };
    $scope.success = function () {
      eayunModal.success('success');
    };
    $scope.warning = function () {
      eayunModal.warning('warning');
    };
    $scope.error = function () {
      eayunModal.error('error', 1000);
    };

    $scope.HD = {
      name: 'name@name',
      size: 1024,
      FAT: true,
      desc: 'desc'
    };

    $scope.maxDate = new Date();
    var copy = angular.copy($scope.HD);
    $scope.reset = function () {
      $scope.HD.projectId = 1;
    };


    $scope.uploadFiles = function (files, errFiles) {
      $scope.files = files;
      $scope.errFiles = errFiles;
      angular.forEach(files, function (file) {
        //console.info(file);
        file.upload = Upload.upload({
          url: 'https://angular-file-upload-cors-srv.appspot.com/upload',
          data: {file: file}
        });

        file.upload.then(function (response) {
          $timeout(function () {
            file.result = response.data;
          });
        }, function (response) {
          if (response.status > 0)
            $scope.errorMsg = response.status + ': ' + response.data;
        }, function (evt) {
          file.progress = Math.min(100, parseInt(100.0 *
            evt.loaded / evt.total));
        });
      });
    }

  })
/**
 * @ngdoc function
 * @name eayunApp.controller:selectRuleCtrl
 * @description
 * # selectRuleCtrl
 * 云主机--管理规则
 */
  .controller('selectRuleCtrl', function ($scope) {
    $scope.items = [];
    $scope.selected = [];
    for (var i = 0; i < 10; i++) {
      var item = {};
      item.id = i;
      item.name = '测试用例' + i;
      $scope.items.push(item);
    }
    $scope.add = function (index) {
      $scope.selected.push($scope.items[index]);
      $scope.items.splice(index, 1);
    };
    $scope.remove = function (index) {
      $scope.items.push($scope.selected[index]);
      $scope.selected.splice(index, 1);
    };
  })
  .controller('tooltipDemoCtrl', function ($scope, $sce) {
    $scope.dynamicTooltip = 'Hello, World!';
    $scope.dynamicTooltipText = 'dynamic';
    $scope.htmlTooltip = $sce.trustAsHtml('I\'ve been made <b>bold</b>!');
  });
;
