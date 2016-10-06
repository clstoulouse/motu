// retourne la hauteur du DIV dont l'ID est passe en parametre
function getDivHeight(blocId) {
	var divHeight = 0;
	if (document.getElementById) {
		var div = document.getElementById(blocId);
		if (div) {
			divHeight = div.offsetHeight;
		} else {
			divHeight = 0;
		}
	}
	return divHeight;
}

// retourne la largeur du DIV dont l'ID est passe en parametre
function getDivWidth(blocId) {
	var divWidth = 0;
	if (document.getElementById) {
		var div = document.getElementById(blocId);
		if (div) {
			divWidth = div.offsetWidth;
		} else {
			divWidth = 0;
		}
	}
	return divWidth;
}

// Fixe la hauteur du bandeau blanc en background par rapport à la hauteur du topleftmenu
// Parametres :
//		ID du div de reference (topleftmenu)
//		hauteur maximum du bandeau blanc
function setWhiteBoxHeight (divID, maxHeight) {
	if (divID) {
		if ((!maxHeight) || (maxHeight<0) || (maxHeight>400)) { maxHeight = 200; }
		var div1 = document.getElementById('white_background_inner');
		var div2 = document.getElementById('white_background_inner_page');
		var hmax = maxHeight;
		var h = getDivHeight(divID);
		if (h > hmax) { h=hmax; }
		if (div1) {
			div1.style.height = h+'px';
		}
		if (div2) {
			div2.style.height = h+'px';
		}
	}
}

// Fixe la largeur de la colonne de texte en fonction de la largeur de la colonne images
function setContentWidth () {
	var div = document.getElementById('page_content');
	var l = getDivWidth('images_content');
	var lmax = 310;
	if (l > lmax) { l=lmax; }
	l = 410 + (310-l) - 50;
	if (div) {
		div.style.width = l+'px';
	}
}