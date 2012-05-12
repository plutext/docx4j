

function newImage(arg) {
	if (document.images) {
		rslt = new Image();
		rslt.src = arg;
		return rslt;
	}
}

function changeImages() {
	if (document.images) {
		for (var i=0; i<changeImages.arguments.length; i+=2) {
			document[changeImages.arguments[i]].src = changeImages.arguments[i+1];
		}
	}
}



function PrintThisPage() 
{
//  --- the directory at the same level than this file where are 
//  --- the "printer friendly" files are located such 
//  --- as ...../pages/mypage.html vs ...../printfriendly/mypage.html
		
// where we are now
		loc=window.location.href.toLowerCase();

//protocol
		ptcl= loc.toLowerCase().split("://")[0];

// little array with all the url components splitted by /
		splitted=loc.toLowerCase().split("/");
		
//the protocol itself
		ptcl = splitted[0];

if (ptcl == "http:"){
		oldTerm= splitted[2];
		newTerm= "/"+oldTerm + "/flat";
		url=replace2FlatUrl(loc, oldTerm, newTerm)
 		PopMeThis(url,750, 600,100,25)
} 
else
{
		loc=window.location.href.toLowerCase().split("//");
		oldTerm = loc[1].split("/")[2];
		newTerm = "/flat/"+oldTerm
		url=replace2FlatUrl(window.location.href, oldTerm, newTerm)
		PopMeThis(url,750,600,100,25)
}
}

//  build pop-up window
function PopMeThis(myUrl,w,h,l,t)
	{	var sOption="toolbar=yes,location=yes,directories=yes,menubar=yes,"; 
        sOption+="resizable=yes,scrollbars=yes,width="+ w +",height="+ h +",left="+ l +",top="+ t ; 

   		var winprint=window.open(myUrl,"pw",sOption); 
	 
        winprint.focus(); 
	}


function writeURL()
 {
		// where we are now
			loc=window.location.href.toLowerCase();
			
			
		//the url of the original web page
			url= replaceString("/flat",'',loc)
			document.write(url);    
 }


function replace2FlatUrl(from, oldTerm, newTerm) 
{
	re = new RegExp('(\\W)'+oldTerm,'gi')
		result=from.replace(re, newTerm);
	return result;
}		

// Replaces oldS with newS in the string fullS 
function replaceString(oldS,newS,fullS)      
{ 
   for (var i=0; i<fullS.length; i++) 

  {      
	if (fullS.substring(i,i+oldS.length) == oldS) 
		{  fullS = fullS.substring(0,i)+newS+fullS.substring(i+oldS.length,fullS.length)   }   
  }  
		 return fullS
}

