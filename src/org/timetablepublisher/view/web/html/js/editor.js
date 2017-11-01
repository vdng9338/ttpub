// http://www.howtocreate.co.uk/tutorials/javascript/browserwindow
var myWidth = 0, myHeight = 0;
function winWidth() 
{
  if( typeof( window.innerWidth ) == 'number' ) 
  {
    //Non-IE
    myWidth = window.innerWidth;
    myHeight = window.innerHeight;
  } 
  else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) 
  {
    //IE 6+ in 'standards compliant mode'
    myWidth = document.documentElement.clientWidth;
    myHeight = document.documentElement.clientHeight;
  }
  else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) 
  {
    //IE 4 compatible
    myWidth = document.body.clientWidth;
    myHeight = document.body.clientHeight;
  }
}

var newwindow;
function openPopUp(url)
{
    var width = 800;
	winWidth();
    if(myWidth > 500 && myWidth < 1700) {
        width = myWidth;
    }
    
	newwindow=window.open(url,'name','top=1,right=1,scrollbars=1,height=170,width=' + width);
	newwindow.visibility = "visible";
	newwindow.focus();
}

function bookmark(url, bm)
{
    var tmp = url;
    var str = tmp.split('#');
	return str[0] + "#" + bm;
}

function navigate(bm)
{
    window.location.href = bookmark(window.location.href, bm);
}

function refreshParent(bm)
{    
    opener.location.href = bookmark(opener.location.href, bm);
    opener.location.reload(true);
}
function refreshParentAndClose()
{
    opener.location.reload(true);
    setTimeout("self.close();", 700);
}

function commitAndClose()
{
  id = setTimeout("self.close();", 3000);
  var answer = confirm ("Your new settings were saved.  Continue editing?")
  if (answer) 
  {
    opener.location.reload(true);
    window.clearTimeout(id);
  }
  return true;
}

function confirmDelete()
{
  return confirm ("Are you sure you want to delete this row?")
}