
$(document).ready(function(){
    var current_fs, next_fs, previous_fs;
    var opacity;

    $.datepicker.setDefaults({
        dateFormat: "yy-mm-dd",
        changeMonth: true,
        changeYear: true,
        showOtherMonths: true,
        selectOtherMonths: true
    });

    $( "#grantEndDate" ).datepicker();
    $( "#reviewDate" ).datepicker({
        minDate: '+1m'
    });

    $("#affirmation-check").change(function(){
        $(this).parents("fieldset").children(".next").prop( "disabled", !$(this).is(":checked") );
    }).trigger('change');

    $("#vaultName, #description, #policyID, #groupID, #reviewDate").change(function(){
        // if all the mandatory values in the info field set are non null or empty set the next button
        // display value to true
        // name
        var nameResult = ($( "input[type=text][id=vaultName]").val().trim() === '');
        // description
        var descResult = ($( "textarea[type=text][id=description]").val() === '');
        // retention policy
        var rpResult = ($("#policyID option:selected").val() === '' || $("#policyID option:selected").prop("disabled"));
        // school
        var schoolResult = ($("#groupID option:selected").val() === '' || $("#groupID option:selected").prop("disabled"));
        // review date
        var reviewResult = ($( "input[id=reviewDate]").val().trim() === '');
        if (nameResult === false && descResult === false && rpResult === false && schoolResult === false  && reviewResult === false) {
            $(this).parents("fieldset").children(".next").prop("disabled", false);
        } else {
            $(this).parents("fieldset").children(".next").prop("disabled", true);
        }
    }).trigger('change');  ;

    $("#pureLink-check").change(function(){
        var result = $(this).is(":checked");
        if (result) {
            $("#confirm").prop("disabled", false);
        } else {
            $("#confirm").prop("disabled", true);
        }
    }).trigger('change');

    $("#isOwnerTrue, #isOwnerFalse, #vaultOwner").change(function(){
        //var ownerTrueResult = $( "input[type=radio][id=isOwnerTrue]").is(":checked");
        var ownerFalseResult = $( "input[type=radio][id=isOwnerFalse]").is(":checked");
        var uunResult = ($( "input[type=text][id=vaultOwner]").val().trim() === '');

        if (ownerFalseResult && uunResult) {
            $(this).parents("fieldset").children(".next").prop("disabled", true);
        } else {
            $(this).parents("fieldset").children(".next").prop("disabled", false);
        }
    }).trigger('change');

    $("#billing-choice-na").change(function(){
        $('.collapse').collapse('hide');
        $(this).parents("fieldset").children(".next").prop( "disabled", false );
    }).trigger('change');  ;

    $("#billing-choice-grantfunding").change(function(){
        if($(this).is(":checked")){
            $('.collapse').not('#billing-form').collapse('hide');
            $('#billing-form').collapse('show');
            $(this).parents("fieldset").children(".next").prop( "disabled", false );
        }
    }).trigger('change');  ;

    $("#billing-choice-budgetcode").change(function(){
        if($(this).is(":checked")) {
            $('.collapse').not('#billing-form').collapse('hide');
            $('#billing-form').collapse('show');
            $(this).parents("fieldset").children(".next").prop( "disabled", false );
        }
    }).trigger('change');  ;

    $("#billing-choice-slice").change(function(){
        if($(this).is(":checked")) {
            $('.collapse').not('#slice-form').collapse('hide');
            $('#slice-form').collapse('show');
            $(this).parents("fieldset").children(".next").prop( "disabled", false );
        }
    });

    $("input[name='isOwner']").change(function(){
        if(this.value === 'true'){
            $('#owner-uun').prop('disabled', true);
        } else {
            $('#owner-uun').prop('disabled', false);
        }
    }).trigger('change');  


    $("#add-ndm-btn").click(function(){
        const firstEmptyNdm = $("#hidden-empty-ndms > div.empty-ndm").first();
        firstEmptyNdm.removeClass("empty-ndm")
                     .addClass("extra-ndm");
        firstEmptyNdm.appendTo("#extra-ndm-list");
        firstEmptyNdm.show();
    });

    $(".remove-ndm-btn").click(function(){
        const currentNdm = $(this).closest('.extra-ndm');
        // Reset value of input to empty string
        currentNdm.find("input:first-child").val("");
        currentNdm.removeClass("extra-ndm")
                   .addClass("empty-ndm");
        // Prepend element to hidden-empty-ndms
        currentNdm.prependTo($("#hidden-empty-ndms"));
       // Clear Error text     
       $(this).siblings(".uun-required-error-span").text("");
    });
    
    $("#add-depositor-btn").click(function(){
        const firstEmptyDepositor = $("#hidden-empty-depositors > div.empty-depositor").first();
        firstEmptyDepositor.removeClass("empty-depositor")
                     .addClass("extra-depositor");
        firstEmptyDepositor.appendTo("#extra-depositor-list");
        firstEmptyDepositor.show();
    });

    $(".remove-depositor-btn").click(function(){
        const currentDepositor = $(this).closest('.extra-depositor');
        // Reset value of input to empty string
        currentDepositor.find("input:first-child").val("");
        currentDepositor.removeClass("extra-depositor")
                   .addClass("empty-depositor");
        // Prepend element to hidden-empty-depositors
        currentDepositor.prependTo($("#hidden-empty-depositors"));
        // Clear Error text     
        $(this).siblings(".uun-required-error-span").text("");
        
    });

    $("#add-data-creator-btn").click(function(){
        const firstEmptyDataCreator = $("#hidden-empty-data-creators > div.empty-data-creator").first();
        firstEmptyDataCreator.removeClass("empty-data-creator")
                     .addClass("extra-data-creator");
        firstEmptyDataCreator.appendTo("#extra-data-creator-list");
        firstEmptyDataCreator.show();
    });

    $(".remove-data-creator-btn").click(function(){
        const currentDataCreator = $(this).closest('.extra-data-creator');
        // Reset value of input to empty string
        currentDataCreator.find("input:first-child").val("");
        currentDataCreator.removeClass("extra-data-creator")
                   .addClass("empty-data-creator");
        // Prepend element to hidden-empty-data-creators
        currentDataCreator.prependTo($("#hidden-empty-data-creators"));
        // Clear Error text     
        $(this).siblings(".uun-required-error-span").text("");
        
    });
   

    $(".next").click(function(){

        current_fs = $(this).parent();
        next_fs = $(this).parent().next();

        //Add Class Active
        $("#progressbar li").eq($("fieldset").index(next_fs)).addClass("active");

        //show the next fieldset
        next_fs.show();
        //hide the current fieldset with style
        current_fs.animate({opacity: 0}, {
            step: function(now) {
                // for making fielset appear animation
                opacity = 1 - now;

                current_fs.css({
                    'display': 'none',
                    'position': 'relative'
                });
                next_fs.css({'opacity': opacity});
            },
            duration: 600
        });
        
        populateSummaryPage();
    });

    $(".previous").click(function(){

        current_fs = $(this).parent();
        previous_fs = $(this).parent().prev();

        //Remove class active
        $("#progressbar li").eq($("fieldset").index(current_fs)).removeClass("active");

        //show the previous fieldset
        previous_fs.show();

        //hide the current fieldset with style
        current_fs.animate({opacity: 0}, {
            step: function(now) {
                // for making fielset appear animation
                opacity = 1 - now;

                current_fs.css({
                    'display': 'none',
                    'position': 'relative'
                });
                previous_fs.css({'opacity': opacity});
            },
            duration: 600
        });
    });

    $('.radio-group .radio').click(function(){
        $(this).parent().find('.radio').removeClass('selected');
        $(this).addClass('selected');
    });

    //$(".submit").click(function(){
    //    return false;
    //})

    $('button[type="submit"]').on("click", function() {
        $('#submitAction').val($(this).attr('value'));
    });

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
    	  $("#summary-policyID").text($("#policyID option:selected").text());
    	  $("#summary-grantEndDate").text($("#grantEndDate").val());
          $("#summary-groupID").text($("#groupID option:selected").text());
          $("#summary-reviewDate").text($("#reviewDate").val());
          // remove line breaks from string with replace(/(\r\n|\n|\r)/gm, "") and need to trim
          var estimateEl = $("input[name='estimate']:checked");
          if(typeof estimateEl !== 'undefined' && estimateEl !== null) {
        	  $("#summary-estimate").text(estimateEl.parent().text().replace(/(\r\n|\n|\r)/gm, "").trim());
          }
          $("#summary-notes").text($("#notes").val());
          
          // Billing
          var billingTypeEl = $("input[id^='billing-choice']:checked");
          if(typeof billingTypeEl !== 'undefined' && billingTypeEl !== null) {
        	  var billingType = billingTypeEl.parent().text().replace(/(\r\n|\n|\r)/gm, "").trim();
        	  // Hack to ensure text after "[" is not added
        	  if(billingType.indexOf("[") > -1) {
        		  billingType = billingType.split("[")[0];
        	  }
        	  $("#summary-billing-type").text(billingType);
        	  //Hide or show billing fields
        	  if(billingType.startsWith("N/A")) {
        		  $(".summary-grant-or_budget-billing-row").hide();
        		  $(".summary-slice-billing-row").hide();
        	  } else if(billingType.startsWith("A Slice")) {
        		  $(".summary-grant-or_budget-billing-row").hide();
        		  $(".summary-slice-billing-row").show();
        	  } else {
        		  $(".summary-slice-billing-row").hide();
        		  $(".summary-grant-or_budget-billing-row").show();
        	  }
          }
          $("#summary-sliceID").text($("#sliceID").val());
          $("#summary-authoriser").text($("#authoriser").val());
          $("#summary-schoolOrUnit").text($("#schoolOrUnit").val());
          $("#summary-subunit").text($("#subunit").val());
          $("#summary-projectID").text($("#projectID").val());
          
          // Vault Users
          
          // Vault Access
          $("#summary-vaultOwner").text($("#vaultOwner").val());
          
          var ndmsArray = [];
          $("input[name^='nominatedDataManagers']").each(function() {
        	  ndmsArray.push($(this).val());
          });
          // comma-separated text
          var ndmsHtml = createArrayHtml(ndmsArray);
          $("#summary-nominatedDataManagers").html(ndmsHtml);
          
          var depositorsArray = [];
          $("input[name^='depositors']").each(function() {
        	  depositorsArray.push($(this).val());
          });
          
          var depositorsHtml = createArrayHtml(depositorsArray);
          $("#summary-depositors").html(depositorsHtml);
          
          // Pure Information
          $("#summary-contactPerson").text($("#contactPerson").val());
          
          var dataCreatorsArray = [];
          $("input[name^='dataCreators']").each(function() {
        	  dataCreatorsArray.push($(this).val());
          });
          
          var dataCreatorsHtml = createArrayHtml(dataCreatorsArray);
          $("#summary-dataCreators").html(dataCreatorsHtml);
    	}
    	
    	
    	function createArrayHtml(array) {
    		var html = "";
    		for (var i = 0; i < array.length; i++) {
    			// To only add  non-empty strings
    			if(array[i].trim().length > 0) {
    			  html += array[i] + "<br>";
    			}
    	    } 
    		return html;
    	}
    	
    }
});
