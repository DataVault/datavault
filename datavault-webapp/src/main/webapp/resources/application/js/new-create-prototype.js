$(document).ready(function () {
	var current_fs, next_fs, previous_fs;
	var opacity;
	var confirmedTrue = ($("#confirmed").val() === 'true');

	// Prevent Enter Key Submitting Form
	$("form input").on("keypress", function (e) {
		return e.keyCode != 13;
	});

	var startYearsInFutureForGEDNegative = -60; // 60 years in past

	var todayForDatepicker = new Date();
	todayForDatepicker.setHours(0, 0, 0, 0);
	var _30YearsFromToday = new Date();
	_30YearsFromToday.setFullYear(_30YearsFromToday.getFullYear() + 30);

	$.datepicker.setDefaults({
		dateFormat: "yy-mm-dd",
		changeMonth: true,
		changeYear: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		maxDate: _30YearsFromToday
	});

	$("#grantEndDate").datepicker();
	$("#billingGrantEndDate").datepicker();
	$("#reviewDate").datepicker({ minDate: '+36m' });

	// Get db Grant End Date when page set
	var dbGrantEndDate = $("#grantEndDate").val().trim();
	console.log("dbGrantEndDate: ", dbGrantEndDate);

	// Ownership display
	$("#isOwnerTrue").change(function () {
		if ($(this).is(":checked")) {
			$('#owner-uun-span').show();
			$("#vaultOwner").hide();
		}
	}).trigger('change');

	$("#isOwnerFalse").change(function () {
		if ($(this).is(":checked")) {
			$('#owner-uun-span').hide();
			$("#vaultOwner").show();
		}
	}).trigger('change');

	/*
	if billinGrantEndDate is filled in, disable granteddate on the info page and populate it with the billing value
	if billingGrantEndDate is cleared enable grantenddate on the info page and populate it with dbGrantEndDate also reset review date
	 */
	$("#billingGrantEndDate").change(function () {
		// clear any validation
		$('#invalid-billing-grant-end-date-span').text('');
		if (confirmedTrue === false) {
			var dateResult = ($("#billingGrantEndDate").val().trim() === '');

			var fundingChecked = ($("#funding-query-yes").is(":checked"));

			if (dateResult === false && fundingChecked === true) {
				// 
				var validationMessage = validateDateString($("#billingGrantEndDate").val().trim(), "Grant End Date", startYearsInFutureForGEDNegative);
				if (validationMessage != "") {
					$('#invalid-billing-grant-end-date-span').text(validationMessage);
				}

				$("#grantEndDate").val($("#billingGrantEndDate").val().trim());
				$("#grantEndDate").prop("disabled", true);
			} else {
				var length = calculateReviewLength();
				var estimatedReviewDateAsISOString = calculateReviewDateForTodayAsISOString(length);
				estimatedReviewDateAsISOString = validateOrChangeReviewDateISOString(estimatedReviewDateAsISOString);
				$("#grantEndDate").prop("disabled", false);
				// Reset to dbGrantEndDate
				$("#grantEndDate").val(dbGrantEndDate);
				$("#reviewDate").val(estimatedReviewDateAsISOString);
			}
		}
	}).trigger('change');

	$("#billingGrantEndDate, #grantEndDate, #policyInfo").change(function () {
		// if the ged changes update the suggested review date
		if ($("#grantEndDate").val().trim() !== '') {
			var grantEndDateValidationMessage = validateDateString($("#grantEndDate").val().trim(), "Grant End Date", startYearsInFutureForGEDNegative);
			$('#invalid-grant-end-date-span').text(grantEndDateValidationMessage);
		} else {
			// clear any validation
			$('#invalid-grant-end-date-span').text('');
		}

		// if no retention policy picked yet ged + 3 years
		// if a retention policy has been picked ged + policy length
		if (confirmedTrue === false) {
			var noRP = ($("#policyInfo option:selected").val() === '' || $("#policyInfo option:selected").prop("disabled"));
			var noGED = ($("#grantEndDate").val().trim() === '');
			var noBillingGED = ($("#billingGrantEndDate").val().trim() === '');
			var ged = $("#grantEndDate").val().trim();
			var gedDate = new Date(ged);
			var gedMm = String(gedDate.getMonth() + 1).padStart(2, '0');
			var gedDd = String(gedDate.getDate()).padStart(2, '0');
			var billingGed = $("#billingGrantEndDate").val().trim();
			var billingGedDate = new Date(billingGed);
			var billingGedMm = String(billingGedDate.getMonth() + 1).padStart(2, '0');
			var billingGedDd = String(billingGedDate.getDate()).padStart(2, '0');
			var defaultLength = 3;
			var policyLength = calculateReviewLength();

			var today = new Date();
			var dd = String(today.getDate()).padStart(2, '0');
			var mm = String(today.getMonth() + 1).padStart(2, '0'); // January is 0
			// Default Review Date (3 years from today)
			var estimatedReviewDateAsISOString = calculateReviewDateForTodayAsISOString(defaultLength);

			// GED in future test
			var grantGEDInFuture = noGED ? false : (dateDiffInDaysStartingAtMidnight(today, gedDate) > 0);
			var billingGEDInFuture = noBillingGED ? false : (dateDiffInDaysStartingAtMidnight(today, billingGedDate) > 0);

			if (noRP === false && noGED === true && noBillingGED === true) {
				// if we only have policy then length + current date = review date
				estimatedReviewDateAsISOString = String(today.getFullYear() + policyLength) + '-' + mm + '-' + dd;
			}

			if (noRP === false && noBillingGED === false && billingGEDInFuture === true) {
				// if we have both then billing ged + policy length = review date
				estimatedReviewDateAsISOString = String(billingGedDate.getFullYear() + policyLength) + '-' + billingGedMm + '-' + billingGedDd;
			}

			if (noRP === true && noBillingGED === false && billingGEDInFuture === true) {
				// if we only have a ged in the billing fieldset then ged + 3 = review date
				estimatedReviewDateAsISOString = String(billingGedDate.getFullYear() + defaultLength) + '-' + billingGedMm + '-' + billingGedDd;

			}

			if (noRP === false && noGED === false && grantGEDInFuture === true) {
				// if we have both then ged + policy length = review date
				estimatedReviewDateAsISOString = String(gedDate.getFullYear() + policyLength) + '-' + gedMm + '-' + gedDd;
			}

			if (noRP === true && noGED === false && grantGEDInFuture === true) {
				// if we only have ged then ged + 3 = review date
				estimatedReviewDateAsISOString = String(gedDate.getFullYear() + defaultLength) + '-' + gedMm + '-' + gedDd;
			}

			if (noRP === false && noBillingGED === false && billingGEDInFuture === false) {
				estimatedReviewDateAsISOString = String(billingGedDate.getFullYear() + policyLength) + '-' + billingGedMm + '-' + billingGedDd;
			}

			if (noRP === false && noGED === false && grantGEDInFuture === false) {
				estimatedReviewDateAsISOString = String(gedDate.getFullYear() + policyLength) + '-' + gedMm + '-' + gedDd;
			}

			estimatedReviewDateAsISOString = validateOrChangeReviewDateISOString(estimatedReviewDateAsISOString);
			$("#reviewDate").val(estimatedReviewDateAsISOString);

			// Clear Error text and update message 
			$("#invalid-review-date-span").text('');
			var reviewDateUpdatedMsg = "Note the Review Date has been updated based on your selections.";
			$("#updated-review-date-span").text(reviewDateUpdatedMsg);

		}
	}).trigger('change');

	$("#reviewDate").change(function () {
		var startYearsInFuture = 3; // Min allowed years in future for  Review Date

		// Clear old messages beside the review date
		$("#updated-review-date-span").text('');
		$("#invalid-review-date-span").text('');

		var reviewDateString = $("#reviewDate").val().trim();
		console.log("reviewDateString", reviewDateString);

		var validationMessage = validateDateString(reviewDateString, "Review Date", startYearsInFuture);

		if (validationMessage.trim() !== '') {
			console.log(validationMessage);
			// Add error message     
			$("#invalid-review-date-span").text(validationMessage);

		} else {
			// Clear Error text     
			$("#invalid-review-date-span").text('');
		}

	}).trigger('change');

	$("#affirmation-check").change(function () {
		console.log("Affirmation: ", $(this).is(":checked"));
		$(this).parents("fieldset").children(".next").prop("disabled", !$(this).is(":checked"));
	}).trigger('change');

	$("#vaultName, #description, #policyInfo, #groupID, #reviewDate, #grantEndDate").change(function () {
		// if all the mandatory values in the info field set are non null or empty set the next button
		// display value to true
		// name
		var nameResult = ($("input[type=text][id=vaultName]").val().trim() === '');
		// description
		var descResult = ($("textarea[type=text][id=description]").val() === '');
		// retention policy
		var rpResult = ($("#policyInfo option:selected").val() === '' || $("#policyInfo option:selected").prop("disabled"));
		// school
		var schoolResult = ($("#groupID option:selected").val() === '' || $("#groupID option:selected").prop("disabled"));
		// review date
		var reviewResult = ($("input[id=reviewDate]").val().trim() === '' || $("#invalid-review-date-span").text().trim() !== '');
		// Check that there are no validation error of grantEndDate input
		// as it is not a required field
		var grantEndResult = ($("#invalid-grant-end-date-span").text().trim() !== '');

		if (nameResult === false && descResult === false && rpResult === false &&
			schoolResult === false && reviewResult === false && grantEndResult === false) {
			$(this).parents("fieldset").children(".next").prop("disabled", false);
		} else {
			$(this).parents("fieldset").children(".next").prop("disabled", true);
		}
	}).trigger('change');;

	$("#pureLink-check").change(function () {
		var result = $(this).is(":checked");
		if (result) {
			$("#confirm").prop("disabled", false);
		} else {
			$("#confirm").prop("disabled", true);
		}
	}).trigger('change');

	$("#isOwnerTrue, #isOwnerFalse, #vaultOwner").change(function () {
		var ownerFalseResult = $("input[type=radio][id=isOwnerFalse]").is(":checked");
		if (ownerFalseResult === true) {
			$('#vaultOwner').prop('disabled', false);
		} else {
			$('#vaultOwner').val("");
			$('#vaultOwner').prop('disabled', true);
		}

		var uunResult = ($("input[type=text][id=vaultOwner]").val().trim() === '');
		var ownerTrueResult = $("input[type=radio][id=isOwnerTrue]").is(":checked");

		if (ownerFalseResult && uunResult) {
			$(this).parents("fieldset").children(".next").prop("disabled", true);
			$('.owner-uun-required-error-span').html("<br>You have not specified any user as the Owner of this vault. Please add an owner, or contact the Research Data Support team if you want to request this vault to be treated as legacy data ie an orphan vault.");
			$('.owner-uun-required-error-span').show();

		} else if (ownerTrueResult) {
			// if ownerTrue and one of the other completed roles is the same uun
			var count = 0;
			var loggedInAS = $("#loggedInAs").val().trim()
			$(".unique-uun-required").each(function () {
				var uun = $(this).val().trim();
				if (uun !== '') {
					console.log("Checking (Role)", loggedInAS, " v ", uun);
					if (uun === loggedInAS) {
						count++;
					} else {
						$('.owner-uun-required-error-span').html("");
						$('.owner-uun-required-error-span').hide();
					}
				}

				if (count > 0) {
					console.log(loggedInAS, " Already has a role");
					$('.owner-uun-required-error-span').html("<br>A user may have only one vault-level user role, either Owner, NDM or Depositor. For further information please ");
					$('.owner-uun-required-error-span').append('<a href="https://www.ed.ac.uk/information-services/research-support/research-data-service/after/datavault/roles-and-access" target="_blank">click here</a>.');
					$('.owner-uun-required-error-span').show();
				} else {
					$('.owner-uun-required-error-span').html("");
					$('.owner-uun-required-error-span').hide();
				}
			});

		} else {
			$(this).parents("fieldset").children(".next").prop("disabled", false);
			$('.owner-uun-required-error-span').html("");
			$('.owner-uun-required-error-span').hide();
		}
	}).trigger('change');

	// Initially, disable next on doc initialisation
	$("#billing-section").parents("fieldset").children(".next").prop("disabled", true);
	// $('#vaultOwner').prop('disabled', true);

	// ----------- Billing ------------------------
	console.log("SliceQueryChoice: ", $("input[name='sliceQueryChoice']:checked").val());
	console.log("FundingQueryChoice: ", $("input[name='fundingQueryChoice']:checked").val());
	console.log("FeewaiverQueryChoice: ", $("input[name='feewaiverQueryChoice']:checked").val());

	$("input[name='feewaiverQueryChoice']").change(function () {
		console.log("feewaiverQueryChoice change");

		if ($(this).is(":checked") && $(this).val() === 'YES') {
			$('#billingType').val('FEEWAIVER');
			$('#sliceID').val('');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');
			$("input[name='sliceQueryChoice']").prop('checked', false).removeAttr('checked');
			$("input[name='fundingQueryChoice']").prop('checked', false).removeAttr('checked');

			$('#slice-form').collapse('hide');
			$('#slice-query-box').collapse('hide');
			$('#funding-query-box').collapse('hide');
			$('#payment-details-form').collapse('hide');
			console.log("feewaiverQueryChoice debugCount: ", 1);
		} else if ($(this).is(":checked") && $(this).val() === 'NO_OR_DO_NOT_KNOW') {
			$('#sliceID').val('');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#slice-query-box').collapse('show');
			console.log("feewaiverQueryChoice debugCount: ", 2);
		} else if (!$(this).is(":checked")) {
			$('#billingType').val('');
			$('#sliceID').val('');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');
			$("input[name='sliceQueryChoice']").prop('checked', false).removeAttr('checked');
			$("input[name='fundingQueryChoice']").prop('checked', false).removeAttr('checked');

			$('#slice-form').collapse('hide');
			$('#slice-query-box').collapse('hide');
			$('#funding-query-box').collapse('hide');
			$('#payment-details-form').collapse('hide');
			console.log("feewaiverQueryChoice debugCount: ", 3);
		}
		validateBillingFields();
	});

	$("input[name='sliceQueryChoice']").change(function () {

		$('.collapse').not('#feewaiver-query-box').not('#slice-query-box').not('#slice-form').collapse('hide');

		if ((!$(this).is(":checked")) && $('#feewaiver-query-no-or-do-not-know').is(":checked")) {
			$('.collapse').not('#feewaiver-query-box').not('#slice-query-box').collapse('hide');

			$('#sliceID').val('');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$("input[name='fundingQueryChoice']").prop('checked', false).removeAttr('checked');

			$('#slice-form').collapse('hide');
			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
			console.log("sliceQueryChoice debugCount: ", 1);
		}

		if ($(this).is(":checked") && $(this).val() === 'YES') {
			$('.collapse').not('#feewaiver-query-box').not('#slice-query-box').not('#slice-form').collapse('hide');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
			$('#slice-form').collapse('show');
			console.log("sliceQueryChoice debugCount: ", 2);
		}

		if ($(this).is(":checked") && $(this).val() == 'NO_OR_DO_NOT_KNOW') {
			$('#sliceID').val('');
			$('#slice-form').collapse('hide');

			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
			$('#funding-query-box').collapse('show');
			console.log("sliceQueryChoice debugCount: ", 4);
		}

		if ($(this).is(":checked") && $(this).val() === 'BUY_NEW_SLICE') {
			$('#sliceID').val('');
			$('#slice-form').collapse('hide');

			$("input[name='fundingQueryChoice']").prop('checked', false).removeAttr('checked');
            $('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
			console.log("sliceQueryChoice debugCount: ", 5);
		}

		validateBillingFields();
	});

	$("#sliceID").change(function () {
		validateBillingFields();
	});


	$("#slice-query-yes").change(function () {
		if ($(this).is(":checked")) {
			$('#billingType').val('SLICE');

			$('.collapse').not('#feewaiver-query-box').not('#slice-query-box').not('#slice-form').not('#slice-query-box').collapse('hide');

			$("input[name='fundingQueryChoice']").prop('checked', false).removeAttr('checked');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
			$('#slice-form').collapse('show');
		}
		validateBillingFields();
	});

	$("#slice-query-buy").change(function () {
		if ($(this).is(":checked")) {
			$('#billingType').val('BUY_NEW_SLICE');

			$('.collapse').not('#feewaiver-query-box').not('#slice-query-box').collapse('hide');

			$("input[name='fundingQueryChoice']").prop('checked', false).removeAttr('checked');
			
			$('#sliceID').val('');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
		}
		validateBillingFields();
	});

	$("#slice-query-no-or-do-not-know").change(function () {
		if ($(this).is(":checked")) {
			$('#billingType').val('');
			$('.collapse').not('#feewaiver-query-box').not('#slice-query-box').collapse('hide');
			$("input[name='fundingQueryChoice']").prop('checked', false).removeAttr('checked');

			$('#sliceID').val('');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
		}
		validateBillingFields();
	});


	$("input[name='fundingQueryChoice']").change(function () {
		console.log("fundingQueryChoice change");
		$('#feewaiver-query-box').collapse('show');
		if ($(this).is(":checked") && $(this).val() === 'YES') {
			$('.collapse').not('#feewaiver-query-box').not('#slice-query-box').not('#funding-query-box').collapse('hide');

			$('#sliceID').val('');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
			$('#funding-query-box').collapse('show');
			$('#payment-details-form').collapse('show');
			console.log("fundingQueryChoice debugCount: ", 1);
		}
		if ($(this).is(":checked") && $(this).val() === 'FUNDING_NO_OR_DO_NOT_KNOW') {
			$('.collapse').not('#feewaiver-query-box').not('#slice-query-box').not('#funding-query-box').collapse('hide');

			$('#sliceID').val('');
			$('#budget-authoriser').val('');
			$('#schoolOrUnit').val('');
			$('#subunit').val('');
			$('#projectTitle').val('');
			$('#billingGrantEndDate').val('');
			$('#budget-payment-details').val('');

			$('#feewaiver-query-box').collapse('show');
			$('#slice-query-box').collapse('show');
			$('#funding-query-box').collapse('show');
			$('#payment-details-form').collapse('hide');
			console.log("fundingQueryChoice debugCount: ", 2);
		}
		validateBillingFields();
	});

	$("#funding-query-no-or-do-not-know").change(function () {
		if (!$(this).val().trim()) {
			$('#billingType').val('');
		} else {
			$('#billingType').val('FUNDING_NO_OR_DO_NOT_KNOW');
		}
		validateBillingFields();
	});

	$("#budget-authoriser").change(function () {
		if (!$(this).val().trim()) {
			$('#billingType').val('');
		} else {
			$('#billingType').val('WILL_PAY');
		}
		validateBillingFields();
	});

	$("#schoolOrUnit").change(function () {
		validateBillingFields();
	});

	$("#subunit").change(function () {
		validateBillingFields();
	});

	$("#projectTitle").change(function () {
		validateBillingFields();
	});

	// Clear all query choices in Billing
	$(".query-choice-clear").click(function () {
		console.log("query-choice-clear pressed");
		// Clear radio button
		$("input[name='feewaiverQueryChoice']").prop('checked', false).removeAttr('checked');
		$("input[name='fundingQueryChoice']").prop('checked', false).removeAttr('checked');
		$("input[name='sliceQueryChoice']").prop('checked', false).removeAttr('checked');
		$('#sliceID').val('');
		$('#budget-authoriser').val('');
		$('#budget-payment-details').val('');
		$('#billingGrantEndDate').val('');
		$('.collapse').not('#feewaiver-query-box').collapse('hide');
		$('#feewaiver-query-box').collapse('show');
		validateBillingFields();
	});

	function validateBillingFields() {
		console.log("Called validateBillingFields");

		$("#billing-section").parents("fieldset").children(".next").prop("disabled", true);
		console.log("called validateBillingFields - Next: DISABLED");

		if ($("#feewaiver-query-yes").is(":checked")) {
			$("#billing-section").parents("fieldset").children(".next").prop("disabled", false);
			console.log("Radio feewaiverQueryChoice - #feewaiver-query-yes: CHECKED - Next: ENABLED");
		}

		if ($("#slice-query-yes").is(":checked")) {
			$("#billing-section").parents("fieldset").children(".next").prop("disabled", false);
			console.log("Radio sliceQueryChoice - #slice-query-yes: CHECKED - Next: ENABLED");
		}

		if ($("#slice-query-buy").is(":checked")) {
			$("#billing-section").parents("fieldset").children(".next").prop("disabled", false);
			console.log("Radio sliceQueryChoice - #slice-query-buy: CHECKED - Next: ENABLED");
		}

		if ($("#funding-query-no-or-do-not-know").is(":checked")) {
			$("#billing-section").parents("fieldset").children(".next").prop("disabled", false);
			console.log("Radio fundingQueryChoice - #funding-query-no-or-do-not-know: CHECKED - Next: ENABLED");

		}

		if ($('#budget-authoriser').val().trim() !== '' &&
			$('#schoolOrUnit').val().trim() !== '' &&
			$('#projectTitle').val().trim() !== '') {
			$("#billing-section").parents("fieldset").children(".next").prop("disabled", false);
			console.log("Budget Authoriser : PRESENT - Next: ENABLED");
		}

	}

	// Start - Call on load only
	$('#feewaiver-query-box').collapse('show');
	$('#slice-query-box').collapse('hide');
	$('#funding-query-box').collapse('hide');
	$('#payment-details-form').collapse('hide');

	if ($('#feewaiver-query-yes').is(":checked")) {
		$('#slice-query-box').collapse('hide');
		$('#funding-query-box').collapse('hide');
		$('#payment-details-form').collapse('hide');
	}

	if ($('#feewaiver-query-no-or-do-not-know').is(":checked")) {
		$('#slice-query-box').collapse('show');
	}

	if ($("#slice-query-yes").is(":checked")) {
		$('#slice-form').collapse('show');
	}

	if ($("#slice-query-no-or-do-not-know").is(":checked")) {
		$('#funding-query-box').collapse('show');
	}

	if ($('#funding-query-yes').is(":checked")) {
		$('#payment-details-form').collapse('show');
	}

	if ($('#funding-query-no-or-do-not-know').is(":checked")) {
		$('#payment-details-form').collapse('hide');
	}

	validateBillingFields();
	// End - Call on load only

	//---------------------------------------------

	$("#add-ndm-btn").click(function () {
		const firstEmptyNdm = $("#hidden-empty-ndms > div.empty-ndm").first();
		firstEmptyNdm.removeClass("empty-ndm")
			.addClass("extra-ndm");
		firstEmptyNdm.appendTo("#extra-ndm-list");
		firstEmptyNdm.show();
	});

	$(".remove-ndm-btn").click(function () {
		const currentNdm = $(this).closest('.extra-ndm');
		// Reset value of input to empty string
		currentNdm.find("input:first-child").val("");
		currentNdm.removeClass("extra-ndm")
			.addClass("empty-ndm");
		// Prepend element to hidden-empty-ndms
		currentNdm.prependTo($("#hidden-empty-ndms"));
		// Clear Error text     
		$(this).siblings(".uun-required-error-span").text('');
	});

	$("#add-depositor-btn").click(function () {
		const firstEmptyDepositor = $("#hidden-empty-depositors > div.empty-depositor").first();
		firstEmptyDepositor.removeClass("empty-depositor")
			.addClass("extra-depositor");
		firstEmptyDepositor.appendTo("#extra-depositor-list");
		firstEmptyDepositor.show();
	});

	$(".remove-depositor-btn").click(function () {
		const currentDepositor = $(this).closest('.extra-depositor');
		// Reset value of input to empty string
		currentDepositor.find("input:first-child").val("");
		currentDepositor.removeClass("extra-depositor")
			.addClass("empty-depositor");
		// Prepend element to hidden-empty-depositors
		currentDepositor.prependTo($("#hidden-empty-depositors"));
		// Clear Error text     
		$(this).siblings(".uun-required-error-span").text('');

	});

	$("#add-data-creator-btn").click(function () {
		const firstEmptyDataCreator = $("#hidden-empty-data-creators > div.empty-data-creator").first();
		firstEmptyDataCreator.removeClass("empty-data-creator")
			.addClass("extra-data-creator");
		firstEmptyDataCreator.appendTo("#extra-data-creator-list");
		firstEmptyDataCreator.show();
	});

	$(".remove-data-creator-btn").click(function () {
		const currentDataCreator = $(this).closest('.extra-data-creator');
		// Reset value of input to empty string
		currentDataCreator.find("input:first-child").val("");
		currentDataCreator.removeClass("extra-data-creator")
			.addClass("empty-data-creator");
		// Prepend element to hidden-empty-data-creators
		currentDataCreator.prependTo($("#hidden-empty-data-creators"));
		// Clear Error text     
		$(this).siblings(".uun-required-error-span").text('');

	});


	$(".next").click(function () {

		current_fs = $(this).parent();
		next_fs = $(this).parent().next();

		//Add Class Active
		$("#progressbar li").eq($("fieldset").index(next_fs)).addClass("active");
		// Add aria-* attributes to progress bar

		var currentProgressText = $("#progressbar > li.active").last().attr("data-progress-text");
		var currentProgressValue = $("#progressbar > li.active").last().attr("data-progress-value");
		console.log("currentProgressText: ", currentProgressText);
		console.log("currentProgressValue: ", currentProgressValue);
		$("#progressbar").attr("aria-valuetext", currentProgressText);
		$("#progressbar").attr("aria-valuenow", currentProgressValue);

		//show the next fieldset
		next_fs.show();
		//hide the current fieldset with style
		current_fs.animate({ opacity: 0 }, {
			step: function (now) {
				// for making fieldset appear animation
				opacity = 1 - now;

				current_fs.css({
					'display': 'none',
					'position': 'relative'
				});
				next_fs.css({ 'opacity': opacity });
			},
			duration: 600
		});

		populateSummaryPage();
	});

	$(".previous").click(function () {

		current_fs = $(this).parent();
		previous_fs = $(this).parent().prev();

		//Remove class active
		$("#progressbar li").eq($("fieldset").index(current_fs)).removeClass("active");

		//show the previous fieldset
		previous_fs.show();

		//hide the current fieldset with style
		current_fs.animate({ opacity: 0 }, {
			step: function (now) {
				// for making fieldset appear animation
				opacity = 1 - now;

				current_fs.css({
					'display': 'none',
					'position': 'relative'
				});
				previous_fs.css({ 'opacity': opacity });
			},
			duration: 600
		});
	});

	$('.radio-group .radio').click(function () {
		$(this).parent().find('.radio').removeClass('selected');
		$(this).addClass('selected');
	});

	$('button[type="submit"]').on("click", function () {
		$('#submitAction').val($(this).attr('value'));
	});

	// If PV is confirmed go to last page
	if ($('#confirmed').val() === 'true') {
		// We need to populate the summary page with data from other fieldset pages
		populateSummaryPage();
		// Set the Progress
		$("#progressbar li").eq(1).addClass("active");
		$("#progressbar li").eq(2).addClass("active");
		$("#progressbar li").eq(3).addClass("active");
		$("#progressbar li").eq(4).addClass("active");

		// Hide the previous button on Summary page and disable Pure link checkbox
		$('#summary-fieldset').find('.previous').hide();
		$('#pureLink-check').prop("disabled", true);
		// Show Summary page and hide the Affirmation page we first land on
		$('#summary-fieldset').show();
		$('#affirmation-fieldset').hide();
	}



	function populateSummaryPage() {
		console.log("populateSummaryPage()");
		// Check if an element with id="summary-fieldset" currently exists,
		// then populate the Summary page either values from form elements on previous pages
		if ($('#summary-fieldset').length) {
			$("#summary-affirmation-check").text($("#affirmation-check").is(":checked") ? "accepted" : "");

			//  To get input tag text we need to use val() not text().

			// VaultInfo
			$("#summary-vaultName").text($("#vaultName").val());
			$("#summary-description").text($("#description").val());
			$("#summary-policyID").text($("#policyInfo option:selected").text());
			var fundingChecked = ($("#funding-query-yes").is(":checked"));
			if (fundingChecked === true) {
				$("#summary-grantEndDate").text($("#billingGrantEndDate").val());
			} else {
				$("#summary-grantEndDate").text($("#grantEndDate").val());
			}
			$("#summary-groupID").text($("#groupID option:selected").text());
			$("#summary-reviewDate").text($("#reviewDate").val());
			// remove line breaks from string with replace(/(\r\n|\n|\r)/gm, "") and need to trim
			var estimateEl = $("input[name='estimate']:checked");
			if (typeof estimateEl !== 'undefined' && estimateEl !== null) {
				$("#summary-estimate").text(estimateEl.parent().text().replace(/(\r\n|\n|\r)/gm, "").trim());
			}
			$("#summary-notes").text($("#notes").val());

			// Billing
			var billingType = $("#billingType").val();
			console.log("billingType: ", billingType);
			$(".summary-slice-billing-row").hide();
			$(".summary-budget-authoriser-billing-row").hide();
			$(".summary-school-or-unit-billing-row").hide();
			$(".summary-subunit-billing-row").hide();
			$(".summary-project-title-billing-row").hide();
			$(".summary-billing-grant-end-date-billing-row").hide();
			$(".summary-payment-details-billing-row").hide();

			// Set text
			$("#summary-billing-type").text(billingType);
			$("#summary-sliceID").text($("#sliceID").val());
			$("#summary-budget-authoriser").text($("#budget-authoriser").val());
			$("#summary-school-or-unit").text($('#schoolOrUnit').val());
			$("#summary-subunit").text($('#subunit').val());
			$("#summary-project-title").text($('#projectTitle').val());
			$("#summary-billing-grant-end-date").text($("#billingGrantEndDate").val())
			$("#summary-payment-details").text($("#budget-payment-details").val());

			//Hide or show billing fields
			if (billingType === "SLICE") {
				$(".summary-slice-billing-row").show();
			} else if (billingType === "WILL_PAY") {
				$(".summary-budget-authoriser-billing-row").show();
				$(".summary-school-or-unit-billing-row").show();
				$(".summary-subunit-billing-row").show();
				$(".summary-project-title-billing-row").show();
				$(".summary-billing-grant-end-date-billing-row").show();
				$(".summary-payment-details-billing-row").show();
			}

			// Vault Users

			// Vault Access
			if ($('#isOwnerFalse').is(":checked")) {
				$("#summary-vaultOwner").text($("#vaultOwner").val());
			}

			if ($('#isOwnerTrue').is(":checked")) {
				$("#summary-vaultOwner").text($('#owner-uun-span').text());
			}

			var ndmsArray = [];
			$("input[name^='nominatedDataManagers']").each(function () {
				ndmsArray.push($(this).val());
			});
			// comma-separated text
			var ndmsHtml = createArrayHtml(ndmsArray);
			$("#summary-nominatedDataManagers").html(ndmsHtml);

			var depositorsArray = [];
			$("input[name^='depositors']").each(function () {
				depositorsArray.push($(this).val());
			});

			var depositorsHtml = createArrayHtml(depositorsArray);
			$("#summary-depositors").html(depositorsHtml);

			// Pure Information
			$("#summary-contactPerson").text($("#contactPerson").val());

			var dataCreatorsArray = [];
			$("input[name^='dataCreators']").each(function () {
				dataCreatorsArray.push($(this).val());
			});

			var dataCreatorsHtml = createArrayHtml(dataCreatorsArray);
			$("#summary-dataCreators").html(dataCreatorsHtml);
		}


		function createArrayHtml(array) {
			var html = "";
			for (var i = 0; i < array.length; i++) {
				// To only add  non-empty strings
				if (array[i].trim().length > 0) {
					html += array[i] + "<br>";
				}
			}
			return html;
		}

	}

	function calculateReviewDateForTodayAsISOString(length) {
		var today = new Date();
		var dd = String(today.getDate()).padStart(2, '0');
		var mm = String(today.getMonth() + 1).padStart(2, '0'); // January is 0
		var yyyy = String(today.getFullYear() + parseInt(length, 10));

		return yyyy + '-' + mm + '-' + dd;
	}

	function calculateReviewLength() {
		var noRP = ($("#policyInfo option:selected").val() === '' || $("#policyInfo option:selected").prop("disabled"));

		var length = 3;
		var policyInfoString = $("#policyInfo option:selected").val();
		var policyInfoArray = policyInfoString.split("-");

		if (noRP === false && policyInfoString !== '' && policyInfoArray[1] !== '') {
			var policyLength = parseInt(policyInfoArray[1], 10);
			if (policyLength > length) {
				length = policyLength;
			}
		}
		return length;
	}

	function validateOrChangeReviewDateISOString(reviewDateToCheckISOString) {
		console.log("validateOrChangeReviewDate - reviewDateToCheckISOString: ", reviewDateToCheckISOString);
		var reviewDateToCheckObject = new Date(reviewDateToCheckISOString);
		var today = new Date();
		var diffInYears = dateDiffInYears(today, reviewDateToCheckObject);
		// Default Review Date 3 years from today.
		var defaultLength = 3;
		if (diffInYears < defaultLength) {
			return calculateReviewDateForTodayAsISOString(defaultLength);
		} else {
			return reviewDateToCheckISOString;
		}
	}
});
