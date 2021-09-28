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
            <input id="vaultOwner" name="${spring.status.expression}" value="${spring.status.value!""}" type="text"  class="autocomplete" placeholder=""/>
        </div>

        <div class="form-group">
            <label class="control-label">Who are the other users who should have access to this data?</label>
        </div>

        <!-- Start: NDMs -->
        <#assign ndmCount = 0>
        <div class="form-group">
            <label class="col-sm-4 control-label">Nominated Data Managers: </label>
        </div>
        
        <#if vault.nominatedDataManagers?has_content>
        <#list vault.nominatedDataManagers as ndm>
        <#assign ndmCount = ndm?index>
           <div class="extra-ndm form-group col-sm-offset-4">
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
        <#else>
           <div class="extra-ndm form-group col-sm-offset-2">
              <@spring.bind "vault.nominatedDataManagers[0]" />
              <input name="nominatedDataManagers[0]" value="${spring.status.value!""}" class="autocomplete" type="text" />
               <button type="button" id="add-ndm-btn" class="btn btn-default btn-sm">Add a NDM</button>
            </div>
        </#if>
           
        <div id="extra-ndm-list"></div>
          
        <!-- START: NDMs hidden -->
        <div id="hidden-empty-ndms" class="collapse col-sm-offset-4">
          <#list 1..15 as i>
          <#assign ndmCount++>
            <div class="empty-ndm form-group col-sm-offset-4 my-1">
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
          <label class="col-sm-4 control-label">Depositors: </label>
        </div>
        
        <#if vault.depositors?has_content>
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
        <#else>
          <div class="extra-depositor form-group col-sm-offset-4">
              <@spring.bind "vault.depositors[0]" />
              <input name="depositors[0]" value="${spring.status.value!""}" class="autocomplete" type="text" />
              <button type="button" id="add-depositor-btn" class="btn btn-default btn-sm">Add a depositor</button>
          </div>
        </#if>
           
        <div id="extra-depositor-list"></div>
          
        <!-- START: depositors hidden -->
        <div id="hidden-empty-depositors" class="collapse col-sm-offset-4">
          <#list 1..15 as i>
          <#assign depositorCount++>
            <div class="empty-depositor form-group col-sm-offset-4 my-1">
               <@spring.bind "vault.depositors[${depositorCount}]" />
               <input name="depositors[${depositorCount}]"  value="${spring.status.value!""}" class="autocomplete" type="text"  placeholder=""/>
               <button type="button" class="remove-depositor-btn btn btn-danger btn-xs">Remove</button>
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
            <input id="contactPerson" name="${spring.status.expression}" value="${spring.status.value!""}" class="autocomplete" type="text" placeholder=""/>
        </div>

        <!-- Start: dataCreators -->
        <#assign dataCreatorCount = 0>
        <div class="form-group">
          <label class="col-sm-4 control-label">Data Creators: </label>
        </div>
        
        <#if vault.dataCreators?has_content>
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
        <#else>
          <div class="extra-data-creator form-group col-sm-offset-4">
              <@spring.bind "vault.dataCreators[0]" />
              <input name="dataCreators[0]" value="${spring.status.value!""}" class="autocomplete" type="text" />
              <button type="button" id="add-data-creator-btn" class="btn btn-default btn-sm">Add a Data Creator</button>
          </div>
        </#if>
           
        <div id="extra-data-creator-list"></div>
          
        <!-- START: dataCreators hidden -->
        <div id="hidden-empty-data-creators" class="collapse col-sm-offset-4">
          <#list 1..15 as i>
          <#assign dataCreatorCount++>
            <div class="empty-data-creator form-group col-sm-offset-4 my-1">
               <@spring.bind "vault.dataCreators[${dataCreatorCount}]" />
               <input name="dataCreators[${dataCreatorCount}]"  value="${spring.status.value!""}" class="autocomplete" type="text"  placeholder=""/>
               <button type="button" class="remove-data-creator-btn btn btn-danger btn-xs">Remove</button>
            </div>
          </#list>
        </div>
        <!-- END: dataCreators hidden -->
        <!-- END: dataCreators -->
        
        <div class="well">
            <div class="alert alert-info">
                <strong>We recommend you review the Pure metadata record for accuracy and add a description, abstract and links to research outputs and other projects/ people.”</strong>
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