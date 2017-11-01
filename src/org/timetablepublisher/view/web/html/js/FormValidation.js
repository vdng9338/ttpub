// ===================================================================
// Author: Frank Purcell <purcellf@trimet.org>
//
// Based on code from: Stephen Poley 
//    http://www.xs4all.nl/~sbpoley/webmatters/formval.html
//
// ===================================================================
function trim(str)
{
  return str.replace(/^\s+|\s+$/g, '')
}

function msg(fld,     // id of element to display message in
             msgtype, // class to give element ("warn" or "error")
             message) // string to display
{
   alert(msgtype + ": " + message);
}

var glb_vfld;
function setFocusDelayed()
{
  glb_vfld.focus()
}
function setfocus(vfld)
{
  // save vfld in global variable so value retained when routine exits
  glb_vfld = vfld;
  setTimeout( 'setFocusDelayed()', 100 );
}

var NODE_TEXT = 3; // DOM text node-type
var PROCEED   = 111;  
function commonCheck    (vfld,   // element to be validated
                         ifld,   // id of element to receive info/error msg
                         reqd)   // true if required
{
  if (!document.getElementById)
    return true;  // not available on this browser - leave validation to the server

  var elem = document.getElementById(ifld);
  if(elem == null)     return true;  // somethings wrong
  if(!elem.firstChild) return true;  // not available on this browser 
  if(elem.firstChild.nodeType != NODE_TEXT) return true;  // ifld is wrong type of node  

  if (emptyString.test(vfld.value)) 
  {
    if (reqd) 
    {
      msg(ifld, "error", "ERROR: required");  
      setfocus(vfld);
      return false;
    }
  }
  return PROCEED;
}

function validateSequence(vfld,   // element to be validated
                          ifld,   // id of element to receive info/error msg
                          reqd)   // true if required
{
//  var stat = commonCheck(vfld, ifld, reqd);
//  if (stat != PROCEED) return stat;

  var tfld = trim(vfld.value);  // value of field with whitespace trimmed off
  var telnr = /^[0-9<>Xx]+$/
  if (!telnr.test(tfld)) 
  {
    msg (ifld, "ERROR", "Options are either a sequence number, an X (to drop timepoint from schedule) or a << or >> to indicate a column merge.");
    setfocus(vfld);
    return false;
  }
}
