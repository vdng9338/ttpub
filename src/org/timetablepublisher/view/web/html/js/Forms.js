/* 
  author:  Purcell :: http://www.frankpurcell.com
  NOTE:    relies on Sugar.js being loaded
*/
function Forms(name, id, value, size, formArray)
{
    function Constructor()
    {
        this.m_name      = name;
        this.m_id        = id;
        this.m_value     = value;
        this.m_origvalue = value;
        this.m_size      = size;
        this.m_open      = false;
        
        document.getElementById( this.m_name ).onmouseover = formHighLight;
        document.getElementById( this.m_name ).onmouseout  = formLowLight;

        formArray.push(this);
    }

    // define methods atop this data
    with(Constructor)
    {
        method('putInput', function()
        {
             if( this.m_open == true) return;

             this.m_open = true;
             document.getElementById( this.m_name ).innerHTML = "<input type='text' value='" + this.m_value + "' id='" + this.m_id +"' size='" + this.m_size + "' onChange='javascript:" + this.m_name + ".setValue(this.value)'>";
             document.getElementById( this.m_id ).focus();
        });  

        method('setValue', function(value)
        {
            this.m_open = false;
            this.m_value = value;
            document.getElementById( this.m_name ).innerHTML = this.m_value;
        });  

        method('getId', function()
        {
            return this.m_id;
        });  

        method('getValue', function()
        {
            return this.m_value;
        });  

        method('isDirty', function()
        {
            if(this.m_value == this.m_origvalue)
                return false;
            else
                return true;
        });  
    } // end of with clause

    return new Constructor();
}

function display(div, formArray)
{
  var string = "";
  for (i=0; i < formArray.length; i++) 
  {
    if(formArray[i].isDirty())
        string += formArray[i].getValue() + "<br/>";
  }
  document.getElementById(div).innerHTML = string;
}


//
// PARAMS: 
//    form        -- the FORM element (eg: this in the onsumbit event)
//    ttFormArray -- the array of Forms.js elements representing the 
//                   editable table elements
//    stopList    -- selection list of stops to add to this list
//
function mySubmit(form, ttFormArray, stopList)
{
  // step 1: grey out all (turn off) submit buttons
  for(i = 0; i < form.length; i++) 
  {
      if(form.elements[i].type.toLowerCase() == "submit") 
      {
          form.elements[i].disabled = true;
      }
  }

  // step 2: add values from the stop list
  for (var i=0; i < stopList.options.length; i++) 
  {    
      var h = document.createElement("input");
      h.setAttribute("type",  "hidden");
      h.setAttribute("name",  stopList.name);
      h.setAttribute("value", stopList.options[i].value);
      form.appendChild(h);
  }

  // step 3: add any changed forms to the FORM summition
  for(var i=0; i < ttFormArray.length; i++) 
  {
    if(ttFormArray[i].isDirty())
    {
      var h = document.createElement("input");
      h.setAttribute("type",  "hidden");
      h.setAttribute("name",  ttFormArray[i].getId());
      h.setAttribute("value", ttFormArray[i].getValue());
      form.appendChild(h);
    }      
  }

  return true;
}

//
// http://www.w3schools.com/htmldom/dom_obj_style.asp
//
function formHighLight()
{
  this.style.outlineStyle = "inset";
  this.style.outlineWidth = "medium";
  this.style.outlineColor = "#11AAFF";
}
function formLowLight()
{
  this.style.outlineStyle = "none";
}
