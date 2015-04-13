/**
 * Alerts Controller
 */

angular
    .module('RDash')
    .controller('AlertsCtrl', ['$scope', AlertsCtrl]);

function AlertsCtrl($scope) {
    $scope.robot = 'true';
    $scope.hideRobot = function() {
        $scope.robot='false';
    };

    $scope.hints = [{
        value: 'use "n" letter as the servers number',
        done: 'false'
    }, {
        value: 'copy your local application to your private github repository',
        done: 'false'
    }, {
        value: 'insert link to your github repository',
        done: 'false'
    }, {
        value: 'set value of something to something if lorem ipsum',
        done: 'false'                
    }, {
        value: 'something and something to something if lorem ipsum',
        done: 'false'                
    }, {
        value: 'set value of something to something if lorem ipsum',
        done: 'false'                
    }, {            
        value: 'save your script by pushing the "save your script" button',
        done: 'false'
    }];

    $scope.closeHint = function(index) {
        $scope.hints.splice(index, 1);
    };

}