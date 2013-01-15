function hideContent(guid) {
	var codeTag = $('codeEditor-' + guid);
	codeTag.style.display = "none";
}

function showContent(guid) {
	var selectBox = $('select-' + guid);

	var url = ROOT_URL;
	if(!url.endsWith('/')) {
		url = url + '/';
	}

	new Ajax.Request(url+'scriptApi/file', {
		method: 'get',
		parameters: { 'path': selectBox.value },
		evalJS: 'false',
		onSuccess: function(transport) {
			var codeTag = $('code-' + guid);
			codeTag.innerText = transport.responseText;

			var codeEditor = $('codeEditor-' + guid);
			codeEditor.style.display = "block";
			prettyPrint();
		},
		onError: function(transport) {
			console.log("failure!", transport);
		}
	});
}

function selectChanged(guid) {
	var codeTag = $('codeEditor-' + guid);
	if(codeTag.style.display == "block") {
		showContent(guid);
	}
}

function loadPreview(guid) {
	var codeTag = $('codeEditor-' + guid);
	if(codeTag.style.display == "block") {
		hideContent(guid);
	} else {
		showContent(guid);
	}

	return false;
}

function saveChanges(guid) {
	var url = ROOT_URL;
	if(!url.endsWith('/')) {
		url = url + '/';
	}

	var selectBox = $('select-' + guid);
	var codeEditor = $('codeEditor-' + guid);
	var codeBox = codeEditor.down("pre");
	codeBox.style.backgroundColor = "#D5D5D5";
	codeBox.removeAttribute("contenteditable");

	new Ajax.Request(url+'scriptApi/updateFile', {
		method: 'post',
		parameters: { 'path': selectBox.value, 'content': codeEditor.innerText },
		evalJS: 'false',
		onSuccess: function(transport) {
			codeBox.style.backgroundColor = "white";
			codeBox.setAttribute("contenteditable", true);
			prettyPrint();
		},
		onError: function(transport) {
			alert("failed to save!", transport);
			console.log("failed to save!");

			codeBox.style.backgroundColor = "white";
			codeBox.setAttribute("contenteditable", true);
			prettyPrint();
		}
	});
}