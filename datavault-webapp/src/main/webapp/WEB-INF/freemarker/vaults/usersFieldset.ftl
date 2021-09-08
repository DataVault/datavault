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
            <label class="col-sm-2 control-label">Owner UUN: </label>
            <@spring.bind "vault.vaultOwner" />
            <input id="vaultOwner" name="${spring.status.expression}" value="${spring.status.value!""}" type="text"  class="autocomplete" placeholder=""/>
        </div>

        <!-- Start: NDMs -->
        <#assign ndmCount = 0>
        <div class="form-group">
          <label class="col-sm-2 control-label">NDMs: </label>
        </div>
        
        <#list vault.nominatedDataManagers as ndm>
        <#assign ndmCount = ndm?index>
           <div class="extra-ndm form-group col-sm-offset-2">
              <@spring.bind "vault.nominatedDataManagers[${ndmCount}]" />
              <input name="nominatedDataManagers[${ndmCount}]" value="${spring.status.value!""}" class="autocomplete" type="text" />
              <!-- Add  remove button for inputs apart from first for which we add an add button. -->
              <#if (ndmCount == 0)>
                <button type="button" id="add-ndm-btn" class="btn btn-default btn-sm">Add a NDM</button>
              <#else>
                 <button type="button" class="remove-ndm-btn btn btn-danger btn-xs">Remove</button>
              </#if>
            </div>
        </#list>
           
        <div id="extra-ndm-list"></div>
          
        <!-- START: NDMs hidden -->
        <div id="hidden-empty-ndms" class="collapse col-sm-offset-2">
          <#list 1..15 as i>
          <#assign ndmCount++>
            <div class="empty-ndm form-group col-sm-offset-2 my-1">
               <@spring.bind "vault.nominatedDataManagers[${ndmCount}]" />
               <input name="nominatedDataManagers[${ndmCount}]"  value="${spring.status.value!""}" class="autocomplete" type="text"  placeholder=""/>
               <button type="button" class="remove-ndm-btn btn btn-danger btn-xs">Remove</button>
            </div>
          </#list>
        </div>
        <!-- END: NDMs hidden -->
       <!-- END: NDMs -->
        
        
       <!-- Start: Depositors -->
        <#assign depositorCount = 0>
        <div class="form-group">
          <label class="col-sm-2 control-label">Depositors: </label>
        </div>
        
        <#list vault.depositors as depositor>
        <#assign depositorCount = depositor?index>
           <div class="extra-depositor form-group col-sm-offset-2">
              <@spring.bind "vault.depositors[${depositorCount}]" />
              <input name="depositors[${depositorCount}]" value="${spring.status.value!""}" class="autocomplete" type="text" />
              <!-- Add  remove button for inputs apart from first for which we add an add button. -->
              <#if (depositorCount == 0)>
                <button type="button" id="add-depositor-btn" class="btn btn-default btn-sm">Add a depositor</button>
              <#else>
                 <button type="button" class="remove-depositor-btn btn btn-danger btn-xs">Remove</button>
              </#if>
            </div>
        </#list>
           
        <div id="extra-depositor-list"></div>
          
        <!-- START: depositors hidden -->
        <div id="hidden-empty-depositors" class="collapse col-sm-offset-2">
          <#list 1..15 as i>
          <#assign depositorCount++>
            <div class="empty-depositor form-group col-sm-offset-2 my-1">
               <@spring.bind "vault.depositors[${depositorCount}]" />
               <input name="depositors[${depositorCount}]"  value="${spring.status.value!""}" class="autocomplete" type="text"  placeholder=""/>
               <button type="button" class="remove-depositor-btn btn btn-danger btn-xs">Remove</button>
            </div>
          </#list>
        </div>
        <!-- END: Depositors hidden -->
        <!-- END: Depositors -->

        <h4>Pure Information</h4>

        <div id="contact-form-group" class="form-group">
            <label for="contactPerson" class="col-sm-2 control-label">Contact person: </label>
            <@spring.bind "vault.contactPerson" />
            <input id="contactPerson" name="${spring.status.expression}" value="${spring.status.value!""}" class="autocomplete" type="text" placeholder=""/>
        </div>

        <!-- Start: dataCreators -->
        <#assign dataCreatorCount = 0>
        <div class="form-group">
          <label class="col-sm-2 control-label">Data Creators: </label>
        </div>
        
        <#list vault.dataCreators as dataCreator>
        <#assign dataCreatorCount = dataCreator?index>
           <div class="extra-data-creator form-group col-sm-offset-2">
              <@spring.bind "vault.dataCreators[${dataCreatorCount}]" />
              <input name="dataCreators[${dataCreatorCount}]" value="${spring.status.value!""}" class="autocomplete" type="text" />
              <!-- Add  remove button for inputs apart from first for which we add an add button. -->
              <#if (dataCreatorCount == 0)>
                <button type="button" id="add-data-creator-btn" class="btn btn-default btn-sm">Add a Data Creator</button>
              <#else>
                 <button type="button" class="remove-data-creator-btn btn btn-danger btn-xs">Remove</button>
              </#if>
            </div>
        </#list>
           
        <div id="extra-data-creator-list"></div>
          
        <!-- START: dataCreators hidden -->
        <div id="hidden-empty-data-creators" class="collapse col-sm-offset-2">
          <#list 1..15 as i>
          <#assign dataCreatorCount++>
            <div class="empty-data-creator form-group col-sm-offset-2 my-1">
               <@spring.bind "vault.dataCreators[${dataCreatorCount}]" />
               <input name="dataCreators[${dataCreatorCount}]"  value="${spring.status.value!""}" class="autocomplete" type="text"  placeholder=""/>
               <button type="button" class="remove-data-creator-btn btn btn-danger btn-xs">Remove</button>
            </div>
          </#list>
        </div>
        <!-- END: dataCreators hidden -->
        <!-- END: dataCreators -->
        
        <div class="well">
            A statement that encourages them to go to Pure later,
            saying they can go to Pure to edit the dataset record
            we will create automatically to describe this vault,
            so they can link the dataset record to their papers and multiple projects.
            <div class="alert alert-info">
                <strong>We recommend you review the Pure metadata record for accuracy and add links to research outputs and other projects/ people.</strong>
            </div>
        </div>
    </div>
    <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
    <#if vault.confirmed?c=="false">
    <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    </#if>
    <button type="button" name="next" class="next action-button btn btn-primary">Next Step &raquo;</button>
    <script>
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
                return false;
            }
        });
    </script>
</fieldset>