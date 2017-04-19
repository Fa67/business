/**
 * Created by eayun on 2017/2/22.
 */
'use strict'

angular.module('eayunApp.controllers')
    .config(function ($stateProvider, $urlRouterProvider) {

        $stateProvider.state('app.rds.detail.dbinstance', {
            url: '/detail',
            templateUrl: 'views/rds/instance/detail/detail.html',
            controller: 'RdsInstanceDetailInstanceCtrl',
            controllerAs: 'detail'
        })

        .state('app.rds.detail.database', {
            url: '/database',
            templateUrl: 'views/rds/instance/database/databasemng.html',
            controller: 'RdsInstanceDetailDatabaseCtrl',
            controllerAs: 'database'
        })

        .state('app.rds.detail.account', {
            url: '/account',
            templateUrl: 'views/rds/instance/account/accountmng.html',
            controller: 'RdsInstanceDetailAccountCtrl',
            controllerAs: 'account'
        });

    })
    .controller('RdsInstanceDetailCtrl', ['$rootScope', 'RDSInstanceService', '$stateParams',
        function ($rootScope, RDSInstanceService, $stateParams) {
            var vm = this;
            vm.showMembers = false;
            if (angular.isDefined($stateParams.rdsId)) {
                RDSInstanceService.getInstanceById($stateParams.rdsId).then(function (_instance) {
                    vm.showMembers = (_instance.isMaster === '1');
                });
            } else {
                vm.showMembers = false;
            }
    }]);