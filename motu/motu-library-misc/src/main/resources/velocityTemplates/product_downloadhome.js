

//----------------------------------------------------------



//Override this function if you want some actions after a page load
function init() {
  aMapTool = new MapTool(document.map, "${formName}");
  aMapTool.setMode('single');
  aMapTool.selectTool("xy");
  aMapTool.setRanges();
//  setRegionRanges();
  
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
  Assert(this.mElement);
  this.mElement.mObject = this;
  this.mCallbackList = new Array();
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

//Following methods needed because, in JavaScript, 'this' will refer
//to the form element associated with the event, not the Widget
//object
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
  //  var index = str.search(/[^\d\s\.\-ewns]/i);
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
 //Modif//
//  if (this.mType == "lon"){
//    this.convert_lon(fvalue);
//  } else {
//    this.convert_lat(fvalue);
//  }
 //Modif//
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

//  if("${hasGeoAxis}" == "true") {
//	  this.minx = $XMin;
//	  this.maxx = $XMax;
//	  this.miny = $YMin;
//	  this.maxy = $YMax;
//  }
}

MapWidget.prototype.onBlur = function(widget){
  var tool = this.mTool;
  var value = widget.getValue();
  // Clip to restricted region
////////////////  if (widget.mType == "lon"){
////////////////    value = this.clip_lon(value);
////////////////  } else {
////////////////    value = this.clip_lat(value);
////////////////  }
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

// This function was originally designed to interact with the
// Map applet.  New code was needed to make it work with
// the non-java map.
MapWidget.prototype.positionTool = function(xlo,xhi,ylo,yhi) {
  var tool = this.mTool;
  if ("${use_java}" == "true") {
  // This will prevent the "cross-hair" location from being displayed
  // in the non-java map, so only use if java is on.
    if (tool == "XY"){
      if (xlo == xhi){
        xlo = this.minx;
        xhi = this.maxx;
      }
      if (ylo == yhi){
        ylo = this.miny;
        yhi = this.maxy;
      }
    }
  } // end if java
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

/////////////////////  xlo = this.clip_lon(xlo);
///////////////////////  xhi = this.clip_lon(xhi);
  if ("${use_java}" == "true") {
    // This logic is pervent the cross hair values from being 
    // added to the text boxes in the non-java case.  Only do
    // if java is on.
     if (xlo == xhi && !("Y" == tool || "PT" == tool)){
       xlo = this.minx;
       xhi = this.maxx;
     }
  }
////////     ylo = this.clip_lat(ylo);
////////     yhi = this.clip_lat(yhi);
  if ("${use_java}" == "true") {
     // Same as above.
     if (ylo == yhi && !("X" == tool || "PT" == tool)){
       ylo = this.miny;
       yhi = this.maxy;
     }
  }

  // In the Java map case, the values are on the client.

  if ( "${use_java}" == "true" ) {
     this.wx[0].setValue(xlo);
     this.wx[1].setValue(xhi);
     this.wy[0].setValue(ylo);
     this.wy[1].setValue(yhi);
  }

  // In the non-java case get them from the mapstate bean from the server.
  else {
//     this.wx[0].setValue(-180);
//     this.wx[1].setValue(180);
//     this.wy[0].setValue(-90);
//     this.wy[1].setValue(90);
     this.wx[0].setValue(xlo);
     this.wx[1].setValue(xhi);
     this.wy[0].setValue(ylo);
     this.wy[1].setValue(yhi);
     
//     if("${hasGeoAxis}" == "true") {
//	     this.wx[0].setValue($XMin);
//	     this.wx[1].setValue($XMax);
//	     this.wy[0].setValue($YMin);
//	     this.wy[1].setValue($YMax);
//  	 }
     
  }


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

//     if("${hasGeoAxis}" == "true") {
//     	this.positionTool($XMin,$XMax,$YMin,$YMax);
//  	 } else { 
//     	this.positionTool(-0,360,-90,90);
//     }
    
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
  if ( "${use_java}" == "true" ) {
     // In the non-java case, the restriction happens on the server side.
     var txlo = parseFloat("-180");
     var txhi = parseFloat("180");
     var tylo = parseFloat("-82");
     var tyhi = parseFloat("81.9746362041896");

     if("${hasGeoAxis}" == "true") {
	     txlo = parseFloat("${XMin}");
	     txhi = parseFloat("${XMax}");
	     tylo = parseFloat("${YMin}");
	     tyhi = parseFloat("${YMax}");
  	 }
     
     this.mApplet.restrictToolRange(0, txlo, txhi, Math.min(tylo, tyhi),
  			       Math.max(tylo,tyhi));
  }
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
//  // do this for map applet
//  if ( "false" == "true") {
//     aMapTool.setRegion(index);
//  }
//  else {
//     // do these for non-java map version

//     this.mAction.value="changeRegion";
//     stuffForm('constrain');
//  }

  	var theForm = findForm();
  
	regionarray = index.split(",");
	var xlo = parseFloat(regionarray[0]);	
	var xhi = parseFloat(regionarray[1]);
	var ylo = parseFloat(regionarray[2]);
	var yhi = parseFloat(regionarray[3]);
    if (Math.abs(xlo - xhi) >= 360.0) {
      xlo = -180;
      xhi = 180;
    }
	theForm.xlo_text.value = String(xlo);	
	theForm.ylo_text.value = String(ylo);
	theForm.xhi_text.value = String(xhi);
	theForm.yhi_text.value = String(yhi);
	
	aMapTool.mApplet.wx[0].validate();
	aMapTool.mApplet.wx[1].validate();
	aMapTool.mApplet.wy[0].validate();
	aMapTool.mApplet.wy[1].validate();
}


var aMapTool;
var aRegionWidget;

function updateMap() {
//   var widxlot = new MapTextWidget(this,"lon", 'region',"xlo_text");
//   var widxhit = new MapTextWidget(this,"lon", 'region',"xhi_text");
//   var widylot = new MapTextWidget(this,"lat", 'region',"ylo_text");
//   var widyhit = new MapTextWidget(this,"lat", 'region',"yhi_text");
 //  document.location.href="constrain?"+widxlot.getValue()+","+widxhit.getValue()+","+widylot.getValue()+","+widyhit.getValue();
  var theForm = findForm();
  theForm.x_lo.value = theForm.xlo_text.value;	
  theForm.y_lo.value = theForm.ylo_text.value;	
  theForm.x_hi.value = theForm.xhi_text.value;	
  theForm.y_hi.value = theForm.yhi_text.value;	
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
  var xvals = [theForm.x_lo.value,theForm.x_hi.value];
  var yvals = [theForm.y_lo.value,theForm.y_hi.value];
  var xlo = parseFloat(theForm.x_lo.value);
  var xhi = parseFloat(theForm.x_hi.value);
  var ylo = parseFloat(theForm.y_lo.value);
  var yhi = parseFloat(theForm.y_hi.value);
  
  xvals = intersect(xlo, xhi, -180, 180, "lon");

  yvals = intersect(ylo, yhi, -90, 90, "lat");
  
  theForm.xlo_text.value = xvals[0];	
  theForm.xhi_text.value = xvals[1];	
  theForm.ylo_text.value = yvals[0];	
  theForm.yhi_text.value = yvals[1];	

}
function getRegionRanges() {


}

function setUseJava(newval){
  getFormElements('region').use_java.value = newval;
  //stuffForm('constrain');
}

function findForm(form) {
  if (form == null){
    return document.forms["${formName}"];
  } else {
    return document.forms[form];
  }
}
