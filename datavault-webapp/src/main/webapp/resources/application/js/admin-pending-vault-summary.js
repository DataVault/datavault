
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
		minDate: today,
		maxDate: _30YearsFromToday
	});

	$( "#reviewDate" ).datepicker();

	$("#reviewDate").change(function() {
		
		var reviewDateString = $("#reviewDate").val().trim();
		console.log("reviewDateString", reviewDateString);
		var validationMessage = validateAdminCurationReviewDateString(reviewDateString);

		if(validationMessage.trim() !== '') {
			console.log(validationMessage);
			// Add error message     
			$("#invalid-review-date-span").text(validationMessage);
			// Disable "Create Vault" button
			$("#create-vault-btn").prop("disabled", true);
			

		} else {
			// Clear Error text     
			$("#invalid-review-date-span").text(""); 
			// Enable "Create Vault" button
			$("#create-vault-btn").prop("disabled", false);
		}

	}).trigger('change');
});