
//----------------------------------------------------------
window.onload = function(e) {
    setRegionRanges();
};

var aMapTool=null;
var aRegionWidget=null;

//Override this function if you want some actions after a page load
function init() {
  aMapTool = new MapTool(document.map, "${formName}");
  aMapTool.setMode('single');
  aMapTool.selectTool("xy");
  aMapTool.setRanges();
}





//This function was originally designed to interact with the
//Map applet. New code was needed to make it work with
//the non-java map.
MapWidget.prototype.positionTool = function(xlo,xhi,ylo,yhi) {
	var tool = this.mTool;
	if (tool == "X" || tool == "PT"){
		 var y = (ylo + yhi)/2.0;
		 ylo = y;
		 yhi = y;
		 if (tool != "PT" && xlo == xhi){
		   xlo = this.minx;
		   xhi = this.maxx;
		 }
	}
	
	if (tool == "Y" || tool == "PT"){
	 var x = (xlo + xhi)/2.0;
	 xlo = x;
	 xhi = x;
	 if (tool != "PT" && ylo == yhi){
	   ylo = this.miny;
	   yhi = this.maxy;
	 }
	}
	
	this.wx[0].setValue(xlo);
	this.wx[1].setValue(xhi);
	this.wy[0].setValue(ylo);
	this.wy[1].setValue(yhi);

}




// Override this function if you want some action before submitting a form
// Note that the onSubmit event isn't issued if form.submit() is called
// programmatically. Sigh.
function doSubmit() {
  if (aMapTool){
    aMapTool.getRanges();
  }
}


var IsNetscape = document.all == null;


function setInherit(cName, bName) {
   if (IsNetscape){
    eval(cName + ".prototype.__proto__ = " + bName + ".prototype");
  } else {
    var aProto = eval(bName + ".prototype");
    for (var prop in aProto){
      var theCom = cName + ".prototype." + prop + "=" + bName + ".prototype."
	+ prop;
      eval(theCom);
    }
  }
}

function funcName(f, length) {
  var s = f.toString();
  if (!length){
      length = 200;
  }
  if (s==null || s.length == 0) s = "anonymous";
  if (s.length > length){
      s = s.substring(0,length) + "\n...\n";
  }
  return s;
}

function stackTrace(length) {
  var s = "";
  for (var a = arguments.caller; a != null; a = a.caller){
    s += funcName(a.callee, length) + "\n";
    if (a.caller == a) break;
  }
  return s;
}

function Assert(isTrue){
  if (!isTrue){
    var result = "Assertion failed:\n";
    result += stackTrace();
    alert(result);
  }
}


// Define a Widget object

function Widget(form, element) {
  var theFormEl = findForm(form);
  Assert(theFormEl);
  this.mForm = theFormEl;
  this.mElement = theFormEl.elements[element];
  // Assert(this.mElement);
  if (this.mElement) {
	  this.mElement.mObject = this;
	  this.mCallbackList = new Array();
  }
}

 // Set the currently selected to value (if it exists)
Widget.prototype.setSelected = function(value) {
  var e = this.mElement;
  var options = e.options;
  Assert(options);
  if (options){
    var selected = 0;
    for (var i=0; i < options.length; ++i){
      if (value == options[i].value){
        selected = i;
	break;
      }
    }
    e.selectedIndex = selected;
  }
}


// Return the value of the currently selected element
Widget.prototype.getSelected = function() {
  var e = this.mElement;
  var options = e.options;
  if (options){
    return options[options.selectedIndex].value;
  } else {
    return e.value;
  }
}

Widget.prototype.getSelectedIndex = function() {
  return this.mElement.options.selectedIndex;
}


// Return the element associated with a (form, element) name pair
Widget.prototype.getElement = function() {
  return this.mElement;
}

// Add a callback to this widget
Widget.prototype.addCallback = function(intype, obj) {
  var type = intype.toLowerCase();
  if (type == "onchange"){
    this.mCallbackList[this.mCallbackList.length] = "onchange()";
    if (obj != null){
      this.mElement.mObject = obj;
      this.mElement.mObject.onChange = obj.onChange;
    }
    this.mElement.onchange = this.globalActionChange;
  } else if (type == "onclick"){
    this.mCallbackList[this.mCallbackList.length] = "onclick()";
    this.mElement.onclick = this.globalActionClick;
  } else if (type == "onblur"){
    this.mCallbackList[this.mCallbackList.length] = "onblur()";
    this.mElement.onblur = this.globalActionBlur;
  } else {
    alert("Bad callback type " + type);
  }
}

// Execute the callbacks for this widget
Widget.prototype.execCallbacks = function() {
  for (var prop in this.mCallbackList){
    var mname =this.mCallbackList[prop];
    with(this.mElement){
      eval(mname);
    }
  }
}

// Following methods needed because, in JavaScript, 'this' will refer
// to the form element associated with the event, not the Widget
// object
Widget.prototype.globalActionChange = function() {
  this.mObject.onChange(this.mObject);
}

Widget.prototype.globalActionClick = function() {
  this.mObject.onClick(this.mObject);
}

Widget.prototype.globalActionBlur = function() {
  this.mObject.onBlur(this.mObject);
}


// Default onClick and onChange handlers
Widget.prototype.onClick = function() {
}

Widget.prototype.onChange = function() {
}

Widget.prototype.onBlur = function() {
}

// Multiwidget contains multiple select widgets and one text field
TimeMultiWidgetList = new Array();

function MultiWidget(formName, names, category) {
  var widgets = new Array();
  for (var j=0; j < names.length-1; ++j){
    widgets[widgets.length] = new Widget(formName, names[j]);
  }
  // Hack for events
  if (names[0].indexOf("t_") == 0){
      TimeMultiWidgetList[TimeMultiWidgetList.length] = this;
  }
  var form = findForm(formName);
  var textField = form.elements[names[names.length-1]];
  for (var i=0; i < widgets.length; ++i){
    widgets[i].addCallback("onchange", this);
  }
  this.mWidgets = widgets;
  this.mTextEl = textField;
  this.mCategory = category;
  this.onChange();
}

MultiWidget.prototype.setSelected = function(newval, splitStr) {
  var splitit = newval.split(splitStr);
  Assert(this.mWidgets.length == splitit.length);
  for (var i=0; i < splitit.length; i++){
      this.mWidgets[i].setSelected(splitit[i]);
  }
  this.onChange();
}

MultiWidget.prototype.getWidgets = function() {
  return this.mWidgets;
}

MultiWidget.prototype.onChange = function(widget) {
  var widgets = this.mWidgets;
  var value = widgets[0].getSelected();
  var ymdLength = Math.min(3, widgets.length);
  var isClim = this.mCategory == 'ctime';
  if (isClim){
    ymdLength = Math.min(2, widgets.length);
  }
  for (var i=1; i < ymdLength; ++i){
    value += '-' + widgets[i].getSelected();
  }

  // Only support hours for now
  if (i < widgets.length){
    var hour = widgets[i].getSelected();
    if (hour.length == 1){
      hour = '0' + hour;	// Gotta love that Ferret parser...
    }
    hour += ':00:00';
    if (isClim){
      value += '-0001 ' + hour;
    } else {
      value += ' ' + hour;
    }
  }
  this.mTextEl.value = value;
}

MultiWidget.prototype.getValue = function() {
  return this.mTextEl.value;
}
MultiWidget.prototype.setValue = function(value) {
  this.mTextEl.value = value;
}

MultiWidget.prototype.getSelectedIndex = function() {
  var length = this.mWidgets.length;
  var rval = new Array(length)
    for (var i=0; i < length; ++i){
      rval[i] = this.mWidgets[i].getSelectedIndex();
    }
  return rval;
}






function ViewWidget(form,element){
  this.base = Widget;
  this.base(form,element);
  this.mAction = this.mForm.action;
  this.addCallback("onchange");
}
setInherit("ViewWidget", "Widget");

ViewWidget.prototype.onChange = function() {
  this.mAction.value="changeView";
  doSubmit();
  this.mForm.submit();
}

// Emulate MapTool applet if user elects not to use Java
function MapTextWidget(parent,wtype,form,element) {
  this.base = Widget;
  this.base(form,element);
  this.mParent = parent;
  this.mType = wtype;
  this.addCallback("onblur");
  this.mFvalue = 0.0;
}
setInherit("MapTextWidget", "Widget");

MapTextWidget.prototype.validate = function() {
  var str = this.mElement.value;
  // var index = str.search(/[^\d\s\.\-ewns]/i);
  var index = str.search(/[\-\+]?[\d\.]+\s*[ewns]?\s*$/i);
  if (index == -1){
    this.setValue(this.mFvalue);
  } else {
    this.setValue(this.getValue());
  }
}

MapTextWidget.prototype.onBlur = function(widget) {
  this.validate();
  this.mParent.onBlur(widget);
  
}

MapTextWidget.prototype.setValue = function(fvalue){
  this.mFvalue = fvalue;
  this.mElement.value = fvalue;
 // Modif//
// if (this.mType == "lon"){
// this.convert_lon(fvalue);
// } else {
// this.convert_lat(fvalue);
// }
 // Modif//
}

MapTextWidget.prototype.getValue = function(){
  if (this.mType == "lon"){
    return this.unconvert_lon();
  } else {
    return this.unconvert_lat();
  }
}

MapTextWidget.prototype.unconvert_lat = function(){
  var inval = this.mElement.value;
  var index = inval.search(/\s*[ns]\s*$/i);
  if (index > -1){
    var orig_val = inval.substring(0, index);
    if (inval.search(/\s*s\s*$/i) > -1){
      orig_val = - orig_val;
    }
    inval = orig_val;
  }
  return inval;
}

function convert_modulo(inval) {
  inval = inval % 360;
  if (inval < 0){
    inval += 360;
  }
  if (inval > 180){
    inval -= 360;
  }
  return inval;
}

MapTextWidget.prototype.unconvert_lon = function(){
  var inval = this.mElement.value;
  var index = inval.search(/\s*[ew]\s*$/i);
  if (index > -1){
    var orig_val = inval.substring(0, index);
    if (inval.search(/\s*w\s*$/i) > -1){
      orig_val = - orig_val;
    }
    inval = convert_modulo(orig_val);
  }
  return inval;
}

MapTextWidget.prototype.convert_lon = function(inval){
  var oldval = this.mElement.value;
  var val = inval + 0.;
  if ( isNaN(val) ) {
     this.mElement.value = oldval
     return;
  }
  if (val < 0){
    this.mElement.value = -inval + " W";
  } else {
    this.mElement.value = inval + " E";
  }
}

MapTextWidget.prototype.convert_lat = function(inval){
  var oldval = this.mElement.value;
  var val = inval + 0.;
  if ( isNaN(val) ) {
     this.mElement.value = oldval;
     return;
  }
  if (val < 0){
     this.mElement.value = -inval + " S";
  } else {
     this.mElement.value = inval + " N";
  }
}

function MapWidget(formName) {
  this.mTool = "XY";
  var es = getFormElements(formName);
  this.wx = new Array();
  this.wy = new Array();
  this.wx[0] = new MapTextWidget(this,"lon", formName,"xlo_text");
  this.wx[1] = new MapTextWidget(this,"lon", formName,"xhi_text");
  this.wy[0] = new MapTextWidget(this,"lat", formName,"ylo_text");
  this.wy[1] = new MapTextWidget(this,"lat", formName,"yhi_text");
  this.minx = -180.0;
  this.maxx = 180.0;
  this.miny = -90.0;
  this.maxy = 90.0;

// if("${hasGeoAxis}" == "true") {
// this.minx = $XMin;
// this.maxx = $XMax;
// this.miny = $YMin;
// this.maxy = $YMax;
// }
}

MapWidget.prototype.onBlur = function(widget){
  var tool = this.mTool;
  var value = widget.getValue();
  widget.setValue(value);

  // Keep as line or point if necessary
  if (tool == "X" || tool == "PT"){
    if (this.wy[0] == widget || this.wy[1] == widget){
      var source = this.wy[0] == widget ? this.wy[0] : this.wy[1];
      var dest = this.wy[0] == widget ? this.wy[1] : this.wy[0];
      dest.setValue(value);
    }
  }
  if (tool == "Y" || tool == "PT"){
    if (this.wx[0] == widget || this.wx[1] == widget){
      var source = this.wx[0] == widget ? this.wx[0] : this.wx[1];
      var dest = this.wx[0] == widget ? this.wx[1] : this.wx[0];
      dest.setValue(value);
    }
  }
}

MapWidget.prototype.setMode = function() {
}

MapWidget.prototype.clip_lon = function(x) {
  x = convert_modulo(x);
  if (x < this.minx){
    x = this.minx;
  }
  if (x > this.maxx){
    x = this.maxx;
  }
  return x;
}

MapWidget.prototype.clip_lat = function(y){
  if (y < this.miny){
    y = this.miny;
  }
  if (y > this.maxy){
    y = this.maxy;
  }
  return y;
}

function unconvert_mod_range(xvals){
    if (xvals[1] - xvals[0] >= 360.0){
	return [-180.0, 180.0];
    }
    var rvals = [xvals[0], xvals[1]];
    if (xvals[1] < xvals[0]){
	xvals[1] += 360.0;
    }
    return rvals;
}

MapWidget.prototype.intersect = function(x1,x2,ox1,ox2, ctype){
    var xrange = [x1,x2];
    var oxrange = [ox1, ox2];
    if (ctype == "lon"){
	xrange = unconvert_mod_range(xrange);
	oxrange = unconvert_mod_range(oxrange);
    }
    x1 = xrange[0]; x2 = xrange[1]; ox1 = oxrange[0]; ox2 = oxrange[1];
    if ((x1 <= ox1 && x2 <= ox1) || (x1 >= ox2 && x2 >= ox2)){
	return [x1,x2];
    }
    var rvals = [Math.max(x1,ox1),Math.min(x2,ox2)];
    if (ctype == "lon"){
	rvals[0] = convert_modulo(rvals[0]);
	rvals[1] = convert_modulo(rvals[1]);
    }
    return rvals;
}

MapWidget.prototype.restrictToolRange = function(dummy,x1,x2,y1,y2) {
  this.minx = x1; this.maxx = x2; this.miny = y1; this.maxy = y2;
    
  x1 = this.wx[0].getValue(); x2 = this.wx[1].getValue();
  y1 = this.wy[0].getValue(); y2 = this.wy[1].getValue();
  x1 = convert_modulo(x1); x2 = convert_modulo(x2);
  if (x1 == x2){
    x1 = -180;
    x2 = 180;
  }
  var xvals = [x1,x2];
  var yvals = [y1,y2];
  xvals = this.intersect(xvals[0],xvals[1], this.minx, this.maxx, "lon");

  yvals = this.intersect(yvals[0],yvals[1], this.miny, this.maxy, "lat");
  this.positionTool(xvals[0], xvals[1], yvals[0], yvals[1]);
}

MapWidget.prototype.get_x_range = function() {
  var x1 = this.wx[0].getValue();
  var x2 = this.wx[1].getValue();
  if (x1 == x2 && "Y" != this.mTool && "PT" != this.mTool){
    x1 = this.minx; x2 = this.maxx;
  }
  if (x2 < x1){
    x2 += 360.0;
  }
  return x1 + " " + x2;
}

MapWidget.prototype.get_y_range = function() {
    return this.wy[0].getValue() + " " + this.wy[1].getValue();
}

MapWidget.prototype.setToolRangeFull = function() {
   	this.positionTool(-180,180,-90,90);

// if("${hasGeoAxis}" == "true") {
// this.positionTool($XMin,$XMax,$YMin,$YMax);
// } else {
// this.positionTool(-0,360,-90,90);
// }
    
}

MapWidget.prototype.setTool = function(tool) {
  this.mTool = tool;
}


// MapTool object
function MapTool(applet, formName){
  function defaultError() {
    return false;
  }

  function appletError() {
    window.onerror = defaultError;
    setUseJava(false);
    return true;
  }

  if (formName == null){
    formName = 'region';
  } 
  this.mForm = document.forms[formName];
  if ("${use_java}" == "false"){
    this.mApplet = new MapWidget(formName);
  } else {
    if ( "${use_java}" != "true" ) {
       var badJava = !navigator.javaEnabled() || !applet;
       if (!badJava){		// Try and access an applet function
         window.onerror = appletError;
         applet.get_x_range(0);
         window.onerror = defaultError;
       }
       if (badJava && "${use_java}" == "true"){
         // Something's wrong -- try non-java
         setUseJava(false);
         return;// Never reached
       }
    }
    this.mApplet = applet;
  }
}

MapTool.prototype.setMode = function(mode_name) {
    this.mApplet.setMode(mode_name);
}

MapTool.prototype.setRanges = function(){
  var xlo = parseFloat(this.mForm.elements.x_lo.value);
  var xhi = parseFloat(this.mForm.elements.x_hi.value);
  var ylo = parseFloat(this.mForm.elements.y_lo.value);
  var yhi = parseFloat(this.mForm.elements.y_hi.value);
  this.mApplet.positionTool(xlo, xhi, Math.min(ylo, yhi),
			    Math.max(ylo,yhi));
}

MapTool.prototype.getRanges = function(){
  var rval = new Array(2);
  var str = new String(this.mApplet.get_x_range(0));
  rval.x = str.split(" ");
// Applet sometimes returns only one value...no comment...
  if (rval.x[1] == null){
    rval.x[1] = rval.x[0];
  }
  str = new String(this.mApplet.get_y_range(0));
  rval.y = str.split(" ");
  if (rval.y[1] == null){
    rval.y[1] = rval.y[0];
  }
  this.mForm.elements.x_lo.value = rval.x[0];
  this.mForm.elements.x_hi.value = rval.x[1];
  this.mForm.elements.y_lo.value = rval.y[0];
  this.mForm.elements.y_hi.value = rval.y[1];
}

MapTool.prototype.setRegion = function(label){
  if ( label.indexOf('No Region') != -1 ) {
    // no op;
  } else if ( label.indexOf('Full Region') != -1 ) {
    this.mApplet.setToolRangeFull();
  } else {
    regionarray = label.split(",");
    var xlo = parseFloat(regionarray[0]);
    var xhi = parseFloat(regionarray[1]);
    var ylo = parseFloat(regionarray[2]);
    var yhi = parseFloat(regionarray[3]);
    if (Math.abs(xlo-xhi) >= 360.0){
      xlo = -180;
      xhi = 180;
    }
    this.mApplet.positionTool(xlo, xhi, Math.min(ylo,yhi), 
                              Math.max(ylo, yhi));
    // Time events
    if (regionarray.length > 4){
		var timeLength = TimeMultiWidgetList.length;
		if (timeLength > 0){
		    TimeMultiWidgetList[0].setSelected(regionarray[4], "-");
		}
		if (timeLength > 1){
		    TimeMultiWidgetList[1].setSelected(regionarray[5], "-");
		}
    }
  }
}

MapTool.prototype.selectTool = function(view) {
  // Tell the Java LiveMap about this choice

  var toolInt = 0;
  var result;
  if ( view.indexOf('x')!=-1 ) toolInt += 1;
  if ( view.indexOf('y')!=-1 ) toolInt += 2;
  switch(toolInt){
  case 0:
    result = "PT";
    break;
  case 1:
    result = "X";
    break;
  case 2:
    result = "Y";
    break;
  case 3:
    result = "XY";
    break;
  }
  Assert(result != null);
  this.mApplet.setTool(result);
}

function RegionWidget(form, element){
  this.base = Widget;
  this.base(form, element);
  this.mAction = this.mForm.action;
  this.addCallback("onchange");
}
setInherit("RegionWidget", "Widget");

RegionWidget.prototype.onChange = function() {
  var index = this.getSelected();
  var theForm = findForm();
  regionarray = index.split(",");
  var xlo = parseFloat(regionarray[0]);    
  var xhi = parseFloat(regionarray[1]);
  var ylo = parseFloat(regionarray[2]);
  var yhi = parseFloat(regionarray[3]);
  //SMY
  //if (Math.abs(xlo - xhi) >= 360.0) {
  //  xlo = -180;
  //  xhi = 180;
  //}
  theForm.xlo_text.value = String(xlo);    
  theForm.ylo_text.value = String(ylo);
  theForm.xhi_text.value = String(xhi);
  theForm.yhi_text.value = String(yhi);
  
  if( aMapTool !== null && aMapTool.mApplet !== null ){
      aMapTool.mApplet.wx[0].validate();
      aMapTool.mApplet.wx[1].validate();
      aMapTool.mApplet.wy[0].validate();
      aMapTool.mApplet.wy[1].validate();
  }
}

function showLoadingMask(){
  var loadingDiv = document.getElementById("Loading");
  loadingDiv.style.visibility="visible";
}

function updateMap() {
  var theForm = findForm();

  if ((theForm.xlo_text != null) && (theForm.x_lo != null)) {
	  theForm.x_lo.value = theForm.xlo_text.value;	
  }
  if ((theForm.ylo_text != null) && (theForm.y_lo != null)) {
	  theForm.y_lo.value = theForm.ylo_text.value;	
  }
  if ((theForm.xhi_text != null) && (theForm.x_hi != null)) {
	  theForm.x_hi.value = theForm.xhi_text.value;	
  }
  if ((theForm.yhi_text != null) && (theForm.y_hi != null)) {
	  theForm.y_hi.value = theForm.yhi_text.value;	
  }
}

function setRegionWidget() {
  aRegionWidget.onChange();
}

function intersect(x1,x2,ox1,ox2, ctype){
    var xrange = [x1,x2];
    var oxrange = [ox1, ox2];
    if (ctype == "lon"){
		xrange = unconvert_mod_range(xrange);
		oxrange = unconvert_mod_range(oxrange);
    }
    x1 = xrange[0]; x2 = xrange[1]; ox1 = oxrange[0]; ox2 = oxrange[1];
    if ((x1 <= ox1 && x2 <= ox1) || (x1 >= ox2 && x2 >= ox2)){
    	return [x1,x2];
    }
    var rvals = [Math.max(x1,ox1),Math.min(x2,ox2)];
    if (ctype == "lon"){
		rvals[0] = convert_modulo(rvals[0]);
		rvals[1] = convert_modulo(rvals[1]);
    }
    return rvals;
}

function setRegionRanges() {
  var theForm = findForm();
  if( theForm.x_lo !== undefined ){ // Not a DGF download page
	  var xvals = [theForm.x_lo.value,theForm.x_hi.value];
	  var yvals = [theForm.y_lo.value,theForm.y_hi.value];
	  var xlo = parseFloat(theForm.x_lo.value);
	  var xhi = parseFloat(theForm.x_hi.value);
	  var ylo = parseFloat(theForm.y_lo.value);
	  var yhi = parseFloat(theForm.y_hi.value);
	  
	  xvals = intersect(xlo, xhi, -180, 360);
	  yvals = intersect(ylo, yhi, -90, 90);
	  
	  theForm.xlo_text.value = xvals[0];	
	  theForm.xhi_text.value = xvals[1];	
	  theForm.ylo_text.value = yvals[0];	
	  theForm.yhi_text.value = yvals[1];
  }
}

function getRegionRanges() {
}

function setUseJava(newval){
  getFormElements('region').use_java.value = newval;
  // stuffForm('constrain');
}

function findForm(form) {
  if (form == null){
    return document.forms["${formName}"];
  } else {
    return document.forms[form];
  }
}
var winMotuScriptCmd = null;

function closeScriptCmdWin() {
	if (winMotuScriptCmd != null) {
		winMotuScriptCmd.close();
		winMotuScriptCmd = null;
	}
}
 function openScriptCmdWin(form) {
  closeScriptCmdWin();
  updateMap();
  var theForm = findForm(form);
  var win = window.open('','MotuScriptCmd');
  winMotuScriptCmd = win;
  var doc = win.document.open("text/html");
  doc.write("<h1>Python script command line that matches the extraction:</h1>");
  doc.write("<p>To resquest data, you can also use the Python script. This page should help you to enter your command line from the shell of you system (Linux/Unix/windows).</p>");

  doc.write('<p>You can download the Motu Python Client package ');
  doc.write('<a href="https://github.com/clstoulouse/motu-client-python" target="_blank">here</a>.</p>');
  
  doc.write('<p><b>Python 2.7</b> or higher is required in order to execute the Motu Python script.');
  doc.write('Python can be downloaded <a href="https://www.python.org/downloads/" target="_blank">here</a>.</p>');

  var x_lo = theForm.elements['x_lo'];
  var x_hi = theForm.elements['x_hi'];
  var y_lo = theForm.elements['y_lo'];
  var y_hi = theForm.elements['y_hi'];

  var t_lo = theForm.elements['t_lo'];
  var t_hi = theForm.elements['t_hi'];

  var z_lo = theForm.elements['z_lo'];
  var z_hi = theForm.elements['z_hi'];

  var variables = theForm.elements['variable'];

  var url = new String(location.href);

  var url_parts = url.split("?");
  var motuUrl = url_parts[0];
  var q = "";
  var qq = '"';
  var motu_client_py = "motu-client.py";
  var userName = "${user}";

  
  var cmd =  'python ';
  cmd +=  motu_client_py;
  
  if (userName != "") {
	  cmd += " -u " + q + userName + q;
	  cmd += " -p " + q + "<i>your_password</i><i><b>(1)</b></i>" + q;
  } else {
	  cmd += " --auth-mode=" + q + "none " + q;
  }
  
  cmd += " -m " + q + motuUrl + q;
  cmd += " -s " + q + "${service.getNameEncoded()}" + q;
  cmd += " -d " + q + "${product.getProductId()}" + q;
  
  if (x_lo != null) {
	  cmd += " -x " + q + x_lo.value + q;
  }
  if (x_hi != null) {
	  cmd += " -X " + q + x_hi.value + q;
  }
  if (y_lo != null) {
	  cmd += " -y " + q + y_lo.value + q;
  }
  if (y_hi != null) {
	  cmd += " -Y " + q + y_hi.value + q;
  }
  

  if (t_lo != null) {
	  cmd += " -t " + qq + t_lo.value + qq;
  }
  if (t_hi != null) {
	  cmd += " -T " + qq + t_hi.value + qq;
  }
  
  if (z_lo != null) {
	  cmd += " -z " + q + z_lo.value + q;
  }
  if (z_hi != null) {
	  cmd += " -Z " + q + z_hi.value + q;
  }
  var nVar = 0;
  var varOptions = "";
  if (variables != null) {
	  for (var variable = 0; variable < variables.length; variable++){
		  var item = variables[variable];
		  if (item.checked) {
		      varOptions += " -v " + q + item.value + q + " ";		    	 
		  }
	  }
	  cmd += varOptions;
  }

  cmd += " -o " + q + "<i>your_output_directory</i><i><b>(1)</b></i>" + q;
  cmd += " -f " + q + "<i>your_output_file_name</i><i><b>(1)</b></i>" + q;

  cmd += " --proxy-server=" + q + "<i>your_proxy_server_url</i><b><font color='red'>:</font></b><i>your_proxy_port_number</i><i><b>(2)</b></i>" + q;
  cmd += " --proxy-user=" + q + "<i>your_proxy_user_login</i><i><b>(3)</b></i>" + q;
  cmd += " --proxy-pwd=" + q + "<i>your_proxy_user_password</i><i><b>(3)</b></i>" + q;


  doc.write('<p>To execute your extraction through the Motu Python Client, type (copy/paste) the <font color="blue">command-line</font> below on your system command prompt:</p>');
  doc.write('<p><font color="blue">');
  doc.write(cmd);
  doc.write('</font></p>');
  doc.write('<p><font color="blue">(1)</font> Value must be replaced by yourself.</p>');  
  doc.write('<p/>');
  doc.write('<p><font color="blue">(2)</font> If you use an HTTP proxy, replace the value by your proxy url and port number: e.g. \'http://myproxy.org:8080\'. If you don\'t use HTTP proxy, remove this option.</p>');
  doc.write('<p/>');
  doc.write('<p><font color="blue">(3)</font> If you use an HTTP proxy with authentication, replace the value by your login and password. If you don\'t need to authenticate to your proxy, remove these options.</p>');
  doc.write('<p/>');
  
  doc.write('<p/>');
  doc.write('<p>Full documentation is available <a href="https://github.com/clstoulouse/motu-client-python#Usage" target="_blank">here</a>.</p>');
  doc.write('<p>To get help on the Motu Python Client, type : \'');
  doc.write(motu_client_py);
  doc.write(' --help\' on your system command prompt.</p>');

  doc.write('<p>Note that if your python bin directory is not in your path environment variable, ');
  doc.write('the full command is:<br/><br><i>path_to_your_python_bin_directory</i>/python <i>path_to_your_motu_python_script_directory</i>/' + motu_client_py + ' <i>your_extraction_parameter_and_options</i></p>');
  

  doc.close();
  win.focus();
}
 
 function unload() {
	closeScriptCmdWin();
 }
 