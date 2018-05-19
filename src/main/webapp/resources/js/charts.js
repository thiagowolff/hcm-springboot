
if (typeof google !== 'undefined') {
	google.charts.load('current', {'packages':['corechart', 'calendar'], 'language': 'pt'});
	google.charts.setOnLoadCallback(drawCharts);
}

function drawChart(title, dataArray, elementId, chartType, additionalOptions) {
	if (typeof google == 'undefined' || !dataArray) {
		return;
	}

	var options = {
		title: title,
		legend: { position: 'none' },
		sliceVisibilityThreshold: 0.04,
		pieResidueSliceColor: '#2f353e',
		pieResidueSliceLabel: "Outros",
		animation:{
			startup: true,
			duration: 1000,
			easing: 'out'
		},
		colors: [ '#d81b60', '#3949ab', '#00897b', '#e53935', '#546e7a', '#5e35b1', '#f06292', '#7986cb', '#4db6ac', '#e57373', '#e0e0e0', '#9575cd' ]
	};

	options = $.extend({}, options, additionalOptions);

	var data = google.visualization.arrayToDataTable(JSON.parse(dataArray));
	var chart = new chartType(document.getElementById(elementId));
	chart.draw(data, options);
}