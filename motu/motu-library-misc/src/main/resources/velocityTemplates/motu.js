
function getFormElements(formName) {
  return document.forms[formName].elements;
}
function findForm(form) {
  if (form == null){
    //return document.forms[1];
    return document.forms[0];
  } else {
    return document.forms[form];
  }
}

function selectAll(name) {
  var theForm = findForm();
  var theCollection = eval("theForm."+name);
  for (c=0;c<theCollection.length;c++){
    theCollection[c].checked=true;
  }
}

function unselectAll(name) {
  var theForm = findForm();
  var theCollection = eval("theForm."+name);
  for (c=0; c<theCollection.length;c++){
    theCollection[c].checked=false;
  }
}

function stuffForm(nextUrl, formName){
  var theForm = findForm(formName);
  if (theForm == null){
    window.location.href=nextUrl;
    return false;
  }
  if (theForm.nexturl == null){
    alert("stuffForm: no nexturl field");
    return false;
  }
  theForm.nexturl.value = nextUrl;
  doSubmit();
  theForm.submit();
  return false;
}

function openDoc(link) {
  var dataWindow = window.open(link, "Documentation", 
  "menubar=yes,toolbar=yes,location=yes,resizable=yes,scrollbars=yes,width=600,height=400");
  dataWindow.focus();
}

function showInfo(title, message){
  var infoWindow = window.open("", "Help",
      "menubar=no,toolbar=no,location=no,resizable=yes,scrollbars=yes,width=300,height=400");
  var doc = infoWindow.document;
  doc.open();
  doc.writeln("<html><head><title>LAS Help</title></head><body>");
  doc.writeln("<h3>" + title + "</h3>");
  doc.writeln(message);
  doc.writeln("</body></html>");
  doc.close();
  infoWindow.focus();
}
