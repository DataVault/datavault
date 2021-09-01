
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
    }).trigger('change');  ;

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

    $("#add-depositor-btn").click(function(){
        $(".example-depositor").clone(true).appendTo("#extra-depositor-list")
            .removeClass("example-depositor")
            .removeClass("hidden")
            .addClass("extra-depositor").show();
    });

    $(".remove-depositor-btn").click(function(){
        const current_depositor = $(this).closest('.extra-depositor');
        current_depositor.remove();
    });

    $("#add-ndm-btn").click(function(){
        $(".example-ndm").clone(true).appendTo("#extra-ndm-list")
            .removeClass("example-ndm")
            .removeClass("hidden")
            .addClass("extra-ndm").show();
    });

    $(".remove-ndm-btn").click(function(){
        const current_depositor = $(this).closest('.extra-ndm');
        current_depositor.remove();
    });

    $("#add-creator-btn").click(function(){
        $(".example-creator").clone(true).appendTo("#extra-creator-list")
            .removeClass("example-creator")
            .removeClass("hidden")
            .addClass("extra-creator").show();
    });

    $(".remove-creator-btn").click(function(){
        const current_creator = $(this).closest('.extra-creator');
        current_creator.remove();
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

});
