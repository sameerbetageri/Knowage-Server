/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular
	.module('qbe.controller', ['configuration','directive','services'])
	.controller('qbeController', 
		["$scope","$rootScope","entity_service","query_service","filters_service","sbiModule_inputParams","sbiModule_config", "sbiModule_restServices", "sbiModule_messaging","$mdDialog", "$mdPanel","$q",qbeFunction]);



function qbeFunction($scope,$rootScope,entity_service,query_service,filters_service,sbiModule_inputParams,sbiModule_config,sbiModule_restServices,sbiModule_messaging, $mdDialog ,$mdPanel,$q){
	
	var entityService = entity_service;
	var inputParamService = sbiModule_inputParams;
	$scope.queryModel = [];
	$scope.pars = [];
	$scope.editQueryObj = new Query("");
	$scope.entityModel;
	$scope.subqueriesModel = {};
/*	$scope.expression = {
	         "type":"NODE_CONST",
	         "value":"$F{Filter1}",
	         "color":"#F44336",
	         "condition": "age < 4",
	         "childNodes":[]
	}*/
	
	$scope.$watch('editQueryObj',function(newValue,oldValue){
		$scope.filters = $scope.editQueryObj.filters;
		if($scope.editQueryObj.filters.length>0){
			$scope.editQueryObj.expression =$scope.expression;
		}
		$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel);
	},true)
	
	$scope.expression = {
			"type": "NODE_CONST",
			"value": "$F{Filter1}",
			"childNodes": []
	};

	entityService.getEntitiyTree(inputParamService.modelName).then(function(response){
		 $scope.entityModel = response.data;
		
	});
	
	$scope.executeQuery = function ( query, bodySend, queryModel) {
		if(query.fields.length>0){
			query_service.executeQuery( query, bodySend, queryModel);
		}else{
			
			queryModel.length = 0;
		}
		
	}
	
	$scope.onDropComplete=function(field,evt){
		$scope.addField(field);
		
    };
	
	$rootScope.$on('applyFunction', function (event, data) {
		var indexOfEntity = findWithAttr($scope.entityModel.entities,'qtip', data.entity);
		var indexOfFieldInEntity = findWithAttr($scope.entityModel.entities[indexOfEntity].children,'id', data.fieldId);
		var indexOfFieldInQuery = findWithAttr($scope.query.fields,'id', data.fieldId);
		if(data.funct!= undefined && data.funct !=null && data.funct!="") {
			$scope.query.fields[indexOfFieldInQuery].funct = data.funct.toUpperCase();
		}
		if(data.filters!= undefined && data.filters != null ) {
			$scope.query.filters = data.filters;
		}
		if(data.pars!= undefined && data.pars != null ) {
			$scope.bodySend.pars = data.pars;
		}
		$scope.query.fields[indexOfFieldInQuery].group = false;
		$scope.executeQuery($scope.entityModel.entities[indexOfEntity].children[indexOfFieldInEntity], $scope.query, $scope.bodySend, $scope.queryModel);
	});
	
	$rootScope.$on('applyFunctionForParams', function (event, data) {
		if(data.pars!= undefined && data.pars!= null ) {
			$scope.pars = data.pars;

		}
	});
	
	
	
	$rootScope.$on('removeColumn', function (event, data) {
	  var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data.id);
	  var indexOfFieldInModel = findWithAttr($scope.queryModel,'id', data.id);
	  if (indexOfFieldInQuery > -1 && indexOfFieldInModel > -1) {
		  $scope.editQueryObj.fields.splice(indexOfFieldInQuery, 1);
		  $scope.queryModel.splice(indexOfFieldInModel, 1);
		}
	});
	
	$rootScope.$on('group', function (event, data) {
	
	  var indexOfEntity = findWithAttr($scope.entityModel.entities,'qtip', data.entity);
	  var indexOfFieldInEntity = findWithAttr($scope.entityModel.entities[indexOfEntity].children,'id', data.fieldId);
	  var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data.fieldId);
	  console.log(data)
	  $scope.editQueryObj.fields[indexOfFieldInQuery].group = data.group;
	  $scope.editQueryObj.fields[indexOfFieldInQuery].funct = "";
	  $scope.executeQuery( $scope.editQueryObj, $scope.bodySend, $scope.queryModel);
	});
	
	var findWithAttr = function(array, attr, value) {
	    for(var i = 0; i < array.length; i += 1) {
	        if(array[i][attr] === value) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	$scope.addField = function (field) {
		
		var newField  = {  
			   "id":field.id,
			   "alias":field.attributes.field,
			   "type":"datamartField",
			   "entity":field.attributes.entity,
			   "field":field.attributes.field,
			   "funct":"",
			   "color":field.color,
			   "group":false,
			   "order":"",
			   "include":true,
			   "visible":true,
			   "longDescription":field.attributes.longDescription
			}
		
		$scope.editQueryObj.fields.push(newField);
	}
	
	$scope.colors = ['#F44336', '#673AB7', '#03A9F4', '#4CAF50', '#FFEB3B', '#3F51B5', '#8BC34A', '#009688', '#F44336'];

    $scope.droppedFunction = function(data) {
        console.log(data)
    };

    $scope.entitiesFunctions = [{
        "label": "add calculated field",
        "icon": "fa fa-calculator",
        "action": function(item, event) {
            $scope.ammacool(item, event);
        }
    }];
    
    
    
    $scope.queryFunctions = [{
        "label": "start subquery",
        "icon": " fa fa-pencil-square-o",
        "action": function(item, event) {
        	$scope.editQueryObj = item;
        }  
    },
    {
        "label": "remove subquery",
        "icon": "fa fa-trash",
        "action": function(item, $event) {
        	var index = $scope.subqueriesModel.subqueries.indexOf(item);
        	  $scope.subqueriesModel.subqueries.splice(index, 1);
        	  $scope.stopEditingSubqueries();
        }  
    }
    
    ];
    
    

    $scope.fieldsFunctions = [{
        "label": "ranges",
        "icon": "fa fa-sliders",
        "action": function(item, event) {
            $scope.ammacool(item, event);
        }
    }, {
        "label": "filters",
        "icon": "fa fa-filter",
        "action": function(item, event) {
        	$scope.openFilters(item,$scope.entityModel,$scope.pars, $scope.editQueryObj.filters,$scope.editQueryObj.subqueries);
        }
    }];

    $scope.ammacool = function (item, event) {
    	console.log(item)
    }

    $scope.query = new Query(1);
    $scope.query.name = "Main query";

    $scope.catalogue = [$scope.query];

    $scope.editQueryObj = $scope.query;
    $scope.subqueriesModel.subqueries = $scope.query.subqueries;
    

    $scope.bodySend = {
    		"catalogue":$scope.catalogue,
    		"qbeJSONQuery":{},
        	"pars":  $scope.pars,
        	"schedulingCronLine":"0 * * * * ?"
    };

    $scope.$on('openFilters',function(event,field){
		$scope.openFilters(field,$scope.entityModel,$scope.pars, $scope.editQueryObj.filters,$scope.editQueryObj.subqueries);
	})
	$scope.$on('distinctSelected',function(){
    	 $scope.editQueryObj.distinct =  !$scope.editQueryObj.distinct;
    })
    $scope.$on('isChecked',function(){
    	 return $scope.editQueryObj.distinct;
    })
	
	$scope.$on('openDialogForParams',function(event){
		$scope.openDialogForParams($scope.pars);
	})
	
	$scope.openDialogForParams = function(pars){
    	var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/parameterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,mdPanelRef){
					$scope.model ={ "pars": pars,"mdPanelRef":mdPanelRef};


				},
				locals: {pars: pars},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
    }
	
	
	$scope.openFilters = function(field, tree, pars, queryFilters, subqueries) {
		if(field.hasOwnProperty('attributes')){
			field_copy = angular.copy(field);
			field={};
			field.field = {}
			field.field.id = field_copy.id;
			field.field.name = field_copy.text;
			field.field.entity = field_copy.attributes.entity;
			field.field.color = field_copy.color;
			field.field.visible= true;
			field.field.group= false;
			field.field.order= 1;
			field.field.filters= [];
		}
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/filterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,field,mdPanelRef){
					$scope.model ={ "field": field, "tree": tree, "pars": pars,"mdPanelRef":mdPanelRef, "queryFilters":queryFilters, "subqueries":subqueries};


				},
				locals: {field: field, tree: tree, pars: pars, queryFilters:queryFilters, subqueries: subqueries},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	};

    $scope.openMenu = function($mdMenu, ev) {
        originatorEv = ev;
        $mdMenu.open(ev);
    };
    
    $scope.createQueryName = function(){
    	var lastcount = 0;
    	var lastIndex = $scope.subqueriesModel.subqueries.length-1;
    	if(lastIndex!=-1){
    		var lastQueryId = $scope.subqueriesModel.subqueries[lastIndex].id;
    		lastcount = parseInt(lastQueryId.substr(1));
    	}else{
    		lastcount = 1;
    	}

    	return lastcount +1;
    }   
    $scope.createSubquery = function(){
    	var subquery = new Query($scope.createQueryName());
    	$scope.query.subqueries.push(subquery);
    	$scope.editQueryObj = subquery;
    }
    $scope.stopEditingSubqueries = function(){
    	$scope.editQueryObj = $scope.query;
    }
}