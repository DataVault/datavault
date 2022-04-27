//Code from https://stackabuse.com/javascript-get-number-of-days-between-dates/
function dateDiffInDaysStartingAtMidnight(d1, d2) {
	var date1 = new Date(d1);
	var date2 = new Date(d2);
	date1.setHours(0,0,0,0);
	date2.setHours(0,0,0,0);

	// One day in milliseconds
	var oneDay = 1000 * 60 * 60 * 24;

	// Calculating the time difference between two dates
	var diffInTime = date2.getTime() - date1.getTime();

	// Calculating the no. of days between two dates
	var diffInDays = Math.round(diffInTime / oneDay);

	return diffInDays;
}

//Code from https://www.codegrepper.com/code-examples/javascript/javascript+calculate+months+between+two+dates
function dateDiffInMonths(d1, d2) {
	var months;
	months = (d2.getFullYear() - d1.getFullYear()) * 12;
	months -= d1.getMonth();
	months += d2.getMonth();
	return months <= 0 ? 0 : months;
}

//Code from https://www.scriptol.com/javascript/dates-difference.php
function dateDiffInYears(d1, d2) {
	var ynew = d2.getFullYear();
	var mnew = d2.getMonth();
	var dnew = d2.getDate();
	var yold = d1.getFullYear();
	var mold = d1.getMonth();
	var dold = d1.getDate();
	var diff = ynew - yold;
	if (mold > mnew) diff--;
	else {
		if (mold == mnew) {
			if (dold > dnew) diff--;
		}
	}
	return diff;
}

// Note textforDateNameInMsg and startYearsInFuture are optional parameters in function.
function validateDateString(dateString, textforDateNameInMsg = "Date", startYearsInFuture = 0) {
	// Regex for format yyyy-mm-dd
	var date_regex =  /^([1-2]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))$/;
	var matchDate = dateString.match(date_regex);
	var msg = "";

	console.log("validateDateString: ", dateString, "matchDate: ", matchDate);

	if(matchDate === null) {
		msg = "Invalid Date format, it must be yyyy-mm-dd, e.g., 2030-02-03."
	} else {
		var today = new Date();
		var dateToValidate = new Date(dateString);
		var differenceInDays = dateDiffInDaysStartingAtMidnight(today, dateToValidate);
		var differenceInYears = dateDiffInYears(today, dateToValidate);
		console.log("differenceInDays: ", differenceInDays);
		console.log("differenceInYears: ", differenceInYears);

		if (differenceInYears > 30) {
			msg = "Invalid " + textforDateNameInMsg + ", it  must be less than 30 years in the future.";
		} else if (differenceInYears < startYearsInFuture) {
			msg = "Invalid " + textforDateNameInMsg + ", it  must be " +  startYearsInFuture + " years in the future.";   
		}
	}
	return msg;
}


//Used by Admin 
function validateAdminCurationReviewDateString(dateString) {
	var textforDateNameInMsg = "Review Date";
	return validateDateString(dateString, 0, textforDateNameInMsg);
}


