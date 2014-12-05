var proto = dcodeIO.ProtoBuf.loadProtoFile("directdraw.proto");
$.ajax({
	url : "/draw/tests",
}).done(function(d) {
	var data = $.parseJSON(d);

	var selected = getUrlParameter('n');

	data.forEach(function(method, index, array) {
		if (selected == null || (selected != null && index == selected)) {
			document.body.innerHTML += '<div id="' + method + 'label"></div><canvas id="' + method + '" width="1000" height="100"></canvas>';
		}
	});

	data.forEach(function(method, index, array) {
		if (selected == null || (selected != null && index == selected)) {
			$.ajax({
				url : "/draw?reset&test=" + method,
			}).done(function(d) {
				var canvas = document.getElementById(method);
				var dd = new WebswingDirectDraw({
					canvas : canvas,
					proto : proto
				});
				var json = $.parseJSON(d);
				dd.draw64(json.protoImg);
				drawImage(canvas, json.originalImg);
				addInfo(json, index, document.getElementById(method + "label"));
			});
		}
	});

});

function addInfo(json, index, element) {
	element.innerHTML = index + ". " + ((json.protoRenderSize / json.originalRenderSize) * 100).toPrecision(4) + "% in size, " + ((json.protoRenderTime / json.originalRenderTime) * 100).toPrecision(4) + "% in time";
}

function drawImage(canvas, b64image) {
	var imageObj;
	imageObj = new Image();
	imageObj.onload = function() {
		var context = canvas.getContext("2d");
		context.drawImage(imageObj, 500, 0);
		imageObj.onload = null;
		imageObj.src = '';
	};
	imageObj.src = 'data:image/png;base64,' + b64image;
}

function getUrlParameter(sParam) {
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for ( var i = 0; i < sURLVariables.length; i++) {
		var sParameterName = sURLVariables[i].split('=');
		if (sParameterName[0] == sParam) {
			return sParameterName[1];
		}
	}
}