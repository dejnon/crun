/**
 * Script Controller
 */

angular
    .module('RDash')
    .controller('ScriptCtrl', ['$scope','$rootScope', ScriptCtrl]);

function ScriptCtrl($scope, $rootScope) {
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/monokai");
    editor.getSession().setMode("ace/mode/sh");
    $rootScope.commands;
    editor.setValue($rootScope.commands);


    $scope.clearWindow = function() {
        editor.setValue("");
    };

    $scope.showWindow = function() {
        editor.setValue('pwd       ' +
        '\nping -c 5 wwww.gooogle.pl'
        +'\nifconfig');
    };

    $scope.$on('sid', function(events, args){
        var sid = args;
        var cmd = editor.getValue();
        console.log("sid function" + {cmd:cmd, sid:sid});
        $rootScope.$broadcast('skrypt', {cmd:cmd, sid:sid});
    });

    $scope.saveWindow = function() {
        $rootScope.commands = editor.getValue();
        alert("To save it on server, You need to choose one of the listed serves!");
    };
}

