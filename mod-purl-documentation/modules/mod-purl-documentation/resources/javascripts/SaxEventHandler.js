// =========================================================================
//
// SaxEventHandler.js - an event handler for XML for <SCRIPT>'s SAX Parser
//                      for use on the PURL project.
// This event handler is used for User/Group/Domain/PURL actions.
//
// Based upon:
// preMadeSaxEventHandler.js - a pre-built event handler for XML for <SCRIPT>'s SAX Parser
// version 3.1
//
// =========================================================================
//
// Modifications Copyright (C) 2007 OCLC (http://oclc.org/) and denoted by
// "DHW".  David Wood (david at http://zepheira.com/)
//
// Original license:
// Copyright (C) 2001 - 2002 David Joham (djoham@yahoo.com)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.

// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// visit the XML for <SCRIPT> home page at xmljs.sourceforge.net
//

// TODO: Trim this file down by removing null-implementations and functions not used.

// DHW
var elementMap;
var elementMapIndex;
var resultsMap;
var resultsMapIndex;
var currentElement = '';
var previousElement = '';
var maintainers = "";
var writers = "";
var members = "";

/*****************************************************************************
                    SAXEventHandler Object
*****************************************************************************/

SAXEventHandler = function() {
    /*****************************************************************************
    function:  SAXEventHandler

    author: djoham@yahoo.com

    description:
        this is the constructor for the object which will be the sink for
        the SAX events
    *****************************************************************************/

    this.characterData = "";   // there is no guarantee that the text event will only fire once
                                                 // for element texts. This variable keeps track of the text that has
                                                 // been returned in the text events. It is reset  when a non-text
                                                 //event is fired and should be read at that time
}  // end function SAXEventHandler



/*****************************************************************************
                    SAXEventHandler Object SAX INTERFACES
*****************************************************************************/


SAXEventHandler.prototype.characters = function(data, start, length) {
    /*****************************************************************************
    function:  characters

    author: djoham@yahoo.com

    description:
        Fires when character data is found

        ****** NOTE******
        there is no guarantee that this event will only fire once for
        text data. Particularly, in the cases of escaped characters,
        this event can be called multiple times. It is best to keep a
        variable around to collect all of the data returned in this event.
        You'll know all of the text is returned when you get a non-characters
        event.

        The variable that this object keeps for just this purpose is this.characterData

        To ensure that text values are handled properly, each event calls the function
        this._handleCharacterData. This event resets the characterData
        variable for non-characters events. It also calls the function
        this._fullCharacterDataReceived.

        since this event can be called many times, you should put your text handling
        code in the function this._fullCharacterDataReceived if you need to act only
        when you know you have all of the character data for the element.

        data is your full XML string
        start is the beginning of the XML character data being reported to you
        end is the end of the XML character data being reported to you

        the data can be retrieved using the following code:
        var data = data.substr(start, length);

        Generally, you won't have any code here. Place your code in
        this._fullCharacterDataReceived instead

    *****************************************************************************/

    this.characterData += data.substr(start, length);

}  // end function characters


SAXEventHandler.prototype.endDocument = function() {
    /*****************************************************************************
    function:  endDocument

    author: djoham@yahoo.com

    description:
        Fires at the end of the document
    *****************************************************************************/

    this._handleCharacterData();

    //place endDocument event handling code below this line
	
}  // end function endDocument


SAXEventHandler.prototype.endElement = function(name) {
    /*****************************************************************************
    function:  endElement

    author: djoham@yahoo.com

    description:
        Fires at the end of an element

        name == the element name that is ending
    *****************************************************************************/

    this._handleCharacterData();

    //place endElement event handling code below this line
	// DHW
	if ( name == 'group' || name == 'domain' || name == 'purl' || name == 'history') {
		// Add entries for maintainers, writers and members, if any were found.
		if ( maintainers.length > 0 ) {
			maintainers = maintainers.substring(1, maintainers.length); // remove leading comma
			elementMap[elementMapIndex] = ["maintainers", maintainers];
			elementMapIndex++;
			maintainers = "";
		}
		if ( writers.length > 0 ) {
			writers = writers.substring(1, writers.length); // remove leading comma
			elementMap[elementMapIndex] = ["writers", writers];
			elementMapIndex++;
			writers = "";
		}
		if ( members.length > 0 ) {
			members = members.substring(1, members.length); // remove leading comma
			elementMap[elementMapIndex] = ["members", members];
			elementMapIndex++;
			members = "";
		}
	}
	
	if ( name == 'user' || name == 'group' || name == 'domain' || name == 'purl' || name == 'history' ) {
		// Write the working array into the results.
		resultsMap[resultsMapIndex] = elementMap;
		resultsMapIndex++;
	}


}  // end function endElement


SAXEventHandler.prototype.processingInstruction = function(target, data) {
    /*****************************************************************************
    function:  processingInstruction

    author: djoham@yahoo.com

    description:
        Fires when a processing Instruction is found

        In the following processing instruction:
        <?xml version=\"1.0\"?>

        target == xml
        data == version"1.0"
    *****************************************************************************/
    this._handleCharacterData();

    //place processingInstruction event handling code below this line


}  // end function processingInstruction


SAXEventHandler.prototype.setDocumentLocator = function(locator) {
    /*****************************************************************************
    function:  setDocumentLocator

    author: djoham@yahoo.com

    description:
        This is the first event ever called by the parser.

        locator is a reference to the actual parser object that is parsing
        the XML text. Normally, you won't need to trap for this error
        or do anything with the locator object, but if you do need to,
        this is how you get a reference to the object
    *****************************************************************************/

    this._handleCharacterData();

    //place setDocumentLocator event handling code below this line


}  // end function setDocumentLocator


SAXEventHandler.prototype.startElement = function(name, atts) {
    /*****************************************************************************
    function:  startElement

    author: djoham@yahoo.com

    description:
        Fires at the start of an element

        name == the name of the element that is starting
        atts == an array of element attributes

        The attribute information can be retrieved by calling
        atts.getName([ordinal])  -- zero based
        atts.getValue([ordinal]) -- zero based
        atts.getLength()
        atts.getValueByName([attributeName])

    *****************************************************************************/

    this._handleCharacterData();

    //place startElement event handling code below this line
	// DHW
	// Update the previous element indicator as needed.  Avoid updating in the case
	// where we are processing several elements of the same name (e.g. <uid>) so
	// we maintain a reference to the parent element type (e.g. <maintainers>).
	if ( currentElement != "uid" ) {
		previousElement = currentElement;
	}
	currentElement = name;
	
	if ( name == 'group' || name == 'domain' || name == 'purl' || name == 'user' ) {
		// Clear the working array.
		elementMap = new Array();
		elementMapIndex = 0;
		maintainers = "";
		writers = "";
		members = "";
		// Extract the 'status' attribute, if present.
		// TODO DBG
		//alert("In element user.  atts has length " + atts.getLength() );
		if ( atts.getLength() > 0 ) {
			for ( i=0; i<atts.getLength() ; i++ ) {
				//alert("Operating on attribute " + atts.getName([i]) + " = " + atts.getValue([i]));
				if ( "status" == atts.getName([i]) ) {
					value = "Pending approval";
					if ( "1" == atts.getValue([i]) ) {
						value = "Approved";
					} else if ( "2" == atts.getValue([i]) ) {
							value = "Tombstoned";
					}
					elementMap[elementMapIndex] = ["status", value];
					elementMapIndex++;
				}
                if ("validation" == atts.getName([i])) {
                    elementMap[elementMapIndex] = ["validation", atts.getValue([i])];
					elementMapIndex++;
                }
			}
		}
	// For history entries.
	} else if ( name == 'entry' ) {
		// Extract the 'type' attribute, if present.
		// TODO DBG
		//alert("In element user.  atts has length " + atts.getLength() );
		for ( i=0; i<atts.getLength() ; i++ ) {
			// TODO DBG
			//alert("Operating on attribute " + atts.getName([i]) + " = " + atts.getValue([i]));
			if ( "type" == atts.getName([i]) ) {
				elementMap[elementMapIndex] = ["action", atts.getValue([i])];
				elementMapIndex++;
			}
		}
	} else if ( name == 'history') {
		// Clear the working array.
		elementMap = new Array();
		elementMapIndex = 0;
		maintainers = "";
		writers = "";
		members = "";
	}
	
}  // end function startElement


SAXEventHandler.prototype.startDocument = function() {
    /*****************************************************************************
    function:  startDocument

    author: djoham@yahoo.com

    description:
        Fires at the start of the document
    *****************************************************************************/

    //place startDocument event handling code below this line
	// DHW
    this._handleCharacterData();
	// Initialize the result array.
	resultsMap = new Array();
	resultsMapIndex = 0;
	previousElement = '';
	currentElement = '';
	

}  // end function startDocument


/*****************************************************************************
                    SAXEventHandler Object Lexical Handlers
*****************************************************************************/


SAXEventHandler.prototype.comment = function(data, start, length) {
    /*****************************************************************************
    function:  comment

    author: djoham@yahoo.com

    description:
        Fires when a comment is found

        data is your full XML string
        start is the beginning of the XML character data being reported to you
        end is the end of the XML character data being reported to you

        the data can be retrieved using the following code:
        var data = data.substr(start, length);
    *****************************************************************************/
    this._handleCharacterData();

    //place comment event handling code below this line


}  // end function comment


SAXEventHandler.prototype.endCDATA = function() {
    /*****************************************************************************
    function:  endCDATA

    author: djoham@yahoo.com

    description:
        Fires at the end of a CDATA element
    *****************************************************************************/
    this._handleCharacterData();

    //place endCDATA event handling code below this line


}  // end function endCDATA


SAXEventHandler.prototype.startCDATA = function() {
    /*****************************************************************************
    function:  startCDATA

    author: djoham@yahoo.com

    description:
        Fires at the start of a CDATA element
    *****************************************************************************/
    this._handleCharacterData();

    //place startCDATA event handling code below this line


}  // end function startCDATA


/*****************************************************************************
                    SAXEventHandler Object Error Interface
*****************************************************************************/


SAXEventHandler.prototype.error = function(exception) {
    /*****************************************************************************
    function:  error

    author: djoham@yahoo.com

    description:
        Fires when an error is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    this._handleCharacterData();

    //place error event handling code below this line


}  // end function error


SAXEventHandler.prototype.fatalError = function(exception) {
    /*****************************************************************************
    function:  fatalError

    author: djoham@yahoo.com

    description:
        Fires when a  fatal error is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    this._handleCharacterData();

    //place fatalError event handling code below this line


}  // end function fatalError


SAXEventHandler.prototype.warning = function(exception) {
    /*****************************************************************************
    function:  warning

    author: djoham@yahoo.com

    description:
        Fires when a warning is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    this._handleCharacterData();

    //place warning event handling code below this line


}  // end function warning


/*****************************************************************************
                   SAXEventHandler Object Internal Functions
*****************************************************************************/


SAXEventHandler.prototype._fullCharacterDataReceived = function(fullCharacterData) {
    /*****************************************************************************
    function:  _fullCharacterDataReceived

    author: djoham@yahoo.com

    description:
        this function is called when we know we are finished getting
        all of the character data. If you need to be sure you handle
        your text processing when you have all of the character data,
        your code for that handling should go here

        fullCharacterData contains all of the character data for the element
    *****************************************************************************/

    var styleData = function(element, data) {
        if (element=="gid") {
            return "<i>" + data + "</i>";
        }
        return data;
    }

    //place character (text) event handling code below this line
	// DHW
	
	// DHW: For those of you wondering: Yes, this method was the one that convinced
	// me that I should have used a DOM parser instead of SAX.  It is on my list :)
	
	// Don't write element without data.
	fullCharacterData = fullCharacterData.replace(/^\s*|\s*$/g,'');
	if ( fullCharacterData != "" && fullCharacterData != "\n" && fullCharacterData != null ) {
		// Create entries in elementMap that relate to the names of the fields on
		// the "modify" forms in the files purl|user|group|domain.html; this facilitates
		// population of the modify forms with record data.
		if ( currentElement == "url" && ( previousElement == "target" || previousElement == "seealso" ) ) {
			elementMap[elementMapIndex] = [previousElement, fullCharacterData];
			elementMapIndex++;
			
		} else if ( ( currentElement == "uid" || currentElement == "gid" ) && previousElement == "maintainers" ) {
			// Store uids and gids to account for possible multiple maintainers.
			// We will write them into the array when the document is fully parsed.
			maintainers = maintainers + "," + styleData(currentElement, fullCharacterData);
			
		} else if ( ( currentElement == "uid" || currentElement == "gid" )  && previousElement == "writers" ) {
			// Store uids and gids to account for possible multiple writers.
			// We will write them into the array when the document is fully parsed.
			writers = writers + "," + styleData(currentElement, fullCharacterData);
			
		} else if ( ( currentElement == "uid" || currentElement == "gid" )  && previousElement == "members" ) {
			// Store uids and gids to account for possible multiple members.
			// We will write them into the array when the document is fully parsed.
			members = members + "," + styleData(currentElement, fullCharacterData);
			
		} else if ( currentElement == "uid" && previousElement == "modifiedby" ) {
			// There should only be one uid that modified a resource in history.
			elementMap[elementMapIndex] = [currentElement, fullCharacterData];
			elementMapIndex++;
		
		// All other entries we want to retain go here.	
		} else if ( currentElement == "id" || currentElement == "name" || currentElement == "type"  || currentElement == "comments"  || currentElement == "public"  || currentElement == "affiliation" || currentElement == "email" || currentElement == "modifieddate" || currentElement =="message" ) {
			elementMap[elementMapIndex] = [currentElement, fullCharacterData];
			elementMapIndex++;
        }
	}

}  // end function _fullCharacterDataReceived


SAXEventHandler.prototype._handleCharacterData = function()  {
    /*****************************************************************************
    function:  _handleCharacterData

    author: djoham@yahoo.com

    description:
        This internal function is called at the beginning of every event, with the exception
        of the characters event.  It fires the internal event this._fullCharacterDataReceived
        if there is any data in the this.characterData internal variable.
        It then resets the this.characterData variable to blank

        Generally, you will not need to modify this function
    *****************************************************************************/

    // call the function that lets the user know that all of the text data has been received
    // but only if there is data.
    if (this.characterData != "") {
        this._fullCharacterDataReceived(this.characterData);
    }

    //reset the characterData variable
    this.characterData = "";

}  // end function _handleCharacterData

