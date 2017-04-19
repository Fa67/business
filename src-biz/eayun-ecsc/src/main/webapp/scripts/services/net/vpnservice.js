/**
 * Created by eayun on 2016/8/11.
 */
'use strict';

angular.module('eayunApp.service')
    .service('VpnService', ['eayunHttp', '$q', 'SysCode', function (eayunHttp, $q, SysCode) {
        var vpnService = this;
        /*根据订单编号查询订单信息*/
        vpnService.getOrderVpnByOrderNo = function (_orderNo) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/getordervpnbyorderno.do', {
                orderNo: _orderNo
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data.data);
                }
            });
            return deferred.promise;
        };

        vpnService.checkIfOrderExist = function (_vpn) {
            var deferred = $q.defer();
            //检查当前是否有正在续费、正在升级、支付未结束的订单或操作，如果有，则不允许续费；
            eayunHttp.post('cloud/vpn/checkVpnOrderExist.do', _vpn.vpnId).then(function (response) {
                if (response.data.flag) {
                    deferred.resolve();
                } else {
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

        vpnService.getVpnInfo = function (_vpnId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/getvpninfo.do', {vpnId: _vpnId}).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        vpnService.checkVpnNameExist = function (_vpn) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/checkvpnnameexist.do', {
                prjId: _vpn.prjId,
                vpnId: _vpn.vpnId,
                vpnName: _vpn.vpnName
            }).then(function (response) {
                if (response.data.respCode == '000000') {
                    deferred.resolve(response.data.respData);
                } else if (response.data.respCode == '010120') {
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

        vpnService.getNetworkListByPrjId = function (_prjId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/querynetworkbyprjid.do', {
                prjId: _prjId
            }).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        };

        vpnService.getSubnetListByNetId = function (_netId, _subnetType) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/subnetwork/getsubnetlist.do', {
                netId: _netId,
                subnetType: _subnetType
            }).then(function (response) {
                deferred.resolve(response.data.resultData);
            });
            return deferred.promise;
        };

        vpnService.buyVpn = function (_vpn) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/buyvpn.do', {
                payType: _vpn.payType,
                orderType: 0,
                price: _vpn.price,
                accountPayment: _vpn.accountPayment,
                thirdPartPayment: _vpn.thirdPartPayment,
                buyCycle: _vpn.buyCycle,
                dcId: _vpn.dcId,
                dcName: _vpn.dcName,
                prjId: _vpn.prjId,
                networkId: _vpn.networkId,
                netName: _vpn.networkName,
                subnetId: _vpn.subnetId,
                subnetName: _vpn.subnetName,
                subnetCidr: _vpn.subnetCidr,
                routeId: _vpn.routeId,
                vpnName: _vpn.vpnName,
                ikeLifetime: _vpn.ikeLifetime,
                ikeNegotiation: _vpn.ikeNegotiation,
                ikeAuth: _vpn.ikeAuth,
                ikeEncryption: _vpn.ikeEncryption,
                ikeDh: _vpn.ikeDh,
                ikeVersion: _vpn.ikeVersion,
                ipsecLifetime: _vpn.ipsecLifetime,
                ipsecProtocol: _vpn.ipsecProtocol,
                ipsecAuth: _vpn.ipsecAuth,
                ipsecEncapsulation: _vpn.ipSecEncapsulation,
                ipsecEncryption: _vpn.ipsecEncryption,
                ipsecDh: _vpn.ipsecDh,
                dpdAction: _vpn.dpdAction,
                dpdInterval: _vpn.dpdInterval,
                dpdTimeout: _vpn.dpdTimeout,
                pskKey: _vpn.pskKey,
                initiator: _vpn.initiator,
                peerId: /*_vpn.peerId*/_vpn.peerAddress,
                peerCidrs: _vpn.peerCidrs,
                peerAddress: _vpn.peerAddress,
                mtu: _vpn.mtu
            }).then(function (response) {
                if (response.data.respCode == SysCode.success) {
                    deferred.resolve(response.data);
                } else if (response.data.respCode == SysCode.warning) {
                    deferred.reject(response.data.respMsg);
                } else if (response.data.respCode == SysCode.error) {
                    deferred.reject('500');
                }
            });
            return deferred.promise;
        };

        vpnService.updateVpn = function (_vpn) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/updatevpn.do', {
                dcId: _vpn.dcId,
                prjId: _vpn.prjId,
                payType: _vpn.payType,
                createTime: _vpn.createTime,
                endTime: _vpn.endTime,
                chargeState: _vpn.chargeState,

                vpnId: _vpn.vpnId,
                vpnName: _vpn.vpnName,
                peerId: /*_vpn.peerId*/_vpn.peerAddress,
                peerCidrs: _vpn.peerCidrs,
                peerAddress: _vpn.peerAddress,
                pskKey: _vpn.pskKey,
                mtu: _vpn.mtu,
                dpdAction: _vpn.dpdAction,
                dpdInterval: _vpn.dpdInterval,
                dpdTimeout: _vpn.dpdTimeout,
                initiator: _vpn.initiator,
                ikeId: _vpn.ikeId,
                ipsecId: _vpn.ipsecId,
                vpnserviceId: _vpn.vpnserviceId
            }).then(function (response) {
                if (response.data.respData) {
                    deferred.resolve();
                } else {
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

        vpnService.deleteVpn = function (_vpn) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/deletevpn.do', {
                dcId: _vpn.dcId,
                prjId: _vpn.prjId,
                vpnId: _vpn.vpnId,
                vpnName: _vpn.vpnName,
                ikeId: _vpn.ikeId,
                ipsecId: _vpn.ipsecId,
                vpnserviceId: _vpn.vpnserviceId,
                payType: _vpn.payType
            }).then(function (response) {
                if (response.data.respData) {
                    deferred.resolve();
                } else {
                    deferred.reject();
                }
            });
            return deferred.promise;
        };

        vpnService.getPrice = function (_vpn) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/getprice.do', {
                dcId: _vpn.dcId,
                payType: _vpn.payType,
                buyCycle: _vpn.buyCycle
            }).then(function (response) {
                switch (response.data.respCode) {
                    case SysCode.success:
                        deferred.resolve(response.data.data);
                        break;
                    case SysCode.error:
                        deferred.reject(response.data.message);
                        break;
                }
            });
            /*eayunHttp.post('billing/factor/getPriceByFactor.do', {
                dcId: _vpn.dcId,
                payType: _vpn.payType,
                number: 1,
                vpnCount: 1,
                cycleCount: _vpn.buyCycle
            }).then(function (response) {
                switch (response.data.respCode) {
                    case SysCode.success:
                        deferred.resolve(response.data.message);
                        break;
                    case SysCode.error:
                        deferred.reject(response.data.message);
                        break;
                }
            });*/
            return deferred.promise;
        };

        vpnService.queryAccount = function () {
            var deferred = $q.defer();
            eayunHttp.post('costcenter/accountoverview/getaccountbalance.do').then(function (response) {
                deferred.resolve(response.data.data.money);
            });
            return deferred.promise;
        };

        vpnService.getVpnQuotasByPrjId = function (_prjId) {
            var deferred = $q.defer();
            eayunHttp.post('cloud/vpn/getvpnquotasbyprjid.do', {
                prjId: _prjId
            }).then(function (response) {
                deferred.resolve(response.data.quota);
            });
            return deferred.promise;
        };
        vpnService.checkPeerCidrs = function (_model, inputFormat) {
            var checkPeerCidrsFlag = true;
            var peerCidrs = _model.peerCidrs;
            //判断字符串中是否有中文的逗号
            if (peerCidrs.indexOf("，") > -1)
                return false;
            var peerCidr = [];
            var list = [];
            peerCidr = peerCidrs.split(",");
            for (var key = 0; key < peerCidr.length; key++) {
                if (peerCidr[key] != '')
                    checkPeerCidrsFlag = inputFormat.test(peerCidr[key]);
                else
                    return false;
                if (!checkPeerCidrsFlag) {
                    return checkPeerCidrsFlag;
                } else {
                    list[key] = peerCidr[key].split("/")[0];
                }
            }
            if (checkPeerCidrsFlag) {
                var sList = list.join(",") + ",";
                for (var i = 0; i < list.length; i++) {
                    if (sList.replace(list[i] + ",", "").indexOf(list[i] + ",") > -1) {
                        return !checkPeerCidrsFlag;
                    }
                }
            }
            return checkPeerCidrsFlag;
        };
    }]);