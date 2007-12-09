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

function load(referrer) {
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
	// Show the appropriate action, if we are on a page that has one.
	if ( typeof(contextMap) != "undefined") {
		for ( key in contextMap ) {
			if (contextMap[key][0] == urlFragment) {
				showAction(key);
			}
		}
	}
	
	// Show the login status in a div on the page.
	showLoginStatus(referrer);
	
}


function showLoginStatus(referrer) {
	resultBlock = $("loginstatus");
	resultBlock.innerHTML = "<p>Getting login status...<\/p>";
	// The referrer (the page we are on when we submit) is the callingContext
	ajaxCaller.get("/admin/loginstatus", null, onLoginStatusResponse, false, referrer);
}


function onLoginStatusResponse (message, headers, callingContext) {
	// Fill the appropriate 'loginstatus' div with a login status indication.
	resultBlock = $("loginstatus");
	resultBlock.innerHTML = "<p>Getting login status...<\/p>";
	
	if ( headers["Content-Type"] == "text/xml" ||
				headers["Content-Type"] == "application/xml" ) {

		// "Parse" the XML
		if ( message.indexOf("logged out") > -1 ) {
			// The user is logged out or does not have an account.
			resultBlock.innerHTML = "<form action=\"/admin/login/login.bsh?referrer=" + callingContext + "\" method=\"POST\" name=\"loginForm\" id=\"loginForm\">Anonymous (<a href=\"#\" onClick=\"login()\">log in</a>)</form>";
			// The use is logged out.  Disable Submit buttons requiring authentication.
			for ( var i=0 ; i<numNeedsAuth ; i++ ) {
				document.getElementById("needAuth_" + i).disabled = true;
				// Add warning messages about not being logged in.
				document.getElementById("needAuthDiv_" + i).innerHTML = "(You must be logged in to perform this action)";
			}
		} else if ( message.indexOf("logged in") > -1 ) {
			// The user is logged in.
			var uid = message.replace(/.*<uid>(.*)<\/uid>.*/, "$1");
			resultBlock.innerHTML = "<form action=\"/admin/logout?referrer=" + referrer + "\" method=\"POST\" name=\"logoutForm\" id=\"logoutForm\">Logged in as <b>" + uid + "</b> (<a href=\"#\" onClick=\"logout()\">log out</a>)</form>";
			// The user is logged in.  Enable all Submit buttons.
			for ( var i=0 ; i<numNeedsAuth ; i++ ) {
				document.getElementById("needAuth_" + i).disabled = false;
				// Remove all warning messages about not being logged in.
				document.getElementById("needAuthDiv_" + i).innerHTML = "";
			}
		} else {
			// Something is strange about the message.
			resultBlock.innerHTML = "<p class=\"error\">Error: Server response unreadable.</p>";
		}
	} else {
		resultBlock.innerHTML += "<p class='error'>ERROR: Content-Type of results not supported.  Expected an XML message from the PURL server.<\/p>";
	}
}


function login() {
	document.loginForm.submit();
}


function logout() {
	document.logoutForm.submit();
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

// TODO: Refactor signalLoadingResults() and signalLoadingPendingList() to be one function.
function signalLoadingResults() {
	resultBlock = $("results");
	resultBlock.innerHTML = "<p>Loading ...<\/p>";
}

function signalLoadingPendingList() {
	resultBlock = $("pending");
	resultBlock.innerHTML = "<p>Loading ...<\/p>";
}

function signalLoadingPendingResults(id) {
	resultBlock = $( id + "_results" );
	resultBlock.innerHTML = "<p>Accessing record ...<\/p>";
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

// TODO: Refactor startParser() and startParserForPending() into one function.
// Parse XML results for User/Group/Domain/PURL actions.
function startParser(xml) {
	var parser = new SAXDriver();
	var eventHandler = new SAXEventHandler();
	parser.setDocumentHandler(eventHandler);
	parser.setLexicalHandler(eventHandler);
	parser.setErrorHandler(eventHandler);
	parser.parse(xml);
}

// Parse XML results for pending User/Domain requests.
function startParserForPending(xml) {
	var parser = new SAXDriver();
	var eventHandler = new SAXEventHandlerForPending();
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
	
// Pre-populate a "modify" form with data from a Search.
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
	
	// Clear all existing text entry fields in the form.
	// Iterate through all names in var labelElements looking for names starting with 'm_'
	// then set those field values to empty.
	for ( field in labelElements ) {
		fieldName = labelElements[field];
		// If this is a field on the modify form...
		if ( fieldName.indexOf("m_") == 0 ) {
			fieldName = fieldName.substring( 0, fieldName.indexOf("_label") );
			// clear its value.
			currentElement = document.getElementById(fieldName);
			if ( currentElement != null ) {
				if ( currentElement.type == "checkbox" ) {
				// TODONEXT: Is this sufficient?  Check to ensure that this works.
				currentElement.checked = false;
				} else {
				currentElement.value = null;
				}
			}
		}
	}
	
	// Fill in the fields with appropriate data.
	for ( key in records ) {
		if (key.length > 0) {
			records[key] = records[key].replace(/%LINEBREAK%/g, "\n");
			currentElement = document.getElementById("m_" + key);
			if ( currentElement != null ) {
				if ( currentElement.type == "checkbox" ) {
				// TODONEXT: Is this sufficient?  Check to ensure that this works.
				currentElement.checked = records[key];
				} else {
				currentElement.value = records[key];
				}
			}
		}
	}
	showAction('Modify');
	return false;
}

// Open results in a separate window.
function showResultsWindow() {
	
	// This will open a "modify" form in the opening window, not the results window.
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
// Used for User/Group/Domain/PURL actions.
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
				if ( xmlArray[outerKey][innerKey][0] == "id" ) {
					htmlList += "<dd><br>" + xmlArray[outerKey][innerKey][0] + ": " + xmlArray[outerKey][innerKey][1] +
							 	" <a href='#modify' class='tooltip' onClick='return loadModify(\"" + 
								dataString +
								"\")'><img src='http://purlz.org/images/edit.png' alt='Modify record'>" +
								"<span>Modify record</span></a>" + 
								"<\/dd>";
				} else {
					fieldValue = xmlArray[outerKey][innerKey][1].replace(/%LINEBREAK%/g, ", ");
					htmlList += "<dd>" + xmlArray[outerKey][innerKey][0] + ": " + fieldValue + "<\/dd>";
				}
			}
		}
	}
	htmlList += "<\/dl></div>";
	return htmlList;
}


// Create a series of HTML forms from an array of XML elements and their values.
// Used for User/Domain approval screens.
function formatPendingResults(xmlArray) {
	
	// Find the ids of all the pending User/Domain requests.
	var html = ""; // The HTML content created.
	var id = new Object();  // An array of ids of the pending Users/Domains.
	var recordCount = 0;  // The number of records to process.
	for ( outerKey in xmlArray ) {
		recordCount++;
		for ( innerKey in xmlArray[outerKey] ) {
			if ( xmlArray[outerKey][innerKey][0] == "id" ) {
				id[outerKey] = xmlArray[outerKey][innerKey][1];
			}
		}
	}
	if ( recordCount == 0 ) {
		html = "<p>No pending records found.<\/p>";
	}

	// Found results, so create HTML for each record.
	for ( outerKey in xmlArray ) {
		
		html += "<form name=\"" + id[outerKey] + "_form\" id=\"" + id[outerKey] + "_form\">";
		html += "<table border=\"0\"><tr>";
	
		// Create two submit buttons; one to approve and one to deny.
		// Give an id to the <div>s so we can overwrite them later.
		// The radio buttons are the only place where we use names instead of ids.
		html += "<td>";
		html += "<div id=\"" + id[outerKey] + "_submit\">";
		html += "<input name=\"" + id[outerKey] + "_decision\" type=\"radio\" value=\"approve\" checked> Approve";
		html += "<input name=\"" + id[outerKey] + "_decision\" type=\"radio\" value=\"deny\"> Deny &nbsp;";
		html += "<input type=\"button\" value=\"Submit\" onClick=\"return resolvePendingSubmit('" + id[outerKey] + "')\" />";
		html += "</div>";
		html += "<div id=\"" + id[outerKey] + "_results\"></div>";
		html += "</td>";
	
		// Create an HTML list containing the record.
		html += "<td>";
	
		// Write an HTML list item for each element in a record.
		for ( innerKey in xmlArray[outerKey] ) {
			fieldName = xmlArray[outerKey][innerKey][0];
			fieldValue = xmlArray[outerKey][innerKey][1];
			if ( fieldName == "email" ) {
				fieldValue = "<a href=\"mailto:" + fieldValue + "\">" + fieldValue + "</a>";
			}				
			html += "<dd>" + fieldName + ": " + fieldValue + "<\/dd>";
		}
		html += "<\/dl></td>";
	
		html += "</tr></table></form><hr>";
	}
	
	return html;
}


// Scrub whitespace from textarea inputs and replace linebreaks with commas.
function scrubTextareaInput (input) {
	output = input.replace(/\n/g, ',');
	output = output.replace(/^\s*(.*)\s*$/,"$1");
	return output;
}


// Callback for User/Domain pending list GET actions.
function onPendingResponse(message, headers, callingContext) {
	// Fill the 'pending' div with a series of HTML forms; one per User/Domain
	// pending approval.
	var pendingBlock = $("pending");
	
	if ( headers["Content-Type"] == "text/xml" ||
				headers["Content-Type"] == "application/xml" ) {
		
		// Parse the XML
		startParserForPending(message);
		// Format the results into an HTML form.
		htmlResults = formatPendingResults(resultsMap);  // resultsMap is in SaxEventHandlerForPending.js.
				
		// Write the results to the results area.
		pendingBlock.innerHTML = htmlResults;
		
	} else {
		pendingBlock.innerHTML += "<p class='error'>ERROR: Content-Type of results not supported.  Expected an XML message from the PURL server.<\/p>";
	}
	
}

// Callback for User/Domain pending list POST actions.
function onPendingResultsResponse(message, headers, callingContext) {
	// Fill the appropriate User/Domain 'pending' div with a status indication.
	var submitBlockId = callingContext + "_submit";
	var submitBlock = $(submitBlockId);
	var resultsBlock = $(callingContext + "_results");
	
	if ( headers["Content-Type"] == "text/xml" ||
				headers["Content-Type"] == "application/xml" ) {

		// "Parse" the XML
		if ( message.indexOf("rejected") > -1 ) {
			// The User/Domain has been successfully rejected (denied).
			setVisibility(submitBlockId, 'none');
			resultsBlock.innerHTML = "<p class=\"rejected\">Denied</p>";
		} else if ( message.indexOf("approved") > -1 ) {
			// The User/Domain has been successfully approved.
			setVisibility(submitBlockId, 'none');
			resultsBlock.innerHTML = "<p class=\"approved\">Approved</p>";
		} else {
			// Something is strange about the message.
			resultsBlock.innerHTML = "<p class=\"error\">Error: Server response unreadable.</p>";
		}
	} else {	
		setVisibility(submitBlockId, 'none');
		resultsBlock.innerHTML += "<p class='error'>ERROR: Content-Type of results not supported.  Expected an XML message from the PURL server.<\/p>";
	}
	
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
		
	} else if ( headers["Content-Type"] == "text/html" ) {
		
		// Display the HTML directly.
		resultBlock.innerHTML = message;
		
	} else {
		resultBlock.innerHTML += "<p class='error'>Warning: Content-Type of results not supported.  Trying anyway:<\/p>";
		resultBlock.innerHTML += "<p class='" + resultClass + "'>" + message + "<\/p>";
	}
	
	// Provide debugging information, if requested.
	printHTTPDetails(message, headers, callingContext);
}

// Get the selected value of a radio button group with a given *name*.
// Some browsers cannot get the length of a radio button group when accessed by id :P
function getSelectedRadio(name) {
	
	radio = document.getElementsByName(name);
	radioNum = radio.length;
	
	value = "";
	for ( i=0; i<radioNum ; i++ ) {
		if (radio[i].checked) {
			value = radio[i].value;
		}
	}
	return value;
}

