/**
 * Created by eayun on 2017/2/22.
 */
'use strict';

angular.module('eayunApp.controllers')
    .config(function ($stateProvider, $urlRouterProvider) {

        $urlRouterProvider.when('/app/rds', '/app/rds/instance');

        $stateProvider.state('app.rds.instance', {
            url: '/instance',
            templateUrl: 'views/rds/instance/instancemng.html',
            controller: 'RdsInstanceCtrl',
            controllerAs: 'instance'
        })

        .state('app.rds.backup', {
            url: '/backup/:params',
            templateUrl: 'views/rds/backup/backupmng.html',
            controller: 'RdsBackupCtrl',
            controllerAs: 'backup'
        })

        .state('app.rds.config', {
            url: '/config',
            templateUrl: 'views/rds/config/configmng.html',
            controller: 'RdsConfigCtrl',
            controllerAs: 'config'
        })

        .state('app.rds.detail', {
            url: '/:rdsId',
            templateUrl: 'views/rds/instance/detail.html',
            controller: 'RdsInstanceDetailCtrl',
            controllerAs: 'detailIndex'
        })

    })
    .controller('RdsCtrl', ['$scope', '$window', '$rootScope', 'cloudprojectList','powerService', function ($scope, $window, $rootScope, cloudprojectList, powerService) {

        powerService.powerRoutesList().then(function(powerList){
            $scope.modulePower = {
                rdsBackupView: powerService.isPower('rds_backup_view'),
            };
        });

        $scope.model = {};
        $scope.cloudprojectList = cloudprojectList;
        var daPrj = sessionStorage["dcPrj"];
        if (daPrj) {
            daPrj = JSON.parse(daPrj);
            angular.forEach($scope.cloudprojectList, function (value, key) {
                if (value.projectId == daPrj.projectId) {
                    $scope.model.projectvoe = value;
                }
            });
        } else {
            angular.forEach($scope.cloudprojectList, function (value) {
                if (value.projectId != null && value.projectId != '' && value.projectId != 'null') {
                    $scope.model.projectvoe = value;
                    $window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
                }
            });
        }

        $scope.$watch('model.projectvoe.projectId', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                if (newVal == null || newVal == '' || newVal == 'null') {
                    $scope.dcId = $scope.model.projectvoe.dcId;
                    angular.forEach($scope.cloudprojectList, function (value) {
                        if (oldVal == value.projectId) {
                            $scope.model.projectvoe = value;
                            return false;
                        }
                    });
                    eayunHttp.post('cloud/project/findProByDcId.do', {dcId: $scope.dcId}).then(function (response) {
                        if (response.data) {
                            eayunModal.warning("您在该数据中心下没有具体资源的访问权限");
                        } else {
                            eayunModal.warning("您在该数据中心下没有任何项目");
                        }
                    });
                } else {
                    $window.sessionStorage["dcPrj"] = JSON.stringify($scope.model.projectvoe);
                }
            }
        }, true);

        $rootScope.navList = function (_routerList, _routerName, _viewType) {
            $scope.routerList = _routerList;
            $scope.routerName = _routerName;
            $scope.viewType = _viewType;
        };
    }]);