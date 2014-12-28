
function bodyCss(doc,tagName)
{
 	e = doc.getElementsByTagName(tagName);
 	for (var i = 0; i < e.length; i++) 
 		with (e[i].style)
		{
 			backgroundColor = "black";
 			color           = "white";
 			margin          = "0px";
 			fontFamily      = "Swiss911 UCm BT, Compacta LT Light, sans-serif";
 			fontSize        = "18pt";
 			textTransform   = "uppercase";
			backgroundImage = "";
 		}
}

function hxCss(doc,tagName)
{
	f = "Swiss911 UCm BT, Compacta LT Light, sans-serif";
	s = "28pt";
	c = "white";
	if (tagName=="h1")
	{
 		f = "Compacta LT Light, Swiss911 UCm BT, sans-serif";
		s = "48pt";
		c = "#FF9900";
	}
	else if (tagName=="h2")
	{
 		f = "Compacta LT Light, Swiss911 UCm BT, sans-serif";
		s = "32pt";
	}
 	e = doc.getElementsByTagName(tagName);
 	for (var i = 0; i < e.length; i++) 
 		with (e[i].style)
		{
			fontFamily = f;
			color      = c;
 			fontSize   = s;
 			fontWeight = "normal";
 		}
}

function textCss(doc,tagName)
{
 	e = doc.getElementsByTagName(tagName);
 	for (var i = 0; i < e.length; i++)
	{
		var attr = e[i].attributes.getNamedItem("size"); 
		if (attr)
		{
			var v = parseInt(attr.nodeValue);
			if (v<0) attr.nodeValue = "0";
  	}
		with (e[i].style)
		{
			fontFamily = "Swiss911 UCm BT, Compacta LT Light, sans-serif";
			fontSize = "18pt";
			fontWeight = "normal";
			fontStyle = "normal";
			textDecoration = "none";
		}
	}
}

function hiliteCss(doc,tagName)
{
 	e = doc.getElementsByTagName(tagName);
 	for (var i = 0; i < e.length; i++)
	{
		var attr = e[i].attributes.getNamedItem("size"); 
		if (attr)
		{
			var v = parseInt(attr.nodeValue);
			if (v<0) attr.nodeValue = "0";
		}
		with (e[i].style)
		{
			fontFamily = "Swiss911 UCm BT, Compacta LT Light, sans-serif";
			color      = "#FF9900";
			fontSize = "18pt";
			fontWeight = "normal";
			fontStyle = "normal";
			textDecoration = "none";
		}
	}
}

function tableCss(doc, tagName)
{
	e = doc.getElementsByTagName(tagName);
	for (var i = 0; i < e.length; i++)
	{
		if (e[i].attributes.getNamedItem("border"))
			e[i].attributes.removeNamedItem("border");
	}
}

function txCss(doc,tagName)
{
 	e = doc.getElementsByTagName(tagName);
 	for (var i = 0; i < e.length; i++)
	{
		if (e[i].attributes.getNamedItem("bgcolor"))
		{
  		e[i].attributes.removeNamedItem("bgcolor");
			//e[i].style.backgroundColor = "#111166";
			//e[i].style.color = "white";
			//e[i].style.border = "0px solid black";
  	}
	}
}

function thCss(doc,tagName)
{
 	e = doc.getElementsByTagName(tagName);
 	for (var i = 0; i < e.length; i++) 
 		with (e[i].style)
		{
			backgroundColor = "#9999FF";
			color           = "black";
			fontSize        = "18pt";
 			fontWeight      = "normal";
			fontStyle       = "normal"; 
			textDecoration  = "none";
 		}
}

function codeCss(doc,tagName)
{
 	e = doc.getElementsByTagName(tagName);
 	for (var i = 0; i < e.length; i++) 
 		with (e[i].style)
		{
 			fontFamily    = "Compacta LT Light, Swiss911 UCm BT, sans-serif";
			fontSize      = "22pt";
 			textTransform = "none";			
 		}
}

function aCss(doc,tagName)
{
 	e = doc.getElementsByTagName(tagName);
 	for (var i = 0; i < e.length; i++) 
 		with (e[i].style)
		{
	    color          = "#FF9900";
	    margin         = "3px";
	    fontWeight     = "normal";
	    fontStyle      = "normal"; 
	    textDecoration = "none";
 		}
}

function css(doc)
{
	bodyCss(doc,"body");
	hxCss(doc,"h1");
	hxCss(doc,"h2");
	hxCss(doc,"h3");
	hxCss(doc,"h4");
	hxCss(doc,"h5");
	hxCss(doc,"h6");
	textCss(doc,"p");
	textCss(doc,"td");
	textCss(doc,"li");
	textCss(doc,"i");
	hiliteCss(doc,"em");
	textCss(doc,"b");
	aCss(doc,"dt");
	textCss(doc,"dl");
	textCss(doc,"font");
	tableCss(doc,"table");
	thCss(doc,"th");
	txCss(doc,"td");
	txCss(doc,"tr");
	codeCss(doc,"code");
	codeCss(doc,"pre");
	aCss(doc,"a");
}

css(document);
/*
var e = document.getElementsByTagName("frame");
for (var i = 0; i < e.length; i++)
{
	// Does not work for whatever reason -->
	css(e[i].document);
	// <--
}
*/


