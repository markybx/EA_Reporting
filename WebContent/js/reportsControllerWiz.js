'use strict';

var reportsApp = angular.module("reportsApp", [ 'ui.bootstrap', 'ui.router', 'angular-loading-bar']);

// app.js
// create our angular app and inject ngAnimate and ui-router 
// =============================================================================

// configuring our routes 
// =============================================================================
reportsApp.config(function($stateProvider, $urlRouterProvider) {
  
    $stateProvider    
        // route to show our basic form (/form)
        .state('form', {
            url: '/form',
            templateUrl: 'formwiz.html',
            controller: 'reportsController'
        })
        
        // nested states 
        // each of these sections will have their own view
        // url will be nested (/form/profile)
        .state('form.reports', {
            url: '/reports',
            templateUrl: 'choosereport.html'
        })
        
        // url will be /form/interests
        .state('form.columns', {
            url: '/columns',
            templateUrl: 'choosecolumns.html'
        })
        
        // url will be /form/payment
        .state('form.searchcriteria', {
            url: '/searchcriteria',
            templateUrl: 'searchcriteria.html'
        });
        
    // catch all route
    // send users to the form page 
    $urlRouterProvider.otherwise('/form/reports');

});

reportsApp.config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.includeBar = false;
  }]);

// controller for the form
// =============================================================================
reportsApp.controller("reportsController", function($scope, $http, 
		getTemplateListService, getColumnOptionsService, createReportService, getValueAssistanceService) {
	
	// Date stuff
	// =========================================================================
	  $scope.today = function() {
		    $scope.dt = new Date();
		  };
		  $scope.today();

		  $scope.clear = function () {
		    $scope.dt = null;
		  };

		  $scope.open = function($event, condition) {
		    $event.preventDefault();
		    $event.stopPropagation();

		    condition.openDate = true;
		  };

		  $scope.dateOptions = {
		    formatYear: 'yyyy',
		    startingDay: 1
		  };

		  $scope.formats = ['dd-MMMM-yyyy', 'yyyy-MM-dd', 'dd.MM.yyyy', 'shortDate'];
		  $scope.format = $scope.formats[0];
		  
		  $scope.formatDate = function(date){
	          var dateOut = new Date(date).toISOString().substring(0, 10);
	          return dateOut;
	    };
    // =========================================================================
	// /Date Stuff

	// create a blank object to hold our form information
	// $scope will allow this to pass between controller and view
	var initRace = function() {
	    // we will store all of our form data in this object
	    $scope.formData = {};
		$scope.formData.templateSpec = {};
		$scope.formData.reportOptions = {};
		$scope.formData.reportSpec = {};
		$scope.formData.columnOrdering = {};
		$scope.formData.reportSpec.selectOptions = [];
		$scope.formData.reportSpec.conditionGroups = [];
		$scope.formData.createData = {};
		initComparisonOps();	
		$scope.valueAssistCache = {};
	};
	
	var initComparisonOps = function() {
		$scope.comparisonOps = {
				BOOLEAN : ['true', 'false'],
				INTEGER : ['=', '>', '>=', '<', '<='],
				STRING : ['Equals','Contains', 'Starts with', 'Ends with'],
				ID : ['='],
				TIME : ['Equals', 'Before', 'After'],
				DOUBLE : ['=', '>', '<'],
				UNDEFINED : ['=']
		};
	};

	// Get a URL Parameter
	var getParameterByName = function (name) {
	    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
	    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
	}
	
	var onFetchError = function(message) {
		alert("Error fetching Template information:\n" + angular.toJson(message.data.errorMessage));
		$scope.busy=false;
	};

	var onFetchCompleted = function(data) {
//		alert('onFetchCompleted ' + angular.toJson(data));
		$scope.formData.reports = data;
		$scope.busy = false;
	};

	// Get Templates List
	var getTemplates = function() {
			$scope.busy = true;
			var user = getParameterByName('userName');
			var ticket = getParameterByName('loginTicket');
//			alert('User name: ' + user + ' Ticket: ' + ticket);
			getTemplateListService.get(user, ticket).then(
					onFetchCompleted, onFetchError);
	};

    
	// Get Report Options
	$scope.getColumnSpec = function() {
		$scope.formData.reportOptions = {};
		$scope.formData.reportSpec = {};
		$scope.formData.reportSpec.selectOptions = [];
		$scope.formData.reportSpec.conditionGroups = [];
		$scope.valueAssistCache = {};
		getColumnOptionsService.post($scope.formData.templateSpec).then(onFetchOptions, onFetchError);
	};

	var onFetchOptions = function(data) {
//		alert('onFetchOptions' + angular.toJson(data));
		$scope.formData.reportOptions = data;
	};

	// Get Value Assist List
	var getAssistValues = function(type, attribute) {
		getValueAssistanceService.get(type, attribute).then(
				function(data) {
					if ($scope.valueAssistCache[type] === undefined) {
						$scope.valueAssistCache[type] = {};
					}
					$scope.valueAssistCache[type][attribute] = data;
				},
				onFetchError);
	};

	$scope.getCompareOps = function(condition) {
//		alert('getCompareOps(' + angular.toJson(condition) + ')');
		var options;
		if (condition.column.valueAssisted) {
			options = ['In'];
		} else {
			options = $scope.comparisonOps[condition.column.dataType];
		}
		condition.compareOp = options[0];
//		alert('getCompareOps: options' + angular.toJson(options));
		return options;
	}

	$scope.addColumns = function(options) {
		angular.forEach(options, function(item) {
			var idx = $scope.formData.reportSpec.selectOptions.indexOf(item);
			if (idx === -1) {
				$scope.formData.reportSpec.selectOptions.push(item);
			}
		});
	};

	$scope.removeColumns = function(options) {
		for(var i = options.length - 1; i >= 0; i--) {
			var item = options[i];
			var idx = $scope.formData.reportSpec.selectOptions.indexOf(item);
			if (idx != -1) {
				$scope.formData.reportSpec.selectOptions.splice(idx, 1);
			}
		}
		$scope.formData.columnOrdering.selectOptions = [];
	};
	
	$scope.moveUp = function () {
        var prevIdx = -1;
        for(var i = 0; i < $scope.formData.columnOrdering.selectOptions.length; i++) {
            var idx = $scope.formData.reportSpec.selectOptions.indexOf($scope.formData.columnOrdering.selectOptions[i])
            if (idx === 0) {
            	break;
            } else if (idx-1 === prevIdx) {
                prevIdx = idx;
            } else {
                var itemToMove = $scope.formData.reportSpec.selectOptions.splice(idx, 1);
               $scope.formData.reportSpec.selectOptions.splice(idx-1, 0, itemToMove[0]);
            }
        }
    };

	$scope.moveDown = function () {
        var prevIdx = $scope.formData.reportSpec.selectOptions.length;
        for(var i = $scope.formData.columnOrdering.selectOptions.length - 1; i >= 0; i--) {
            var idx = $scope.formData.reportSpec.selectOptions.indexOf($scope.formData.columnOrdering.selectOptions[i])
            if (idx === $scope.formData.reportSpec.selectOptions.length - 1) {
            	break;
            } else if (idx+1 === prevIdx) {
                prevIdx = idx;
            } else {
                var itemToMove = $scope.formData.reportSpec.selectOptions.splice(idx, 1);
                $scope.formData.reportSpec.selectOptions.splice(idx+1, 0, itemToMove[0]);
            }
        }
    };

    $scope.addConditionGroup = function () {
    	var condition = {logicOp : 'AND'};
    	var conditions = [condition];
    	var group = { 'conditions' : conditions};
    	$scope.formData.reportSpec.conditionGroups.push(group);
    };

    $scope.addCondition = function (group) {
//    	alert('addCondition group ' + angular.toJson(group));    	
    	if (group.conditions === undefined) {
    		group.conditions = [];
    	}
//    	alert('addCondition condition' + angular.toJson(condition));
		group.conditions.push({logicOp : 'AND'});
    };
    
    $scope.removeCondition = function(group, condition) {
		var idx = group.conditions.indexOf(condition);
		if (idx != -1) {
			group.conditions.splice(idx, 1);
		}
		if (group.conditions.length === 0 ) {
			idx = $scope.formData.reportSpec.conditionGroups.indexOf(group);
			$scope.formData.reportSpec.conditionGroups.splice(idx, 1);
		}
    };
    
    $scope.getValueInputType = function(condition) {
    	if (condition.column.dataType === 'TIME') {
    		return 'date';
    	}
    	return 'text';
    };
    
    $scope.onSelectColumn = function(condition) {
    	condition.compareOpts = $scope.getCompareOps(condition);
    	condition.rightValue = '';
    	if (condition.column.valueAssisted === true) {
    		$scope.getValueAssist(condition.column);
    	}
    }

    $scope.getValueAssist = function(column) {
//  	alert('column ' + angular.toJson(column));
//  	alert('valueAssisted ' + valueAssisted);
    	var type = column.typeAlias;
		var attribute = column.attribute;
//  		alert('type ' + type + ' attr ' + attribute);        		
		if ($scope.valueAssistCache[type] === undefined || $scope.valueAssistCache[type][attribute] === undefined) {
			getAssistValues(type, attribute);
		}
    };

    // Create the Report
	$scope.createReport = function() {
		if ($scope.busy == true) {
			alert('Sorry, busy');
			return;
		}
		$scope.formData.createData = {};
		var request = createReportReq();
//		alert('Request '+ angular.toJson(request));
		$scope.busy = true;
		createReportService.post(request).then(onCreateReport, onCreateReport);
	};

	// Add the search criteria to the request
	var createReportReq = function() {
		var request = {};
		request.reportName = $scope.formData.reportSpec.reportName;
		request.title = $scope.formData.templateSpec.title;
		request.selectOptions = $scope.formData.reportSpec.selectOptions;
		
		request.conditions = [];
		var conditionGroups = $scope.formData.reportSpec.conditionGroups;
		angular.forEach(conditionGroups, function(group) {
			for(var i = 0; i < group.conditions.length; i++) {
				var item = group.conditions[i];
				var condition = {};
				if (i === 0) {
					condition.groupBegin = true;
				}
				if (i === group.conditions.length - 1) {
					condition.groupEnd = true;
				}
				
				condition.logicOp = item.logicOp;
//				alert('Adding item: ' + angular.toJson(item));
				condition.description = item.column.description;
//				alert('Adding condition: ' + angular.toJson(condition));
				var column = item.column;
				var dataType = column.dataType;
				condition.leftValue = column.typeAlias + '.' + column.attribute;
				if (dataType == 'BOOLEAN') {
					condition.rightValue = item.compareOp;
					condition.compareOp = '=';
				} else if (dataType == 'INTEGER' || dataType == 'DOUBLE') {
					condition.compareOp = item.compareOp;
					condition.rightValue = item.rightValue;
				} else if (dataType == 'ID' || dataType == 'UNDEFINED') {
					condition.compareOp = item.compareOp;
					condition.rightValue = '\'' + item.rightValue + '\'';
				} else if (dataType == 'STRING') {
					if (item.compareOp == 'Equals') {
						condition.compareOp = '=';
						condition.rightValue = '\'' + item.rightValue + '\'';
					} else if (item.compareOp == 'Contains') {
						condition.compareOp = 'LIKE';
						condition.rightValue = '\'%' + item.rightValue + '%\'';
					} else if (item.compareOp == 'Starts with') {
						condition.compareOp = 'LIKE';
						condition.rightValue = '\'' + item.rightValue + '%\'';
					} else if (item.compareOp == 'Ends with') {
						condition.compareOp = 'LIKE';
						condition.rightValue = '\'%' + item.rightValue + '\'';
					} else if (item.compareOp == 'In') {
						condition.compareOp = 'IN';
						var first = true;
						condition.rightValue = '(';
						angular.forEach(item.rightValue, function(value){
							if (! first) {
								condition.rightValue += ',';
							}
							condition.rightValue += '\'';
							condition.rightValue += value;
							condition.rightValue += '\'';
							first = false;
						});
						condition.rightValue += ')';
					}
				} else if (dataType == 'TIME') {
					if (item.compareOp == 'Equals') {
						var condition1 = {};
						condition1.logicOp = item.logicOp;
						condition1.leftValue = '(' + condition.leftValue;
						condition1.compareOp = '>=';
						condition1.rightValue = 'DATE(\'' + $scope.formatDate(item.rightValue) + ' 00:00:00\',\'yyyy-mm-dd hh:mi:ss\')';
						request.conditions.push(condition1);
						condition.logicOp = 'AND';
						condition.compareOp = '<=';
						condition.rightValue = 'DATE(\'' + $scope.formatDate(item.rightValue) + ' 23:59:59\',\'yyyy-mm-dd hh:mi:ss\'))';
					} else if (item.compareOp == 'Before') {
						condition.compareOp = '<';
						condition.rightValue = 'DATE(\'' + $scope.formatDate(item.rightValue) + ' 00:00:00\',\'yyyy-mm-dd hh:mi:ss\')';
					} else if (item.compareOp == 'After') {
						condition.compareOp = '>';
						condition.rightValue = 'DATE(\'' + $scope.formatDate(item.rightValue) + ' 23:59:59\',\'yyyy-mm-dd hh:mi:ss\')';
					}
				}
				request.conditions.push(condition);
			}
		});
//		alert('Request conditions: ' + angular.toJson(request.conditions));
		return request;
	};
	
	var onCreateReport = function(data) {
		$scope.busy=false;
		if (data.success == true) {
			$scope.formData.createData = data;
		} else {
			alert('Error creating report: ' + angular.toJson(data.data.errorMessage));
		}
	};

	// Initialize NOW
	initRace();

	// Fetch the Report Templates
	getTemplates();

});
