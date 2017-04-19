'use strict';

/**
 * @ngdoc service
 * @name eayunApp.constant
 * @description
 * # constant
 * Constant in the eayunApp.
 */
angular.module('eayunApp.constant', [])
    .provider("ObsBasePath", function(){
        this.$get = ['eayunHttp','$q',function(eayunHttp,$q){
        	var ObsBasePath = {};
        	var path;
        	ObsBasePath.getPath = function(){
        		var deferred = $q.defer();
        		if(!path){
        			eayunHttp.post('obs/storage/getEayunObsHost.do',{}).then(function(resp){
            			path = '.'+resp.data.eayunObsHost;
            			deferred.resolve(path);
                    });
        		}else{
        			deferred.resolve(path);
        		}
        		return deferred.promise;
        	};
            return ObsBasePath;
        }];
    })
    .constant('AuthState',{
        'app.cloud.cloudhost.volume.list' : {jumpTarget: 'app.cloud.cloudhost',auth:'disk_view'},
    	'app.cloud.cloudhost.volume.snapshot' : {jumpTarget: 'app.cloud.cloudhost.host',auth:'snap_view'},
    	'app.monitor.monitorbar.alarm.rule' : {jumpTarget: 'app.monitor.monitorbar.alarm.list',auth:'alarm_rulemng'} ,
    	'app.monitor.monitorbar.contact.list' : {jumpTarget: 'app.monitor.monitorbar.alarm.list',auth:'contact_view'},
    	'app.monitor.monitorbar.contact.group' : {jumpTarget: 'app.monitor.monitorbar.alarm.list',auth:'contact_view'},
    	'app.auth.customer' : {jumpTarget: 'app.auth.user',auth:'customer_view'},
    	'app.auth.usermanage' : {jumpTarget: 'app.auth.user',auth:'usermng_view'},
    	'app.role.role' : {jumpTarget: 'app.auth.user',auth:'role_mng'},
    	'app.accesskey.accesskey' : {jumpTarget: 'app.auth.user',auth:'api_mng'},
    	'app.business.tag.tagGroupList' : {jumpTarget: 'app.auth.user',auth:'tag_view'},
    	'app.business.tag.tagDetail' : {jumpTarget: 'app.auth.user',auth:'tag_view'},
    	'app.business.syssetup' : {jumpTarget: 'app.auth.user',auth:'syssetup_view'},
    	'app.count.count' : {jumpTarget:'app.auth.user',auth:'result_view'},
    	'app.log.log' : {jumpTarget: 'app.auth.user',auth:'log_view'},
    	'app.costcenter.guidebar.account':{jumpTarget: 'app.auth.user',auth:'account_view'},
    	'app.costcenter.guidebar.report.postpay':{jumpTarget: 'app.auth.user',auth:'report_view'},
    	'app.costcenter.guidebar.report.prepayment':{jumpTarget: 'app.auth.user',auth:'report_view'},
    	'app.costcenter.prepaymentDetail':{jumpTarget: 'app.auth.user',auth:'report_view'},
    	'app.costcenter.postpayDetail':{jumpTarget: 'app.auth.user',auth:'report_view'}
    })
	.constant('SysCode', {
		success: '000000',
		error: '010120',
		warning: '010110'
	})
	.constant('BuyCycle', {
		cycleTypeList: [
			{
				text: '年付',
				value: 'year'
			},
			{
				text: '月付',
				value: 'month'
			}
		],
		cycleList: {
			'year': [
				{
					text: '1年',
					value: '12'
				},
				{
					text: '2年',
					value: '24'
				},
				{
					text: '3年',
					value: '36'
				}
			],
			'month': [
				{
					text: '1个月',
					value: '1'
				},
				{
					text: '2个月',
					value: '2'
				},
				{
					text: '3个月',
					value: '3'
				},
				{
					text: '4个月',
					value: '4'
				},
				{
					text: '5个月',
					value: '5'
				},
				{
					text: '6个月',
					value: '6'
				},
				{
					text: '7个月',
					value: '7'
				},
				{
					text: '8个月',
					value: '8'
				},
				{
					text: '9个月',
					value: '9'
				},
				{
					text: '10个月',
					value: '10'
				},
				{
					text: '11个月',
					value: '11'
				}
			]
		}
	});
