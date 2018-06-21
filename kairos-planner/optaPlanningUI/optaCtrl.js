var app = angular.module('myApp', []);
app.controller('namesCtrl',['$scope','$http',function($scope,$http) {
    $scope.firstName = "pradeep";

    $scope.unplanned = {};
    $scope.task = {};
    $scope.employees = [];
    $scope.showSecond = false;

    $scope.submitXml = function(){
        var task = $scope.task;
        $http({method:'POST',url: 'http://localhost:8081/api/taskPlanning/planning/submitXml',data:task}).then(function(response) {
            $scope.employees = response.data.data.emplyees;
            $scope.tasksList = response.data.data.unassignTask;
            $scope.unavailableEmpl = response.data.data.unavailableEmployees;
            $scope.taskListSize = response.data.data.taskListSize;
            // $scope.citizenList = response.data.data.citizenList;
            $scope.vehicleList = response.data.data.vehicleList;
            $scope.employeeList = response.data.data.employeeList;
            $scope.unassigntasksList = response.data.data.unassignTaskList;
            $scope.assignedEmp = response.data.data.assignedEmp;
            $scope.unAssignEmp = response.data.data.unAssignEmp;
            $scope.avialableEmp = response.data.data.avialableEmp;

            $scope.plannerScore = response.data.data.plannerScore;
        });
    };
    $scope.counter=0;
    $scope.getStatusClass = function(value){
        if(value) return "taskHardContraints";

    };

   


    $scope.getStatus = function(){
        $http({method:'GET',url: 'http://localhost:8081/api/opta/getStatus',params: {id: $scope.problemId.planningId}}).then(function(response) {
            $scope.planningProblem = response.data.data;
        });

    };

    $scope.getMapByEmployee = function(){
        $scope.mapShow = true;
        var mapOptions = {
            center: new google.maps.LatLng(28.5355, 77.3910),
            zoom: 10,
            mapTypeId: google.maps.MapTypeId.HYBRID
        }
        var map = new google.maps.Map(document.getElementById("map"), mapOptions);
    };
    var toUTCDate = function(date){
        var _utc = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(),  date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
        return _utc;
    };

    $scope.millisToUTCDate = function(millis){
        return toUTCDate(new Date(millis));
    };


    $scope.getMap = function(employee){
        $scope.mapShow = true;
        var locations = [];
        var emplocation = employee.employeeLocation;
        locations.push(emplocation);
        angular.forEach(employee.nextTasks,function(task){
            locations.push(task.taskLocation);
        });
        var geocoder;
        var map;
        var directionsDisplay;
        var directionsService = new google.maps.DirectionsService();

        directionsDisplay = new google.maps.DirectionsRenderer();


        var map = new google.maps.Map(document.getElementById('map'), {
            zoom: 25,
            center: new google.maps.LatLng(locations[0].latitude, locations[0].longitude),
            mapTypeId: google.maps.MapTypeId.ROADMAP
        });
        directionsDisplay.setMap(map);
        var infowindow = new google.maps.InfoWindow();

        var marker, i;
        var request = {
            travelMode: google.maps.TravelMode.DRIVING
        };
        for (i = 0; i < locations.length; i++) {
            marker = new google.maps.Marker({
                position: new google.maps.LatLng(locations[i].latitude, locations[i].longitude),
            });

            google.maps.event.addListener(marker, 'click', (function(marker, i) {
                return function() {
                    infowindow.setContent(locations[i]);
                    infowindow.open(map, marker);
                }
            })(marker, i));

            if (i == 0) request.origin = marker.getPosition();
            else if (i == locations.length - 1) request.destination = marker.getPosition();
            else {
                if (!request.waypoints) request.waypoints = [];
                request.waypoints.push({
                    location: marker.getPosition(),
                    stopover: true
                });
            }

        }
        directionsService.route(request, function(result, status) {
            if (status == google.maps.DirectionsStatus.OK) {
                directionsDisplay.setDirections(result);
            }
        });
    };

    $scope.getDirection = function(){
        var geocoder;
        var map;
        var directionsDisplay;
        var directionsService = new google.maps.DirectionsService();
        /*var locations = [
            ['Noida', 28.5355, 77.3910, 2],
            ['gurgaon', 28.4595, 77.0266, 4],
            ['Delhi',28.7041, 77.1025, 5],
            ['Rohtak', 28.8955, 76.6066, 1],
            ['Plawal', 28.1487, 77.3320, 3]
        ];*/

        var locations= [['Task1',56.46255674,10.03392478,1002155], ['Task2',56.63988443,9.79311734,149], ['Task3',56.63988443,9.79311734,6661], ['Task4',56.63665599,9.79565149,3214], ['Task5',56.63890169,9.79341759,431], ['Task6',56.63636299,9.79634343,178], ['Task7',56.63636299,9.79634343,6016], ['Task8',56.63681427,9.79528994,2545], ['Task9',56.63681427,9.79528994,4590], ['Task10',56.63619445,9.7964408,6463], ['Task11',56.46258085,10.03426641,1002118], ['Task12',56.46258085,10.03426641,1002115], ['Task13',56.46258085,10.03426641,1002117], ['Task14',56.46258085,10.03426641,1002116], ['Task15',56.63597553,9.79687034,4272]];
        directionsDisplay = new google.maps.DirectionsRenderer();


        var map = new google.maps.Map(document.getElementById('map'), {
            zoom: 10,
            center: new google.maps.LatLng(28.4595, 77.0266),
            mapTypeId: google.maps.MapTypeId.ROADMAP
        });
        directionsDisplay.setMap(map);
        var infowindow = new google.maps.InfoWindow();

        var marker, i;
        var request = {
            travelMode: google.maps.TravelMode.DRIVING
        };
        for (i = 0; i < locations.length; i++) {
            marker = new google.maps.Marker({
                position: new google.maps.LatLng(locations[i][1], locations[i][2]),
            });

            google.maps.event.addListener(marker, 'click', (function(marker, i) {
                return function() {
                    infowindow.setContent(locations[i][0]);
                    infowindow.open(map, marker);
                }
            })(marker, i));

            if (i == 0) request.origin = marker.getPosition();
            else if (i == locations.length - 1) request.destination = marker.getPosition();
            else {
                if (!request.waypoints) request.waypoints = [];
                request.waypoints.push({
                    location: marker.getPosition(),
                    stopover: true
                });
            }

        }
        directionsService.route(request, function(result, status) {
            if (status == google.maps.DirectionsStatus.OK) {
                directionsDisplay.setDirections(result);
            }
        });

        //google.maps.event.addDomListener(window, "load", initialize);
    };

    $scope.getReasonByTask = function(value){
        if(value[0]!=0)return "Skill Mismatch";
        if(value[1]!=0)return "Wrong order";
        if(value[2]!=0)return "Dup Vehicle";
        if(value[3]!=0)return "Exceeds Availability";
        if(value[4]!=0)return "Exceeds Availability";
        if(value[5]!=0)return "Employee not assigned";
        if(value[6]!=0)return "Vehicle not assigned";
    };

    $scope.getSolution = function(){
        $http({method:'GET',url: 'http://localhost:8081/api/opta/getSolution',params: {id:$scope.problemId.planningId}}).then(function(response) {
            $scope.employees = response.data.data;
        });
    };

    $scope.getProblemIds = function(){
        $http({method:'GET',url: 'http://localhost:8081/api/opta/getProblemIds'}).then(function(response) {
            $scope.problemIds = response.data.data;
            $scope.problemId = $scope.problemIds[0];
        });
    };
    // $scope.getProblemIds();

    $scope.makeLocationData = function(){
        $http({method:'POST',url: 'http://localhost:8081/api/opta/makeLocationData'}).then(function(response) {
        });

    };
    /*        
    $scope.deleteData = function(){
        $http({method:'POST',url: 'http://localhost:8080/api/opta'}).then(function(response) {
            $scope.greeting = response.data;
        });

    };

    $scope.getPlannedSolution = function(){
        $http({method:'POST',url: 'http://localhost:8080/api/opta'}).then(function(response) {
            $scope.greeting = response.data;
        });

    };


    */


}]).config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);