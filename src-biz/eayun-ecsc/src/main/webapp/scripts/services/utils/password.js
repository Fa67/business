/**
 * Created by eayun on 2017/2/25.
 */
'use strict';

angular.module('eayunApp.service')
    .service('PWService', [function () {

        var vm = this;

        vm.threeRules = function (_password) {
            var pwd = _password;
            var numFlag = 0;
            var lowerCharFlag = 0;
            var upperCharFlag = 0;
            var regex = new RegExp("^[0-9a-zA-Z]{8,30}$");
            var regexNum = new RegExp("^[0-9]$");
            var regexLowerChar = new RegExp("^[a-z]$");
            var regexUpperChar = new RegExp("^[A-Z]$");
            if (pwd && regex.test(pwd)) {
                for (var i = 0; i < pwd.length; i++) {
                    if (pwd[i] && regexNum.test(pwd[i])) {
                        numFlag = 1;
                        continue;
                    }
                    else if (pwd[i] && regexLowerChar.test(pwd[i])) {
                        lowerCharFlag = 1;
                        continue;
                    }
                    else if (pwd[i] && regexUpperChar.test(pwd[i])) {
                        upperCharFlag = 1;
                        continue;
                    }
                }
            }
            return (numFlag + lowerCharFlag + upperCharFlag) < 3;
        };

        vm.threeOfFourRules = function (_password) {
            var pwd = _password;
            var numFlag = 0;
            var lowerCharFlag = 0;
            var upperCharFlag = 0;
            var specCharFlag = 0;
            var regex = new RegExp("^[0-9a-zA-Z~@#%+-=\/\(_\)\*\&\<\>\[\\]\\\\\"\;\'\|\$\^\?\!.\{\}\`/\,]{8,30}$");
            var regexNum = new RegExp("^[0-9]$");
            var regexLowerChar = new RegExp("^[a-z]$");
            var regexUpperChar = new RegExp("^[A-Z]$");
            var regexSpecChar = new RegExp("^[~@#%+-=\/\(_\)\*\&\<\>\[\\]\\\\\"\;\'\|\$\^\?\!.\{\}\`/\,]$");
            if (pwd && regex.test(pwd)) {
                for (var i = 0; i < pwd.length; i++) {
                    if (pwd[i] && regexNum.test(pwd[i])) {
                        numFlag = 1;
                        continue;
                    }
                    else if (pwd[i] && regexLowerChar.test(pwd[i])) {
                        lowerCharFlag = 1;
                        continue;
                    }
                    else if (pwd[i] && regexUpperChar.test(pwd[i])) {
                        upperCharFlag = 1;
                        continue;
                    }
                    else if (pwd[i] && regexSpecChar.test(pwd[i])) {
                        specCharFlag = 1;
                        continue;
                    }
                }
            }
            return (numFlag + lowerCharFlag + upperCharFlag + specCharFlag) < 3;
        };

    }]);