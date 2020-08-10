<fieldset>
    <div class="form-card">
        <h2 class="fs-title text-center">Vault Users</h2> <br><br>

        <h4>Vault Access</h4>

        <div class="form-group" required>
            <label class="control-label">Are you the owner of the vault:</label>
            <div class="radio-inline">
                <label>
                    <input type="radio" name="isOwner" value="true"> Yes
                </label>
            </div>
            <div class="radio-inline">
                <label>
                    <input type="radio" name="isOwner" value="false" checked> No
                </label>
            </div>
        </div>
        <div class="form-group" required>
            <label class="col-sm-2 control-label">Owner UUN: </label>
            <input id="owner-uun" type="text"  placeholder="autofilled uun with ldap"/>
        </div>

        <div class="form-group">
            <label class="col-sm-2 control-label">NDMs: </label>
            <input type="text" placeholder="autofilled uun with ldap" />
            <button type="button" id="add-ndm-btn" class="btn btn-default btn-sm">Add a NDM</button>
            <div id="extra-ndm-list"></div>
            <div class="example-ndm hidden col-sm-offset-2">
                <input name="ndm-uun" class="ndm" type="text"  placeholder="autofilled uun with ldap"/>
                <button type="button" class="remove-ndm-btn btn btn-danger btn-xs">Remove</button>
            </div>
        </div>

        <div id="depositors-form-group" class="form-group">
            <label class="col-sm-2 control-label">Depositors: </label>
            <input name="depositor-uun" class="depositor" type="text" placeholder="autofilled uun with ldap"/>
            <button type="button" id="add-depositor-btn" class="btn btn-default btn-sm">Add a Depositor</button>
            <div id="extra-depositor-list"></div>
            <div class="example-depositor hidden col-sm-offset-2">
                <input name="depositor-uun" class="depositor" type="text"  placeholder="autofilled uun with ldap"/>
                <button type="button" class="remove-depositor-btn btn btn-danger btn-xs">Remove</button>
            </div>
        </div>

        <h4>Pure Information</h4>

        <div id="contact-form-group" class="form-group">
            <label class="col-sm-2 control-label">Contact person: </label>
            <input name="contact-uun" class="contact" type="text" placeholder="autofilled uun with ldap"/>
        </div>

        <div id="creators-form-group" class="form-group">
            <label class="col-sm-2 control-label">Data Creator: </label>
            <input name="creator-uun" class="creator" type="text" placeholder="autofilled uun with ldap"/>
            <button type="button" id="add-creator-btn" class="btn btn-default btn-sm">Add a Data Creator</button>
            <div id="extra-creator-list"></div>
            <div class="example-creator hidden col-sm-offset-2">
                <input name="creator-uun" class="creator" type="text"  placeholder="autofilled uun with ldap"/>
                <button type="button" class="remove-creator-btn btn btn-danger btn-xs">Remove</button>
            </div>
        </div>

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
    <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    <button type="button" name="next" class="next action-button btn btn-primary">Next Step &raquo;</button>
</fieldset>