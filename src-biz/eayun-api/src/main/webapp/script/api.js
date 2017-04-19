angular.module("eayun.api", [])
    .config(function () {
        requirejs.config({
            baseUrl: 'script'
        })
    })
    .controller("apiCtrl", ['$scope', '$http', function ($scope, $http) {
        $scope.api = {};
        $scope.params = "";
        $scope.api.replies = [{key: "", value: ""}];
        $scope.api.canDescReply = false;
        $scope.api.incrReply = function ($index) {
            //$scope.api.replies.splice($index + 1, 0,
            //    {key: "", value: ""});
            $scope.api.replies.push({});
        }

        $scope.api.decrReply = function ($index) {
            if ($scope.api.replies.length > 1) {
                $scope.api.replies.splice($index, 1);
            }
        }
        $scope.callApi = function () {
            requirejs(['crypto'], function (CryptoJS) {
                var config = {
                    url: "http://" + window.location.host + $scope.url,
                    method: $scope.method,
                    headers: {}
                };

                for (var i = 0; i < $scope.api.replies.length; i++) {
                    config.headers[$scope.api.replies[i].key] = $scope.api.replies[i].value;
                }

                var ak = $scope.ak,
                    sk = $scope.sk,
                    dateStr = config.headers['x-date'];
                var signature = CryptoJS.createHmac('sha1', sk).update(dateStr).digest().toString('base64');
                config.headers.Authorization = 'Basic ' + btoa(ak + ':' + signature);
                config.data = $scope.params;

                $http(config).success(function (data, status, fn, config) {
                    $scope.data = data;
                    $scope.headers = config.headers;
                }).error(function (data, status, fn, config) {
                    $scope.data = data;
                    $scope.headers = config.headers;
                });
            });
        }
    }]);
