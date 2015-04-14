/**
 * Servers Controller
 */

angular
    .module('RDash')
    .controller('ServersCtrl', ['$scope','$rootScope','$http','$filter', ServersCtrl]);

function ServersCtrl($scope, $rootScope, $http, $filter) {

    var adres = "";

    var loc = window.location;
    adres = loc.host;

    $rootScope.servers;
    if ($rootScope.results == undefined) {
        $rootScope.results={};        
    }
    $scope.see;

    $scope.connected = 'false';
    $scope.connect = 'true';
    $scope.loading = 'false';

        $scope.myWebSocket = new WebSocket("ws://"+adres+"/api/listmachines");

        $scope.myWebSocket.onmessage = function(event) {
            console.log("onmessage. size: " + event.data);
            var obj = JSON.parse(event.data);
            $scope.$apply(function() {
                $rootScope.servers = obj;
            });
        }

        $scope.myWebSocket.onopen = function(evt) {
            $scope.myWebSocket.send("START");
            console.log("onopen - myWebSocket");
            $scope.connected='true';
            $scope.connect='false';
        };

        $scope.myWebSocket.onclose = function(evt) {
            console.log("onclose - myWebSocket");
            $scope.connected='false';
            $scope.connect='true';
        };

        $scope.myWebSocket.onerror = function(evt) {
            console.log("Error!");
        };

        $scope.myResultWebSocket = new WebSocket("ws://"+adres+"/api/screens");
       
        $scope.myResultWebSocket.onmessage = function(event) {
            console.log("My myResultWebSocket size: " + event.data);
            var obj = JSON.parse(event.data);

            id = obj.id.toString();
            console.log("ID: "+id);
            cmd = obj.cmd;
            console.log("cmd: "+cmd);

            $scope.$apply(function() {
                if(id in $rootScope.results) {
                    $rootScope.results[id].push(cmd);
                } else {
                    $rootScope.results[id] = [cmd];
                } 
            });

            console.log($rootScope.results);

        };


        $scope.myResultWebSocket.onopen = function(evt) {
            $scope.myResultWebSocket.send("START");
            console.log("onopen - myResultWebSocket");
            $scope.connected='true';
            $scope.connect='false';
        };

        $scope.myResultWebSocket.onclose = function(evt) {
            console.log("onclose - myResultWebSocket");
            $scope.connected='false';
            $scope.connect='true';
        };

        $scope.myResultWebSocket.onerror = function(evt) {
            console.log("Error!");
        };


    $scope.addServer = function() {
        $http.get('http://'+adres+'/api/addmachine').
            success(function(data){
                console.log("OK, wyslano zadanie dodania maszyny");
            }).
            error(function(data){
                console.log("Fail http get");
            });
    };
    
    $scope.connectToWS = function() {
        $scope.myWebSocket.send("START");
        $scope.myResultWebSocket.send("START");
    }

    $scope.seeServer = function(index) {
        $scope.see=index;
    };

    $scope.sendToServer = function(index){
        var sid = $rootScope.servers[index].id;
        console.log("send server sid: "+sid);
        $rootScope.$broadcast('sid', sid);
    };

    $scope.$on('skrypt', function(events, args){
        var sid = args.sid;
        var cmd = args.cmd; 
        console.log("skrypt broadcast sid: "+sid+" cmd: "+cmd);
       $scope.myResultWebSocket.send(
           '{"id": "'+sid+'", "cmd": "'+cmd+'"}'
       );

    });

    $scope.closeServer = function(index) {
        $scope.servers.splice(index, 1);
    };


    $scope.refreshServers = function() {
        $http.get('http://'+adres+'/api/refreshmachines').success(function(data){
            console.log("OK, list will be refreshed");
        }).error(function(data){
            console.log("Fail http get");
        });
    };

    $scope.closeAllServers = function() {
        $http.get('http://'+adres+'/api/removeall').success(function(data){
            console.log("OK, all servers will be removed soon");
        }).error(function(data){
            console.log("Fail http get");
        });
    };
}