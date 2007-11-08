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

// The contextMap provides a container for information about the current
// computing context.  It maps actions that are taken by a user to hints
// for variable names and the location of the navigation pulldown menu.
// contextMap[key][2] provides the order in the navigation pulldown for
// PURLs and contextMap[key][3] provides the order for other nouns (i.e.
// users/groups/domains).
var contextMap = new Object();
contextMap["Create"] = ["create", "c_", 1, 1];
contextMap["AdvancedCreate"] = ["advancedcreate", "a_", 2, 0];
contextMap["Modify"] = ["modify", "m_", 3, 2];
contextMap["Search"] = ["search", "s_", 4, 3];
contextMap["Validate"] = ["validate", "v_", 5, 0];
contextMap["Delete"] = ["delete", "d_", 6, 4];

var resultBlock = $("results");
var htmlResults = "";

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
			showAction(key);
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
		// it is not always set by the user (as in a Modify hyperlink from a Search).
		// Because the menus are of different lengths on different pages,
		// we need to refer to the var 'context' that is set on each page.
		if ( context == "purl" ) {
			userchoice.options[contextMap[directive][2]].selected = true;
		} else {
			userchoice.options[contextMap[directive][3]].selected = true;
		}
	}
	clearResults();
	
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
	// Un-Pseudo-Webify the input.
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
			records[key] = records[key].replace(/%LINEBREAK%/g, "\n");
			currentElement = document.getElementById("m_" + key);
			if ( currentElement != null && currentElement.type == "checkbox" ) {
				// TODONEXT: Is this sufficient?  Check to ensure that this works.
				currentElement.checked = records[key];
			} else if (currentElement != null ) {
				currentElement.value = records[key];
			}
		}
	}
	showAction('Modify');
	return false;
}

// Open results in a separate window.
function showResultsWindow() {
	
	// TODO: Modify the HTML shown in this window so that clicking the "modify"
	// link will open the correct function in the main window (the 'opener')
	// instead of this newly-created window.
	localHtmlResults = htmlResults.replace(/return loadModify/g, "return opener.loadModify");
	
	resultsWindow = window.open('','resultsWindow','status=yes,toolbar=yes,scrollbars=yes,resizable=yes');
	var resultsDoc = resultsWindow.document;
	htmlDoc = "<html>";
	htmlDoc += "<head><link type='text/css' rel='stylesheet' href='style.css' /></head>";
	htmlDoc += "<body><h1>Results</h1>";
	htmlDoc += "<div class='withresults'>";
	htmlDoc += localHtmlResults;
	htmlDoc += "</div></body></html>";
	resultsDoc.write(htmlDoc);
	resultsDoc.close();
	if (window.focus) {
		resultsWindow.focus();
	}
	return false;
}

// Create an HTML list from an array of XML elements and their values.
function getHTMLFromXMLArray(xmlArray) {
		
	var htmlList = "<dl>";
	keyCount = 0;
	for ( outerKey in xmlArray ) {
		keyCount++;
	}
	if ( keyCount == 0 ) {
		htmlList += "<dd>No results found<\/dd>";
	} else {
		// Found results, so create HTML for each record.
		for ( outerKey in xmlArray ) {
			// Create a serialization of the data to allow easy passing to the Modify feature.
			// The dataString passed needs to be different for each record.
			dataString = "";
			for ( innerKey in xmlArray[outerKey] ) {
				escapedValue = xmlArray[outerKey][innerKey][1].replace(/\n/g, "%LINEBREAK%");
				dataString += xmlArray[outerKey][innerKey][0] + "=" + escapedValue + "&";
			}
			// Write an HTML list item for each element in a record.
			for ( innerKey in xmlArray[outerKey] ) {
				if ( xmlArray[outerKey][innerKey][0] == "id" || xmlArray[outerKey][innerKey][0] == "pid" ) {
					htmlList += "<dd><br>" + xmlArray[outerKey][innerKey][0] + ": " + xmlArray[outerKey][innerKey][1] +
							 	" <a href='#modify' class='tooltip' onClick='return loadModify(\"" + 
								dataString +
								"\")'><img src='http://purlz.org/images/edit.png' alt='Modify record'>" +
								"<span>Modify record</span></a>" + 
								"<\/dd>";
				} else {
					htmlList += "<dd>" + xmlArray[outerKey][innerKey][0] + ": " + xmlArray[outerKey][innerKey][1] + "<\/dd>";
				}
			}
		}
	}
	htmlList += "<\/dl></div>";
	return htmlList;
}

// Scrub whitespace from textarea inputs and replace linebreaks with commas.
function scrubTextareaInput (input) {
	output = input.replace(/^\s*|\s*$/g,'');
	output = output.replace(/\n/g, ',');
	return output;
}

// Callback for Create/Modify/Search/Delete (POST/PUT/GET/DELETE) actions.
function onResponse(message, headers, callingContext) {
	// If bad parameters were passed, highlight them via CSS.
	if ( headers["Status"] == "400" && headers["X-bad-params"] ) {
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
	
	// Show the result header and allow results to be duplicated in a new window.
	resultBlock.innerHTML = "<h3 class='" + resultClass + "'>" +
							"<a href='#' class='tooltip' onClick='return showResultsWindow()'>" +
							"<img src='http://purlz.org/images/tearoff_icon.png' alt='Open results in a new window' />" +
							"<span>Open results in a new window</span></a>" +
							" " + resultHeader + "<\/h3>";

	// Style the results based on their Content-Type.
	if ( headers["Content-Type"] == "text/plain" || headers["Content-Type"] == "text/html") {
		resultBlock.innerHTML += "<p class='" + resultClass + "'>" + message + "<\/p>";
		
	} else if ( headers["Content-Type"] == "text/xml" ||
				headers["Content-Type"] == "application/xml" ) {
		
		// Parse the XML
		startParser(message);		
		htmlResults = getHTMLFromXMLArray(resultsMap);  // resultsMap is in SaxEventHandler.js.
				
		// Write the results to the results area.
		resultBlock.innerHTML += htmlResults;
		
	} else {
		resultBlock.innerHTML += "<p class='error'>Warning: Content-Type of results not supported.  Trying anyway:<\/p>";
		resultBlock.innerHTML += "<p class='" + resultClass + "'>" + message + "<\/p>";
	}
	
	// Provide debugging information, if requested.
	printHTTPDetails(message, headers, callingContext);
}

