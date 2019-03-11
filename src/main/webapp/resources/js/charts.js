
function drawChart(title, jsonData, elementId, chartType, colors, additionalOptions) {
	var data = JSON.parse(jsonData);
	var defaultColors = ['#455a64', '#303f9f', '#5d4037', '#512da8', '#00796b', '#d32f2f', '#1976d2', '#388e3c', '#ffa000'];

	if (!colors) {
		colors = defaultColors;
	}

	if (data.datasets.length === 1) {
		data.datasets[0].backgroundColor = colors;
		data.datasets[0].borderColor = colors;
		data.datasets[0].fill = false;
	} else {
		for (var i = 0; i < data.datasets.length; i++) {
			data.datasets[i].backgroundColor = colors[i];
			data.datasets[i].borderColor = colors[i];
			data.datasets[i].fill = false;
		}
	}

	var options = {
		title: {
			display: true,
				text: title,
				position: 'top'
		},
		maintainAspectRatio: false
	};

	options = $.extend({}, options, additionalOptions);

	var context = document.getElementById(elementId).getContext('2d');
	var chart = new Chart(context, {
		type: chartType,
		data: data,
		options: options
	});
}