// =========================================================================
//
// purlUtil.js - Provide functions for the administration of PURLs.
//
// version 1.0, 16 August 2007
// David Wood (david at http://zepheira.com/)
//
// =========================================================================
//
// Copyright (C) 2007 OCLC (http://oclc.org)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// =========================================================================
//
// MAINTENANCE HINTS:
// HTML forms are submitted via the functions createSubmit(), modifySubmit(),
// searchSubmit() and deleteSubmit() via AJAX (all these functions are defined
// in the relevant HTML page).  The callback function onResponse(), below,
// handles results from the server.  The remainder are utility functions.
//

var HttpResponseCodes = new Object();
HttpResponseCodes["200"] = "OK";
HttpResponseCodes["201"] = "Created";
HttpResponseCodes["400"] = "Bad Request";
HttpResponseCodes["405"] = "Method Not Allowed";
HttpResponseCodes["409"] = "Conflict";
HttpResponseCodes["412"] = "Precondition Failed";

var contextMap = new Object();
contextMap["Create"] = ["create", "c_", 1];
contextMap["Modify"] = ["modify", "m_", 2];
contextMap["Search"] = ["search", "s_", 3];
contextMap["Delete"] = ["delete", "d_", 4];
contextMap["AdvancedCreate"] = ["advancedcreate", "a_", 5];

var resultBlock = $("results");

function load() {
	// Get the current URL and look for fragments.
	// If found, attempt to show desired form.
	var location = document.URL;
	var urlFragment = "";
	var urlFragmentIndex = location.indexOf("#");
	var queryStringIndex = location.indexOf("?");
	if ( urlFragmentIndex > -1 && queryStringIndex == -1 ) {
		// Our URL has a fragment identifier, but no query string.
		urlFragment = location.substring(urlFragmentIndex + 1, location.length);
	} else if ( urlFragmentIndex > -1 && queryStringIndex > -1 ) {
		// Our URL has a fragment identifier and a query string.
		urlFragment = location.substring(urlFragmentIndex + 1, queryStringIndex);
	}
	for ( key in contextMap ) {
		if (contextMap[key][0] == urlFragment) {
			setVisibility(urlFragment, 'inline');
		}
	}
}

function showAction(directive)
{
	userchoice = document.naviform.naviselect;
	if (directive == null) {
		destination = userchoice.options[userchoice.selectedIndex].value;
	} else {
		destination = contextMap[directive][0];
		// Explicitly set the location of the menu pulldown since
		// it is not always set by the user (as in a Modify from a Search).
		userchoice.options[contextMap[directive][2]].selected = true;
	}
	// TODO: Leave results in place?
	//clearResults();
	
	// Hide action divs except the one we want.
	for ( key in contextMap ) {
		if(document.getElementById(contextMap[key][0])) {
			setVisibility(contextMap[key][0], 'none');
		}
	}
	if(document.getElementById(destination)) {
		setVisibility(destination, 'inline');
	}
}

function setVisibility(id, visibility) {
	document.getElementById(id).style.display = visibility;
}

function setClass(id, newClass) {
	document.getElementById(id).className = newClass;
}

function clearErrorIndications() {
	for ( i=0 ; i < labelElements.length ; i++ ) {
		document.getElementById(labelElements[i]).className = "";
	}
}

function signalLoadingResults() {
	resultBlock = $("results");
	resultBlock.innerHTML = "<p>Loading ...<\/p>";
}

function clearResults() {
	setClass("results", "noresults");
	resultBlock = $("results");
	resultBlock.innerHTML = "<p>&nbsp;<\/p>";
}

function printHTTPDetails(message, headers, callingContext) {
	if (debugThisPage) {
		resultBlock.innerHTML +=
			"<h3>Debug mode is On<\/h3>"
		resultBlock.innerHTML +=
			"<h3>Response Headers:<\/h3>"
			+ "<pre class='response'>" + getHeaderHTML(headers) + "<\/pre>";
		resultBlock.innerHTML +=
				"<h3>Calling Context:<\/h3>"
			+ "<div class='response'>" + callingContext + "<\/div>";
		resultBlock.innerHTML +=
				"<h3>Raw Message:<\/h3>"
			+ "<div class='response'>" + message + "<\/div>";
	}
}

// In support of function printHTTPDetails(), above.
function getHeaderHTML(headers) {
	if (ajaxCaller.shouldMakeHeaderMap) {
		message = "";
		for (key in headers) {
			message += "[" + key + "] -&gt; [" + headers[key] + "]<br/>";
		}
		return message;
	} else {
		return headers;
	}
}

// Parse XML results, if required.
function startParser(xml) {
	var parser = new SAXDriver();
	var eventHandler = new SAXEventHandler();
	parser.setDocumentHandler(eventHandler);
	parser.setLexicalHandler(eventHandler);
	parser.setErrorHandler(eventHandler);
	parser.parse(xml);
}	

// Confirm a Delete command before proceeding with function deleteSubmit().
function checkDeleteSubmit(typeOfObject, deletionObject) {
	var thingToBeDeleted = "";
	if ( deletionObject != null ) {
		thingToBeDeleted = " '" + document.getElementById(deletionObject).value + "'";
	}
	var answer = confirm("Are you certain that you want to delete the " + typeOfObject + thingToBeDeleted + "?");
	if (answer){
		deleteSubmit();
	}
	return false;
}	
	
// Pre-populate a Modify form with data from a Search.
function loadModify(recordData) {
	// Un-Webify the input.  TODO: This is rather hackish and should be refactored.
	var entries = new Array();
	var elements = new Array();
	var records = new Array();
	entries = recordData.split('&');
	for (entry in entries) {
		elements = entries[entry].split('=');
		records[elements[0]] = elements[1];
	}
	for ( key in records ) {
		if (key.length > 0) {
			// TODO: Test for existence of the element first!
			records[key] = records[key].replace(/%LINEBREAK%/g, "\n");
			document.getElementById("m_" + key).value = records[key];
			//document.getElementById("m_comments").value += key + " : " + records[key] + "\n";
		}
	}
	showAction('Modify');
	return false;
}

// Callback for Create/Modify/Search/Delete (POST/PUT/GET/DELETE) actions.
function onResponse(message, headers, callingContext) {
	// If bad parameters were passed, highlight them via CSS.
	if ( headers["Status"] == "400") {
		var badParams = headers["X-bad-params"].split(",");
		for ( i=0 ; i < badParams.length ; i++ ) {
			setClass(contextMap[callingContext][1] + badParams[i] + "_label", "error");
		}
	}
	
	// Highlight the results if an error occurs.
	setClass("results", "withresults");
	resultHeader = callingContext + " Successful";
	resultClass = "response";
	explanation = "";
	if ( headers["Status"] != "200" && headers["Status"] != "201" ) {
		if ( headers["Status"] ) {
			explanation = HttpResponseCodes[headers["Status"]];
		}
		resultHeader = callingContext + " Failed: " + explanation + " (" + headers["Status"] + ")";
		resultClass = "error";
	}
	resultBlock.innerHTML = "<h3 class='" + resultClass + "'>" + resultHeader + "<\/h3>";

	// Style the results based on their Content-Type.
	if ( headers["Content-Type"] == "text/plain" || headers["Content-Type"] == "text/html") {
		resultBlock.innerHTML += "<p class='" + resultClass + "'>" + message + "<\/p>";
		
	} else if ( headers["Content-Type"] == "text/xml" ||
				headers["Content-Type"] == "application/xml" ) {
		
		// Parse the XML
		startParser(message);
		
		// Create a serialization of the data to allow easy passing to the Modify feature.
		dataString = "";
		keyCount = 0;
		for ( key in elementMap ) {
			escapedValue = elementMap[key].replace(/\n/g, "%LINEBREAK%");
			dataString += key + "=" + escapedValue + "&";
			keyCount++;
		}
		
		resultBlock.innerHTML += "<dl>";
		if ( keyCount == 0 ) {
			resultBlock.innerHTML += "<dd>No results found<\/dd>";
		} else {
			for ( key in elementMap ) {
				// Write pretty-printed result to the results area.
				if ( key == "id" || key == "pid" ) {
					// TODONEXT: This will NOT handle multiple results yet.
					resultBlock.innerHTML += "<dd>" + key + ": " + elementMap[key] + " <a href='#modify' onClick='return loadModify(\"" + dataString + "\")'><img src='http://purlz.org/images/edit.png' alt='Modify record'></a> " + "<\/dd>";
				} else {
					resultBlock.innerHTML += "<dd>" + key + ": " + elementMap[key] + "<\/dd>";
				}
			}
		}
		resultBlock.innerHTML += "<\/dl>";
		
	} else {
		resultBlock.innerHTML += "<p class='error'>Warning: Content-Type of results not supported.  Trying anyway:<\/p>";
		resultBlock.innerHTML += "<p class='" + resultClass + "'>" + message + "<\/p>";
	}
	
	// Provide debugging information, if requested.
	printHTTPDetails(message, headers, callingContext);
}

