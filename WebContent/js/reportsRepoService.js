'use strict';

var appModule = angular.module("reportsApp");

appModule.factory("getTemplateListService", function($http) {

	var getReports = function (userName, ticket) {
		return $http.get('spring/race/templatelist' + '?userName=' + userName + '&ticket=' + ticket).then(
				function(response) {
					return response.data;
				});
	};

	return {
		get : getReports
	};

});

appModule.factory("getColumnOptionsService", function($http) {

	var getOptions = function(reqData) {
//		alert('getColumnOptionsService : ' + angular.toJson(reqData));
		var request = {
				method: 'POST',
				url : 'spring/race/getreportoptions',
				headers : {
					'Content-Type' : 'application/json'
				},
				data : reqData,
				};
		return $http(request).then(function(response) {
			return response.data;
		});

	};

	return {
		post : getOptions
	};

});

appModule.factory("getValueAssistanceService", function($http) {

	var getValues = function(typeAlias, attribute) {
		return $http.get('spring/race/getvalueassistvalues?typeAlias=' + typeAlias + '&attribute=' + attribute).then(
				function(response) {
					return response.data;
				});
	};

	return {
		get : getValues
	};

});

appModule.factory("createReportService", function($http) {

	var createReport = function(reqData) {
		var request = {
				method: 'POST',
				url : 'spring/race/createreport',
				headers : {
					'Content-Type' : 'application/json'
				},
				data : reqData,
				};
		return $http(request).then(function(response) {
			return response.data;
		});

	};

	return {
		post : createReport
	};

});
