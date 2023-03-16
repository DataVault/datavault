$(document).ready(function(){
	var today = new Date();
	today.setHours(0,0,0,0);
	var _30YearsFromToday= new Date();
	_30YearsFromToday.setFullYear(_30YearsFromToday.getFullYear() + 30);

	$.datepicker.setDefaults({
		dateFormat: "yy-mm-dd",
		changeMonth: true,
		changeYear: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		maxDate: _30YearsFromToday
	});

	$( "#reviewDate" ).datepicker();

});