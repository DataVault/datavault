<fieldset>
    <div id="add-role-vault-dialog" class="form-card">
        <h2 class="fs-title text-center">Vault Users</h2> <br><br>
        <div id="autocomplete-error" class="alert alert-danger hidden error" role="alert"></div>
        <h4>Vault Access</h4>

        <div class="form-group" required>
            <label class="control-label">Are you the owner of the vault:</label>
            <@spring.bind "vault.isOwner" />
            <div class="radio-inline">
                <label>
                    <input type="radio" id="isOwnerTrue" name="${spring.status.expression}" value="true" <#if vault.isOwner??>${(vault.isOwner)?then('checked', '')}</#if>> Yes
                </label>
            </div>
            <div class="radio-inline">
                <label>
                    <input type="radio" id="isOwnerFalse" name="${spring.status.expression}" value="false" <#if vault.isOwner??>${(!vault.isOwner)?then('checked', '')}</#if>> No
                </label>
            </div>
        </div>
        <div class="form-group" required>
            <label class="col-sm-4 control-label">Owner UUN: </label>
            <@spring.bind "vault.vaultOwner" />
            <input id="vaultOwner" name="${spring.status.expression}" value="${spring.status.value!""}" type="text"
                   class="autocomplete uun-required unique-uun-required owner-uun-required" placeholder=""/>
            <span class="uun-required-error-span owner-uun-required-error-span"></span>
        </div>

        <div class="form-group">
            <label class="control-label">Who are the other users who should have access to this data?</label>
        </div>

        <!-- Start: NDMs -->
        <#assign ndmCount = 0>
        <div class="form-group">
            <label class="col-sm-4 control-label">Nominated Data Managers: 
                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                      title="Nominated Data Manager: Acting on behalf of the Data Owner, may view the vault, edit metadata fields, deposit data and retrieve any deposit in the vault. Can assign other users to the Depositor role.">
                </span> 
            </label>
        </div>
        
        <#if vault.nominatedDataManagers?has_content>
        <#list vault.nominatedDataManagers as ndm>
        <#assign ndmCount = ndm?index>
           <div class="extra-ndm form-group col-sm-offset-4">
              <@spring.bind "vault.nominatedDataManagers[${ndmCount}]" />
              <input name="nominatedDataManagers[${ndmCount}]" value="${spring.status.value!""}" class="autocomplete uun-required unique-uun-required" type="text" />
              <!-- Add  remove button for inputs apart from first for which we add an add button. -->
              <#if (ndmCount == 0)>
                <button type="button" id="add-ndm-btn" class="btn btn-default btn-sm">Add a NDM</button>
              <#else>
                 <button type="button" class="remove-ndm-btn btn btn-danger btn-xs">Remove</button>
              </#if>
              <span class="uun-required-error-span"></span>
            </div>
        </#list>
        <#else>
           <div class="extra-ndm form-group col-sm-offset-4">
              <@spring.bind "vault.nominatedDataManagers[0]" />
              <input name="nominatedDataManagers[0]" value="${spring.status.value!""}" class="autocomplete uun-required unique-uun-required" type="text" />
               <button type="button" id="add-ndm-btn" class="btn btn-default btn-sm">Add a NDM</button>
               <span class="uun-required-error-span"></span>
            </div>
        </#if>
           
        <div id="extra-ndm-list"></div>
          
        <!-- START: NDMs hidden -->
        <div id="hidden-empty-ndms" class="collapse col-sm-offset-4">
          <#list 1..15 as i>
          <#assign ndmCount++>
            <div class="empty-ndm form-group col-sm-offset-4 my-1">
               <@spring.bind "vault.nominatedDataManagers[${ndmCount}]" />
               <input name="nominatedDataManagers[${ndmCount}]"  value="${spring.status.value!""}" class="autocomplete uun-required unique-uun-required" type="text"  placeholder=""/>
               <button type="button" class="remove-ndm-btn btn btn-danger btn-xs">Remove</button>
               <span class="uun-required-error-span"></span>
            </div>
          </#list>
        </div>
        <!-- END: NDMs hidden -->
       <!-- END: NDMs -->
        
        
       <!-- Start: Depositors -->
        <#assign depositorCount = 0>
        <div class="form-group">
          <label class="col-sm-4 control-label">Depositors: 
              <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                    title="Depositor: Acting on behalf of the Data Owner, may view the vault, deposit data and retrieve any deposit in the vault.">
              </span>
          </label>
        </div>
        
        <#if vault.depositors?has_content>
        <#list vault.depositors as depositor>
        <#assign depositorCount = depositor?index>
           <div class="extra-depositor form-group col-sm-offset-4">
              <@spring.bind "vault.depositors[${depositorCount}]" />
              <input name="depositors[${depositorCount}]" value="${spring.status.value!""}" class="autocomplete uun-required unique-uun-required" type="text" />
              <!-- Add  remove button for inputs apart from first for which we add an add button. -->
              <#if (depositorCount == 0)>
                <button type="button" id="add-depositor-btn" class="btn btn-default btn-sm">Add a depositor</button>
              <#else>
                 <button type="button" class="remove-depositor-btn btn btn-danger btn-xs">Remove</button>
              </#if>
              <span class="uun-required-error-span"></span>
            </div>
        </#list>
        <#else>
          <div class="extra-depositor form-group col-sm-offset-4">
              <@spring.bind "vault.depositors[0]" />
              <input name="depositors[0]" value="${spring.status.value!""}" class="autocomplete uun-required unique-uun-required" type="text" />
              <button type="button" id="add-depositor-btn" class="btn btn-default btn-sm">Add a depositor</button>
              <span class="uun-required-error-span"></span>
          </div>
        </#if>
           
        <div id="extra-depositor-list"></div>
          
        <!-- START: depositors hidden -->
        <div id="hidden-empty-depositors" class="collapse col-sm-offset-4">
          <#list 1..15 as i>
          <#assign depositorCount++>
            <div class="empty-depositor form-group col-sm-offset-4 my-1">
               <@spring.bind "vault.depositors[${depositorCount}]" />
               <input name="depositors[${depositorCount}]"  value="${spring.status.value!""}" class="autocomplete uun-required unique-uun-required" type="text"  placeholder=""/>
               <button type="button" class="remove-depositor-btn btn btn-danger btn-xs">Remove</button>
               <span class="uun-required-error-span"></span>
            </div>
          </#list>
        </div>
        <!-- END: Depositors hidden -->
        <!-- END: Depositors -->

        <h4>Pure Information<span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="We will create a Pure record, publicly visible on the Research Explorer, describing this Vault. Please add here the names you wish to be included on that record.">
        </span></h4>


        <div id="contact-form-group" class="form-group">
            <label for="contactPerson" class="col-sm-4 control-label">Contact person: </label>
            <@spring.bind "vault.contactPerson" />
            <input id="contactPerson" name="${spring.status.expression}" value="${spring.status.value!""}" class="autocomplete uun-required" type="text" placeholder=""/>
            <span class="uun-required-error-span"></span>
        </div>

        <!-- Start: dataCreators -->
        <#assign dataCreatorCount = 0>
        <div class="form-group">
          <label class="col-sm-4 control-label">Data Creators: </label>
        </div>
        
        <#if vault.dataCreators?has_content>
        <#list vault.dataCreators as dataCreator>
        <#assign dataCreatorCount = dataCreator?index>
           <div class="extra-data-creator form-group col-sm-offset-4">
              <@spring.bind "vault.dataCreators[${dataCreatorCount}]" />
              <input name="dataCreators[${dataCreatorCount}]" value="${spring.status.value!""}" class="autocomplete uun-required" type="text" />
              <!-- Add  remove button for inputs apart from first for which we add an add button. -->
              <#if (dataCreatorCount == 0)>
                <button type="button" id="add-data-creator-btn" class="btn btn-default btn-sm">Add a Data Creator</button>
              <#else>
                 <button type="button" class="remove-data-creator-btn btn btn-danger btn-xs">Remove</button>
              </#if>
              <span class="uun-required-error-span"></span>
            </div>
        </#list>
        <#else>
          <div class="extra-data-creator form-group col-sm-offset-4">
              <@spring.bind "vault.dataCreators[0]" />
              <input name="dataCreators[0]" value="${spring.status.value!""}" class="autocomplete uun-required" type="text" />
              <button type="button" id="add-data-creator-btn" class="btn btn-default btn-sm">Add a Data Creator</button>
              <span class="uun-required-error-span"></span>
          </div>
        </#if>
           
        <div id="extra-data-creator-list"></div>
          
        <!-- START: dataCreators hidden -->
        <div id="hidden-empty-data-creators" class="collapse col-sm-offset-4">
          <#list 1..15 as i>
          <#assign dataCreatorCount++>
            <div class="empty-data-creator form-group col-sm-offset-4 my-1">
               <@spring.bind "vault.dataCreators[${dataCreatorCount}]" />
               <input name="dataCreators[${dataCreatorCount}]"  value="${spring.status.value!""}" class="autocomplete uun-required" type="text"  placeholder=""/>
               <button type="button" class="remove-data-creator-btn btn btn-danger btn-xs">Remove</button>
               <span class="uun-required-error-span"></span>
            </div>
          </#list>
        </div>
        <!-- END: dataCreators hidden -->
        <!-- END: dataCreators -->
        
        <div class="well">
            <div class="alert alert-info">
                <strong>We recommend you review the Pure metadata record for accuracy and add a description, abstract and links to research outputs and other projects/ people.</strong>
            </div>
        </div>
    </div>
    <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
    <#if vault.confirmed?c=="false">
    <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    </#if>
    <button type="button" id="user-fields-next" name="next" class="next action-button btn btn-primary">Next Step &raquo;</button>
    <script>
    $(document).ready(function(){
        $(".autocomplete").autocomplete({
            autoFocus: true,
            appendTo: "#add-role-vault-dialog",
            minLength: 2,
            source: function (request, response) {
                var term = request.term;
                $.ajax({
                    url: ErrorHandler._springContextPath + "/vaults/autocompleteuun/" + term,
                    type: 'GET',
                    dataType: "json",
                    success: function (data) {
                        response(data);
                    },
                    error: function(xhr) {
                        ErrorHandler.handleAjaxError('#autocomplete-error', xhr);
                    }
                });
            },
            select: function (event, ui) {
                var attributes = ui.item.value.split(" - ");
                this.value = attributes[0];
                // Trigger keyup event as select will not be detected 
                // by the input element
                $(this).trigger("keyup");
                return false;
            }
        });
        
        // Hide all spans with class uun-required-span and set text red
        $(".uun-required-error-span").css({"color" : "red"});
        $(".uun-required-error-span").val("");
        $(".uun-required-error-span").hide();


        // Function that validates owner never empty
        function validateOwnerUUN() {

            if($( "input[type=text][id=vaultOwner]").val().trim() === '') {
                // if ownerUUN and isOwner is false
                // show error and disable next button (this is done via the validationErrorPresent function)
                var ownerFalseResult = $( "input[type=radio][id=isOwnerFalse]").is(":checked");
                console.log("OwnerFalseResult: ", ownerFalseResult);
                // and vault owner is empty
                if (ownerFalseResult === true) {
                    console.log("OwnerFalseResult is true and input text is empty");
                    $(".owner-uun-required-error-span").text("You have not specified any user as the Owner of this vault. Please add an owner, or contact the Research Data Support team if you want to request this vault to be treated as legacy data ie an orphan vault.");
                    $(".owner-uun-required-error-span").show();
                } else {

                    $(".owner-uun-required-error-span").text("");
                    $(".owner-uun-required-error-span").hide();
                }
                // As <span> does not have assign a 'change' event by default.
                // This event is limited to <input> elements, <textarea> boxes and <select> elements
                // We assign a 'change' event to errorSpan.
                $(".owner-uun-required-error-span").change();
            }
        }
        
        // Function that validates uun
        var currentRequest = null;
        function validateUUN(inputText,errorSpan,loggedInUUn) {
          if(inputText !== "") {
            console.log("inputText: ", inputText);
            currentRequest = $.ajax({
                    url: ErrorHandler._springContextPath + "/vaults/isuun/" + inputText,
                    type: 'GET',
                    dataType: "json",
                    beforeSend: function() {
                        if(currentRequest != null) {
                            currentRequest.abort();
                        }
                    },
                    success: function(isUUN) {
                        if(isUUN) {
                            // get owner if logged in user (id isOwnerTrue)
                            // get owner if not logged in user (id vaultOwner)

                            var count = 0;
                            // check if logged in owner
                            //var ownerUun = $( "input[type=text][id=vaultOwner]").val().trim();
                            var ownerTrueResult = $( "input[type=radio][id=isOwnerTrue]").is(":checked");
                            if (ownerTrueResult === true) {
                                //ownerUun = loggedInUUn;
                                console.log("Checking (logged in Owner) ", inputText, " v ", loggedInUUn);
                                if(loggedInUUn === inputText) {
                                    console.log(inputText, " Already has a role" );
                                    count++;
                                } else {
                                    errorSpan.text("");
                                    errorSpan.hide();
                                }
                            }

                            // foreach uuid input get uuid if not empty string
                            // check if inputText only appears once in all role fields
                            // (includes manually entered owner)
                            $(".unique-uun-required").each(function() {
                                var uun = $(this).val().trim();
                                if (uun !== '') {
                                    console.log("Checking (Role)", inputText, " v ",  uun);
                                    if(uun === inputText) {
                                        count++;
                                    } else {
                                        errorSpan.text("");
                                        errorSpan.hide();
                                    }
                                }
                            });
                            if (count > 1) {
                                console.log(inputText, " Already has a role" );
                                errorSpan.text("A user may have only one vault-level user role, either Owner, NDM or Depositor. For further information please ");
                                errorSpan.append('<a href="https://www.ed.ac.uk/information-services/research-support/research-data-service/after/datavault/roles-and-access" target="_blank">click here</a>.');
                                errorSpan.show();
                            } else {
                                errorSpan.text("");
                                errorSpan.hide();
                            }

                        } else {
                          errorSpan.text("Invalid UUN");
                          errorSpan.show();
                        }
                      // As <span> does not have assign a 'change' event by default.
                      // This event is limited to <input> elements, <textarea> boxes and <select> elements
                      // We assign a 'change' event to errorSpan.
                      errorSpan.change();  
                    },
                    error: function(xhr) {
                        //ErrorHandler.handleAjaxError('#autocomplete-error', xhr);
                    }
            });
          } else {
              errorSpan.text("");
              errorSpan.hide();
          }
          validateOwnerUUN();

        }
        
        // Validate input is uun for keyup
        // for input tags with class "uun-required"
        $(".uun-required").on("keyup", function() {
          var inputText = $(this).val().trim();
          var errorSpan =  $(this).siblings(".uun-required-error-span");
          var loggedInAS = $("#loggedInAs").val().trim()
          console.log("ON EVENT: inputText: ", inputText);
          validateUUN(inputText,errorSpan, loggedInAS);
        });
        
       // Ensure  Next button only enabled if no "Invalid UUN present 
       // on Dom change within within div with id="add-role-vault-dialog"
       $("#add-role-vault-dialog").bind('DOMSubtreeModified', function () {
         var validationErrorPresent = false;
         // loop through all spans with .uun-required-error-span
         $(".uun-required-error-span").each(function() {
              if($(this).text().trim() !== "") {
                 validationErrorPresent = true;
              }
         });

         console.log("validationErrorPresent: ", validationErrorPresent);
         // If no validation errors enable next button
         if(validationErrorPresent) {
             $('#user-fields-next').prop("disabled", true);
         } else {
           $('#user-fields-next').prop("disabled", false);
         }
       });
   
      });  
    </script>
</fieldset>