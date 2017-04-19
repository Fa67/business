'use strict';
/**
 * Created by zhenghailong on 2016/3/7.
 */
angular.module('eayunApp.service')
    /**
     * @ngdoc service
     * @name eayunApp.obsHttp
     * @description
     * # obsHttp
     * 对象存储Ajax服务
     */
    .factory('ObsHttp', ['eayunHttp', 'ObsBasePath', '$http', function (eayunHttp, ObsBasePath, $http) {
        var obsHttp = {};
        var eayunObsRequest='';
        var setAuth = function (config, bucketName, url, httpMethod) {
            config.headers = config.headers || {};
            return eayunHttp.post('obs/storage/getAuthorization.do', {
                uri: encodeURIComponent('/' + bucketName + url),
                httpMethod: httpMethod.toUpperCase(),
                contentType: config.headers['Content-Type'] || ''
            }).then(function (response) {
                config.headers.Authorization = response.data.authorization;
                config.headers['x-amz-date'] = response.data.date;
                return config;
            });
        };

        // GET DELETE HEAD JSONE 请求
        angular.forEach(['get', 'delete', 'head', 'jsonp'], function (name) {
            obsHttp[name] = function (bucketName, url, config) {
                config = config || {};
                return setAuth(config, bucketName, url, name).then(function (config) {
                	return ObsBasePath.getPath().then(function(path){
                		 return eayunHttp.post('obs/storage/getEayunOBSRequest.do').then(function(response){
                         	return $http[name](response.data.eayunOBSRequest + bucketName + path + url, config);
                         });
                    });
                });
            };
        });

        //POST PUT 请求
        angular.forEach(['post', 'put'], function (name) {
            obsHttp[name] = function (bucketName, url, data, config) {
                config = config || {};
                return setAuth(config, bucketName, url, name).then(function (config) {
                    return ObsBasePath.getPath().then(function(path){
                    	 return eayunHttp.post('obs/storage/getEayunOBSRequest.do').then(function(response){
                    		 return $http[name](response.data.eayunOBSRequest + bucketName + path + url, data, config);
                          });
                    });
                });
            };
        });

        return obsHttp;
    }])
    /**
     * @ngdoc service
     * @name eayunApp.obsService
     * @description
     * # obsService
     * 对象存储API服务
     */
    .service('obsService', ['ObsHttp', '$q', function (ObsHttp, $q) {
        var api = {
            initMultipartUpload: function (bucketName, address, fileName) {
                return ObsHttp.post(bucketName, '/' + address + fileName + '?uploads', {}, {
                    headers: {
                        'Content-Type': 'application/octet-stream'
                    }
                }).then(function (response) {
                    var data = response.data;
                    var reMsg = data.substring(data.indexOf('<UploadId>') + 10, data.lastIndexOf('</UploadId>'));
                    return reMsg;
                });
            },
            multipartUpload: function (bucketName, address, fileName, uploadID, file) {
                var deferred = $q.defer();
                var upload = function (_config) {
                    var url = '/' + address + fileName + '?partNumber=' + _config.partNumber + '&uploadId=' + uploadID;
                    return ObsHttp.put(bucketName, url, file.slice(_config.transferSize, _config.end), {
                        headers: {
                            'Content-Type': 'application/octet-stream'
                        }
                    }).then(function () {

                    }, function (error) {
                        var data = error.data;
                        var reMsg = data.substring(data.indexOf('<Code>') + 6, data.lastIndexOf('</Code>'));
                        deferred.reject(reMsg);
                    });
                };
                var config = {
                    totalSize: file.size,
                    chunkSize: Math.ceil(file.size / 100) > 5242880 ? Math.ceil(file.size / 100) : 5242880,
                    partNumber: 0,
                    transferSize: 0,
                    end: 0,
                    isDone: false,
                    getNext: function () {
                        config.partNumber++;
                        config.end = config.transferSize + config.chunkSize;
                        if (config.end >= config.totalSize) {
                            config.isDone = true;
                            config.end = config.totalSize;
                        }
                    },
                    sendFile: function () {
                        if (file.cancel) {
                            api.cancelUpload(bucketName, address, fileName, uploadID);
                            return deferred.reject('用户取消上传');
                        }
                        if (config.isDone) {
                            deferred.resolve(uploadID);
                        } else {
                            config.getNext();
                            upload(config).then(function () {
                                config.transferSize = config.end;
                                deferred.notify(config.transferSize);
                                config.sendFile();
                            });
                        }
                    }
                };
                config.sendFile();
                return deferred.promise;
            },
            multipartList: function (bucketName, address, fileName, uploadID) {
                return ObsHttp.get(bucketName, '/' + address + fileName + '?uploadId=' + uploadID, {}, {})
                    .then(function (response) {
                        var data = response.data;
                        var reMsg = data.substring(data.indexOf('<Part>'), data.lastIndexOf('</Part>') + 7);
                        return reMsg;
                    });
            },
            multipartComplete: function (bucketName, address, fileName, uploadID) {
                return api.multipartList(bucketName, address, fileName, uploadID).then(function (xmlStr) {
                    xmlStr = '<CompleteMultipartUpload>' + xmlStr + '</CompleteMultipartUpload>';
                    return ObsHttp.post(bucketName, '/' + address + fileName + '?uploadId=' + uploadID, xmlStr, {
                        headers: {
                            'Content-Type': 'application/octet-stream'
                        }
                    });
                });
            },
            cancelUpload: function (bucketName, address, fileName, uploadID) {
                return ObsHttp.delete(bucketName, '/' + address + fileName + '?uploadId=' + uploadID, {}, {});
            }
        };
        return {
            uploadFile: function (_bucketName, _address, file) {
                var bucketName = encodeURIComponent(_bucketName),
                    address = _address,
                    fileName = encodeURIComponent(file.name),
                    deferred = $q.defer();
                api.initMultipartUpload(bucketName, address, fileName)
                    .then(function (uploadId) {
                        //初始化上传成功，准备开始上传文件
                        deferred.notify(0);
                        if (file.cancel) {
                            api.cancelUpload(bucketName, address, fileName, uploadId);
                            return deferred.reject('用户取消上传');
                        }
                        file.done = false;
                        file.uploadId = uploadId;
                        return api.multipartUpload(bucketName, address, fileName, uploadId, file);
                    })
                    .then(function (uploadId) {
                        //文件上传成功，准备开始合并文件
                        if (file.cancel) {
                            api.cancelUpload(bucketName, address, fileName, uploadId);
                            return deferred.reject('用户取消上传');
                        }
                        return api.multipartComplete(bucketName, address, fileName, uploadId);
                    }, function (error) {
                        //文件上传失败
                        deferred.reject(error);
                    }, function (message) {
                        //分段上传完成
                        deferred.notify(message);
                    })
                    .then(function (response) {
                        //文件上传完成
                        deferred.resolve(response);
                    });
                return deferred.promise;
            },
            cancelUpload: function (_bucketName, _address, file) {
                var bucketName = encodeURIComponent(_bucketName),
                    address = _address,
                    fileName = encodeURIComponent(file.name),
                    uploadId = file.uploadId;
                return api.cancelUpload(bucketName, address, fileName, uploadId);
            }
        };
    }])
;